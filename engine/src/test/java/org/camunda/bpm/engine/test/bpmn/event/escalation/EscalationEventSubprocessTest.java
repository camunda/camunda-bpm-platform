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

package org.camunda.bpm.engine.test.bpmn.event.escalation;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Philipp Ossler
 */
public class EscalationEventSubprocessTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testCatchEscalationEventInsideSubprocess() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting event subprocess inside the subprocess should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
    // and continue the subprocess
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment
  public void testCatchEscalationEventFromEmbeddedSubprocess() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting event subprocess outside the subprocess should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
    // and continue the subprocess
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.throwEscalationEvent.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testCatchEscalationEventFromCallActivity.bpmn20.xml"})
  public void testCatchEscalationEventFromCallActivity() {
    runtimeService.startProcessInstanceByKey("catchEscalationProcess");
    // when throw an escalation event on called process

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting event subprocess should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
    // and continue the called process
    assertEquals(1, taskService.createTaskQuery().taskName("task after thrown escalation").count());
  }

  @Deployment
  public void testCatchEscalationEventFromTopLevelProcess() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event from top level process

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting event subprocess on the top level process should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
    // and continue the process
    assertEquals(1, taskService.createTaskQuery().taskName("task after thrown escalation").count());
  }

  @Deployment
  public void testCatchEscalationEventFromMultiInstanceSubprocess() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside a multi-instance subprocess

    assertEquals(10, taskService.createTaskQuery().count());
    // the non-interrupting event subprocess outside the subprocess should catch every escalation event
    assertEquals(5, taskService.createTaskQuery().taskName("task after catched escalation").count());
    // and continue the subprocess
    assertEquals(5, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment
  public void testPreferEscalationEventSubprocessToBoundaryEvent() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting event subprocess inside the subprocess should catch the escalation event
    // (the boundary event on the subprocess should not catch the escalation event since the event subprocess consume this event)
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation inside subprocess").count());
    // and continue the subprocess
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment
  public void testEscalationEventSubprocessWithEscalationCode() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess with escalationCode=1

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting event subprocess with escalationCode=1 should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation 1").count());
    // and continue the subprocess
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment
  public void testEscalationEventSubprocessWithoutEscalationCode() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting event subprocess without escalationCode should catch the escalation event (and all other escalation events)
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
    // and continue the subprocess
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment
  public void testInterruptionEscalationEventSubprocess() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    // the interrupting event subprocess inside the subprocess should catch the escalation event event and cancel the subprocess
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.throwEscalationEvent.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testInterruptingEscalationEventSubprocessWithCallActivity.bpmn20.xml"})
  public void testInterruptingEscalationEventSubprocessWithCallActivity() {
    runtimeService.startProcessInstanceByKey("catchEscalationProcess");
    // when throw an escalation event on called process

    // the interrupting event subprocess should catch the escalation event and cancel the called process
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
  }

  @Deployment
  public void testInterruptionEscalationEventSubprocessWithMultiInstanceSubprocess() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the multi-instance subprocess

    // the interrupting event subprocess outside the subprocess should catch the first escalation event and cancel all instances of the subprocess
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
  }

  @Deployment
  public void testReThrowEscalationEventToBoundaryEvent() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    // the non-interrupting event subprocess inside the subprocess should catch the escalation event
    Task task = taskService.createTaskQuery().taskName("task after catched escalation inside subprocess").singleResult();
    assertNotNull(task);

    // when re-throw the escalation event from the escalation event subprocess
    taskService.complete(task.getId());

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting boundary event on subprocess should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation on boundary event").count());
    // and continue the process
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment
  public void testReThrowEscalationEventToBoundaryEventWithoutEscalationCode() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    // the non-interrupting event subprocess inside the subprocess should catch the escalation event
    Task task = taskService.createTaskQuery().taskName("task after catched escalation inside subprocess").singleResult();
    assertNotNull(task);

    // when re-throw the escalation event from the escalation event subprocess
    taskService.complete(task.getId());

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting boundary event on subprocess without escalationCode should catch the escalation event (and all other escalation events)
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation on boundary event").count());
    // and continue the process
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment
  public void testReThrowEscalationEventToEventSubprocess() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    // the non-interrupting event subprocess inside the subprocess should catch the escalation event
    Task task = taskService.createTaskQuery().taskName("task after catched escalation inside subprocess").singleResult();
    assertNotNull(task);

    // when re-throw the escalation event from the escalation event subprocess
    taskService.complete(task.getId());

    assertEquals(2, taskService.createTaskQuery().count());
    // the non-interrupting event subprocess on process level should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation on process level").count());
    // and continue the process
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment
  public void testReThrowEscalationEventIsNotCatched() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    // the non-interrupting event subprocess inside the subprocess should catch the escalation event
    Task task = taskService.createTaskQuery().taskName("task after catched escalation inside subprocess").singleResult();
    assertNotNull(task);

    // when re-throw the escalation event from the escalation event subprocess
    taskService.complete(task.getId());

    // continue the subprocess, no activity should catch the re-thrown escalation event
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment
  public void testThrowEscalationEventToEventSubprocess() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    assertEquals(2, taskService.createTaskQuery().count());
    // the first non-interrupting event subprocess inside the subprocess should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation inside subprocess1").count());
    // and continue the subprocess
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());

    // when throw a second escalation event from the first event subprocess
    String taskId = taskService.createTaskQuery().taskName("task after catched escalation inside subprocess1").singleResult().getId();
    taskService.complete(taskId);

    assertEquals(2, taskService.createTaskQuery().count());
    // the second non-interrupting event subprocess inside the subprocess should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation inside subprocess2").count());
    assertEquals(1, taskService.createTaskQuery().taskName("task in subprocess").count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.throwEscalationEvent.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testPropagateOutputVariablesWhileCatchEscalationOnCallActivity.bpmn20.xml"})
  public void testPropagateOutputVariablesWhileCatchEscalationOnCallActivity() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("input", 42);
    String processInstanceId = runtimeService.startProcessInstanceByKey("catchEscalationProcess", variables).getId();
    // when throw an escalation event on called process

    // the non-interrupting event subprocess should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
    // and set the output variable of the called process to the process
    assertEquals(42, runtimeService.getVariable(processInstanceId, "output"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.throwEscalationEvent.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testPropagateOutputVariablesWhileCatchEscalationOnCallActivity.bpmn20.xml"})
  public void testPropagateOutputVariablesTwoTimes() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("input", 42);
    String processInstanceId = runtimeService.startProcessInstanceByKey("catchEscalationProcess", variables).getId();
    // when throw an escalation event on called process

    // (1) the variables has been passed for the first time (from sub process to super process)
    Task taskInSuperProcess = taskService.createTaskQuery().taskDefinitionKey("taskAfterCatchedEscalation").singleResult();
    assertNotNull(taskInSuperProcess);
    assertEquals(42, runtimeService.getVariable(processInstanceId, "output"));

    // change variable "input" in sub process
    Task taskInSubProcess = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    runtimeService.setVariable(taskInSubProcess.getProcessInstanceId(), "input", 999);
    taskService.complete(taskInSubProcess.getId());

    // (2) the variables has been passed for the second time (from sub process to super process)
    assertEquals(999, runtimeService.getVariable(processInstanceId, "output"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.throwEscalationEvent.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testPropagateOutputVariablesWhileCatchInterruptingEscalationOnCallActivity.bpmn20.xml"})
  public void testPropagateOutputVariablesWhileCatchInterruptingEscalationOnCallActivity() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("input", 42);
    String processInstanceId = runtimeService.startProcessInstanceByKey("catchEscalationProcess", variables).getId();
    // when throw an escalation event on called process

    // the interrupting event subprocess should catch the escalation event
    assertEquals(1, taskService.createTaskQuery().taskName("task after catched escalation").count());
    // and set the output variable of the called process to the process
    assertEquals(42, runtimeService.getVariable(processInstanceId, "output"));
  }

  @Deployment
  public void testRetrieveEscalationCodeVariableOnEventSubprocess() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    // the event subprocess should catch the escalation event
    Task task = taskService.createTaskQuery().taskName("task after catched escalation").singleResult();
    assertNotNull(task);

    // and set the escalationCode of the escalation event to the declared variable
    assertEquals("escalationCode", runtimeService.getVariable(task.getExecutionId(), "escalationCodeVar"));
  }

  @Deployment
  public void testRetrieveEscalationCodeVariableOnEventSubprocessWithoutEscalationCode() {
    runtimeService.startProcessInstanceByKey("escalationProcess");
    // when throw an escalation event inside the subprocess

    // the event subprocess without escalationCode should catch the escalation event
    Task task = taskService.createTaskQuery().taskName("task after catched escalation").singleResult();
    assertNotNull(task);

    // and set the escalationCode of the escalation event to the declared variable
    assertEquals("escalationCode", runtimeService.getVariable(task.getExecutionId(), "escalationCodeVar"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.throwEscalationEvent.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testInterruptingRetrieveEscalationCodeInSuperProcess.bpmn20.xml"})
  public void testInterruptingRetrieveEscalationCodeInSuperProcess() {
    runtimeService.startProcessInstanceByKey("catchEscalationProcess");

    // the event subprocess without escalationCode should catch the escalation event
    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterCatchedEscalation").singleResult();
    assertNotNull(task);

    // and set the escalationCode of the escalation event to the declared variable
    assertEquals("escalationCode", runtimeService.getVariable(task.getExecutionId(), "escalationCodeVar"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.throwEscalationEvent.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testInterruptingRetrieveEscalationCodeInSuperProcessWithoutEscalationCode.bpmn20.xml"})
  public void testInterruptingRetrieveEscalationCodeInSuperProcessWithoutEscalationCode() {
    runtimeService.startProcessInstanceByKey("catchEscalationProcess");

    // the event subprocess without escalationCode should catch the escalation event
    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterCatchedEscalation").singleResult();
    assertNotNull(task);

    // and set the escalationCode of the escalation event to the declared variable
    assertEquals("escalationCode", runtimeService.getVariable(task.getExecutionId(), "escalationCodeVar"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.throwEscalationEvent.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testNonInterruptingRetrieveEscalationCodeInSuperProcess.bpmn20.xml"})
  public void testNonInterruptingRetrieveEscalationCodeInSuperProcess() {
    runtimeService.startProcessInstanceByKey("catchEscalationProcess");

    // the event subprocess without escalationCode should catch the escalation event
    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterCatchedEscalation").singleResult();
    assertNotNull(task);

    // and set the escalationCode of the escalation event to the declared variable
    assertEquals("escalationCode", runtimeService.getVariable(task.getExecutionId(), "escalationCodeVar"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventTest.throwEscalationEvent.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testNonInterruptingRetrieveEscalationCodeInSuperProcessWithoutEscalationCode.bpmn20.xml"})
  public void testNonInterruptingRetrieveEscalationCodeInSuperProcessWithoutEscalationCode() {
    runtimeService.startProcessInstanceByKey("catchEscalationProcess");

    // the event subprocess without escalationCode should catch the escalation event
    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterCatchedEscalation").singleResult();
    assertNotNull(task);

    // and set the escalationCode of the escalation event to the declared variable
    assertEquals("escalationCode", runtimeService.getVariable(task.getExecutionId(), "escalationCodeVar"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testNonInterruptingEscalationTriggeredTwice.bpmn20.xml"})
  public void testNonInterruptingEscalationTriggeredTwiceWithMainTaskCompletedFirst() {

    // given
    runtimeService.startProcessInstanceByKey("escalationProcess");
    Task taskInMainprocess = taskService.createTaskQuery().taskDefinitionKey("TaskInMainprocess").singleResult();

    // when
    taskService.complete(taskInMainprocess.getId());

    // then
    assertEquals(2, taskService.createTaskQuery().taskDefinitionKey("TaskInSubprocess").count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testNonInterruptingEscalationTriggeredTwice.bpmn20.xml"})
  public void testNonInterruptingEscalationTriggeredTwiceWithSubprocessTaskCompletedFirst() {

    // given
    runtimeService.startProcessInstanceByKey("escalationProcess");
    Task taskInMainprocess = taskService.createTaskQuery().taskDefinitionKey("TaskInMainprocess").singleResult();
    Task taskInSubprocess = taskService.createTaskQuery().taskDefinitionKey("TaskInSubprocess").singleResult();

    // when
    taskService.complete(taskInSubprocess.getId());
    taskService.complete(taskInMainprocess.getId());

    // then
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("TaskInSubprocess").count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testNonInterruptingEscalationTriggeredTwiceByIntermediateEvent.bpmn20.xml"})
  public void testNonInterruptingEscalationTriggeredTwiceByIntermediateEventWithMainTaskCompletedFirst() {

    // given
    runtimeService.startProcessInstanceByKey("escalationProcess");
    Task taskInMainprocess = taskService.createTaskQuery().taskDefinitionKey("FirstTaskInMainprocess").singleResult();

    // when
    taskService.complete(taskInMainprocess.getId());

    // then
    assertEquals(2, taskService.createTaskQuery().taskDefinitionKey("TaskInSubprocess").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("SecondTaskInMainprocess").count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/escalation/EscalationEventSubprocessTest.testNonInterruptingEscalationTriggeredTwiceByIntermediateEvent.bpmn20.xml"})
  public void testNonInterruptingEscalationTriggeredTwiceByIntermediateEventWithSubprocessTaskCompletedFirst() {

    // given
    runtimeService.startProcessInstanceByKey("escalationProcess");
    Task taskInMainprocess = taskService.createTaskQuery().taskDefinitionKey("FirstTaskInMainprocess").singleResult();
    Task taskInSubprocess = taskService.createTaskQuery().taskDefinitionKey("TaskInSubprocess").singleResult();

    // when
    taskService.complete(taskInSubprocess.getId());
    taskService.complete(taskInMainprocess.getId());

    // then
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("TaskInSubprocess").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("SecondTaskInMainprocess").count());
  }

}