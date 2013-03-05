package com.camunda.fox.engine.impl.cmd;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.calendar.DurationHelper;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.cmd.DecrementJobRetriesCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.MessageAddedNotification;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import com.camunda.fox.engine.impl.bpmn.parser.FoxFailedJobParseListener;

public class FoxJobRetryCmd implements Command<Object> {

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
      executeStandardStrategy(commandContext);
    } else {
      executeCustomStrategy(commandContext, job, activity);
    }
//  else if (type.equals(ProcessEventJobHandler.TYPE)) {
//  EventSubscriptionEntity eventSubscription = Context.getCommandContext()
//                                                     .getEventSubscriptionManager()
//                                                     .findEventSubscriptionbyId(job.getJobHandlerConfiguration());
//  activity = eventSubscription.getActivity();
//}
    
    return null;
  }

  private void executeCustomStrategy(CommandContext commandContext, JobEntity job, ActivityImpl activity) {
    String failedJobRetryTimeCycle = (String) activity.getProperty(FoxFailedJobParseListener.FOX_FAILED_JOB_CONFIGURATION);
    if (failedJobRetryTimeCycle != null) {
      try {
        DurationHelper durationHelper = new DurationHelper(failedJobRetryTimeCycle);
        job.setLockExpirationTime(durationHelper.getDateAfter());
        // TODO: why isn't the lockOwner set to null like in @DecrementJobRetriesCmd
        // TODO: why revision == 2, isn't it used for optimistic locking?
        if (job.getRevision() == 2) {
          int times = durationHelper.getTimes();
          if (times > 0) {
            job.setRetries(durationHelper.getTimes());
          } else {
            throw new ActivitiException("Cannot parse the amount of retries.");
          }
        }
        job.setRetries(job.getRetries() - 1);
      } catch (Exception e) {
        executeStandardStrategy(commandContext);
        throw new ActivitiException("Due to parsing failure the default retry strategy has been chosen.", e);
      }
      if (exception != null) {
        job.setExceptionMessage(exception.getMessage());
        job.setExceptionStacktrace(getExceptionStacktrace());
      }
      JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
      MessageAddedNotification messageAddedNotification = new MessageAddedNotification(jobExecutor);
      TransactionContext transactionContext = commandContext.getTransactionContext();
      transactionContext.addTransactionListener(TransactionState.COMMITTED, messageAddedNotification);
    } else {
      executeStandardStrategy(commandContext);
    }
  }

  private ActivityImpl getCurrentActivity(CommandContext commandContext, JobEntity job) {
    String type = job.getJobHandlerType();
    ActivityImpl activity = null;
    if (type.equals(TimerExecuteNestedActivityJobHandler.TYPE)
            || type.equals(TimerCatchIntermediateEventJobHandler.TYPE)) {
      String activityId = job.getJobHandlerConfiguration();
      ExecutionEntity execution = fetchExecutionEntity(job.getExecutionId());
      if (execution == null) {
        executeStandardStrategy(commandContext);
        throw new ActivitiException("No execution found for key '" + job.getJobHandlerConfiguration() + "'");
      }
      activity = execution.getProcessDefinition().findActivity(activityId);
      
    } else if (type.equals(TimerStartEventJobHandler.TYPE)) {
      DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();

      ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(job.getJobHandlerConfiguration());
      if (processDefinition == null) {
        executeStandardStrategy(commandContext);
        throw new ActivitiException("No process definition found for key '" + job.getJobHandlerConfiguration() + "'");
      }

      activity = processDefinition.getInitial();

    } else if (type.equals(AsyncContinuationJobHandler.TYPE)) {
      ExecutionEntity execution = this.fetchExecutionEntity(job.getExecutionId());
      if (execution == null) {
        executeStandardStrategy(commandContext);
        throw new ActivitiException("No execution found for key '" + job.getJobHandlerConfiguration() + "'");
      }
      activity = execution.getActivity();
      
    }
//  else if (type.equals(ProcessEventJobHandler.TYPE)) {
//  EventSubscriptionEntity eventSubscription = Context.getCommandContext()
//                                                     .getEventSubscriptionManager()
//                                                     .findEventSubscriptionbyId(job.getJobHandlerConfiguration());
//  activity = eventSubscription.getActivity();
//}
    
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
