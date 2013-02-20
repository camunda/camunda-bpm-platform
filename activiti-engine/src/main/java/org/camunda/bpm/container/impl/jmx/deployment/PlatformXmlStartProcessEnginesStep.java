package org.camunda.bpm.container.impl.jmx.deployment;

import static org.camunda.bpm.container.impl.jmx.deployment.Attachments.BPM_PLATFORM_XML;

import java.util.List;

import org.camunda.bpm.application.impl.deployment.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;

/**
 * <p> Retrieves the List of ProcessEngines frm an attached {@link ProcessesXml}.</p>
 * 
 * @see AbstractParseBpmPlatformXmlStep 
 *  
 */
public class PlatformXmlStartProcessEnginesStep extends AbstractStartProcessEnginesStep {

  protected List<ProcessEngineXml> getProcessEnginesXmls(MBeanDeploymentOperation operationContext) {
    
    final BpmPlatformXml bpmPlatformXml = operationContext.getAttachment(BPM_PLATFORM_XML);
    
    return bpmPlatformXml.getProcessEngines();
  }

}
