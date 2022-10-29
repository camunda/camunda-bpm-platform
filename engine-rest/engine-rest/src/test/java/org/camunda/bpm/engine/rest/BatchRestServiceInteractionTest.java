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

import static java.util.Collections.singletonMap;

import static io.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.util.JsonPathUtil.from;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchQuery;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class BatchRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String BATCH_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/batch";
  protected static final String SINGLE_BATCH_RESOURCE_URL = BATCH_RESOURCE_URL + "/{id}";
  protected static final String SUSPENDED_BATCH_RESOURCE_URL = SINGLE_BATCH_RESOURCE_URL + "/suspended";

  protected ManagementService managementServiceMock;
  protected BatchQuery queryMock;

  @Before
  public void setUpBatchQueryMock() {
    Batch batchMock = MockProvider.createMockBatch();

    queryMock = mock(BatchQuery.class);
    when(queryMock.batchId(eq(MockProvider.EXAMPLE_BATCH_ID))).thenReturn(queryMock);
    when(queryMock.singleResult()).thenReturn(batchMock);

    managementServiceMock = mock(ManagementService.class);
    when(managementServiceMock.createBatchQuery()).thenReturn(queryMock);

    when(processEngine.getManagementService()).thenReturn(managementServiceMock);
  }

  @Test
  public void testGetBatch() {
    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_BATCH_ID)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(SINGLE_BATCH_RESOURCE_URL);

    InOrder inOrder = inOrder(queryMock);
    inOrder.verify(queryMock).batchId(MockProvider.EXAMPLE_BATCH_ID);
    inOrder.verify(queryMock).singleResult();
    inOrder.verifyNoMoreInteractions();

    verifyBatchJson(response.asString());
  }

  @Test
  public void testGetNonExistingBatch() {
    String nonExistingId = MockProvider.NON_EXISTING_ID;
    BatchQuery batchQuery = mock(BatchQuery.class);
    when(batchQuery.batchId(nonExistingId)).thenReturn(batchQuery);
    when(batchQuery.singleResult()).thenReturn(null);
    when(managementServiceMock.createBatchQuery()).thenReturn(batchQuery);

    given()
      .pathParam("id", nonExistingId)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Batch with id '" + nonExistingId + "' does not exist"))
    .when()
      .get(SINGLE_BATCH_RESOURCE_URL);
  }

  @Test
  public void deleteBatch() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_BATCH_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_BATCH_RESOURCE_URL);

    verify(managementServiceMock).deleteBatch(eq(MockProvider.EXAMPLE_BATCH_ID), eq(false));
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  public void deleteBatchNotCascade() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_BATCH_ID)
      .queryParam("cascade", false)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_BATCH_RESOURCE_URL);

    verify(managementServiceMock).deleteBatch(eq(MockProvider.EXAMPLE_BATCH_ID), eq(false));
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  public void deleteBatchCascade() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_BATCH_ID)
      .queryParam("cascade", true)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_BATCH_RESOURCE_URL);

    verify(managementServiceMock).deleteBatch(eq(MockProvider.EXAMPLE_BATCH_ID), eq(true));
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  public void deleteNonExistingBatch() {
    String nonExistingId = MockProvider.NON_EXISTING_ID;

    doThrow(new BadUserRequestException("Batch for id '" + nonExistingId + "' cannot be found"))
      .when(managementServiceMock).deleteBatch(eq(nonExistingId), eq(false));

    given()
      .pathParam("id", nonExistingId)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Unable to delete batch with id '" + nonExistingId + "'"))
    .when()
      .delete(SINGLE_BATCH_RESOURCE_URL);
  }

  @Test
  public void deleteNonExistingBatchNotCascade() {
    String nonExistingId = MockProvider.NON_EXISTING_ID;

    doThrow(new BadUserRequestException("Batch for id '" + nonExistingId + "' cannot be found"))
      .when(managementServiceMock).deleteBatch(eq(nonExistingId), eq(false));

    given()
      .pathParam("id", nonExistingId)
      .queryParam("cascade", false)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Unable to delete batch with id '" + nonExistingId + "'"))
    .when()
      .delete(SINGLE_BATCH_RESOURCE_URL);
  }

  @Test
  public void deleteNonExistingBatchCascade() {
    String nonExistingId = MockProvider.NON_EXISTING_ID;

    doThrow(new BadUserRequestException("Batch for id '" + nonExistingId + "' cannot be found"))
      .when(managementServiceMock).deleteBatch(eq(nonExistingId), eq(true));

    given()
      .pathParam("id", nonExistingId)
      .queryParam("cascade", true)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Unable to delete batch with id '" + nonExistingId + "'"))
    .when()
      .delete(SINGLE_BATCH_RESOURCE_URL);
  }

  @Test
  public void suspendBatch() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_BATCH_ID)
      .contentType(ContentType.JSON)
      .body(singletonMap("suspended", true))
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SUSPENDED_BATCH_RESOURCE_URL);

    verify(managementServiceMock).suspendBatchById(eq(MockProvider.EXAMPLE_BATCH_ID));
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  public void suspendNonExistingBatch() {
    String nonExistingId = MockProvider.NON_EXISTING_ID;

    doThrow(new BadUserRequestException("Batch for id '" + nonExistingId + "' cannot be found"))
      .when(managementServiceMock).suspendBatchById(eq(nonExistingId));

    given()
      .pathParam("id", nonExistingId)
      .contentType(ContentType.JSON)
      .body(singletonMap("suspended", true))
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Unable to suspend batch with id '" + nonExistingId + "'"))
    .when()
      .put(SUSPENDED_BATCH_RESOURCE_URL);
  }

  @Test
  public void suspendBatchUnauthorized() {
    String batchId = MockProvider.EXAMPLE_BATCH_ID;
    String expectedMessage = "The user with id 'userId' does not have 'UPDATE' permission on resource '" + batchId + "' of type 'Batch'.";

    doThrow(new AuthorizationException(expectedMessage))
      .when(managementServiceMock).suspendBatchById(eq(batchId));

    given()
      .pathParam("id", batchId)
      .contentType(ContentType.JSON)
      .body(singletonMap("suspended", true))
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(expectedMessage))
    .when()
      .put(SUSPENDED_BATCH_RESOURCE_URL);

  }

  @Test
  public void activateBatch() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_BATCH_ID)
      .contentType(ContentType.JSON)
      .body(singletonMap("suspended", false))
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SUSPENDED_BATCH_RESOURCE_URL);

    verify(managementServiceMock).activateBatchById(eq(MockProvider.EXAMPLE_BATCH_ID));
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  public void activateNonExistingBatch() {
    String nonExistingId = MockProvider.NON_EXISTING_ID;

    doThrow(new BadUserRequestException("Batch for id '" + nonExistingId + "' cannot be found"))
      .when(managementServiceMock).activateBatchById(eq(nonExistingId));

    given()
      .pathParam("id", nonExistingId)
      .contentType(ContentType.JSON)
      .body(singletonMap("suspended", false))
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Unable to activate batch with id '" + nonExistingId + "'"))
    .when()
      .put(SUSPENDED_BATCH_RESOURCE_URL);
  }

  @Test
  public void activateBatchUnauthorized() {
    String batchId = MockProvider.EXAMPLE_BATCH_ID;
    String expectedMessage = "The user with id 'userId' does not have 'UPDATE' permission on resource '" + batchId + "' of type 'Batch'.";

    doThrow(new AuthorizationException(expectedMessage))
      .when(managementServiceMock).activateBatchById(eq(batchId));

    given()
      .pathParam("id", batchId)
      .contentType(ContentType.JSON)
      .body(singletonMap("suspended", false))
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(expectedMessage))
    .when()
      .put(SUSPENDED_BATCH_RESOURCE_URL);

  }

  protected void verifyBatchJson(String batchJson) {
    BatchDto batch = from(batchJson).getObject("", BatchDto.class);
    String returnedStartTime = from(batchJson).getString("startTime");
    String returnedExecStartTime = from(batchJson).getString("executionStartTime");

    assertNotNull("The returned batch should not be null.", batch);
    assertEquals(MockProvider.EXAMPLE_BATCH_ID, batch.getId());
    assertEquals(MockProvider.EXAMPLE_BATCH_TYPE, batch.getType());
    assertEquals(MockProvider.EXAMPLE_BATCH_TOTAL_JOBS, batch.getTotalJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOBS_PER_SEED, batch.getBatchJobsPerSeed());
    assertEquals(MockProvider.EXAMPLE_INVOCATIONS_PER_BATCH_JOB, batch.getInvocationsPerBatchJob());
    assertEquals(MockProvider.EXAMPLE_SEED_JOB_DEFINITION_ID, batch.getSeedJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_MONITOR_JOB_DEFINITION_ID, batch.getMonitorJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOB_DEFINITION_ID, batch.getBatchJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_TENANT_ID, batch.getTenantId());
    assertEquals(MockProvider.EXAMPLE_USER_ID, batch.getCreateUserId());
    assertEquals(MockProvider.EXAMPLE_HISTORIC_BATCH_START_TIME, returnedStartTime);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_BATCH_START_TIME, returnedExecStartTime);
  }

}
