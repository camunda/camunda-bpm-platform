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

import org.camunda.bpm.engine.delegate.VariableScope;



/** behavior for activities that delegate to a complete separate execution of
 * a process definition.  In BPMN terminology this can be used to implement a reusable subprocess.
 *
 * @author Tom Baeyens
 */
public interface SubProcessActivityBehavior extends ActivityBehavior {

  /**
   * Pass the output variables from the process instance of the subprocess to the given execution.
   * This should be called before the process instance is destroyed.
   *
   * @param targetExecution execution of the calling process instance to pass the variables to
   * @param calledElementInstance instance of the called element that serves as the variable source
   */
  void passOutputVariables(ActivityExecution targetExecution, VariableScope calledElementInstance);

  /**
   * Called after the process instance is destroyed for
   * this activity to perform its outgoing control flow logic.
   *
   * @param execution
   * @throws java.lang.Exception
   */
  void completed(ActivityExecution execution) throws Exception;
}
