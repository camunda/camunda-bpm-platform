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

import java.util.Collection;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * @author Daniel Meyer
 *
 */
public class JobExecutorLogger extends ProcessEngineLogger {

  public void debugAcquiredJobNotFound(String jobId) {
    logDebug(
        "001", "Acquired job with id '{}' not found.", jobId);
  }

  public void exceptionWhileExecutingJob(JobEntity job, Throwable exception) {
    logWarn(
        "002", "Exception while executing job {}: ", job, exception);
  }

  public void debugFallbackToDefaultRetryStrategy() {
    logDebug(
        "003", "Falling back to default retry strategy");
  }

  public void debugDecrementingRetriesForJob(String id) {
    logDebug(
        "004", "Decrementing retries of job {}", id);
  }

  public void debugInitiallyAppyingRetryCycleForJob(String id, int times) {
    logDebug(
        "005", "Applying job retry time cycle for the first time for job {}, retires {}", id, times);
  }

  public void exceptionWhileExecutingJob(String nextJobId, Throwable t) {
    logWarn(
        "006", "Exception while executing job {}: ", nextJobId, t);
  }

  public void couldNotDeterminePriority(ExecutionEntity execution, Object value, ProcessEngineException e) {
    logWarn(
        "007",
        "Could not determine priority for job created in context of execution {}. Using default priority {}",
        execution, value, e);
  }

  public void debugAddingNewExclusiveJobToJobExecutorCOntext(String jobId) {
    logInfo(
        "008",
        "Adding new exclusive job to job executor context. Job Id='{}'", jobId);
  }

  public void timeoutDuringShutdown() {
    logWarn(
        "009",
        "Timeout during shutdown of job executor. The current running jobs could not end within 60 seconds after shutdown operation");
  }

  public void interruptedWhileShuttingDownjobExecutor(InterruptedException e) {
    logWarn(
        "010",
        "Interrupted while shutting down the job executor", e);
  }

  public void debugJobAcquisitionThreadSleeping(long millis) {
    logDebug("011", "Job acquisition thread sleeping for {} millis", millis);
  }

  public void jobExecutorThreadWokeUp() {
    logDebug("012", "Job acquisition thread woke up");
  }

  public void jobExecutionWaitInterrupted() {
    logDebug("013", "Job Execution wait interrupted");
  }

  public void startingUpJobExecutor(String name) {
    logInfo(
        "014", "Starting up the JobExecutor[{}].", name);
  }

  public void shuttingDownTheJobExecutor(String name) {
    logInfo(
        "015", "Shutting down the JobExecutor[{}]", name);
  }

  public void ignoringSuspendedJob(ProcessDefinition processDefinition) {
    logDebug(
        "016",
        "Ignoring job of suspended {}", processDefinition);
  }

  public void debugNotifyingJobExecutor(String string) {
    logDebug(
        "017", "Notifying Job Executor of new job {}", string);
  }

  public void startingToAcquireJobs(String name) {
    logInfo(
        "018", "{} starting to acquire jobs", name);
  }

  public void exceptionDuringJobAcquisition(Exception e) {
    logError(
        "019", "Exception during job acquisition {}", e.getMessage(), e);
  }

  public void stoppedJobAcquisition(String name) {
    logInfo(
        "020", "{} stopped job acquisition", name);
  }

  public void exceptionWhileUnlockingJob(String jobId, Throwable t) {
    logWarn(
        "021",
        "Exception while unaquiring job {}: ", jobId, t);
  }

  public void acquiredJobs(String processEngine, AcquiredJobs acquiredJobs) {
    logDebug(
        "022",
        "Acquired {} jobs for process engine '{}': {}", acquiredJobs.size(), processEngine, acquiredJobs.getJobIdBatches());
  }

  public void executeJobs(String processEngine, Collection<String> jobs) {
    logDebug(
        "023",
        "Execute jobs for process engine '{}': {}", processEngine, jobs);
  }

  public void debugFailedJobNotFound(String jobId) {
    logDebug(
        "024", "Failed job with id '{}' not found.", jobId);
  }

  public ProcessEngineException wrapJobExecutionFailure(JobFailureCollector jobFailureCollector, Throwable cause) {
    JobEntity job = jobFailureCollector.getJob();
    if (job != null) {
      return new ProcessEngineException(exceptionMessage(
        "025", "Exception while executing job {}: ", jobFailureCollector.getJob()), cause);
    }
    else {
      return new ProcessEngineException(exceptionMessage(
         "025", "Exception while executing job {}: ", jobFailureCollector.getJobId()), cause);
    }
  }

  public ProcessEngineException jobNotFoundException(String jobId) {
    return new ProcessEngineException(exceptionMessage(
        "026", "No job found with id '{}'", jobId));
  }

  public void exceptionWhileParsingExpression(String jobId, String exceptionMessage) {
    logWarn(
        "027", "Falling back to default retry strategy. Exception while executing job {}: {}", jobId, exceptionMessage);
  }

  public void warnHistoryCleanupBatchWindowNotFound() {
    logWarn(
      "028",
      "Batch window for history cleanup was not calculated. History cleanup job(s) will be suspended.");
  }

}
