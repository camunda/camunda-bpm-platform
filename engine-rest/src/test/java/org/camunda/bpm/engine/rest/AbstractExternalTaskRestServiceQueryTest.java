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
package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractExternalTaskRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String EXTERNAL_TASK_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/external-task";
  protected static final String EXTERNAL_TASK_COUNT_QUERY_URL = EXTERNAL_TASK_QUERY_URL + "/count";

  protected ExternalTaskQuery mockQuery;

  @Before
  public void setUpRuntimeData() {
    mockQuery = setUpMockExternalTaskQuery(MockProvider.createMockExternalTasks());
  }

  private ExternalTaskQuery setUpMockExternalTaskQuery(List<ExternalTask> mockedTasks) {
    ExternalTaskQuery sampleTaskQuery = mock(ExternalTaskQuery.class);
    when(sampleTaskQuery.list()).thenReturn(mockedTasks);
    when(sampleTaskQuery.count()).thenReturn((long) mockedTasks.size());

    when(processEngine.getExternalTaskService().createExternalTaskQuery()).thenReturn(sampleTaskQuery);

    return sampleTaskQuery;
  }

  @Test
  public void testInvalidDateParameter() {
    given().queryParams("lockExpirationBefore", "anInvalidDate")
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot set query parameter 'lockExpirationBefore' to value 'anInvalidDate': "
          + "Cannot convert value \"anInvalidDate\" to java type java.util.Date"))
      .when().get(EXTERNAL_TASK_QUERY_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "processInstanceId")
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(EXTERNAL_TASK_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(EXTERNAL_TASK_QUERY_URL);
  }

  @Test
  public void testSimpleTaskQuery() {
    Response response = given()
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(EXTERNAL_TASK_QUERY_URL);

    Mockito.verify(mockQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one external task returned.", 1, instances.size());
    Assert.assertNotNull("The returned external task should not be null.", instances.get(0));

    String activityId = from(content).getString("[0].activityId");
    String activityInstanceId = from(content).getString("[0].activityInstanceId");
    String errorMessage = from(content).getString("[0].errorMessage");
    String executionId = from(content).getString("[0].executionId");
    String id = from(content).getString("[0].id");
    String lockExpirationTime = from(content).getString("[0].lockExpirationTime");
    String processDefinitionId = from(content).getString("[0].processDefinitionId");
    String processDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String processInstanceId = from(content).getString("[0].processInstanceId");
    Integer retries = from(content).getInt("[0].retries");
    Boolean suspended = from(content).getBoolean("[0].suspended");
    String topicName = from(content).getString("[0].topicName");
    String workerId = from(content).getString("[0].workerId");

    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_ID, activityId);
    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_INSTANCE_ID, activityInstanceId);
    Assert.assertEquals(MockProvider.EXTERNAL_TASK_ERROR_MESSAGE, errorMessage);
    Assert.assertEquals(MockProvider.EXAMPLE_EXECUTION_ID, executionId);
    Assert.assertEquals(MockProvider.EXTERNAL_TASK_ID, id);
    Assert.assertEquals(MockProvider.EXTERNAL_TASK_LOCK_EXPIRATION_TIME, lockExpirationTime);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, processDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, processDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, processInstanceId);
    Assert.assertEquals(MockProvider.EXTERNAL_TASK_RETRIES, retries);
    Assert.assertEquals(MockProvider.EXTERNAL_TASK_SUSPENDED, suspended);
    Assert.assertEquals(MockProvider.EXTERNAL_TASK_TOPIC_NAME, topicName);
    Assert.assertEquals(MockProvider.EXTERNAL_TASK_WORKER_ID, workerId);

  }

  @Test
  public void testCompleteGETQuery() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("externalTaskId", "someExternalTaskId");
    parameters.put("activityId", "someActivityId");
    parameters.put("lockExpirationBefore", "2013-01-23T14:42:42");
    parameters.put("lockExpirationAfter", "2013-01-23T15:52:52");
    parameters.put("topicName", "someTopic");
    parameters.put("locked", "true");
    parameters.put("notLocked", "true");
    parameters.put("executionId", "someExecutionId");
    parameters.put("processInstanceId", "someProcessInstanceId");
    parameters.put("processDefinitionId", "someProcessDefinitionId");
    parameters.put("active", "true");
    parameters.put("suspended", "true");
    parameters.put("withRetriesLeft", "true");
    parameters.put("noRetriesLeft", "true");
    parameters.put("workerId", "someWorkerId");

    given()
      .queryParams(parameters)
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(EXTERNAL_TASK_QUERY_URL);

    verify(mockQuery).externalTaskId("someExternalTaskId");
    verify(mockQuery).activityId("someActivityId");
    verify(mockQuery).lockExpirationBefore(DateTimeUtil.parseDate("2013-01-23T14:42:42"));
    verify(mockQuery).lockExpirationAfter(DateTimeUtil.parseDate("2013-01-23T15:52:52"));
    verify(mockQuery).topicName("someTopic");
    verify(mockQuery).locked();
    verify(mockQuery).notLocked();
    verify(mockQuery).executionId("someExecutionId");
    verify(mockQuery).processInstanceId("someProcessInstanceId");
    verify(mockQuery).processDefinitionId("someProcessDefinitionId");
    verify(mockQuery).active();
    verify(mockQuery).suspended();
    verify(mockQuery).withRetriesLeft();
    verify(mockQuery).noRetriesLeft();
    verify(mockQuery).workerId("someWorkerId");
  }

  @Test
  public void testCompletePOSTQuery() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("externalTaskId", "someExternalTaskId");
    parameters.put("activityId", "someActivityId");
    parameters.put("lockExpirationBefore", "2013-01-23T14:42:42");
    parameters.put("lockExpirationAfter", "2013-01-23T15:52:52");
    parameters.put("topicName", "someTopic");
    parameters.put("locked", "true");
    parameters.put("notLocked", "true");
    parameters.put("executionId", "someExecutionId");
    parameters.put("processInstanceId", "someProcessInstanceId");
    parameters.put("processDefinitionId", "someProcessDefinitionId");
    parameters.put("active", "true");
    parameters.put("suspended", "true");
    parameters.put("withRetriesLeft", "true");
    parameters.put("noRetriesLeft", "true");
    parameters.put("workerId", "someWorkerId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().post(EXTERNAL_TASK_QUERY_URL);

    verify(mockQuery).externalTaskId("someExternalTaskId");
    verify(mockQuery).activityId("someActivityId");
    verify(mockQuery).lockExpirationBefore(DateTimeUtil.parseDate("2013-01-23T14:42:42"));
    verify(mockQuery).lockExpirationAfter(DateTimeUtil.parseDate("2013-01-23T15:52:52"));
    verify(mockQuery).topicName("someTopic");
    verify(mockQuery).locked();
    verify(mockQuery).notLocked();
    verify(mockQuery).executionId("someExecutionId");
    verify(mockQuery).processInstanceId("someProcessInstanceId");
    verify(mockQuery).processDefinitionId("someProcessDefinitionId");
    verify(mockQuery).active();
    verify(mockQuery).suspended();
    verify(mockQuery).withRetriesLeft();
    verify(mockQuery).noRetriesLeft();
    verify(mockQuery).workerId("someWorkerId");
  }

  @Test
  public void testGETQuerySorting() {
    // desc
    InOrder inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("id", "desc", Status.OK);
    inOrder.verify(mockQuery).orderById();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("lockExpirationTime", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByLockExpirationTime();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("processInstanceId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessInstanceId();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("processDefinitionId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessDefinitionId();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("processDefinitionKey", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockQuery).desc();

    // asc
    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("id", "asc", Status.OK);
    inOrder.verify(mockQuery).orderById();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("lockExpirationTime", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByLockExpirationTime();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("processInstanceId", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessInstanceId();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("processDefinitionId", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessDefinitionId();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyGETSorting("processDefinitionKey", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockQuery).asc();
  }

  protected void executeAndVerifyGETSorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(EXTERNAL_TASK_QUERY_URL);
  }

  @Test
  public void testPOSTQuerySorting() {
    InOrder inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifyPOSTSorting(
      OrderingBuilder.create()
        .orderBy("processDefinitionKey").desc()
        .orderBy("lockExpirationTime").asc()
        .getJson(),
      Status.OK);

    inOrder.verify(mockQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockQuery).desc();
    inOrder.verify(mockQuery).orderByLockExpirationTime();
    inOrder.verify(mockQuery).asc();
  }

  protected void executeAndVerifyPOSTSorting(List<Map<String, Object>> sortingJson, Status expectedStatus) {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("sorting", sortingJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(EXTERNAL_TASK_QUERY_URL);
  }

  @Test
  public void testPagination() {
    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(EXTERNAL_TASK_QUERY_URL);

    verify(mockQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testGETQueryCount() {
    given()
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .body("count", equalTo(1))
    .when()
      .get(EXTERNAL_TASK_COUNT_QUERY_URL);

    verify(mockQuery).count();
  }

  @Test
  public void testPOSTQueryCount() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .header("accept", MediaType.APPLICATION_JSON)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(EXTERNAL_TASK_COUNT_QUERY_URL);

    verify(mockQuery).count();
  }
}
