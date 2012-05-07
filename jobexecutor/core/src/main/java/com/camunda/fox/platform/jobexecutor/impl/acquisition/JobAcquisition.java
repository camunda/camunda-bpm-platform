package com.camunda.fox.platform.jobexecutor.impl.acquisition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.jobexecutor.impl.PlatformJobExecutor;
import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;
import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionStrategy;

/**
 * 
 * @author Daniel Meyer
 */
public class JobAcquisition extends JobExecutor {

  protected JobAcquisitionStrategy jobAcquisitionStrategy;
  protected Map<String, ProcessEngineConfigurationImpl> processEnginesByName = new HashMap<String, ProcessEngineConfigurationImpl>();
  protected List<ProcessEngineConfigurationImpl> registeredProcessEngines = Collections.synchronizedList(new ArrayList<ProcessEngineConfigurationImpl>());
  protected PlatformJobExecutor platformJobExecutor;
  protected JobAcquisitionConfiguration jobAcquisitionConfiguration;
  protected Object scheduledAcquisition;
  
  public JobAcquisition(JobAcquisitionConfiguration jobAcquisitionConfiguration) {
    // the configuration is passed in such that a custom JobAcquisitionStrategy implementation 
    // may retreive custom configuration fields in a custom AcquireJobsRunnable. It is not 
    // used by the default implementation
    this.jobAcquisitionConfiguration = jobAcquisitionConfiguration;
  }

  protected void startExecutingJobs() {   
    scheduledAcquisition = platformJobExecutor.scheduleAcquisition(this.acquireJobsRunnable);
  }
  
  protected void stopExecutingJobs() {
    platformJobExecutor.unscheduleAcquisition(scheduledAcquisition);
  }

  protected void executeJobs(List<String> jobIds) {
    // TODO: think about changing this in activiti!
    throw new UnsupportedOperationException("executeJobs(List<String> jobIds) is unsupported, use executeJobs(List<String> jobIds, CommandExecutor commandExecutor)");
  }
  
  protected void ensureInitialization() {
    this.acquireJobsRunnable = jobAcquisitionStrategy.getAcquireJobsRunnable(this);
    this.acquireJobsCmd = new AcquireJobsCmd(this);
  }
  
  public void executeJobs(List<String> jobIds, CommandExecutor commandExecutor) {
    platformJobExecutor.executeJobs(jobIds, commandExecutor);
  }

  public void registerProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration) {
    String processEngineName = processEngineConfiguration.getProcessEngineName();
    if(processEnginesByName.containsKey(processEngineName)) {
      throw new FoxPlatformException("cannot register process engine with acquisition: process engine with name '"
              + processEngineName + "' already registerd.");
    }
    processEnginesByName.put(processEngineName, processEngineConfiguration);
    registeredProcessEngines.add(processEngineConfiguration);
  }

  public void unregisterProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration) {
    String processEngineName = processEngineConfiguration.getProcessEngineName();
    if(!processEnginesByName.containsKey(processEngineName)) {
      throw new FoxPlatformException("cannot unregister process engine with acquisition: process engine with name '"
              + processEngineName + "' not registerd.");
    }
    processEnginesByName.remove(processEngineName);
    registeredProcessEngines.remove(processEngineConfiguration);
  }

  public List<ProcessEngineConfigurationImpl> getRegisteredProcessEngines() {
    return registeredProcessEngines;
  }

  public void setPlatformJobExecutor(PlatformJobExecutor platformJobExecutor) {
    this.platformJobExecutor = platformJobExecutor;
  }
  
  public JobAcquisitionStrategy getJobAcquisitionStrategy() {
    return jobAcquisitionStrategy;
  }
  
  public void setJobAcquisitionStrategy(JobAcquisitionStrategy jobAcquisitionStrategy) {
    this.jobAcquisitionStrategy = jobAcquisitionStrategy;
  }
  
  public JobAcquisitionConfiguration getJobAcquisitionConfiguration() {
    return jobAcquisitionConfiguration;
  }

}
