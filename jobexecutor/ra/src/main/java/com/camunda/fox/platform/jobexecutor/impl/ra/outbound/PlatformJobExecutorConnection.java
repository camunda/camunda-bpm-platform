package com.camunda.fox.platform.jobexecutor.impl.ra.outbound;

import com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService;

/**
 * 
 * @author Daniel Meyer
 *
 */
public interface PlatformJobExecutorConnection extends PlatformJobExecutorService {

  public void closeConnection();
  
}
