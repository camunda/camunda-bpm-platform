package org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl;

import java.util.List;

import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorContext;

/**
 * 
 * @author Daniel Meyer
 */
public class PlatformExecuteJobsRunnable implements Runnable {
  
  protected final List<String> jobIds;
  protected final CommandExecutor commandExecutor;

  public PlatformExecuteJobsRunnable(List<String> jobIds, CommandExecutor commandExecutor) {
    this.jobIds = jobIds;
    this.commandExecutor = commandExecutor;
  }

  @Override
  public void run() {
    final JobExecutorContext jobExecutorContext = new JobExecutorContext(); 
    final List<String> currentProcessorJobQueue = jobExecutorContext.getCurrentProcessorJobQueue();

    currentProcessorJobQueue.addAll(jobIds);
    
    Context.setJobExecutorContext(jobExecutorContext);
    try {
      while (!currentProcessorJobQueue.isEmpty()) {
        String nextJobId = currentProcessorJobQueue.remove(0);
        executeJob(nextJobId);
      }      
    }finally {
      Context.removeJobExecutorContext();
    }
  }

  protected void executeJob(String nextJobId) {
    commandExecutor.execute(new ExecuteJobsCmd(nextJobId));
  }

}
