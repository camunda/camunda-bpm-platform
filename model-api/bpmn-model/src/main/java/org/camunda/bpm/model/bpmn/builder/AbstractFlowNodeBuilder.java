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
package org.camunda.bpm.model.bpmn.builder;

import org.camunda.bpm.model.bpmn.AssociationDirection;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFailedJobRetryTimeCycle;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractFlowNodeBuilder<B extends AbstractFlowNodeBuilder<B, E>, E extends FlowNode> extends AbstractFlowElementBuilder<B, E> {

  private SequenceFlowBuilder currentSequenceFlowBuilder;

  protected boolean compensationStarted;
  protected BoundaryEvent compensateBoundaryEvent;

  protected AbstractFlowNodeBuilder(BpmnModelInstance modelInstance, E element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  private SequenceFlowBuilder getCurrentSequenceFlowBuilder() {
    if (currentSequenceFlowBuilder == null) {
      SequenceFlow sequenceFlow = createSibling(SequenceFlow.class);
      currentSequenceFlowBuilder = sequenceFlow.builder();
    }
    return currentSequenceFlowBuilder;
  }

  public B condition(String name, String condition) {
    if (name != null) {
      getCurrentSequenceFlowBuilder().name(name);
    }
    ConditionExpression conditionExpression = createInstance(ConditionExpression.class);
    conditionExpression.setTextContent(condition);
    getCurrentSequenceFlowBuilder().condition(conditionExpression);
    return myself;
  }

  protected void connectTarget(FlowNode target) {
    // check if compensation was started
    if (isBoundaryEventWithStartedCompensation()) {
        // the target activity should be marked for compensation
        if (target instanceof Activity) {
          ((Activity) target).setForCompensation(true);
        }

        // connect the target via association instead of sequence flow
        connectTargetWithAssociation(target);
    }
    else if (isCompensationHandler()) {
      // cannot connect to a compensation handler
      throw new BpmnModelException("Only single compensation handler allowed. Call compensationDone() to continue main flow.");
    }
    else {
      // connect as sequence flow by default
      connectTargetWithSequenceFlow(target);
    }
  }

  protected void connectTargetWithSequenceFlow(FlowNode target) {
    getCurrentSequenceFlowBuilder().from(element).to(target);

    SequenceFlow sequenceFlow = getCurrentSequenceFlowBuilder().getElement();
    createEdge(sequenceFlow);
    currentSequenceFlowBuilder = null;
  }

  protected void connectTargetWithAssociation(FlowNode target) {
    Association association = modelInstance.newInstance(Association.class);
    association.setTarget(target);
    association.setSource(element);
    association.setAssociationDirection(AssociationDirection.One);
    element.getParentElement().addChildElement(association);

    createEdge(association);
  }

  public AbstractFlowNodeBuilder compensationDone(){
    if (compensateBoundaryEvent != null) {
      return compensateBoundaryEvent.getAttachedTo().builder();
    }
    else {
      throw new BpmnModelException("No compensation in progress. Call compensationStart() first.");
    }
  }

  public B sequenceFlowId(String sequenceFlowId) {
    getCurrentSequenceFlowBuilder().id(sequenceFlowId);
    return myself;
  }

  private <T extends FlowNode> T createTarget(Class<T> typeClass) {
    return createTarget(typeClass, null);
  }

  protected <T extends FlowNode> T createTarget(Class<T> typeClass, String identifier) {
    T target = createSibling(typeClass, identifier);

    BpmnShape targetBpmnShape = createBpmnShape(target);
    setCoordinates(targetBpmnShape);
    connectTarget(target);
    resizeSubProcess(targetBpmnShape);
    return target;
  }

  protected <T extends AbstractFlowNodeBuilder, F extends FlowNode> T createTargetBuilder(Class<F> typeClass) {
    return createTargetBuilder(typeClass, null);
  }

  @SuppressWarnings("unchecked")
  protected <T extends AbstractFlowNodeBuilder, F extends FlowNode> T createTargetBuilder(Class<F> typeClass, String id) {
    AbstractFlowNodeBuilder builder = createTarget(typeClass, id).builder();

    if (compensationStarted) {
      // pass on current boundary event to return after compensationDone call
      builder.compensateBoundaryEvent = compensateBoundaryEvent;
    }

    return (T) builder;

  }

  public ServiceTaskBuilder serviceTask() {
    return createTargetBuilder(ServiceTask.class);
  }

  public ServiceTaskBuilder serviceTask(String id) {
    return createTargetBuilder(ServiceTask.class, id);
  }

  public SendTaskBuilder sendTask() {
    return createTargetBuilder(SendTask.class);
  }

  public SendTaskBuilder sendTask(String id) {
    return createTargetBuilder(SendTask.class, id);
  }

  public UserTaskBuilder userTask() {
    return createTargetBuilder(UserTask.class);
  }

  public UserTaskBuilder userTask(String id) {
    return createTargetBuilder(UserTask.class, id);
  }

  public BusinessRuleTaskBuilder businessRuleTask() {
    return createTargetBuilder(BusinessRuleTask.class);
  }

  public BusinessRuleTaskBuilder businessRuleTask(String id) {
    return createTargetBuilder(BusinessRuleTask.class, id);
  }

  public ScriptTaskBuilder scriptTask() {
    return createTargetBuilder(ScriptTask.class);
  }

  public ScriptTaskBuilder scriptTask(String id) {
    return createTargetBuilder(ScriptTask.class, id);
  }

  public ReceiveTaskBuilder receiveTask() {
    return createTargetBuilder(ReceiveTask.class);
  }

  public ReceiveTaskBuilder receiveTask(String id) {
    return createTargetBuilder(ReceiveTask.class, id);
  }

  public ManualTaskBuilder manualTask() {
    return createTargetBuilder(ManualTask.class);
  }

  public ManualTaskBuilder manualTask(String id) {
    return createTargetBuilder(ManualTask.class, id);
  }

  public EndEventBuilder endEvent() {
    return createTarget(EndEvent.class).builder();
  }

  public EndEventBuilder endEvent(String id) {
    return createTarget(EndEvent.class, id).builder();
  }

  public ParallelGatewayBuilder parallelGateway() {
    return createTarget(ParallelGateway.class).builder();
  }

  public ParallelGatewayBuilder parallelGateway(String id) {
    return createTarget(ParallelGateway.class, id).builder();
  }

  public ExclusiveGatewayBuilder exclusiveGateway() {
    return createTarget(ExclusiveGateway.class).builder();
  }

  public InclusiveGatewayBuilder inclusiveGateway() {
    return createTarget(InclusiveGateway.class).builder();
  }

  public EventBasedGatewayBuilder eventBasedGateway() {
    return createTarget(EventBasedGateway.class).builder();
  }

  public ExclusiveGatewayBuilder exclusiveGateway(String id) {
    return createTarget(ExclusiveGateway.class, id).builder();
  }

  public InclusiveGatewayBuilder inclusiveGateway(String id) {
    return createTarget(InclusiveGateway.class, id).builder();
  }

  public IntermediateCatchEventBuilder intermediateCatchEvent() {
    return createTarget(IntermediateCatchEvent.class).builder();
  }

  public IntermediateCatchEventBuilder intermediateCatchEvent(String id) {
    return createTarget(IntermediateCatchEvent.class, id).builder();
  }

  public IntermediateThrowEventBuilder intermediateThrowEvent() {
    return createTarget(IntermediateThrowEvent.class).builder();
  }

  public IntermediateThrowEventBuilder intermediateThrowEvent(String id) {
    return createTarget(IntermediateThrowEvent.class, id).builder();
  }

  public CallActivityBuilder callActivity() {
    return createTarget(CallActivity.class).builder();
  }

  public CallActivityBuilder callActivity(String id) {
    return createTarget(CallActivity.class, id).builder();
  }

  public SubProcessBuilder subProcess() {
    return createTarget(SubProcess.class).builder();
  }

  public SubProcessBuilder subProcess(String id) {
    return createTarget(SubProcess.class, id).builder();
  }

  public TransactionBuilder transaction() {
    Transaction transaction = createTarget(Transaction.class);
    return new TransactionBuilder(modelInstance, transaction);
  }

  public TransactionBuilder transaction(String id) {
    Transaction transaction = createTarget(Transaction.class, id);
    return new TransactionBuilder(modelInstance, transaction);
  }

  public Gateway findLastGateway() {
    FlowNode lastGateway = element;
    while (true) {
      try {
        lastGateway = lastGateway.getPreviousNodes().singleResult();
        if (lastGateway instanceof Gateway) {
          return (Gateway) lastGateway;
        }
      } catch (BpmnModelException e) {
        throw new BpmnModelException("Unable to determine an unique previous gateway of " + lastGateway.getId(), e);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  public AbstractGatewayBuilder moveToLastGateway() {
    return findLastGateway().builder();
  }

  @SuppressWarnings("rawtypes")
  public AbstractFlowNodeBuilder moveToNode(String identifier) {
    ModelElementInstance instance = modelInstance.getModelElementById(identifier);
    if (instance != null && instance instanceof FlowNode) {
      return ((FlowNode) instance).builder();
    } else {
      throw new BpmnModelException("Flow node not found for id " + identifier);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public <T extends AbstractActivityBuilder> T moveToActivity(String identifier) {
    ModelElementInstance instance = modelInstance.getModelElementById(identifier);
    if (instance != null && instance instanceof Activity) {
      return (T) ((Activity) instance).builder();
    } else {
      throw new BpmnModelException("Activity not found for id " + identifier);
    }
  }

  @SuppressWarnings("rawtypes")
  public AbstractFlowNodeBuilder connectTo(String identifier) {
    ModelElementInstance target = modelInstance.getModelElementById(identifier);
    if (target == null) {
      throw new BpmnModelException("Unable to connect " + element.getId() + " to element " + identifier + " cause it not exists.");
    } else if (!(target instanceof FlowNode)) {
      throw new BpmnModelException("Unable to connect " + element.getId() + " to element " + identifier + " cause its not a flow node.");
    } else {
      FlowNode targetNode = (FlowNode) target;
      connectTarget(targetNode);
      return targetNode.builder();
    }
  }

  /**
   * Sets the Camunda AsyncBefore attribute for the build flow node.
   *
   * @param asyncBefore
   *          boolean value to set
   * @return the builder object
   */
  public B camundaAsyncBefore(boolean asyncBefore) {
    element.setCamundaAsyncBefore(asyncBefore);
    return myself;
  }

  /**
   * Sets the Camunda asyncBefore attribute to true.
   *
   * @return the builder object
   */
  public B camundaAsyncBefore() {
    element.setCamundaAsyncBefore(true);
    return myself;
  }

  /**
   * Sets the Camunda asyncAfter attribute for the build flow node.
   *
   * @param asyncAfter
   *          boolean value to set
   * @return the builder object
   */
  public B camundaAsyncAfter(boolean asyncAfter) {
    element.setCamundaAsyncAfter(asyncAfter);
    return myself;
  }

  /**
   * Sets the Camunda asyncAfter attribute to true.
   *
   * @return the builder object
   */
  public B camundaAsyncAfter() {
    element.setCamundaAsyncAfter(true);
    return myself;
  }

  /**
   * Sets the Camunda exclusive attribute to true.
   *
   * @return the builder object
   */
  public B notCamundaExclusive() {
    element.setCamundaExclusive(false);
    return myself;
  }

  /**
   * Sets the camunda exclusive attribute for the build flow node.
   *
   * @param exclusive
   *          boolean value to set
   * @return the builder object
   */
  public B camundaExclusive(boolean exclusive) {
    element.setCamundaExclusive(exclusive);
    return myself;
  }

  public B camundaJobPriority(String jobPriority) {
    element.setCamundaJobPriority(jobPriority);
    return myself;
  }

  /**
   * Sets the camunda failedJobRetryTimeCycle attribute for the build flow node.
   *
   * @param retryTimeCycle
   *          the retry time cycle value to set
   * @return the builder object
   */
  public B camundaFailedJobRetryTimeCycle(String retryTimeCycle) {
    CamundaFailedJobRetryTimeCycle failedJobRetryTimeCycle = createInstance(CamundaFailedJobRetryTimeCycle.class);
    failedJobRetryTimeCycle.setTextContent(retryTimeCycle);

    addExtensionElement(failedJobRetryTimeCycle);

    return myself;
  }

  @SuppressWarnings("rawtypes")
  public B camundaExecutionListenerClass(String eventName, Class listenerClass) {
    return camundaExecutionListenerClass(eventName, listenerClass.getName());
  }

  public B camundaExecutionListenerClass(String eventName, String fullQualifiedClassName) {
    CamundaExecutionListener executionListener = createInstance(CamundaExecutionListener.class);
    executionListener.setCamundaEvent(eventName);
    executionListener.setCamundaClass(fullQualifiedClassName);

    addExtensionElement(executionListener);

    return myself;
  }

  public B camundaExecutionListenerExpression(String eventName, String expression) {
    CamundaExecutionListener executionListener = createInstance(CamundaExecutionListener.class);
    executionListener.setCamundaEvent(eventName);
    executionListener.setCamundaExpression(expression);

    addExtensionElement(executionListener);

    return myself;
  }

  public B camundaExecutionListenerDelegateExpression(String eventName, String delegateExpression) {
    CamundaExecutionListener executionListener = createInstance(CamundaExecutionListener.class);
    executionListener.setCamundaEvent(eventName);
    executionListener.setCamundaDelegateExpression(delegateExpression);

    addExtensionElement(executionListener);

    return myself;
  }

  public B compensationStart() {
    if (element instanceof BoundaryEvent) {
      BoundaryEvent boundaryEvent = (BoundaryEvent) element;
      for (EventDefinition eventDefinition : boundaryEvent.getEventDefinitions()) {
        if(eventDefinition instanceof CompensateEventDefinition) {
          // if the boundary event contains a compensate event definition then
          // save the boundary event to later return to it and start a compensation

          compensateBoundaryEvent = boundaryEvent;
          compensationStarted = true;

          return myself;
        }
      }
    }

    throw new BpmnModelException("Compensation can only be started on a boundary event with a compensation event definition");
  }

  protected boolean isBoundaryEventWithStartedCompensation() {
    return compensationStarted && compensateBoundaryEvent != null;
  }

  protected boolean isCompensationHandler() {
    return !compensationStarted && compensateBoundaryEvent != null;
  }

}
