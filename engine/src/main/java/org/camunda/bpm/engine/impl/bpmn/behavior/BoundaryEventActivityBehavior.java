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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.pvm.PvmActivity;


/**
 * <p>The BPMN Boundary Event.</p>
 *
 * <p>The corresponding activity must either be
 * <ul>
 *  <li>{@link PvmActivity#isCancelActivity()} in case of an interrupting boundary event. In this case
 *  the scope to which the boundary event is attached will already be cancelled when this behavior is executed.</li>
 *  <li>{@link PvmActivity#isConcurrent()} in case of a non-interrupting boundary event. In this case
 *  the new concurrent execution will already have been created when this behavior is executed.</li>
 * </ul>
 * </p>
 * <p>As a result, the boundary event does noting "special". It will be left by all outgoing sequence flows
 * just like any other activity.</p>
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 */
public class BoundaryEventActivityBehavior extends FlowNodeActivityBehavior {

}
