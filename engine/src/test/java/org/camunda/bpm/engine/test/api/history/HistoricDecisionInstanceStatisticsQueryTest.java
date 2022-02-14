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
package org.camunda.bpm.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceStatisticsQuery;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;


/**
 * @author Askar Akhmerov
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricDecisionInstanceStatisticsQueryTest {

  protected static final String DISH_DRG_DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";
  protected static final String SCORE_DRG_DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";

  protected static final String NON_EXISTING = "fake";
  protected static final String DISH_DECISION = "dish-decision";
  protected static final String TEMPERATURE = "temperature";
  protected static final String DAY_TYPE = "dayType";
  protected static final String WEEKEND = "Weekend";

  protected DecisionService decisionService;
  protected RepositoryService repositoryService;
  protected HistoryService historyService;

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() {
    decisionService = engineRule.getDecisionService();
    repositoryService = engineRule.getRepositoryService();
    historyService = engineRule.getHistoryService();
    testRule.deploy(DISH_DRG_DMN);
  }

  @Test
  public void testStatisticForRootDecisionEvaluation() throws Exception {
    //when
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue(TEMPERATURE, 21).putValue(DAY_TYPE, WEEKEND))
        .evaluate();

    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue(TEMPERATURE, 11).putValue(DAY_TYPE, WEEKEND))
        .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    HistoricDecisionInstanceStatisticsQuery statisticsQuery = historyService
        .createHistoricDecisionInstanceStatisticsQuery(
            decisionRequirementsDefinition.getId());

    //then
    assertThat(statisticsQuery.count()).isEqualTo(3L);
    assertThat(statisticsQuery.list()).hasSize(3);
    assertThat(statisticsQuery.list().get(0).getEvaluations()).isEqualTo(2);
    assertThat(statisticsQuery.list().get(0).getDecisionDefinitionKey()).isNotNull();
  }

  @Test
  public void testStatisticForRootDecisionWithInstanceConstraintEvaluation() throws Exception {
    //when
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue(TEMPERATURE, 21).putValue(DAY_TYPE, WEEKEND))
        .evaluate();

    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue(TEMPERATURE, 11).putValue(DAY_TYPE, WEEKEND))
        .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();


    String decisionInstanceId = engineRule.getHistoryService()
        .createHistoricDecisionInstanceQuery()
        .decisionRequirementsDefinitionId(decisionRequirementsDefinition.getId())
        .rootDecisionInstancesOnly()
        .list()
        .get(0)
        .getId();

    HistoricDecisionInstanceStatisticsQuery query = historyService
        .createHistoricDecisionInstanceStatisticsQuery(
            decisionRequirementsDefinition.getId())
        .decisionInstanceId(decisionInstanceId);

    //then
    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.list()).hasSize(3);
    assertThat(query.list().get(0).getEvaluations()).isEqualTo(1);
    assertThat(query.list().get(0).getDecisionDefinitionKey()).isNotNull();
  }

  @Test
  public void testStatisticForRootDecisionWithFakeInstanceConstraintEvaluation() throws Exception {
    //when
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue(TEMPERATURE, 21).putValue(DAY_TYPE, WEEKEND))
        .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    HistoricDecisionInstanceStatisticsQuery query = historyService
        .createHistoricDecisionInstanceStatisticsQuery(
            decisionRequirementsDefinition.getId())
        .decisionInstanceId(NON_EXISTING);

    //then
    assertThat(query.count()).isEqualTo(0L);
    assertThat(query.list()).hasSize(0);


  }

  @Test
  public void testStatisticForRootDecisionWithNullInstanceConstraintEvaluation() throws Exception {
    //when
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue(TEMPERATURE, 21).putValue(DAY_TYPE, WEEKEND))
        .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();
    //when
    HistoricDecisionInstanceStatisticsQuery query = historyService
        .createHistoricDecisionInstanceStatisticsQuery(
            decisionRequirementsDefinition.getId())
        .decisionInstanceId(null);

    //then
    try {
      query.count();
    } catch (NullValueException e) {
      //expected
    }

    try {
      query.list();
    } catch (NullValueException e) {
      //expected
    }
  }

  @Test
  public void testStatisticForChildDecisionEvaluation() throws Exception {
    //when
    decisionService.evaluateDecisionTableByKey("season")
        .variables(Variables.createVariables().putValue(TEMPERATURE, 21))
        .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    HistoricDecisionInstanceStatisticsQuery statisticsQuery = historyService
        .createHistoricDecisionInstanceStatisticsQuery(
            decisionRequirementsDefinition.getId());

    //then
    assertThat(statisticsQuery.count()).isEqualTo(1L);
    assertThat(statisticsQuery.list()).hasSize(1);
    assertThat(statisticsQuery.list().get(0).getEvaluations()).isEqualTo(1);
    assertThat(statisticsQuery.list().get(0).getDecisionDefinitionKey()).isNotNull();
  }

  @Test
  public void testStatisticConstrainedToOneDRD() throws Exception {
    //given
    testRule.deploy(SCORE_DRG_DMN);

    //when
    decisionService.evaluateDecisionTableByKey("score-decision")
        .variables(Variables.createVariables().putValue("input", "john"))
        .evaluate();

    decisionService.evaluateDecisionTableByKey("season")
        .variables(Variables.createVariables().putValue(TEMPERATURE, 21))
        .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionName("Score")
        .singleResult();

    HistoricDecisionInstanceStatisticsQuery statisticsQuery = historyService
        .createHistoricDecisionInstanceStatisticsQuery(
            decisionRequirementsDefinition.getId());

    //then
    assertThat(statisticsQuery.count()).isEqualTo(1L);
    assertThat(statisticsQuery.list()).hasSize(1);
    assertThat(statisticsQuery.list().get(0).getEvaluations()).isEqualTo(1);
    assertThat(statisticsQuery.list().get(0).getDecisionDefinitionKey()).isNotNull();
  }

  @Test
  public void testStatisticDoesNotExistForFakeId() throws Exception {
    assertThat(
        historyService.createHistoricDecisionInstanceStatisticsQuery(
            NON_EXISTING).count()).isEqualTo(0L);

    assertThat(
        historyService.createHistoricDecisionInstanceStatisticsQuery(
            NON_EXISTING).list().size()).isEqualTo(0);

  }

  @Test
  public void testStatisticThrowsExceptionOnNullConstraintsCount() throws Exception {
    // when/then
    assertThatThrownBy(() -> historyService.createHistoricDecisionInstanceStatisticsQuery(null).count())
      .isInstanceOf(NullValueException.class);
  }

  @Test
  public void testStatisticThrowsExceptionOnNullConstraintsList() throws Exception {
    // when/then
    assertThatThrownBy(() -> historyService.createHistoricDecisionInstanceStatisticsQuery(null).list())
      .isInstanceOf(NullValueException.class);
  }

  @Test
  public void testStatisticForNotEvaluatedDRD() throws Exception {
    //when
    DecisionRequirementsDefinition decisionRequirementsDefinition =
        repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    HistoricDecisionInstanceStatisticsQuery statisticsQuery = historyService.createHistoricDecisionInstanceStatisticsQuery(
        decisionRequirementsDefinition.getId());

    //then
    assertThat(statisticsQuery.count()).isEqualTo(0L);
    assertThat(statisticsQuery.list()).hasSize(0);
  }
}