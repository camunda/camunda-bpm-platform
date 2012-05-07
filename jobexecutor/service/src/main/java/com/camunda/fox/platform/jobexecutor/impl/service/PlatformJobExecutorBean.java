package com.camunda.fox.platform.jobexecutor.impl.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.resource.ResourceException;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.jobexecutor.api.PlatformJobExecutorService;
import com.camunda.fox.platform.jobexecutor.impl.ra.outbound.PlatformJobExecutorConnection;
import com.camunda.fox.platform.jobexecutor.impl.ra.outbound.PlatformJobExecutorConnectionFactory;
import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionConfiguration;

/**
 * Bean exposing the Jca implementation of the {@link PlatformJobExecutorService} as
 * as stateless bean.
 * 
 * @author Daniel Meyer
 *
 */
@Stateless
@Local(PlatformJobExecutorService.class)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class PlatformJobExecutorBean implements PlatformJobExecutorService {
  
  @Resource(mappedName="eis/PlatformJobExecutorConnectionFactory")
  protected PlatformJobExecutorConnectionFactory platformJobExecutorConnectionFactory;
  
  protected PlatformJobExecutorConnection platformJobExecutorConnection;

  @PostConstruct
  protected void openConnection() {
    try {
      platformJobExecutorConnection = platformJobExecutorConnectionFactory.getConnection();
    } catch (ResourceException e) {
      throw new FoxPlatformException("Could not open connection to platform job executor resource ", e);
    } 
  }
  
  @PreDestroy
  protected void closeConnection() {
    if(platformJobExecutorConnection != null) {
      platformJobExecutorConnection.closeConnection();
    }
  }

  public JobExecutor startJobAcquisition(JobAcquisitionConfiguration configuration) {
    return platformJobExecutorConnection.startJobAcquisition(configuration);
  }

  public void stopJobAcquisition(String jobAcquisitionName) {
    platformJobExecutorConnection.stopJobAcquisition(jobAcquisitionName);
  }

  public JobExecutor registerProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName) {
    return platformJobExecutorConnection.registerProcessEngine(processEngineConfiguration, acquisitionName);
  }

  public void unregisterProcessEngine(ProcessEngineConfigurationImpl processEngineConfiguration, String acquisitionName) {
    platformJobExecutorConnection.unregisterProcessEngine(processEngineConfiguration, acquisitionName);
  }

  public JobExecutor getJobAcquisitionByName(String name) {
    return platformJobExecutorConnection.getJobAcquisitionByName(name);
  }

  public List<JobExecutor> getJobAcquisitions() {
    return platformJobExecutorConnection.getJobAcquisitions();
  }
  
}
