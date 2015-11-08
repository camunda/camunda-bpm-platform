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
package org.camunda.bpm.engine.cdi.impl;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.cdi.annotation.ProcessEngineName;


/**
 * This bean provides producers for the process engine services such
 * that the injection point can choose the process engine it wants to
 * inject by its name:
 *
 * @Inject
 * @ProcessEngineName("second-engine")
 * private RuntimeService runtimeService;
 *
 * @author Daniel Meyer
 */
public class NamedProcessEngineServicesProducer {

  @Produces @ProcessEngineName("")
  public ProcessEngine processEngine(InjectionPoint ip) {

    ProcessEngineName annotation = ip.getAnnotated().getAnnotation(ProcessEngineName.class);
    String processEngineName = annotation.value();
    if(processEngineName == null || processEngineName.length() == 0) {
     throw new ProcessEngineException("Cannot determine which process engine to inject: @ProcessEngineName must specify the name of a process engine.");
    }
    try {
      ProcessEngineService processEngineService = BpmPlatform.getProcessEngineService();
      return processEngineService.getProcessEngine(processEngineName);
    }catch (Exception e) {
      throw new ProcessEngineException("Cannot find process engine named '"+processEngineName+"' specified using @ProcessEngineName: "+e.getMessage(), e);
    }

  }

  @Produces @ProcessEngineName("") public RuntimeService runtimeService(InjectionPoint ip) { return processEngine(ip).getRuntimeService(); }

  @Produces @ProcessEngineName("") public TaskService taskService(InjectionPoint ip) { return processEngine(ip).getTaskService(); }

  @Produces @ProcessEngineName("") public RepositoryService repositoryService(InjectionPoint ip) { return processEngine(ip).getRepositoryService(); }

  @Produces @ProcessEngineName("") public FormService formService(InjectionPoint ip) { return processEngine(ip).getFormService(); }

  @Produces @ProcessEngineName("") public HistoryService historyService(InjectionPoint ip) { return processEngine(ip).getHistoryService(); }

  @Produces @ProcessEngineName("") public IdentityService identityService(InjectionPoint ip) { return processEngine(ip).getIdentityService(); }

  @Produces @ProcessEngineName("") public ManagementService managementService(InjectionPoint ip) { return processEngine(ip).getManagementService(); }

  @Produces @ProcessEngineName("") public AuthorizationService authorizationService(InjectionPoint ip) { return processEngine(ip).getAuthorizationService(); }

  @Produces @ProcessEngineName("") public FilterService filterService(InjectionPoint ip) { return processEngine(ip).getFilterService(); }

  @Produces @ProcessEngineName("") public ExternalTaskService externalTaskService(InjectionPoint ip) { return processEngine(ip).getExternalTaskService(); }

  @Produces @ProcessEngineName("") public CaseService caseService(InjectionPoint ip) { return processEngine(ip).getCaseService(); }

  @Produces @ProcessEngineName("") public DecisionService decisionService(InjectionPoint ip) { return processEngine(ip).getDecisionService(); }

}
