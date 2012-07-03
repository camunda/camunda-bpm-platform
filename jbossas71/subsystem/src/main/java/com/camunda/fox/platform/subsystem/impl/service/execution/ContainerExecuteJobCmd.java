package com.camunda.fox.platform.subsystem.impl.service.execution;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.cmd.ExecuteJobsCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;

import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.subsystem.impl.service.ContainerPlatformService;

/**
 * <p>{@link Command} used for executing Jobs on Jboss AS.</p> 
 * 
 * <p>This command attempts to locate the process archive for the current job.
 * <ul> 
 *   <li>If the process archive can be found, we make a single context swich, before executing the job.</li>
 *   <li>If the process archive cannot be found, we attempt executing the job anyway.</li>
 * </ul>
 * </p>
 * 
 * <p>Actual exeution of the job is handled by the default {@link ExecuteJobsCmd}. We combine both commands 
 * ({@link ContainerExecuteJobCmd} and {@link ExecuteJobsCmd}) into a single command context here.
 * The reason is that in order to resolve the process archive for the job, we have to query the database 
 * for the job Entity and referenced entities like the execution. Since the {@link ExecuteJobsCmd} needs to do that 
 * as well, we do not degrade performance as the enities are cached by the shared {@link DbSqlSession}.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ContainerExecuteJobCmd implements Command<Void> {
  
  public final static Logger log = Logger.getLogger(ContainerExecuteJobCmd.class.getName());
  
  protected final ContainerPlatformService platformService;
  protected final String jobId;
  protected final ExecuteJobsCmd executeJobsCmd;

  public ContainerExecuteJobCmd(String jobId, ContainerPlatformService platformService) {
    this.jobId = jobId;
    this.platformService = platformService;
    this.executeJobsCmd = new ExecuteJobsCmd(jobId);
  }

  public Void execute(CommandContext commandContext) {
    ProcessArchive processArchive = null;
    try {
      processArchive = getProcessArchive(commandContext);
    }catch (Exception e) {
      // ignore silently
      log.log(Level.FINE, "exception while attempting to resolve the process archive for job "+jobId, e);      
    }
    
    if(processArchive != null)  {
      
      ProcessArchiveContext processArchiveContext = platformService.getProcessArchiveContext(processArchive.getName(), getProcessEngineName());
      try {
        ProcessArchiveContext.executeWithinContext(new JobRequestContextInterceptor(commandContext, executeJobsCmd), processArchiveContext);
      } catch (Exception e) {
        log.log(Level.WARNING, "exception while executing job in process archive context"+jobId, e);
      }
      
    } else {
      
      // TODO: or do we always require to find a process archive?
      executeJobsCmd.execute(commandContext); 
    }
    
    return null;
  }

  protected ProcessArchive getProcessArchive(CommandContext commandContext) {

    final JobEntity job = commandContext.getJobManager().findJobById(jobId);
    if (job == null) {
      return null;
    }

    final String jobHandlerType = job.getJobHandlerType();
    final String processEngineName = getProcessEngineName();
    
    ProcessArchive processArchive = null;    

    if (AsyncContinuationJobHandler.TYPE.equals(jobHandlerType) 
            || TimerCatchIntermediateEventJobHandler.TYPE.equals(jobHandlerType)
            || TimerExecuteNestedActivityJobHandler.TYPE.equals(jobHandlerType)) {

      processArchive = getProcessArchiveForProcessInstance(job.getProcessInstanceId(), commandContext, processEngineName);

    } else if (TimerStartEventJobHandler.TYPE.equals(jobHandlerType)) {
      String processDefinitionKey = job.getJobHandlerConfiguration();

      processArchive = platformService.getProcessArchiveByProcessDefinitionKey(processDefinitionKey, processEngineName);

    } else if (ProcessEventJobHandler.TYPE.equals(jobHandlerType)) {
      EventSubscriptionEntity eventSubscriptionEntity = commandContext
        .getEventSubscriptionManager()
        .findEventSubscriptionbyId(job.getJobHandlerConfiguration());      
      String processInstanceId = eventSubscriptionEntity.getProcessInstanceId();
      if(processInstanceId != null) {
        processArchive = getProcessArchiveForProcessInstance(processInstanceId, commandContext, processEngineName);
      }else {
        // TODO!
      }
    }
    
    return processArchive;
  }

  protected String getProcessEngineName() {
    String processEngineName = Context.getProcessEngineConfiguration().getProcessEngineName();
    return processEngineName;
  }
  
  protected ProcessArchive getProcessArchiveForProcessInstance(String processInstanceId, CommandContext commandContext, String processEngineName) {   
    
    final ExecutionEntity processInstance = commandContext
      .getExecutionManager()
      .findExecutionById(processInstanceId);
    
    if(processInstance != null) {
      return  platformService.getProcessArchiveByProcessDefinitionId(processInstance.getProcessDefinitionId(), processEngineName);
      
    } else {
      return null;
      
    }
  }

}
