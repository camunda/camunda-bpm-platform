package com.camunda.fox.platform.qa.deployer.testextension;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class FoxExtensionTesterArchiveAppender extends CachedAuxilliaryArchiveAppender {

  @Override
  protected Archive<?> buildArchive() {
    return ShrinkWrap
        .create(JavaArchive.class, "arquillian-fox-extension-tester.jar")
           .addPackages(true, Filters.exclude(FoxExtensionTesterArchiveAppender.class, FoxExtensionTester.class), this.getClass().getPackage())
           .addPackages(true, "org.fest")
           .addAsServiceProvider(RemoteLoadableExtension.class, FoxExtensionRemoteTester.class);
  }
}
