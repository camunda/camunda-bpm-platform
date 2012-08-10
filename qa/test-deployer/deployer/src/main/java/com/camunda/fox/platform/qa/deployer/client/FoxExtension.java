package com.camunda.fox.platform.qa.deployer.client;

import com.camunda.fox.platform.qa.deployer.deployment.FoxDynamicDependencyAppender;
import com.camunda.fox.platform.qa.deployer.deployment.FoxExtensionArchiveAppender;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class FoxExtension implements LoadableExtension {

  public void register(ExtensionBuilder builder) {
    builder.service(ApplicationArchiveProcessor.class, FoxDynamicDependencyAppender.class)
           .service(AuxiliaryArchiveAppender.class, FoxExtensionArchiveAppender.class)
           .observer(FoxDeploymentConfigurationProducer.class)
           .observer(FoxConfigurationProducer.class);
  }
}
