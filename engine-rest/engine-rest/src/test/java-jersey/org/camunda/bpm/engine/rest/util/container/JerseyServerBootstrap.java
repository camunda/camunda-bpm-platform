package org.camunda.bpm.engine.rest.util.container;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ApplicationAdapter;
import com.sun.jersey.api.core.ResourceConfig;

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
      throw new ServerBootstrapException(e);
    }
  }

  private void setupServer(Application application) {
    ResourceConfig rc = new ApplicationAdapter(application);

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(ResourceConfig.FEATURE_TRACE, "true");
    rc.setPropertiesAndFeatures(properties);

    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));
    URI serverUri = UriBuilder.fromPath(ROOT_RESOURCE_PATH).scheme("http").host("localhost").port(port).build();
    try {
      server = GrizzlyServerFactory.createHttpServer(serverUri, rc);
    } catch (IllegalArgumentException e) {
      throw new ServerBootstrapException(e);
    } catch (NullPointerException e) {
      throw new ServerBootstrapException(e);
    } catch (IOException e) {
      throw new ServerBootstrapException(e);
    }
  }

  @Override
  public void stop() {
    server.stop();
  }
}
