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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.transaction.TransactionManager;

import org.jboss.as.connector.subsystems.datasources.DataSourceReferenceFactoryService;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceController.Substate;
import org.jboss.msc.service.ServiceController.Transition;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.service.PlatformService;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;
import com.camunda.fox.platform.subsystem.impl.util.PlatformServiceReferenceFactory;
import com.camunda.fox.platform.subsystem.impl.util.ServiceListenerFuture;

/**
 * @author Daniel Meyer
 */
public class ContainerPlatformService extends PlatformService implements Service<ContainerPlatformService> {
  
 private static Logger log = Logger.getLogger(ContainerPlatformService.class.getName());
  
  // state ///////////////////////////////////////////
  
  // jndi bindings
  protected ServiceController<ManagedReferenceFactory> processArchiveServiceBinding;
  protected ServiceController<ManagedReferenceFactory> processEngineServiceBinding;

  private ServiceContainer serviceContainer;
        
  ///////////////////////////////////////////////////

  @Override
  public void start(StartContext context) throws StartException {
    start();
    serviceContainer = context.getController().getServiceContainer();
    createJndiBindings(context);
  }
   

  @Override
  public void stop(StopContext context) {
    stop();
    removeJndiBindings();
  }
  
  
  protected void createJndiBindings(StartContext context) {
    
    final String prefix = "java:global/camunda-fox-platform/";
    final String processArchiveServiceSuffix = "PlatformService!com.camunda.fox.platform.api.ProcessArchiveService";
    final String processEngineServiceSuffix = "PlatformService!com.camunda.fox.platform.api.ProcessEngineService";    
    
    String moduleName = "process-engine";
    
    final String processArchiveServiceBindingName = prefix + moduleName + "/"+ processArchiveServiceSuffix;
    final String processEngineServiceBindingName = prefix + moduleName + "/"+ processEngineServiceSuffix;
        
    final ServiceName processEngineServiceBindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME            
      .append("camunda-fox-platform")
      .append(moduleName)
      .append(processEngineServiceSuffix);
    
    final ServiceName processArchiveServiceBindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME
      .append("camunda-fox-platform")
      .append(moduleName)
      .append(processArchiveServiceSuffix);
    
    final ServiceContainer serviceContainer = context.getController().getServiceContainer();
    
    BinderService processEngineServiceBinder = new BinderService(processEngineServiceBindingName);
    ServiceBuilder<ManagedReferenceFactory> processEngineServiceBuilder = serviceContainer
            .addService(processEngineServiceBindingServiceName, processEngineServiceBinder);
    processEngineServiceBuilder.addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, processEngineServiceBinder.getNamingStoreInjector());
    processEngineServiceBinder.getManagedObjectInjector().inject(new PlatformServiceReferenceFactory(this));
    
    BinderService processArchiveServiceBinder = new BinderService(processEngineServiceBindingName);
    ServiceBuilder<ManagedReferenceFactory> processArchiveServiceBuilder = serviceContainer.addService(processArchiveServiceBindingServiceName, processArchiveServiceBinder);
    processArchiveServiceBuilder.addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, processArchiveServiceBinder.getNamingStoreInjector());
    processArchiveServiceBinder.getManagedObjectInjector().inject(new PlatformServiceReferenceFactory(this));
        
    processEngineServiceBinding = processEngineServiceBuilder.install();
    processArchiveServiceBinding = processArchiveServiceBuilder.install();
    
    log.info("the bindings for the fox platform services are as follows: \n\n"
      + "        "
      + processArchiveServiceBindingName + "\n" 
      + "        " 
      + processEngineServiceBindingName + "\n");
  }

  protected void removeJndiBindings() {
    processEngineServiceBinding.setMode(Mode.REMOVE);
    processArchiveServiceBinding.setMode(Mode.REMOVE);
  }

  public Future<ProcessEngineStartOperation> startProcessEngine(ProcessEngineConfiguration processEngineConfiguration) {    
  
    final ContainerProcessEngineController processEngineController = new ContainerProcessEngineController(processEngineConfiguration);    
    final ContextNames.BindInfo datasourceBindInfo = ContextNames.bindInfoFor(processEngineConfiguration.getDatasourceJndiName());
   
    final ServiceListenerFuture<ContainerProcessEngineController, ProcessEngineStartOperation> listener = new ServiceListenerFuture<ContainerProcessEngineController, ProcessEngineStartOperation>(processEngineController) {
      protected void serviceAvailable() {
        this.value = new ProcessEngineStartOperationImpl(serviceInstance.getProcessEngine());
      }      
    };
    
    serviceContainer.addService(ContainerProcessEngineController.createServiceName(processEngineConfiguration.getProcessEngineName()), processEngineController)
      .addDependency(ServiceName.JBOSS.append("txn").append("TransactionManager"), TransactionManager.class, processEngineController.getTransactionManagerInjector())
      .addDependency(datasourceBindInfo.getBinderServiceName(), DataSourceReferenceFactoryService.class, processEngineController.getDatasourceBinderServiceInjector())
      .addDependency(getServiceName(), ContainerPlatformService.class, processEngineController.getContainerPlatformServiceInjector())
      .addDependency(ContainerJobExecutorService.getServiceName(), ContainerJobExecutorService.class, processEngineController.getContainerJobExecutorInjector())
      .setInitialMode(Mode.ACTIVE)
      .addListener(listener)
      .install();
    
    return listener;
  }
  
  @SuppressWarnings("unchecked")
  public void stopProcessEngine(String name) {
    if(!processEngineRegistry.getProcessEngineNames().contains(name)) {
      throw new FoxPlatformException("Cannot stop process engine '"+name+"': no such process engine found");
    } else {
      
      final ServiceName createServiceName = ContainerProcessEngineController.createServiceName(name);
      final ServiceController<ContainerProcessEngineController> service = (ServiceController<ContainerProcessEngineController>) serviceContainer.getService(createServiceName);
      
      final Object shutdownMonitor = new Object();      
      service.addListener(new AbstractServiceListener<ContainerProcessEngineController>() {
        public void transition(ServiceController< ? extends ContainerProcessEngineController> controller, Transition transition) {
          if(isDown(transition.getAfter())) {
            synchronized (shutdownMonitor) {
              shutdownMonitor.notifyAll();              
            }
          }
        }        
      });
      
      synchronized (shutdownMonitor) {
        service.setMode(Mode.REMOVE);
        if (!isDown(service.getSubstate())) {
          try {
            // block until the service is down (give up after 2 minutes):
            shutdownMonitor.wait(2 * 60 * 1000);
          } catch (InterruptedException e) {
            throw new FoxPlatformException("Interrupted while waiting for process engine '" + name + "' to shut down.");
          }
        }
      }
      
    } 
  }
  
  public ProcessArchiveInstallation installProcessArchive(ProcessArchive processArchive) {
    
    // the ProcessArchiveService is started asynchronously but we make it appear sychronously here.
    
      ProcessArchiveService processArchiveService = new ProcessArchiveService(processArchive);
      ServiceName serviceName = ProcessArchiveService.getServiceName(processArchive.getName());
      
      final ServiceListenerFuture<ProcessArchiveService, ProcessArchiveInstallation> listener = new ServiceListenerFuture<ProcessArchiveService, ProcessArchiveInstallation>(processArchiveService) {
        protected void serviceAvailable() {
          this.value = serviceInstance.getProcessArchiveInstallation();
        }      
      };
      
      String processEngineName = processArchive.getProcessEngineName();   
      ServiceName processEngineServiceName = ContainerProcessEngineController.createServiceName(processEngineName);
      
      serviceContainer.addService(serviceName, processArchiveService)
        .addDependency(processEngineServiceName, ContainerProcessEngineController.class, processArchiveService.getProcessEngineControllerInjector())
        .addDependency(ContainerPlatformService.getServiceName(), ContainerPlatformService.class, processArchiveService.getContainerPlatformServiceInjector())
        .setInitialMode(Mode.ACTIVE)
        .addListener(listener)
        .install();
      
      try {
        
        ProcessArchiveInstallation processArchiveInstallation = listener.get();
        FoxPlatformException exception = processArchiveService.getException();
        if(exception != null) {
          serviceContainer.getService(serviceName).setMode(Mode.REMOVE);
          throw exception;
        
        } else {
          return processArchiveInstallation;
          
        }
        
      } catch (InterruptedException e) {
        throw new FoxPlatformException("Interrupted while waiting for Process archive installation", e);
      } catch (ExecutionException e) {
        throw new FoxPlatformException("Exception while waiting for Process archive installation", e);
      } 
      
  }
  
  @SuppressWarnings("unchecked")
  public void unInstallProcessArchive(String processArchiveName) {
    
    unInstallProcessArchiveInternal(processArchiveName);

    // remove the process archive service asynchronously.
    ServiceName serviceName = ProcessArchiveService.getServiceName(processArchiveName);
    ServiceController<ProcessArchiveService> service = (ServiceController<ProcessArchiveService>) serviceContainer.getService(serviceName);
    service.setMode(Mode.REMOVE);    
  }
   
  private boolean isDown(Substate state) {
    return state.equals(Substate.DOWN)||state.equals(Substate.REMOVED);
  }

  public ContainerPlatformService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }
  
  public static ServiceName getServiceName() {
    return ServiceName.of("foxPlatform", "platformService");
  }

  public ProcessArchiveContext getProcessArchiveContext(String processArchiveName, String processEngineName) {
    ContainerProcessEngineController processEngineController = (ContainerProcessEngineController) processEngineRegistry.getProcessEngineController(processEngineName);
    
    if(processEngineController == null) {      
      return null;
    }
    
    return processEngineController.getProcessArchiveContextByName(processArchiveName);
    
  }


  public void removeProcessArchive(ProcessArchive processArchive) {
    // only uninstall the process archive if it is currently installed
    if(processEnginesByProcessArchiveName.get(processArchive.getName())!= null) {
      unInstallProcessArchiveInternal(processArchive.getName());
    }
    
  }

}
