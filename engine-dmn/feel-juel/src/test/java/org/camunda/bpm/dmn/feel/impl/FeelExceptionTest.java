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
package org.camunda.bpm.dmn.feel.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.camunda.bpm.dmn.feel.impl.juel.FeelEngineFactoryImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FeelExceptionTest {

  public static final String INPUT_VARIABLE = "input";

  public static FeelEngine feelEngine;

  public VariableMap variables;

  @BeforeClass
  public static void initFeelEngine() {
    feelEngine = new FeelEngineFactoryImpl().createInstance();
  }

  @Test
  public void testSimpleExpressionNotSupported() {
    try {
      feelEngine.evaluateSimpleExpression("12 == 12", Variables.emptyVariableContext());
      failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
    }
    catch (UnsupportedOperationException e) {
      assertThat(e).hasMessageStartingWith("FEEL-01016");
    }
  }

  @Before
  public void initVariables() {
    variables = Variables.createVariables();
    variables.putValue(INPUT_VARIABLE, 13);
  }

  @Test
  public void testInvalidNot() {
    assertException("FEEL-01001",
      "not(",
      "not(2",
      "not(2, 12",
      "not(]",
      "not(("
      );
  }

  @Test
  public void testInvalidInterval() {
    assertException("FEEL-01002",
      "[1..3",
      "(1..3",
      "]1..3",
      "[1..3(",
      "(1..3(",
      "]1..3(",
      "[1..",
      "(1..",
      "]1..",
      "[1.3",
      "[1.3]",
      "[1.3)",
      "[1.3[",
      "(1.3",
      "(1.3]",
      "(1.3)",
      "(1.3[",
      "]1.3",
      "]1.3]",
      "]1.3)",
      "]1.3[",
      "(1.3",
      "(1.3]",
      "(1.3)",
      "(1.3[",
      "[1....3"
    );
  }

  @Test
  public void testInvalidComparison() {
    assertException("FEEL-01003",
      ">",
      ">=",
      "<",
      "<="
    );
  }

  @Test
  public void testUnknownMethod() {
    assertException("FEEL-01007",
      "unknown(12)",
      "not(unknown(12))",
      "12,13,unknown(12),14",
      "not(12,13,unknown(12),14)",
      "[12..unknown(12))",
      "not([12..unknown(12)))",
      "12,13,[12..unknown(12)),14",
      "not(12,13,[12..unknown(12)),14)"
    );
  }

  @Test
  public void testUnknownVariable() {
    assertException("FEEL-01009",
      "a",
      "not(a)",
      "12,13,a,14",
      "not(12,13,a,14)",
      "[12..a)",
      "not([12..a))",
      "12,13,[12..a),14",
      "not(12,13,[12..a),14)"
    );
  }

  @Test
  public void testInvalidSyntax() {
    assertException("FEEL-01010",
      "!= x",
      "== x",
      "=< 12",
      "=> 12",
      "< = 12",
      "> = 12",
      "1..3]",
      "1..3)",
      "1..3[",
      ")1..3",
      "1..3(",
      "[1....3]",
      "[1....3)",
      "[1....3[",
      "< [1..3)",
      ">= [1..3)",
      "${cellInput == 12}"
    );
  }

  @Test
  public void testUnableToConvertToBoolean() {
    variables.putValue(INPUT_VARIABLE, true);
    assertException("FEEL-01015",
      "''",
      "'camunda'",
      "12",
      "'true'",
      "\"false\""
    );
  }

  @Test
  public void testUnableToConvertToBigDecimal() {
    variables.putValue(INPUT_VARIABLE, BigDecimal.valueOf(10));
    assertException("FEEL-01015",
      "''",
      "< ''",
      "'camunda'",
      "< 'camunda'",
      "false",
      "< true",
      "'12'",
      "< '12'",
      "\"12\"",
      "< \"12\""
    );
  }

  @Test
  public void testUnableToConvertToBigInteger() {
    variables.putValue(INPUT_VARIABLE, BigInteger.valueOf(10));
    assertException("FEEL-01015",
      "''",
      "< ''",
      "'camunda'",
      "< 'camunda'",
      "false",
      "< true",
      "'12'",
      "< '12'",
      "\"12\"",
      "< \"12\""
    );
  }

  @Test
  public void testUnableToConvertToDouble() {
    variables.putValue(INPUT_VARIABLE, 10.0);
    assertException("FEEL-01015",
      "''",
      "< ''",
      "'camunda'",
      "< 'camunda'",
      "false",
      "< true",
      "'12.2'",
      "< '12.2'",
      "\"12.2\"",
      "< \"12.2\""
    );
  }

  @Test
  public void testUnableToConvertToLong() {
    variables.putValue(INPUT_VARIABLE, 10L);
    assertException("FEEL-01015",
      "''",
      "< ''",
      "'camunda'",
      "< 'camunda'",
      "false",
      "< true",
      "'12'",
      "< '12'",
      "\"12\"",
      "< \"12\""
    );
  }

  @Test
  public void testUnableToConvertToString() {
    variables.putValue(INPUT_VARIABLE, "camunda");
    assertException("FEEL-01015",
      "false",
      "< true",
      "12",
      "< 12"
    );
  }

  @Test
  public void testMissingInputVariable() {
    variables.remove(INPUT_VARIABLE);
    assertException("FEEL-01017",
      "false",
      "12",
      "< 12",
      "'Hello'"
    );
  }

  @Test
  public void testInvalidDateAndTimeFormat() {
    assertException("FEEL-01019",
      "date and time('camunda')",
      "date and time('2012-13-13')",
      "date and time('13:13:13')",
      "date and time('2012-12-12T25:00')"
    );
  }

  @Test
  public void testInvalidListFormat() {
    assertException("FEEL-01020",
      ",",
      "1,",
      "1,2,,3",
      ",1,2",
      "1,2,   ,3,4",
      "1,\t,2"
      );
  }

  public void assertException(String exceptionCode, String... feelExpressions) {
    for (String feelExpression : feelExpressions) {
      try {
        evaluateFeel(feelExpression);
        failBecauseExceptionWasNotThrown(FeelException.class);
      }
      catch (FeelException e) {
        assertThat(e).hasMessageStartingWith(exceptionCode);
        assertThat(e).hasMessageContaining(feelExpression);
        if (!feelExpression.startsWith("${")) {
          assertThat(e.getMessage()).doesNotContain("${");
        }
      }
    }
  }

  public boolean evaluateFeel(String feelExpression) {
    return feelEngine.evaluateSimpleUnaryTests(feelExpression, INPUT_VARIABLE, variables.asVariableContext());
  }

}
