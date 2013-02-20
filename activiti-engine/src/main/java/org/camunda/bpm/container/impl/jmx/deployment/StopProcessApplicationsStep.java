package org.camunda.bpm.container.impl.jmx.deployment;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;

/**
 * <p>Deployment operation step that is responsible for stopping all process applications</p>
 * 
 * @author Daniel Meyer
 *
 */
public class StopProcessApplicationsStep extends MBeanDeploymentOperationStep {
  
  public final static Logger LOGGER = Logger.getLogger(StopProcessApplicationsStep.class.getName());

  public String getName() {
    return "Stopping process applications";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    List<ProcessApplication> processApplications = serviceContainer.getServiceValuesByType(ServiceTypes.PROCESS_APPLICATION);
    
    for (ProcessApplication processApplication : processApplications) {
      stopProcessApplication(processApplication);      
    }

  }

  /**
   * <p> Stops a process application. Exceptions are logged but not rethrown).
   * 
   * @param processApplication
   */
  protected void stopProcessApplication(ProcessApplication processApplication) {
    
    try {
      
      // unless the user has overridden the stop behavior, 
      // this causes the process application to remove its services. 
      processApplication.stop();
      
    } catch(Throwable t) {
      LOGGER.log(Level.WARNING, "Exception while stopping ProcessApplication ", processApplication);
    }
            
  }

}
