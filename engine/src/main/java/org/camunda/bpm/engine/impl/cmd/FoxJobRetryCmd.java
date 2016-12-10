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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.FoxFailedJobParseListener;
import org.camunda.bpm.engine.impl.calendar.DurationHelper;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.*;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

import java.util.Arrays;
import java.util.List;

/**
 * @author Roman Smirnov
 */
public class FoxJobRetryCmd extends JobRetryCmd {

  public static final List<String> SUPPORTED_TYPES = Arrays.asList(
      TimerExecuteNestedActivityJobHandler.TYPE,
      TimerCatchIntermediateEventJobHandler.TYPE,
      TimerStartEventJobHandler.TYPE,
      TimerStartEventSubprocessJobHandler.TYPE,
      AsyncContinuationJobHandler.TYPE
  );
  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  public FoxJobRetryCmd(String jobId, Throwable exception) {
    super(jobId, exception);
  }

  public Object execute(CommandContext commandContext) {
    JobEntity job = getJob();

    ActivityImpl activity = getCurrentActivity(commandContext, job);

    if (activity == null) {
      LOG.debugFallbackToDefaultRetryStrategy();
      executeStandardStrategy(commandContext);

    } else {
      try {
        executeCustomStrategy(commandContext, job, activity);

      } catch (Exception e) {
        LOG.debugFallbackToDefaultRetryStrategy();
        executeStandardStrategy(commandContext);
      }
    }

    return null;
  }

  protected void executeStandardStrategy(CommandContext commandContext) {
    DecrementJobRetriesCmd decrementCmd = new DecrementJobRetriesCmd(jobId, exception);
    decrementCmd.execute(commandContext);
  }

  protected void executeCustomStrategy(CommandContext commandContext, JobEntity job, ActivityImpl activity) throws Exception {
    String failedJobRetryTimeCycle = getFailedJobRetryTimeCycle(activity);

    if(failedJobRetryTimeCycle == null) {
      executeStandardStrategy(commandContext);

    } else {
      DurationHelper durationHelper = getDurationHelper(failedJobRetryTimeCycle);

      setLockExpirationTime(job, failedJobRetryTimeCycle, durationHelper);

      if (isFirstJobExecution(job)) {
        // then change default retries to the ones configured
        initializeRetries(job, failedJobRetryTimeCycle, durationHelper);

      } else {
        LOG.debugDecrementingRetriesForJob(job.getId());
      }

      logException(job);
      decrementRetries(job);
      notifyAcquisition(commandContext);
    }
  }

  protected ActivityImpl getCurrentActivity(CommandContext commandContext, JobEntity job) {
    String type = job.getJobHandlerType();
    ActivityImpl activity = null;

    if (SUPPORTED_TYPES.contains(type)) {
      DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
      ProcessDefinitionEntity processDefinitionEntity =
          deploymentCache.findDeployedProcessDefinitionById(job.getProcessDefinitionId());
      activity = processDefinitionEntity.findActivity(job.getActivityId());

    } else {
      // noop, because activity type is not supported
    }

    return activity;
  }

  protected ExecutionEntity fetchExecutionEntity(String executionId) {
    return Context.getCommandContext()
                  .getExecutionManager()
                  .findExecutionById(executionId);
  }

  protected String getFailedJobRetryTimeCycle(ActivityImpl activity) {
    return activity.getProperties().get(FoxFailedJobParseListener.FOX_FAILED_JOB_CONFIGURATION);
  }

  protected DurationHelper getDurationHelper(String failedJobRetryTimeCycle) throws Exception {
    return new DurationHelper(failedJobRetryTimeCycle);
  }

  protected void setLockExpirationTime(JobEntity job, String failedJobRetryTimeCycle, DurationHelper durationHelper) {
    job.setLockExpirationTime(durationHelper.getDateAfter());
  }

  protected boolean isFirstJobExecution(JobEntity job) {
    // check if this is jobs' first execution (recognize
    // this because no exception is set. Only the first
    // execution can be without exception - because if
    // no exception occurred the job would have been completed)
    // see https://app.camunda.com/jira/browse/CAM-1039
    return job.getExceptionByteArrayId() == null && job.getExceptionMessage() == null;
  }

  protected void initializeRetries(JobEntity job, String failedJobRetryTimeCycle, DurationHelper durationHelper) {
    LOG.debugInitiallyAppyingRetryCycleForJob(job.getId(),  durationHelper.getTimes());
    job.setRetries(durationHelper.getTimes());
  }

}
