package org.camunda.bpm.engine.rest.util;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public class EmbeddedRestDeployableContainer implements DeployableContainer<EmbeddedRestContainerConfiguration> {

  @Inject @ContainerScoped
  private InstanceProducer<ResteasyServerBootstrap> embeddedRestInstanceProducer;
  private ResteasyServerBootstrap bootstrap;
  
  @Override
  public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
    ProtocolMetaData data = new ProtocolMetaData();
    return data;
  }

  @Override
  public void deploy(Descriptor arg0) throws DeploymentException {
  }

  @Override
  public Class<EmbeddedRestContainerConfiguration> getConfigurationClass() {
    return EmbeddedRestContainerConfiguration.class;
  }

  @Override
  public ProtocolDescription getDefaultProtocol() {
    return null;
  }

  @Override
  public void setup(EmbeddedRestContainerConfiguration configuration) {
    
  }

  @Override
  public void start() throws LifecycleException {
    bootstrap = new ResteasyServerBootstrap();
    bootstrap.start();
    
    embeddedRestInstanceProducer.set(bootstrap);
  }

  @Override
  public void stop() throws LifecycleException {
    bootstrap.stop();
  }

  @Override
  public void undeploy(Archive<?> archive) throws DeploymentException {
    
  }

  @Override
  public void undeploy(Descriptor archive) throws DeploymentException {
    
  }

}
