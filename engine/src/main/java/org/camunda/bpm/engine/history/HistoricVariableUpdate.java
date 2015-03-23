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

package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.variable.value.TypedValue;

/** Update of a process variable.  This is only available if history
 * level is configured to FULL.
 *
 * @author Tom Baeyens
 */
public interface HistoricVariableUpdate extends HistoricDetail {

  String getVariableName();

  /**
   * Returns the id of the corresponding variable instance.
   */
  String getVariableInstanceId();

  /**
   * Returns the type name of the variable
   *
   * @return the type name of the variable
   */
  String getTypeName();

  /**
   * @return the name of the variable type.
   * @deprecated since 7.2. Use {@link #getTypeName()}
   */
  @Deprecated
  String getVariableTypeName();

  Object getValue();

  /**
   * @return the {@link TypedValue} for this variable update
   */
  TypedValue getTypedValue();

  int getRevision();

  /**
   * If the variable value could not be loaded, this returns the error message.
   * @return an error message indicating why the variable value could not be loaded.
   */
  String getErrorMessage();
}
