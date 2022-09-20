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
package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.management.Metrics;

/**
 * @author Frederik Heremans
 * @author Bernd Ruecker
 */
public class FailedJobListener implements Command<Void> {

  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected CommandExecutor commandExecutor;
  protected JobFailureCollector jobFailureCollector;
  protected int countRetries = 0;
  protected int totalRetries = ProcessEngineConfigurationImpl.DEFAULT_FAILED_JOB_LISTENER_MAX_RETRIES;

  public FailedJobListener(CommandExecutor commandExecutor, JobFailureCollector jobFailureCollector) {
    this.commandExecutor = commandExecutor;
    this.jobFailureCollector = jobFailureCollector;
  }

  public Void execute(CommandContext commandContext) {
    if (isJobReacquired(commandContext)) {
      // skip failed listener if job has been already re-acquired
      LOG.debugFailedJobListenerSkipped(jobFailureCollector.getJobId());
      return null;
    }

    initTotalRetries(commandContext);

    logJobFailure(commandContext);

    FailedJobCommandFactory failedJobCommandFactory = commandContext.getFailedJobCommandFactory();
    String jobId = jobFailureCollector.getJobId();
    Command<Object> cmd = failedJobCommandFactory.getCommand(jobId, jobFailureCollector.getFailure());
    commandExecutor.execute(new FailedJobListenerCmd(jobId, cmd));

    return null;
  }

  protected boolean isJobReacquired(CommandContext commandContext) {
    // if persisted job's lockExpirationTime is different, then it's been already re-acquired
    JobEntity persistedJob = commandContext.getJobManager().findJobById(jobFailureCollector.getJobId());
    JobEntity job = jobFailureCollector.getJob();

    if (persistedJob == null || persistedJob.getLockExpirationTime() == null) {
      return false;
    }
    return !persistedJob.getLockExpirationTime().equals(job.getLockExpirationTime());
  }

  private void initTotalRetries(CommandContext commandContext) {
    totalRetries = commandContext.getProcessEngineConfiguration().getFailedJobListenerMaxRetries();
  }

  protected void fireHistoricJobFailedEvt(JobEntity job) {
    CommandContext commandContext = Context.getCommandContext();

    // the given job failed and a rollback happened,
    // that's why we have to increment the job
    // sequence counter once again
    job.incrementSequenceCounter();

    commandContext
            .getHistoricJobLogManager()
            .fireJobFailedEvent(job, jobFailureCollector.getFailure());
  }

  protected void logJobFailure(CommandContext commandContext) {
    if (commandContext.getProcessEngineConfiguration().isMetricsEnabled()) {
      commandContext.getProcessEngineConfiguration()
              .getMetricsRegistry()
              .markOccurrence(Metrics.JOB_FAILED);
    }
  }

  public void incrementCountRetries() {
    this.countRetries++;
  }

  public int getRetriesLeft() {
    return Math.max(0, totalRetries - countRetries);
  }

  protected class FailedJobListenerCmd implements Command<Void> {

    protected String jobId;
    protected Command<Object> cmd;

    public FailedJobListenerCmd(String jobId, Command<Object> cmd) {
      this.jobId = jobId;
      this.cmd = cmd;
    }

    @Override
    public Void execute(CommandContext commandContext) {
      JobEntity job = commandContext
          .getJobManager()
          .findJobById(jobId);

      if (job != null) {
        job.setFailedActivityId(jobFailureCollector.getFailedActivityId());
        fireHistoricJobFailedEvt(job);
        cmd.execute(commandContext);
      } else {
        LOG.debugFailedJobNotFound(jobId);
      }
      return null;
    }
  }

}
