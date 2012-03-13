/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.configuration.spi.ProcessEngineConfigurationFactory;
import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.deployment.ActivitiDeployer;
import com.camunda.fox.platform.impl.engine.ProcessArchiveProcessEngine;
import com.camunda.fox.platform.impl.schema.DbSchemaOperations;
import com.camunda.fox.platform.impl.util.Services;
import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * <p>Implementation of the {@link ProcessArchiveService} and {@link ProcessEngineService}</p>
 * 
 * @author Daniel Meyer
 */
public abstract class AbstractProcessEngineService implements ProcessArchiveService, ProcessEngineService {
  
  private static Logger log = Logger.getLogger(AbstractProcessEngineService.class.getName());

  ///////////////////////////////// state
    
  protected Map<String, ProcessArchiveContext> installedProcessArchivesByName = new HashMap<String, ProcessArchiveContext>();
  protected Map<String, ProcessArchiveContext> installedProcessArchivesByProcessDefinitionKey = new HashMap<String, ProcessArchiveContext>();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ProcessEngine processEngine;
  protected ActivitiDeployer activitiDeployer;
  protected ProcessEngine globalProcessEngine;

  protected boolean isActive = false;
    
  /////////////////////////////////// configuration
  
  protected String datasourceJndiName;
  protected boolean isAutoUpdateSchema;
  protected String history;

  protected boolean activateJobExecutor;
  protected int jobExecutor_maxJobsPerAcquisition;
  protected int jobExecutor_waitTimeInMillis;
  protected int jobExecutor_lockTimeInMillis;
      
  ////////////////////////////////// lifecycle
  
  public synchronized void start() {
    init();
		isActive = true;
  }

  public synchronized void stop() {
    closeProcessEngine();   
    installedProcessArchivesByName.clear();
    installedProcessArchivesByProcessDefinitionKey.clear();
    isActive = false;
  }
  
  protected void init() { 
    Services.clear();
    logConfiguration();
    updateSchema();   
    initProcessEngineConfiguration();
    initProcessEngine();
    initDeployer();
  }
  
  protected void closeProcessEngine() {
    ProcessEngines.unregister(processEngine);
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    if(jobExecutor.isActive()) {
      jobExecutor.shutdown();
    }
    processEngine = null;
    processEngineConfiguration = null;    
  }

  protected void logConfiguration() {
  	StringWriter configLog = new StringWriter();
  	configLog.append("process engine configuration: \n");
  	configLog.append("\n");
  	configLog.append("        history: "+history+"\n");
  	configLog.append("        datasourceJndiName: "+datasourceJndiName+"\n");
  	configLog.append("        isAutoUpdateSchema: "+isAutoUpdateSchema+"\n");
  	configLog.append("        activateJobExecutor: "+activateJobExecutor+"\n");
  	configLog.append("        jobExecutor_maxJobsPerAcquisition: "+jobExecutor_maxJobsPerAcquisition+"\n");
  	configLog.append("        jobExecutor_waitTimeInMillis: "+jobExecutor_waitTimeInMillis+"\n");
  	configLog.append("        jobExecutor_lockTimeInMillis: "+jobExecutor_lockTimeInMillis+"\n");
  	log.info(configLog.toString());
  }

  protected void initProcessEngine() {   
    processEngine = processEngineConfiguration.buildProcessEngine();    
    log.info("Using IdGenerator["+processEngineConfiguration.getIdGenerator().getClass().getName()+"]");
    
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.setLockTimeInMillis(jobExecutor_lockTimeInMillis);
    jobExecutor.setMaxJobsPerAcquisition(jobExecutor_maxJobsPerAcquisition);
    jobExecutor.setWaitTimeInMillis(jobExecutor_waitTimeInMillis);
    
    globalProcessEngine = processEngine;
  }

  protected void initProcessEngineConfiguration() {
    // obtain a configuration form the factory
    ProcessEngineConfigurationFactory configurationFactory = Services.getService(ProcessEngineConfigurationFactory.class);
    configurationFactory.setProcessEngineServiceBean(this);
    
    processEngineConfiguration = configurationFactory.getProcessEngineConfiguration();
    processEngineConfiguration.setDataSourceJndiName(datasourceJndiName);
    processEngineConfiguration.setJobExecutorActivate(activateJobExecutor);    
    processEngineConfiguration.setDatabaseSchemaUpdate("false");  // never perform operations with this PEC
    processEngineConfiguration.setHistory(history);
  }

  protected void updateSchema() {
    if(isAutoUpdateSchema) {
      log.info("now performing process engine auto schema update.");
      DbSchemaOperations dbSchemaOperations = new DbSchemaOperations();
      dbSchemaOperations.setHistory(history);
      dbSchemaOperations.setDataSourceJndiName(datasourceJndiName);    
      dbSchemaOperations.update();      
    }
  }
  
  protected void initDeployer() {
    activitiDeployer = new ActivitiDeployer(processEngineConfiguration.getCommandExecutorTxRequired());
  }

  ////////////////////////////////// ProcessArchiveService implementation

  public synchronized ProcessEngine installProcessArchive(ProcessArchive processArchive) {    
    final String paName = processArchive.getName();

    if(!isActive) {
      throw new FoxPlatformException("Cannot install process archive with name "+paName+": ProcessEngineService is not active.");
    }
    
    if(installedProcessArchivesByName.containsKey(paName)) {
      throw new FoxPlatformException("Cannot install process archive with name '"+paName+"': procass archive with same name already installed.");
    }
    
    log.info("Installing process archive '"+paName+"'"); 
    Deployment deployment = performEngineDeployment(processArchive);
    if(deployment != null) {
      ProcessArchiveContext processArchiveContext = new ProcessArchiveContext(deployment, processArchive);
      processArchiveContext.setActive(true);
      ProcessArchiveProcessEngine processEngineProxy = new ProcessArchiveProcessEngine(processArchiveContext, processEngine);    
   
      installedProcessArchivesByName.put(processArchive.getName(), processArchiveContext);
      
      List<ProcessDefinition> processDefinitionsForThisDeployment = processEngine.getRepositoryService()
        .createProcessDefinitionQuery()
        .deploymentId(deployment.getId())
        .list();
      for (ProcessDefinition processDefinition : processDefinitionsForThisDeployment) {
        installedProcessArchivesByProcessDefinitionKey.put(processDefinition.getKey(), processArchiveContext);       
      }            
      
      log.info("Installed process archive '"+paName+"'");
      return processEngineProxy;
    } else {
      log.info("Installed empty process archive '"+paName+"'. Process archive will have access to global process engine.");
      return globalProcessEngine;
    }
  }

  protected boolean ensureActive() {
    start();
    return isActive;
  }

  public synchronized void unInstallProcessArchive(ProcessArchive processArchive) {
    final String paName = processArchive.getName();
    if (!isActive) {
      log.fine("Ignoring uninstall operation of process archive '" + paName + "' on non-active ProcessEngineService.");
    } else {
      performUndeployment(processArchive);
      log.info("Uninstalled process archive '" + paName + "'");
    }
  }

  protected void performUndeployment(ProcessArchive processArchive) {    
    final String paName = processArchive.getName();
  
    ProcessArchiveContext processArchiveContext = installedProcessArchivesByName.get(paName);
    if(processArchiveContext == null) {
      return;
    }
    
    try {
      processArchiveContext.setUndelploying(true);    
      performEngineUndeployment(processArchive);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Exception while performing process engine undeployment: ", e);    
    } finally {
      processArchiveContext.setUndelploying(false);      
      processArchiveContext.setActive(false);
      
      installedProcessArchivesByName.remove(paName);
      
      String activitiDeploymentId = processArchiveContext.getActivitiDeployment().getId();
      List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService()
        .createProcessDefinitionQuery()
        .deploymentId(activitiDeploymentId)
        .list();
      for (ProcessDefinition processDefinition : processDefinitions) {
        installedProcessArchivesByProcessDefinitionKey.remove(processDefinition.getKey());
      }
    }    
  }

  protected void performEngineUndeployment(ProcessArchive processArchive) {
    activitiDeployer.processArchiveUndeployed(processArchive);
  }

  protected Deployment performEngineDeployment(ProcessArchive processArchive) {
    String deploymentId = activitiDeployer.processArchiveDeployed(processArchive);   
    if(deploymentId == null) {
      return null;
    } else {
      return processEngine.getRepositoryService()
              .createDeploymentQuery()
              .deploymentId(deploymentId)
              .singleResult();
    }
  }
  
  public ProcessArchiveContext getProcessArchiveContext(String processDefinitionKey) {
    return installedProcessArchivesByProcessDefinitionKey.get(processDefinitionKey);
  }

  // ProcessEngineService implementation //////////////////////////
  
  @Override
  public ProcessEngine getDefaultProcessEngine() {
    return globalProcessEngine;
  }
  
  // getters/setters //////////////////////////////////////////////

  
  public Map<String, ProcessArchiveContext> getInstalledProcessArchivesByName() {
    return installedProcessArchivesByName;
  }
  
  public Map<String, ProcessArchiveContext> getInstalledProcessArchivesByProcessDefinitionKey() {
    return installedProcessArchivesByProcessDefinitionKey;
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }
  
  public ActivitiDeployer getActivitiDeployer() {
    return activitiDeployer;
  }
  
  public ProcessEngine getGlobalProcessEngine() {
    return globalProcessEngine;
  }

  public boolean isActive() {
    return isActive;
  }

  public String getDatasourceJndiName() {
    return datasourceJndiName;
  }

  public void setDatasourceJndiName(String datasourceJndiName) {
    this.datasourceJndiName = datasourceJndiName;
  }

  public boolean isAutoUpdateSchema() {
    return isAutoUpdateSchema;
  }

  public void setAutoUpdateSchema(boolean isAutoUpdateSchema) {
    this.isAutoUpdateSchema = isAutoUpdateSchema;
  }

  public boolean isActivateJobExecutor() {
    return activateJobExecutor;
  }

  public void setActivateJobExecutor(boolean activateJobExecutor) {
    this.activateJobExecutor = activateJobExecutor;
    if(isActive) {
      log.info("Process engine configuration 'activateJobExecutor' set to "+activateJobExecutor);
      JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
      if(activateJobExecutor && !jobExecutor.isActive()) {
        jobExecutor.start();
      } else if(!activateJobExecutor && jobExecutor.isActive()) {
        jobExecutor.shutdown();
      }
    }
  }

  public String getHistory() {
    return history;
  }

  public void setHistory(String history) {
    this.history = history;
  }

  public int getJobExecutor_maxJobsPerAcquisition() {
    return jobExecutor_maxJobsPerAcquisition;
  }

  public void setJobExecutor_maxJobsPerAcquisition(int jobExecutor_maxJobsPerAcquisition) {
    this.jobExecutor_maxJobsPerAcquisition = jobExecutor_maxJobsPerAcquisition;
    if(isActive) {
      log.info("Process engine configuration 'jobExecutor_maxJobsPerAcquisition' set to "+jobExecutor_maxJobsPerAcquisition);
      processEngineConfiguration.getJobExecutor()
        .setMaxJobsPerAcquisition(jobExecutor_maxJobsPerAcquisition);
    }
  }

  public int getJobExecutor_waitTimeInMillis() {
    return jobExecutor_waitTimeInMillis;
  }

  public void setJobExecutor_waitTimeInMillis(int jobExecutor_waitTimeInMillis) {
    this.jobExecutor_waitTimeInMillis = jobExecutor_waitTimeInMillis;
    if(isActive) {
      log.info("Process engine configuration 'jobExecutor_waitTimeInMillis' set to "+jobExecutor_waitTimeInMillis);
      processEngineConfiguration.getJobExecutor()
        .setWaitTimeInMillis(jobExecutor_waitTimeInMillis);
    }
  }

  public int getJobExecutor_lockTimeInMillis() {
    return jobExecutor_lockTimeInMillis;
  }

  public void setJobExecutor_lockTimeInMillis(int jobExecutor_lockTimeInMillis) {
    this.jobExecutor_lockTimeInMillis = jobExecutor_lockTimeInMillis;
    if(isActive) {
      log.info("Process engine configuration 'jobExecutor_lockTimeInMillis' set to "+jobExecutor_lockTimeInMillis);
      processEngineConfiguration.getJobExecutor()
        .setLockTimeInMillis(jobExecutor_lockTimeInMillis);
    }
  }
  
}
