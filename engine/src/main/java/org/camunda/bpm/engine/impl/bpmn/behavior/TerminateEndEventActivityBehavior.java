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
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * <p>The BPMN terminate End Event.</p>
 *
 * <p>The hosting activity must be marked {@link PvmActivity#isCancelScope()} since it will
 * cancel the scope in which it is embedded. When this activitiy is executed, the sope execution
 * will have performed "cancel scope" behavior and the {@link ActivityBehavior} will be called on
 * the scope execution.</p>
 *
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 */
public class TerminateEndEventActivityBehavior extends FlowNodeActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
    // we are the last execution inside this scope: calling end() ends this scope.
    execution.end();
  }

}
