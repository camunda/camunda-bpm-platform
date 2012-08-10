package com.camunda.fox.platform.qa.deployer.testextension;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class FoxExtensionTester implements LoadableExtension {

  @Override
  public void register(ExtensionBuilder builder) {
    builder.service(AuxiliaryArchiveAppender.class, FoxExtensionTesterArchiveAppender.class);
  }
}
