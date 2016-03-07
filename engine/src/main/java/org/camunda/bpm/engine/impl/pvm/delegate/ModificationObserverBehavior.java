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
package org.camunda.bpm.engine.impl.pvm.delegate;

import java.util.List;

/**
 * When a (scope) activity behavior implements this behavior,
 * its scope execution is notified in case of an external modification about the following:
 *
 * <ul>
 *   <li> the scope execution is newly created
 *   <li> a new concurrent execution is created in that scope
 *   <li> a concurrent execution is removed in that scope
 * </ul>
 *
 * @author Thorben Lindhauer
 */
public interface ModificationObserverBehavior extends ActivityBehavior {

  /**
   * Implement to customize initialization of the scope. Called with the
   * scope execution already created. Implementations may set variables, etc.
   * Implementations should provide return as many executions as there are requested by the argument.
   * Valid number of instances are >= 0.
   */
  List<ActivityExecution> initializeScope(ActivityExecution scopeExecution, int nrOfInnerInstances);

  /**
   * Returns an execution that can be used to execute an activity within that scope.
   * May reorganize other executions in that scope (e.g. implement to override the default pruning behavior).
   */
  ActivityExecution createInnerInstance(ActivityExecution scopeExecution);

  /**
   * implement to destroy an execution in this scope and handle the scope's reorganization
   * (e.g. implement to override the default pruning behavior). The argument execution is not yet removed.
   */
  void destroyInnerInstance(ActivityExecution concurrentExecution);

}
