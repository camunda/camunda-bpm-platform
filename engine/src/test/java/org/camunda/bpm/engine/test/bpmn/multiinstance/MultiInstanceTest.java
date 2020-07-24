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
package org.camunda.bpm.engine.test.bpmn.multiinstance;

import static org.camunda.bpm.engine.test.bpmn.event.error.ThrowErrorDelegate.leaveExecution;
import static org.camunda.bpm.engine.test.bpmn.event.error.ThrowErrorDelegate.throwError;
import static org.camunda.bpm.engine.test.bpmn.event.error.ThrowErrorDelegate.throwException;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.event.error.ThrowErrorDelegate;
import org.camunda.bpm.engine.test.util.ActivityInstanceAssert;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author Joram Barrez
 * @author Bernd Ruecker
 */
public class MultiInstanceTest extends PluggableProcessEngineTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml"})
  @Test
  public void testSequentialUserTasks() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialUserTasks",
            CollectionUtil.singletonMap("nrOfLoops", 3));
    String procId = processInstance.getId();

    // now there is now 1 activity instance below the pi:
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    ActivityInstance expectedTree = describeActivityInstanceTree(processInstance.getProcessDefinitionId())
      .beginMiBody("miTasks")
        .activity("miTasks")
    .done();
    assertThat(tree).hasStructure(expectedTree);

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    assertEquals("kermit_0", task.getAssignee());
    taskService.complete(task.getId());

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(expectedTree);

    task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    assertEquals("kermit_1", task.getAssignee());
    taskService.complete(task.getId());

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(expectedTree);

    task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    assertEquals("kermit_2", task.getAssignee());
    taskService.complete(task.getId());

    assertNull(taskService.createTaskQuery().singleResult());
    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml"})
  @Test
  public void testSequentialUserTasksHistory() {
    runtimeService.startProcessInstanceByKey("miSequentialUserTasks",
            CollectionUtil.singletonMap("nrOfLoops", 4)).getId();
    for (int i=0; i<4; i++) {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
    }

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
      assertEquals(4, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getActivityId());
        assertNotNull(hai.getActivityName());
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
        assertNotNull(hai.getAssignee());
      }

    }

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();
      assertEquals(4, historicTaskInstances.size());
      for (HistoricTaskInstance ht : historicTaskInstances) {
        assertNotNull(ht.getAssignee());
        assertNotNull(ht.getStartTime());
        assertNotNull(ht.getEndTime());
      }

    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml"})
  @Test
  public void testSequentialUserTasksWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasks",
            CollectionUtil.singletonMap("nrOfLoops", 3)).getId();

    // Complete 1 tasks
    taskService.complete(taskService.createTaskQuery().singleResult().getId());

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml"})
  @Test
  public void testSequentialUserTasksCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasks",
            CollectionUtil.singletonMap("nrOfLoops", 10)).getId();

    // 10 tasks are to be created, but completionCondition stops them at 5
    for (int i=0; i<5; i++) {
      Task task = taskService.createTaskQuery().singleResult();
      taskService.complete(task.getId());
    }
    assertNull(taskService.createTaskQuery().singleResult());
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testSequentialMITasksExecutionListener() {
    RecordInvocationListener.reset();

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("nrOfLoops", 2);
    runtimeService.startProcessInstanceByKey("miSequentialListener", vars);

    assertEquals(1, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_START));
    assertNull(RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_END));

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertEquals(2, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_START));
    assertEquals(1, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_END));

    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertEquals(2, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_START));
    assertEquals(2, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_END));
  }

  @Deployment
  @Test
  public void testParallelMITasksExecutionListener() {
    RecordInvocationListener.reset();

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("nrOfLoops", 5);
    runtimeService.startProcessInstanceByKey("miSequentialListener", vars);

    assertEquals(5, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_START));
    assertNull(RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_END));

    List<Task> tasks = taskService.createTaskQuery().list();
    taskService.complete(tasks.get(0).getId());

    assertEquals(5, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_START));
    assertEquals(1, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_END));

    taskService.complete(tasks.get(1).getId());
    taskService.complete(tasks.get(2).getId());
    taskService.complete(tasks.get(3).getId());
    taskService.complete(tasks.get(4).getId());

    assertEquals(5, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_START));
    assertEquals(5, (int) RecordInvocationListener.INVOCATIONS.get(ExecutionListener.EVENTNAME_END));
  }

  @Deployment
  @Test
  public void testNestedSequentialUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialUserTasks").getId();

    for (int i=0; i<3; i++) {
      Task task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
      assertEquals("My Task", task.getName());
      ActivityInstance processInstance = runtimeService.getActivityInstance(procId);
      List<ActivityInstance> instancesForActivitiyId = getInstancesForActivityId(processInstance, "miTasks");
      assertEquals(1, instancesForActivitiyId.size());
      taskService.complete(task.getId());
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testParallelUserTasks() {
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    String procId = procInst.getId();

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(3, tasks.size());
    assertEquals("My Task 0", tasks.get(0).getName());
    assertEquals("My Task 1", tasks.get(1).getName());
    assertEquals("My Task 2", tasks.get(2).getName());

    ActivityInstance processInstance = runtimeService.getActivityInstance(procId);
    assertEquals(3, processInstance.getActivityInstances("miTasks").length);

    taskService.complete(tasks.get(0).getId());

    processInstance = runtimeService.getActivityInstance(procId);

    assertEquals(2, processInstance.getActivityInstances("miTasks").length);

    taskService.complete(tasks.get(1).getId());

    processInstance = runtimeService.getActivityInstance(procId);
    assertEquals(1, processInstance.getActivityInstances("miTasks").length);

    taskService.complete(tasks.get(2).getId());
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testParallelReceiveTasks() {
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("miParallelReceiveTasks");
    String procId = procInst.getId();

    assertEquals(3, runtimeService.createEventSubscriptionQuery().count());

    List<Execution> receiveTaskExecutions = runtimeService
        .createExecutionQuery().activityId("miTasks").list();

    for (Execution execution : receiveTaskExecutions) {
      runtimeService.messageEventReceived("message", execution.getId());
    }
    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelReceiveTasks.bpmn20.xml")
  @Test
  public void testParallelReceiveTasksAssertEventSubscriptionRemoval() {
    runtimeService.startProcessInstanceByKey("miParallelReceiveTasks");

    assertEquals(3, runtimeService.createEventSubscriptionQuery().count());

    List<Execution> receiveTaskExecutions = runtimeService
        .createExecutionQuery().activityId("miTasks").list();

    // signal one of the instances
    runtimeService.messageEventReceived("message", receiveTaskExecutions.get(0).getId());

    // now there should be two subscriptions left
    assertEquals(2, runtimeService.createEventSubscriptionQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelUserTasks.bpmn20.xml"})
  @Test
  public void testParallelUserTasksHistory() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }

    // Validate history
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().asc().list();
      for (int i=0; i<historicTaskInstances.size(); i++) {
        HistoricTaskInstance hi = historicTaskInstances.get(i);
        assertNotNull(hi.getStartTime());
        assertNotNull(hi.getEndTime());
        assertEquals("kermit_"+i, hi.getAssignee());
      }

      HistoricActivityInstance multiInstanceBodyInstance = historyService.createHistoricActivityInstanceQuery()
          .activityId("miTasks#multiInstanceBody").singleResult();
      assertNotNull(multiInstanceBodyInstance);
      assertEquals(pi.getId(), multiInstanceBodyInstance.getParentActivityInstanceId());

      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
      assertEquals(3, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
        assertNotNull(hai.getAssignee());
        assertEquals("userTask", hai.getActivityType());
        assertEquals(multiInstanceBodyInstance.getId(), hai.getParentActivityInstanceId());
        assertNotNull(hai.getTaskId());
      }
    }
  }

  @Deployment
  @Test
  public void testParallelUserTasksWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksWithTimer").getId();

    List<Task> tasks = taskService.createTaskQuery().list();
    taskService.complete(tasks.get(0).getId());

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testParallelUserTasksCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksCompletionCondition").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(5, tasks.size());

    // Completing 3 tasks gives 50% of tasks completed, which triggers completionCondition
    for (int i=0; i<3; i++) {
      assertEquals(5-i, taskService.createTaskQuery().count());
      taskService.complete(tasks.get(i).getId());
    }
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testParallelUserTasksBasedOnCollection() {
    List<String> assigneeList = Arrays.asList("kermit", "gonzo", "mispiggy", "fozzie", "bubba");
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksBasedOnCollection",
          CollectionUtil.singletonMap("assigneeList", assigneeList)).getId();

    List<Task> tasks = taskService.createTaskQuery().orderByTaskAssignee().asc().list();
    assertEquals(5, tasks.size());
    assertEquals("bubba", tasks.get(0).getAssignee());
    assertEquals("fozzie", tasks.get(1).getAssignee());
    assertEquals("gonzo", tasks.get(2).getAssignee());
    assertEquals("kermit", tasks.get(3).getAssignee());
    assertEquals("mispiggy", tasks.get(4).getAssignee());

    // Completing 3 tasks will trigger completioncondition
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    taskService.complete(tasks.get(2).getId());
    assertEquals(0, taskService.createTaskQuery().count());
    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelUserTasksBasedOnCollection.bpmn20.xml")
  @Test
  public void testEmptyCollectionInMI() {
    List<String> assigneeList = new ArrayList<String>();
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksBasedOnCollection",
      CollectionUtil.singletonMap("assigneeList", assigneeList)).getId();

    assertEquals(0, taskService.createTaskQuery().count());
    testRule.assertProcessEnded(procId);
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> activities = historyService
          .createHistoricActivityInstanceQuery()
          .processInstanceId(procId)
          .orderByActivityId()
          .asc().list();
      assertEquals(3, activities.size());
      assertEquals("miTasks#multiInstanceBody", activities.get(0).getActivityId());
      assertEquals("theEnd", activities.get(1).getActivityId());
      assertEquals("theStart", activities.get(2).getActivityId());
    }
  }

  @Deployment
  @Ignore
  @Test
  public void testParallelUserTasksBasedOnCollectionExpression() {
    DelegateEvent.clearEvents();

    runtimeService.startProcessInstanceByKey("process",
        Variables.createVariables().putValue("myBean", new DelegateBean()));

    List<DelegateEvent> recordedEvents = DelegateEvent.getEvents();
    assertEquals(2, recordedEvents.size());

    assertEquals("miTasks#multiInstanceBody", recordedEvents.get(0).getCurrentActivityId());
    assertEquals("miTasks#multiInstanceBody", recordedEvents.get(1).getCurrentActivityId()); // or miTasks

    DelegateEvent.clearEvents();
  }

  @Deployment
  @Test
  public void testParallelUserTasksCustomExtensions() {
    Map<String, Object> vars = new HashMap<String, Object>();
    List<String> assigneeList = Arrays.asList("kermit", "gonzo", "fozzie");
    vars.put("assigneeList", assigneeList);
    runtimeService.startProcessInstanceByKey("miSequentialUserTasks", vars);

    for (String assignee : assigneeList) {
      Task task = taskService.createTaskQuery().singleResult();
      assertEquals(assignee, task.getAssignee());
      taskService.complete(task.getId());
    }
  }

  @Deployment
  @Test
  public void testParallelUserTasksExecutionAndTaskListeners() {
    runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    Execution waitState = runtimeService.createExecutionQuery().singleResult();
    assertEquals(3, runtimeService.getVariable(waitState.getId(), "taskListenerCounter"));
    assertEquals(3, runtimeService.getVariable(waitState.getId(), "executionListenerCounter"));
  }

  @Deployment
  @Test
  public void testNestedParallelUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelUserTasks").getId();

    List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
    for (Task task : tasks) {
      assertEquals("My Task", task.getName());
      taskService.complete(task.getId());
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testSequentialScriptTasks() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put("nrOfLoops", 5);
    runtimeService.startProcessInstanceByKey("miSequentialScriptTask", vars);
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(10, sum);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialScriptTasks.bpmn20.xml")
  @Test
  public void testSequentialScriptTasksNoStackOverflow() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put("nrOfLoops", 200);
    runtimeService.startProcessInstanceByKey("miSequentialScriptTask", vars);
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(19900, sum);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialScriptTasks.bpmn20.xml"})
  @Test
  public void testSequentialScriptTasksHistory() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put("nrOfLoops", 7);
    runtimeService.startProcessInstanceByKey("miSequentialScriptTask", vars);

    // Validate history
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicInstances = historyService.createHistoricActivityInstanceQuery().activityType("scriptTask").orderByActivityId().asc().list();
      assertEquals(7, historicInstances.size());
      for (int i=0; i<7; i++) {
        HistoricActivityInstance hai = historicInstances.get(i);
        assertEquals("scriptTask", hai.getActivityType());
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
      }
    }
  }

  @Deployment
  @Test
  public void testSequentialScriptTasksCompletionCondition() {
    runtimeService.startProcessInstanceByKey("miSequentialScriptTaskCompletionCondition").getId();
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(5, sum);
  }

  @Deployment
  @Test
  public void testParallelScriptTasks() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put("nrOfLoops", 10);
    runtimeService.startProcessInstanceByKey("miParallelScriptTask", vars);
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(45, sum);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelScriptTasks.bpmn20.xml"})
  @Test
  public void testParallelScriptTasksHistory() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put("nrOfLoops", 4);
    runtimeService.startProcessInstanceByKey("miParallelScriptTask", vars);
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("scriptTask").list();
      assertEquals(4, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getStartTime());
      }
    }
  }

  @Deployment
  @Test
  public void testParallelScriptTasksCompletionCondition() {
    runtimeService.startProcessInstanceByKey("miParallelScriptTaskCompletionCondition");
    Execution waitStateExecution = runtimeService.createExecutionQuery().singleResult();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertEquals(2, sum);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelScriptTasksCompletionCondition.bpmn20.xml"})
  @Test
  public void testParallelScriptTasksCompletionConditionHistory() {
    runtimeService.startProcessInstanceByKey("miParallelScriptTaskCompletionCondition");
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("scriptTask").list();
      assertEquals(2, historicActivityInstances.size());
    }
  }

  @Deployment
  @Test
  public void testSequentialSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocess").getId();

    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    for (int i=0; i<4; i++) {
      List<Task> tasks = query.list();
      assertEquals(2, tasks.size());

      assertEquals("task one", tasks.get(0).getName());
      assertEquals("task two", tasks.get(1).getName());

      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());

      if(i != 3) {
        List<String> activities = runtimeService.getActiveActivityIds(procId);
        assertNotNull(activities);
        assertEquals(2, activities.size());
      }
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testSequentialSubProcessEndEvent() {
    // ACT-1185: end-event in subprocess causes inactivated execution
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocess").getId();

    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    for (int i=0; i<4; i++) {
      List<Task> tasks = query.list();
      assertEquals(1, tasks.size());

      assertEquals("task one", tasks.get(0).getName());

      taskService.complete(tasks.get(0).getId());

      // Last run, the execution no longer exists
      if(i != 3) {
        List<String> activities = runtimeService.getActiveActivityIds(procId);
        assertNotNull(activities);
        assertEquals(1, activities.size());
      }
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialSubProcess.bpmn20.xml"})
  @Test
  public void testSequentialSubProcessHistory() {
    runtimeService.startProcessInstanceByKey("miSequentialSubprocess");
    for (int i=0; i<4; i++) {
      List<Task> tasks = taskService.createTaskQuery().list();
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    // Validate history
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> onlySubProcessInstances = historyService.createHistoricActivityInstanceQuery().activityType("subProcess").list();
      assertEquals(4, onlySubProcessInstances.size());

      List<HistoricActivityInstance> historicInstances = historyService.createHistoricActivityInstanceQuery().activityType("subProcess").list();
      assertEquals(4, historicInstances.size());
      for (HistoricActivityInstance hai : historicInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
      }

      historicInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
      assertEquals(8, historicInstances.size());
      for (HistoricActivityInstance hai : historicInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
      }
    }
  }

  @Deployment
  @Test
  public void testSequentialSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocessWithTimer").getId();

    // Complete one subprocess
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testSequentialSubProcessCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocessCompletionCondition").getId();

    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    for (int i=0; i<3; i++) {
      List<Task> tasks = query.list();
      assertEquals(2, tasks.size());

      assertEquals("task one", tasks.get(0).getName());
      assertEquals("task two", tasks.get(1).getName());

      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testNestedSequentialSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialSubProcess").getId();

    for (int i=0; i<3; i++) {
      List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testNestedSequentialSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialSubProcessWithTimer").getId();

    for (int i=0; i<2; i++) {
      List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    // Complete one task, to make it a bit more trickier
    List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
    taskService.complete(tasks.get(0).getId());

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testParallelSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocess").getId();
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(4, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelSubProcess.bpmn20.xml"})
  @Test
  public void testParallelSubProcessHistory() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("miParallelSubprocess");

    // Validate history
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityId("miSubProcess").list();
      assertEquals(2, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getStartTime());
        // now end time is null
        assertNull(hai.getEndTime());
        assertNotNull(pi.getId(), hai.getParentActivityInstanceId());
      }
    }

    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }

    // Validate history
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityId("miSubProcess").list();
      assertEquals(2, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
        assertNotNull(pi.getId(), hai.getParentActivityInstanceId());
      }
    }
  }

  @Deployment
  @Test
  public void testParallelSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessWithTimer").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(6, tasks.size());

    // Complete two tasks
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testParallelSubProcessCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessCompletionCondition").getId();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(4, tasks.size());

    // get activities of a single subprocess
    ActivityInstance[] taskActivities = runtimeService.getActivityInstance(procId)
      .getActivityInstances("miSubProcess")[0]
      .getChildActivityInstances();

    for (ActivityInstance taskActivity : taskActivities) {
      Task task = taskService.createTaskQuery().activityInstanceIdIn(taskActivity.getId()).singleResult();
      taskService.complete(task.getId());
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testParallelSubProcessAllAutomatic() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessAllAutomatics",
            CollectionUtil.singletonMap("nrOfLoops", 5)).getId();
    Execution waitState = runtimeService.createExecutionQuery().singleResult();
    assertEquals(10, runtimeService.getVariable(waitState.getId(), "sum"));

    runtimeService.signal(waitState.getId());
    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelSubProcessAllAutomatic.bpmn20.xml"})
  @Test
  public void testParallelSubProcessAllAutomaticCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessAllAutomatics",
            CollectionUtil.singletonMap("nrOfLoops", 10)).getId();
    Execution waitState = runtimeService.createExecutionQuery().singleResult();
    assertEquals(12, runtimeService.getVariable(waitState.getId(), "sum"));

    runtimeService.signal(waitState.getId());
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testNestedParallelSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelSubProcess").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(8, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testNestedParallelSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelSubProcess").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(12, tasks.size());

    for (int i=0; i<3; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialCallActivity.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml"})
  @Test
  public void testSequentialCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialCallActivity").getId();

    for (int i=0; i<3; i++) {
      List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
      assertEquals(2, tasks.size());
      assertEquals("task one", tasks.get(0).getName());
      assertEquals("task two", tasks.get(1).getName());
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialCallActivityWithList.bpmn20.xml")
  @Test
  public void testSequentialCallActivityWithList() {
    ArrayList<String> list = new ArrayList<String>();
    list.add("one");
    list.add("two");

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("list", list);

    String procId = runtimeService.startProcessInstanceByKey("parentProcess", variables).getId();

    Task task1 = taskService.createTaskQuery().processVariableValueEquals("element", "one").singleResult();
    Task task2 = taskService.createTaskQuery().processVariableValueEquals("element", "two").singleResult();

    assertNotNull(task1);
    assertNotNull(task2);

    HashMap<String, Object> subVariables = new HashMap<String, Object>();
    subVariables.put("x", "y");

    taskService.complete(task1.getId(), subVariables);
    taskService.complete(task2.getId(), subVariables);

    Task task3 = taskService.createTaskQuery().processDefinitionKey("midProcess").singleResult();
    assertNotNull(task3);
    taskService.complete(task3.getId());

    Task task4 = taskService.createTaskQuery().processDefinitionKey("parentProcess").singleResult();
    assertNotNull(task4);
    taskService.complete(task4.getId());

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialCallActivityWithTimer.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  @Test
  public void testSequentialCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialCallActivityWithTimer").getId();

    // Complete first subprocess
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task one", tasks.get(0).getName());
    assertEquals("task two", tasks.get(1).getName());
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  @Test
  public void testParallelCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelCallActivity").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(12, tasks.size());
    for (int i = 0; i < tasks.size(); i++) {
      taskService.complete(tasks.get(i).getId());
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivity.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  @Test
  public void testParallelCallActivityHistory() {
    runtimeService.startProcessInstanceByKey("miParallelCallActivity");
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(12, tasks.size());
    for (int i = 0; i < tasks.size(); i++) {
      taskService.complete(tasks.get(i).getId());
    }

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      // Validate historic processes
      List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
      assertEquals(7, historicProcessInstances.size()); // 6 subprocesses + main process
      for (HistoricProcessInstance hpi : historicProcessInstances) {
        assertNotNull(hpi.getStartTime());
        assertNotNull(hpi.getEndTime());
      }

      // Validate historic activities
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("callActivity").list();
      assertEquals(6, historicActivityInstances.size());
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertNotNull(hai.getStartTime());
        assertNotNull(hai.getEndTime());
      }
    }

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      // Validate historic tasks
      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();
      assertEquals(12, historicTaskInstances.size());
      for (HistoricTaskInstance hti : historicTaskInstances) {
        assertNotNull(hti.getStartTime());
        assertNotNull(hti.getEndTime());
        assertNotNull(hti.getAssignee());
        assertEquals("completed", hti.getDeleteReason());
      }
    }
  }


  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivityWithTimer.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  @Test
  public void testParallelCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelCallActivity").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(6, tasks.size());
    for (int i = 0; i < 2; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedSequentialCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  @Test
  public void testNestedSequentialCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialCallActivity").getId();

    for (int i=0; i<4; i++) {
      List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
      assertEquals(2, tasks.size());
      assertEquals("task one", tasks.get(0).getName());
      assertEquals("task two", tasks.get(1).getName());
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedSequentialCallActivityWithTimer.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  @Test
  public void testNestedSequentialCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialCallActivityWithTimer").getId();

    // first instance
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task one", tasks.get(0).getName());
    assertEquals("task two", tasks.get(1).getName());
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // one task of second instance
    tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());
    taskService.complete(tasks.get(0).getId());

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedParallelCallActivity.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  @Test
  public void testNestedParallelCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelCallActivity").getId();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(14, tasks.size());
    for (int i = 0; i < 14; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedParallelCallActivityWithTimer.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  @Test
  public void testNestedParallelCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelCallActivityWithTimer").getId();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(4, tasks.size());
    for (int i = 0; i < 3; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());

    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedParallelCallActivityCompletionCondition.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  @Test
  public void testNestedParallelCallActivityCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelCallActivityCompletionCondition").getId();

    assertEquals(8, taskService.createTaskQuery().count());

    for (int i = 0; i < 2; i++) {
      ProcessInstance nextSubProcessInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("externalSubProcess").listPage(0, 1).get(0);
      List<Task> tasks = taskService.createTaskQuery().processInstanceId(nextSubProcessInstance.getId()).list();
      assertEquals(2, tasks.size());
      for (Task task : tasks) {
        taskService.complete(task.getId());
      }
    }

    testRule.assertProcessEnded(procId);
  }

  // ACT-764
  @Deployment
  @Test
  public void testSequentialServiceTaskWithClass() {
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("multiInstanceServiceTask", CollectionUtil.singletonMap("result", 5));
    Integer result = (Integer) runtimeService.getVariable(procInst.getId(), "result");
    assertEquals(160, result.intValue());

    runtimeService.signal(procInst.getId());
    testRule.assertProcessEnded(procInst.getId());
  }

  @Deployment
  @Test
  public void testSequentialServiceTaskWithClassAndCollection() {
    Collection<Integer> items = Arrays.asList(1,2,3,4,5,6);
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("result", 1);
    vars.put("items", items);

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("multiInstanceServiceTask", vars);
    Integer result = (Integer) runtimeService.getVariable(procInst.getId(), "result");
    assertEquals(720, result.intValue());

    runtimeService.signal(procInst.getId());
    testRule.assertProcessEnded(procInst.getId());
  }

  // ACT-901
  @Deployment
  @Test
  public void testAct901() {

    Date startTime = ClockUtil.getCurrentTime();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("multiInstanceSubProcess");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc().list();

    ClockUtil.setCurrentTime(new Date(startTime.getTime() + 61000L)); // timer is set to one minute
    List<Job> timers = managementService.createJobQuery().list();
    assertEquals(5, timers.size());

    // Execute all timers one by one (single thread vs thread pool of job executor, which leads to optimisticlockingexceptions!)
    for (Job timer : timers) {
      managementService.executeJob(timer.getId());
    }

    // All tasks should be canceled
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc().list();
    assertEquals(0, tasks.size());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.callActivityWithBoundaryErrorEvent.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.throwingErrorEventSubProcess.bpmn20.xml" })
  @Test
  public void testMultiInstanceCallActivityWithErrorBoundaryEvent() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("assignees", Arrays.asList("kermit", "gonzo"));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variableMap);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());

    // finish first call activity with error
    variableMap = new HashMap<String, Object>();
    variableMap.put("done", false);
    taskService.complete(tasks.get(0).getId(), variableMap);

    tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    taskService.complete(tasks.get(0).getId());

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey("process").list();
    assertEquals(0, processInstances.size());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.callActivityWithBoundaryErrorEventSequential.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.throwingErrorEventSubProcess.bpmn20.xml" })
  @Test
  public void testSequentialMultiInstanceCallActivityWithErrorBoundaryEvent() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("assignees", Arrays.asList("kermit", "gonzo"));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variableMap);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    // finish first call activity with error
    variableMap = new HashMap<String, Object>();
    variableMap.put("done", false);
    taskService.complete(tasks.get(0).getId(), variableMap);

    tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    taskService.complete(tasks.get(0).getId());

    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedMultiInstanceTasks.bpmn20.xml"})
  @Test
  public void testNestedMultiInstanceTasks() {
    List<String> processes = Arrays.asList("process A", "process B");
    List<String> assignees = Arrays.asList("kermit", "gonzo");
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("subProcesses", processes);
    variableMap.put("assignees", assignees);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miNestedMultiInstanceTasks", variableMap);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(processes.size() * assignees.size(), tasks.size());

    for (Task t : tasks) {
      taskService.complete(t.getId());
    }

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey("miNestedMultiInstanceTasks").list();
    assertEquals(0, processInstances.size());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedMultiInstanceTasks.bpmn20.xml"})
  @Test
  public void testNestedMultiInstanceTasksActivityInstance() {
    List<String> processes = Arrays.asList("process A", "process B");
    List<String> assignees = Arrays.asList("kermit", "gonzo");
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("subProcesses", processes);
    variableMap.put("assignees", assignees);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miNestedMultiInstanceTasks", variableMap);

    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
    ActivityInstanceAssert.assertThat(activityInstance)
    .hasStructure(
        ActivityInstanceAssert
        .describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("subprocess1")
          .beginScope("subprocess1")
            .beginMiBody("miTasks")
              .activity("miTasks")
              .activity("miTasks")
            .endScope()
          .endScope()
          .beginScope("subprocess1")
            .beginMiBody("miTasks")
              .activity("miTasks")
              .activity("miTasks")
            .endScope()
          .endScope()
        .done());

  }


  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelUserTasks.bpmn20.xml"})
  @Test
  public void testActiveExecutionsInParallelTasks() {
    runtimeService.startProcessInstanceByKey("miParallelUserTasks").getId();

    ProcessInstance instance = runtimeService.createProcessInstanceQuery().singleResult();

    List<Execution> executions = runtimeService.createExecutionQuery().list();
    assertEquals(5, executions.size());

    for (Execution execution : executions) {
      ExecutionEntity entity = (ExecutionEntity) execution;

      if (!entity.getId().equals(instance.getId()) && !entity.getParentId().equals(instance.getId())) {
        // child executions
        assertTrue(entity.isActive());
      } else {
        // process instance and scope execution
        assertFalse(entity.isActive());
      }
    }
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownBySequentialAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownByExecuteOfSequentialAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess", throwException()).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownBySequentialAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownByExecuteOfSequentialAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess", throwError()).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownBySequentialAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownBySignalOfSequentialAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess").getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    // signal 2 times to execute first sequential behaviors
    runtimeService.setVariables(pi, leaveExecution());
    runtimeService.signal(runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult().getId());
    runtimeService.setVariables(pi, leaveExecution());
    runtimeService.signal(runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult().getId());

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult();
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwException());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownBySequentialAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownBySignalOfSequentialAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess").getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    // signal 2 times to execute first sequential behaviors
    runtimeService.setVariables(pi, leaveExecution());
    runtimeService.signal(runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult().getId());
    runtimeService.setVariables(pi, leaveExecution());
    runtimeService.signal(runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult().getId());

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult();
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwError());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownByParallelAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownByExecuteOfParallelAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess", throwException()).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownByParallelAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownByExecuteOfParallelAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess", throwError()).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownByParallelAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownBySignalOfParallelAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess").getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").list().get(3);
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwException());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownByParallelAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownBySignalOfParallelAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess").getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").list().get(3);
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwError());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownBySequentialDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownByExecuteOfSequentialDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    variables.putAll(throwException());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownBySequentialDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownByExecuteOfSequentialDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    variables.putAll(throwError());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownBySequentialDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownBySignalOfSequentialDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    // signal 2 times to execute first sequential behaviors
    runtimeService.setVariables(pi, leaveExecution());
    runtimeService.signal(runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult().getId());
    runtimeService.setVariables(pi, leaveExecution());
    runtimeService.signal(runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult().getId());

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult();
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwException());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownBySequentialDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownBySignalOfSequentialDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    // signal 2 times to execute first sequential behaviors
    runtimeService.setVariables(pi, leaveExecution());
    runtimeService.signal(runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult().getId());
    runtimeService.setVariables(pi, leaveExecution());
    runtimeService.signal(runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult().getId());

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult();
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwError());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownByParallelDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownByExecuteOfParallelDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    variables.putAll(throwException());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownByParallelDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownByExecuteOfParallelDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    variables.putAll(throwError());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownByParallelDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownBySignalOfParallelDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").list().get(3);
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwException());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/multiinstance/MultiInstanceTest.testCatchErrorThrownByParallelDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownBySignalOfParallelDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").list().get(3);
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwError());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }
}
