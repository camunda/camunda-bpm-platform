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

    assertEvaluatesToTrue(Variables.longValue(12L), "<= typed");
    assertEvaluatesToTrue(Variables.longValue(12L), "<= primitive");
    assertEvaluatesToTrue(Variables.longValue(12L), "<= integer");
  }

  @Test
  public void testEndpointString() {
    assertEvaluatesToTrue("Hello World", "\"Hello World\"");
    assertEvaluatesToFalse("Hello World", "\"Hello Camunda\"");
    assertEvaluatesToFalse("Hello World", "\"\"");
    assertEvaluatesToTrue("", "\"\"");
    assertEvaluatesToTrue("123", "\"123\"");
    assertEvaluatesToTrue("Why.not?", "\"Why.not?\"");
  }

  @Test
  public void testEndpointVariable() {
    variables.put("y", "a");
    assertEvaluatesToTrue("a", "y");
    assertEvaluatesToFalse("b", "y");

    variables.put("customer", Collections.singletonMap("name", "camunda"));
    assertEvaluatesToTrue("camunda", "customer.name");
    assertEvaluatesToFalse("hello", "customer.name");
  }

  @Test
  public void testEndpointVariableGreater() {
    variables.put("y", 13.37);
    assertEvaluatesToTrue(12, "<y");
    assertEvaluatesToFalse(13.38, "<y");
  }

  @Test
  public void testEndpointVariableGreaterEqual() {
    variables.put("y", 13.37);
    assertEvaluatesToTrue(12, "<=y");
    assertEvaluatesToTrue(13.37, "<=y");
    assertEvaluatesToFalse(13.38, "<=y");
  }

  @Test
  public void testEndpointVariableLess() {
    variables.put("y", 13.37);
    assertEvaluatesToFalse(12, ">y");
    assertEvaluatesToTrue(13.38, ">y");
  }

  @Test
  public void testEndpointVariableLessEqual() {
    variables.put("y", 13.37);
    assertEvaluatesToFalse(12, ">=y");
    assertEvaluatesToTrue(13.37, ">=y");
    assertEvaluatesToTrue(13.38, ">=y");
  }

  @Test
  public void testEndpointBoolean() {
    assertEvaluatesToTrue(true, "true");
    assertEvaluatesToFalse(true, "false");
    assertEvaluatesToTrue(false, "false");
    assertEvaluatesToFalse(false, "true");
  }

  @Test
  public void testEndpointNumber() {
    assertEvaluatesToTrue(13, "13");
    assertEvaluatesToTrue(13.37, "13.37");
    assertEvaluatesToTrue(0.37, ".37");
    assertEvaluatesToFalse(13.37, "23.42");
    assertEvaluatesToFalse(0.42, ".37");
  }

  @Test
  public void testEndpointNumberGreater() {
    assertEvaluatesToTrue(12, "<13");
    assertEvaluatesToTrue(13.35, "<13.37");
    assertEvaluatesToTrue(0.337, "<.37");
    assertEvaluatesToFalse(13.37, "<13.37");
    assertEvaluatesToFalse(0.37, "<.37");
  }

  @Test
  public void testEndpointNumberGreaterEqual() {
    assertEvaluatesToTrue(13.37, "<=13.37");
    assertEvaluatesToTrue(13.337, "<=13.37");
    assertEvaluatesToTrue(0.37, "<=.37");
    assertEvaluatesToTrue(0.337, "<=.37");
    assertEvaluatesToFalse(13.42, "<=13.37");
    assertEvaluatesToFalse(0.42, "<=.37");
  }

  @Test
  public void testEndpointNumberLess() {
    assertEvaluatesToTrue(13.37, ">13");
    assertEvaluatesToTrue(13.42, ">13.37");
    assertEvaluatesToTrue(0.42, ">.37");
    assertEvaluatesToFalse(13.37, ">13.37");
    assertEvaluatesToFalse(0.37, ">.37");
  }

  @Test
  public void testEndpointNumberLessEqual() {
    assertEvaluatesToTrue(13.37, ">=13");
    assertEvaluatesToTrue(13.37, ">=13.37");
    assertEvaluatesToTrue(0.37, ">=.37");
    assertEvaluatesToTrue(0.42, ">=.37");
    assertEvaluatesToFalse(13.337, ">=13.37");
    assertEvaluatesToFalse(0.23, ">=.37");
  }

  @Test
  @Ignore
  public void testEndpointDate() {
    LocalDateValue date = parseDate("2015-12-12");
    assertEvaluatesToTrue(date, "date(\"2015-12-12\")");

    variables.put("y", "2015-12-12");
    assertEvaluatesToTrue(date, "date(y)");
  }

  @Test
  @Ignore
  public void testEndpointTime() {
    LocalTimeValue time = parseTime("22:12:53");

    assertEvaluatesToTrue(time, "time(\"22:12:53\")");

    variables.put("y", "22:12:53");
    assertEvaluatesToTrue(time, "time(y)");
  }

  @Test
  public void testEndpointDateAndTime() {
    DateValue dateTime = parseDateTime("2015-12-12T22:12:53");

    assertEvaluatesToTrue(dateTime, "date and time(\"2015-12-12T22:12:53\")");

    variables.put("y", "2015-12-12T22:12:53");
    assertEvaluatesToTrue(dateTime, "date and time(y)");
  }

  @Test
  public void testIntervalNumber() {
    assertEvaluatesToTrue(0.23, "[.12...37]");
    assertEvaluatesToTrue(0.23, "[.12...37)");
    assertEvaluatesToTrue(0.23, "[.12...37[");

    assertEvaluatesToTrue(0.23, "(.12...37]");
    assertEvaluatesToTrue(0.23, "(.12...37)");
    assertEvaluatesToTrue(0.23, "(.12...37[");

    assertEvaluatesToTrue(0.23, "].12...37]");
    assertEvaluatesToTrue(0.23, "].12...37)");
    assertEvaluatesToTrue(0.23, "].12...37[");

    assertEvaluatesToFalse(13.37, "[.12...37]");
    assertEvaluatesToFalse(13.37, "[.12...37)");
    assertEvaluatesToFalse(13.37, "[.12...37[");

    assertEvaluatesToFalse(13.37, "(.12...37]");
    assertEvaluatesToFalse(13.37, "(.12...37)");
    assertEvaluatesToFalse(13.37, "(.12...37[");

    assertEvaluatesToFalse(13.37, "].12...37]");
    assertEvaluatesToFalse(13.37, "].12...37)");
    assertEvaluatesToFalse(13.37, "].12...37[");
  }

  @Test
  public void testIntervalVariable() {
    variables.put("a", 10);
    variables.put("b", 15);

    assertEvaluatesToTrue(13.37, "[a..b]");
    assertEvaluatesToTrue(13.37, "[a..b)");
    assertEvaluatesToTrue(13.37, "[a..b[");

    assertEvaluatesToTrue(13.37, "(a..b]");
    assertEvaluatesToTrue(13.37, "(a..b)");
    assertEvaluatesToTrue(13.37, "(a..b[");

    assertEvaluatesToTrue(13.37, "]a..b]");
    assertEvaluatesToTrue(13.37, "]a..b)");
    assertEvaluatesToTrue(13.37, "]a..b[");

    assertEvaluatesToFalse(0.37, "[a..b]");
    assertEvaluatesToFalse(0.37, "[a..b)");
    assertEvaluatesToFalse(0.37, "[a..b[");

    assertEvaluatesToFalse(0.37, "(a..b]");
    assertEvaluatesToFalse(0.37, "(a..b)");
    assertEvaluatesToFalse(0.37, "(a..b[");

    assertEvaluatesToFalse(0.37, "]a..b]");
    assertEvaluatesToFalse(0.37, "]a..b)");
    assertEvaluatesToFalse(0.37, "]a..b[");
  }

  @Test
  @Ignore
  public void testIntervalDate() {
    LocalDateValue date = parseDate("2016-03-03");
    assertEvaluatesToTrue(date, "[date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertEvaluatesToTrue(date, "[date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertEvaluatesToTrue(date, "[date(\"2015-12-12\")..date(\"2016-06-06\")[");

    assertEvaluatesToTrue(date, "(date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertEvaluatesToTrue(date, "(date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertEvaluatesToTrue(date, "(date(\"2015-12-12\")..date(\"2016-06-06\")[");

    assertEvaluatesToTrue(date, "]date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertEvaluatesToTrue(date, "]date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertEvaluatesToTrue(date, "]date(\"2015-12-12\")..date(\"2016-06-06\")[");

    date = parseDate("2013-03-03");
    assertEvaluatesToFalse(date, "[date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertEvaluatesToFalse(date, "[date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertEvaluatesToFalse(date, "[date(\"2015-12-12\")..date(\"2016-06-06\")[");

    assertEvaluatesToFalse(date, "(date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertEvaluatesToFalse(date, "(date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertEvaluatesToFalse(date, "(date(\"2015-12-12\")..date(\"2016-06-06\")[");

    assertEvaluatesToFalse(date, "]date(\"2015-12-12\")..date(\"2016-06-06\")]");
    assertEvaluatesToFalse(date, "]date(\"2015-12-12\")..date(\"2016-06-06\"))");
    assertEvaluatesToFalse(date, "]date(\"2015-12-12\")..date(\"2016-06-06\")[");
  }

  @Test
  public void testNot() {
    variables.put("y", 13.37);

    assertEvaluatesToTrue("Hello camunda", "not(\"Hello World\")");
    assertEvaluatesToTrue(0.37, "not(y)");
    assertEvaluatesToFalse(0.37, "not(<y)");
    assertEvaluatesToFalse(0.37, "not(<=y)");
    assertEvaluatesToTrue(0.37, "not(>y)");
    assertEvaluatesToTrue(0.37, "not(>=y)");
    assertEvaluatesToTrue(0.37, "not(13.37)");
    assertEvaluatesToFalse(0.37, "not(<13.37)");
    assertEvaluatesToFalse(0.37, "not(<=13.37)");
    assertEvaluatesToTrue(0.37, "not(>13.37)");
    assertEvaluatesToTrue(0.37, "not(>=13.37)");
    assertEvaluatesToFalse(0.37, "not(.37)");
    assertEvaluatesToTrue(0.37, "not(<.37)");
    assertEvaluatesToFalse(0.37, "not(<=.37)");
    assertEvaluatesToTrue(0.37, "not(>.37)");
    assertEvaluatesToFalse(0.37, "not(>=.37)");
  }

  @Test
  public void testList() {
    variables.put("a", "Hello camunda");
    variables.put("y", 0);

    assertEvaluatesToTrue("Hello World", "a,\"Hello World\"");
    assertEvaluatesToTrue("Hello camunda", "a,\"Hello World\"");
    assertEvaluatesToFalse("Hello unknown", "a,\"Hello World\"");
    assertEvaluatesToTrue(0, "y,12,13.37,.37");
    assertEvaluatesToTrue(12, "y,12,13.37,.37");
    assertEvaluatesToTrue(13.37, "y,12,13.37,.37");
    assertEvaluatesToTrue(0.37, "y,12,13.37,.37");
    assertEvaluatesToFalse(0.23, "y,12,13.37,.37");
    assertEvaluatesToTrue(-1, "<y,>13.37,>=.37");
    assertEvaluatesToTrue(0.37, "<y,>13.37,>=.37");
    assertEvaluatesToFalse(0, "<y,>13.37,>=.37");
  }

  @Test
  public void testNested() {
    variables.put("a", 23.42);
    assertEvaluatesToTrue(0.37, "not(>=a,13.37,].37...42),<.37)");
    assertEvaluatesToFalse(23.42, "not(>=a,13.37,].37...42),<.37)");
    assertEvaluatesToFalse(13.37, "not(>=a,13.37,].37...42),<.37)");
    assertEvaluatesToFalse(0.38, "not(>=a,13.37,].37...42),<.37)");
    assertEvaluatesToFalse(0, "not(>=a,13.37,].37...42),<.37)");
  }

  @Test
  public void testDontCare() {
    assertEvaluatesToTrue(13.37, "-");
  }

  @Test
  public void testWhitespace() {
    assertEvaluatesToTrue("Hello World", "'Hello World' ");
    assertEvaluatesToTrue("Hello World", " 'Hello World'");
    assertEvaluatesToTrue("Hello World", " 'Hello World' ");
    assertEvaluatesToTrue(12, " 12 ");
    assertEvaluatesToTrue(10.2, " <12 ");
    assertEvaluatesToTrue(0, "< 12 ");
    assertEvaluatesToTrue(12.3, "\t>=12 ");
    assertEvaluatesToTrue(0, " not( 13 ,\t>0)\t");
  }

  public void assertEvaluatesToTrue(Object input, String feelExpression) {
    boolean result = evaluateFeel(input, feelExpression);
    assertThat(result).isTrue();
  }

  public void assertEvaluatesToFalse(Object input, String feelExpression) {
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
