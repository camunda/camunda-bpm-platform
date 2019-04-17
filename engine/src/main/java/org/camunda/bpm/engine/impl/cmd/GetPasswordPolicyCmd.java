package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;

import org.camunda.bpm.engine.identity.PasswordPolicy;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Miklas Boskamp
 */
public class GetPasswordPolicyCmd implements Command<PasswordPolicy>, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public PasswordPolicy execute(CommandContext commandContext) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    if (!processEngineConfiguration.isDisablePasswordPolicy()) {
      return processEngineConfiguration.getPasswordPolicy();
    } else {
      return null;
    }
  }
}