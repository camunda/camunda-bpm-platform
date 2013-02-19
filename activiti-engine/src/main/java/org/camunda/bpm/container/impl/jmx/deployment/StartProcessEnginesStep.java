package org.camunda.bpm.container.impl.jmx.deployment;

import static org.camunda.bpm.container.impl.jmx.deployment.Attachments.PROCESSES_XML_RESOURCES;

import java.net.URL;
import java.util.Map;

import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;

/**
 * <p>Deployment operation step that is responsible for starting all process
 * engines declared in the processes.xml file.</p>
 * 
 * <p>This step does not start the process engines directly but rather creates
 * individual {@link StartProcessEngineStep} instances that each start a process
 * engine.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class StartProcessEnginesStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Start process engines";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    Map<URL, ProcessesXml> processesXmls = operationContext.getAttachment(PROCESSES_XML_RESOURCES);
    
    for (ProcessesXml processesXml : processesXmls.values()) {
      for (ProcessEngineXml parsedProcessEngine : processesXml.getProcessEngines()) {
        
        // for each process engine add a new deployment step
        operationContext.addStep(new StartProcessEngineStep(parsedProcessEngine));
      }
    }

  }

}
