/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.dmn.feel.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Date;

import org.camunda.bpm.dmn.feel.FeelEngine;
import org.camunda.bpm.dmn.feel.impl.el.FeelFunctionMapper;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.LocalDateValue;
import org.camunda.bpm.engine.variable.value.LocalTimeValue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class FeelEngineTest {

  public static final String INPUT_VARIABLE = "input";

  public static FeelEngine feelEngine;

  public VariableMap variables;

  @BeforeClass
  public static void initFeelEngine() {
    feelEngine = new FeelEngineProviderImpl().createInstance();
  }

  @Before
  public void initVariables() {
    variables = Variables.createVariables();
  }

  @Test
  public void testLong() {
    variables.putValue("integer", 12);
    variables.putValue("primitive", 12L);
    variables.putValue("typed", Variables.longValue(12L));

    assertTrue(Variables.longValue(12L), "<= typed");
    assertTrue(Variables.longValue(12L), "<= primitive");
    assertTrue(Variables.longValue(12L), "<= integer");
  }

  @Test
  public void testEndpointString() {
    assertTrue("Hello World", "\"Hello World\"");
    assertFalse("Hello World", "\"Hello Camunda\"");
    assertFalse("Hello World", "\"\"");
    assertTrue("", "\"\"");
    assertTrue("123", "\"123\"");
    assertTrue("Why.not?", "\"Why.not?\"");
  }

  @Test
  public void testEndpointVariable() {
    variables.put("y", "a");
    assertTrue("a", "y");
    assertFalse("b", "y");

    variables.put("customer", Collections.singletonMap("name", "camunda"));
    assertTrue("camunda", "customer.name");
    assertFalse("hello", "customer.name");
  }

  @Test
  public void testEndpointVariableGreater() {
    variables.put("y", 13.37);
    assertTrue(12, "<y");
    assertFalse(13.38, "<y");
  }

  @Test
  public void testEndpointVariableGreaterEqual() {
    variables.put("y", 13.37);
    assertTrue(12, "<=y");
    assertTrue(13.37, "<=y");
    assertFalse(13.38, "<=y");
  }

  @Test
  public void testEndpointVariableLess() {
    variables.put("y", 13.37);
    assertFalse(12, ">y");
    assertTrue(13.38, ">y");
  }

  @Test
  public void testEndpointVariableLessEqual() {
    variables.put("y", 13.37);
    assertFalse(12, ">=y");
    assertTrue(13.37, ">=y");
    assertTrue(13.38, ">=y");
  }

  @Test
  public void testEndpointBoolean() {
    assertTrue(true, "true");
    assertFalse(true, "false");
    assertTrue(false, "false");
    assertFalse(false, "true");
  }

  @Test
  public void testEndpointNumber() {
    assertTrue(13, "13");
    assertTrue(13.37, "13.37");
    assertTrue(0.37, ".37");
    assertFalse(13.37, "23.42");
    assertFalse(0.42, ".37");
  }

  @Test
  public void testEndpointNumberGreater() {
    assertTrue(12, "<13");
    assertTrue(13.35, "<13.37");
    assertTrue(0.337, "<.37");
    assertFalse(13.37, "<13.37");
    assertFalse(0.37, "<.37");
  }

  @Test
  public void testEndpointNumberGreaterEqual() {
    assertTrue(13.37, "<=13.37");
    assertTrue(13.337, "<=13.37");
    assertTrue(0.37, "<=.37");
    assertTrue(0.337, "<=.37");
    assertFalse(13.42, "<=13.37");
    assertFalse(0.42, "<=.37");
  }

  @Test
  public void testEndpointNumberLess() {
    assertTrue(13.37, ">13");
    assertTrue(13.42, ">13.37");
    assertTrue(0.42, ">.37");
    assertFalse(13.37, ">13.37");
    assertFalse(0.37, ">.37");
  }

  @Test
  public void testEndpointNumberLessEqual() {
    assertTrue(13.37, ">=13");
    assertTrue(13.37, ">=13.37");
    assertTrue(0.37, ">=.37");
    assertTrue(0.42, ">=.37");
    assertFalse(13.337, ">=13.37");
    assertFalse(0.23, ">=.37");
  }

  @Test
  @Ignore
  public void testEndpointDate() {
    LocalDateValue date = parseDate("2015-12-12");
    assertTrue(date, "date(\"2015-12-12\")");

    variables.put("y", "2015-12-12");
    assertTrue(date, "date(y)");
  }

  @Test
  @Ignore
  public void testEndpointTime() {
    LocalTimeValue time = parseTime("22:12:53");

    assertTrue(time, "time(\"22:12:53\")");

    variables.put("y", "22:12:53");
    assertTrue(time, "time(y)");
  }

  @Test
  public void testEndpointDateAndTime() {
    DateValue dateTime = parseDateTime("2015-12-12T22:12:53");

    assertTrue(dateTime, "date and time(\"2015-12-12T22:12:53\")");

    variables.put("y", "2015-12-12T22:12:53");
    assertTrue(dateTime, "date and time(y)");
  }

  @Test
  public void testIntervalNumber() {
    assertTrue(0.23, "[.12...37]");
    assertTrue(0.23, "[.12...37)");
    assertTrue(0.23, "[.12...37[");

    assertTrue(0.23, "(.12...37]");
    assertTrue(0.23, "(.12...37)");
    assertTrue(0.23, "(.12...37[");

    assertTrue(0.23, "].12...37]");
    assertTrue(0.23, "].12...37)");
    assertTrue(0.23, "].12...37[");

    assertFalse(13.37, "[.12...37]");
    assertFalse(13.37, "[.12...37)");
    assertFalse(13.37, "[.12...37[");

    assertFalse(13.37, "(.12...37]");
    assertFalse(13.37, "(.12...37)");
    assertFalse(13.37, "(.12...37[");

    assertFalse(13.37, "].12...37]");
    assertFalse(13.37, "].12...37)");
    assertFalse(13.37, "].12...37[");
  }

  @Test
  public void testIntervalVariable() {
    variables.put("a", 10);
    variables.put("b", 15);

    assertTrue(13.37, "[a..b]");
    assertTrue(13.37, "[a..b)");
    assertTrue(13.37, "[a..b[");

    assertTrue(13.37, "(a..b]");
    assertTrue(13.37, "(a..b)");
    assertTrue(13.37, "(a..b[");

    assertTrue(13.37, "]a..b]");
    assertTrue(13.37, "]a..b)");
    assertTrue(13.37, "]a..b[");

    assertFalse(0.37, "[a..b]");
    assertFalse(0.37, "[a..b)");
    assertFalse(0.37, "[a..b[");

    assertFalse(0.37, "(a..b]");
    assertFalse(0.37, "(a..b)");
    assertFalse(0.37, "(a..b[");

    assertFalse(0.37, "]a..b]");
    assertFalse(0.37, "]a..b)");
    assertFalse(0.37, "]a..b[");
  }

  @Test
  @Ignore
  public void testIntervalDate() {
    LocalDateValue date = parseDate("2016-03-03");
    assertTrue(date, "[date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertTrue(date, "[date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertTrue(date, "[date(\"2015-12-12\")..date(\"2016-06-06\")[");

    assertTrue(date, "(date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertTrue(date, "(date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertTrue(date, "(date(\"2015-12-12\")..date(\"2016-06-06\")[");

    assertTrue(date, "]date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertTrue(date, "]date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertTrue(date, "]date(\"2015-12-12\")..date(\"2016-06-06\")[");

    date = parseDate("2013-03-03");
    assertFalse(date, "[date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertFalse(date, "[date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertFalse(date, "[date(\"2015-12-12\")..date(\"2016-06-06\")[");

    assertFalse(date, "(date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertFalse(date, "(date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertFalse(date, "(date(\"2015-12-12\")..date(\"2016-06-06\")[");

    assertFalse(date, "]date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertFalse(date, "]date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertFalse(date, "]date(\"2015-12-12\")..date(\"2016-06-06\")[");
  }

  @Test
  public void testNot() {
    variables.put("y", 13.37);

    assertTrue("Hello camunda", "not(\"Hello World\")");
    assertTrue(0.37, "not(y)");
    assertFalse(0.37, "not(<y)");
    assertFalse(0.37, "not(<=y)");
    assertTrue(0.37, "not(>y)");
    assertTrue(0.37, "not(>=y)");
    assertTrue(0.37, "not(13.37)");
    assertFalse(0.37, "not(<13.37)");
    assertFalse(0.37, "not(<=13.37)");
    assertTrue(0.37, "not(>13.37)");
    assertTrue(0.37, "not(>=13.37)");
    assertFalse(0.37, "not(.37)");
    assertTrue(0.37, "not(<.37)");
    assertFalse(0.37, "not(<=.37)");
    assertTrue(0.37, "not(>.37)");
    assertFalse(0.37, "not(>=.37)");
  }

  @Test
  public void testList() {
    variables.put("a", "Hello camunda");
    variables.put("y", 0);

    assertTrue("Hello World", "a,\"Hello World\"");
    assertTrue("Hello camunda", "a,\"Hello World\"");
    assertFalse("Hello unknown", "a,\"Hello World\"");
    assertTrue(0, "y,12,13.37,.37");
    assertTrue(12, "y,12,13.37,.37");
    assertTrue(13.37, "y,12,13.37,.37");
    assertTrue(0.37, "y,12,13.37,.37");
    assertFalse(0.23, "y,12,13.37,.37");
    assertTrue(-1, "<y,>13.37,>=.37");
    assertTrue(0.37, "<y,>13.37,>=.37");
    assertFalse(0, "<y,>13.37,>=.37");
  }

  @Test
  public void testNested() {
    variables.put("a", 23.42);
    assertTrue(0.37, "not(>=a,13.37,].37...42),<.37)");
    assertFalse(23.42, "not(>=a,13.37,].37...42),<.37)");
    assertFalse(13.37, "not(>=a,13.37,].37...42),<.37)");
    assertFalse(0.38, "not(>=a,13.37,].37...42),<.37)");
    assertFalse(0, "not(>=a,13.37,].37...42),<.37)");
  }

  @Test
  public void testDontCare() {
    assertTrue(13.37, "-");
  }

  @Test
  public void testWhitespace() {
    assertTrue("Hello World", "'Hello World' ");
    assertTrue("Hello World", " 'Hello World'");
    assertTrue("Hello World", " 'Hello World' ");
    assertTrue(12, " 12 ");
    assertTrue(10.2, " <12 ");
    assertTrue(0, "< 12 ");
    assertTrue(12.3, "\t>=12 ");
    assertTrue(0, " not( 13 ,\t>0)\t");
  }

  public void assertTrue(Object input, String feelExpression) {
    boolean result = evaluateFeel(input, feelExpression);
    assertThat(result).isTrue();
  }

  public void assertFalse(Object input, String feelExpression) {
    boolean result = evaluateFeel(input, feelExpression);
    assertThat(result).isFalse();
  }

  public boolean evaluateFeel(Object input, String feelExpression) {
    variables.putValue(INPUT_VARIABLE, input);
    return feelEngine.evaluateSimpleUnaryTests(feelExpression, INPUT_VARIABLE, variables);
  }

  protected LocalDateValue parseDate(String dateString) {
    return Variables.localDateValue(dateString);
  }

  protected LocalTimeValue parseTime(String timeString) {
    return Variables.localTimeValue(timeString);
  }

  protected DateValue parseDateTime(String dateTimeString) {
    Date date = FeelFunctionMapper.parseDateTime(dateTimeString);
    return Variables.dateValue(date);
  }

}
