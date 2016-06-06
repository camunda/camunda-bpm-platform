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

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.CompensateEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.EscalationEventDefinition;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractStartEventBuilder<B extends AbstractStartEventBuilder<B>> extends AbstractCatchEventBuilder<B, StartEvent> {

  protected AbstractStartEventBuilder(BpmnModelInstance modelInstance, StartEvent element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /** camunda extensions */

  /**
   * @deprecated use camundaAsyncBefore() instead.
   *
   * Sets the camunda async attribute to true.
   *
   * @return the builder object
   */
  @Deprecated
  public B camundaAsync() {
    element.setCamundaAsyncBefore(true);
    return myself;
  }

  /**
   * @deprecated use camundaAsyncBefore(isCamundaAsyncBefore) instead.
   *
   * Sets the camunda async attribute.
   *
   * @param isCamundaAsync  the async state of the task
   * @return the builder object
   */
  @Deprecated
  public B camundaAsync(boolean isCamundaAsync) {
    element.setCamundaAsyncBefore(isCamundaAsync);
    return myself;
  }

  /**
   * Sets the camunda form handler class attribute.
   *
   * @param camundaFormHandlerClass  the class name of the form handler
   * @return the builder object
   */
  public B camundaFormHandlerClass(String camundaFormHandlerClass) {
    element.setCamundaFormHandlerClass(camundaFormHandlerClass);
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
   * Sets the camunda initiator attribute.
   *
   * @param camundaInitiator  the initiator to set
   * @return the builder object
   */
  public B camundaInitiator(String camundaInitiator) {
    element.setCamundaInitiator(camundaInitiator);
    return myself;
  }

  /**
   * Creates a new camunda form field extension element.
   *
   * @return the builder object
   */
  public CamundaStartEventFormFieldBuilder camundaFormField() {
    CamundaFormData camundaFormData = getCreateSingleExtensionElement(CamundaFormData.class);
    CamundaFormField camundaFormField = createChild(camundaFormData, CamundaFormField.class);
    return new CamundaStartEventFormFieldBuilder(modelInstance, element, camundaFormField);
  }

  /**
   * Sets a catch all error definition.
   *
   * @return the builder object
   */
  public B error() {
    ErrorEventDefinition errorEventDefinition = createInstance(ErrorEventDefinition.class);
    element.getEventDefinitions().add(errorEventDefinition);

    return myself;
  }

  /**
   * Sets an error definition for the given error code. If already an error
   * with this code exists it will be used, otherwise a new error is created.
   *
   * @param errorCode the code of the error
   * @return the builder object
   */
  public B error(String errorCode) {
    ErrorEventDefinition errorEventDefinition = createErrorEventDefinition(errorCode);
    element.getEventDefinitions().add(errorEventDefinition);

    return myself;
  }

  /**
   * Creates an error event definition with an unique id
   * and returns a builder for the error event definition.
   *
   * @return the error event definition builder object
   */
  public ErrorEventDefinitionBuilder errorEventDefinition(String id) {
    ErrorEventDefinition errorEventDefinition = createEmptyErrorEventDefinition();
    if (id != null) {
      errorEventDefinition.setId(id);
    }

    element.getEventDefinitions().add(errorEventDefinition);
    return new ErrorEventDefinitionBuilder(modelInstance, errorEventDefinition);
  }

  /**
   * Creates an error event definition
   * and returns a builder for the error event definition.
   *
   * @return the error event definition builder object
   */
  public ErrorEventDefinitionBuilder errorEventDefinition() {
    ErrorEventDefinition errorEventDefinition = createEmptyErrorEventDefinition();
    element.getEventDefinitions().add(errorEventDefinition);
    return new ErrorEventDefinitionBuilder(modelInstance, errorEventDefinition);
  }

  /**
   * Sets a catch all escalation definition.
   *
   * @return the builder object
   */
  public B escalation() {
    EscalationEventDefinition escalationEventDefinition = createInstance(EscalationEventDefinition.class);
    element.getEventDefinitions().add(escalationEventDefinition);

    return myself;
  }

  /**
   * Sets an escalation definition for the given escalation code. If already an escalation
   * with this code exists it will be used, otherwise a new escalation is created.
   *
   * @param escalationCode the code of the escalation
   * @return the builder object
   */
  public B escalation(String escalationCode) {
    EscalationEventDefinition escalationEventDefinition = createEscalationEventDefinition(escalationCode);
    element.getEventDefinitions().add(escalationEventDefinition);

    return myself;
  }

  /**
   * Sets a catch compensation definition.
   *
   * @return the builder object
   */
  public B compensation() {
    CompensateEventDefinition compensateEventDefinition = createCompensateEventDefinition();
    element.getEventDefinitions().add(compensateEventDefinition);

    return myself;
  }

  /**
   * Sets whether the start event is interrupting or not.
   */
  public B interrupting(boolean interrupting) {
    element.setInterrupting(interrupting);

    return myself;
  }

}
