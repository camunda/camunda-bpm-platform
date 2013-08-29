package org.camunda.bpm.engine.impl.cmd;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.bpmn.parser.FoxFailedJobParseListener;
import org.camunda.bpm.engine.impl.calendar.DurationHelper;
import org.camunda.bpm.engine.impl.cfg.TransactionContext;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.MessageAddedNotification;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;


public class FoxJobRetryCmd implements Command<Object> {

  private static final Logger log = Logger.getLogger(FoxJobRetryCmd.class.getName());

  protected String jobId;
  protected Throwable exception;

  public FoxJobRetryCmd(String jobId, Throwable exception) {
    this.jobId = jobId;
    this.exception = exception;
  }

  public Object execute(CommandContext commandContext) {
    JobEntity job = Context.getCommandContext()
                           .getJobManager()
                           .findJobById(jobId);

    ActivityImpl activity = getCurrentActivity(commandContext, job);

    if (activity == null) {
      log.log(Level.SEVERE, "Failure while executing " + FoxJobRetryCmd.class.getName() + " for job id '" + jobId + "'. Falling back to standard job retry strategy.");
      executeStandardStrategy(commandContext);
    } else {
      try {
        executeCustomStrategy(commandContext, job, activity);
      } catch (Exception e) {
        log.log(Level.SEVERE, "Failure while executing " + FoxJobRetryCmd.class.getName() + " for job id '" + jobId + "'. Falling back to standard job retry strategy.", e);
        executeStandardStrategy(commandContext);
      }
    }

    return null;
  }

  private void executeCustomStrategy(CommandContext commandContext, JobEntity job, ActivityImpl activity) throws Exception {
    String failedJobRetryTimeCycle = (String) activity.getProperty(FoxFailedJobParseListener.FOX_FAILED_JOB_CONFIGURATION);

    if(failedJobRetryTimeCycle == null) {
      executeStandardStrategy(commandContext);

    } else {
      DurationHelper durationHelper = new DurationHelper(failedJobRetryTimeCycle);
      job.setLockExpirationTime(durationHelper.getDateAfter());

      // check if this is jobs' first execution (recognize this because no exception is set. Only the first execution can be without exception - because if no exception occurred the job would have been completed)
      // see https://app.camunda.com/jira/browse/CAM-1039
      if (job.getExceptionByteArrayId() == null && job.getExceptionMessage()==null) {
          log.fine("Applying JobRetryStrategy '" + failedJobRetryTimeCycle+ "' the first time for job " + job.getId() + " with "+durationHelper.getTimes()+" retries");
        // then change default retries to the ones configured
        job.setRetries(durationHelper.getTimes());
      }
      else {
      	log.fine("Decrementing retries of JobRetryStrategy '" + failedJobRetryTimeCycle+ "' for job " + job.getId());
      }

      if (exception != null) {
        job.setExceptionMessage(exception.getMessage());
        job.setExceptionStacktrace(getExceptionStacktrace());
      }

      job.setRetries(job.getRetries() - 1);

      JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
      MessageAddedNotification messageAddedNotification = new MessageAddedNotification(jobExecutor);
      TransactionContext transactionContext = commandContext.getTransactionContext();
      transactionContext.addTransactionListener(TransactionState.COMMITTED, messageAddedNotification);
    }
  }

  private ActivityImpl getCurrentActivity(CommandContext commandContext, JobEntity job) {
    String type = job.getJobHandlerType();
    ActivityImpl activity = null;

    if (TimerExecuteNestedActivityJobHandler.TYPE.equals(type) ||
        TimerCatchIntermediateEventJobHandler.TYPE.equals(type)) {
      ExecutionEntity execution = fetchExecutionEntity(job.getExecutionId());
      if (execution != null) {
        activity = execution.getProcessDefinition().findActivity(job.getJobHandlerConfiguration());
      }
    } else if (TimerStartEventJobHandler.TYPE.equals(type)) {
      DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
      ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(job.getJobHandlerConfiguration());
      if (processDefinition != null) {
        activity = processDefinition.getInitial();
      }
    } else if (AsyncContinuationJobHandler.TYPE.equals(type)) {
      ExecutionEntity execution = fetchExecutionEntity(job.getExecutionId());
      if (execution != null) {
        activity = execution.getActivity();
      }
    } else {
      // nop, because activity type is not supported
    }

    return activity;
  }

  private String getExceptionStacktrace() {
    StringWriter stringWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  private ExecutionEntity fetchExecutionEntity(String executionId) {
    return Context.getCommandContext()
                  .getExecutionManager()
                  .findExecutionById(executionId);
  }

  private void executeStandardStrategy(CommandContext commandContext) {
    DecrementJobRetriesCmd decrementCmd = new DecrementJobRetriesCmd(jobId, exception);
    decrementCmd.execute(commandContext);
  }

}
