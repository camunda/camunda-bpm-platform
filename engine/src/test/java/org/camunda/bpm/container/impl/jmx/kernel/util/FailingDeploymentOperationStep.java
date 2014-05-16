package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;

/**
 * Deployment operation step that throws an exception.
 * 
 * @author Daniel Meyer
 *
 */
public class FailingDeploymentOperationStep extends DeploymentOperationStep {

  @Override
  public String getName() {
    return "failing step";
  }

  @Override
  public void performOperationStep(DeploymentOperation operationContext) {
    
    throw new RuntimeException("Big time failure.");

  }

}
