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

package me.adaptive.ide.codewok.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.NewDirectoryProjectAction;
import com.intellij.util.Function;
import me.adaptive.ide.codewok.project.generator.CodeWokGenerateProjectDialog;
import me.adaptive.ide.codewok.project.generator.CodewokProjectGenerator;
import org.jetbrains.annotations.Nullable;

//import me.adaptive.ide.branding.CodeWokIcons;

/**
 * Created by panthro on 10/04/15.
 */
public class GenerateCodeWokProjectAction extends NewDirectoryProjectAction {
    private static final Logger LOG = Logger.getInstance(GenerateCodeWokProjectAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        CodeWokGenerateProjectDialog dlg = new CodeWokGenerateProjectDialog(project);
        dlg.show();
        if(dlg.isOK()){
            generateProject(project,dlg);
        }

    }

    @Nullable
    protected Project generateProject(Project project, CodeWokGenerateProjectDialog dialog){
        DirectoryProjectGenerator[] generators = Extensions.getExtensions(DirectoryProjectGenerator.EP_NAME);
        for(DirectoryProjectGenerator directoryProjectGenerator : generators){
            if(directoryProjectGenerator instanceof CodewokProjectGenerator){
                final CodewokProjectGenerator generator = (CodewokProjectGenerator)directoryProjectGenerator;
                generator.setAppName(dialog.getProjectName());
                generator.setAdaptiveVersion(dialog.getAdaptiveVersion());
                generator.setTypescriptSupport(dialog.isTypeScriptEnabled());
                generator.setBoilerplate(dialog.getBoilerplate());
                return doGenerateProject(project,dialog.getProjectLocation(),generator,new Function<VirtualFile, Object>() {
                    @Override
                    public Object fun(VirtualFile file) {
                        return showSettings(generator, file);
                    }
                });
            }
        }

        return project;
    }

    //@Override
    //public void update(@NotNull AnActionEvent e) {
    //    if (NewWelcomeScreen.isNewWelcomeScreen(e)) {
    //        try {
    //            e.getPresentation().setIcon(CodeWokIcons.CodeWok);
    //        } catch (NoClassDefFoundError ex) {
    //            LOG.info("CodeWokIcons class not found");
    //            e.getPresentation().setIcon(AllIcons.Welcome.CreateNewProject);
    //        }
    //    }
    //}
}
