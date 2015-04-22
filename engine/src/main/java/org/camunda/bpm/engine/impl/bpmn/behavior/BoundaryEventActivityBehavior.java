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

import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;


/**
 * <p>The BPMN Boundary Event.</p>
 *
 * <p>The behavior of the boundary event is defined via it's {@link ActivityStartBehavior}. It must be either
 * {@value ActivityStartBehavior#CANCEL_EVENT_SCOPE} or {@value ActivityStartBehavior#CONCURRENT_IN_FLOW_SCOPE} meaning
 * that it will either cancel the scope execution for the activity it is attached to (it's event scope) or will be executed concurrently
 * in it's flow scope.</p>
 * <p>The boundary event does noting "special" in its inner behavior.</p>
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 */
public class BoundaryEventActivityBehavior extends FlowNodeActivityBehavior {

}
