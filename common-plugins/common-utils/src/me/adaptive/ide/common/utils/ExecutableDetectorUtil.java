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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by panthro on 27/04/2015.
 */
public class ExecutableDetectorUtil {

    private static final Logger LOG = Logger.getInstance(ExecutableDetectorUtil.class);
    private static final String[] UNIX_PATHS = {"/usr/local/bin",
            "/usr/bin",
            "/opt/local/bin",
            "/opt/bin"};

    private static final String[] WIN_EXTENSIONS = new String[]{"exe", "bat", "cmd"};
    private static final String[] UNIX_EXTENSIONS = new String[]{"sh", "bin"};
    private static final File WIN_ROOT = new File("C:");
    public static final String PATH_ENV = "PATH";
    public static final String WIN_APPDATA_ENV = "APPDATA";
    public static final String NPM_FOLDER_NAME = "npm";
    public static final String ADAPTIVE_FOLDER = ".adaptive";
    public static final String ADAPTIVE_FOLDER_LOCATION = System.getProperty("user.home");

    private String command;

    public ExecutableDetectorUtil(String command) {
        this.command = command;
    }

    @NotNull
    public String detect() {
        if (SystemInfo.isWindows) {
            return detectForWindows();
        }
        return detectForUnix();
    }

    @NotNull
    private String detectForUnix() {
        String exec = checkInAdaptiveFolder();
        if (exec != null) {
            return exec;
        }

        exec = checkInNpmModules();
        if (exec != null) {
            return exec;
        }

        for (String p : UNIX_PATHS) {
            File f = new File(p, command);
            if (f.exists()) {
                return f.getPath();
            }
        }
        return command;
    }

    @NotNull
    private String detectForWindows() {

        String exec = checkInAdaptiveFolder();
        if (exec != null) {
            return exec;
        }

        exec = checkInPath();
        if (exec != null) {
            return exec;
        }

        exec = checkInNpmModules();
        if (exec != null) {
            return exec;
        }

        exec = checkProgramFiles();
        if (exec != null) {
            return exec;
        }


        exec = checkCygwin();
        if (exec != null) {
            return exec;
        }

        return checkSoleExecutable();
    }


    private String checkInAdaptiveFolder() {
        File adaptiveFolder = new File(ADAPTIVE_FOLDER_LOCATION, ADAPTIVE_FOLDER);
        String exePath = null;
        for (File subFolder : adaptiveFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        })) {
            exePath = findCommandInDir(subFolder.getAbsolutePath());
            if (exePath == null) {
                exePath = findCommandInBinDir(subFolder.getAbsolutePath());
            }
        }
        return exePath;
    }
    /**
     * Looks into the %PATH% and checks command directories mentioned there.
     *
     * @return command executable to be used or null if nothing interesting was found in the PATH.
     */
    @Nullable
    private String checkInPath() {
        String PATH = getPath();
        if (PATH == null) {
            return null;
        }
        List<String> pathEntries = StringUtil.split(PATH, File.pathSeparator);
        for (String pathEntry : pathEntries) {
            String found = findCommandInDir(pathEntry);
            if (found != null) {
                return found;
            } else {
                found = findCommandInBinDir(pathEntry);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }


    private String findCommandInBinDir(String dir) {
        return findCommandInDir(new File(dir, "bin").getAbsolutePath());
    }

    private String findCommandInDir(String dir) {
        if (!new File(dir).exists()) {
            return null;
        }
        for (File file : new File(dir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().startsWith(command);
            }
        })) {
            String[] fileAndExt = file.getName().split("\\.");
            if (fileAndExt.length == 1 && command.equals(fileAndExt[0]) && !SystemInfo.isWindows) {
                return file.getAbsolutePath();
            } else if (fileAndExt.length == 2) {
                if (SystemInfo.isWindows) {
                    for (String ext : WIN_EXTENSIONS) {
                        if (command.equals(fileAndExt[0]) && ext.equals(fileAndExt[1])) {
                            return file.getAbsolutePath();
                        }
                    }
                } else {
                    for (String ext : UNIX_EXTENSIONS) {
                        if (command.equals(fileAndExt[0]) && ext.equals(fileAndExt[1])) {
                            return file.getAbsolutePath();
                        }
                    }
                }
            }
        }
        return null;
    }


    @Nullable
    private String checkInNpmModules() {
        if (System.getenv(WIN_APPDATA_ENV) == null) {
            return null;
        }
        File npmFolder = new File(System.getenv(WIN_APPDATA_ENV), NPM_FOLDER_NAME);
        if (!npmFolder.exists()) {
            return null;
        }
        return findCommandInDir(npmFolder.getAbsolutePath());
    }

    @Nullable
    private String checkProgramFiles() {
        final String[] PROGRAM_FILES = {"Program Files", "Program Files (x86)"};

        // collecting all potential msys distributives
        List<File> distrs = new ArrayList<File>();
        for (String programFiles : PROGRAM_FILES) {
            File pf = new File(WIN_ROOT, programFiles);
            File[] children = pf.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() && pathname.getName().toLowerCase().contains(command);
                }
            });
            if (!pf.exists() || children == null) {
                continue;
            }
            distrs.addAll(Arrays.asList(children));
        }


        for (File distr : distrs) {
            String exec = checkDistributive(distr);
            if (exec != null) {
                return exec;
            }
        }
        return null;
    }

    @Nullable
    private String checkCygwin() {
        final String[] OTHER_WINDOWS_PATHS = {FileUtil.toSystemDependentName("cygwin/bin/" + command)};
        for (String otherPath : OTHER_WINDOWS_PATHS) {
            for (String extension : WIN_EXTENSIONS) {
                File file = new File(WIN_ROOT, otherPath + "." + extension);
                if (file.exists()) {
                    return file.getPath();
                }
            }
        }
        return null;
    }

    @NotNull
    private String checkSoleExecutable() {
        if (runs(command)) {
            return command;
        }
        return command;
    }

    @Nullable
    private String checkDistributive(@Nullable File commandDir) {
        if (commandDir == null || !commandDir.exists()) {
            return null;
        }

        final String[] binDirs = {"cmd", "bin"};
        for (String binDir : binDirs) {
            String exec = checkBinDir(new File(commandDir, binDir));
            if (exec != null) {
                return exec;
            }
        }

        return null;
    }

    @Nullable
    private String checkBinDir(@NotNull File binDir) {
        if (!binDir.exists()) {
            return null;
        }

        for (String extension : WIN_EXTENSIONS) {
            File fe = new File(binDir, command + "." + extension);
            if (fe.exists()) {
                return fe.getPath();
            }
        }

        return null;
    }

    /**
     * Checks if it is possible to run the specified program.
     * Made protected for tests not to start a process there.
     */
    protected boolean runs(@NotNull String exec) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(exec);
        try {
            CapturingProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(), CharsetToolkit.getDefaultSystemCharset());
            ProcessOutput result = handler.runProcess((int) TimeUnit.SECONDS.toMillis(5));
            return !result.isTimeout();
        } catch (ExecutionException e) {
            return false;
        }
    }

    @Nullable
    protected String getPath() {
        return System.getenv(PATH_ENV);
    }

}
