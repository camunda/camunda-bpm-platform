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

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.DefaultFailedJobParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FailedJobRetryConfiguration;
import org.camunda.bpm.engine.impl.calendar.DurationHelper;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorLogger;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.ParseUtil;

/**
 * @author Roman Smirnov
 */
public class DefaultJobRetryCmd extends JobRetryCmd {

  public static final List<String> SUPPORTED_TYPES = Arrays.asList(
      TimerExecuteNestedActivityJobHandler.TYPE,
      TimerCatchIntermediateEventJobHandler.TYPE,
      TimerStartEventJobHandler.TYPE,
      TimerStartEventSubprocessJobHandler.TYPE,
      AsyncContinuationJobHandler.TYPE
  );
  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  public DefaultJobRetryCmd(String jobId, Throwable exception) {
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
    JobEntity job = getJob();
    if (job != null) {
      job.unlock();
      logException(job);
      decrementRetries(job);
      notifyAcquisition(commandContext);
    } else {
      LOG.debugFailedJobNotFound(jobId);
    }
  }

  protected void executeCustomStrategy(CommandContext commandContext, JobEntity job, ActivityImpl activity) throws Exception {
    FailedJobRetryConfiguration retryConfiguration = getFailedJobRetryConfiguration(job, activity);

    if (retryConfiguration == null) {
      executeStandardStrategy(commandContext);

    } else {

      if (isFirstJobExecution(job)) {
        // then change default retries to the ones configured
        initializeRetries(job, retryConfiguration.getRetries());

      } else {
        LOG.debugDecrementingRetriesForJob(job.getId());
      }

      List<String> intervals = retryConfiguration.getRetryIntervals();
      int intervalsCount = intervals.size();
      int indexOfInterval = Math.max(0, Math.min(intervalsCount - 1, intervalsCount - (job.getRetries() - 1)));
      DurationHelper durationHelper = getDurationHelper(intervals.get(indexOfInterval));
      job.setLockExpirationTime(durationHelper.getDateAfter());

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

  protected FailedJobRetryConfiguration getFailedJobRetryConfiguration(JobEntity job, ActivityImpl activity) {
    FailedJobRetryConfiguration retryConfiguration = activity.getProperties().get(DefaultFailedJobParseListener.FAILED_JOB_CONFIGURATION);

    while (retryConfiguration != null && retryConfiguration.getExpression() != null) {
      String retryIntervals = getFailedJobRetryTimeCycle(job, retryConfiguration.getExpression());
      retryConfiguration = ParseUtil.parseRetryIntervals(retryIntervals);
    }

    return retryConfiguration;
  }

  protected String getFailedJobRetryTimeCycle(JobEntity job, Expression expression) {

    String executionId = job.getExecutionId();
    ExecutionEntity execution = null;

    if (executionId != null) {
      execution = fetchExecutionEntity(executionId);
    }

    Object value = null;

    if (expression == null) {
      return null;
    }

    try {
       value = expression.getValue(execution, execution);
    }
    catch (Exception e) {
      LOG.exceptionWhileParsingExpression(jobId, e.getCause().getMessage());
    }

    if (value instanceof String) {
      return (String) value;
    }
    else
    {
      // default behavior
      return null;
    }

  }

  protected DurationHelper getDurationHelper(String failedJobRetryTimeCycle) throws Exception {
    return new DurationHelper(failedJobRetryTimeCycle);
  }

  protected boolean isFirstJobExecution(JobEntity job) {
    // check if this is jobs' first execution (recognize
    // this because no exception is set. Only the first
    // execution can be without exception - because if
    // no exception occurred the job would have been completed)
    // see https://app.camunda.com/jira/browse/CAM-1039
    return job.getExceptionByteArrayId() == null && job.getExceptionMessage() == null;
  }

  protected void initializeRetries(JobEntity job, int retries) {
    LOG.debugInitiallyAppyingRetryCycleForJob(job.getId(), retries);
    job.setRetries(retries);
  }

}
