package org.camunda.bpm.engine.rest.util;

import java.util.Properties;

import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.impl.application.DefaultApplication;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;

public class ResteasyServerBootstrap extends EmbeddedServerBootstrap {

  private NettyJaxrsServer server;
  
  public ResteasyServerBootstrap() {
    setupServer(new DefaultApplication());
  }
  
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