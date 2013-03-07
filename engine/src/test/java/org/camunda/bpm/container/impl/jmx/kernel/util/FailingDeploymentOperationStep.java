package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;

/**
 * Deployment operation step that throws an exception.
 * 
 * @author Daniel Meyer
 *
 */
public class FailingDeploymentOperationStep extends MBeanDeploymentOperationStep {

  @Override
  public String getName() {
    return "failing step";
  }

  @Override
  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    throw new RuntimeException("Big time failure.");

  }

}
