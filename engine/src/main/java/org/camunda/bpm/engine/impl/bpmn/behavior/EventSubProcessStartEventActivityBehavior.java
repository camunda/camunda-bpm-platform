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
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;


/**
 * <p>Specialization of the Start Event for Event Sub-Processes.</p>
 *
 * The corresponding activity must either be
 * <ul>
 *  <li>{@link PvmActivity#isCancelScope()} in case of an interrupting event subprocess. In this case
 *  the scope will already be cancelled when this behavior is executed.</li>
 *  <li>{@link PvmActivity#isConcurrent()} in case of a non-interrupting event subprocess. In this case
 *  the new concurrent execution will already be created when this behavior is executed.</li>
 * </ul>
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 */
public class EventSubProcessStartEventActivityBehavior extends NoneStartEventActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
    PvmActivity parent = (PvmActivity) execution.getActivity().getParent();

    if (parent.isCancelScope()) {

      // We need to do an interrupt scope in order to remove all jobs (timers ...) and
      // Message / signal event subscriptions created by this or other start events.

      // The interrupting event subprocess must only fire once and cancel the Event Handlers
      // created by other event subprocesses.
      execution.interruptScope("Interrupting event sub process "+ parent + " fired.");
    }

    super.execute(execution);
  }

}
