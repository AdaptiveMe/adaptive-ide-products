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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.MessageView;
import me.adaptive.ide.common.utils.ExecutableDetectorUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by panthro on 17/04/15.
 */
public class NibbleComponent extends AbstractProjectComponent {

    public static final String COMPONENT_NAME = "Adaptive Nibble";
    public static final String NIBBLE_MODULE_NAME = "npm-adaptiveme-nibble";
    public static final String NIBBLE_MODULE_BINARY_LOCATION = "bin" + File.separator + "adaptive-nibble-emulator" + File.separator + "bin";
    public static final String DEFAULT_INDEX_PATH = "src/index.html"; //This is used as a VirtualFile, so it needs to use "/"

    private static final String WACTHER_PARAM = "-w true";
    //true needs to be passed https://github.com/AdaptiveMe/adaptive-tools-nibble/issues/8
    private static final String GLOBAL_NIBBLE_COMMAND = "nibble";
    private static final String MODULE_NIBBLE_COMMAND = "adaptive-nibble-emulator";


    private ConsoleView consoleView;
    private boolean withWatcher = true;

    private GeneralCommandLine commandLine;
    private volatile ProcessHandler processHandler;

    protected NibbleComponent(Project project) {
        super(project);
    }

    public void setConsoleView(ConsoleView consoleView) {
        this.consoleView = consoleView;
    }

    private List<String> getParamList(String filePath) {
        List<String> paramList = new LinkedList<String>();
        paramList.add("-p");
        paramList.add(filePath);
        if (withWatcher) {
            paramList.addAll(Arrays.asList(WACTHER_PARAM.split(" ")));
        }
        return paramList;
    }


    public void runOnNibble(@NotNull VirtualFile file){
        runOnNibble(VfsUtil.virtualToIoFile(file));
    }

    public void runOnNibble(@NotNull File file) {
        disposeComponent();

        VirtualFile nibbleModuleRoot = NpmModuleFinder.findModuleInProject(myProject, NIBBLE_MODULE_NAME);
        commandLine = new GeneralCommandLine();
        if (nibbleModuleRoot != null) {
            commandLine.setExePath(
                    new File(nibbleModuleRoot.getPath(), NIBBLE_MODULE_BINARY_LOCATION + File.separator + MODULE_NIBBLE_COMMAND).getAbsolutePath());
        } else {
            commandLine.setExePath(new ExecutableDetectorUtil(GLOBAL_NIBBLE_COMMAND).detect());
        }
        //commandLine.withWorkDirectory(myProject.getBasePath());
        commandLine.addParameters(getParamList(file.getAbsolutePath()));

        try {
            processHandler = new OSProcessHandler(commandLine);
            if (consoleView == null) {
                setupDefaultConsoleView();
            } else {
                consoleView.attachToProcess(processHandler);
            }
            ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                @Override
                public void run() {
                    processHandler.startNotify();
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disposeComponent() {
        if (isRunning()) {
            processHandler.destroyProcess();
            processHandler = null;
        }
    }

    public boolean isRunning() {
        return processHandler != null && !processHandler.isProcessTerminated() && !processHandler.isProcessTerminating();
    }

    @Override
    public void projectClosed() {
        disposeComponent();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public void projectOpened() {
        if (isAdaptiveProject()) {
            File indexHtml = new File(myProject.getBasePath(), DEFAULT_INDEX_PATH);
            if (indexHtml.exists() && !isRunning()) {
                runOnNibble(indexHtml);
            }
        }
    }

    public boolean isAdaptiveProject() {
        return true;
        //ModuleUtil.hasModulesOfType(myProject, CodeWokModuleType.getInstance());
    }

    protected void setupDefaultConsoleView() {
        if (consoleView != null) {
            return;
        }
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(myProject).getConsole();
        final MessageView messageView = MessageView.SERVICE.getInstance(myProject);
        messageView.runWhenInitialized(new Runnable() {
            @Override
            public void run() {
                messageView.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(consoleView.getComponent(), COMPONENT_NAME, false));
                consoleView.attachToProcess(processHandler);
            }
        });
    }
}
