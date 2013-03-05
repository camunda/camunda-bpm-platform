package com.camunda.fox.platform.subsystem.impl.service.execution;

import java.util.List;

import org.activiti.engine.impl.cmd.ExecuteJobsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;

import com.camunda.fox.platform.jobexecutor.impl.PlatformExecuteJobsRunnable;

/**
 * Custom Runnable delegating to the {@link ContainerExecuteJobCmd}
 * 
 * @author Daniel Meyer
 * 
 */
public class ContainerExecuteJobsRunnable extends PlatformExecuteJobsRunnable {
  
  public ContainerExecuteJobsRunnable(List<String> jobIds, CommandExecutor commandExecutor) {
    super(jobIds, commandExecutor);
  }

  @Override
  protected void executeJob(final String nextJobId) {
    commandExecutor.execute(new ExecuteJobsCmd(nextJobId));      
  }

}
