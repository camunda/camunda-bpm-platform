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
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static junit.framework.TestCase.assertNotNull;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HistoricExternalTaskLogRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/external-task-log";
  protected static final String HISTORIC_EXTERNAL_TASK_LOG_COUNT_RESOURCE_URL = HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL + "/count";

  protected static final long EXTERNAL_TASK_LOG_QUERY_MAX_PRIORITY = Long.MAX_VALUE;
  protected static final long EXTERNAL_TASK_LOG_QUERY_MIN_PRIORITY = Long.MIN_VALUE;

  protected HistoricExternalTaskLogQuery mockedQuery;

  @Before
  public void setUpRuntimeData() throws IOException {
    mockedQuery = setUpMockHistoricExternalTaskLogQuery(MockProvider.createMockHistoricExternalTaskLogs());
  }

  protected HistoricExternalTaskLogQuery setUpMockHistoricExternalTaskLogQuery(List<HistoricExternalTaskLog> mockedHistoricExternalTaskLogs) {
    HistoricExternalTaskLogQuery mockedHistoricExternalTaskLogQuery = mock(HistoricExternalTaskLogQuery.class);
    when(mockedHistoricExternalTaskLogQuery.list()).thenReturn(mockedHistoricExternalTaskLogs);
    when(mockedHistoricExternalTaskLogQuery.count()).thenReturn((long) mockedHistoricExternalTaskLogs.size());

    when(processEngine.getHistoryService().createHistoricExternalTaskLogQuery()).thenReturn(mockedHistoricExternalTaskLogQuery);

    return mockedHistoricExternalTaskLogQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("processDefinitionKey", queryKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).list();
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryAsPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "processDefinitionId")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("timestamp", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTimestamp();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("timestamp", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTimestamp();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("externalTaskId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByExternalTaskId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("externalTaskId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByExternalTaskId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("topicName", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTopicName();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("topicName", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTopicName();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("workerId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByWorkerId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("workerId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByWorkerId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("retries", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByRetries();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("retries", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByRetries();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("priority", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByPriority();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("priority", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByPriority();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("executionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByExecutionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("executionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByExecutionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("sorting", OrderingBuilder.create()
      .orderBy("processInstanceId").desc()
      .orderBy("timestamp").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByTimestamp();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }


  @Test
  public void testQueryCountForPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .body("count", equalTo(1))
    .when()
      .post(HISTORIC_EXTERNAL_TASK_LOG_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricExternalTaskLogQuery() {
    String processInstanceId = MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;

    Response response = given()
      .queryParam("processInstanceId", processInstanceId)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).processInstanceId(processInstanceId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> logs = from(content).getList("");
    assertEquals("There should be one historic externalTask log returned.", 1, logs.size());
    assertNotNull("The returned historic externalTask log should not be null.", logs.get(0));

    String returnedId = from(content).getString("[0].id");
    String returnedTimestamp = from(content).getString("[0].timestamp");
    String returnedExternalTaskId = from(content).getString("[0].externalTaskId");
    String returnedExternalTaskTopicName = from(content).getString("[0].topicName");
    String returnedExternalTaskWorkerId = from(content).getString("[0].workerId");
    int returnedRetries = from(content).getInt("[0].retries");
    long returnedPriority = from(content).getLong("[0].priority");
    String returnedErrorMessage = from(content).getString("[0].errorMessage");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedActivityInstanceId = from(content).getString("[0].activityInstanceId");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    boolean returnedCreationLog = from(content).getBoolean("[0].creationLog");
    boolean returnedFailureLog = from(content).getBoolean("[0].failureLog");
    boolean returnedSuccessLog = from(content).getBoolean("[0].successLog");
    boolean returnedDeletionLog = from(content).getBoolean("[0].deletionLog");

    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID, returnedId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TIMESTAMP, returnedTimestamp);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXTERNAL_TASK_ID, returnedExternalTaskId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TOPIC_NAME, returnedExternalTaskTopicName);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_WORKER_ID, returnedExternalTaskWorkerId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_RETRIES, returnedRetries);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PRIORITY, returnedPriority);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ERROR_MSG, returnedErrorMessage);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_ID, returnedActivityId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_INSTANCE_ID, returnedActivityInstanceId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXECUTION_ID, returnedExecutionId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_INST_ID, returnedProcessInstanceId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_ID, returnedProcessDefinitionId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_KEY, returnedProcessDefinitionKey);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_CREATION_LOG, returnedCreationLog);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_FAILURE_LOG, returnedFailureLog);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_SUCCESS_LOG, returnedSuccessLog);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_DELETION_LOG, returnedDeletionLog);
  }

  @Test
  public void testSimpleHistoricExternalTaskLogQueryAsPost() {
    String processInstanceId = MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_INST_ID;

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("processInstanceId", processInstanceId);

    Response response =
      given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(json)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).processInstanceId(processInstanceId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> logs = from(content).getList("");
    assertEquals("There should be one historic externalTask log returned.", 1, logs.size());
    assertNotNull("The returned historic externalTask log should not be null.", logs.get(0));

    String returnedId = from(content).getString("[0].id");
    String returnedTimestamp = from(content).getString("[0].timestamp");
    String returnedExternalTaskId = from(content).getString("[0].externalTaskId");
    String returnedExternalTaskTopicName = from(content).getString("[0].topicName");
    String returnedExternalTaskWorkerId = from(content).getString("[0].workerId");
    int returnedRetries = from(content).getInt("[0].retries");
    long returnedPriority = from(content).getLong("[0].priority");
    String returnedErrorMessage = from(content).getString("[0].errorMessage");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedActivityInstanceId = from(content).getString("[0].activityInstanceId");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    boolean returnedCreationLog = from(content).getBoolean("[0].creationLog");
    boolean returnedFailureLog = from(content).getBoolean("[0].failureLog");
    boolean returnedSuccessLog = from(content).getBoolean("[0].successLog");
    boolean returnedDeletionLog = from(content).getBoolean("[0].deletionLog");

    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID, returnedId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TIMESTAMP, returnedTimestamp);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXTERNAL_TASK_ID, returnedExternalTaskId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TOPIC_NAME, returnedExternalTaskTopicName);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_WORKER_ID, returnedExternalTaskWorkerId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_RETRIES, returnedRetries);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PRIORITY, returnedPriority);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ERROR_MSG, returnedErrorMessage);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_ID, returnedActivityId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_INSTANCE_ID, returnedActivityInstanceId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXECUTION_ID, returnedExecutionId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_INST_ID, returnedProcessInstanceId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_ID, returnedProcessDefinitionId);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_KEY, returnedProcessDefinitionKey);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_CREATION_LOG, returnedCreationLog);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_FAILURE_LOG, returnedFailureLog);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_SUCCESS_LOG, returnedSuccessLog);
    assertEquals(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_DELETION_LOG, returnedDeletionLog);
  }

  @Test
  public void testStringParameters() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .queryParams(stringQueryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  public void testStringParametersAsPost() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(stringQueryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  protected Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("logId", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID);
    parameters.put("externalTaskId", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXTERNAL_TASK_ID);
    parameters.put("topicName", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TOPIC_NAME);
    parameters.put("workerId", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_WORKER_ID);
    parameters.put("errorMessage", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ERROR_MSG);
    parameters.put("processInstanceId", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_INST_ID);
    parameters.put("processDefinitionId", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_ID);
    parameters.put("processDefinitionKey", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_KEY);

    return parameters;
  }

  protected void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    verify(mockedQuery).logId(stringQueryParameters.get("logId"));
    verify(mockedQuery).externalTaskId(stringQueryParameters.get("externalTaskId"));
    verify(mockedQuery).topicName(stringQueryParameters.get("topicName"));
    verify(mockedQuery).workerId(stringQueryParameters.get("workerId"));
    verify(mockedQuery).errorMessage(stringQueryParameters.get("errorMessage"));
    verify(mockedQuery).processInstanceId(stringQueryParameters.get("processInstanceId"));
    verify(mockedQuery).processDefinitionId(stringQueryParameters.get("processDefinitionId"));
    verify(mockedQuery).processDefinitionKey(stringQueryParameters.get("processDefinitionKey"));

    verify(mockedQuery).list();
  }

  @Test
  public void testListParameters() {
    String anActId = "anActId";
    String anotherActId = "anotherActId";

    String anActInstId = "anActInstId";
    String anotherActInstId = "anotherActInstId";

    String anExecutionId = "anExecutionId";
    String anotherExecutionId = "anotherExecutionId";

    given()
      .queryParam("activityIdIn", anActId + "," + anotherActId)
      .queryParam("activityInstanceIdIn", anActInstId + "," + anotherActInstId)
      .queryParam("executionIdIn", anExecutionId + "," + anotherExecutionId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).activityIdIn(anActId, anotherActId);
    verify(mockedQuery).activityInstanceIdIn(anActInstId, anotherActInstId);
    verify(mockedQuery).executionIdIn(anExecutionId, anotherExecutionId);
    verify(mockedQuery).list();
  }

  @Test
  public void testListParametersAsPost() {
    String anActId = "anActId";
    String anotherActId = "anotherActId";

    String anActInstId = "anActInstId";
    String anotherActInstId = "anotherActInstId";

    String anExecutionId = "anExecutionId";
    String anotherExecutionId = "anotherExecutionId";

    Map<String, List<String>> json = new HashMap<String, List<String>>();
    json.put("activityIdIn", Arrays.asList(anActId, anotherActId));
    json.put("activityInstanceIdIn", Arrays.asList(anActInstId, anotherActInstId));
    json.put("executionIdIn", Arrays.asList(anExecutionId, anotherExecutionId));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).activityIdIn(anActId, anotherActId);
    verify(mockedQuery).activityInstanceIdIn(anActInstId, anotherActInstId);
    verify(mockedQuery).executionIdIn(anExecutionId, anotherExecutionId);
    verify(mockedQuery).list();
  }

  @Test
  public void testBooleanParameters() {
    Map<String, Boolean> params = getCompleteBooleanQueryParameters();

    given()
      .queryParams(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verifyBooleanParameterQueryInvocations();
  }

  @Test
  public void testBooleanParametersAsPost() {
    Map<String, Boolean> params = getCompleteBooleanQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verifyBooleanParameterQueryInvocations();

  }

  protected Map<String, Boolean> getCompleteBooleanQueryParameters() {
    Map<String, Boolean> parameters = new HashMap<String, Boolean>();

    parameters.put("creationLog", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_CREATION_LOG);
    parameters.put("failureLog", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_FAILURE_LOG);
    parameters.put("successLog", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_SUCCESS_LOG);
    parameters.put("deletionLog", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_DELETION_LOG);

    return parameters;
  }

  protected void verifyBooleanParameterQueryInvocations() {
    verify(mockedQuery).creationLog();
    verify(mockedQuery).failureLog();
    verify(mockedQuery).successLog();
    verify(mockedQuery).deletionLog();
    verify(mockedQuery).list();
  }

  @Test
  public void testIntegerParameters() {
    Map<String, Object> params = getCompleteIntegerQueryParameters();

    given()
      .queryParams(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verifyIntegerParameterQueryInvocations();
  }

  @Test
  public void testIntegerParametersAsPost() {
    Map<String, Object> params = getCompleteIntegerQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verifyIntegerParameterQueryInvocations();

  }

  protected Map<String, Object> getCompleteIntegerQueryParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("priorityLowerThanOrEquals", EXTERNAL_TASK_LOG_QUERY_MAX_PRIORITY);
    parameters.put("priorityHigherThanOrEquals", EXTERNAL_TASK_LOG_QUERY_MIN_PRIORITY);

    return parameters;
  }

  protected void verifyIntegerParameterQueryInvocations() {
    verify(mockedQuery).priorityLowerThanOrEquals(EXTERNAL_TASK_LOG_QUERY_MAX_PRIORITY);
    verify(mockedQuery).priorityHigherThanOrEquals(EXTERNAL_TASK_LOG_QUERY_MIN_PRIORITY);
    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockHistoricExternalTaskLogQuery(createMockHistoricExternalTaskLogsTwoTenants());

    Response response =
      given()
        .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> externalTaskLogs = from(content).getList("");
    assertThat(externalTaskLogs).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testTenantIdListPostParameter() {
    mockedQuery = setUpMockHistoricExternalTaskLogQuery(createMockHistoricExternalTaskLogsTwoTenants());

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));

    Response response =
      given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> externalTaskLogs = from(content).getList("");
    assertThat(externalTaskLogs).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  private List<HistoricExternalTaskLog> createMockHistoricExternalTaskLogsTwoTenants() {
    return Arrays.asList(
      MockProvider.createMockHistoricExternalTaskLog(MockProvider.EXAMPLE_TENANT_ID),
      MockProvider.createMockHistoricExternalTaskLog(MockProvider.ANOTHER_EXAMPLE_TENANT_ID));
  }

  
}
