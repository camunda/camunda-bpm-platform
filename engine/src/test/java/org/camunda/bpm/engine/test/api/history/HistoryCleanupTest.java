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
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
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
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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
import org.junit.rules.RuleChain;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Svetlana Dorokhova
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupTest {

  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";
  protected static final String DECISION = "decision";

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setHistoryCleanupBatchSize(20);
      configuration.setHistoryCleanupBatchThreshold(10);
      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private HistoryService historyService;
  private RuntimeService runtimeService;
  private ManagementService managementService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);
  
  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml", "org/camunda/bpm/engine/test/api/dmn/Example.dmn");
  }

  @After
  public void clearDatabase(){
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = engineRule.getManagementService().createJobQuery().list();
        if (jobs.size() > 0) {
          assertEquals(1, jobs.size());
          String jobId = jobs.get(0).getId();
          commandContext.getJobManager().deleteJob((JobEntity) jobs.get(0));
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        }

        List<HistoricIncident> historicIncidents = engineRule.getHistoryService().createHistoricIncidentQuery().list();
        for (HistoricIncident historicIncident : historicIncidents) {
          commandContext.getDbEntityManager().delete((HistoricIncidentEntity)historicIncident);
        }

        commandContext.getMeterLogManager().deleteAll();

        return null;
      }
    });

    List<HistoricProcessInstance> historicProcessInstances = engineRule.getHistoryService().createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance historicProcessInstance: historicProcessInstances) {
      historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
    }

    List<HistoricDecisionInstance> historicDecisionInstances = engineRule.getHistoryService().createHistoricDecisionInstanceQuery().list();
    for (HistoricDecisionInstance historicDecisionInstance: historicDecisionInstances) {
      historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());
    }

    clearMetrics();

  }

  protected void clearMetrics() {
    Collection<Meter> meters = engineRule.getProcessEngineConfiguration().getMetricsRegistry().getMeters().values();
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
  @Deployment(resources = {"org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml" })
  public void testHistoryCleanupWithDesicionInstancesOnlyDecisionInstancesRemoved() {
    //given
    prepareDecisionInstances(null, 5);

    ClockUtil.setCurrentTime(new Date());
    //when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    //then
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(0, historyService.createHistoricDecisionInstanceQuery().count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml" })
  public void testHistoryCleanupWithDesicionInstancesOnlyProcessInstancesRemoved() {
    //given
    prepareDecisionInstances(5, null);

    ClockUtil.setCurrentTime(new Date());
    //when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(13, historyService.createHistoricDecisionInstanceQuery().count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml" })
  public void testHistoryCleanupWithDesicionInstancesEverythingRemoved() {
    //given
    prepareDecisionInstances(5, 5);

    ClockUtil.setCurrentTime(new Date());
    //when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    //then
    assertResult(0);
  }

  private void prepareDecisionInstances(Integer processInstanceTimeToLive, Integer decisionTimeToLive) {
    //update time to live
    List<ProcessDefinition> processDefinitions = engineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testProcess").list();
    assertEquals(1, processDefinitions.size());
    engineRule.getRepositoryService().updateProcessDefinitionHistoryTimeToLive(processDefinitions.get(0).getId(), processInstanceTimeToLive);

    final List<DecisionDefinition> decisionDefinitions = engineRule.getRepositoryService().createDecisionDefinitionQuery().decisionDefinitionKey("testDecision").list();
    assertEquals(1, decisionDefinitions.size());
    engineRule.getRepositoryService().updateDecisionDefinitionHistoryTimeToLive(decisionDefinitions.get(0).getId(), decisionTimeToLive);

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -6));

    //create 3 process instances
    List<String> processInstanceIds = new ArrayList<String>();
    Map<String, Object> variables = Variables.createVariables().putValue("pojo", new TestPojo("okay", 13.37));
    for (int i = 0; i < 3; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", variables);
      processInstanceIds.add(processInstance.getId());
    }
    runtimeService.deleteProcessInstances(processInstanceIds, null, true, true);

    //+10 standalone decisions
    for (int i = 0; i < 10; i++) {
      engineRule.getDecisionService().evaluateDecisionByKey("testDecision").variables(variables).evaluate();
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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

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
    List<ProcessDefinition> processDefinitions = engineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(ONE_TASK_PROCESS).list();
    assertEquals(1, processDefinitions.size());
    engineRule.getRepositoryService().updateProcessDefinitionHistoryTimeToLive(processDefinitions.get(0).getId(), null);

    final List<DecisionDefinition> decisionDefinitions = engineRule.getRepositoryService().createDecisionDefinitionQuery().decisionDefinitionKey(DECISION).list();
    assertEquals(1, decisionDefinitions.size());
    engineRule.getRepositoryService().updateDecisionDefinitionHistoryTimeToLive(decisionDefinitions.get(0).getId(), null);
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

    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(null);
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(null);
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

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

    //when
    //call to cleanup history means that incident was resolved
    jobId = historyService.cleanUpHistoryAsync(true).getId();

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    assertEquals(null, jobEntity.getExceptionByteArrayId());
    assertEquals(null, jobEntity.getExceptionMessage());

  }

  private void imitateFailedJob(final String jobId) {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();
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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 1))); //now + 1 hour
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));   //now + 5 hours
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

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
    Date result = DateUtils.setMilliseconds(DateUtils.addSeconds(date, Math.min((int)(Math.pow(2., (double)countEmptyRuns) * HistoryCleanupJobHandlerConfiguration.START_DELAY),
        HistoryCleanupJobHandlerConfiguration.MAX_DELAY)), 0);
    return result;
  }

  private JobEntity getJobEntity(String jobId) {
    return (JobEntity)engineRule.getManagementService().createJobQuery().jobId(jobId).list().get(0);
  }

  @Test
  public void testLessThanThresholdWithinBatchWindowAgain() {
    //given
    prepareData(5);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 1)));
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    for (int i = 1; i <= 6; i++) {
      managementService.executeJob(jobId);
    }

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till current time + (2 power count)*delay
    Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 5);
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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 2)));
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addMinutes(now, 30)));
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(twoHoursAgo));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(twoHoursAgo, 1)));
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();
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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23:00");
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("01:00");
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23:00");
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("01:00");
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

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
  public void testLessThanThresholdWithinBatchWindowBeforeMidnight() {
    //given
    prepareData(5);

    //we're within batch window, but batch window passes midnight
    Date date = new Date();
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 23), 10));  //23:10
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23:00");
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("01:00");
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();

    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23:00");
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("01:00");
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();

    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

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
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23:00+0200");
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+2:00"));
    Date startTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowStartTimeAsDate();
    c.setTime(startTime);
    assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23:00");
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();
    c = Calendar.getInstance();
    startTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowStartTimeAsDate();
    c.setTime(startTime);
    assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("01:35-0800");
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();
    c = Calendar.getInstance(TimeZone.getTimeZone("GMT-8:00"));
    Date endTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowEndTimeAsDate();
    c.setTime(endTime);
    assertEquals(1, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(35, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("01:35");
    engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();
    c = Calendar.getInstance();
    endTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowEndTimeAsDate();
    c.setTime(endTime);
    assertEquals(1, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(35, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));
  }

  @Test
  public void testConfigurationFailure() {
    try {
      engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23");
      engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("01:00");
      engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();
      fail("Exception expected.");
    } catch (ProcessEngineException ex) {
      assertTrue(ex.getMessage().contains("historyCleanupBatchWindowStartTime"));
    }

    try {
      engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23:00");
      engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("wrongValue");
      engineRule.getProcessEngineConfiguration().initHistoryCleanupBatchWindow();
      fail("Exception expected.");
    } catch (ProcessEngineException ex) {
      assertTrue(ex.getMessage().contains("historyCleanupBatchWindowEndTime"));
    }
  }

  private Date getNextRunWithinBatchWindow(Date currentTime) {
    Date batchWindowStartTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowStartTimeAsDate();
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
    int createdInstances = instanceCount / 2;
    prepareBPMNData(createdInstances, ONE_TASK_PROCESS);
    prepareDMNData(instanceCount - createdInstances);
  }

  private void prepareBPMNData(int instanceCount, String businesskey) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -6));
    final List<String> ids = prepareHistoricProcesses(businesskey, getVariables(), instanceCount);
    runtimeService.deleteProcessInstances(ids, null, true, true);
    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  private void prepareDMNData(int instanceCount) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -6));
    for (int i = 0; i < instanceCount; i++) {
      engineRule.getDecisionService().evaluateDecisionByKey(DECISION).variables(getDMNVariables()).evaluate();
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
    long count = historyService.createHistoricProcessInstanceQuery().count();
    count = count + historyService.createHistoricDecisionInstanceQuery().count();
    assertEquals(expectedInstanceCount, count);
  }

}
