package org.activiti.engine.impl.cmd;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;

/**
 * 
 * 
 * @author Daniel Meyer
 *
 */
public class ActivateDeploymentForApplicationCmd implements Command<ProcessApplicationRegistration> {

  protected ProcessApplicationReference reference;
  protected String deploymentId;

  public ActivateDeploymentForApplicationCmd(String deploymentId, ProcessApplicationReference reference) {
    this.deploymentId = deploymentId;
    this.reference = reference;
  }

  public ProcessApplicationRegistration execute(CommandContext commandContext) {
    
    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();    
    final ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();
    
    return processApplicationManager.registerProcessApplicationForDeployment(deploymentId, reference);
    
  }

}
