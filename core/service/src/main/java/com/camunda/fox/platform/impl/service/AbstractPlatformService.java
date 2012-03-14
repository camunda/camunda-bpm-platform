package com.camunda.fox.platform.impl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>Abstract implementation of the {@link ProcessEngineService}
 * and {@link ProcessArchiveService}</p>
 * 
 * @author Daniel Meyer
 */
public abstract class AbstractPlatformService implements ProcessEngineService, ProcessArchiveService {
  
  private final static Logger log = Logger.getLogger(AbstractPlatformService.class.getName()); 
    
  // state //////////////////////////////////////////////////////////////////////////

  protected PlatformProcessEngine defaultProcessEngine = null;
  protected boolean startingDefaultEngine = false;
  
  protected Map<String, PlatformProcessEngine> processEnginesByName = new HashMap<String, PlatformProcessEngine>();
  protected Map<String, PlatformProcessEngine> processEnginesByProcessArchiveName = new HashMap<String, PlatformProcessEngine>();

  protected List<ProcessEngine> cachedProcessEngines = Collections.emptyList();
  protected List<String> cachedProcessEngineNames = Collections.emptyList();
  
  // ProcessEngineService implementation /////////////////////////////////////////////

  public ProcessEngine getDefaultProcessEngine() {
    if (defaultProcessEngine == null) {
      throw new FoxPlatformException("No default process engine defined.");
    }
    return defaultProcessEngine.getProcessEngine();
  }

  public List<ProcessEngine> getProcessEngines() {
    return cachedProcessEngines;
  }

  public List<String> getProcessEngineNames() {
    return cachedProcessEngineNames;
  }

  public ProcessEngine getProcessEngine(String name) {
    final PlatformProcessEngine processEngine = processEnginesByName.get(name);
    if (processEngine == null) {
      throw new FoxPlatformException("No process engine with the name '" + name + "' found.");
    }
    return processEngine.getProcessEngine();
  }

  protected ProcessEngineStartOperation doStartProcessEngine(ProcessEngineConfiguration processEngineConfiguration) {
    try {
      final boolean isDefault = processEngineConfiguration.isDefault();
      final String processEngineName = processEngineConfiguration.getProcessEngineName();
  
      synchronized (this) { 
        // do integrity checks 
    
        if (processEngineName == null) {
          throw new FoxPlatformException("Cannot start process engine: name is 'null'");
        }
        
        if(cachedProcessEngineNames.contains(processEngineName)) {
          throw new FoxPlatformException("Cannot start process engine: process engine with name '"+processEngineName+"' already started");
        } 
    
        if (isDefault) {
          if (defaultProcessEngine != null || startingDefaultEngine) {
            throw new FoxPlatformException("Cannot start process engine with name '" + processEngineName + "': Default process engine already defined. ");
          }
          startingDefaultEngine = true;
        }
        
        // add the process engine name in the synchronized block. This allows us to sync only for a short time
        // while still guaranteeing that we do not start two process engines with the same name.
        final ArrayList<String> processEngineNames = new ArrayList<String>(cachedProcessEngineNames);
        processEngineNames.add(processEngineName);      
        cachedProcessEngineNames = Collections.unmodifiableList(processEngineNames);      
      }
      
      PlatformProcessEngine processEngine = new PlatformProcessEngine();
      processEngine.setProcessEngineName(processEngineName);
      processEngine.setDatasourceJndiName(processEngineConfiguration.getDatasourceJndiName());
      processEngine.setAutoUpdateSchema(processEngineConfiguration.isAutoSchemaUpdate());
      processEngine.setHistory(processEngineConfiguration.getHistoryLevel());
      processEngine.setActivateJobExecutor(processEngineConfiguration.isActivateJobExcutor());
      
      // start the process engine
      processEngine.start();
      
      // add process engine to registry
      processEnginesByName.put(processEngineName, processEngine);
      
      final ArrayList<ProcessEngine> processEngines = new ArrayList<ProcessEngine>(cachedProcessEngines);
      processEngines.add(processEngine.getProcessEngine());
      cachedProcessEngines = Collections.unmodifiableList(processEngines);
      
      if(isDefault) {
        synchronized (this) {
          startingDefaultEngine = false;
          defaultProcessEngine = processEngine;
        }
      }
          
      return new ProcessEngineStartOperationImpl(processEngine.getProcessEngine());
    }catch (Exception e) {
      log.log(Level.SEVERE, "Exception while attempting to start process engine: "+e.getMessage(), e);
      return new ProcessEngineStartOperationImpl(e);
    }
  }

  protected synchronized ProcessEngineStopOperation doStopProcessEngine(String processEngineName) {
    try {
      // stopping of process engines is performed in a serialized fashion
  
      if (processEngineName == null) {
        throw new FoxPlatformException("Cannot stop process engine: process engine name is null.");
      }
  
      PlatformProcessEngine platformProcessEngine = processEnginesByName.get(processEngineName);
      if (platformProcessEngine == null) {
        throw new FoxPlatformException("Cannot stop process engine with name '" + processEngineName + ": no such process engine.");
      }
      
      final ProcessEngine processEngine = platformProcessEngine.getProcessEngine();
      
      // stop the engine
      platformProcessEngine.stop();
      
      processEnginesByName.remove(processEngineName);
      
      if(defaultProcessEngine != null && defaultProcessEngine.getProcessEngineName().equals(processEngineName)) {
        defaultProcessEngine = null;
      }
  
      final ArrayList<String> processEngineNames = new ArrayList<String>(cachedProcessEngineNames);
      processEngineNames.remove(processEngineName);
      cachedProcessEngineNames = Collections.unmodifiableList(processEngineNames);
      
      final ArrayList<ProcessEngine> processEngines = new ArrayList<ProcessEngine>(cachedProcessEngines);
      processEngines.remove(processEngine);
      cachedProcessEngines = Collections.unmodifiableList(processEngines);
      
      return new ProcessEngineStopOperationImpl();
    }catch (Exception e) {
      log.log(Level.SEVERE, "Exception while attempting to stop process engine: "+e.getMessage(), e);
      return new ProcessEngineStopOperationImpl(e);
    }      
  }
  
  public Future<ProcessEngineStopOperation> stopProcessEngine(final ProcessEngine processEngine) {
    if(processEngine == null) {
      throw new FoxPlatformException("ProcessEngine is null");
    }
    return stopProcessEngine(processEngine.getName());
  }
  
    
  // ProcessArchiveService implementation ////////////////////////////////////////////
  
  protected ProcessArchiveInstallOperation doInstallProcessArchive(ProcessArchive processArchive) {
    try {
      if(processArchive.getName() == null) {
        throw new FoxPlatformException("Cannot install process archive: name is null");
      }

      final PlatformProcessEngine processEngine = getProcessEngineSerivce(processArchive);
      
      synchronized (this) {
        PlatformProcessEngine platformProcessEngine = processEnginesByProcessArchiveName.get(processArchive.getName());
        if(platformProcessEngine != null) {
          throw new FoxPlatformException("Cannot install process archive with name '" + processArchive.getName()
                  + "': process archive with same name already installed to process engine '" + platformProcessEngine.getProcessEngineName() + "'.");
        } else {
          processEnginesByProcessArchiveName.put(processArchive.getName(), processEngine);
        }
      }
      
      try {
      
        final ProcessEngine processEngineHandle = processEngine.installProcessArchive(processArchive);
        
        final Deployment deployment = processEngine
          .getInstalledProcessArchivesByName()
          .get(processArchive.getName())
          .getActivitiDeployment();
        
        String deploymentId = null;      
        if(deployment != null) {
          deploymentId = deployment.getId();
        }
        
        processEnginesByProcessArchiveName.put(processArchive.getName(), processEngine);
        
        return new ProcessArchiveInstallOperationImpl(processEngineHandle, deploymentId);
      
      } catch (Exception e) {
        processEnginesByProcessArchiveName.remove(processArchive.getName());
        throw e;
      }      
    } catch (Exception e) {      
      log.log(Level.SEVERE, "Could not install process archive: "+e.getMessage(), e);      
      return new ProcessArchiveInstallOperationImpl(e);
    }
  }

  protected PlatformProcessEngine getProcessEngineSerivce(ProcessArchive processArchive) {
    final String processEngineName = processArchive.getProcessEngineName();
    if(processEngineName == null) {
      if(defaultProcessEngine == null) {
        throw new FoxPlatformException("Cannot determine process engine for process archive '" + processArchive.getName()
                + "': specified process engine name is null and there is no default process engine defined.");
      } else {
        return defaultProcessEngine;
      }      
    } else {
      PlatformProcessEngine processEngine = processEnginesByName.get(processEngineName); 
      if(processEngine == null) {
        throw new FoxPlatformException("Cannot determine process engine for process archive '" + processArchive.getName()
                + "': specified process engine with name '"+processEngineName+"' does not exist.");
      } else {
        return processEngine;
      }
    }
    
  }

  public ProcessArchiveUninstallOperation doUnInstallProcessArchive(String processArchiveName) {      
    try {
      
      final PlatformProcessEngine processEngine = processEnginesByProcessArchiveName.get(processArchiveName);
      
      if(processEngine == null) {
        throw new FoxPlatformException("Cannot uninstall process archive with name '"+processArchiveName+"': no such process archive");        
        
      } else {        
        final ProcessArchiveContext processArchiveContext = processEngine
          .getInstalledProcessArchivesByName()
          .get(processArchiveName);
        
        processEngine.unInstallProcessArchive(processArchiveContext.getProcessArchive());
        
        processEnginesByProcessArchiveName.remove(processArchiveName);
      }  
      
      return new ProcessArchiveUninstallOperationImpl();      
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not uninstall process archive: "+e.getMessage(), e);      
      return new ProcessArchiveUninstallOperationImpl(e);
    }   
  }

  public List<ProcessArchive> getInstalledProcessArchives() {
    final List<ProcessArchive> installedProcessArchives = new ArrayList<ProcessArchive>();
    for (PlatformProcessEngine processEngineService : processEnginesByName.values()) {
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
      final PlatformProcessEngine processEngineService = processEnginesByName.get(processEngineName); 
      if(processEngineService == null) {
        throw new FoxPlatformException("Cannot retreive list of process archives fot process engine: process engine with name '"+processEngineName+"' is not managed by the fox platform.");
      }    
      return processEngineService.getCachedProcessArchives();
    }
  }
  
  public Future<ProcessArchiveUninstallOperation> unInstallProcessArchive(ProcessArchive processArchive) {
    if(processArchive == null) {
      throw new FoxPlatformException("Cannot uninstall process archive: process archive is null");
    }
    return unInstallProcessArchive(processArchive.getName());
  }
  
  // operation impl /////////////////////////////////////////////////

  public static class ProcessEngineStopOperationImpl implements ProcessEngineStopOperation {


    private final Throwable exception;

    public ProcessEngineStopOperationImpl() {
      exception = null;
    }
    
    public ProcessEngineStopOperationImpl(Throwable exception) {
      this.exception = exception;            
    }
    
    public boolean wasSuccessful() {
      return exception == null;
    }
    
    public Throwable getException() {
      return exception;
    }
  }

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
  
  public static class ProcessArchiveInstallOperationImpl implements ProcessArchiveInstallOperation {
    
    private final ProcessEngine processEngine;
    private final String processEngineDeploymentId;
    private final Throwable exception;

    public ProcessArchiveInstallOperationImpl(ProcessEngine processEngine, String processEngineDeploymentId) {
      this.processEngine = processEngine;
      this.processEngineDeploymentId = processEngineDeploymentId;
      this.exception = null;
    }
        
    public ProcessArchiveInstallOperationImpl(Throwable exception) {
      this.exception = exception;
      this.processEngine = null;
      this.processEngineDeploymentId = null;
    }

    public boolean wasSuccessful() {
      return exception == null;
    }

    public ProcessEngine getProcessenEngine() {
      return processEngine;
    }

    public String processEngineDeploymentId() {
      return processEngineDeploymentId;
    }

    public Throwable getException() {
      return exception;
    }
    
  }
  
  public static class ProcessArchiveUninstallOperationImpl implements ProcessArchiveUninstallOperation {

    private final Throwable exception;

    public ProcessArchiveUninstallOperationImpl() {
      exception = null;
    }
    
    public ProcessArchiveUninstallOperationImpl(Throwable exception) {
      this.exception = exception;            
    }
    
    public boolean wasSuccessful() {
      return exception == null;
    }
    
    public Throwable getException() {
      return exception;
    }
  }
  
}
