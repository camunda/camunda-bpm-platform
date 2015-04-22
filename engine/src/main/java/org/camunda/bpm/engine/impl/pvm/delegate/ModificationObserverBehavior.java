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
   * Implement to customize the creation of executions in the scope. Called with the
   * scope execution already created for this scope, implementations may create additional execution,
   * set variables, etc. (cf. parallel multi instance that always creates a concurrent execution,
   * although there is only one instance)
   */
  ActivityExecution initializeScope(ActivityExecution scopeExecution);

  /**
   * implement to handle reorganization of the scope when a concurrent execution in the scope is created
   * (e.g. implement to override the default pruning behavior)
   */
  void concurrentExecutionCreated(ActivityExecution scopeExecution, ActivityExecution concurrentExecution);

  /**
   * implement to handle reorganization of the scope when a concurrent execution in the scope is removed
   * (e.g. implement to override the default pruning behavior)
   */
  void concurrentExecutionDeleted(ActivityExecution scopeExecution, ActivityExecution concurrentExecution);

}
