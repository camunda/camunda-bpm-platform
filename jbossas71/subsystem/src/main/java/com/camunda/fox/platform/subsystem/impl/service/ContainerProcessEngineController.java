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

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.jboss.as.connector.subsystems.datasources.DataSourceReferenceFactoryService;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import com.camunda.fox.platform.impl.configuration.JtaCmpeProcessEngineConfiguration;
import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.service.ProcessEngineController;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;
import com.camunda.fox.platform.subsystem.impl.util.Tccl;
import com.camunda.fox.platform.subsystem.impl.util.Tccl.Operation;

/**
 * @author Daniel Meyer
 */
public class ContainerProcessEngineController extends ProcessEngineController implements Service<ContainerProcessEngineController> {
    
  // Injecting these values makes the MSC aware of our dependencies on these resources.
  // This ensures that they are available when this service is started
  private final InjectedValue<TransactionManager> transactionManagerInjector = new InjectedValue<TransactionManager>();
  private final InjectedValue<DataSourceReferenceFactoryService> datasourceBinderServiceInjector = new InjectedValue<DataSourceReferenceFactoryService>();
  private final InjectedValue<ContainerPlatformService> containerPlatformServiceInjector = new InjectedValue<ContainerPlatformService>();
  private final InjectedValue<ContainerJobExecutorService> containerJobExecutorInjector = new InjectedValue<ContainerJobExecutorService>();
  
  public ContainerProcessEngineController(ProcessEngineConfiguration processEngineConfiguration) {
    super(processEngineConfiguration);
  }
  
  public static ServiceName createServiceName(String engineName) {
    return ServiceName.of("foxPlatform", "processEngineController", engineName);
  }

  public ContainerProcessEngineController getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }
  
  public void start(StartContext context) throws StartException {
    
    processEngineRegistry = containerPlatformServiceInjector.getValue().getProcessEngineRegistry();
    processEngineRegistry.startInstallingNewProcessEngine(processEngineUserConfiguration);
    
    // setting the TCCL to the Classloader of this module.
    // this exploits a hack in MyBatis allowing it to use the TCCL to load the 
    // mapping files from the process engine module
    Tccl.runUnderClassloader(new Operation<Void>() {
      public Void run() {
        start();
        return null;
      }
    }, ContainerProcessEngineController.class.getClassLoader());   
  }
  
  protected void initProcessEngineConfiguration() {
    super.initProcessEngineConfiguration();
    JtaCmpeProcessEngineConfiguration processEngineConfiguration = (JtaCmpeProcessEngineConfiguration) this.processEngineConfiguration;
    // use the injected datasource
    processEngineConfiguration.setDataSource((DataSource) datasourceBinderServiceInjector.getValue().getReference().getInstance());
    // use the injected transaction manager
    processEngineConfiguration.setTransactionManager(transactionManagerInjector.getValue());    
  }

  @Override
  public void stop(StopContext context) {
    super.stop();
  }

  public Injector<TransactionManager> getTransactionManagerInjector() {
    return transactionManagerInjector;
  }

  public Injector<DataSourceReferenceFactoryService> getDatasourceBinderServiceInjector() {
    return datasourceBinderServiceInjector;
  }
    
  public InjectedValue<ContainerPlatformService> getContainerPlatformServiceInjector() {
    return containerPlatformServiceInjector;
  }
  
  public InjectedValue<ContainerJobExecutorService> getContainerJobExecutorInjector() {
    return containerJobExecutorInjector;
  }
  
}
