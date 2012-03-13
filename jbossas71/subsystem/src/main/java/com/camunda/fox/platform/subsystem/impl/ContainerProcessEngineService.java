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
package com.camunda.fox.platform.subsystem.impl;

import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.jboss.as.connector.subsystems.datasources.DataSourceReferenceFactoryService;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.AbstractProcessEngineService;
import com.camunda.fox.platform.impl.configuration.JtaCmpeProcessEngineConfiguration;
import com.camunda.fox.platform.impl.jobexecutor.simple.SimpleJobExecutor;
import com.camunda.fox.platform.subsystem.impl.util.ProcessEngineServiceReferenceFactory;
import com.camunda.fox.platform.subsystem.impl.util.Tccl;
import com.camunda.fox.platform.subsystem.impl.util.Tccl.Operation;


/**
 * <p>{@link ProcessEngineService} and {@link ProcessArchiveService} iplementation as a jboss msc service.</p>
 * 
 * <p>This service is boostraped by the service container</p>
 *   
 * <p>This service represents a running process engine. There is an instance of this service for each 
 * process engine defined in the server configuration.</p>
 * 
 * @author Daniel Meyer
 */
public class ContainerProcessEngineService extends AbstractProcessEngineService implements Service<ContainerProcessEngineService> {
  
  private static Logger log = Logger.getLogger(ContainerProcessEngineService.class.getName());
  
  // state ///////////////////////////////////////////
  
  // Injecting these values makes the MSC aware of our dependencies on these resources.
  // This ensures that they are available when this service is started
  private final InjectedValue<TransactionManager> transactionManagerValue = new InjectedValue<TransactionManager>();
  private final InjectedValue<DataSourceReferenceFactoryService> datasourceBinderServiceValue = new InjectedValue<DataSourceReferenceFactoryService>();

  // jndi bindings
  protected ServiceController<ManagedReferenceFactory> processArchiveServiceBinding;
  protected ServiceController<ManagedReferenceFactory> processEngineServiceBinding;

  
  // additional configuration values ////////////////////////////////
  
  protected final String processEngineName;
  
  protected int jobExecutor_queueSize;
  protected int jobExecutor_corePoolSize;
  protected int jobExecutor_maxPoolSize;
  
  public ContainerProcessEngineService(String processEngineName) {
    this.processEngineName = processEngineName;
  }
  
  // org.jboss.msc.service.Service implementation //////////////////////////////////////
  
  @Override
  public ContainerProcessEngineService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  @Override
  public synchronized void start(StartContext context) throws StartException {
    // setting the TCCL to the Classloader of this module.
    // this exploits a hack in MyBatis allowing it to use the TCCL to load the 
    // mapping files from the process engine module
    Tccl.runUnderClassloader(new Operation<Void>() {
      public Void run() {
        start();
        return null;
      }
    }, getClass().getClassLoader());   
    
    createJndiBindings(context);
  }
  
  @Override
  public synchronized void stop(StopContext arg0) {   
    stop();    
    removeJndiBindings();
  }
    
  // implementation //////////////////////////////
  
  @Override
  protected void initProcessEngineConfiguration() {
    super.initProcessEngineConfiguration();
    JtaCmpeProcessEngineConfiguration processEngineConfiguration = (JtaCmpeProcessEngineConfiguration) this.processEngineConfiguration;
    // use the injected datasource
    processEngineConfiguration.setDataSource((DataSource) datasourceBinderServiceValue.getValue().getReference().getInstance());
    // use the injected transaction manager
    processEngineConfiguration.setTransactionManager(transactionManagerValue.getValue());
    
    processEngineConfiguration.initJobExecutor();
    SimpleJobExecutor jobExecutor = (SimpleJobExecutor) processEngineConfiguration.getJobExecutor();
    jobExecutor.setCorePoolSize(jobExecutor_corePoolSize);
    jobExecutor.setMaxPoolSize(jobExecutor_maxPoolSize);
    jobExecutor.setQueueSize(jobExecutor_queueSize);
  }
  
  public static ServiceName createServiceName(String engineName) {
    return ServiceName.of("ProcessEngineService", engineName);
  }
  
  protected void createJndiBindings(StartContext context) {
    
    final String prefix = "java:global/camunda-fox-platform/";
    final String processArchiveServiceSuffix = "ProcessEngineService!com.camunda.fox.platform.api.ProcessArchiveService";
    final String processEngineServiceSuffix = "ProcessEngineService!com.camunda.fox.platform.api.ProcessEngineService";    
    final String processArchiveServiceBindingName = prefix + processEngineName + "/"+ processArchiveServiceSuffix;
    final String processEngineServiceBindingName = prefix + processEngineName + "/"+ processEngineServiceSuffix;
        
    final ServiceName processEngineServiceBindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME            
            .append("camunda-fox-platform")
            .append(processEngineName)
            .append(processEngineServiceSuffix);
    
    final ServiceName processArchiveServiceBindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME
            .append("camunda-fox-platform")
            .append(processEngineName)
            .append(processArchiveServiceSuffix);
    
    final ServiceContainer serviceContainer = context.getController().getServiceContainer();
    
    BinderService processEngineServiceBinder = new BinderService(processEngineServiceBindingName);
    ServiceBuilder<ManagedReferenceFactory> processEngineServiceBuilder = serviceContainer
            .addService(processEngineServiceBindingServiceName, processEngineServiceBinder);
    processEngineServiceBuilder.addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, processEngineServiceBinder.getNamingStoreInjector());
    processEngineServiceBinder.getManagedObjectInjector().inject(new ProcessEngineServiceReferenceFactory(this));
    
    BinderService processArchiveServiceBinder = new BinderService(processEngineServiceBindingName);
    ServiceBuilder<ManagedReferenceFactory> processArchiveServiceBuilder = serviceContainer.addService(processArchiveServiceBindingServiceName, processArchiveServiceBinder);
    processArchiveServiceBuilder.addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, processArchiveServiceBinder.getNamingStoreInjector());
    processArchiveServiceBinder.getManagedObjectInjector().inject(new ProcessEngineServiceReferenceFactory(this));
        
    processEngineServiceBinding = processEngineServiceBuilder.install();
    processArchiveServiceBinding = processArchiveServiceBuilder.install();
    
    log.info("created bindings for process engine service: \n\n"
            + "        "
            + processArchiveServiceBindingName + "\n" 
            + "        " 
            + processEngineServiceBindingName + "\n");
  }

  protected void removeJndiBindings() {
    processEngineServiceBinding.setMode(Mode.REMOVE);
    processArchiveServiceBinding.setMode(Mode.REMOVE);
  }

  // getters / setters /////////////////////////////

  public InjectedValue<TransactionManager> getTransactionManagerValue() {
    return transactionManagerValue;
  }

  public InjectedValue<DataSourceReferenceFactoryService> getDatasourceBinderServiceValue() {
    return datasourceBinderServiceValue;
  }

  public int getJobExecutor_queueSize() {
    return jobExecutor_queueSize;
  }

  public void setJobExecutor_queueSize(int jobExecutor_queueSize) {
    this.jobExecutor_queueSize = jobExecutor_queueSize;    
  }

  public int getJobExecutor_corePoolSize() {
    return jobExecutor_corePoolSize;
  }

  public void setJobExecutor_corePoolSize(int jobExecutor_corePoolSize) {
    this.jobExecutor_corePoolSize = jobExecutor_corePoolSize;
  }

  public int getJobExecutor_maxPoolSize() {
    return jobExecutor_maxPoolSize;
  }

  public void setJobExecutor_maxPoolSize(int jobExecutor_maxPoolSize) {
    this.jobExecutor_maxPoolSize = jobExecutor_maxPoolSize;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

}
