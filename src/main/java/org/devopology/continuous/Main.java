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

package org.devopology.continuous;

import org.devopology.continuous.task.Git;
import org.devopology.continuous.task.Mvn;
import org.devopology.continuous.task.Prepare;
import org.devopology.continuous.task.Status;
import org.devopology.continuous.task.TaskChain;
import org.devopology.continuous.utils.OrderedProperties;
import org.devopology.tools.Toolset;
import org.devopology.tools.exception.FailureException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

public class Main extends Toolset {

    private static int maxKeyLength = 0;
    private static int maxValueLength = 0;

    public static void main(String [] args) throws Exception {
        OrderedProperties tempProperties = new OrderedProperties();
        tempProperties.load(new FileInputStream(args[0]));

        String workspace = tempProperties.getProperty("workspace");
        String name = tempProperties.getProperty("name");
        String nameEscaped = escape(name);
        tempProperties.put("name.escaped", nameEscaped);

        PrintStream printStream = new PrintStream(new FileOutputStream(workspace + File.separator + nameEscaped + ".log"));
        System.setOut(printStream);
        System.setErr(printStream);

        Main main = new Main();

        maxKeyLength = 0;
        maxValueLength = 0;
        for (Object key : tempProperties.keySet()) {
            String value = (String) tempProperties.get(key);
            if ((null != value) && (value.length() > 0)) {
                main.setProperty((String) key, value);
                maxKeyLength = Math.max(maxKeyLength, ((String) key).length());
                maxValueLength = Math.max(maxValueLength, ((String) tempProperties.get(key)).length());
            }
        }

        main.execute(args);
    }

    public Status status = new Status();

    public void publishStatus() throws Exception {
        if (null == getProperty("status")) {
            setProperty("status", Status.BUILD_UNKNOWN);
        }

        status.execute(this);
    }

    public void execute(String [] args) throws Exception {
        banner("BEGIN Main::execute()");

        try {
            Map<String, String> properties = getProperties();

            for (String key : properties.keySet()) {
                String value = getProperty(key);
                key = getStringUtils().rightPad(key, maxKeyLength);
                info(key + " = " + value);
            }

            info(BANNER_LINE);

            // Prepare everything
            new Prepare().execute(this);
            setProperty("status", Status.BUILD_IN_PROGRESS);
            publishStatus();

            TaskChain taskChain = new TaskChain();
            taskChain.addTask(new Git());
            taskChain.addTask(new Mvn());

            TaskResult taskResult = taskChain.execute(this);
            if ((null != taskResult) && (0 != taskResult.getExitCode())) {
                throw new FailureException(taskResult.getOutput());
            }

            String workspaceHome = properties.get("workspace.home");
            String apidocsDeployDestination = properties.get("apidocs.deploy.destination");
            getZipUtils().unzip(workspaceHome + File.separator + "target" + File.separator zipFilename, String destinationPath))

            setProperty("status", Status.BUILD_PASSING);
            publishStatus();
        }
        catch (Throwable t) {
            setProperty("status", Status.BUILD_FAILING);
            publishStatus();

            error("Exception", t);
        }
        finally {
            banner("END Main::execute() " + getProperty("status"), true);
        }
    }

    private static String escape(String string) {
        return string.replaceAll("[^a-zA-Z0-9\\.-]", "_");
    }
}
