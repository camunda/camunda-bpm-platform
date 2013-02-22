package com.camunda.fox.platform.jobexecutor;

import java.util.List;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;

/**
 * <p>Service interface of the platform job executor</p>
 * 
 * <p><strong>NOTE:</strong> this class is not part of fox platform 
 * public api and as such subject to incompatible change.</p>
 * 
 * @author Daniel Meyer
 *
 */
public interface JobExecutorService {

  public JobExecutor getJobAcquisitionByName(String name);

  public List<JobExecutor> getJobAcquisitions();
  
  public JobExecutor startJobAcquisition(JobAcquisitionConfiguration configuration);

  public void stopJobAcquisition(String jobAcquisitionName);

  public JobExecutor registerProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName);

  public void unregisterProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName);

}