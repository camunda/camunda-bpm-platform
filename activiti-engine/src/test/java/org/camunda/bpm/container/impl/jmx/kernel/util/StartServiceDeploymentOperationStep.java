package org.camunda.bpm.container.impl.jmx.kernel.util;

import javax.management.ObjectName;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;

public class StartServiceDeploymentOperationStep extends MBeanDeploymentOperationStep {
  
  private TestService service;
  private ObjectName serviceName;

  public StartServiceDeploymentOperationStep(ObjectName serviceName, TestService service) {
    this.serviceName = serviceName;
    this.service = service;
  }

  public String getName() {
    return "start "+serviceName;
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    
    serviceContainer.startService(serviceName, service);
  }

}
