package org.camunda.bpm.container.impl.jobexecutor.ra.execution.spi;

import org.camunda.bpm.container.impl.jobexecutor.ra.PlatformJobExecutorConnector;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.PlatformJobExecutor;


/**
 * Allows to configure a {@link PlatformJobExecutor} instance.
 * 
 * @author Christian Lipphardt
 * 
 */
public interface PlatformJobExecutorFactory {

  PlatformJobExecutor createPlatformJobExecutor(PlatformJobExecutorConnector platformJobExecutorConnector);
  
}
