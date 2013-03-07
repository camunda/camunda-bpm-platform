package org.camunda.bpm.engine.rest.util;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class EmbeddedRestContainerExtension implements LoadableExtension {

  @Override
  public void register(ExtensionBuilder builder) {
    builder.service(DeployableContainer.class, EmbeddedRestDeployableContainer.class);
  }

}
