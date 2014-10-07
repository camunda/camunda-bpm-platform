/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.model.bpmn.builder;

import java.io.IOException;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.GatewayDirection;
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.CALL_ACTIVITY_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SERVICE_TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.START_EVENT_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SUB_PROCESS_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_CLASS_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_DELEGATE_EXPRESSION_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_DUE_DATE_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_EXPRESSION_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_LIST_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_PRIORITY_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_STRING_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_LIST_API;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;

/**
 * @author Sebastian Menski
 */
public class ProcessBuilderTest {

  private BpmnModelInstance modelInstance;
  private static ModelElementType taskType;
  private static ModelElementType gatewayType;
  private static ModelElementType eventType;
  private static ModelElementType processType;

  @BeforeClass
  public static void getElementTypes() {
    Model model = Bpmn.createEmptyModel().getModel();
    taskType = model.getType(Task.class);
    gatewayType = model.getType(Gateway.class);
    eventType = model.getType(Event.class);
    processType = model.getType(Process.class);
  }

  @Test
  public void testCreateEmptyProcess() {
    modelInstance = Bpmn.createProcess()
      .done();
    
    Definitions definitions = modelInstance.getDefinitions();
    assertThat(definitions).isNotNull();
    assertThat(definitions.getTargetNamespace()).isEqualTo(BPMN20_NS);

    assertThat(modelInstance.getModelElementsByType(processType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithStartEvent() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithEndEvent() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
  }

  @Test
  public void testCreateProcessWithServiceTask() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .serviceTask()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithSendTask() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .sendTask()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithUserTask() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .userTask()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithBusinessRuleTask() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .businessRuleTask()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithScriptTask() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .scriptTask()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithReceiveTask() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .receiveTask()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithManualTask() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .manualTask()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithParallelGateway() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .parallelGateway()
        .scriptTask()
        .endEvent()
      .moveToLastGateway()
        .userTask()
        .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(3);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(gatewayType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithExclusiveGateway() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .userTask()
      .exclusiveGateway()
        .condition("approved", "${approved}")
        .serviceTask()
        .endEvent()
      .moveToLastGateway()
        .condition("not approved", "${!approved}")
        .scriptTask()
        .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(3);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(3);
    assertThat(modelInstance.getModelElementsByType(gatewayType))
      .hasSize(1);
  }

  @Test
  public void testCreateProcessWithForkAndJoin() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .userTask()
      .parallelGateway()
        .serviceTask()
        .parallelGateway()
        .id("join")
      .moveToLastGateway()
        .scriptTask()
      .connectTo("join")
      .userTask()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(4);
    assertThat(modelInstance.getModelElementsByType(gatewayType))
      .hasSize(2);
  }

  @Test
  public void testCreateProcessWithMultipleParallelTask() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .parallelGateway("fork")
        .userTask()
        .parallelGateway("join")
      .moveToNode("fork")
        .serviceTask()
        .connectTo("join")
      .moveToNode("fork")
        .userTask()
        .connectTo("join")
      .moveToNode("fork")
        .scriptTask()
        .connectTo("join")
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(eventType))
      .hasSize(2);
    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(4);
    assertThat(modelInstance.getModelElementsByType(gatewayType))
      .hasSize(2);
  }

  @Test
  public void testExtend() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .userTask()
        .id("task1")
      .serviceTask()
      .endEvent()
      .done();

    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(2);

    UserTask userTask = modelInstance.getModelElementById("task1");
    SequenceFlow outgoingSequenceFlow = userTask.getOutgoing().iterator().next();
    FlowNode serviceTask = outgoingSequenceFlow.getTarget();
    userTask.getOutgoing().remove(outgoingSequenceFlow);
    userTask.builder()
      .scriptTask()
      .userTask()
      .connectTo(serviceTask.getId());

    assertThat(modelInstance.getModelElementsByType(taskType))
      .hasSize(4);
  }

  @Test
  public void testCreateInvoiceProcess() {
    modelInstance = Bpmn.createProcess()
      .executable()
      .startEvent()
        .name("Invoice received")
        .camundaFormKey("embedded:app:forms/start-form.html")
      .userTask()
        .name("Assign Approver")
        .camundaFormKey("embedded:app:forms/assign-approver.html")
        .camundaAssignee("demo")
      .userTask("approveInvoice")
        .name("Approve Invoice")
        .camundaFormKey("embedded:app:forms/approve-invoice.html")
        .camundaAssignee("${approver}")
      .exclusiveGateway()
        .name("Invoice approved?")
        .gatewayDirection(GatewayDirection.Diverging)
      .condition("yes", "${approved}")
      .userTask()
        .name("Prepare Bank Transfer")
        .camundaFormKey("embedded:app:forms/prepare-bank-transfer.html")
        .camundaCandidateGroups("accounting")
      .serviceTask()
        .name("Archive Invoice")
        .camundaClass("org.camunda.bpm.example.invoice.service.ArchiveInvoiceService" )
      .endEvent()
        .name("Invoice processed")
      .moveToLastGateway()
      .condition("no", "${!approved}")
      .userTask()
        .name("Review Invoice")
        .camundaFormKey("embedded:app:forms/review-invoice.html" )
        .camundaAssignee("demo")
       .exclusiveGateway()
        .name("Review successful?")
        .gatewayDirection(GatewayDirection.Diverging)
      .condition("no", "${!clarified}")
      .endEvent()
        .name("Invoice not processed")
      .moveToLastGateway()
      .condition("yes", "${clarified}")
      .connectTo("approveInvoice")
      .done();
  }

  @Test
  public void testTaskCamundaExtensions() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .serviceTask(TASK_ID)
        .camundaAsyncBefore()
        .notCamundaExclusive()
      .endEvent()
      .done();

    ServiceTask serviceTask = modelInstance.getModelElementById(TASK_ID);
    assertThat(serviceTask.isCamundaAsyncBefore()).isTrue();
    assertThat(serviceTask.isCamundaExclusive()).isFalse();
  }

  @Test
  public void testServiceTaskCamundaExtensions() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .serviceTask(TASK_ID)
        .camundaClass(TEST_CLASS_API)
        .camundaDelegateExpression(TEST_DELEGATE_EXPRESSION_API)
        .camundaExpression(TEST_EXPRESSION_API)
        .camundaResultVariable(TEST_STRING_API)
        .camundaType(TEST_STRING_API)
      .done();

    ServiceTask serviceTask = modelInstance.getModelElementById(TASK_ID);
    assertThat(serviceTask.getCamundaClass()).isEqualTo(TEST_CLASS_API);
    assertThat(serviceTask.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
    assertThat(serviceTask.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(serviceTask.getCamundaResultVariable()).isEqualTo(TEST_STRING_API);
    assertThat(serviceTask.getCamundaType()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testSendTaskCamundaExtensions() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .sendTask(TASK_ID)
        .camundaClass(TEST_CLASS_API)
        .camundaDelegateExpression(TEST_DELEGATE_EXPRESSION_API)
        .camundaExpression(TEST_EXPRESSION_API)
        .camundaResultVariable(TEST_STRING_API)
        .camundaType(TEST_STRING_API)
      .endEvent()
      .done();

    SendTask sendTask = modelInstance.getModelElementById(TASK_ID);
    assertThat(sendTask.getCamundaClass()).isEqualTo(TEST_CLASS_API);
    assertThat(sendTask.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
    assertThat(sendTask.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(sendTask.getCamundaResultVariable()).isEqualTo(TEST_STRING_API);
    assertThat(sendTask.getCamundaType()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testUserTaskCamundaExtensions() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .userTask(TASK_ID)
        .camundaAssignee(TEST_STRING_API)
        .camundaCandidateGroups(TEST_GROUPS_API)
        .camundaCandidateUsers(TEST_USERS_LIST_API)
        .camundaDueDate(TEST_DUE_DATE_API)
        .camundaFormHandlerClass(TEST_CLASS_API)
        .camundaFormKey(TEST_STRING_API)
        .camundaPriority(TEST_PRIORITY_API)
      .endEvent()
      .done();

    UserTask userTask = modelInstance.getModelElementById(TASK_ID);
    assertThat(userTask.getCamundaAssignee()).isEqualTo(TEST_STRING_API);
    assertThat(userTask.getCamundaCandidateGroups()).isEqualTo(TEST_GROUPS_API);
    assertThat(userTask.getCamundaCandidateGroupsList()).containsAll(TEST_GROUPS_LIST_API);
    assertThat(userTask.getCamundaCandidateUsers()).isEqualTo(TEST_USERS_API);
    assertThat(userTask.getCamundaCandidateUsersList()).containsAll(TEST_USERS_LIST_API);
    assertThat(userTask.getCamundaDueDate()).isEqualTo(TEST_DUE_DATE_API);
    assertThat(userTask.getCamundaFormHandlerClass()).isEqualTo(TEST_CLASS_API);
    assertThat(userTask.getCamundaFormKey()).isEqualTo(TEST_STRING_API);
    assertThat(userTask.getCamundaPriority()).isEqualTo(TEST_PRIORITY_API);
  }

  @Test
  public void testBusinessRuleTaskCamundaExtensions() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .businessRuleTask(TASK_ID)
        .camundaClass(TEST_CLASS_API)
        .camundaDelegateExpression(TEST_DELEGATE_EXPRESSION_API)
        .camundaExpression(TEST_EXPRESSION_API)
        .camundaResultVariable(TEST_STRING_API)
        .camundaType(TEST_STRING_API)
      .endEvent()
      .done();

    BusinessRuleTask businessRuleTask = modelInstance.getModelElementById(TASK_ID);
    assertThat(businessRuleTask.getCamundaClass()).isEqualTo(TEST_CLASS_API);
    assertThat(businessRuleTask.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
    assertThat(businessRuleTask.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(businessRuleTask.getCamundaResultVariable()).isEqualTo(TEST_STRING_API);
    assertThat(businessRuleTask.getCamundaType()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testScriptTaskCamundaExtensions() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .scriptTask(TASK_ID)
        .camundaResultVariable(TEST_STRING_API)
        .camundaResource(TEST_STRING_API)
      .endEvent()
      .done();

    ScriptTask scriptTask = modelInstance.getModelElementById(TASK_ID);
    assertThat(scriptTask.getCamundaResultVariable()).isEqualTo(TEST_STRING_API);
    assertThat(scriptTask.getCamundaResource()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testStartEventCamundaExtensions() {
    modelInstance = Bpmn.createProcess()
      .startEvent(START_EVENT_ID)
        .camundaAsyncBefore()
        .notCamundaExclusive()
        .camundaFormHandlerClass(TEST_CLASS_API)
        .camundaFormKey(TEST_STRING_API)
        .camundaInitiator(TEST_STRING_API)
      .done();

    StartEvent startEvent = modelInstance.getModelElementById(START_EVENT_ID);
    assertThat(startEvent.isCamundaAsyncBefore()).isTrue();
    assertThat(startEvent.isCamundaExclusive()).isFalse();
    assertThat(startEvent.getCamundaFormHandlerClass()).isEqualTo(TEST_CLASS_API);
    assertThat(startEvent.getCamundaFormKey()).isEqualTo(TEST_STRING_API);
    assertThat(startEvent.getCamundaInitiator()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCallActivityCamundaExtension() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .callActivity(CALL_ACTIVITY_ID)
        .calledElement(TEST_STRING_API)
        .camundaAsyncBefore()
        .camundaCalledElementBinding("version")
        .camundaCalledElementVersion("1.0")
        .notCamundaExclusive()
      .endEvent()
      .done();

    CallActivity callActivity = modelInstance.getModelElementById(CALL_ACTIVITY_ID);
    assertThat(callActivity.getCalledElement()).isEqualTo(TEST_STRING_API);
    assertThat(callActivity.isCamundaAsyncBefore()).isTrue();
    assertThat(callActivity.getCamundaCalledElementBinding()).isEqualTo("version");
    assertThat(callActivity.getCamundaCalledElementVersion()).isEqualTo("1.0");
    assertThat(callActivity.isCamundaExclusive()).isFalse();
  }

  @Test
  public void testSubProcessBuilder() {
    BpmnModelInstance modelInstance = Bpmn.createProcess()
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
        .camundaAsyncBefore()
        .embeddedSubProcess()
          .startEvent()
          .userTask()
          .endEvent()
        .subProcessDone()
      .serviceTask(SERVICE_TASK_ID)
      .endEvent()
      .done();

    SubProcess subProcess = modelInstance.getModelElementById(SUB_PROCESS_ID);
    ServiceTask serviceTask = modelInstance.getModelElementById(SERVICE_TASK_ID);
    assertThat(subProcess.isCamundaAsyncBefore()).isTrue();
    assertThat(subProcess.isCamundaExclusive()).isTrue();
    assertThat(subProcess.getChildElementsByType(Event.class)).hasSize(2);
    assertThat(subProcess.getChildElementsByType(Task.class)).hasSize(1);
    assertThat(subProcess.getFlowElements()).hasSize(5);
    assertThat(subProcess.getSucceedingNodes().singleResult()).isEqualTo(serviceTask);
  }

  @Test
  public void testSubProcessBuilderDetached() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .subProcess(SUB_PROCESS_ID)
      .serviceTask(SERVICE_TASK_ID)
      .endEvent()
      .done();

    SubProcess subProcess = modelInstance.getModelElementById(SUB_PROCESS_ID);

    subProcess.builder()
      .camundaAsyncBefore()
      .embeddedSubProcess()
        .startEvent()
        .userTask()
        .endEvent();

    ServiceTask serviceTask = modelInstance.getModelElementById(SERVICE_TASK_ID);
    assertThat(subProcess.isCamundaAsyncBefore()).isTrue();
    assertThat(subProcess.isCamundaExclusive()).isTrue();
    assertThat(subProcess.getChildElementsByType(Event.class)).hasSize(2);
    assertThat(subProcess.getChildElementsByType(Task.class)).hasSize(1);
    assertThat(subProcess.getFlowElements()).hasSize(5);
    assertThat(subProcess.getSucceedingNodes().singleResult()).isEqualTo(serviceTask);
  }

  @Test
  public void testSubProcessBuilderNested() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .subProcess(SUB_PROCESS_ID + 1)
        .camundaAsyncBefore()
        .embeddedSubProcess()
          .startEvent()
          .userTask()
          .subProcess(SUB_PROCESS_ID + 2)
            .camundaAsyncBefore()
            .notCamundaExclusive()
            .embeddedSubProcess()
              .startEvent()
              .userTask()
              .endEvent()
            .subProcessDone()
          .serviceTask(SERVICE_TASK_ID + 1)
          .endEvent()
        .subProcessDone()
      .serviceTask(SERVICE_TASK_ID + 2)
      .endEvent()
      .done();

    SubProcess subProcess = modelInstance.getModelElementById(SUB_PROCESS_ID + 1);
    ServiceTask serviceTask = modelInstance.getModelElementById(SERVICE_TASK_ID + 2);
    assertThat(subProcess.isCamundaAsyncBefore()).isTrue();
    assertThat(subProcess.isCamundaExclusive()).isTrue();
    assertThat(subProcess.getChildElementsByType(Event.class)).hasSize(2);
    assertThat(subProcess.getChildElementsByType(Task.class)).hasSize(2);
    assertThat(subProcess.getChildElementsByType(SubProcess.class)).hasSize(1);
    assertThat(subProcess.getFlowElements()).hasSize(9);
    assertThat(subProcess.getSucceedingNodes().singleResult()).isEqualTo(serviceTask);

    SubProcess nestedSubProcess = modelInstance.getModelElementById(SUB_PROCESS_ID + 2);
    ServiceTask nestedServiceTask = modelInstance.getModelElementById(SERVICE_TASK_ID + 1);
    assertThat(nestedSubProcess.isCamundaAsyncBefore()).isTrue();
    assertThat(nestedSubProcess.isCamundaExclusive()).isFalse();
    assertThat(nestedSubProcess.getChildElementsByType(Event.class)).hasSize(2);
    assertThat(nestedSubProcess.getChildElementsByType(Task.class)).hasSize(1);
    assertThat(nestedSubProcess.getFlowElements()).hasSize(5);
    assertThat(nestedSubProcess.getSucceedingNodes().singleResult()).isEqualTo(nestedServiceTask);
  }

  @Test
  public void testSubProcessBuilderWrongScope() {
    try {
      modelInstance = Bpmn.createProcess()
        .startEvent()
        .subProcessDone()
        .endEvent()
        .done();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(BpmnModelException.class);
    }
  }

  @Test
  public void testScriptText() {
    modelInstance = Bpmn.createProcess()
      .startEvent()
      .scriptTask("script")
        .scriptFormat("groovy")
        .scriptText("println \"hello, world\";")
      .endEvent()
      .done();

    ScriptTask scriptTask = modelInstance.getModelElementById("script");
    assertThat(scriptTask.getScriptFormat()).isEqualTo("groovy");
    assertThat(scriptTask.getScript().getTextContent()).isEqualTo("println \"hello, world\";");
  }

  @Test
  public void testEventBasedGatewayAsyncAfter() {
    try {
      modelInstance = Bpmn.createProcess()
        .startEvent()
        .eventBasedGateway()
          .camundaAsyncAfter()
        .done();

      fail("Expected UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // happy path
    }

    try {
      modelInstance = Bpmn.createProcess()
        .startEvent()
        .eventBasedGateway()
          .camundaAsyncAfter(true)
        .endEvent()
        .done();
      fail("Expected UnsupportedOperationException");
    } catch(UnsupportedOperationException ex) {
      // happy ending :D
    }
  }


  @After
  public void validateModel() throws IOException {
    if (modelInstance != null) {
      Bpmn.validateModel(modelInstance);
    }
  }

}
