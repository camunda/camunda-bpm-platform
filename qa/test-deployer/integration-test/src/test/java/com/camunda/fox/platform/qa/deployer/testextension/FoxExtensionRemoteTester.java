package com.camunda.fox.platform.qa.deployer.testextension;

import com.camunda.fox.platform.qa.deployer.testextension.event.FoxEventObserver;
import com.camunda.fox.platform.qa.deployer.testextension.event.FoxEventVerifierProducer;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class FoxExtensionRemoteTester implements RemoteLoadableExtension {

  @Override
  public void register(ExtensionBuilder builder) {
    builder.service(ResourceProvider.class, FoxEventVerifierProducer.class);
    builder.observer(FoxEventObserver.class);
  }
}
