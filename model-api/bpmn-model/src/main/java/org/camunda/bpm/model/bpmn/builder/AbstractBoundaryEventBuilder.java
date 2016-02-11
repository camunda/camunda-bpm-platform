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
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractBoundaryEventBuilder<B extends AbstractBoundaryEventBuilder<B>> extends AbstractCatchEventBuilder<B, BoundaryEvent> {

  protected AbstractBoundaryEventBuilder(BpmnModelInstance modelInstance, BoundaryEvent element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Set if the boundary event cancels the attached activity.
   *
   * @param cancelActivity true if the boundary event cancels the activiy, false otherwise
   * @return the builder object
   */
  public B cancelActivity(Boolean cancelActivity) {
    element.setCancelActivity(cancelActivity);

    return myself;
  }

}
