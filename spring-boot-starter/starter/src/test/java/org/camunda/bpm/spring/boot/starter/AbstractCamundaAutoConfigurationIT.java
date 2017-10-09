package org.camunda.bpm.spring.boot.starter;

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
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCamundaAutoConfigurationIT {

  @Autowired
  protected RuntimeService runtimeService;

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected FormService formService;

  @Autowired
  protected TaskService taskService;

  @Autowired
  protected HistoryService historyService;

  @Autowired
  protected IdentityService identityService;

  @Autowired
  protected ManagementService managementService;

  @Autowired
  protected AuthorizationService authorizationService;

  @Autowired
  protected CaseService caseService;

  @Autowired
  protected FilterService filterService;

  @Autowired
  protected ExternalTaskService externalTaskService;

  @Autowired
  protected DecisionService decisionService;

  @Autowired(required = false)
  protected JobExecutor jobExecutor;

  @Autowired
  protected ProcessEngine processEngine;

  @After
  public void cleanup() {
    //remove history level from database
    ((ProcessEngineConfigurationImpl)processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        final PropertyEntity historyLevel = commandContext.getPropertyManager().findPropertyById("historyLevel");
        if (historyLevel != null) {
          commandContext.getDbEntityManager().delete(historyLevel);
        }
        return null;
      }
    });
  }

}
