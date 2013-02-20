package org.camunda.bpm.container.impl.jmx.deployment;

import java.net.URL;

import org.camunda.bpm.application.impl.deployment.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.application.impl.deployment.parser.BpmPlatformXmlParser;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;

/**
 * <p>Deployment operation step responsible for parsing and attaching the bpm-platform.xml file.</p>
 * 
 * @author Daniel Meyer
 *
 */
public abstract class AbstractParseBpmPlatformXmlStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Parsing bpm-platform.xml file";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    URL bpmPlatformXmlSource = getBpmPlatformXmlStream();
    
    // parse the bpm platform xml
    BpmPlatformXml bpmPlatformXml = new BpmPlatformXmlParser().createParse()
      .sourceUrl(bpmPlatformXmlSource)
      .execute()
      .getBpmPlatformXml();
    
    // attach to operation context
    operationContext.addAttachment(Attachments.BPM_PLATFORM_XML, bpmPlatformXml);     
    
  }

  protected abstract URL getBpmPlatformXmlStream();

}
