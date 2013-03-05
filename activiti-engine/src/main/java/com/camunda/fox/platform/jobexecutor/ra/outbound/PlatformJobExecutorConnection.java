package com.camunda.fox.platform.jobexecutor.ra.outbound;

import com.camunda.fox.platform.jobexecutor.JobExecutorService;

/**
 * 
 * @author Daniel Meyer
 *
 */
public interface PlatformJobExecutorConnection extends JobExecutorService {

  public void closeConnection();
  
}
