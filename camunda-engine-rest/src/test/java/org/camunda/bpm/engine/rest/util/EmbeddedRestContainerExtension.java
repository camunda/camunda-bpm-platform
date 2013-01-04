package org.camunda.bpm.engine.rest.util;


import org.camunda.bpm.engine.rest.EmbeddedRestBootstrap;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public class EmbeddedRestContainerExtension implements LoadableExtension {

  private static final int PORT = 8080;
  protected static final String SERVER_ADDRESS = "http://localhost:";
  
  public void register(ExtensionBuilder builder) {

    builder.service(DeployableContainer.class, EmbeddedRestDeployableContainer.class);

  }

  public static class EmbeddedRestDeployableContainer implements DeployableContainer<EmbeddedRestContainerConfiguration> {
    
    @Inject @ContainerScoped
    private InstanceProducer<EmbeddedRestBootstrap> embeddedRestInstanceProducer;

    protected EmbeddedRestBootstrap embeddedRestBootstrap;

    public ProtocolMetaData deploy(Archive<?> arg0) throws DeploymentException {
      ProtocolMetaData data = new ProtocolMetaData();
      return data;
    }

    public void deploy(Descriptor arg0) throws DeploymentException {
    }

    public Class<EmbeddedRestContainerConfiguration> getConfigurationClass() {
      return EmbeddedRestContainerConfiguration.class;
    }

    public ProtocolDescription getDefaultProtocol() {
      return null;
    }

    public void setup(EmbeddedRestContainerConfiguration arg0) {
    }

    public void start() throws LifecycleException {
      
      embeddedRestBootstrap = new EmbeddedRestBootstrap(SERVER_ADDRESS, PORT);
      embeddedRestBootstrap.start();
      
      embeddedRestInstanceProducer.set(embeddedRestBootstrap);
      
    }

    public void stop() throws LifecycleException {
      
      embeddedRestBootstrap.stop();
    }

    public void undeploy(Archive<?> arg0) throws DeploymentException {
    }

    public void undeploy(Descriptor arg0) throws DeploymentException {
    }

  }

  public static class EmbeddedRestContainerConfiguration implements ContainerConfiguration {

    @Override
    public void validate()
        throws ConfigurationException {
      
    }

  } 
}
