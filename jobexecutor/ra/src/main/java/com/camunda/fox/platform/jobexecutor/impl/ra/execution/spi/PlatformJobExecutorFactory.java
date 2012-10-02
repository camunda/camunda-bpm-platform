package com.camunda.fox.platform.jobexecutor.impl.ra.execution.spi;

import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.ra.PlatformJobExecutorConnector;


public interface PlatformJobExecutorFactory {

  PlatformJobExecutor createPlatformJobExecutor(PlatformJobExecutorConnector platformJobExecutorConnector);
  
}
