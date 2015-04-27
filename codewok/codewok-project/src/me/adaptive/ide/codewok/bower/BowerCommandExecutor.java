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

package me.adaptive.ide.codewok.bower;

import com.intellij.execution.ui.ConsoleView;
import me.adaptive.ide.codewok.SimpleCommandLineExecutor;
import me.adaptive.ide.common.utils.ExecutableDetectorUtil;

import java.util.Arrays;

/**
 * Created by panthro on 17/04/15.
 */
public class BowerCommandExecutor extends SimpleCommandLineExecutor {

    public static final String BOWER_COMMAND = "bower";
    public static final String INSTALL_COMMAND = "install";


    public void runInstall(String basePath, ConsoleView consoleView) {
        runCommand(new ExecutableDetectorUtil(BOWER_COMMAND).detect(), basePath, Arrays.asList(new String[]{INSTALL_COMMAND}), consoleView);
    }
}
