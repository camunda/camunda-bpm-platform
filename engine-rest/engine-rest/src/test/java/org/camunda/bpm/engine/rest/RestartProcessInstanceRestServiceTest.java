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
package org.camunda.bpm.engine.rest;

import static io.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockBatch;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.ModificationInstructionBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.RestartProcessInstanceBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class RestartProcessInstanceRestServiceTest extends AbstractRestServiceTest {
  
  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();
  
  protected static final String PROCESS_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition";
  protected static final String SINGLE_PROCESS_DEFINITION_URL = PROCESS_DEFINITION_URL + "/{id}";
  protected static final String RESTART_PROCESS_INSTANCE_URL = SINGLE_PROCESS_DEFINITION_URL + "/restart";
  protected static final String RESTART_PROCESS_INSTANCE_ASYNC_URL = SINGLE_PROCESS_DEFINITION_URL + "/restart-async";

  RuntimeService runtimeServiceMock;
  HistoryService historyServiceMock;
  RestartProcessInstanceBuilder builderMock;

  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
    
    historyServiceMock = mock(HistoryService.class);
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);

    builderMock = mock(RestartProcessInstanceBuilder.class);
    when(builderMock.startAfterActivity(anyString())).thenReturn(builderMock);
    when(builderMock.startBeforeActivity(anyString())).thenReturn(builderMock);
    when(builderMock.startTransition(anyString())).thenReturn(builderMock);
    when(builderMock.processInstanceIds(anyList())).thenReturn(builderMock);
    when(builderMock.historicProcessInstanceQuery(any(HistoricProcessInstanceQuery.class))).thenReturn(builderMock);
    when(builderMock.skipCustomListeners()).thenReturn(builderMock);
    when(builderMock.skipIoMappings()).thenReturn(builderMock);
    when(builderMock.initialSetOfVariables()).thenReturn(builderMock);
    when(builderMock.withoutBusinessKey()).thenReturn(builderMock);

    Batch batchMock = createMockBatch();
    when(builderMock.executeAsync()).thenReturn(batchMock);

    when(runtimeServiceMock.restartProcessInstances(anyString())).thenReturn(builderMock);
  }
  
  @Test
  public void testRestartProcessInstanceSync() {

    HashMap<String, Object> json = new HashMap<String, Object>();
    ArrayList<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("processInstanceIds", Arrays.asList("processInstanceId1", "processInstanceId2"));
    
    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_URL);
    
    verify(runtimeServiceMock).restartProcessInstances(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(builderMock).startAfterActivity("activityId");
    verify(builderMock).processInstanceIds(Arrays.asList("processInstanceId1", "processInstanceId2"));
    verify(builderMock).execute();
  }
  
  @Test
  public void testRestartProcessInstanceAsync() {
    HashMap<String, Object> json = new HashMap<String, Object>();
    ArrayList<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("processInstanceIds", Arrays.asList("processInstanceId1", "processInstanceId2"));
    
    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
    
    verify(runtimeServiceMock).restartProcessInstances(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(builderMock).startAfterActivity("activityId");
    verify(builderMock).processInstanceIds(Arrays.asList("processInstanceId1", "processInstanceId2"));
    verify(builderMock).executeAsync();
  }

  @Test
  public void testRestartProcessInstanceWithNullProcessInstanceIdsSync() {
    doThrow(new BadUserRequestException("processInstanceIds is null")).when(builderMock).execute();
    
    HashMap<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    
    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testRestartProcessInstanceWithNullProcessInstanceIdsAsync() {
    doThrow(new BadUserRequestException("processInstanceIds is null")).when(builderMock).executeAsync();
    
    HashMap<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    
    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
  }
  
  @Test
  public void testRestartProcessInstanceWithHistoricProcessInstanceQuerySync() {
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(new HistoricProcessInstanceQueryImpl());
    HashMap<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    
    HistoricProcessInstanceQueryDto query = new HistoricProcessInstanceQueryDto();
    query.setProcessInstanceBusinessKey("businessKey");
    
    json.put("historicProcessInstanceQuery", query);

    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_URL);
    
    verify(runtimeServiceMock).restartProcessInstances(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).historicProcessInstanceQuery(query.toQuery(processEngine));
    verify(builderMock).execute();
  }
  
  @Test
  public void testRestartProcessInstanceWithHistoricProcessInstanceQueryAsync() {
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(new HistoricProcessInstanceQueryImpl());
    HashMap<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    
    HistoricProcessInstanceQueryDto query = new HistoricProcessInstanceQueryDto();
    query.setProcessInstanceBusinessKey("businessKey");
    
    json.put("historicProcessInstanceQuery", query);

    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
    
    verify(runtimeServiceMock).restartProcessInstances(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).historicProcessInstanceQuery(query.toQuery(processEngine));
    verify(builderMock).executeAsync();
  }
  
  @Test
  public void testRestartProcessInstanceWithNullInstructionsSync() {
    doThrow(new BadUserRequestException("instructions is null")).when(builderMock).execute();
    
    HashMap<String, Object> json = new HashMap<String, Object>();
    json.put("processInstanceIds", "processInstanceId");
    
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(RESTART_PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testRestartProcessInstanceWithNullInstructionsAsync() {
    doThrow(new BadUserRequestException("instructions is null")).when(builderMock).executeAsync();
    
    HashMap<String, Object> json = new HashMap<String, Object>();
    json.put("processInstanceIds", "processInstanceId");
    
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
  }
  
  @Test
  public void testRestartProcessInstanceWithInvalidModificationInstructionForStartAfterSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.startAfter().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("For instruction type 'startAfterActivity': 'activityId' must be set"))
    .when()
      .post(RESTART_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testRestartProcessInstanceWithInvalidModificationInstructionForStartAfterAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.startAfter().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("For instruction type 'startAfterActivity': 'activityId' must be set"))
    .when()
      .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
  }

  @Test
  public void testRestartProcessInstanceWithInvalidModificationInstructionForStartBeforeSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.startBefore().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("For instruction type 'startBeforeActivity': 'activityId' must be set"))
    .when()
      .post(RESTART_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testRestartProcessInstanceWithInvalidModificationInstructionForStartBeforeAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.startBefore().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("For instruction type 'startBeforeActivity': 'activityId' must be set"))
    .when()
      .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
  }

  @Test
  public void testRestartProcessInstanceWithInvalidModificationInstructionForStartTransitionSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.startTransition().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("For instruction type 'startTransition': 'transitionId' must be set"))
    .when()
      .post(RESTART_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testRestartProcessInstanceWithInvalidModificationInstructionForStartTransitionAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.startTransition().getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("For instruction type 'startTransition': 'transitionId' must be set"))
    .when()
      .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
  }

  @Test
  public void testRestartProcessInstanceWithInitialVariablesAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("processInstance1", "processInstance2"));
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("initialVariables", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
     .when()
       .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);

    verify(builderMock).processInstanceIds(Arrays.asList("processInstance1", "processInstance2"));
    verify(builderMock).initialSetOfVariables();
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).executeAsync();
  }

  @Test
  public void testRestartProcessInstanceWithInitialVariablesSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("processInstance1", "processInstance2"));
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("initialVariables", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
     .when()
       .post(RESTART_PROCESS_INSTANCE_URL);

    verify(builderMock).processInstanceIds(Arrays.asList("processInstance1", "processInstance2"));
    verify(builderMock).initialSetOfVariables();
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).execute();
  }

  @Test
  public void testRestartProcessInstanceWithSkipCustomListenersAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("processInstance1", "processInstance2"));
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("skipCustomListeners", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
     .when()
       .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);

    verify(builderMock).processInstanceIds(Arrays.asList("processInstance1", "processInstance2"));
    verify(builderMock).skipCustomListeners();
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).executeAsync();
  }

  @Test
  public void testRestartProcessInstanceWithSkipCustomListenersSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("processInstance1", "processInstance2"));
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("skipCustomListeners", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
     .when()
       .post(RESTART_PROCESS_INSTANCE_URL);

    verify(builderMock).processInstanceIds(Arrays.asList("processInstance1", "processInstance2"));
    verify(builderMock).skipCustomListeners();
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).execute();
  }

  @Test
  public void testRestartProcessInstanceWithSkipIoMappingsAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("processInstance1", "processInstance2"));
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("skipIoMappings", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
     .when()
       .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);

    verify(builderMock).processInstanceIds(Arrays.asList("processInstance1", "processInstance2"));
    verify(builderMock).skipIoMappings();
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).executeAsync();
  }

  @Test
  public void testRestartProcessInstanceWithSkipIoMappingsSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("processInstance1", "processInstance2"));
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("skipIoMappings", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
     .when()
       .post(RESTART_PROCESS_INSTANCE_URL);

    verify(builderMock).processInstanceIds(Arrays.asList("processInstance1", "processInstance2"));
    verify(builderMock).skipIoMappings();
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).execute();
  }

  @Test
  public void testRestartProcessInstanceWithoutBusinessKey() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("processInstanceIds", Arrays.asList("processInstance1", "processInstance2"));
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("withoutBusinessKey", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
     .when()
       .post(RESTART_PROCESS_INSTANCE_URL);

    verify(builderMock).processInstanceIds(Arrays.asList("processInstance1", "processInstance2"));
    verify(builderMock).withoutBusinessKey();
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).execute();
  }

  @Test
  public void testRestartProcessInstanceWithoutProcessInstanceIdsSync() {
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(new HistoricProcessInstanceQueryImpl());
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    HistoricProcessInstanceQueryDto query = new HistoricProcessInstanceQueryDto();
    query.setFinished(true);
    json.put("historicProcessInstanceQuery", query);
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
     .when()
       .post(RESTART_PROCESS_INSTANCE_URL);

    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).historicProcessInstanceQuery(query.toQuery(processEngine));
    verify(builderMock).execute();
    verifyNoMoreInteractions(builderMock);
  }

  @Test
  public void testRestartProcessInstanceWithoutProcessInstanceIdsAsync() {
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(new HistoricProcessInstanceQueryImpl());
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    HistoricProcessInstanceQueryDto query = new HistoricProcessInstanceQueryDto();
    query.setFinished(true);
    json.put("historicProcessInstanceQuery", query);
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
     .when()
       .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);

    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).historicProcessInstanceQuery(query.toQuery(processEngine));
    verify(builderMock).executeAsync();
    verifyNoMoreInteractions(builderMock);
  }
}
