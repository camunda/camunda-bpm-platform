package org.camunda.bpm.container.impl.jmx.deployment;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;

/**
 * <p>Deployment operation step that stop all process engines.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class StopProcessEnginesStep extends MBeanDeploymentOperationStep {
  
  public final static Logger LOGGER = Logger.getLogger(StopProcessEnginesStep.class.getName());

  public String getName() {
    return "Stopping process engines";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    List<ProcessEngine> processEngines = serviceContainer.getServiceValuesByType(ServiceTypes.PROCESS_ENGINE);
    
    for (ProcessEngine processEngine : processEngines) {
      stopProcessEngine(processEngine);      
    }
    
  }

  /**
   * Stops  a process engine, failures are logged but no exceptions are thrown. 
   * 
   * @param processEngine
   */
  protected void stopProcessEngine(ProcessEngine processEngine) {
    
    try {
      
      // closing the process eninge makes sure it unregristers with the service container.
      processEngine.close();
      
    } catch(Throwable t) {
      LOGGER.log(Level.WARNING, "Exception while stopping process engine ", t);      
    }
    
  }

}
