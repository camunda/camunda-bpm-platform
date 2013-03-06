package org.camunda.bpm.engine.impl.jobexecutor.tobemerged.ra.outbound;

import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.JobExecutorService;

/**
 * 
 * @author Daniel Meyer
 *
 */
public interface PlatformJobExecutorConnection extends JobExecutorService {

  public void closeConnection();
  
}
