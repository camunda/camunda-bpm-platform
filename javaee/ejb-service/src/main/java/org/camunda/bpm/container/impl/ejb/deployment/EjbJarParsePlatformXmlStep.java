package org.camunda.bpm.container.impl.ejb.deployment;

import org.camunda.bpm.container.impl.jmx.deployment.AbstractParseBpmPlatformXmlStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;

import java.net.URL;

/**
 * <p>Deployment Step that reads the bpm-platform.xml from the classpath</p>
 * 
 * @author Daniel Meyer
 * @author Christian Lipphardt
 *
 */
public class EjbJarParsePlatformXmlStep extends AbstractParseBpmPlatformXmlStep {

  protected URL getBpmPlatformXmlStream(MBeanDeploymentOperation operationContext) {
    return lookupBpmPlatformXml();
  }

}
