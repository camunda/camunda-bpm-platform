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
package org.camunda.bpm.engine.test.bpmn.tasklistener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.RecorderTaskListener;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.junit.Before;
import org.junit.Test;

public class TaskListenerErrorThrowTest extends AbstractTaskListenerTest {
  /*
  Testing BPMN Error Throw in Task Listener
   */

  public static final String ERROR_CODE = "208";

  @Before
  public void resetListenerCounters() {
    ThrowBPMNErrorListener.reset();
  }

  @Test
  public void testThrowErrorOnCreateAndCatchOnUserTask() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnUserTask(TaskListener.EVENTNAME_CREATE);

    testRule.deploy(model);

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnAssignmentAndCatchOnUserTask() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnUserTask(TaskListener.EVENTNAME_ASSIGNMENT);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    firstTask.setAssignee("elmo");
    engineRule.getTaskService().saveTask(firstTask);

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCompleteAndCatchOnUserTask() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnUserTask(TaskListener.EVENTNAME_COMPLETE);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    taskService.complete(firstTask.getId());

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCreateAndCatchOnSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnSubprocess(TaskListener.EVENTNAME_CREATE);

    testRule.deploy(model);

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnAssignmentAndCatchOnSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnSubprocess(TaskListener.EVENTNAME_ASSIGNMENT);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    firstTask.setAssignee("elmo");
    engineRule.getTaskService().saveTask(firstTask);

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCompleteAndCatchOnSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnSubprocess(TaskListener.EVENTNAME_COMPLETE);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    taskService.complete(firstTask.getId());

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCreateAndCatchOnEventSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnEventSubprocess(TaskListener.EVENTNAME_CREATE);
    System.out.println(Bpmn.convertToString(model));
    testRule.deploy(model);

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnAssignmentAndCatchOnEventSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnEventSubprocess(TaskListener.EVENTNAME_ASSIGNMENT);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    firstTask.setAssignee("elmo");
    engineRule.getTaskService().saveTask(firstTask);

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCompleteAndCatchOnEventSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnEventSubprocess(TaskListener.EVENTNAME_COMPLETE);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    taskService.complete(firstTask.getId());

    // then
    verifyErrorGotCaught();
  }

  @Test
  @Deployment
  public void testThrowErrorOnCreateScriptListenerAndCatchOnUserTask() {
    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    Task resultTask = taskService.createTaskQuery().singleResult();
    assertNotNull(resultTask);
    assertEquals("afterCatch", resultTask.getName());
  }

  @Test
  public void testThrowErrorOnAssignmentExpressionListenerAndCatchOnUserTask() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ThrowBPMNErrorListener());
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
                                  .startEvent()
                                    .userTask("mainTask")
                                      .camundaTaskListenerExpression(TaskListener.EVENTNAME_ASSIGNMENT, "${myListener.notify(task)}")
                                      .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, RecorderTaskListener.class.getName())
                                      .boundaryEvent("throw")
                                        .error(ERROR_CODE)
                                    .userTask("afterCatch")
                                    .moveToActivity("mainTask")
                                    .userTask("afterThrow")
                                  .endEvent()
                                  .done();
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    firstTask.setAssignee("elmo");
    engineRule.getTaskService().saveTask(firstTask);

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnDeleteAndCatchOnUserTaskShouldNotTriggerPropagation() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnUserTask(TaskListener.EVENTNAME_DELETE);

    DeploymentWithDefinitions deployment      = testRule.deploy(model);
    ProcessInstance           processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    try {
      runtimeService.deleteProcessInstance(processInstance.getId(), "invoke delete listener");
    } catch (Exception e) {
      // then
      assertTrue(e.getMessage().contains("business error"));
      assertEquals(1, ThrowBPMNErrorListener.INVOCATIONS);
      assertEquals(0, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_DELETE));
    }

    // cleanup
    engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true, true);
  }

  @Test
  public void testThrowUncaughtErrorOnCompleteAndCatchOnUserTask() {
    // given
    processEngineConfiguration.setEnableExceptionsAfterUnhandledBpmnError(true);
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
                                  .startEvent()
                                    .userTask("mainTask")
                                      .camundaTaskListenerClass(TaskListener.EVENTNAME_COMPLETE, ThrowBPMNErrorListener.class.getName())
                                      .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, RecorderTaskListener.class.getName())
                                    .userTask("afterThrow")
                                  .endEvent()
                                  .done();

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    try {
      // when
      taskService.complete(firstTask.getId());
    } catch (ProcessEngineException e) {
      // then
      assertTrue(e.getMessage().contains("There was an exception while invoking the TaskListener"));
      assertTrue(e.getMessage().contains("Execution with id 'mainTask' throws an error event with errorCode '208', but no error handler was defined."));
    }

    // then
    Task resultTask = taskService.createTaskQuery().singleResult();
    assertNotNull(resultTask);
    assertEquals("mainTask", resultTask.getName());
    assertEquals(1, ThrowBPMNErrorListener.INVOCATIONS);
    assertEquals(0, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_DELETE));

    // cleanup
    processEngineConfiguration.setEnableExceptionsAfterUnhandledBpmnError(false);
  }

  // HELPER methods

  protected void verifyErrorGotCaught() {
    Task resultTask = taskService.createTaskQuery().singleResult();
    assertNotNull(resultTask);
    assertEquals("afterCatch", resultTask.getName());
    assertEquals(1, ThrowBPMNErrorListener.INVOCATIONS);
    assertEquals(1, RecorderTaskListener.getEventCount(TaskListener.EVENTNAME_DELETE));
  }

  protected BpmnModelInstance createModelThrowErrorInListenerAndCatchOnUserTask(String eventName) {
    return Bpmn.createExecutableProcess("process")
               .startEvent()
                 .userTask("mainTask")
                   .camundaTaskListenerClass(eventName, ThrowBPMNErrorListener.class.getName())
                   .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, RecorderTaskListener.class.getName())
                   .boundaryEvent("throw")
                     .error(ERROR_CODE)
                 .userTask("afterCatch")
                 .moveToActivity("mainTask")
                 .userTask("afterThrow")
               .endEvent()
               .done();
  }

  protected BpmnModelInstance createModelThrowErrorInListenerAndCatchOnSubprocess(String eventName) {
    return Bpmn.createExecutableProcess("process")
               .startEvent()
                 .subProcess("sub")
                 .embeddedSubProcess()
                   .startEvent("inSub")
                     .userTask("mainTask")
                       .camundaTaskListenerClass(eventName, ThrowBPMNErrorListener.class.getName())
                       .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, RecorderTaskListener.class.getName())
                     .userTask("afterThrow")
                   .endEvent()
                 .moveToActivity("sub")
                   .boundaryEvent("throw")
                     .error(ERROR_CODE)
                 .userTask("afterCatch")
               .endEvent()
               .done();
  }

  protected BpmnModelInstance createModelThrowErrorInListenerAndCatchOnEventSubprocess(String eventName) {
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process");
    BpmnModelInstance model = processBuilder
        .startEvent()
          .userTask("mainTask")
            .camundaTaskListenerClass(eventName, ThrowBPMNErrorListener.class.getName())
            .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, RecorderTaskListener.class.getName())
          .userTask("afterThrow")
          .endEvent()
        .done();
    processBuilder.eventSubProcess()
                  .startEvent("errorEvent").error(ERROR_CODE)
                    .userTask("afterCatch")
                  .endEvent();
    return model;
  }

  public static class ThrowBPMNErrorListener implements TaskListener {
    public static int INVOCATIONS = 0;

    public void notify(DelegateTask delegateTask) {
      INVOCATIONS++;
      throw new BpmnError(ERROR_CODE, "business error 208");
    }

    public static void reset() {
      INVOCATIONS = 0;
    }
  }
}