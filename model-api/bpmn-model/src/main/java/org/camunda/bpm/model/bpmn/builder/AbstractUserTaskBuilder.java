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

import java.util.List;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractUserTaskBuilder<B extends AbstractUserTaskBuilder<B>> extends AbstractTaskBuilder<B, UserTask> {

  protected AbstractUserTaskBuilder(BpmnModelInstance modelInstance, UserTask element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets the implementation of the build user task.
   *
   * @param implementation  the implementation to set
   * @return the builder object
   */
  public B implementation(String implementation) {
    element.setImplementation(implementation);
    return myself;
  }

  /** camunda extensions */

  /**
   * Sets the camunda attribute assignee.
   *
   * @param camundaAssignee  the assignee to set
   * @return the builder object
   */
  public B camundaAssignee(String camundaAssignee) {
    element.setCamundaAssignee(camundaAssignee);
    return myself;
  }

  /**
   * Sets the camunda candidate groups attribute.
   *
   * @param camundaCandidateGroups  the candidate groups to set
   * @return the builder object
   */
  public B camundaCandidateGroups(String camundaCandidateGroups) {
    element.setCamundaCandidateGroups(camundaCandidateGroups);
    return myself;
  }

  /**
   * Sets the camunda candidate groups attribute.
   *
   * @param camundaCandidateGroups  the candidate groups to set
   * @return the builder object
   */
  public B camundaCandidateGroups(List<String> camundaCandidateGroups) {
    element.setCamundaCandidateGroupsList(camundaCandidateGroups);
    return myself;
  }

  /**
   * Sets the camunda candidate users attribute.
   *
   * @param camundaCandidateUsers  the candidate users to set
   * @return the builder object
   */
  public B camundaCandidateUsers(String camundaCandidateUsers) {
    element.setCamundaCandidateUsers(camundaCandidateUsers);
    return myself;
  }

  /**
   * Sets the camunda candidate users attribute.
   *
   * @param camundaCandidateUsers  the candidate users to set
   * @return the builder object
   */
  public B camundaCandidateUsers(List<String> camundaCandidateUsers) {
    element.setCamundaCandidateUsersList(camundaCandidateUsers);
    return myself;
  }

  /**
   * Sets the camunda due date attribute.
   *
   * @param camundaDueDate  the due date of the user task
   * @return the builder object
   */
  public B camundaDueDate(String camundaDueDate) {
    element.setCamundaDueDate(camundaDueDate);
    return myself;
  }

  /**
   * Sets the camunda follow up date attribute.
   *
   * @param camundaFollowUpDate  the follow up date of the user task
   * @return the builder object
   */
  public B camundaFollowUpDate(String camundaFollowUpDate) {
    element.setCamundaFollowUpDate(camundaFollowUpDate);
    return myself;
  }

  /**
   * Sets the camunda form handler class attribute.
   *
   * @param camundaFormHandlerClass  the class name of the form handler
   * @return the builder object
   */
  @SuppressWarnings("rawtypes")
  public B camundaFormHandlerClass(Class camundaFormHandlerClass) {
    return camundaFormHandlerClass(camundaFormHandlerClass.getName());
  }

  /**
   * Sets the camunda form handler class attribute.
   *
   * @param camundaFormHandlerClass  the class name of the form handler
   * @return the builder object
   */
  public B camundaFormHandlerClass(String fullQualifiedClassName) {
    element.setCamundaFormHandlerClass(fullQualifiedClassName);
    return myself;
  }

  /**
   * Sets the camunda form key attribute.
   *
   * @param camundaFormKey  the form key to set
   * @return the builder object
   */
  public B camundaFormKey(String camundaFormKey) {
    element.setCamundaFormKey(camundaFormKey);
    return myself;
  }

  /**
   * Sets the camunda form ref attribute.
   *
   * @param camundaFormRef the form ref to set
   * @return the builder object
   */
  public B camundaFormRef(String camundaFormRef) {
    element.setCamundaFormRef(camundaFormRef);
    return myself;
  }

  /**
   * Sets the camunda form ref binding attribute.
   *
   * @param camundaFormRef the form ref binding to set
   * @return the builder object
   */
  public B camundaFormRefBinding(String camundaFormRefBinding) {
    element.setCamundaFormRefBinding(camundaFormRefBinding);
    return myself;
  }

  /**
   * Sets the camunda form ref version attribute.
   *
   * @param camundaFormRef the form ref version to set
   * @return the builder object
   */
  public B camundaFormRefVersion(String camundaFormRefVersion) {
    element.setCamundaFormRefVersion(camundaFormRefVersion);
    return myself;
  }

  /**
   * Sets the camunda priority attribute.
   *
   * @param camundaPriority  the priority of the user task
   * @return the builder object
   */
  public B camundaPriority(String camundaPriority) {
    element.setCamundaPriority(camundaPriority);
    return myself;
  }

  /**
   * Creates a new camunda form field extension element.
   *
   * @return the builder object
   */
  public CamundaUserTaskFormFieldBuilder camundaFormField() {
    CamundaFormData camundaFormData = getCreateSingleExtensionElement(CamundaFormData.class);
    CamundaFormField camundaFormField = createChild(camundaFormData, CamundaFormField.class);
    return new CamundaUserTaskFormFieldBuilder(modelInstance, element, camundaFormField);
  }

  /**
   * Add a class based task listener with specified event name
   *
   * @param eventName - event names to listen to
   * @param fullQualifiedClassName - a string representing a class
   * @return the builder object
   */
  @SuppressWarnings("rawtypes")
  public B camundaTaskListenerClass(String eventName, Class listenerClass) {
    return camundaTaskListenerClass(eventName, listenerClass.getName());
  }

  /**
   * Add a class based task listener with specified event name
   *
   * @param eventName - event names to listen to
   * @param fullQualifiedClassName - a string representing a class
   * @return the builder object
   */
  public B camundaTaskListenerClass(String eventName, String fullQualifiedClassName) {
    CamundaTaskListener executionListener = createInstance(CamundaTaskListener.class);
    executionListener.setCamundaEvent(eventName);
    executionListener.setCamundaClass(fullQualifiedClassName);

    addExtensionElement(executionListener);

    return myself;
  }

  public B camundaTaskListenerExpression(String eventName, String expression) {
    CamundaTaskListener executionListener = createInstance(CamundaTaskListener.class);
    executionListener.setCamundaEvent(eventName);
    executionListener.setCamundaExpression(expression);

    addExtensionElement(executionListener);

    return myself;
  }

  public B camundaTaskListenerDelegateExpression(String eventName, String delegateExpression) {
    CamundaTaskListener executionListener = createInstance(CamundaTaskListener.class);
    executionListener.setCamundaEvent(eventName);
    executionListener.setCamundaDelegateExpression(delegateExpression);

    addExtensionElement(executionListener);

    return myself;
  }

  @SuppressWarnings("rawtypes")
  public B camundaTaskListenerClassTimeoutWithCycle(String id, Class listenerClass, String timerCycle) {
    return camundaTaskListenerClassTimeoutWithCycle(id, listenerClass.getName(), timerCycle);
  }

  @SuppressWarnings("rawtypes")
  public B camundaTaskListenerClassTimeoutWithDate(String id, Class listenerClass, String timerDate) {
    return camundaTaskListenerClassTimeoutWithDate(id, listenerClass.getName(), timerDate);
  }

  @SuppressWarnings("rawtypes")
  public B camundaTaskListenerClassTimeoutWithDuration(String id, Class listenerClass, String timerDuration) {
    return camundaTaskListenerClassTimeoutWithDuration(id, listenerClass.getName(), timerDuration);
  }

  public B camundaTaskListenerClassTimeoutWithCycle(String id, String fullQualifiedClassName, String timerCycle) {
    return createCamundaTaskListenerClassTimeout(id, fullQualifiedClassName, createTimeCycle(timerCycle));
  }

  public B camundaTaskListenerClassTimeoutWithDate(String id, String fullQualifiedClassName, String timerDate) {
    return createCamundaTaskListenerClassTimeout(id, fullQualifiedClassName, createTimeDate(timerDate));
  }

  public B camundaTaskListenerClassTimeoutWithDuration(String id, String fullQualifiedClassName, String timerDuration) {
    return createCamundaTaskListenerClassTimeout(id, fullQualifiedClassName, createTimeDuration(timerDuration));
  }

  public B camundaTaskListenerExpressionTimeoutWithCycle(String id, String expression, String timerCycle) {
    return createCamundaTaskListenerExpressionTimeout(id, expression, createTimeCycle(timerCycle));
  }

  public B camundaTaskListenerExpressionTimeoutWithDate(String id, String expression, String timerDate) {
    return createCamundaTaskListenerExpressionTimeout(id, expression, createTimeDate(timerDate));
  }

  public B camundaTaskListenerExpressionTimeoutWithDuration(String id, String expression, String timerDuration) {
    return createCamundaTaskListenerExpressionTimeout(id, expression, createTimeDuration(timerDuration));
  }

  public B camundaTaskListenerDelegateExpressionTimeoutWithCycle(String id, String delegateExpression, String timerCycle) {
    return createCamundaTaskListenerDelegateExpressionTimeout(id, delegateExpression, createTimeCycle(timerCycle));
  }

  public B camundaTaskListenerDelegateExpressionTimeoutWithDate(String id, String delegateExpression, String timerDate) {
    return createCamundaTaskListenerDelegateExpressionTimeout(id, delegateExpression, createTimeDate(timerDate));
  }

  public B camundaTaskListenerDelegateExpressionTimeoutWithDuration(String id, String delegateExpression, String timerDuration) {
    return createCamundaTaskListenerDelegateExpressionTimeout(id, delegateExpression, createTimeDuration(timerDuration));
  }

  protected B createCamundaTaskListenerClassTimeout(String id, String fullQualifiedClassName, TimerEventDefinition timerDefinition) {
    CamundaTaskListener executionListener = createCamundaTaskListenerTimeout(id, timerDefinition);
    executionListener.setCamundaClass(fullQualifiedClassName);
    return myself;
  }

  protected B createCamundaTaskListenerExpressionTimeout(String id, String expression, TimerEventDefinition timerDefinition) {
    CamundaTaskListener executionListener = createCamundaTaskListenerTimeout(id, timerDefinition);
    executionListener.setCamundaExpression(expression);
    return myself;
  }

  protected B createCamundaTaskListenerDelegateExpressionTimeout(String id, String delegateExpression, TimerEventDefinition timerDefinition) {
    CamundaTaskListener executionListener = createCamundaTaskListenerTimeout(id, timerDefinition);
    executionListener.setCamundaDelegateExpression(delegateExpression);
    return myself;
  }

  protected CamundaTaskListener createCamundaTaskListenerTimeout(String id, TimerEventDefinition timerDefinition) {
    CamundaTaskListener executionListener = createInstance(CamundaTaskListener.class);
    executionListener.setAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID, id, true);
    executionListener.setCamundaEvent("timeout");
    executionListener.addChildElement(timerDefinition);
    addExtensionElement(executionListener);
    return executionListener;
  }
}
