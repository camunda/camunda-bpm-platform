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
package org.camunda.bpm.dmn.engine.type;

import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformer;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformerRegistry;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.feel.syntaxtree.ZonedTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the build-in {@link DmnDataTypeTransformer}s.
 *
 * @author Philipp Ossler
 */
public class DmnDataTypeTransformerTest extends DmnEngineTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected DmnDataTypeTransformerRegistry registry;

  @Before
  public void initRegistry() {
    DmnEngineConfiguration configuration = dmnEngine.getConfiguration();
    registry = ((DefaultDmnEngineConfiguration) configuration).getTransformer().getDataTypeTransformerRegistry();
  }

  @Test
  public void customType() {
    // by default, the factory should return a transformer for unsupported type
    // that just box the value into an untyped value
    assertThat(registry.getTransformer("custom").transform("42"), is(Variables.untypedValue("42")));
  }

  @Test
  public void stringType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("string");

    assertThat(typeTransformer.transform("abc"), is((TypedValue) Variables.stringValue("abc")));
    assertThat(typeTransformer.transform(true), is((TypedValue) Variables.stringValue("true")));
    assertThat(typeTransformer.transform(4), is((TypedValue) Variables.stringValue("4")));
    assertThat(typeTransformer.transform(2L), is((TypedValue) Variables.stringValue("2")));
    assertThat(typeTransformer.transform(4.2), is((TypedValue) Variables.stringValue("4.2")));
  }

  @Test
  public void booleanType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("boolean");

    assertThat(typeTransformer.transform(true), is((TypedValue) Variables.booleanValue(true)));
    assertThat(typeTransformer.transform(false), is((TypedValue) Variables.booleanValue(false)));

    assertThat(typeTransformer.transform("true"), is((TypedValue) Variables.booleanValue(true)));
    assertThat(typeTransformer.transform("false"), is((TypedValue) Variables.booleanValue(false)));
  }

  @Test
  public void invalidStringValueForBooleanType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("boolean");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform("NaB");
  }

  @Test
  public void integerType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("integer");

    assertThat(typeTransformer.transform(4), is((TypedValue) Variables.integerValue(4)));
    assertThat(typeTransformer.transform("4"), is((TypedValue) Variables.integerValue(4)));
    assertThat(typeTransformer.transform(2L), is((TypedValue) Variables.integerValue(2)));
    assertThat(typeTransformer.transform(4.0), is((TypedValue) Variables.integerValue(4)));

    assertThat(typeTransformer.transform(Integer.MIN_VALUE), is((TypedValue) Variables.integerValue(Integer.MIN_VALUE)));
    assertThat(typeTransformer.transform(Integer.MAX_VALUE), is((TypedValue) Variables.integerValue(Integer.MAX_VALUE)));
  }

  @Test
  public void invalidStringValueForIntegerType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform("4.2");
  }

  @Test
  public void invalidDoubleValueForIntegerType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(4.2);
  }

  @Test
  public void invalidLongValueForIntegerType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(Long.MAX_VALUE);
  }

  @Test
  public void invalidIntegerMinValueForIntegerType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(Integer.MIN_VALUE - 1L);
  }

  @Test
  public void invalidIntegerMaxValueForIntegerType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("integer");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(Integer.MAX_VALUE + 1L);
  }

  @Test
  public void longType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("long");

    assertThat(typeTransformer.transform(2L), is((TypedValue) Variables.longValue(2L)));
    assertThat(typeTransformer.transform("2"), is((TypedValue) Variables.longValue(2L)));
    assertThat(typeTransformer.transform(4), is((TypedValue) Variables.longValue(4L)));
    assertThat(typeTransformer.transform(4.0), is((TypedValue) Variables.longValue(4L)));

    assertThat(typeTransformer.transform(Long.MIN_VALUE), is((TypedValue) Variables.longValue(Long.MIN_VALUE)));
    assertThat(typeTransformer.transform(Long.MAX_VALUE), is((TypedValue) Variables.longValue(Long.MAX_VALUE)));
  }

  @Test
  public void invalidStringValueForLongType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("long");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform("4.2");
  }

  @Test
  public void invalidDoubleValueForLongType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("long");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(4.2);
  }

  @Test
  public void invalidDoubleMinValueForLongType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("long");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform(Double.MIN_VALUE);
  }

  @Test
  public void doubleType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("double");

    assertThat(typeTransformer.transform(4.2), is((TypedValue) Variables.doubleValue(4.2)));
    assertThat(typeTransformer.transform("4.2"), is((TypedValue) Variables.doubleValue(4.2)));
    assertThat(typeTransformer.transform(4), is((TypedValue) Variables.doubleValue(4.0)));
    assertThat(typeTransformer.transform(4L), is((TypedValue) Variables.doubleValue(4.0)));

    assertThat(typeTransformer.transform(Double.MIN_VALUE), is((TypedValue) Variables.doubleValue(Double.MIN_VALUE)));
    assertThat(typeTransformer.transform(Double.MAX_VALUE), is((TypedValue) Variables.doubleValue(Double.MAX_VALUE)));
    assertThat(typeTransformer.transform(-Double.MAX_VALUE), is((TypedValue) Variables.doubleValue(-Double.MAX_VALUE)));
    assertThat(typeTransformer.transform(Long.MAX_VALUE), is((TypedValue) Variables.doubleValue((double) Long.MAX_VALUE)));
  }

  @Test
  public void invalidStringValueForDoubleType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("double");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform("NaD");
  }

  @Test
  public void dateType() throws ParseException {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("date");

    Date date = toDate("2015-09-18T12:00:00", null);
    TypedValue dateValue = Variables.dateValue(date);

    assertThat(typeTransformer.transform("2015-09-18T12:00:00"), is(dateValue));
    assertThat(typeTransformer.transform(date), is(dateValue));
  }

  @Test
  public void shouldTransformZonedDateTime() {
    // given
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("date");

    Date date = toDate("2015-09-18T12:00:00", "Europe/Berlin");
    TypedValue dateValue = Variables.dateValue(date);
    ZonedDateTime zonedDateTime = ZonedDateTime.of(2015, 9, 18, 12, 0, 0, 0, ZoneId.of("Europe/Berlin"));

    // when
    TypedValue transformedFromZonedDateTime = typeTransformer.transform(zonedDateTime);

    // then
    assertThat(transformedFromZonedDateTime, is(dateValue));
  }

  @Test
  public void shouldTransformLocalDateTime() {
    // given
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("date");

    Date date = toDate("2015-09-18T15:00:00", null);
    TypedValue dateValue = Variables.dateValue(date);
    LocalDateTime localDateTime = LocalDateTime.parse("2015-09-18T15:00:00");

    // when
    TypedValue transformedFromLocalDateTime = typeTransformer.transform(localDateTime);

    // then
    assertThat(transformedFromLocalDateTime, is(dateValue));
  }

  @Test
  public void shouldThrowExceptionDueToUnsupportedType_LocalTime() {
    // given
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("date");

    java.time.LocalTime localTime = java.time.LocalTime.now();

    // then
    thrown.expect(DmnEngineException.class);
    thrown.expectMessage("Unsupported type: 'java.time.LocalTime' " +
      "cannot be converted to 'java.util.Date'");

    // when
    typeTransformer.transform(localTime);
  }

  @Test
  public void shouldThrowExceptionDueToUnsupportedType_LocalDate() {
    // given
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("date");

    LocalDate localDate = LocalDate.now();

    // then
    thrown.expect(DmnEngineException.class);
    thrown.expectMessage("Unsupported type: 'java.time.LocalDate' " +
      "cannot be converted to 'java.util.Date'");

    // when
    typeTransformer.transform(localDate);
  }

  @Test
  public void shouldThrowExceptionDueToUnsupportedType_ZonedTime() {
    // given
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("date");

    ZonedTime zonedTime = ZonedTime.parse("22:22:22@Europe/Berlin");

    // then
    thrown.expect(DmnEngineException.class);
    thrown.expectMessage("Unsupported type: 'org.camunda.feel.syntaxtree.ZonedTime' " +
                           "cannot be converted to 'java.util.Date'");

    // when
    typeTransformer.transform(zonedTime);
  }

  @Test
  public void shouldThrowExceptionDueToUnsupportedType_Duration() {
    // given
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("date");

    Duration duration = Duration.ofMillis(5);

    // then
    thrown.expect(DmnEngineException.class);
    thrown.expectMessage("Unsupported type: 'java.time.Duration' " +
      "cannot be converted to 'java.util.Date'");

    // when
    typeTransformer.transform(duration);
  }

  @Test
  public void shouldThrowExceptionDueToUnsupportedType_Period() {
    // given
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("date");

    Period period = Period.ofDays(5);

    // then
    thrown.expect(DmnEngineException.class);
    thrown.expectMessage("Unsupported type: 'java.time.Period' " +
      "cannot be converted to 'java.util.Date'");

    // when
    typeTransformer.transform(period);
  }

  @Test
  public void invalidStringForDateType() {
    DmnDataTypeTransformer typeTransformer = registry.getTransformer("date");

    thrown.expect(IllegalArgumentException.class);

    typeTransformer.transform("18.09.2015 12:00:00");
  }

  protected Date toDate(String date, String timeZone) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    if (timeZone != null) {
        format.setTimeZone(TimeZone.getTimeZone(timeZone));
    }

    try {
      return format.parse(date);

    } catch (ParseException e) {
      throw new RuntimeException(e);

    }
  }

}
