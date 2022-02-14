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
package org.camunda.bpm.engine.test.api.history.removaltime.cleanup;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addMinutes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;
import static org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHandler.MAX_BATCH_SIZE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReportResult;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.DefaultHistoryRemovalTimeProvider;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.resources.GetByteArrayCommand;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
@RequiredHistoryLevel(HISTORY_FULL)
public class HistoryCleanupRemovalTimeTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected FormService formService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected ManagementService managementService;
  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ExternalTaskService externalTaskService;
  protected DecisionService decisionService;
  protected AuthorizationService authorizationService;

  protected static ProcessEngineConfigurationImpl engineConfiguration;

  protected Set<String> jobIds;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    formService = engineRule.getFormService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    managementService = engineRule.getManagementService();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
    externalTaskService = engineRule.getExternalTaskService();
    decisionService = engineRule.getDecisionService();
    authorizationService = engineRule.getAuthorizationService();

    engineConfiguration = engineRule.getProcessEngineConfiguration();

    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .setHistoryRemovalTimeProvider(new DefaultHistoryRemovalTimeProvider())
      .initHistoryRemovalTime();

    engineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED);

    engineConfiguration.setHistoryCleanupBatchSize(MAX_BATCH_SIZE);
    engineConfiguration.setHistoryCleanupBatchWindowStartTime(null);
    engineConfiguration.setHistoryCleanupDegreeOfParallelism(1);

    engineConfiguration.setBatchOperationHistoryTimeToLive(null);
    engineConfiguration.setBatchOperationsForHistoryCleanup(null);

    engineConfiguration.setHistoryTimeToLive(null);

    engineConfiguration.setTaskMetricsEnabled(false);
    engineConfiguration.setTaskMetricsTimeToLive(null);

    engineConfiguration.initHistoryCleanup();

    jobIds = new HashSet<>();
  }

  @After
  public void tearDown() {
    clearMeterLog();

    for (String jobId : jobIds) {
      clearJobLog(jobId);
      clearJob(jobId);
    }
  }

  @AfterClass
  public static void tearDownAfterAll() {
    if (engineConfiguration != null) {
      engineConfiguration
        .setHistoryRemovalTimeProvider(null)
        .setHistoryRemovalTimeStrategy(null)
        .initHistoryRemovalTime();

      engineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED);

      engineConfiguration.setHistoryCleanupBatchSize(MAX_BATCH_SIZE);
      engineConfiguration.setHistoryCleanupBatchWindowStartTime(null);
      engineConfiguration.setHistoryCleanupDegreeOfParallelism(1);

      engineConfiguration.setBatchOperationHistoryTimeToLive(null);
      engineConfiguration.setBatchOperationsForHistoryCleanup(null);

      engineConfiguration.setHistoryCleanupJobLogTimeToLive(null);

      engineConfiguration.setTaskMetricsTimeToLive(null);

      engineConfiguration.initHistoryCleanup();

      engineConfiguration.setAuthorizationEnabled(false);
      engineConfiguration.setEnableHistoricInstancePermissions(false);
      engineConfiguration.setTaskMetricsEnabled(false);
    }

    ClockUtil.reset();
  }

  protected final String PROCESS_KEY = "process";
  protected final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess(PROCESS_KEY)
    .camundaHistoryTimeToLive(5)
    .startEvent()
      .userTask("userTask").name("userTask")
    .endEvent().done();


  protected final BpmnModelInstance CALLED_PROCESS_INCIDENT = Bpmn.createExecutableProcess(PROCESS_KEY)
    .startEvent()
      .scriptTask()
        .camundaAsyncBefore()
        .scriptFormat("groovy")
        .scriptText("if(execution.getIncidents().size() == 0) throw new RuntimeException(\"I'm supposed to fail!\")")
      .userTask("userTask")
    .endEvent().done();

  protected final String CALLING_PROCESS_KEY = "callingProcess";
  protected final BpmnModelInstance CALLING_PROCESS = Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
    .camundaHistoryTimeToLive(5)
    .startEvent()
      .callActivity()
        .calledElement(PROCESS_KEY)
    .endEvent().done();

  protected final BpmnModelInstance CALLING_PROCESS_WO_TTL = Bpmn.createExecutableProcess(CALLING_PROCESS_KEY)
      .startEvent()
        .callActivity()
          .calledElement(PROCESS_KEY)
      .endEvent().done();

  protected final String CALLING_PROCESS_CALLS_DMN_KEY = "callingProcessCallsDmn";
  protected final BpmnModelInstance CALLING_PROCESS_CALLS_DMN = Bpmn.createExecutableProcess(CALLING_PROCESS_CALLS_DMN_KEY)
    .camundaHistoryTimeToLive(5)
    .startEvent()
      .businessRuleTask()
        .camundaAsyncAfter()
        .camundaDecisionRef("dish-decision")
    .endEvent().done();

  protected final Date END_DATE = new Date(1363608000000L);

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldCleanupDecisionInstance() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.executeJob(jobId);

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.size(), is(3));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.size(), is(0));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldCleanupStandaloneDecisionInstance() {
    // given
    ClockUtil.setCurrentTime(END_DATE);

    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery()
      .decisionDefinitionKey("dish-decision")
      .singleResult();
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinition.getId(), 5);


    // when
    decisionService.evaluateDecisionTableByKey("dish-decision", Variables.createVariables()
      .putValue("temperature", 32)
      .putValue("dayType", "Weekend"));

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .includeOutputs()
      .list();

    // assume
    assertThat(historicDecisionInstances.size(), is(3));

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    // when
    runHistoryCleanup();

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .includeOutputs()
      .list();

    // then
    assertThat(historicDecisionInstances.size(), is(0));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldReportMetricsForDecisionInstanceCleanup() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.executeJob(jobId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    long removedDecisionInstancesSum = managementService.createMetricsQuery()
      .name(Metrics.HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES)
      .sum();

    // then
    assertThat(removedDecisionInstancesSum, is(3L));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldCleanupDecisionInputInstance() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.executeJob(jobId);

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .list();

    // assume
    assertThat(historicDecisionInstances.size(), is(3));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .list();

    // then
    assertThat(historicDecisionInstances.size(), is(0));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldCleanupDecisionOutputInstance() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
      Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.executeJob(jobId);

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .list();

    // assume
    assertThat(historicDecisionInstances.size(), is(3));

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .list();

    // then
    assertThat(historicDecisionInstances.size(), is(0));
  }

  @Test
  public void shouldCleanupProcessInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // assume
    assertThat(historicProcessInstances.size(), is(1));

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // then
    assertThat(historicProcessInstances.size(), is(0));
  }

  @Test
  public void shouldNotCleanupProcessInstanceWithoutTTL() {
    // given
    testRule.deploy(CALLING_PROCESS_WO_TTL);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // assume
    assertThat(historicProcessInstances.size(), is(1));

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // then
    assertThat(historicProcessInstances.size(), is(1));
  }

  @Test
  public void shouldCleanupProcessInstanceWithoutTTLWithConfigDefault() {
    // given
    engineConfiguration.setHistoryTimeToLive("5");

    testRule.deploy(CALLING_PROCESS_WO_TTL);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // assume
    assertThat(historicProcessInstances.size(), is(1));

    Date removalTime = addDays(END_DATE, 5);
    ClockUtil.setCurrentTime(removalTime);

    // when
    runHistoryCleanup();

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // then
    assertThat(historicProcessInstances.size(), is(0));
  }

  @Test
  public void shouldReportMetricsForProcessInstanceCleanup() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    long removedProcessInstancesSum = managementService.createMetricsQuery()
      .name(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES)
      .sum();

    // then
    assertThat(removedProcessInstancesSum, is(2L));
  }

  @Test
  public void shouldCleanupActivityInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().list();

    // assume
    assertThat(historicActivityInstances.size(), is(6));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicActivityInstances = historyService.createHistoricActivityInstanceQuery().list();

    // then
    assertThat(historicActivityInstances.size(), is(0));
  }

  @Test
  public void shouldCleanupTaskInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    taskService.complete(taskId);

    List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();

    // assume
    assertThat(historicTaskInstances.size(), is(1));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();

    // then
    assertThat(historicTaskInstances.size(), is(0));
  }

  @Test
  public void shouldCleanupTaskInstanceAuthorization() {
    // given
    engineConfiguration.setEnableHistoricInstancePermissions(true);

    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    engineConfiguration.setAuthorizationEnabled(true);
    taskService.setAssignee(taskId, "myUserId");
    engineConfiguration.setAuthorizationEnabled(false);

    taskService.complete(taskId);

    List<Authorization> authorizations = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_TASK)
        .list();

    // assume
    assertThat(authorizations.size(), is(1));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    authorizations = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_TASK)
        .list();

    // then
    assertThat(authorizations.size(), is(0));

    // clear
    clearAuthorization();
  }

  @Test
  public void shouldCleanupVariableInstance() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    runtimeService.setVariable(processInstance.getId(), "aVariableName", Variables.stringValue("anotherVariableValue"));

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    taskService.complete(taskId);

    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery().list();

    // assume
    assertThat(historicVariableInstances.size(), is(1));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicVariableInstances = historyService.createHistoricVariableInstanceQuery().list();

    // then
    assertThat(historicVariableInstances.size(), is(0));
  }

  @Test
  public void shouldCleanupDetail() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY,
      Variables.createVariables()
        .putValue("aVariableName", Variables.stringValue("aVariableValue")));

    runtimeService.setVariable(processInstance.getId(), "aVariableName", Variables.stringValue("anotherVariableValue"));

    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
      .variableUpdates()
      .list();

    // assume
    assertThat(historicDetails.size(), is(2));

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = historyService.createHistoricTaskInstanceQuery().singleResult().getId();

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicDetails = historyService.createHistoricDetailQuery()
      .variableUpdates()
      .list();

    // then
    assertThat(historicDetails.size(), is(0));
  }

  @Test
  public void shouldCleanupIncident() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS_INCIDENT);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.setJobRetries(jobId, 0);

    try {
      managementService.executeJob(jobId);
    } catch (Exception ignored) { }

    List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();

    // assume
    assertThat(historicIncidents.size(), is(2));

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicIncidents = historyService.createHistoricIncidentQuery().list();

    // then
    assertThat(historicIncidents.size(), is(0));
  }

  @Test
  public void shouldCleanupExternalTaskLog() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("calledProcess")
      .startEvent()
        .serviceTask().camundaExternalTask("anExternalTaskTopic")
      .endEvent().done());

    testRule.deploy(Bpmn.createExecutableProcess("callingProcess")
      .camundaHistoryTimeToLive(5)
      .startEvent()
        .callActivity()
          .calledElement("calledProcess")
      .endEvent().done());

    runtimeService.startProcessInstanceByKey("callingProcess");

    LockedExternalTask externalTask = externalTaskService.fetchAndLock(1, "aWorkerId")
      .topic("anExternalTaskTopic", 3000)
      .execute()
      .get(0);

    List<HistoricExternalTaskLog> externalTaskLogs = historyService.createHistoricExternalTaskLogQuery().list();

    // assume
    assertThat(externalTaskLogs.size(), is(1));

    ClockUtil.setCurrentTime(END_DATE);

    externalTaskService.complete(externalTask.getId(), "aWorkerId");

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    externalTaskLogs = historyService.createHistoricExternalTaskLogQuery().list();

    // then
    assertThat(externalTaskLogs.size(), is(0));
  }

  @Test
  public void shouldCleanupJobLog() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent().camundaAsyncBefore()
        .userTask("userTask").name("userTask")
      .endEvent().done());

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    managementService.executeJob(jobId);

    List<HistoricJobLog> jobLogs = historyService.createHistoricJobLogQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // assume
    assertThat(jobLogs.size(), is(2));

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    jobLogs = historyService.createHistoricJobLogQuery()
      .processDefinitionKey(PROCESS_KEY)
      .list();

    // then
    assertThat(jobLogs.size(), is(0));
  }

  @Test
  public void shouldCleanupHistoryCleanupJobsFromHistoricJobLog() {
    // given
    engineConfiguration.setHistoryCleanupJobLogTimeToLive("P5D");

    ClockUtil.setCurrentTime(END_DATE);

    // when
    runHistoryCleanup();
    List<String> initialHistoryCleanupJobLog = historyService.createHistoricJobLogQuery().list().stream().map(HistoricJobLog::getId).collect(Collectors.toList());
    ClockUtil.setCurrentTime(addDays(END_DATE, 5));
    runHistoryCleanup();

    // then
    List<HistoricJobLog> finalJobLog = historyService.createHistoricJobLogQuery().list();
    assertThat(finalJobLog).hasSize(1);
    assertThat(finalJobLog).extracting("id").doesNotContainAnyElementsOf(initialHistoryCleanupJobLog);
  }

  @Test
  public void shouldNotCleanupHistoryCleanupJobsFromHistoricJobLog() {
    // given
    engineConfiguration.setHistoryCleanupJobLogTimeToLive(null);
    ClockUtil.setCurrentTime(END_DATE);

    // when
    runHistoryCleanup();
    List<String> initialHistoryCleanupJobLog = historyService.createHistoricJobLogQuery().list().stream().map(HistoricJobLog::getId).collect(Collectors.toList());
    ClockUtil.setCurrentTime(addDays(END_DATE, 5));
    runHistoryCleanup();

    // then
    List<HistoricJobLog> finalJobLog = historyService.createHistoricJobLogQuery().list();
    assertThat(finalJobLog).hasSize(3);
    assertThat(finalJobLog).extracting("id").containsAll(initialHistoryCleanupJobLog);
  }

  @Test
  public void shouldCleanupUserOperationLog() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent().camundaAsyncBefore()
        .userTask("userTask").name("userTask")
      .endEvent().done());

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    identityService.setAuthenticatedUserId("aUserId");
    managementService.setJobRetries(jobId, 65);
    identityService.clearAuthentication();

    List<UserOperationLogEntry> userOperationLogs = historyService.createUserOperationLogQuery().list();

    // assume
    assertThat(userOperationLogs.size(), is(1));

    managementService.executeJob(jobId);

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    userOperationLogs = historyService.createUserOperationLogQuery().list();

    // then
    assertThat(userOperationLogs.size(), is(0));
  }

  @Test
  public void shouldCleanupIdentityLink() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.addCandidateUser(taskId, "aUserId");

    List<HistoricIdentityLinkLog> historicIdentityLinkLogs = historyService.createHistoricIdentityLinkLogQuery().list();

    // assume
    assertThat(historicIdentityLinkLogs.size(), is(1));

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicIdentityLinkLogs = historyService.createHistoricIdentityLinkLogQuery().list();

    // then
    assertThat(historicIdentityLinkLogs.size(), is(0));
  }

  @Test
  public void shouldCleanupComment() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery()
      .activityIdIn("userTask")
      .singleResult()
      .getId();

    taskService.createComment(null, processInstanceId, "aMessage");

    List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);

    // assume
    assertThat(comments.size(), is(1));

    String taskId = taskService.createTaskQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    comments = taskService.getProcessInstanceComments(processInstanceId);

    // then
    assertThat(comments.size(), is(0));
  }

  @Test
  public void shouldCleanupAttachment() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery()
      .activityIdIn("userTask")
      .singleResult()
      .getId();

    taskService.createAttachment(null, null, processInstanceId, null, null, "http://camunda.com").getId();

    List<Attachment> attachments = taskService.getProcessInstanceAttachments(processInstanceId);

    // assume
    assertThat(attachments.size(), is(1));

    String taskId = taskService.createTaskQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    attachments = taskService.getProcessInstanceAttachments(processInstanceId);

    // then
    assertThat(attachments.size(), is(0));
  }

  @Test
  public void shouldCleanupByteArray() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS_INCIDENT);

    runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

    String jobId = managementService.createJobQuery()
      .singleResult()
      .getId();

    try {
      managementService.executeJob(jobId);
    } catch (Exception ignored) { }

    HistoricJobLogEventEntity jobLog = (HistoricJobLogEventEntity) historyService.createHistoricJobLogQuery()
      .failureLog()
      .singleResult();

    ByteArrayEntity byteArray = findByteArrayById(jobLog.getExceptionByteArrayId());

    // assume
    assertThat(byteArray, notNullValue());

    managementService.setJobRetries(jobId, 0);

    managementService.executeJob(jobId);

    ClockUtil.setCurrentTime(END_DATE);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    byteArray = findByteArrayById(jobLog.getExceptionByteArrayId());

    // then
    assertThat(byteArray, nullValue());
  }

  @Test
  public void shouldCleanupBatch() {
    // given
    engineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);

    testRule.deploy(CALLING_PROCESS);

    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    String batchId = runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstanceId), "aDeleteReason").getId();

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.executeJob(jobId);
    jobIds.add(jobId);

    List<Job> jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      managementService.executeJob(job.getId());
      jobIds.add(job.getId());
    }

    // assume
    List<HistoricBatch> historicBatches = historyService.createHistoricBatchQuery().list();

    assertThat(historicBatches.size(), is(1));

    // assume
    List<HistoricJobLog> historicJobLogs = historyService.createHistoricJobLogQuery()
      .jobDefinitionConfiguration(batchId)
      .list();

    assertThat(historicJobLogs.size(), is(6));

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    runHistoryCleanup();

    historicBatches = historyService.createHistoricBatchQuery().list();
    historicJobLogs = historyService.createHistoricJobLogQuery()
      .jobDefinitionConfiguration(batchId)
      .list();

    // then
    assertThat(historicBatches.size(), is(0));
    assertThat(historicJobLogs.size(), is(0));
  }

  @Test
  public void shouldReportMetricsForBatchCleanup() {
    // given
    engineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);

    testRule.deploy(CALLING_PROCESS);

    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstanceId), "aDeleteReason");

    ClockUtil.setCurrentTime(END_DATE);

    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.executeJob(jobId);
    jobIds.add(jobId);

    List<Job> jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      managementService.executeJob(job.getId());
      jobIds.add(job.getId());
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    List<HistoricBatch> historicBatches = historyService.createHistoricBatchQuery().list();

    // assume
    assertThat(historicBatches.size(), is(1));

    // when
    runHistoryCleanup();

    long removedBatchesSum = managementService.createMetricsQuery()
      .name(Metrics.HISTORY_CLEANUP_REMOVED_BATCH_OPERATIONS)
      .sum();

    // then
    assertThat(removedBatchesSum, is(1L));
  }

  @Test
  public void shouldCleanupTaskMetrics() {
    // given
    engineConfiguration.setTaskMetricsEnabled(true);
    engineConfiguration.setTaskMetricsTimeToLive("P5D");
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.setAssignee(taskId, "kermit");

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // assume
    assertThat(managementService.getUniqueTaskWorkerCount(null, null)).isEqualTo(1L);

    // when
    runHistoryCleanup();

    // then
    assertThat(managementService.getUniqueTaskWorkerCount(null, null)).isEqualTo(0L);
  }

  @Test
  public void shouldReportMetricsForTaskMetricsCleanup() {
    // given
    engineConfiguration.setTaskMetricsEnabled(true);
    engineConfiguration.setTaskMetricsTimeToLive("P5D");
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);

    runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();

    ClockUtil.setCurrentTime(END_DATE);

    taskService.setAssignee(taskId, "kermit");

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // assume
    assertThat(managementService.getUniqueTaskWorkerCount(null, null)).isEqualTo(1L);

    // when
    runHistoryCleanup();

    // then
    long removedMetricsSum = managementService.createMetricsQuery()
      .name(Metrics.HISTORY_CLEANUP_REMOVED_TASK_METRICS)
      .sum();

    // then
    assertThat(removedMetricsSum, is(1L));
  }

  // parallelism test cases ////////////////////////////////////////////////////////////////////////////////////////////

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldDistributeWorkForDecisions() {
    // given
    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
          Variables.createVariables()
            .putValue("temperature", 32)
            .putValue("dayType", "Weekend"));

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        String jobId = managementService.createJobQuery().singleResult().getId();
        managementService.executeJob(jobId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricDecisionInstanceQuery decisionInstanceQuery =
        historyService.createHistoricDecisionInstanceQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, decisionInstanceQuery::count, 45L);
  }

  @Test
  public void shouldDistributeWorkForProcessInstances() {
    // given
    testRule.deploy(PROCESS);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(PROCESS_KEY);

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        String taskId = taskService.createTaskQuery().singleResult().getId();
        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricProcessInstanceQuery processInstanceQuery =
        historyService.createHistoricProcessInstanceQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, processInstanceQuery::count, 15L);
  }

  @Test
  public void shouldDistributeWorkForActivityInstances() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        String taskId = taskService.createTaskQuery().singleResult().getId();

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricActivityInstanceQuery activityInstanceQuery =
        historyService.createHistoricActivityInstanceQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, activityInstanceQuery::count, 90L);
  }

  @Test
  public void shouldDistributeWorkForTaskInstances() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        String taskId = taskService.createTaskQuery().singleResult().getId();

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, taskInstanceQuery::count, 15L);
  }

  @Test
  public void shouldDistributeWorkForAuthorizations() {
    // given
    engineConfiguration.setEnableHistoricInstancePermissions(true);

    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        String taskId = taskService.createTaskQuery().singleResult().getId();

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        engineConfiguration.setAuthorizationEnabled(true);
        taskService.setAssignee(taskId, "myUserId");
        engineConfiguration.setAuthorizationEnabled(false);

        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    AuthorizationQuery authorizationQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_TASK);

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, authorizationQuery::count, 15L);

    // clear
    clearAuthorization();
  }

  @Test
  public void shouldDistributeWorkForVariableInstances() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        runtimeService.setVariable(processInstance.getId(), "aVariableName", Variables.stringValue("anotherVariableValue"));

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        String taskId = taskService.createTaskQuery().singleResult().getId();

        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricVariableInstanceQuery variableInstanceQuery =
        historyService.createHistoricVariableInstanceQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, variableInstanceQuery::count, 15L);
  }

  @Test
  public void shouldDistributeWorkForDetails() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        runtimeService.setVariable(processInstance.getId(), "aVariableName", Variables.stringValue("anotherVariableValue"));

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        String taskId = taskService.createTaskQuery().singleResult().getId();
        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricDetailQuery historicDetailQuery = historyService.createHistoricDetailQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, historicDetailQuery::count, 15L);
  }

  @Test
  public void shouldDistributeWorkForIncidents() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS_INCIDENT);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        String jobId = managementService.createJobQuery().singleResult().getId();

        managementService.setJobRetries(jobId, 0);

        try {
          managementService.executeJob(jobId);
        } catch (Exception ignored) { }

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        String taskId = taskService.createTaskQuery().singleResult().getId();
        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricIncidentQuery historicIncidentQuery = historyService.createHistoricIncidentQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, historicIncidentQuery::count, 30L);
  }

  @Test
  public void shouldDistributeWorkForExternalTaskLogs() {
    // given
    testRule.deploy(Bpmn.createExecutableProcess("calledProcess")
      .startEvent()
        .serviceTask().camundaExternalTask("anExternalTaskTopic")
      .endEvent().done());

    testRule.deploy(Bpmn.createExecutableProcess("callingProcess")
      .camundaHistoryTimeToLive(5)
      .startEvent()
        .callActivity()
          .calledElement("calledProcess")
      .endEvent().done());

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey("callingProcess");

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        LockedExternalTask externalTask = externalTaskService.fetchAndLock(1, "aWorkerId")
          .topic("anExternalTaskTopic", 3000)
          .execute()
          .get(0);

        externalTaskService.complete(externalTask.getId(), "aWorkerId");
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricExternalTaskLogQuery externalTaskLogQuery =
        historyService.createHistoricExternalTaskLogQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, externalTaskLogQuery::count, 30L);
  }

  @Test
  public void shouldDistributeWorkForJobLogs() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent().camundaAsyncBefore()
        .userTask("userTask").name("userTask")
      .endEvent().done());

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        String jobId = managementService.createJobQuery()
          .singleResult()
          .getId();

        managementService.executeJob(jobId);

        String taskId = taskService.createTaskQuery().singleResult().getId();
        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricJobLogQuery jobLogQuery = historyService.createHistoricJobLogQuery()
        .processDefinitionKey(PROCESS_KEY);

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, jobLogQuery::count, 30L);
  }

  @Test
  public void shouldDistributeWorkForUserOperationLogs() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(Bpmn.createExecutableProcess(PROCESS_KEY)
      .startEvent().camundaAsyncBefore()
        .userTask("userTask").name("userTask")
      .endEvent().done());

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        String jobId = managementService.createJobQuery()
          .singleResult()
          .getId();

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        identityService.setAuthenticatedUserId("aUserId");
        managementService.setJobRetries(jobId, 65);
        identityService.clearAuthentication();

        managementService.executeJob(jobId);

        String taskId = taskService.createTaskQuery().singleResult().getId();
        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    UserOperationLogQuery userOperationLogQuery = historyService.createUserOperationLogQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, userOperationLogQuery::count, 15L);
  }

  @Test
  public void shouldDistributeWorkForIdentityLinkLogs() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        String taskId = taskService.createTaskQuery().singleResult().getId();

        taskService.addCandidateUser(taskId, "aUserId");

        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricIdentityLinkLogQuery identityLinkLogQuery =
        historyService.createHistoricIdentityLinkLogQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, identityLinkLogQuery::count, 15L);
  }

  @Test
  public void shouldDistributeWorkForComment() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    List<String> processInstanceIds = new ArrayList<>();
    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        String processInstanceId = runtimeService.createProcessInstanceQuery()
          .activityIdIn("userTask")
          .singleResult()
          .getId();

        processInstanceIds.add(processInstanceId);

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        taskService.createComment(null, processInstanceId, "aMessage");

        String taskId = taskService.createTaskQuery().singleResult().getId();
        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, () -> getCommentCountBy(processInstanceIds), 15);
  }

  @Test
  public void shouldDistributeWorkForAttachment() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(PROCESS);

    List<String> processInstanceIds = new ArrayList<>();
    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        String processInstanceId = runtimeService.createProcessInstanceQuery()
          .activityIdIn("userTask")
          .singleResult()
          .getId();

        processInstanceIds.add(processInstanceId);

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        taskService.createAttachment(null, null, processInstanceId, null, null, "http://camunda.com").getId();

        String taskId = taskService.createTaskQuery().singleResult().getId();
        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, () -> getAttachmentCountBy(processInstanceIds), 15);
  }

  @Test
  public void shouldDistributeWorkForByteArray() {
    // given
    testRule.deploy(CALLING_PROCESS);

    testRule.deploy(CALLED_PROCESS_INCIDENT);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        runtimeService.startProcessInstanceByKey(CALLING_PROCESS_KEY);

        String jobId = managementService.createJobQuery()
          .singleResult()
          .getId();

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        try {
          managementService.executeJob(jobId);
        } catch (Exception ignored) { }

        managementService.setJobRetries(jobId, 0);

        managementService.executeJob(jobId);

        String taskId = taskService.createTaskQuery().singleResult().getId();
        taskService.complete(taskId);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, this::byteArrayCount, 15);
  }

  @Test
  public void shouldDistributeWorkForBatches() {
    // given
    engineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);

    testRule.deploy(CALLING_PROCESS);

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));

        runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstanceId), "aDeleteReason");

        String jobId = managementService.createJobQuery().singleResult().getId();
        managementService.executeJob(jobId);
        jobIds.add(jobId);

        List<Job> jobs = managementService.createJobQuery().list();
        for (Job job : jobs) {
          managementService.executeJob(job.getId());
          jobIds.add(job.getId());
        }
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    HistoricBatchQuery historicBatchQuery = historyService.createHistoricBatchQuery();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, historicBatchQuery::count, 15);
  }

  @Test
  public void shouldDistributeWorkForTaskMetrics() {
    // given
    engineConfiguration.setTaskMetricsEnabled(true);
    engineConfiguration.setTaskMetricsTimeToLive("P5D");
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);
    runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    String taskId = taskService.createTaskQuery().singleResult().getId();

    for (int i = 0; i < 60; i++) {
      if (i%4 == 0) {
        ClockUtil.setCurrentTime(addMinutes(END_DATE, i));
        taskService.setAssignee(taskId, "kermit" + i);
      }
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 6));

    engineConfiguration.setHistoryCleanupDegreeOfParallelism(3);
    engineConfiguration.initHistoryCleanup();

    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();

    // assume, when & then
    assumeWhenThenParallelizedCleanup(jobs, () -> managementService.getUniqueTaskWorkerCount(null, null), 15);
  }

  // report tests //////////////////////////////////////////////////////////////////////////////////////////////////////

  @Test
  public void shouldSeeCleanableButNoFinishedProcessInstancesInReport() {
    // given
    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    testRule.deploy(PROCESS);

    ClockUtil.setCurrentTime(END_DATE);

    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    CleanableHistoricProcessInstanceReportResult report = historyService.createCleanableHistoricProcessInstanceReport()
      .compact()
      .singleResult();

    // then
    assertThat(report.getCleanableProcessInstanceCount(), is(5L));
    assertThat(report.getFinishedProcessInstanceCount(), is(0L));
  }

  @Test
  public void shouldSeeFinishedButNoCleanableProcessInstancesInReport() {
    // given
    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    testRule.deploy(PROCESS);

    ClockUtil.setCurrentTime(END_DATE);

    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);

      String taskId = taskService.createTaskQuery().singleResult().getId();
      taskService.complete(taskId);
    }

    // when
    CleanableHistoricProcessInstanceReportResult report = historyService.createCleanableHistoricProcessInstanceReport()
      .compact()
      .singleResult();

    // then
    assertThat(report.getFinishedProcessInstanceCount(), is(5L));
    assertThat(report.getCleanableProcessInstanceCount(), is(0L));
  }

  @Test
  public void shouldNotSeeCleanableProcessInstancesReport() {
    // given
    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    testRule.deploy(PROCESS);

    ClockUtil.setCurrentTime(END_DATE);

    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    CleanableHistoricProcessInstanceReportResult report = historyService.createCleanableHistoricProcessInstanceReport()
      .compact()
      .singleResult();

    // then
    assertThat(report, nullValue());
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldSeeCleanableDecisionInstancesInReport() {
    // given
    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    ClockUtil.setCurrentTime(END_DATE);

    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend"));
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    CleanableHistoricDecisionInstanceReportResult report = historyService.createCleanableHistoricDecisionInstanceReport()
      .decisionDefinitionKeyIn("dish-decision")
      .compact()
      .singleResult();

    // then
    assertThat(report.getCleanableDecisionInstanceCount(), is(5L));
    assertThat(report.getFinishedDecisionInstanceCount(), is(5L));
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  public void shouldNotSeeCleanableDecisionInstancesInReport() {
    // given
    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    testRule.deploy(CALLING_PROCESS_CALLS_DMN);

    ClockUtil.setCurrentTime(END_DATE);

    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey(CALLING_PROCESS_CALLS_DMN_KEY,
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend"));
    }

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    CleanableHistoricDecisionInstanceReportResult report = historyService.createCleanableHistoricDecisionInstanceReport()
      .decisionDefinitionKeyIn("dish-decision")
      .compact()
      .singleResult();

    // then
    assertThat(report.getCleanableDecisionInstanceCount(), is(0L));
    assertThat(report.getFinishedDecisionInstanceCount(), is(5L));
  }

  @Test
  public void shouldSeeCleanableBatchesInReport() {
    // given
    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START)
      .initHistoryRemovalTime();

    engineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);

    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    ClockUtil.setCurrentTime(END_DATE);

    Batch batch = runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstanceId), "aDeleteReason");

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    CleanableHistoricBatchReportResult report = historyService.createCleanableHistoricBatchReport().singleResult();

    // then
    assertThat(report.getCleanableBatchesCount(), is(1L));
    assertThat(report.getFinishedBatchesCount(), is(0L));

    // cleanup
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  public void shouldNotSeeCleanableBatchesInReport() {
    // given
    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .initHistoryRemovalTime();

    engineConfiguration.setBatchOperationHistoryTimeToLive("P5D");
    engineConfiguration.initHistoryCleanup();

    testRule.deploy(PROCESS);

    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    ClockUtil.setCurrentTime(END_DATE);

    Batch batch = runtimeService.deleteProcessInstancesAsync(Collections.singletonList(processInstanceId), "aDeleteReason");

    ClockUtil.setCurrentTime(addDays(END_DATE, 5));

    // when
    CleanableHistoricBatchReportResult report = historyService.createCleanableHistoricBatchReport().singleResult();

    // then
    assertThat(report.getCleanableBatchesCount(), is(0L));
    assertThat(report.getFinishedBatchesCount(), is(0L));

    // cleanup
    managementService.deleteBatch(batch.getId(), true);
  }

  // helper /////////////////////////////////////////////////////////////////

  protected void assumeWhenThenParallelizedCleanup(List<Job> jobs, Supplier<Long> supplier,
                                                   long initialInstanceCount) {
    // assume
    assertThat(jobs.size(), is(3));
    assertThat(supplier.get(), is(initialInstanceCount));

    long expectedInstanceCount = initialInstanceCount-(initialInstanceCount/3);

    for (Job job : jobs) {
      String jobId = job.getId();
      jobIds.add(jobId);

      // when
      managementService.executeJob(jobId);

      // then
      assertThat(supplier.get(), is(expectedInstanceCount));

      expectedInstanceCount = expectedInstanceCount - (initialInstanceCount / 3);
    }
  }

  protected List<Job> runHistoryCleanup() {
    historyService.cleanUpHistoryAsync(true);

    List<Job> jobs = historyService.findHistoryCleanupJobs();
    for (Job job : jobs) {
      jobIds.add(job.getId());
      managementService.executeJob(job.getId());
    }

    return jobs;
  }

  protected Long getAttachmentCountBy(List<String> processInstanceIds) {
    List<Attachment> attachments = new ArrayList<>();
    for (String processInstanceId : processInstanceIds) {
      attachments.addAll(taskService.getProcessInstanceAttachments(processInstanceId));
    }

    return (long) attachments.size();
  }

  protected Long getCommentCountBy(List<String> processInstanceIds) {
    List<Comment> comments = new ArrayList<>();
    for (String processInstanceId : processInstanceIds) {
      comments.addAll(taskService.getProcessInstanceComments(processInstanceId));
    }

    return (long) comments.size();
  }

  protected ByteArrayEntity findByteArrayById(String byteArrayId) {
    return engineConfiguration.getCommandExecutorTxRequired()
      .execute(new GetByteArrayCommand(byteArrayId));
  }

  protected Long byteArrayCount() {
    List<HistoricJobLog> jobLogs = historyService.createHistoricJobLogQuery()
      .failureLog()
      .list();

    List<ByteArrayEntity> byteArrays = new ArrayList<>();
    for (HistoricJobLog jobLog: jobLogs) {
      byteArrays.add(findByteArrayById(((HistoricJobLogEventEntity) jobLog).getExceptionByteArrayId()));
    }

    return (long) byteArrays.size();
  }

  protected void clearJobLog(final String jobId) {
    CommandExecutor commandExecutor = engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        return null;
      }
    });
  }

  protected void clearJob(final String jobId) {
    engineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        JobEntity job = commandContext.getJobManager().findJobById(jobId);
        if (job != null) {
          commandContext.getJobManager().delete(job);
        }
        return null;
      }
    });
  }

  protected void clearMeterLog() {
    engineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });
  }

  protected void clearAuthorization() {
    authorizationService.createAuthorizationQuery().list()
        .forEach(authorization -> authorizationService.deleteAuthorization(authorization.getId()));
  }

}
