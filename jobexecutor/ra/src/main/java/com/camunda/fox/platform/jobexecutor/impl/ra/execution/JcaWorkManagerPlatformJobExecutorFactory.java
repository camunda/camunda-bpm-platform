package com.camunda.fox.platform.jobexecutor.impl.ra.execution;

import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.ra.PlatformJobExecutorConnector;
import com.camunda.fox.platform.jobexecutor.impl.ra.execution.spi.PlatformJobExecutorFactory;


public class JcaWorkManagerPlatformJobExecutorFactory implements PlatformJobExecutorFactory {

  @Override
  public PlatformJobExecutor createPlatformJobExecutor(PlatformJobExecutorConnector platformJobExecutorConnector) {
    return new JcaWorkManagerPlatformJobExecutor(platformJobExecutorConnector.getWorkManager(), platformJobExecutorConnector);
  }

}
