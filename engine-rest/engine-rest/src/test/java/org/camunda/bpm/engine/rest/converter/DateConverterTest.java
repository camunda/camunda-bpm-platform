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
package org.camunda.bpm.engine.rest.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;

import java.util.Date;

public class DateConverterTest {
  private DateConverter converter;

  @Before
  public void setUp() throws Exception {
    converter = new DateConverter();
  }

  @Test(expected = InvalidRequestException.class)
  public void shouldFailForDoubleQuotedValue() {
    //when
    converter.convertQueryParameterToType("\"pizza\"");
  }

  @Test(expected = InvalidRequestException.class)
  public void shouldFailForSingleDoubleQuotedValue() {
    //when
    converter.convertQueryParameterToType("2014-01-01T00:00:00+0200\"");
  }

  @Test
  public void shouldConvertDate() throws JsonProcessingException {
    //given
    String value = "2014-01-01T00:00:00+0200";
    ObjectMapper mock = mock(ObjectMapper.class);
    converter.setObjectMapper(mock);
    when(mock.readValue(anyString(), eq(Date.class))).thenReturn(DateTimeUtil.parseDate(value));

    //when
    Date date = converter.convertQueryParameterToType(value);

    //then
    assertEquals(date, DateTimeUtil.parseDate(value));
  }
}