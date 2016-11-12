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
import org.devopology.tools.Toolset;

import java.io.File;

public class Prepare implements Task {

    public String getNamespace() {
        return getClass().getName();
    }

    public TaskResult execute(Toolset toolset) throws Exception {
        toolset.info(getNamespace() + "::execute()");

        String name = toolset.getProperty("name");
        String nameEscaped = escape(name);
        toolset.setProperty("name.escaped", nameEscaped);

        String gitBranch = toolset.getProperty("git.branch");
        String gitHome = toolset.getProperty("git.home");
        String gitURL = toolset.getProperty("git.url");
        String javaHome = toolset.getProperty("java.home");
        String workspace = toolset.getProperty("workspace");

        toolset.setProperty("workspace.home", workspace + File.separator + nameEscaped);

        String workspaceClean = "true";
        toolset.setProperty("workspace.clean", workspaceClean);

        toolset.changeDirectory(workspace);

        String path = toolset.absolutePath(toolset.getFileUtils().getPath(workspace, name));
            if (!toolset.getFileUtils().exists(path)) {
                toolset.getFileUtils().forceMkdir(path);
        }
            else {
            if ("true".equals(workspaceClean)) {
                toolset.getFileUtils().deleteDirectory(path);
                toolset.getFileUtils().forceMkdir(path);
            }
        }

        toolset.changeDirectory(path);

        return new TaskResultImpl(this, 0, null, null);
    }

    private static String escape(String string) {
        return string.replaceAll("[^a-zA-Z0-9\\.-]", "_");
    }
}
