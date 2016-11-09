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
import java.nio.charset.StandardCharsets;

public class Status implements Task {

    public final static String BUILD_UNKNOWN = "BUILD_UNKNOWN";
    public final static String BUILD_IN_PROGRESS = "BUILD_IN_PROGRESS";
    public final static String BUILD_PASSING = "BUILD_PASSING";
    public final static String BUILD_FAILING = "BUILD_FAILING";

    private final static String BUILD_UNKNOWN_SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"102\" height=\"20\"><linearGradient id=\"b\" x2=\"0\" y2=\"100%\"><stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/><stop offset=\"1\" stop-opacity=\".1\"/></linearGradient><mask id=\"a\"><rect width=\"102\" height=\"20\" rx=\"3\" fill=\"#fff\"/></mask><g mask=\"url(#a)\"><path fill=\"#555\" d=\"M0 0h37v20H0z\"/><path fill=\"#9f9f9f\" d=\"M37 0h65v20H37z\"/><path fill=\"url(#b)\" d=\"M0 0h102v20H0z\"/></g><g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\"><text x=\"18.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">build</text><text x=\"18.5\" y=\"14\">build</text><text x=\"68.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">unknown</text><text x=\"68.5\" y=\"14\">unknown</text></g></svg>";
    private final static String BUILD_IN_PROGRESS_SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"102\" height=\"20\"><linearGradient id=\"b\" x2=\"0\" y2=\"100%\"><stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"/><stop offset=\"1\" stop-opacity=\".1\"/></linearGradient><mask id=\"a\"><rect width=\"102\" height=\"20\" rx=\"3\" fill=\"#fff\"/></mask><g mask=\"url(#a)\"><path fill=\"#555\" d=\"M0 0h37v20H0z\"/><path fill=\"#9f9f9f\" d=\"M37 0h65v20H37z\"/><path fill=\"url(#b)\" d=\"M0 0h102v20H0z\"/></g><g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\"><text x=\"18.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">build</text><text x=\"18.5\" y=\"14\">build</text><text x=\"68.5\" y=\"15\" fill=\"#010101\" fill-opacity=\".3\">in progress</text><text x=\"68.5\" y=\"14\">in progress</text></g></svg>";
    private final static String BUILD_PASSING_SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"88\" height=\"20\"><g shape-rendering=\"crispEdges\"><path fill=\"#555\" d=\"M0 0h37v20H0z\"/><path fill=\"#97CA00\" d=\"M37 0h51v20H37z\"/></g><g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\"><text x=\"18.5\" y=\"14\">build</text><text x=\"61.5\" y=\"14\">passing</text></g></svg>";
    private final static String BUILD_FAILING_SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"80\" height=\"20\"><g shape-rendering=\"crispEdges\"><path fill=\"#555\" d=\"M0 0h37v20H0z\"/><path fill=\"#e05d44\" d=\"M37 0h43v20H37z\"/></g><g fill=\"#fff\" text-anchor=\"middle\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"11\"><text x=\"18.5\" y=\"14\">build</text><text x=\"57.5\" y=\"14\">failing</text></g></svg>";

    public String getNamespace() {
        return getClass().getName();
    }

    public TaskResult execute(Toolset toolset) throws Exception {
        //toolset.info(getNamespace() + "::execute()");

        String workspace = toolset.getProperty("workspace");
        String name = toolset.getProperty("name");
        String nameEscaped = escape(name);
        toolset.setProperty("name.escaped", nameEscaped);
        String statusFilename = toolset.absolutePath(workspace + File.separator + nameEscaped + ".status.svg");
        String status = toolset.getProperty("status", null);

        if (BUILD_PASSING.equals(status)) {
            toolset.getFileUtils().writeStringToFile(statusFilename, BUILD_PASSING_SVG, StandardCharsets.UTF_8);
        }
        else if (BUILD_FAILING.equals(status)) {
            toolset.getFileUtils().writeStringToFile(statusFilename, BUILD_FAILING_SVG, StandardCharsets.UTF_8);
        }
        else if (BUILD_IN_PROGRESS.equals(status)) {
            toolset.getFileUtils().writeStringToFile(statusFilename, BUILD_IN_PROGRESS_SVG, StandardCharsets.UTF_8);
        }
        else {
            toolset.getFileUtils().writeStringToFile(statusFilename, BUILD_UNKNOWN_SVG, StandardCharsets.UTF_8);
        }

        return new TaskResultImpl(this, 0, null, null);
    }

    private static String escape(String string) {
        return string.replaceAll("[^a-zA-Z0-9\\.-]", "_");
    }
}
