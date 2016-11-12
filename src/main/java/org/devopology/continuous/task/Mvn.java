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

import org.devopology.continuous.Task;
import org.devopology.continuous.TaskResult;
import org.devopology.continuous.TaskResultImpl;
import org.devopology.tools.ExecResult;
import org.devopology.tools.Toolset;

import java.util.Map;

public class Mvn implements Task {

    public String getNamespace() {
        return getClass().getName();
    }

    public TaskResult execute(Toolset toolset) throws Exception {
        toolset.info(getNamespace() + "::execute()");

        String javaHome = toolset.getProperty("java.home");
        String mvnHome = toolset.getProperty("mvn.home");
        String mvnExecutable = toolset.getFileUtils().getPath(mvnHome, "bin", "mvn");
        String mvnPom = toolset.absolutePath(toolset.getProperty("mvn.pom"));
        String mvnGoals = toolset.getProperty("mvn.goals");

        Map<String, String> environmentVariableMap = toolset.getExecUtils().getEnvironmentVariableMap();
        environmentVariableMap.put("JAVA_HOME", javaHome);

        toolset.info("mvn -f " + mvnPom + " " + mvnGoals);

        int exitCode = -1;

        ExecResult execResult = toolset.getExecUtils().execute(mvnExecutable, mvnGoals.split(" "), environmentVariableMap);

        toolset.info(execResult.getOutput());

        return new TaskResultImpl(this, execResult.getExitCode(), execResult.getOutput(), null);

    }
}
