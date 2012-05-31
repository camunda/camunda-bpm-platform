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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.service.spi.PlatformServiceExtension;
import com.camunda.fox.platform.impl.util.PlatformServiceExtensionHelper;
import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * <p>Abstract implementation of the {@link ProcessEngineService}
 * and {@link ProcessArchiveService}</p>
 * 
 * @author Daniel Meyer
 */
public abstract class PlatformService implements ProcessEngineService, ProcessArchiveService {
  
  private static Logger log = Logger.getLogger(PlatformService.class.getName());
    
  // state //////////////////////////////////////////////////////////////////////////
    
  protected Map<String, ProcessEngineController> processEnginesByProcessArchiveName = new HashMap<String, ProcessEngineController>();
  protected final ProcessEngineRegistry processEngineRegistry = new ProcessEngineRegistry();
  
  // Lifecycle ///////////////////////////////////////////////////////////////////////
  
  public void start() {
    fireOnPlatformServiceStart();
  }

  public void stop() {
    fireOnPlatformServiceStop();
  }
  
  // ProcessEngineService implementation /////////////////////////////////////////////
  
  public ProcessEngine getDefaultProcessEngine() {
    return processEngineRegistry.getDefaultProcessEngine();
  }
  
  public ProcessEngine getProcessEngine(String name) {    
    return processEngineRegistry.getProcessEngine(name);
  }
  
  public List<String> getProcessEngineNames() {
    return processEngineRegistry.getProcessEngineNames();
  }
  
  public List<ProcessEngine> getProcessEngines() {
    return processEngineRegistry.getProcessEngines();
  }
    
  public void stopProcessEngine(final ProcessEngine processEngine) {
    if(processEngine == null) {
      throw new FoxPlatformException("ProcessEngine is null");
    }
    stopProcessEngine(processEngine.getName());
  }
  
    
  // ProcessArchiveService implementation ////////////////////////////////////////////
  
  public ProcessArchiveInstallation installProcessArchive(ProcessArchive processArchive) {
    if (processArchive.getName() == null) {
      throw new FoxPlatformException("Cannot install process archive: name is null");
    }

    final ProcessEngineController processEngine = getProcessEngineSerivce(processArchive);
    
    fireBeforeProcessArchiveInstalled(processArchive, processEngine);

    synchronized (this) {
      ProcessEngineController platformProcessEngine = processEnginesByProcessArchiveName.get(processArchive.getName());
      if (platformProcessEngine != null) {
        throw new FoxPlatformException("Cannot install process archive with name '" + processArchive.getName()
                + "': process archive with same name already installed to process engine '" + platformProcessEngine.getProcessEngineName() + "'.");
      } else {
        processEnginesByProcessArchiveName.put(processArchive.getName(), processEngine);
      }
    }

    try {

      final ProcessEngine processEngineHandle = processEngine.installProcessArchive(processArchive);

      final Deployment deployment = processEngine.getInstalledProcessArchivesByName().get(processArchive.getName()).getActivitiDeployment();

      String deploymentId = null;
      if (deployment != null) {
        deploymentId = deployment.getId();
      }

      processEnginesByProcessArchiveName.put(processArchive.getName(), processEngine);
      
      fireAfterProcessArchiveInstalled(processArchive, processEngine, deploymentId);

      return new ProcessArchiveInstallationImpl(processEngineHandle, deploymentId);

    } catch (Exception e) {
      synchronized (this) {
        processEnginesByProcessArchiveName.remove(processArchive.getName());        
      }
      throw new FoxPlatformException("Exception while intalling process archive",e);
    }
  }

  protected ProcessEngineController getProcessEngineSerivce(ProcessArchive processArchive) {
    final String processEngineName = processArchive.getProcessEngineName();
    if(processEngineName == null) {
      ProcessEngine defaultProcessEngine = getDefaultProcessEngine();      
      if(defaultProcessEngine == null) {
        throw new FoxPlatformException("Cannot determine process engine for process archive '" + processArchive.getName()
                + "': specified process engine name is null and there is no default process engine defined.");
      } else {
        return processEngineRegistry.getProcessEngineController(defaultProcessEngine.getName());
      }      
    } else {
      ProcessEngineController processEngineController = processEngineRegistry.getProcessEngineController(processEngineName); 
      if(processEngineController == null) {
        throw new FoxPlatformException("Cannot determine process engine for process archive '" + processArchive.getName()
                + "': specified process engine with name '"+processEngineName+"' does not exist.");
      } else {
        return processEngineController;
      }
    }
    
  }

  public void unInstallProcessArchive(String processArchiveName) {

    final ProcessEngineController processEngine = processEnginesByProcessArchiveName.get(processArchiveName);

    if (processEngine == null) {
      throw new FoxPlatformException("Cannot uninstall process archive with name '" + processArchiveName + "': no such process archive");

    } else {
      final ProcessArchiveContext processArchiveContext = processEngine
              .getInstalledProcessArchivesByName()
              .get(processArchiveName);
      
      fireBeforeProcessArchiveUninstalled(processArchiveContext.getProcessArchive(), processEngine);
      
      processEngine.unInstallProcessArchive(processArchiveContext.getProcessArchive());

      processEnginesByProcessArchiveName.remove(processArchiveName);
      
      fireAfterProcessArchiveUninstalled(processArchiveContext.getProcessArchive(), processEngine);
    }

  }

  public List<ProcessArchive> getInstalledProcessArchives() {
    final List<ProcessArchive> installedProcessArchives = new ArrayList<ProcessArchive>();
    for (ProcessEngineController processEngineService : processEngineRegistry.getProcessEngineControllers()) {
      installedProcessArchives.addAll(processEngineService.getCachedProcessArchives());      
    }
    return installedProcessArchives;
  }

  public List<ProcessArchive> getInstalledProcessArchives(ProcessEngine processEngine) {
    if(processEngine == null) {
      throw new FoxPlatformException("Cannot retreive process archives for process engine: process engine is null.");
    }
    return getInstalledProcessArchives(processEngine.getName());
  }

  public List<ProcessArchive> getInstalledProcessArchives(String processEngineName) {
    if(processEngineName == null) {
      throw new FoxPlatformException("Cannot retreive process archives for process engine: process engine name is null.");
    } else {
      final ProcessEngineController processEngineService = processEngineRegistry.getProcessEngineController(processEngineName); 
      if(processEngineService == null) {
        throw new FoxPlatformException("Cannot retreive list of process archives fot process engine: process engine with name '"+processEngineName+"' is not managed by the fox platform.");
      }    
      return processEngineService.getCachedProcessArchives();
    }
  }
  
  public void unInstallProcessArchive(ProcessArchive processArchive) {
    if(processArchive == null) {
      throw new FoxPlatformException("Cannot uninstall process archive: process archive is null");
    }
    unInstallProcessArchive(processArchive.getName());
  }
  
  public ProcessEngineRegistry getProcessEngineRegistry() {
    return processEngineRegistry;
  }
  
  // operation impl /////////////////////////////////////////////////

  public static class ProcessEngineStartOperationImpl implements ProcessEngineStartOperation {

    final Throwable exception;
    final ProcessEngine processEngine;

    public ProcessEngineStartOperationImpl(ProcessEngine processEngine) {
      this.processEngine = processEngine;
      exception = null;
    }

    public ProcessEngineStartOperationImpl(Throwable exception) {
      this.exception = exception;
      processEngine = null;
    }

    public boolean wasSuccessful() {
      return exception == null;
    }
    
    public Throwable getException() {
      return exception;
    }
    
    public ProcessEngine getProcessenEngine() {
      return processEngine;
    }
  }
  
  public static class ProcessArchiveInstallationImpl implements ProcessArchiveInstallation {
    
    private final ProcessEngine processEngine;
    private final String processEngineDeploymentId;

    public ProcessArchiveInstallationImpl(ProcessEngine processEngine, String processEngineDeploymentId) {
      this.processEngine = processEngine;
      this.processEngineDeploymentId = processEngineDeploymentId;
    }

    public ProcessEngine getProcessEngine() {
      return processEngine;
    }

    public String getProcessEngineDeploymentId() {
      return processEngineDeploymentId;
    }
  }
  
  // extensions support //////////////////////////////////////////////////////
  

  protected void fireOnPlatformServiceStart() {
    PlatformServiceExtensionHelper.clearCachedExtensions();
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {
      try {
        platformServiceExtension.onPlatformServiceStart(this);
      }catch (Exception e) {
        throw new FoxPlatformException("Exception while invoking 'onPlatformServiceStart' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }    
  }  
  
  protected void fireOnPlatformServiceStop() {
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    Collections.reverse(loadableExtensions);
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {
      try {
        platformServiceExtension.onPlatformServiceStop(this);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while invoking 'onPlatformServiceStop' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }
    PlatformServiceExtensionHelper.clearCachedExtensions();
  }
  
  protected void fireBeforeProcessArchiveInstalled(ProcessArchive processArchive, ProcessEngineController processEngine) {
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {
      try {
        platformServiceExtension.beforeProcessArchiveInstalled(processArchive, processEngine);
      }catch (Exception e) {
        throw new FoxPlatformException("Exception while invoking 'beforeProcessArchiveInstalled' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }
  }
  
  protected void fireAfterProcessArchiveInstalled(ProcessArchive processArchive, ProcessEngineController processEngine, String deploymentId) {
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {
      try {
        platformServiceExtension.afterProcessArchiveInstalled(processArchive, processEngine, deploymentId);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while invoking 'afterProcessArchiveInstalled' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }
  }
  

  protected void fireBeforeProcessArchiveUninstalled(ProcessArchive processArchive, ProcessEngineController processEngine) {
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    Collections.reverse(loadableExtensions);
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {
      try {
        platformServiceExtension.beforeProcessArchiveUninstalled(processArchive, processEngine);
      }catch (Exception e) {
        throw new FoxPlatformException("Exception while invoking 'beforeProcessArchiveUninstalled' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }
  }
  
  protected void fireAfterProcessArchiveUninstalled(ProcessArchive processArchive, ProcessEngineController processEngine) {
    List<PlatformServiceExtension> loadableExtensions = PlatformServiceExtensionHelper.getLoadableExtensions();
    Collections.reverse(loadableExtensions);
    for (PlatformServiceExtension platformServiceExtension : loadableExtensions) {
      try {
        platformServiceExtension.afterProcessArchiveUninstalled(processArchive, processEngine);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while invoking 'afterProcessArchiveUninstalled' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
      }
    }
  }

    
}
