package org.camunda.bpm.container.impl.jmx.deployment;

import static org.camunda.bpm.container.impl.jmx.deployment.Attachments.PROCESSES_XML_RESOURCES;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;

/**
 * <p>Deployment step responsible for creating individual {@link DeployProcessArchiveStep} 
 * instances for each process archive configured in the META-INF/processes.xml file.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class DeployProcessArchivesStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Deploy process archvies";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {

    Map<URL, ProcessesXml> processesXmls = operationContext.getAttachment(PROCESSES_XML_RESOURCES);

    for (Entry<URL, ProcessesXml> processesXml : processesXmls.entrySet()) {
      for (ProcessArchiveXml processArchive : processesXml.getValue().getProcessArchives()) {

        // for each process archive add an individual operation step
        operationContext.addStep(new DeployProcessArchiveStep(processArchive, processesXml.getKey()));
      }
    }

  }

}
