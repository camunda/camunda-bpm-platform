package org.camunda.bpm.engine.impl.persistence.deploy;

import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cmd.UnregisterDeploymentCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class DeploymentFailListener implements TransactionListener {

  protected String deploymentId;
  
  public DeploymentFailListener(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  public void execute(CommandContext commandContext) {
    new UnregisterDeploymentCmd(deploymentId).execute(commandContext);
  }

}
