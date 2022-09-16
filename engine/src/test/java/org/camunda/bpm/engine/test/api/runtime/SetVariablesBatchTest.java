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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.dmn.businessruletask.TestPojo;
import org.camunda.bpm.engine.test.util.BatchRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class SetVariablesBatchTest {

  protected static final String PROCESS_KEY = "process";
  protected static final Date TEST_DATE = new Date(1457326800000L);

  protected static final VariableMap SINGLE_VARIABLE = Variables.putValue("foo", "bar");

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule engineTestRule = new ProcessEngineTestRule(engineRule);
  protected BatchRule rule = new BatchRule(engineRule, engineTestRule);
  protected BatchHelper helper = new BatchHelper(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(engineTestRule).around(rule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ManagementService managementService;

  @Before
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
  }

  @Before
  public void deployProcess() {
    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
          .userTask()
        .endEvent()
        .done();
    engineTestRule.deploy(process);
  }

  @After
  public void clearAuthentication() {
    ClockUtil.reset();
    engineRule.getIdentityService()
        .setAuthenticatedUserId(null);
  }

  @Test
  public void shouldSetByIds() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    Batch batch = runtimeService.setVariablesAsync(processInstances, SINGLE_VARIABLE);

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
          .containsExactly(tuple(null, "foo", "bar", batch.getId()));

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdOne, "foo", "bar"),
            tuple(processInstanceIdTwo, "foo", "bar")
        );
  }

  @Test
  public void shouldSetByIds_TypedValue() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    Batch batch = runtimeService.setVariablesAsync(processInstances,
        Variables.putValue("foo", Variables.stringValue("bar")));

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
          .containsExactly(tuple(null, "foo", "bar", batch.getId()));

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdOne, "foo", "bar"),
            tuple(processInstanceIdTwo, "foo", "bar")
        );

  }

  @Test
  public void shouldSetByIds_MixedTypedAndUntypedValues() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    Batch batch = runtimeService.setVariablesAsync(processInstances,
        Variables
            .putValue("foo", Variables.stringValue("bar"))
            .putValue("bar", "foo"));

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
          .containsExactlyInAnyOrder(
              tuple(null, "foo", "bar", batch.getId()),
              tuple(null, "bar", "foo", batch.getId())
          );

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdOne, "foo", "bar"),
            tuple(processInstanceIdOne, "bar", "foo"),

            tuple(processInstanceIdTwo, "foo", "bar"),
            tuple(processInstanceIdTwo, "bar", "foo")
        );

  }

  @Test
  public void shouldSetByIds_ObjectValue() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    TestPojo pojo = new TestPojo("bar", 3D);
    Batch batch = runtimeService.setVariablesAsync(processInstances,
        Variables.putValue("foo", Variables.objectValue(pojo)));

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
        .containsExactly(tuple(null, "foo", pojo, batch.getId()));

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdOne, "foo", pojo),
            tuple(processInstanceIdTwo, "foo", pojo)
        );
  }

  @Test
  public void shouldSetByIds_MultipleVariables() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    Batch batch = runtimeService.setVariablesAsync(processInstances,
        Variables
            .putValue("variableOne", "string")
            .putValue("variableTwo", 42)
            .putValue("variableThree", (short) 3));

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
          .containsExactlyInAnyOrder(
              tuple(null, "variableOne", "string", batch.getId()),
              tuple(null, "variableTwo", 42, batch.getId()),
              tuple(null, "variableThree", (short) 3, batch.getId())
          );

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdOne, "variableOne", "string"),
            tuple(processInstanceIdOne, "variableTwo", 42),
            tuple(processInstanceIdOne, "variableThree", (short) 3),

            tuple(processInstanceIdTwo, "variableOne", "string"),
            tuple(processInstanceIdTwo, "variableTwo", 42),
            tuple(processInstanceIdTwo, "variableThree", (short) 3)
        );
  }

  @Test
  public void shouldSetByIds_VariablesAsMap() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    List<String> processInstanceIds = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    Map<String, Object> variablesMap = new HashMap<>();
    variablesMap.put("foo", "bar");
    variablesMap.put("bar", "foo");

    Batch batch = runtimeService.setVariablesAsync(processInstanceIds, variablesMap);

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
          .containsExactlyInAnyOrder(
              tuple(null, "foo", "bar", batch.getId()),
              tuple(null, "bar", "foo", batch.getId())
          );

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdOne, "foo", "bar"),
            tuple(processInstanceIdOne, "bar", "foo"),

            tuple(processInstanceIdTwo, "foo", "bar"),
            tuple(processInstanceIdTwo, "bar", "foo")
        );
  }

  @Test
  public void shouldSetByRuntimeQuery() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();
    Batch batch = runtimeService.setVariablesAsync(runtimeQuery, SINGLE_VARIABLE);

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
          .containsExactly(tuple(null, "foo", "bar", batch.getId()));

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdOne, "foo", "bar"),
            tuple(processInstanceIdTwo, "foo", "bar")
        );
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void shouldSetByIdsAndQueries() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdThree = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    ProcessInstanceQuery runtimeQuery =
        runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceIdTwo);

    HistoricProcessInstanceQuery historyQuery =
        historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceIdThree);

    Batch batch = runtimeService.setVariablesAsync(Collections.singletonList(processInstanceIdOne),
        runtimeQuery,
        historyQuery,
        SINGLE_VARIABLE);

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
          .containsExactly(tuple(null, "foo", "bar", batch.getId()));

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdOne, "foo", "bar"),
            tuple(processInstanceIdTwo, "foo", "bar"),
            tuple(processInstanceIdThree, "foo", "bar")
        );
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void shouldSetByHistoryQuery() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    HistoricProcessInstanceQuery historyQuery = historyService.createHistoricProcessInstanceQuery();
    Batch batch = runtimeService.setVariablesAsync(historyQuery, SINGLE_VARIABLE);

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
          .containsExactly(tuple(null, "foo", "bar", batch.getId()));

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdOne, "foo", "bar"),
            tuple(processInstanceIdTwo, "foo", "bar")
        );
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void shouldSetByHistoryQuery_WithFinishedInstances() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    runtimeService.deleteProcessInstance(processInstanceIdOne, "dunno");

    HistoricProcessInstanceQuery historyQuery = historyService.createHistoricProcessInstanceQuery();
    Batch batch = runtimeService.setVariablesAsync(historyQuery, SINGLE_VARIABLE);

    // assume
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    assertThat(query.list())
        .extracting("processInstanceId", "name", "value", "batchId")
          .containsExactly(tuple(null, "foo", "bar", batch.getId()));

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list())
        .extracting("processInstanceId", "name", "value")
        .containsExactlyInAnyOrder(
            tuple(processInstanceIdTwo, "foo", "bar")
        );
  }

  @Test
  public void shouldThrowException_TransientVariable() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    // when/then
    assertThatThrownBy(() -> runtimeService.setVariablesAsync(Collections.singletonList(processInstanceId),
        Variables.putValue("foo", Variables.stringValue("bar", true))))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("ENGINE-13044 Setting transient variable 'foo' " +
          "asynchronously is currently not supported.");
  }

  @Test
  public void shouldThrowException_JavaSerializationForbidden() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when/then
    assertThatThrownBy(() -> runtimeService.setVariablesAsync(runtimeQuery,
        Variables.putValue("foo",
            Variables.serializedObjectValue()
                .serializedValue("foo")
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                .create())))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("ENGINE-17007 Cannot set variable with name foo. " +
          "Java serialization format is prohibited");
  }

  @Test
  public void shouldThrowException_NoProcessInstancesFound() {
    // given
    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when/then
    assertThatThrownBy(() -> runtimeService.setVariablesAsync(runtimeQuery, SINGLE_VARIABLE))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("processInstanceIds is empty");
  }

  @Test
  public void shouldThrowException_QueriesAndIdsNull() {
    // when/then
    assertThatThrownBy(() -> runtimeService.setVariablesAsync(null,
        null,
        null,
        SINGLE_VARIABLE))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("No process instances found.");

  }

  @Test
  public void shouldThrowException_VariablesNull() {
    // given
    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when/then
    assertThatThrownBy(() -> runtimeService.setVariablesAsync(runtimeQuery, null))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("variables is null");

  }

  @Test
  public void shouldThrowException_VariablesEmpty() {
    // given
    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when/then
    assertThatThrownBy(() -> runtimeService.setVariablesAsync(runtimeQuery, Collections.emptyMap()))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("variables is empty");
  }

  @Test
  public void shouldCreateDeploymentAwareBatchJobs_ByIds() {
    // given
    engineRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(2);

    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    deployProcess();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    List<Deployment> list = engineRule.getRepositoryService().createDeploymentQuery().list();
    String deploymentIdOne = list.get(0).getId();
    String deploymentIdTwo = list.get(1).getId();

    List<String> processInstanceIds = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    // when
    Batch batch = runtimeService.setVariablesAsync(processInstanceIds, SINGLE_VARIABLE);

    rule.executeSeedJobs(batch);

    // then
    List<Job> executionJobs = rule.getExecutionJobs(batch);
    assertThat(executionJobs)
        .extracting("deploymentId")
        .containsExactlyInAnyOrder(deploymentIdOne, deploymentIdTwo);

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  public void shouldCreateDeploymentAwareBatchJobs_ByRuntimeQuery() {
    // given
    engineRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(2);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    deployProcess();
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    List<Deployment> list = engineRule.getRepositoryService().createDeploymentQuery().list();
    String deploymentIdOne = list.get(0).getId();
    String deploymentIdTwo = list.get(1).getId();

    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when
    Batch batch = runtimeService.setVariablesAsync(runtimeQuery, SINGLE_VARIABLE);

    rule.executeSeedJobs(batch);

    // then
    List<Job> executionJobs = rule.getExecutionJobs(batch);
    assertThat(executionJobs)
        .extracting("deploymentId")
        .containsExactlyInAnyOrder(deploymentIdOne, deploymentIdTwo);

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void shouldCreateDeploymentAwareBatchJobs_ByHistoryQuery() {
    // given
    engineRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(2);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    deployProcess();
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    List<Deployment> list = engineRule.getRepositoryService().createDeploymentQuery().list();
    String deploymentIdOne = list.get(0).getId();
    String deploymentIdTwo = list.get(1).getId();

    HistoricProcessInstanceQuery historyQuery = historyService.createHistoricProcessInstanceQuery();

    // when
    Batch batch = runtimeService.setVariablesAsync(historyQuery, SINGLE_VARIABLE);

    rule.executeSeedJobs(batch);

    // then
    List<Job> executionJobs = rule.getExecutionJobs(batch);
    assertThat(executionJobs)
        .extracting("deploymentId")
        .containsExactlyInAnyOrder(deploymentIdOne, deploymentIdTwo);

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldLogOperation() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    engineRule.getIdentityService()
        .setAuthenticatedUserId("demo");

    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when
    Batch batch = runtimeService.setVariablesAsync(runtimeQuery, SINGLE_VARIABLE);

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery()
        .list();

    assertThat(logs)
        .extracting("property", "orgValue", "newValue", "operationType",
            "entityType", "category", "userId")
        .containsExactlyInAnyOrder(
            tuple("nrOfInstances", null, "1", "SetVariables", "ProcessInstance", "Operator", "demo"),
            tuple("nrOfVariables", null, "1", "SetVariables", "ProcessInstance", "Operator", "demo"),
            tuple("async", null, "true", "SetVariables", "ProcessInstance", "Operator", "demo")
        );

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldNotLogInstanceOperation() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when
    Batch batch = runtimeService.setVariablesAsync(runtimeQuery, SINGLE_VARIABLE);

    engineRule.getIdentityService()
        .setAuthenticatedUserId("demo");

    rule.syncExec(batch);

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE)
        .list();

    assertThat(logs.size()).isEqualTo(0);
  }

  @Test
  public void shouldCreateProcessInstanceRelatedBatchJobsForSingleInvocations() {
    // given
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(1);

    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    List<String> processInstanceIds = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    // when
    Batch batch = runtimeService.setVariablesAsync(processInstanceIds, SINGLE_VARIABLE);

    rule.executeSeedJobs(batch);

    // then
    List<Job> executionJobs = rule.getExecutionJobs(batch);
    assertThat(executionJobs)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne, processInstanceIdTwo);

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  public void shouldNotCreateProcessInstanceRelatedBatchJobsForMultipleInvocations() {
    // given
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);

    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    List<String> processInstanceIds = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    // when
    Batch batch = runtimeService.setVariablesAsync(processInstanceIds, SINGLE_VARIABLE);

    rule.executeSeedJobs(batch);

    // then
    List<Job> executionJobs = rule.getExecutionJobs(batch);
    assertThat(executionJobs)
        .extracting("processInstanceId")
        .containsOnlyNulls();

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldSetExecutionStartTimeInBatchAndHistory() {
    // given
    ClockUtil.setCurrentTime(TEST_DATE);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    Batch batch = runtimeService.setVariablesAsync(runtimeService.createProcessInstanceQuery(), SINGLE_VARIABLE);
    helper.executeSeedJob(batch);
    List<Job> executionJobs = helper.getExecutionJobs(batch, Batch.TYPE_SET_VARIABLES);

    // when
    helper.executeJob(executionJobs.get(0));

    // then
    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    batch = managementService.createBatchQuery().singleResult();

    Assertions.assertThat(batch.getExecutionStartTime()).isEqualToIgnoringMillis(TEST_DATE);
    Assertions.assertThat(historicBatch.getExecutionStartTime()).isEqualToIgnoringMillis(TEST_DATE);

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

}
