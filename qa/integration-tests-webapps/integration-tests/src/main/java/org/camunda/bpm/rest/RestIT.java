/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.rest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.camunda.bpm.AbstractWebIntegrationTest;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class RestIT extends AbstractWebIntegrationTest {

  private static final String ENGINE_DEFAULT_PATH = "engine/default";

  private static final String PROCESS_DEFINITION_PATH = ENGINE_DEFAULT_PATH + "/process-definition";

  private static final String JOB_DEFINITION_PATH = ENGINE_DEFAULT_PATH + "/job-definition";

  private static final String TASK_PATH = ENGINE_DEFAULT_PATH + "/task";

  private static final String FILTER_PATH = ENGINE_DEFAULT_PATH + "/filter";

  private static final String HISTORIC_DETAIL_PATH = ENGINE_DEFAULT_PATH + "/history/detail";

  private static final String PROCESS_INSTANCE_PATH = ENGINE_DEFAULT_PATH + "/process-instance";


  private final static Logger log = Logger.getLogger(RestIT.class.getName());

  @Before
  public void createClient() throws Exception {
    preventRaceConditions();
    createClient(getRestCtxPath());
  }

  @Test
  public void testScenario() throws JSONException {
    // get process definitions for default engine
    log.info("Checking " + APP_BASE_PATH + PROCESS_DEFINITION_PATH);
    WebResource resource = client.resource(APP_BASE_PATH + PROCESS_DEFINITION_PATH);
    ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    assertEquals(200, response.getStatus());

    JSONArray definitionsJson = response.getEntity(JSONArray.class);
    response.close();

    // invoice example
    assertEquals(3, definitionsJson.length());

    // order of results is not consistent between database types
    for (int i = 0; i < definitionsJson.length(); i++) {
      JSONObject definitionJson = definitionsJson.getJSONObject(i);
      assertTrue(definitionJson.isNull("description"));
      assertFalse(definitionJson.getBoolean("suspended"));
      if (definitionJson.getString("key").equals("ReviewInvoice")) {
        assertEquals("http://bpmn.io/schema/bpmn", definitionJson.getString("category"));
        assertEquals("Review Invoice", definitionJson.getString("name"));
        assertTrue(definitionJson.getString("resource").equals("reviewInvoice.bpmn"));
      } else if (definitionJson.getString("key").equals("invoice")) {
        assertEquals("http://www.omg.org/spec/BPMN/20100524/MODEL", definitionJson.getString("category"));
        assertEquals("Invoice Receipt", definitionJson.getString("name"));
        assertTrue(definitionJson.getString("resource").matches("invoice\\.v[1,2]\\.bpmn"));
      } else {
        fail("Unexpected definition key in response JSON.");
      }
    }
  }

  @Test
  public void assertJodaTimePresent() {
    log.info("Checking " + APP_BASE_PATH + TASK_PATH);

    WebResource resource = client.resource(APP_BASE_PATH + TASK_PATH);
    resource.queryParam("dueAfter", "2000-01-01T00-00-00");
    ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    assertEquals(200, response.getStatus());

    JSONArray definitionsJson = response.getEntity(JSONArray.class);
    assertEquals(6, definitionsJson.length());

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

  /**
   * Tests that a feature implemented via Jackson-2 annotations works:
   * polymorphic serialization of historic details
   */
  @Test
  public void testPolymorphicSerialization() throws JSONException {
    JSONObject historicVariableUpdate = getFirstHistoricVariableUpdates();

    // variable update specific property
    assertTrue(historicVariableUpdate.has("variableName"));

  }

  /**
   * Uses Jackson's object mapper directly
   */
  @Test
  public void testProcessInstanceQuery() {
    WebResource resource = client.resource(APP_BASE_PATH + PROCESS_INSTANCE_PATH);
    ClientResponse response = resource.queryParam("variables", "invoiceNumber_eq_GPFE-23232323").accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    JSONArray instancesJson = response.getEntity(JSONArray.class);
    response.close();

    assertEquals(200, response.getStatus());
    // invoice example instance
    assertEquals(2, instancesJson.length());

  }

  @Test
  public void testComplexObjectJacksonSerialization() throws JSONException {
    WebResource resource = client.resource(APP_BASE_PATH + PROCESS_DEFINITION_PATH + "/statistics");
    ClientResponse response = resource.queryParam("incidents", "true").accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    JSONArray definitionStatistics = response.getEntity(JSONArray.class);
    response.close();

    assertEquals(200, response.getStatus());
    // invoice example instance
    assertEquals(3, definitionStatistics.length());

    // check that definition is also serialized
    for (int i = 0; i < definitionStatistics.length(); i++) {
      JSONObject definitionStatistic = definitionStatistics.getJSONObject(i);
      assertEquals("org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto", definitionStatistic.getString("@class"));
      assertEquals(0, definitionStatistic.getJSONArray("incidents").length());
      JSONObject definition = definitionStatistic.getJSONObject("definition");
      assertTrue(definition.getString("name").toLowerCase().contains("invoice"));
      assertFalse(definition.getBoolean("suspended"));
    }
  }

  @Test
  public void testOptionsRequest() {
    //since WAS 9 contains patched cxf, which does not support OPTIONS request, we have to test this
    String resourcePath = APP_BASE_PATH + FILTER_PATH;
    log.info("Send OPTIONS request to " + resourcePath);

    // given
    WebResource resource = client.resource(resourcePath);

    // when
    ClientResponse response = resource.options(ClientResponse.class);

    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    JSONObject entity = response.getEntity(JSONObject.class);
    assertNotNull(entity.has("links"));
  }

  @Test
  public void testEmptyBodyFilterIsActive() throws JSONException {
    ClientResponse response = client.resource(APP_BASE_PATH + FILTER_PATH + "/create").accept(MediaType.APPLICATION_JSON)
      .entity(null, MediaType.APPLICATION_JSON_TYPE)
      .post(ClientResponse.class);

    assertEquals(400, response.getStatus());
    response.close();
  }

  protected JSONObject getFirstTask() throws JSONException {
    ClientResponse response = client.resource(APP_BASE_PATH + TASK_PATH).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    JSONArray tasks = response.getEntity(JSONArray.class);
    JSONObject firstTask = tasks.getJSONObject(0);
    response.close();
    return firstTask;
  }

  protected JSONObject getFirstHistoricVariableUpdates() throws JSONException {
    ClientResponse response = client.resource(APP_BASE_PATH + HISTORIC_DETAIL_PATH)
        .queryParam("variableUpdates", "true")
        .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    JSONArray updates = response.getEntity(JSONArray.class);
    JSONObject firstUpdate = updates.getJSONObject(0);
    response.close();
    return firstUpdate;
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
