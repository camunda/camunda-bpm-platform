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
package com.camunda.fox.platform.impl.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.impl.configuration.spi.ProcessEngineConfigurationFactory;
import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.deployment.ActivitiDeployer;
import com.camunda.fox.platform.impl.schema.DbSchemaOperations;
import com.camunda.fox.platform.impl.service.spi.PlatformServiceExtension;
import com.camunda.fox.platform.impl.util.PlatformServiceExtensionHelper;
import com.camunda.fox.platform.impl.util.PropertyHelper;
import com.camunda.fox.platform.impl.util.Services;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>The controller is in charge of managing the lifecycle of a process engine
 * and the process are deployed to it.</p>
 * 
 * @author Daniel Meyer
 */
public class ProcessEngineController {
  
  private static Logger log = Logger.getLogger(ProcessEngineController.class.getName());

  ///////////////////////////////// state
    
  protected Map<String, ProcessArchiveContext> installedProcessArchivesByName = new HashMap<String, ProcessArchiveContext>();
  protected Map<String, ProcessArchiveContext> installedProcessArchivesByProcessDefinitionKey = new HashMap<String, ProcessArchiveContext>();
  protected List<ProcessArchive> cachedProcessArchives = Collections.emptyList();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  
  protected ProcessEngine activitiProcessEngine;
  protected ActivitiDeployer activitiDeployer;
  
  protected boolean isActive = false;
    
  /////////////////////////////////// configuration
  
  protected String processEngineName;
  
  protected String datasourceJndiName;
  protected boolean isAutoUpdateSchema;
  protected boolean isIdentityUsed;
  protected String history;
  protected String databaseTablePrefix;

  protected boolean activateJobExecutor;
  protected int jobExecutor_maxJobsPerAcquisition = 3;
  protected int jobExecutor_waitTimeInMillis =  5 * 1000;
  protected int jobExecutor_lockTimeInMillis =  5 * 60 * 1000;

  protected ProcessEngineRegistry processEngineRegistry;
  protected final ProcessEngineConfiguration processEngineUserConfiguration;

  
  public ProcessEngineController(ProcessEngineConfiguration processEngineConfiguration) {
    
    this.processEngineUserConfiguration = processEngineConfiguration;    
    
    this.processEngineName = processEngineConfiguration.getProcessEngineName();
    this.datasourceJndiName = processEngineConfiguration.getDatasourceJndiName();
    this.isAutoUpdateSchema = PropertyHelper.getProperty(processEngineConfiguration.getProperties(), ProcessEngineConfiguration.PROP_IS_AUTO_SCHEMA_UPDATE, false);
    this.isIdentityUsed = PropertyHelper.getProperty(processEngineConfiguration.getProperties(), ProcessEngineConfiguration.PROP_IS_IDENTITY_USED, true);
    this.activateJobExecutor = PropertyHelper.getProperty(processEngineConfiguration.getProperties(), ProcessEngineConfiguration.PROP_IS_ACTIVATE_JOB_EXECUTOR, false);
    this.databaseTablePrefix = PropertyHelper.getProperty(processEngineConfiguration.getProperties(), ProcessEngineConfiguration.PROP_DB_TABLE_PREFIX, null);
    this.history = processEngineConfiguration.getHistoryLevel();    
  }
      
  ////////////////////////////////// lifecycle
  
  public synchronized void start() {
    try {     
      fireBeforeProcessEngineControllerStart(this);
      init();
      processEngineRegistry.processEngineInstallationSuccess(processEngineUserConfiguration, this);
  		isActive = true;
  		fireAfterProcessEngineControllerStart(this);
    } catch (Exception e) {
      processEngineRegistry.processEngineInstallationFailed(processEngineUserConfiguration);
      throw new FoxPlatformException("Exception while attempting to start process engine: ", e);
    }
  }

  public synchronized void stop() {
    fireBeforeProcessEngineControllerStop(this);    
    closeProcessEngine();       

    Collection<ProcessArchiveContext> installedProcessArchives = new ArrayList<ProcessArchiveContext>(installedProcessArchivesByName.values());
    for (ProcessArchiveContext processArchive : installedProcessArchives) {
      unInstallProcessArchive(processArchive.getProcessArchive());      
    }

    processEngineRegistry.processEngineUninstalled(this);

    installedProcessArchivesByName.clear();
    installedProcessArchivesByProcessDefinitionKey.clear();
    activitiProcessEngine = null;
    processEngineConfiguration = null;    
    isActive = false;
    
    fireAfterProcessEngineControllerStop(this);
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
    ProcessEngines.unregister(activitiProcessEngine);
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    if(jobExecutor != null && jobExecutor.isActive()) {
      jobExecutor.shutdown();
    }
  }

  protected void logConfiguration() {
  	StringWriter configLog = new StringWriter();
  	configLog.append("process engine configuration for process engine '"+processEngineName+"': \n");
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
    activitiProcessEngine = processEngineConfiguration.buildProcessEngine();    
    log.info("Using IdGenerator["+processEngineConfiguration.getIdGenerator().getClass().getName()+"]");
    
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.setLockTimeInMillis(jobExecutor_lockTimeInMillis);
    jobExecutor.setMaxJobsPerAcquisition(jobExecutor_maxJobsPerAcquisition);
    jobExecutor.setWaitTimeInMillis(jobExecutor_waitTimeInMillis);
    
  }

  protected void initProcessEngineConfiguration() {
    // obtain a configuration form the factory
    ProcessEngineConfigurationFactory configurationFactory = Services.getService(ProcessEngineConfigurationFactory.class);
    configurationFactory.setProcessEngineController(this);
    
    processEngineConfiguration = configurationFactory.getProcessEngineConfiguration();
    processEngineConfiguration.setProcessEngineName(processEngineName);
    processEngineConfiguration.setDataSourceJndiName(datasourceJndiName);
    processEngineConfiguration.setJobExecutorActivate(activateJobExecutor);
    
    // disable Activiti schema mechanism complety, it should not even check anything
    // we do this on our own, and at least https://jira.codehaus.org/browse/ACT-1062 makes problems
    // with schema prefixes of multiple engines
    processEngineConfiguration.setDatabaseSchemaUpdate("fox");
    if(databaseTablePrefix != null) {
      processEngineConfiguration.setDatabaseTablePrefix(databaseTablePrefix);
    }
    processEngineConfiguration.setDbIdentityUsed(isIdentityUsed);

    processEngineConfiguration.setHistory(history);
  }

  protected void updateSchema() {
    if(isAutoUpdateSchema) {
      log.info("now performing process engine auto schema update.");
      DbSchemaOperations dbSchemaOperations = new DbSchemaOperations();
      dbSchemaOperations.setHistory(history);
      dbSchemaOperations.setDbIdentityUsed(isIdentityUsed);
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
      throw new FoxPlatformException("Cannot install process archive with name '"+paName+"': process archive with same name already installed.");
    }
    
    log.info("Installing process archive '"+paName+"'"); 
    Deployment deployment = performEngineDeployment(processArchive);
    
    ProcessArchiveContext processArchiveContext = new ProcessArchiveContext(deployment, processArchive);
    processArchiveContext.setActive(true);
    installedProcessArchivesByName.put(processArchive.getName(), processArchiveContext);

    ArrayList<ProcessArchive> processArchives = new ArrayList<ProcessArchive>(cachedProcessArchives);
    processArchives.add(processArchive);
    cachedProcessArchives = Collections.unmodifiableList(processArchives);

    if (deployment != null) {
      List<ProcessDefinition> processDefinitionsForThisDeployment = activitiProcessEngine.getRepositoryService().createProcessDefinitionQuery()
              .deploymentId(deployment.getId()).list();
      for (ProcessDefinition processDefinition : processDefinitionsForThisDeployment) {
        installedProcessArchivesByProcessDefinitionKey.put(processDefinition.getKey(), processArchiveContext);
      }
      log.info("Installed process archive '" + paName + "' to process engine '"+processEngineName+"'.");
      return activitiProcessEngine;
    } else {      
      log.info("Installed empty process archive '"+paName+"'. Process archive will have access to process engine with name '"+processEngineName+"'.");
      return activitiProcessEngine;
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
      
      ArrayList<ProcessArchive> processArchives = new ArrayList<ProcessArchive>(cachedProcessArchives);
      processArchives.remove(processArchive);
      cachedProcessArchives = Collections.unmodifiableList(processArchives);
      
      if(processArchiveContext.getActivitiDeployment() != null) {
        String activitiDeploymentId = processArchiveContext.getActivitiDeployment().getId();
        List<ProcessDefinition> processDefinitions = activitiProcessEngine.getRepositoryService()
          .createProcessDefinitionQuery()
          .deploymentId(activitiDeploymentId)
          .list();
        for (ProcessDefinition processDefinition : processDefinitions) {
          installedProcessArchivesByProcessDefinitionKey.remove(processDefinition.getKey());
        }
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
      return activitiProcessEngine.getRepositoryService()
              .createDeploymentQuery()
              .deploymentId(deploymentId)
              .singleResult();
    }
  }
  
  public ProcessArchiveContext getProcessArchiveContext(String processDefinitionKey) {
    return installedProcessArchivesByProcessDefinitionKey.get(processDefinitionKey);
  }
  
  public ProcessArchive getProcessArchiveByProcessDefinitionId(final String processDefinitionId) {
    // first try to hit the cache
    ProcessDefinitionEntity processDefinitionEntity = processEngineConfiguration
      .getDeploymentCache()
      .getProcessDefinitionCache()
      .get(processDefinitionId);
    if(processDefinitionEntity == null) {
      // now look for it in the database (will add it to the cache).
      processDefinitionEntity = processEngineConfiguration
        .getCommandExecutorTxRequired()
        .execute(new Command<ProcessDefinitionEntity>() {
          public ProcessDefinitionEntity execute(CommandContext commandContext) {
            return commandContext.getProcessDefinitionManager()
              .findLatestProcessDefinitionById(processDefinitionId);
            }
          });
    }
    if(processDefinitionEntity == null) {
      throw new FoxPlatformException("Could not find process definition with id '"+processDefinitionId+"' for process engine '"+processEngineName+"'.");  
    }    
    String processDefinitionKey = processDefinitionEntity.getKey();
    if(processDefinitionKey == null) {
      throw new FoxPlatformException("Could not find process definition with id '"+processDefinitionId+"' for process engine '"+processEngineName+"'.");
    }
    return getProcessArchiveByProcessDefinitionKey(processDefinitionKey);
  }
  
  public ProcessArchive getProcessArchiveByProcessDefinitionKey(String processDefinitionKey) {
    ProcessArchiveContext processArchiveContext = installedProcessArchivesByProcessDefinitionKey.get(processDefinitionKey);
    if(processArchiveContext == null) {
      throw new FoxPlatformException("No process archive installed for key '"+processDefinitionKey+"', on process engine '"+processEngineName+"'.");
    }
    return processArchiveContext.getProcessArchive();
  }

  
  // getters/setters //////////////////////////////////////////////
    
  public ProcessEngine getProcessEngine() {
    return activitiProcessEngine;
  }
  
  public Map<String, ProcessArchiveContext> getInstalledProcessArchivesByName() {
    return new HashMap<String, ProcessArchiveContext>(installedProcessArchivesByName);
  }
  
  public Map<String, ProcessArchiveContext> getInstalledProcessArchivesByProcessDefinitionKey() {
    return new HashMap<String, ProcessArchiveContext>(installedProcessArchivesByProcessDefinitionKey);
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }
  
  public ActivitiDeployer getActivitiDeployer() {
    return activitiDeployer;
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
  
  public boolean isIdentityUsed() {
    return isIdentityUsed;
  }
 
  public void setIdentityUsed(boolean isIdentityUsed) {
    this.isIdentityUsed = isIdentityUsed;
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
  
  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }
  
  public String getProcessEngineName() {
    return processEngineName;
  }
    
  public List<ProcessArchive> getCachedProcessArchives() {
    return cachedProcessArchives;
  }
  
  public void setProcessEngineRegistry(ProcessEngineRegistry processEngineRegistry) {
    this.processEngineRegistry = processEngineRegistry;
  }
  
  public ProcessEngineConfiguration getProcessEngineUserConfiguration() {
    return processEngineUserConfiguration;
  }
  
  // extensions support ////////////////////////////////////////////////////
  
  protected void fireBeforeProcessEngineControllerStart(ProcessEngineController processEngineController) {
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {      
      try {
        platformServiceExtension.beforeProcessEngineControllerStart(processEngineController);
      }catch (Exception e) {
        throw new FoxPlatformException("Exception while invoking 'beforeProcessEngineControllerStart' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }
  }
  
  protected void fireAfterProcessEngineControllerStart(ProcessEngineController processEngineController) {
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {
      try {
        platformServiceExtension.afterProcessEngineControllerStart(processEngineController);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while invoking 'afterProcessEngineControllerStart' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }
  }
  
  protected void fireBeforeProcessEngineControllerStop(ProcessEngineController processEngineController) {
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    Collections.reverse(loadableExtensions);
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {
      try {
        platformServiceExtension.beforeProcessEngineControllerStop(processEngineController);
      }catch (Exception e) {
        throw new FoxPlatformException("Exception while invoking 'beforeProcessEngineControllerStop' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }
  }
  
  protected void fireAfterProcessEngineControllerStop(ProcessEngineController processEngineController) {
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    Collections.reverse(loadableExtensions);
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {
      try {
        platformServiceExtension.afterProcessEngineControllerStop(processEngineController);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while invoking 'afterProcessEngineControllerStop' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }
  }

}
