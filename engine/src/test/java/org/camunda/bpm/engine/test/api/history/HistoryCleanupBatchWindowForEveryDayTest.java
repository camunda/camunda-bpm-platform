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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Anna Pazola
 *
 */
@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupBatchWindowForEveryDayTest {

  protected String defaultStartTime;
  protected String defaultEndTime;
  protected int defaultBatchSize;

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    configuration.setHistoryCleanupBatchSize(20);
    configuration.setHistoryCleanupBatchThreshold(10);
    configuration.setDefaultNumberOfRetries(5);
  });

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private HistoryService historyService;
  private ManagementService managementService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  @Parameterized.Parameter(0)
  public String startTime;

  @Parameterized.Parameter(1)
  public String endTime;

  @Parameterized.Parameter(2)
  public Date startDateForCheck;

  @Parameterized.Parameter(3)
  public Date endDateForCheck;

  @Parameterized.Parameter(4)
  public Date currentDate;

  @Parameterized.Parameters
  public static Collection<Object[]> scenarios() throws ParseException {
    return Arrays.asList(new Object[][] {
        // inside the batch window on the same day
        { "22:00", "23:00", sdf.parse("2017-09-06T22:00:00"), sdf.parse("2017-09-06T23:00:00"), sdf.parse("2017-09-06T22:15:00")},
        // inside the batch window on the next day
        { "23:00", "01:00", sdf.parse("2017-09-06T23:00:00"), sdf.parse("2017-09-07T01:00:00"), sdf.parse("2017-09-07T00:15:00")},
        // batch window 24h
        { "00:00", "00:00", sdf.parse("2017-09-06T00:00:00"), sdf.parse("2017-09-07T00:00:00"), sdf.parse("2017-09-06T15:00:00")},
        // batch window 24h
        { "00:00", "00:00", sdf.parse("2017-09-06T00:00:00"), sdf.parse("2017-09-07T00:00:00"), sdf.parse("2017-09-06T00:00:00")},
        // before the batch window on the same day
        { "22:00", "23:00", sdf.parse("2017-09-06T22:00:00"), sdf.parse("2017-09-06T23:00:00"), sdf.parse("2017-09-06T21:15:00")},
        // after the batch window on the same day
        { "22:00", "23:00", sdf.parse("2017-09-07T22:00:00"), sdf.parse("2017-09-07T23:00:00"), sdf.parse("2017-09-06T23:15:00")},
        // after the batch window on the next day
        { "22:00", "23:00", sdf.parse("2017-09-07T22:00:00"), sdf.parse("2017-09-07T23:00:00"), sdf.parse("2017-09-07T00:15:00")} });
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

    processEngineConfiguration.setHistoryCleanupBatchWindowStartTime(startTime);
    processEngineConfiguration.setHistoryCleanupBatchWindowEndTime(endTime);

    processEngineConfiguration.initHistoryCleanup();

    Job job = historyService.cleanUpHistoryAsync();

    assertFalse(startDateForCheck.after(job.getDuedate())); // job due date is not before start date
    assertTrue(endDateForCheck.after(job.getDuedate()));
  }
}
