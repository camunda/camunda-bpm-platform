/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.DefaultHistoryRemovalTimeProvider;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.junit.After;
import org.junit.AfterClass;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_END_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHandler.MAX_BATCH_SIZE;

/**
 * @author Tassilo Weidner
 */
public abstract class AbstractHistoryCleanupSchedulerTest {

  protected Class<?> thisClass = this.getClass();

  protected HistoryLevel customHistoryLevel = new CustomHistoryLevelRemovalTime();

  protected static ProcessEngineConfigurationImpl engineConfiguration;

  protected Set<String> jobIds = new HashSet<>();

  protected HistoryService historyService;
  protected ManagementService managementService;

  protected final Date END_DATE = new Date(1363608000000L);
  
  protected String oldHistoryLevel = "";
  protected String jdbcUrl = "";
  protected String jdbcUser =  "";
  protected String jdbcPwd =  "";
  protected String jdbcPrefix = "";

  public void initEngineConfiguration(ProcessEngineConfigurationImpl engineConfiguration) {
    engineConfiguration
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END)
      .setHistoryRemovalTimeProvider(new DefaultHistoryRemovalTimeProvider())
      .initHistoryRemovalTime();

    engineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED);

    engineConfiguration.setHistoryCleanupBatchSize(MAX_BATCH_SIZE);
    engineConfiguration.setHistoryCleanupBatchWindowStartTime("13:00");
    engineConfiguration.setHistoryCleanupDegreeOfParallelism(1);

    engineConfiguration.initHistoryCleanup();
  }

  protected void saveCurrentHistoryLevelAndUpdateTo(ProcessEngineConfigurationImpl tmpEngineConf, int newHistoryLevel) {
    String oldValue = null; 
    try {
      jdbcUrl = tmpEngineConf.getJdbcUrl();
      jdbcUser =  tmpEngineConf.getJdbcUsername();
      jdbcPwd =  tmpEngineConf.getJdbcPassword();
      jdbcPrefix = tmpEngineConf.getDatabaseTablePrefix();

      Connection con = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPwd);
      Statement stat = con.createStatement();

      ResultSet rs= stat.executeQuery("SELECT VALUE_ FROM " + jdbcPrefix + "ACT_GE_PROPERTY WHERE NAME_ = 'historyLevel' ");
      if (rs.next()) {
        oldValue = rs.getString(1);
      }
      rs.close();
      stat.close();

      stat = con.createStatement();
      if (oldValue == null) {
        stat.execute("INSERT INTO " + jdbcPrefix + "ACT_GE_PROPERTY (NAME_, VALUE_, REV_) VALUES ('historyLevel', '" + newHistoryLevel + "', 1)"  );
      } else {
        stat.execute("UPDATE " + jdbcPrefix + "ACT_GE_PROPERTY SET VALUE_ = '" + newHistoryLevel + "' WHERE NAME_ = 'historyLevel' ");
      }
      stat.close();
      con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    oldHistoryLevel=  oldValue;
  }

  private void setHistoryLevelBackToOriginal(String newHistoryLevel) {
    System.out.println("HistoryLevel set back to " + newHistoryLevel);
    try {
      Connection con = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPwd);
      Statement stat = con.createStatement();
      if (newHistoryLevel != null)
        stat.execute("UPDATE " + jdbcPrefix + "ACT_GE_PROPERTY SET VALUE_ = '" + newHistoryLevel + "' WHERE NAME_ = 'historyLevel' ");
      else {
        stat.execute("DELETE FROM " + jdbcPrefix + "ACT_GE_PROPERTY WHERE NAME_ = 'historyLevel' ");
      }
      stat.close();
      con.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @After
  public void tearDown() {
    setHistoryLevelBackToOriginal(oldHistoryLevel);
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

      engineConfiguration.setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_END_TIME_BASED);

      engineConfiguration.setHistoryCleanupBatchSize(MAX_BATCH_SIZE);
      engineConfiguration.setHistoryCleanupBatchWindowStartTime(null);
      engineConfiguration.setHistoryCleanupDegreeOfParallelism(1);

      engineConfiguration.initHistoryCleanup();
    }

    ClockUtil.reset();
  }

  // helper /////////////////////////////////////////////////////////////////

  protected List<HistoryLevel> setCustomHistoryLevel(HistoryEventTypes eventType) {
    ((CustomHistoryLevelRemovalTime)customHistoryLevel).setEventTypes(eventType);

    return Collections.singletonList(customHistoryLevel);
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

  protected void clearJobLog(final String jobId) {
    CommandExecutor commandExecutor = engineConfiguration.getCommandExecutorTxRequired();
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

  protected List<HistoryLevel> setCustomHistoryLevel(HistoryEventTypes... eventType) {
    ((CustomHistoryLevelRemovalTime)customHistoryLevel).setEventTypes(eventType);

    return Collections.singletonList(customHistoryLevel);
  }

  public ProcessEngineConfiguration configure(ProcessEngineConfigurationImpl configuration, HistoryEventTypes... historyEventTypes) {
    configuration.setCustomHistoryLevels(setCustomHistoryLevel(historyEventTypes));
    configuration.setHistory(customHistoryLevel.getName());
    return configuration;
  }

}
