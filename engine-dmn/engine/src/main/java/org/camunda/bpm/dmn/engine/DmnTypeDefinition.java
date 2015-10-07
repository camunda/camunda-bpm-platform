/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.dmn.engine;

import org.camunda.bpm.engine.variable.value.TypedValue;

public interface DmnTypeDefinition {

  String getTypeName();

  /**
   * Transform the given value into the type specified by the type name.
   *
   * @param value to transform into the specified type
   * @return value of specified type
   * @throws IllegalArgumentException if the value can not be transformed
   */
  TypedValue transform(Object value) throws IllegalArgumentException;

}
