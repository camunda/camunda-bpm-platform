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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;



/**
 * @author Tom Baeyens
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public interface Condition {

  /**
   * Evaluates the condition and returns the result.
   * The scope will be the same as the execution.
   *
   * @param execution the execution which is used to evaluate the condition
   * @return the result
   */
  boolean evaluate(DelegateExecution execution);

  /**
   * Evaluates the condition and returns the result.
   *
   * @param scope the variable scope which can differ of the execution
   * @param execution the execution which is used to evaluate the condition
   * @return the result
   */
  boolean evaluate(VariableScope scope, DelegateExecution execution);

  /**
   * Tries to evaluate the condition. If the property which is used in the condition does not exist
   * false will be returned.
   *
   * @param scope the variable scope which can differ of the execution
   * @param execution the execution which is used to evaluate the condition
   * @return the result
   */
  boolean tryEvaluate(VariableScope scope, DelegateExecution execution);
}
