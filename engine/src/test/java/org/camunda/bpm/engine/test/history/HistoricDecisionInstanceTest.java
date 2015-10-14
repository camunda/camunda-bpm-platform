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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.history.NativeHistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.joda.time.DateTime;

/**
 * @author Philipp Ossler
 */
public class HistoricDecisionInstanceTest extends PluggableProcessEngineTestCase {

  public static final String DECISION_PROCESS = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml";
  public static final String DECISION_SINGLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn10.xml";
  public static final String DECISION_MULTIPLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionMultipleOutput.dmn10.xml";
  public static final String DECISION_COMPOUND_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionCompoundOutput.dmn10.xml";
  public static final String DECISION_MULTIPLE_INPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionMultipleInput.dmn10.xml";
  public static final String DECISION_COLLECT_SUM_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionCollectSum.dmn10.xml";

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDecisionInstanceProperties() {

    startProcessInstanceAndEvaluateDecision();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey("testDecision").singleResult().getId();
    String activityInstanceId = historyService.createHistoricActivityInstanceQuery().activityId("task").singleResult().getId();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance, is(notNullValue()));
    assertThat(historicDecisionInstance.getDecisionDefinitionId(), is(decisionDefinitionId));
    assertThat(historicDecisionInstance.getDecisionDefinitionKey(), is("testDecision"));
    assertThat(historicDecisionInstance.getDecisionDefinitionName(), is("sample decision"));

    assertThat(historicDecisionInstance.getProcessDefinitionKey(), is(processDefinition.getKey()));
    assertThat(historicDecisionInstance.getProcessDefinitionId(), is(processDefinition.getId()));

    assertThat(historicDecisionInstance.getProcessInstanceId(), is(processInstance.getId()));

    assertThat(historicDecisionInstance.getActivityId(), is("task"));
    assertThat(historicDecisionInstance.getActivityInstanceId(), is(activityInstanceId));

    assertThat(historicDecisionInstance.getEvaluationTime(), is(notNullValue()));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryIncludeInputs() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    try {
      query.singleResult().getInputs();
      fail("expected exception: input not fetched");
    } catch (ProcessEngineException e) {
      // should throw exception if input is not fetched
    }

    assertThat(query.includeInputs().singleResult().getInputs().size(), is(1));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryIncludeInputsForNonExistingDecision() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().includeInputs();
    assertThat(query.singleResult(), is(nullValue()));

    startProcessInstanceAndEvaluateDecision();

    assertThat(query.decisionInstanceId("nonExisting").singleResult(), is(nullValue()));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryIncludeOutputs() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    try {
      query.singleResult().getOutputs();
      fail("expected exception: output not fetched");
    } catch (ProcessEngineException e) {
      // should throw exception if output is not fetched
    }

    assertThat(query.includeOutputs().singleResult().getOutputs().size(), is(1));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryIncludeOutputsForNonExistingDecision() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().includeOutputs();
    assertThat(query.singleResult(), is(nullValue()));

    startProcessInstanceAndEvaluateDecision();

    assertThat(query.decisionInstanceId("nonExisting").singleResult(), is(nullValue()));
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

  @Deployment(resources = { DECISION_PROCESS, DECISION_COLLECT_SUM_DMN })
  public void testCollectResultValue() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery().singleResult();

    assertThat(historicDecisionInstance.getCollectResultValue(), is(notNullValue()));
    assertThat(historicDecisionInstance.getCollectResultValue(), is(3.0));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryPaging() {

    startProcessInstanceAndEvaluateDecision();
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.listPage(0, 2).size(), is(2));
    assertThat(query.listPage(1, 1).size(), is(1));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQuerySortByEvaluationTime() {

    startProcessInstanceAndEvaluateDecision();
    waitASignificantAmountOfTime();
    startProcessInstanceAndEvaluateDecision();

    List<HistoricDecisionInstance> orderAsc = historyService.createHistoricDecisionInstanceQuery().orderByEvaluationTime().asc().list();
    assertThat(orderAsc.get(0).getEvaluationTime().before(orderAsc.get(1).getEvaluationTime()), is(true));

    List<HistoricDecisionInstance> orderDesc = historyService.createHistoricDecisionInstanceQuery().orderByEvaluationTime().desc().list();
    assertThat(orderDesc.get(0).getEvaluationTime().after(orderDesc.get(1).getEvaluationTime()), is(true));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByDecisionInstanceId() {
    ProcessInstance pi1 = startProcessInstanceAndEvaluateDecision();
    ProcessInstance pi2 = startProcessInstanceAndEvaluateDecision();

    String decisionInstanceId1 = historyService.createHistoricDecisionInstanceQuery().processInstanceId(pi1.getId()).singleResult().getId();
    String decisionInstanceId2 = historyService.createHistoricDecisionInstanceQuery().processInstanceId(pi2.getId()).singleResult().getId();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionInstanceId(decisionInstanceId1).count(), is(1L));
    assertThat(query.decisionInstanceId(decisionInstanceId2).count(), is(1L));
    assertThat(query.decisionInstanceId("unknown").count(), is(0L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByDecisionInstanceIds() {
    ProcessInstance pi1 = startProcessInstanceAndEvaluateDecision();
    ProcessInstance pi2 = startProcessInstanceAndEvaluateDecision();

    String decisionInstanceId1 = historyService.createHistoricDecisionInstanceQuery().processInstanceId(pi1.getId()).singleResult().getId();
    String decisionInstanceId2 = historyService.createHistoricDecisionInstanceQuery().processInstanceId(pi2.getId()).singleResult().getId();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionInstanceIdIn(decisionInstanceId1).count(), is(1L));
    assertThat(query.decisionInstanceIdIn(decisionInstanceId2).count(), is(1L));
    assertThat(query.decisionInstanceIdIn(decisionInstanceId1, decisionInstanceId2).count(), is(2L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByDecisionDefinitionId() {
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey("testDecision").singleResult().getId();

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionId(decisionDefinitionId).count(), is(1L));
    assertThat(query.decisionDefinitionId("other id").count(), is(0L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByDecisionDefinitionKey() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionKey("testDecision").count(), is(1L));
    assertThat(query.decisionDefinitionKey("other key").count(), is(0L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByDecisionDefinitionName() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.decisionDefinitionName("sample decision").count(), is(1L));
    assertThat(query.decisionDefinitionName("other name").count(), is(0L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByProcessDefinitionKey() {
    String processDefinitionKey = repositoryService.createProcessDefinitionQuery().singleResult().getKey();

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.processDefinitionKey(processDefinitionKey).count(), is(1L));
    assertThat(query.processDefinitionKey("other process").count(), is(0L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByProcessDefinitionId() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.processDefinitionId(processDefinitionId).count(), is(1L));
    assertThat(query.processDefinitionId("other process").count(), is(0L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByProcessInstanceId() {

    startProcessInstanceAndEvaluateDecision();

    String processInstanceId = runtimeService.createProcessInstanceQuery().singleResult().getId();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.processInstanceId(processInstanceId).count(), is(1L));
    assertThat(query.processInstanceId("other process").count(), is(0L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByActivityId() {

    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    assertThat(query.activityIdIn("task").count(), is(1L));
    assertThat(query.activityIdIn("other activity").count(), is(0L));
    assertThat(query.activityIdIn("task", "other activity").count(), is(1L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByActivityInstanceId() {

    startProcessInstanceAndEvaluateDecision();

    String activityInstanceId = historyService.createHistoricActivityInstanceQuery().activityId("task").singleResult().getId();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.activityInstanceIdIn(activityInstanceId).count(), is(1L));
    assertThat(query.activityInstanceIdIn("other activity").count(), is(0L));
    assertThat(query.activityInstanceIdIn(activityInstanceId, "other activity").count(), is(1L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByEvaluatedBefore() {
    Date beforeEvaluated = new Date(1441612000);
    Date evaluated = new Date(1441613000);
    Date afterEvaluated = new Date(1441614000);

    ClockUtil.setCurrentTime(evaluated);
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.evaluatedBefore(afterEvaluated).count(), is(1L));
    assertThat(query.evaluatedBefore(evaluated).count(), is(1L));
    assertThat(query.evaluatedBefore(beforeEvaluated).count(), is(0L));

    ClockUtil.reset();
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testQueryByEvaluatedAfter() {
    Date beforeEvaluated = new Date(1441612000);
    Date evaluated = new Date(1441613000);
    Date afterEvaluated = new Date(1441614000);

    ClockUtil.setCurrentTime(evaluated);
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    assertThat(query.evaluatedAfter(beforeEvaluated).count(), is(1L));
    assertThat(query.evaluatedAfter(evaluated).count(), is(1L));
    assertThat(query.evaluatedAfter(afterEvaluated).count(), is(0L));

    ClockUtil.reset();
  }

  public void testTableNames() {

    assertThat(managementService.getTableName(HistoricDecisionInstance.class), is("ACT_HI_DECINST"));

    assertThat(managementService.getTableName(HistoricDecisionInstanceEntity.class), is("ACT_HI_DECINST"));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testNativeQuery() {

    startProcessInstanceAndEvaluateDecision();

    NativeHistoricDecisionInstanceQuery nativeQuery = historyService
        .createNativeHistoricDecisionInstanceQuery().sql("SELECT * FROM ACT_HI_DECINST");

    assertThat(nativeQuery.list().size(), is(1));

    NativeHistoricDecisionInstanceQuery nativeQueryWithParameter = historyService
        .createNativeHistoricDecisionInstanceQuery()
        .sql("SELECT * FROM ACT_HI_DECINST H WHERE H.DEC_DEF_KEY_ = #{decisionDefinitionKey}");

    assertThat(nativeQueryWithParameter.parameter("decisionDefinitionKey", "testDecision").list().size(), is(1));
    assertThat(nativeQueryWithParameter.parameter("decisionDefinitionKey", "other decision").list().size(), is(0));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testNativeCountQuery() {

    startProcessInstanceAndEvaluateDecision();

    NativeHistoricDecisionInstanceQuery nativeQuery = historyService
        .createNativeHistoricDecisionInstanceQuery().sql("SELECT count(*) FROM ACT_HI_DECINST");

    assertThat(nativeQuery.count(), is(1L));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testNativeQueryPaging() {

    startProcessInstanceAndEvaluateDecision();
    startProcessInstanceAndEvaluateDecision();

    NativeHistoricDecisionInstanceQuery nativeQuery = historyService.createNativeHistoricDecisionInstanceQuery()
        .sql("SELECT * FROM ACT_HI_DECINST");

    assertThat(nativeQuery.listPage(0, 2).size(), is(2));
    assertThat(nativeQuery.listPage(1, 1).size(), is(1));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN })
  public void testDeleteHistoricDecisionInstances() {
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey("testDecision");

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

  protected ProcessInstance startProcessInstanceAndEvaluateDecision() {
    return startProcessInstanceAndEvaluateDecision(null);
  }


  protected ProcessInstance startProcessInstanceAndEvaluateDecision(Object input) {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", input);
    return runtimeService.startProcessInstanceByKey("testProcess", variables);
  }

  /**
   * Use between two rule evaluations to ensure the expected order by evaluation time.
   */
  protected void waitASignificantAmountOfTime() {
    DateTime now = new DateTime(ClockUtil.getCurrentTime());
    ClockUtil.setCurrentTime(now.plusSeconds(10).toDate());
  }

}
