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
import java.util.TimeZone;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
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
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
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

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setHistoryCleanupBatchSize(20);
      configuration.setHistoryCleanupBatchThreshold(10);
      configuration.setEnableAutoHistoryCleanup(true);
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
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml");
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
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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

    //when
    String jobId = historyService.cleanUpHistoryAsync(false).getId();

    managementService.executeJob(jobId);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  public void testAutoHistoryCleanupDisabledJobIsNotCreated() {
    //given
    prepareData(15);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setEnableAutoHistoryCleanup(false);
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));

    //when
    final Job job = historyService.cleanUpHistoryAsync(false);

    //then
    assertNull(job);
    assertEquals(15, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  public void testAutoHistoryCleanupDisabledJobIsNotExecuted() {
    //given
    prepareData(15);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));

    final String jobId = historyService.cleanUpHistoryAsync(false).getId();

    engineRule.getProcessEngineConfiguration().setEnableAutoHistoryCleanup(false);

    //when
    managementService.executeJob(jobId);

    //then
    assertEquals(15, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  public void testHistoryCleanupJobNullTTL() {
    //given
    List<ProcessDefinition> processDefinitions = engineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(ONE_TASK_PROCESS).list();
    assertEquals(1, processDefinitions.size());
    String id = processDefinitions.get(0).getId();

    engineRule.getRepositoryService().updateProcessDefinitionTimeToLive(id, null);

    prepareData(15);

    ClockUtil.setCurrentTime(new Date());
    //when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    //then
    assertEquals(15, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml" })
  public void testHistoryCleanupJobDefaultTTL() {
    //given
    prepareData(15, "twoTasksProcess");

    ClockUtil.setCurrentTime(new Date());
    //when
    String jobId = historyService.cleanUpHistoryAsync(true).getId();

    managementService.executeJob(jobId);

    //then
    assertEquals(15, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("twoTasksProcess").count());
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
    String jobId = historyService.cleanUpHistoryAsync().getId();
    //job is executed once within batch window
    managementService.executeJob(jobId);

    //when
    //time passed -> outside batch window
    ClockUtil.setCurrentTime(DateUtils.addHours(now, 6));
    //the job is called for the second time
    managementService.executeJob(jobId);

    //then
    //second execution was not abe to delete rest data
    assertEquals(20, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  public void testManualRunDoesNotRespectDisabledHistoryCleanup() {
    //given
    prepareData(40);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setEnableAutoHistoryCleanup(false);
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));

    //when
    //job is executed before batch window start
    String jobId = historyService.cleanUpHistoryAsync(true).getId();
    managementService.executeJob(jobId);

    //the job is called for the second time after batch window end
    ClockUtil.setCurrentTime(DateUtils.addHours(now, 6)); //now + 6 hours
    managementService.executeJob(jobId);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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

    //when
    //job is executed before batch window start
    String jobId = historyService.cleanUpHistoryAsync(true).getId();
    managementService.executeJob(jobId);

    //the job is called for the second time after batch window end
    ClockUtil.setCurrentTime(DateUtils.addHours(now, 6)); //now + 6 hours
    managementService.executeJob(jobId);

    //then
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  public void testLessThanThresholdOutsideBatchWindow() {
    //given
    prepareData(5);

    //we're outside batch window
    Date twoHoursAgo = new Date();
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(twoHoursAgo));
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(twoHoursAgo, 1)));
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
    assertEquals(5, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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
    assertEquals(5, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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
    assertEquals(5, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
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
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  public void testConfiguration() {
    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23:00+0200");
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+2:00"));
    Date startTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowStartTimeAsDate();
    c.setTime(startTime);
    assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowStartTime("23:00");
    c = Calendar.getInstance();
    startTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowStartTimeAsDate();
    c.setTime(startTime);
    assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("01:35-0800");
    c = Calendar.getInstance(TimeZone.getTimeZone("GMT-8:00"));
    Date endTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowEndTimeAsDate();
    c.setTime(endTime);
    assertEquals(1, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(35, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));

    engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("01:35");
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
      Date startTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowStartTimeAsDate();
      fail("Exception expected.");
    } catch (ProcessEngineException ex) {
      assertTrue(ex.getMessage().contains("historyCleanupBatchWindowStartTime"));
    }

    try {
      engineRule.getProcessEngineConfiguration().setHistoryCleanupBatchWindowEndTime("wrongValue");
      Date endTime = engineRule.getProcessEngineConfiguration().getHistoryCleanupBatchWindowEndTimeAsDate();
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


  private void prepareData(int processInstanceCount) {
    prepareData(processInstanceCount, ONE_TASK_PROCESS);
  }

  private void prepareData(int processInstanceCount, String businesskey) {
    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -6));
    final List<String> ids = prepareHistoricProcesses(businesskey, getVariables(), processInstanceCount);
    runtimeService.deleteProcessInstances(ids, null, true, true);
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

}
