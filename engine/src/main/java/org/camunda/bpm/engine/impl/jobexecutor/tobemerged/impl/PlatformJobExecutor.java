package org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.JobExecutorService;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.acquisition.JobAcquisition;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.acquisition.PlatformJobExecutorDelegate;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.acquisition.SequentialJobAcquisition;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.spi.JobAcquisitionConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.spi.JobAcquisitionStrategy;

/**
 * <p>Abstract implementation of the {@link JobExecutorService} API</p>
 * 
 * <p>Actual thread management is delegated to subclasses</p>
 * 
 * @author Daniel Meyer
 */
public abstract class PlatformJobExecutor implements JobExecutorService {

  protected Map<String, JobAcquisition> jobAcquisitionsByName = new HashMap<String, JobAcquisition>();
  
  public Map<String, JobAcquisitionStrategy> discoveredStrategies;

  public synchronized void start() {
    if(discoveredStrategies == null) {
      loadJobAcquisitionStrategies();
    }
  }
  
  protected void loadJobAcquisitionStrategies() {
    discoveredStrategies = new HashMap<String, JobAcquisitionStrategy>();
    
    // load user-provided strategies
    ServiceLoader<JobAcquisitionStrategy> loader = ServiceLoader.load(JobAcquisitionStrategy.class);
    Iterator<JobAcquisitionStrategy> iterator = loader.iterator();
    while (iterator.hasNext()) {
      JobAcquisitionStrategy jobAcquisitionStrategy = (JobAcquisitionStrategy) iterator.next();
      String strategyName = jobAcquisitionStrategy.getJobAcquisitionName();
      if (discoveredStrategies.containsKey(strategyName)) {
        throw new ProcessEngineException("More than one JobAcquisitionStrategy with name '" + strategyName + "' registered.");
      } else {
        discoveredStrategies.put(strategyName, jobAcquisitionStrategy);
      }
    }
    
    // register default strategies if not overridden by user:
    if(!discoveredStrategies.containsKey(JobAcquisitionStrategy.SEQUENTIAL)) {
      discoveredStrategies.put(JobAcquisitionStrategy.SEQUENTIAL, new SequentialJobAcquisition());
    }
  }

  public synchronized void stop() {
    ArrayList<String> names = new ArrayList<String>(jobAcquisitionsByName.keySet());
    for (String name : names) {
      stopJobAcquisition(name);      
    }    
    discoveredStrategies = null;
  }
  
  public synchronized JobAcquisition startJobAcquisition(JobAcquisitionConfiguration configuration) {
    String acquisitionName = configuration.getAcquisitionName();
    JobAcquisition jobAcquisition = jobAcquisitionsByName.get(acquisitionName);
    if (jobAcquisition != null) {
      throw new ProcessEngineException("Cannot add acquisition with name '" + acquisitionName + "': already exists.");
    }
    JobAcquisition acquisition = new JobAcquisition(configuration);
    acquisition.setPlatformJobExecutor(this);
    String jobAcquisitionStrategyName = configuration.getJobAcquisitionStrategy();
    JobAcquisitionStrategy jobAcquisitionStrategy = discoveredStrategies.get(jobAcquisitionStrategyName);
    if(jobAcquisitionStrategy == null) {
      throw new ProcessEngineException("Unknown JobAcquisitionStrategy with name '"+jobAcquisitionStrategyName+"'. Discovered strategies: "+discoveredStrategies.keySet());
    }
    acquisition.setJobAcquisitionStrategy(jobAcquisitionStrategy);
    Map<String, Object> properties = configuration.getJobAcquisitionProperties();
    if (properties.containsKey(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS)) {
      acquisition.setLockTimeInMillis((Integer) properties.get(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS));
    }
    if (properties.containsKey(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION)) {
      acquisition.setWaitTimeInMillis((Integer) properties.get(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS));
    }
    if (properties.containsKey(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS)) {
      acquisition.setMaxJobsPerAcquisition((Integer) properties.get(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION));
    }
    jobAcquisitionsByName.put(configuration.getAcquisitionName(), acquisition);
    return acquisition;
  }

  public synchronized void stopJobAcquisition(String jobAcquisitionName) {
    JobAcquisition jobAcquisition = jobAcquisitionsByName.get(jobAcquisitionName);
    if (jobAcquisition == null) {
      return;
    }
    jobAcquisition.shutdown();  
    jobAcquisitionsByName.remove(jobAcquisitionName);
  }


  public synchronized JobExecutor registerProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName) {
    JobAcquisition jobAcquisition = jobAcquisitionsByName.get(acquisitionName);
    if (jobAcquisition == null) {
      throw new ProcessEngineException("Cannot register process engine with PlatformJobExecutor: acquisition with name '" + acquisitionName + "' not found.");
    }
    jobAcquisition.registerProcessEngine(processEngineConfiguration);
    
    if(!jobAcquisition.isActive()) {
      jobAcquisition.start();
    }
    
    return new PlatformJobExecutorDelegate(this, acquisitionName);
  }

  public synchronized void unregisterProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName) {
    JobAcquisition jobAcquisition = jobAcquisitionsByName.get(acquisitionName);
    if (jobAcquisition == null) {
      return;
    }
    
    jobAcquisition.unregisterProcessEngine(processEngineConfiguration);
    
    if(jobAcquisition.isActive() && jobAcquisition.getRegisteredProcessEngines().size() == 0) {
      jobAcquisition.shutdown();
    }    
  }
  
  @Override
  public JobAcquisition getJobAcquisitionByName(String name) {
    JobAcquisition jobAcquisition = jobAcquisitionsByName.get(name);
    if (jobAcquisition == null) {
      throw new ProcessEngineException("acquisition with name '" + name + "' not found.");
    }
    return jobAcquisition;
  }
  
  @Override
  public List<JobExecutor> getJobAcquisitions() {
    return new ArrayList<JobExecutor>(jobAcquisitionsByName.values());
  }
  
  public Map<String, JobAcquisitionStrategy> getDiscoveredStrategies() {
    return discoveredStrategies;
  }

  public abstract void executeJobs(List<String> jobIds, CommandExecutor commandExecutor);
  
  public abstract Object scheduleAcquisition(Runnable acquisitionRunnable);

  public abstract void unscheduleAcquisition(Object scheduledAcquisition);

}
