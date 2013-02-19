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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.impl.simple.DefaultProcessEngineService.ProcessEngineStartOperationImpl;
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

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;
import com.camunda.fox.platform.subsystem.impl.util.PlatformServiceReferenceFactory;
import com.camunda.fox.platform.subsystem.impl.util.ServiceListenerFuture;

/**
 * An implementation of the {@link ProcessEngineService} backed by the JBoss MSC ServiceContainer. 
 * 
 * @author Daniel Meyer
 */
public class ContainerProcessEngineService implements Service<ProcessEngineService>, ProcessEngineService {
  
  // state ///////////////////////////////////////////
  
  // jndi bindings
  protected ServiceController<ManagedReferenceFactory> processEngineServiceBinding;

  private ServiceContainer serviceContainer;
  
  private List<ProcessEngine> processEngines = new CopyOnWriteArrayList<ProcessEngine>();
        
  // Lifecycle /////////////////////////////////////////////////

  @Override
  public void start(StartContext context) throws StartException {
    serviceContainer = context.getController().getServiceContainer();
    createJndiBindings(context);
    BpmPlatform.registerProcessEngineService(this);
  }
   

  @Override
  public void stop(StopContext context) {
    removeJndiBindings();
    BpmPlatform.registerProcessEngineService(null);
  }
  
  // interface implementation /////////////////////////////
       
  public ContainerProcessEngineService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  public ProcessEngine getDefaultProcessEngine() {
    ServiceName processEgineServiceName = ContainerProcessEngineController.createServiceNameForDefaultEngine();
    return getProcessEngineService(processEgineServiceName);
  }

  public List<ProcessEngine> getProcessEngines() {
    return new ArrayList<ProcessEngine>(processEngines);
  }

  public List<String> getProcessEngineNames() {
    List<ProcessEngine> processEngines = getProcessEngines();
    ArrayList<String> result = new ArrayList<String>();
    for (ProcessEngine engine : processEngines) {
      result.add(engine.getName());
    }
    return result;
  }

  public ProcessEngine getProcessEngine(String name) {    
    return getProcessEngineService(ContainerProcessEngineController.createServiceName(name));
  }

  public void stopProcessEngine(ProcessEngine processEngine) {
    if(processEngine == null) {
      throw new FoxPlatformException("ProcessEngine cannot be null");
    }
    stopProcessEngine(processEngine.getName());
  }
  
  public void stopProcessEngine(String name) {
    ServiceName processEngineServiceName = ContainerProcessEngineController.createServiceName(name);
    ServiceController<ContainerProcessEngineController> processEngineServiceController = getProcessEngineServiceController(processEngineServiceName);
    
    // remove the service asynchronously
    processEngineServiceController.setMode(Mode.REMOVE);
  }


  public Future<ProcessEngineStartOperation> startProcessEngine(ProcessEngineConfiguration processEngineConfiguration) {    
  
    final ContainerProcessEngineController processEngineController = new ContainerProcessEngineController(processEngineConfiguration);    
   
    final ServiceListenerFuture<ContainerProcessEngineController, ProcessEngineStartOperation> listener = new ServiceListenerFuture<ContainerProcessEngineController, ProcessEngineStartOperation>(processEngineController) {
      protected void serviceAvailable() {
        this.value = new ProcessEngineStartOperationImpl(serviceInstance.getProcessEngine());
      }      
    };
    
    ServiceName serviceName = ContainerProcessEngineController.createServiceName(processEngineConfiguration.getProcessEngineName());
    
    ServiceBuilder<ContainerProcessEngineController> serviceBuilder = serviceContainer.addService(serviceName, processEngineController);
    
    ContainerProcessEngineController.initializeServiceBuilder(processEngineConfiguration, processEngineController, serviceBuilder);
    
    serviceBuilder.addListener(listener);    
    serviceBuilder.install();
    
    return listener;
  }
  
  // internal implementation ///////////////////////////////
  

  public static ServiceName getServiceName() {
    return ServiceName.of("foxPlatform", "platformService");
  }
  

  protected void createJndiBindings(StartContext context) {
    
    final String prefix = "java:global/camunda-fox-platform/";
    final String processEngineServiceSuffix = "DefaultProcessEngineService!com.camunda.fox.platform.api.ProcessEngineService";    
    
    String moduleName = "process-engine";
    
    final String processEngineServiceBindingName = prefix + moduleName + "/"+ processEngineServiceSuffix;
        
    final ServiceName processEngineServiceBindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME            
      .append("camunda-fox-platform")
      .append(moduleName)
      .append(processEngineServiceSuffix);
        
    final ServiceContainer serviceContainer = context.getController().getServiceContainer();
    
    BinderService processEngineServiceBinder = new BinderService(processEngineServiceBindingName);
    ServiceBuilder<ManagedReferenceFactory> processEngineServiceBuilder = serviceContainer
            .addService(processEngineServiceBindingServiceName, processEngineServiceBinder);
    processEngineServiceBuilder.addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, processEngineServiceBinder.getNamingStoreInjector());
    processEngineServiceBinder.getManagedObjectInjector().inject(new PlatformServiceReferenceFactory(this));
            
    processEngineServiceBinding = processEngineServiceBuilder.install();
    
  }

  protected void removeJndiBindings() {
    processEngineServiceBinding.setMode(Mode.REMOVE);
  }
  
  private ProcessEngine getProcessEngineService(ServiceName processEgineServiceName) {
    ServiceController<ContainerProcessEngineController> serviceController = getProcessEngineServiceController(processEgineServiceName);
    ContainerProcessEngineController processEngineController = serviceController.getValue();
    return processEngineController.getProcessEngine();
  }
  
  @SuppressWarnings("unchecked")
  private ServiceController<ContainerProcessEngineController> getProcessEngineServiceController(ServiceName processEgineServiceName) {
    ServiceController<ContainerProcessEngineController> serviceController = (ServiceController<ContainerProcessEngineController>) serviceContainer.getRequiredService(processEgineServiceName);
    return serviceController;
  }
  
  public void registerProcessEngine(ProcessEngine engine) {
    processEngines.add(engine);
  }
  
  public void removeProcessEngine(ProcessEngine engine) {
    processEngines.remove(engine);
  }

}
