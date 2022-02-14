/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.client.variable.impl.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.DateValue;

public class DateValueMapper extends PrimitiveValueMapper<DateValue> {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected String dateFormat;

  public DateValueMapper(String dateFormat) {
    super(ValueType.DATE);
    this.dateFormat = dateFormat;
  }

  public DateValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.dateValue((Date) untypedValue.getValue());
  }

  public DateValue readValue(TypedValueField typedValueField) {
    Date date = null;

    String value = (String) typedValueField.getValue();
    if (value != null) {
      SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
      try {
        date = sdf.parse(value);
      }
      catch (ParseException e) {
        throw LOG.valueMapperExceptionWhileParsingDate(value, e);
      }
    }

    return Variables.dateValue(date);
  }

  public void writeValue(DateValue dateValue, TypedValueField typedValueField) {
    Date date = (Date) dateValue.getValue();

    if (date != null) {
      SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
      typedValueField.setValue(sdf.format(date));
    }
  }

  protected boolean canReadValue(TypedValueField typedValueField) {
    Object value = typedValueField.getValue();
    return value == null || value instanceof String;
  }

}
