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
package org.camunda.bpm.client.variable.impl;

import org.camunda.bpm.client.variable.impl.value.DeferredFileValueImpl;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class VariableValue<T extends TypedValue> {

  protected String processInstanceId;
  protected String variableName;
  protected TypedValueField typedValueField;
  protected ValueMappers mappers;

  protected ValueMapper<T> serializer;
  protected T cachedValue;

  public VariableValue(String processInstanceId, String variableName, TypedValueField typedValueField, ValueMappers mappers) {
    this.processInstanceId = processInstanceId;
    this.variableName = variableName;
    this.typedValueField = typedValueField;
    this.mappers = mappers;
  }

  public Object getValue() {
    TypedValue typedValue = getTypedValue();
    if (typedValue != null) {
      return typedValue.getValue();
    } else {
      return null;
    }
  }

  public T getTypedValue() {
    return getTypedValue(true);
  }

  public T getTypedValue(boolean deserializeValue) {
    if (cachedValue != null && cachedValue instanceof SerializableValue) {
      SerializableValue serializableValue = (SerializableValue) cachedValue;
      if(deserializeValue && !serializableValue.isDeserialized()) {
        cachedValue = null;
      }
    }

    if (cachedValue == null) {
      cachedValue = getSerializer().readValue(typedValueField, deserializeValue);

      if (cachedValue instanceof DeferredFileValueImpl) {
        DeferredFileValueImpl fileValue = (DeferredFileValueImpl) cachedValue;
        fileValue.setProcessInstanceId(processInstanceId);
        fileValue.setVariableName(variableName);
      }
    }

    return cachedValue;
  }

  @SuppressWarnings("unchecked")
  public ValueMapper<T> getSerializer() {
    if (serializer == null) {
      serializer = mappers.findMapperForTypedValueField(typedValueField);
    }
    return serializer;
  }

}
