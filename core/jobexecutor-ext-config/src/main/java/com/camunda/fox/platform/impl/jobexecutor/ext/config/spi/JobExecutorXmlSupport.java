package com.camunda.fox.platform.impl.jobexecutor.ext.config.spi;

import com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService;


/**
 * @author roman.smirnov
 */
public interface JobExecutorXmlSupport {
  
  public abstract void startJobExecutor(PlatformJobExecutorService platformJobExecutorService);

  public abstract void stopJobExecutor(PlatformJobExecutorService platformJobExecutorService);

}
