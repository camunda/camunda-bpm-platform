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
package org.camunda.bpm.engine.impl.variable;



/**
 * @author Tom Baeyens
 * @author roman.smirnov
 */
public interface VariableType {

  /**
   * name of variable type (limited to 100 characters length)
   */
  String getTypeName();

  /**
   * <p>Returns the type name (i.e. the simple class name) of the passed value.</p>
   *
   * <p><strong>Note:</strong>For a serializable value only the type name "Serializable"
   * will currently be returned and not the simple class name of the passed value.</p>
   */
  String getTypeNameForValue(Object value);
  boolean isCachable();
  boolean isAbleToStore(Object value);
  void setValue(Object value, ValueFields valueFields);
  Object getValue(ValueFields valueFields);
  Object getRawValue(ValueFields valueFields);

}
