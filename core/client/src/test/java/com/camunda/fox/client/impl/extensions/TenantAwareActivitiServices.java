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
package com.camunda.fox.client.impl.extensions;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.activiti.cdi.impl.util.ActivitiServices;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

import com.camunda.fox.client.impl.ProcessArchiveSupport;
import com.camunda.fox.platform.spi.ProcessArchive;

@Specializes
public class TenantAwareActivitiServices extends ActivitiServices {
  
   @Inject 
   private MockPrincipal principal;
   
   @Inject
   private ProcessArchiveSupport processArchiveSupport;
   
   @Specializes @Produces @RequestScoped public ProcessEngine processEngine() { 
     
     String tenant = principal.getName();
     
     Map<ProcessArchive, ProcessEngine> installedProcessArchives = processArchiveSupport.getInstalledProcessArchives();
     for (ProcessArchive processArchive : installedProcessArchives.keySet()) {
       if(processArchive.getName().equals(tenant)) {
         return installedProcessArchives.get(processArchive);
       }
     }
     throw new RuntimeException("Unable to find ProcessArchive for tenant'"+tenant+"'");
     
   }

   @Specializes @Produces @RequestScoped public RuntimeService runtimeService() { return processEngine().getRuntimeService(); }

   @Specializes @Produces @RequestScoped public TaskService taskService() { return processEngine().getTaskService(); }

   @Specializes @Produces @RequestScoped public RepositoryService repositoryService() { return processEngine().getRepositoryService(); }

   @Specializes @Produces @RequestScoped public FormService formService() { return processEngine().getFormService(); }

   @Specializes @Produces @RequestScoped public HistoryService historyService() { return processEngine().getHistoryService(); }

   @Specializes @Produces @RequestScoped public IdentityService identityService() { return processEngine().getIdentityService(); }

   @Specializes @Produces @RequestScoped public ManagementService managementService() { return processEngine().getManagementService(); }   
  
}
