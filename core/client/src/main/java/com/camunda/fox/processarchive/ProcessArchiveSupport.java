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
package com.camunda.fox.processarchive;

import java.util.Iterator;
import java.util.ServiceLoader;

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

import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.processarchive.executor.ProcessArchiveContextExecutor;
import com.camunda.fox.processarchive.parser.DefaultProcessesXmlParser;
import com.camunda.fox.processarchive.parser.spi.ProcessesXmlParser;
import com.camunda.fox.processarchive.schema.ProcessesXml;

@Startup
@Singleton
//make sure the container does not rollback transactions if this bean throws an exception
@TransactionManagement(TransactionManagementType.BEAN)
//make sure the container does not synchronize access to this bean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN) 
public class ProcessArchiveSupport {
  
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

  protected ProcessArchiveImpl processArchive;

  protected ProcessEngine processEngine;
    
  @PostConstruct
  protected void installProcessArchive() {
    final String defaultProcessEngineName = processEngineService.getDefaultProcessEngine().getName(); 
    final ProcessesXmlParser parser = getProcessesXmlParser();
    
    ProcessesXml processesXml = parser.parseProcessesXml();
    setProcessArchiveName(processesXml);
    processArchive = new ProcessArchiveImpl(processesXml, processArchiveContextExecutorBean, defaultProcessEngineName);
    processEngine = processArchiveService.installProcessArchive(processArchive).getProcessenEngine();
  }

  @PreDestroy
  protected void uninstallProcessArchive() { 
    processArchiveService.unInstallProcessArchive(processArchive);    
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
  
  protected void setProcessArchiveName(ProcessesXml processesXml) {
    if(processesXml.name == null || processesXml.name.length() == 0) {      
      processesXml.name = getContextName();
    }
  }

  protected String getContextName() {
    String appName = (String) sessionContext.lookup("java:app/AppName");   
    return appName;    
  }
    
  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
    
  public ProcessArchiveImpl getProcessArchive() {
    return processArchive;
  }
}
