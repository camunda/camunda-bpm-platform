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
package org.camunda.bpm.dmn.engine.feel;

import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.feel.integration.CamundaFeelEngineFactory;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.dmn.engine.feel.FeelBehaviorTest.DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION;

public class NiceToHaveFeelBehaviorTest extends DmnEngineTest {

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.setFeelEngineFactory(new CamundaFeelEngineFactory());
    return configuration;
  }

  /*
    - Does not work on JUEL because of absence of FEEL in literal expressions
    - Could be enabled by extending transformer
   */
  @Test
  @DecisionResource(resource = "DateConversionLit.dmn11.xml")
  public void shouldEvaltuateToUtilDateWithLiteralExpression() {
    // given
    getVariables()
      .putValue("date1", new Date());

    // when
    Object foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isInstanceOf(Date.class);
  }

  /* Could be enabled by extending transformer */
  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION)
  public void shouldEvaluateJodaDateWithTable_InputClauseTypeDate() {
    // given
    getVariables()
      .putValue("date1", org.joda.time.LocalDate.now());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  /*
  - Does not work on JUEL because of absence of FEEL in output expressions
  - Could be enabled by extending transformer
  */
  @Test
  @DecisionResource(resource = "DateConversionTable_OutputClauseDateBuiltinFunction.dmn11.dmn")
  public void shouldEvaluateOutputClause() {
    // given

    // when
    Date foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("2019-08-08T22:22:22");
  }

}
