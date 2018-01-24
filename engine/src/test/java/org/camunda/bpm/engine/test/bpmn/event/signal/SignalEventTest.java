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

package org.camunda.bpm.engine.test.bpmn.event.signal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.variables.FailingJavaSerializable;
import org.camunda.bpm.engine.test.bpmn.executionlistener.RecorderExecutionListener;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Daniel Meyer
 */
public class SignalEventTest {

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setJavaSerializationFormatEnabled(true);
      return configuration;
    }
  };
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private RuntimeService runtimeService;
  private TaskService taskService;
  private RepositoryService repositoryService;
  private ManagementService managementService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
    managementService = engineRule.getManagementService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  @Test
  public void testSignalCatchIntermediate() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalBoundary.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  @Test
  public void testSignalCatchBoundary() {
    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalBoundaryWithReceiveTask.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  @Test
  public void testSignalCatchBoundaryWithVariables() {
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("processName", "catchSignal");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("catchSignal", variables1);

    HashMap<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("processName", "throwSignal");
    runtimeService.startProcessInstanceByKey("throwSignal", variables2);

    assertEquals("catchSignal", runtimeService.getVariable(pi.getId(), "processName"));
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignalAsynch.bpmn20.xml"})
  @Test
  public void testSignalCatchIntermediateAsynch() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // there is a job:
    assertEquals(1, managementService.createJobQuery().count());

    try {
      ClockUtil.setCurrentTime(new Date(System.currentTimeMillis() + 1000));
      testRule.waitForJobExecutorToProcessAllJobs(10000);

      assertEquals(0, createEventSubscriptionQuery().count());
      assertEquals(0, runtimeService.createProcessInstanceQuery().count());
      assertEquals(0, managementService.createJobQuery().count());
    } finally {
      ClockUtil.setCurrentTime(new Date());
    }

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchMultipleSignals.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAbortSignal.bpmn20.xml"})
  @Test
  public void testSignalCatchDifferentSignals() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(2, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    runtimeService.startProcessInstanceByKey("throwAbort");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    Task taskAfterAbort = taskService.createTaskQuery().taskAssignee("gonzo").singleResult();
    assertNotNull(taskAfterAbort);
    taskService.complete(taskAfterAbort.getId());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  /**
   * Verifies the solution of https://jira.codehaus.org/browse/ACT-1309
   */
  @Deployment
  @Test
  public void testSignalBoundaryOnSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("signalEventOnSubprocess");
    runtimeService.signalEventReceived("stopSignal");
    testRule.assertProcessEnded(pi.getProcessInstanceId());
  }

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutorTxRequired());
  }

  /**
   * TestCase to reproduce Issue ACT-1344
   */
  @Deployment
  @Test
  public void testNonInterruptingSignal() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingSignalEvent");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());
    Task currentTask = tasks.get(0);
    assertEquals("My User Task", currentTask.getName());

    runtimeService.signalEventReceived("alert");

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      if (!task.getName().equals("My User Task") && !task.getName().equals("My Second User Task")) {
        fail("Expected: <My User Task> or <My Second User Task> but was <" + task.getName() + ">.");
      }
    }

    taskService.complete(taskService.createTaskQuery().taskName("My User Task").singleResult().getId());

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());
    currentTask = tasks.get(0);
    assertEquals("My Second User Task", currentTask.getName());
  }


  /**
   * TestCase to reproduce Issue ACT-1344
   */
  @Deployment
  @Test
  public void testNonInterruptingSignalWithSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingSignalWithSubProcess");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());

    Task currentTask = tasks.get(0);
    assertEquals("Approve", currentTask.getName());

    runtimeService.signalEventReceived("alert");

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      if (!task.getName().equals("Approve") && !task.getName().equals("Review")) {
        fail("Expected: <Approve> or <Review> but was <" + task.getName() + ">.");
      }
    }

    taskService.complete(taskService.createTaskQuery().taskName("Approve").singleResult().getId());

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());

    currentTask = tasks.get(0);
    assertEquals("Review", currentTask.getName());

    taskService.complete(taskService.createTaskQuery().taskName("Review").singleResult().getId());

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());

  }

  @Deployment
  @Test
  public void testSignalStartEventInEventSubProcess() {
    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalStartEventInEventSubProcess");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    // send interrupting signal to event sub process
    runtimeService.signalEventReceived("alert");

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task doesn't exist because signal start event is interrupting
    taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(0, taskQuery.count());

    // check if execution doesn't exist because signal start event is interrupting
    executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(0, executionQuery.count());
  }

  @Deployment
  @Test
  public void testNonInterruptingSignalStartEventInEventSubProcess() {
    // start process instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingSignalStartEventInEventSubProcess");

    // check if execution exists
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());

    // check if user task exists
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    // send non interrupting signal to event sub process
    runtimeService.signalEventReceived("alert");

    assertEquals(true, DummyServiceTask.wasExecuted);

    // check if user task still exists because signal start event is non interrupting
    taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());

    // check if execution still exists because signal start event is non interrupting
    executionQuery = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId());
    assertEquals(1, executionQuery.count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml"})
  @Test
  public void testSignalStartEvent() {
    // event subscription for signal start event
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert").count());

    runtimeService.signalEventReceived("alert");
    // the signal should start a new process instance
    assertEquals(1, taskService.createTaskQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml"})
  @Test
  public void testSuspendedProcessWithSignalStartEvent() {
    // event subscription for signal start event
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert").count());

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    runtimeService.signalEventReceived("alert");
    // the signal should not start a process instance for the suspended process definition
    assertEquals(0, taskService.createTaskQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.testOtherSignalStartEvent.bpmn20.xml"})
  @Test
  public void testMultipleProcessesWithSameSignalStartEvent() {
    // event subscriptions for signal start event
    assertEquals(2, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert").count());

    runtimeService.signalEventReceived("alert");
    // the signal should start new process instances for both process definitions
    assertEquals(2, taskService.createTaskQuery().count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  @Test
  public void testStartProcessInstanceBySignalFromIntermediateThrowingSignalEvent() {
    // start a process instance to throw a signal
    runtimeService.startProcessInstanceByKey("throwSignal");
    // the signal should start a new process instance
    assertEquals(1, taskService.createTaskQuery().count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  @Test
  public void testIntermediateThrowingSignalEventWithSuspendedSignalStartEvent() {
    // event subscription for signal start event
    assertEquals(1, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert").count());

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("startBySignal").singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    // start a process instance to throw a signal
    runtimeService.startProcessInstanceByKey("throwSignal");
    // the signal should not start a new process instance of the suspended process definition
    assertEquals(0, taskService.createTaskQuery().count());
  }

  @Deployment
  @Test
  public void testProcessesWithMultipleSignalStartEvents() {
    // event subscriptions for signal start event
    assertEquals(2, runtimeService.createEventSubscriptionQuery().eventType("signal").count());

    runtimeService.signalEventReceived("alert");
    // the signal should start new process instances for both process definitions
    assertEquals(1, taskService.createTaskQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertTwiceAndTerminate.bpmn20.xml"})
  @Test
  public void testThrowSignalMultipleCancellingReceivers() {
    RecorderExecutionListener.clear();

    runtimeService.startProcessInstanceByKey("catchAlertTwiceAndTerminate");

    // event subscription for intermediate signal events
    assertEquals(2, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert").count());

    // try to send 'alert' signal to both executions
    runtimeService.signalEventReceived("alert");

    // then only one terminate end event was executed
    assertEquals(1, RecorderExecutionListener.getRecordedEvents().size());

    // and instances ended successfully
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertTwiceAndTerminate.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  @Test
  public void testIntermediateThrowSignalMultipleCancellingReceivers() {
    RecorderExecutionListener.clear();

    runtimeService.startProcessInstanceByKey("catchAlertTwiceAndTerminate");

    // event subscriptions for intermediate events
    assertEquals(2, runtimeService.createEventSubscriptionQuery().eventType("signal").eventName("alert").count());

    // started process instance try to send 'alert' signal to both executions
    runtimeService.startProcessInstanceByKey("throwSignal");

    // then only one terminate end event was executed
    assertEquals(1, RecorderExecutionListener.getRecordedEvents().size());

    // and both instances ended successfully
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignalAsync.bpmn20.xml"})
  @Test
  public void testAsyncSignalStartEventJobProperties() {
    ProcessDefinition catchingProcessDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("startBySignal")
        .singleResult();

    // given a process instance that throws a signal asynchronously
    runtimeService.startProcessInstanceByKey("throwSignalAsync");
    // where the throwing instance ends immediately

    // then there is not yet a catching process instance
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // but there is a job for the asynchronous continuation
    Job asyncJob = managementService.createJobQuery().singleResult();
    assertEquals(catchingProcessDefinition.getId(), asyncJob.getProcessDefinitionId());
    assertEquals(catchingProcessDefinition.getKey(), asyncJob.getProcessDefinitionKey());
    assertNull(asyncJob.getExceptionMessage());
    assertNull(asyncJob.getExecutionId());
    assertNull(asyncJob.getJobDefinitionId());
    assertEquals(0, asyncJob.getPriority());
    assertNull(asyncJob.getProcessInstanceId());
    assertEquals(3, asyncJob.getRetries());
    assertNull(asyncJob.getDuedate());
    assertNull(asyncJob.getDeploymentId());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignalAsync.bpmn20.xml"})
  @Test
  public void testAsyncSignalStartEvent() {
    ProcessDefinition catchingProcessDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("startBySignal")
        .singleResult();

    // given a process instance that throws a signal asynchronously
    runtimeService.startProcessInstanceByKey("throwSignalAsync");

    // with an async job to trigger the signal event
    Job job = managementService.createJobQuery().singleResult();

    // when the job is executed
    managementService.executeJob(job.getId());

    // then there is a process instance
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
    assertEquals(catchingProcessDefinition.getId(), processInstance.getProcessDefinitionId());

    // and a task
    assertEquals(1, taskService.createTaskQuery().count());
  }

  /**
   * CAM-4527
   */
  @Deployment
  @Test
  @Ignore
  public void FAILING_testNoContinuationWhenSignalInterruptsThrowingActivity() {

    // given a process instance
    runtimeService.startProcessInstanceByKey("signalEventSubProcess");

    // when throwing a signal in the sub process that interrupts the subprocess
    Task subProcessTask = taskService.createTaskQuery().singleResult();
    taskService.complete(subProcessTask.getId());

    // then execution should not have been continued after the subprocess
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("afterSubProcessTask").count());
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTest.signalStartEvent.bpmn20.xml")
  @Test
  public void testSetSerializedVariableValues() throws IOException, ClassNotFoundException {

    // when
    FailingJavaSerializable javaSerializable = new FailingJavaSerializable("foo");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(javaSerializable);
    String serializedObject = StringUtil.fromBytes(Base64.encodeBase64(baos.toByteArray()), engineRule.getProcessEngine());

    // then it is not possible to deserialize the object
    try {
      new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    } catch (RuntimeException e) {
      testRule.assertTextPresent("Exception while deserializing object.", e.getMessage());
    }

    // but it can be set as a variable when delivering a message:
    runtimeService
        .signalEventReceived(
            "alert",
            Variables.createVariables().putValueTyped("var",
                Variables
                    .serializedObjectValue(serializedObject)
                    .objectTypeName(FailingJavaSerializable.class.getName())
                    .serializationDataFormat(SerializationDataFormats.JAVA)
                    .create()));

    // then
    ProcessInstance startedInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(startedInstance);

    ObjectValue variableTyped = runtimeService.getVariableTyped(startedInstance.getId(), "var", false);
    assertNotNull(variableTyped);
    assertFalse(variableTyped.isDeserialized());
    assertEquals(serializedObject, variableTyped.getValueSerialized());
    assertEquals(FailingJavaSerializable.class.getName(), variableTyped.getObjectTypeName());
    assertEquals(SerializationDataFormats.JAVA.getName(), variableTyped.getSerializationDataFormat());
  }

  /**
   * CAM-6807
   */
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalBoundary.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignalAsync.bpmn20.xml"})
  @Test
  @Ignore
  public void FAILING_testAsyncSignalBoundary() {
    runtimeService.startProcessInstanceByKey("catchSignal");

    // given a process instance that throws a signal asynchronously
    runtimeService.startProcessInstanceByKey("throwSignalAsync");

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);  // Throws Exception!

    // when the job is executed
    managementService.executeJob(job.getId());

    // then there is a process instance
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
//    assertEquals(catchingProcessDefinition.getId(), processInstance.getProcessDefinitionId());

    // and a task
    assertEquals(1, taskService.createTaskQuery().count());
  }

}
