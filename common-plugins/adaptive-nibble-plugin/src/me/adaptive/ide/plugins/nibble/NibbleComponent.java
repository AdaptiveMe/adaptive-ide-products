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
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tools.Tool;
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


    public void runOnNibbleOnRunToolWindow(@NotNull VirtualFile file) {
        runOnNibbleOnRunToolWindow(VfsUtil.virtualToIoFile(file));
    }

    /**
     * Runs nibble inside the Run tool window, where the user can stop or kill the process at any time.
     *
     * @param file
     */
    public void runOnNibbleOnRunToolWindow(@NotNull File file) {
        final Tool nibbleTool = new Tool() {
            @Override
            public boolean isUseConsole() {
                return true;
            }

            @Override
            public String getName() {
                return COMPONENT_NAME;
            }
        };

        nibbleTool.setParameters(StringUtil.join(getParamList(file.getAbsolutePath()), " "));
        nibbleTool.setProgram(getNibbleExecutablePath());

        nibbleTool.setWorkingDirectory(myProject.getBasePath());
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                nibbleTool.execute(null, SimpleDataContext.getProjectContext(myProject), COMPONENT_NAME.hashCode(), null);
            }
        });
    }

    protected String getNibbleExecutablePath(){
        VirtualFile nibbleModuleRoot = NpmModuleFinder.findModuleInProject(myProject, NIBBLE_MODULE_NAME);
        String exePath;
        if (nibbleModuleRoot != null) {
            exePath = new File(nibbleModuleRoot.getPath(), NIBBLE_MODULE_BINARY_LOCATION + File.separator + MODULE_NIBBLE_COMMAND).getAbsolutePath();
        } else {
            exePath = new ExecutableDetectorUtil(GLOBAL_NIBBLE_COMMAND).detect();
        }
        return exePath;
    }

    /**
     * Runs nibble as a sepparate program and attach the output to the Messages tool window
     *
     * @param file
     * @see #runOnNibbleOnRunToolWindow(VirtualFile) if you want the user to have control over the process
     */
    public void runOnNibble(@NotNull File file) {
        disposeComponent();

        commandLine = new GeneralCommandLine();
        commandLine.setExePath(getNibbleExecutablePath());
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
                runOnNibbleOnRunToolWindow(indexHtml);
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
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                messageView.runWhenInitialized(new Runnable() {
                    @Override
                    public void run() {
                        messageView.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(consoleView.getComponent(), COMPONENT_NAME, false));
                        consoleView.attachToProcess(processHandler);
                    }
                });
            }
        });
    }
}
