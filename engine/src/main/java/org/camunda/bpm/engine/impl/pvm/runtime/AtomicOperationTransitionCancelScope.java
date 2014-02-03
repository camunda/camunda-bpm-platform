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

import org.camunda.bpm.engine.impl.pvm.PvmActivity;

/**
 * Cancel scope operation performed when an execution enters a {@link PvmActivity#isCancelScope()}
 * activity trough a transition. See  {@link AtomicOperationCancelScope} for more details.
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public class AtomicOperationTransitionCancelScope extends AtomicOperationCancelScope {

  public String getCanonicalName() {
    return "transition-cancel-scope";
  }

  protected void scopeCancelled(InterpretableExecution execution) {
    execution.performOperation(TRANSITION_CREATE_SCOPE);
  }

  @Override
  protected PvmActivity getCancellingActivity(InterpretableExecution execution) {
    // the cancelling activity is the activity currently being executed
    return execution.getActivity();
  }

}
