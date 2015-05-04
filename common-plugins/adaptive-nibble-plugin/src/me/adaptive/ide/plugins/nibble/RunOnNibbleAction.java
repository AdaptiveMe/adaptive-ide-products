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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by panthro on 17/04/15.
 */
public class RunOnNibbleAction extends AnAction implements DumbAware {


    private static final String HTML_FILE_REGEX = ".*\\.(html|htm|sht|shtm|shtml|xhtml)$";

    @Override
    public void update(final AnActionEvent e) {
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
        Not in an editor AND no file selected OR editor is empty
         */
        if ((editor == null && file == null) || (editor != null && editor.getDocument().getTextLength() == 0)) {
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
            return;
        }

        /*
        Do now show the action in case the file is not a .html file
         */
        if (file != null && !file.getName().matches(HTML_FILE_REGEX)) {
            e.getPresentation().setVisible(false);
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(true);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null || project.isDefault()) {
            return;
        }

        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (editor == null && file == null) {
            return;
        }


        final NibbleComponent component = (NibbleComponent) project.getComponent(NibbleComponent.COMPONENT_NAME);
        new Task.Backgroundable(project, "Launching Nibble") {
            @Override
            public void run(ProgressIndicator indicator) {
                //component.runOnNibble(file);
                component.runOnNibbleOnRunToolWindow(file);
            }
        }.queue();


    }
}
