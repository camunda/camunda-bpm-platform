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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.cfg.BatchWindowConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.Test;

/**
 * @author Nikola Koevski
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoryCleanupOnEngineBootstrapTest {

  private static final String ENGINE_NAME = "engineWithHistoryCleanupBatchWindow";

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  @Test
  public void testConsecutiveEngineBootstrapHistoryCleanupJobReconfiguration() {

    // given
    // create history cleanup job
    ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/batchwindow.camunda.cfg.xml")
      .buildProcessEngine()
      .close();

    // when
    // suspend history cleanup job
    ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/no-batchwindow.camunda.cfg.xml")
      .buildProcessEngine()
      .close();

    // then
    // reconfigure history cleanup job
    ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/batchwindow.camunda.cfg.xml");
    processEngineConfiguration.setProcessEngineName(ENGINE_NAME);
    ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

    assertNotNull(ProcessEngines.getProcessEngine(ENGINE_NAME));

    closeProcessEngine(processEngine);
  }

  @Test
  public void testDecreaseNumberOfHistoryCleanupJobs() {
    // given
    // create history cleanup job
    ProcessEngine engine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-parallelism-default.camunda.cfg.xml")
      .buildProcessEngine();

    // assume
    ManagementService managementService = engine.getManagementService();
    assertEquals(4, managementService.createJobQuery().list().size());

    engine.close();

    // when
    engine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-parallelism-less.camunda.cfg.xml")
      .buildProcessEngine();

    // then
    // reconfigure history cleanup job
    managementService = engine.getManagementService();
    assertEquals(1, managementService.createJobQuery().list().size());

    Job job = managementService.createJobQuery().singleResult();
    assertEquals(0, getHistoryCleanupJobHandlerConfiguration(job).getMinuteFrom());
    assertEquals(59, getHistoryCleanupJobHandlerConfiguration(job).getMinuteTo());

    closeProcessEngine(engine);
  }

  @Test
  public void testIncreaseNumberOfHistoryCleanupJobs() {
    // given
    // create history cleanup job
    ProcessEngine engine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-parallelism-default.camunda.cfg.xml")
      .buildProcessEngine();

    // assume
    ManagementService managementService = engine.getManagementService();
    assertEquals(4, managementService.createJobQuery().count());

    engine.close();

    // when
    engine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-parallelism-more.camunda.cfg.xml")
      .buildProcessEngine();

    // then
    // reconfigure history cleanup job
    managementService = engine.getManagementService();
    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(8, jobs.size());

    for (Job job : jobs) {
      int minuteTo = getHistoryCleanupJobHandlerConfiguration(job).getMinuteTo();
      int minuteFrom = getHistoryCleanupJobHandlerConfiguration(job).getMinuteFrom();

      if (minuteFrom == 0) {
        assertEquals(6, minuteTo);
      }
      else if (minuteFrom == 7) {
        assertEquals(13, minuteTo);
      }
      else if (minuteFrom == 14) {
        assertEquals(20, minuteTo);
      }
      else if (minuteFrom == 21) {
        assertEquals(27, minuteTo);
      }
      else if (minuteFrom == 28) {
        assertEquals(34, minuteTo);
      }
      else if (minuteFrom == 35) {
        assertEquals(41, minuteTo);
      }
      else if (minuteFrom == 42) {
        assertEquals(48, minuteTo);
      }
      else if (minuteFrom == 49) {
        assertEquals(59, minuteTo);
      }
      else {
        fail("unexpected minute from " + minuteFrom);
      }
    }

    closeProcessEngine(engine);
  }

  @Test
  public void testBatchWindowXmlConfigParsingException() throws ParseException {
    // when/then
    assertThatThrownBy(() -> ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-batch-window-map-wrong-values.camunda.cfg.xml")
      .buildProcessEngine())
    .isInstanceOf(Exception.class)
    .hasMessageContaining("startTime");
  }

  @Test
  public void testBatchWindowMapInXmlConfig() throws ParseException {
    // given
    //we're on Monday
    ClockUtil.setCurrentTime(sdf.parse("2018-05-14T22:00:00"));

    //when
    //we configure batch window only for Wednesday and start the server
    ProcessEngine engine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-batch-window-map.camunda.cfg.xml")
      .buildProcessEngine();

    //then
    //history cleanup is scheduled for Wednesday
    List<Job> historyCleanupJobs = engine.getHistoryService().findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    assertEquals(1, historyCleanupJobs.size());
    assertEquals(sdf.parse("2018-05-16T23:00:00"), historyCleanupJobs.get(0).getDuedate());
    assertEquals(false, historyCleanupJobs.get(0).isSuspended());

    engine.close();

    //when
    //we reconfigure batch window with default values
    engine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/history/history-cleanup-batch-window-default.camunda.cfg.xml")
      .buildProcessEngine();

    //then
    //history cleanup is scheduled for today
    historyCleanupJobs = engine.getHistoryService().findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    assertEquals(1, historyCleanupJobs.size());
    assertEquals(sdf.parse("2018-05-14T23:00:00"), historyCleanupJobs.get(0).getDuedate());
    assertEquals(false, historyCleanupJobs.get(0).isSuspended());

    closeProcessEngine(engine);
  }

  @Test
  public void testHistoryCleanupJobScheduled() throws ParseException {

    final ProcessEngineConfigurationImpl standaloneInMemProcessEngineConfiguration = (ProcessEngineConfigurationImpl)ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
    standaloneInMemProcessEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    standaloneInMemProcessEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00");
    standaloneInMemProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName() + "testHistoryCleanupJobScheduled");

    ProcessEngine engine = standaloneInMemProcessEngineConfiguration
      .buildProcessEngine();

    try {
      final List<Job> historyCleanupJobs = engine.getHistoryService().findHistoryCleanupJobs();
      assertFalse(historyCleanupJobs.isEmpty());
      final ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) engine.getProcessEngineConfiguration();
      for (Job historyCleanupJob : historyCleanupJobs) {
        assertEquals(processEngineConfiguration.getBatchWindowManager().getCurrentOrNextBatchWindow(ClockUtil.getCurrentTime(), processEngineConfiguration).getStart(), historyCleanupJob.getDuedate());
      }
    } finally {
      closeProcessEngine(engine);
    }
  }

  @Test
  public void shouldCreateHistoryCleanupJobLogs() {

    final ProcessEngineConfigurationImpl standaloneInMemProcessEngineConfiguration =
        (ProcessEngineConfigurationImpl)ProcessEngineConfiguration
            .createStandaloneInMemProcessEngineConfiguration();
    standaloneInMemProcessEngineConfiguration.setHistoryCleanupBatchWindowStartTime("23:00");
    standaloneInMemProcessEngineConfiguration.setHistoryCleanupBatchWindowEndTime("01:00");
    standaloneInMemProcessEngineConfiguration
        .setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName() + "testHistoryCleanupJobScheduled");

    ProcessEngine engine = standaloneInMemProcessEngineConfiguration.buildProcessEngine();
    try {
      List<HistoricJobLog> historicJobLogs = engine.getHistoryService()
                                                   .createHistoricJobLogQuery()
                                                   .jobDefinitionType(HistoryCleanupJobHandler.TYPE)
                                                   .list();
      for (HistoricJobLog historicJobLog : historicJobLogs) {
        assertNotNull(historicJobLog.getHostname());
      }
    } finally {
      closeProcessEngine(engine);
    }
  }

  @Test
  public void testBatchWindowOneDayOfWeek() throws ParseException {
    ClockUtil.setCurrentTime(sdf.parse("2018-05-14T22:00:00"));       //monday
    //given
    final ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl)ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
    //we have batch window only once per week - Monday afternoon
    configuration.getHistoryCleanupBatchWindows().put(Calendar.MONDAY, new BatchWindowConfiguration("18:00", "20:00"));
    configuration.setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName() + "testBatchWindowOneDayOfWeek");

    //when
    //we're on Monday evening
    //and we bootstrap the engine
    ProcessEngine engine = configuration.buildProcessEngine();

    //then
    //job is scheduled for next week Monday
    List<Job> historyCleanupJobs = engine.getHistoryService().findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    assertEquals(1, historyCleanupJobs.size());
    assertEquals(sdf.parse("2018-05-21T18:00:00"), historyCleanupJobs.get(0).getDuedate());     //monday next week
    assertEquals(false, historyCleanupJobs.get(0).isSuspended());

    //when
    //we're on Monday evening next week, right aftre the end of batch window
    ClockUtil.setCurrentTime(sdf.parse("2018-05-21T20:00:01"));       //monday
    //we force history job to be rescheduled
    engine.getManagementService().executeJob(historyCleanupJobs.get(0).getId());

    //then
    //job is scheduled for next week Monday
    historyCleanupJobs = engine.getHistoryService().findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    assertEquals(1, historyCleanupJobs.size());
    assertEquals(sdf.parse("2018-05-28T18:00:00"), historyCleanupJobs.get(0).getDuedate());     //monday next week
    assertEquals(false, historyCleanupJobs.get(0).isSuspended());

    closeProcessEngine(engine);
  }

  @Test
  public void testBatchWindow24Hours() throws ParseException {
    //given
    final ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl)ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
    //we have batch window for 24 hours
    configuration.getHistoryCleanupBatchWindows().put(Calendar.MONDAY, new BatchWindowConfiguration("06:00", "06:00"));
    configuration.setJdbcUrl("jdbc:h2:mem:camunda" + getClass().getSimpleName() + "testBatchWindow24Hours");

    //when
    //we're on Monday early morning
    ClockUtil.setCurrentTime(sdf.parse("2018-05-14T05:00:00"));       //monday
    //and we bootstrap the engine
    ProcessEngine engine = configuration.buildProcessEngine();

    //then
    //job is scheduled for Monday 06 AM
    List<Job> historyCleanupJobs = engine.getHistoryService().findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    assertEquals(1, historyCleanupJobs.size());
    assertEquals(sdf.parse("2018-05-14T06:00:00"), historyCleanupJobs.get(0).getDuedate());
    assertEquals(false, historyCleanupJobs.get(0).isSuspended());

    //when
    //we're on Monday afternoon
    ClockUtil.setCurrentTime(sdf.parse("2018-05-14T15:00:00"));
    //we force history job to be rescheduled
    engine.getManagementService().executeJob(historyCleanupJobs.get(0).getId());

    //then
    //job is still within current batch window
    historyCleanupJobs = engine.getHistoryService().findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    assertEquals(1, historyCleanupJobs.size());
    assertTrue(sdf.parse("2018-05-15T06:00:00").after(historyCleanupJobs.get(0).getDuedate()));
    assertEquals(false, historyCleanupJobs.get(0).isSuspended());

    //when
    //we're on Tuesday early morning close to the end of batch window
    ClockUtil.setCurrentTime(sdf.parse("2018-05-15T05:59:00"));
    //we force history job to be rescheduled
    engine.getManagementService().executeJob(historyCleanupJobs.get(0).getId());

    //then
    //job is still within current batch window
    historyCleanupJobs = engine.getHistoryService().findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    assertEquals(1, historyCleanupJobs.size());
    assertTrue(sdf.parse("2018-05-15T06:00:00").after(historyCleanupJobs.get(0).getDuedate()));
    assertEquals(false, historyCleanupJobs.get(0).isSuspended());

    //when
    //we're on Tuesday early morning shortly after the end of batch window
    ClockUtil.setCurrentTime(sdf.parse("2018-05-15T06:01:00"));
    //we force history job to be rescheduled
    engine.getManagementService().executeJob(historyCleanupJobs.get(0).getId());

    //then
    //job is rescheduled till next Monday
    historyCleanupJobs = engine.getHistoryService().findHistoryCleanupJobs();
    assertFalse(historyCleanupJobs.isEmpty());
    assertEquals(1, historyCleanupJobs.size());
    assertEquals(sdf.parse("2018-05-21T06:00:00"), historyCleanupJobs.get(0).getDuedate());
    assertEquals(false, historyCleanupJobs.get(0).isSuspended());

    closeProcessEngine(engine);
  }

  protected HistoryCleanupJobHandlerConfiguration getHistoryCleanupJobHandlerConfiguration(Job job) {
    return HistoryCleanupJobHandlerConfiguration
          .fromJson(JsonUtil.asObject(((JobEntity) job).getJobHandlerConfigurationRaw()));
  }

  protected void closeProcessEngine(ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    final HistoryService historyService = processEngine.getHistoryService();
    configuration.getCommandExecutorTxRequired().execute((Command<Void>) commandContext -> {

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

      commandContext.getMeterLogManager().deleteAll();

      return null;
    });

    processEngine.close();
  }

}
