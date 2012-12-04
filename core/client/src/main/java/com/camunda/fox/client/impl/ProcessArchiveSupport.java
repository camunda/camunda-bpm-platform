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
package com.camunda.fox.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.activiti.engine.ProcessEngine;

import com.camunda.fox.client.impl.executor.ProcessArchiveContextExecutor;
import com.camunda.fox.client.impl.parser.DefaultProcessesXmlParser;
import com.camunda.fox.client.impl.parser.spi.ProcessesXmlParser;
import com.camunda.fox.client.impl.schema.ProcessesXml;
import com.camunda.fox.client.impl.schema.ProcessesXml.ProcessArchiveXml;
import com.camunda.fox.client.impl.spi.ProcessArchiveExtension;
import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessArchiveService.ProcessArchiveInstallation;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.spi.ProcessArchive;

@Startup
@Singleton
//make sure the container does not rollback transactions if this bean throws an exception
@TransactionManagement(TransactionManagementType.BEAN)
//make sure the container does not synchronize access to this bean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN) 
public class ProcessArchiveSupport {
  
  private final static Logger log = Logger.getLogger(ProcessArchiveSupport.class.getName());
  
  public final static String PROCESSES_XML_FILE_LOCATION = "META-INF/processes.xml";
  
  public final static String PROCESS_ARCHIVE_SERVICE_NAME =
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "PlatformService!com.camunda.fox.platform.api.ProcessArchiveService";
  
  public final static String PROCESS_ENGINE_SERVICE_NAME =
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "PlatformService!com.camunda.fox.platform.api.ProcessEngineService";
  
  @EJB(lookup=PROCESS_ARCHIVE_SERVICE_NAME)
  protected ProcessArchiveService processArchiveService;
  
  @EJB(lookup=PROCESS_ENGINE_SERVICE_NAME)
  protected ProcessEngineService processEngineService;
  
  // lookup the process archive context executor
  @EJB
  protected ProcessArchiveContextExecutor processArchiveContextExecutorBean;
  
  @Resource
  protected SessionContext sessionContext;
  
  protected List<ProcessArchiveExtension> processArchiveExtensions = new ArrayList<ProcessArchiveExtension>();

  protected Map<ProcessArchive, ProcessEngine> installedProcessArchives = new HashMap<ProcessArchive, ProcessEngine>();
    
  @PostConstruct
  public void start() {
    fireBeforeProcessArchiveStart();
    installProcessArchives();
    fireAfterProcessArchiveStart();
  }

  @PreDestroy
  public void stop() {
    fireBeforeProcessArchiveStop();
    uninstallProcessArchives();
    fireAfterProcessArchiveStop();
  }
  
  protected void installProcessArchives() {
    
    final ProcessesXmlParser parser = getProcessesXmlParser();
    List<ProcessesXml> processesXmls = parser.parseProcessesXml(PROCESSES_XML_FILE_LOCATION);

    if (processesXmls.size() > 0) {
      try {
        List<ProcessArchive> processArchives = getConfiguredProcessArchives(processesXmls);
        for (ProcessArchive processArchive : processArchives) {
          ProcessArchiveInstallation installation = processArchiveService.installProcessArchive(processArchive);
          installedProcessArchives.put(processArchive, installation.getProcessEngine());
        }
      } catch(RuntimeException e) {
        uninstallProcessArchives();
        throw e;          
      }
    } else {
      log.log(Level.INFO, "No " + PROCESSES_XML_FILE_LOCATION + " found. Not creating a process archive installation.");
    }
  }

  protected void uninstallProcessArchives() { 
    for (ProcessArchive processArchive : installedProcessArchives.keySet()) {
      try {
        processArchiveService.unInstallProcessArchive(processArchive);
      }catch (Exception e) {
        log.log(Level.WARNING, "Exception while uninstalling process archive '"+processArchive.getName(), e);
      }
    }
  }

  protected List<ProcessArchive> getConfiguredProcessArchives(List<ProcessesXml> processesXmls) {
    List<ProcessArchive> processArchives = new ArrayList<ProcessArchive>();
    List<String> processArchiveNamesSeen = new ArrayList<String>();
    for (ProcessesXml processesXml : processesXmls) {      
      for (ProcessArchiveXml processArchiveXml : processesXml.processArchives) {
        if(processArchiveXml.name == null) {
          setProcessArchiveName(processArchiveXml);
        }
        if(processArchiveNamesSeen.contains(processArchiveXml.name)) {
          throw new FoxPlatformException("Cannot install more than one process archive with name '" + processArchiveXml.name
                  + "'. Make sure to set different names when declaring more than a single process-archive in '"+PROCESSES_XML_FILE_LOCATION+"'.");
        } else {
          processArchiveNamesSeen.add(processArchiveXml.name);
        }
        
        if(processArchiveXml.configuration.processEngineName == null) {
          processArchiveXml.configuration.processEngineName = processEngineService.getDefaultProcessEngine().getName();
        }
        
        processArchives.add(new ProcessArchiveImpl(processArchiveXml, processesXml.metaFileUrl, processArchiveContextExecutorBean));      
      }    
    }
    return processArchives;
  }

  protected ProcessesXmlParser getProcessesXmlParser() {
    ServiceLoader<ProcessesXmlParser> parserLoader = ServiceLoader.load(ProcessesXmlParser.class);
    Iterator<ProcessesXmlParser> iterator = parserLoader.iterator();
    if(iterator.hasNext()) {
      return iterator.next();
    } else {
      return new DefaultProcessesXmlParser();
    }
  }
  
  protected void setProcessArchiveName(ProcessArchiveXml processArchive) {
    if(processArchive.name == null || processArchive.name.length() == 0) {      
      processArchive.name = getContextName();
    }
  }

  protected String getContextName() {
    String appName = (String) sessionContext.lookup("java:app/AppName");
    String moduleName = (String) sessionContext.lookup("java:module/ModuleName");
    
    String detectedName = null;
    if(moduleName != null && !moduleName.equals(appName)) {
      // make sure that if an EAR carries multiple PAs, they are correctly identified by appName + moduleName
      detectedName = appName + "/" + moduleName;
    } else {
      detectedName = appName;
    }
    
    log.info("No name specified for process archive in '"+PROCESSES_XML_FILE_LOCATION+"'. Using Java EE application name '"+detectedName+"'.");
    
    return detectedName;
  }
    
  public Map<ProcessArchive, ProcessEngine> getInstalledProcessArchives() {
    return installedProcessArchives;
  }
  
  public ProcessArchiveService getProcessArchiveService() {
    return processArchiveService;
  }
  
  public ProcessEngineService getProcessEngineService() {
    return processEngineService;
  }
  
  public ProcessArchiveContextExecutor getProcessArchiveContextExecutorBean() {
    return processArchiveContextExecutorBean;
  }
  
  // extensions //////////////////////////////////
  
  private void initProcessArchiveExtensions() {
    ServiceLoader<ProcessArchiveExtension> processArchiveExtensionLoader = ServiceLoader.load(ProcessArchiveExtension.class);
    Iterator<ProcessArchiveExtension> loadableExtensions = processArchiveExtensionLoader.iterator();
    while (loadableExtensions.hasNext()) {
      ProcessArchiveExtension processArchiveExtension = (ProcessArchiveExtension) loadableExtensions.next();
      processArchiveExtensions.add(processArchiveExtension);
    }
    
  }

  protected void fireBeforeProcessArchiveStart() {
    initProcessArchiveExtensions();
    
    for (ProcessArchiveExtension processArchiveExtension : processArchiveExtensions) {
      try {
        processArchiveExtension.beforeProcessArchiveStart(this);
      }catch (Exception e) {
        throw new FoxPlatformException("Exception while invoking 'beforeProcessArchiveStart' for ProcessArchiveExtension "+processArchiveExtension.getClass(), e);
      }
    }
  }
  
  protected void fireAfterProcessArchiveStart() {
    for (ProcessArchiveExtension processArchiveExtension : processArchiveExtensions) {
      try {
        processArchiveExtension.afterProcessArchiveStart(this);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while invoking 'afterProcessArchiveStart' for ProcessArchiveExtension "+processArchiveExtension.getClass(), e);
      }
    }
  }
  
  protected void fireBeforeProcessArchiveStop() {
    for (ProcessArchiveExtension processArchiveExtension : processArchiveExtensions) {
      try {
        processArchiveExtension.beforeProcessArchiveStop(this);
      }catch (Exception e) {
        throw new FoxPlatformException("Exception while invoking 'beforeProcessArchiveStop' for ProcessArchiveExtension "+processArchiveExtension.getClass(), e);
      }
    }
  }
  
  protected void fireAfterProcessArchiveStop() {
    for (ProcessArchiveExtension processArchiveExtension : processArchiveExtensions) {
      try {
        processArchiveExtension.afterProcessArchiveStop(this);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while invoking 'afterProcessArchiveStop' for ProcessArchiveExtension "+processArchiveExtension.getClass(), e);
      }
    }
  }
}
