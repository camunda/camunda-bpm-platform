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

package org.camunda.bpm.model.bpmn;

import java.util.Collections;

import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.Expression;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ScriptTask;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConstraint;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFailedJobRetryTimeCycle;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormProperty;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOut;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaPotentialStarter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaScript;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.CALL_ACTIVITY_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.END_EVENT_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.PROCESS_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SCRIPT_TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SEND_TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SEQUENCE_FLOW_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.SERVICE_TASK_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.START_EVENT_ID;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_CLASS_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_CLASS_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_DELEGATE_EXPRESSION_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_DELEGATE_EXPRESSION_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_DUE_DATE_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_DUE_DATE_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_EXECUTION_EVENT_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_EXECUTION_EVENT_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_EXPRESSION_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_EXPRESSION_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_LIST_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_LIST_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_GROUPS_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_PRIORITY_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_PRIORITY_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_STRING_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_STRING_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_TASK_EVENT_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_TASK_EVENT_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_TYPE_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_TYPE_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_LIST_API;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_LIST_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.TEST_USERS_XML;
import static org.camunda.bpm.model.bpmn.BpmnTestConstants.USER_TASK_ID;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

/**
 * @author Sebastian Menski
 */
public class CamundaExtensionsTest {

  private BpmnModelInstance modelInstance;
  private Process process;
  private StartEvent startEvent;
  private SequenceFlow sequenceFlow;
  private UserTask userTask;
  private ServiceTask serviceTask;
  private SendTask sendTask;
  private ScriptTask scriptTask;
  private CallActivity callActivity;
  private EndEvent endEvent;
  private MessageEventDefinition messageEventDefinition;
  private ParallelGateway parallelGateway;

  @Before
  public void parseModel() {
    modelInstance = Bpmn.readModelFromStream(getClass().getResourceAsStream(getClass().getSimpleName() + ".xml"));
    process = modelInstance.getModelElementById(PROCESS_ID);
    startEvent = modelInstance.getModelElementById(START_EVENT_ID);
    sequenceFlow = modelInstance.getModelElementById(SEQUENCE_FLOW_ID);
    userTask = modelInstance.getModelElementById(USER_TASK_ID);
    serviceTask = modelInstance.getModelElementById(SERVICE_TASK_ID);
    sendTask = modelInstance.getModelElementById(SEND_TASK_ID);
    scriptTask = modelInstance.getModelElementById(SCRIPT_TASK_ID);
    callActivity = modelInstance.getModelElementById(CALL_ACTIVITY_ID);
    endEvent = modelInstance.getModelElementById(END_EVENT_ID);
    messageEventDefinition = (MessageEventDefinition) endEvent.getEventDefinitions().iterator().next();
    parallelGateway = modelInstance.getModelElementById("parallelGateway");
  }

  @Test
  public void testAssignee() {
    assertThat(userTask.getCamundaAssignee()).isEqualTo(TEST_STRING_XML);
    userTask.setCamundaAssignee(TEST_STRING_API);
    assertThat(userTask.getCamundaAssignee()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testAsync() {
    assertThat(startEvent.isCamundaAsync()).isFalse();
    assertThat(userTask.isCamundaAsync()).isTrue();
    userTask.setCamundaAsync(false);
    assertThat(userTask.isCamundaAsync()).isFalse();
    assertThat(parallelGateway.isCamundaAsync()).isTrue();
    parallelGateway.setCamundaAsync(false);
    assertThat(parallelGateway.isCamundaAsync()).isFalse();
  }

  @Test
  public void testCalledElementBinding() {
    assertThat(callActivity.getCamundaCalledElementBinding()).isEqualTo(TEST_STRING_XML);
    callActivity.setCamundaCalledElementBinding(TEST_STRING_API);
    assertThat(callActivity.getCamundaCalledElementBinding()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCalledElementVersion() {
    assertThat(callActivity.getCamundaCalledElementVersion()).isEqualTo(TEST_STRING_XML);
    callActivity.setCamundaCalledElementVersion(TEST_STRING_API);
    assertThat(callActivity.getCamundaCalledElementVersion()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testCandidateGroups() {
    assertThat(userTask.getCamundaCandidateGroups()).isEqualTo(TEST_GROUPS_XML);
    assertThat(userTask.getCamundaCandidateGroupsList()).containsAll(TEST_GROUPS_LIST_XML);
    userTask.setCamundaCandidateGroups(TEST_GROUPS_API);
    assertThat(userTask.getCamundaCandidateGroups()).isEqualTo(TEST_GROUPS_API);
    assertThat(userTask.getCamundaCandidateGroupsList()).containsAll(TEST_GROUPS_LIST_API);
    userTask.setCamundaCandidateGroupsList(TEST_GROUPS_LIST_XML);
    assertThat(userTask.getCamundaCandidateGroups()).isEqualTo(TEST_GROUPS_XML);
    assertThat(userTask.getCamundaCandidateGroupsList()).containsAll(TEST_GROUPS_LIST_XML);
  }

  @Test
  public void testCandidateStarterGroups() {
    assertThat(process.getCamundaCandidateStarterGroups()).isEqualTo(TEST_GROUPS_XML);
    assertThat(process.getCamundaCandidateStarterGroupsList()).containsAll(TEST_GROUPS_LIST_XML);
    process.setCamundaCandidateStarterGroups(TEST_GROUPS_API);
    assertThat(process.getCamundaCandidateStarterGroups()).isEqualTo(TEST_GROUPS_API);
    assertThat(process.getCamundaCandidateStarterGroupsList()).containsAll(TEST_GROUPS_LIST_API);
    process.setCamundaCandidateStarterGroupsList(TEST_GROUPS_LIST_XML);
    assertThat(process.getCamundaCandidateStarterGroups()).isEqualTo(TEST_GROUPS_XML);
    assertThat(process.getCamundaCandidateStarterGroupsList()).containsAll(TEST_GROUPS_LIST_XML);
  }

  @Test
  public void testCandidateStarterUsers() {
    assertThat(process.getCamundaCandidateStarterUsers()).isEqualTo(TEST_USERS_XML);
    assertThat(process.getCamundaCandidateStarterUsersList()).containsAll(TEST_USERS_LIST_XML);
    process.setCamundaCandidateStarterUsers(TEST_USERS_API);
    assertThat(process.getCamundaCandidateStarterUsers()).isEqualTo(TEST_USERS_API);
    assertThat(process.getCamundaCandidateStarterUsersList()).containsAll(TEST_USERS_LIST_API);
    process.setCamundaCandidateStarterUsersList(TEST_USERS_LIST_XML);
    assertThat(process.getCamundaCandidateStarterUsers()).isEqualTo(TEST_USERS_XML);
    assertThat(process.getCamundaCandidateStarterUsersList()).containsAll(TEST_USERS_LIST_XML);
  }

  @Test
  public void testCandidateUsers() {
    assertThat(userTask.getCamundaCandidateUsers()).isEqualTo(TEST_USERS_XML);
    assertThat(userTask.getCamundaCandidateUsersList()).containsAll(TEST_USERS_LIST_XML);
    userTask.setCamundaCandidateUsers(TEST_USERS_API);
    assertThat(userTask.getCamundaCandidateUsers()).isEqualTo(TEST_USERS_API);
    assertThat(userTask.getCamundaCandidateUsersList()).containsAll(TEST_USERS_LIST_API);
    userTask.setCamundaCandidateUsersList(TEST_USERS_LIST_XML);
    assertThat(userTask.getCamundaCandidateUsers()).isEqualTo(TEST_USERS_XML);
    assertThat(userTask.getCamundaCandidateUsersList()).containsAll(TEST_USERS_LIST_XML);
  }

  @Test
  public void testClass() {
    assertThat(serviceTask.getCamundaClass()).isEqualTo(TEST_CLASS_XML);
    assertThat(messageEventDefinition.getCamundaClass()).isEqualTo(TEST_CLASS_XML);
    serviceTask.setCamundaClass(TEST_CLASS_API);
    messageEventDefinition.setCamundaClass(TEST_CLASS_API);
    assertThat(serviceTask.getCamundaClass()).isEqualTo(TEST_CLASS_API);
    assertThat(messageEventDefinition.getCamundaClass()).isEqualTo(TEST_CLASS_API);
  }

  @Test
  public void testDelegateExpression() {
    assertThat(serviceTask.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_XML);
    assertThat(messageEventDefinition.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_XML);
    serviceTask.setCamundaDelegateExpression(TEST_DELEGATE_EXPRESSION_API);
    messageEventDefinition.setCamundaDelegateExpression(TEST_DELEGATE_EXPRESSION_API);
    assertThat(serviceTask.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
    assertThat(messageEventDefinition.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
  }

  @Test
  public void testDueDate() {
    assertThat(userTask.getCamundaDueDate()).isEqualTo(TEST_DUE_DATE_XML);
    userTask.setCamundaDueDate(TEST_DUE_DATE_API);
    assertThat(userTask.getCamundaDueDate()).isEqualTo(TEST_DUE_DATE_API);
  }

  @Test
  public void testExclusive() {
    assertThat(startEvent.isCamundaExclusive()).isTrue();
    assertThat(userTask.isCamundaExclusive()).isFalse();
    userTask.setCamundaExclusive(true);
    assertThat(userTask.isCamundaExclusive()).isTrue();
    assertThat(parallelGateway.isCamundaExclusive()).isTrue();
    parallelGateway.setCamundaExclusive(false);
    assertThat(parallelGateway.isCamundaExclusive()).isFalse();
  }

  @Test
  public void testExpression() {
    assertThat(serviceTask.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(messageEventDefinition.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_XML);
    serviceTask.setCamundaExpression(TEST_EXPRESSION_API);
    messageEventDefinition.setCamundaExpression(TEST_EXPRESSION_API);
    assertThat(serviceTask.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(messageEventDefinition.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_API);
  }

  @Test
  public void testFormHandlerClass() {
    assertThat(startEvent.getCamundaFormHandlerClass()).isEqualTo(TEST_CLASS_XML);
    assertThat(userTask.getCamundaFormHandlerClass()).isEqualTo(TEST_CLASS_XML);
    startEvent.setCamundaFormHandlerClass(TEST_CLASS_API);
    userTask.setCamundaFormHandlerClass(TEST_CLASS_API);
    assertThat(startEvent.getCamundaFormHandlerClass()).isEqualTo(TEST_CLASS_API);
    assertThat(userTask.getCamundaFormHandlerClass()).isEqualTo(TEST_CLASS_API);
  }

  @Test
  public void testFormKey() {
    assertThat(startEvent.getCamundaFormKey()).isEqualTo(TEST_STRING_XML);
    assertThat(userTask.getCamundaFormKey()).isEqualTo(TEST_STRING_XML);
    startEvent.setCamundaFormKey(TEST_STRING_API);
    userTask.setCamundaFormKey(TEST_STRING_API);
    assertThat(startEvent.getCamundaFormKey()).isEqualTo(TEST_STRING_API);
    assertThat(userTask.getCamundaFormKey()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testInitiator() {
    assertThat(startEvent.getCamundaInitiator()).isEqualTo(TEST_STRING_XML);
    startEvent.setCamundaInitiator(TEST_STRING_API);
    assertThat(startEvent.getCamundaInitiator()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testPriority() {
    assertThat(userTask.getCamundaPriority()).isEqualTo(TEST_PRIORITY_XML);
    userTask.setCamundaPriority(TEST_PRIORITY_API);
    assertThat(userTask.getCamundaPriority()).isEqualTo(TEST_PRIORITY_API);
  }

  @Test
  public void testResultVariable() {
    assertThat(serviceTask.getCamundaResultVariable()).isEqualTo(TEST_STRING_XML);
    assertThat(messageEventDefinition.getCamundaResultVariable()).isEqualTo(TEST_STRING_XML);
    serviceTask.setCamundaResultVariable(TEST_STRING_API);
    messageEventDefinition.setCamundaResultVariable(TEST_STRING_API);
    assertThat(serviceTask.getCamundaResultVariable()).isEqualTo(TEST_STRING_API);
    assertThat(messageEventDefinition.getCamundaResultVariable()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testType() {
    assertThat(serviceTask.getCamundaType()).isEqualTo(TEST_TYPE_XML);
    serviceTask.setCamundaType(TEST_TYPE_API);
    assertThat(serviceTask.getCamundaType()).isEqualTo(TEST_TYPE_API);
  }

  @Test
  public void testExecutionListenerExtension() {
    CamundaExecutionListener processListener = process.getExtensionElements().getElementsQuery().filterByType(CamundaExecutionListener.class).singleResult();
    CamundaExecutionListener startEventListener = startEvent.getExtensionElements().getElementsQuery().filterByType(CamundaExecutionListener.class).singleResult();
    CamundaExecutionListener serviceTaskListener = serviceTask.getExtensionElements().getElementsQuery().filterByType(CamundaExecutionListener.class).singleResult();
    assertThat(processListener.getCamundaClass()).isEqualTo(TEST_CLASS_XML);
    assertThat(processListener.getCamundaEvent()).isEqualTo(TEST_EXECUTION_EVENT_XML);
    assertThat(startEventListener.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(startEventListener.getCamundaEvent()).isEqualTo(TEST_EXECUTION_EVENT_XML);
    assertThat(serviceTaskListener.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_XML);
    assertThat(serviceTaskListener.getCamundaEvent()).isEqualTo(TEST_EXECUTION_EVENT_XML);
    processListener.setCamundaClass(TEST_CLASS_API);
    processListener.setCamundaEvent(TEST_EXECUTION_EVENT_API);
    startEventListener.setCamundaExpression(TEST_EXPRESSION_API);
    startEventListener.setCamundaEvent(TEST_EXECUTION_EVENT_API);
    serviceTaskListener.setCamundaDelegateExpression(TEST_DELEGATE_EXPRESSION_API);
    serviceTaskListener.setCamundaEvent(TEST_EXECUTION_EVENT_API);
    assertThat(processListener.getCamundaClass()).isEqualTo(TEST_CLASS_API);
    assertThat(processListener.getCamundaEvent()).isEqualTo(TEST_EXECUTION_EVENT_API);
    assertThat(startEventListener.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(startEventListener.getCamundaEvent()).isEqualTo(TEST_EXECUTION_EVENT_API);
    assertThat(serviceTaskListener.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);
    assertThat(serviceTaskListener.getCamundaEvent()).isEqualTo(TEST_EXECUTION_EVENT_API);
  }

  @Test
  public void testCamundaScriptExecutionListener() {
    CamundaExecutionListener sequenceFlowListener = sequenceFlow.getExtensionElements().getElementsQuery().filterByType(CamundaExecutionListener.class).singleResult();

    CamundaScript script = sequenceFlowListener.getCamundaScript();
    assertThat(script.getCamundaScriptFormat()).isEqualTo("groovy");
    assertThat(script.getCamundaResource()).isNull();
    assertThat(script.getTextContent()).isEqualTo("println 'Hello World'");

    CamundaScript newScript = modelInstance.newInstance(CamundaScript.class);
    newScript.setCamundaScriptFormat("groovy");
    newScript.setCamundaResource("test.groovy");
    sequenceFlowListener.setCamundaScript(newScript);

    script = sequenceFlowListener.getCamundaScript();
    assertThat(script.getCamundaScriptFormat()).isEqualTo("groovy");
    assertThat(script.getCamundaResource()).isEqualTo("test.groovy");
    assertThat(script.getTextContent()).isEmpty();
  }

  @Test
  public void testFailedJobRetryTimeCycleExtension() {
    CamundaFailedJobRetryTimeCycle timeCycle = sendTask.getExtensionElements().getElementsQuery().filterByType(CamundaFailedJobRetryTimeCycle.class).singleResult();
    assertThat(timeCycle.getTextContent()).isEqualTo(TEST_STRING_XML);
    timeCycle.setTextContent(TEST_STRING_API);
    assertThat(timeCycle.getTextContent()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testFieldExtension() {
    CamundaField field = sendTask.getExtensionElements().getElementsQuery().filterByType(CamundaField.class).singleResult();
    assertThat(field.getCamundaName()).isEqualTo(TEST_STRING_XML);
    assertThat(field.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(field.getCamundaStringValue()).isEqualTo(TEST_STRING_XML);
    assertThat(field.getCamundaExpressionChild().getTextContent()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(field.getCamundaString().getTextContent()).isEqualTo(TEST_STRING_XML);
    field.setCamundaName(TEST_STRING_API);
    field.setCamundaExpression(TEST_EXPRESSION_API);
    field.setCamundaStringValue(TEST_STRING_API);
    field.getCamundaExpressionChild().setTextContent(TEST_EXPRESSION_API);
    field.getCamundaString().setTextContent(TEST_STRING_API);
    assertThat(field.getCamundaName()).isEqualTo(TEST_STRING_API);
    assertThat(field.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(field.getCamundaStringValue()).isEqualTo(TEST_STRING_API);
    assertThat(field.getCamundaExpressionChild().getTextContent()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(field.getCamundaString().getTextContent()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testFormData() {
    CamundaFormData formData = userTask.getExtensionElements().getElementsQuery().filterByType(CamundaFormData.class).singleResult();
    CamundaFormField formField = formData.getCamundaFormFields().iterator().next();
    assertThat(formField.getCamundaId()).isEqualTo(TEST_STRING_XML);
    assertThat(formField.getCamundaLabel()).isEqualTo(TEST_STRING_XML);
    assertThat(formField.getCamundaType()).isEqualTo(TEST_STRING_XML);
    assertThat(formField.getCamundaDatePattern()).isEqualTo(TEST_STRING_XML);
    assertThat(formField.getCamundaDefaultValue()).isEqualTo(TEST_STRING_XML);
    formField.setCamundaId(TEST_STRING_API);
    formField.setCamundaLabel(TEST_STRING_API);
    formField.setCamundaType(TEST_STRING_API);
    formField.setCamundaDatePattern(TEST_STRING_API);
    formField.setCamundaDefaultValue(TEST_STRING_API);
    assertThat(formField.getCamundaId()).isEqualTo(TEST_STRING_API);
    assertThat(formField.getCamundaLabel()).isEqualTo(TEST_STRING_API);
    assertThat(formField.getCamundaType()).isEqualTo(TEST_STRING_API);
    assertThat(formField.getCamundaDatePattern()).isEqualTo(TEST_STRING_API);
    assertThat(formField.getCamundaDefaultValue()).isEqualTo(TEST_STRING_API);

    CamundaProperty property = formField.getCamundaProperties().getCamundaProperties().iterator().next();
    assertThat(property.getCamundaId()).isEqualTo(TEST_STRING_XML);
    assertThat(property.getCamundaValue()).isEqualTo(TEST_STRING_XML);
    property.setCamundaId(TEST_STRING_API);
    property.setCamundaValue(TEST_STRING_API);
    assertThat(property.getCamundaId()).isEqualTo(TEST_STRING_API);
    assertThat(property.getCamundaValue()).isEqualTo(TEST_STRING_API);

    CamundaConstraint constraint = formField.getCamundaValidation().getCamundaConstraints().iterator().next();
    assertThat(constraint.getCamundaName()).isEqualTo(TEST_STRING_XML);
    assertThat(constraint.getCamundaConfig()).isEqualTo(TEST_STRING_XML);
    constraint.setCamundaName(TEST_STRING_API);
    constraint.setCamundaConfig(TEST_STRING_API);
    assertThat(constraint.getCamundaName()).isEqualTo(TEST_STRING_API);
    assertThat(constraint.getCamundaConfig()).isEqualTo(TEST_STRING_API);

    CamundaValue value = formField.getCamundaValues().iterator().next();
    assertThat(value.getCamundaId()).isEqualTo(TEST_STRING_XML);
    assertThat(value.getCamundaName()).isEqualTo(TEST_STRING_XML);
    value.setCamundaId(TEST_STRING_API);
    value.setCamundaName(TEST_STRING_API);
    assertThat(value.getCamundaId()).isEqualTo(TEST_STRING_API);
    assertThat(value.getCamundaName()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testFormProperty() {
    CamundaFormProperty formProperty = startEvent.getExtensionElements().getElementsQuery().filterByType(CamundaFormProperty.class).singleResult();
    assertThat(formProperty.getCamundaId()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.getCamundaName()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.getCamundaType()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.isCamundaRequired()).isFalse();
    assertThat(formProperty.isCamundaReadable()).isTrue();
    assertThat(formProperty.isCamundaWriteable()).isTrue();
    assertThat(formProperty.getCamundaVariable()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(formProperty.getCamundaDatePattern()).isEqualTo(TEST_STRING_XML);
    assertThat(formProperty.getCamundaDefault()).isEqualTo(TEST_STRING_XML);
    formProperty.setCamundaId(TEST_STRING_API);
    formProperty.setCamundaName(TEST_STRING_API);
    formProperty.setCamundaType(TEST_STRING_API);
    formProperty.setCamundaRequired(true);
    formProperty.setCamundaReadable(false);
    formProperty.setCamundaWriteable(false);
    formProperty.setCamundaVariable(TEST_STRING_API);
    formProperty.setCamundaExpression(TEST_EXPRESSION_API);
    formProperty.setCamundaDatePattern(TEST_STRING_API);
    formProperty.setCamundaDefault(TEST_STRING_API);
    assertThat(formProperty.getCamundaId()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.getCamundaName()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.getCamundaType()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.isCamundaRequired()).isTrue();
    assertThat(formProperty.isCamundaReadable()).isFalse();
    assertThat(formProperty.isCamundaWriteable()).isFalse();
    assertThat(formProperty.getCamundaVariable()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(formProperty.getCamundaDatePattern()).isEqualTo(TEST_STRING_API);
    assertThat(formProperty.getCamundaDefault()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testInExtension() {
    CamundaIn in = callActivity.getExtensionElements().getElementsQuery().filterByType(CamundaIn.class).singleResult();
    assertThat(in.getCamundaSource()).isEqualTo(TEST_STRING_XML);
    assertThat(in.getCamundaSourceExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(in.getCamundaVariables()).isEqualTo(TEST_STRING_XML);
    assertThat(in.getCamundaTarget()).isEqualTo(TEST_STRING_XML);
    assertThat(in.getCamundaBusinessKey()).isEqualTo(TEST_EXPRESSION_XML);
    in.setCamundaSource(TEST_STRING_API);
    in.setCamundaSourceExpression(TEST_EXPRESSION_API);
    in.setCamundaVariables(TEST_STRING_API);
    in.setCamundaTarget(TEST_STRING_API);
    in.setCamundaBusinessKey(TEST_EXPRESSION_API);
    assertThat(in.getCamundaSource()).isEqualTo(TEST_STRING_API);
    assertThat(in.getCamundaSourceExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(in.getCamundaVariables()).isEqualTo(TEST_STRING_API);
    assertThat(in.getCamundaTarget()).isEqualTo(TEST_STRING_API);
    assertThat(in.getCamundaBusinessKey()).isEqualTo(TEST_EXPRESSION_API);
  }

  @Test
  public void testOutExtension() {
    CamundaOut out = callActivity.getExtensionElements().getElementsQuery().filterByType(CamundaOut.class).singleResult();
    assertThat(out.getCamundaSource()).isEqualTo(TEST_STRING_XML);
    assertThat(out.getCamundaSourceExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(out.getCamundaVariables()).isEqualTo(TEST_STRING_XML);
    assertThat(out.getCamundaTarget()).isEqualTo(TEST_STRING_XML);
    out.setCamundaSource(TEST_STRING_API);
    out.setCamundaSourceExpression(TEST_EXPRESSION_API);
    out.setCamundaVariables(TEST_STRING_API);
    out.setCamundaTarget(TEST_STRING_API);
    assertThat(out.getCamundaSource()).isEqualTo(TEST_STRING_API);
    assertThat(out.getCamundaSourceExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(out.getCamundaVariables()).isEqualTo(TEST_STRING_API);
    assertThat(out.getCamundaTarget()).isEqualTo(TEST_STRING_API);
  }

  @Test
  public void testPotentialStarter() {
    CamundaPotentialStarter potentialStarter = startEvent.getExtensionElements().getElementsQuery().filterByType(CamundaPotentialStarter.class).singleResult();
    Expression expression = potentialStarter.getResourceAssignmentExpression().getExpression();
    assertThat(expression.getTextContent()).isEqualTo(TEST_GROUPS_XML);
    expression.setTextContent(TEST_GROUPS_API);
    assertThat(expression.getTextContent()).isEqualTo(TEST_GROUPS_API);
  }

  @Test
  public void testTaskListener() {
    CamundaTaskListener taskListener = userTask.getExtensionElements().getElementsQuery().filterByType(CamundaTaskListener.class).list().get(0);
    assertThat(taskListener.getCamundaEvent()).isEqualTo(TEST_TASK_EVENT_XML);
    assertThat(taskListener.getCamundaClass()).isEqualTo(TEST_CLASS_XML);
    assertThat(taskListener.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_XML);
    assertThat(taskListener.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_XML);
    taskListener.setCamundaEvent(TEST_TASK_EVENT_API);
    taskListener.setCamundaClass(TEST_CLASS_API);
    taskListener.setCamundaExpression(TEST_EXPRESSION_API);
    taskListener.setCamundaDelegateExpression(TEST_DELEGATE_EXPRESSION_API);
    assertThat(taskListener.getCamundaEvent()).isEqualTo(TEST_TASK_EVENT_API);
    assertThat(taskListener.getCamundaClass()).isEqualTo(TEST_CLASS_API);
    assertThat(taskListener.getCamundaExpression()).isEqualTo(TEST_EXPRESSION_API);
    assertThat(taskListener.getCamundaDelegateExpression()).isEqualTo(TEST_DELEGATE_EXPRESSION_API);

    CamundaField field = taskListener.getCamundaFields().iterator().next();
    assertThat(field.getCamundaName()).isEqualTo(TEST_STRING_XML);
    assertThat(field.getCamundaString().getTextContent()).isEqualTo(TEST_STRING_XML);
  }

  @Test
  public void testCamundaScriptTaskListener() {
    CamundaTaskListener taskListener = userTask.getExtensionElements().getElementsQuery().filterByType(CamundaTaskListener.class).list().get(1);

    CamundaScript script = taskListener.getCamundaScript();
    assertThat(script.getCamundaScriptFormat()).isEqualTo("groovy");
    assertThat(script.getCamundaResource()).isEqualTo("test.groovy");
    assertThat(script.getTextContent()).isEmpty();

    CamundaScript newScript = modelInstance.newInstance(CamundaScript.class);
    newScript.setCamundaScriptFormat("groovy");
    newScript.setTextContent("println 'Hello World'");
    taskListener.setCamundaScript(newScript);

    script = taskListener.getCamundaScript();
    assertThat(script.getCamundaScriptFormat()).isEqualTo("groovy");
    assertThat(script.getCamundaResource()).isNull();
    assertThat(script.getTextContent()).isEqualTo("println 'Hello World'");
  }

  @Test
  public void testCamundaModelerProperties() {
    CamundaProperties camundaProperties = endEvent.getExtensionElements().getElementsQuery().filterByType(CamundaProperties.class).singleResult();
    assertThat(camundaProperties).isNotNull();
    assertThat(camundaProperties.getCamundaProperties()).hasSize(2);

    for (CamundaProperty camundaProperty : camundaProperties.getCamundaProperties()) {
      assertThat(camundaProperty.getCamundaId()).isNull();
      assertThat(camundaProperty.getCamundaName()).startsWith("name");
      assertThat(camundaProperty.getCamundaValue()).startsWith("value");
    }
  }

  @Test
  public void testGetNonExistingCamundaCandidateUsers() {
    userTask.removeAttributeNs(CAMUNDA_NS, "candidateUsers");
    assertThat(userTask.getCamundaCandidateUsers()).isNull();
    assertThat(userTask.getCamundaCandidateUsersList()).isEmpty();
  }

  @Test
  public void testSetNullCamundaCandidateUsers() {
    assertThat(userTask.getCamundaCandidateUsers()).isNotEmpty();
    assertThat(userTask.getCamundaCandidateUsersList()).isNotEmpty();
    userTask.setCamundaCandidateUsers(null);
    assertThat(userTask.getCamundaCandidateUsers()).isNull();
    assertThat(userTask.getCamundaCandidateUsersList()).isEmpty();
  }

  @Test
  public void testEmptyCamundaCandidateUsers() {
    assertThat(userTask.getCamundaCandidateUsers()).isNotEmpty();
    assertThat(userTask.getCamundaCandidateUsersList()).isNotEmpty();
    userTask.setCamundaCandidateUsers("");
    assertThat(userTask.getCamundaCandidateUsers()).isNull();
    assertThat(userTask.getCamundaCandidateUsersList()).isEmpty();
  }

  @Test
  public void testSetNullCamundaCandidateUsersList() {
    assertThat(userTask.getCamundaCandidateUsers()).isNotEmpty();
    assertThat(userTask.getCamundaCandidateUsersList()).isNotEmpty();
    userTask.setCamundaCandidateUsersList(null);
    assertThat(userTask.getCamundaCandidateUsers()).isNull();
    assertThat(userTask.getCamundaCandidateUsersList()).isEmpty();
  }

  @Test
  public void testEmptyCamundaCandidateUsersList() {
    assertThat(userTask.getCamundaCandidateUsers()).isNotEmpty();
    assertThat(userTask.getCamundaCandidateUsersList()).isNotEmpty();
    userTask.setCamundaCandidateUsersList(Collections.<String>emptyList());
    assertThat(userTask.getCamundaCandidateUsers()).isNull();
    assertThat(userTask.getCamundaCandidateUsersList()).isEmpty();
  }

  @Test
  public void testScriptResource() {
    assertThat(scriptTask.getScriptFormat()).isEqualTo("groovy");
    assertThat(scriptTask.getCamundaResource()).isEqualTo("test.groovy");
  }


  @After
  public void validateModel() {
    Bpmn.validateModel(modelInstance);
  }
}
