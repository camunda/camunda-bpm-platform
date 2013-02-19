package org.camunda.bpm.container.impl.jmx.deployment;

import java.util.List;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.deployment.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.deployment.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxProcessApplication;

/**
 * <p>Deployment operation responsible for undeploying all process archives.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class UndeployProcessArchivesStep extends MBeanDeploymentOperationStep {
  
  protected final static Logger log = Logger.getLogger(UndeployProcessArchivesStep.class.getName());
  
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
      for (ProcessArchiveXml parsedProcessArchive : processesXml.getProcessArchives()) {
        operationContext.addStep(new UndeployProcessArchiveStep(deployedProcessApplication, parsedProcessArchive));        
      }      
    }
    
  }
  
}
