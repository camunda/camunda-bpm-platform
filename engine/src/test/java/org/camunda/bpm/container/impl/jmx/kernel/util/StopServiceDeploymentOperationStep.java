package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;

public class StopServiceDeploymentOperationStep extends DeploymentOperationStep {

  private String serviceName;

  public StopServiceDeploymentOperationStep(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getName() {
    return "stop "+serviceName;
  }

  public void performOperationStep(DeploymentOperation operationContext) {
    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();

    serviceContainer.stopService(serviceName);
  }

}
