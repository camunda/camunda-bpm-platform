package org.camunda.bpm.engine.rest.util.container;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JerseyServerBootstrap extends EmbeddedServerBootstrap {

  private HttpServer server;

  public JerseyServerBootstrap() {
    setupServer(new JaxrsApplication());
  }

  public JerseyServerBootstrap(Application application) {
    setupServer(application);
  }

  @Override
  public void start() {
    try {
      server.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void setupServer(Application application) {
    ResourceConfig rc = ResourceConfig.forApplication(application);

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(ServerProperties.TRACING, Boolean.TRUE);
    rc.addProperties(properties);

    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));
    URI serverUri = UriBuilder.fromPath(ROOT_RESOURCE_PATH).scheme("http").host("localhost").port(port).build();
    try {
      server = GrizzlyHttpServerFactory.createHttpServer(serverUri, rc);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void stop() {
    server.shutdownNow();
  }
}
