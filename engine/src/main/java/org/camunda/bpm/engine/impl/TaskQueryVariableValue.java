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



/**
 * Represents a variable value used in a task query.
 *
 * @author Frederik Heremans
 */
public class TaskQueryVariableValue extends QueryVariableValue {

  private static final long serialVersionUID = 1L;

  protected boolean isProcessInstanceVariable;

  /**
   * <p>The parameters <code>isTaskVariable</code> and <code> isProcessInstanceVariable</code>
   * have the following meaning:</p>
   *
   * <ul>
   *   <li>if <code>isTaskVariable == true</code>: only query after task variables</li>
   *   <li>if <code>isTaskVariable == false && isProcessInstanceVariable == true</code>:
   *       only query after process instance variables</li>
   *   <li>if <code>isTaskVariable == false && isProcessInstanceVariable == false</code>:
   *       only query after case instance variables</li>
   * </ul>
   */
  public TaskQueryVariableValue(String name, Object value, QueryOperator operator, boolean isTaskVariable, boolean isProcessInstanceVariable) {
    super(name, value, operator, isTaskVariable);
    this.isProcessInstanceVariable = isProcessInstanceVariable;
  }

  public boolean isProcessInstanceVariable() {
    return isProcessInstanceVariable;
  }

}
