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
package org.camunda.bpm.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_END_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_OPERATOR;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CREATE_HISTORY_CLEANUP_JOB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.BatchWindowConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.HistoryCleanupCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.management.MetricIntervalValue;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.dmn.businessruletask.TestPojo;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Svetlana Dorokhova
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupTest {

  private static final int PROCESS_INSTANCES_COUNT = 3;
  private static final int DECISIONS_IN_PROCESS_INSTANCES = 3;
  private static final int DECISION_INSTANCES_COUNT = 10;
  private static final int CASE_INSTANCES_COUNT = 4;
  private static final int HISTORY_TIME_TO_LIVE = 5;
  private static final int DAYS_IN_THE_PAST = -6;
  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";
  protected static final String DECISION = "decision";
  protected static final String ONE_TASK_CASE = "case";
  private static final int NUMBER_OF_THREADS = 3;
  private static final String USER_ID = "demo";

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  protected String defaultStartTime;
  protected String defaultEndTime;
  protected int defaultBatchSize;

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
      configuration.setHistoryCleanupBatchSize(20);
      configuration.setHistoryCleanupBatchThreshold(10);
      configuration.setDefaultNumberOfRetries(5);
      configuration.setHistoryCleanupDegreeOfParallelism(NUMBER_OF_THREADS);
  });

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  private Random random = new Random();

  private HistoryService historyService;
  private RuntimeService runtimeService;
  private ManagementService managementService;
  private CaseService caseService;
  private RepositoryService repositoryService;
  private IdentityService identityService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;


  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    caseService = engineRule.getCaseService();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml", "org/camunda/bpm/engine/test/api/dmn/Example.dmn", "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithHistoryTimeToLive.cmmn");
    defaultStartTime = processEngineConfiguration.getHistoryCleanupBatchWindowStartTime();
    defaultEndTime = processEngineConfiguration.getHistoryCleanupBatchWindowEndTime();
    defaultBatchSize = processEngineConfiguration.getHistoryCleanupBatchSize();
    processEngineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_END_TIME_BASED);

    identityService.setAuthenticatedUserId(USER_ID);
  }

  @After
  public void clearDatabase() {
    //reset configuration changes
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(defaultStartTime);
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(defaultEndTime);
    processEngineConfiguration.setHistoryCleanupBatchSize(defaultBatchSize);
    processEngineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED);
    processEngineConfiguration.setHistoryCleanupEnabled(true);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = historyService.findHistoryCleanupJobs();
        for (Job job: jobs) {
          commandContext.getJobManager().deleteJob((JobEntity) job);
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
        }

        //cleanup "detached" historic job logs
        final List<HistoricJobLog> list = historyService.createHistoricJobLogQuery().list();
        for (HistoricJobLog jobLog: list) {
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobLog.getJobId());
        }

        List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery().list();
        for (HistoricIncident historicIncident : historicIncidents) {
          commandContext.getDbEntityManager().delete((HistoricIncidentEntity) historicIncident);
        }

        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance historicProcessInstance: historicProcessInstances) {
      historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
    }

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();
    for (HistoricDecisionInstance historicDecisionInstance: historicDecisionInstances) {
      historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());
    }

    List<HistoricCaseInstance> historicCaseInstances = historyService.createHistoricCaseInstanceQuery().list();
    for (HistoricCaseInstance historicCaseInstance: historicCaseInstances) {
      historyService.deleteHistoricCaseInstance(historicCaseInstance.getId());
    }

    clearMetrics();

    identityService.clearAuthentication();
  }

  protected void clearMetrics() {
    Collection<Meter> meters = processEngineConfiguration.getMetricsRegistry().getDbMeters().values();
    for (Meter meter : meters) {
      meter.getAndClear();
    }
    managementService.deleteMetrics(null);
  }

  @Test
  public void testHistoryCleanupManualRun() {
    //given
    prepareData(15);

    ClockUtil.setCurrentTime(new Date());
    //when
    runHistoryCleanup(true);

    //then
    assertResult(0);


    List<UserOperationLogEntry> userOperationLogEntries = historyService
      .createUserOperationLogQuery()
      .operationType(OPERATION_TYPE_CREATE_HISTORY_CLEANUP_JOB)
      .list();

    assertEquals(1, userOperationLogEntries.size());

    UserOperationLogEntry entry = userOperationLogEntries.get(0);
    assertEquals(CATEGORY_OPERATOR, entry.getCategory());
  }

  @Test
  public void shouldThrowExceptionWhenCleanupDisabled_1() {
    // given
    processEngineConfiguration.setHistoryCleanupEnabled(false);

    // when/then
    assertThatThrownBy(() -> historyService.cleanUpHistoryAsync())
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("History cleanup is disabled for this engine");
  }

  @Test
  public void shouldThrowExceptionWhenCleanupDisabled_2() {
    // given
    processEngineConfiguration.setHistoryCleanupEnabled(false);

    // when/then
    assertThatThrownBy(() -> historyService.cleanUpHistoryAsync(true))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("History cleanup is disabled for this engine");
  }

  @Test
  public void testDataSplitBetweenThreads() {
    //given
    prepareData(15);

    ClockUtil.setCurrentTime(new Date());

    //when
    historyService.cleanUpHistoryAsync(true).getId();
    for (Job job : historyService.findHistoryCleanupJobs()) {
      managementService.executeJob(job.getId());
      //assert that the corresponding data was removed
      final HistoryCleanupJobHandlerConfiguration jobHandlerConfiguration = getHistoryCleanupJobHandlerConfiguration(job);
      final int minuteFrom = jobHandlerConfiguration.getMinuteFrom();
      final int minuteTo = jobHandlerConfiguration.getMinuteTo();

      final List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
      for (HistoricProcessInstance historicProcessInstance: historicProcessInstances) {
        if (historicProcessInstance.getEndTime() != null) {
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(historicProcessInstance.getEndTime());
          assertTrue(minuteFrom > calendar.get(Calendar.MINUTE) || calendar.get(Calendar.MINUTE) > minuteTo);
        }
      }

      final List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();
      for (HistoricDecisionInstance historicDecisionInstance: historicDecisionInstances) {
        if (historicDecisionInstance.getEvaluationTime() != null) {
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(historicDecisionInstance.getEvaluationTime());
          assertTrue(minuteFrom > calendar.get(Calendar.MINUTE) || calendar.get(Calendar.MINUTE) > minuteTo);
        }
      }

      final List<HistoricCaseInstance> historicCaseInstances = historyService.createHistoricCaseInstanceQuery().list();
      for (HistoricCaseInstance historicCaseInstance: historicCaseInstances) {
        if (historicCaseInstance.getCloseTime() != null) {
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(historicCaseInstance.getCloseTime());
          assertTrue(minuteFrom > calendar.get(Calendar.MINUTE) || calendar.get(Calendar.MINUTE) > minuteTo);
        }
      }

    }

    assertResult(0);
  }

  private HistoryCleanupJobHandlerConfiguration getHistoryCleanupJobHandlerConfiguration(Job job) {
    return HistoryCleanupJobHandlerConfiguration
          .fromJson(JsonUtil.asObject(((JobEntity) job).getJobHandlerConfigurationRaw()));
  }

  private void runHistoryCleanup() {
    runHistoryCleanup(false);
  }

  private void runHistoryCleanup(boolean manualRun) {
    historyService.cleanUpHistoryAsync(manualRun);

    for (Job job : historyService.findHistoryCleanupJobs()) {
      managementService.executeJob(job.getId());
    }
  }

  @Test
  public void testHistoryCleanupMetrics() {
    //given
    processEngineConfiguration.setHistoryCleanupMetricsEnabled(true);
    prepareData(15);

    ClockUtil.setCurrentTime(new Date());
    //when
    runHistoryCleanup(true);

    //then
    final long removedProcessInstances = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES).sum();
    final long removedDecisionInstances = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES).sum();
    final long removedCaseInstances = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_CASE_INSTANCES).sum();

    assertTrue(removedProcessInstances > 0);
    assertTrue(removedDecisionInstances > 0);
    assertTrue(removedCaseInstances > 0);

    assertEquals(15, removedProcessInstances + removedCaseInstances + removedDecisionInstances);
  }


  @Test
  public void testHistoryCleanupMetricsExtend() {
    Date currentDate = new Date();
    // given
    processEngineConfiguration.setHistoryCleanupMetricsEnabled(true);
    prepareData(15);

    ClockUtil.setCurrentTime(currentDate);
    // when
    runHistoryCleanup(true);

    // assume
    assertResult(0);

    // then
    MetricsQuery processMetricsQuery = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES);
    long removedProcessInstances = processMetricsQuery.startDate(DateUtils.addDays(currentDate, DAYS_IN_THE_PAST)).endDate(DateUtils.addHours(currentDate, 1)).sum();
    assertEquals(5, removedProcessInstances);
    MetricsQuery decisionMetricsQuery = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES);
    long removedDecisionInstances = decisionMetricsQuery.startDate(DateUtils.addDays(currentDate, DAYS_IN_THE_PAST)).endDate(DateUtils.addHours(currentDate, 1)).sum();
    assertEquals(5, removedDecisionInstances);
    MetricsQuery caseMetricsQuery = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_CASE_INSTANCES);
    long removedCaseInstances = caseMetricsQuery.startDate(DateUtils.addDays(currentDate, DAYS_IN_THE_PAST)).endDate(DateUtils.addHours(currentDate, 1)).sum();
    assertEquals(5, removedCaseInstances);

    long noneProcessInstances = processMetricsQuery.startDate(DateUtils.addHours(currentDate, 1)).limit(1).sum();
    assertEquals(0, noneProcessInstances);
    long noneDecisionInstances = decisionMetricsQuery.startDate(DateUtils.addHours(currentDate, 1)).limit(1).sum();
    assertEquals(0, noneDecisionInstances);
    long noneCaseInstances = caseMetricsQuery.startDate(DateUtils.addHours(currentDate, 1)).limit(1).sum();
    assertEquals(0, noneCaseInstances);

    List<MetricIntervalValue> piList = processMetricsQuery.startDate(currentDate).interval(900);
    assertEquals(1, piList.size());
    assertEquals(5, piList.get(0).getValue());
    List<MetricIntervalValue> diList = decisionMetricsQuery.startDate(DateUtils.addDays(currentDate, DAYS_IN_THE_PAST)).interval(900);
    assertEquals(1, diList.size());
    assertEquals(5, diList.get(0).getValue());
    List<MetricIntervalValue> ciList = caseMetricsQuery.startDate(DateUtils.addDays(currentDate, DAYS_IN_THE_PAST)).interval(900);
    assertEquals(1, ciList.size());
    assertEquals(5, ciList.get(0).getValue());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml", "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn" })
  public void testHistoryCleanupOnlyDecisionInstancesRemoved() {
    // given
    prepareInstances(null, HISTORY_TIME_TO_LIVE, null);

    ClockUtil.setCurrentTime(new Date());
    // when
    runHistoryCleanup(true);

    // then
    assertEquals(PROCESS_INSTANCES_COUNT, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
    assertEquals(CASE_INSTANCES_COUNT, historyService.createHistoricCaseInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml", "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml", "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn"})
  public void testHistoryCleanupOnlyProcessInstancesRemoved() {
    // given
    prepareInstances(HISTORY_TIME_TO_LIVE, null, null);

    ClockUtil.setCurrentTime(new Date());
    // when
    runHistoryCleanup(true);

    // then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(DECISION_INSTANCES_COUNT + DECISIONS_IN_PROCESS_INSTANCES, historyService.createHistoricDecisionInstanceQuery().count());
    assertEquals(CASE_INSTANCES_COUNT, historyService.createHistoricCaseInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml", "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn" })
  public void testHistoryCleanupOnlyCaseInstancesRemoved() {
    // given
    prepareInstances(null, null, HISTORY_TIME_TO_LIVE);

    ClockUtil.setCurrentTime(new Date());

    // when
    runHistoryCleanup(true);

    // then
    assertEquals(PROCESS_INSTANCES_COUNT, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(DECISION_INSTANCES_COUNT + DECISIONS_IN_PROCESS_INSTANCES, historyService.createHistoricDecisionInstanceQuery().count());
    assertEquals(0, historyService.createHistoricCaseInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml", "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn" })
  public void testHistoryCleanupOnlyDecisionInstancesNotRemoved() {
    // given
    prepareInstances(HISTORY_TIME_TO_LIVE, null, HISTORY_TIME_TO_LIVE);

    ClockUtil.setCurrentTime(new Date());
    // when
    runHistoryCleanup(true);

    // then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(DECISION_INSTANCES_COUNT + DECISIONS_IN_PROCESS_INSTANCES, historyService.createHistoricDecisionInstanceQuery().count());
    assertEquals(0, historyService.createHistoricCaseInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml", "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn" })
  public void testHistoryCleanupOnlyProcessInstancesNotRemoved() {
    // given
    prepareInstances(null, HISTORY_TIME_TO_LIVE, HISTORY_TIME_TO_LIVE);

    ClockUtil.setCurrentTime(new Date());
    // when
    runHistoryCleanup(true);

    // then
    assertEquals(PROCESS_INSTANCES_COUNT, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
    assertEquals(0, historyService.createHistoricCaseInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml", "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn" })
  public void testHistoryCleanupOnlyCaseInstancesNotRemoved() {
    // given
    prepareInstances(HISTORY_TIME_TO_LIVE, HISTORY_TIME_TO_LIVE, null);

    ClockUtil.setCurrentTime(new Date());

    // when
    runHistoryCleanup(true);

    // then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
    assertEquals(CASE_INSTANCES_COUNT, historyService.createHistoricCaseInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml", "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn" })
  public void testHistoryCleanupEverythingRemoved() {
    // given
    prepareInstances(HISTORY_TIME_TO_LIVE, HISTORY_TIME_TO_LIVE, HISTORY_TIME_TO_LIVE);

    ClockUtil.setCurrentTime(new Date());
    // when
    runHistoryCleanup(true);

    // then
    assertResult(0);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml", "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn" })
  public void testHistoryCleanupNothingRemoved() {
    // given
    prepareInstances(null, null, null);

    ClockUtil.setCurrentTime(new Date());
    // when
    runHistoryCleanup(true);

    // then
    assertEquals(PROCESS_INSTANCES_COUNT, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(DECISION_INSTANCES_COUNT + DECISIONS_IN_PROCESS_INSTANCES, historyService.createHistoricDecisionInstanceQuery().count());
    assertEquals(CASE_INSTANCES_COUNT, historyService.createHistoricCaseInstanceQuery().count());
  }

  private void prepareInstances(Integer processInstanceTimeToLive, Integer decisionTimeToLive, Integer caseTimeToLive) {
    //update time to live
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey("testProcess").list();
    assertEquals(1, processDefinitions.size());
    repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinitions.get(0).getId(), processInstanceTimeToLive);

    final List<DecisionDefinition> decisionDefinitions = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey("testDecision").list();
    assertEquals(1, decisionDefinitions.size());
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinitions.get(0).getId(), decisionTimeToLive);

    List<CaseDefinition> caseDefinitions = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("oneTaskCase").list();
    assertEquals(1, caseDefinitions.size());
    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitions.get(0).getId(), caseTimeToLive);

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), DAYS_IN_THE_PAST));

    //create 3 process instances
    List<String> processInstanceIds = new ArrayList<>();
    Map<String, Object> variables = Variables.createVariables().putValue("pojo", new TestPojo("okay", 13.37));
    for (int i = 0; i < PROCESS_INSTANCES_COUNT; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", variables);
      processInstanceIds.add(processInstance.getId());
    }
    runtimeService.deleteProcessInstances(processInstanceIds, null, true, true);

    //+10 standalone decisions
    for (int i = 0; i < DECISION_INSTANCES_COUNT; i++) {
      engineRule.getDecisionService().evaluateDecisionByKey("testDecision").variables(variables).evaluate();
    }

    // create 4 case instances
    for (int i = 0; i < CASE_INSTANCES_COUNT; i++) {
      CaseInstance caseInstance = caseService.createCaseInstanceByKey("oneTaskCase",
          Variables.createVariables().putValue("pojo", new TestPojo("okay", 13.37 + i)));
      caseService.terminateCaseExecution(caseInstance.getId());
      caseService.closeCaseInstance(caseInstance.getId());
    }

    ClockUtil.setCurrentTime(oldCurrentTime);

  }

  @Test
  public void testHistoryCleanupWithinBatchWindow() {
    //given
    prepareData(15);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, HISTORY_TIME_TO_LIVE)));
    processEngineConfiguration.initHistoryCleanup();

    //when
    runHistoryCleanup();

    //then
    assertResult(0);
  }

  @Test
  public void testHistoryCleanupJobNullTTL() {
    //given
    removeHistoryTimeToLive();

    prepareData(15);

    ClockUtil.setCurrentTime(new Date());
    //when
    runHistoryCleanup(true);

    //then
    assertResult(15);
  }

  private void removeHistoryTimeToLive() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey(ONE_TASK_PROCESS).list();
    assertEquals(1, processDefinitions.size());
    repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinitions.get(0).getId(), null);

    final List<DecisionDefinition> decisionDefinitions = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(DECISION).list();
    assertEquals(1, decisionDefinitions.size());
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinitions.get(0).getId(), null);

    final List<CaseDefinition> caseDefinitions = repositoryService.createCaseDefinitionQuery().caseDefinitionKey(ONE_TASK_CASE).list();
    assertEquals(1, caseDefinitions.size());
    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitions.get(0).getId(), null);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml" })
  public void testHistoryCleanupJobDefaultTTL() {
    //given
    prepareBPMNData(15, "twoTasksProcess");

    ClockUtil.setCurrentTime(new Date());
    //when
    runHistoryCleanup(true);

    //then
    assertResult(15);
  }

  @Test
  public void testFindHistoryCleanupJob() {
    //given
    historyService.cleanUpHistoryAsync(true).getId();

    //when
    final List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();

    //then
    assertEquals(NUMBER_OF_THREADS, historyCleanupJobs.size());
  }

  @Test
  public void testRescheduleForNever() {
    //given

    //force creation of job
    historyService.cleanUpHistoryAsync(true);
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    for (Job job : historyCleanupJobs) {
      assertNotNull(job.getDuedate());
    }

    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(null);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(null);
    processEngineConfiguration.initHistoryCleanup();

    ClockUtil.setCurrentTime(new Date());

    //when
    historyService.cleanUpHistoryAsync(false);

    //then
    historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job job : historyCleanupJobs) {
      assertTrue(job.isSuspended());
      assertNull(job.getDuedate());
    }

  }

  @Test
  public void testHistoryCleanupJobResolveIncident() {
    //given
    String jobId = historyService.cleanUpHistoryAsync(true).getId();
    imitateFailedJob(jobId);

    assertEquals(5, processEngineConfiguration.getDefaultNumberOfRetries());
    //when
    //call to cleanup history means that incident was resolved
    jobId = historyService.cleanUpHistoryAsync(true).getId();

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    assertEquals(5, jobEntity.getRetries());
    assertEquals(null, jobEntity.getExceptionByteArrayId());
    assertEquals(null, jobEntity.getExceptionMessage());

  }

  private void imitateFailedJob(final String jobId) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobEntity jobEntity = getJobEntity(jobId);
        jobEntity.setRetries(0);
        jobEntity.setExceptionMessage("Something bad happened");
        jobEntity.setExceptionStacktrace(ExceptionUtil.getExceptionStacktrace(new RuntimeException("Something bad happened")));
        return null;
      }
    });
  }

  @Test
  public void testLessThanThresholdManualRun() {
    //given
    prepareData(5);

    ClockUtil.setCurrentTime(new Date());
    //when
    runHistoryCleanup(true);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());

    for (Job job : historyService.findHistoryCleanupJobs()) {
      assertTrue(job.isSuspended());
    }
  }

  @Test
  public void testNotEnoughTimeToDeleteEverything() {
    //given
    //we have something to cleanup
    prepareData(80);
    //we call history cleanup within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, HISTORY_TIME_TO_LIVE)));
    processEngineConfiguration.initHistoryCleanup();
    //job is executed once within batch window
    //we run the job in 3 threads, so not more than 60 instances can be removed in one run
    runHistoryCleanup();

    //when
    //time passed -> outside batch window
    ClockUtil.setCurrentTime(DateUtils.addHours(now, 6));
    //the job is called for the second time
    for (Job job : historyService.findHistoryCleanupJobs()) {
      managementService.executeJob(job.getId());
    }

    //then
    //second execution was not able to delete rest data
    assertResultNotLess(20);
  }

  @Test
  public void testManualRunDoesNotRespectBatchWindow() {
    //given
    //we have something to cleanup
    int processInstanceCount = 40;
    prepareData(processInstanceCount);

    //we call history cleanup outside batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 1))); //now + 1 hour
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, HISTORY_TIME_TO_LIVE)));   //now + 5 hours
    processEngineConfiguration.initHistoryCleanup();

    //when
    //job is executed before batch window start
    runHistoryCleanup(true);

    //the job is called for the second time after batch window end
    ClockUtil.setCurrentTime(DateUtils.addHours(now, 6)); //now + 6 hours
    for (Job job : historyService.findHistoryCleanupJobs()) {
      managementService.executeJob(job.getId());
    }

    //then
    assertResult(0);
  }


  @Test
  public void testLessThanThresholdWithinBatchWindow() {
    //given
    prepareData(5);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, HISTORY_TIME_TO_LIVE)));
    processEngineConfiguration.initHistoryCleanup();

    //when
    runHistoryCleanup();

    //then
    final List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job job : historyCleanupJobs) {
      JobEntity jobEntity = (JobEntity) job;
      HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

      //job rescheduled till current time + delay
      Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 0);
      assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
      Date nextRunMax = DateUtils.addSeconds(ClockUtil.getCurrentTime(), HistoryCleanupJobHandlerConfiguration.MAX_DELAY);
      assertTrue(jobEntity.getDuedate().before(nextRunMax));

      //countEmptyRuns incremented
      assertEquals(1, configuration.getCountEmptyRuns());
    }

    //data is still removed
    assertResult(0);
  }

  private Date getNextRunWithDelay(Date date, int countEmptyRuns) {
    //ignore milliseconds because MySQL does not support them, and it's not important for test
    return DateUtils.setMilliseconds(DateUtils.addSeconds(date, Math.min((int)(Math.pow(2., countEmptyRuns) * HistoryCleanupJobHandlerConfiguration.START_DELAY),
        HistoryCleanupJobHandlerConfiguration.MAX_DELAY)), 0);
  }

  private JobEntity getJobEntity(String jobId) {
    return (JobEntity)managementService.createJobQuery().jobId(jobId).list().get(0);
  }

  @Test
  public void testLessThanThresholdWithinBatchWindowAgain() {
    //given
    prepareData(5);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 1)));
    processEngineConfiguration.initHistoryCleanup();

    //when
    historyService.cleanUpHistoryAsync();
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (int i = 1; i <= 6; i++) {
      for (Job job : historyCleanupJobs) {
        managementService.executeJob(job.getId());
      }
    }

    //then
    historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job job : historyCleanupJobs) {
      JobEntity jobEntity = (JobEntity) job;
      HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

      //job rescheduled till current time + (2 power count)*delay
      Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), HISTORY_TIME_TO_LIVE);
      assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
      Date nextRunMax = DateUtils.addSeconds(ClockUtil.getCurrentTime(), HistoryCleanupJobHandlerConfiguration.MAX_DELAY);
      assertTrue(jobEntity.getDuedate().before(nextRunMax));

      //countEmptyRuns incremented
      assertEquals(6, configuration.getCountEmptyRuns());
    }

    //data is still removed
    assertResult(0);
  }

  @Test
  public void testLessThanThresholdWithinBatchWindowMaxDelayReached() {
    //given
    prepareData(5);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 2)));
    processEngineConfiguration.initHistoryCleanup();

    //when
    historyService.cleanUpHistoryAsync();
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (int i = 1; i <= 11; i++) {
      for (Job job : historyCleanupJobs) {
        managementService.executeJob(job.getId());
      }
    }

    //then
    historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job job : historyCleanupJobs) {
      JobEntity jobEntity = (JobEntity) job;
      HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

      //job rescheduled till current time + max delay
      Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 10);
      assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
      assertTrue(jobEntity.getDuedate().before(getNextRunWithinBatchWindow(now)));

      //countEmptyRuns incremented
      assertEquals(11, configuration.getCountEmptyRuns());
    }

    //data is still removed
    assertResult(0);
  }

  @Test
  public void testLessThanThresholdCloseToBatchWindowEndTime() {
    //given
    prepareData(5);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addMinutes(now, 30)));
    processEngineConfiguration.initHistoryCleanup();

    //when
    historyService.cleanUpHistoryAsync();
    List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (int i = 1; i <= 9; i++) {
      for (Job job : historyCleanupJobs) {
        managementService.executeJob(job.getId());
      }
    }

    //then
    historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job job: historyCleanupJobs) {
      JobEntity jobEntity = (JobEntity)job;
      HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

      //job rescheduled till next batch window start time
      Date nextRun = getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
      assertTrue(jobEntity.getDuedate().equals(nextRun));

      //countEmptyRuns canceled
      assertEquals(0, configuration.getCountEmptyRuns());
    }

    //data is still removed
    assertResult(0);
  }

  @Test
  public void testLessThanThresholdOutsideBatchWindow() {
    //given
    prepareData(5);

    //we're outside batch window
    Date twoHoursAgo = new Date();
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(twoHoursAgo));
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(twoHoursAgo, 1)));
    processEngineConfiguration.initHistoryCleanup();
    ClockUtil.setCurrentTime(DateUtils.addHours(twoHoursAgo, 2));

    //when
    for (int i = 1; i <= 3; i++) {
      runHistoryCleanup();
    }

    //then
    final List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job job : historyCleanupJobs) {
      JobEntity jobEntity = (JobEntity) job;
      HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

      //job rescheduled till next batch window start
      Date nextRun = getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
      assertTrue(jobEntity.getDuedate().equals(nextRun));

      //countEmptyRuns canceled
      assertEquals(0, configuration.getCountEmptyRuns());
    }

    //nothing was removed
    assertResult(5);
  }

  @Test
  public void testLessThanThresholdOutsideBatchWindowAfterMidnight() throws ParseException {
    //given
    prepareData(5);

    //we're outside batch window, batch window passes midnight
    Date date = addDays(new Date(), 1);
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 1), 10));  // 01:10 tomorrow
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00");
    processEngineConfiguration.initHistoryCleanup();

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    managementService.executeJob(jobId);

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till next batch window start
    Date nextRun = getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
    assertTrue(jobEntity.getDuedate().equals(nextRun));
    assertTrue(nextRun.after(ClockUtil.getCurrentTime()));

    //countEmptyRuns canceled
    assertEquals(0, configuration.getCountEmptyRuns());

    //nothing was removed
    assertResult(5);
  }

  @Test
  public void testLessThanThresholdOutsideBatchWindowBeforeMidnight() {
    //given
    prepareData(5);

    //we're outside batch window, batch window passes midnight
    Date date = new Date();
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 22), 10));  //22:10
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00");
    processEngineConfiguration.initHistoryCleanup();

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    managementService.executeJob(jobId);

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till next batch window start
    Date nextRun = getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
    assertTrue(jobEntity.getDuedate().equals(nextRun));
    assertTrue(nextRun.after(ClockUtil.getCurrentTime()));

    //countEmptyRuns cancelled
    assertEquals(0, configuration.getCountEmptyRuns());

    //nothing was removed
    assertResult(5);
  }

  @Test
  public void testLessThanThresholdWithinBatchWindowBeforeMidnight() {
    //given
    prepareData(5);

    //we're within batch window, but batch window passes midnight
    Date date = new Date();
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 23), 10));  //23:10
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00");
    processEngineConfiguration.initHistoryCleanup();

    //when
    runHistoryCleanup();

    //then
    final List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job job : historyCleanupJobs) {
      JobEntity jobEntity = (JobEntity) job;
      HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

      //job rescheduled till current time + delay
      Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 0);
      assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
      Date nextRunMax = DateUtils.addSeconds(ClockUtil.getCurrentTime(), HistoryCleanupJobHandlerConfiguration.MAX_DELAY);
      assertTrue(jobEntity.getDuedate().before(nextRunMax));

      //countEmptyRuns incremented
      assertEquals(1, configuration.getCountEmptyRuns());
    }

    //data is still removed
    assertResult(0);
  }

  @Test
  public void testLessThanThresholdWithinBatchWindowAfterMidnight() throws ParseException {
    //given
    prepareData(5);

    //we're within batch window, but batch window passes midnight
    Date date = addDays(new Date(), 1);
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 0), 10));  // 00:10 tomorrow
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00");
    processEngineConfiguration.initHistoryCleanup();

    //when
    runHistoryCleanup(false);

    //then
    final List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job job : historyCleanupJobs) {
      JobEntity jobEntity = (JobEntity) job;
      HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

      //job rescheduled till current time + delay
      Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 0);
      assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
      Date nextRunMax = DateUtils.addSeconds(ClockUtil.getCurrentTime(), HistoryCleanupJobHandlerConfiguration.MAX_DELAY);
      assertTrue(jobEntity.getDuedate().before(nextRunMax));

      //countEmptyRuns incremented
      assertEquals(1, configuration.getCountEmptyRuns());
    }

    //data is still removed
    assertResult(0);
  }

  @Test
  @Ignore("CAM-10055")
  public void testLessThanThresholdOutsideBatchWindowAfterMidnightDaylightSaving() throws ParseException {
    //given
    prepareData(5);

    //we're outside batch window, batch window passes midnight
    ClockUtil.setCurrentTime(sdf.parse("2019-05-28T01:10:00"));  // 01:10
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00CET");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00CET");
    processEngineConfiguration.initHistoryCleanup();

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    managementService.executeJob(jobId);

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till next batch window start
    Date nextRun = getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
    assertTrue(jobEntity.getDuedate().equals(nextRun));
    assertTrue(nextRun.after(ClockUtil.getCurrentTime()));

    //countEmptyRuns canceled
    assertEquals(0, configuration.getCountEmptyRuns());

    //nothing was removed
    assertResult(5);
  }

  @Test
  @Ignore("CAM-10055")
  public void testLessThanThresholdWithinBatchWindowAfterMidnightDaylightSaving() throws ParseException {
    //given
    prepareData(5);

    //we're within batch window, but batch window passes midnight
    ClockUtil.setCurrentTime(sdf.parse("2018-05-14T00:10:00"));  // 00:10
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00CET");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00CET");
    processEngineConfiguration.initHistoryCleanup();

    //when
    runHistoryCleanup(false);

    //then
    final List<Job> historyCleanupJobs = historyService.findHistoryCleanupJobs();
    for (Job job : historyCleanupJobs) {
      JobEntity jobEntity = (JobEntity) job;
      HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

      //job rescheduled till current time + delay
      Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 0);
      assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
      Date nextRunMax = DateUtils.addSeconds(ClockUtil.getCurrentTime(), HistoryCleanupJobHandlerConfiguration.MAX_DELAY);
      assertTrue(jobEntity.getDuedate().before(nextRunMax));

      //countEmptyRuns incremented
      assertEquals(1, configuration.getCountEmptyRuns());
    }

    //data is still removed
    assertResult(0);
  }

  @Test
  public void testConfiguration() {
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00+0200");
    processEngineConfiguration.initHistoryCleanup();
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+2:00"));
    Date startTime = processEngineConfiguration.getHistoryCleanupBatchWindowStartTimeAsDate();
    c.setTime(startTime);
    assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.initHistoryCleanup();
    c = Calendar.getInstance();
    startTime = processEngineConfiguration.getHistoryCleanupBatchWindowStartTimeAsDate();
    c.setTime(startTime);
    assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:35-0800");
    processEngineConfiguration.initHistoryCleanup();
    c = Calendar.getInstance(TimeZone.getTimeZone("GMT-8:00"));
    Date endTime = processEngineConfiguration.getHistoryCleanupBatchWindowEndTimeAsDate();
    c.setTime(endTime);
    assertEquals(1, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(35, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:35");
    processEngineConfiguration.initHistoryCleanup();
    c = Calendar.getInstance();
    endTime = processEngineConfiguration.getHistoryCleanupBatchWindowEndTimeAsDate();
    c.setTime(endTime);
    assertEquals(1, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(35, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    processEngineConfiguration.setHistoryCleanupBatchSize(500);
    processEngineConfiguration.initHistoryCleanup();
    assertEquals(processEngineConfiguration.getHistoryCleanupBatchSize(), 500);

    processEngineConfiguration.setHistoryTimeToLive("5");
    processEngineConfiguration.initHistoryCleanup();
    assertEquals(5, ParseUtil.parseHistoryTimeToLive(processEngineConfiguration.getHistoryTimeToLive()).intValue());

    processEngineConfiguration.setHistoryTimeToLive("P6D");
    processEngineConfiguration.initHistoryCleanup();
    assertEquals(6, ParseUtil.parseHistoryTimeToLive(processEngineConfiguration.getHistoryTimeToLive()).intValue());
  }

  @Test
  public void testHistoryCleanupHelper() throws ParseException {
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("22:00+0100");
    processEngineConfiguration.initHistoryCleanup();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    Date date = sdf.parse("2017-09-06T22:15:00+0100");

    assertTrue(HistoryCleanupHelper.isWithinBatchWindow(date, processEngineConfiguration));

    date = sdf.parse("2017-09-06T22:15:00+0200");
    assertFalse(HistoryCleanupHelper.isWithinBatchWindow(date, processEngineConfiguration));
  }

  @Test
  public void testConfigurationFailureWrongStartTime() {
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00");

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyCleanupBatchWindowStartTime");
  }

  @Test
  public void testConfigurationFailureWrongDayOfTheWeekStartTime() throws ParseException {

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.getHistoryCleanupBatchWindows()
        .put(Calendar.MONDAY, new BatchWindowConfiguration("23", "01:00")))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("startTime");
  }

  @Test
  public void testConfigurationFailureWrongDayOfTheWeekEndTime() throws ParseException {

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.getHistoryCleanupBatchWindows()
        .put(Calendar.MONDAY, new BatchWindowConfiguration("23:00", "01")))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("endTime");
  }

  @Test
  public void testConfigurationFailureWrongDegreeOfParallelism() {
    processEngineConfiguration.setHistoryCleanupDegreeOfParallelism(0);

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyCleanupDegreeOfParallelism");

    // and
    processEngineConfiguration.setHistoryCleanupDegreeOfParallelism(HistoryCleanupCmd.MAX_THREADS_NUMBER + 1);

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyCleanupDegreeOfParallelism");
  }

  @Test
  public void testConfigurationFailureWrongEndTime() {
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("wrongValue");

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyCleanupBatchWindowEndTime");
  }

  @Test
  public void testConfigurationFailureWrongBatchSize() {
    processEngineConfiguration.setHistoryCleanupBatchSize(501);

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyCleanupBatchSize");
  }

  @Test
  public void testConfigurationFailureWrongBatchSize2() {
    processEngineConfiguration.setHistoryCleanupBatchSize(-5);

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyCleanupBatchSize");
  }

  @Test
  public void testConfigurationFailureWrongBatchThreshold() {
    processEngineConfiguration.setHistoryCleanupBatchThreshold(-1);

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyCleanupBatchThreshold");
  }

  @Test
  public void testConfigurationFailureMalformedHistoryTimeToLive() {
    processEngineConfiguration.setHistoryTimeToLive("PP5555DDDD");

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyTimeToLive");
  }

  @Test
  public void testConfigurationFailureInvalidHistoryTimeToLive() {
    processEngineConfiguration.setHistoryTimeToLive("invalidValue");

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyTimeToLive");
  }

  @Test
  public void testConfigurationFailureNegativeHistoryTimeToLive() {
    processEngineConfiguration.setHistoryTimeToLive("-6");

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("historyTimeToLive");
  }

  private Date getNextRunWithinBatchWindow(Date currentTime) {
    return processEngineConfiguration.getBatchWindowManager().getNextBatchWindow(currentTime, processEngineConfiguration).getStart();
  }

  private HistoryCleanupJobHandlerConfiguration getConfiguration(JobEntity jobEntity) {
    String jobHandlerConfigurationRaw = jobEntity.getJobHandlerConfigurationRaw();
    return HistoryCleanupJobHandlerConfiguration.fromJson(JsonUtil.asObject(jobHandlerConfigurationRaw));
  }

  private void prepareData(int instanceCount) {
    int createdInstances = instanceCount / 3;
    prepareBPMNData(createdInstances, ONE_TASK_PROCESS);
    prepareDMNData(createdInstances);
    prepareCMMNData(instanceCount - 2 * createdInstances);
  }

  private void prepareBPMNData(int instanceCount, String definitionKey) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), DAYS_IN_THE_PAST));
    final List<String> ids = prepareHistoricProcesses(definitionKey, getVariables(), instanceCount);
    deleteProcessInstances(ids);
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  private void deleteProcessInstances(List<String> ids) {
    final Date currentTime = ClockUtil.getCurrentTime();
    for (String id : ids) {
      //spread end_time between different "minutes"
      ClockUtil.setCurrentTime(DateUtils.setMinutes(currentTime, random.nextInt(60)));
      runtimeService.deleteProcessInstance(id, null, true, true);
    }
  }

  private void prepareDMNData(int instanceCount) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), DAYS_IN_THE_PAST));
    for (int i = 0; i < instanceCount; i++) {
      //spread end_time between different "minutes"
      ClockUtil.setCurrentTime(DateUtils.setMinutes(ClockUtil.getCurrentTime(), random.nextInt(60)));
      engineRule.getDecisionService().evaluateDecisionByKey(DECISION).variables(getDMNVariables()).evaluate();
    }
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  private void prepareCMMNData(int instanceCount) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), DAYS_IN_THE_PAST));

    for (int i = 0; i < instanceCount; i++) {
      CaseInstance caseInstance = caseService.createCaseInstanceByKey(ONE_TASK_CASE);
      //spread end_time between different "minutes"
      ClockUtil.setCurrentTime(DateUtils.setMinutes(ClockUtil.getCurrentTime(), random.nextInt(60)));
      caseService.terminateCaseExecution(caseInstance.getId());
      caseService.closeCaseInstance(caseInstance.getId());
    }
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  private List<String> prepareHistoricProcesses(String definitionKey, VariableMap variables, Integer processInstanceCount) {
    List<String> processInstanceIds = new ArrayList<>();

    for (int i = 0; i < processInstanceCount; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(definitionKey, variables);
      processInstanceIds.add(processInstance.getId());
    }

    return processInstanceIds;
  }

  private VariableMap getVariables() {
    return Variables.createVariables().putValue("aVariableName", "aVariableValue").putValue("anotherVariableName", "anotherVariableValue");
  }

  protected VariableMap getDMNVariables() {
    return Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
  }

  private void assertResult(long expectedInstanceCount) {
    long count = historyService.createHistoricProcessInstanceQuery().count()
        + historyService.createHistoricDecisionInstanceQuery().count()
        + historyService.createHistoricCaseInstanceQuery().count();
    assertEquals(expectedInstanceCount, count);
  }

  private void assertResultNotLess(long expectedInstanceCount) {
    long count = historyService.createHistoricProcessInstanceQuery().count()
      + historyService.createHistoricDecisionInstanceQuery().count()
      + historyService.createHistoricCaseInstanceQuery().count();
    assertTrue(expectedInstanceCount <= count);
  }

  protected static Date addDays(Date date, int days) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, days);
    return calendar.getTime();
  }

}
