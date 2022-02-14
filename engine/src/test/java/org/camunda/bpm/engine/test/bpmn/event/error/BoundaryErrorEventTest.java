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
package org.camunda.bpm.engine.test.bpmn.event.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.bpmn.event.error.ThrowErrorDelegate.throwError;
import static org.camunda.bpm.engine.test.bpmn.event.error.ThrowErrorDelegate.throwException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Joram Barrez
 * @author Falko Menge
 */
public class BoundaryErrorEventTest extends PluggableProcessEngineTest {

  @Before
  public void setUp() throws Exception {


    // Normally the UI will do this automatically for us
    identityService.setAuthenticatedUserId("kermit");
  }

  @After
  public void tearDown() throws Exception {
    identityService.clearAuthentication();

  }

  @Deployment
  @Test
  public void testCatchErrorOnEmbeddedSubprocess() {
    runtimeService.startProcessInstanceByKey("boundaryErrorOnEmbeddedSubprocess");

    // After process start, usertask in subprocess should exist
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("subprocessTask", task.getName());

    // After task completion, error end event is reached and caught
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task after catching the error", task.getName());
  }

  @Test
  public void testThrowErrorWithoutErrorCode() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testThrowErrorWithoutErrorCode.bpmn20.xml")
        .deploy();
      fail("ProcessEngineException expected");
    } catch (ParseException e) {
      testRule.assertTextPresent("'errorCode' is mandatory on errors referenced by throwing error event definitions", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("theEnd");
    }
  }

  @Test
  public void testThrowErrorWithEmptyErrorCode() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testThrowErrorWithEmptyErrorCode.bpmn20.xml")
        .deploy();
      fail("ProcessEngineException expected");
    } catch (ParseException e) {
      testRule.assertTextPresent("'errorCode' is mandatory on errors referenced by throwing error event definitions", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("theEnd");
    }
  }

  @Deployment
  @Test
  public void testCatchErrorOnEmbeddedSubprocessWithEmptyErrorCode() {
    testCatchErrorOnEmbeddedSubprocess();
  }

  @Deployment
  @Test
  public void testCatchErrorOnEmbeddedSubprocessWithoutErrorCode() {
    testCatchErrorOnEmbeddedSubprocess();
  }

  @Deployment
  @Test
  public void testCatchErrorOfInnerSubprocessOnOuterSubprocess() {
    runtimeService.startProcessInstanceByKey("boundaryErrorTest");

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("Inner subprocess task 1", tasks.get(0).getName());
    assertEquals("Inner subprocess task 2", tasks.get(1).getName());

    // Completing task 2, will cause the end error event to throw error with code 123
    taskService.complete(tasks.get(1).getId());
    tasks = taskService.createTaskQuery().list();
    Task taskAfterError = taskService.createTaskQuery().singleResult();
    assertEquals("task outside subprocess", taskAfterError.getName());
  }

  @Deployment
  @Test
  public void testCatchErrorInConcurrentEmbeddedSubprocesses() {
    assertErrorCaughtInConcurrentEmbeddedSubprocesses("boundaryEventTestConcurrentSubprocesses");
  }

  @Deployment
  @Test
  public void testCatchErrorInConcurrentEmbeddedSubprocessesThrownByScriptTask() {
    assertErrorCaughtInConcurrentEmbeddedSubprocesses("catchErrorInConcurrentEmbeddedSubprocessesThrownByScriptTask");
  }

  private void assertErrorCaughtInConcurrentEmbeddedSubprocesses(String processDefinitionKey) {
    // Completing task A will lead to task D
    String procId = runtimeService.startProcessInstanceByKey(processDefinitionKey).getId();
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task A", tasks.get(0).getName());
    assertEquals("task B", tasks.get(1).getName());
    taskService.complete(tasks.get(0).getId());
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task D", task.getName());
    taskService.complete(task.getId());
    testRule.assertProcessEnded(procId);

    // Completing task B will lead to task C
    procId = runtimeService.startProcessInstanceByKey(processDefinitionKey).getId();
    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task A", tasks.get(0).getName());
    assertEquals("task B", tasks.get(1).getName());
    taskService.complete(tasks.get(1).getId());

    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("task A", tasks.get(0).getName());
    assertEquals("task C", tasks.get(1).getName());
    taskService.complete(tasks.get(1).getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task A", task.getName());

    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task D", task.getName());
  }

  @Deployment
  @Test
  public void testDeeplyNestedErrorThrown() {

    // Input = 1 -> error1 will be thrown, which will destroy ALL BUT ONE
    // subprocess, which leads to an end event, which ultimately leads to ending the process instance
    String procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Nested task", task.getName());
    taskService.complete(task.getId(), CollectionUtil.singletonMap("input", 1));
    testRule.assertProcessEnded(procId);

    // Input == 2 -> error2 will be thrown, leading to a userTask outside all subprocesses
    procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown").getId();
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Nested task", task.getName());
    taskService.complete(task.getId(), CollectionUtil.singletonMap("input", 2));
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task after catch", task.getName());
    taskService.complete(task.getId());
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testDeeplyNestedErrorThrownOnlyAutomaticSteps() {
    // input == 1 -> error2 is thrown -> caught on subprocess2 -> end event in subprocess -> proc inst end 1
    String procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown",
            CollectionUtil.singletonMap("input", 1)).getId();
    testRule.assertProcessEnded(procId);

    HistoricProcessInstance hip;
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      hip = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
      assertEquals("processEnd1", hip.getEndActivityId());
    }
    // input == 2 -> error2 is thrown -> caught on subprocess1 -> proc inst end 2
    procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown",
            CollectionUtil.singletonMap("input", 1)).getId();
    testRule.assertProcessEnded(procId);

    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      hip = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
      assertEquals("processEnd1", hip.getEndActivityId());
    }
  }

  @Deployment(resources = {
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorOnCallActivity-parent.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml"
  })
  @Test
  public void testCatchErrorOnCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnCallActivity").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task in subprocess", task.getName());

    // Completing the task will reach the end error event,
    // which is caught on the call activity boundary
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Escalated Task", task.getName());

    // Completing the task will end the process instance
    taskService.complete(task.getId());
    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorOnCallActivity-parent.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml"
  })
  public void FAILING_testCatchErrorOnCallActivityShouldEndCalledProcessProperly() {
    // given a process instance that has instantiated (called) a sub process instance
    runtimeService.startProcessInstanceByKey("catchErrorOnCallActivity").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task in subprocess", task.getName());

    // when an error end event is triggered in the sub process instance and catched in the super process instance
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Escalated Task", task.getName());

    // then the called historic process instance should have properly ended
    HistoricProcessInstance historicSubProcessInstance = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("simpleSubProcess").singleResult();
    assertNotNull(historicSubProcessInstance);
    assertNull(historicSubProcessInstance.getDeleteReason());
    assertEquals("theEnd", historicSubProcessInstance.getEndActivityId());
  }

  @Deployment(resources = {
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml"
  })
  @Test
  public void testUncaughtError() {
    runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task in subprocess", task.getName());

    try {
      // Completing the task will reach the end error event,
      // which is never caught in the process
      taskService.complete(task.getId());
    } catch (BpmnError e) {
      testRule.assertTextPresent("No catching boundary event found for error with errorCode 'myError', neither in same process nor in parent process", e.getMessage());
    }
  }


  @Deployment(resources = {
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testUncaughtErrorOnCallActivity-parent.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml"
  })
  @Test
  public void testUncaughtErrorOnCallActivity() {
    runtimeService.startProcessInstanceByKey("uncaughtErrorOnCallActivity");
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task in subprocess", task.getName());

    try {
      // Completing the task will reach the end error event,
      // which is never caught in the process
      taskService.complete(task.getId());
    } catch (BpmnError e) {
      testRule.assertTextPresent("No catching boundary event found for error with errorCode 'myError', neither in same process nor in parent process", e.getMessage());
    }
  }

  @Deployment(resources = {
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByCallActivityOnSubprocess.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownByCallActivityOnSubprocess() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnSubprocess").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task in subprocess", task.getName());

    // Completing the task will reach the end error event,
    // which is caught on the call activity boundary
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Escalated Task", task.getName());

    // Completing the task will end the process instance
    taskService.complete(task.getId());
    testRule.assertProcessEnded(procId);
  }

  @Deployment(resources = {
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByCallActivityOnCallActivity.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess2ndLevel.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownByCallActivityOnCallActivity() throws InterruptedException {
      String procId = runtimeService.startProcessInstanceByKey("catchErrorOnCallActivity2ndLevel").getId();

      Task task = taskService.createTaskQuery().singleResult();
      assertEquals("Task in subprocess", task.getName());

      taskService.complete(task.getId());

      task = taskService.createTaskQuery().singleResult();
      assertEquals("Escalated Task", task.getName());

      // Completing the task will end the process instance
      taskService.complete(task.getId());
      testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorOnParallelMultiInstance() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnParallelMi").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(5, tasks.size());

    // Complete two subprocesses, just to make it a bit more complex
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("throwError", false);
    taskService.complete(tasks.get(2).getId(), vars);
    taskService.complete(tasks.get(3).getId(), vars);

    // Reach the error event
    vars.put("throwError", true);
    taskService.complete(tasks.get(1).getId(), vars);

    assertEquals(0, taskService.createTaskQuery().count());
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorOnSequentialMultiInstance() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnSequentialMi").getId();

    // complete one task
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("throwError", false);
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId(), vars);

    // complete second task and throw error
    vars.put("throwError", true);
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId(), vars);

    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorThrownBySignallableActivityBehaviour() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownBySignallableActivityBehaviour").getId();
    assertNotNull("Didn't get a process id from runtime service", procId);
    ActivityInstance processActivityInstance = runtimeService.getActivityInstance(procId);
    ActivityInstance serviceTask = processActivityInstance.getChildActivityInstances()[0];
    assertEquals("Expected the service task to be active after starting the process", "serviceTask", serviceTask.getActivityId());
    runtimeService.signal(serviceTask.getExecutionIds()[0]);
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByJavaDelegateOnServiceTask() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnServiceTask").getId();
    assertThatErrorHasBeenCaught(procId);

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("exceptionType", true);
    procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnServiceTask", variables).getId();
    assertThatExceptionHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByJavaDelegateOnServiceTaskNotCancelActivity() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnServiceTaskNotCancelActiviti").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByJavaDelegateOnServiceTaskWithErrorCode() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnServiceTaskWithErrorCode").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByJavaDelegateOnEmbeddedSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnEmbeddedSubProcess").getId();
    assertThatErrorHasBeenCaught(procId);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("exceptionType", true);
    procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnEmbeddedSubProcess", variables).getId();
    assertThatExceptionHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByJavaDelegateOnEmbeddedSubProcessInduction() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnEmbeddedSubProcessInduction").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment(resources = {
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-parent.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-child.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownByJavaDelegateOnCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnCallActivity-parent").getId();
    assertThatErrorHasBeenCaught(procId);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("exceptionType", true);
    procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnCallActivity-parent", variables).getId();
    assertThatExceptionHasBeenCaught(procId);
  }

  @Deployment(resources = {
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-child.bpmn20.xml"
  })
  @Test
  public void testUncaughtErrorThrownByJavaDelegateOnServiceTask() {
    try {
      runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnCallActivity-child");
    } catch (BpmnError e) {
      testRule.assertTextPresent("No catching boundary event found for error with errorCode '23', neither in same process nor in parent process", e.getMessage());
    }
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownByExecuteOfAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess", throwException()).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownByExecuteOfAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess", throwError()).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownBySignalMethodOfAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess").getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

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

  @Deployment
  @Test
  public void testCatchExceptionExpressionThrownByFollowUpTask() {
    try {
      Map<String, Object> vars = throwException();
      runtimeService.startProcessInstanceByKey("testProcess", vars).getId();
      fail("should fail and not catch the error on the first task");
    } catch (ProcessEngineException e) {
      // happy path
    }

    assertNull(taskService.createTaskQuery().singleResult());
  }

  @Deployment
  @Test
  public void testCatchExceptionClassDelegateThrownByFollowUpTask() {
    try {
      Map<String, Object> vars = throwException();
      runtimeService.startProcessInstanceByKey("testProcess", vars).getId();
      fail("should fail");
    } catch (ProcessEngineException e) {
      // happy path
    }

    assertNull(taskService.createTaskQuery().singleResult());
  }

  @Deployment
  @Test
  public void testCatchExceptionExpressionThrownByFollowUpScopeTask() {
    try {
      Map<String, Object> vars = throwException();
      runtimeService.startProcessInstanceByKey("testProcess", vars).getId();
      fail("should fail and not catch the error on the first task");
    } catch (ProcessEngineException e) {
      // happy path
    }
    assertNull(taskService.createTaskQuery().singleResult());
  }


  @Deployment( resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownBySignalOfAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess").getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

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
    "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownByExecuteOfDelegateExpression() {
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
    "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownByExecuteOfDelegateExpression() {
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
    "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchExceptionThrownBySignalMethodOfDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

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
    "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByDelegateExpression.bpmn20.xml"
  })
  @Test
  public void testCatchErrorThrownBySignalOfDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

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

  @Deployment(resources = {
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testUncaughtErrorThrownByJavaDelegateOnCallActivity-parent.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-child.bpmn20.xml"
  })
  @Test
  public void testUncaughtErrorThrownByJavaDelegateOnCallActivity() {
    try {
      runtimeService.startProcessInstanceByKey("uncaughtErrorThrownByJavaDelegateOnCallActivity-parent");
    } catch (BpmnError e) {
      testRule.assertTextPresent("No catching boundary event found for error with errorCode '23', neither in same process nor in parent process", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskSequential() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("executionsBeforeError", 2);
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskSequential", variables).getId();
    assertThatErrorHasBeenCaught(procId);

    variables.put("executionsBeforeError", 2);
    variables.put("exceptionType", true);
    procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskSequential", variables).getId();
    assertThatExceptionHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskParallel() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("executionsBeforeError", 2);
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskParallel", variables).getId();
    assertThatErrorHasBeenCaught(procId);

    variables.put("executionsBeforeError", 2);
    variables.put("exceptionType", true);
    procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskParallel", variables).getId();
    assertThatExceptionHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testErrorThrownByJavaDelegateNotCaughtByOtherEventType() {
    String procId = runtimeService.startProcessInstanceByKey("testErrorThrownByJavaDelegateNotCaughtByOtherEventType").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  private void assertThatErrorHasBeenCaught(String procId) {
    // The service task will throw an error event,
    // which is caught on the service task boundary
    assertEquals("No tasks found in task list.", 1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Escalated Task", task.getName());

    // Completing the task will end the process instance
    taskService.complete(task.getId());
    testRule.assertProcessEnded(procId);
  }

  private void assertThatExceptionHasBeenCaught(String procId) {
    // The service task will throw an error event,
    // which is caught on the service task boundary
    assertEquals("No tasks found in task list.", 1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Escalated Exception Task", task.getName());

    // Completing the task will end the process instance
    taskService.complete(task.getId());
    testRule.assertProcessEnded(procId);
  }

  @Deployment
  @Test
  public void testConcurrentExecutionsInterruptedOnDestroyScope() {

    // this test makes sure that if the first concurrent execution destroys the scope
    // (due to the interrupting boundary catch), the second concurrent execution does not
    // move forward.

    // if the test fails, it produces a constraint violation in db.

    runtimeService.startProcessInstanceByKey("process");
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByExpressionOnServiceTask() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("bpmnErrorBean", new BpmnErrorBean());
    String procId = runtimeService.startProcessInstanceByKey("testCatchErrorThrownByExpressionOnServiceTask", variables).getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByDelegateExpressionOnServiceTask() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("bpmnErrorBean", new BpmnErrorBean());
    String procId = runtimeService.startProcessInstanceByKey("testCatchErrorThrownByDelegateExpressionOnServiceTask", variables).getId();
    assertThatErrorHasBeenCaught(procId);

    variables.put("exceptionType", true);
    procId = runtimeService.startProcessInstanceByKey("testCatchErrorThrownByDelegateExpressionOnServiceTask", variables).getId();
    assertThatExceptionHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchErrorThrownByJavaDelegateProvidedByDelegateExpressionOnServiceTask() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("bpmnErrorBean", new BpmnErrorBean());
    String procId = runtimeService.startProcessInstanceByKey("testCatchErrorThrownByJavaDelegateProvidedByDelegateExpressionOnServiceTask", variables).getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchExceptionThrownByExpressionOnServiceTask() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("bpmnErrorBean", new BpmnErrorBean());
    String procId = runtimeService.startProcessInstanceByKey("testCatchExceptionThrownByExpressionOnServiceTask", variables).getId();
    assertThatExceptionHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchExceptionThrownByScriptTask() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    String procId = runtimeService.startProcessInstanceByKey("testCatchExceptionThrownByScriptTask", variables).getId();
    assertThatExceptionHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testCatchSpecializedExceptionThrownByDelegate() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("bpmnErrorBean", new BpmnErrorBean());
    String procId = runtimeService.startProcessInstanceByKey("testCatchSpecializedExceptionThrownByDelegate", variables).getId();
    assertThatExceptionHasBeenCaught(procId);
  }

  @Deployment
  @Test
  public void testUncaughtRuntimeException() {
    try {
      runtimeService.startProcessInstanceByKey("testUncaughtRuntimeException");
      fail("error should not be caught");
    } catch (RuntimeException e) {
      assertEquals("This should not be caught!", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testUncaughtBusinessExceptionWrongErrorCode() {
    try {
      runtimeService.startProcessInstanceByKey("testUncaughtBusinessExceptionWrongErrorCode");
      fail("error should not be caught");
    } catch (RuntimeException e) {
      assertEquals("couldn't execute activity <serviceTask id=\"serviceTask\" ...>: Business Exception", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testCatchErrorOnSubprocessThrownByNonInterruptingEventSubprocess() {
    runtimeService.startProcessInstanceByKey("testProcess");
    EventSubscription messageSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    runtimeService.messageEventReceived("message", messageSubscription.getExecutionId());

    // should successfully have reached the task following the boundary event
    Execution taskExecution = runtimeService.createExecutionQuery().activityId("afterBoundaryTask").singleResult();
    assertNotNull(taskExecution);
    Task task = taskService.createTaskQuery().executionId(taskExecution.getId()).singleResult();
    assertNotNull(task);
  }

  @Deployment
  @Test
  public void testCatchErrorOnSubprocessThrownByInterruptingEventSubprocess() {
    runtimeService.startProcessInstanceByKey("testProcess");
    EventSubscription messageSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    runtimeService.messageEventReceived("message", messageSubscription.getExecutionId());

    // should successfully have reached the task following the boundary event
    Execution taskExecution = runtimeService.createExecutionQuery().activityId("afterBoundaryTask").singleResult();
    assertNotNull(taskExecution);
    Task task = taskService.createTaskQuery().executionId(taskExecution.getId()).singleResult();
    assertNotNull(task);
  }

  @Deployment
  @Test
  public void testCatchErrorOnSubprocessThrownByNestedEventSubprocess() {
    runtimeService.startProcessInstanceByKey("testProcess");

    // trigger outer event subprocess
    EventSubscription messageSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    runtimeService.messageEventReceived("outerMessage", messageSubscription.getExecutionId());

    // trigger inner event subprocess
    messageSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    runtimeService.messageEventReceived("innerMessage", messageSubscription.getExecutionId());

    // should successfully have reached the task following the boundary event
    Execution taskExecution = runtimeService.createExecutionQuery().activityId("afterBoundaryTask").singleResult();
    assertNotNull(taskExecution);
    Task task = taskService.createTaskQuery().executionId(taskExecution.getId()).singleResult();
    assertNotNull(task);
  }

  @Deployment
  @Test
  public void testCatchErrorOnSubprocessSetsErrorVariables(){
    runtimeService.startProcessInstanceByKey("Process_1");
    //the name used in "camunda:errorCodeVariable" in the BPMN
    String variableName = "errorVariable";
    Object errorCode = "error1";

    checkErrorVariable(variableName, errorCode);
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/error/ThrowErrorProcess.bpmn",
      "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByCallActivityOnSubprocessSetsErrorCodeVariable.bpmn"
  })
  @Test
  public void testCatchErrorThrownByCallActivityOnSubprocessSetsErrorVariables(){
    runtimeService.startProcessInstanceByKey("Process_1");
    //the name used in "camunda:errorCodeVariable" in the BPMN
    String variableName = "errorVariable";
    //the code we gave the thrown error
    Object errorCode = "error";

    checkErrorVariable(variableName, errorCode);
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByMultiInstanceSubProcessSetsErrorCodeVariable.bpmn"
  })
  @Test
  public void testCatchErrorThrownByMultiInstanceSubProcessSetsErrorVariables(){
    runtimeService.startProcessInstanceByKey("Process_1");
    //the name used in "camunda:errorCodeVariable" in the BPMN
    String variableName = "errorVariable";
    //the code we gave the thrown error
    Object errorCode = "error";

    checkErrorVariable(variableName, errorCode);
  }

  private void checkErrorVariable(String variableName, Object expectedValue){
    VariableInstance errorVariable = runtimeService.createVariableInstanceQuery().variableName(variableName).singleResult();
    assertThat(errorVariable).isNotNull();
    assertThat(errorVariable.getValue()).isEqualTo(expectedValue);
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchBpmnErrorThrownByJavaDelegateInCallActivityOnSubprocessSetsErrorVariables.bpmn",
    "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithThrownError.bpmn"
  })
  @Test
  public void testCatchBpmnErrorThrownByJavaDelegateInCallActivityOnSubprocessSetsErrorVariables(){
    runtimeService.startProcessInstanceByKey("Process_1");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    //the name used in "camunda:errorCodeVariable" in the BPMN
    String variableName = "errorCode";
    //the code we gave the thrown error
    Object errorCode = "errorCode";
    checkErrorVariable(variableName, errorCode);
    checkErrorVariable("errorMessageVariable", "ouch!");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/error/reviewSalesLead.bpmn20.xml"})
  @Test
  public void testReviewSalesLeadProcess() {

    // After starting the process, a task should be assigned to the 'initiator' (normally set by GUI)
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("details", "very interesting");
    variables.put("customerName", "Alfresco");
    String procId = runtimeService.startProcessInstanceByKey("reviewSaledLead", variables).getId();
    Task task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
    assertEquals("Provide new sales lead", task.getName());

    // After completing the task, the review subprocess will be active
    taskService.complete(task.getId());
    Task ratingTask = taskService.createTaskQuery().taskCandidateGroup("accountancy").singleResult();
    assertEquals("Review customer rating", ratingTask.getName());
    Task profitabilityTask = taskService.createTaskQuery().taskCandidateGroup("management").singleResult();
    assertEquals("Review profitability", profitabilityTask.getName());

    // Complete the management task by stating that not enough info was provided
    // This should throw the error event, which closes the subprocess
    variables = new HashMap<String, Object>();
    variables.put("notEnoughInformation", true);
    taskService.complete(profitabilityTask.getId(), variables);

    // The 'provide additional details' task should now be active
    Task provideDetailsTask = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
    assertEquals("Provide additional details", provideDetailsTask.getName());

    // Providing more details (ie. completing the task), will activate the subprocess again
    taskService.complete(provideDetailsTask.getId());
    List<Task> reviewTasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals("Review customer rating", reviewTasks.get(0).getName());
    assertEquals("Review profitability", reviewTasks.get(1).getName());

    // Completing both tasks normally ends the process
    taskService.complete(reviewTasks.get(0).getId());
    variables.put("notEnoughInformation", false);
    taskService.complete(reviewTasks.get(1).getId(), variables);
    testRule.assertProcessEnded(procId);
  }
}
