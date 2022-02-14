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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.history.DecisionServiceDelegate;
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
public class HistoricDecisionInstanceTest extends PluggableProcessEngineTest {

  public static final String DECISION_CASE = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.caseWithDecisionTask.cmmn";
  public static final String DECISION_CASE_WITH_DECISION_SERVICE = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.testCaseDecisionEvaluatedWithDecisionServiceInsideDelegate.cmmn";
  public static final String DECISION_CASE_WITH_DECISION_SERVICE_INSIDE_RULE = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.testManualActivationRuleEvaluatesDecision.cmmn";
  public static final String DECISION_CASE_WITH_DECISION_SERVICE_INSIDE_IF_PART = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.testIfPartEvaluatesDecision.cmmn";

  public static final String DECISION_PROCESS = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml";

  public static final String DECISION_SINGLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";
  public static final String DECISION_MULTIPLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionMultipleOutput.dmn11.xml";
  public static final String DECISION_COMPOUND_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionCompoundOutput.dmn11.xml";
  public static final String DECISION_MULTIPLE_INPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionMultipleInput.dmn11.xml";
  public static final String DECISION_COLLECT_SUM_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionCollectSum.dmn11.xml";
  public static final String DECISION_RETURNS_TRUE = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.returnsTrue.dmn11.xml";

  public static final String DECISION_LITERAL_EXPRESSION_DMN = "org/camunda/bpm/engine/test/api/dmn/DecisionWithLiteralExpression.dmn";

  public static final String DRG_DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  public static final String DECISION_DEFINITION_KEY = "testDecision";

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
  public void testDecisionInstanceProperties() {

    startProcessInstanceAndEvaluateDecision();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();
    String activityInstanceId = historyService.createHistoricActivityInstanceQuery().activityId("task").singleResult().getId();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance).isNotNull();
    assertThat(historicDecisionInstance.getDecisionDefinitionId()).isEqualTo(decisionDefinitionId);
    assertThat(historicDecisionInstance.getDecisionDefinitionKey()).isEqualTo(DECISION_DEFINITION_KEY);
    assertThat(historicDecisionInstance.getDecisionDefinitionName()).isEqualTo("sample decision");

    assertThat(historicDecisionInstance.getProcessDefinitionKey()).isEqualTo(processDefinition.getKey());
    assertThat(historicDecisionInstance.getProcessDefinitionId()).isEqualTo(processDefinition.getId());

    assertThat(historicDecisionInstance.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(historicDecisionInstance.getCaseDefinitionKey()).isNull();
    assertThat(historicDecisionInstance.getCaseDefinitionId()).isNull();

    assertThat(historicDecisionInstance.getCaseInstanceId()).isNull();

    assertThat(historicDecisionInstance.getActivityId()).isEqualTo("task");
    assertThat(historicDecisionInstance.getActivityInstanceId()).isEqualTo(activityInstanceId);

    assertThat(historicDecisionInstance.getRootDecisionInstanceId()).isNull();
    assertThat(historicDecisionInstance.getDecisionRequirementsDefinitionId()).isNull();
    assertThat(historicDecisionInstance.getDecisionRequirementsDefinitionKey()).isNull();

    assertThat(historicDecisionInstance.getEvaluationTime()).isNotNull();
  }

  @Deployment(resources = { DECISION_CASE, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testCaseDecisionInstanceProperties() {

    CaseInstance caseInstance = createCaseInstanceAndEvaluateDecision();

    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionId(caseInstance.getCaseDefinitionId())
        .singleResult();

    String decisionDefinitionId = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .singleResult()
        .getId();

    String activityInstanceId = historyService
        .createHistoricCaseActivityInstanceQuery()
        .caseActivityId("PI_DecisionTask_1")
        .singleResult()
        .getId();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance).isNotNull();
    assertThat(historicDecisionInstance.getDecisionDefinitionId()).isEqualTo(decisionDefinitionId);
    assertThat(historicDecisionInstance.getDecisionDefinitionKey()).isEqualTo(DECISION_DEFINITION_KEY);
    assertThat(historicDecisionInstance.getDecisionDefinitionName()).isEqualTo("sample decision");

    assertThat(historicDecisionInstance.getProcessDefinitionKey()).isNull();
    assertThat(historicDecisionInstance.getProcessDefinitionId()).isNull();
    assertThat(historicDecisionInstance.getProcessInstanceId()).isNull();

    assertThat(historicDecisionInstance.getCaseDefinitionKey()).isEqualTo(caseDefinition.getKey());
    assertThat(historicDecisionInstance.getCaseDefinitionId()).isEqualTo(caseDefinition.getId());
    assertThat(historicDecisionInstance.getCaseInstanceId()).isEqualTo(caseInstance.getId());

    assertThat(historicDecisionInstance.getActivityId()).isEqualTo("PI_DecisionTask_1");
    assertThat(historicDecisionInstance.getActivityInstanceId()).isEqualTo(activityInstanceId);

    assertThat(historicDecisionInstance.getEvaluationTime()).isNotNull();
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDecisionInputInstanceProperties() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs).isNotNull();
    assertThat(inputs.size()).isEqualTo(1);

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getDecisionInstanceId()).isEqualTo(historicDecisionInstance.getId());
    assertThat(input.getClauseId()).isEqualTo("in");
    assertThat(input.getClauseName()).isEqualTo("input");
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testMultipleDecisionInstances() {

    startProcessInstanceAndEvaluateDecision("a");
    waitASignificantAmountOfTime();
    startProcessInstanceAndEvaluateDecision("b");

    List<HistoricDecisionInstance> historicDecisionInstances = historyService
        .createHistoricDecisionInstanceQuery()
        .includeInputs()
        .orderByEvaluationTime().asc()
        .list();
    assertThat(historicDecisionInstances.size()).isEqualTo(2);

    List<HistoricDecisionInputInstance> inputsOfFirstDecision = historicDecisionInstances.get(0).getInputs();
    assertThat(inputsOfFirstDecision.size()).isEqualTo(1);
    assertThat(inputsOfFirstDecision.get(0).getValue()).isEqualTo((Object) "a");

    List<HistoricDecisionInputInstance> inputsOfSecondDecision = historicDecisionInstances.get(1).getInputs();
    assertThat(inputsOfSecondDecision.size()).isEqualTo(1);
    assertThat(inputsOfSecondDecision.get(0).getValue()).isEqualTo((Object) "b");
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_MULTIPLE_INPUT_DMN})
  @Test
  public void testMultipleDecisionInputInstances() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", "a");
    variables.put("input2", 1);
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs.size()).isEqualTo(2);

    assertThat(inputs.get(0).getValue()).isEqualTo((Object) "a");
    assertThat(inputs.get(1).getValue()).isEqualTo((Object) 1);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDisableDecisionInputInstanceByteValue() {

    byte[] bytes = "object".getBytes();
    startProcessInstanceAndEvaluateDecision(bytes);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().disableBinaryFetching().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs.size()).isEqualTo(1);

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getTypeName()).isEqualTo("bytes");
    assertThat(input.getValue()).isNull();
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDecisionOutputInstanceProperties() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs).isNotNull();
    assertThat(outputs.size()).isEqualTo(1);

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getDecisionInstanceId()).isEqualTo(historicDecisionInstance.getId());
    assertThat(output.getClauseId()).isEqualTo("out");
    assertThat(output.getClauseName()).isEqualTo("output");

    assertThat(output.getRuleId()).isEqualTo("rule");
    assertThat(output.getRuleOrder()).isEqualTo(1);

    assertThat(output.getVariableName()).isEqualTo("result");
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_MULTIPLE_OUTPUT_DMN })
  @Test
  public void testMultipleDecisionOutputInstances() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size()).isEqualTo(2);

    HistoricDecisionOutputInstance firstOutput = outputs.get(0);
    assertThat(firstOutput.getClauseId()).isEqualTo("out1");
    assertThat(firstOutput.getRuleId()).isEqualTo("rule1");
    assertThat(firstOutput.getRuleOrder()).isEqualTo(1);
    assertThat(firstOutput.getVariableName()).isEqualTo("result1");
    assertThat(firstOutput.getValue()).isEqualTo((Object) "okay");

    HistoricDecisionOutputInstance secondOutput = outputs.get(1);
    assertThat(secondOutput.getClauseId()).isEqualTo("out1");
    assertThat(secondOutput.getRuleId()).isEqualTo("rule2");
    assertThat(secondOutput.getRuleOrder()).isEqualTo(2);
    assertThat(secondOutput.getVariableName()).isEqualTo("result1");
    assertThat(secondOutput.getValue()).isEqualTo((Object) "not okay");
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_COMPOUND_OUTPUT_DMN })
  @Test
  public void testCompoundDecisionOutputInstances() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size()).isEqualTo(2);

    HistoricDecisionOutputInstance firstOutput = outputs.get(0);
    assertThat(firstOutput.getClauseId()).isEqualTo("out1");
    assertThat(firstOutput.getRuleId()).isEqualTo("rule1");
    assertThat(firstOutput.getRuleOrder()).isEqualTo(1);
    assertThat(firstOutput.getVariableName()).isEqualTo("result1");
    assertThat(firstOutput.getValue()).isEqualTo((Object) "okay");

    HistoricDecisionOutputInstance secondOutput = outputs.get(1);
    assertThat(secondOutput.getClauseId()).isEqualTo("out2");
    assertThat(secondOutput.getRuleId()).isEqualTo("rule1");
    assertThat(secondOutput.getRuleOrder()).isEqualTo(1);
    assertThat(secondOutput.getVariableName()).isEqualTo("result2");
    assertThat(secondOutput.getValue()).isEqualTo((Object) "not okay");
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_COLLECT_SUM_DMN })
  @Test
  public void testCollectResultValue() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance.getCollectResultValue()).isNotNull();
    assertThat(historicDecisionInstance.getCollectResultValue()).isEqualTo(3.0);
  }

  @Deployment(resources = DECISION_LITERAL_EXPRESSION_DMN)
  @Test
  public void testDecisionInstancePropertiesOfDecisionLiteralExpression() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    decisionService.evaluateDecisionByKey("decision")
      .variables(Variables.createVariables().putValue("sum", 2205))
      .evaluate();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().includeInputs().includeOutputs();
    assertThat(query.count()).isEqualTo(1L);

    HistoricDecisionInstance historicDecisionInstance = query.singleResult();

    assertThat(historicDecisionInstance.getDecisionDefinitionId()).isEqualTo(decisionDefinition.getId());
    assertThat(historicDecisionInstance.getDecisionDefinitionKey()).isEqualTo("decision");
    assertThat(historicDecisionInstance.getDecisionDefinitionName()).isEqualTo("Decision with Literal Expression");
    assertThat(historicDecisionInstance.getEvaluationTime()).isNotNull();

    assertThat(historicDecisionInstance.getInputs().size()).isEqualTo(0);

    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size()).isEqualTo(1);

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getVariableName()).isEqualTo("result");
    assertThat(output.getTypeName()).isEqualTo("string");
    assertThat((String) output.getValue()).isEqualTo("ok");

    assertThat(output.getClauseId()).isNull();
    assertThat(output.getClauseName()).isNull();
    assertThat(output.getRuleId()).isNull();
    assertThat(output.getRuleOrder()).isNull();
  }

  @Deployment(resources = DRG_DMN)
  @Test
  public void testDecisionInstancePropertiesOfDrdDecision() {

    decisionService.evaluateDecisionTableByKey("dish-decision")
      .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
      .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.count()).isEqualTo(3L);

    HistoricDecisionInstance rootHistoricDecisionInstance = query.decisionDefinitionKey("dish-decision").singleResult();
    HistoricDecisionInstance requiredHistoricDecisionInstance1 = query.decisionDefinitionKey("season").singleResult();
    HistoricDecisionInstance requiredHistoricDecisionInstance2 = query.decisionDefinitionKey("guestCount").singleResult();

    assertThat(rootHistoricDecisionInstance.getRootDecisionInstanceId()).isNull();
    assertThat(rootHistoricDecisionInstance.getDecisionRequirementsDefinitionId()).isEqualTo(decisionRequirementsDefinition.getId());
    assertThat(rootHistoricDecisionInstance.getDecisionRequirementsDefinitionKey()).isEqualTo(decisionRequirementsDefinition.getKey());

    assertThat(requiredHistoricDecisionInstance1.getRootDecisionInstanceId()).isEqualTo(rootHistoricDecisionInstance.getId());
    assertThat(requiredHistoricDecisionInstance1.getDecisionRequirementsDefinitionId()).isEqualTo(decisionRequirementsDefinition.getId());
    assertThat(requiredHistoricDecisionInstance1.getDecisionRequirementsDefinitionKey()).isEqualTo(decisionRequirementsDefinition.getKey());

    assertThat(requiredHistoricDecisionInstance2.getRootDecisionInstanceId()).isEqualTo(rootHistoricDecisionInstance.getId());
    assertThat(requiredHistoricDecisionInstance2.getDecisionRequirementsDefinitionId()).isEqualTo(decisionRequirementsDefinition.getId());
    assertThat(requiredHistoricDecisionInstance2.getDecisionRequirementsDefinitionKey()).isEqualTo(decisionRequirementsDefinition.getKey());
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDeleteHistoricDecisionInstances() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY);

    startProcessInstanceAndEvaluateDecision();

    assertThat(query.count()).isEqualTo(1L);

    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();
    historyService.deleteHistoricDecisionInstanceByDefinitionId(decisionDefinition.getId());

    assertThat(query.count()).isEqualTo(0L);
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDeleteHistoricDecisionInstanceByInstanceId() {

    // given
    startProcessInstanceAndEvaluateDecision();
    HistoricDecisionInstanceQuery query =
        historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY);

    assertThat(query.count()).isEqualTo(1L);
    HistoricDecisionInstance historicDecisionInstance = query.includeInputs().includeOutputs().singleResult();

    // when
    historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());

    // then
    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testDeleteHistoricDecisionInstanceByUndeployment() {
    String firstDeploymentId = repositoryService.createDeployment()
      .addClasspathResource(DECISION_PROCESS)
      .addClasspathResource(DECISION_SINGLE_OUTPUT_DMN)
      .deploy().getId();

    startProcessInstanceAndEvaluateDecision();

    String secondDeploymentId = repositoryService.createDeployment()
        .addClasspathResource(DECISION_PROCESS)
        .addClasspathResource(DECISION_MULTIPLE_OUTPUT_DMN)
        .deploy().getId();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);

    repositoryService.deleteDeployment(secondDeploymentId, true);
    assertThat(query.count()).isEqualTo(1L);

    repositoryService.deleteDeployment(firstDeploymentId, true);
    assertThat(query.count()).isEqualTo(0L);
  }

  @Deployment(resources = { DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDecisionEvaluatedWithDecisionService() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", "test");
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY, variables);

    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance).isNotNull();
    assertThat(historicDecisionInstance.getDecisionDefinitionId()).isEqualTo(decisionDefinitionId);
    assertThat(historicDecisionInstance.getDecisionDefinitionKey()).isEqualTo(DECISION_DEFINITION_KEY);
    assertThat(historicDecisionInstance.getDecisionDefinitionName()).isEqualTo("sample decision");

    assertThat(historicDecisionInstance.getEvaluationTime()).isNotNull();
    // references to process instance should be null since the decision is not evaluated while executing a process instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey()).isNull();
    assertThat(historicDecisionInstance.getProcessDefinitionId()).isNull();
    assertThat(historicDecisionInstance.getProcessInstanceId()).isNull();
    assertThat(historicDecisionInstance.getActivityId()).isNull();
    assertThat(historicDecisionInstance.getActivityInstanceId()).isNull();
    // the user should be null since no user was authenticated during evaluation
    assertThat(historicDecisionInstance.getUserId()).isNull();
  }

  @Deployment(resources = { DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDecisionEvaluatedWithAuthenticatedUser() {
    identityService.setAuthenticatedUserId("demo");
    VariableMap variables = Variables.putValue("input1", "test");
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY, variables);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance).isNotNull();
    // the user should be set since the decision was evaluated with the decision service
    assertThat(historicDecisionInstance.getUserId()).isEqualTo("demo");
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDecisionEvaluatedWithAuthenticatedUserFromProcess() {
    identityService.setAuthenticatedUserId("demo");
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance).isNotNull();
    // the user should be null since the decision was evaluated by the process
    assertThat(historicDecisionInstance.getUserId()).isNull();
  }

  @Deployment(resources = { DECISION_CASE_WITH_DECISION_SERVICE, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testDecisionEvaluatedWithAuthenticatedUserFromCase() {
    identityService.setAuthenticatedUserId("demo");
    createCaseInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService
        .createHistoricDecisionInstanceQuery()
        .singleResult();

    assertThat(historicDecisionInstance).isNotNull();
    // the user should be null since decision was evaluated by the case
    assertThat(historicDecisionInstance.getUserId()).isNull();
  }

  @Deployment(resources = { DECISION_CASE_WITH_DECISION_SERVICE, DECISION_SINGLE_OUTPUT_DMN })
  @Test
  public void testCaseDecisionEvaluatedWithDecisionServiceInsideDelegate() {

    CaseInstance caseInstance = createCaseInstanceAndEvaluateDecision();

    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionId(caseInstance.getCaseDefinitionId())
        .singleResult();

    String decisionDefinitionId = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .singleResult()
        .getId();

    String activityInstanceId = historyService
        .createHistoricCaseActivityInstanceQuery()
        .caseActivityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    HistoricDecisionInstance historicDecisionInstance = historyService
        .createHistoricDecisionInstanceQuery()
        .singleResult();

    assertThat(historicDecisionInstance).isNotNull();
    assertThat(historicDecisionInstance.getDecisionDefinitionId()).isEqualTo(decisionDefinitionId);
    assertThat(historicDecisionInstance.getDecisionDefinitionKey()).isEqualTo(DECISION_DEFINITION_KEY);
    assertThat(historicDecisionInstance.getDecisionDefinitionName()).isEqualTo("sample decision");

    // references to case instance should be set since the decision is evaluated while executing a case instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey()).isNull();
    assertThat(historicDecisionInstance.getProcessDefinitionId()).isNull();
    assertThat(historicDecisionInstance.getProcessInstanceId()).isNull();
    assertThat(historicDecisionInstance.getCaseDefinitionKey()).isEqualTo(caseDefinition.getKey());
    assertThat(historicDecisionInstance.getCaseDefinitionId()).isEqualTo(caseDefinition.getId());
    assertThat(historicDecisionInstance.getCaseInstanceId()).isEqualTo(caseInstance.getId());
    assertThat(historicDecisionInstance.getActivityId()).isEqualTo("PI_HumanTask_1");
    assertThat(historicDecisionInstance.getActivityInstanceId()).isEqualTo(activityInstanceId);
    assertThat(historicDecisionInstance.getEvaluationTime()).isNotNull();
  }

  @Deployment(resources = { DECISION_CASE_WITH_DECISION_SERVICE_INSIDE_RULE, DECISION_RETURNS_TRUE })
  @Test
  public void testManualActivationRuleEvaluatesDecision() {

    CaseInstance caseInstance = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("input1", null)
        .setVariable("myBean", new DecisionServiceDelegate())
        .create();

    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionId(caseInstance.getCaseDefinitionId())
        .singleResult();

    String decisionDefinitionId = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .singleResult()
        .getId();

    String activityInstanceId = historyService
        .createHistoricCaseActivityInstanceQuery()
        .caseActivityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    HistoricDecisionInstance historicDecisionInstance = historyService
        .createHistoricDecisionInstanceQuery()
        .singleResult();

    assertThat(historicDecisionInstance).isNotNull();
    assertThat(historicDecisionInstance.getDecisionDefinitionId()).isEqualTo(decisionDefinitionId);
    assertThat(historicDecisionInstance.getDecisionDefinitionKey()).isEqualTo(DECISION_DEFINITION_KEY);
    assertThat(historicDecisionInstance.getDecisionDefinitionName()).isEqualTo("sample decision");

    // references to case instance should be set since the decision is evaluated while executing a case instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey()).isNull();
    assertThat(historicDecisionInstance.getProcessDefinitionId()).isNull();
    assertThat(historicDecisionInstance.getProcessInstanceId()).isNull();
    assertThat(historicDecisionInstance.getCaseDefinitionKey()).isEqualTo(caseDefinition.getKey());
    assertThat(historicDecisionInstance.getCaseDefinitionId()).isEqualTo(caseDefinition.getId());
    assertThat(historicDecisionInstance.getCaseInstanceId()).isEqualTo(caseInstance.getId());
    assertThat(historicDecisionInstance.getActivityId()).isEqualTo("PI_HumanTask_1");
    assertThat(historicDecisionInstance.getActivityInstanceId()).isEqualTo(activityInstanceId);
    assertThat(historicDecisionInstance.getEvaluationTime()).isNotNull();
  }

  @Deployment(resources = { DECISION_CASE_WITH_DECISION_SERVICE_INSIDE_IF_PART, DECISION_RETURNS_TRUE })
  @Test
  public void testIfPartEvaluatesDecision() {

    CaseInstance caseInstance = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("input1", null)
        .setVariable("myBean", new DecisionServiceDelegate())
        .create();

    String humanTask1 = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();
    caseService.completeCaseExecution(humanTask1);

    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionId(caseInstance.getCaseDefinitionId())
        .singleResult();

    String decisionDefinitionId = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .singleResult()
        .getId();

    String activityInstanceId = historyService
        .createHistoricCaseActivityInstanceQuery()
        .caseActivityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    HistoricDecisionInstance historicDecisionInstance = historyService
        .createHistoricDecisionInstanceQuery()
        .singleResult();

    assertThat(historicDecisionInstance).isNotNull();
    assertThat(historicDecisionInstance.getDecisionDefinitionId()).isEqualTo(decisionDefinitionId);
    assertThat(historicDecisionInstance.getDecisionDefinitionKey()).isEqualTo(DECISION_DEFINITION_KEY);
    assertThat(historicDecisionInstance.getDecisionDefinitionName()).isEqualTo("sample decision");

    // references to case instance should be set since the decision is evaluated while executing a case instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey()).isNull();
    assertThat(historicDecisionInstance.getProcessDefinitionId()).isNull();
    assertThat(historicDecisionInstance.getProcessInstanceId()).isNull();
    assertThat(historicDecisionInstance.getCaseDefinitionKey()).isEqualTo(caseDefinition.getKey());
    assertThat(historicDecisionInstance.getCaseDefinitionId()).isEqualTo(caseDefinition.getId());
    assertThat(historicDecisionInstance.getCaseInstanceId()).isEqualTo(caseInstance.getId());
    assertThat(historicDecisionInstance.getActivityId()).isEqualTo("PI_HumanTask_1");
    assertThat(historicDecisionInstance.getActivityInstanceId()).isEqualTo(activityInstanceId);
    assertThat(historicDecisionInstance.getEvaluationTime()).isNotNull();
  }

  @Test
  public void testTableNames() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    assertThat(managementService.getTableName(HistoricDecisionInstance.class)).isEqualTo(tablePrefix +"ACT_HI_DECINST");

    assertThat(managementService.getTableName(HistoricDecisionInstanceEntity.class)).isEqualTo(tablePrefix + "ACT_HI_DECINST");
  }

  protected ProcessInstance startProcessInstanceAndEvaluateDecision() {
    return startProcessInstanceAndEvaluateDecision(null);
  }

  protected ProcessInstance startProcessInstanceAndEvaluateDecision(Object input) {
    return runtimeService.startProcessInstanceByKey("testProcess", getVariables(input));
  }

  protected CaseInstance createCaseInstanceAndEvaluateDecision() {
    return caseService
        .withCaseDefinitionByKey("case")
        .setVariables(getVariables("test"))
        .create();
  }

  protected VariableMap getVariables(Object input) {
    VariableMap variables = Variables.createVariables();
    variables.put("input1", input);
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
