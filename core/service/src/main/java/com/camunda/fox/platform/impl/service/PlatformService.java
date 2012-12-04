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
    return installProcessArchiveInternal(processArchive);
  }
  
  public synchronized ProcessArchiveInstallation installProcessArchiveInternal(ProcessArchive processArchive) {
    String paName = processArchive.getName();
    if (paName == null) {
      throw new FoxPlatformException("Cannot install process archive: name is null");
    }
    
    // check whether a process archive with the same name is already installed
    ProcessEngineController processEngineController = processEnginesByProcessArchiveName.get(paName);
    if (processEngineController != null) {
      throw new FoxPlatformException("Cannot install process archive with name '" + paName
              + "': process archive with same name already installed to process engine '" + processEngineController.getProcessEngineName() + "'.");
    }
    
    // get the process engine controller for this installation.
    processEngineController = getProcessEngineSerivce(processArchive);
    
    // start the installation process
    try {
      fireBeforeProcessArchiveInstalled(processArchive, processEngineController);
   
      ProcessEngine processEngine = processEngineController.installProcessArchive(processArchive);

      Deployment deployment = processEngineController.getProcessArchiveContextByName(paName).getActivitiDeployment();

      String deploymentId = null;
      if (deployment != null) {
        deploymentId = deployment.getId();
      }

      processEnginesByProcessArchiveName.put(paName, processEngineController);
      
      fireAfterProcessArchiveInstalled(processArchive, processEngineController, deploymentId);

      return new ProcessArchiveInstallationImpl(processEngine, deploymentId);

    } catch (Exception e) {     
      processEnginesByProcessArchiveName.remove(paName);           
      processEngineController.unInstallProcessArchive(processArchive);
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
    unInstallProcessArchiveInternal(processArchiveName);
  }
  
  public synchronized void unInstallProcessArchiveInternal(String paName) {
    try {
      final ProcessEngineController processEngine = processEnginesByProcessArchiveName.get(paName);
  
      if (processEngine == null) {
        throw new FoxPlatformException("Cannot uninstall process archive with name '" + paName + "': no such process archive");
  
      } else {
        final ProcessArchiveContext processArchiveContext = processEngine.getProcessArchiveContextByName(paName);

        fireBeforeProcessArchiveUninstalled(processArchiveContext.getProcessArchive(), processEngine);
               
        // perform the uninstall operation
        processEngine.unInstallProcessArchive(processArchiveContext.getProcessArchive());
          
        fireAfterProcessArchiveUninstalled(processArchiveContext.getProcessArchive(), processEngine);
      }
    } catch (Throwable t) {
      log.log(Level.WARNING, "Exception while uninstalling process archive '"+paName+"'", t);
    
    } finally {      
      // always remove reference
      processEnginesByProcessArchiveName.remove(paName);
    
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
        throw new FoxPlatformException("Cannot retreive list of process archives for process engine: process engine with name '"+processEngineName+"' is not managed by the fox platform.");
      }    
      return new ArrayList<ProcessArchive>(processEngineService.getCachedProcessArchives());
    }
  }
  
  public ProcessArchive getProcessArchiveByProcessDefinitionId(String processDefinitionId, String processEngineName) {
    if(processEngineName == null) {
      throw new FoxPlatformException("Cannot retreive process archive for process engine: process engine name is null.");
    } else {
      final ProcessEngineController processEngineService = processEngineRegistry.getProcessEngineController(processEngineName); 
      if(processEngineService == null) {
        throw new FoxPlatformException("Cannot retreive list of process archives for process engine: process engine with name '"+processEngineName+"' is not managed by the fox platform.");
      }    
      return processEngineService.getProcessArchiveByProcessDefinitionId(processDefinitionId);
    }
  }
  
  public ProcessArchive getProcessArchiveByProcessDefinitionKey(String processDefinitionKey, String processEngineName) {
    if(processEngineName == null) {
      throw new FoxPlatformException("Cannot retreive process archive for process engine: process engine name is null.");
    } else {
      final ProcessEngineController processEngineService = processEngineRegistry.getProcessEngineController(processEngineName); 
      if(processEngineService == null) {
        throw new FoxPlatformException("Cannot retreive list of process archives for process engine: process engine with name '"+processEngineName+"' is not managed by the fox platform.");
      }    
      return processEngineService.getProcessArchiveByProcessDefinitionKey(processDefinitionKey);
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
        log.log(Level.SEVERE, "Exception while invoking 'fireBeforeProcessArchiveUninstalled' for PlatformServiceExtension "+platformServiceExtension.getClass(), e);
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
