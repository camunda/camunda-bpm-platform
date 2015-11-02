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
package org.camunda.bpm.engine.variable;

import java.util.Map;

import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * A Map of variables.
 *
 * @author Daniel Meyer
 *
 */
public interface VariableMap extends Map<String, Object> {

  // fluent api for collecting variables ////////////////////////

  VariableMap putValue(String name, Object value);

  VariableMap putValueTyped(String name, TypedValue value);

  // retrieving variables ///////////////////////////////////////

  <T> T getValue(String name, Class<T> type);

  <T extends TypedValue> T getValueTyped(String name);

  /**
   * Interprets the variable map as variable context
   *
   * @return A VariableContext which is capable of resolving all variables in the map
   */
  VariableContext asVariableContext();
}
