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

import java.util.Map;

import org.camunda.bpm.engine.delegate.SerializedVariableValue;
import org.camunda.bpm.engine.impl.core.variable.SerializedVariableValueImpl;

/**
 * Primitive variable types do not differentiate between an object and a serialized representation, i.e.
 * {@link #getValue(ValueFields)} and {@link #getSerializedValue(ValueFields)} return the same values (or in the latter case wrapped).
 * Similarly, {@link #setValue(Object, ValueFields)} and {@link #setValueFromSerialized(Object, Map, ValueFields)} expect
 * the same value argument and an empty or null configuration map in the latter case.
 *
 * @author Thorben Lindhauer
 */
public abstract class PrimitiveVariableType implements VariableType {


  public SerializedVariableValue getSerializedValue(ValueFields valueFields) {
    SerializedVariableValueImpl result = new SerializedVariableValueImpl();
    result.setValue(getValue(valueFields));
    return result;
  }

  public void setValueFromSerialized(Object serializedValue, Map<String, Object> configuration, ValueFields valueFields) {
    setValue(serializedValue, valueFields);
  }

  public boolean isAbleToStoreSerializedValue(Object value, Map<String, Object> configuration) {
    return isAbleToStore(value);
  }
}
