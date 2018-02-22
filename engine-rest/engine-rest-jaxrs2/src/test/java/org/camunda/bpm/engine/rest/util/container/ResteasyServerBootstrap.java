package org.camunda.bpm.engine.rest.util.container;

import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;

import javax.ws.rs.core.Application;
import java.util.Properties;

public class ResteasyServerBootstrap extends EmbeddedServerBootstrap {

  private NettyJaxrsServer server;

  public ResteasyServerBootstrap(Application application) {
    setupServer(application);
  }

  public void start() {
    server.start();
  }

  public void stop() {
    server.stop();
  }

  private void setupServer(Application application) {
    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));

    server = new NettyJaxrsServer();

    server.setRootResourcePath(ROOT_RESOURCE_PATH);
    server.setPort(port);

    server.getDeployment().setApplication(application);
  }

}
