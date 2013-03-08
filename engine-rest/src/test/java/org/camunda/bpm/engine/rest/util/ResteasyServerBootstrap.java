package org.camunda.bpm.engine.rest.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.impl.ProcessEngineRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessInstanceServiceImpl;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.EngineQueryDtoGetReader;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

public class ResteasyServerBootstrap {

  private static final String PORT_PROPERTY = "rest.http.port";
  private static final String ROOT_RESOURCE_PATH = "/rest-test";
  
  private static final String PROPERTIES_FILE = "/testconfig.properties";
  
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
    Properties serverProperties = readProperties();
    int port = Integer.parseInt(serverProperties.getProperty(PORT_PROPERTY));
    
    server = new TJWSEmbeddedJaxrsServer();
    server.setRootResourcePath(ROOT_RESOURCE_PATH);
    
    server.setPort(port);
    server.getDeployment().getActualResourceClasses().add(ProcessDefinitionServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(ProcessInstanceServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(TaskRestServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(ProcessEngineRestServiceImpl.class);
    
    server.getDeployment().getActualProviderClasses().add(EngineQueryDtoGetReader.class);
    server.getDeployment().getActualProviderClasses().add(JacksonConfigurator.class);
    
    server.getDeployment().getActualProviderClasses().add(JacksonJsonProvider.class);
  }
  
  private Properties readProperties() {
    InputStream propStream = null;
    Properties properties = new Properties();
    
    try {
      propStream = AbstractRestServiceTest.class.getResourceAsStream(PROPERTIES_FILE);
      properties.load(propStream);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        propStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    return properties;
  }
}
