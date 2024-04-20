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
package org.camunda.bpm.engine.test.api.history.removaltime.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import static org.camunda.bpm.engine.test.api.history.removaltime.batch.helper.BatchSetRemovalTimeRule.addDays;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.batch.removaltime.ProcessSetRemovalTimeJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.history.removaltime.batch.helper.BatchSetRemovalTimeRule;
import org.camunda.bpm.engine.test.api.history.removaltime.batch.helper.BatchSetRemovalTimeRule.TestProcessBuilder;
import org.camunda.bpm.engine.test.dmn.businessruletask.TestPojo;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(HISTORY_FULL)
public class BatchSetRemovalTimeInChunksTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule engineTestRule = new ProcessEngineTestRule(engineRule);
  protected BatchSetRemovalTimeRule testRule = new BatchSetRemovalTimeRule(engineRule, engineTestRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(engineTestRule).around(testRule);

  protected final Date REMOVAL_TIME = testRule.REMOVAL_TIME;

  protected final Date CREATE_TIME = new GregorianCalendar(2013, Calendar.MARCH, 18, 13, 0, 0).getTime();

  protected final Date CURRENT_DATE = testRule.CURRENT_DATE;

  protected ProcessEngineConfigurationImpl engineConfiguration;
  protected RuntimeService runtimeService;
  protected DecisionService decisionService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected TaskService taskService;
  protected IdentityService identityService;
  protected ExternalTaskService externalTaskService;
  protected AuthorizationService authorizationService;
  protected int defaultMaxUpdateRows;
  protected int defaultInvocationsPerBatchJob;

  @Before
  public void setup() {
    engineConfiguration = engineRule.getProcessEngineConfiguration();

    defaultMaxUpdateRows = engineConfiguration.getRemovalTimeUpdateChunkSize();
    defaultInvocationsPerBatchJob = engineConfiguration.getInvocationsPerBatchJob();
    engineConfiguration.setRemovalTimeUpdateChunkSize(1);

    runtimeService = engineRule.getRuntimeService();
    decisionService = engineRule.getDecisionService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();
    externalTaskService = engineRule.getExternalTaskService();
    authorizationService = engineRule.getAuthorizationService();
  }

  @After
  public void tearDown() {
    engineConfiguration.setRemovalTimeUpdateChunkSize(defaultMaxUpdateRows);
    engineConfiguration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
  }

  @Test
  public void shouldRescheduleRemovalTimeJob() {
    // given
    testRule.process().serviceTask().serviceTask().userTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    Batch batch = historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync();
    testRule.executeSeedJobs(batch, true);
    // first execution updating one row per table
    testRule.getExecutionJobs(batch).forEach(job -> managementService.executeJob(job.getId()));

    // when second execution updating one row per table
    testRule.getExecutionJobs(batch).forEach(job -> managementService.executeJob(job.getId()));

    // then more executions exist
    assertThat(testRule.getExecutionJobs(batch)).isNotEmpty();
  }

  @Test
  public void shouldUseCustomChunkSize() {
    // given
    testRule.process().serviceTask().serviceTask().userTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    Batch batch = historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .chunkSize(100) // data updated in one chunk
        .executeAsync();
    testRule.executeSeedJobs(batch, true);
    // first execution updating all but the process instance
    testRule.getExecutionJobs(batch).forEach(job -> managementService.executeJob(job.getId()));

    // when second execution updating process instance
    testRule.getExecutionJobs(batch).forEach(job -> managementService.executeJob(job.getId()));

    // then no more jobs exist
    assertThat(testRule.getExecutionJobs(batch)).isEmpty();
  }

  @Test
  public void shouldUpdateProcessInstanceLast() {
    // given
    testRule.process().serviceTask().serviceTask().userTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    Batch batch = historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .chunkSize(100) // most data updated in one chunk
        .executeAsync();
    testRule.executeSeedJobs(batch, true);
    // first execution updating all but the process instance
    testRule.getExecutionJobs(batch).forEach(job -> managementService.executeJob(job.getId()));

    HistoricProcessInstance historicProcessInstance = query.singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isNull();

    // when second execution updating process instance
    testRule.getExecutionJobs(batch).forEach(job -> managementService.executeJob(job.getId()));

    // then
    historicProcessInstance = query.singleResult();

    assertThat(testRule.getExecutionJobs(batch)).isEmpty();
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldUpdateProcessInstanceLast_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    Batch batch = historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .chunkSize(100) // most data updated in one chunk
        .executeAsync();
    testRule.executeSeedJobs(batch, true);
    // first execution updating all but the process instance
    testRule.getExecutionJobs(batch).forEach(job -> managementService.executeJob(job.getId()));

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isNull();

    // when second execution updating process instance
    testRule.getExecutionJobs(batch).forEach(job -> managementService.executeJob(job.getId()));

    // then
    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    assertThat(testRule.getExecutionJobs(batch)).isEmpty();
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldCreateOneRemovalTimeJobPerProcessInstance() {
    // given
    engineConfiguration.setInvocationsPerBatchJob(3);

    TestProcessBuilder process = testRule.process().userTask().deploy();
    process.start();
    process.start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    Batch batch = historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync();

    // when
    testRule.executeSeedJobs(batch, true);

    // then
    assertThat(testRule.getExecutionJobs(batch)).hasSize(2);
  }

  @Test
  public void shouldRejectChunkSize_Zero() {
    // given
    engineConfiguration.setRemovalTimeUpdateChunkSize(0);

    // when
    assertThatThrownBy(() -> engineConfiguration.initBatchHandlers())

    // then
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("chunk size should be between 1 and");
  }

  @Test
  public void shouldRejectChunkSize_Negative() {
    // given
    engineConfiguration.setRemovalTimeUpdateChunkSize(-5);

    // when
    assertThatThrownBy(() -> engineConfiguration.initBatchHandlers())

    // then
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("chunk size should be between 1 and");
  }

  @Test
  public void shouldRejectChunkSize_ExceedingLimit() {
    // given
    engineConfiguration.setRemovalTimeUpdateChunkSize(ProcessSetRemovalTimeJobHandler.MAX_CHUNK_SIZE + 5);

    // when
    assertThatThrownBy(() -> engineConfiguration.initBatchHandlers())

    // then
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("chunk size should be between 1 and");
  }

  @Test
  public void shouldRejectCustomChunkSize_Zero() {
    // given
    final SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builder =
        historyService.setRemovalTimeToHistoricProcessInstances();

    // when
    assertThatThrownBy(() -> builder.chunkSize(0))

    // then
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("chunk size should be between 1 and");
  }

  @Test
  public void shouldRejectCustomChunkSize_Negative() {
    // given
    final SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builder =
        historyService.setRemovalTimeToHistoricProcessInstances();

    // when
    assertThatThrownBy(() -> builder.chunkSize(-3))

    // then
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("chunk size should be between 1 and");
  }

  @Test
  public void shouldRejectCustomChunkSize_ExceedingLimit() {
    // given
    final SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builder =
        historyService.setRemovalTimeToHistoricProcessInstances();

    // when
    assertThatThrownBy(() -> builder.chunkSize(ProcessSetRemovalTimeJobHandler.MAX_CHUNK_SIZE + 1))

    // then
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("chunk size should be between 1 and");
  }

  // NON-HIERARCHICAL TEST CASES

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTime_DecisionInstance() {
    // given
    testRule.process().ruleTask("dish-decision").deploy().startWithVariables(
      Variables.createVariables()
      .putValue("temperature", 32)
      .putValue("dayType", "Weekend")
    );

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTime_DecisionInputInstance() {
    // given
    testRule.process().ruleTask("dish-decision").deploy().startWithVariables(
      Variables.createVariables()
      .putValue("temperature", 32)
      .putValue("dayType", "Weekend")
    );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInputInstances.get(1).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
    assertThat(historicDecisionInputInstances.get(1).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTime_DecisionOutputInstance() {
    // given
    testRule.process().ruleTask("dish-decision").deploy().startWithVariables(
      Variables.createVariables()
      .putValue("temperature", 32)
      .putValue("dayType", "Weekend")
    );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // then
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // then
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_ProcessInstance() {
    // given
    testRule.process().userTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // assume
    assertThat(historicProcessInstance.getRemovalTime()).isNull();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();

    // then
    assertThat(historicProcessInstance.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_ActivityInstance() {
    // given
    testRule.process().userTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
      .activityName("userTask")
      .singleResult();

    // then
    assertThat(historicActivityInstance.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_TaskInstance() {
    // given
    testRule.process().userTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();

    // then
    assertThat(historicTaskInstance.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_HistoricTaskInstanceAuthorization() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(true);

    testRule.enableAuth();
    testRule.process().userTask().deploy().start();
    testRule.disableAuth();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    Authorization authorization =
        authorizationService.createAuthorizationQuery()
            .resourceType(Resources.HISTORIC_TASK)
            .singleResult();

    // then
    assertThat(authorization.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldNotSetRemovalTime_HistoricTaskInstancePermissionsDisabled() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(true);

    testRule.enableAuth();
    testRule.process().userTask().deploy().start();
    testRule.disableAuth();

    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(false);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    Authorization authorization =
        authorizationService.createAuthorizationQuery()
            .resourceType(Resources.HISTORIC_TASK)
            .singleResult();

    // then
    assertThat(authorization.getRemovalTime()).isNull();
  }

  @Test
  public void shouldSetRemovalTime_HistoricProcessInstanceAuthorization() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(true);

    String processInstanceId = testRule.process().userTask().deploy().start();

    Authorization authorization =
        authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.HISTORIC_PROCESS_INSTANCE);
    authorization.setResourceId(processInstanceId);
    authorization.setUserId("foo");

    authorizationService.saveAuthorization(authorization);

    // assume
    AuthorizationQuery authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(null, processInstanceId, processInstanceId));

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    testRule.syncExec(
        historyService.setRemovalTimeToHistoricProcessInstances()
            .absoluteRemovalTime(REMOVAL_TIME)
            .byQuery(query)
            .updateInChunks()
            .executeAsync()
    );

    // then
    authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(REMOVAL_TIME, processInstanceId, processInstanceId));
  }

  @Test
  public void shouldNotSetRemovalTime_HistoricProcessInstancePermissionsDisabled() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(false);

    String processInstanceId = testRule.process().userTask().deploy().start();

    Authorization authorization =
        authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.HISTORIC_PROCESS_INSTANCE);
    authorization.setResourceId(processInstanceId);
    authorization.setUserId("foo");

    authorizationService.saveAuthorization(authorization);

    // assume
    AuthorizationQuery authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(null, processInstanceId, processInstanceId));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
        historyService.setRemovalTimeToHistoricProcessInstances()
            .absoluteRemovalTime(REMOVAL_TIME)
            .byQuery(query)
            .updateInChunks()
            .executeAsync()
    );

    // then
    authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(null, processInstanceId, processInstanceId));
  }

  @Test
  public void shouldSetRemovalTime_VariableInstance() {
    // given
    testRule.process().userTask().deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("aVariableName", "aVariableValue"));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    // then
    assertThat(historicVariableInstance.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_Detail() {
    // given
    testRule.process().userTask().deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("aVariableName", "aVariableValue"));

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    HistoricDetail historicDetail = historyService.createHistoricDetailQuery().singleResult();

    // then
    assertThat(historicDetail.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_ExternalTaskLog() {
    // given
    testRule.process().externalTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    HistoricExternalTaskLog historicExternalTaskLog = historyService.createHistoricExternalTaskLogQuery().singleResult();

    // assume
    assertThat(historicExternalTaskLog.getRemovalTime()).isNull();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    historicExternalTaskLog = historyService.createHistoricExternalTaskLogQuery().singleResult();

    // then
    assertThat(historicExternalTaskLog.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  /**
   * See https://app.camunda.com/jira/browse/CAM-10172
   */
  @Test
  public void shouldSetRemovalTime_ExternalTaskLog_WithPreservedCreateTime() {
    // given
    ClockUtil.setCurrentTime(CREATE_TIME);

    testRule.process().externalTask().deploy().start();

    HistoricExternalTaskLog historicExternalTaskLog = historyService.createHistoricExternalTaskLogQuery().singleResult();

    // assume
    assertThat(historicExternalTaskLog.getTimestamp()).isEqualTo(CREATE_TIME);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    historicExternalTaskLog = historyService.createHistoricExternalTaskLogQuery().singleResult();

    // then
    assertThat(historicExternalTaskLog.getTimestamp()).isEqualTo(CREATE_TIME);
  }

  @Test
  public void shouldSetRemovalTime_JobLog() {
    // given
    String processInstanceId = testRule.process().async().userTask().deploy().start();

    HistoricJobLog job = historyService.createHistoricJobLogQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    // assume
    assertThat(job.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    job = historyService.createHistoricJobLogQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    // then
    assertThat(job.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_Incident() {
    // given
    testRule.process().async().userTask().deploy().start();

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.setJobRetries(jobId, 0);

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();

    // assume
    assertThat(historicIncident.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    historicIncident = historyService.createHistoricIncidentQuery().singleResult();

    // then
    assertThat(historicIncident.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  /**
   * See https://app.camunda.com/jira/browse/CAM-10172
   */
  @Test
  public void shouldSetRemovalTime_Incident_WithPreservedCreateTime() {
    // given
    ClockUtil.setCurrentTime(CREATE_TIME);

    testRule.process().async().userTask().deploy().start();

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.setJobRetries(jobId, 0);

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();

    // assume
    assertThat(historicIncident.getCreateTime()).isEqualTo(CREATE_TIME);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    historicIncident = historyService.createHistoricIncidentQuery().singleResult();

    // then
    assertThat(historicIncident.getCreateTime()).isEqualTo(CREATE_TIME);
  }

  @Test
  public void shouldSetRemovalTime_OperationLog() {
    // given
    String processInstanceId = testRule.process().async().userTask().deploy().start();

    identityService.setAuthenticatedUserId("aUserId");
    runtimeService.suspendProcessInstanceById(processInstanceId);
    identityService.clearAuthentication();

    UserOperationLogEntry userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // assume
    assertThat(userOperationLog.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // then
    assertThat(userOperationLog.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  /**
   * See https://app.camunda.com/jira/browse/CAM-10172
   */
  @Test
  public void shouldSetRemovalTime_OperationLog_WithPreservedTimestamp() {
    // given
    ClockUtil.setCurrentTime(CREATE_TIME);

    String processInstanceId = testRule.process().async().userTask().deploy().start();

    identityService.setAuthenticatedUserId("aUserId");
    runtimeService.suspendProcessInstanceById(processInstanceId);
    identityService.clearAuthentication();

    UserOperationLogEntry userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // assume
    assertThat(userOperationLog.getTimestamp()).isEqualTo(CREATE_TIME);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // then
    assertThat(userOperationLog.getTimestamp()).isEqualTo(CREATE_TIME);
  }

  @Test
  public void shouldSetRemovalTime_IdentityLinkLog() {
    // given
    testRule.process().userTask().deploy().start();

    HistoricIdentityLinkLog identityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // assume
    assertThat(identityLinkLog.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    identityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // then
    assertThat(identityLinkLog.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  /**
   * See https://app.camunda.com/jira/browse/CAM-10172
   */
  @Test
  public void shouldSetRemovalTime_IdentityLinkLog_WithPreservedTime() {
    // given
    ClockUtil.setCurrentTime(CREATE_TIME);

    testRule.process().userTask().deploy().start();

    HistoricIdentityLinkLog identityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // assume
    assertThat(identityLinkLog.getTime()).isEqualTo(CREATE_TIME);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    identityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // then
    assertThat(identityLinkLog.getTime()).isEqualTo(CREATE_TIME);
  }

  @Test
  public void shouldNotSetUnaffectedRemovalTime_IdentityLinkLog() {
    // given
    TestProcessBuilder testProcessBuilder = testRule.process().userTask().deploy();

    String instance1 = testProcessBuilder.start();
    String instance2 = testProcessBuilder.start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query.processInstanceId(instance1))
        .updateInChunks()
        .executeAsync()
    );

    Task task2 = taskService.createTaskQuery().processInstanceId(instance2).singleResult();

    HistoricIdentityLinkLog identityLinkLog = historyService.createHistoricIdentityLinkLogQuery()
        .taskId(task2.getId()).singleResult();

    // then
    assertThat(identityLinkLog.getRemovalTime()).isNull();
  }

  @Test
  public void shouldSetRemovalTime_CommentByTaskId() {
    // given
    testRule.process().userTask().deploy().start();

    String taskId = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult()
      .getId();

    taskService.createComment(taskId, null, "aComment");

    Comment comment = taskService.getTaskComments(taskId).get(0);

    // assume
    assertThat(comment.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    comment = taskService.getTaskComments(taskId).get(0);

    // then
    assertThat(comment.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_CommentByProcessInstanceId() {
    // given
    String processInstanceId = testRule.process().userTask().deploy().start();

    taskService.createComment(null, processInstanceId, "aComment");

    Comment comment = taskService.getProcessInstanceComments(processInstanceId).get(0);

    // assume
    assertThat(comment.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    comment = taskService.getProcessInstanceComments(processInstanceId).get(0);

    // then
    assertThat(comment.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_AttachmentByTaskId() {
    // given
    testRule.process().userTask().deploy().start();

    String taskId = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult()
      .getId();

    Attachment attachment = taskService.createAttachment(null, taskId,
      null, null, null, "http://camunda.com");

    // assume
    assertThat(attachment.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    attachment = taskService.getTaskAttachments(taskId).get(0);

    // then
    assertThat(attachment.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_AttachmentByProcessInstanceId() {
    // given
    String processInstanceId = testRule.process().userTask().deploy().start();

    Attachment attachment = taskService.createAttachment(null, null,
      processInstanceId, null, null, "http://camunda.com");

    // assume
    assertThat(attachment.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    attachment = taskService.getProcessInstanceAttachments(processInstanceId).get(0);

    // then
    assertThat(attachment.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_AttachmentByTaskId() {
    // given
    testRule.process().userTask().deploy().start();

    String taskId = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult()
      .getId();

    AttachmentEntity attachment = (AttachmentEntity) taskService.createAttachment(null, taskId,
      null, null, null, new ByteArrayInputStream("".getBytes()));

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(attachment.getContentId());

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(attachment.getContentId());

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_AttachmentByProcessInstanceId() {
    // given
    String processInstanceId = testRule.process().userTask().deploy().start();

    AttachmentEntity attachment = (AttachmentEntity) taskService.createAttachment(null, null,
      processInstanceId, null, null, new ByteArrayInputStream("".getBytes()));

    String byteArrayId = attachment.getContentId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_Variable() {
    // given
    testRule.process().userTask().deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("aVariableName",
            Variables.fileValue("file.xml")
              .file("<root />".getBytes())));

    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    String byteArrayId = ((HistoricVariableInstanceEntity) historicVariableInstance).getByteArrayId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_JobLog() {
    // given
    testRule.process().async().scriptTask().deploy().start();

    String jobId = managementService.createJobQuery().singleResult().getId();

    try {
      managementService.executeJob(jobId);

    } catch (Exception ignored) { }

    HistoricJobLog historicJobLog = historyService.createHistoricJobLogQuery()
      .failureLog()
      .singleResult();

    String byteArrayId = ((HistoricJobLogEventEntity) historicJobLog).getExceptionByteArrayId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_ExternalTaskLog() {
    // given
    testRule.process().externalTask().deploy().start();

    String externalTaskId = externalTaskService.fetchAndLock(1, "aWorkerId")
      .topic("aTopicName", Integer.MAX_VALUE)
      .execute()
      .get(0)
      .getId();

    externalTaskService.handleFailure(externalTaskId, "aWorkerId",
      null, "errorDetails", 5, 3000L);

    HistoricExternalTaskLog externalTaskLog = historyService.createHistoricExternalTaskLogQuery()
      .failureLog()
      .singleResult();

    String byteArrayId = ((HistoricExternalTaskLogEntity) externalTaskLog).getErrorDetailsByteArrayId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml"
  })
  public void shouldSetRemovalTime_ByteArray_DecisionInputInstance() {
    // given
    testRule.process().ruleTask("testDecision").deploy().startWithVariables(
      Variables.createVariables()
        .putValue("pojo", new TestPojo("okay", 13.37))
    );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    String byteArrayId = ((HistoricDecisionInputInstanceEntity) historicDecisionInstance.getInputs().get(0))
      .getByteArrayValueId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml"
  })
  public void shouldSetRemovalTime_ByteArray_DecisionOutputInstance() {
    // given
    testRule.process().ruleTask("testDecision").deploy().startWithVariables(
      Variables.createVariables()
        .putValue("pojo", new TestPojo("okay", 13.37))
    );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    String byteArrayId = ((HistoricDecisionOutputInstanceEntity) historicDecisionInstance.getOutputs().get(0))
      .getByteArrayValueId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  // HIERARCHICAL TEST CASES

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTime_DecisionInstance_Hierarchical() {
    // given
    testRule.process()
      .call()
        .passVars("temperature", "dayType")
      .ruleTask("dish-decision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      );

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTime_DecisionInputInstance_Hierarchical() {
    // given
    testRule.process()
      .call()
        .passVars("temperature", "dayType")
      .ruleTask("dish-decision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend")
      );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // assume
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInputInstances.get(1).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicDecisionInputInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTime_DecisionOutputInstance_Hierarchical() {
    // given
    testRule.process()
      .call()
        .passVars("temperature", "dayType")
      .ruleTask("dish-decision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend")
      );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // assume
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // then
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ProcessInstance_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().rootProcessInstances().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ActivityInstance_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy().start();

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
      .activityName("userTask")
      .singleResult();

    // assume
    assertThat(historicActivityInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
      .activityName("userTask")
      .singleResult();

    // then
    assertThat(historicActivityInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_TaskInstance_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy().start();

    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();

    // assume
    assertThat(historicTaskInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();

    // then
    assertThat(historicTaskInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_HistoricTaskInstanceAuthorization_Hierarchical() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(true);

    testRule.enableAuth();
    testRule.process().call().userTask().deploy().start();
    testRule.disableAuth();

    HistoricTaskInstance historicTaskInstance =
        historyService.createHistoricTaskInstanceQuery().singleResult();

    // assume
    assertThat(historicTaskInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query =
        historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    Authorization authorization =
        authorizationService.createAuthorizationQuery()
            .resourceType(Resources.HISTORIC_TASK)
            .singleResult();

    // then
    assertThat(authorization.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldNotSetRemovalTime_HistoricTaskInstancePermissionsDisabled_Hierarchical() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(true);

    testRule.enableAuth();
    testRule.process().call().userTask().deploy().start();
    testRule.disableAuth();

    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(false);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query =
        historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    Authorization authorization =
        authorizationService.createAuthorizationQuery()
            .resourceType(Resources.HISTORIC_TASK)
            .singleResult();

    // then
    assertThat(authorization.getRemovalTime()).isNull();
  }

  @Test
  public void shouldSetRemovalTime_HistoricProcessInstanceAuthorization_Hierarchical() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(true);

    String rootProcessInstanceId = testRule.process().call().userTask().deploy().start();

    Authorization authorization =
        authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.HISTORIC_PROCESS_INSTANCE);

    String processInstanceId = historyService.createHistoricProcessInstanceQuery()
        .activeActivityIdIn("userTask")
        .singleResult()
        .getId();

    authorization.setResourceId(processInstanceId);
    authorization.setUserId("foo");

    authorizationService.saveAuthorization(authorization);

    // assume
    AuthorizationQuery authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(null, processInstanceId, rootProcessInstanceId));

    // when
    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query =
        historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    testRule.syncExec(
        historyService.setRemovalTimeToHistoricProcessInstances()
            .calculatedRemovalTime()
            .byQuery(query)
            .hierarchical()
            .updateInChunks()
            .executeAsync()
    );

    // then
    authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    Date removalTime = addDays(CURRENT_DATE, 5);
    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(removalTime, processInstanceId, rootProcessInstanceId));
  }

  @Test
  public void shouldNotSetRemovalTime_HistoricProcessInstancePermissionsDisabled_Hierarchical() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(false);

    String rootProcessInstanceId = testRule.process().call().userTask().deploy().start();

    Authorization authorization =
        authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.HISTORIC_PROCESS_INSTANCE);

    String processInstanceId = historyService.createHistoricProcessInstanceQuery()
        .activeActivityIdIn("userTask")
        .singleResult()
        .getId();

    authorization.setResourceId(processInstanceId);
    authorization.setUserId("foo");

    authorizationService.saveAuthorization(authorization);

    // assume
    AuthorizationQuery authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(null, processInstanceId, rootProcessInstanceId));

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // when
    HistoricProcessInstanceQuery query =
        historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    testRule.syncExec(
        historyService.setRemovalTimeToHistoricProcessInstances()
            .calculatedRemovalTime()
            .byQuery(query)
            .hierarchical()
            .updateInChunks()
            .executeAsync()
    );

    // then
    authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(null, processInstanceId, rootProcessInstanceId));
  }

  @Test
  public void shouldSetRemovalTime_VariableInstance_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("aVariableName", "aVariableValue"));

    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    // assume
    assertThat(historicVariableInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    // then
    assertThat(historicVariableInstance.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_Detail_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("aVariableName", "aVariableValue"));

    HistoricDetail historicDetail = historyService.createHistoricDetailQuery().singleResult();

    // assume
    assertThat(historicDetail.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicDetail = historyService.createHistoricDetailQuery().singleResult();

    // then
    assertThat(historicDetail.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ExternalTaskLog_Hierarchical() {
    // given
    testRule.process().call().externalTask().deploy().start();

    HistoricExternalTaskLog historicExternalTaskLog = historyService.createHistoricExternalTaskLogQuery().singleResult();

    // assume
    assertThat(historicExternalTaskLog.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicExternalTaskLog = historyService.createHistoricExternalTaskLogQuery().singleResult();

    // then
    assertThat(historicExternalTaskLog.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_JobLog_Hierarchical() {
    // given
    testRule.process().call().async().userTask().deploy().start();

    HistoricJobLog job = historyService.createHistoricJobLogQuery()
      .processDefinitionKey("process")
      .singleResult();

    // assume
    assertThat(job.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    job = historyService.createHistoricJobLogQuery()
      .processDefinitionKey("process")
      .singleResult();

    // then
    assertThat(job.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_Incident_Hierarchical() {
    // given
    String rootProcessInstanceId = testRule.process().call().async().userTask().deploy().start();

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.setJobRetries(jobId, 0);

    String leafProcessInstanceId = historyService.createHistoricProcessInstanceQuery()
      .superProcessInstanceId(rootProcessInstanceId)
      .singleResult()
      .getId();

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery()
      .processInstanceId(leafProcessInstanceId)
      .singleResult();

    // assume
    assertThat(historicIncident.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    historicIncident = historyService.createHistoricIncidentQuery()
      .processInstanceId(leafProcessInstanceId)
      .singleResult();

    // then
    assertThat(historicIncident.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_OperationLog_Hierarchical() {
    // given
    String processInstanceId = testRule.process().call().async().userTask().deploy().start();

    identityService.setAuthenticatedUserId("aUserId");
    runtimeService.suspendProcessInstanceById(processInstanceId);
    identityService.clearAuthentication();

    UserOperationLogEntry userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // assume
    assertThat(userOperationLog.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // then
    assertThat(userOperationLog.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_IdentityLinkLog_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy().start();

    HistoricIdentityLinkLog identityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // assume
    assertThat(identityLinkLog.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    identityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // then
    assertThat(identityLinkLog.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_CommentByTaskId_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy().start();

    String taskId = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult()
      .getId();

    taskService.createComment(taskId, null, "aComment");

    Comment comment = taskService.getTaskComments(taskId).get(0);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(comment.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    comment = taskService.getTaskComments(taskId).get(0);

    // then
    assertThat(comment.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_CommentByProcessInstanceId_Hierarchical() {
    // given
    String processInstanceId = testRule.process().call().userTask().deploy().start();

    taskService.createComment(null, processInstanceId, "aComment");

    Comment comment = taskService.getProcessInstanceComments(processInstanceId).get(0);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(comment.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    comment = taskService.getProcessInstanceComments(processInstanceId).get(0);

    // then
    assertThat(comment.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_AttachmentByTaskId_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy().start();

    String taskId = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult()
      .getId();

    Attachment attachment = taskService.createAttachment(null, taskId,
      null, null, null, "http://camunda.com");

    // assume
    assertThat(attachment.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    attachment = taskService.getTaskAttachments(taskId).get(0);

    // then
    assertThat(attachment.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_AttachmentByProcessInstanceId_Hierarchical() {
    // given
    String processInstanceId = testRule.process().call().userTask().deploy().start();

    Attachment attachment = taskService.createAttachment(null, null,
      processInstanceId, null, null, "http://camunda.com");

    // assume
    assertThat(attachment.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    attachment = taskService.getProcessInstanceAttachments(processInstanceId).get(0);

    // then
    assertThat(attachment.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_AttachmentByTaskId_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy().start();

    String taskId = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult()
      .getId();

    AttachmentEntity attachment = (AttachmentEntity) taskService.createAttachment(null, taskId,
      null, null, null, new ByteArrayInputStream("".getBytes()));

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(attachment.getContentId());

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(attachment.getContentId());

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_AttachmentByProcessInstanceId_Hierarchical() {
    // given
    String processInstanceId = testRule.process().call().userTask().deploy().start();

    AttachmentEntity attachment = (AttachmentEntity) taskService.createAttachment(null, null,
      processInstanceId, null, null, new ByteArrayInputStream("".getBytes()));

    String byteArrayId = attachment.getContentId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_Variable_Hierarchical() {
    // given
    testRule.process().call().userTask().deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("aVariableName",
            Variables.fileValue("file.xml")
              .file("<root />".getBytes())));

    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    String byteArrayId = ((HistoricVariableInstanceEntity) historicVariableInstance).getByteArrayId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_JobLog_Hierarchical() {
    // given
    testRule.process().call().async().scriptTask().deploy().start();

    String jobId = managementService.createJobQuery().singleResult().getId();

    try {
      managementService.executeJob(jobId);

    } catch (Exception ignored) { }

    HistoricJobLog historicJobLog = historyService.createHistoricJobLogQuery()
      .failureLog()
      .singleResult();

    String byteArrayId = ((HistoricJobLogEventEntity) historicJobLog).getExceptionByteArrayId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  public void shouldSetRemovalTime_ByteArray_ExternalTaskLog_Hierarchical() {
    // given
    testRule.process().call().externalTask().deploy().start();

    String externalTaskId = externalTaskService.fetchAndLock(1, "aWorkerId")
      .topic("aTopicName", Integer.MAX_VALUE)
      .execute()
      .get(0)
      .getId();

    externalTaskService.handleFailure(externalTaskId, "aWorkerId",
      null, "errorDetails", 5, 3000L);

    HistoricExternalTaskLog externalTaskLog = historyService.createHistoricExternalTaskLogQuery()
      .failureLog()
      .singleResult();

    String byteArrayId = ((HistoricExternalTaskLogEntity) externalTaskLog).getErrorDetailsByteArrayId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml"
  })
  public void shouldSetRemovalTime_ByteArray_DecisionInputInstance_Hierarchical() {
    // given
    testRule.process()
      .call()
        .passVars("pojo")
      .ruleTask("testDecision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("pojo", new TestPojo("okay", 13.37))
      );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    String byteArrayId = ((HistoricDecisionInputInstanceEntity) historicDecisionInstance.getInputs().get(0))
      .getByteArrayValueId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml"
  })
  public void shouldSetRemovalTime_ByteArray_DecisionOutputInstance_Hierarchical() {
    // given
    testRule.process()
      .call()
        .passVars("pojo")
      .ruleTask("testDecision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("pojo", new TestPojo("okay", 13.37))
      );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    String byteArrayId = ((HistoricDecisionOutputInstanceEntity) historicDecisionInstance.getOutputs().get(0))
      .getByteArrayValueId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .updateInChunks()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(CURRENT_DATE, 5));
  }
}
