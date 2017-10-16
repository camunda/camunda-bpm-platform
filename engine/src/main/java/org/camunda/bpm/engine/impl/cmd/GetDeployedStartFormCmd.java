package org.camunda.bpm.engine.impl.cmd;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * 
 * @author Anna Pazola
 *
 */
public class GetDeployedStartFormCmd extends AbstractGetDeployedFormCmd {

  protected String processDefinitionId;

  public GetDeployedStartFormCmd(String processDefinitionId) {
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Process definition id cannot be null", "processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
  }

  @Override
  protected FormData getFormData(final CommandContext commandContext) {
    return commandContext.runWithoutAuthorization(new Callable<FormData>() {
      @Override
      public FormData call() throws Exception {
        return new GetStartFormCmd(processDefinitionId).execute(commandContext);
      }
    });
  }

  @Override
  protected void checkAuthorization(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();
    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId);
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadProcessDefinition(processDefinition);
    }
  }
}
