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
import static java.util.stream.Stream.of;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.BatchRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class CorrelateAllMessageBatchTest {

  protected static final String PROCESS_ONE_KEY = "process";
  protected static final String PROCESS_TWO_KEY = "process-two";
  protected static final String PROCESS_THREE_KEY = "process-three";
  protected static final String MESSAGE_ONE_REF = "message";
  protected static final String MESSAGE_TWO_REF = "message-two";
  protected static final Date TEST_DATE = new Date(1457326800000L);

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
  public void deployProcessIntermediateMessageOne() {
    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_ONE_KEY)
      .startEvent()
      .intermediateCatchEvent("messageCatch")
      .message(MESSAGE_ONE_REF)
      .userTask("task")
      .endEvent()
      .done();
    engineTestRule.deploy(process);
  }

  @After
  public void clearAuthentication() {
    engineRule.getIdentityService().setAuthenticatedUserId(null);
  }

  @After
  public void resetConfiguration() {
    ClockUtil.reset();
    engineRule.getProcessEngineConfiguration()
      .setInvocationsPerBatchJob(ProcessEngineConfigurationImpl.DEFAULT_INVOCATIONS_PER_BATCH_JOB);
  }

  @Test
  public void shouldCorrelateAllWithInstanceIds() {
    // given
    deployProcessIntermediateMessageTwo();
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdThree = runtimeService.startProcessInstanceByKey(PROCESS_TWO_KEY).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne, processInstanceIdThree);

    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceIds(processInstances)
      .correlateAllAsync();

    ExecutionQuery taskExecutionQueryInstanceOne = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdOne);
    ExecutionQuery taskExecutionQueryInstanceTwo = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdTwo);
    ExecutionQuery taskExecutionQueryInstanceThree = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdThree);

    // assume
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceTwo.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(0L);

    // when
    rule.syncExec(batch);

    // then
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(1L);
    assertThat(taskExecutionQueryInstanceTwo.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(0L);
  }

  @Test
  public void shouldCorrelateAllWithInstanceQuery() {
    // given
    deployProcessIntermediateMessageTwo();
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdThree = runtimeService.startProcessInstanceByKey(PROCESS_TWO_KEY).getId();

    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery()
      .processInstanceIds(of(processInstanceIdOne, processInstanceIdThree).collect(toSet()));

    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceQuery(runtimeQuery)
      .correlateAllAsync();

    ExecutionQuery taskExecutionQueryInstanceOne = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdOne);
    ExecutionQuery taskExecutionQueryInstanceTwo = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdTwo);
    ExecutionQuery taskExecutionQueryInstanceThree = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdThree);

    // assume
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceTwo.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(0L);

    // when
    rule.syncExec(batch);

    // then
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(1L);
    assertThat(taskExecutionQueryInstanceTwo.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(0L);
  }

  @Test
  public void shouldCorrelateAllWithHistoricInstanceQuery() {
    // given
    deployProcessIntermediateMessageTwo();
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdThree = runtimeService.startProcessInstanceByKey(PROCESS_TWO_KEY).getId();

    HistoricProcessInstanceQuery historyQuery = historyService.createHistoricProcessInstanceQuery()
      .processInstanceIds(of(processInstanceIdOne, processInstanceIdThree).collect(toSet()));

    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .historicProcessInstanceQuery(historyQuery)
      .correlateAllAsync();

    ExecutionQuery taskExecutionQueryInstanceOne = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdOne);
    ExecutionQuery taskExecutionQueryInstanceTwo = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdTwo);
    ExecutionQuery taskExecutionQueryInstanceThree = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdThree);

    // assume
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceTwo.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(0L);

    // when
    rule.syncExec(batch);

    // then
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(1L);
    assertThat(taskExecutionQueryInstanceTwo.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(0L);
  }

  @Test
  public void shouldCorrelateAllWithoutMessage() {
    // given
    deployProcessIntermediateMessageTwo();
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdThree = runtimeService.startProcessInstanceByKey(PROCESS_TWO_KEY).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne, processInstanceIdThree);

    Batch batch = runtimeService.createMessageCorrelationAsync(null)
      .processInstanceIds(processInstances)
      .correlateAllAsync();

    ExecutionQuery taskExecutionQueryInstanceOne = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdOne);
    ExecutionQuery taskExecutionQueryInstanceTwo = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdTwo);
    ExecutionQuery taskExecutionQueryInstanceThree = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdThree);

    // assume
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceTwo.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(0L);

    // when
    rule.syncExec(batch);

    // then
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(1L);
    assertThat(taskExecutionQueryInstanceTwo.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(1L);
  }

  @Test
  public void shouldNotCorrelateStartMessageEvent() {
    // given
    deployProcessStartMessageOne();
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne);

    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceIds(processInstances)
      .correlateAllAsync();

    ExecutionQuery taskExecutionQueryInstanceOne = runtimeService.createExecutionQuery()
      .activityId("task")
      .processInstanceId(processInstanceIdOne);
    ExecutionQuery taskExecutionQueryInstanceThree = runtimeService.createExecutionQuery()
      .activityId("task")
      .processDefinitionKey(PROCESS_THREE_KEY);

    // assume
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(0L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(0L);

    // when
    rule.syncExec(batch);

    // then
    assertThat(taskExecutionQueryInstanceOne.count()).isEqualTo(1L);
    assertThat(taskExecutionQueryInstanceThree.count()).isEqualTo(0L);
  }

  @Test
  public void shouldSetVariablesOnCorrelation() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceIds(processInstances)
      .setVariable("foo", "bar")
      .correlateAllAsync();

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // assume
    assertThat(query.list()).extracting("processInstanceId", "name", "value", "batchId")
      .containsExactly(tuple(null, "foo", "bar", batch.getId()));

    // when
    rule.syncExec(batch);

    // then
    assertThat(query.list()).extracting("processInstanceId", "name", "value")
      .containsExactlyInAnyOrder(tuple(processInstanceIdOne, "foo", "bar"), tuple(processInstanceIdTwo, "foo", "bar"));
  }

  @Test
  public void shouldThrowException_NoProcessInstancesFound() {
    // when/then
    assertThatThrownBy(() ->
      runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
        .processInstanceIds(Collections.emptyList())
        .correlateAllAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("process instance ids is empty");
  }

  @Test
  public void shouldThrowException_QueriesAndIdsNull() {
    // when/then
    assertThatThrownBy(() -> runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF).correlateAllAsync())
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("No process instances found");

  }

  @Test
  public void shouldThrowException_NullProcessInstanceIds() {
    // when/then
    assertThatThrownBy(() ->
      runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
        .processInstanceIds(null)
        .correlateAllAsync())
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("processInstanceIds");
  }

  @Test
  public void shouldThrowException_NullProcessInstanceQuery() {
    // when/then
    assertThatThrownBy(() ->
      runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
        .processInstanceQuery(null)
        .correlateAllAsync())
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("processInstanceQuery");
  }

  @Test
  public void shouldThrowException_NullHistoricProcessInstanceQuery() {
    // when/then
    assertThatThrownBy(() ->
      runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
        .historicProcessInstanceQuery(null)
        .correlateAllAsync())
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("historicProcessInstanceQuery");
  }

  @Test
  public void shouldThrowException_NullVariableName() {
    // when/then
    assertThatThrownBy(() ->
      runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
        .setVariable(null, "bar")
        .correlateAllAsync())
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("variableName");
  }

  @Test
  public void shouldThrowException_JavaSerializationForbidden() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY);
    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when/then
    assertThatThrownBy(() ->
      runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
        .processInstanceQuery(runtimeQuery)
        .setVariables(
            Variables.putValue("foo",
                Variables.serializedObjectValue()
                 .serializedValue("foo")
                 .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                 .create()))
        .correlateAllAsync())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("ENGINE-17007 Cannot set variable with name foo. " +
          "Java serialization format is prohibited");
  }

  @Test
  public void shouldCreateDeploymentAwareBatchJobs_ByIds() {
    // given
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);

    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    deployProcessIntermediateMessageOne();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();

    List<Deployment> list = engineRule.getRepositoryService().createDeploymentQuery().list();
    String deploymentIdOne = list.get(0).getId();
    String deploymentIdTwo = list.get(1).getId();

    List<String> processInstances = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    // when
    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceIds(processInstances)
      .correlateAllAsync();

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
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);

    runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY);
    deployProcessIntermediateMessageOne();
    runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY);

    List<Deployment> list = engineRule.getRepositoryService().createDeploymentQuery().list();
    String deploymentIdOne = list.get(0).getId();
    String deploymentIdTwo = list.get(1).getId();

    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when
    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceQuery(runtimeQuery)
      .correlateAllAsync();

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
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);

    runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY);
    deployProcessIntermediateMessageOne();
    runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY);

    List<Deployment> list = engineRule.getRepositoryService().createDeploymentQuery().list();
    String deploymentIdOne = list.get(0).getId();
    String deploymentIdTwo = list.get(1).getId();

    HistoricProcessInstanceQuery historyQuery = historyService.createHistoricProcessInstanceQuery();

    // when
    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .historicProcessInstanceQuery(historyQuery)
      .correlateAllAsync();

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
    runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY);

    engineRule.getIdentityService().setAuthenticatedUserId("demo");

    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when
    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceQuery(runtimeQuery)
      .setVariable("foo", "bar")
      .correlateAllAsync();

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().list();

    assertThat(logs)
      .extracting("property", "orgValue", "newValue", "operationType", "entityType", "category", "userId")
      .containsExactlyInAnyOrder(
          tuple("messageName", null, MESSAGE_ONE_REF, "CorrelateMessage", "ProcessInstance", "Operator", "demo"),
          tuple("nrOfInstances", null, "1", "CorrelateMessage", "ProcessInstance", "Operator", "demo"),
          tuple("nrOfVariables", null, "1", "CorrelateMessage", "ProcessInstance", "Operator", "demo"),
          tuple("async", null, "true", "CorrelateMessage", "ProcessInstance", "Operator", "demo"));

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldNotLogInstanceOperation() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY);

    ProcessInstanceQuery runtimeQuery = runtimeService.createProcessInstanceQuery();

    // when
    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceQuery(runtimeQuery)
      .setVariable("foo", "bar")
      .correlateAllAsync();

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery()
      .operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE)
      .list();

    assertThat(logs.size()).isEqualTo(0);

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  public void shouldCreateProcessInstanceRelatedBatchJobsForSingleInvocations() {
    // given
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();

    List<String> processInstanceIds = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    // when
    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceIds(processInstanceIds)
      .correlateAllAsync();

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

    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();

    List<String> processInstanceIds = Arrays.asList(processInstanceIdOne, processInstanceIdTwo);

    // when
    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
      .processInstanceIds(processInstanceIds)
      .correlateAllAsync();

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

    String processInstanceIdOne = runtimeService.startProcessInstanceByKey(PROCESS_ONE_KEY).getId();
    Batch batch = runtimeService.createMessageCorrelationAsync(MESSAGE_ONE_REF)
        .processInstanceIds(Collections.singletonList(processInstanceIdOne))
        .correlateAllAsync();
    helper.executeSeedJob(batch);
    List<Job> executionJobs = helper.getExecutionJobs(batch, Batch.TYPE_CORRELATE_MESSAGE);

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

  protected void deployProcessIntermediateMessageTwo() {
    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_TWO_KEY)
      .startEvent()
      .intermediateCatchEvent()
      .message(MESSAGE_TWO_REF)
      .userTask("task")
      .endEvent()
      .done();
    engineTestRule.deploy(process);
  }

  protected void deployProcessStartMessageOne() {
    BpmnModelInstance process = Bpmn.createExecutableProcess(PROCESS_THREE_KEY)
      .startEvent()
      .message(MESSAGE_ONE_REF)
      .userTask("task")
      .endEvent()
      .done();
    engineTestRule.deploy(process);
  }

}
