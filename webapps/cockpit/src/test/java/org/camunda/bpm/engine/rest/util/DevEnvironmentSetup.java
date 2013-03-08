package org.camunda.bpm.engine.rest.util;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessInstanceServiceImpl;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.EngineQueryDtoGetReader;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

/**
 * Enviroment with process engine and embedded HTTP Server to be used in development.
 * 
 * @author Daniel Meyer
 * 
 */
public class DevEnvironmentSetup implements ProcessEngineProvider {

  private static final int numOfProcessesPerDefinition = 10;
  private static ProcessEngine processEngine;

  public static void main(String[] args) {
    createProcessEngine();
    createDemoData();
    startEmbeddedHttpServer();
  }
  
  protected static void createProcessEngine() {
    ProcessEngineConfiguration processEngineConfiguration = 
        ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
    // use UUIDs
    ((ProcessEngineConfigurationImpl)processEngineConfiguration).setIdGenerator(new StrongUuidGenerator());
    processEngine = processEngineConfiguration.buildProcessEngine();
  }


  private static void createDemoData() {
    RepositoryService repositoryService = processEngine.getRepositoryService();
    
    repositoryService
      .createDeployment()
      .addClasspathResource("processes/fox-invoice_en.bpmn")
      .addClasspathResource("processes/fox-invoice_en_long_id.bpmn")
      .addClasspathResource("processes/collaboration_scroll.bpmn")
      .addClasspathResource("processes/newBpmnDiagram_1.bpmn")
      .deploy();
    
    RuntimeService runtimeService = processEngine.getRuntimeService();
    List<ProcessDefinition> pds = repositoryService.createProcessDefinitionQuery().list();
    for (ProcessDefinition pd : pds) {
      System.out.println("ProcessDefinition - id: " + pd.getId() +  ", key: " + pd.getKey() + ", name: " + pd.getName());
      for (int i = 0; i < numOfProcessesPerDefinition; i++) {
        ProcessInstance pi = runtimeService.startProcessInstanceById(pd.getId());
      }
    }
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

  public ProcessEngine getDefaultProcessEngine() {
    return processEngine;
  }

  public ProcessEngine getProcessEngine(String name) {
    return processEngine;
  }

  public Set<String> getProcessEngineNames() {
    Set<String> names = new HashSet<String>();
    names.add(processEngine.getName());
    return names;
  }

}
