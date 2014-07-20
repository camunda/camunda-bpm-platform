/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.hal.task;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.task.Task;

import static javax.ws.rs.core.UriBuilder.fromResource;

/**
 * @author Daniel Meyer
 *
 */
public class HalTaskList extends HalResource<HalTaskList> {

  protected long count = 0;

  public long getCount() {
    return count;
  }

  public static HalTaskList fromTaskList(List<Task> tasks, long count) {

    HalTaskList taskList = new HalTaskList();

    // embedd tasks
    List<HalResource<?>> embeddedTasks = new ArrayList<HalResource<?>>();
    for (Task task : tasks) {
      embeddedTasks.add(HalTask.fromTask(task));
    }

    taskList.addEmbedded("tasks", embeddedTasks);

    // links
    taskList.addLink("self", fromResource(TaskRestService.class).build());

    taskList.count = count;

    return taskList;
  }

}
