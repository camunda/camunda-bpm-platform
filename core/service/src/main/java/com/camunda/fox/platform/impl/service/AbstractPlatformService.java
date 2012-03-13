package com.camunda.fox.platform.impl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>Abstract implementation of the {@link ProcessEngineService}.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public abstract class AbstractPlatformService implements ProcessEngineService, ProcessArchiveService {
  
  private final static Logger log = Logger.getLogger(AbstractPlatformService.class.getName()); 
    
  // state //////////////////////////////////

  protected CmpeProcessEngineService defaultProcessEngine = null;
  protected Map<String, CmpeProcessEngineService> processEnginesByName = new HashMap<String, CmpeProcessEngineService>();
  protected Map<String, CmpeProcessEngineService> processEnginesByProcessArchiveName = new HashMap<String, CmpeProcessEngineService>();

  protected List<ProcessEngine> cachedProcessEngines = Collections.emptyList();
  protected List<String> cachedProcessEngineNames = Collections.emptyList();
  
  // ProcessEngineService implementation //////////////

  public ProcessEngine getDefaultProcessEngine() {
    if (defaultProcessEngine == null) {
      throw new FoxPlatformException("No default process engine defined.");
    }
    return defaultProcessEngine.getProcessEngine();
  }

  public List<ProcessEngine> getProcessEngines() {
    return cachedProcessEngines;
  }

  @Override
  public List<String> getProcessEngineNames() {
    return new ArrayList<String>();
  }

  @Override
  public ProcessEngine getProcessEngine(String name) {
    final CmpeProcessEngineService processEngine = processEnginesByName.get(name);
    if (processEngine == null) {
      throw new FoxPlatformException("No process engine with the name '" + name + "' found.");
    }
    return processEngine.getProcessEngine();
  }

  protected ProcessEngine doStartProcessEngine(ProcessEngineConfiguration processEngineConfiguration) {
    
    final boolean isDefault = processEngineConfiguration.isDefault();
    final String processEngineName = processEngineConfiguration.getProcessEngineName();

    synchronized (this) { 
      // do integrity checks 
  
      if (processEngineName == null) {
        throw new FoxPlatformException("Cannot start process engine: name is 'null'");
      }
      
      if(cachedProcessEngineNames.contains(processEngineName)) {
        throw new FoxPlatformException("Cannot start process engine: name is 'null'");
      } 
  
      if (isDefault) {
        if (defaultProcessEngine != null) {
          throw new FoxPlatformException("Cannot start process engine with name '" + processEngineName + "': Default process engine already defined. ");
        }
      }
      
      // add the process engine name in the synchronized block. This allows us to sync only for a short time
      // while still guaranteeing that we do not start two process engines with the same name.
      final ArrayList<String> processEngineNames = new ArrayList<String>(cachedProcessEngineNames);
      processEngineNames.add(processEngineName);      
      cachedProcessEngineNames = Collections.unmodifiableList(processEngineNames);
      
    }
    
    CmpeProcessEngineService processEngine = new CmpeProcessEngineService();
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
        
    return processEngine.getProcessEngine();
  }

  protected synchronized void doStopProcessEngine(String processEngineName) {
    // stopping of process engines is performed in a serialized fashion

    if (processEngineName == null) {
      throw new FoxPlatformException("Cannot stop process engine: process engine name is null.");
    }

    CmpeProcessEngineService cmpeProcessEngine = processEnginesByName.get(processEngineName);
    if (cmpeProcessEngine == null) {
      throw new FoxPlatformException("Cannot stop process engine with name '" + processEngineName + ": no such process engine.");
    }
    
    cmpeProcessEngine.stop();
    
    processEnginesByName.remove(processEngineName);

    final ArrayList<String> processEngineNames = new ArrayList<String>(cachedProcessEngineNames);
    processEngineNames.remove(processEngineName);
    cachedProcessEngineNames = Collections.unmodifiableList(processEngineNames);
  }
  
  // ProcessArchiveService implementation //////////////
  
  protected ProcessArchiveInstallOperation doInstallProcessArchive(ProcessArchive processArchive) {
    try {
      CmpeProcessEngineService processEngine = getProcessEngineSerivce(processArchive);
      
      final ProcessEngine processEngineHandle = processEngine.installProcessArchive(processArchive);
      
      final String deploymentId = processEngine
        .getInstalledProcessArchivesByName()
        .get(processArchive.getName())
        .getActivitiDeployment()
        .getId();
      
      processEnginesByProcessArchiveName.put(processArchive.getName(), processEngine);
      
      return new ProcessArchiveInstallOperationImpl(processEngineHandle, deploymentId);
      
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not install process archive: "+e.getMessage(), e);      
      return new ProcessArchiveInstallOperationImpl(e);
    }
  }

  protected CmpeProcessEngineService getProcessEngineSerivce(ProcessArchive processArchive) {
    final String processEngineName = processArchive.getProcessEngineName();
    if(processEngineName == null) {
      if(defaultProcessEngine == null) {
        throw new FoxPlatformException("Cannot determine process engine for process archive '" + processArchive.getName()
                + "': specified process engine name is null and there is no default process engine defined.");
      } else {
        return defaultProcessEngine;
      }      
    } else {
      CmpeProcessEngineService processEngine = processEnginesByName.get(processEngineName); 
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
      
      final CmpeProcessEngineService processEngine = processEnginesByProcessArchiveName.get(processArchiveName);
      
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

  @Override
  public List<ProcessArchive> getInstalledProcessArchives() {
    
    return null;
  }

  @Override
  public List<ProcessArchive> getInstalledProcessArchives(ProcessEngine processEngine) {
    
    return null;
  }

  @Override
  public List<ProcessArchive> getInstalledProcessArchives(String processEngineName) {
    // TODO Auto-generated method stub
    return null;
  }
  
  // operation impl ////////////////////////

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
