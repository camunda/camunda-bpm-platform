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
package org.camunda.bpm.engine.test.bpmn.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class ParallelGatewayTest extends PluggableProcessEngineTest {

  /**
   * Case where there is a parallel gateway that splits into 3 paths of
   * execution, that are immediately joined, without any wait states in between.
   * In the end, no executions should be in the database.
   */
  @Deployment
  @Test
  public void testSplitMergeNoWaitstates() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("forkJoinNoWaitStates");
    assertTrue(processInstance.isEnded());
  }

  @Deployment
  @Test
  public void testUnstructuredConcurrencyTwoForks() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("unstructuredConcurrencyTwoForks");
    assertTrue(processInstance.isEnded());
  }

  @Deployment
  @Test
  public void testUnstructuredConcurrencyTwoJoins() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("unstructuredConcurrencyTwoJoins");
    assertTrue(processInstance.isEnded());
  }

  @Deployment
  @Test
  public void testForkFollowedByOnlyEndEvents() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("forkFollowedByEndEvents");
    assertTrue(processInstance.isEnded());
  }

  @Deployment
  @Test
  public void testNestedForksFollowedByEndEvents() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedForksFollowedByEndEvents");
    assertTrue(processInstance.isEnded());
  }

  // ACT-482
  @Deployment
  @Test
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
  @Test
  public void testReceyclingExecutionWithCallActivity() {
    runtimeService.startProcessInstanceByKey("parent-process").getId();

    // After process start we have two tasks, one from the parent and one from
    // the sub process
    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    List<Task> tasks = query.list();
    assertEquals(2, tasks.size());
    assertEquals("Another task", tasks.get(0).getName());
    assertEquals("Some Task", tasks.get(1).getName());

    // we complete the task from the parent process, the root execution is
    // receycled, the task in the sub process is still there
    taskService.complete(tasks.get(1).getId());
    tasks = query.list();
    assertEquals(1, tasks.size());
    assertEquals("Another task", tasks.get(0).getName());

    // we end the task in the sub process and the sub process instance end is
    // propagated to the parent process
    taskService.complete(tasks.get(0).getId());
    assertEquals(0, taskService.createTaskQuery().count());

    // There is a QA config without history, so we cannot work with this:
    // assertEquals(1,
    // historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).finished().count());
  }

  @Deployment
  @Test
  public void testCompletingJoin() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    assertTrue(processInstance.isEnded());
  }

  @Deployment
  @Test
  public void testAsyncParallelGateway() {

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    assertNotNull(jobDefinition);
    assertEquals("parallelJoinEnd", jobDefinition.getActivityId());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertFalse(processInstance.isEnded());

    // there are two jobs to continue the gateway:
    List<Job> list = managementService.createJobQuery().list();
    assertEquals(2, list.size());

    managementService.executeJob(list.get(0).getId());
    managementService.executeJob(list.get(1).getId());

    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Deployment
  @Test
  public void testAsyncParallelGatewayAfterScopeTask() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertFalse(processInstance.isEnded());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // there are two jobs to continue the gateway:
    List<Job> list = managementService.createJobQuery().list();
    assertEquals(2, list.size());

    managementService.executeJob(list.get(0).getId());
    managementService.executeJob(list.get(1).getId());

    assertNull(runtimeService.createProcessInstanceQuery().singleResult());
  }

  @Deployment
  @Test
  public void testCompletingJoinInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    assertTrue(processInstance.isEnded());
  }

  @Deployment
  @Test
  public void testParallelGatewayBeforeAndInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(3);

    ActivityInstance instance = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(instance.getActivityName()).isEqualTo("Process1");
    ActivityInstance[] childActivityInstances = instance.getChildActivityInstances();
    for (ActivityInstance activityInstance : childActivityInstances) {
      if (activityInstance.getActivityId().equals("SubProcess_1")) {
        ActivityInstance[] instances = activityInstance.getChildActivityInstances();
        for (ActivityInstance activityInstance2 : instances) {
          assertThat(activityInstance2.getActivityName()).isIn("Inner User Task 1", "Inner User Task 2");
        }
      } else {
        assertThat(activityInstance.getActivityName()).isEqualTo("Outer User Task");
      }
    }
  }

  @Deployment
  @Test
  public void testForkJoin() {

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");
    TaskQuery query = taskService
                        .createTaskQuery()
                        .processInstanceId(pi.getId())
                        .orderByTaskName()
                        .asc();

    List<Task> tasks = query.list();
    assertEquals(2, tasks.size());
    // the tasks are ordered by name (see above)
    Task task1 = tasks.get(0);
    assertEquals("Receive Payment", task1.getName());
    Task task2 = tasks.get(1);
    assertEquals("Ship Order", task2.getName());

    // Completing both tasks will join the concurrent executions
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    tasks = query.list();
    assertEquals(1, tasks.size());
    assertEquals("Archive Order", tasks.get(0).getName());
  }

  @Deployment
  @Test
  public void testUnbalancedForkJoin() {

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("UnbalancedForkJoin");
    TaskQuery query = taskService.createTaskQuery()
                                 .processInstanceId(pi.getId())
                                 .orderByTaskName()
                                 .asc();

    List<Task> tasks = query.list();
    assertEquals(3, tasks.size());
    // the tasks are ordered by name (see above)
    Task task1 = tasks.get(0);
    assertEquals("Task 1", task1.getName());
    Task task2 = tasks.get(1);
    assertEquals("Task 2", task2.getName());

    // Completing the first task should *not* trigger the join
    taskService.complete(task1.getId());

    // Completing the second task should trigger the first join
    taskService.complete(task2.getId());

    tasks = query.list();
    Task task3 = tasks.get(0);
    assertEquals(2, tasks.size());
    assertEquals("Task 3", task3.getName());
    Task task4 = tasks.get(1);
    assertEquals("Task 4", task4.getName());

    // Completing the remaing tasks should trigger the second join and end the process
    taskService.complete(task3.getId());
    taskService.complete(task4.getId());

    testRule.assertProcessEnded(pi.getId());
  }

  @Test
  public void testRemoveConcurrentExecutionLocalVariablesOnJoin() {
   testRule.deploy(Bpmn.createExecutableProcess("process")
      .startEvent()
      .parallelGateway("fork")
      .userTask("task1")
      .parallelGateway("join")
      .userTask("afterTask")
      .endEvent()
      .moveToNode("fork")
      .userTask("task2")
      .connectTo("join")
      .done());

    // given
    runtimeService.startProcessInstanceByKey("process");

    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      runtimeService.setVariableLocal(task.getExecutionId(), "var", "value");
    }

    // when
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // then
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());
  }

  @Deployment
  @Test
  public void testImplicitParallelGatewayAfterSignalBehavior() {
    // given
    Exception exceptionOccurred = null;
    runtimeService.startProcessInstanceByKey("process");
    Execution execution = runtimeService.createExecutionQuery()
      .activityId("service")
      .singleResult();

    // when
    try {
      runtimeService.signal(execution.getId());
    } catch (Exception e) {
      exceptionOccurred = e;
    }

    // then
    assertNull(exceptionOccurred);
    assertEquals(3, taskService.createTaskQuery().count());
  }

  @Deployment
  @Test
  public void testExplicitParallelGatewayAfterSignalBehavior() {
    // given
    runtimeService.startProcessInstanceByKey("process");
    Execution execution = runtimeService.createExecutionQuery()
      .activityId("service")
      .singleResult();

    // when
    runtimeService.signal(execution.getId());

    // then
    assertEquals(3, taskService.createTaskQuery().count());
  }
}
