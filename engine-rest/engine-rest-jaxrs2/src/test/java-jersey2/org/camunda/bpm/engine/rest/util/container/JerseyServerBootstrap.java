package org.camunda.bpm.engine.rest.util.container;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

public class JerseyServerBootstrap extends EmbeddedServerBootstrap {

  private HttpServer server;

  public JerseyServerBootstrap(Application application) {
    setupServer(application);
  }

  @Override
  public void start() {
    try {
      server.start();
    } catch (IOException e) {
      throw new ServerBootstrapException(e);
    }
  }

  private void setupServer(Application application) {
    ResourceConfig rc = ResourceConfig.forApplication(application);

    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));
    URI serverUri = UriBuilder.fromPath(ROOT_RESOURCE_PATH).scheme("http").host("localhost").port(port).build();

    final HttpServer grizzlyServer = GrizzlyHttpServerFactory.createHttpServer(serverUri, rc);
    try {
      grizzlyServer.start();
    } catch (IOException e) {
      e.printStackTrace();
    }

    server = grizzlyServer;

  }

  @Override
  public void stop() {
    server.stop();
  }
}
