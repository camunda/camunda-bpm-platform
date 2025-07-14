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

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;

import org.camunda.bpm.AbstractWebIntegrationTest;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

  private static final String SCHEMA_LOG_PATH = ENGINE_DEFAULT_PATH + "/schema/log";

  private final static Logger log = Logger.getLogger(RestIT.class.getName());

  @Before
  public void createClient() throws Exception {
    preventRaceConditions();
    createClient(getRestCtxPath());
  }

  @Test
  public void testScenario() throws Exception {
    // get process definitions for default engine
    log.info("Checking " + appBasePath + PROCESS_DEFINITION_PATH);
    HttpResponse<JsonNode> response = Unirest.get(appBasePath + PROCESS_DEFINITION_PATH)
        .header("Accept", "application/json")
        .asJson();

    assertEquals(200, response.getStatus());

    JSONArray definitionsJson = response.getBody().getArray();

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
        assertEquals("reviewInvoice.bpmn", definitionJson.getString("resource"));
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
    log.info("Checking " + appBasePath + TASK_PATH);

    HttpResponse<JsonNode> response = Unirest.get(appBasePath + TASK_PATH)
        .queryString("dueAfter", "2000-01-01T00:00:00.000+0200")
        .header("Accept", "application/json")
        .asJson();

    assertEquals(200, response.getStatus());

    JSONArray definitionsJson = response.getBody().getArray();
    assertEquals(4, definitionsJson.length());
  }

  @Test
  public void testDelayedJobDefinitionSuspension() {
    log.info("Checking " + appBasePath + JOB_DEFINITION_PATH + "/suspended");

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("processDefinitionKey", "jobExampleProcess");
    requestBody.put("suspended", true);
    requestBody.put("includeJobs", true);
    requestBody.put("executionDate", "2014-08-25T13:55:45");

    HttpResponse<String> response = Unirest.put(appBasePath + JOB_DEFINITION_PATH + "/suspended")
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .body(requestBody)
        .asString();

    assertEquals(204, response.getStatus());
  }

  @Test
  public void testTaskQueryContentType() {
    String resourcePath = appBasePath + TASK_PATH;
    log.info("Checking " + resourcePath);
    assertMediaTypesOfResource(resourcePath, false);
  }

  @Test
  public void testSingleTaskContentType() throws Exception {
    // get id of first task
    String taskId = getFirstTask().getString("id");

    String resourcePath = appBasePath + TASK_PATH + "/" + taskId;
    log.info("Checking " + resourcePath);
    assertMediaTypesOfResource(resourcePath, false);
  }

  @Test
  public void testTaskFilterResultContentType() throws Exception {
    // create filter for first task, so single result will not throw an exception
    JSONObject firstTask = getFirstTask();
    Map<String, Object> query = new HashMap<>();
    query.put("taskDefinitionKey", firstTask.getString("taskDefinitionKey"));
    query.put("processInstanceId", firstTask.getString("processInstanceId"));
    Map<String, Object> filter = new HashMap<>();
    filter.put("resourceType", "Task");
    filter.put("name", "IT Test Filter");
    filter.put("query", query);

    HttpResponse<JsonNode> response = Unirest.post(appBasePath + FILTER_PATH + "/create")
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .body(filter)
        .asJson();

    assertEquals(200, response.getStatus());
    String filterId = response.getBody().getObject().getString("id");

    String resourcePath = appBasePath + FILTER_PATH + "/" + filterId + "/list";
    log.info("Checking " + resourcePath);
    assertMediaTypesOfResource(resourcePath, true);

    resourcePath = appBasePath + FILTER_PATH + "/" + filterId + "/singleResult";
    log.info("Checking " + resourcePath);
    assertMediaTypesOfResource(resourcePath, true);

    // delete test filter
    HttpResponse<String> deleteResponse = Unirest.delete(appBasePath + FILTER_PATH + "/" + filterId).asString();
    assertEquals(204, deleteResponse.getStatus());
  }

  @Test
  public void shouldSerializeDateWithDefinedFormat() throws Exception {
    // when
    HttpResponse<JsonNode> response = Unirest.get(appBasePath + SCHEMA_LOG_PATH)
        .header("Accept", "application/json")
        .asJson();

    // then
    assertEquals(200, response.getStatus());
    JSONObject logElement = response.getBody().getArray().getJSONObject(0);
    String timestamp = logElement.getString("timestamp");
    try {
      new SimpleDateFormat(JacksonConfigurator.DEFAULT_DATE_FORMAT).parse(timestamp);
    } catch (ParseException pex) {
      fail("Couldn't parse timestamp from schema log: " + timestamp);
    }
  }

  /**
   * Tests that a feature implemented via Jackson-2 annotations works:
   * polymorphic serialization of historic details
   */
  @Test
  public void testPolymorphicSerialization() throws Exception {
    JSONObject historicVariableUpdate = getFirstHistoricVariableUpdates();

    // variable update specific property
    assertTrue(historicVariableUpdate.has("variableName"));
  }

  /**
   * Uses Jackson's object mapper directly
   */
  @Test
  public void testProcessInstanceQuery() {
    HttpResponse<JsonNode> response = Unirest.get(appBasePath + PROCESS_INSTANCE_PATH)
        .queryString("variables", "invoiceNumber_eq_GPFE-23232323")
        .header("Accept", "application/json")
        .asJson();

    JSONArray instancesJson = response.getBody().getArray();

    assertEquals(200, response.getStatus());
    // invoice example instance
    assertEquals(2, instancesJson.length());
  }

  @Test
  public void testComplexObjectJacksonSerialization() throws Exception {
    HttpResponse<JsonNode> response = Unirest.get(appBasePath + PROCESS_DEFINITION_PATH + "/statistics")
        .queryString("incidents", "true")
        .header("Accept", "application/json")
        .asJson();

    JSONArray definitionStatistics = response.getBody().getArray();

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
    String resourcePath = appBasePath + FILTER_PATH;
    log.info("Send OPTIONS request to " + resourcePath);

    // when
    HttpResponse<JsonNode> response = Unirest.options(resourcePath).asJson();

    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    JSONObject entity = response.getBody().getObject();
    assertTrue(entity.has("links"));
  }

  @Test
  public void testEmptyBodyFilterIsActive() {
    HttpResponse<String> response = Unirest.post(appBasePath + FILTER_PATH + "/create")
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .body("")
        .asString();

    assertEquals(400, response.getStatus());
  }

  protected JSONObject getFirstTask() throws Exception {
    HttpResponse<JsonNode> response = Unirest.get(appBasePath + TASK_PATH)
        .header("Accept", "application/json")
        .asJson();

    JSONArray tasks = response.getBody().getArray();
    return tasks.getJSONObject(0);
  }

  protected JSONObject getFirstHistoricVariableUpdates() throws Exception {
    HttpResponse<JsonNode> response = Unirest.get(appBasePath + HISTORIC_DETAIL_PATH)
        .queryString("variableUpdates", "true")
        .header("Accept", "application/json")
        .asJson();

    JSONArray updates = response.getBody().getArray();
    return updates.getJSONObject(0);
  }

  protected void assertMediaTypesOfResource(String resourcePath, boolean postSupported) {
    assertMediaTypes(resourcePath, postSupported, "application/json");
    assertMediaTypes(resourcePath, postSupported, "application/json", "*/*");
    assertMediaTypes(resourcePath, postSupported, "application/json", "application/json");
    assertMediaTypes(resourcePath, postSupported, Hal.APPLICATION_HAL_JSON, Hal.APPLICATION_HAL_JSON);
    assertMediaTypes(resourcePath, postSupported, Hal.APPLICATION_HAL_JSON, Hal.APPLICATION_HAL_JSON, "application/json; q=0.5");
    assertMediaTypes(resourcePath, postSupported, "application/json", Hal.APPLICATION_HAL_JSON + "; q=0.5", "application/json");
    assertMediaTypes(resourcePath, postSupported, "application/json", Hal.APPLICATION_HAL_JSON + "; q=0.5 ", "application/json; q=0.6");
    assertMediaTypes(resourcePath, postSupported, Hal.APPLICATION_HAL_JSON, Hal.APPLICATION_HAL_JSON + "; q=0.6", "application/json; q=0.5");
  }

  protected void assertMediaTypes(String resourcePath, boolean postSupported, String expectedMediaType, String... acceptMediaTypes) {
    // test GET request
    HttpResponse<String> response = Unirest.get(resourcePath)
        .header("Accept", String.join(",", acceptMediaTypes))
        .asString();
    assertMediaType(response, expectedMediaType);

    if (postSupported) {
      // test POST request
      response = Unirest.post(resourcePath)
          .header("Accept", String.join(",", acceptMediaTypes))
          .header("Content-Type", "application/json")
          .body(Collections.EMPTY_MAP)
          .asString();
      assertMediaType(response, expectedMediaType);
    }
  }

  protected void assertMediaType(HttpResponse<String> response, String expected) {
    String actual = response.getHeaders().getFirst("Content-Type");
    assertEquals(200, response.getStatus());
    // use startsWith cause sometimes server also returns quality parameters
    assertTrue("Expected: " + expected + " Actual: " + actual, actual != null && actual.startsWith(expected));
  }
}
