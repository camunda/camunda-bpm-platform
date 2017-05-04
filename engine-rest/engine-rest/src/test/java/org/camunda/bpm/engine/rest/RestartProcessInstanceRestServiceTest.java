package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockBatch;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
import org.camunda.bpm.engine.rest.util.ModificationInstructionBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.RestartProcessInstanceBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

public class RestartProcessInstanceRestServiceTest extends AbstractRestServiceTest {
  
  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();
  
  protected static final String PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance";
  protected static final String RESTART_PROCESS_INSTANCE_URL = PROCESS_INSTANCE_URL + "/restart-process-instance";
  protected static final String RESTART_PROCESS_INSTANCE_ASYNC_URL = PROCESS_INSTANCE_URL + "/restart-process-instance-async";

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
    when(builderMock.processInstanceIds(anyListOf(String.class))).thenReturn(builderMock);
    when(builderMock.historicProcessInstanceQuery(any(HistoricProcessInstanceQuery.class))).thenReturn(builderMock);

    Batch batchMock = createMockBatch();
    when(builderMock.executeAsync()).thenReturn(batchMock);

    when(runtimeServiceMock.restartProcessInstances(anyString())).thenReturn(builderMock);
  }
  
  @Test
  public void testRestartProcessInstanceSync() {

    HashMap<String, Object> json = new HashMap<String, Object>();
    json.put("processDefinitionId", "processDefinitionId");
    ArrayList<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("processInstanceIds", Arrays.asList("processInstanceId1", "processInstanceId2"));
    
    given()
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_URL);
    
    verify(runtimeServiceMock).restartProcessInstances("processDefinitionId");
    verify(builderMock).startAfterActivity("activityId");
    verify(builderMock).processInstanceIds(Arrays.asList("processInstanceId1", "processInstanceId2"));
    verify(builderMock).execute();
  }
  
  @Test
  public void testRestartProcessInstanceAsync() {
    HashMap<String, Object> json = new HashMap<String, Object>();
    json.put("processDefinitionId", "processDefinitionId");
    ArrayList<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("processInstanceIds", Arrays.asList("processInstanceId1", "processInstanceId2"));
    
    given()
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
    
    verify(runtimeServiceMock).restartProcessInstances("processDefinitionId");
    verify(builderMock).startAfterActivity("activityId");
    verify(builderMock).processInstanceIds(Arrays.asList("processInstanceId1", "processInstanceId2"));
    verify(builderMock).executeAsync();
  }
  
  @Test
  public void testRestartProcessInstanceWithNullProcessDefinitionSync() {
    doThrow(new BadUserRequestException("processDefinitionId is null")).when(builderMock).execute();
    
    HashMap<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    json.put("instuctions", instructions);
    json.put("processInstanceIds", "processInstanceId");

    given()
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testRestartProcessInstanceWithNullProcessDefinitionAsync() {
    doThrow(new BadUserRequestException("processDefinitionId is null")).when(builderMock).executeAsync();
    
    HashMap<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    json.put("instuctions", instructions);
    json.put("processInstanceIds", "processInstanceId");

    given()
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
  }
  
  @Test
  public void testRestartProcessInstanceWithNullProcessInstanceIdsSync() {
    doThrow(new BadUserRequestException("processInstanceIds is null")).when(builderMock).execute();
    
    HashMap<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("processDefinitionId", "processDefinitionId");
    json.put("instructions", instructions);
    
    given()
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
    json.put("processDefinitionId", "processDefinitionId");
    json.put("instructions", instructions);
    
    given()
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
    json.put("processDefinitionId", "processDefinitionId");
    
    given()
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_URL);
    
    verify(runtimeServiceMock).restartProcessInstances("processDefinitionId");
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
    json.put("processDefinitionId", "processDefinitionId");
    
    given()
    .contentType(ContentType.JSON)
    .body(json)
    .then().expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(RESTART_PROCESS_INSTANCE_ASYNC_URL);
    
    verify(runtimeServiceMock).restartProcessInstances("processDefinitionId");
    verify(builderMock).startBeforeActivity("activityId");
    verify(builderMock).historicProcessInstanceQuery(query.toQuery(processEngine));
    verify(builderMock).executeAsync();
  }
  
  @Test
  public void testRestartProcessInstanceWithNullInstructionsSync() {
    doThrow(new BadUserRequestException("instructions is null")).when(builderMock).execute();
    
    HashMap<String, Object> json = new HashMap<String, Object>();
    json.put("processDefinitionId", "processDefinitionId");
    json.put("processInstanceIds", "processInstanceId");
    
    given()
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
    json.put("processDefinitionId", "processDefinitionId");
    json.put("processInstanceIds", "processInstanceId");
    
    given()
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
    json.put("processDefinitionId", "processDefinitionId");

    given()
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
    json.put("processDefinitionId", "processDefinitionId");

    given()
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
    json.put("processDefinitionId", "processDefinitionId");

    given()
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
    json.put("processDefinitionId", "processDefinitionId");

    given()
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
    json.put("processDefinitionId", "processDefinitionId");

    given()
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
    json.put("processDefinitionId", "processDefinitionId");

    given()
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
}
