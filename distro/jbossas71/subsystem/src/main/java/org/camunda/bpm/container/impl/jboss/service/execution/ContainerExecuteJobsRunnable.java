package org.camunda.bpm.container.impl.jboss.service.execution;

import java.util.List;

import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.PlatformExecuteJobsRunnable;


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
