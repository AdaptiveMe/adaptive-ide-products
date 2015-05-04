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

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.MessageView;
import me.adaptive.ide.codewok.npm.NpmCommandExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by panthro on 4/05/15.
 */
public class NpmExecuteCommandAction extends AnAction implements DumbAware {

    private String command;
    private ConsoleView consoleView;

    public NpmExecuteCommandAction(@NotNull String command) {
        super(command);
        this.command = command;
    }

    protected String getTitle() {
        return "npm " + command;
    }

    protected void setupConsoleView(Project project) {
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        final MessageView messageView = MessageView.SERVICE.getInstance(project);
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                messageView.runWhenInitialized(new Runnable() {
                    @Override
                    public void run() {
                        Content content = ContentFactory.SERVICE.getInstance().createContent(consoleView.getComponent(), getTitle(), false);
                        messageView.getContentManager().addContent(content);
                        messageView.getContentManager().requestFocus(content, true);
                    }
                });
            }
        });
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null || project.isDefault()) {
            return;
        }

        if (consoleView == null) {
            setupConsoleView(project);
        }

        new Task.Backgroundable(project, getTitle()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                new NpmCommandExecutor(project.getBasePath()).runCommand(command, consoleView);
            }
        }.queue();
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        if (NpmCommandExecutor.UPDATE_COMMAND.equals(command)) {
            e.getPresentation().setIcon(AllIcons.Actions.Refresh);
        } else if (NpmCommandExecutor.INSTALL_COMMAND.equals(command)) {
            e.getPresentation().setIcon(AllIcons.Actions.Install);
        }
    }
}
