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
package me.adaptive.ide.common.utils;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.MessageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by panthro on 5/05/15.
 */
public class ConsoleViewUtil {

    protected static Map<String, ConsoleView> registeredConsoleView = new HashMap<String, ConsoleView>();


    public static ConsoleView registerConsoleView(final Project project, final String title, final boolean requestFocus) {

        if (registeredConsoleView.containsKey(title)) {
            return registeredConsoleView.get(title);
        }

        final ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                final MessageView messageView = MessageView.SERVICE.getInstance(project);
                messageView.runWhenInitialized(new Runnable() {
                    @Override
                    public void run() {
                        Content content = ContentFactory.SERVICE.getInstance().createContent(consoleView.getComponent(), title, false);
                        messageView.getContentManager().addContent(content);
                        if (requestFocus) {
                            messageView.getContentManager().requestFocus(content, true);
                        }
                    }
                });
            }
        });

        registeredConsoleView.put(title, consoleView);
        return consoleView;
    }
}
