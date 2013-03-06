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
package com.camunda.fox.platform.subsystem.impl.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngineController;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.jta.db.DbSchemaOperations;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.jboss.as.connector.subsystems.datasources.DataSourceReferenceFactoryService;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import com.camunda.fox.platform.subsystem.impl.metadata.ManagedProcessEngineMetadata;
import com.camunda.fox.platform.subsystem.impl.util.Tccl;
import com.camunda.fox.platform.subsystem.impl.util.Tccl.Operation;

/**
 * <p>Service responsible for starting / stopping a managed process engine inside the Msc</p>
 * 
 * <p>This service is used for managing process engines that are started / stopped 
 * through the application server management infrastructure.</p>
 * 
 * <p>This is the Msc counterpart of the {@link JmxManagedProcessEngineController}</p>
 * 
 * @author Daniel Meyer
 */
public class MscManagedProcessEngineController extends MscManagedProcessEngine {
  
  private final static Logger LOGGER = Logger.getLogger(MscManagedProcessEngineController.class.getName());
    
  // Injecting these values makes the MSC aware of our dependencies on these resources.
  // This ensures that they are available when this service is started
  protected final InjectedValue<TransactionManager> transactionManagerInjector = new InjectedValue<TransactionManager>();
  protected final InjectedValue<DataSourceReferenceFactoryService> datasourceBinderServiceInjector = new InjectedValue<DataSourceReferenceFactoryService>();
  protected final InjectedValue<MscRuntimeContainerDelegate> containerPlatformServiceInjector = new InjectedValue<MscRuntimeContainerDelegate>();
  protected final InjectedValue<ContainerJobExecutorService> containerJobExecutorInjector = new InjectedValue<ContainerJobExecutorService>();
    
  protected ManagedProcessEngineMetadata processEngineMetadata;

  protected JtaProcessEngineConfiguration processEngineConfiguration;
  
  /**
   * Instantiate  the process engine controller for a process engine configuration. 
   * 
   */
  public MscManagedProcessEngineController(ManagedProcessEngineMetadata processEngineConfiguration) {
    this.processEngineMetadata = processEngineConfiguration;
  }
  
  public void start(StartContext context) throws StartException {
        
    // setting the TCCL to the Classloader of this module.
    // this exploits a hack in MyBatis allowing it to use the TCCL to load the 
    // mapping files from the process engine module
    Tccl.runUnderClassloader(new Operation<Void>() {
      public Void run() {        
        performDbSchemaOperations();
        startProcessEngine();
        return null;
      }

    }, ProcessEngine.class.getClassLoader());   
    
    setJobExecutorDelegate();    
  }
  
  protected void performDbSchemaOperations() {
    if(processEngineMetadata.isAutoSchemaUpdate()) {
      
      if(processEngineMetadata.getDbTablePrefix() != null) {
        throw new ProcessEngineException("Cannot use '" + ManagedProcessEngineMetadata.PROP_IS_AUTO_SCHEMA_UPDATE + "=true' in combination with "
            + ManagedProcessEngineMetadata.PROP_DB_TABLE_PREFIX);
      }
      
      LOGGER.info("now performing process engine auto schema update for process engine "+processEngineMetadata.getEngineName());
      DbSchemaOperations dbSchemaOperations = new DbSchemaOperations();
      dbSchemaOperations.setHistory(processEngineMetadata.getHistoryLevel());
      dbSchemaOperations.setDbIdentityUsed(processEngineMetadata.isIdentityUsed());
      dbSchemaOperations.setDataSourceJndiName(processEngineMetadata.getDatasourceJndiName());
      dbSchemaOperations.update();      
    }    
  }
  
  protected void startProcessEngine() {
    
    processEngineConfiguration = new JtaProcessEngineConfiguration();
        
    // set the name for the process engine 
    processEngineConfiguration.setProcessEngineName(processEngineMetadata.getEngineName());
    
    // set UUid generator
    // TODO: move this to configuration and use as default?
    processEngineConfiguration.setIdGenerator(new StrongUuidGenerator());
    
    // use the injected datasource
    processEngineConfiguration.setDataSource((DataSource) datasourceBinderServiceInjector.getValue().getReference().getInstance());
    
    // use the injected transaction manager
    processEngineConfiguration.setTransactionManager(transactionManagerInjector.getValue());    
    
    // set the value for the history
    processEngineConfiguration.setHistory(processEngineMetadata.getHistoryLevel());

    // set db table prefix
    if( processEngineMetadata.getDbTablePrefix() != null ) {
      processEngineConfiguration.setDatabaseTablePrefix(processEngineMetadata.getDbTablePrefix());
    }
    
    processEngine = processEngineConfiguration.buildProcessEngine();        
  }

  public void stop(StopContext context) {
    
    try {
      processEngine.close();
      
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "exception while closing process engine", e);
    }
      
    releaseJobExecutorDelegate();      
   
  }

  public Injector<TransactionManager> getTransactionManagerInjector() {
    return transactionManagerInjector;
  }

  public Injector<DataSourceReferenceFactoryService> getDatasourceBinderServiceInjector() {
    return datasourceBinderServiceInjector;
  }
    
  public InjectedValue<MscRuntimeContainerDelegate> getContainerPlatformServiceInjector() {
    return containerPlatformServiceInjector;
  }
  
  public InjectedValue<ContainerJobExecutorService> getContainerJobExecutorInjector() {
    return containerJobExecutorInjector;
  }

  public static void initializeServiceBuilder(ManagedProcessEngineMetadata processEngineConfiguration, MscManagedProcessEngineController service,
          ServiceBuilder<ProcessEngine> serviceBuilder) {
    
    ContextNames.BindInfo datasourceBindInfo = ContextNames.bindInfoFor(processEngineConfiguration.getDatasourceJndiName());
    serviceBuilder.addDependency(ServiceName.JBOSS.append("txn").append("TransactionManager"), TransactionManager.class, service.getTransactionManagerInjector())
      .addDependency(datasourceBindInfo.getBinderServiceName(), DataSourceReferenceFactoryService.class, service.getDatasourceBinderServiceInjector())
      .addDependency(ServiceNames.forMscRuntimeContainerDelegate(), MscRuntimeContainerDelegate.class, service.getContainerPlatformServiceInjector())
      .addDependency(ContainerJobExecutorService.getServiceName(), ContainerJobExecutorService.class, service.getContainerJobExecutorInjector())            
      .setInitialMode(Mode.ACTIVE);
    
  }

  protected void setJobExecutorDelegate() {

    String jobAcquisitionName = processEngineMetadata.getJobExecutorAcquisitionName();
    
    if (jobAcquisitionName != null) {
      // register the process engine
      JobExecutor jobExecutorDelegate = containerJobExecutorInjector.getValue().registerProcessEngine(processEngineConfiguration, jobAcquisitionName);
      processEngineConfiguration.setJobExecutor(jobExecutorDelegate);
    }

  }
  
  protected void releaseJobExecutorDelegate() {

    String jobAcquisitionName = processEngineMetadata.getJobExecutorAcquisitionName();
    
    if (jobAcquisitionName != null) {
      // register the process engine
      containerJobExecutorInjector.getValue().unregisterProcessEngine(processEngineConfiguration, jobAcquisitionName);
      processEngineConfiguration.setJobExecutor(null);
    }

  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
  
}
