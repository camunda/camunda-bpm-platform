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

package org.camunda.bpm.engine.delegate;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tom Baeyens
 */
public interface VariableScope {

  String getVariableScopeKey();

  Map<String, Object> getVariables();

  VariableMap getVariablesTyped();

  VariableMap getVariablesTyped(boolean deserializeValues);

  Map<String, Object> getVariablesLocal();

  VariableMap getVariablesLocalTyped();

  VariableMap getVariablesLocalTyped(boolean deserializeValues);

  Object getVariable(String variableName);

  Object getVariableLocal(String variableName);

  <T extends TypedValue> T getVariableTyped(String variableName);

  <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeValue);

  <T extends TypedValue> T getVariableLocalTyped(String variableName);

  <T extends TypedValue> T getVariableLocalTyped(String variableName, boolean deserializeValue);

  Set<String> getVariableNames();

  Set<String> getVariableNamesLocal();

  void setVariable(String variableName, Object value);

  void setVariableLocal(String variableName, Object value);

  void setVariables(Map<String, ? extends Object> variables);

  void setVariablesLocal(Map<String, ? extends Object> variables);

  boolean hasVariables();

  boolean hasVariablesLocal();

  boolean hasVariable(String variableName);

  boolean hasVariableLocal(String variableName);

  /**
   * Removes the variable and creates a new
   * {@link HistoricVariableUpdateEntity}.
   */
  void removeVariable(String variableName);

  /**
   * Removes the local variable and creates a new
   * {@link HistoricVariableUpdateEntity}.
   */
  void removeVariableLocal(String variableName);

  /**
   * Removes the variables and creates a new
   * {@link HistoricVariableUpdateEntity} for each of them.
   */
  void removeVariables(Collection<String> variableNames);

  /**
   * Removes the local variables and creates a new
   * {@link HistoricVariableUpdateEntity} for each of them.
   */
  void removeVariablesLocal(Collection<String> variableNames);

  /**
   * Removes the (local) variables and creates a new
   * {@link HistoricVariableUpdateEntity} for each of them.
   */
  void removeVariables();

  /**
   * Removes the (local) variables and creates a new
   * {@link HistoricVariableUpdateEntity} for each of them.
   */
  void removeVariablesLocal();

}
