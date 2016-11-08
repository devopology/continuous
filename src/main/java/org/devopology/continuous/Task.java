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

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.devopology.tools.ExecResult;
import org.devopology.tools.JSONUtils;
import org.devopology.tools.Toolset;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class Task extends Toolset {

    private final static String BUILD_IN_PROGRESS = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"102\" height=\"20\"><linearGradient id=\"b\" x2=\"0\" y2=\"100%\"><stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/><stop offset=\"1\" stop-opacity=\".1\"/></linearGradient><mask id=\"a\"><rect width=\"102\" height=\"20\" rx=\"3\" fill=\"#fff\"/></mask><g mask=\"url(#a)\"><path fill=\"#555\" d=\"M0 0h37v20H0z\"/><path fill=\"#9f9f9f\" d=\"M37 0h65v20H37z\"/><path fill=\"url(#b)\" d=\"M0 0h102v20H0z\"/></g><g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\"><text x=\"18.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">build</text><text x=\"18.5\" y=\"14\">build</text><text x=\"68.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">in progress</text><text x=\"68.5\" y=\"14\">in progress</text></g></svg>";
    private final static String BUILD_PASSING = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"88\" height=\"20\"><g shape-rendering=\"crispEdges\"><path fill=\"#555\" d=\"M0 0h37v20H0z\"/><path fill=\"#97CA00\" d=\"M37 0h51v20H37z\"/></g><g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\"><text x=\"18.5\" y=\"14\">build</text><text x=\"61.5\" y=\"14\">passing</text></g></svg>";
    private final static String BUILD_FAILING = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"80\" height=\"20\"><g shape-rendering=\"crispEdges\"><path fill=\"#555\" d=\"M0 0h37v20H0z\"/><path fill=\"#e05d44\" d=\"M37 0h43v20H37z\"/></g><g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\"><text x=\"18.5\" y=\"14\">build</text><text x=\"57.5\" y=\"14\">failing</text></g></svg>";

    private final static JSONParser jsonParser = new JSONParser();

    public static void main(String [] args) throws Exception {
        String json = readFile(args[0], StandardCharsets.UTF_8);
        JSONObject jsonObject = parseJSONObject(json);
        String workspace = (String) jsonObject.get("workspace");
        String name = (String) jsonObject.get("name");
        name = escape(name);

        File outputFilename = new File(workspace + File.separator + name + ".log");
        File outputFilenameParent = outputFilename.getParentFile();

        if (false == outputFilenameParent.exists()) {
            outputFilenameParent.mkdirs();
        }

        PrintStream printStream = new PrintStream(new FileOutputStream(workspace + File.separator + name + ".log"), true);
        System.setOut(printStream);
        System.setErr(printStream);

        new Task().execute(args);
    }

    public static String readFile(String path, Charset encoding) throws IOException
    {
        byte [] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * Method to parse a String as a JSONObject
     *
     * @param json
     * @return JSONOBject
     */
    public static JSONObject parseJSONObject(String json) throws IOException {
        try {
            return (JSONObject) jsonParser.parse(json);
        }
        catch (Throwable t) {
            throw new IOException("parseJSONObject() Exception ", t);
        }
    }

    private Map taskDefinitionMap = null;

    /*
    private String name = null;
    private String gitBranch = null;
    private String gitCommand = "clone";
    private String gitHome = null;
    private String gitURL = null;
    private String javaHome = null;
    private String workspace = null;
    private String workspaceClean = "true";
    private String statusFile = null;
    */

    public void setProperty(String key, String value) {
        taskDefinitionMap.put(key, value);
    }

    public String getProperty(String key) {
        return (String) taskDefinitionMap.get(key);
    }

    public String getProperty(String key, String defaultValue) {
        String result = getProperty(key);
        if (null == result) {
            result = defaultValue;
        }
        return result;
    }

    public void execute(String [] args) throws Exception {
        String status = "FAILURE";
        banner("BEGIN TASK");

        try {
            String taskDefinitionFilename = absolutePath(args[0]);
            int maxKeyLength = "taskDefinitionFilename".length();
            int maxValueLength = taskDefinitionFilename.length();

            String taskDefinitionJSON = getFileUtils().readFileToString(taskDefinitionFilename);
            taskDefinitionMap = getJsonUtils().parseMap(taskDefinitionJSON, JSONUtils.LINKED_HASHMAP_CONTAINER_FACTORY);

            // Code to put "taskDefinitionFilename" as the first item in the list
            // Not required from a functionality standpoint, but looks good in the output
            LinkedHashMap newmap = new LinkedHashMap(taskDefinitionMap);
            taskDefinitionMap.clear();
            taskDefinitionMap.put("taskDefinitionFilename", taskDefinitionFilename);
            taskDefinitionMap.putAll(newmap);

            for (Object oentry : taskDefinitionMap.entrySet()) {
                Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) oentry;

                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (key.length() > maxKeyLength) {
                    maxKeyLength = key.length();
                }

                if (null != value) {
                    if (value.length() > maxValueLength) {
                        maxValueLength = value.length();
                    }
                }

                info("key [ " + getStringUtils().leftPad(key, maxKeyLength) + " ] = [ " + getStringUtils().rightPad(value, maxValueLength) + " ]");
            }

            info(BANNER_LINE);

            int exitCode = prepare();

            if (0 == exitCode) {
                exitCode = gitStep();
            }
            else {
                publishStatus(BUILD_FAILING);
            }

            if (0 == exitCode) {
                exitCode = mvnStep();
            }
            else {
                publishStatus(BUILD_FAILING);
            }

            if (0 == exitCode) {
                exitCode = publishAPIDocsStep();
            }

            if (0 == exitCode) {
                exitCode = publishAPIDocsStep();
            }

            if (0 == exitCode) {
                publishStatus(BUILD_PASSING);
                status = "SUCCESS";
            }
            else {
                publishStatus(BUILD_FAILING);
            }
        }
        catch (Throwable t) {
            error("Exception", t);
        }
        finally {
            banner("END TASK " + status, true);
        }
    }

    public int prepare() throws Exception {
        //info("prepare ...");

        String name = getProperty("name");
        name = escape(name);

        setProperty("name.escaped", name);

        publishStatus(BUILD_IN_PROGRESS);

        String gitBranch = getProperty("git.branch");
        String gitHome = getProperty("git.home");
        String gitURL = getProperty("git.url");
        String javaHome = getProperty("java.home");
        String workspace = getProperty("workspace");

        String workspaceClean = "true";
        setProperty("workspace.clean", workspaceClean);

        changeDirectory(workspace);

        String path = absolutePath(getFileUtils().getPath(workspace, name));
        if (!getFileUtils().exists(path)) {
            getFileUtils().forceMkdir(path);
        }
        else {
            if ("true".equals(workspaceClean)) {
                getFileUtils().deleteDirectory(path);
                getFileUtils().forceMkdir(path);
            }
        }

        changeDirectory(path);
        //info("expanded workspace = [ " + pwd() + " ]");

        return 0;
    }

    public int gitStep() throws Exception {
        //info("gitStep()");

        String gitHome = getProperty("git.home");
        String gitExecutable = getFileUtils().getPath(gitHome, "git");
        String gitBranch = getProperty("git.branch");
        String gitURL = getProperty("git.url");
        String workspaceClean = getProperty("workspace.clean", "true");

        if (OSUtils.isWindows()) {
            gitExecutable = gitExecutable + ".exe";
        }

        String gitCommand = "clone";

        if ("false".equals(workspaceClean)) {
            gitCommand = "pull";
        }

        ExecResult execResult = null;

        if ("clone".equals(gitCommand)) {
            info("git " + gitCommand +  " -b " + gitBranch + " " + gitURL + " .");
            execResult = getExecUtils().execute(gitExecutable, arguments("clone", "-b" + gitBranch, gitURL, "."));
        }
        else {
            //info("pwd() = [" + pwd() + "]");
            execResult = getExecUtils().execute(gitExecutable, arguments("pull"));
        }

        info(execResult.getOutput());
        //info("git exitCode = [" + execResult.getExitCode() + "] output > " + execResult.getOutput());

        return execResult.getExitCode();
    }

    public int mvnStep() throws Exception {
        //info("mvnStep()");

        String mvnPom = absolutePath(getProperty("mvn.pom"));
        String mvnGoals = getProperty("mvn.goals");
        info("mvn -f " + mvnPom + " " + mvnGoals);

        int exitCode = -1;
        String javaHome = getProperty("java.home");
        String mvnHome = getProperty("mvn.home");

        InvocationRequest request = new DefaultInvocationRequest();
        request.setJavaHome(new File(javaHome));
        request.setBaseDirectory(new File(getCurrentDirectory().getPath()));
        request.setPomFile(new File(mvnPom));
        request.setGoals(this.stringToList(mvnGoals));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mvnHome));
        InvocationResult invocationResult = invoker.execute(request);

        if (null != invocationResult.getExecutionException()) {
            error("mvn exception -> " + invocationResult.getExecutionException());
        }
        else {
            exitCode = invocationResult.getExitCode();

            if (0 != exitCode) {
                error("mvn exitCode = [" + exitCode + "]");
            }
        }

        return exitCode;
    }

    public int publishAPIDocsStep() throws Exception {
        String workspace = getProperty("workspace");
        String nameEscaped = getProperty("name.escaped");
        String apiDocsSource = absolutePath(getFileUtils().getPath(workspace, nameEscaped, "target", "apidocs"));

        if (getFileUtils().exists(apiDocsSource)) {

            String apiDocsDeployDestination = getProperty("apidocs.deploy.destination");

            if (null != apiDocsDeployDestination) {
                if (!getFileUtils().exists(apiDocsDeployDestination)) {
                    getFileUtils().forceMkdir(apiDocsDeployDestination);
                }

                getFileUtils().copyDirectory(apiDocsSource, apiDocsDeployDestination);
            }
        }

        return 0;
    }

    public void publishStatus(String status) throws IOException {
        String workspace = getProperty("workspace");
        String name = getProperty("name.escaped");
        String statusFilename = absolutePath(workspace + File.separator + name + ".status.svg");

        //info("statusFilename = [" + statusFilename + "]");
        getFileUtils().writeStringToFile(statusFilename, status, StandardCharsets.UTF_8);
    }

    private static String escape(String string) {
        return string.replaceAll("[^a-zA-Z0-9\\.-]", "_");
    }
}
