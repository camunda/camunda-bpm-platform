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
package org.camunda.bpm.engine.variable.impl.value;

import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 *
 */
public class AbstractTypedValue<T> implements TypedValue {

  private static final long serialVersionUID = 1L;

  protected T value;

  protected ValueType type;

  protected boolean isTransient;

  public AbstractTypedValue(T value, ValueType type) {
    this.value = value;
    this.type = type;
  }

  public T getValue() {
    return value;
  }

  public ValueType getType() {
    return type;
  }

  public String toString() {
    return "Value '" + value + "' of type '" + type + "', isTransient=" + isTransient;
  }

  @Override
  public boolean isTransient() {
    return isTransient;
  }

  public void setTransient(boolean isTransient) {
    this.isTransient = isTransient;
  }

}
