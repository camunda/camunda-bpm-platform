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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.container.impl.RuntimeContainerConfiguration;
import org.camunda.bpm.container.spi.RuntimeContainerDelegate;
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

import com.camunda.fox.platform.jobexecutor.impl.acquisition.JobAcquisition;
import com.camunda.fox.platform.subsystem.impl.util.PlatformServiceReferenceFactory;
import com.camunda.fox.platform.subsystem.impl.util.ServiceTracker;

/**
 * <p>A {@link RuntimeContainerDelegate} implementation for JBoss AS 7</p>
 * 
 * @author Daniel Meyer
 */
public class MscRuntimeContainerDelegate implements Service<MscRuntimeContainerDelegate>, RuntimeContainerDelegate, ProcessEngineService, ProcessApplicationService {
  
  // jndi bindings
  protected ServiceController<ManagedReferenceFactory> processEngineServiceBinding;

  protected ServiceContainer serviceContainer;  
  protected List<ProcessEngine> processEngines = new CopyOnWriteArrayList<ProcessEngine>();
  protected ServiceTracker<ProcessEngine> processEngineServiceTracker;
          
  // Lifecycle /////////////////////////////////////////////////

  public void start(StartContext context) throws StartException {
    serviceContainer = context.getController().getServiceContainer();
    addServiceTrackers(serviceContainer);
    createJndiBindings(context);
    
    RuntimeContainerConfiguration containerConfiguration = RuntimeContainerConfiguration.getInstance();
    containerConfiguration.setContainerDelegate(this);
    containerConfiguration.setRuntimeContainerName("JBoss AS 7");
  }

  public void stop(StopContext context) {
    removeServiceTrackers(serviceContainer);
    removeJndiBindings();
  }

  protected void addServiceTrackers(ServiceContainer serviceContainer) {
    ServiceName processEngineServiceType = ManagedProcessEngineController.serviceTypeName();
    processEngineServiceTracker = new ServiceTracker<ProcessEngine>(processEngineServiceType, processEngines);
    serviceContainer.addListener(processEngineServiceTracker);
  }
  

  protected void removeServiceTrackers(ServiceContainer serviceContainer) {
    serviceContainer.removeListener(processEngineServiceTracker);    
  }
       
  public MscRuntimeContainerDelegate getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  // RuntimeContainerDelegate implementation /////////////////////////////
  
  public void registerProcessEngine(ProcessEngine processEngine) {
    
    ServiceName serviceName = ManagedProcessEngineController.createServiceName(processEngine.getName());
    
    if(serviceContainer.getService(serviceName) == null) {
      ManagedProcessEngineRegistration processEngineRegistration = new ManagedProcessEngineRegistration(processEngine);
      
      // install the service asynchronously. 
      serviceContainer.addService(serviceName, processEngineRegistration)
        .setInitialMode(Mode.ACTIVE)
        .install();    
    }
    
  }
  
  @SuppressWarnings("unchecked")
  public void unregisterProcessEngine(ProcessEngine processEngine) {
    
    ServiceName serviceName = ManagedProcessEngineController.createServiceName(processEngine.getName());
    
    // remove the service asynchronously
    ServiceController<ProcessEngine> service = (ServiceController<ProcessEngine>) serviceContainer.getService(serviceName);
    if(service != null && service.getService() instanceof ManagedProcessEngineRegistration) {
      service.setMode(Mode.REMOVE);
    }
        
  }
  
  public void deployProcessApplication(AbstractProcessApplication processApplication) {
    // on JBoss AS 7, process applications are deployed using the deployment processors subsystem at application deployment time. 
    // a subsequent call to deployProcessApplication() can thus be ignored, if the application is already registered. 
  }
  
  public void undeployProcessApplication(AbstractProcessApplication processApplication) {
    // on JBoss AS 7, process applications are undeployed using the deployment processors subsystem at application undeployment time. 
    // a subsequent call to undeployProcessApplication() can thus be ignored.
  }
  
  public void registerJobAcquisition(JobAcquisition jobAcquisitionConfiguration) {
    // TODO
  }
  
  public void unregisterJobAcquisition(JobAcquisition jobAcquisitionConfiguration) {
    // TODO    
  }
  
  public ProcessEngineService getProcessEngineService() {
    return this;
  }

  public ProcessApplicationService getProcessApplicationService() {
    return this;
  }
  
  // ProcessEngineService implementation /////////////////////////////////
  
  public ProcessEngine getDefaultProcessEngine() {
    ServiceName processEgineServiceName = ManagedProcessEngineController.createServiceNameForDefaultEngine();
    return getProcessEngineService(processEgineServiceName);
  }

  public List<ProcessEngine> getProcessEngines() {
    return new ArrayList<ProcessEngine>(processEngines);
  }

  public Set<String> getProcessEngineNames() {
    HashSet<String> result = new HashSet<String>();
    for (ProcessEngine engine : processEngines) {
      result.add(engine.getName());
    }
    return result;
  }

  public ProcessEngine getProcessEngine(String name) {    
    return getProcessEngineService(ManagedProcessEngineController.createServiceName(name));
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
    ServiceController<ProcessEngine> serviceController = getProcessEngineServiceController(processEgineServiceName);
    return serviceController.getValue();
  }
  
  @SuppressWarnings("unchecked")
  private ServiceController<ProcessEngine> getProcessEngineServiceController(ServiceName processEgineServiceName) {
    ServiceController<ProcessEngine> serviceController = (ServiceController<ProcessEngine>) serviceContainer.getRequiredService(processEgineServiceName);
    return serviceController;
  }
  
  public void removeProcessEngine(ProcessEngine engine) {
    processEngines.remove(engine);
  }

}
