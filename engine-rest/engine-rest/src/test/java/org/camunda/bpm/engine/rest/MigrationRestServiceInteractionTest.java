/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.helper.MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_BATCH_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_BATCH_JOBS_PER_SEED;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_BATCH_JOB_DEFINITION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_BATCH_TOTAL_JOBS;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_BATCH_TYPE;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_INVOCATIONS_PER_BATCH_JOB;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_MONITOR_JOB_DEFINITION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_SEED_JOB_DEFINITION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TENANT_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.NON_EXISTING_ACTIVITY_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockBatch;
import static org.camunda.bpm.engine.rest.helper.NoIntermediaryInvocation.immediatelyAfter;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.migration.MigratingActivityInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigratingTransitionInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationInstructionValidationReport;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.migration.MigrationPlanValidationReport;
import org.camunda.bpm.engine.rest.dto.migration.MigrationInstructionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.helper.FluentAnswer;
import org.camunda.bpm.engine.rest.helper.MockMigrationPlanBuilder;
import org.camunda.bpm.engine.rest.helper.MockMigrationPlanBuilder.JoinedMigrationPlanBuilderMock;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.rest.util.migration.MigrationExecutionDtoBuilder;
import org.camunda.bpm.engine.rest.util.migration.MigrationPlanDtoBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;
import java.util.List;
import org.camunda.bpm.engine.rest.util.migration.MigrationInstructionDtoBuilder;

public class MigrationRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String MIGRATION_URL = TEST_RESOURCE_ROOT_PATH + "/migration";
  protected static final String GENERATE_MIGRATION_URL = MIGRATION_URL + "/generate";
  protected static final String VALIDATE_MIGRATION_URL = MIGRATION_URL + "/validate";
  protected static final String EXECUTE_MIGRATION_URL = MIGRATION_URL + "/execute";
  protected static final String EXECUTE_MIGRATION_ASYNC_URL = MIGRATION_URL + "/executeAsync";

  protected RuntimeService runtimeServiceMock;
  protected JoinedMigrationPlanBuilderMock migrationPlanBuilderMock;
  protected MigrationPlanExecutionBuilder migrationPlanExecutionBuilderMock;

  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);

    migrationPlanBuilderMock = new MockMigrationPlanBuilder()
      .sourceProcessDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .targetProcessDefinitionId(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
      .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .builder();

    when(runtimeServiceMock.createMigrationPlan(eq(EXAMPLE_PROCESS_DEFINITION_ID), eq(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)))
      .thenReturn(migrationPlanBuilderMock);

    migrationPlanExecutionBuilderMock = mock(MigrationPlanExecutionBuilder.class);
    when(migrationPlanExecutionBuilderMock.processInstanceIds(anyListOf(String.class))).thenReturn(migrationPlanExecutionBuilderMock);

    when(runtimeServiceMock.newMigration(any(MigrationPlan.class))).thenReturn(migrationPlanExecutionBuilderMock);
  }

  @Test
  public void generateMigrationPlanWithInitialEmptyInstructions() {
    Map<String, Object> initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .instructions(Collections.<Map<String, Object>>emptyList())
      .build();

    Response response = given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(initialMigrationPlan)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(GENERATE_MIGRATION_URL);

    verifyGenerateMigrationPlanInteraction(migrationPlanBuilderMock, initialMigrationPlan);
    verifyGenerateMigrationPlanResponse(response);
  }

  @Test
  public void generateMigrationPlanWithInitialNullInstructions() {
    Map<String, Object> initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .instructions(null)
      .build();

    Response response = given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(initialMigrationPlan)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(GENERATE_MIGRATION_URL);

    verifyGenerateMigrationPlanInteraction(migrationPlanBuilderMock, initialMigrationPlan);
    verifyGenerateMigrationPlanResponse(response);
  }

  @Test
  public void generateMigrationPlanWithNoInitialInstructions() {
    Map<String, Object> initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .build();

    Response response = given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(initialMigrationPlan)
    .then()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(GENERATE_MIGRATION_URL);

    verifyGenerateMigrationPlanInteraction(migrationPlanBuilderMock, initialMigrationPlan);
    verifyGenerateMigrationPlanResponse(response);
  }

  @Test
  public void generateMigrationPlanIgnoringInitialInstructions() {
    Map<String, Object> initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .instruction("ignored", "ignored")
      .build();

    Response response = given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(initialMigrationPlan)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(GENERATE_MIGRATION_URL);

    verifyGenerateMigrationPlanInteraction(migrationPlanBuilderMock, initialMigrationPlan);
    verifyGenerateMigrationPlanResponse(response);
  }

  @Test
  public void generateMigrationPlanWithNullSourceProcessDefinition() {
    String message = "source process definition id is null";
    MigrationPlanBuilder planBuilder = mock(MigrationPlanBuilder.class, Mockito.RETURNS_DEEP_STUBS);
    when(runtimeServiceMock.createMigrationPlan(isNull(String.class), anyString()))
      .thenReturn(planBuilder);

    when(planBuilder.mapEqualActivities().build())
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> initialMigrationPlan = new MigrationPlanDtoBuilder(null, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID).build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(initialMigrationPlan)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(GENERATE_MIGRATION_URL);
  }

  @Test
  public void generateMigrationPlanWithNonExistingSourceProcessDefinition() {
    String message = "source process definition with id " + NON_EXISTING_PROCESS_DEFINITION_ID + " does not exist";
    MigrationPlanBuilder migrationPlanBuilder = mock(MigrationPlanBuilder.class, Mockito.RETURNS_DEEP_STUBS);
    when(runtimeServiceMock.createMigrationPlan(eq(NON_EXISTING_PROCESS_DEFINITION_ID), anyString()))
      .thenReturn(migrationPlanBuilder);

    when(
      migrationPlanBuilder
        .mapEqualActivities()
        .build())
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> initialMigrationPlan = new MigrationPlanDtoBuilder(NON_EXISTING_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID).build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(initialMigrationPlan)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(GENERATE_MIGRATION_URL);
  }

  @Test
  public void generateMigrationPlanWithNullTargetProcessDefinition() {
    String message = "target process definition id is null";
    MigrationPlanBuilder migrationPlanBuilder = mock(MigrationPlanBuilder.class, Mockito.RETURNS_DEEP_STUBS);
    when(runtimeServiceMock.createMigrationPlan(anyString(), isNull(String.class)))
      .thenReturn(migrationPlanBuilder);
    when(
      migrationPlanBuilder
        .mapEqualActivities()
        .build())
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, null).build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(initialMigrationPlan)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(GENERATE_MIGRATION_URL);
  }

  @Test
  public void generateMigrationPlanWithNonExistingTargetProcessDefinition() {
    String message = "target process definition with id " + NON_EXISTING_PROCESS_DEFINITION_ID + " does not exist";
    MigrationPlanBuilder migrationPlanBuilder = mock(MigrationPlanBuilder.class, Mockito.RETURNS_DEEP_STUBS);
    when(runtimeServiceMock.createMigrationPlan(anyString(), eq(NON_EXISTING_PROCESS_DEFINITION_ID)))
      .thenReturn(migrationPlanBuilder);
    when(
      migrationPlanBuilder
        .mapEqualActivities()
        .build())
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, NON_EXISTING_PROCESS_DEFINITION_ID).build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(initialMigrationPlan)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(GENERATE_MIGRATION_URL);
  }

  @Test
  public void generatePlanUpdateEventTriggers() {
    migrationPlanBuilderMock = new MockMigrationPlanBuilder()
      .sourceProcessDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .targetProcessDefinitionId(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID, true)
      .builder();

    Map<String, Object> generationRequest = new HashMap<String, Object>();
    generationRequest.put("sourceProcessDefinitionId", EXAMPLE_PROCESS_DEFINITION_ID);
    generationRequest.put("targetProcessDefinitionId", ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);
    generationRequest.put("updateEventTriggers", true);

    when(runtimeServiceMock.createMigrationPlan(anyString(), anyString()))
      .thenReturn(migrationPlanBuilderMock);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(generationRequest)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(GENERATE_MIGRATION_URL);

    verify(runtimeServiceMock).createMigrationPlan(eq(EXAMPLE_PROCESS_DEFINITION_ID), eq(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID));

    InOrder inOrder = Mockito.inOrder(migrationPlanBuilderMock);

    // the map equal activities method should be called
    inOrder.verify(migrationPlanBuilderMock).mapEqualActivities();
    inOrder.verify(migrationPlanBuilderMock, immediatelyAfter()).updateEventTriggers();
    verify(migrationPlanBuilderMock, never()).mapActivities(anyString(), anyString());
  }

  @Test
  public void generatePlanUpdateEventTriggerResponse() {
    migrationPlanBuilderMock = new MockMigrationPlanBuilder()
      .sourceProcessDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .targetProcessDefinitionId(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID, true)
      .builder();
    when(runtimeServiceMock.createMigrationPlan(anyString(), anyString()))
      .thenReturn(migrationPlanBuilderMock);

    Map<String, Object> generationRequest = new HashMap<String, Object>();
      generationRequest.put("sourceProcessDefinitionId", EXAMPLE_PROCESS_DEFINITION_ID);
      generationRequest.put("targetProcessDefinitionId", ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(generationRequest)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("instructions[0].sourceActivityIds[0]", equalTo(EXAMPLE_ACTIVITY_ID))
      .body("instructions[0].targetActivityIds[0]", equalTo(ANOTHER_EXAMPLE_ACTIVITY_ID))
      .body("instructions[0].updateEventTrigger", equalTo(true))
    .when()
      .post(GENERATE_MIGRATION_URL);

  }

  @Test
  public void executeMigrationPlan() {
    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MIGRATION_URL);

    verifyCreateMigrationPlanInteraction(migrationPlanBuilderMock, (Map<String, Object>) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_MIGRATION_PLAN));
    verifyMigrationPlanExecutionInteraction(migrationExecution);
  }

  @Test
  public void executeMigrationPlanWithProcessInstanceQuery() {
    when(runtimeServiceMock.createProcessInstanceQuery())
      .thenReturn(new ProcessInstanceQueryImpl());

    ProcessInstanceQueryDto processInstanceQuery = new ProcessInstanceQueryDto();
    processInstanceQuery.setProcessDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID);

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstanceQuery(processInstanceQuery)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MIGRATION_URL);


    verifyCreateMigrationPlanInteraction(migrationPlanBuilderMock, (Map<String, Object>) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_MIGRATION_PLAN));
    verifyMigrationPlanExecutionInteraction(migrationExecution);
  }

  @Test
  public void executeMigrationPlanSkipListeners() {

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID)
      .skipCustomListeners(true)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MIGRATION_URL);

    verifyMigrationPlanExecutionInteraction(migrationExecution);
  }

  @Test
  public void executeMigrationPlanSkipIoMappings() {

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID)
      .skipIoMappings(true)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MIGRATION_URL);

    verifyMigrationPlanExecutionInteraction(migrationExecution);
  }

  @Test
  public void executeMigrationPlanWithNullInstructions() {
    MigrationInstructionValidationReport instructionReport = mock(MigrationInstructionValidationReport.class);
    when(instructionReport.getMigrationInstruction()).thenReturn(null);
    when(instructionReport.getFailures()).thenReturn(Collections.singletonList("failure"));

    MigrationPlanValidationReport validationReport = mock(MigrationPlanValidationReport.class);
    when(validationReport.getInstructionReports()).thenReturn(Collections.singletonList(instructionReport));

    when(migrationPlanBuilderMock.build()).thenThrow(new MigrationPlanValidationException("fooo", validationReport));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(MigrationPlanValidationException.class.getSimpleName()))
      .body("message", is("fooo"))
      .body("validationReport.instructionReports", hasSize(1))
      .body("validationReport.instructionReports[0].instruction", nullValue())
      .body("validationReport.instructionReports[0].failures", hasSize(1))
      .body("validationReport.instructionReports[0].failures[0]", is("failure"))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanWithEmptyInstructions() {
    MigrationInstructionValidationReport instructionReport = mock(MigrationInstructionValidationReport.class);
    when(instructionReport.getMigrationInstruction()).thenReturn(null);
    when(instructionReport.getFailures()).thenReturn(Collections.singletonList("failure"));

    MigrationPlanValidationReport validationReport = mock(MigrationPlanValidationReport.class);
    when(validationReport.getInstructionReports()).thenReturn(Collections.singletonList(instructionReport));

    when(migrationPlanBuilderMock.build()).thenThrow(new MigrationPlanValidationException("fooo", validationReport));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    ((Map<String, Object>) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_MIGRATION_PLAN))
            .put(MigrationPlanDtoBuilder.PROP_INSTRUCTIONS, Collections.<MigrationInstructionDto>emptyList());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(MigrationPlanValidationException.class.getSimpleName()))
      .body("message", is("fooo"))
      .body("validationReport.instructionReports", hasSize(1))
      .body("validationReport.instructionReports[0].instruction", nullValue())
      .body("validationReport.instructionReports[0].failures", hasSize(1))
      .body("validationReport.instructionReports[0].failures[0]", is("failure"))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanWithNullSourceProcessInstanceId() {
    String message = "source process definition id is null";
    JoinedMigrationPlanBuilderMock migrationPlanBuilder = mock(JoinedMigrationPlanBuilderMock.class, new FluentAnswer());
    when(runtimeServiceMock.createMigrationPlan(isNull(String.class), anyString()))
      .thenReturn(migrationPlanBuilder);
    when(migrationPlanBuilder.build()).thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(null, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanWithNonExistingSourceProcessInstanceId() {
    String message = "source process definition with id " + NON_EXISTING_PROCESS_DEFINITION_ID + " does not exist";
    JoinedMigrationPlanBuilderMock migrationPlanBuilder = mock(JoinedMigrationPlanBuilderMock.class, new FluentAnswer());
    when(runtimeServiceMock.createMigrationPlan(eq(NON_EXISTING_PROCESS_DEFINITION_ID), anyString()))
      .thenReturn(migrationPlanBuilder);
    when(migrationPlanBuilder.build()).thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(NON_EXISTING_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanWithNullTargetProcessInstanceId() {
    String message = "target process definition id is null";
    JoinedMigrationPlanBuilderMock migrationPlanBuilder = mock(JoinedMigrationPlanBuilderMock.class, new FluentAnswer());
    when(runtimeServiceMock.createMigrationPlan(anyString(), isNull(String.class)))
      .thenReturn(migrationPlanBuilder);
    when(migrationPlanBuilder.build()).thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, null)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanWithNonExistingTargetProcessInstanceId() {
    String message = "target process definition with id " + NON_EXISTING_PROCESS_DEFINITION_ID + " does not exist";
    JoinedMigrationPlanBuilderMock migrationPlanBuilder = mock(JoinedMigrationPlanBuilderMock.class, new FluentAnswer());
    when(runtimeServiceMock.createMigrationPlan(anyString(), eq(NON_EXISTING_PROCESS_DEFINITION_ID)))
      .thenReturn(migrationPlanBuilder);
    when(migrationPlanBuilder.build()).thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, NON_EXISTING_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanWithNullSourceActivityId() {
    String message = "sourceActivityId is null";
    when(migrationPlanBuilderMock.mapActivities(isNull(String.class), anyString()))
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(null, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanWithNonExistingSourceActivityId() {
    String message = "sourceActivity is null";
    when(migrationPlanBuilderMock.mapActivities(eq(NON_EXISTING_ACTIVITY_ID), anyString()))
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(NON_EXISTING_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanWithNullTargetActivityId() {
    String message = "targetActivityId is null";
    when(migrationPlanBuilderMock.mapActivities(anyString(), isNull(String.class)))
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, null)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanWithNonExistingTargetActivityId() {
    String message = "targetActivity is null";
    when(migrationPlanBuilderMock.mapActivities(anyString(), eq(NON_EXISTING_ACTIVITY_ID)))
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, NON_EXISTING_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanValidationException() {

    MigrationInstruction migrationInstruction = mock(MigrationInstruction.class);
    when(migrationInstruction.getSourceActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(migrationInstruction.getTargetActivityId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);

    MigrationInstructionValidationReport instructionReport1 = mock(MigrationInstructionValidationReport.class);
    when(instructionReport1.getMigrationInstruction()).thenReturn(migrationInstruction);
    when(instructionReport1.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));

    MigrationInstructionValidationReport instructionReport2 = mock(MigrationInstructionValidationReport.class);
    when(instructionReport2.getMigrationInstruction()).thenReturn(migrationInstruction);
    when(instructionReport2.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));

    MigrationPlanValidationReport validationReport = mock(MigrationPlanValidationReport.class);
    when(validationReport.getInstructionReports()).thenReturn(Arrays.asList(instructionReport1, instructionReport2));

    when(migrationPlanBuilderMock.build()).thenThrow(new MigrationPlanValidationException("fooo", validationReport));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(MigrationPlanValidationException.class.getSimpleName()))
      .body("message", is("fooo"))
      .body("validationReport.instructionReports", hasSize(2))
      .body("validationReport.instructionReports[0].instruction.sourceActivityIds", hasSize(1))
      .body("validationReport.instructionReports[0].instruction.sourceActivityIds[0]", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instructionReports[0].instruction.targetActivityIds", hasSize(1))
      .body("validationReport.instructionReports[0].instruction.targetActivityIds[0]", is(ANOTHER_EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instructionReports[0].failures", hasSize(2))
      .body("validationReport.instructionReports[0].failures[0]", is("failure1"))
      .body("validationReport.instructionReports[0].failures[1]", is("failure2"))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigratingProcessInstanceValidationException() {

    MigrationInstruction migrationInstruction = mock(MigrationInstruction.class);
    when(migrationInstruction.getSourceActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(migrationInstruction.getTargetActivityId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);

    MigratingActivityInstanceValidationReport instanceReport1 = mock(MigratingActivityInstanceValidationReport.class);
    when(instanceReport1.getActivityInstanceId()).thenReturn(EXAMPLE_ACTIVITY_INSTANCE_ID);
    when(instanceReport1.getMigrationInstruction()).thenReturn(migrationInstruction);
    when(instanceReport1.getSourceScopeId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(instanceReport1.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));

    MigratingTransitionInstanceValidationReport instanceReport2 = mock(MigratingTransitionInstanceValidationReport.class);
    when(instanceReport2.getTransitionInstanceId()).thenReturn("transitionInstanceId");
    when(instanceReport2.getMigrationInstruction()).thenReturn(migrationInstruction);
    when(instanceReport2.getSourceScopeId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(instanceReport2.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));

    MigratingProcessInstanceValidationReport processInstanceReport = mock(MigratingProcessInstanceValidationReport.class);
    when(processInstanceReport.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(processInstanceReport.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));
    when(processInstanceReport.getActivityInstanceReports()).thenReturn(Arrays.asList(instanceReport1));
    when(processInstanceReport.getTransitionInstanceReports()).thenReturn(Arrays.asList(instanceReport2));

    doThrow(new MigratingProcessInstanceValidationException("fooo", processInstanceReport))
      .when(migrationPlanExecutionBuilderMock).execute();

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(MigratingProcessInstanceValidationException.class.getSimpleName()))
      .body("message", is("fooo"))
      .body("validationReport.processInstanceId", is(EXAMPLE_PROCESS_INSTANCE_ID))
      .body("validationReport.failures", hasSize(2))
      .body("validationReport.failures[0]", is("failure1"))
      .body("validationReport.failures[1]", is("failure2"))
      .body("validationReport.activityInstanceValidationReports", hasSize(1))
      .body("validationReport.activityInstanceValidationReports[0].migrationInstruction.sourceActivityIds", hasSize(1))
      .body("validationReport.activityInstanceValidationReports[0].migrationInstruction.sourceActivityIds[0]", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.activityInstanceValidationReports[0].migrationInstruction.targetActivityIds", hasSize(1))
      .body("validationReport.activityInstanceValidationReports[0].migrationInstruction.targetActivityIds[0]", is(ANOTHER_EXAMPLE_ACTIVITY_ID))
      .body("validationReport.activityInstanceValidationReports[0].activityInstanceId", is(EXAMPLE_ACTIVITY_INSTANCE_ID))
      .body("validationReport.activityInstanceValidationReports[0].sourceScopeId", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.activityInstanceValidationReports[0].failures", hasSize(2))
      .body("validationReport.activityInstanceValidationReports[0].failures[0]", is("failure1"))
      .body("validationReport.activityInstanceValidationReports[0].failures[1]", is("failure2"))
      .body("validationReport.transitionInstanceValidationReports", hasSize(1))
      .body("validationReport.transitionInstanceValidationReports[0].migrationInstruction.sourceActivityIds", hasSize(1))
      .body("validationReport.transitionInstanceValidationReports[0].migrationInstruction.sourceActivityIds[0]", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.transitionInstanceValidationReports[0].migrationInstruction.targetActivityIds", hasSize(1))
      .body("validationReport.transitionInstanceValidationReports[0].migrationInstruction.targetActivityIds[0]", is(ANOTHER_EXAMPLE_ACTIVITY_ID))
      .body("validationReport.transitionInstanceValidationReports[0].transitionInstanceId", is("transitionInstanceId"))
      .body("validationReport.transitionInstanceValidationReports[0].sourceScopeId", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.transitionInstanceValidationReports[0].failures", hasSize(2))
      .body("validationReport.transitionInstanceValidationReports[0].failures[0]", is("failure1"))
      .body("validationReport.transitionInstanceValidationReports[0].failures[1]", is("failure2"))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanAsync() {
    Batch batchMock = createMockBatch();
    when(migrationPlanExecutionBuilderMock.executeAsync()).thenReturn(batchMock);

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", is(EXAMPLE_BATCH_ID))
      .body("type", is(EXAMPLE_BATCH_TYPE))
      .body("totalJobs", is(EXAMPLE_BATCH_TOTAL_JOBS))
      .body("batchJobsPerSeed", is(EXAMPLE_BATCH_JOBS_PER_SEED))
      .body("invocationsPerBatchJob", is(EXAMPLE_INVOCATIONS_PER_BATCH_JOB))
      .body("seedJobDefinitionId", is(EXAMPLE_SEED_JOB_DEFINITION_ID))
      .body("monitorJobDefinitionId", is(EXAMPLE_MONITOR_JOB_DEFINITION_ID))
      .body("batchJobDefinitionId", is(EXAMPLE_BATCH_JOB_DEFINITION_ID))
      .body("tenantId", is(EXAMPLE_TENANT_ID))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);

    verifyCreateMigrationPlanInteraction(migrationPlanBuilderMock, (Map<String, Object>) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_MIGRATION_PLAN));
    verifyMigrationPlanAsyncExecutionInteraction(migrationExecution);
  }

  @Test
  public void executeMigrationPlanAsyncWithProcessInstanceQuery() {
    when(runtimeServiceMock.createProcessInstanceQuery())
      .thenReturn(new ProcessInstanceQueryImpl());

    ProcessInstanceQueryDto processInstanceQuery = new ProcessInstanceQueryDto();
    processInstanceQuery.setProcessDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID);

    Batch batchMock = createMockBatch();
    when(migrationPlanExecutionBuilderMock.executeAsync()).thenReturn(batchMock);

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstanceQuery(processInstanceQuery)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", is(EXAMPLE_BATCH_ID))
      .body("type", is(EXAMPLE_BATCH_TYPE))
      .body("totalJobs", is(EXAMPLE_BATCH_TOTAL_JOBS))
      .body("batchJobsPerSeed", is(EXAMPLE_BATCH_JOBS_PER_SEED))
      .body("invocationsPerBatchJob", is(EXAMPLE_INVOCATIONS_PER_BATCH_JOB))
      .body("seedJobDefinitionId", is(EXAMPLE_SEED_JOB_DEFINITION_ID))
      .body("monitorJobDefinitionId", is(EXAMPLE_MONITOR_JOB_DEFINITION_ID))
      .body("batchJobDefinitionId", is(EXAMPLE_BATCH_JOB_DEFINITION_ID))
      .body("tenantId", is(EXAMPLE_TENANT_ID))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);

    verifyCreateMigrationPlanInteraction(migrationPlanBuilderMock, (Map<String, Object>) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_MIGRATION_PLAN));
    verifyMigrationPlanAsyncExecutionInteraction(migrationExecution);
  }

  @Test
  public void executeMigrationPlanAsyncSkipListeners() {
    Batch batchMock = createMockBatch();
    when(migrationPlanExecutionBuilderMock.executeAsync()).thenReturn(batchMock);

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID)
      .skipCustomListeners(true)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);

    verifyMigrationPlanAsyncExecutionInteraction(migrationExecution);
  }

  @Test
  public void executeMigrationPlanAsyncSkipIoMappings() {
    Batch batchMock = createMockBatch();
    when(migrationPlanExecutionBuilderMock.executeAsync()).thenReturn(batchMock);

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID)
      .skipIoMappings(true)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);

    verifyMigrationPlanAsyncExecutionInteraction(migrationExecution);
  }

  @Test
  public void executeMigrationPlanAsyncWithNullInstructions() {
    MigrationInstructionValidationReport instructionReport = mock(MigrationInstructionValidationReport.class);
    when(instructionReport.getMigrationInstruction()).thenReturn(null);
    when(instructionReport.getFailures()).thenReturn(Collections.singletonList("failure"));

    MigrationPlanValidationReport validationReport = mock(MigrationPlanValidationReport.class);
    when(validationReport.getInstructionReports()).thenReturn(Collections.singletonList(instructionReport));

    when(migrationPlanBuilderMock.build()).thenThrow(new MigrationPlanValidationException("fooo", validationReport));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(MigrationPlanValidationException.class.getSimpleName()))
      .body("message", is("fooo"))
      .body("validationReport.instructionReports", hasSize(1))
      .body("validationReport.instructionReports[0].instruction", nullValue())
      .body("validationReport.instructionReports[0].failures", hasSize(1))
      .body("validationReport.instructionReports[0].failures[0]", is("failure"))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanAsyncWithEmptyInstructions() {
    MigrationInstructionValidationReport instructionReport = mock(MigrationInstructionValidationReport.class);
    when(instructionReport.getMigrationInstruction()).thenReturn(null);
    when(instructionReport.getFailures()).thenReturn(Collections.singletonList("failure"));

    MigrationPlanValidationReport validationReport = mock(MigrationPlanValidationReport.class);
    when(validationReport.getInstructionReports()).thenReturn(Collections.singletonList(instructionReport));

    when(migrationPlanBuilderMock.build()).thenThrow(new MigrationPlanValidationException("fooo", validationReport));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    ((Map<String, Object>) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_MIGRATION_PLAN))
            .put(MigrationPlanDtoBuilder.PROP_INSTRUCTIONS, Collections.<MigrationInstructionDto>emptyList());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(MigrationPlanValidationException.class.getSimpleName()))
      .body("message", is("fooo"))
      .body("validationReport.instructionReports", hasSize(1))
      .body("validationReport.instructionReports[0].instruction", nullValue())
      .body("validationReport.instructionReports[0].failures", hasSize(1))
      .body("validationReport.instructionReports[0].failures[0]", is("failure"))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanAsyncWithNullSourceProcessDefinitionId() {
    String message = "source process definition id is null";
    JoinedMigrationPlanBuilderMock migrationPlanBuilder = mock(JoinedMigrationPlanBuilderMock.class, new FluentAnswer());
    when(runtimeServiceMock.createMigrationPlan(isNull(String.class), anyString()))
      .thenReturn(migrationPlanBuilder);
    when(migrationPlanBuilder.build()).thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(null, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanAsyncWithNonExistingSourceProcessDefinitionId() {
    String message = "source process definition with id " + NON_EXISTING_PROCESS_DEFINITION_ID + " does not exist";
    JoinedMigrationPlanBuilderMock migrationPlanBuilder = mock(JoinedMigrationPlanBuilderMock.class, new FluentAnswer());
    when(runtimeServiceMock.createMigrationPlan(eq(NON_EXISTING_PROCESS_DEFINITION_ID), anyString()))
      .thenReturn(migrationPlanBuilder);
    when(migrationPlanBuilder.build()).thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(NON_EXISTING_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanAsyncWithNullTargetProcessDefinitionId() {
    String message = "target process definition id is null";
    JoinedMigrationPlanBuilderMock migrationPlanBuilder = mock(JoinedMigrationPlanBuilderMock.class, new FluentAnswer());
    when(runtimeServiceMock.createMigrationPlan(anyString(), isNull(String.class)))
      .thenReturn(migrationPlanBuilder);
    when(migrationPlanBuilder.build()).thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, null)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanAsyncWithNonExistingTargetProcessDefinitionId() {
    String message = "target process definition with id " + NON_EXISTING_PROCESS_DEFINITION_ID + " does not exist";
    JoinedMigrationPlanBuilderMock migrationPlanBuilder = mock(JoinedMigrationPlanBuilderMock.class, new FluentAnswer());
    when(runtimeServiceMock.createMigrationPlan(anyString(), eq(NON_EXISTING_PROCESS_DEFINITION_ID)))
      .thenReturn(migrationPlanBuilder);
    when(migrationPlanBuilder.build()).thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, NON_EXISTING_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanAsyncWithNullSourceActivityId() {
    String message = "sourceActivityId is null";
    when(migrationPlanBuilderMock.mapActivities(isNull(String.class), anyString()))
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(null, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanAsyncWithNonExistingSourceActivityId() {
    String message = "sourceActivity is null";
    when(migrationPlanBuilderMock.mapActivities(eq(NON_EXISTING_ACTIVITY_ID), anyString()))
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(NON_EXISTING_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanAsyncWithNullTargetActivityId() {
    String message = "targetActivityId is null";
    when(migrationPlanBuilderMock.mapActivities(anyString(), isNull(String.class)))
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, null)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanAsyncWithNonExistingTargetActivityId() {
    String message = "targetActivity is null";
    when(migrationPlanBuilderMock.mapActivities(anyString(), eq(NON_EXISTING_ACTIVITY_ID)))
      .thenThrow(new BadUserRequestException(message));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, NON_EXISTING_ACTIVITY_ID)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", is(message))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  @Test
  public void executeMigrationPlanAsyncValidationException() {

    MigrationInstruction migrationInstruction = mock(MigrationInstruction.class);
    when(migrationInstruction.getSourceActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(migrationInstruction.getTargetActivityId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);

    MigrationInstructionValidationReport instructionReport1 = mock(MigrationInstructionValidationReport.class);
    when(instructionReport1.getMigrationInstruction()).thenReturn(migrationInstruction);
    when(instructionReport1.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));

    MigrationInstructionValidationReport instructionReport2 = mock(MigrationInstructionValidationReport.class);
    when(instructionReport2.getMigrationInstruction()).thenReturn(migrationInstruction);
    when(instructionReport2.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));

    MigrationPlanValidationReport validationReport = mock(MigrationPlanValidationReport.class);
    when(validationReport.getInstructionReports()).thenReturn(Arrays.asList(instructionReport1, instructionReport2));

    when(migrationPlanBuilderMock.build()).thenThrow(new MigrationPlanValidationException("fooo", validationReport));

    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
      .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(MigrationPlanValidationException.class.getSimpleName()))
      .body("message", is("fooo"))
      .body("validationReport.instructionReports", hasSize(2))
      .body("validationReport.instructionReports[0].instruction.sourceActivityIds", hasSize(1))
      .body("validationReport.instructionReports[0].instruction.sourceActivityIds[0]", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instructionReports[0].instruction.targetActivityIds", hasSize(1))
      .body("validationReport.instructionReports[0].instruction.targetActivityIds[0]", is(ANOTHER_EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instructionReports[0].failures", hasSize(2))
      .body("validationReport.instructionReports[0].failures[0]", is("failure1"))
      .body("validationReport.instructionReports[0].failures[1]", is("failure2"))
    .when()
      .post(EXECUTE_MIGRATION_ASYNC_URL);
  }

  @Test
  public void executeMigrationPlanUpdateEventTrigger() {
    Map<String, Object> migrationExecution = new MigrationExecutionDtoBuilder()
      .migrationPlan(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
        .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID, true)
        .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID, false)
        .done()
      .processInstances(EXAMPLE_PROCESS_INSTANCE_ID, ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationExecution)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(EXECUTE_MIGRATION_URL);

    verifyCreateMigrationPlanInteraction(migrationPlanBuilderMock, (Map<String, Object>) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_MIGRATION_PLAN));
    verifyMigrationPlanExecutionInteraction(migrationExecution);
  }

  @Test
  public void validateMigrationPlan() {
    Map<String, Object> migrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
      .instruction(ANOTHER_EXAMPLE_ACTIVITY_ID, EXAMPLE_ACTIVITY_ID, true)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationPlan)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("instructionReports", hasSize(0))
    .when()
      .post(VALIDATE_MIGRATION_URL);

    verifyCreateMigrationPlanInteraction(migrationPlanBuilderMock, migrationPlan);
  }

  @Test
  public void validateMigrationPlanValidationException() {
    MigrationInstruction migrationInstruction = mock(MigrationInstruction.class);
    when(migrationInstruction.getSourceActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(migrationInstruction.getTargetActivityId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);

    MigrationInstructionValidationReport instructionReport1 = mock(MigrationInstructionValidationReport.class);
    when(instructionReport1.getMigrationInstruction()).thenReturn(migrationInstruction);
    when(instructionReport1.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));

    MigrationInstructionValidationReport instructionReport2 = mock(MigrationInstructionValidationReport.class);
    when(instructionReport2.getMigrationInstruction()).thenReturn(migrationInstruction);
    when(instructionReport2.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));

    MigrationPlanValidationReport validationReport = mock(MigrationPlanValidationReport.class);
    when(validationReport.getInstructionReports()).thenReturn(Arrays.asList(instructionReport1, instructionReport2));

    when(migrationPlanBuilderMock.build()).thenThrow(new MigrationPlanValidationException("fooo", validationReport));

    Map<String, Object> migrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .instruction(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID)
      .build();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(migrationPlan)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("instructionReports", hasSize(2))
      .body("instructionReports[0].instruction.sourceActivityIds", hasSize(1))
      .body("instructionReports[0].instruction.sourceActivityIds[0]", is(EXAMPLE_ACTIVITY_ID))
      .body("instructionReports[0].instruction.targetActivityIds", hasSize(1))
      .body("instructionReports[0].instruction.targetActivityIds[0]", is(ANOTHER_EXAMPLE_ACTIVITY_ID))
      .body("instructionReports[0].failures", hasSize(2))
      .body("instructionReports[0].failures[0]", is("failure1"))
      .body("instructionReports[0].failures[1]", is("failure2"))
    .when()
      .post(VALIDATE_MIGRATION_URL);
  }

  protected void verifyGenerateMigrationPlanResponse(Response response) {
    String responseContent = response.asString();
    String sourceProcessDefinitionId = from(responseContent).getString("sourceProcessDefinitionId");
    String targetProcessDefinitionId = from(responseContent).getString("targetProcessDefinitionId");
    List<Map<String, Object>> instructions = from(responseContent).getList("instructions");

    assertThat(sourceProcessDefinitionId).isEqualTo(EXAMPLE_PROCESS_DEFINITION_ID);
    assertThat(targetProcessDefinitionId).isEqualTo(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);

    assertThat(instructions).hasSize(2);
    assertThat(instructions.get(0))
      .includes(
        entry("sourceActivityIds", Collections.singletonList(EXAMPLE_ACTIVITY_ID)),
        entry("targetActivityIds", Collections.singletonList(ANOTHER_EXAMPLE_ACTIVITY_ID)),
        entry("updateEventTrigger", false)
      );
    assertThat(instructions.get(1))
      .includes(
        entry("sourceActivityIds", Collections.singletonList(ANOTHER_EXAMPLE_ACTIVITY_ID)),
        entry("targetActivityIds", Collections.singletonList(EXAMPLE_ACTIVITY_ID)),
        entry("updateEventTrigger", false)
      );
  }

  protected void verifyGenerateMigrationPlanInteraction(MigrationPlanBuilder migrationPlanBuilderMock, Map<String, Object> initialMigrationPlan) {
    verify(runtimeServiceMock).createMigrationPlan(eq(initialMigrationPlan.get(MigrationPlanDtoBuilder.PROP_SOURCE_PROCESS_DEFINITION_ID).toString()),
                                                   eq(initialMigrationPlan.get(MigrationPlanDtoBuilder.PROP_TARGET_PROCESS_DEFINITION_ID).toString()));
    // the map equal activities method should be called
    verify(migrationPlanBuilderMock).mapEqualActivities();
    // other instructions are ignored
    verify(migrationPlanBuilderMock, never()).mapActivities(anyString(), anyString());
  }

  protected void verifyCreateMigrationPlanInteraction(JoinedMigrationPlanBuilderMock migrationPlanBuilderMock, Map<String, Object> migrationPlan) {
    verify(runtimeServiceMock).createMigrationPlan(migrationPlan.get(MigrationPlanDtoBuilder.PROP_SOURCE_PROCESS_DEFINITION_ID).toString(),
                                                   migrationPlan.get(MigrationPlanDtoBuilder.PROP_TARGET_PROCESS_DEFINITION_ID).toString());
    // the map equal activities method should not be called
    verify(migrationPlanBuilderMock, never()).mapEqualActivities();
    // all instructions are added
    List<Map<String, Object>> instructions = (List<Map<String, Object>>) migrationPlan.get(MigrationPlanDtoBuilder.PROP_INSTRUCTIONS);
    if (instructions != null) {
      for (Map<String, Object> migrationInstructionDto : instructions) {

        InOrder inOrder = Mockito.inOrder(migrationPlanBuilderMock);
        String sourceActivityId = ((List<String>) migrationInstructionDto.get(MigrationInstructionDtoBuilder.PROP_SOURCE_ACTIVITY_IDS)).get(0);
        String targetActivityId = ((List<String>) migrationInstructionDto.get(MigrationInstructionDtoBuilder.PROP_TARGET_ACTIVITY_IDS)).get(0);
        inOrder.verify(migrationPlanBuilderMock).mapActivities(eq(sourceActivityId), eq(targetActivityId));
        Boolean updateEventTrigger = (Boolean) migrationInstructionDto.get(MigrationInstructionDtoBuilder.PROP_UPDATE_EVENT_TRIGGER);
        if (Boolean.TRUE.equals(updateEventTrigger)) {
          inOrder.verify(migrationPlanBuilderMock, immediatelyAfter()).updateEventTrigger();
        }
      }
    }
  }

  protected void verifyMigrationPlanExecutionInteraction(Map<String, Object> migrationExecution) {
    InOrder inOrder = inOrder(runtimeServiceMock, migrationPlanExecutionBuilderMock);

    inOrder.verify(runtimeServiceMock).newMigration(any(MigrationPlan.class));

    verifyMigrationExecutionBuilderInteraction(inOrder, migrationExecution);
    inOrder.verify(migrationPlanExecutionBuilderMock).execute();

    inOrder.verifyNoMoreInteractions();
  }

  protected void verifyMigrationPlanAsyncExecutionInteraction(Map<String, Object> migrationExecution) {
    InOrder inOrder = inOrder(runtimeServiceMock, migrationPlanExecutionBuilderMock);

    inOrder.verify(runtimeServiceMock).newMigration(any(MigrationPlan.class));

    verifyMigrationExecutionBuilderInteraction(inOrder, migrationExecution);
    inOrder.verify(migrationPlanExecutionBuilderMock).executeAsync();

    Mockito.verifyNoMoreInteractions(migrationPlanExecutionBuilderMock);
  }

  protected void verifyMigrationExecutionBuilderInteraction(InOrder inOrder, Map<String, Object> migrationExecution) {
    List<String> processInstanceIds = ((List<String>) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_PROCESS_INSTANCE_IDS));

    inOrder.verify(migrationPlanExecutionBuilderMock).processInstanceIds(eq(processInstanceIds));
    ProcessInstanceQueryDto processInstanceQuery = (ProcessInstanceQueryDto) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_PROCESS_INSTANCE_QUERY);
    if (processInstanceQuery != null) {
      verifyMigrationPlanExecutionProcessInstanceQuery(inOrder);
    }
    Boolean skipCustomListeners = (Boolean) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_SKIP_CUSTOM_LISTENERS);
    if (Boolean.TRUE.equals(skipCustomListeners)) {
      inOrder.verify(migrationPlanExecutionBuilderMock).skipCustomListeners();
    }
    Boolean skipIoMappings = (Boolean) migrationExecution.get(MigrationExecutionDtoBuilder.PROP_SKIP_IO_MAPPINGS);
    if (Boolean.TRUE.equals(skipIoMappings)) {
      inOrder.verify(migrationPlanExecutionBuilderMock).skipIoMappings();
    }
  }

  protected void verifyMigrationPlanExecutionProcessInstanceQuery(InOrder inOrder) {
    ArgumentCaptor<ProcessInstanceQuery> queryCapture = ArgumentCaptor.forClass(ProcessInstanceQuery.class);
    inOrder.verify(migrationPlanExecutionBuilderMock).processInstanceQuery(queryCapture.capture());

    ProcessInstanceQueryImpl actualQuery = (ProcessInstanceQueryImpl) queryCapture.getValue();
    assertThat(actualQuery).isNotNull();
    assertThat(actualQuery.getProcessDefinitionId()).isEqualTo(EXAMPLE_PROCESS_DEFINITION_ID);
  }

}
