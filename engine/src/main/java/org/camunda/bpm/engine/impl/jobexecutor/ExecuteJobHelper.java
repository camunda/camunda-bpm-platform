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

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.interceptor.ProcessDataContext;

public class ExecuteJobHelper {

  private static final JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  public static ExceptionLoggingHandler LOGGING_HANDLER = new ExceptionLoggingHandler() {

    @Override
    public void exceptionWhileExecutingJob(String jobId, Throwable exception) {

      // Default behavior, just log exception
      LOG.exceptionWhileExecutingJob(jobId, exception);
    }

  };

  public static void executeJob(String jobId, CommandExecutor commandExecutor) {

    JobFailureCollector jobFailureCollector = new JobFailureCollector(jobId);

    executeJob(jobId, commandExecutor, jobFailureCollector, new ExecuteJobsCmd(jobId, jobFailureCollector));

  }

  public static void executeJob(String nextJobId, CommandExecutor commandExecutor, JobFailureCollector jobFailureCollector, Command<Void> cmd) {
    executeJob(nextJobId, commandExecutor, jobFailureCollector, cmd, null);
  }

  public static void executeJob(String nextJobId, CommandExecutor commandExecutor, JobFailureCollector jobFailureCollector, Command<Void> cmd,
      ProcessEngineConfigurationImpl configuration) {
    try {
      commandExecutor.execute(cmd);
    } catch (RuntimeException exception) {
      handleJobFailure(nextJobId, jobFailureCollector, exception);
      // throw the original exception to indicate the ExecuteJobCmd failed
      throw exception;
    } catch (Throwable exception) {
      handleJobFailure(nextJobId, jobFailureCollector, exception);
      // wrap the exception and throw it to indicate the ExecuteJobCmd failed
      throw LOG.wrapJobExecutionFailure(jobFailureCollector, exception);
    } finally {
      // preserve MDC properties before listener invocation and clear MDC for job listener
      ProcessDataContext processDataContext = null;
      if (configuration != null) {
        processDataContext = new ProcessDataContext(configuration, true);
        processDataContext.clearMdc();
      }
      // invoke job listener
      invokeJobListener(commandExecutor, jobFailureCollector);
      /*
       * reset MDC properties after successful listener invocation,
       * in case of an exception in the listener the logging context
       * of the listener is preserved and used from here on
       */
      if (processDataContext != null) {
        processDataContext.updateMdcFromCurrentValues();
      }
    }
  }

  protected static void invokeJobListener(CommandExecutor commandExecutor, JobFailureCollector jobFailureCollector) {
    if(jobFailureCollector.getJobId() != null) {
      if (jobFailureCollector.getFailure() != null) {
        // the failed job listener is responsible for decrementing the retries and logging the exception to the DB.

        FailedJobListener failedJobListener = createFailedJobListener(commandExecutor, jobFailureCollector);

        OptimisticLockingException exception = callFailedJobListenerWithRetries(commandExecutor, failedJobListener);
        if (exception != null) {
          throw exception;
        }

      } else {
        SuccessfulJobListener successListener = createSuccessfulJobListener(commandExecutor);
        commandExecutor.execute(successListener);
      }
    }
  }

  /**
   * Calls FailedJobListener, in case of OptimisticLockException retries configured amount of times.
   *
   * @return exception or null if succeeded
   */
  private static OptimisticLockingException callFailedJobListenerWithRetries(CommandExecutor commandExecutor, FailedJobListener failedJobListener) {
    try {
      commandExecutor.execute(failedJobListener);
      return null;
    } catch (OptimisticLockingException ex) {
      failedJobListener.incrementCountRetries();
      if (failedJobListener.getRetriesLeft() > 0) {
        return callFailedJobListenerWithRetries(commandExecutor, failedJobListener);
      }
      return ex;
    }
  }

  protected static void handleJobFailure(final String nextJobId, final JobFailureCollector jobFailureCollector, Throwable exception) {
    jobFailureCollector.setFailure(exception);
  }


  protected static FailedJobListener createFailedJobListener(CommandExecutor commandExecutor, JobFailureCollector jobFailureCollector) {
    return new FailedJobListener(commandExecutor, jobFailureCollector);
  }

  protected static SuccessfulJobListener createSuccessfulJobListener(CommandExecutor commandExecutor) {
    return new SuccessfulJobListener();
  }

  public interface ExceptionLoggingHandler {
    void exceptionWhileExecutingJob(String jobId, Throwable exception);
  }

}
