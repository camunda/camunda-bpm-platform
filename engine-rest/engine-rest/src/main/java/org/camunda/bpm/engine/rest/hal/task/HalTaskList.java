/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.hal.task;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.hal.HalCollectionResource;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.task.Task;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.UriBuilder.fromPath;

/**
 * @author Daniel Meyer
 *
 */
public class HalTaskList extends HalCollectionResource<HalTaskList> {

  public static HalTaskList generate(List<Task> tasks, long count, ProcessEngine engine) {
    return fromTaskList(tasks, count)
      .embed(HalTask.REL_ASSIGNEE, engine)
      .embed(HalTask.REL_OWNER, engine)
      .embed(HalTask.REL_PROCESS_DEFINITION, engine)
      .embed(HalTask.REL_CASE_DEFINITION, engine);
  }

  public static HalTaskList fromTaskList(List<Task> tasks, long count) {

    HalTaskList taskList = new HalTaskList();

    // embed tasks
    List<HalResource<?>> embeddedTasks = new ArrayList<HalResource<?>>();
    for (Task task : tasks) {
      embeddedTasks.add(HalTask.fromTask(task));
    }

    taskList.addEmbedded("task", embeddedTasks);

    // links
    taskList.addLink("self", fromPath(TaskRestService.PATH).build());

    taskList.count = count;

    return taskList;
  }

}
