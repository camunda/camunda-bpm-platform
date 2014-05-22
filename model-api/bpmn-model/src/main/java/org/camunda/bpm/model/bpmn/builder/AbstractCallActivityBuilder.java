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
import org.camunda.bpm.model.bpmn.instance.CallActivity;

/**
 * @author Sebastian Menski
 */
public class AbstractCallActivityBuilder<B extends AbstractCallActivityBuilder<B>> extends AbstractActivityBuilder<B, CallActivity> {

  protected AbstractCallActivityBuilder(BpmnModelInstance modelInstance, CallActivity element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets the called element
   *
   * @param calledElement  the process to call
   * @return the builder object
   */
  public B calledElement(String calledElement) {
    element.setCalledElement(calledElement);
    return myself;
  }

  /** camunda extensions */

  /**
   * Sets the camunda async attribute to true.
   *
   * @return the builder object
   */
  public B camundaAsync() {
    element.setCamundaAsync(true);
    return myself;
  }

  /**
   * Sets the camunda async attribute.
   *
   * @param isCamundaAsync  the async state of the task
   * @return the builder object
   */
  public B camundaAsync(boolean isCamundaAsync) {
    element.setCamundaAsync(isCamundaAsync);
    return myself;
  }

  /**
   * Sets the camunda calledElementBinding attribute
   *
   * @param camundaCalledElementBinding  the element binding to use
   * @return the builder object
   */
  public B camundaCalledElementBinding(String camundaCalledElementBinding) {
    element.setCamundaCalledElementBinding(camundaCalledElementBinding);
    return myself;
  }

  /**
   * Sets the camunda calledElementVersion attribute
   *
   * @param camundaCalledElementVersion  the element version to use
   * @return the builder object
   */
  public B camundaCalledElementVersion(String camundaCalledElementVersion) {
    element.setCamundaCalledElementVersion(camundaCalledElementVersion);
    return myself;
  }

  /**
   * Sets camunda exclusive attribute to false.
   *
   * @return the builder object
   */
  public B notCamundaExclusive() {
    element.setCamundaExclusive(false);
    return myself;
  }

  /**
   * Sets the camunda exclusive attribute.
   *
   * @param isCamundaExclusive  the exclusive state of the task.
   * @return the builder object
   */
  public B camundaExclusive(boolean isCamundaExclusive) {
    element.setCamundaExclusive(isCamundaExclusive);
    return myself;
  }

}
