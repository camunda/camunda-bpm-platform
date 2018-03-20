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

import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Tassilo Weidner
 */
public class DateValueMapper extends AbstractPrimitiveValueMapper<DateValue> {

  public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  protected final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

  public DateValueMapper() {
    super(ValueType.DATE);
  }

  @SuppressWarnings("unchecked")
  public DateValue deserializeTypedValue(TypedValueDto typedValueDto) {
    Object value = typedValueDto.getValue();

    Date date = null;
    try {
      date = sdf.parse((String) value);
    } catch (ParseException e) {
      return null;
    }

    typedValueDto.setValue(date);

    return super.deserializeTypedValue(typedValueDto);
  }

  public TypedValueDto serializeTypedValue(TypedValue typedValue) {
    TypedValueDto typedValueDto = super.serializeTypedValue(typedValue);
    Date date = (Date) typedValue.getValue();
    typedValueDto.setValue(sdf.format(date));

    return typedValueDto;
  }

}