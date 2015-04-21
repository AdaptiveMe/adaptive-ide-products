/*
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

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.MessageView;
import me.adaptive.ide.codewok.bower.BowerCommandExecutor;
import me.adaptive.ide.codewok.npm.NpmCommandExecutor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

//import me.adaptive.ide.branding.CodeWokIcons;

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
  public void generateProject(@NotNull final Project project,
                              @NotNull final VirtualFile baseDir,
                              @Nullable Object settings,
                              @NotNull Module module) {
    if (ApplicationManager.getApplication().isDispatchThread()) {
      final ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
      final MessageView messageView = MessageView.SERVICE.getInstance(project);
      messageView.getContentManager().addContent(
              ContentFactory.SERVICE.getInstance().createContent(consoleView.getComponent(), "CodeWok Generator", false));
      ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating CodeWok project", false) {
        @Override
        public NotificationInfo getNotificationInfo() {
          return new NotificationInfo("CodeWokGenerator", "CodeWok Project", "Generation Finished");
        }

        @Override
        public void run(@NotNull final ProgressIndicator progressIndicator) {
          progressIndicator.setIndeterminate(true);
          progressIndicator.setText("Creating the project files");
          progressIndicator.pushState();
          GeneratorRunner runner = new GeneratorRunner(project.getName(), getAdaptiveVersion(), typescriptSupport, getBoilerplate());
          runner.setSkipInstall(true);
          runner.setSkipServer(true);
          runner.generate(project, consoleView);
          baseDir.refresh(true, true);
          progressIndicator.setText("Running NPM install");
          NpmCommandExecutor npmCommandExecutor = new NpmCommandExecutor(project.getBasePath());
          processSentToBackground();
          npmCommandExecutor.runInstall(consoleView);
          baseDir.refresh(true, true);
          progressIndicator.setText("Running Bower Install");
          BowerCommandExecutor bowerCommandExecutor = new BowerCommandExecutor();
          bowerCommandExecutor.runInstall(project.getBasePath(), consoleView);
        }
      });


    }
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
