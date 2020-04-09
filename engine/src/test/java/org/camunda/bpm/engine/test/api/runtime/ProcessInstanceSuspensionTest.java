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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class ProcessInstanceSuspensionTest extends PluggableProcessEngineTest {

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testProcessInstanceActiveByDefault() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspendActivateProcessInstance() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());

    //suspend
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertTrue(processInstance.isSuspended());

    //activate
    runtimeService.activateProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspendActivateProcessInstanceByProcessDefinitionId() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());

    //suspend
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertTrue(processInstance.isSuspended());

    //activate
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinition.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspendActivateProcessInstanceByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());

    //suspend
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertTrue(processInstance.isSuspended());

    //activate
    runtimeService.activateProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testActivateAlreadyActiveProcessInstance() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());

    try {
      //activate
      runtimeService.activateProcessInstanceById(processInstance.getId());
      processInstance = runtimeService.createProcessInstanceQuery().singleResult();
      assertFalse(processInstance.isSuspended());
    } catch (ProcessEngineException e) {
      fail("Should not fail");
    }

    try {
      //activate
      runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinition.getId());
      processInstance = runtimeService.createProcessInstanceQuery().singleResult();
      assertFalse(processInstance.isSuspended());
    } catch (ProcessEngineException e) {
      fail("Should not fail");
    }

    try {
      //activate
      runtimeService.activateProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
      processInstance = runtimeService.createProcessInstanceQuery().singleResult();
      assertFalse(processInstance.isSuspended());
    } catch (ProcessEngineException e) {
      fail("Should not fail");
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspendAlreadySuspendedProcessInstance() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());

    runtimeService.suspendProcessInstanceById(processInstance.getId());

    try {
      runtimeService.suspendProcessInstanceById(processInstance.getId());
      processInstance = runtimeService.createProcessInstanceQuery().singleResult();
      assertTrue(processInstance.isSuspended());
    } catch (ProcessEngineException e) {
      fail("Should not fail");
    }

    try {
      runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());
      processInstance = runtimeService.createProcessInstanceQuery().singleResult();
      assertTrue(processInstance.isSuspended());
    } catch (ProcessEngineException e) {
      fail("Should not fail");
    }

    try {
      runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
      processInstance = runtimeService.createProcessInstanceQuery().singleResult();
      assertTrue(processInstance.isSuspended());
    } catch (ProcessEngineException e) {
      fail("Should not fail");
    }
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/api/runtime/superProcessWithMultipleNestedSubProcess.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
          "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"
          })
  @Test
  public void testQueryForActiveAndSuspendedProcessInstances() {
    runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");

    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(5, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());

    ProcessInstance piToSuspend = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey("nestedSubProcessQueryTest")
            .singleResult();
    runtimeService.suspendProcessInstanceById(piToSuspend.getId());

    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(4, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());

    assertEquals(piToSuspend.getId(), runtimeService.createProcessInstanceQuery().suspended().singleResult().getId());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/runtime/superProcessWithMultipleNestedSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"
      })
  @Test
  public void testQueryForActiveAndSuspendedProcessInstancesByProcessDefinitionId() {
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("nestedSubProcessQueryTest")
        .singleResult();

    runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");

    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(5, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());

    ProcessInstance piToSuspend = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey("nestedSubProcessQueryTest")
            .singleResult();
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());

    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(4, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());

    assertEquals(piToSuspend.getId(), runtimeService.createProcessInstanceQuery().suspended().singleResult().getId());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/runtime/superProcessWithMultipleNestedSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"
      })
  @Test
  public void testQueryForActiveAndSuspendedProcessInstancesByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("nestedSubProcessQueryTest")
        .singleResult();

    runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");

    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(5, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());

    ProcessInstance piToSuspend = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey("nestedSubProcessQueryTest")
            .singleResult();
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());

    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(4, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());

    assertEquals(piToSuspend.getId(), runtimeService.createProcessInstanceQuery().suspended().singleResult().getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskSuspendedAfterProcessInstanceSuspension() {

    // Start Process Instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    // Suspense process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // Assert that the task is now also suspended
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertTrue(task.isSuspended());
    }

    // Activate process instance again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertFalse(task.isSuspended());
    }
  }

  /**
   * See https://app.camunda.com/jira/browse/CAM-9505
   */
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testPreserveCreateTimeOnUpdatedTask() {
    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    Task taskBeforeSuspension = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date createTime = taskBeforeSuspension.getCreateTime();

    // when
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // assume
    assertTrue(task.isSuspended());

    // then
    assertEquals(createTime, task.getCreateTime());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskSuspendedAfterProcessInstanceSuspensionByProcessDefinitionId() {

    // Start Process Instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    // Suspense process instance
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());

    // Assert that the task is now also suspended
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertTrue(task.isSuspended());
    }

    // Activate process instance again
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinition.getId());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertFalse(task.isSuspended());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskSuspendedAfterProcessInstanceSuspensionByProcessDefinitionKey() {

    // Start Process Instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    // Suspense process instance
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());

    // Assert that the task is now also suspended
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertTrue(task.isSuspended());
    }

    // Activate process instance again
    runtimeService.activateProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertFalse(task.isSuspended());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskQueryAfterProcessInstanceSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    task = taskService.createTaskQuery().active().singleResult();
    assertNotNull(task);

    // Suspend
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().active().count());

    // Activate
    runtimeService.activateProcessInstanceById(processInstance.getId());
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(0, taskService.createTaskQuery().suspended().count());
    assertEquals(1, taskService.createTaskQuery().active().count());

    // Completing should end the process instance
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskQueryAfterProcessInstanceSuspendByProcessDefinitionId() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    task = taskService.createTaskQuery().active().singleResult();
    assertNotNull(task);

    // Suspend
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().active().count());

    // Activate
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinition.getId());
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(0, taskService.createTaskQuery().suspended().count());
    assertEquals(1, taskService.createTaskQuery().active().count());

    // Completing should end the process instance
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskQueryAfterProcessInstanceSuspendByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    task = taskService.createTaskQuery().active().singleResult();
    assertNotNull(task);

    // Suspend
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().active().count());

    // Activate
    runtimeService.activateProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(0, taskService.createTaskQuery().suspended().count());
    assertEquals(1, taskService.createTaskQuery().active().count());

    // Completing should end the process instance
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment
  @Test
  public void testChildExecutionsSuspendedAfterProcessInstanceSuspend() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testChildExecutionsSuspended");
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertTrue(execution.isSuspended());
    }

    // Activate again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertFalse(execution.isSuspended());
    }

    // Finish process
    while (taskService.createTaskQuery().count() > 0) {
      for (Task task : taskService.createTaskQuery().list()) {
        taskService.complete(task.getId());
      }
    }
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.testChildExecutionsSuspendedAfterProcessInstanceSuspend.bpmn20.xml"})
  @Test
  public void testChildExecutionsSuspendedAfterProcessInstanceSuspendByProcessDefinitionId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testChildExecutionsSuspended");
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processInstance.getProcessDefinitionId());

    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertTrue(execution.isSuspended());
    }

    // Activate again
    runtimeService.activateProcessInstanceByProcessDefinitionId(processInstance.getProcessDefinitionId());
    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertFalse(execution.isSuspended());
    }

    // Finish process
    while (taskService.createTaskQuery().count() > 0) {
      for (Task task : taskService.createTaskQuery().list()) {
        taskService.complete(task.getId());
      }
    }
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.testChildExecutionsSuspendedAfterProcessInstanceSuspend.bpmn20.xml"})
  @Test
  public void testChildExecutionsSuspendedAfterProcessInstanceSuspendByProcessDefinitionKey() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testChildExecutionsSuspended");
    runtimeService.suspendProcessInstanceByProcessDefinitionKey("testChildExecutionsSuspended");

    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertTrue(execution.isSuspended());
    }

    // Activate again
    runtimeService.activateProcessInstanceByProcessDefinitionKey("testChildExecutionsSuspended");
    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertFalse(execution.isSuspended());
    }

    // Finish process
    while (taskService.createTaskQuery().count() > 0) {
      for (Task task : taskService.createTaskQuery().list()) {
        taskService.complete(task.getId());
      }
    }
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testChangeVariablesAfterProcessInstanceSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    try {
      runtimeService.removeVariable(processInstance.getId(), "someVariable");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.removeVariableLocal(processInstance.getId(), "someVariable");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.removeVariables(processInstance.getId(), Arrays.asList("one", "two", "three"));
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }


    try {
      runtimeService.removeVariablesLocal(processInstance.getId(), Arrays.asList("one", "two", "three"));
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariable(processInstance.getId(), "someVariable", "someValue");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariableLocal(processInstance.getId(), "someVariable", "someValue");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariables(processInstance.getId(), new HashMap<String, Object>());
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariablesLocal(processInstance.getId(), new HashMap<String, Object>());
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testChangeVariablesAfterProcessInstanceSuspendByProcessDefinitionId() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processInstance.getProcessDefinitionId());

    try {
      runtimeService.removeVariable(processInstance.getId(), "someVariable");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.removeVariableLocal(processInstance.getId(), "someVariable");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.removeVariables(processInstance.getId(), Arrays.asList("one", "two", "three"));
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }


    try {
      runtimeService.removeVariablesLocal(processInstance.getId(), Arrays.asList("one", "two", "three"));
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariable(processInstance.getId(), "someVariable", "someValue");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariableLocal(processInstance.getId(), "someVariable", "someValue");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariables(processInstance.getId(), new HashMap<String, Object>());
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariablesLocal(processInstance.getId(), new HashMap<String, Object>());
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testChangeVariablesAfterProcessInstanceSuspendByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());

    try {
      runtimeService.removeVariable(processInstance.getId(), "someVariable");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.removeVariableLocal(processInstance.getId(), "someVariable");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.removeVariables(processInstance.getId(), Arrays.asList("one", "two", "three"));
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.removeVariablesLocal(processInstance.getId(), Arrays.asList("one", "two", "three"));
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariable(processInstance.getId(), "someVariable", "someValue");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariableLocal(processInstance.getId(), "someVariable", "someValue");
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariables(processInstance.getId(), new HashMap<String, Object>());
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }

    try {
      runtimeService.setVariablesLocal(processInstance.getId(), new HashMap<String, Object>());
    } catch (ProcessEngineException e) {
      fail("This should be possible");
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSubmitTaskFormFailAfterProcessInstanceSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    try {
      formService.submitTaskFormData(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId(), new HashMap<String, String>());
      fail();
    } catch(SuspendedEntityInteractionException e) {
      // This is expected
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSubmitTaskFormFailAfterProcessInstanceSuspendByProcessDefinitionId() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());

    try {
      formService.submitTaskFormData(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId(), new HashMap<String, String>());
      fail();
    } catch(SuspendedEntityInteractionException e) {
      // This is expected
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSubmitTaskFormFailAfterProcessInstanceSuspendByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());

    try {
      formService.submitTaskFormData(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId(), new HashMap<String, String>());
      fail();
    } catch(SuspendedEntityInteractionException e) {
      // This is expected
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testProcessInstanceSignalFailAfterSuspend() {

    // Suspend process instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    try {
      runtimeService.signal(processInstance.getId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
      assertTrue(e instanceof BadUserRequestException);
    }

    try {
      runtimeService.signal(processInstance.getId(), new HashMap<String, Object>());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
      assertTrue(e instanceof BadUserRequestException);
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testProcessInstanceSignalFailAfterSuspendByProcessDefinitionId() {

    // Suspend process instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());

    try {
      runtimeService.signal(processInstance.getId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
      assertTrue(e instanceof BadUserRequestException);
    }

    try {
      runtimeService.signal(processInstance.getId(), new HashMap<String, Object>());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
      assertTrue(e instanceof BadUserRequestException);
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testProcessInstanceSignalFailAfterSuspendByProcessDefinitionKey() {

    // Suspend process instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());

    try {
      runtimeService.signal(processInstance.getId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
      assertTrue(e instanceof BadUserRequestException);
    }

    try {
      runtimeService.signal(processInstance.getId(), new HashMap<String, Object>());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
      assertTrue(e instanceof BadUserRequestException);
    }
  }

  @Deployment
  @Test
  public void testMessageEventReceiveFailAfterSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    EventSubscription subscription = runtimeService.createEventSubscriptionQuery().singleResult();

    try {
      runtimeService.messageEventReceived("someMessage", subscription.getExecutionId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }

    try {
      runtimeService.messageEventReceived("someMessage", subscription.getExecutionId(), new HashMap<String, Object>());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.testMessageEventReceiveFailAfterSuspend.bpmn20.xml"})
  @Test
  public void testMessageEventReceiveFailAfterSuspendByProcessDefinitionId() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());
    EventSubscription subscription = runtimeService.createEventSubscriptionQuery().singleResult();

    try {
      runtimeService.messageEventReceived("someMessage", subscription.getExecutionId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }

    try {
      runtimeService.messageEventReceived("someMessage", subscription.getExecutionId(), new HashMap<String, Object>());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.testMessageEventReceiveFailAfterSuspend.bpmn20.xml"})
  @Test
  public void testMessageEventReceiveFailAfterSuspendByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    EventSubscription subscription = runtimeService.createEventSubscriptionQuery().singleResult();

    try {
      runtimeService.messageEventReceived("someMessage", subscription.getExecutionId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }

    try {
      runtimeService.messageEventReceived("someMessage", subscription.getExecutionId(), new HashMap<String, Object>());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testSignalEventReceivedAfterProcessInstanceSuspended() {

    final String signal = "Some Signal";

    // Test if process instance can be completed using the signal
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.signalEventReceived(signal);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // Now test when suspending the process instance: the process instance shouldn't be continued
    processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.signalEventReceived(signal, new HashMap<String, Object>());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    EventSubscription subscription = runtimeService.createEventSubscriptionQuery().singleResult();
    try {
      runtimeService.signalEventReceived(signal, subscription.getExecutionId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }

    try {
      runtimeService.signalEventReceived(signal, subscription.getExecutionId(), new HashMap<String, Object>());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }

    // Activate and try again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.testSignalEventReceivedAfterProcessInstanceSuspended.bpmn20.xml"})
  @Test
  public void testSignalEventReceivedAfterProcessInstanceSuspendedByProcessDefinitionId() {

    final String signal = "Some Signal";

    // Test if process instance can be completed using the signal
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.signalEventReceived(signal);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // Now test when suspending the process instance: the process instance shouldn't be continued
    processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processInstance.getProcessDefinitionId());
    runtimeService.signalEventReceived(signal);
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.signalEventReceived(signal, new HashMap<String, Object>());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    EventSubscription subscription = runtimeService.createEventSubscriptionQuery().singleResult();
    try {
      runtimeService.signalEventReceived(signal, subscription.getExecutionId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }

    try {
      runtimeService.signalEventReceived(signal, subscription.getExecutionId(), new HashMap<String, Object>());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }

    // Activate and try again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.testSignalEventReceivedAfterProcessInstanceSuspended.bpmn20.xml"})
  @Test
  public void testSignalEventReceivedAfterProcessInstanceSuspendedByProcessDefinitionKey() {

    final String signal = "Some Signal";

    // Test if process instance can be completed using the signal
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.signalEventReceived(signal);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // Now test when suspending the process instance: the process instance shouldn't be continued
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("signalSuspendedProcessInstance")
        .singleResult();

    processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    runtimeService.signalEventReceived(signal);
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.signalEventReceived(signal, new HashMap<String, Object>());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    EventSubscription subscription = runtimeService.createEventSubscriptionQuery().singleResult();
    try {
      runtimeService.signalEventReceived(signal, subscription.getExecutionId());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }

    try {
      runtimeService.signalEventReceived(signal, subscription.getExecutionId(), new HashMap<String, Object>());
      fail();
    } catch (SuspendedEntityInteractionException e) {
      // This is expected
      testRule.assertTextPresent("is suspended", e.getMessage());
    }

    // Activate and try again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskLifecycleOperationsFailAfterProcessInstanceSuspend() {

    // Start a new process instance with one task
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    // Suspend the process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // Completing the task should fail
    try {
      taskService.complete(task.getId());
      fail("It is not allowed to complete a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Claiming the task should fail
    try {
      taskService.claim(task.getId(), "jos");
      fail("It is not allowed to claim a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }



    // Adding candidate groups on the task should fail
    try {
      taskService.addCandidateGroup(task.getId(), "blahGroup");
      fail("It is not allowed to add a candidate group on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Adding candidate users on the task should fail
    try {
      taskService.addCandidateUser(task.getId(), "blahUser");
      fail("It is not allowed to add a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Adding group identity links on the task should fail
    try {
      taskService.addGroupIdentityLink(task.getId(), "blahGroup", IdentityLinkType.CANDIDATE);
      fail("It is not allowed to add a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Adding an identity link on the task should fail
    try {
      taskService.addUserIdentityLink(task.getId(), "blahUser", IdentityLinkType.OWNER);
      fail("It is not allowed to add an identityLink on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }


    // Set an assignee on the task should fail
    try {
      taskService.setAssignee(task.getId(), "mispiggy");
      fail("It is not allowed to set an assignee on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Set an owner on the task should fail
    try {
      taskService.setOwner(task.getId(), "kermit");
      fail("It is not allowed to set an owner on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing candidate groups on the task should fail
    try {
      taskService.deleteCandidateGroup(task.getId(), "blahGroup");
      fail("It is not allowed to remove a candidate group on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing candidate users on the task should fail
    try {
      taskService.deleteCandidateUser(task.getId(), "blahUser");
      fail("It is not allowed to remove a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing group identity links on the task should fail
    try {
      taskService.deleteGroupIdentityLink(task.getId(), "blahGroup", IdentityLinkType.CANDIDATE);
      fail("It is not allowed to remove a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing an identity link on the task should fail
    try {
      taskService.deleteUserIdentityLink(task.getId(), "blahUser", IdentityLinkType.OWNER);
      fail("It is not allowed to remove an identityLink on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskLifecycleOperationsFailAfterProcessInstanceSuspendByProcessDefinitionId() {

    // Start a new process instance with one task
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    // Suspend the process instance
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());

    // Completing the task should fail
    try {
      taskService.complete(task.getId());
      fail("It is not allowed to complete a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Claiming the task should fail
    try {
      taskService.claim(task.getId(), "jos");
      fail("It is not allowed to claim a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }



    // Adding candidate groups on the task should fail
    try {
      taskService.addCandidateGroup(task.getId(), "blahGroup");
      fail("It is not allowed to add a candidate group on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Adding candidate users on the task should fail
    try {
      taskService.addCandidateUser(task.getId(), "blahUser");
      fail("It is not allowed to add a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Adding group identity links on the task should fail
    try {
      taskService.addGroupIdentityLink(task.getId(), "blahGroup", IdentityLinkType.CANDIDATE);
      fail("It is not allowed to add a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Adding an identity link on the task should fail
    try {
      taskService.addUserIdentityLink(task.getId(), "blahUser", IdentityLinkType.OWNER);
      fail("It is not allowed to add an identityLink on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }


    // Set an assignee on the task should fail
    try {
      taskService.setAssignee(task.getId(), "mispiggy");
      fail("It is not allowed to set an assignee on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Set an owner on the task should fail
    try {
      taskService.setOwner(task.getId(), "kermit");
      fail("It is not allowed to set an owner on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing candidate groups on the task should fail
    try {
      taskService.deleteCandidateGroup(task.getId(), "blahGroup");
      fail("It is not allowed to remove a candidate group on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing candidate users on the task should fail
    try {
      taskService.deleteCandidateUser(task.getId(), "blahUser");
      fail("It is not allowed to remove a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing group identity links on the task should fail
    try {
      taskService.deleteGroupIdentityLink(task.getId(), "blahGroup", IdentityLinkType.CANDIDATE);
      fail("It is not allowed to remove a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing an identity link on the task should fail
    try {
      taskService.deleteUserIdentityLink(task.getId(), "blahUser", IdentityLinkType.OWNER);
      fail("It is not allowed to remove an identityLink on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskLifecycleOperationsFailAfterProcessInstanceSuspendByProcessDefinitionKey() {

    // Start a new process instance with one task
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    // Suspend the process instance
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());

    // Completing the task should fail
    try {
      taskService.complete(task.getId());
      fail("It is not allowed to complete a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Claiming the task should fail
    try {
      taskService.claim(task.getId(), "jos");
      fail("It is not allowed to claim a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }



    // Adding candidate groups on the task should fail
    try {
      taskService.addCandidateGroup(task.getId(), "blahGroup");
      fail("It is not allowed to add a candidate group on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Adding candidate users on the task should fail
    try {
      taskService.addCandidateUser(task.getId(), "blahUser");
      fail("It is not allowed to add a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Adding group identity links on the task should fail
    try {
      taskService.addGroupIdentityLink(task.getId(), "blahGroup", IdentityLinkType.CANDIDATE);
      fail("It is not allowed to add a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Adding an identity link on the task should fail
    try {
      taskService.addUserIdentityLink(task.getId(), "blahUser", IdentityLinkType.OWNER);
      fail("It is not allowed to add an identityLink on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }


    // Set an assignee on the task should fail
    try {
      taskService.setAssignee(task.getId(), "mispiggy");
      fail("It is not allowed to set an assignee on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Set an owner on the task should fail
    try {
      taskService.setOwner(task.getId(), "kermit");
      fail("It is not allowed to set an owner on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing candidate groups on the task should fail
    try {
      taskService.deleteCandidateGroup(task.getId(), "blahGroup");
      fail("It is not allowed to remove a candidate group on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing candidate users on the task should fail
    try {
      taskService.deleteCandidateUser(task.getId(), "blahUser");
      fail("It is not allowed to remove a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing group identity links on the task should fail
    try {
      taskService.deleteGroupIdentityLink(task.getId(), "blahGroup", IdentityLinkType.CANDIDATE);
      fail("It is not allowed to remove a candidate user on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }

    // Removing an identity link on the task should fail
    try {
      taskService.deleteUserIdentityLink(task.getId(), "blahUser", IdentityLinkType.OWNER);
      fail("It is not allowed to remove an identityLink on a task of a suspended process instance");
    } catch (SuspendedEntityInteractionException e) {
      // This is good
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSubTaskCreationFailAfterProcessInstanceSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    Task subTask = taskService.newTask("someTaskId");
    subTask.setParentTaskId(task.getId());

    try {
      taskService.saveTask(subTask);
      fail("Creating sub tasks for suspended task should not be possible");
    } catch (SuspendedEntityInteractionException e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSubTaskCreationFailAfterProcessInstanceSuspendByProcessDefinitionId() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());

    Task subTask = taskService.newTask("someTaskId");
    subTask.setParentTaskId(task.getId());

    try {
      taskService.saveTask(subTask);
      fail("Creating sub tasks for suspended task should not be possible");
    } catch (SuspendedEntityInteractionException e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSubTaskCreationFailAfterProcessInstanceSuspendByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());

    Task subTask = taskService.newTask("someTaskId");
    subTask.setParentTaskId(task.getId());

    try {
      taskService.saveTask(subTask);
      fail("Creating sub tasks for suspended task should not be possible");
    } catch (SuspendedEntityInteractionException e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskNonLifecycleOperationsSucceedAfterProcessInstanceSuspend() {

    // Start a new process instance with one task
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    assertNotNull(task);

    try {
      taskService.setVariable(task.getId(), "someVar", "someValue");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.setVariableLocal(task.getId(), "someVar", "someValue");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      HashMap<String, String> variables = new HashMap<String, String>();
      variables.put("varOne", "one");
      variables.put("varTwo", "two");
      taskService.setVariables(task.getId(), variables);
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      HashMap<String, String> variables = new HashMap<String, String>();
      variables.put("varOne", "one");
      variables.put("varTwo", "two");
      taskService.setVariablesLocal(task.getId(), variables);
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariable(task.getId(), "someVar");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariableLocal(task.getId(), "someVar");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariables(task.getId(), Arrays.asList("one", "two"));
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariablesLocal(task.getId(), Arrays.asList("one", "two"));
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    if(processEngineConfiguration.getHistoryLevel().getId() > HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId()) {

      try {
        taskService.createComment(task.getId(), processInstance.getId(), "test comment");
      } catch (SuspendedEntityInteractionException e) {
        fail("should be allowed");
      }

      try {
        taskService.createAttachment("text", task.getId(), processInstance.getId(), "tesTastName", "testDescription", "http://test.com");
      } catch (SuspendedEntityInteractionException e) {
        fail("should be allowed");
      }

    }


    try {
      taskService.setPriority(task.getId(), 99);
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskNonLifecycleOperationsSucceedAfterProcessInstanceSuspendByProcessDefinitionId() {

    // Start a new process instance with one task
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processInstance.getProcessDefinitionId());
    assertNotNull(task);

    try {
      taskService.setVariable(task.getId(), "someVar", "someValue");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.setVariableLocal(task.getId(), "someVar", "someValue");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      HashMap<String, String> variables = new HashMap<String, String>();
      variables.put("varOne", "one");
      variables.put("varTwo", "two");
      taskService.setVariables(task.getId(), variables);
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      HashMap<String, String> variables = new HashMap<String, String>();
      variables.put("varOne", "one");
      variables.put("varTwo", "two");
      taskService.setVariablesLocal(task.getId(), variables);
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariable(task.getId(), "someVar");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariableLocal(task.getId(), "someVar");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariables(task.getId(), Arrays.asList("one", "two"));
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariablesLocal(task.getId(), Arrays.asList("one", "two"));
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    if(processEngineConfiguration.getHistoryLevel().getId() > HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId()) {

      try {
        taskService.createComment(task.getId(), processInstance.getId(), "test comment");
      } catch (SuspendedEntityInteractionException e) {
        fail("should be allowed");
      }

      try {
        taskService.createAttachment("text", task.getId(), processInstance.getId(), "tesTastName", "testDescription", "http://test.com");
      } catch (SuspendedEntityInteractionException e) {
        fail("should be allowed");
      }

    }


    try {
      taskService.setPriority(task.getId(), 99);
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testTaskNonLifecycleOperationsSucceedAfterProcessInstanceSuspendByProcessDefinitionKey() {

    // Start a new process instance with one task
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    assertNotNull(task);

    try {
      taskService.setVariable(task.getId(), "someVar", "someValue");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.setVariableLocal(task.getId(), "someVar", "someValue");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      HashMap<String, String> variables = new HashMap<String, String>();
      variables.put("varOne", "one");
      variables.put("varTwo", "two");
      taskService.setVariables(task.getId(), variables);
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      HashMap<String, String> variables = new HashMap<String, String>();
      variables.put("varOne", "one");
      variables.put("varTwo", "two");
      taskService.setVariablesLocal(task.getId(), variables);
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariable(task.getId(), "someVar");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariableLocal(task.getId(), "someVar");
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariables(task.getId(), Arrays.asList("one", "two"));
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    try {
      taskService.removeVariablesLocal(task.getId(), Arrays.asList("one", "two"));
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }

    if(processEngineConfiguration.getHistoryLevel().getId() > HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId()) {

      try {
        taskService.createComment(task.getId(), processInstance.getId(), "test comment");
      } catch (SuspendedEntityInteractionException e) {
        fail("should be allowed");
      }

      try {
        taskService.createAttachment("text", task.getId(), processInstance.getId(), "tesTastName", "testDescription", "http://test.com");
      } catch (SuspendedEntityInteractionException e) {
        fail("should be allowed");
      }

    }


    try {
      taskService.setPriority(task.getId(), 99);
    } catch (SuspendedEntityInteractionException e) {
      fail("should be allowed");
    }
  }

  @Deployment
  @Test
  public void testJobNotExecutedAfterProcessInstanceSuspend() {

    Date now = new Date();
    ClockUtil.setCurrentTime(now);

    // Suspending the process instance should also stop the execution of jobs for that process instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(1, managementService.createJobQuery().count());
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    assertEquals(1, managementService.createJobQuery().count());

    // The jobs should not be executed now
    ClockUtil.setCurrentTime(new Date(now.getTime() + (60 * 60 * 1000))); // Timer is set to fire on 5 minutes
    assertEquals(0, managementService.createJobQuery().executable().count());

    // Activation of the process instance should now allow for job execution
    runtimeService.activateProcessInstanceById(processInstance.getId());
    assertEquals(1, managementService.createJobQuery().executable().count());
    managementService.executeJob(managementService.createJobQuery().singleResult().getId());
    assertEquals(0, managementService.createJobQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.testJobNotExecutedAfterProcessInstanceSuspend.bpmn20.xml"})
  @Test
  public void testJobNotExecutedAfterProcessInstanceSuspendByProcessDefinitionId() {

    Date now = new Date();
    ClockUtil.setCurrentTime(now);

    // Suspending the process instance should also stop the execution of jobs for that process instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(1, managementService.createJobQuery().count());
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinition.getId());
    assertEquals(1, managementService.createJobQuery().count());

    // The jobs should not be executed now
    ClockUtil.setCurrentTime(new Date(now.getTime() + (60 * 60 * 1000))); // Timer is set to fire on 5 minutes
    assertEquals(0, managementService.createJobQuery().executable().count());

    // Activation of the process instance should now allow for job execution
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinition.getId());
    assertEquals(1, managementService.createJobQuery().executable().count());
    managementService.executeJob(managementService.createJobQuery().singleResult().getId());
    assertEquals(0, managementService.createJobQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.testJobNotExecutedAfterProcessInstanceSuspend.bpmn20.xml"})
  @Test
  public void testJobNotExecutedAfterProcessInstanceSuspendByProcessDefinitionKey() {

    Date now = new Date();
    ClockUtil.setCurrentTime(now);

    // Suspending the process instance should also stop the execution of jobs for that process instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(1, managementService.createJobQuery().count());
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    assertEquals(1, managementService.createJobQuery().count());

    // The jobs should not be executed now
    ClockUtil.setCurrentTime(new Date(now.getTime() + (60 * 60 * 1000))); // Timer is set to fire on 5 minutes
    assertEquals(0, managementService.createJobQuery().executable().count());

    // Activation of the process instance should now allow for job execution
    runtimeService.activateProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    assertEquals(1, managementService.createJobQuery().executable().count());
    managementService.executeJob(managementService.createJobQuery().singleResult().getId());
    assertEquals(0, managementService.createJobQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.callSimpleProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  @Test
  public void testCallActivityReturnAfterProcessInstanceSuspend() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("callSimpleProcess");
    runtimeService.suspendProcessInstanceById(instance.getId());

    Task task = taskService.createTaskQuery().singleResult();

    try {
      taskService.complete(task.getId());
      fail("this should not be successful, as the execution of a suspended instance is resumed");
    } catch (SuspendedEntityInteractionException e) {
      // this is expected to fail
    }

    // should be successful after reactivation
    runtimeService.activateProcessInstanceById(instance.getId());
    taskService.complete(task.getId());

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.callSimpleProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  @Test
  public void testCallActivityReturnAfterProcessInstanceSuspendByProcessDefinitionId() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("callSimpleProcess");
    runtimeService.suspendProcessInstanceByProcessDefinitionId(instance.getProcessDefinitionId());

    Task task = taskService.createTaskQuery().singleResult();

    try {
      taskService.complete(task.getId());
      fail("this should not be successful, as the execution of a suspended instance is resumed");
    } catch (SuspendedEntityInteractionException e) {
      // this is expected to fail
    }

    // should be successful after reactivation
    runtimeService.activateProcessInstanceByProcessDefinitionId(instance.getProcessDefinitionId());
    taskService.complete(task.getId());

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.callSimpleProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  @Test
  public void testCallActivityReturnAfterProcessInstanceSuspendByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("callSimpleProcess")
        .singleResult();

    runtimeService.startProcessInstanceByKey("callSimpleProcess");
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());

    Task task = taskService.createTaskQuery().singleResult();

    try {
      taskService.complete(task.getId());
      fail("this should not be successful, as the execution of a suspended instance is resumed");
    } catch (SuspendedEntityInteractionException e) {
      // this is expected to fail
    }

    // should be successful after reactivation
    runtimeService.activateProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    taskService.complete(task.getId());

    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.callMISimpleProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  @Test
  public void testMICallActivityReturnAfterProcessInstanceSuspend() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("callMISimpleProcess");
    runtimeService.suspendProcessInstanceById(instance.getId());

    List<Task> tasks = taskService.createTaskQuery().list();
    Task task1 = tasks.get(0);
    Task task2 = tasks.get(1);

    try {
      taskService.complete(task1.getId());
      fail("this should not be successful, as the execution of a suspended instance is resumed");
    } catch (SuspendedEntityInteractionException e) {
      // this is expected to fail
    }

    try {
      taskService.complete(task2.getId());
      fail("this should not be successful, as the execution of a suspended instance is resumed");
    } catch (SuspendedEntityInteractionException e) {
      // this is expected to fail
    }

    // should be successful after reactivation
    runtimeService.activateProcessInstanceById(instance.getId());
    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.callMISimpleProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  @Test
  public void testMICallActivityReturnAfterProcessInstanceSuspendByProcessDefinitionId() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("callMISimpleProcess");
    runtimeService.suspendProcessInstanceByProcessDefinitionId(instance.getProcessDefinitionId());

    List<Task> tasks = taskService.createTaskQuery().list();
    Task task1 = tasks.get(0);
    Task task2 = tasks.get(1);

    try {
      taskService.complete(task1.getId());
      fail("this should not be successful, as the execution of a suspended instance is resumed");
    } catch (SuspendedEntityInteractionException e) {
      // this is expected to fail
    }

    try {
      taskService.complete(task2.getId());
      fail("this should not be successful, as the execution of a suspended instance is resumed");
    } catch (SuspendedEntityInteractionException e) {
      // this is expected to fail
    }

    // should be successful after reactivation
    runtimeService.activateProcessInstanceByProcessDefinitionId(instance.getProcessDefinitionId());
    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/ProcessInstanceSuspensionTest.callMISimpleProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  @Test
  public void testMICallActivityReturnAfterProcessInstanceSuspendByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("callMISimpleProcess")
        .singleResult();
    runtimeService.startProcessInstanceByKey("callMISimpleProcess");
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinition.getKey());

    List<Task> tasks = taskService.createTaskQuery().list();
    Task task1 = tasks.get(0);
    Task task2 = tasks.get(1);

    try {
      taskService.complete(task1.getId());
      fail("this should not be successful, as the execution of a suspended instance is resumed");
    } catch (SuspendedEntityInteractionException e) {
      // this is expected to fail
    }

    try {
      taskService.complete(task2.getId());
      fail("this should not be successful, as the execution of a suspended instance is resumed");
    } catch (SuspendedEntityInteractionException e) {
      // this is expected to fail
    }

    // should be successful after reactivation
    runtimeService.activateProcessInstanceByProcessDefinitionKey(processDefinition.getKey());
    taskService.complete(task1.getId());
    taskService.complete(task2.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testStartBeforeActivityForSuspendProcessInstance() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    //start process instance
    runtimeService.startProcessInstanceById(processDefinition.getId());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    // Suspend process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // try to start before activity for suspended processDefinition
    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startBeforeActivity("theTask").execute();
      fail("Exception is expected but not thrown");
    } catch(SuspendedEntityInteractionException e) {
      testRule.assertTextPresentIgnoreCase("is suspended", e.getMessage());
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testStartAfterActivityForSuspendProcessInstance() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    //start process instance
    runtimeService.startProcessInstanceById(processDefinition.getId());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    // Suspend process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // try to start after activity for suspended processDefinition
    try {
      runtimeService.createProcessInstanceModification(processInstance.getId()).startAfterActivity("theTask").execute();
      fail("Exception is expected but not thrown");
    } catch(SuspendedEntityInteractionException e) {
      testRule.assertTextPresentIgnoreCase("is suspended", e.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspensionByIdCascadesToExternalTasks() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    ExternalTask task1 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(task1.isSuspended());

    // when the process instance is suspended
    runtimeService.suspendProcessInstanceById(processInstance1.getId());

    // then the task is suspended
    task1 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(task1.isSuspended());

    // the other task is not
    ExternalTask task2 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(task2.isSuspended());

    // when it is activated again
    runtimeService.activateProcessInstanceById(processInstance1.getId());

    // then the task is activated too
    task1 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(task1.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspensionByProcessDefinitionIdCascadesToExternalTasks() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    ExternalTask task1 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(task1.isSuspended());

    // when the process instance is suspended
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processInstance1.getProcessDefinitionId());

    // then the task is suspended
    task1 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(task1.isSuspended());

    // the other task is not
    ExternalTask task2 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(task2.isSuspended());

    // when it is activated again
    runtimeService.activateProcessInstanceByProcessDefinitionId(processInstance1.getProcessDefinitionId());

    // then the task is activated too
    task1 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(task1.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspensionByProcessDefinitionKeyCascadesToExternalTasks() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    ExternalTask task1 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(task1.isSuspended());

    // when the process instance is suspended
    runtimeService.suspendProcessInstanceByProcessDefinitionKey("oneExternalTaskProcess");

    // then the task is suspended
    task1 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance1.getId()).singleResult();
    assertTrue(task1.isSuspended());

    // the other task is not
    ExternalTask task2 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance2.getId()).singleResult();
    assertFalse(task2.isSuspended());

    // when it is activated again
    runtimeService.activateProcessInstanceByProcessDefinitionKey("oneExternalTaskProcess");

    // then the task is activated too
    task1 = externalTaskService.createExternalTaskQuery()
        .processInstanceId(processInstance1.getId()).singleResult();
    assertFalse(task1.isSuspended());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspendAndActivateProcessInstanceByIdUsingBuilder() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());

    //suspend
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessInstanceId(processInstance.getId())
      .suspend();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertEquals(0, query.active().count());
    assertEquals(1, query.suspended().count());

    //activate
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessInstanceId(processInstance.getId())
      .activate();

    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspendAndActivateProcessInstanceByProcessDefinitionIdUsingBuilder() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());

    //suspend
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .suspend();

    assertEquals(0, query.active().count());
    assertEquals(1, query.suspended().count());

    //activate
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .activate();

    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testSuspendAndActivateProcessInstanceByProcessDefinitionKeyUsingBuilder() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());

    //suspend
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey("oneTaskProcess")
      .suspend();

    assertEquals(0, query.active().count());
    assertEquals(1, query.suspended().count());

    //activate
    runtimeService
      .updateProcessInstanceSuspensionState()
      .byProcessDefinitionKey("oneTaskProcess")
      .activate();

    assertEquals(1, query.active().count());
    assertEquals(0, query.suspended().count());
  }

  @Deployment
  @Test
  public void testJobSuspensionStateUpdate() {

    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process");
    String id = instance.getProcessInstanceId();

    //when
    runtimeService.suspendProcessInstanceById(id);
    Job job = managementService.createJobQuery().processInstanceId(id).singleResult();

    // then
    assertTrue(job.isSuspended());
  }

}
