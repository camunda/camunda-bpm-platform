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
package org.camunda.bpm.engine.variable.value;

import java.io.Serializable;

import org.camunda.bpm.engine.variable.type.ValueType;

/**
 * <p>A {@link TypedValue} is a value with additional type information (the {@link ValueType}).
 * TypedValues are used for representing variable values.</p>
 *
 * @author Daniel Meyer
 * @since 7.2
 */
public interface TypedValue extends Serializable {

  /**
   * The actual value. May be null in case the value is null.
   *
   * @return the value
   */
  Object getValue();

  /**
   * The type of the value. See ValueType for a list of built-in ValueTypes.
   * @return the type of the value.
   */
  ValueType getType();

  /**
   * Indicator for transience of the value
   * @return isTransient
   */
  boolean isTransient();

}
