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
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobHelper;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Svetlana Dorokhova
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupTest {

  protected static final String ONE_TASK_PROCESS = "oneTaskProcess";

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private HistoryService historyService;
  private RuntimeService runtimeService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);
  
  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    ProcessEngineConfigurationImpl processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    processEngineConfiguration.setHistoryCleanupBatchSize(20);
    processEngineConfiguration.setHistoryCleanupBatchThreshold(10);
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

  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testWrongConfigurationFail() {
    //given
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime(null);
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime(null);

    ClockUtil.setCurrentTime(new Date());

    try {
      //when
      historyService.cleanUpHistoryAsync(false);
      fail("BadUserRequestException was expected");
    } catch (BadUserRequestException ex) {
      assertTrue(ex.getMessage().contains("History cleanup won't be scheduled"));
    }

  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoryCleanupJob() {
      //given
    prepareData(15);

      ClockUtil.setCurrentTime(new Date());
      //when
      String jobId = historyService.cleanUpHistoryAsync(true).getId();

      ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

      //then
      assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
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
        jobEntity.update();
        return null;
      }
    });
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testLessThanThresholdAreLeft() {
    //given
    prepareData(5);

    ClockUtil.setCurrentTime(new Date());
    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();

    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //then
    assertEquals(5, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNotEnoughTimeToDeleteEverything() {
    //given
    //we have something to cleanup
    prepareData(40);
    //we call history cleanup within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));
    String jobId = historyService.cleanUpHistoryAsync().getId();
    //job is executed once within batch window
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //when
    //time passed -> outside batch window
    ClockUtil.setCurrentTime(DateUtils.addHours(now, 6));
    //the job is called for the second time
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //then
    //second execution was not abe to delete rest data
    assertEquals(20, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testManualRunDoesNotRespectBatchWindow() {
    //given
    //we have something to cleanup
    int processInstanceCount = 40;
    prepareData(processInstanceCount);
    //we call history cleanup outside batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 1)));
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));
    String jobId = historyService.cleanUpHistoryAsync(true).getId();
    //job is executed once within batch window
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //when
    //time passed -> outside batch window
    ClockUtil.setCurrentTime(DateUtils.addHours(now, 6));
    //the job is called for the second time
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //then
    //second execution was not abe to delete rest data
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNothingToDeleteWithinBatchWindow() {
    //given
    prepareData(5);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 5)));

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
  }

  public Date getNextRunWithDelay(Date date, int countEmptyRuns) {
    //ignore milliseconds because MySQL does not support them, and it's not important for test
    Date result = DateUtils.setMilliseconds(DateUtils.addSeconds(date, Math.min((int)(Math.pow(2., (double)countEmptyRuns) * HistoryCleanupJobHandlerConfiguration.START_DELAY),
        HistoryCleanupJobHandlerConfiguration.MAX_DELAY)), 0);
    return result;
  }

  private JobEntity getJobEntity(String jobId) {
    return (JobEntity)engineRule.getManagementService().createJobQuery().jobId(jobId).list().get(0);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNothingToDeleteWithinBatchWindowAgain() {
    //given
    prepareData(5);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 1)));

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    updateEmptyRunsCount(jobId,5);
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

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
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNothingToDeleteWithinBatchWindowMaxDelayReached() {
    //given
    prepareData(5);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(now, 2)));

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    updateEmptyRunsCount(jobId,10);
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till current time + max delay
    Date nextRun = getNextRunWithDelay(ClockUtil.getCurrentTime(), 10);
    assertTrue(jobEntity.getDuedate().equals(nextRun) || jobEntity.getDuedate().after(nextRun));
    assertTrue(jobEntity.getDuedate().before(configuration.getNextRunWithinBatchWindow(now)));

    //countEmptyRuns incremented
    assertEquals(11, configuration.getCountEmptyRuns());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNothingToDeleteCloseToBatchWindowEndTime() {
    //given
    prepareData(5);

    //we're within batch window
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(now));
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addMinutes(now, 30)));

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    updateEmptyRunsCount(jobId,8);
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till current time + max delay
    Date nextRun = configuration.getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
    assertTrue(jobEntity.getDuedate().equals(nextRun));

    //countEmptyRuns incremented
    assertEquals(0, configuration.getCountEmptyRuns());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNothingToDeleteOutsideBatchWindow() {
    //given
    prepareData(5);

    //we're within batch window
    Date twoHoursAgo = new Date();
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime(new SimpleDateFormat("HH:mm").format(twoHoursAgo));
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime(new SimpleDateFormat("HH:mm").format(DateUtils.addHours(twoHoursAgo, 1)));
    ClockUtil.setCurrentTime(DateUtils.addHours(twoHoursAgo, 2));

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    updateEmptyRunsCount(jobId,2);
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till next batch window start
    Date nextRun = configuration.getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
    assertTrue(jobEntity.getDuedate().equals(nextRun));

    //countEmptyRuns canceled
    assertEquals(0, configuration.getCountEmptyRuns());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNothingToDeleteOutsideBatchWindowAfterMidnight() {
    //given
    prepareData(5);

    //we're outside batch window, batch window passes midnight
    Date date = new Date();
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 1), 10));  //01:10
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime("23:00");
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime("01:00");

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    updateEmptyRunsCount(jobId,2);
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till next batch window start
    Date nextRun = configuration.getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
    assertTrue(jobEntity.getDuedate().equals(nextRun));

    //countEmptyRuns canceled
    assertEquals(0, configuration.getCountEmptyRuns());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNothingToDeleteOutsideBatchWindowBeforeMidnight() {
    //given
    prepareData(5);

    //we're outside batch window, batch window passes midnight
    Date date = new Date();
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 22), 10));  //22:10
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime("23:00");
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime("01:00");

    //when
    String jobId = historyService.cleanUpHistoryAsync().getId();
    updateEmptyRunsCount(jobId,2);
    ExecuteJobHelper.executeJob(jobId, engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired());

    //then
    JobEntity jobEntity = getJobEntity(jobId);
    HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);

    //job rescheduled till next batch window start
    Date nextRun = configuration.getNextRunWithinBatchWindow(ClockUtil.getCurrentTime());
    assertTrue(jobEntity.getDuedate().equals(nextRun));

    //countEmptyRuns canceled
    assertEquals(0, configuration.getCountEmptyRuns());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNothingToDeleteWithinBatchWindowBeforeMidnight() {
    //given
    prepareData(5);

    //we're within batch window, but batch window passes midnight
    Date date = new Date();
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 23), 10));  //23:10
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime("23:00");
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime("01:00");

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
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNothingToDeleteWithinBatchWindowAfterMidnight() {
    //given
    prepareData(5);

    //we're within batch window, but batch window passes midnight
    Date date = new Date();
    ClockUtil.setCurrentTime(DateUtils.setMinutes(DateUtils.setHours(date, 0), 10));  //00:10
    engineRule.getProcessEngineConfiguration().setBatchWindowStartTime("23:00");
    engineRule.getProcessEngineConfiguration().setBatchWindowEndTime("01:00");

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
  }

  private void updateEmptyRunsCount(final String jobId, final Integer countEmptyRuns) {
    engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobEntity jobEntity = getJobEntity(jobId);
        HistoryCleanupJobHandlerConfiguration configuration = getConfiguration(jobEntity);
        configuration.setCountEmptyRuns(countEmptyRuns);
        jobEntity.setJobHandlerConfiguration(configuration);
        jobEntity.update();
        return null;
      }
    });
  }

  private HistoryCleanupJobHandlerConfiguration getConfiguration(JobEntity jobEntity) {
    String jobHandlerConfigurationRaw = jobEntity.getJobHandlerConfigurationRaw();
    return HistoryCleanupJobHandlerConfiguration.fromJson(new JSONObject(jobHandlerConfigurationRaw));
  }


  private void prepareData(int processInstanceCount) {
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), -6));
    final List<String> ids = prepareHistoricProcesses(ONE_TASK_PROCESS, getVariables(), processInstanceCount);  //two batch sizes
    runtimeService.deleteProcessInstances(ids, null, true, true);
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
