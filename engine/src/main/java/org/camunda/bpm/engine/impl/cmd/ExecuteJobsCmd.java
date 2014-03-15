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
package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.FailedJobListener;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ExecuteJobsCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(ExecuteJobsCmd.class.getName());

  protected String jobId;

  public ExecuteJobsCmd(String jobId) {
    this.jobId = jobId;
  }

  public Object execute(CommandContext commandContext) {
    if(jobId == null) {
      throw new ProcessEngineException("jobId is null");
    }

    if (log.isLoggable(Level.FINE)) {
      log.fine("Executing job " + jobId);
    }
    JobEntity job = commandContext
      .getJobManager()
      .findJobById(jobId);

    final CommandExecutor commandExecutor = Context.getProcessEngineConfiguration().getCommandExecutorTxRequiresNew();
    final JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();

    if (job == null) {

      if(jobExecutorContext != null) {
        // CAM-1842
        // Job was acquired but does not exist anymore. This is not a problem.
        // It usually means that the job has been deleted after it was acquired which can happen if the
        // the activity instance corresponding to the job is cancelled.
        log.log(Level.FINE, "Job with Id " + jobId + " was acquired but cannot be found in database.");
        return null;

      } else {
        throw new ProcessEngineException("No job found with id '" + jobId + "'");

      }

    }


    if(jobExecutorContext != null) { // if null, then we are not called by the job executor
      jobExecutorContext.setCurrentJob(job);
    }

    try {
      job.execute(commandContext);
      return null;

    } catch (RuntimeException exception) {

      // the failed job listener is responsible for decrementing the retries and logging the exception to the DB.
      FailedJobListener failedJobListener = createFailedJobListener(exception, commandExecutor);

      try {

        commandContext.getTransactionContext().addTransactionListener(
                TransactionState.ROLLED_BACK,
                failedJobListener);

      } catch(Exception ex) {
        // if the TX has already been rolled back, the listener cannot be registered.
        log.log(Level.FINE, "Could not register transaction synchronization. Probably the TX has already been rolled back by application code.", ex);

        // Execute the listener in new TX, here.
        executeCmdInNewTx(commandContext, failedJobListener);

      }

      // throw the original exception to indicate the ExecuteJobCmd failed
      throw exception;

    } finally {
      if(jobExecutorContext != null) {
        jobExecutorContext.setCurrentJob(null);
      }
    }

  }

  protected void executeCmdInNewTx(CommandContext commandContext, FailedJobListener failedJobListener) {
    try {

      failedJobListener.execute(commandContext);

    } catch(Exception ex) {
      log.log(Level.SEVERE, "Could not execute the FailedJobListener: "+ex.getMessage(), ex);
    }
  }

  protected FailedJobListener createFailedJobListener(RuntimeException exception, CommandExecutor commandExecutor) {
    return new FailedJobListener(commandExecutor, jobId, exception);
  }
}
