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
import org.devopology.tools.Toolset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Doug on 11/8/2016.
 */
public class TaskChain {

    private List<Task> taskList = new ArrayList<Task>();

    public void addTask(Task task) {
        taskList.add(task);
    }

    public String getNamespace() {
        return getClass().getName();
    }

    public TaskResult execute(Toolset toolset) throws Exception {
        for (Task task : taskList) {
            TaskResult taskResult = task.execute(toolset);
            if (0 != taskResult.getExitCode()) {
                return taskResult;
            }
        }

        return null;
    }
}
