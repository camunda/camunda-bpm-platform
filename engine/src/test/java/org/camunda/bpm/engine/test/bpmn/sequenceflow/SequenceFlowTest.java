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
package org.camunda.bpm.engine.test.bpmn.sequenceflow;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class SequenceFlowTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testTakeAllOutgoingFlowsFromNonScopeTask() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("testProcess");

    // when
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // then
    assertEquals(2, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("task2").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("task3").count());

    for (Task followUpTask : taskService.createTaskQuery().list()) {
      taskService.complete(followUpTask.getId());
    }

    assertProcessEnded(instance.getId());

  }

  @Deployment
  public void testTakeAllOutgoingFlowsFromScopeTask() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("testProcess");

    // when
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // then
    assertEquals(2, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("task2").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("task3").count());

    for (Task followUpTask : taskService.createTaskQuery().list()) {
      taskService.complete(followUpTask.getId());
    }

    assertProcessEnded(instance.getId());
  }
}
