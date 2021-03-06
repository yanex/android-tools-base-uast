/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.lint.checks;

import static com.android.SdkConstants.CLASS_VIEW;
import static com.android.tools.lint.checks.JavaPerformanceDetector.ON_DRAW;
import static com.android.tools.lint.checks.JavaPerformanceDetector.ON_LAYOUT;
import static com.android.tools.lint.checks.JavaPerformanceDetector.ON_MEASURE;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;

import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UFunction;
import org.jetbrains.uast.UQualifiedExpression;
import org.jetbrains.uast.USuperExpression;
import org.jetbrains.uast.UastUtils;
import org.jetbrains.uast.visitor.UastVisitor;

import java.util.Arrays;
import java.util.List;

/**
 * Checks for cases where the wrong call is being made
 */
public class WrongCallDetector extends Detector implements Detector.UastScanner {
    /** Calling the wrong method */
    public static final Issue ISSUE = Issue.create(
            "WrongCall", //$NON-NLS-1$
            "Using wrong draw/layout method",

            "Custom views typically need to call `measure()` on their children, not `onMeasure`. " +
            "Ditto for onDraw, onLayout, etc.",

            Category.CORRECTNESS,
            6,
            Severity.FATAL,
            new Implementation(
                    WrongCallDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    /** Constructs a new {@link WrongCallDetector} */
    public WrongCallDetector() {
    }

    // ---- Implements UastScanner ----

    @Override
    @Nullable
    public List<String> getApplicableFunctionNames() {
        return Arrays.asList(
                ON_DRAW,
                ON_MEASURE,
                ON_LAYOUT
        );
    }

    @Override
    public void visitFunctionCallExpression(@NonNull JavaContext context,
            @Nullable UastVisitor visitor, @NonNull UCallExpression call,
            @NonNull UFunction function) {
        // Call is only allowed if it is both only called on the super class (invoke special)
        // as well as within the same overriding method (e.g. you can't call super.onLayout
        // from the onMeasure method)
        UElement parent = call.getParent();
        if (parent instanceof UQualifiedExpression) {
            UExpression receiver = ((UQualifiedExpression) parent).getReceiver();
            if (!(receiver instanceof USuperExpression)) {
                report(context, call, function);
                return;
            }
        }

        UFunction containingFunction = UastUtils.getContainingFunction(call);
        if (containingFunction != null) {
            String callName = call.getFunctionName();
            if (callName != null && !callName.equals(containingFunction.getName())) {
                report(context, call, function);
            }
        }

    }

    private static void report(
            @NonNull JavaContext context,
            @NonNull UCallExpression node,
            @NonNull UFunction function) {
        // Make sure the call is on a view

        if (!UastUtils.getContainingClassOrEmpty(function).isSubclassOf(CLASS_VIEW, false)) {
            return;
        }

        String name = function.getName();
        String suggestion = Character.toLowerCase(name.charAt(2)) + name.substring(3);
        String message = String.format(
                // Keep in sync with {@link #getOldValue} and {@link #getNewValue} below!
                "Suspicious method call; should probably call \"`%1$s`\" rather than \"`%2$s`\"",
                suggestion, name);
        context.report(ISSUE, node, context.getLocation(node.getFunctionNameElement()), message);
    }

    /**
     * Given an error message produced by this lint detector for the given issue type,
     * returns the old value to be replaced in the source code.
     * <p>
     * Intended for IDE quickfix implementations.
     *
     * @param errorMessage the error message associated with the error
     * @param format the format of the error message
     * @return the corresponding old value, or null if not recognized
     */
    @Nullable
    public static String getOldValue(@NonNull String errorMessage, @NonNull TextFormat format) {
        errorMessage = format.toText(errorMessage);
        return LintUtils.findSubstring(errorMessage, "than \"", "\"");
    }

    /**
     * Given an error message produced by this lint detector for the given issue type,
     * returns the new value to be put into the source code.
     * <p>
     * Intended for IDE quickfix implementations.
     *
     * @param errorMessage the error message associated with the error
     * @param format the format of the error message
     * @return the corresponding new value, or null if not recognized
     */
    @Nullable
    public static String getNewValue(@NonNull String errorMessage, @NonNull TextFormat format) {
        errorMessage = format.toText(errorMessage);
        return LintUtils.findSubstring(errorMessage, "call \"", "\"");
    }
}
