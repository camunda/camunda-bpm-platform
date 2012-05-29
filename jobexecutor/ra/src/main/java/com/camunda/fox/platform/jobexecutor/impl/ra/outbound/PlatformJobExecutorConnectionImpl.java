package com.camunda.fox.platform.jobexecutor.impl.ra.outbound;

import java.util.List;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;


/**
 * 
 * @author Daniel Meyer
 * 
 */
public class PlatformJobExecutorConnectionImpl implements PlatformJobExecutorConnection {

  protected PlatformJobExecutorManagedConnection mc;
  protected PlatformJobExecutorManagedConnectionFactory mcf;
  
  public PlatformJobExecutorConnectionImpl() {
  }
  
  public PlatformJobExecutorConnectionImpl(PlatformJobExecutorManagedConnection mc, PlatformJobExecutorManagedConnectionFactory mcf) {
    this.mc = mc;
    this.mcf = mcf;
  }

  public void closeConnection() {
    mc.closeHandle(this);
  }

  public JobExecutor startJobAcquisition(JobAcquisitionConfiguration configuration) {
    return mc.startJobAcquisition(configuration);
  }

  public void stopJobAcquisition(String jobAcquisitionName) {
    mc.stopJobAcquisition(jobAcquisitionName);
  }

  public JobExecutor registerProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName) {
    return mc.registerProcessEngine(processEngineConfiguration, acquisitionName);
  }

  public void unregisterProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName) {
    mc.unregisterProcessEngine(processEngineConfiguration, acquisitionName);
  }

  public JobExecutor getJobAcquisitionByName(String name) {
    return mc.getJobAcquisitionByName(name);
  }

  public List<JobExecutor> getJobAcquisitions() {
    return mc.getJobAcquisitions();
  }

}
