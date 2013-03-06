package org.camunda.bpm.container.impl.ejb.deployment;

import java.net.URL;

import org.camunda.bpm.container.impl.jmx.deployment.AbstractParseBpmPlatformXmlStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;

/**
 * <p>Deployment Step that reads the bpm-platform.xml from the classpath</p>
 * 
 * @author Daniel Meyer
 *
 */
public class EjbJarParsePlatformXmlStep extends AbstractParseBpmPlatformXmlStep {

  protected URL getBpmPlatformXmlStream(MBeanDeploymentOperation operationContext) {
    
    String bpmPlatformResourceLocation = operationContext.getAttachment(EjbJarAttachments.BPM_PLATFORM_RESOURCE);
    
    return ClassLoaderUtil.getClassloader(getClass()).getResource(bpmPlatformResourceLocation);
  }

}
