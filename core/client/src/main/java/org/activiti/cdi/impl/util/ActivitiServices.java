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
package org.activiti.cdi.impl.util;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

import com.camunda.fox.client.impl.ProcessArchiveSupport;
import com.camunda.fox.platform.api.ProcessEngineService;

/**
 * <p>Replaces the ActivitiService producers provided by activiti-cdi.</p>
 * 
 * <p>The reason is that org.activiti.cdi.impl.util.ActivitiServices 
 * is an {@link ApplicationScoped} bean and an instance is looked up 
 * in an AfterDeploymentValidation event. At that point, the application
 * context may not yet be activated.</p>
 * 
 * @author Daniel Meyer
 */
@Dependent
public class ActivitiServices {
  
  @EJB(lookup=ProcessArchiveSupport.PROCESS_ENGINE_SERVICE_NAME)
  private ProcessEngineService processEngineService;
  
  public void setProcessEngine(ProcessEngine processEngine) {
    // noop
  }

  @Produces @Named @ApplicationScoped public ProcessEngine processEngine() { return processEngineService.getDefaultProcessEngine(); }

  @Produces @Named @ApplicationScoped public RuntimeService runtimeService() { return processEngine().getRuntimeService(); }

  @Produces @Named @ApplicationScoped public TaskService taskService() { return processEngine().getTaskService(); }

  @Produces @Named @ApplicationScoped public RepositoryService repositoryService() { return processEngine().getRepositoryService(); }

  @Produces @Named @ApplicationScoped public FormService formService() { return processEngine().getFormService(); }

  @Produces @Named @ApplicationScoped public HistoryService historyService() { return processEngine().getHistoryService(); }

  @Produces @Named @ApplicationScoped public IdentityService identityService() { return processEngine().getIdentityService(); }

  @Produces @Named @ApplicationScoped public ManagementService managementService() { return processEngine().getManagementService(); }

}