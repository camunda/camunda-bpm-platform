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
package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstantiationAtActivitiesTest extends PluggableProcessEngineTest {

  protected static final String PARALLEL_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.parallelGateway.bpmn20.xml";
  protected static final String EXCLUSIVE_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGateway.bpmn20.xml";
  protected static final String SUBPROCESS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.subprocess.bpmn20.xml";
  protected static final String LISTENERS_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstantiationAtActivitiesTest.listeners.bpmn20.xml";
  protected static final String IO_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstantiationAtActivitiesTest.ioMappings.bpmn20.xml";
  protected static final String ASYNC_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.exclusiveGatewayAsyncTask.bpmn20.xml";
  protected static final String SYNC_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstantiationAtActivitiesTest.synchronous.bpmn20.xml";

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  @Test
  public void testSingleActivityInstantiation() {
    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("exclusiveGateway")
      .startBeforeActivity("task1")
      .execute();

    // then
    assertNotNull(instance);

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("task1")
      .done());

    // and it is possible to end the process
    completeTasksInOrder("task1");
    testRule.assertProcessEnded(instance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  @Test
  public void testSingleActivityInstantiationById() {
    // given
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceById(processDefinitionId)
      .startBeforeActivity("task1")
      .execute();

    // then
    assertNotNull(instance);

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("task1")
      .done());

    // and it is possible to end the process
    completeTasksInOrder("task1");
    testRule.assertProcessEnded(instance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  @Test
  public void testSingleActivityInstantiationSetBusinessKey() {
    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("exclusiveGateway")
      .businessKey("businessKey")
      .startBeforeActivity("task1")
      .execute();

    // then
    assertNotNull(instance);
    assertEquals("businessKey", instance.getBusinessKey());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  @Test
  public void testSingleActivityInstantiationSetCaseInstanceId() {
    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("exclusiveGateway")
      .caseInstanceId("caseInstanceId")
      .startBeforeActivity("task1")
      .execute();

    // then
    assertNotNull(instance);
    assertEquals("caseInstanceId", instance.getCaseInstanceId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  @Test
  public void testStartEventInstantiation() {
    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("exclusiveGateway")
      .startBeforeActivity("theStart")
      .execute();

    // then
    assertNotNull(instance);

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("task1")
      .done());

    // and it is possible to end the process
    completeTasksInOrder("task1");
    testRule.assertProcessEnded(instance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  @Test
  public void testStartEventInstantiationWithVariables() {
    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("exclusiveGateway")
      .startBeforeActivity("theStart")
      .setVariable("aVariable", "aValue")
      .execute();

    // then
    assertNotNull(instance);

    assertEquals("aValue", runtimeService.getVariable(instance.getId(), "aVariable"));
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  @Test
  public void testStartWithInvalidInitialActivity() {
    try {
      // when
      runtimeService
          .createProcessInstanceByKey("exclusiveGateway")
          .startBeforeActivity("someNonExistingActivity")
          .execute();
      fail("should not succeed");
    } catch (NotValidException e) {
      // then
      testRule.assertTextPresentIgnoreCase("element 'someNonExistingActivity' does not exist in process ", e.getMessage());
    }
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  @Test
  public void testMultipleActivitiesInstantiation() {

    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("exclusiveGateway")
      .startBeforeActivity("task1")
      .startBeforeActivity("task2")
      .startBeforeActivity("task1")
      .execute();

    // then
    assertNotNull(instance);

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("task1")
        .activity("task2")
        .activity("task1")
      .done());

    // and it is possible to end the process
    completeTasksInOrder("task1", "task2", "task1");
    testRule.assertProcessEnded(instance.getId());
  }

  @Deployment(resources = EXCLUSIVE_GATEWAY_PROCESS)
  @Test
  public void testMultipleActivitiesInstantiationWithVariables() {
    // when
    runtimeService
      .createProcessInstanceByKey("exclusiveGateway")
      .startBeforeActivity("task1")
        .setVariableLocal("aVar1", "aValue1")
      .startBeforeActivity("task2")
        .setVariableLocal("aVar2", "aValue2")
      .execute();

    // then
    // variables for task2's execution
    Execution task2Execution = runtimeService.createExecutionQuery().activityId("task2").singleResult();
    assertNotNull(task2Execution);
    assertNull(runtimeService.getVariableLocal(task2Execution.getId(), "aVar1"));
    assertEquals("aValue2", runtimeService.getVariableLocal(task2Execution.getId(), "aVar2"));

    // variables for task1's execution
    Execution task1Execution = runtimeService.createExecutionQuery().activityId("task1").singleResult();
    assertNotNull(task1Execution);

    assertNull(runtimeService.getVariableLocal(task1Execution.getId(), "aVar2"));

    // this variable is not a local variable on execution1 due to tree expansion
    assertNull(runtimeService.getVariableLocal(task1Execution.getId(), "aVar1"));
    assertEquals("aValue1", runtimeService.getVariable(task1Execution.getId(), "aVar1"));

  }

  @Deployment(resources = SUBPROCESS_PROCESS)
  @Test
  public void testNestedActivitiesInstantiation() {
    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("subprocess")
      .startBeforeActivity("innerTask")
      .startBeforeActivity("outerTask")
      .startBeforeActivity("innerTask")
      .execute();

    // then
    assertNotNull(instance);

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("outerTask")
        .beginScope("subProcess")
          .activity("innerTask")
          .activity("innerTask")
      .done());

    // and it is possible to end the process
    completeTasksInOrder("innerTask", "innerTask", "outerTask", "innerTask");
    testRule.assertProcessEnded(instance.getId());
  }

  @Test
  public void testStartNonExistingProcessDefinition() {
    try {
      runtimeService.createProcessInstanceById("I don't exist").startBeforeActivity("start").execute();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("no deployed process definition found with id", e.getMessage());
    }

    try {
      runtimeService.createProcessInstanceByKey("I don't exist either").startBeforeActivity("start").execute();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("no processes deployed with key", e.getMessage());
    }
  }

  @Test
  public void testStartNullProcessDefinition() {
    try {
      runtimeService.createProcessInstanceById(null).startBeforeActivity("start").execute();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // happy path
    }

    try {
      runtimeService.createProcessInstanceByKey(null).startBeforeActivity("start").execute();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  @Deployment(resources = LISTENERS_PROCESS)
  @Test
  public void testListenerInvocation() {
    RecorderExecutionListener.clear();

    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("listenerProcess")
      .startBeforeActivity("innerTask")
      .execute();

    // then
    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .beginScope("subProcess")
          .activity("innerTask")
      .done());

    List<RecordedEvent> events = RecorderExecutionListener.getRecordedEvents();
    assertEquals(3, events.size());

    RecordedEvent processStartEvent = events.get(0);
    assertEquals(ExecutionListener.EVENTNAME_START, processStartEvent.getEventName());
    assertEquals("innerTask", processStartEvent.getActivityId());

    RecordedEvent subProcessStartEvent = events.get(1);
    assertEquals(ExecutionListener.EVENTNAME_START, subProcessStartEvent.getEventName());
    assertEquals("subProcess", subProcessStartEvent.getActivityId());

    RecordedEvent innerTaskStartEvent = events.get(2);
    assertEquals(ExecutionListener.EVENTNAME_START, innerTaskStartEvent.getEventName());
    assertEquals("innerTask", innerTaskStartEvent.getActivityId());

  }

  @Deployment(resources = LISTENERS_PROCESS)
  @Test
  public void testSkipListenerInvocation() {
    RecorderExecutionListener.clear();

    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("listenerProcess")
      .startBeforeActivity("innerTask")
      .execute(true, true);

    // then
    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .beginScope("subProcess")
          .activity("innerTask")
      .done());

    List<RecordedEvent> events = RecorderExecutionListener.getRecordedEvents();
    assertEquals(0, events.size());
  }

  @Deployment(resources = IO_PROCESS)
  @Test
  public void testIoMappingInvocation() {
    // when
    runtimeService
      .createProcessInstanceByKey("ioProcess")
      .startBeforeActivity("innerTask")
      .execute();

    // then no io mappings have been executed
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery()
        .orderByVariableName().asc().list();
    assertEquals(2, variables.size());

    Execution innerTaskExecution = runtimeService.createExecutionQuery().activityId("innerTask").singleResult();
    VariableInstance innerTaskVariable = variables.get(0);
    assertEquals("innerTaskVariable", innerTaskVariable.getName());
    assertEquals("innerTaskValue", innerTaskVariable.getValue());
    assertEquals(innerTaskExecution.getId(), innerTaskVariable.getExecutionId());

    VariableInstance subProcessVariable = variables.get(1);
    assertEquals("subProcessVariable", subProcessVariable.getName());
    assertEquals("subProcessValue", subProcessVariable.getValue());
    assertEquals(((ExecutionEntity) innerTaskExecution).getParentId(), subProcessVariable.getExecutionId());
  }

  @Deployment(resources = IO_PROCESS)
  @Test
  public void testSkipIoMappingInvocation() {
    // when
    runtimeService
      .createProcessInstanceByKey("ioProcess")
      .startBeforeActivity("innerTask")
      .execute(true, true);

    // then no io mappings have been executed
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());
  }

  @Deployment(resources = SUBPROCESS_PROCESS)
  @Test
  public void testSetProcessInstanceVariable() {
    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("subprocess")
      .setVariable("aVariable1", "aValue1")
      .setVariableLocal("aVariable2", "aValue2")
      .setVariables(Variables.createVariables().putValue("aVariable3", "aValue3"))
      .setVariablesLocal(Variables.createVariables().putValue("aVariable4", "aValue4"))
      .startBeforeActivity("innerTask")
      .execute();

    // then
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery()
        .orderByVariableName().asc().list();

    assertEquals(4, variables.size());
    assertEquals("aVariable1", variables.get(0).getName());
    assertEquals("aValue1", variables.get(0).getValue());
    assertEquals(instance.getId(), variables.get(0).getExecutionId());

    assertEquals("aVariable2", variables.get(1).getName());
    assertEquals("aValue2", variables.get(1).getValue());
    assertEquals(instance.getId(), variables.get(1).getExecutionId());

    assertEquals("aVariable3", variables.get(2).getName());
    assertEquals("aValue3", variables.get(2).getValue());
    assertEquals(instance.getId(), variables.get(2).getExecutionId());

    assertEquals("aVariable4", variables.get(3).getName());
    assertEquals("aValue4", variables.get(3).getValue());
    assertEquals(instance.getId(), variables.get(3).getExecutionId());

  }

  @Deployment(resources = ASYNC_PROCESS)
  @Test
  public void testStartAsyncTask() {
    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("exclusiveGateway")
      .startBeforeActivity("task2")
      .execute();

    // then
    assertNotNull(instance);

    ActivityInstance updatedTree = runtimeService.getActivityInstance(instance.getId());
    assertNotNull(updatedTree);

    assertThat(updatedTree).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .transition("task2")
      .done());

    // and it is possible to end the process
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    completeTasksInOrder("task2");
    testRule.assertProcessEnded(instance.getId());
  }

  @Deployment(resources = SYNC_PROCESS)
  @Test
  public void testStartMultipleTasksInSyncProcess() {
    RecorderExecutionListener.clear();

    // when
    ProcessInstance instance = runtimeService
      .createProcessInstanceByKey("syncProcess")
      .startBeforeActivity("syncTask")
      .startBeforeActivity("syncTask")
      .startBeforeActivity("syncTask")
      .execute();

    // then the request was successful even though the process instance has already ended
    assertNotNull(instance);
    testRule.assertProcessEnded(instance.getId());

    // and the execution listener was invoked correctly
    List<RecordedEvent> events = RecorderExecutionListener.getRecordedEvents();
    assertEquals(8, events.size());

    // process start event
    assertEquals(ExecutionListener.EVENTNAME_START, events.get(0).getEventName());
    assertEquals("syncTask", events.get(0).getActivityId());

    // start instruction 1
    assertEquals(ExecutionListener.EVENTNAME_START, events.get(1).getEventName());
    assertEquals("syncTask", events.get(1).getActivityId());
    assertEquals(ExecutionListener.EVENTNAME_END, events.get(2).getEventName());
    assertEquals("syncTask", events.get(2).getActivityId());

    // start instruction 2
    assertEquals(ExecutionListener.EVENTNAME_START, events.get(3).getEventName());
    assertEquals("syncTask", events.get(3).getActivityId());
    assertEquals(ExecutionListener.EVENTNAME_END, events.get(4).getEventName());
    assertEquals("syncTask", events.get(4).getActivityId());

    // start instruction 3
    assertEquals(ExecutionListener.EVENTNAME_START, events.get(5).getEventName());
    assertEquals("syncTask", events.get(5).getActivityId());
    assertEquals(ExecutionListener.EVENTNAME_END, events.get(6).getEventName());
    assertEquals("syncTask", events.get(6).getActivityId());

    // process end event
    assertEquals(ExecutionListener.EVENTNAME_END, events.get(7).getEventName());
    assertEquals("end", events.get(7).getActivityId());
  }

  @Deployment
  @Test
  public void testInitiatorVariable() {
    // given
    identityService.setAuthenticatedUserId("kermit");

    // when
    ProcessInstance instance = runtimeService
        .createProcessInstanceByKey("initiatorProcess")
        .startBeforeActivity("task")
        .execute();

    // then
    String initiator = (String) runtimeService.getVariable(instance.getId(), "initiator");
    assertEquals("kermit", initiator);

    identityService.clearAuthentication();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/concurrentExecutionVariableWithSubprocess.bpmn20.xml"})
  public void shouldFinishProcessWithIoMappingAndEventSubprocess() {
    // given
    // Start process instance before the FirstTask (UserTask) with I/O mapping
    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey("process")
        .startBeforeActivity("FirstTask")
        .execute();

    // There should be one execution in the event sub process since the condition is met
    List<Execution> executions = runtimeService.createExecutionQuery()
        .processInstanceId(processInstance.getId())
        .activityId("Event_0s2zckl")
        .list();
    assertEquals(1, executions.size());

    // when the user tasks are completed
    String id = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
    taskService.complete(id);
    id = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
    taskService.complete(id);

    // then
    // Sub process should finish since the second condition should be true as well
    executions = runtimeService.createExecutionQuery()
        .processInstanceId(processInstance.getId())
        .activityId("Event_0s2zckl")
        .list();
    assertEquals(0, executions.size());
  }

  protected void completeTasksInOrder(String... taskNames) {
    for (String taskName : taskNames) {
      // complete any task with that name
      List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskName).listPage(0, 1);
      assertTrue("task for activity " + taskName + " does not exist", !tasks.isEmpty());
      taskService.complete(tasks.get(0).getId());
    }
  }
}
