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

import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.management.Metrics;


/**
 * @author Frederik Heremans
 * @author Bernd Ruecker
 */
public class FailedJobListener implements TransactionListener, CommandContextListener {

  protected CommandExecutor commandExecutor;
  protected String jobId;
  protected Throwable exception;

  public FailedJobListener(CommandExecutor commandExecutor, String jobId, Throwable exception) {
    this(commandExecutor, jobId);
    this.exception = exception;
  }

  public FailedJobListener(CommandExecutor commandExecutor, String jobId) {
    this.commandExecutor = commandExecutor;
    this.jobId = jobId;
  }

  public void execute(CommandContext commandContext) {
    logJobFailure(commandContext);

    FailedJobCommandFactory failedJobCommandFactory = commandContext.getFailedJobCommandFactory();
    final Command<Object> cmd = failedJobCommandFactory.getCommand(jobId, exception);

    commandExecutor.execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {
        fireHistoricJobFailedEvt(jobId);
        cmd.execute(commandContext);
        return null;
      }

    });
  }

  protected void fireHistoricJobFailedEvt(String jobId) {
    CommandContext commandContext = Context.getCommandContext();
    JobEntity job = commandContext
        .getJobManager()
        .findJobById(jobId);

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

  public void setException(Throwable exception) {
    this.exception = exception;
  }

  public Throwable getException() {
    return exception;
  }

  public void onCommandContextClose(CommandContext commandContext) {
    // ignored
  }

  public void onCommandFailed(CommandContext commandContext, Throwable t) {
    // log exception if not already present
    if(this.exception == null) {
      this.exception = t;
    }
  }

}
