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

package org.camunda.bpm.engine.test.history;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
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
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.variables.JavaSerializable;
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
  public static final String DECISION_PROCESS_WITH_DECISION_SERVICE = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.testDecisionEvaluatedWithDecisionServiceInsideDelegation.bpmn20.xml";
  public static final String DECISION_PROCESS_WITH_START_LISTENER = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.testDecisionEvaluatedWithDecisionServiceInsideStartListener.bpmn20.xml";
  public static final String DECISION_PROCESS_WITH_END_LISTENER = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.testDecisionEvaluatedWithDecisionServiceInsideEndListener.bpmn20.xml";
  public static final String DECISION_PROCESS_WITH_TAKE_LISTENER = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.testDecisionEvaluatedWithDecisionServiceInsideTakeListener.bpmn20.xml";
  public static final String DECISION_PROCESS_INSIDE_EXPRESSION = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.testDecisionEvaluatedWithDecisionServiceInsideExpression.bpmn20.xml";
  public static final String DECISION_PROCESS_INSIDE_DELEGATE_EXPRESSION = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.testDecisionEvaluatedWithDecisionServiceInsideDelegateExpression.bpmn20.xml";

  public static final String DECISION_SINGLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";
  public static final String DECISION_MULTIPLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionMultipleOutput.dmn11.xml";
  public static final String DECISION_COMPOUND_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionCompoundOutput.dmn11.xml";
  public static final String DECISION_MULTIPLE_INPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionMultipleInput.dmn11.xml";
  public static final String DECISION_COLLECT_SUM_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionCollectSum.dmn11.xml";
  public static final String DECISION_RETURNS_TRUE = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.returnsTrue.dmn11.xml";

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
  public void testDecisionInputInstanceStringValue() {

    startProcessInstanceAndEvaluateDecision("a");

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs.size(), is(1));

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getTypeName(), is("string"));
    assertThat(input.getValue(), is((Object) "a"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionInputInstanceLongValue() {

    startProcessInstanceAndEvaluateDecision(1L);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs.size(), is(1));

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getTypeName(), is("long"));
    assertThat(input.getValue(), is((Object) 1L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionInputInstanceDoubleValue() {

    startProcessInstanceAndEvaluateDecision(2.5);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs.size(), is(1));

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getTypeName(), is("double"));
    assertThat(input.getValue(), is((Object) 2.5));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionInputInstanceByteValue() {

    byte[] bytes = "object".getBytes();
    startProcessInstanceAndEvaluateDecision(bytes);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs.size(), is(1));

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getTypeName(), is("bytes"));
    assertThat(input.getValue(), is((Object) bytes));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionInputInstanceObjectValue() {

    JavaSerializable object = new JavaSerializable("foo");
    startProcessInstanceAndEvaluateDecision(object);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeInputs().singleResult();
    List<HistoricDecisionInputInstance> inputs = historicDecisionInstance.getInputs();
    assertThat(inputs.size(), is(1));

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getTypeName(), is("object"));
    assertThat(input.getValue(), is((Object) object));
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

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionOutputInstanceStringValue() {

    startProcessInstanceAndEvaluateDecision("a");

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size(), is(1));

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getTypeName(), is("string"));
    assertThat(output.getValue(), is((Object) "a"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionOutputInstanceLongValue() {

    startProcessInstanceAndEvaluateDecision(1L);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size(), is(1));

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getTypeName(), is("long"));
    assertThat(output.getValue(), is((Object) 1L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionOutputInstanceDoubleValue() {

    startProcessInstanceAndEvaluateDecision(2.5);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size(), is(1));

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getTypeName(), is("double"));
    assertThat(output.getValue(), is((Object) 2.5));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionOutputInstanceByteValue() {

    byte[] bytes = "object".getBytes();
    startProcessInstanceAndEvaluateDecision(bytes);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size(), is(1));

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getTypeName(), is("bytes"));
    assertThat(output.getValue(), is((Object) bytes));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionOutputInstanceObjectValue() {

    JavaSerializable object = new JavaSerializable("foo");
    startProcessInstanceAndEvaluateDecision(object);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size(), is(1));

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getTypeName(), is("object"));
    assertThat(output.getValue(), is((Object) object));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionOutputInstanceObjectListValue() {

    List<JavaSerializable> object = new ArrayList<JavaSerializable>();
    object.add(new JavaSerializable("foo"));
    startProcessInstanceAndEvaluateDecision(object);

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().includeOutputs().singleResult();
    List<HistoricDecisionOutputInstance> outputs = historicDecisionInstance.getOutputs();
    assertThat(outputs.size(), is(1));

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getTypeName(), is("object"));
    assertThat(output.getValue(), is((Object) object));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_COLLECT_SUM_DMN })
  public void testCollectResultValue() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance.getCollectResultValue(), is(notNullValue()));
    assertThat(historicDecisionInstance.getCollectResultValue(), is(3.0));
  }

  public void testTableNames() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    assertThat(managementService.getTableName(HistoricDecisionInstance.class), is(tablePrefix +"ACT_HI_DECINST"));

    assertThat(managementService.getTableName(HistoricDecisionInstanceEntity.class), is(tablePrefix + "ACT_HI_DECINST"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDeleteHistoricDecisionInstances() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY);

    startProcessInstanceAndEvaluateDecision();

    assertThat(query.count(), is(1L));

    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();
    historyService.deleteHistoricDecisionInstance(decisionDefinition.getId());

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

  @Deployment(resources = { DECISION_PROCESS_WITH_DECISION_SERVICE, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithDecisionServiceInsideDelegate() {

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

    // references to process instance should be set since the decision is evaluated while executing a process instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(processDefinition.getKey()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(processDefinition.getId()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(processInstance.getId()));
    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getActivityId(), is("task"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_PROCESS_WITH_START_LISTENER, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithDecisionServiceInsideStartListener() {

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

    // references to process instance should be set since the decision is evaluated while executing a process instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(processDefinition.getKey()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(processDefinition.getId()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(processInstance.getId()));
    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getActivityId(), is("task"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_PROCESS_WITH_END_LISTENER, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithDecisionServiceInsideEndListener() {

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

    // references to process instance should be set since the decision is evaluated while executing a process instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(processDefinition.getKey()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(processDefinition.getId()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(processInstance.getId()));
    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getActivityId(), is("task"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_PROCESS_INSIDE_EXPRESSION, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithDecisionServiceInsideExpression() {

    VariableMap variables = Variables
        .createVariables()
        .putValue("input1", null)
        .putValue("myBean", new DecisionServiceDelegate());
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();
    String activityInstanceId = historyService.createHistoricActivityInstanceQuery().activityId("task").singleResult().getId();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is(DECISION_DEFINITION_KEY));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    // references to process instance should be set since the decision is evaluated while executing a process instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(processDefinition.getKey()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(processDefinition.getId()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(processInstance.getId()));
    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getActivityId(), is("task"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_PROCESS_INSIDE_DELEGATE_EXPRESSION, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithDecisionServiceInsideDelegateExpression() {

    VariableMap variables = Variables
        .createVariables()
        .putValue("input1", null)
        .putValue("myBean", new DecisionServiceDelegate());
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();
    String activityInstanceId = historyService.createHistoricActivityInstanceQuery().activityId("task").singleResult().getId();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is(DECISION_DEFINITION_KEY));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    // references to process instance should be set since the decision is evaluated while executing a process instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(processDefinition.getKey()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(processDefinition.getId()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(processInstance.getId()));
    assertThat(historicDecisionInstance.getCaseDefinitionKey(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseDefinitionId(), is(nullValue()));
    assertThat(historicDecisionInstance.getCaseInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getActivityId(), is("task"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_PROCESS_WITH_TAKE_LISTENER, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionEvaluatedWithDecisionServiceInsideTakeListener() {

    startProcessInstanceAndEvaluateDecision();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION_DEFINITION_KEY).singleResult().getId();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is(DECISION_DEFINITION_KEY));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    // references to process instance should be set since the decision is evaluated while executing a process instance
    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(processDefinition.getKey()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(processDefinition.getId()));
    assertThat(historicDecisionInstance.getProcessInstanceId(), is(processInstance.getId()));
    assertThat(historicDecisionInstance.getActivityId(), is("start"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(nullValue()));
    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
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
