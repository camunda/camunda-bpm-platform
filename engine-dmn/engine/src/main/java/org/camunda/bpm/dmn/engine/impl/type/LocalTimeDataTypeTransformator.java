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
package org.camunda.bpm.dmn.engine.impl.type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.dmn.engine.type.DataTypeTransformer;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.LocalTimeValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Transform values of type {@link Date} and {@link String} into
 * {@link LocalTimeValue}. A String should have the format {@code HH:mm:ss}.
 *
 * @author Philipp Ossler
 */
public class LocalTimeDataTypeTransformator implements DataTypeTransformer {

  protected SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

  @Override
  public TypedValue transform(Object value) throws IllegalArgumentException {
    if (value instanceof String) {
      String localTime = (String) value;
      validateLocalTime(localTime);
      return Variables.localTimeValue(localTime);

    } else if (value instanceof Date) {
      String localTime = transformDate((Date) value);
      return Variables.localTimeValue(localTime);

    } else {
      throw new IllegalArgumentException();
    }
  }

  protected void validateLocalTime(String localTime) throws IllegalArgumentException {
    try {
      format.parse(localTime);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected String transformDate(Date date) {
    return format.format(date);
  }

}
