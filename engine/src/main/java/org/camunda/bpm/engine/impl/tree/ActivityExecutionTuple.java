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

package org.camunda.bpm.engine.impl.tree;

import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Tuple of a scope and an execution.
 *
 * @author Philipp Ossler
 *
 */
public class ActivityExecutionTuple {

  private final ActivityExecution execution;
  private final PvmScope scope;

  public ActivityExecutionTuple(PvmScope scope, ActivityExecution execution) {
    this.execution = execution;
    this.scope = scope;
  }

  public ActivityExecution getExecution() {
    return execution;
  }

  public PvmScope getScope() {
    return scope;
  }
}