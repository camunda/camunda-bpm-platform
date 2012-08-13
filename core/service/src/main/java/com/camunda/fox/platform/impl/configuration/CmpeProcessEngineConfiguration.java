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
package com.camunda.fox.platform.impl.configuration;

import java.util.List;

import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.bpmn.webservice.MessageInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.engine.impl.variable.BooleanType;
import org.activiti.engine.impl.variable.ByteArrayType;
import org.activiti.engine.impl.variable.CustomObjectType;
import org.activiti.engine.impl.variable.DateType;
import org.activiti.engine.impl.variable.DefaultVariableTypes;
import org.activiti.engine.impl.variable.DoubleType;
import org.activiti.engine.impl.variable.IntegerType;
import org.activiti.engine.impl.variable.LongType;
import org.activiti.engine.impl.variable.NullType;
import org.activiti.engine.impl.variable.ShortType;
import org.activiti.engine.impl.variable.StringType;
import org.activiti.engine.impl.variable.VariableType;

import com.camunda.fox.engine.impl.bpmn.parser.FoxFailedJobParseListener;
import com.camunda.fox.engine.impl.jobexecutor.FoxFailedJobCommandFactory;
import com.camunda.fox.platform.impl.context.DefaultProcessArchiveServices;
import com.camunda.fox.platform.impl.context.ProcessArchiveServicesSupport;
import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;
import com.camunda.fox.platform.impl.jobexecutor.spi.JobExecutorFactory;
import com.camunda.fox.platform.impl.service.ProcessEngineController;
import com.camunda.fox.platform.impl.util.Services;

/**
 * <p>Configuration for a CMPE (Container-Managed Process Engine) running in a Java EE 6+ 
 * compliant application server</p>
 * 
 * <p>We use externally-managed transactions to enable the process engine to participate in 
 * client transactions (and vice versa).</p>
 * 
 * @author Daniel Meyer
 */
public abstract class CmpeProcessEngineConfiguration extends ProcessEngineConfigurationImpl {
  
  protected ProcessArchiveServices processArchiveServices;
  protected final ProcessEngineController cmpeProcessEngine;
  
  public CmpeProcessEngineConfiguration(ProcessEngineController processEngineServiceBean) {
    this.cmpeProcessEngine = processEngineServiceBean;
  }
    
  @Override
  protected void init() {
    transactionsExternallyManaged = true;
    isCreateDiagramOnDeploy = false;
    initProcessArchiveServices();    
    super.init();    
  }
  
  @Override
  protected void initFailedJobCommandFactory() {
    if (failedJobCommandFactory == null) {
    	failedJobCommandFactory = new FoxFailedJobCommandFactory();
    }
  }
  
  @Override
  protected List<BpmnParseListener> getDefaultBPMNParseListeners() {
    List<BpmnParseListener> result = super.getDefaultBPMNParseListeners();
    result.add(new FoxFailedJobParseListener());
    return result;
  }
  
  protected void initProcessArchiveServices() {
    if(processArchiveServices == null) {
      processArchiveServices = new DefaultProcessArchiveServices();
      processArchiveServices.setProcessEngineController(cmpeProcessEngine);
    }
  }
 
  @Override
  public void initJobExecutor() {
      if(jobExecutor == null) {
        JobExecutorFactory jobExecutorFactory = Services.getService(JobExecutorFactory.class);
        jobExecutor = jobExecutorFactory.getJobExecutor();
      }   
      if (jobExecutor instanceof ProcessArchiveServicesSupport) {
        ProcessArchiveServicesSupport support = (ProcessArchiveServicesSupport) jobExecutor;
        support.setProcessArchiveServices(processArchiveServices);        
      }
      super.initJobExecutor();
  }
  
  @Override
  protected void initIdGenerator() {
    if(idGenerator == null) {
      idGenerator = new StrongUuidGenerator();
    }
    super.initIdGenerator();
  }
  

  @Override
  protected void initExpressionManager() {
    expressionManager = new CmpeExpressionManager(processArchiveServices);    
  }
  
  @Override
  protected void initCommandContextFactory() {
    if(commandContextFactory == null) {
      commandContextFactory = new CmpeCommandContextFactory();
      commandContextFactory.setProcessEngineConfiguration(this);      
    }
    if (commandContextFactory instanceof ProcessArchiveServicesSupport) {
      ProcessArchiveServicesSupport support = (ProcessArchiveServicesSupport) commandContextFactory;
      support.setProcessArchiveServices(processArchiveServices);        
    }
  }
    
  public void setProcessArchiveServices(ProcessArchiveServices paServices) {
    this.processArchiveServices = paServices;
  }
    
  public ProcessArchiveServices getProcessArchiveServices() {
    return processArchiveServices;
  }
  
  public ProcessEngineController getProcessEngineServiceBean() {
    return cmpeProcessEngine;
  }
  
  @Override
  protected void initVariableTypes() {
    if (variableTypes==null) {
      variableTypes = new DefaultVariableTypes();
      if (customPreVariableTypes!=null) {
        for (VariableType customVariableType: customPreVariableTypes) {
          variableTypes.addType(customVariableType);
        }
      }
      variableTypes.addType(new NullType());
      variableTypes.addType(new StringType());
      variableTypes.addType(new BooleanType());
      variableTypes.addType(new ShortType());
      variableTypes.addType(new IntegerType());
      variableTypes.addType(new LongType());
      variableTypes.addType(new DateType());
      variableTypes.addType(new DoubleType());
      variableTypes.addType(new ByteArrayType());
      
     
      variableTypes.addType(new CmpeSerializableType(processArchiveServices));
      
      variableTypes.addType(new CustomObjectType("item", ItemInstance.class));
      variableTypes.addType(new CustomObjectType("message", MessageInstance.class));
      if (customPostVariableTypes!=null) {
        for (VariableType customVariableType: customPostVariableTypes) {
          variableTypes.addType(customVariableType);
        }
      }
    }
    if (variableTypes instanceof ProcessArchiveServicesSupport) {
      ProcessArchiveServicesSupport support = (ProcessArchiveServicesSupport) variableTypes;
      support.setProcessArchiveServices(processArchiveServices);        
    }
  }
}
