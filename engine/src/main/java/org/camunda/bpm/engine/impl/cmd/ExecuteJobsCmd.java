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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.FailedJobListener;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorLogger;
import org.camunda.bpm.engine.impl.jobexecutor.SuccessfulJobListener;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ExecuteJobsCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected String jobId;

  public ExecuteJobsCmd(String jobId) {
    this.jobId = jobId;
  }

  public Object execute(CommandContext commandContext) {
    ensureNotNull("jobId", jobId);

    JobEntity job = commandContext.getDbEntityManager().selectById(JobEntity.class, jobId);

    final CommandExecutor commandExecutor = Context.getProcessEngineConfiguration().getCommandExecutorTxRequiresNew();
    final JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();

    if (job == null) {

      if (jobExecutorContext != null) {
        // CAM-1842
        // Job was acquired but does not exist anymore. This is not a problem.
        // It usually means that the job has been deleted after it was acquired which can happen if the
        // the activity instance corresponding to the job is cancelled.
        LOG.debugAcquiredJobNotFound(jobId);
        return null;

      } else {
        throw new ProcessEngineException("No job found with id '" + jobId + "'");

      }

    }

    if (jobExecutorContext == null) { // if null, then we are not called by the job executor
      AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
      authorizationManager.checkUpdateProcessInstance(job);
    }

    // set the given job to executing
    job.setExecuting(true);

    // the failed job listener is responsible for decrementing the retries and logging the exception to the DB.
    FailedJobListener failedJobListener = createFailedJobListener(commandExecutor);

    // the listener is ALWAYS added to the transaction as SYNC on ROLLBACK. If the transaction does not rollback, it is ignored.
    commandContext.getTransactionContext().addTransactionListener(
        TransactionState.ROLLED_BACK,
        failedJobListener);

    // register as command context close lister to intercept exceptions on flush
    commandContext.registerCommandContextListener(failedJobListener);

    // register a listener in case job is executed successfully
    SuccessfulJobListener successListener = createSuccessfulJobListener(commandExecutor);
    commandContext.getTransactionContext().addTransactionListener(
        TransactionState.COMMITTED,
        successListener);

    if (jobExecutorContext != null) { // if null, then we are not called by the job executor
      jobExecutorContext.setCurrentJob(job);
    }

    try {
      job.execute(commandContext);
      return null;
    }
    catch (RuntimeException exception) {

      LOG.exceptionWhileExecutingJob(job, exception);

      // log the exception in the job
      failedJobListener.setException(exception);

      // throw the original exception to indicate the ExecuteJobCmd failed
      throw exception;

    } finally {
      if (jobExecutorContext != null) {
        jobExecutorContext.setCurrentJob(null);
      }
    }

  }

  protected FailedJobListener createFailedJobListener(CommandExecutor commandExecutor) {
    return new FailedJobListener(commandExecutor, jobId);
  }

  protected SuccessfulJobListener createSuccessfulJobListener(CommandExecutor commandExecutor) {
    return new SuccessfulJobListener();
  }

}
