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

import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_END_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHandler.MAX_BATCH_SIZE;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

/**
 * @author Tassilo Weidner
 */
public abstract class AbstractHistoryCleanupSchedulerTest {

  protected Class<?> thisClass = this.getClass();

  protected static HistoryLevel customHistoryLevel = new CustomHistoryLevelRemovalTime();

  protected static ProcessEngineConfigurationImpl engineConfiguration;

  protected Set<String> jobIds = new HashSet<>();

  protected HistoryService historyService;
  protected ManagementService managementService;

  protected final Date END_DATE = new Date(1363608000000L);

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

  protected static List<HistoryLevel> setCustomHistoryLevel(HistoryEventTypes... eventType) {
    ((CustomHistoryLevelRemovalTime)customHistoryLevel).setEventTypes(eventType);

    return Collections.singletonList(customHistoryLevel);
  }

  public static ProcessEngineConfiguration configure(ProcessEngineConfigurationImpl configuration, HistoryEventTypes... historyEventTypes) {
    configuration.setJdbcUrl("jdbc:h2:mem:" + AbstractHistoryCleanupSchedulerTest.class.getSimpleName());
    configuration.setCustomHistoryLevels(setCustomHistoryLevel(historyEventTypes));
    configuration.setHistory(customHistoryLevel.getName());
    configuration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_CREATE_DROP);
    return configuration;
  }

}
