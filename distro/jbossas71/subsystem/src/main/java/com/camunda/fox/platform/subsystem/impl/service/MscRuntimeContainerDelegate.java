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
import java.util.concurrent.CopyOnWriteArraySet;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.container.RuntimeContainerDelegate;
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
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import com.camunda.fox.platform.subsystem.impl.util.PlatformServiceReferenceFactory;
import com.camunda.fox.platform.subsystem.impl.util.ServiceTracker;

/**
 * <p>A {@link RuntimeContainerDelegate} implementation for JBoss AS 7</p>
 * 
 * @author Daniel Meyer
 */
public class MscRuntimeContainerDelegate implements Service<MscRuntimeContainerDelegate>, RuntimeContainerDelegate, ProcessEngineService, ProcessApplicationService {

  // used for installing services
  protected ServiceTarget childTarget;
  // used for looking up services
  protected ServiceContainer serviceContainer;
  
  protected ServiceTracker<ProcessEngine> processEngineServiceTracker;
  protected Set<ProcessEngine> processEngines = new CopyOnWriteArraySet<ProcessEngine>();
  
  protected ServiceTracker<ProcessApplicationInfo> processApplicationServiceTracker; 
  protected Set<ProcessApplicationInfo> processApplications = new CopyOnWriteArraySet<ProcessApplicationInfo>();
          
  // Lifecycle /////////////////////////////////////////////////

  public void start(StartContext context) throws StartException {
    serviceContainer = context.getController().getServiceContainer();
    childTarget = context.getChildTarget();
    
    startTrackingServices();
    createJndiBindings();
    
    // set this implementation as Runtime Container
    RuntimeContainerDelegate.INSTANCE.set(this);
  }

  public void stop(StopContext context) {
    stopTrackingServices();
  }
       
  public MscRuntimeContainerDelegate getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  // RuntimeContainerDelegate implementation /////////////////////////////
  
  public void registerProcessEngine(ProcessEngine processEngine) {
    
    if(processEngine == null) {
      throw new ActivitiException("Cannot register process engine with Msc Runtime Container: process engine is 'null'");
    }
    
    ServiceName serviceName = ServiceNames.forManagedProcessEngine(processEngine.getName());
    
    if(serviceContainer.getService(serviceName) == null) {
      MscManagedProcessEngine processEngineRegistration = new MscManagedProcessEngine(processEngine);
      
      // install the service asynchronously. 
      childTarget.addService(serviceName, processEngineRegistration)
        .setInitialMode(Mode.ACTIVE)
        .install();    
    }
    
  }
  
  @SuppressWarnings("unchecked")
  public void unregisterProcessEngine(ProcessEngine processEngine) {
    
    if(processEngine == null) {
      throw new ActivitiException("Cannot unregister process engine with Msc Runtime Container: process engine is 'null'");
    }
    
    ServiceName serviceName = ServiceNames.forManagedProcessEngine(processEngine.getName());
    
    // remove the service asynchronously
    ServiceController<ProcessEngine> service = (ServiceController<ProcessEngine>) serviceContainer.getService(serviceName);
    if(service != null && service.getService() instanceof MscManagedProcessEngine) {
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
  
  public ProcessEngineService getProcessEngineService() {
    // TODO: return proxy?
    return this;
  }

  public ProcessApplicationService getProcessApplicationService() {
    // TODO: return proxy?
    return this;
  }
  
  // ProcessEngineService implementation /////////////////////////////////
  
  public ProcessEngine getDefaultProcessEngine() {
    return getProcessEngineService(ServiceNames.forDefaultProcessEngine());
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
    return getProcessEngineService(ServiceNames.forManagedProcessEngine(name));
  }
  
  // ProcessApplicationService implementation //////////////////////////////
  
  @SuppressWarnings("unchecked")
  public ProcessApplicationInfo getProcessApplicationInfo(String processApplicationName) {
    ServiceController<ProcessApplicationInfo> serviceController = (ServiceController<ProcessApplicationInfo>) serviceContainer.getService(ServiceNames.forManagedProcessApplication(processApplicationName));
    if(serviceController == null) {
      return null;
    } else {
      return serviceController.getValue();
    }
  }
  
  public Set<String> getProcessApplicationNames() {
    HashSet<String> result = new HashSet<String>();
    for (ProcessApplicationInfo application : processApplications) {
      result.add(application.getName());
    }
    return result;
  }
  
  // internal implementation ///////////////////////////////
 
  protected void createJndiBindings() {
    
    final String prefix = "java:global/camunda-fox-platform/";
    final String processEngineServiceSuffix = "DefaultProcessEngineService!com.camunda.fox.platform.api.ProcessEngineService";    
    
    String moduleName = "process-engine";
    
    final String processEngineServiceBindingName = prefix + moduleName + "/"+ processEngineServiceSuffix;
        
    final ServiceName processEngineServiceBindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME            
      .append("camunda-fox-platform")
      .append(moduleName)
      .append(processEngineServiceSuffix);
    
    BinderService processEngineServiceBinder = new BinderService(processEngineServiceBindingName);
    ServiceBuilder<ManagedReferenceFactory> processEngineServiceBuilder = childTarget
            .addService(processEngineServiceBindingServiceName, processEngineServiceBinder)
            .addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, processEngineServiceBinder.getNamingStoreInjector());
    processEngineServiceBinder.getManagedObjectInjector().inject(new PlatformServiceReferenceFactory(this));
            
    processEngineServiceBuilder.install();
    
  }
  
  protected ProcessEngine getProcessEngineService(ServiceName processEgineServiceName) {
    ServiceController<ProcessEngine> serviceController = getProcessEngineServiceController(processEgineServiceName);
    return serviceController.getValue();
  }
  
  @SuppressWarnings("unchecked")
  protected ServiceController<ProcessEngine> getProcessEngineServiceController(ServiceName processEgineServiceName) {
    ServiceController<ProcessEngine> serviceController = (ServiceController<ProcessEngine>) serviceContainer.getRequiredService(processEgineServiceName);
    return serviceController;
  }
  
  protected void startTrackingServices() {
    processEngineServiceTracker = new ServiceTracker<ProcessEngine>(ServiceNames.forManagedProcessEngines(), processEngines);
    serviceContainer.addListener(processEngineServiceTracker);
    
    processApplicationServiceTracker = new ServiceTracker<ProcessApplicationInfo>(ServiceNames.forManagedProcessApplications(), processApplications);
    serviceContainer.addListener(processApplicationServiceTracker);
  }

  protected void stopTrackingServices() {
    serviceContainer.removeListener(processEngineServiceTracker);    
  }
  
}
