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
package org.camunda.bpm.client.impl.variable;

import org.camunda.bpm.client.impl.variable.mapper.ValueMapper;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class VariableValue {

  protected ValueMappers mappers;

  protected TypedValueField typedValueField;

  protected ValueMapper<?> serializer;
  protected TypedValue cachedValue;

  public VariableValue(TypedValueField typedValueField, ValueMappers mappers) {
    this.typedValueField = typedValueField;
    this.mappers = mappers;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    TypedValue typedValue = getTypedValue();
    if (typedValue != null) {
      return (T) typedValue.getValue();
    } else {
      return null;
    }
  }

  public <T extends TypedValue> T getTypedValue() {
    return getTypedValue(true);
  }

  @SuppressWarnings("unchecked")
  public <T extends TypedValue> T getTypedValue(boolean deserializeValue) {
    if (cachedValue != null && cachedValue instanceof SerializableValue) {
      SerializableValue serializableValue = (SerializableValue) cachedValue;
      if(deserializeValue && !serializableValue.isDeserialized()) {
        cachedValue = null;
      }
    }

    if (cachedValue == null ) {
      cachedValue = getSerializer().readValue(typedValueField, deserializeValue);
    }

    return (T) cachedValue;
  }

  public ValueMapper<?> getSerializer() {
    if (serializer == null) {
      serializer = mappers.findMapperForTypedValueField(typedValueField);
    }
    return serializer;
  }

}
