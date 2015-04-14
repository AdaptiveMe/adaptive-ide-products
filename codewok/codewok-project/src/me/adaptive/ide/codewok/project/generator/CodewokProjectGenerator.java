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

package me.adaptive.ide.codewok.project.generator;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
//import me.adaptive.ide.branding.CodeWokIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by panthro on 13/04/15.
 */
public class CodewokProjectGenerator implements DirectoryProjectGenerator {

    private String appName;
    private GeneratorRunner.AdaptiveVersion adaptiveVersion;
    private boolean typescriptSupport;
    private GeneratorRunner.Boilerplate boilerplate;

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "CodeWok";
    }

    @Nullable
    @Override
    public Object showGenerationSettings(VirtualFile baseDir) throws ProcessCanceledException {
        return null;
    }

    @Nullable
    @Override
    public Icon getLogo() {
        //try{
        //    return CodeWokIcons.CodeWok;
        //}catch(NoClassDefFoundError e){
            return AllIcons.Welcome.CreateNewProject;
        //}
    }

    @Override
    public void generateProject(@NotNull final Project project, @NotNull final VirtualFile baseDir, @Nullable Object settings, @NotNull Module module) {
      ProgressManager.getInstance().run(new Task.Modal(project,"Generating CodeWok project",false) {
        @Override
        public void run(@NotNull final ProgressIndicator progressIndicator) {
          progressIndicator.setFraction(0.3);
          progressIndicator.setText("Gathering System Information");
          GeneratorRunner runner = new GeneratorRunner(project.getName(),getAdaptiveVersion(),typescriptSupport,getBoilerplate());
          runner.generate(baseDir.getPath(), new Runnable() {
            @Override
            public void run() {
              progressIndicator.setText("Finished");
              progressIndicator.setFraction(1);

            }
          });
          progressIndicator.setFraction(0.5);
          progressIndicator.setText("Initiating Components");

        }
      });


    }

    @NotNull
    @Override
    public ValidationResult validate(@NotNull String baseDirPath) {
        return ValidationResult.OK;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public GeneratorRunner.AdaptiveVersion getAdaptiveVersion() {
        return adaptiveVersion;
    }

    public void setAdaptiveVersion(GeneratorRunner.AdaptiveVersion adaptiveVersion) {
        this.adaptiveVersion = adaptiveVersion;
    }

    public boolean isTypescriptSupport() {
        return typescriptSupport;
    }

    public void setTypescriptSupport(boolean typescriptSupport) {
        this.typescriptSupport = typescriptSupport;
    }

    public GeneratorRunner.Boilerplate getBoilerplate() {
        return boilerplate;
    }

    public void setBoilerplate(GeneratorRunner.Boilerplate boilerplate) {
        this.boilerplate = boilerplate;
    }
}
