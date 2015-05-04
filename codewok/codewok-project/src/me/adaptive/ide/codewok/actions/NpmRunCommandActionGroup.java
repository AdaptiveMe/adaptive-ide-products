/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package me.adaptive.ide.codewok.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tools.SimpleActionGroup;
import me.adaptive.ide.codewok.npm.NpmCommandExecutor;

/**
 * Created by panthro on 4/05/15.
 */
public class NpmRunCommandActionGroup extends SimpleActionGroup implements DumbAware {

    private static final String VALID_FILE_NAME_REGEX = "(package\\.json|node_modules)";

    /**
     * Load all actions available based on the available command list from the executor
     */
    public NpmRunCommandActionGroup() {
        for (int i = 0; i < NpmCommandExecutor.AVAILABLE_COMMANDS.length; i++) {
            add(new NpmExecuteCommandAction(NpmCommandExecutor.AVAILABLE_COMMANDS[i]));
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null || project.isDefault()) { //If we are not inside a project, shouldn't be able to run it
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
            return;
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

    /*
    Do not show the action in the following cases
    Click on Editor, no file selected
     */
        if ((editor != null || file == null)) {
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
            return;
        }

    /*
    Do now show the action in case the file is not a 'package.json' file nor a 'node_modules' directory
     */
        if (file != null && !file.getName().matches(VALID_FILE_NAME_REGEX)) {
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(true);
    }


}
