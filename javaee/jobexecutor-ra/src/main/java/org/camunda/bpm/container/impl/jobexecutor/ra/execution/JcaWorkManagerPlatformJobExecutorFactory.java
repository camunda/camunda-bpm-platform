package org.camunda.bpm.container.impl.jobexecutor.ra.execution;

import org.camunda.bpm.container.impl.jobexecutor.ra.PlatformJobExecutorConnector;
import org.camunda.bpm.container.impl.jobexecutor.ra.execution.spi.PlatformJobExecutorFactory;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.PlatformJobExecutor;


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
