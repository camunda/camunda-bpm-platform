package com.camunda.fox.platform.subsystem.impl.service;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService.ProcessArchiveInstallation;
import com.camunda.fox.platform.spi.ProcessArchive;


public class ProcessArchiveService implements Service<ProcessArchiveService> {
  
  private InjectedValue<ContainerProcessEngineController> processEngineControllerInjector = new InjectedValue<ContainerProcessEngineController>();
  private InjectedValue<ContainerPlatformService> containerPlatformServiceInjector = new InjectedValue<ContainerPlatformService>();
  
  private final ProcessArchive processArchive;
  
  private ProcessArchiveInstallation processArchiveInstallation;
  
  public ProcessArchiveService(ProcessArchive processArchive) {
    this.processArchive = processArchive;
  }

  public ProcessArchiveService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  public void start(StartContext arg0) throws StartException {
    ContainerPlatformService containerPlatformService = containerPlatformServiceInjector.getOptionalValue();
    processArchiveInstallation = containerPlatformService.installProcessArchiveInternal(processArchive);
  }

  public void stop(StopContext arg0) {
    ContainerPlatformService containerPlatformService = containerPlatformServiceInjector.getValue();
    containerPlatformService.removeProcessArchive(processArchive);
  } 
  
  public static ServiceName getServiceName(String processArchiveName) {
    if(processArchiveName == null) {
      throw new FoxPlatformException("The name of a process archive cannot be null");
    }
    return ContainerPlatformService.getServiceName().append("process-archive").append(processArchiveName);
              
  }
  
  public InjectedValue<ContainerProcessEngineController> getProcessEngineControllerInjector() {
    return processEngineControllerInjector;
  }
  
  public InjectedValue<ContainerPlatformService> getContainerPlatformServiceInjector() {
    return containerPlatformServiceInjector;
  }
  
  public ProcessArchiveInstallation getProcessArchiveInstallation() {
    return processArchiveInstallation;
  }
}
