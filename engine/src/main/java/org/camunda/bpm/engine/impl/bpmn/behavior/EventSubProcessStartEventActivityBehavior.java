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
 * <p>Specialization of the Start Event for Event Sub-Processes.</p>
 *
 * The corresponding activity must either be
 * <ul>
 *  <li>{@link PvmActivity#isInterrupting()} in case of an interrupting event subprocess. In this case
 *  the scope will already be interrupted when this behavior is executed.</li>
 *  <li>{@link PvmActivity#isConcurrent()} in case of a non-interrupting event subprocess. In this case
 *  the new concurrent execution will already be created when this behavior is executed.</li>
 * </ul>
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 */
public class EventSubProcessStartEventActivityBehavior extends NoneStartEventActivityBehavior {

}
