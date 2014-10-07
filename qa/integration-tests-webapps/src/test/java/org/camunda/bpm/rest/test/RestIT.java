package org.camunda.bpm.rest.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.AbstractWebappIntegrationTest;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestIT extends AbstractWebappIntegrationTest {

  private static final String ENGINE_DEFAULT_PATH = "engine/default";

  private static final String PROCESS_DEFINITION_PATH = ENGINE_DEFAULT_PATH + "/process-definition";

  private static final String JOB_DEFINITION_PATH = ENGINE_DEFAULT_PATH + "/job-definition";

  private static final String TASK_PATH = ENGINE_DEFAULT_PATH + "/task";

  private final static Logger log = Logger.getLogger(RestIT.class.getName());

  protected String getApplicationContextPath() {
    return "engine-rest/";
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

    assertEquals(200, response.getStatus());

    JSONArray definitionsJson = response.getEntity(JSONArray.class);
    // invoice example
    assertEquals(1, definitionsJson.length());

    JSONObject definitionJson = definitionsJson.getJSONObject(0);

    assertEquals("invoice", definitionJson.getString("key"));
    assertEquals("http://www.omg.org/spec/BPMN/20100524/MODEL", definitionJson.getString("category"));
    assertEquals("invoice receipt", definitionJson.getString("name"));
    Assert.assertTrue(definitionJson.isNull("description"));
    Assert.assertTrue(definitionJson.getString("resource").contains("invoice.bpmn"));
    Assert.assertFalse(definitionJson.getBoolean("suspended"));

    response.close();

  }

  @Test
  public void assertJodaTimePresent() {
    log.info("Checking " + APP_BASE_PATH + TASK_PATH);

    WebResource resource = client.resource(APP_BASE_PATH + TASK_PATH);
    resource.queryParam("dueAfter", "2000-01-01T00-00-00");
    ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    assertEquals(200, response.getStatus());

    JSONArray definitionsJson = response.getEntity(JSONArray.class);
    // no tasks are found
    assertEquals(1, definitionsJson.length());

    response.close();
  }

  @Test
  public void testDelayedJobDefinitionSuspension() {
    log.info("Checking " + APP_BASE_PATH + JOB_DEFINITION_PATH + "/suspended");

    WebResource resource = client.resource(APP_BASE_PATH + JOB_DEFINITION_PATH + "/suspended");

    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("processDefinitionKey", "jobExampleProcess");
    requestBody.put("suspended", true);
    requestBody.put("includeJobs", true);
    requestBody.put("executionDate", "2014-08-25T13:55:45");

    ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).put(ClientResponse.class, requestBody);

    assertEquals(204, response.getStatus());
  }

}
