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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.util.ModificationInstructionBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.ModificationBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import io.restassured.http.ContentType;

public class ModificationRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/modification";
  protected static final String EXECUTE_MODIFICATION_SYNC_URL = PROCESS_INSTANCE_URL + "/execute";
  protected static final String EXECUTE_MODIFICATION_ASYNC_URL = PROCESS_INSTANCE_URL + "/executeAsync";

  protected RuntimeService runtimeServiceMock;
  protected ModificationBuilder modificationBuilderMock;

  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);

    modificationBuilderMock = mock(ModificationBuilder.class);
    when(modificationBuilderMock.cancelAllForActivity(any())).thenReturn(modificationBuilderMock);
    when(modificationBuilderMock.startAfterActivity(any())).thenReturn(modificationBuilderMock);
    when(modificationBuilderMock.startBeforeActivity(any())).thenReturn(modificationBuilderMock);
    when(modificationBuilderMock.startTransition(any())).thenReturn(modificationBuilderMock);
    when(modificationBuilderMock.processInstanceIds(Mockito.<List<String>>any())).thenReturn(modificationBuilderMock);

    Batch batchMock = createMockBatch();
    when(modificationBuilderMock.executeAsync()).thenReturn(batchMock);

    when(runtimeServiceMock.createModification(any())).thenReturn(modificationBuilderMock);
  }

  @Test
  public void executeModificationSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("skipCustomListeners", true);
    json.put("skipIoMappings", true);
    json.put("processDefinitionId", "processDefinitionId");
    json.put("processInstanceIds", Arrays.asList("100", "20"));
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startTransition().transitionId("transitionId").getJson());

    json.put("instructions", instructions);

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);

    verify(runtimeServiceMock).createModification("processDefinitionId");
    verify(modificationBuilderMock).processInstanceIds(eq(Arrays.asList("100", "20")));
    verify(modificationBuilderMock).cancelAllForActivity("activityId");
    verify(modificationBuilderMock).startBeforeActivity("activityId");
    verify(modificationBuilderMock).startAfterActivity("activityId");
    verify(modificationBuilderMock).startTransition("transitionId");
    verify(modificationBuilderMock).skipCustomListeners();
    verify(modificationBuilderMock).skipIoMappings();
    verify(modificationBuilderMock).execute();
  }

  @Test
  public void executeModificationWithNullProcessDefinitionIdAsync() {
    doThrow(new BadUserRequestException("processDefinitionId must be set"))
    .when(modificationBuilderMock).executeAsync();

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("skipCustomListeners", true);
    json.put("skipIoMappings", true);
    json.put("processInstanceIds", Arrays.asList("100", "20"));
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startTransition().transitionId("transitionId").getJson());

    json.put("instructions", instructions);

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);

    verify(runtimeServiceMock).createModification(null);
    verify(modificationBuilderMock).processInstanceIds(eq(Arrays.asList("100", "20")));
    verify(modificationBuilderMock).cancelAllForActivity("activityId");
    verify(modificationBuilderMock).startBeforeActivity("activityId");
    verify(modificationBuilderMock).startAfterActivity("activityId");
    verify(modificationBuilderMock).startTransition("transitionId");
    verify(modificationBuilderMock).skipCustomListeners();
    verify(modificationBuilderMock).skipIoMappings();
    verify(modificationBuilderMock).executeAsync();
  }

  @Test
  public void executeModificationWithNullProcessDefinitionIdSync() {
    doThrow(new BadUserRequestException("processDefinitionId must be set"))
    .when(modificationBuilderMock).execute();

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("skipCustomListeners", true);
    json.put("skipIoMappings", true);
    json.put("processInstanceIds", Arrays.asList("100", "20"));
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startTransition().transitionId("transitionId").getJson());

    json.put("instructions", instructions);

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);

    verify(runtimeServiceMock).createModification(null);
    verify(modificationBuilderMock).processInstanceIds(eq(Arrays.asList("100", "20")));
    verify(modificationBuilderMock).cancelAllForActivity("activityId");
    verify(modificationBuilderMock).startBeforeActivity("activityId");
    verify(modificationBuilderMock).startAfterActivity("activityId");
    verify(modificationBuilderMock).startTransition("transitionId");
    verify(modificationBuilderMock).skipCustomListeners();
    verify(modificationBuilderMock).skipIoMappings();
    verify(modificationBuilderMock).execute();
  }

  @Test
  public void executeModificationWithNullProcessInstanceIdsSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    String message = "Process instance ids is null";
    doThrow(new BadUserRequestException(message))
    .when(modificationBuilderMock).execute();

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId(EXAMPLE_ACTIVITY_ID).getJson());
    instructions.add(ModificationInstructionBuilder.startTransition().transitionId("transitionId").getJson());
    json.put("processDefinitionId", "processDefinitionId");
    json.put("instructions", instructions);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);
  }

  @Test
  public void executeModificationAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startTransition().transitionId("transitionId").getJson());
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").getJson());
    json.put("processDefinitionId", "processDefinitionId");
    json.put("instructions", instructions);
    json.put("processInstanceIds", Arrays.asList("100", "20"));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);

    verify(runtimeServiceMock).createModification("processDefinitionId");
    verify(modificationBuilderMock).processInstanceIds(eq(Arrays.asList("100", "20")));
    verify(modificationBuilderMock).cancelAllForActivity("activityId");
    verify(modificationBuilderMock).startBeforeActivity("activityId");
    verify(modificationBuilderMock).startAfterActivity("activityId");
    verify(modificationBuilderMock).startTransition("transitionId");
    verify(modificationBuilderMock).executeAsync();
  }

  @Test
  public void executeModificationWithNullProcessInstanceIdsAsync() {
    Map<String, Object> json = new HashMap<String, Object>();

    String message = "Process instance ids is null";
    doThrow(new BadUserRequestException(message))
    .when(modificationBuilderMock).executeAsync();

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId(EXAMPLE_ACTIVITY_ID).getJson());
    instructions.add(ModificationInstructionBuilder.startTransition().transitionId("transitionId").getJson());

    json.put("instructions", instructions);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);
  }

  @Test
  public void executeModificationWithValidProcessInstanceQuerySync() {

    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(new ProcessInstanceQueryImpl());
    Map<String, Object> json = new HashMap<String, Object>();

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());
    json.put("processDefinitionId", "processDefinitionId");

    ProcessInstanceQueryDto processInstanceQueryDto = new ProcessInstanceQueryDto();
    processInstanceQueryDto.setBusinessKey("foo");

    json.put("processInstanceQuery", processInstanceQueryDto);
    json.put("instructions", instructions);

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);

    verify(runtimeServiceMock, times(1)).createProcessInstanceQuery();
    verify(modificationBuilderMock).startAfterActivity("activityId");
    verify(modificationBuilderMock).processInstanceQuery(processInstanceQueryDto.toQuery(processEngine));
    verify(modificationBuilderMock).execute();
  }

  @Test
  public void executeModificationWithValidProcessInstanceQueryAsync() {

    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(new ProcessInstanceQueryImpl());
    Map<String, Object> json = new HashMap<String, Object>();

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());

    ProcessInstanceQueryDto processInstanceQueryDto = new ProcessInstanceQueryDto();
    processInstanceQueryDto.setBusinessKey("foo");

    json.put("processInstanceQuery", processInstanceQueryDto);
    json.put("instructions", instructions);
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);

    verify(runtimeServiceMock, times(1)).createProcessInstanceQuery();
    verify(modificationBuilderMock).startAfterActivity("activityId");
    verify(modificationBuilderMock).processInstanceQuery(processInstanceQueryDto.toQuery(processEngine));
    verify(modificationBuilderMock).executeAsync();
  }

  @Test
  public void executeModificationWithInvalidProcessInstanceQuerySync() {

    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(new ProcessInstanceQueryImpl());
    Map<String, Object> json = new HashMap<String, Object>();

    String message = "Process instance ids is null";
    doThrow(new BadUserRequestException(message)).when(modificationBuilderMock).execute();

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("acivityId").getJson());

    ProcessInstanceQueryDto processInstanceQueryDto = new ProcessInstanceQueryDto();
    processInstanceQueryDto.setBusinessKey("foo");
    json.put("processInstanceQuery", processInstanceQueryDto);
    json.put("instructions", instructions);
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);

  }

  @Test
  public void executeModificationWithInvalidProcessInstanceQueryAsync() {

    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(new ProcessInstanceQueryImpl());
    Map<String, Object> json = new HashMap<String, Object>();

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("acivityId").getJson());

    ProcessInstanceQueryDto processInstanceQueryDto = new ProcessInstanceQueryDto();
    processInstanceQueryDto.setBusinessKey("foo");
    json.put("processInstanceQuery", processInstanceQueryDto);
    json.put("instructions", instructions);
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);
  }

  @Test
  public void executeModificationWithNullInstructionsSync() {
    doThrow(new BadUserRequestException("Instructions must be set")).when(modificationBuilderMock).execute();

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("processInstanceIds", Arrays.asList("200", "11"));
    json.put("skipIoMappings", true);
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Instructions must be set"))
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);
  }

  @Test
  public void executeModificationWithNullInstructionsAsync() {
    doThrow(new BadUserRequestException("Instructions must be set")).when(modificationBuilderMock).executeAsync();
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("processInstanceIds", Arrays.asList("200", "11"));
    json.put("skipIoMappings", true);
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Instructions must be set"))
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);
  }

  @Test
  public void executeModificationThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(modificationBuilderMock).executeAsync();

    Map<String, Object> json = new HashMap<String, Object>();

    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    instructions.add(ModificationInstructionBuilder.startAfter().activityId("activityId").getJson());

    json.put("instructions", instructions);
    json.put("processInstanceIds", Arrays.asList("200", "323"));
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);
  }

  @Test
  public void executeInvalidModificationForStartAfterSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
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
      .post(EXECUTE_MODIFICATION_SYNC_URL);
  }

  @Test
  public void executeInvalidModificationForStartAfterAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
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
      .post(EXECUTE_MODIFICATION_ASYNC_URL);
  }

  @Test
  public void executeInvalidModificationForStartBeforeSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
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
      .post(EXECUTE_MODIFICATION_SYNC_URL);
  }

  @Test
  public void executeInvalidModificationForStartBeforeAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
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
      .post(EXECUTE_MODIFICATION_ASYNC_URL);
  }

  @Test
  public void executeInvalidModificationForStartTransitionSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
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
      .post(EXECUTE_MODIFICATION_SYNC_URL);
  }

  @Test
  public void executeInvalidModificationForStartTransitionAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
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
      .post(EXECUTE_MODIFICATION_ASYNC_URL);
  }

  @Test
  public void executeInvalidModificationForCancelAllSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.cancellation().getJson());
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
      .body("message", equalTo("For instruction type 'cancel': 'activityId' must be set"))
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);
  }

  @Test
  public void executeInvalidModificationForCancelAllAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.cancellation().getJson());
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
      .body("message", equalTo("For instruction type 'cancel': 'activityId' must be set"))
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);
  }

  @Test
  public void executeCancellationWithActiveFlagSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").cancelCurrentActiveActivityInstances(true).getJson());
    json.put("instructions", instructions);
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);

    verify(modificationBuilderMock).cancelAllForActivity("activityId", true);
    verify(modificationBuilderMock).execute();
  }

  @Test
  public void executeCancellationWithActiveFlagAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").cancelCurrentActiveActivityInstances(true).getJson());
    json.put("instructions", instructions);
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);

    verify(modificationBuilderMock).cancelAllForActivity("activityId", true);
    verify(modificationBuilderMock).executeAsync();
  }

  @Test
  public void executeCancellationWithoutActiveFlagSync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").cancelCurrentActiveActivityInstances(false).getJson());
    json.put("instructions", instructions);
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);

    verify(modificationBuilderMock).cancelAllForActivity("activityId");
    verify(modificationBuilderMock).execute();
  }

  @Test
  public void executeCancellationWithoutActiveFlagAsync() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").cancelCurrentActiveActivityInstances(false).getJson());
    json.put("instructions", instructions);
    json.put("processDefinitionId", "processDefinitionId");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);

    verify(modificationBuilderMock).cancelAllForActivity("activityId");
    verify(modificationBuilderMock).executeAsync();
  }

  @Test
  public void executeSyncModificationWithAnnotation() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipIoMappings", true);
    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.cancellation().activityId("activityId").cancelCurrentActiveActivityInstances(false).getJson());
    json.put("instructions", instructions);
    json.put("processDefinitionId", "processDefinitionId");
    json.put("annotation", "anAnnotation");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_SYNC_URL);

    verify(modificationBuilderMock).skipIoMappings();
    verify(modificationBuilderMock).cancelAllForActivity("activityId");
    verify(modificationBuilderMock).setAnnotation("anAnnotation");
    verify(modificationBuilderMock).execute();
  }

  @Test
  public void executeAsyncModificationWithAnnotation() {
    Map<String, Object> json = new HashMap<String, Object>();
    List<Map<String, Object>> instructions = new ArrayList<Map<String, Object>>();

    json.put("skipCustomListeners", true);
    json.put("processInstanceIds", Arrays.asList("200", "100"));
    instructions.add(ModificationInstructionBuilder.startBefore().activityId("activityId").getJson());
    json.put("instructions", instructions);
    json.put("processDefinitionId", "processDefinitionId");
    json.put("annotation", "anAnnotation");

    given()
      .contentType(ContentType.JSON)
      .body(json)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_MODIFICATION_ASYNC_URL);

    verify(modificationBuilderMock).skipCustomListeners();
    verify(modificationBuilderMock).startBeforeActivity("activityId");
    verify(modificationBuilderMock).setAnnotation("anAnnotation");
    verify(modificationBuilderMock).executeAsync();
  }

}
