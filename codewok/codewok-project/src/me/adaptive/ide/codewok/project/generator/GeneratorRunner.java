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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panthro on 10/04/15.
 */
public class GeneratorRunner {

    public enum AdaptiveVersion {
        LATEST("latest");

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        AdaptiveVersion(String name) {
            this.name = name;
        }
    }

    public enum Boilerplate{
        HTML5("HTML5 Boilerplate"),
        MOBILE_HTML5("Mobile HTML5 Boilerplate"),
        INITIALIZR_RESPONSIVE("Initializr Responsive"),
        INITIALIZR_BOOTSTRAP("Initializr Boostrap"),
        NONE("None");

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        Boilerplate(String name) {
            this.name = name;
        }
    }

    public static final String YEOMAN_COMMAND = "yo";
    public static final String GENERATOR_NAME = "adaptiveme";

    private static volatile OSProcessHandler processHandler;
    private static final Logger LOG = Logger.getInstance(GeneratorRunner.class);


    private String appName;
    private AdaptiveVersion adaptiveVersion;
    private boolean typescriptSupport;
    private Boilerplate boilerplate;


    /**
     * Creates a generator instance with all parameters set
     * @param appName
     * @param adaptiveVersion
     * @param typescriptSupport
     * @param boilerplate
     */
    public GeneratorRunner(String appName, AdaptiveVersion adaptiveVersion, boolean typescriptSupport, Boilerplate boilerplate) {
        this.appName = appName;
        this.adaptiveVersion = adaptiveVersion;
        this.typescriptSupport = typescriptSupport;
        this.boilerplate = boilerplate;
    }

    /**
     * Creates a generator with all default options
     * @param appName
     */
    public GeneratorRunner(String appName) {
        this(appName,AdaptiveVersion.LATEST,false,Boilerplate.NONE);
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public AdaptiveVersion getAdaptiveVersion() {
        return adaptiveVersion;
    }

    public void setAdaptiveVersion(AdaptiveVersion adaptiveVersion) {
        this.adaptiveVersion = adaptiveVersion;
    }

    public Boolean isTypescriptSupport() {
        return typescriptSupport;
    }

    public void setTypescriptSupport(boolean typescriptSupport) {
        this.typescriptSupport = typescriptSupport;
    }

    public Boilerplate getBoilerplate() {
        return boilerplate;
    }

    public void setBoilerplate(Boilerplate boilerplate) {
        this.boilerplate = boilerplate;
    }

    private List<String> getParametersList(){
        List<String> paramList = new ArrayList<String>();
        paramList.add(GENERATOR_NAME);
        paramList.add(getAppName());
        paramList.add(getAdaptiveVersion().getName().toLowerCase());
        paramList.add(isTypescriptSupport().toString().toLowerCase());
        paramList.add(getBoilerplate().getName());
        return paramList;
    }

    public void generate(Project project) {
        generate(project, null, null);
    }

    public void generate(Project project, ConsoleView consoleView) {
        generate(project, null, consoleView);
    }

    public void generate(final Project project, final Runnable onFinish, ConsoleView consoleView) {
        final GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(YEOMAN_COMMAND);
        commandLine.withWorkDirectory(project.getBasePath());
        commandLine.addParameters(getParametersList());
        commandLine.setRedirectErrorStream(true);
        //ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        //    @Override
        //    public void run() {
                try {

                    processHandler = new OSProcessHandler(commandLine.createProcess(), "");
                    if (consoleView != null) {
                        consoleView.attachToProcess(processHandler);
                    }
                    processHandler.startNotify();
                    processHandler.waitFor();
                } catch (ExecutionException e) {
                    LOG.info(e);
                } finally {
                    processHandler = null;
                    if (onFinish != null) {
                        onFinish.run();
                    }
                }
        //}
        //});
    }

}
