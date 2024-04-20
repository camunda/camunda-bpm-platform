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
package org.camunda.bpm.client.variable;

import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.client.variable.impl.mapper.DateValueMapper;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

public class DateValueMapperTest {

  protected final static String DATE_FORMAT = "dd.MM.yyyy - HH:mm:ss.SSSZ";
  protected final Date VARIABLE_VALUE_DATE = new GregorianCalendar(2018, Calendar.JANUARY, 1, 8, 0, 0).getTime();
  protected final String VARIABLE_VALUE_DATE_SERIALIZED = new SimpleDateFormat(DATE_FORMAT).format(VARIABLE_VALUE_DATE);

  protected DateValueMapper dateValueMapper;

  @Before
  public void setup() {
    dateValueMapper = new DateValueMapper(DATE_FORMAT);
  }

  @Test
  public void shouldConvertToTypedValue() {
    // given
    UntypedValueImpl untypedValue = (UntypedValueImpl) Variables.untypedValue(VARIABLE_VALUE_DATE);

    // when
    DateValue dateValue = dateValueMapper.convertToTypedValue(untypedValue);

    // then
    assertThat(dateValue.getType()).isInstanceOf(PrimitiveValueTypeImpl.DateTypeImpl.class);
    assertThat(dateValue.getValue()).isEqualTo(VARIABLE_VALUE_DATE);
  }

  @Test
  public void shouldReadValue() {
    // given
    TypedValueField typedValueField = new TypedValueField();
    typedValueField.setValue(VARIABLE_VALUE_DATE_SERIALIZED);
    typedValueField.setType("Date");

    // when
    DateValue dateValue = dateValueMapper.readValue(typedValueField);

    // then
    assertThat(dateValue.getType()).isInstanceOf(PrimitiveValueTypeImpl.DateTypeImpl.class);
    assertThat(dateValue.getValue()).isEqualTo(VARIABLE_VALUE_DATE);
  }

  @Test
  public void shouldReadValue_Null() {
    // given
    TypedValueField typedValueField = new TypedValueField();
    typedValueField.setValue(null);
    typedValueField.setType("Date");

    // when
    DateValue dateValue = dateValueMapper.readValue(typedValueField);

    // then
    assertThat(dateValue.getType()).isInstanceOf(PrimitiveValueTypeImpl.DateTypeImpl.class);
    assertThat(dateValue.getValue()).isEqualTo(null);
  }

  @Test
  public void shouldWriteValue() {
    // given
    DateValue dateValue = Variables.dateValue(VARIABLE_VALUE_DATE);
    TypedValueField typedValueField = new TypedValueField();

    // when
    dateValueMapper.writeValue(dateValue, typedValueField);

    // then
    assertThat(typedValueField.getValue()).isEqualTo(VARIABLE_VALUE_DATE_SERIALIZED);
  }

}
