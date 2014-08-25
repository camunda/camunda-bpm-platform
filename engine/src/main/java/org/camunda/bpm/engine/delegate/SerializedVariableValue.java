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

import java.util.Map;

import org.camunda.bpm.engine.impl.variable.VariableType;

/**
 * The serialized representation of a process variable.
 * Depending on the variable type, it can have different values and configurations.
 * Confer {@link ProcessEngineVariableType} for documentation of the default variable
 * types.
 *
 * @author Thorben Lindhauer
 */
public interface SerializedVariableValue {

  /**
   * Returns the serialized representation of the variable.
   * For primitive types (integer, string, etc.), serialized values are the same as the regular values.
   * For object types, serialized values return the representation of these objects
   * as stored in the database.
   */
  Object getValue();

  /**
   * Returns variable configuration that is required for the serialized that provides
   * meaning to the serialized value. For example, the configuration could contain
   * the class name of the serialized object. The actual configuration depends on the
   * {@link VariableType}; These classes also provide constants for accessing the expected
   * configuration properties.
   */
  Map<String, Object> getConfig();
}
