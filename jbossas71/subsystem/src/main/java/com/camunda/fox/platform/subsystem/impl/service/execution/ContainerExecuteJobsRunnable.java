package com.camunda.fox.platform.subsystem.impl.service.execution;

import java.util.List;

import org.activiti.engine.impl.interceptor.CommandExecutor;

import com.camunda.fox.platform.jobexecutor.impl.PlatformExecuteJobsRunnable;
import com.camunda.fox.platform.subsystem.impl.service.ContainerPlatformService;

/**
 * Custom Runnable delagting to the {@link ContainerExecuteJobCmd}
 * 
 * @author Daniel Meyer
 * 
 */
public class ContainerExecuteJobsRunnable extends PlatformExecuteJobsRunnable {
  
  protected final ContainerPlatformService platformService;

  public ContainerExecuteJobsRunnable(List<String> jobIds, CommandExecutor commandExecutor, ContainerPlatformService platformService) {
    super(jobIds, commandExecutor);
    this.platformService = platformService;
  }

  @Override
  protected void executeJob(final String nextJobId) {
    commandExecutor.execute(new ContainerExecuteJobCmd(nextJobId, platformService));      
  }

}
