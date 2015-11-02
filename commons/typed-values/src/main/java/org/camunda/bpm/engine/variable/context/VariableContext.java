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
package org.camunda.bpm.engine.variable.context;

import java.util.Set;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * A context for variables. Allows resolving variables.
 *
 * An API may choose to accept a VariableContext instead of a map of concrete values
 * in situations where passing all available variables would be expensive and
 * lazy-loading is a desirable optimization.
 *
 * @author Daniel Meyer
 *
 */
public interface VariableContext {

  /**
   * Resolve a value in this context.
   *
   * @param variableName the name of the variable to resolve.
   * @return the value of the variable or null in case the variable does not exist.
   */
  TypedValue resolve(String variableName);

  /**
   * Checks whether a variable with the given name is resolve through this context.
   *
   * @param variableName the name of the variable to check
   * @return true if the variable is resolve.
   */
  boolean containsVariable(String variableName);

  /**
   * @return a set of all variable names resolvable through this Context.
   */
  Set<String> keySet();

}
