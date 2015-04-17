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

package me.adaptive.ide.codewok;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by panthro on 17/04/15.
 */
public class SimpleCommandLineExecutor {

    private static final Logger LOG = Logger.getInstance(SimpleCommandLineExecutor.class);

    public void runCommand(@NotNull String command, @NotNull String basePath, @Nullable List<String> paramList, @Nullable ConsoleView consoleView) {
        final GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(command);
        commandLine.withWorkDirectory(basePath);
        commandLine.addParameters(paramList);
        commandLine.setRedirectErrorStream(true);

        try {
            ProcessHandler processHandler = new OSProcessHandler(commandLine);
            if (consoleView != null) {
                consoleView.attachToProcess(processHandler);
            }
            processHandler.startNotify();
            processHandler.waitFor();
        } catch (ExecutionException e) {
            LOG.warn(e);
        }
    }

}
