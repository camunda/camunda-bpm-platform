package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Thorben Lindhauer
 */
public class UnregisterDeploymentCmd implements Command<Void> {

  protected String deploymentId;
  
  public UnregisterDeploymentCmd(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  public Void execute(CommandContext commandContext) {
    Context.getProcessEngineConfiguration().getRegisteredDeployments().remove(deploymentId);
    return null;
  }

}
