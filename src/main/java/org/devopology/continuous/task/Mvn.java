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

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.devopology.continuous.Task;
import org.devopology.continuous.TaskResult;
import org.devopology.continuous.TaskResultImpl;
import org.devopology.tools.Toolset;

import java.io.File;

public class Mvn implements Task {

    public String getNamespace() {
        return getClass().getName();
    }

    public TaskResult execute(Toolset toolset) throws Exception {
        toolset.info(getNamespace() + "::execute()");

        String mvnPom = toolset.absolutePath(toolset.getProperty("mvn.pom"));
        String mvnGoals = toolset.getProperty("mvn.goals");
        toolset.info("mvn -f " + mvnPom + " " + mvnGoals);

        int exitCode = -1;
        String javaHome = toolset.getProperty("java.home");
        String mvnHome = toolset.getProperty("mvn.home");

        InvocationRequest request = new DefaultInvocationRequest();
        request.setJavaHome(new File(javaHome));
        request.setBaseDirectory(new File(toolset.getCurrentDirectory().getPath()));
        request.setPomFile(new File(mvnPom));
        request.setGoals(toolset.stringToList(mvnGoals));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mvnHome));
        InvocationResult invocationResult = invoker.execute(request);

        if (null != invocationResult.getExecutionException()) {
            toolset.error("mvn exception -> " + invocationResult.getExecutionException());
            toolset.setProperty("status", Status.BUILD_FAILING);

            return new TaskResultImpl(this, -1, "Exception", invocationResult.getExecutionException());
        }
        else {
            exitCode = invocationResult.getExitCode();

            if (0 != exitCode) {
                toolset.error("mvn exitCode = [" + exitCode + "]");
                toolset.setProperty("status", Status.BUILD_FAILING);

                return new TaskResultImpl(this, -1, "Exception", invocationResult.getExecutionException());
            }
        }

        toolset.setProperty("status", Status.BUILD_PASSING);
        return new TaskResultImpl(this, 0, null, null);
    }
}
