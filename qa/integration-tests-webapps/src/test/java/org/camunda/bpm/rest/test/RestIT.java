package org.camunda.bpm.rest.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.AbstractWebappIntegrationTest;
import org.camunda.bpm.engine.rest.hal.Hal;
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

  private static final String FILTER_PATH = ENGINE_DEFAULT_PATH + "/filter";

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
    assertEquals("Invoice Receipt", definitionJson.getString("name"));
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
    assertEquals(3, definitionsJson.length());

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

  @Test
  public void testTaskQueryContentType() {
    String resourcePath = APP_BASE_PATH + TASK_PATH;
    log.info("Checking " + resourcePath);
    assertMediaTypesOfResource(resourcePath, false);
  }

  @Test
  public void testSingleTaskContentType() throws JSONException {
    // get id of first task
    String taskId = getFirstTask().getString("id");

    String resourcePath = APP_BASE_PATH + TASK_PATH + "/" + taskId;
    log.info("Checking " + resourcePath);
    assertMediaTypesOfResource(resourcePath, false);
  }

  @Test
  public void testTaskFilterResultContentType() throws JSONException {
    // create filter for first task, so single result will not throw an exception
    JSONObject firstTask = getFirstTask();
    Map<String, Object> query = new HashMap<String, Object>();
    query.put("taskDefinitionKey", firstTask.getString("taskDefinitionKey"));
    query.put("processInstanceId", firstTask.getString("processInstanceId"));
    Map<String, Object> filter = new HashMap<String, Object>();
    filter.put("resourceType", "Task");
    filter.put("name", "IT Test Filter");
    filter.put("query", query);

    ClientResponse response = client.resource(APP_BASE_PATH + FILTER_PATH + "/create").accept(MediaType.APPLICATION_JSON)
      .entity(filter, MediaType.APPLICATION_JSON_TYPE)
      .post(ClientResponse.class);
    assertEquals(200, response.getStatus());
    String filterId = response.getEntity(JSONObject.class).getString("id");
    response.close();

    String resourcePath = APP_BASE_PATH + FILTER_PATH + "/" + filterId + "/list";
    log.info("Checking " + resourcePath);
    assertMediaTypesOfResource(resourcePath, true);

    resourcePath = APP_BASE_PATH + FILTER_PATH + "/" + filterId + "/singleResult";
    log.info("Checking " + resourcePath);
    assertMediaTypesOfResource(resourcePath, true);

    // delete test filter
    response = client.resource(APP_BASE_PATH + FILTER_PATH + "/" + filterId ).delete(ClientResponse.class);
    assertEquals(204, response.getStatus());
    response.close();
  }

  protected JSONObject getFirstTask() throws JSONException {
    ClientResponse response = client.resource(APP_BASE_PATH + TASK_PATH).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    JSONArray tasks = response.getEntity(JSONArray.class);
    JSONObject firstTask = tasks.getJSONObject(0);
    response.close();
    return firstTask;
  }

  protected void assertMediaTypesOfResource(String resourcePath, boolean postSupported) {
    WebResource resource = client.resource(resourcePath);
    assertMediaTypes(resource, postSupported, MediaType.APPLICATION_JSON_TYPE);
    assertMediaTypes(resource, postSupported, MediaType.APPLICATION_JSON_TYPE, MediaType.WILDCARD);
    assertMediaTypes(resource, postSupported, MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON);
    assertMediaTypes(resource, postSupported, Hal.APPLICATION_HAL_JSON_TYPE, Hal.APPLICATION_HAL_JSON);
    assertMediaTypes(resource, postSupported, Hal.APPLICATION_HAL_JSON_TYPE, Hal.APPLICATION_HAL_JSON, MediaType.APPLICATION_JSON + "; q=0.5");
    assertMediaTypes(resource, postSupported, MediaType.APPLICATION_JSON_TYPE, Hal.APPLICATION_HAL_JSON + "; q=0.5", MediaType.APPLICATION_JSON);
    assertMediaTypes(resource, postSupported, MediaType.APPLICATION_JSON_TYPE, Hal.APPLICATION_HAL_JSON + "; q=0.5 ", MediaType.APPLICATION_JSON + "; q=0.6");
    assertMediaTypes(resource, postSupported, Hal.APPLICATION_HAL_JSON_TYPE, Hal.APPLICATION_HAL_JSON + "; q=0.6", MediaType.APPLICATION_JSON + "; q=0.5");
  }

  protected void assertMediaTypes(WebResource resource, boolean postSupported, MediaType expectedMediaType, String... acceptMediaTypes) {
    // test GET request
    ClientResponse response = resource.accept(acceptMediaTypes).get(ClientResponse.class);
    assertMediaType(response, expectedMediaType);
    response.close();

    if (postSupported) {
      // test POST request
      response = resource.accept(acceptMediaTypes).entity(Collections.EMPTY_MAP, MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class);
      assertMediaType(response, expectedMediaType);
      response.close();
    }
  }

  protected void assertMediaType(ClientResponse response, MediaType expected) {
    MediaType actual = response.getType();
    assertEquals(200, response.getStatus());
    // use startsWith cause sometimes server also returns quality parameters (e.g. websphere/wink)
    assertTrue("Expected: " + expected + " Actual: " + actual, actual.toString().startsWith(expected.toString()));
  }

}
