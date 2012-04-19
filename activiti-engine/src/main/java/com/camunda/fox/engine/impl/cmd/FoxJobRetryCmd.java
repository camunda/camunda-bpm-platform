package com.camunda.fox.engine.impl.cmd;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

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
  
  private ExecutionEntity fetchExecutionEntity(String executionId) {
    return Context.getCommandContext()
                  .getExecutionManager()
                  .findExecutionById(executionId);
  }

  public Object execute(CommandContext commandContext) {
    JobEntity job = Context.getCommandContext()
                           .getJobManager()
                           .findJobById(jobId);

    String type = job.getJobHandlerType();
    ActivityImpl activity = null;
    if (type.equals(TimerExecuteNestedActivityJobHandler.TYPE)
            || type.equals(TimerCatchIntermediateEventJobHandler.TYPE)) {
      String activityId = job.getJobHandlerConfiguration();
      ExecutionEntity execution = this.fetchExecutionEntity(job.getExecutionId());
      if (execution == null) {
        this.executeStandardStrategy(commandContext);
        throw new ActivitiException("No execution found for key '" + job.getJobHandlerConfiguration() + "'");
      }
      activity = execution.getProcessDefinition().findActivity(activityId);
      
    } else if (type.equals(TimerStartEventJobHandler.TYPE)) {
      DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();

      ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedLatestProcessDefinitionByKey(job.getJobHandlerConfiguration());
      if (processDefinition == null) {
        this.executeStandardStrategy(commandContext);
        throw new ActivitiException("No process definition found for key '" + job.getJobHandlerConfiguration() + "'");
      }

      activity = processDefinition.getInitial();

    } else if (type.equals(AsyncContinuationJobHandler.TYPE)) {
      ExecutionEntity execution = this.fetchExecutionEntity(job.getExecutionId());
      if (execution == null) {
        this.executeStandardStrategy(commandContext);
        throw new ActivitiException("No execution found for key '" + job.getJobHandlerConfiguration() + "'");
      }
      activity = execution.getActivity();
      
    } 
//    else if (type.equals(ProcessEventJobHandler.TYPE)) {
//      EventSubscriptionEntity eventSubscription = Context.getCommandContext()
//                                                         .getEventSubscriptionManager()
//                                                         .findEventSubscriptionbyId(job.getJobHandlerConfiguration());
//      activity = eventSubscription.getActivity();
//    }

    if (activity == null) {
      this.executeStandardStrategy(commandContext);
      return null;
    }
    
    String failedJobRetryTimeCycle = (String) activity.getProperty(FoxFailedJobParseListener.FOX_FAILED_JOB_CONFIGURATION);
    if (failedJobRetryTimeCycle != null) {
      try {
        DurationHelper durationHelper = new DurationHelper(failedJobRetryTimeCycle);
        job.setLockExpirationTime(durationHelper.getDateAfter());
        List<String> expression = Arrays.asList(failedJobRetryTimeCycle.split("/"));
        boolean secondRevision = job.getRevision() == 2;
        if (!secondRevision && !(expression.size() == 1 && expression.get(0).startsWith("D"))) {
          job.setRetries(job.getRetries() - 1);
        } else if (secondRevision) {
          if (expression.size() == 2 && expression.get(0).startsWith("R")) {
            job.setRetries((expression.get(0).length() ==  1 ? Integer.MAX_VALUE : Integer.parseInt(expression.get(0).substring(1))) - 1);
          } else if (expression.get(0).startsWith("D")) {
            job.setRetries(1);
          }
        }
      } catch (Exception e) {
        this.executeStandardStrategy(commandContext);
        throw new ActivitiException("Due to parsing failure the default retry strategy has been chosen.", e);
      }
      if(exception != null) {
        job.setExceptionMessage(exception.getMessage());
        job.setExceptionStacktrace(getExceptionStacktrace());
      }
      JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
      MessageAddedNotification messageAddedNotification = new MessageAddedNotification(jobExecutor);
      TransactionContext transactionContext = commandContext.getTransactionContext();
      transactionContext.addTransactionListener(TransactionState.COMMITTED, messageAddedNotification);
    } else {
      this.executeStandardStrategy(commandContext);
    }
    return null;
  }
  
  private String getExceptionStacktrace() {
    StringWriter stringWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }
  
  private void executeStandardStrategy(CommandContext commandContext) {
    DecrementJobRetriesCmd decrementCmd = new DecrementJobRetriesCmd(jobId, exception);
    decrementCmd.execute(commandContext);
  }

}
