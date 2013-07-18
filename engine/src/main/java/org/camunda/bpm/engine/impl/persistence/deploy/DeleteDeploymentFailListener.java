package org.camunda.bpm.engine.impl.persistence.deploy;

import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cmd.RegisterDeploymentCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class DeleteDeploymentFailListener implements TransactionListener {

  protected String deploymentId;
  
  public DeleteDeploymentFailListener(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  public void execute(CommandContext commandContext) {
    new RegisterDeploymentCmd(deploymentId).execute(commandContext);
  }

}
