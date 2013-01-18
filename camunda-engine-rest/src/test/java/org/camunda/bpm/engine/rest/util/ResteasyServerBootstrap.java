package org.camunda.bpm.engine.rest.util;

import org.camunda.bpm.engine.rest.impl.ProcessDefinitionServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessInstanceServiceImpl;
import org.camunda.bpm.engine.rest.mapper.ProcessDefinitionQueryDtoReader;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

public class ResteasyServerBootstrap {

  private static final int PORT = 8080;
  private static final String ROOT_RESOURCE_PATH = "/rest-test";
  
  private TJWSEmbeddedJaxrsServer server;
  
  public ResteasyServerBootstrap() {
    setupServer();
  }
  
  public void start() {
    server.start();
  }
  
  public void stop() {
    server.stop();
  }
  
  private void setupServer() {
    server = new TJWSEmbeddedJaxrsServer();
    server.setRootResourcePath(ROOT_RESOURCE_PATH);
    server.setPort(PORT);
    server.getDeployment().getActualResourceClasses().add(ProcessDefinitionServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(ProcessInstanceServiceImpl.class);
    server.getDeployment().getActualProviderClasses().add(ProcessDefinitionQueryDtoReader.class);
    
    server.getDeployment().getActualProviderClasses().add(JacksonJaxbJsonProvider.class);
  }
}
