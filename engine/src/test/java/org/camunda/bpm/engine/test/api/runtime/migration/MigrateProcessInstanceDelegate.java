package org.camunda.bpm.engine.test.api.runtime.migration;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.repository.ProcessDefinition;

public class MigrateProcessInstanceDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    RepositoryService repoService = execution.getProcessEngineServices().getRepositoryService();
    ProcessDefinition targetDefinition = repoService.createProcessDefinitionQuery().latestVersion().singleResult();
    
    SetProcessDefinitionVersionCmd migrationCommand = 
        new SetProcessDefinitionVersionCmd(execution.getProcessInstanceId(), targetDefinition.getVersion());
    
    Context.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(migrationCommand);

  }

}
