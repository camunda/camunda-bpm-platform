package com.camunda.fox.platform.jobexecutor.impl.ra.execution.commonj;

import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.PlatformJobExecutor;

import com.camunda.fox.platform.jobexecutor.impl.ra.PlatformJobExecutorConnector;
import com.camunda.fox.platform.jobexecutor.impl.ra.execution.spi.PlatformJobExecutorFactory;

/**
 * Implements {@link PlatformJobExecutorFactory} to return a {@link CommonjWorkManagerPlatformJobExecutor} instance.
 * 
 * @author Christian Lipphardt
 * 
 */
public class CommonjWorkManagerPlatformJobExecutorFactory implements PlatformJobExecutorFactory {

  @Override
  public PlatformJobExecutor createPlatformJobExecutor(PlatformJobExecutorConnector platformJobExecutorConnector) {
    return new CommonjWorkManagerPlatformJobExecutor(platformJobExecutorConnector);
  }

}
