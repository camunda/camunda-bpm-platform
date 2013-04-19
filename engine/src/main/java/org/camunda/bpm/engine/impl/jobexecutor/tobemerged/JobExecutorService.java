package org.camunda.bpm.engine.impl.jobexecutor.tobemerged;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.spi.JobAcquisitionConfiguration;


/**
 * <p>Service interface of the platform job executor</p>
 * 
 * <p><strong>NOTE:</strong> this class is not part of camunda BPM platform 
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