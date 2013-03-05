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

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;

import com.camunda.fox.client.ProcessEngineName;
import com.camunda.fox.platform.FoxPlatformException;

/**
 * This bean provides producers for the activiti services such 
 * that the injection point can choose the process engine it wants to 
 * inject by its name: 
 * 
 * @Inject 
 * @ProcessEngineName("second-engine")
 * private RuntimeService runtimeService;
 * 
 * @author Daniel Meyer
 */
public class NamedActivitiServicesProducer {
  
  @Produces @ProcessEngineName("") 
  public ProcessEngine processEngine(InjectionPoint ip) { 
    
    ProcessEngineName annotation = ip.getAnnotated().getAnnotation(ProcessEngineName.class);
    String processEngineName = annotation.value();
    if(processEngineName == null || processEngineName.length() == 0) {
     throw new FoxPlatformException("Cannot determine which process engine to inject: @ProcessEngineName must specify the name of a process engine."); 
    }    
    try {
      ProcessEngineService processEngineService = BpmPlatform.getProcessEngineService();
      return processEngineService.getProcessEngine(processEngineName);
    }catch (Exception e) {
      throw new FoxPlatformException("Cannot find process engine named '"+processEngineName+"' specified using @ProcessEngineName: "+e.getMessage(), e);
    }
    
  }

  @Produces @ProcessEngineName("") public RuntimeService runtimeService(InjectionPoint ip) { return processEngine(ip).getRuntimeService(); }

  @Produces @ProcessEngineName("") public TaskService taskService(InjectionPoint ip) { return processEngine(ip).getTaskService(); }

  @Produces @ProcessEngineName("") public RepositoryService repositoryService(InjectionPoint ip) { return processEngine(ip).getRepositoryService(); }

  @Produces @ProcessEngineName("") public FormService formService(InjectionPoint ip) { return processEngine(ip).getFormService(); }

  @Produces @ProcessEngineName("") public HistoryService historyService(InjectionPoint ip) { return processEngine(ip).getHistoryService(); }

  @Produces @ProcessEngineName("") public IdentityService identityService(InjectionPoint ip) { return processEngine(ip).getIdentityService(); }

  @Produces @ProcessEngineName("") public ManagementService managementService(InjectionPoint ip) { return processEngine(ip).getManagementService(); }

}
