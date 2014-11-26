package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;

public class StartServiceDeploymentOperationStep extends DeploymentOperationStep {

  private TestService service;
  private String serviceName;

  public StartServiceDeploymentOperationStep(String serviceName, TestService service) {
    this.serviceName = serviceName;
    this.service = service;
  }

  public String getName() {
    return "start "+serviceName;
  }

  public void performOperationStep(DeploymentOperation operationContext) {
    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();

    serviceContainer.startService(serviceName, service);
  }

}
