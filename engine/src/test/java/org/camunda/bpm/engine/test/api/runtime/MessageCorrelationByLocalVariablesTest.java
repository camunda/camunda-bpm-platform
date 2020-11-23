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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Svetlana Dorokhova
 */
public class MessageCorrelationByLocalVariablesTest {

  public static final String TEST_MESSAGE_NAME = "TEST_MSG";
  @Rule public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  @Rule public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Test
  public void testReceiveTaskMessageCorrelation() {
    //given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
          .subProcess("SubProcess_1").embeddedSubProcess()
          .startEvent()
            .receiveTask("MessageReceiver_1").message(TEST_MESSAGE_NAME)
              .camundaInputParameter("localVar", "${loopVar}")
              .camundaInputParameter("constVar", "someValue")   //to test array of parameters
            .userTask("UserTask_1")
          .endEvent()
          .subProcessDone()
          .multiInstance().camundaCollection("${vars}").camundaElementVariable("loopVar").multiInstanceDone()
        .endEvent().done();

    testHelper.deploy(model);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("vars", Arrays.asList(1, 2, 3));
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //when correlated by local variables
    String messageName = TEST_MESSAGE_NAME;
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    int correlationKey = 1;
    correlationKeys.put("localVar", correlationKey);
    correlationKeys.put("constVar", "someValue");

    MessageCorrelationResult messageCorrelationResult = engineRule.getRuntimeService().createMessageCorrelation(messageName)
        .localVariablesEqual(correlationKeys).setVariables(Variables.createVariables().putValue("newVar", "newValue")).correlateWithResult();

    //then one message is correlated, two other continue waiting
    checkExecutionMessageCorrelationResult(messageCorrelationResult, processInstance, "MessageReceiver_1");

    //uncorrelated executions
    List<Execution> uncorrelatedExecutions = engineRule.getRuntimeService().createExecutionQuery().activityId("MessageReceiver_1").list();
    assertEquals(2, uncorrelatedExecutions.size());

  }

  @Test
  public void testIntermediateCatchEventMessageCorrelation() {
    //given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
          .subProcess("SubProcess_1").embeddedSubProcess()
          .startEvent()
            .intermediateCatchEvent("MessageReceiver_1").message(TEST_MESSAGE_NAME)
              .camundaInputParameter("localVar", "${loopVar}")
            .userTask("UserTask_1")
          .endEvent()
          .subProcessDone()
          .multiInstance().camundaCollection("${vars}").camundaElementVariable("loopVar").multiInstanceDone()
        .endEvent().done();

    testHelper.deploy(model);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("vars", Arrays.asList(1, 2, 3));
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //when correlated by local variables
    String messageName = TEST_MESSAGE_NAME;
    int correlationKey = 1;

    MessageCorrelationResult messageCorrelationResult = engineRule.getRuntimeService().createMessageCorrelation(messageName)
        .localVariableEquals("localVar", correlationKey).setVariables(Variables.createVariables().putValue("newVar", "newValue")).correlateWithResult();

    //then one message is correlated, two others continue waiting
    checkExecutionMessageCorrelationResult(messageCorrelationResult, processInstance, "MessageReceiver_1");

    //uncorrelated executions
    List<Execution> uncorrelatedExecutions = engineRule.getRuntimeService().createExecutionQuery().activityId("MessageReceiver_1").list();
    assertEquals(2, uncorrelatedExecutions.size());

  }

  @Test
  public void testMessageBoundaryEventMessageCorrelation() {
    //given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
          .subProcess("SubProcess_1").embeddedSubProcess()
          .startEvent()
            .userTask("UserTask_1")
              .camundaInputParameter("localVar", "${loopVar}")
              .camundaInputParameter("constVar", "someValue")   //to test array of parameters
              .boundaryEvent("MessageReceiver_1").message(TEST_MESSAGE_NAME)
            .userTask("UserTask_2")
          .endEvent()
          .subProcessDone()
          .multiInstance().camundaCollection("${vars}").camundaElementVariable("loopVar").multiInstanceDone()
        .endEvent().done();

    testHelper.deploy(model);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("vars", Arrays.asList(1, 2, 3));
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //when correlated by local variables
    String messageName = TEST_MESSAGE_NAME;
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    int correlationKey = 1;
    correlationKeys.put("localVar", correlationKey);
    correlationKeys.put("constVar", "someValue");
    Map<String, Object> messagePayload = new HashMap<String, Object>();
    messagePayload.put("newVar", "newValue");

    MessageCorrelationResult messageCorrelationResult = engineRule.getRuntimeService().createMessageCorrelation(messageName)
        .localVariablesEqual(correlationKeys).setVariables(messagePayload).correlateWithResult();

    //then one message is correlated, two others continue waiting
    checkExecutionMessageCorrelationResult(messageCorrelationResult, processInstance, "UserTask_1");

    //uncorrelated executions
    List<Execution> uncorrelatedExecutions = engineRule.getRuntimeService().createExecutionQuery().activityId("UserTask_1").list();
    assertEquals(2, uncorrelatedExecutions.size());

  }

  @Test
  public void testBothInstanceAndLocalVariableMessageCorrelation() {
    //given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
          .subProcess("SubProcess_1").embeddedSubProcess()
          .startEvent()
            .receiveTask("MessageReceiver_1").message(TEST_MESSAGE_NAME)
            .userTask("UserTask_1")
          .endEvent()
          .subProcessDone()
          .multiInstance().camundaCollection("${vars}").camundaElementVariable("loopVar").multiInstanceDone()
        .endEvent().done();

    model = modify(model).activityBuilder("MessageReceiver_1")
        .camundaInputParameter("localVar", "${loopVar}")
        .camundaInputParameter("constVar", "someValue")   //to test array of parameters
        .done();

    testHelper.deploy(model);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("vars", Arrays.asList(1, 2, 3));
    variables.put("processInstanceVar", "processInstanceVarValue");
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //second process instance with another process instance variable value
    variables = new HashMap<String, Object>();
    variables.put("vars", Arrays.asList(1, 2, 3));
    variables.put("processInstanceVar", "anotherProcessInstanceVarValue");
    engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //when correlated by local variables
    String messageName = TEST_MESSAGE_NAME;
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    int correlationKey = 1;
    correlationKeys.put("localVar", correlationKey);
    correlationKeys.put("constVar", "someValue");
    Map<String, Object> processInstanceKeys = new HashMap<String, Object>();
    String processInstanceVarValue = "processInstanceVarValue";
    processInstanceKeys.put("processInstanceVar", processInstanceVarValue);
    Map<String, Object> messagePayload = new HashMap<String, Object>();
    messagePayload.put("newVar", "newValue");

    MessageCorrelationResult messageCorrelationResult = engineRule.getRuntimeService().createMessageCorrelation(messageName)
        .processInstanceVariablesEqual(processInstanceKeys).localVariablesEqual(correlationKeys).setVariables(messagePayload).correlateWithResult();

    //then exactly one message is correlated = one receive task is passed by, two + three others continue waiting
    checkExecutionMessageCorrelationResult(messageCorrelationResult, processInstance, "MessageReceiver_1");

    //uncorrelated executions
    List<Execution> uncorrelatedExecutions = engineRule.getRuntimeService().createExecutionQuery().activityId("MessageReceiver_1").list();
    assertEquals(5, uncorrelatedExecutions.size());

  }

  @Test
  public void testReceiveTaskMessageCorrelationFail() {
    //given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
          .subProcess("SubProcess_1").embeddedSubProcess()
          .startEvent()
            .receiveTask("MessageReceiver_1").message(TEST_MESSAGE_NAME)
              .camundaInputParameter("localVar", "${loopVar}")
              .camundaInputParameter("constVar", "someValue")   //to test array of parameters
            .userTask("UserTask_1")
          .endEvent()
          .subProcessDone()
          .multiInstance().camundaCollection("${vars}").camundaElementVariable("loopVar").multiInstanceDone()
        .endEvent().done();

    testHelper.deploy(model);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("vars", Arrays.asList(1, 2, 1));
    engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //when correlated by local variables
    String messageName = TEST_MESSAGE_NAME;
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    int correlationKey = 1;
    correlationKeys.put("localVar", correlationKey);
    correlationKeys.put("constVar", "someValue");

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService().createMessageCorrelation(messageName)
        .localVariablesEqual(correlationKeys).setVariables(Variables.createVariables().putValue("newVar", "newValue")).correlateWithResult())
      .isInstanceOf(MismatchingMessageCorrelationException.class)
      .hasMessageContaining(String.format("Cannot correlate a message with name '%s' to a single execution", TEST_MESSAGE_NAME));

  }

  @Test
  public void testReceiveTaskMessageCorrelationAll() {
    //given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
          .subProcess("SubProcess_1").embeddedSubProcess()
          .startEvent()
            .receiveTask("MessageReceiver_1").message(TEST_MESSAGE_NAME)
              .camundaInputParameter("localVar", "${loopVar}")
              .camundaInputParameter("constVar", "someValue")   //to test array of parameters
            .userTask("UserTask_1")
          .endEvent()
          .subProcessDone()
          .multiInstance().camundaCollection("${vars}").camundaElementVariable("loopVar").multiInstanceDone()
        .endEvent().done();

    testHelper.deploy(model);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("vars", Arrays.asList(1, 2, 1));
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //when correlated ALL by local variables
    String messageName = TEST_MESSAGE_NAME;
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    int correlationKey = 1;
    correlationKeys.put("localVar", correlationKey);
    correlationKeys.put("constVar", "someValue");

    List<MessageCorrelationResult> messageCorrelationResults = engineRule.getRuntimeService().createMessageCorrelation(messageName)
        .localVariablesEqual(correlationKeys).setVariables(Variables.createVariables().putValue("newVar", "newValue")).correlateAllWithResult();

    //then two messages correlated, one message task is still waiting
    for (MessageCorrelationResult result: messageCorrelationResults) {
      checkExecutionMessageCorrelationResult(result, processInstance, "MessageReceiver_1");
    }

    //uncorrelated executions
    List<Execution> uncorrelatedExecutions = engineRule.getRuntimeService().createExecutionQuery().activityId("MessageReceiver_1").list();
    assertEquals(1, uncorrelatedExecutions.size());

  }

  protected void checkExecutionMessageCorrelationResult(MessageCorrelationResult result, ProcessInstance processInstance, String activityId) {
    assertNotNull(result);
    assertEquals(MessageCorrelationResultType.Execution, result.getResultType());
    assertEquals(processInstance.getId(), result.getExecution().getProcessInstanceId());
    ExecutionEntity entity = (ExecutionEntity) result.getExecution();
    assertEquals(activityId, entity.getActivityId());
  }

}
