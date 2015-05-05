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
package me.adaptive.ide.codewok.project;

import com.intellij.notification.*;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import me.adaptive.ide.codewok.bower.BowerCommandExecutor;
import me.adaptive.ide.codewok.npm.NpmCommandExecutor;
import me.adaptive.ide.common.utils.ConsoleViewUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;

/**
 * Created by panthro on 4/05/15.
 */
public class CodeWokProjectComponent extends AbstractProjectComponent {

    private static final String COMPONENT_NAME = CodeWokProjectComponent.class.getSimpleName();
    private static final String PACKAGE_JSON_FILE = "package.json";
    private static final String NODE_MODULES_DIR = "node_modules";
    private static final NotificationGroup NPM_NOTIFICATION = new NotificationGroup("NPM Notifications", NotificationDisplayType.BALLOON, true);

    private NotificationListener npmInstallNotificationListener;

    protected CodeWokProjectComponent(final Project project) {
        super(project);
        npmInstallNotificationListener = new NotificationListener() {
            @Override
            public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
                Runnable commandToExec = null;
                if (event.getDescription().contains("npm install")) {
                    commandToExec = new Runnable() {
                        @Override
                        public void run() {
                            new NpmCommandExecutor(project.getBasePath()).runInstall(ConsoleViewUtil.registerConsoleView(project, event.getDescription(), true));
                        }
                    };

                } else if (event.getDescription().contains("bower install")) {
                    commandToExec = new Runnable() {
                        @Override
                        public void run() {
                            new BowerCommandExecutor().runInstall(project.getBasePath(), ConsoleViewUtil.registerConsoleView(project, event.getDescription(), true));
                        }
                    };
                }

                if (commandToExec != null) {
                    final Runnable finalCommandToExec = commandToExec;
                    new Task.Backgroundable(project, event.getDescription(), false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            indicator.setIndeterminate(true);
                            finalCommandToExec.run();
                        }
                    }.queue();
                }
            }
        };
    }

    @Override
    public void projectOpened() {
        checkNodeModules();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    /**
     * Checks if the "package.json" file exists and detect if the node_modules is populated,
     * if not suggests the user to execute "npm install" command
     */
    private void checkNodeModules() {
        VirtualFile packageJson = myProject.getBaseDir().findChild(PACKAGE_JSON_FILE);
        if (packageJson != null && packageJson.exists()) {
            VirtualFile nodeModules = myProject.getBaseDir().findChild(NODE_MODULES_DIR);
            if (nodeModules == null || !nodeModules.exists()) {
                Notification notification = NPM_NOTIFICATION.createNotification("Npm Install", "It looks like <b>npm install</b> was not executed. <br /> <a href=\"npm install\">Click here</a> to execute .",
                        NotificationType.INFORMATION, npmInstallNotificationListener);
                notification.notify(myProject);
            }
        }
    }


}
