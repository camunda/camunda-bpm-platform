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
import static org.camunda.bpm.engine.rest.helper.MockProvider.NON_EXISTING_ACTIVITY_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;
import static org.hamcrest.CoreMatchers.is;
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
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingActivityInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationInstructionValidationReport;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.migration.MigrationPlanValidationReport;
import org.camunda.bpm.engine.rest.dto.migration.MigrationExecutionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationInstructionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanDto;
import org.camunda.bpm.engine.rest.helper.MockMigrationPlanBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.rest.util.migration.MigrationExecutionDtoBuilder;
import org.camunda.bpm.engine.rest.util.migration.MigrationPlanDtoBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.response.Response;

public class MigrationRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String MIGRATION_URL = TEST_RESOURCE_ROOT_PATH + "/migration";
  protected static final String GENERATE_MIGRATION_URL = MIGRATION_URL + "/generate";
  protected static final String EXECUTE_MIGRATION_URL = MIGRATION_URL + "/execute";

  protected RuntimeService runtimeServiceMock;
  protected MigrationPlanBuilder migrationPlanBuilderMock;
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
    MigrationPlanDto initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
      .instructions(Collections.<MigrationInstructionDto>emptyList())
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
    MigrationPlanDto initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
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
    MigrationPlanDto initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
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
    MigrationPlanDto initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID)
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
    when(runtimeServiceMock.createMigrationPlan(isNull(String.class), anyString()))
      .thenThrow(new BadUserRequestException(message));

    MigrationPlanDto initialMigrationPlan = new MigrationPlanDtoBuilder(null, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID).build();

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
    when(runtimeServiceMock.createMigrationPlan(eq(NON_EXISTING_PROCESS_DEFINITION_ID), anyString()))
      .thenThrow(new BadUserRequestException(message));

    MigrationPlanDto initialMigrationPlan = new MigrationPlanDtoBuilder(NON_EXISTING_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID).build();

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
    when(runtimeServiceMock.createMigrationPlan(anyString(), isNull(String.class)))
      .thenThrow(new BadUserRequestException(message));

    MigrationPlanDto initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, null).build();

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
    when(runtimeServiceMock.createMigrationPlan(anyString(), eq(NON_EXISTING_PROCESS_DEFINITION_ID)))
        .thenThrow(new BadUserRequestException(message));

    MigrationPlanDto initialMigrationPlan = new MigrationPlanDtoBuilder(EXAMPLE_PROCESS_DEFINITION_ID, NON_EXISTING_PROCESS_DEFINITION_ID).build();

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
  public void executeMigrationPlan() {
    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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

    verifyCreateMigrationPlanInteraction(migrationPlanBuilderMock, migrationExecution);
    verifyMigrationPlanExecutionInteraction(migrationExecution);
  }

  @Test
  public void executeMigrationPlanWithNullSourceProcessInstanceId() {
    String message = "source process definition id is null";
    when(runtimeServiceMock.createMigrationPlan(isNull(String.class), anyString()))
      .thenThrow(new BadUserRequestException(message));

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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
    when(runtimeServiceMock.createMigrationPlan(eq(NON_EXISTING_PROCESS_DEFINITION_ID), anyString()))
      .thenThrow(new BadUserRequestException(message));

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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
    when(runtimeServiceMock.createMigrationPlan(anyString(), isNull(String.class)))
      .thenThrow(new BadUserRequestException(message));

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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
    when(runtimeServiceMock.createMigrationPlan(anyString(), eq(NON_EXISTING_PROCESS_DEFINITION_ID)))
      .thenThrow(new BadUserRequestException(message));

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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

    MigratingActivityInstanceValidationReport instanceReport2 = mock(MigratingActivityInstanceValidationReport.class);
    when(instanceReport2.getActivityInstanceId()).thenReturn(EXAMPLE_ACTIVITY_INSTANCE_ID);
    when(instanceReport2.getMigrationInstruction()).thenReturn(migrationInstruction);
    when(instanceReport2.getSourceScopeId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(instanceReport2.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));

    MigratingProcessInstanceValidationReport processInstanceReport = mock(MigratingProcessInstanceValidationReport.class);
    when(processInstanceReport.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(processInstanceReport.getFailures()).thenReturn(Arrays.asList("failure1", "failure2"));
    when(processInstanceReport.getReports()).thenReturn(Arrays.asList(instanceReport1, instanceReport2));

    doThrow(new MigratingProcessInstanceValidationException("fooo", processInstanceReport))
      .when(migrationPlanExecutionBuilderMock).execute();

    MigrationExecutionDto migrationExecution = new MigrationExecutionDtoBuilder()
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
      .body("validationReport.instanceValidationReports", hasSize(2))
      .body("validationReport.instanceValidationReports[0].migrationInstruction.sourceActivityIds", hasSize(1))
      .body("validationReport.instanceValidationReports[0].migrationInstruction.sourceActivityIds[0]", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instanceValidationReports[0].migrationInstruction.targetActivityIds", hasSize(1))
      .body("validationReport.instanceValidationReports[0].migrationInstruction.targetActivityIds[0]", is(ANOTHER_EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instanceValidationReports[0].activityInstanceId", is(EXAMPLE_ACTIVITY_INSTANCE_ID))
      .body("validationReport.instanceValidationReports[0].sourceScopeId", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instanceValidationReports[0].failures", hasSize(2))
      .body("validationReport.instanceValidationReports[0].failures[0]", is("failure1"))
      .body("validationReport.instanceValidationReports[0].failures[1]", is("failure2"))
      .body("validationReport.instanceValidationReports[1].migrationInstruction.sourceActivityIds", hasSize(1))
      .body("validationReport.instanceValidationReports[1].migrationInstruction.sourceActivityIds[0]", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instanceValidationReports[1].migrationInstruction.targetActivityIds", hasSize(1))
      .body("validationReport.instanceValidationReports[1].migrationInstruction.targetActivityIds[0]", is(ANOTHER_EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instanceValidationReports[1].activityInstanceId", is(EXAMPLE_ACTIVITY_INSTANCE_ID))
      .body("validationReport.instanceValidationReports[1].sourceScopeId", is(EXAMPLE_ACTIVITY_ID))
      .body("validationReport.instanceValidationReports[1].failures", hasSize(2))
      .body("validationReport.instanceValidationReports[1].failures[0]", is("failure1"))
      .body("validationReport.instanceValidationReports[1].failures[1]", is("failure2"))
    .when()
      .post(EXECUTE_MIGRATION_URL);
  }

  protected void verifyGenerateMigrationPlanResponse(Response response) {
    String responseContent = response.asString();
    String sourceProcessDefinitionId = from(responseContent).getString("sourceProcessDefinitionId");
    String targetProcessDefinitionId = from(responseContent).getString("targetProcessDefinitionId");
    List<Map<String, List<String>>> instructions = from(responseContent).getList("instructions");

    assertThat(sourceProcessDefinitionId).isEqualTo(EXAMPLE_PROCESS_DEFINITION_ID);
    assertThat(targetProcessDefinitionId).isEqualTo(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);

    assertThat(instructions).hasSize(2);
    assertThat(instructions.get(0))
      .includes(
        entry("sourceActivityIds", Collections.singletonList(EXAMPLE_ACTIVITY_ID)),
        entry("targetActivityIds", Collections.singletonList(ANOTHER_EXAMPLE_ACTIVITY_ID))
      );
    assertThat(instructions.get(1))
      .includes(
        entry("sourceActivityIds", Collections.singletonList(ANOTHER_EXAMPLE_ACTIVITY_ID)),
        entry("targetActivityIds", Collections.singletonList(EXAMPLE_ACTIVITY_ID))
      );
  }

  protected void verifyGenerateMigrationPlanInteraction(MigrationPlanBuilder migrationPlanBuilderMock, MigrationPlanDto initialMigrationPlan) {
    verify(runtimeServiceMock).createMigrationPlan(eq(initialMigrationPlan.getSourceProcessDefinitionId()), eq(initialMigrationPlan.getTargetProcessDefinitionId()));
    // the map equal activities method should be called
    verify(migrationPlanBuilderMock).mapEqualActivities();
    // other instructions are ignored
    verify(migrationPlanBuilderMock, never()).mapActivities(anyString(), anyString());
    verify(migrationPlanBuilderMock, never()).mapActivities(anyString(), anyString());
  }

  protected void verifyCreateMigrationPlanInteraction(MigrationPlanBuilder migrationPlanBuilderMock, MigrationExecutionDto migrationExecution) {
    MigrationPlanDto migrationPlan = migrationExecution.getMigrationPlan();
    verify(runtimeServiceMock).createMigrationPlan(migrationPlan.getSourceProcessDefinitionId(), migrationPlan.getTargetProcessDefinitionId());
    // the map equal activities method should not be called
    verify(migrationPlanBuilderMock, never()).mapEqualActivities();
    // all instructions are added
    for (MigrationInstructionDto migrationInstructionDto : migrationPlan.getInstructions()) {
      verify(migrationPlanBuilderMock).mapActivities(eq(migrationInstructionDto.getSourceActivityIds().get(0)), eq(migrationInstructionDto.getTargetActivityIds().get(0)));
    }
  }

  protected void verifyMigrationPlanExecutionInteraction(MigrationExecutionDto migrationExecution) {
    InOrder inOrder = inOrder(runtimeServiceMock, migrationPlanExecutionBuilderMock);
    inOrder.verify(runtimeServiceMock).newMigration(any(MigrationPlan.class));
    inOrder.verify(migrationPlanExecutionBuilderMock).processInstanceIds(eq(migrationExecution.getProcessInstanceIds()));
    inOrder.verify(migrationPlanExecutionBuilderMock).execute();
    inOrder.verifyNoMoreInteractions();
  }

}
