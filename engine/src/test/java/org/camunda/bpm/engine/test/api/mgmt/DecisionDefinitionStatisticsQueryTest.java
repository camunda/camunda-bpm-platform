/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.test.api.mgmt;

import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.management.DecisionDefinitionStatisticsQuery;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class DecisionDefinitionStatisticsQueryTest {

  protected static final String DISH_DRG_DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";
  protected static final String SCORE_DRG_DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";

  protected static final String FAKE = "fake";
  protected static final String DISH_DECISION = "dish-decision";
  protected static final String TEMPERATURE = "temperature";
  protected static final String DAY_TYPE = "dayType";
  protected static final String WEEKEND = "Weekend";

  protected DecisionService decisionService;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() {
    decisionService = engineRule.getDecisionService();
    repositoryService = engineRule.getRepositoryService();
    managementService = engineRule.getManagementService();
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

    DecisionDefinitionStatisticsQuery statisticsQuery = managementService
        .createDecisionRequirementsDefinitionStatisticsQuery(
            decisionRequirementsDefinition.getId());

    //then
    assertThat(statisticsQuery.count(), is(3L));
    assertThat(statisticsQuery.list().size(), is(3));
    assertThat(statisticsQuery.list().get(0).getEvaluations(), is(2));
    assertThat(statisticsQuery.list().get(0).getDecisionDefinitionKey(), is(notNullValue()));
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

    DecisionDefinitionStatisticsQuery query = managementService
        .createDecisionRequirementsDefinitionStatisticsQuery(
            decisionRequirementsDefinition.getId())
        .decisionInstanceId(decisionInstanceId);

    //then
    assertThat(query.count(), is(3L));
    assertThat(query.list().size(), is(3));
    assertThat(query.list().get(0).getEvaluations(), is(1));
    assertThat(query.list().get(0).getDecisionDefinitionKey(), is(notNullValue()));
  }

  @Test
  public void testStatisticForRootDecisionWithFakeInstanceConstraintEvaluation() throws Exception {
    //when
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue(TEMPERATURE, 21).putValue(DAY_TYPE, WEEKEND))
        .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    DecisionDefinitionStatisticsQuery query = managementService
        .createDecisionRequirementsDefinitionStatisticsQuery(
            decisionRequirementsDefinition.getId())
        .decisionInstanceId(FAKE);

    //then
    assertThat(query.count(), is(0L));
    assertThat(query.list().size(), is(0));

    //when
    query = managementService
        .createDecisionRequirementsDefinitionStatisticsQuery(
            decisionRequirementsDefinition.getId())
        .decisionInstanceId(null);

    //then
    assertThat(query.count(), is(0L));
    assertThat(query.list().size(), is(0));
  }

  @Test
  public void testStatisticForChildDecisionEvaluation() throws Exception {
    //when
    decisionService.evaluateDecisionTableByKey("season")
        .variables(Variables.createVariables().putValue(TEMPERATURE, 21))
        .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    DecisionDefinitionStatisticsQuery statisticsQuery = managementService
        .createDecisionRequirementsDefinitionStatisticsQuery(
            decisionRequirementsDefinition.getId());

    //then
    assertThat(statisticsQuery.count(), is(1L));
    assertThat(statisticsQuery.list().size(), is(1));
    assertThat(statisticsQuery.list().get(0).getEvaluations(), is(1));
    assertThat(statisticsQuery.list().get(0).getDecisionDefinitionKey(), is(notNullValue()));
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

    DecisionDefinitionStatisticsQuery statisticsQuery = managementService
        .createDecisionRequirementsDefinitionStatisticsQuery(
            decisionRequirementsDefinition.getId());

    //then
    assertThat(statisticsQuery.count(), is(1L));
    assertThat(statisticsQuery.list().size(), is(1));
    assertThat(statisticsQuery.list().get(0).getEvaluations(), is(1));
    assertThat(statisticsQuery.list().get(0).getDecisionDefinitionKey(), is(notNullValue()));
  }

  @Test
  public void testStatisticDoesNotExistForFakeId() throws Exception {
    assertThat(
        "available statistics count of fake",
        managementService.createDecisionRequirementsDefinitionStatisticsQuery(
            FAKE).count(), is(0L));

    assertThat(
        "available statistics count of null",
        managementService.createDecisionRequirementsDefinitionStatisticsQuery(
            null).count(), is(0L));

    assertThat(
        "available statistics elements of fake",
        managementService.createDecisionRequirementsDefinitionStatisticsQuery(
            FAKE).list().size(), is(0));

    assertThat(
        "available statistics elements of null",
        managementService.createDecisionRequirementsDefinitionStatisticsQuery(
            null).list().size(), is(0));
  }

  @Test
  public void testStatisticForNotEvaluatedDRD() throws Exception {
    //when
    DecisionRequirementsDefinition decisionRequirementsDefinition =
        repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    DecisionDefinitionStatisticsQuery statisticsQuery = managementService.createDecisionRequirementsDefinitionStatisticsQuery(
        decisionRequirementsDefinition.getId());

    //then
    assertThat("available statistics count", statisticsQuery.count(), is(1L));
    assertThat("available statistics elements", statisticsQuery.list().size(), is(3));
    assertThat(statisticsQuery.list().get(0).getEvaluations(), is(0));
    assertThat(statisticsQuery.list().get(0).getDecisionDefinitionKey(), is(notNullValue()));
  }
}