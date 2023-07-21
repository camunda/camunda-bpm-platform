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
package org.camunda.bpm.engine.test.history.dmn;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricDecisionInstanceReturnBlankTableOutputAsNullTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule =
      new ProcessEngineBootstrapRule(config -> config.setDmnReturnBlankTableOutputAsNull(true));

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  public static final String RESULT_TEST_DMN = "org/camunda/bpm/engine/test/history/ReturnBlankTableOutputAsNull.dmn";

  @Test
  @Deployment(resources = RESULT_TEST_DMN)
  public void shouldReturnNullWhenExpressionIsNull() {
    // given

    // when
    engineRule.getDecisionService().evaluateDecisionByKey("Decision_0vmcc71")
        .variables(Variables.putValue("name", "A"))
        .evaluate();

    // then
    HistoricDecisionInstance historicDecisionInstance = engineRule.getProcessEngine()
        .getHistoryService()
        .createHistoricDecisionInstanceQuery()
        .includeOutputs()
        .singleResult();

    assertThat(historicDecisionInstance.getOutputs())
        .extracting("variableName", "value")
        .containsOnly(tuple("output", null));
  }

  @Test
  @Deployment(resources = RESULT_TEST_DMN)
  public void shouldReturnNullWhenTextTagEmpty() {
    // given

    // when
    engineRule.getDecisionService().evaluateDecisionByKey("Decision_0vmcc71")
        .variables(Variables.putValue("name", "B"))
        .evaluate();

    // then
    HistoricDecisionInstance historicDecisionInstance = engineRule.getProcessEngine()
        .getHistoryService()
        .createHistoricDecisionInstanceQuery()
        .includeOutputs()
        .singleResult();

    assertThat(historicDecisionInstance.getOutputs())
        .extracting("variableName", "value")
        .containsOnly(tuple("output", null));
  }

  @Test
  @Deployment(resources = RESULT_TEST_DMN)
  public void shouldReturnEmpty() {
    // given

    // when
    engineRule.getDecisionService().evaluateDecisionByKey("Decision_0vmcc71")
        .variables(Variables.putValue("name", "C"))
        .evaluate();

    // then
    HistoricDecisionInstance historicDecisionInstance = engineRule.getProcessEngine()
        .getHistoryService()
        .createHistoricDecisionInstanceQuery()
        .includeOutputs()
        .singleResult();

    assertThat(historicDecisionInstance.getOutputs())
        .extracting("variableName", "value")
        .containsOnly(tuple("output", ""));
  }

  @Test
  @Deployment(resources = RESULT_TEST_DMN)
  public void shouldReturnNullWhenOutputEntryEmpty() {
    // given

    // when
    engineRule.getDecisionService().evaluateDecisionByKey("Decision_0vmcc71")
        .variables(Variables.putValue("name", "D"))
        .evaluate();

    // then
    HistoricDecisionInstance historicDecisionInstance = engineRule.getProcessEngine()
        .getHistoryService()
        .createHistoricDecisionInstanceQuery()
        .includeOutputs()
        .singleResult();

    assertThat(historicDecisionInstance.getOutputs())
        .extracting("variableName", "value")
        .containsOnly(tuple("output", null));
  }

}
