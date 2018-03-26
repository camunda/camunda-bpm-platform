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
package org.camunda.bpm.client.impl.variable.mapper.primitive;

import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.variable.mapper.ValueMapper;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tassilo Weidner
 */
public abstract class PrimitiveValueMapper<T extends TypedValue> implements ValueMapper<T> {

  protected PrimitiveValueType type;

  protected PrimitiveValueMapper(PrimitiveValueType type) {
    this.type = type;
  }

  public String getTypeName() {
    return type.getName();
  }

  public boolean isAssignable(TypedValue typedValue) {
    if (typedValue == null || typedValue.getValue() == null) { // forward to null value mapper
      return false;
    }

    Object value = typedValue.getValue();
    return isAssignable(value);
  }

  protected boolean isAssignable(Object value) {
    return type.getJavaType().isAssignableFrom(value.getClass());
  }

  @SuppressWarnings("unchecked")
  public T convertToTypedValue(UntypedValueImpl untypedValue) {
    return (T) type.createValue(untypedValue.getValue(), null); // untyped variables have no value info
  }

  @SuppressWarnings("unchecked")
  public T deserializeTypedValue(TypedValueDto typedValueDto) throws EngineClientException {
    Object value = typedValueDto.getValue();

    if (isAssignable(value)) {
      return (T) type.createValue(value, typedValueDto.getValueInfo());
    }
    else {
      return null; // value does not correspond to type; might occur due to manipulated engine database
    }
  }

}
