package org.camunda.bpm.container.impl.threading.ra.outbound;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;

/**
 * 
 * @author Daniel Meyer
 *
 */
public interface JcaExecutorServiceConnection {
    
  public boolean schedule(Runnable runnable, boolean isLongRunning);

  public Runnable getExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine);
  
  public void closeConnection();
}
