package com.camunda.fox.platform.jobexecutor.impl.util;

import java.util.HashMap;
import java.util.Map;

import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;

/**
 * Java Bean implementation of {@link JobAcquisitionConfiguration}
 * 
 * @author Daniel Meyer
 * 
 */
public class JobAcquisitionConfigurationBean implements JobAcquisitionConfiguration {

  protected String acquisitionName;
  protected String jobAcquisitionStrategy;
  protected String lockOwner;

  protected Integer lockTimeInMillis;
  protected Integer maxJobsPerAcquisition;
  protected Integer waitTimeInMillis;
  
  protected Map<String, Object> properties = new HashMap<String, Object>();
  
  public JobAcquisitionConfigurationBean() {
    initJobAcquisitionConfigurationDefaultValues();
  }

  public String getAcquisitionName() {
    return acquisitionName;
  }

  public void setAcquisitionName(String acquisitionName) {
    this.acquisitionName = acquisitionName;
  }

  public String getJobAcquisitionStrategy() {
    return jobAcquisitionStrategy;
  }

  public void setJobAcquisitionStrategy(String jobAcquisitionStrategy) {
    this.jobAcquisitionStrategy = jobAcquisitionStrategy;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  public Integer getLockTimeInMillis() {
    return lockTimeInMillis;
  }

  public void setLockTimeInMillis(Integer lockTimeInMillis) {
    this.lockTimeInMillis = lockTimeInMillis;
  }

  public Integer getMaxJobsPerAcquisition() {
    return maxJobsPerAcquisition;
  }

  public void setMaxJobsPerAcquisition(Integer maxJobsPerAcquisition) {
    this.maxJobsPerAcquisition = maxJobsPerAcquisition;
  }

  public Integer getWaitTimeInMillis() {
    return waitTimeInMillis;
  }

  public void setWaitTimeInMillis(Integer waitTimeInMillis) {
    this.waitTimeInMillis = waitTimeInMillis;
  }

  @Override
  public Map<String, Object> getJobAcquisitionProperties() {
//    properties.put(PROP_LOCK_TIME_IN_MILLIS, lockTimeInMillis);
//    properties.put(PROP_MAX_JOBS_PER_ACQUISITION, maxJobsPerAcquisition);
//    properties.put(PROP_WAIT_TIME_IN_MILLIS, waitTimeInMillis);
    return properties;
  }
  
  private void initJobAcquisitionConfigurationDefaultValues() {
    lockTimeInMillis = 5 * 60 * 1000;
    maxJobsPerAcquisition = 3;
    waitTimeInMillis = 5 * 1000;
    
    if (properties.get(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS) == null) {
       properties.put(JobAcquisitionConfiguration.PROP_LOCK_TIME_IN_MILLIS, lockTimeInMillis);          
    }
    if (properties.get(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION) == null) {
       properties.put(JobAcquisitionConfiguration.PROP_MAX_JOBS_PER_ACQUISITION, maxJobsPerAcquisition);
    }
    if (properties.get(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS) == null) {
       properties.put(JobAcquisitionConfiguration.PROP_WAIT_TIME_IN_MILLIS, waitTimeInMillis);
    }    

  }

}
