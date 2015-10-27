/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.build.transform.api;

import com.google.common.annotations.Beta;

/**
 * The format in which a content is stored.
 */
@Beta
public enum Format {

    /**
     * The content is jar(s).
     *
     * <p/>
     * As Input, there can be one or more jar files.
     * As output, a transform can only write a single jar files.
     */
    JAR,
    /**
     * The content is directly under the root direcory.
     *
     * <p/>
     * This means that in the case of java class files, the files should be in directories
     * matching their package names, directly under the root directory.
     */
    DIRECTORY
}