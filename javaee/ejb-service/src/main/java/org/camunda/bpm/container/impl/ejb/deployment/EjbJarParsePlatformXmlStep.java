package org.camunda.bpm.container.impl.ejb.deployment;

import java.net.URL;

import org.camunda.bpm.container.impl.deployment.AbstractParseBpmPlatformXmlStep;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;

/**
 * <p>Deployment Step that reads the bpm-platform.xml from the classpath</p>
 *
 * @author Daniel Meyer
 * @author Christian Lipphardt
 *
 */
public class EjbJarParsePlatformXmlStep extends AbstractParseBpmPlatformXmlStep {

  public URL getBpmPlatformXmlStream(DeploymentOperation operationContext) {
    return lookupBpmPlatformXml();
  }

}
