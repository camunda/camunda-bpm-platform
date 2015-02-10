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

package org.camunda.bpm.engine.test.bpmn.gateway;

import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class ParallelGatewayTest extends PluggableProcessEngineTestCase {

  /**
   * Case where there is a parallel gateway that splits into 3 paths of
   * execution, that are immediately joined, without any wait states in between.
   * In the end, no executions should be in the database.
   */
  @Deployment
  public void testSplitMergeNoWaitstates() {
    ProcessInstance processInstance =
      runtimeService.startProcessInstanceByKey("forkJoinNoWaitStates");
    assertTrue(processInstance.isEnded());
  }

  @Deployment
  public void testUnstructuredConcurrencyTwoForks() {
    ProcessInstance processInstance =
      runtimeService.startProcessInstanceByKey("unstructuredConcurrencyTwoForks");
    assertTrue(processInstance.isEnded());
  }

  @Deployment
  public void testUnstructuredConcurrencyTwoJoins() {
    ProcessInstance processInstance =
      runtimeService.startProcessInstanceByKey("unstructuredConcurrencyTwoJoins");
    assertTrue(processInstance.isEnded());
  }

  @Deployment
  public void testForkFollowedByOnlyEndEvents() {
    ProcessInstance processInstance =
      runtimeService.startProcessInstanceByKey("forkFollowedByEndEvents");
    assertTrue(processInstance.isEnded());
  }

  @Deployment
  public void testNestedForksFollowedByEndEvents() {
    ProcessInstance processInstance =
      runtimeService.startProcessInstanceByKey("nestedForksFollowedByEndEvents");
    assertTrue(processInstance.isEnded());
  }

  // ACT-482
  @Deployment
  public void testNestedForkJoin() {
   String pid = runtimeService.startProcessInstanceByKey("nestedForkJoin").getId();

   // After process startm, only task 0 should be active
   TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
   List<Task> tasks = query.list();
   assertEquals(1, tasks.size());
   assertEquals("Task 0", tasks.get(0).getName());
   assertEquals(1, runtimeService.getActivityInstance(pid).getChildActivityInstances().length);

   // Completing task 0 will create Task A and B
   taskService.complete(tasks.get(0).getId());
   tasks = query.list();
   assertEquals(2, tasks.size());
   assertEquals("Task A", tasks.get(0).getName());
   assertEquals("Task B", tasks.get(1).getName());
   assertEquals(2, runtimeService.getActivityInstance(pid).getChildActivityInstances().length);

   // Completing task A should not trigger any new tasks
   taskService.complete(tasks.get(0).getId());
   tasks = query.list();
   assertEquals(1, tasks.size());
   assertEquals("Task B", tasks.get(0).getName());
   assertEquals(2, runtimeService.getActivityInstance(pid).getChildActivityInstances().length);

   // Completing task B creates tasks B1 and B2
   taskService.complete(tasks.get(0).getId());
   tasks = query.list();
   assertEquals(2, tasks.size());
   assertEquals("Task B1", tasks.get(0).getName());
   assertEquals("Task B2", tasks.get(1).getName());
   assertEquals(3, runtimeService.getActivityInstance(pid).getChildActivityInstances().length);

   // Completing B1 and B2 will activate both joins, and process reaches task C
   taskService.complete(tasks.get(0).getId());
   taskService.complete(tasks.get(1).getId());
   tasks = query.list();
   assertEquals(1, tasks.size());
   assertEquals("Task C", tasks.get(0).getName());
   assertEquals(1, runtimeService.getActivityInstance(pid).getChildActivityInstances().length);
  }

  /**
   * http://jira.codehaus.org/browse/ACT-1222
   */
  @Deployment
  public void testReceyclingExecutionWithCallActivity() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("parent-process").getId();

    // After process start we have two tasks, one from the parent and one from the sub process
    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    List<Task> tasks = query.list();
    assertEquals(2, tasks.size());
    assertEquals("Another task", tasks.get(0).getName());
    assertEquals("Some Task", tasks.get(1).getName());

    // we complete the task from the parent process, the root execution is receycled, the task in the sub process is still there
    taskService.complete(tasks.get(1).getId());
    tasks = query.list();
    assertEquals(1, tasks.size());
    assertEquals("Another task", tasks.get(0).getName());

    // we end the task in the sub process and the sub process instance end is propagated to the parent process
    taskService.complete(tasks.get(0).getId());
    assertEquals(0, taskService.createTaskQuery().count());

    // There is a QA config without history, so we cannot work with this:
    //assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).finished().count());
  }

  @Deployment
  public void testCompletingJoin() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    assertTrue(processInstance.isEnded());
  }

  @Deployment
  public void testAsyncParallelGateway() {

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    assertNotNull(jobDefinition);
    assertEquals("parallelJoinEnd", jobDefinition.getActivityId());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertFalse(processInstance.isEnded());

    // there are two jobs to continue the gateway:
    List<Job> list = managementService.createJobQuery()
      .list();
    assertEquals(2, list.size());

    managementService.executeJob(list.get(0).getId());
    managementService.executeJob(list.get(1).getId());

    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Deployment
  public void testAsyncParallelGatewayAfterScopeTask() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertFalse(processInstance.isEnded());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // there are two jobs to continue the gateway:
    List<Job> list = managementService.createJobQuery()
      .list();
    assertEquals(2, list.size());

    managementService.executeJob(list.get(0).getId());
    managementService.executeJob(list.get(1).getId());

    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Deployment
  public void testCompletingJoinInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    assertTrue(processInstance.isEnded());
  }
}
