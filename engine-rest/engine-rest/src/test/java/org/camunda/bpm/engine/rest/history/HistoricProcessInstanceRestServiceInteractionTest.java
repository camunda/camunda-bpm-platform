/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.history;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.JsonPathUtil;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HistoricProcessInstanceRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();
  
  protected static final String DELETE_REASON = "deleteReason";
  protected static final String TEST_DELETE_REASON = "test";
  protected static final String HISTORIC_PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/process-instance";
  protected static final String HISTORIC_SINGLE_PROCESS_INSTANCE_URL = HISTORIC_PROCESS_INSTANCE_URL + "/{id}";
  protected static final String DELETE_HISTORIC_PROCESS_INSTANCES_ASYNC_URL = HISTORIC_PROCESS_INSTANCE_URL + "/delete";

  private HistoryService historyServiceMock;

  @Before
  public void setUpRuntimeData() {
    historyServiceMock = mock(HistoryService.class);

    // runtime service
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);
  }

  @Test
  public void testGetSingleInstance() {
    HistoricProcessInstance mockInstance = MockProvider.createMockHistoricProcessInstance();
    HistoricProcessInstanceQuery sampleInstanceQuery = mock(HistoricProcessInstanceQuery.class);

    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(mockInstance);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);

    String content = response.asString();

    String returnedProcessInstanceId = from(content).getString("id");
    String returnedProcessInstanceBusinessKey = from(content).getString("businessKey");
    String returnedProcessDefinitionId = from(content).getString("processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("processDefinitionKey");
    String returnedStartTime = from(content).getString("startTime");
    String returnedEndTime = from(content).getString("endTime");
    long returnedDurationInMillis = from(content).getLong("durationInMillis");
    String returnedStartUserId = from(content).getString("startUserId");
    String returnedStartActivityId = from(content).getString("startActivityId");
    String returnedDeleteReason = from(content).getString(DELETE_REASON);
    String returnedSuperProcessInstanceId = from(content).getString("superProcessInstanceId");
    String returnedSuperCaseInstanceId = from(content).getString("superCaseInstanceId");
    String returnedCaseInstanceId = from(content).getString("caseInstanceId");
    String returnedTenantId = from(content).getString("tenantId");
    String returnedState = from(content).getString("state");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY, returnedProcessInstanceBusinessKey);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, returnedProcessDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME, returnedStartTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME, returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID, returnedStartUserId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID, returnedStartActivityId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON, returnedDeleteReason);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID, returnedSuperProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_CASE_INSTANCE_ID, returnedSuperCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STATE, returnedState);

  }

  @Test
  public void testGetNonExistingProcessInstance() {
    HistoricProcessInstanceQuery sampleInstanceQuery = mock(HistoricProcessInstanceQuery.class);

    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(anyString())).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(null);

    given().pathParam("id", "aNonExistingInstanceId")
        .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Historic process instance with id aNonExistingInstanceId does not exist"))
        .when().get(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteProcessInstance() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
        .when().delete(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);

    verify(historyServiceMock).deleteHistoricProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
  }

  @Test
  public void testDeleteNonExistingProcessInstance() {
    doThrow(new ProcessEngineException("expected exception")).when(historyServiceMock).deleteHistoricProcessInstance(anyString());

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Historic process instance with id " + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID + " does not exist"))
        .when().delete(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteProcessInstanceThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(historyServiceMock).deleteHistoricProcessInstance(anyString());

    given()
        .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
        .when()
        .delete(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteAsync() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    Batch batchEntity = MockProvider.createMockBatch();
    when(historyServiceMock.deleteHistoricProcessInstancesAsync(
        anyListOf(String.class),
        any(HistoricProcessInstanceQuery.class),
        anyString())
    ).thenReturn(batchEntity);

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    messageBodyJson.put("historicProcessInstanceIds", ids);
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);

    Response response = given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(DELETE_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(historyServiceMock, times(1)).deleteHistoricProcessInstancesAsync(
        eq(ids), eq((HistoricProcessInstanceQuery) null), eq(TEST_DELETE_REASON));
  }

  @Test
  public void testDeleteAsyncWithQuery() {
    Batch batchEntity = MockProvider.createMockBatch();
    when(historyServiceMock.deleteHistoricProcessInstancesAsync(
        anyListOf(String.class),
        any(HistoricProcessInstanceQuery.class),
        anyString())
    ).thenReturn(batchEntity);

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);
    HistoricProcessInstanceQueryDto query = new HistoricProcessInstanceQueryDto();
    messageBodyJson.put("historicProcessInstanceQuery", query);

    Response response = given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(DELETE_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(historyServiceMock, times(1)).deleteHistoricProcessInstancesAsync(
        eq((List<String>) null), any(HistoricProcessInstanceQuery.class), Mockito.eq(TEST_DELETE_REASON));
  }


  @Test
  public void testDeleteAsyncWithBadRequestQuery() {
    doThrow(new BadUserRequestException("process instance ids are empty"))
        .when(historyServiceMock).deleteHistoricProcessInstancesAsync(eq((List<String>) null), eq((HistoricProcessInstanceQuery) null), anyString());

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);

    given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .when().post(DELETE_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);
  }

  protected void verifyBatchJson(String batchJson) {
    BatchDto batch = JsonPathUtil.from(batchJson).getObject("", BatchDto.class);
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
  }


}
