/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.delegate;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

/**
 * Implemented by classes which provide access to the {@link BpmnModelInstance}
 * and the currently executed {@link FlowElement}.
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public interface BpmnModelExecutionContext {

  /**
   * Returns the {@link BpmnModelInstance} for the currently executed Bpmn Model
   *
   * @return the current {@link BpmnModelInstance}
   */
  BpmnModelInstance getBpmnModelInstance();

  /**
   * <p>Returns the currently executed Element in the BPMN Model. This method returns a {@link FlowElement} which may be casted
   * to the concrete type of the Bpmn Model Element currently executed.</p>
   *
   * <p>If called from a Service {@link ExecutionListener}, the method will return the corresponding {@link FlowNode}
   * for {@link ExecutionListener#EVENTNAME_START} and {@link ExecutionListener#EVENTNAME_END} and the corresponding
   * {@link SequenceFlow} for {@link ExecutionListener#EVENTNAME_TAKE}.</p>
   *
   * @return the {@link FlowElement} corresponding to the current Bpmn Model Element
   */
  FlowElement getBpmnModelElementInstance();

}
