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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.NativeHistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Philipp Ossler
 * @author Ingo Richtsmeier
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricDecisionInstanceQueryTest extends PluggableProcessEngineTest {

  protected static final String DECISION_CASE = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.caseWithDecisionTask.cmmn";
  protected static final String DECISION_PROCESS = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml";
  protected static final String DECISION_PROCESS_WITH_UNDERSCORE = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask_.bpmn20.xml";

  protected static final String DECISION_SINGLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";
  protected static final String DECISION_SINGLE_OUTPUT_DMN_WITH_UNDERSCORE = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput_.dmn11.xml";
  protected static final String DECISION_NO_INPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.noInput.dmn11.xml";

  protected static final String DRG_DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected static final String DECISION_DEFINITION_KEY = "testDecision";
  protected static final String DISH_DECISION = "dish-decision";

  @Before
  public void setUp() throws Exception {
    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        processEngineConfiguration.getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();


  }

  @After
  public void tearDown() throws Exception {
    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        processEngineConfiguration.getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();


  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryIncludeInputsForNonExistingDecision() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().includeInputs();
    assertThat(query.singleResult()).isNull();

    startProcessInstanceAndEvaluateDecision();

    assertThat(query.decisionInstanceId("nonExisting").singleResult()).isNull();
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryIncludeOutputs() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    try {
      query.singleResult().getOutputs();
      fail("expected exception: output not fetched");
    } catch (ProcessEngineException e) {
      // should throw exception if output is not fetched
    }

    assertThat(query.includeOutputs().singleResult().getOutputs()).hasSize(1);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryIncludeOutputsForNonExistingDecision() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().includeOutputs();
    assertThat(query.singleResult()).isNull();

    startProcessInstanceAndEvaluateDecision();

    assertThat(query.decisionInstanceId("nonExisting").singleResult()).isNull();
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_NO_INPUT_DMN })
  @Test
  public void testQueryIncludeInputsNoInput() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.includeInputs().singleResult().getInputs()).isEmpty();
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_NO_INPUT_DMN })
  @Test
  public void testQueryIncludeOutputsNoInput() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.includeOutputs().singleResult().getOutputs()).isEmpty();
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryPaging() {

    startProcessInstanceAndEvaluateDecision();
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.listPage(0, 2)).hasSize(2);
    assertThat(query.listPage(1, 1)).hasSize(1);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQuerySortByEvaluationTime() {

    startProcessInstanceAndEvaluateDecision();
    waitASignificantAmountOfTime();
    startProcessInstanceAndEvaluateDecision();

    List<HistoricDecisionInstance> orderAsc = historyService.createHistoricDecisionInstanceQuery().orderByEvaluationTime().asc().list();
    assertThat(orderAsc.get(0).getEvaluationTime().before(orderAsc.get(1).getEvaluationTime())).isTrue();

    List<HistoricDecisionInstance> orderDesc = historyService.createHistoricDecisionInstanceQuery().orderByEvaluationTime().desc().list();
    assertThat(orderDesc.get(0).getEvaluationTime().after(orderDesc.get(1).getEvaluationTime())).isTrue();
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByDecisionInstanceId() {
    ProcessInstance pi1 = startProcessInstanceAndEvaluateDecision();
    ProcessInstance pi2 = startProcessInstanceAndEvaluateDecision();

    String decisionInstanceId1 = historyService.createHistoricDecisionInstanceQuery().processInstanceId(pi1.getId()).singleResult().getId();
    String decisionInstanceId2 = historyService.createHistoricDecisionInstanceQuery().processInstanceId(pi2.getId()).singleResult().getId();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionInstanceId(decisionInstanceId1).count()).isEqualTo(1L);
    assertThat(query.decisionInstanceId(decisionInstanceId2).count()).isEqualTo(1L);
    assertThat(query.decisionInstanceId("unknown").count()).isEqualTo(0L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByDecisionInstanceIds() {
    ProcessInstance pi1 = startProcessInstanceAndEvaluateDecision();
    ProcessInstance pi2 = startProcessInstanceAndEvaluateDecision();

    String decisionInstanceId1 = historyService.createHistoricDecisionInstanceQuery().processInstanceId(pi1.getId()).singleResult().getId();
    String decisionInstanceId2 = historyService.createHistoricDecisionInstanceQuery().processInstanceId(pi2.getId()).singleResult().getId();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionInstanceIdIn(decisionInstanceId1).count()).isEqualTo(1L);
    assertThat(query.decisionInstanceIdIn(decisionInstanceId2).count()).isEqualTo(1L);
    assertThat(query.decisionInstanceIdIn(decisionInstanceId1, decisionInstanceId2).count()).isEqualTo(2L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByDecisionDefinitionId() {
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionId(decisionDefinitionId).count()).isEqualTo(1L);
    assertThat(query.decisionDefinitionId("other id").count()).isEqualTo(0L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN, DRG_DMN })
  @Test
  public void testQueryByDecisionDefinitionIdIn() {
    //given
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();
    String decisionDefinitionId2 = repositoryService.createDecisionDefinitionQuery()
        .decisionDefinitionKey(DISH_DECISION).singleResult().getId();

    //when
    startProcessInstanceAndEvaluateDecision();
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
        .evaluate();

    //then
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionIdIn(decisionDefinitionId, decisionDefinitionId2).count()).isEqualTo(2L);
    assertThat(query.decisionDefinitionIdIn("other id", "anotherFake").count()).isEqualTo(0L);
  }

  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN, DRG_DMN})
  @Test
  public void testQueryByInvalidDecisionDefinitionIdIn() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    try {
      query.decisionDefinitionIdIn("aFake", null).count();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      //expected
    }
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN, DRG_DMN })
  @Test
  public void testQueryByDecisionDefinitionKeyIn() {

    //when
    startProcessInstanceAndEvaluateDecision();
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
        .evaluate();

    //then
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionKeyIn(DISH_DECISION, DECISION_DEFINITION_KEY).count()).isEqualTo(2L);
    assertThat(query.decisionDefinitionKeyIn("other id", "anotherFake").count()).isEqualTo(0L);
  }

  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN, DRG_DMN})
  @Test
  public void testQueryByInvalidDecisionDefinitionKeyIn() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    try {
      query.decisionDefinitionKeyIn("aFake", null).count();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      //expected
    }
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByDecisionDefinitionKey() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionKey(DECISION_DEFINITION_KEY).count()).isEqualTo(1L);
    assertThat(query.decisionDefinitionKey("other key").count()).isEqualTo(0L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByDecisionDefinitionName() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionName("sample decision").count()).isEqualTo(1L);
    assertThat(query.decisionDefinitionName("other name").count()).isEqualTo(0L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_PROCESS_WITH_UNDERSCORE, DECISION_SINGLE_OUTPUT_DMN, DECISION_SINGLE_OUTPUT_DMN_WITH_UNDERSCORE })
  @Test
  public void testQueryByDecisionDefinitionNameLike() {

    startProcessInstanceAndEvaluateDecision();
    startProcessInstanceAndEvaluateDecisionWithUnderscore();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionNameLike("%ample dec%").count()).isEqualTo(1L);
    assertThat(query.decisionDefinitionNameLike("%ample\\_%").count()).isEqualTo(1L);

  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByInvalidDecisionDefinitionNameLike() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionNameLike("%invalid%").count()).isEqualTo(0L);

    try {
      query.decisionDefinitionNameLike(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByProcessDefinitionKey() {
    String processDefinitionKey = repositoryService.createProcessDefinitionQuery().singleResult().getKey();

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.processDefinitionKey(processDefinitionKey).count()).isEqualTo(1L);
    assertThat(query.processDefinitionKey("other process").count()).isEqualTo(0L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByProcessDefinitionId() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.processDefinitionId(processDefinitionId).count()).isEqualTo(1L);
    assertThat(query.processDefinitionId("other process").count()).isEqualTo(0L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByProcessInstanceId() {

    startProcessInstanceAndEvaluateDecision();

    String processInstanceId = runtimeService.createProcessInstanceQuery().singleResult().getId();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.processInstanceId(processInstanceId).count()).isEqualTo(1L);
    assertThat(query.processInstanceId("other process").count()).isEqualTo(0L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByActivityId() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.activityIdIn("task").count()).isEqualTo(1L);
    assertThat(query.activityIdIn("other activity").count()).isEqualTo(0L);
    assertThat(query.activityIdIn("task", "other activity").count()).isEqualTo(1L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByActivityInstanceId() {

    startProcessInstanceAndEvaluateDecision();

    String activityInstanceId = historyService.createHistoricActivityInstanceQuery().activityId("task").singleResult().getId();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.activityInstanceIdIn(activityInstanceId).count()).isEqualTo(1L);
    assertThat(query.activityInstanceIdIn("other activity").count()).isEqualTo(0L);
    assertThat(query.activityInstanceIdIn(activityInstanceId, "other activity").count()).isEqualTo(1L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByEvaluatedBefore() {
    Date beforeEvaluated = new Date(1441612000);
    Date evaluated = new Date(1441613000);
    Date afterEvaluated = new Date(1441614000);

    ClockUtil.setCurrentTime(evaluated);
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.evaluatedBefore(afterEvaluated).count()).isEqualTo(1L);
    assertThat(query.evaluatedBefore(evaluated).count()).isEqualTo(1L);
    assertThat(query.evaluatedBefore(beforeEvaluated).count()).isEqualTo(0L);

    ClockUtil.reset();
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByEvaluatedAfter() {
    Date beforeEvaluated = new Date(1441612000);
    Date evaluated = new Date(1441613000);
    Date afterEvaluated = new Date(1441614000);

    ClockUtil.setCurrentTime(evaluated);
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.evaluatedAfter(beforeEvaluated).count()).isEqualTo(1L);
    assertThat(query.evaluatedAfter(evaluated).count()).isEqualTo(1L);
    assertThat(query.evaluatedAfter(afterEvaluated).count()).isEqualTo(0L);

    ClockUtil.reset();
  }

  @Deployment(resources = { DECISION_CASE, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByCaseDefinitionKey() {
    createCaseInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.caseDefinitionKey("case").count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByInvalidCaseDefinitionKey() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.caseDefinitionKey("invalid").count()).isEqualTo(0L);

    try {
      query.caseDefinitionKey(null);
      fail("exception expected");
    } catch (ProcessEngineException e) {
    }
  }

  @Deployment(resources = { DECISION_CASE, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByCaseDefinitionId() {
    CaseInstance caseInstance = createCaseInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.caseDefinitionId(caseInstance.getCaseDefinitionId()).count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByInvalidCaseDefinitionId() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.caseDefinitionId("invalid").count()).isEqualTo(0L);

    try {
      query.caseDefinitionId(null);
      fail("exception expected");
    } catch (ProcessEngineException e) {
    }
  }

  @Deployment(resources = { DECISION_CASE, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByCaseInstanceId() {
    CaseInstance caseInstance = createCaseInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.caseInstanceId(caseInstance.getId()).count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByInvalidCaseInstanceId() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.caseInstanceId("invalid").count()).isEqualTo(0L);

    try {
      query.caseInstanceId(null);
      fail("exception expected");
    } catch (ProcessEngineException e) {
    }
  }

  @Deployment(resources = { DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByUserId() {
    evaluateDecisionWithAuthenticatedUser("demo");

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.userId("demo").count()).isEqualTo(1L);
  }

  @Deployment(resources = { DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testQueryByInvalidUserId() {
    evaluateDecisionWithAuthenticatedUser("demo");

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.userId("dem1").count()).isEqualTo(0L);

    try {
      query.userId(null);
      fail("exception expected");
    } catch (ProcessEngineException e) {
    }
  }

  @Deployment(resources = { DRG_DMN })
  @Test
  public void testQueryByRootDecisionInstanceId() {
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
      .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
      .evaluate();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.count()).isEqualTo(3L);

    String rootDecisionInstanceId = query.decisionDefinitionKey(DISH_DECISION).singleResult().getId();
    String requiredDecisionInstanceId1 = query.decisionDefinitionKey("season").singleResult().getId();
    String requiredDecisionInstanceId2 = query.decisionDefinitionKey("guestCount").singleResult().getId();

    query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.rootDecisionInstanceId(rootDecisionInstanceId).count()).isEqualTo(3L);
    assertThat(query.rootDecisionInstanceId(requiredDecisionInstanceId1).count()).isEqualTo(0L);
    assertThat(query.rootDecisionInstanceId(requiredDecisionInstanceId2).count()).isEqualTo(0L);
  }

  @Deployment(resources = { DRG_DMN })
  @Test
  public void testQueryByRootDecisionInstancesOnly() {
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
      .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
      .evaluate();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.rootDecisionInstancesOnly().count()).isEqualTo(1L);
    assertThat(query.rootDecisionInstancesOnly().singleResult().getDecisionDefinitionKey()).isEqualTo(DISH_DECISION);
  }

  @Deployment(resources = { DRG_DMN })
  @Test
  public void testQueryByDecisionRequirementsDefinitionId() {
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
      .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
      .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionRequirementsDefinitionId("notExisting").count()).isEqualTo(0L);
    assertThat(query.decisionRequirementsDefinitionId(decisionRequirementsDefinition.getId()).count()).isEqualTo(3L);
  }

  @Deployment(resources = { DRG_DMN })
  @Test
  public void testQueryByDecisionRequirementsDefinitionKey() {
    decisionService.evaluateDecisionTableByKey(DISH_DECISION)
      .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
      .evaluate();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionRequirementsDefinitionKey("notExisting").count()).isEqualTo(0L);
    assertThat(query.decisionRequirementsDefinitionKey("dish").count()).isEqualTo(3L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testNativeQuery() {

    startProcessInstanceAndEvaluateDecision();

    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    NativeHistoricDecisionInstanceQuery nativeQuery = historyService
        .createNativeHistoricDecisionInstanceQuery().sql("SELECT * FROM " + tablePrefix + "ACT_HI_DECINST");

    assertThat(nativeQuery.list().size()).isEqualTo(1);

    NativeHistoricDecisionInstanceQuery nativeQueryWithParameter = historyService
        .createNativeHistoricDecisionInstanceQuery()
        .sql("SELECT * FROM " + tablePrefix + "ACT_HI_DECINST H WHERE H.DEC_DEF_KEY_ = #{decisionDefinitionKey}");

    assertThat(nativeQueryWithParameter.parameter("decisionDefinitionKey", DECISION_DEFINITION_KEY).list().size()).isEqualTo(1);
    assertThat(nativeQueryWithParameter.parameter("decisionDefinitionKey", "other decision").list().size()).isEqualTo(0);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testNativeCountQuery() {

    startProcessInstanceAndEvaluateDecision();

    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    NativeHistoricDecisionInstanceQuery nativeQuery = historyService
        .createNativeHistoricDecisionInstanceQuery().sql("SELECT count(*) FROM " + tablePrefix + "ACT_HI_DECINST");

    assertThat(nativeQuery.count()).isEqualTo(1L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testNativeQueryPaging() {

    startProcessInstanceAndEvaluateDecision();
    startProcessInstanceAndEvaluateDecision();

    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    NativeHistoricDecisionInstanceQuery nativeQuery = historyService.createNativeHistoricDecisionInstanceQuery()
        .sql("SELECT * FROM " + tablePrefix + "ACT_HI_DECINST");

    assertThat(nativeQuery.listPage(0, 2).size()).isEqualTo(2);
    assertThat(nativeQuery.listPage(1, 1).size()).isEqualTo(1);
  }

  protected ProcessInstance startProcessInstanceAndEvaluateDecision() {
    return runtimeService.startProcessInstanceByKey("testProcess", getVariables());
  }

  protected ProcessInstance startProcessInstanceAndEvaluateDecisionWithUnderscore() {
    return runtimeService.startProcessInstanceByKey("testProcess_", getVariables());
  }

  protected CaseInstance createCaseInstanceAndEvaluateDecision() {
    return caseService
        .withCaseDefinitionByKey("case")
        .setVariables(getVariables())
        .create();
  }

  protected void evaluateDecisionWithAuthenticatedUser(String userId) {
    identityService.setAuthenticatedUserId(userId);
    VariableMap variables = Variables.putValue("input1", "test");
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY, variables);
  }

  protected VariableMap getVariables() {
    VariableMap variables = Variables.createVariables();
    variables.put("input1", "test");
    return variables;
  }

  /**
   * Use between two rule evaluations to ensure the expected order by evaluation time.
   */
  protected void waitASignificantAmountOfTime() {
    DateTime now = new DateTime(ClockUtil.getCurrentTime());
    ClockUtil.setCurrentTime(now.plusSeconds(10).toDate());
  }

}
