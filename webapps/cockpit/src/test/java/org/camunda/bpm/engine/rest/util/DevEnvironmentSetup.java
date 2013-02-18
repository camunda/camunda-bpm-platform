package org.camunda.bpm.engine.rest.util;

import java.io.File;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessInstanceServiceImpl;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.EngineQueryDtoGetReader;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

/**
 * Enviroment with process engine and embedded HTTP Server to be used in development.
 * 
 * @author Daniel Meyer
 * 
 */
public class DevEnvironmentSetup implements ProcessEngineProvider {

  private static ProcessEngine processEngine;

  public static void main(String[] args) {
    createProcessEngine();
    createDemoData();
    startEmbeddedHttpServer();
  }
  
  protected static void createProcessEngine() {
    processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
  }


  private static void createDemoData() {
   // TODO
  }

  private static void startEmbeddedHttpServer() {
    
    TJWSEmbeddedJaxrsServer server = new TJWSEmbeddedJaxrsServer();

    server.setRootResourcePath("/camunda-engine-rest");
    server.addFileMapping("/cockpit", new File("./src/main/webapp"));

    server.setPort(8081);

    server.getDeployment().getActualResourceClasses().add(ProcessDefinitionServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(ProcessInstanceServiceImpl.class);
    server.getDeployment().getActualResourceClasses().add(TaskRestServiceImpl.class);

    server.getDeployment().getActualProviderClasses().add(EngineQueryDtoGetReader.class);
    server.getDeployment().getActualProviderClasses().add(JacksonConfigurator.class);

    server.getDeployment().getActualProviderClasses().add(JacksonJsonProvider.class);

    server.start();
    
  }


  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

}
