package com.camunda.fox.platform.jobexecutor.impl.ra.execution.spi;

import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.PlatformJobExecutor;

import com.camunda.fox.platform.jobexecutor.impl.ra.PlatformJobExecutorConnector;

/**
 * Allows to configure a {@link PlatformJobExecutor} instance.
 * 
 * @author Christian Lipphardt
 * 
 */
public interface PlatformJobExecutorFactory {

  PlatformJobExecutor createPlatformJobExecutor(PlatformJobExecutorConnector platformJobExecutorConnector);
  
}
