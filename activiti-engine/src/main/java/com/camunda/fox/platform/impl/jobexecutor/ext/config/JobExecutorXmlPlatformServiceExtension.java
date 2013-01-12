package com.camunda.fox.platform.impl.jobexecutor.ext.config;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.camunda.fox.platform.impl.jobexecutor.ext.config.spi.JobExecutorXmlSupport;
import com.camunda.fox.platform.impl.jobexecutor.ext.util.ServiceLoaderUtil;
import com.camunda.fox.platform.impl.service.PlatformService;
import com.camunda.fox.platform.impl.service.PlatformServiceExtensionAdapter;
import com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService;


public class JobExecutorXmlPlatformServiceExtension extends PlatformServiceExtensionAdapter {
  
  private JobExecutorXmlSupport jobExecutorXmlSupport;
  private PlatformJobExecutorService platformJobExecutorService;
  private Logger log = Logger.getLogger(JobExecutorXmlPlatformServiceExtension.class.getName());
  
  public final static String PLATFORM_JOB_EXECUTOR_SERVICE_NAME = 
          "java:global/" +
          "camunda-fox-platform/" +
          "job-executor/" +
          "PlatformJobExecutorBean!com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService";
  
  public int getPrecedence() {
    return 100;
  }
  
  public void onPlatformServiceStart(PlatformService platformService) {
    PlatformJobExecutorService platformJobExecutorService = this.getPlatformJobExecutorService();
    if (platformJobExecutorService != null) {
      this.jobExecutorXmlSupport = ServiceLoaderUtil.loadService(JobExecutorXmlSupport.class, JobExecutorXmlSupportImpl.class);
      this.jobExecutorXmlSupport.startJobExecutor(this.platformJobExecutorService);
    }
  }

  public void onPlatformServiceStop(PlatformService platformService) {
    PlatformJobExecutorService platformJobExecutorService = this.getPlatformJobExecutorService();
    if (platformJobExecutorService != null) {
      this.jobExecutorXmlSupport.stopJobExecutor(this.platformJobExecutorService);
    }
  }

  protected PlatformJobExecutorService getPlatformJobExecutorService() {
    if(platformJobExecutorService == null) {
      try {
        platformJobExecutorService = InitialContext.doLookup(PLATFORM_JOB_EXECUTOR_SERVICE_NAME);
      } catch (NamingException e) {
        log.log(Level.FINE, "NamingException while looking up PlaftormJobExecutorService", e);
      }
    }
    return platformJobExecutorService;
  }
  
}
