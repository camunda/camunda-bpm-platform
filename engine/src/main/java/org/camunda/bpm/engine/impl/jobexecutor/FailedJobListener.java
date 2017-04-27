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

  protected CommandExecutor commandExecutor;
  protected String jobId;
  protected Throwable exception;
  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;
  private int countRetries = 0;
  private int totalRetries = ProcessEngineConfigurationImpl.DEFAULT_FAILED_JOB_LISTENER_MAX_RETRIES;

  public FailedJobListener(CommandExecutor commandExecutor, String jobId, Throwable exception) {
    this.commandExecutor = commandExecutor;
    this.jobId = jobId;
    this.exception = exception;
  }

  public Void execute(CommandContext commandContext) {
    initTotalRetries(commandContext);

    logJobFailure(commandContext);

    FailedJobCommandFactory failedJobCommandFactory = commandContext.getFailedJobCommandFactory();
    final Command<Object> cmd = failedJobCommandFactory.getCommand(jobId, exception);

    commandExecutor.execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {
        JobEntity job = commandContext
                .getJobManager()
                .findJobById(jobId);

        if (job != null) {
          fireHistoricJobFailedEvt(job);
          cmd.execute(commandContext);
        } else {
          LOG.debugFailedJobNotFound(jobId);
        }
        return null;
      }
    });

    return null;
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
            .fireJobFailedEvent(job, exception);
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
}
