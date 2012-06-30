package com.camunda.fox.platform.subsystem.impl.service.ext;

import java.util.Map;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.impl.service.PlatformServiceExtensionAdapter;
import com.camunda.fox.platform.impl.service.ProcessEngineController;
import com.camunda.fox.platform.impl.service.spi.PlatformServiceExtension;
import com.camunda.fox.platform.impl.util.PropertyHelper;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;
import com.camunda.fox.platform.subsystem.impl.service.ContainerJobExecutorService;
import com.camunda.fox.platform.subsystem.impl.service.ContainerProcessEngineController;

/**
 * {@link PlatformServiceExtension} registering process engines with the {@link ContainerJobExecutorService}
 * 
 *  @author Daniel Meyer
 */
public class ContainerPlatformJobExecutorExtension extends PlatformServiceExtensionAdapter {
  
  @Override
  public int getPrecedence() {
    return PLATFORM_JOB_EXECUTOR_EXTENSION_PRECEDENCE;
  }

  @Override
  public void afterProcessEngineControllerStart(ProcessEngineController processEngineController) {
    Map<String, Object> configProperties = processEngineController.getProcessEngineUserConfiguration().getProperties();    
    String jobAcquisitionName = PropertyHelper.getProperty(configProperties, ProcessEngineConfiguration.PROP_JOB_EXECUTOR_ACQUISITION_NAME, null);
    if(jobAcquisitionName != null) {
      
      ContainerProcessEngineController processEngineControllerService = (ContainerProcessEngineController) processEngineController;
      ContainerJobExecutorService containerJobExecutorService = processEngineControllerService.getContainerJobExecutorInjector().getValue();
      
      // register the process engine
      ProcessEngineConfigurationImpl processEngineConfiguration = processEngineController.getProcessEngineConfiguration();
      JobExecutor jobExecutorDelegate = containerJobExecutorService.registerProcessEngine(processEngineConfiguration, jobAcquisitionName);
      processEngineConfiguration.setJobExecutor(jobExecutorDelegate);
      
    }
  }
  
  @Override
  public void beforeProcessEngineControllerStop(ProcessEngineController processEngineController) {
    Map<String, Object> configProperties = processEngineController.getProcessEngineUserConfiguration().getProperties();    
    String jobAcquisitionName = PropertyHelper.getProperty(configProperties, ProcessEngineConfiguration.PROP_JOB_EXECUTOR_ACQUISITION_NAME, null);
    if(jobAcquisitionName != null) {
      
      ContainerProcessEngineController processEngineControllerService = (ContainerProcessEngineController) processEngineController;
      ContainerJobExecutorService containerJobExecutorService = processEngineControllerService.getContainerJobExecutorInjector().getValue();
      
      // unregister the process engine
      ProcessEngineConfigurationImpl processEngineConfiguration = processEngineController.getProcessEngineConfiguration();
      containerJobExecutorService.unregisterProcessEngine(processEngineConfiguration, jobAcquisitionName);
      processEngineConfiguration.setJobExecutor(null);
    }
  }
  

}
