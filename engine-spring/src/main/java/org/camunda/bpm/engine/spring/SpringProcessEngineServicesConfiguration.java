package org.camunda.bpm.engine.spring;

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
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes all camunda process engine services as beans.
 */
@Configuration
public class SpringProcessEngineServicesConfiguration implements ProcessEngineServices {

    @Autowired
    private ProcessEngine processEngine;

    @Bean(name = "runtimeService")
    @Override
    public RuntimeService getRuntimeService() {
        return processEngine.getRuntimeService();
    }

    @Bean(name = "repositoryService")
    @Override
    public RepositoryService getRepositoryService() {
        return processEngine.getRepositoryService();
    }

    @Bean(name = "formService")
    @Override
    public FormService getFormService() {
        return processEngine.getFormService();
    }

    @Bean(name = "taskService")
    @Override
    public TaskService getTaskService() {
        return processEngine.getTaskService();
    }

    @Bean(name = "historyService")
    @Override
    public HistoryService getHistoryService() {
        return processEngine.getHistoryService();
    }

    @Bean(name = "identityService")
    @Override
    public IdentityService getIdentityService() {
        return processEngine.getIdentityService();
    }

    @Bean(name = "managementService")
    @Override
    public ManagementService getManagementService() {
        return processEngine.getManagementService();
    }

    @Bean(name = "authorizationService")
    @Override
    public AuthorizationService getAuthorizationService() {
        return processEngine.getAuthorizationService();
    }

    @Bean(name = "caseService")
    @Override
    public CaseService getCaseService() {
        return processEngine.getCaseService();
    }

    @Bean(name = "filterService")
    @Override
    public FilterService getFilterService() {
        return processEngine.getFilterService();
    }

    @Bean(name = "externalTaskService")
    @Override
    public ExternalTaskService getExternalTaskService() {
        return processEngine.getExternalTaskService();
    }

    @Bean(name = "decisionService")
    @Override
    public DecisionService getDecisionService() {
        return processEngine.getDecisionService();
    }

}
