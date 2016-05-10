package org.camunda.bpm.engine.impl.cmd;

import java.util.Collections;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 *
 *
 * @author Daniel Meyer
 *
 */
public class RegisterProcessApplicationCmd implements Command<ProcessApplicationRegistration> {

  protected ProcessApplicationReference reference;
  protected Set<String> deploymentsToRegister;

  public RegisterProcessApplicationCmd(String deploymentId, ProcessApplicationReference reference) {
    this(Collections.singleton(deploymentId), reference);
  }

  public RegisterProcessApplicationCmd(Set<String> deploymentsToRegister, ProcessApplicationReference appReference) {
    this.deploymentsToRegister = deploymentsToRegister;
    reference = appReference;

  }

  public ProcessApplicationRegistration execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkCamundaAdmin();

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    final ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();

    return processApplicationManager.registerProcessApplicationForDeployments(deploymentsToRegister, reference);
  }

}
