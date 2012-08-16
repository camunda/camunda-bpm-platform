package com.camunda.fox.platform.qa.deployer.container;

import com.camunda.fox.platform.qa.deployer.lifecycle.FoxTestHandler;
import com.camunda.fox.platform.qa.deployer.fox.ApplicationArchiveContextProducer;
import com.camunda.fox.platform.qa.deployer.fox.ProcessArchiveServiceProducer;
import com.camunda.fox.platform.qa.deployer.lifecycle.ProcessDeploymentHandler;
import com.camunda.fox.platform.qa.deployer.fox.TestProcessDeploymentProducer;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;

/**
 * Defines all the bindings for the Fox Arquillian extension run in the
 * container.
 *
 * @author nico.rehwaldt@camunda.com
 */
public class RemoteFoxExtension implements RemoteLoadableExtension {

  @Override
  public void register(ExtensionBuilder builder) {
    builder.observer(RemoteFoxConfigurationProducer.class)
           .observer(RemoteFoxDeploymentConfigurationProducer.class)
           .observer(ProcessArchiveServiceProducer.class)
           .observer(FoxTestHandler.class)
           
           .observer(ApplicationArchiveContextProducer.class)
           .observer(TestProcessDeploymentProducer.class)
           .observer(ProcessDeploymentHandler.class);
  }
}
