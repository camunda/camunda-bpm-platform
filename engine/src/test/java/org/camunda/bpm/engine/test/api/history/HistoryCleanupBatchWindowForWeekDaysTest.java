package org.camunda.bpm.engine.test.api.history;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.BatchWindowConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Svetlana Dorokhova
 *
 */
@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupBatchWindowForWeekDaysTest {

  protected String defaultStartTime;
  protected String defaultEndTime;
  protected int defaultBatchSize;

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setHistoryCleanupBatchSize(20);
      configuration.setHistoryCleanupBatchThreshold(10);
      configuration.setDefaultNumberOfRetries(5);

      configuration.getHistoryCleanupBatchWindows().put(Calendar.MONDAY, new BatchWindowConfiguration("22:00", "01:00"));
      configuration.getHistoryCleanupBatchWindows().put(Calendar.TUESDAY, new BatchWindowConfiguration("22:00", "23:00"));
      configuration.getHistoryCleanupBatchWindows().put(Calendar.WEDNESDAY, new BatchWindowConfiguration("15:00", "20:00"));
      configuration.getHistoryCleanupBatchWindows().put(Calendar.FRIDAY, new BatchWindowConfiguration("22:00", "01:00"));
      configuration.getHistoryCleanupBatchWindows().put(Calendar.SUNDAY, new BatchWindowConfiguration("10:00", "20:00"));

      return configuration;
    }
  };

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  private HistoryService historyService;
  private ManagementService managementService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  @Parameterized.Parameter(0)
  public Date currentDate;

  @Parameterized.Parameter(1)
  public Date startDateForCheck;

  @Parameterized.Parameter(2)
  public Date endDateForCheck;

  @Parameterized.Parameter(3)
  public Date startDateForCheckWithDefaultValues;

  @Parameterized.Parameter(4)
  public Date endDateForCheckWithDefaultValues;

  @Parameterized.Parameters
  public static Collection<Object[]> scenarios() throws ParseException {
    return Arrays.asList(new Object[][] {
        {  sdf.parse("2018-05-14T10:00:00"), sdf.parse("2018-05-14T22:00:00"), sdf.parse("2018-05-15T01:00:00"), null, null},  //monday
        {  sdf.parse("2018-05-14T23:00:00"), sdf.parse("2018-05-14T22:00:00"), sdf.parse("2018-05-15T01:00:00"), null, null},  //monday
        {  sdf.parse("2018-05-15T00:30:00"), sdf.parse("2018-05-14T22:00:00"), sdf.parse("2018-05-15T01:00:00"), null, null},  //tuesday
        {  sdf.parse("2018-05-15T02:00:00"), sdf.parse("2018-05-15T22:00:00"), sdf.parse("2018-05-15T23:00:00"), null, null},  //tuesday
        {  sdf.parse("2018-05-15T23:30:00"), sdf.parse("2018-05-16T15:00:00"), sdf.parse("2018-05-16T20:00:00"), null, null},  //tuesday
        {  sdf.parse("2018-05-16T21:00:00"), sdf.parse("2018-05-18T22:00:00"), sdf.parse("2018-05-19T01:00:00"),
              sdf.parse("2018-05-17T23:00:00"), sdf.parse("2018-05-18T00:00:00") },                                 //wednesday
        {  sdf.parse("2018-05-20T09:00:00"), sdf.parse("2018-05-20T10:00:00"), sdf.parse("2018-05-20T20:00:00"), null, null }} ); //sunday
  }

  @Before
  public void init() {
    historyService = engineRule.getHistoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    managementService = engineRule.getManagementService();

    defaultStartTime = processEngineConfiguration.getHistoryCleanupBatchWindowStartTime();

    defaultEndTime = processEngineConfiguration.getHistoryCleanupBatchWindowEndTime();
    defaultBatchSize = processEngineConfiguration.getHistoryCleanupBatchSize();
  }

  @After
  public void clearDatabase() {
    //reset configuration changes
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

        return null;
      }
    });
  }

  @Test
  public void testScheduleJobForBatchWindow() throws ParseException {
    ClockUtil.setCurrentTime(currentDate);

    Job job = historyService.cleanUpHistoryAsync();

    assertFalse(startDateForCheck.after(job.getDuedate())); // job due date is not before start date
    assertTrue(endDateForCheck.after(job.getDuedate()));

    ClockUtil.setCurrentTime(DateUtils.addMinutes(endDateForCheck, -1));

    job = historyService.cleanUpHistoryAsync();

    assertFalse(startDateForCheck.after(job.getDuedate()));
    assertTrue(endDateForCheck.after(job.getDuedate()));

    ClockUtil.setCurrentTime(DateUtils.addMinutes(endDateForCheck, 1));

    job = historyService.cleanUpHistoryAsync();

    assertTrue(endDateForCheck.before(job.getDuedate()));
  }

  @Test
  public void testScheduleJobForBatchWindowWithDefaultWindowConfigured() throws ParseException {
    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime("00:00");
    processEngineConfiguration.initHistoryCleanup();

    ClockUtil.setCurrentTime(currentDate);

    Job job = historyService.cleanUpHistoryAsync();

    if (startDateForCheckWithDefaultValues == null) {
      startDateForCheckWithDefaultValues = startDateForCheck;
    }
    if (endDateForCheckWithDefaultValues == null) {
      endDateForCheckWithDefaultValues = endDateForCheck;
    }

    assertFalse(startDateForCheckWithDefaultValues.after(job.getDuedate())); // job due date is not before start date
    assertTrue(endDateForCheckWithDefaultValues.after(job.getDuedate()));

    ClockUtil.setCurrentTime(DateUtils.addMinutes(endDateForCheckWithDefaultValues, -1));

    job = historyService.cleanUpHistoryAsync();

    assertFalse(startDateForCheckWithDefaultValues.after(job.getDuedate()));
    assertTrue(endDateForCheckWithDefaultValues.after(job.getDuedate()));

    ClockUtil.setCurrentTime(DateUtils.addMinutes(endDateForCheckWithDefaultValues, 1));

    job = historyService.cleanUpHistoryAsync();

    assertTrue(endDateForCheckWithDefaultValues.before(job.getDuedate()));
  }

  @Test
  public void testScheduleJobForBatchWindowWithShortcutConfiguration() throws ParseException {
    processEngineConfiguration.setThursdayHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setThursdayHistoryCleanupBatchWindowEndTime("00:00");
    processEngineConfiguration.setSaturdayHistoryCleanupBatchWindowStartTime("23:00");
    processEngineConfiguration.setSaturdayHistoryCleanupBatchWindowEndTime("00:00");
    processEngineConfiguration.initHistoryCleanup();

    ClockUtil.setCurrentTime(currentDate);

    Job job = historyService.cleanUpHistoryAsync();

    if (startDateForCheckWithDefaultValues == null) {
      startDateForCheckWithDefaultValues = startDateForCheck;
    }
    if (endDateForCheckWithDefaultValues == null) {
      endDateForCheckWithDefaultValues = endDateForCheck;
    }

    assertFalse(startDateForCheckWithDefaultValues.after(job.getDuedate())); // job due date is not before start date
    assertTrue(endDateForCheckWithDefaultValues.after(job.getDuedate()));

    ClockUtil.setCurrentTime(DateUtils.addMinutes(endDateForCheckWithDefaultValues, -1));

    job = historyService.cleanUpHistoryAsync();

    assertFalse(startDateForCheckWithDefaultValues.after(job.getDuedate()));
    assertTrue(endDateForCheckWithDefaultValues.after(job.getDuedate()));

    ClockUtil.setCurrentTime(DateUtils.addMinutes(endDateForCheckWithDefaultValues, 1));

    job = historyService.cleanUpHistoryAsync();

    assertTrue(endDateForCheckWithDefaultValues.before(job.getDuedate()));
  }

}
