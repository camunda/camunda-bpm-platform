package org.camunda.bpm.engine.rest.util;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.camunda.bpm.engine.rest.impl.ProcessDefinitionRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessInstanceRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

public class JerseyServerBootstrap extends EmbeddedServerBootstrap {

  private HttpServer server;
  
  public JerseyServerBootstrap() {
    setupServer();
  }
  
  @Override
  public void start() {
    try {
      server.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void setupServer() {
    ResourceConfig rc = new ClassNamesResourceConfig(getResourceClasses());
    
    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));
    URI serverUri = UriBuilder.fromPath(ROOT_RESOURCE_PATH).scheme("http").host("localhost").port(port).build();
    try {
      server = GrizzlyServerFactory.createHttpServer(serverUri, rc);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (NullPointerException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void stop() {
    server.stop();
  }
  
  private Class<?>[] getResourceClasses() {
    Class<?>[] classes = new Class<?>[]{
      ProcessDefinitionRestServiceImpl.class,
      ProcessInstanceRestServiceImpl.class,
      TaskRestServiceImpl.class,
      ProcessEngineRestServiceImpl.class,
      
      JacksonConfigurator.class,
      JacksonJsonProvider.class
    };
    
    return classes;
  }

}
