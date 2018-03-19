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

import org.apache.commons.codec.binary.Base64;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tassilo Weidner
 */
public class BytesValueMapper extends AbstractPrimitiveValueMapper<BytesValue> {

  public BytesValueMapper() {
    super(ValueType.BYTES);
  }

  @SuppressWarnings("unchecked")
  public BytesValue deserializeTypedValue(TypedValueDto typedValueDto) {
    String value = (String) typedValueDto.getValue();
    byte[] bytes = Base64.decodeBase64(value);
    typedValueDto.setValue(bytes);

    return super.deserializeTypedValue(typedValueDto);
  }

  @Override
  public TypedValueDto serializeTypedValue(TypedValue typedValue) {
    TypedValueDto typedValueDto = super.serializeTypedValue(typedValue);
    byte[] bytes = (byte[]) typedValue.getValue();
    typedValueDto.setValue(Base64.encodeBase64String(bytes));

    return typedValueDto;
  }

}
