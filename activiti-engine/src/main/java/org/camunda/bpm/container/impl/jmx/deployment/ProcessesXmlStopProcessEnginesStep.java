package org.camunda.bpm.container.impl.jmx.deployment;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxProcessApplication;

/**
 * <p>Deployment operation responsible for stopping all process engines started by the deployment.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ProcessesXmlStopProcessEnginesStep extends MBeanDeploymentOperationStep {
  
  protected final static Logger log = Logger.getLogger(ProcessesXmlStopProcessEnginesStep.class.getName());
  
  public String getName() {
    return "Stopping process engines";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    final ProcessApplication processApplication = operationContext.getAttachment(Attachments.PROCESS_APPLICATION);    
    final JmxProcessApplication deployedProcessApplication = serviceContainer.getServiceValue(ServiceTypes.PROCESS_APPLICATION, processApplication.getName());
    
    if(deployedProcessApplication == null) {
      throw new ActivitiException("Cannot find process application with name "+processApplication.getName()+".");
    }
    
    List<ProcessesXml> processesXmls = deployedProcessApplication.getProcessesXmls();
    for (ProcessesXml processesXml : processesXmls) {
      stopProcessEngines(processesXml.getProcessEngines(), operationContext);            
    }

  }

  protected void stopProcessEngines(List<ProcessEngineXml> processEngine, MBeanDeploymentOperation operationContext) {    
    for (ProcessEngineXml parsedProcessEngine : processEngine) {
      stopProcessEngine(parsedProcessEngine.getName(), operationContext);      
    }        
  }

  protected void stopProcessEngine(String processEngineName, MBeanDeploymentOperation operationContext) {    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();    
    
    ProcessEngine processEngine = serviceContainer.getServiceValue(ServiceTypes.PROCESS_ENGINE, processEngineName);
    if(processEngine != null) {
      processEngine.close();
      
    } else {
      log.log(Level.INFO, "Cannot stop process engine; no such process engine registered.");
      
    }
    
  }

}
