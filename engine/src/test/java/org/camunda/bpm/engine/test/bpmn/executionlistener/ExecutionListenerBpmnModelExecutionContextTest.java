/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.test.bpmn.executionlistener;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.Collection;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

/**
 * @author Sebastian Menski
 */
public class ExecutionListenerBpmnModelExecutionContextTest extends PluggableProcessEngineTestCase {

  private static final String PROCESS_ID = "process";
  private static final String START_ID = "start";
  private static final String SEQUENCE_FLOW_ID = "sequenceFlow";
  private static final String CATCH_EVENT_ID = "catchEvent";
  private static final String GATEWAY_ID = "gateway";
  private static final String USER_TASK_ID = "userTask";
  private static final String END_ID = "end";
  private static final String MESSAGE_ID = "messageId";
  private static final String MESSAGE_NAME = "messageName";

  private String deploymentId;

  public void testProcessStartEvent() {
    deployAndStartTestProcess(PROCESS_ID, ExecutionListener.EVENTNAME_START);
    assertFlowElementIs(StartEvent.class);
    sendMessage();
    completeTask();
  }

  public void testStartEventEndEvent() {
    deployAndStartTestProcess(START_ID, ExecutionListener.EVENTNAME_END);
    assertFlowElementIs(StartEvent.class);
    sendMessage();
    completeTask();
  }

  public void testSequenceFlowTakeEvent() {
    deployAndStartTestProcess(SEQUENCE_FLOW_ID, ExecutionListener.EVENTNAME_TAKE);
    assertFlowElementIs(SequenceFlow.class);
    sendMessage();
    completeTask();
  }

  public void testIntermediateCatchEventStartEvent() {
    deployAndStartTestProcess(CATCH_EVENT_ID, ExecutionListener.EVENTNAME_START);
    assertFlowElementIs(IntermediateCatchEvent.class);
    sendMessage();
    completeTask();
  }

  public void testIntermediateCatchEventEndEvent() {
    deployAndStartTestProcess(CATCH_EVENT_ID, ExecutionListener.EVENTNAME_END);
    assertNotNotified();
    sendMessage();
    assertFlowElementIs(IntermediateCatchEvent.class);
    completeTask();
  }

  public void testGatewayStartEvent() {
    deployAndStartTestProcess(GATEWAY_ID, ExecutionListener.EVENTNAME_START);
    assertNotNotified();
    sendMessage();
    assertFlowElementIs(Gateway.class);
    completeTask();
  }

  public void testGatewayEndEvent() {
    deployAndStartTestProcess(GATEWAY_ID, ExecutionListener.EVENTNAME_END);
    assertNotNotified();
    sendMessage();
    assertFlowElementIs(ParallelGateway.class);
    completeTask();
  }

  public void testUserTaskStartEvent() {
    deployAndStartTestProcess(USER_TASK_ID, ExecutionListener.EVENTNAME_START);
    assertNotNotified();
    sendMessage();
    assertFlowElementIs(UserTask.class);
    completeTask();
  }

  public void testUserTaskEndEvent() {
    deployAndStartTestProcess(USER_TASK_ID, ExecutionListener.EVENTNAME_END);
    assertNotNotified();
    sendMessage();
    completeTask();
    assertFlowElementIs(UserTask.class);
  }

  public void testEndEventStartEvent() {
    deployAndStartTestProcess(END_ID, ExecutionListener.EVENTNAME_START);
    assertNotNotified();
    sendMessage();
    completeTask();
    assertFlowElementIs(EndEvent.class);
  }

  public void testProcessEndEvent() {
    deployAndStartTestProcess(PROCESS_ID, ExecutionListener.EVENTNAME_END);
    assertNotNotified();
    sendMessage();
    completeTask();
    assertFlowElementIs(EndEvent.class);
  }

  private void assertNotNotified() {
    assertNull(ModelExecutionContextExecutionListener.modelInstance);
    assertNull(ModelExecutionContextExecutionListener.flowElement);
  }

  private void assertFlowElementIs(Class<? extends FlowElement> elementClass) {
    BpmnModelInstance modelInstance = ModelExecutionContextExecutionListener.modelInstance;
    assertNotNull(modelInstance);

    Model model = modelInstance.getModel();
    Collection<ModelElementInstance> events = modelInstance.getModelElementsByType(model.getType(Event.class));
    assertEquals(3, events.size());
    Collection<ModelElementInstance> gateways = modelInstance.getModelElementsByType(model.getType(Gateway.class));
    assertEquals(1, gateways.size());
    Collection<ModelElementInstance> tasks = modelInstance.getModelElementsByType(model.getType(Task.class));
    assertEquals(1, tasks.size());

    FlowElement flowElement = ModelExecutionContextExecutionListener.flowElement;
    assertNotNull(flowElement);
    assertTrue(elementClass.isAssignableFrom(flowElement.getClass()));
  }

  private void sendMessage() {
    runtimeService.correlateMessage(MESSAGE_NAME);
  }

  private void completeTask() {
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);
  }

  private void deployAndStartTestProcess(String elementId, String eventName) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
      .startEvent(START_ID)
        .sequenceFlowId(SEQUENCE_FLOW_ID)
      .intermediateCatchEvent(CATCH_EVENT_ID)
      .parallelGateway(GATEWAY_ID)
      .userTask(USER_TASK_ID)
      .endEvent(END_ID)
      .done();

    addMessageEventDefinition((CatchEvent) modelInstance.getModelElementById(CATCH_EVENT_ID));
    addExecutionListener((BaseElement) modelInstance.getModelElementById(elementId), eventName);
    deployAndStartProcess(modelInstance);
  }

  private void addMessageEventDefinition(CatchEvent catchEvent) {
    BpmnModelInstance modelInstance = (BpmnModelInstance) catchEvent.getModelInstance();
    Message message = modelInstance.newInstance(Message.class);
    message.setId(MESSAGE_ID);
    message.setName(MESSAGE_NAME);
    modelInstance.getDefinitions().addChildElement(message);
    MessageEventDefinition messageEventDefinition = modelInstance.newInstance(MessageEventDefinition.class);
    messageEventDefinition.setMessage(message);
    catchEvent.getEventDefinitions().add(messageEventDefinition);
  }

  private void addExecutionListener(BaseElement element, String eventName) {
    ExtensionElements extensionElements = element.getModelInstance().newInstance(ExtensionElements.class);
    ModelElementInstance executionListener = extensionElements.addExtensionElement(CAMUNDA_NS, "executionListener");
    executionListener.setAttributeValueNs(CAMUNDA_NS, "class", ModelExecutionContextExecutionListener.class.getName());
    executionListener.setAttributeValueNs(CAMUNDA_NS, "event", eventName);
    element.setExtensionElements(extensionElements);
  }

  private void deployAndStartProcess(BpmnModelInstance modelInstance) {
    deploymentId = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy().getId();
    runtimeService.startProcessInstanceByKey(PROCESS_ID);
  }

  public void tearDown() {
    ModelExecutionContextExecutionListener.clear();
    repositoryService.deleteDeployment(deploymentId, true);
  }

}
