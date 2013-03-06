package org.camunda.bpm.container.impl.jobexecutor.ra.execution.commonj;

import org.camunda.bpm.container.impl.jobexecutor.ra.PlatformJobExecutorConnector;
import org.camunda.bpm.container.impl.jobexecutor.ra.execution.spi.PlatformJobExecutorFactory;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.PlatformJobExecutor;


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
