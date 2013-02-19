/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cdi.impl;

import javax.enterprise.context.RequestScoped;
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
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;

/**
 * Makes the managed process engine and the provided services available for injection
 * 
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class ProcessEngineServiceProducer {
  
  @Produces @Named @RequestScoped public ProcessEngine processEngine() { return getProcessEngineService().getDefaultProcessEngine(); }

  @Produces @Named @RequestScoped public RuntimeService runtimeService() { return processEngine().getRuntimeService(); }

  @Produces @Named @RequestScoped public TaskService taskService() { return processEngine().getTaskService(); }

  @Produces @Named @RequestScoped public RepositoryService repositoryService() { return processEngine().getRepositoryService(); }

  @Produces @Named @RequestScoped public FormService formService() { return processEngine().getFormService(); }

  @Produces @Named @RequestScoped public HistoryService historyService() { return processEngine().getHistoryService(); }

  @Produces @Named @RequestScoped public IdentityService identityService() { return processEngine().getIdentityService(); }

  @Produces @Named @RequestScoped public ManagementService managementService() { return processEngine().getManagementService(); }
  
  private ProcessEngineService getProcessEngineService() {
    return BpmPlatform.getProcessEngineService();
  }

}
