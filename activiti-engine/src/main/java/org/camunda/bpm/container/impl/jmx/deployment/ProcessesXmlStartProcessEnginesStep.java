package org.camunda.bpm.container.impl.jmx.deployment;

import static org.camunda.bpm.container.impl.jmx.deployment.Attachments.PROCESSES_XML_RESOURCES;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;

/**
 * <p> Retrieves the List of ProcessEngines frm an attached {@link ProcessesXml}.</p>
 * 
 * @see AbstractParseBpmPlatformXmlStep 
 *  
 */
public class ProcessesXmlStartProcessEnginesStep extends AbstractStartProcessEnginesStep {

  protected List<ProcessEngineXml> getProcessEnginesXmls(MBeanDeploymentOperation operationContext) {

    final Map<URL, ProcessesXml> processesXmls = operationContext.getAttachment(PROCESSES_XML_RESOURCES);

    List<ProcessEngineXml> processEngines = new ArrayList<ProcessEngineXml>();

    for (ProcessesXml processesXml : processesXmls.values()) {
      processEngines.addAll(processesXml.getProcessEngines());

    }

    return processEngines;
  }

}
