package org.camunda.bpm.container.impl.jmx.deployment;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxProcessApplication;

/**
 * <p>Deployment operations step responsible for removing the {@link JmxProcessApplication} service.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class StopProcessApplicationServiceStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Removing process application"; 
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    final ProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);    
    
    // remove the service
    serviceContainer.stopService(ServiceTypes.PROCESS_APPLICATION, processApplication.getName());
  }

}
