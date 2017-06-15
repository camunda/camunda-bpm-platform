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

package org.camunda.bpm.engine.test.api.history;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobHelper;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.management.Metrics;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setHistoryCleanupBatchSize(20);
      configuration.setHistoryCleanupBatchThreshold(10);
      configuration.setDefaultNumberOfRetries(5);
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private HistoryService historyService;
  private RuntimeService runtimeService;
  private ManagementService managementService;
  private CaseService caseService;
  private RepositoryService repositoryService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    caseService = engineRule.getCaseService();
    repositoryService = engineRule.getRepositoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml", "org/camunda/bpm/engine/test/api/dmn/Example.dmn", "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithHistoryTimeToLive.cmmn");
  }

  @After
  public void clearDatabase() {
    //reset configuration changes
    String defaultStartTime = processEngineConfiguration.getHistoryCleanupBatchWindowStartTime();
    String defaultEndTime = processEngineConfiguration.getHistoryCleanupBatchWindowEndTime();
    int defaultBatchSize = processEngineConfiguration.getHistoryCleanupBatchSize();

    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(defaultStartTime);
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(defaultEndTime);
    processEngineConfiguration.setHistoryCleanupBatchSize(defaultBatchSize);

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = managementService.createJobQuery().list();
        if (jobs.size() > 0) {
          assertEquals(1, jobs.size());
          String jobId = jobs.get(0).getId();
          commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
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

  }

  protected void clearMetrics() {
    Collection<Meter> meters = processEngineConfiguration.getMetricsRegistry().getMeters().values();
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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    //then
    assertResult(0);
  }

  @Test
  public void testHistoryCleanupMetrics() {
    //given
    processEngineConfiguration.setHistoryCleanupMetricsEnabled(true);
    prepareData(15);

    ClockUtil.setCurrentTime(new Date());
    //when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    //then
    final long removedProcessInstances = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES).sum();
    final long removedDecisionInstances = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_CASE_INSTANCES).sum();
    final long removedCaseInstances = managementService.createMetricsQuery().name(Metrics.HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES).sum();

    assertTrue(removedProcessInstances > 0);
    assertTrue(removedDecisionInstances > 0);
    assertTrue(removedCaseInstances > 0);

    assertEquals(15, removedProcessInstances + removedCaseInstances + removedDecisionInstances);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml", "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn" })
  public void testHistoryCleanupOnlyDecisionInstancesRemoved() {
    // given
    prepareInstances(null, HISTORY_TIME_TO_LIVE, null);

    ClockUtil.setCurrentTime(new Date());
    // when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

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
    List<String> processInstanceIds = new ArrayList<String>();
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
    String jobId = historyService.cleanUpHistoryAsync(false).getId();

    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    //then
    assertResult(15);
  }

  @Test
  public void testFindHistoryCleanupJob() {
    //given
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    //when
    Job historyCleanupJob = historyService.findHistoryCleanupJob();

    //then
    assertNotNull(historyCleanupJob);
    assertEquals(jobId, historyCleanupJob.getId());
  }

  @Test
  public void testRescheduleForNever() {
    //given

    //force creation of job
    historyService.cleanUpHistoryAsync(true);
    JobEntity historyCleanupJob = (JobEntity)historyService.findHistoryCleanupJob();
    assertNotNull(historyCleanupJob);
    assertNotNull(historyCleanupJob.getDuedate());

    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(null);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(null);
    processEngineConfiguration.initHistoryCleanup();

    ClockUtil.setCurrentTime(new Date());

    //when
    historyService.cleanUpHistoryAsync(false);

    //then
    historyCleanupJob = (JobEntity)historyService.findHistoryCleanupJob();
    assertEquals(SuspensionState.SUSPENDED.getStateCode(), historyCleanupJob.getSuspensionState());
    assertNull(historyCleanupJob.getDuedate());

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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());

    JobEntity jobEntity = getJobEntity(jobId);
    assertEquals(SuspensionState.SUSPENDED.getStateCode(), jobEntity.getSuspensionState());
  }

  @Test
  public void testNotEnoughTimeToDeleteEverything() {
    //given
    //we have something to cleanup
    prepareData(40);
    //we call history cleanup within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, HISTORY_TIME_TO_LIVE)));
    processEngineConfiguration.initHistoryCleanup();
    String jobId = historyService.cleanUpHistoryAsync().getId();
    //job is executed once within batch window
    managementService.executeJob(jobId);

    //when
    //time passed -> outside batch window
    ClockUtil.setCurrentTime(DateUtils.addHours(now, 6));
    //the job is called for the second time
    managementService.executeJob(jobId);

    //then
    //second execution was not able to delete rest data
    assertResult(20);
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
    String jobId = historyService.cleanUpHistoryAsync(true).getId();
    managementService.executeJob(jobId);

    //the job is called for the second time after batch window end
    ClockUtil.setCurrentTime(DateUtils.addHours(now, 6)); //now + 6 hours
    managementService.executeJob(jobId);

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
    String jobId = historyService.cleanUpHistoryAsync().getId();

    managementService.executeJob(jobId);

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till current time + delay
    Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 0);
    assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
    Date nextRunMax = DateUtils.addSeconds(ClockUtil.getCurrentTime(), HistoryCleanupJobHandlerConfiguration.MAX_DELAY);
    assertTrue(jobEntity.getDuedate().before(nextRunMax));

    //countEmptyRuns incremented
    assertEquals(1, configuration.getCountEmptyRuns());

    //data is still removed
    assertResult(0);
  }

  private Date getNextRunWithDelay(Date date, int countEmptyRuns) {
    //ignore milliseconds because MySQL does not support them, and it's not important for test
    Date result = DateUtils.setMilliseconds(DateUtils.addSeconds(date, Math.min((int)(Math.pow(2., countEmptyRuns) * HistoryCleanupJobHandlerConfiguration.START_DELAY),
        HistoryCleanupJobHandlerConfiguration.MAX_DELAY)), 0);
    return result;
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
    String jobId = historyService.cleanUpHistoryAsync().getId();
    for (int i = 1; i <= 6; i++) {
      managementService.executeJob(jobId);
    }

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till current time + (2 power count)*delay
    Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), HISTORY_TIME_TO_LIVE);
    assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
    Date nextRunMax = DateUtils.addSeconds(ClockUtil.getCurrentTime(), HistoryCleanupJobHandlerConfiguration.MAX_DELAY);
    assertTrue(jobEntity.getDuedate().before(nextRunMax));

    //countEmptyRuns incremented
    assertEquals(6, configuration.getCountEmptyRuns());

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
    String jobId = historyService.cleanUpHistoryAsync().getId();
    for (int i = 1; i <= 11; i++) {
      managementService.executeJob(jobId);
    }

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till current time + max delay
    Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 10);
    assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
    assertTrue(jobEntity.getDuedate().before(getNextRunWithinBatchWindow(now)));

    //countEmptyRuns incremented
    assertEquals(11, configuration.getCountEmptyRuns());

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
    String jobId = historyService.cleanUpHistoryAsync().getId();
    for (int i = 1; i <= 9; i++) {
      managementService.executeJob(jobId);
    }

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till next batch window start time
    Date nextRun = getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
    assertTrue(jobEntity.getDuedate().equals(nextRun));

    //countEmptyRuns canceled
    assertEquals(0, configuration.getCountEmptyRuns());

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
    String jobId = historyService.cleanUpHistoryAsync().getId();
    for (int i = 1; i <= 3; i++) {
      managementService.executeJob(jobId);
    }

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till next batch window start
    Date nextRun = getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
    assertTrue(jobEntity.getDuedate().equals(nextRun));

    //countEmptyRuns canceled
    assertEquals(0, configuration.getCountEmptyRuns());

    //nothing was removed
    assertResult(5);
  }

  @Test
  public void testLessThanThresholdOutsideBatchWindowAfterMidnight() {
    //given
    prepareData(5);

    //we're outside batch window, batch window passes midnight
    Date date = new Date();
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 1), 10));  //01:10
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
    String jobId = historyService.cleanUpHistoryAsync().getId();

    ExecuteJobHelper.executeJob(jobId, processEngineConfiguration.getCommandExecutorTxRequired());

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till current time + delay
    Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 0);
    assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
    Date nextRunMax = DateUtils.addSeconds(ClockUtil.getCurrentTime(), HistoryCleanupJobHandlerConfiguration.MAX_DELAY);
    assertTrue(jobEntity.getDuedate().before(nextRunMax));

    //countEmptyRuns incremented
    assertEquals(1, configuration.getCountEmptyRuns());

    //data is still removed
    assertResult(0);
  }

  @Test
  public void testLessThanThresholdWithinBatchWindowAfterMidnight() {
    //given
    prepareData(5);

    //we're within batch window, but batch window passes midnight
    Date date = new Date();
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 0), 10));  //00:10
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00");
    processEngineConfiguration.initHistoryCleanup();

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();

    ExecuteJobHelper.executeJob(jobId, processEngineConfiguration.getCommandExecutorTxRequired());

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till current time + delay
    Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 0);
    assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
    Date nextRunMax = DateUtils.addSeconds(ClockUtil.getCurrentTime(), HistoryCleanupJobHandlerConfiguration.MAX_DELAY);
    assertTrue(jobEntity.getDuedate().before(nextRunMax));

    //countEmptyRuns incremented
    assertEquals(1, configuration.getCountEmptyRuns());

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
  }

  @Test
  public void testConfigurationFailureWrongStartTime() {
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00");

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("historyCleanupBatchWindowStartTime");

    processEngineConfiguration.initHistoryCleanup();
  }

  @Test
  public void testConfigurationFailureWrongEndTime() {
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("wrongValue");

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("historyCleanupBatchWindowEndTime");

    processEngineConfiguration.initHistoryCleanup();
  }

  @Test
  public void testConfigurationFailureWrongBatchSize() {
    processEngineConfiguration.setHistoryCleanupBatchSize(501);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("historyCleanupBatchSize");

    processEngineConfiguration.initHistoryCleanup();
  }

  @Test
  public void testConfigurationFailureWrongBatchSize2() {
    processEngineConfiguration.setHistoryCleanupBatchSize(-5);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("historyCleanupBatchSize");

    processEngineConfiguration.initHistoryCleanup();
  }

  @Test
  public void testConfigurationFailureWrongBatchThreshold() {
    processEngineConfiguration.setHistoryCleanupBatchThreshold(-1);

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("historyCleanupBatchThreshold");

    processEngineConfiguration.initHistoryCleanup();
  }

  private Date getNextRunWithinBatchWindow(Date currentTime) {
    Date batchWindowStartTime = processEngineConfiguration.getHistoryCleanupBatchWindowStartTimeAsDate();
    return getNextRunWithinBatchWindow(currentTime, batchWindowStartTime);
  }

  public Date getNextRunWithinBatchWindow(Date date, Date batchWindowStartTime) {
    Date todayPossibleRun = updateTime(date, batchWindowStartTime);
    if (todayPossibleRun.after(date)) {
      return todayPossibleRun;
    } else {
      //tomorrow
      return DateUtils.addDays(todayPossibleRun, 1);
    }
  }

  private Date updateTime(Date now, Date newTime) {
    Date result = now;

    Calendar newTimeCalendar = Calendar.getInstance();
    newTimeCalendar.setTime(newTime);

    result = DateUtils.setHours(result, newTimeCalendar.get(Calendar.HOUR_OF_DAY));
    result = DateUtils.setMinutes(result, newTimeCalendar.get(Calendar.MINUTE));
    result = DateUtils.setSeconds(result, newTimeCalendar.get(Calendar.SECOND));
    result = DateUtils.setMilliseconds(result, newTimeCalendar.get(Calendar.MILLISECOND));
    return result;
  }

  private HistoryCleanupJobHandlerConfiguration getConfiguration(JobEntity jobEntity) {
    String jobHandlerConfigurationRaw = jobEntity.getJobHandlerConfigurationRaw();
    return HistoryCleanupJobHandlerConfiguration.fromJson(new JSONObject(jobHandlerConfigurationRaw));
  }


  private void prepareData(int instanceCount) {
    int createdInstances = instanceCount / 3;
    prepareBPMNData(createdInstances, ONE_TASK_PROCESS);
    prepareDMNData(createdInstances);
    prepareCMMNData(instanceCount - 2*createdInstances);
  }

  private void prepareBPMNData(int instanceCount, String businesskey) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), DAYS_IN_THE_PAST));
    final List<String> ids = prepareHistoricProcesses(businesskey, getVariables(), instanceCount);
    runtimeService.deleteProcessInstances(ids, null, true, true);
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  private void prepareDMNData(int instanceCount) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), DAYS_IN_THE_PAST));
    for (int i = 0; i < instanceCount; i++) {
      engineRule.getDecisionService().evaluateDecisionByKey(DECISION).variables(getDMNVariables()).evaluate();
    }
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  private void prepareCMMNData(int instanceCount) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), DAYS_IN_THE_PAST));

    for (int i = 0; i < instanceCount; i++) {
      CaseInstance caseInstance = caseService.createCaseInstanceByKey(ONE_TASK_CASE);
      caseService.terminateCaseExecution(caseInstance.getId());
      caseService.closeCaseInstance(caseInstance.getId());
    }
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  private List<String> prepareHistoricProcesses(String businessKey, VariableMap variables, Integer processInstanceCount) {
    List<String> processInstanceIds = new ArrayList<String>();

    for (int i = 0; i < processInstanceCount; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(businessKey, variables);
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

}
