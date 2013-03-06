package org.camunda.bpm.container.impl.jobexecutor.ra.outbound;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.ra.outbound.PlatformJobExecutorConnection;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.spi.JobAcquisitionConfiguration;



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
