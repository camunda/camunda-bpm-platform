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

import org.activiti.engine.ProcessEngine;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * <p>A simple registry that keeps track of process engines.</p>
 *  
 * @author Daniel Meyer
 */
public class ProcessEngineRegistry {
  
  // state ///////////////////////
  
  protected Map<String, ProcessEngineController> processEnginesByName = new HashMap<String, ProcessEngineController>();
  protected ProcessEngineController defaultProcessEngine = null;

  protected List<ProcessEngine> cachedProcessEngines = Collections.emptyList();  
  protected List<String> cachedProcessEngineNames = Collections.emptyList();
  
  protected List<String> startingProcessEngines = new ArrayList<String>();
  protected boolean startingDefaultEngine;
  
  ////////////////////////////////// 
  
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
    final ProcessEngineController processEngine = processEnginesByName.get(name);
    if (processEngine == null) {
      throw new FoxPlatformException("No process engine with the name '" + name + "' found.");
    }
    return processEngine.getProcessEngine();
  }
  
  public synchronized void startInstallingNewProcessEngine(ProcessEngineConfiguration processEngineConfiguration) {
    final boolean isDefault = processEngineConfiguration.isDefault();
    final String processEngineName = processEngineConfiguration.getProcessEngineName();
    // do integrity checks
    if (processEngineName == null) {
      throw new FoxPlatformException("Cannot start process engine: name is 'null'");
    }

    if (cachedProcessEngineNames.contains(processEngineName) && !startingProcessEngines.contains(processEngineName)) {
      throw new FoxPlatformException("Cannot start process engine: process engine with name '" + processEngineName + "' already started");
    }

    if (isDefault) {
      if (defaultProcessEngine != null || startingDefaultEngine) {
        throw new FoxPlatformException("Cannot start process engine with name '" + processEngineName + "': Default process engine already defined. ");
      } else {
        startingDefaultEngine = true;
      }
      
    }
 
    startingProcessEngines.add(processEngineName);
  }
  
  /**
   * indicates that the installation of the process engine was successfull
   */
  public synchronized void processEngineInstallationSuccess(ProcessEngineConfiguration processEngineConfiguration, ProcessEngineController processEngineController) {
    if(startingDefaultEngine && processEngineConfiguration.isDefault()) {
      startingDefaultEngine = false;
      defaultProcessEngine = processEngineController;
    }
    
    final String processEngineName = processEngineConfiguration.getProcessEngineName();
    
    startingProcessEngines.remove(processEngineName);
    
    final ArrayList<String> processEngineNames = new ArrayList<String>(cachedProcessEngineNames);
    processEngineNames.add(processEngineName);      
    cachedProcessEngineNames = Collections.unmodifiableList(processEngineNames);    
    
    final ArrayList<ProcessEngine> processEngines = new ArrayList<ProcessEngine>(cachedProcessEngines);
    processEngines.add(processEngineController.getProcessEngine());      
    cachedProcessEngines = Collections.unmodifiableList(processEngines);    
    
    processEnginesByName.put(processEngineName, processEngineController);    
  }
  
  /**
   * indicates that the installation of the process engine was unsuccessfull
   */
  public synchronized void processEngineInstallationFailed(ProcessEngineConfiguration processEngineConfiguration) {
    if(startingDefaultEngine) {
      startingDefaultEngine = false;
    }
    
    final String processEngineName = processEngineConfiguration.getProcessEngineName();
    
    startingProcessEngines.remove(processEngineName);   
  }
  
  public synchronized void processEngineUninstalled(ProcessEngineController processEngineController) {
    
    final String processEngineName = processEngineController.getProcessEngineName();
    
    if(defaultProcessEngine != null && defaultProcessEngine == processEngineController) {
      defaultProcessEngine = null;
    }

    processEnginesByName.remove(processEngineName);
        
    final ArrayList<String> processEngineNames = new ArrayList<String>(cachedProcessEngineNames);
    processEngineNames.remove(processEngineName);      
    cachedProcessEngineNames = Collections.unmodifiableList(processEngineNames);    
    
    final ArrayList<ProcessEngine> processEngines = new ArrayList<ProcessEngine>(cachedProcessEngines);
    processEngines.remove(processEngineController.getProcessEngine());      
    cachedProcessEngines = Collections.unmodifiableList(processEngines);    
    
  }

  public ProcessEngineController getProcessEngineController(String name) {    
    return processEnginesByName.get(name);
  }

  public List<ProcessEngineController> getProcessEngineControllers() {
    return new ArrayList<ProcessEngineController>(processEnginesByName.values());
  }

}
