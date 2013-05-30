package org.camunda.bpm.rest.test;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;

import org.camunda.bpm.AbstractWebappIntegrationTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestIT extends AbstractWebappIntegrationTest {

  private static final String TEST_PACKAGE_PATH = "org/camunda/bpm/rest";
  private static final String CONFIG_PATH = "activiti.cfg.xml";
  private static final String SIMPLE_PROCESS_PATH = TEST_PACKAGE_PATH + "/simpleProcess.bpmn20.xml";

  private static final String ENGINES_PATH = "engine";
  private static final String PROCESS_DEFINITION_PATH = "engine/default/process-definition";

  private final static Logger log = Logger.getLogger(RestIT.class.getName());

  private ProcessEngine processEngine;
  private Deployment deployment;

  protected String getApplicationContextPath() {
    return "engine-rest/";
  }

  @Before
  public void setup() throws IOException {
    createProcessEngine();

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

  @Test
  public void testScenario() throws JSONException {
    
    // FIXME: cannot do this on JBoss AS7, see https://app.camunda.com/jira/browse/CAM-787    
    
    // get list of process engines
    // log.info("Checking " + APP_BASE_PATH + ENGINES_PATH);
    // WebResource resource = client.resource(APP_BASE_PATH + ENGINES_PATH);
    // ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    //
    // Assert.assertEquals(200, response.getStatus());
    //
    // JSONArray enginesJson = response.getEntity(JSONArray.class);
    // Assert.assertEquals(1, enginesJson.length());
    //
    // JSONObject engineJson = enginesJson.getJSONObject(0);
    // Assert.assertEquals("default", engineJson.getString("name"));
    //
    // response.close();

    // get process definitions for default engine
    log.info("Checking " + APP_BASE_PATH + PROCESS_DEFINITION_PATH);
    WebResource resource = client.resource(APP_BASE_PATH + PROCESS_DEFINITION_PATH);
    ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    Assert.assertEquals(200, response.getStatus());

    JSONArray definitionsJson = response.getEntity(JSONArray.class);
    // 2 = simple process + invoice example
    Assert.assertEquals(2, definitionsJson.length());

    JSONObject definitionJson = definitionsJson.getJSONObject(0);

    Assert.assertEquals("ExampleProcess", definitionJson.getString("key"));
    Assert.assertEquals("Examples", definitionJson.getString("category"));
    Assert.assertEquals("Example Process", definitionJson.getString("name"));
    Assert.assertTrue(definitionJson.isNull("description"));
    Assert.assertEquals(deployment.getId(), definitionJson.getString("deploymentId"));
    Assert.assertEquals(1, definitionJson.getInt("version"));
    Assert.assertEquals(SIMPLE_PROCESS_PATH, definitionJson.getString("resource"));
    Assert.assertTrue(definitionJson.isNull("diagram"));
    Assert.assertFalse(definitionJson.getBoolean("suspended"));

    ProcessDefinition definition =
        processEngine.getRepositoryService().createProcessDefinitionQuery().
        deploymentId(deployment.getId()).singleResult();

    Assert.assertEquals(definition.getId(), definitionJson.getString("id"));

    response.close();
  }

  @After
  public void tearDown() {
    processEngine.getRepositoryService().deleteDeployment(deployment.getId());
  }
}
