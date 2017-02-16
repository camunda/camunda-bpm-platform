package org.camunda.bpm.engine.test.api.runtime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Svetlana Dorokhova
 */
public class MessageCorrelationByLocalVariablesTest {

  public static final String TEST_MESSAGE_NAME = "TEST_MSG";
  @Rule public ProcessEngineRule engineRule = new ProcessEngineRule();
  @Rule public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testReceiveTaskMessageCorrelation() {
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
    engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //when correlated by local variables
    String messageName = TEST_MESSAGE_NAME;
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    int correlationKey = 1;
    correlationKeys.put("localVar", correlationKey);
    correlationKeys.put("constVar", "someValue");

    engineRule.getRuntimeService().createMessageCorrelation(messageName)
        .localVariablesEqual(correlationKeys).setVariables(
            Variables.createVariables()
                .putValue("newVar", "newValue"))
        .correlateWithResult();

    //then exactly one message is correlated = one receive task is passed by, two other continue waiting

    //correlated esecution
    assertEquals(1, engineRule.getRuntimeService().createExecutionQuery().variableValueEquals("newVar", "newValue").list().size());

    //uncorrelated executions
    List<Execution> uncorrelatedExecutions = engineRule.getRuntimeService().createExecutionQuery().activityId("MessageReceiver_1").list();
    assertEquals(2, uncorrelatedExecutions.size());
    for (Execution execution : uncorrelatedExecutions) {
      //all of them has another variable values
      assertEquals(0,
          engineRule.getRuntimeService().createVariableInstanceQuery()
              .executionIdIn(execution.getId())
              .variableValueEquals("localVar", correlationKey)
              .list().size());
    }

  }

  @Test
  public void testIntermediateCatchEventMessageCorrelation() {
    //given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
        .subProcess("SubProcess_1").embeddedSubProcess()
        .startEvent()
        .intermediateCatchEvent("MessageReceiver_1").message(TEST_MESSAGE_NAME)
        .userTask("UserTask_1")
        .endEvent()
        .subProcessDone()
        .multiInstance().camundaCollection("${vars}").camundaElementVariable("loopVar").multiInstanceDone()
        .endEvent().done();

    CamundaInputParameter inputParameter1 = model.newInstance(CamundaInputParameter.class);
    inputParameter1.setCamundaName("localVar");
    inputParameter1.setTextContent("${loopVar}");

    CamundaInputOutput inputOutput = model.newInstance(CamundaInputOutput.class);
    inputOutput.addChildElement(inputParameter1);
    model = ((IntermediateCatchEvent) model.getModelElementById("MessageReceiver_1")).builder()
        .addExtensionElement(inputOutput).done();

    testHelper.deploy(model);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("vars", Arrays.asList(1, 2, 3));
    engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //when correlated by local variables
    String messageName = TEST_MESSAGE_NAME;
    int correlationKey = 1;

    engineRule.getRuntimeService()
        .createMessageCorrelation(messageName)
        .localVariableEquals("localVar", correlationKey)
        .setVariables(
            Variables.createVariables()
                .putValue("newVar", "newValue"))
        .correlateWithResult();

    //then exactly one message is correlated = one receive task is passed by, two others continue waiting

    //correlated esecution
    assertEquals(1, engineRule.getRuntimeService().createExecutionQuery().variableValueEquals("newVar", "newValue").list().size());

    //uncorrelated executions
    List<Execution> uncorrelatedExecutions = engineRule.getRuntimeService().createExecutionQuery().activityId("MessageReceiver_1").list();
    assertEquals(2, uncorrelatedExecutions.size());
    for (Execution execution : uncorrelatedExecutions) {
      //all of them has another variable values
      assertEquals(0,
          engineRule.getRuntimeService().createVariableInstanceQuery()
              .executionIdIn(execution.getId())
              .variableValueEquals("localVar", correlationKey)
              .list().size());
    }

  }

  @Test
  public void testMessageBoundaryEventMessageCorrelation() {
    //given
    BpmnModelInstance model = Bpmn.createExecutableProcess("Process_1")
        .startEvent()
        .subProcess("SubProcess_1").embeddedSubProcess()
        .startEvent()
        .userTask("UserTask_1")
        .boundaryEvent("MessageReceiver_1").message(TEST_MESSAGE_NAME)
        .userTask("UserTask_2")
        .endEvent()
        .subProcessDone()
        .multiInstance().camundaCollection("${vars}").camundaElementVariable("loopVar").multiInstanceDone()
        .endEvent().done();

    model = modify(model).activityBuilder("UserTask_1")
        .camundaInputParameter("localVar", "${loopVar}")
        .camundaInputParameter("constVar", "someValue")   //to test array of parameters
        .done();

    testHelper.deploy(model);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("vars", Arrays.asList(1, 2, 3));
    engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

    //when correlated by local variables
    String messageName = TEST_MESSAGE_NAME;
    Map<String, Object> correlationKeys = new HashMap<String, Object>();
    int correlationKey = 1;
    correlationKeys.put("localVar", correlationKey);
    correlationKeys.put("constVar", "someValue");
    Map<String, Object> messagePayload = new HashMap<String, Object>();
    messagePayload.put("newVar", "newValue");

    engineRule.getRuntimeService().createMessageCorrelation(messageName)
        .localVariablesEqual(correlationKeys).setVariables(messagePayload).correlateWithResult();

    //then exactly one message is correlated = one receive task is passed by, two others continue waiting

    //correlated esecution
    assertEquals(1, engineRule.getRuntimeService().createExecutionQuery().variableValueEquals("newVar", "newValue").list().size());

    //uncorrelated executions
    List<Execution> uncorrelatedExecutions = engineRule.getRuntimeService().createExecutionQuery().activityId("UserTask_1").list();
    assertEquals(2, uncorrelatedExecutions.size());
    for (Execution execution : uncorrelatedExecutions) {
      //all of them has another variable values
      assertEquals(0,
          engineRule.getRuntimeService().createVariableInstanceQuery()
              .executionIdIn(execution.getId())
              .variableValueEquals("localVar", correlationKey)
              .list().size());
    }

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
    engineRule.getRuntimeService().startProcessInstanceByKey("Process_1", variables);

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

    engineRule.getRuntimeService().createMessageCorrelation(messageName)
        .processInstanceVariablesEqual(processInstanceKeys)
        .localVariablesEqual(correlationKeys).setVariables(messagePayload).correlateWithResult();

    //then exactly one message is correlated = one receive task is passed by, two + three others continue waiting

    //correlated esecution
    assertEquals(1, engineRule.getRuntimeService().createExecutionQuery().variableValueEquals("newVar", "newValue").list().size());

    //uncorrelated executions
    List<Execution> uncorrelatedExecutions = engineRule.getRuntimeService().createExecutionQuery().activityId("MessageReceiver_1").list();
    assertEquals(5, uncorrelatedExecutions.size());
    for (Execution execution : uncorrelatedExecutions) {
      //one of the variables has wrong value (or both)
      assertTrue(
          engineRule.getRuntimeService().createVariableInstanceQuery()
              .executionIdIn(execution.getId())
              .variableValueEquals("localVar", correlationKey)
              .list().size() == 0
            || engineRule.getRuntimeService().createVariableInstanceQuery()
                .executionIdIn(execution.getId())
                .variableValueEquals("processInstanceVar", processInstanceVarValue)
                .list().size() == 0
      );
    }

  }

}
