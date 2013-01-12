package com.camunda.fox.platform.jobexecutor.impl.ra.execution;

import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.impl.ra.PlatformJobExecutorConnector;
import com.camunda.fox.platform.jobexecutor.impl.ra.execution.spi.PlatformJobExecutorFactory;

/**
 * Implements {@link PlatformJobExecutorFactory} to return a {@link JcaWorkManagerPlatformJobExecutor} instance.
 * 
 * @author Christian Lipphardt
 * 
 */
public class JcaWorkManagerPlatformJobExecutorFactory implements PlatformJobExecutorFactory {

  @Override
  public PlatformJobExecutor createPlatformJobExecutor(PlatformJobExecutorConnector platformJobExecutorConnector) {
    return new JcaWorkManagerPlatformJobExecutor(platformJobExecutorConnector.getWorkManager(), platformJobExecutorConnector);
  }

}
