/*
 * Copyright 2016 Doug Hoard
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.devopology.continuous.task;

import org.devopology.continuous.utils.OSUtils;
import org.devopology.continuous.Task;
import org.devopology.continuous.TaskResult;
import org.devopology.continuous.TaskResultImpl;
import org.devopology.tools.ExecResult;
import org.devopology.tools.Toolset;

public class Git implements Task {

    public String getNamespace() {
        return getClass().getName();
    }

    public TaskResult execute(Toolset toolset) throws Exception {
        toolset.info(getNamespace() + "::execute()");

        String gitHome = toolset.getProperty("git.home");
        String gitExecutable = toolset.getFileUtils().getPath(gitHome, "git");
        String gitBranch = toolset.getProperty("git.branch");
        String gitURL = toolset.getProperty("git.url");
        String workspaceClean = toolset.getProperty("workspace.clean", "true");

        if (OSUtils.isWindows()) {
            gitExecutable = gitExecutable + ".exe";
        }

        String gitCommand = "clone";

        if ("false".equals(workspaceClean)) {
            gitCommand = "pull";
        }

        ExecResult execResult = null;

        if ("clone".equals(gitCommand)) {
            toolset.info("git " + gitCommand +  " -b " + gitBranch + " " + gitURL + " .");
            execResult = toolset.getExecUtils().execute(gitExecutable, toolset.arguments("clone", "-b" + gitBranch, gitURL, "."));
        }
        else {
            //info("pwd() = [" + pwd() + "]");
            execResult = toolset.getExecUtils().execute(gitExecutable, toolset.arguments("pull"));
        }

        toolset.info(execResult.getOutput());
        //info("git exitCode = [" + execResult.getExitCode() + "] output > " + execResult.getOutput());

        return new TaskResultImpl(this, execResult.getExitCode(), execResult.getOutput(), null);
    }
}
