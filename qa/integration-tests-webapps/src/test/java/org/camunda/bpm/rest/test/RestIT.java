package org.camunda.bpm.rest.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.camunda.bpm.cycle.util.IoUtil;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.ApacheHttpClient4;

public class RestIT {

  private static final String TEST_PACKAGE_PATH = "org/camunda/bpm/rest";
  private static final String CONFIG_PATH = "activiti.cfg.xml";
  private static final String SIMPLE_PROCESS_PATH = TEST_PACKAGE_PATH + "/simpleProcess.bpmn20.xml";
  
  private static String BASE_PATH;
  private static final String HOST_NAME = "localhost";
  
  private static final String ENGINES_PATH = "/engine";
  private static final String PROCESS_DEFINITION_PATH = "/engine/default/process-definition";
  
  private final static Logger log = Logger.getLogger(RestIT.class.getName());  
  
  private ProcessEngine processEngine;
  private Deployment deployment;
  private ApacheHttpClient4 client;
  
  @Before
  public void setup() throws IOException {
    createProcessEngine();
    createHttpClient();
    
    RepositoryService repoService = processEngine.getRepositoryService();
    deployment = repoService.createDeployment().addClasspathResource(SIMPLE_PROCESS_PATH).deploy();
  }
  
  private void createProcessEngine() {
    if (processEngine == null) {
      processEngine = ProcessEngineConfiguration
              .createProcessEngineConfigurationFromResource(CONFIG_PATH)
              .buildProcessEngine();
    }
  }
  
  private void createHttpClient() throws IOException {
    Properties properties = new Properties();
    
    InputStream propertiesStream = null;
    String httpPort;
    try {
      propertiesStream = RestIT.class.getResourceAsStream("/testconfig.properties");
      properties.load(propertiesStream);
      httpPort = (String) properties.get("http.port");
    } finally {
      IoUtil.closeSilently(propertiesStream);
    }
    
    BASE_PATH = "http://" + HOST_NAME + ":" + httpPort + "/engine-rest";
    log.info("Connecting to REST API at " + BASE_PATH);
    
    client = ApacheHttpClient4.create();
  }
  
  @Test
  public void testScenario() {
    // get list of process engines
    log.info("Checking " + BASE_PATH + ENGINES_PATH);
    WebResource resource = client.resource(BASE_PATH + ENGINES_PATH);
    ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    
    Assert.assertEquals(200, response.getStatus());
    
    response.close();
    
    // get process definitions for default engine
    log.info("Checking " + BASE_PATH + PROCESS_DEFINITION_PATH);
    resource = client.resource(BASE_PATH + PROCESS_DEFINITION_PATH);
    response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    
    Assert.assertEquals(200, response.getStatus());
    
    response.close();
  }
  
  @After
  public void tearDown() {
    processEngine.getRepositoryService().deleteDeployment(deployment.getId());
  }
}
