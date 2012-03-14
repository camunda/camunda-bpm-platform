package com.camunda.fox.platform.subsystem.impl.platform;

import java.util.concurrent.Future;
import java.util.logging.Logger;

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

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.impl.service.PlatformService;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;
import com.camunda.fox.platform.subsystem.impl.util.PlatformServiceReferenceFactory;

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
    serviceContainer = context.getController().getServiceContainer();
    createJndiBindings(context);
  }
   

  @Override
  public void stop(StopContext context) {
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
  
    final ProcessEngineControllerService processEngineController = new ProcessEngineControllerService(processEngineConfiguration);    
    final ContextNames.BindInfo datasourceBindInfo = ContextNames.bindInfoFor(processEngineConfiguration.getDatasourceJndiName());
   
    final ServiceListenerFuture<ProcessEngineControllerService, ProcessEngineStartOperation> listener = new ServiceListenerFuture<ProcessEngineControllerService, ProcessEngineStartOperation>(processEngineController) {
      protected void serviceAvailable() {
        this.value = new ProcessEngineStartOperationImpl(serviceInstance.getProcessEngine());
      }      
    };
    
    serviceContainer.addService(ProcessEngineControllerService.createServiceName(processEngineConfiguration.getProcessEngineName()), processEngineController)
      .addDependency(ServiceName.JBOSS.append("txn").append("TransactionManager"), TransactionManager.class, processEngineController.getTransactionManagerInjector())
      .addDependency(datasourceBindInfo.getBinderServiceName(), DataSourceReferenceFactoryService.class, processEngineController.getDatasourceBinderServiceInjector())
      .addDependency(getServiceName(), ContainerPlatformService.class, processEngineController.getContainerPlatformServiceInjector()) 
      .setInitialMode(Mode.ACTIVE)
      .addListener(listener)
      .install();
    
    return listener;
  }
    
  public void stopProcessEngine(String name) {
    if(processEngineRegistry.getProcessEngineNames().contains(name)) {
      ServiceController<ProcessEngineControllerService> service = 
                (ServiceController<ProcessEngineControllerService>) serviceContainer.getService(ProcessEngineControllerService.createServiceName(name));
      service.setMode(Mode.REMOVE);
    } else {
      throw new FoxPlatformException("Cannot stop process engine '"+name+"': no such process engine found");
    }
  }

  public ContainerPlatformService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }
  
  public static ServiceName getServiceName() {
    return ServiceName.of("foxPlatform", "platformService");
  }

}
