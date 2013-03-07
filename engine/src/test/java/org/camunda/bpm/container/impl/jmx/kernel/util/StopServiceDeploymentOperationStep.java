package org.camunda.bpm.container.impl.jmx.kernel.util;

import javax.management.ObjectName;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;

public class StopServiceDeploymentOperationStep extends MBeanDeploymentOperationStep {
  
  private ObjectName serviceName;

  public StopServiceDeploymentOperationStep(ObjectName serviceName) {
    this.serviceName = serviceName;
  }

  public String getName() {
    return "stop "+serviceName;
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    
    serviceContainer.stopService(serviceName);
  }

}
