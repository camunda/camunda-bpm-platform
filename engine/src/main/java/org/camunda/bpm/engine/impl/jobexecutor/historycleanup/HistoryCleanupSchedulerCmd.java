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
package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Date;
import java.util.Map;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorLogger;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Tassilo Weidner
 */
public class HistoryCleanupSchedulerCmd implements Command<Void> {

  protected final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected boolean isRescheduleNow;
  protected HistoryCleanupJobHandlerConfiguration configuration;
  protected String jobId;
  protected Map<String, Long> reports;

  public HistoryCleanupSchedulerCmd(boolean isRescheduleNow, Map<String, Long> reports, HistoryCleanupJobHandlerConfiguration configuration, String jobId) {
    this.isRescheduleNow = isRescheduleNow;
    this.configuration = configuration;
    this.jobId = jobId;
    this.reports = reports;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    if (isMetricsEnabled()) {
      reportMetrics(commandContext);
    }

    JobEntity jobEntity = commandContext.getJobManager().findJobById(jobId);

    boolean rescheduled = false;

    if (isRescheduleNow) {
      commandContext.getJobManager().reschedule(jobEntity, ClockUtil.getCurrentTime());
      rescheduled = true;
      cancelCountEmptyRuns(configuration, jobEntity);
    } else {
      if (HistoryCleanupHelper.isWithinBatchWindow(ClockUtil.getCurrentTime(), commandContext.getProcessEngineConfiguration())) {
        Date nextRunDate = configuration.getNextRunWithDelay(ClockUtil.getCurrentTime());
        if (HistoryCleanupHelper.isWithinBatchWindow(nextRunDate, commandContext.getProcessEngineConfiguration())) {
          commandContext.getJobManager().reschedule(jobEntity, nextRunDate);
          rescheduled = true;
          incrementCountEmptyRuns(configuration, jobEntity);
        }
      }
    }

    if (!rescheduled) {
      if (HistoryCleanupHelper.isBatchWindowConfigured(commandContext)) {
        rescheduleRegularCall(commandContext, jobEntity);
      } else {
        suspendJob(jobEntity);
      }
      cancelCountEmptyRuns(configuration, jobEntity);
    }

    return null;
  }

  protected void rescheduleRegularCall(CommandContext commandContext, JobEntity jobEntity) {
    final BatchWindow nextBatchWindow = commandContext.getProcessEngineConfiguration().getBatchWindowManager()
      .getNextBatchWindow(ClockUtil.getCurrentTime(), commandContext.getProcessEngineConfiguration());
    if (nextBatchWindow != null) {
      commandContext.getJobManager().reschedule(jobEntity, nextBatchWindow.getStart());
    } else {
      LOG.warnHistoryCleanupBatchWindowNotFound();
      suspendJob(jobEntity);
    }
  }

  protected void suspendJob(JobEntity jobEntity) {
    jobEntity.setSuspensionState(SuspensionState.SUSPENDED.getStateCode());
  }

  protected void incrementCountEmptyRuns(HistoryCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
    configuration.setCountEmptyRuns(configuration.getCountEmptyRuns() + 1);
    jobEntity.setJobHandlerConfiguration(configuration);
  }

  protected void cancelCountEmptyRuns(HistoryCleanupJobHandlerConfiguration configuration, JobEntity jobEntity) {
    configuration.setCountEmptyRuns(0);
    jobEntity.setJobHandlerConfiguration(configuration);
  }

  protected void reportMetrics(CommandContext commandContext) {
    ProcessEngineConfigurationImpl engineConfiguration = commandContext.getProcessEngineConfiguration();
    if (engineConfiguration.isHistoryCleanupMetricsEnabled()) {
      for (Map.Entry<String, Long> report : reports.entrySet()){
        engineConfiguration.getDbMetricsReporter().reportValueAtOnce(report.getKey(), report.getValue());
      }
    }
  }

  protected boolean isMetricsEnabled() {
    return Context
        .getProcessEngineConfiguration()
        .isHistoryCleanupMetricsEnabled();
  }
}
