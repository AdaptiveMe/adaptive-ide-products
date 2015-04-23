/*
 * Copyright 2014-2015. Adaptive.me.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.adaptive.ide.plugins.nibble;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

/**
 * Created by panthro on 17/04/15.
 */
public class NpmModuleFinder {


    private static final String NODE_MODULES_FOLDER_NAME = "node_modules";

    /**
     * Finds a node module inside the {@code NODE_MODULES_FOLDER_NAME} and return it's representing {@code VirtualFile}
     *
     * @param project    the {@code Project} where the module is
     * @param moduleName the name of the module
     * @return a {@code VirtualFile} pointing to the root of the module or {@code null} if not found
     * @see #findModuleInProject(VirtualFile, String)
     */
    public static VirtualFile findModuleInProject(Project project, String moduleName) {
        return findModuleInProject(project.getBaseDir(), moduleName);
    }

    /**
     * * Finds a node module inside the {@code NODE_MODULES_FOLDER_NAME} and return it's representing {@code VirtualFile}
     *
     * @param baseDir    the directory where the module is
     * @param moduleName the name of the module
     * @return a {@code VirtualFile} pointing to the root of the module or {@code null} if not found
     */
    public static VirtualFile findModuleInProject(VirtualFile baseDir, String moduleName) {
        return baseDir.findFileByRelativePath(NODE_MODULES_FOLDER_NAME + File.separator + moduleName);
    }
}
