package com.camunda.fox.platform.impl.jobexecutor.ext.config;

import com.camunda.fox.platform.impl.jobexecutor.ext.config.spi.JobExecutorXmlSupport;
import com.camunda.fox.platform.impl.jobexecutor.ext.util.ServiceLoaderUtil;
import com.camunda.fox.platform.impl.service.PlatformService;
import com.camunda.fox.platform.impl.service.PlatformServiceExtensionAdapter;
import com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService;
import com.camunda.fox.platform.jobexecutor.impl.DefaultPlatformJobExecutor;


public class JobExecutorXmlPlatformServiceExtension extends PlatformServiceExtensionAdapter {
  
  private JobExecutorXmlSupport jobExecutorXmlSupport;
  private PlatformJobExecutorService platformJobExecutorService;
  
  public void onPlatformServiceStart(PlatformService platformService) {
    this.jobExecutorXmlSupport = ServiceLoaderUtil.loadService(JobExecutorXmlSupport.class, JobExecutorXmlSupportImpl.class);
    this.platformJobExecutorService = ServiceLoaderUtil.loadService(PlatformJobExecutorService.class, DefaultPlatformJobExecutor.class);
    this.jobExecutorXmlSupport.startJobExecutor(this.platformJobExecutorService);
  }

  public void onPlatformServiceStop(PlatformService platformService) {
    this.platformJobExecutorService = ServiceLoaderUtil.loadService(PlatformJobExecutorService.class, DefaultPlatformJobExecutor.class);
    this.jobExecutorXmlSupport.stopJobExecutor(this.platformJobExecutorService);
  }

}
