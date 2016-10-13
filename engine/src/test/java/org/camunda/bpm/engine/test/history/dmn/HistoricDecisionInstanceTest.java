/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.history.dmn;

import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
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
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.joda.time.DateTime;

/**
 * @author Philipp Ossler
 * @author Ingo Richtsmeier
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricDecisionInstanceTest extends PluggableProcessEngineTestCase {

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

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionInstanceProperties() {

    startProcessInstanceAndEvaluateDecision();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();
    String activityInstanceId = historyService.createHistoricActivityInstanceQuery().activityId("task").singleResult().getId();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is(DECISION_DEFINITION_KEY));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(processDefinition.getKey()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(processDefinition.getId()));

    assertThat(historicDecisionInstance.getProcessInstanceId(), is(processInstance.getId()));

    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(nullValue()));

    assertThat(historicDecisionInstance.getCaseInstanceId(), is(nullValue()));

    assertThat(historicDecisionInstance.getActivityId(), is("task"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));

    assertThat(historicDecisionInstance.getRootDecisionInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getDecisionRequirementsDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getDecisionRequirementsDefinitionKey(), is(nullValue()));

    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_CASE, DECISION_SINGLE_OUTPUT_DMN })
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

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is(DECISION_DEFINITION_KEY));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(nullValue()));

    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(caseDefinition.getKey()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(caseDefinition.getId()));
    assertThat(historicDecisionInstance.getCaseInstanceId(), is(caseInstance.getId()));

    assertThat(historicDecisionInstance.getActivityId(), is("PI_DecisionTask_1"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));

    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionInputInstanceProperties() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs, is(notNullValue()));
    assertThat(inputs.size(), is(1));

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getDecisionInstanceId(), is(historicDecisionInstance.getId()));
    assertThat(input.getClauseId(), is("in"));
    assertThat(input.getClauseName(), is("input"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testMultipleDecisionInstances() {

    startProcessInstanceAndEvaluateDecision("a");
    waitASignificantAmountOfTime();
    startProcessInstanceAndEvaluateDecision("b");

    List<HistoricDecisionInstance> historicDecisionInstances = historyService
        .createHistoricDecisionInstanceQuery()
        .includeInputs()
        .orderByEvaluationTime().asc()
        .list();
    assertThat(historicDecisionInstances.size(), is(2));

    List<HistoricDecisionInputInstance> inputsOfFirstDecision = historicDecisionInstances.get(0).getInputs();
    assertThat(inputsOfFirstDecision.size(), is(1));
    assertThat(inputsOfFirstDecision.get(0).getValue(), is((Object) "a"));

    List<HistoricDecisionInputInstance> inputsOfSecondDecision = historicDecisionInstances.get(1).getInputs();
    assertThat(inputsOfSecondDecision.size(), is(1));
    assertThat(inputsOfSecondDecision.get(0).getValue(), is((Object) "b"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_MULTIPLE_INPUT_DMN})
  public void testMultipleDecisionInputInstances() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", "a");
    variables.put("input2", 1);
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs.size(), is(2));

    assertThat(inputs.get(0).getValue(), is((Object) "a"));
    assertThat(inputs.get(1).getValue(), is((Object) 1));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDisableDecisionInputInstanceByteValue() {

    byte[] bytes = "object".getBytes();
    startProcessInstanceAndEvaluateDecision(bytes);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().disableBinaryFetching().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs.size(), is(1));

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getTypeName(), is("bytes"));
    assertThat(input.getValue(), is(nullValue()));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionOutputInstanceProperties() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs, is(notNullValue()));
    assertThat(outputs.size(), is(1));

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getDecisionInstanceId(), is(historicDecisionInstance.getId()));
    assertThat(output.getClauseId(), is("out"));
    assertThat(output.getClauseName(), is("output"));

    assertThat(output.getRuleId(), is("rule"));
    assertThat(output.getRuleOrder(), is(1));

    assertThat(output.getVariableName(), is("result"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_MULTIPLE_OUTPUT_DMN })
  public void testMultipleDecisionOutputInstances() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size(), is(2));

    HistoricDecisionOutputInstance firstOutput = outputs.get(0);
    assertThat(firstOutput.getClauseId(), is("out1"));
    assertThat(firstOutput.getRuleId(), is("rule1"));
    assertThat(firstOutput.getRuleOrder(), is(1));
    assertThat(firstOutput.getVariableName(), is("result1"));
    assertThat(firstOutput.getValue(), is((Object) "okay"));

    HistoricDecisionOutputInstance secondOutput = outputs.get(1);
    assertThat(secondOutput.getClauseId(), is("out1"));
    assertThat(secondOutput.getRuleId(), is("rule2"));
    assertThat(secondOutput.getRuleOrder(), is(2));
    assertThat(secondOutput.getVariableName(), is("result1"));
    assertThat(secondOutput.getValue(), is((Object) "not okay"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_COMPOUND_OUTPUT_DMN })
  public void testCompoundDecisionOutputInstances() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size(), is(2));

    HistoricDecisionOutputInstance firstOutput = outputs.get(0);
    assertThat(firstOutput.getClauseId(), is("out1"));
    assertThat(firstOutput.getRuleId(), is("rule1"));
    assertThat(firstOutput.getRuleOrder(), is(1));
    assertThat(firstOutput.getVariableName(), is("result1"));
    assertThat(firstOutput.getValue(), is((Object) "okay"));

    HistoricDecisionOutputInstance secondOutput = outputs.get(1);
    assertThat(secondOutput.getClauseId(), is("out2"));
    assertThat(secondOutput.getRuleId(), is("rule1"));
    assertThat(secondOutput.getRuleOrder(), is(1));
    assertThat(secondOutput.getVariableName(), is("result2"));
    assertThat(secondOutput.getValue(), is((Object) "not okay"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_COLLECT_SUM_DMN })
  public void testCollectResultValue() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance.getCollectResultValue(), is(notNullValue()));
    assertThat(historicDecisionInstance.getCollectResultValue(), is(3.0));
  }

  @Deployment(resources = DECISION_LITERAL_EXPRESSION_DMN)
  public void testDecisionInstancePropertiesOfDecisionLiteralExpression() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    decisionService.evaluateDecisionByKey("decision")
      .variables(Variables.createVariables().putValue("sum", 2205))
      .evaluate();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().includeInputs().includeOutputs();
    assertThat(query.count(), is(1L));

    HistoricDecisionInstance historicDecisionInstance = query.singleResult();

    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinition.getId()));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is("decision"));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("Decision with Literal Expression"));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));

    assertThat(historicDecisionInstance.getInputs().size(), is(0));

    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size(), is(1));

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getVariableName(), is("result"));
    assertThat(output.getTypeName(), is("string"));
    assertThat((String) output.getValue(), is("ok"));

    assertThat(output.getClauseId(), is(nullValue()));
    assertThat(output.getClauseName(), is(nullValue()));
    assertThat(output.getRuleId(), is(nullValue()));
    assertThat(output.getRuleOrder(), is(nullValue()));
  }

  @Deployment(resources = DRG_DMN)
  public void testDecisionInstancePropertiesOfDrdDecision() {

    decisionService.evaluateDecisionTableByKey("dish-decision")
      .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
      .evaluate();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.count(), is(3L));

    HistoricDecisionInstance rootHistoricDecisionInstance = query.decisionDefinitionKey("dish-decision").singleResult();
    HistoricDecisionInstance requiredHistoricDecisionInstance1 = query.decisionDefinitionKey("season").singleResult();
    HistoricDecisionInstance requiredHistoricDecisionInstance2 = query.decisionDefinitionKey("guestCount").singleResult();

    assertThat(rootHistoricDecisionInstance.getRootDecisionInstanceId(), is(nullValue()));
    assertThat(rootHistoricDecisionInstance.getDecisionRequirementsDefinitionId(), is(decisionRequirementsDefinition.getId()));
    assertThat(rootHistoricDecisionInstance.getDecisionRequirementsDefinitionKey(), is(decisionRequirementsDefinition.getKey()));

    assertThat(requiredHistoricDecisionInstance1.getRootDecisionInstanceId(), is(rootHistoricDecisionInstance.getId()));
    assertThat(requiredHistoricDecisionInstance1.getDecisionRequirementsDefinitionId(), is(decisionRequirementsDefinition.getId()));
    assertThat(requiredHistoricDecisionInstance1.getDecisionRequirementsDefinitionKey(), is(decisionRequirementsDefinition.getKey()));

    assertThat(requiredHistoricDecisionInstance2.getRootDecisionInstanceId(), is(rootHistoricDecisionInstance.getId()));
    assertThat(requiredHistoricDecisionInstance2.getDecisionRequirementsDefinitionId(), is(decisionRequirementsDefinition.getId()));
    assertThat(requiredHistoricDecisionInstance2.getDecisionRequirementsDefinitionKey(), is(decisionRequirementsDefinition.getKey()));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDeleteHistoricDecisionInstances() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY);

    startProcessInstanceAndEvaluateDecision();

    assertThat(query.count(), is(1L));

    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();
    historyService.deleteHistoricDecisionInstanceByDefinitionId(decisionDefinition.getId());

    assertThat(query.count(), is(0L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDeleteHistoricDecisionInstanceByInstanceId() {

    // given
    startProcessInstanceAndEvaluateDecision();
    HistoricDecisionInstanceQuery query =
        historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY);

    assertThat(query.count(), is(1L));
    HistoricDecisionInstance historicDecisionInstance = query.includeInputs().includeOutputs().singleResult();

    // when
    historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());

    // then
    assertThat(query.count(), is(0L));
  }

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
    assertThat(query.count(), is(1L));

    repositoryService.deleteDeployment(secondDeploymentId, true);
    assertThat(query.count(), is(1L));

    repositoryService.deleteDeployment(firstDeploymentId, true);
    assertThat(query.count(), is(0L));
  }

  @Deployment(resources = { DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithDecisionService() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", "test");
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY, variables);

    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is(DECISION_DEFINITION_KEY));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
    // references to process instance should be null since the decision is not evaluated while executing a process instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getActivityId(), is(nullValue()));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(nullValue()));
    // the user should be null since no user was authenticated during evaluation
    assertThat(historicDecisionInstance.getUserId(), is(nullValue()));
  }

  @Deployment(resources = { DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithAuthenticatedUser() {
    identityService.setAuthenticatedUserId("demo");
    VariableMap variables = Variables.putValue("input1", "test");
    decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY, variables);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance, is(notNullValue()));
    // the user should be set since the decision was evaluated with the decision service
    assertThat(historicDecisionInstance.getUserId(), is("demo"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithAuthenticatedUserFromProcess() {
    identityService.setAuthenticatedUserId("demo");
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance, is(notNullValue()));
    // the user should be null since the decision was evaluated by the process
    assertThat(historicDecisionInstance.getUserId(), is(nullValue()));
  }

  @Deployment(resources = { DECISION_CASE_WITH_DECISION_SERVICE, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithAuthenticatedUserFromCase() {
    identityService.setAuthenticatedUserId("demo");
    createCaseInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService
        .createHistoricDecisionInstanceQuery()
        .singleResult();

    assertThat(historicDecisionInstance, is(notNullValue()));
    // the user should be null since decision was evaluated by the case
    assertThat(historicDecisionInstance.getUserId(), is(nullValue()));
  }

  @Deployment(resources = { DECISION_CASE_WITH_DECISION_SERVICE, DECISION_SINGLE_OUTPUT_DMN })
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

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is(DECISION_DEFINITION_KEY));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    // references to case instance should be set since the decision is evaluated while executing a case instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(caseDefinition.getKey()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(caseDefinition.getId()));
    assertThat(historicDecisionInstance.getCaseInstanceId(), is(caseInstance.getId()));
    assertThat(historicDecisionInstance.getActivityId(), is("PI_HumanTask_1"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_CASE_WITH_DECISION_SERVICE_INSIDE_RULE, DECISION_RETURNS_TRUE })
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

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is(DECISION_DEFINITION_KEY));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    // references to case instance should be set since the decision is evaluated while executing a case instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(caseDefinition.getKey()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(caseDefinition.getId()));
    assertThat(historicDecisionInstance.getCaseInstanceId(), is(caseInstance.getId()));
    assertThat(historicDecisionInstance.getActivityId(), is("PI_HumanTask_1"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_CASE_WITH_DECISION_SERVICE_INSIDE_IF_PART, DECISION_RETURNS_TRUE })
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

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is(DECISION_DEFINITION_KEY));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    // references to case instance should be set since the decision is evaluated while executing a case instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(caseDefinition.getKey()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(caseDefinition.getId()));
    assertThat(historicDecisionInstance.getCaseInstanceId(), is(caseInstance.getId()));
    assertThat(historicDecisionInstance.getActivityId(), is("PI_HumanTask_1"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  public void testTableNames() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    assertThat(managementService.getTableName(HistoricDecisionInstance.class), is(tablePrefix +"ACT_HI_DECINST"));

    assertThat(managementService.getTableName(HistoricDecisionInstanceEntity.class), is(tablePrefix + "ACT_HI_DECINST"));
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
