package com.camunda.fox.platform.impl.ext;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.impl.service.PlatformServiceExtensionAdapter;
import com.camunda.fox.platform.impl.service.ProcessEngineController;
import com.camunda.fox.platform.impl.util.PropertyHelper;
import com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>Loadable extension registering process engines with the platform job executor.</p>
 * 
 * <p>Only registers process engines for which the 
 * {@link ProcessEngineConfiguration#PROP_JOB_EXECUTOR_ACQUISITION_NAME} configuration
 * property is defined.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class PlatformJobExecutorExtension extends PlatformServiceExtensionAdapter {
  
  private Logger log = Logger.getLogger(PlatformJobExecutorExtension.class.getName());

  protected PlatformJobExecutorService platformJobExecutorService;
  
  public final static String PLATFORM_JOB_EXECUTOR_SERVICE_NAME = 
    "java:global/" +
    "camunda-fox-platform/" +
    "job-executor/" +
    "PlatformJobExecutorBean!com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService";
  
  @Override
  public void afterProcessEngineControllerStart(ProcessEngineController processEngineController) {
    PlatformJobExecutorService platformJobExecutorService = getPlatformJobExecutorService();
    if (platformJobExecutorService != null) {
      ProcessEngineConfigurationImpl processEngineConfiguration = processEngineController.getProcessEngineConfiguration();
      ProcessEngineConfiguration processEngineUserConfiguration = processEngineController.getProcessEngineUserConfiguration();
      String jobAcquisitionName = getJobAcquisitionName(processEngineUserConfiguration);
      if (jobAcquisitionName != null) {
        JobExecutor platformJobExecutorDelegate = platformJobExecutorService.registerProcessEngine(processEngineConfiguration, jobAcquisitionName);
        processEngineConfiguration.setJobExecutor(platformJobExecutorDelegate);
      }
    } else {
      log.log(Level.WARNING, "PlatformJobExecutorExtension active but PlatformJobExecutorService not found!");
    }
  }
  
  @Override
  public void beforeProcessEngineControllerStop(ProcessEngineController processEngineController) {
    PlatformJobExecutorService platformJobExecutorService = getPlatformJobExecutorService();
    if (platformJobExecutorService != null) {
      ProcessEngineConfigurationImpl processEngineConfiguration = processEngineController.getProcessEngineConfiguration();
      ProcessEngineConfiguration processEngineUserConfiguration = processEngineController.getProcessEngineUserConfiguration();
      String jobAcquisitionName = getJobAcquisitionName(processEngineUserConfiguration);
      if (jobAcquisitionName != null) {
        platformJobExecutorService.unregisterProcessEngine(processEngineConfiguration, jobAcquisitionName);
        processEngineConfiguration.setJobExecutor(null);
      }
    }
  }

  protected String getJobAcquisitionName(ProcessEngineConfiguration processEngineUserConfiguration) {
    return PropertyHelper.getProperty(
      processEngineUserConfiguration.getProperties(), 
      ProcessEngineConfiguration.PROP_JOB_EXECUTOR_ACQUISITION_NAME, 
      null);
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
