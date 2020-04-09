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
import static org.assertj.core.api.Assertions.tuple;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
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
import org.camunda.bpm.engine.history.UserOperationLogEntry;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
@RequiredHistoryLevel(HISTORY_FULL)
public class BatchSetRemovalTimeNonHierarchicalTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule engineTestRule = new ProcessEngineTestRule(engineRule);
  protected BatchSetRemovalTimeRule testRule = new BatchSetRemovalTimeRule(engineRule, engineTestRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(engineTestRule).around(testRule);

  protected final Date REMOVAL_TIME = testRule.REMOVAL_TIME;

  protected final Date CREATE_TIME = new Date(1363608000000L);

  protected RuntimeService runtimeService;
  protected DecisionService decisionService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected TaskService taskService;
  protected IdentityService identityService;
  protected ExternalTaskService externalTaskService;
  protected AuthorizationService authorizationService;

  @Before
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
    decisionService = engineRule.getDecisionService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();
    externalTaskService = engineRule.getExternalTaskService();
    authorizationService = engineRule.getAuthorizationService();
  }

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
  public void shouldSetRemovalTimeToStandaloneDecision_DecisionInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
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
  public void shouldSetRemovalTimeToStandaloneDecision_DecisionInputInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .includeInputs()
      .singleResult();

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .includeInputs()
      .singleResult();

    historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isEqualTo(REMOVAL_TIME);
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
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSetRemovalTimeToStandaloneDecision_DecisionOutputInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .decisionDefinitionKey("season")
      .singleResult();

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // then
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .decisionDefinitionKey("season")
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
  public void shouldSetRemovalTimeToStandaloneDecisions_ByteArray_DecisionInputInstance() {
    // given
    decisionService.evaluateDecisionByKey("testDecision")
      .variables(
        Variables.createVariables()
          .putValue("pojo", new TestPojo("okay", 13.37))
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    String byteArrayId = ((HistoricDecisionInputInstanceEntity) historicDecisionInstance.getInputs().get(0))
      .getByteArrayValueId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
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
  public void shouldSetRemovalTimeToStandaloneDecisions_ByteArray_DecisionOutputInstance() {
    // given
    decisionService.evaluateDecisionByKey("testDecision")
      .variables(
        Variables.createVariables()
        .putValue("pojo", new TestPojo("okay", 13.37))
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    String byteArrayId = ((HistoricDecisionOutputInstanceEntity) historicDecisionInstance.getOutputs().get(0))
      .getByteArrayValueId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(REMOVAL_TIME);
  }

  @Test
  public void shouldSetRemovalTimeToBatch() {
    // given
    String processInstanceId = testRule.process().userTask().deploy().start();

    Batch batch = runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstanceId), "aDeleteReason");

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();

    // assume
    assertThat(historicBatch.getRemovalTime()).isNull();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicBatch = historyService.createHistoricBatchQuery()
      .type(Batch.TYPE_PROCESS_INSTANCE_DELETION)
      .singleResult();

    // then
    assertThat(historicBatch.getRemovalTime()).isEqualTo(REMOVAL_TIME);

    // clear database
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  public void shouldSetRemovalTimeToBatch_JobLog() {
    // given
    String processInstanceId = testRule.process().userTask().deploy().start();

    Batch batch = runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstanceId), "aDeleteReason");

    HistoricJobLog historicJobLog = historyService.createHistoricJobLogQuery()
      .jobDefinitionConfiguration(batch.getId())
      .singleResult();

    // assume
    assertThat(historicJobLog.getRemovalTime()).isNull();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicJobLog = historyService.createHistoricJobLogQuery()
      .jobDefinitionConfiguration(batch.getId())
      .singleResult();

    // then
    assertThat(historicJobLog.getRemovalTime()).isEqualTo(REMOVAL_TIME);

    // clear database
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  public void shouldSetRemovalTimeToBatch_JobLogByteArray() {
    // given
    String processInstance = testRule.process().failingCustomListener().deploy().start();
    Batch batch = runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstance), "aDeleteReason");

    try {
      testRule.syncExec(batch);
    } catch (RuntimeException e) {
      // assume
      assertThat(e).hasMessage("I'm supposed to fail!");
    }

    HistoricJobLogEventEntity historicJobLog = (HistoricJobLogEventEntity) historyService.createHistoricJobLogQuery()
      .jobDefinitionConfiguration(batch.getId())
      .failureLog()
      .singleResult();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(historicJobLog.getExceptionByteArrayId());

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(historicJobLog.getExceptionByteArrayId());

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(REMOVAL_TIME);

    // clear database
    managementService.deleteBatch(batch.getId(), true);
    runtimeService.deleteProcessInstance(processInstance, "", true);
  }

  @Test
  public void shouldSetRemovalTimeToBatch_Incident() {
    // given
    String processInstance = testRule.process().failingCustomListener().deploy().start();
    Batch batch = runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstance), "aDeleteReason");

    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 0);

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();

    // assume
    assertThat(historicIncident.getRemovalTime()).isNull();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricBatches()
        .absoluteRemovalTime(REMOVAL_TIME)
        .byQuery(query)
        .executeAsync()
    );

    historicIncident = historyService.createHistoricIncidentQuery().singleResult();

    // then
    assertThat(historicIncident.getRemovalTime()).isEqualTo(REMOVAL_TIME);

    // clear database
    managementService.deleteBatch(batch.getId(), true);
    runtimeService.deleteProcessInstance(processInstance, "", true);
  }

}
