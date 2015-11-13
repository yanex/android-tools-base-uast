package com.android.tools.fd.runtime;

import android.app.Activity;
import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ContextThemeWrapper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.android.tools.fd.runtime.BootstrapApplication.LOG_TAG;

//
/**
 * A utility class which uses reflection hacks to replace the application instance and
 * the resource data for the current app.
 * This is based on the reflection parts of
 *     com.google.devtools.build.android.incrementaldeployment.StubApplication,
 * plus changes to compile on JDK 6.
 * <p>
 * The original is
 * https://github.com/google/bazel/blob/master/src/tools/android/java/com/google/devtools/build/android/incrementaldeployment/StubApplication.java
 * (May 11 revision, ca96e11)
 * <p>
 * (The code to handle resource loading etc is different; see FileManager.)
 * Furthermore, the resource patching was hacked on some more such that it can
 * handle live (activity-restart) changes, which allows us to for example patch
 * the theme and have existing activities have their themes updated!
 * <p>
 * Original comment for the StubApplication, which contained the reflection methods:
 * <p>
 * A stub application that patches the class loader, then replaces itself with the real application
 * by applying a liberal amount of reflection on Android internals.
 * <p/>
 * <p>This is, of course, terribly error-prone. Most of this code was tested with API versions
 * 8, 10, 14, 15, 16, 17, 18, 19 and 21 on the Android emulator, a Nexus 5 running Lollipop LRX22C
 * and a Samsung GT-I5800 running Froyo XWJPE. The exception is {@code monkeyPatchAssetManagers},
 * which only works on Kitkat and Lollipop.
 * <p/>
 * <p>Note that due to a bug in Dalvik, this only works on Kitkat if ART is the Java runtime.
 * <p/>
 * <p>Unfortunately, if this does not work, we don't have a fallback mechanism: as soon as we
 * build the APK with this class as the Application, we are committed to going through with it.
 * <p/>
 * <p>This class should use as few other classes as possible before the class loader is patched
 * because any class loaded before it cannot be incrementally deployed.
 */
public class MonkeyPatcher {
    @SuppressWarnings("unchecked")  // Lots of conversions with generic types
    public static void monkeyPatchApplication(@Nullable Application bootstrap,
                                              @Nullable Application realApplication,
                                              @Nullable String externalResourceFile) {
        /*
        The code seems to perform this:
        Application realApplication = the newly instantiated (in attachBaseContext) user app

        currentActivityThread = ActivityThread.currentActivityThread;
        Application initialApplication = currentActivityThread.mInitialApplication;
        if (initialApplication == BootstrapApplication.this) {
            currentActivityThread.mInitialApplication = realApplication;

        // Replace all instance of the stub application in ActivityThread#mAllApplications with the
        // real one
        List<Application> allApplications = currentActivityThread.mAllApplications;
        for (int i = 0; i < allApplications.size(); i++) {
            if (allApplications.get(i) == BootstrapApplication.this) {
                allApplications.set(i, realApplication);
            }
        }

        // Enumerate all LoadedApk (or PackageInfo) fields in ActivityThread#mPackages and
        // ActivityThread#mResourcePackages and do two things:
        //   - Replace the Application instance in its mApplication field with the real one
        //   - Replace mResDir to point to the external resource file instead of the .apk. This is
        //     used as the asset path for new Resources objects.
        //   - Set Application#mLoadedApk to the found LoadedApk instance

        ArrayMap<String, WeakReference<LoadedApk>> map1 = currentActivityThread.mPackages;
        for (Map.Entry<String, WeakReference<?>> entry : map1.entrySet()) {
            Object loadedApk = entry.getValue().get();
            if (loadedApk == null) {
                continue;
            }

            if (loadedApk.mApplication == BootstrapApplication.this) {
                loadedApk.mApplication = realApplication;
                if (externalResourceFile != null) {
                    loadedApk.mResDir = externalResourceFile;
                }
                realApplication.mLoadedApk = loadedApk;
            }
        }

        // Exactly the same as above, except done for mResourcePackages instead of mPackages
        ArrayMap<String, WeakReference<LoadedApk>> map2 = currentActivityThread.mResourcePackages;
        for (Map.Entry<String, WeakReference<?>> entry : map2.entrySet()) {
            Object loadedApk = entry.getValue().get();
            if (loadedApk == null) {
                continue;
            }

            if (loadedApk.mApplication == BootstrapApplication.this) {
                loadedApk.mApplication = realApplication;
                if (externalResourceFile != null) {
                    loadedApk.mResDir = externalResourceFile;
                }
                realApplication.mLoadedApk = loadedApk;
            }
        }
        */

        // BootstrapApplication is created by reflection in Application#handleBindApplication() ->
        // LoadedApk#makeApplication(), and its return value is used to set the Application field in all
        // sorts of Android internals.
        //
        // Fortunately, Application#onCreate() is called quite soon after, so what we do is monkey
        // patch in the real Application instance in BootstrapApplication#onCreate().
        //
        // A few places directly use the created Application instance (as opposed to the fields it is
        // eventually stored in). Fortunately, it's easy to forward those to the actual real
        // Application class.
        try {
            // Find the ActivityThread instance for the current thread
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(null);

            // Find the mInitialApplication field of the ActivityThread to the real application
            Field mInitialApplication = activityThread.getDeclaredField("mInitialApplication");
            mInitialApplication.setAccessible(true);
            Application initialApplication = (Application) mInitialApplication.get(currentActivityThread);
            if (realApplication != null && initialApplication == bootstrap) {
                mInitialApplication.set(currentActivityThread, realApplication);
            }

            // Replace all instance of the stub application in ActivityThread#mAllApplications with the
            // real one
            if (realApplication != null) {
                Field mAllApplications = activityThread.getDeclaredField("mAllApplications");
                mAllApplications.setAccessible(true);
                List<Application> allApplications = (List<Application>) mAllApplications
                        .get(currentActivityThread);
                for (int i = 0; i < allApplications.size(); i++) {
                    if (allApplications.get(i) == bootstrap) {
                        allApplications.set(i, realApplication);
                    }
                }
            }

            // Figure out how loaded APKs are stored.

            // API version 8 has PackageInfo, 10 has LoadedApk. 9, I don't know.
            Class<?> loadedApkClass;
            try {
                loadedApkClass = Class.forName("android.app.LoadedApk");
            } catch (ClassNotFoundException e) {
                loadedApkClass = Class.forName("android.app.ActivityThread$PackageInfo");
            }
            Field mApplication = loadedApkClass.getDeclaredField("mApplication");
            mApplication.setAccessible(true);
            Field mResDir = loadedApkClass.getDeclaredField("mResDir");
            mResDir.setAccessible(true);

            // 10 doesn't have this field, 14 does. Fortunately, there are not many Honeycomb devices
            // floating around.
            Field mLoadedApk = null;
            try {
                mLoadedApk = Application.class.getDeclaredField("mLoadedApk");
            } catch (NoSuchFieldException e) {
                // According to testing, it's okay to ignore this.
            }

            // Enumerate all LoadedApk (or PackageInfo) fields in ActivityThread#mPackages and
            // ActivityThread#mResourcePackages and do two things:
            //   - Replace the Application instance in its mApplication field with the real one
            //   - Replace mResDir to point to the external resource file instead of the .apk. This is
            //     used as the asset path for new Resources objects.
            //   - Set Application#mLoadedApk to the found LoadedApk instance
            for (String fieldName : new String[]{"mPackages", "mResourcePackages"}) {
                Field field = activityThread.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(currentActivityThread);

                for (Map.Entry<String, WeakReference<?>> entry :
                        ((Map<String, WeakReference<?>>) value).entrySet()) {
                    Object loadedApk = entry.getValue().get();
                    if (loadedApk == null) {
                        continue;
                    }

                    if (mApplication.get(loadedApk) == bootstrap) {
                        if (realApplication != null) {
                            mApplication.set(loadedApk, realApplication);
                        }
                        if (externalResourceFile != null) {
                            mResDir.set(loadedApk, externalResourceFile);
                        }

                        if (realApplication != null && mLoadedApk != null) {
                            mLoadedApk.set(realApplication, loadedApk);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static void monkeyPatchExistingResources(@Nullable String externalResourceFile,
                                                    @Nullable Collection<Activity> activities) {
        if (externalResourceFile == null) {
            return;
        }

        /*
        (Note: the resource directory is *also* inserted into the loadedApk in
        monkeyPatchApplication)
        The code seems to perform this:
        File externalResourceFile = <path to resources.ap_ or extracted directory>

        AssetManager newAssetManager = new AssetManager();
        newAssetManager.addAssetPath(externalResourceFile)

        // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
        // in L, so we do it unconditionally.
        newAssetManager.ensureStringBlocks();

        // Find the singleton instance of ResourcesManager
        ResourcesManager resourcesManager = ResourcesManager.getInstance();

        // Iterate over all known Resources objects
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (WeakReference<Resources> wr : resourcesManager.mActiveResources.values()) {
                Resources resources = wr.get();
                // Set the AssetManager of the Resources instance to our brand new one
                resources.mAssets = newAssetManager;
                resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
            }
        }

        // Also, for each context, call getTheme() to get the current theme; null out its
        // mTheme field, then invoke initializeTheme() to force it to be recreated (with the
        // new asset manager!)

        */

        try {
            // Create a new AssetManager instance and point it to the resources installed under
            // /sdcard
            AssetManager newAssetManager = AssetManager.class.getConstructor().newInstance();
            Method mAddAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            mAddAssetPath.setAccessible(true);
            if (((Integer) mAddAssetPath.invoke(newAssetManager, externalResourceFile)) == 0) {
                throw new IllegalStateException("Could not create new AssetManager");
            }

            // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
            // in L, so we do it unconditionally.
            Method mEnsureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            mEnsureStringBlocks.setAccessible(true);
            mEnsureStringBlocks.invoke(newAssetManager);

            // Find the singleton instance of ResourcesManager
            Class<?> clazz = Class.forName("android.app.ResourcesManager");
            Method mGetInstance = clazz.getDeclaredMethod("getInstance");
            mGetInstance.setAccessible(true);
            Object resourcesManager = mGetInstance.invoke(null);

            Field mAssets = Resources.class.getDeclaredField("mAssets");
            mAssets.setAccessible(true);

            if (activities != null) {
                for (Activity activity : activities) {
                    Resources resources = activity.getResources();
                    mAssets.set(resources, newAssetManager);

                    Resources.Theme theme = activity.getTheme();
                    try {
                        Field ma = Resources.Theme.class.getDeclaredField("mAssets");
                        ma.setAccessible(true);
                        ma.set(theme, newAssetManager);

                        Field mt = ContextThemeWrapper.class.getDeclaredField("mTheme");
                        mt.setAccessible(true);
                        mt.set(activity, null);
                        Method mtm = ContextThemeWrapper.class.getDeclaredMethod("initializeTheme");
                        mtm.setAccessible(true);
                        mtm.invoke(activity);


                        Method mCreateTheme = AssetManager.class.getDeclaredMethod("createTheme");
                        mCreateTheme.setAccessible(true);
                        Object internalTheme = mCreateTheme.invoke(newAssetManager);
                        Field mTheme = Resources.Theme.class.getDeclaredField("mTheme");
                        mTheme.setAccessible(true);
                        mTheme.set(theme, internalTheme);
                    } catch (Throwable e) {
                        Log.e(LOG_TAG, "Failed to update existing theme for activity " + activity,
                                e);
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Iterate over all known Resources objects
                Field fMActiveResources = clazz.getDeclaredField("mActiveResources");
                fMActiveResources.setAccessible(true);
                @SuppressWarnings("unchecked")
                ArrayMap<?, WeakReference<Resources>> arrayMap =
                        (ArrayMap<?, WeakReference<Resources>>) fMActiveResources.get(resourcesManager);
                for (WeakReference<Resources> wr : arrayMap.values()) {
                    Resources resources = wr.get();
                    if (resources != null) {
                        // Set the AssetManager of the Resources instance to our brand new one
                        mAssets.set(resources, newAssetManager);

                        resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
                    }
                }
            }

        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}