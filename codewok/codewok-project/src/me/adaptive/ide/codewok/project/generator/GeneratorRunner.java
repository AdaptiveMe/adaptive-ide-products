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

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import me.adaptive.ide.codewok.SimpleCommandLineExecutor;
import me.adaptive.ide.common.utils.ExecutableDetectorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panthro on 10/04/15.
 */
public class GeneratorRunner extends SimpleCommandLineExecutor {


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

  public enum Boilerplate {
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
  public static final String SKIP_INSTALL = "--skip-install";
  public static final String SKIP_SERVER = "--skip-server";
  public static final String SKIP_CACHE = "--skip-cache";

  private String appName;
  private AdaptiveVersion adaptiveVersion;
  private boolean typescriptSupport;
  private Boilerplate boilerplate;
  private boolean skipInstall;
  private boolean skipServer;
  private boolean skipCache;


  /**
   * Creates a generator instance with all parameters set
   *
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
   *
   * @param appName
   */
  public GeneratorRunner(String appName) {
    this(appName, AdaptiveVersion.LATEST, false, Boilerplate.NONE);
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

  public boolean isSkipInstall() {
    return skipInstall;
  }

  public void setSkipInstall(boolean skipInstall) {
    this.skipInstall = skipInstall;
  }

  public boolean isSkipServer() {
    return skipServer;
  }

  public void setSkipServer(boolean skipServer) {
    this.skipServer = skipServer;
  }

  public boolean isSkipCache() {
    return skipCache;
  }

  public void setSkipCache(boolean skipCache) {
    this.skipCache = skipCache;
  }

  private List<String> getParametersList() {
    List<String> paramList = new ArrayList<String>();
    paramList.add(GENERATOR_NAME);
    paramList.add(getAppName());
    paramList.add(getAdaptiveVersion().getName().toLowerCase());
    paramList.add(isTypescriptSupport().toString().toLowerCase());
    paramList.add(getBoilerplate().getName());
    if (skipInstall) {
      paramList.add(SKIP_INSTALL);
    }
    if (skipServer) {
      paramList.add(SKIP_SERVER);
    }
    if (skipCache) {
      paramList.add(SKIP_CACHE);
    }
    return paramList;
  }

  public void generate(Project project) {
    generate(project, null);
  }


  public void generate(final Project project, ConsoleView consoleView) {
    runCommand(new ExecutableDetectorUtil(YEOMAN_COMMAND).detect(), project.getBasePath(), getParametersList(), consoleView);
  }

}
