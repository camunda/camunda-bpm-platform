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
package org.camunda.bpm.engine.impl.pvm.runtime;

import org.camunda.bpm.engine.impl.pvm.PvmEvent;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * <p>This atomic operation simply fires the activity end event</p>
 *
 * @author Daniel Meyer
 *
 */
public class AtomicOperationFireActivityEnd extends AbstractEventAtomicOperation implements AtomicOperation {

  public String getCanonicalName() {
    return "fire-activity-end";
  }

  protected ScopeImpl getScope(InterpretableExecution execution) {
    return (ScopeImpl) execution.getActivity();
  }

  protected String getEventName() {
    return PvmEvent.EVENTNAME_END;
  }

  protected void eventNotificationsCompleted(InterpretableExecution execution) {
    // nothing to do
  }

}
