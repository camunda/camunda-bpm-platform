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
package org.camunda.bpm.engine.rest.history;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.util.QueryParamUtils.arrayAsCommaSeperatedList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.HistoricTaskInstanceQueryImpl;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class HistoricTaskInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_TASK_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/task";
  protected static final String HISTORIC_TASK_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_TASK_INSTANCE_RESOURCE_URL + "/count";

  protected HistoricTaskInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockHistoricTaskInstanceQuery(MockProvider.createMockHistoricTaskInstances());
  }

  private HistoricTaskInstanceQuery setUpMockHistoricTaskInstanceQuery(List<HistoricTaskInstance> mockedHistoricTaskInstances) {
    mockedQuery = mock(HistoricTaskInstanceQuery.class);

    when(mockedQuery.list()).thenReturn(mockedHistoricTaskInstances);
    when(mockedQuery.count()).thenReturn((long) mockedHistoricTaskInstances.size());

    when(processEngine.getHistoryService().createHistoricTaskInstanceQuery()).thenReturn(mockedQuery);

    return mockedQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("processInstanceId", queryKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryAsPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("processInstanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
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
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceId();
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
    executeAndVerifySorting("processInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
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
    executeAndVerifySorting("duration", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricTaskInstanceDuration();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricTaskInstanceDuration();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricTaskInstanceEndTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricTaskInstanceEndTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("startTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceStartTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("startTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceStartTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskName", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskName();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskName", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskName();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskDescription", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDescription();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskDescription", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDescription();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("assignee", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskAssignee();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("assignee", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskAssignee();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("owner", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskOwner();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("owner", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskOwner();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("dueDate", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDueDate();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("dueDate", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDueDate();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("followUpDate", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskFollowUpDate();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("followUpDate", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskFollowUpDate();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("deleteReason", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeleteReason();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("deleteReason", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeleteReason();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskDefinitionKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDefinitionKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskDefinitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("priority", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskPriority();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("priority", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskPriority();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseDefinitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseDefinitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseExecutionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseExecutionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseExecutionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseExecutionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    Map<String, Object> json = new HashMap<>();
    json.put("sorting", OrderingBuilder.create()
      .orderBy("owner").desc()
      .orderBy("priority").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    inOrder.verify(mockedQuery).orderByTaskOwner();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByTaskPriority();
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
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

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
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

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
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_TASK_INSTANCE_COUNT_RESOURCE_URL);

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
        .post(HISTORIC_TASK_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricTaskInstanceQuery() {
    Response response = given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one historic task instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned historic task instance should not be null.", instances.get(0));

    verifyHistoricTaskInstanceEntries(content);
  }

  @Test
  public void testSimpleHistoricTaskInstanceQueryAsPost() {
    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(EMPTY_JSON_OBJECT)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one historic task instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned historic task instance should not be null.", instances.get(0));

    verifyHistoricTaskInstanceEntries(content);
  }

  @Test
  public void testQueryByTaskId() {
    String taskId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ID;

    given()
      .queryParam("taskId", taskId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskId(taskId);
  }

  @Test
  public void testQueryByTaskIdAsPost() {
    String taskId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ID;

    Map<String, Object> params = new HashMap<>();
    params.put("taskId", taskId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskId(taskId);
  }

  @Test
  public void testQueryByProcessInstanceId() {
    String processInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID;

    given()
      .queryParam("processInstanceId", processInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceId(processInstanceId);
  }

  @Test
  public void testQueryByProcessInstanceIdAsPost() {
    String processInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID;

    Map<String, Object> params = new HashMap<>();
    params.put("processInstanceId", processInstanceId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceId(processInstanceId);
  }

  @Test
  public void testQueryByProcessInstanceBusinessKey() {
    String processInstanceBusinessKey = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_BUSINESS_KEY;

    given()
      .queryParam("processInstanceBusinessKey", processInstanceBusinessKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceBusinessKey(processInstanceBusinessKey);
  }

  @Test
  public void testQueryByProcessInstanceBusinessKeyAsPost() {
    String processInstanceBusinessKey = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_BUSINESS_KEY;

    Map<String, Object> params = new HashMap<>();
    params.put("processInstanceBusinessKey", processInstanceBusinessKey);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceBusinessKey(processInstanceBusinessKey);
  }

  @Test
  public void testQueryByProcessInstanceBusinessKeyLike() {
    String processInstanceBusinessKeyLike = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_BUSINESS_KEY;

    given()
      .queryParam("processInstanceBusinessKeyLike", processInstanceBusinessKeyLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
  }

  @Test
  public void testQueryByProcessInstanceBusinessKeyLikeAsPost() {
    String processInstanceBusinessKeyLike = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_BUSINESS_KEY;

    Map<String, Object> params = new HashMap<>();
    params.put("processInstanceBusinessKeyLike", processInstanceBusinessKeyLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
  }

  @Test
  public void testQueryByProcessInstanceBusinessKeyIn() {
    given()
        .queryParam("processInstanceBusinessKeyIn", arrayAsCommaSeperatedList("aBusinessKey", "anotherBusinessKey"))
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceBusinessKeyIn("aBusinessKey", "anotherBusinessKey");
  }

  @Test
  public void testQueryByProcessInstanceBusinessKeyInAsPost() {
    String businessKey1 = "aBusinessKey";
    String businessKey2 = "anotherBusinessKey";
    List<String> processInstanceBusinessKeyIn = new ArrayList<>();
    processInstanceBusinessKeyIn.add(businessKey1);
    processInstanceBusinessKeyIn.add(businessKey2);

    Map<String, Object> params = new HashMap<>();
    params.put("processInstanceBusinessKeyIn", processInstanceBusinessKeyIn);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceBusinessKeyIn(businessKey1, businessKey2);
  }

  @Test
  public void testQueryByExecutionId() {
    String executionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_EXEC_ID;

    given()
      .queryParam("executionId", executionId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).executionId(executionId);
  }

  @Test
  public void testQueryByExecutionIdAsPost() {
    String executionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_EXEC_ID;

    Map<String, Object> params = new HashMap<>();
    params.put("executionId", executionId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).executionId(executionId);
  }

  @Test
  public void testQueryByActivityInstanceId() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID;

    given()
      .queryParam("activityInstanceIdIn", activityInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activityInstanceIdIn(activityInstanceId);
  }

  @Test
  public void testQueryByActivityInstanceIdAsPost() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID;

    List<String> activityInstanceIds = new ArrayList<>();
    activityInstanceIds.add(activityInstanceId);

    Map<String, Object> params = new HashMap<>();
    params.put("activityInstanceIdIn", activityInstanceIds);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activityInstanceIdIn(activityInstanceId);
  }

  @Test
  public void testQueryByActivityInstanceIds() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID;
    String anotherActivityInstanceId = "anotherActivityInstanceId";

    given()
      .queryParam("activityInstanceIdIn", activityInstanceId + "," + anotherActivityInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activityInstanceIdIn(activityInstanceId, anotherActivityInstanceId);
  }

  @Test
  public void testQueryByActivityInstanceIdsAsPost() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID;
    String anotherActivityInstanceId = "anotherActivityInstanceId";

    List<String> activityInstanceIds = new ArrayList<>();
    activityInstanceIds.add(activityInstanceId);
    activityInstanceIds.add(anotherActivityInstanceId);

    Map<String, Object> params = new HashMap<>();
    params.put("activityInstanceIdIn", activityInstanceIds);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activityInstanceIdIn(activityInstanceId, anotherActivityInstanceId);
  }

  @Test
  public void testQueryByProcessDefinitionId() {
    String processDefinitionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID;

    given()
      .queryParam("processDefinitionId", processDefinitionId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionId(processDefinitionId);
  }

  @Test
  public void testQueryByProcessDefinitionIdAsPost() {
    String processDefinitionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID;

    Map<String, Object> params = new HashMap<>();
    params.put("processDefinitionId", processDefinitionId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionId(processDefinitionId);
  }

  @Test
  public void testQueryByProcessDefinitionKey() {
    String processDefinitionKey = "aProcDefKey";

    given()
      .queryParam("processDefinitionKey", processDefinitionKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionKey(processDefinitionKey);
  }

  @Test
  public void testQueryByProcessDefinitionKeyAsPost() {
    String processDefinitionKey = "aProcDefKey";

    Map<String, Object> params = new HashMap<>();
    params.put("processDefinitionKey", processDefinitionKey);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionKey(processDefinitionKey);
  }

  @Test
  public void testQueryByProcessDefinitionName() {
    String processDefinitionName = "aProcDefName";

    given()
      .queryParam("processDefinitionName", processDefinitionName)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionName(processDefinitionName);
  }

  @Test
  public void testQueryByProcessDefinitionNameAsPost() {
    String processDefinitionName = "aProcDefName";

    Map<String, Object> params = new HashMap<>();
    params.put("processDefinitionName", processDefinitionName);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionName(processDefinitionName);
  }

  @Test
  public void testQueryByTaskName() {
    String taskName = "aTaskName";

    given()
      .queryParam("taskName", taskName)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskName(taskName);
  }

  @Test
  public void testQueryByTaskNameAsPost() {
    String taskName = "aTaskName";

    Map<String, Object> params = new HashMap<>();
    params.put("taskName", taskName);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskName(taskName);
  }

  @Test
  public void testQueryByTaskNameLike() {
    String taskNameLike = "aTaskNameLike";

    given()
      .queryParam("taskNameLike", taskNameLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskNameLike(taskNameLike);
  }

  @Test
  public void testQueryByTaskNameLikeAsPost() {
    String taskNameLike = "aTaskNameLike";

    Map<String, Object> params = new HashMap<>();
    params.put("taskNameLike", taskNameLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskNameLike(taskNameLike);
  }

  @Test
  public void testQueryByTaskDescription() {
    String taskDescription = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION;

    given()
      .queryParam("taskDescription", taskDescription)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDescription(taskDescription);
  }

  @Test
  public void testQueryByTaskDescriptionAsPost() {
    String taskDescription = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION;

    Map<String, Object> params = new HashMap<>();
    params.put("taskDescription", taskDescription);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDescription(taskDescription);
  }

  @Test
  public void testQueryByTaskDescriptionLike() {
    String taskDescriptionLike = "aTaskDescriptionLike";

    given()
      .queryParam("taskDescriptionLike", taskDescriptionLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDescriptionLike(taskDescriptionLike);
  }

  @Test
  public void testQueryByTaskDescriptionLikeAsPost() {
    String taskDescriptionLike = "aTaskDescriptionLike";

    Map<String, Object> params = new HashMap<>();
    params.put("taskDescriptionLike", taskDescriptionLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDescriptionLike(taskDescriptionLike);
  }

  @Test
  public void testQueryByTaskDefinitionKey() {
    String taskDefinitionKey = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DEF_KEY;

    given()
      .queryParam("taskDefinitionKey", taskDefinitionKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDefinitionKey(taskDefinitionKey);
  }

  @Test
  public void testQueryByTaskDefinitionKeyAsPost() {
    String taskDefinitionKey = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DEF_KEY;

    Map<String, Object> params = new HashMap<>();
    params.put("taskDefinitionKey", taskDefinitionKey);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDefinitionKey(taskDefinitionKey);
  }

  @Test
  public void testQueryByTaskDeleteReason() {
    String taskDeleteReason = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON;

    given()
      .queryParam("taskDeleteReason", taskDeleteReason)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDeleteReason(taskDeleteReason);
  }

  @Test
  public void testQueryByTaskDeleteReasonAsPost() {
    String taskDeleteReason = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON;

    Map<String, Object> params = new HashMap<>();
    params.put("taskDeleteReason", taskDeleteReason);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDeleteReason(taskDeleteReason);
  }

  @Test
  public void testQueryByTaskDeleteReasonLike() {
    String taskDeleteReasonLike = "aTaskDeleteReasonLike";

    given()
      .queryParam("taskDeleteReasonLike", taskDeleteReasonLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDeleteReasonLike(taskDeleteReasonLike);
  }

  @Test
  public void testQueryByTaskDeleteReasonLikeAsPost() {
    String taskDeleteReasonLike = "aTaskDeleteReasonLike";

    Map<String, Object> params = new HashMap<>();
    params.put("taskDeleteReasonLike", taskDeleteReasonLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDeleteReasonLike(taskDeleteReasonLike);
  }

  @Test
  public void testQueryByTaskAssignee() {
    String taskAssignee = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE;

    given()
      .queryParam("taskAssignee", taskAssignee)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssignee(taskAssignee);
  }

  @Test
  public void testQueryByTaskAssigneeAsPost() {
    String taskAssignee = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE;

    Map<String, Object> params = new HashMap<>();
    params.put("taskAssignee", taskAssignee);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssignee(taskAssignee);
  }

  @Test
  public void testQueryByTaskAssigneeLike() {
    String taskAssigneeLike = "aTaskAssigneeLike";

    given()
      .queryParam("taskAssigneeLike", taskAssigneeLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssigneeLike(taskAssigneeLike);
  }

  @Test
  public void testQueryByTaskAssigneeLikeAsPost() {
    String taskAssigneeLike = "aTaskAssigneeLike";

    Map<String, Object> params = new HashMap<>();
    params.put("taskAssigneeLike", taskAssigneeLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssigneeLike(taskAssigneeLike);
  }

  @Test
  public void testQueryByTaskOwner() {
    String taskOwner = MockProvider.EXAMPLE_HISTORIC_TASK_INST_OWNER;

    given()
      .queryParam("taskOwner", taskOwner)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskOwner(taskOwner);
  }

  @Test
  public void testQueryByTaskOwnerAsPost() {
    String taskOwner = MockProvider.EXAMPLE_HISTORIC_TASK_INST_OWNER;

    Map<String, Object> params = new HashMap<>();
    params.put("taskOwner", taskOwner);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskOwner(taskOwner);
  }

  @Test
  public void testQueryByTaskOwnerLike() {
    String taskOwnerLike = "aTaskOwnerLike";

    given()
      .queryParam("taskOwnerLike", taskOwnerLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskOwnerLike(taskOwnerLike);
  }

  @Test
  public void testQueryByTaskOwnerLikeAsPost() {
    String taskOwnerLike = "aTaskOwnerLike";

    Map<String, Object> params = new HashMap<>();
    params.put("taskOwnerLike", taskOwnerLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskOwnerLike(taskOwnerLike);
  }

  @Test
  public void testQueryByTaskPriority() {
    int taskPriority = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PRIORITY;

    given()
      .queryParam("taskPriority", taskPriority)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskPriority(taskPriority);
  }

  @Test
  public void testQueryByTaskPriorityAsPost() {
    int taskPriority = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PRIORITY;

    Map<String, Object> params = new HashMap<>();
    params.put("taskPriority", taskPriority);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskPriority(taskPriority);
  }

  @Test
  public void testQueryByAssigned() {
    given()
      .queryParam("assigned", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssigned();
  }

  @Test
  public void testQueryByAssignedAsPost() {
    Map<String, Object> params = new HashMap<>();
    params.put("assigned", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssigned();
  }

  @Test
  public void testQueryByWithCandidateGroups() {
    given()
      .queryParam("withCandidateGroups", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withCandidateGroups();
  }

  @Test
  public void testQueryByWithCandidateGroupsAsPost() {
    Map<String, Object> params = new HashMap<>();
    params.put("withCandidateGroups", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withCandidateGroups();
  }

  @Test
  public void testQueryByWithoutCandidateGroups() {
    given()
      .queryParam("withoutCandidateGroups", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withoutCandidateGroups();
  }

  @Test
  public void testQueryByWithoutCandidateGroupsAsPost() {
    Map<String, Object> params = new HashMap<>();
    params.put("withoutCandidateGroups", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withoutCandidateGroups();
  }

  @Test
  public void testQueryByUnassigned() {
    given()
      .queryParam("unassigned", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskUnassigned();
  }

  @Test
  public void testQueryByUnassignedAsPost() {
    Map<String, Object> params = new HashMap<>();
    params.put("unassigned", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskUnassigned();
  }

  @Test
  public void testQueryByFinished() {
    given()
      .queryParam("finished", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).finished();
  }

  @Test
  public void testQueryByFinishedAsPost() {
    Map<String, Object> params = new HashMap<>();
    params.put("finished", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).finished();
  }

  @Test
  public void testQueryByUnfinished() {
    given()
      .queryParam("unfinished", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).unfinished();
  }

  @Test
  public void testQueryByUnfinishedAsPost() {
    Map<String, Object> params = new HashMap<>();
    params.put("unfinished", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).unfinished();
  }

  @Test
  public void testQueryByProcessFinished() {
    given()
      .queryParam("processFinished", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processFinished();
  }

  @Test
  public void testQueryByProcessFinishedAsPost() {
    Map<String, Object> params = new HashMap<>();
    params.put("processFinished", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processFinished();
  }

  @Test
  public void testQueryByProcessUnfinished() {
    given()
      .queryParam("processUnfinished", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processUnfinished();
  }

  @Test
  public void testQueryByProcessUnfinishedAsPost() {
    Map<String, Object> params = new HashMap<>();
    params.put("processUnfinished", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processUnfinished();
  }

  @Test
  public void testQueryByTaskParentTaskId() {
    String taskParentTaskId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID;

    given()
      .queryParam("taskParentTaskId", taskParentTaskId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskParentTaskId(taskParentTaskId);
  }

  @Test
  public void testQueryByTaskParentTaskIdAsPost() {
    String taskParentTaskId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID;

    Map<String, Object> params = new HashMap<>();
    params.put("taskParentTaskId", taskParentTaskId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskParentTaskId(taskParentTaskId);
  }

  @Test
  public void testQueryByTaskDueDate() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    given()
      .queryParam("taskDueDate", due)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueDate(DateTimeUtil.parseDate(due));
  }

  @Test
  public void testQueryByTaskDueDateAsPost() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    Map<String, Object> params = new HashMap<>();
    params.put("taskDueDate", DateTimeUtil.parseDate(due));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueDate(DateTimeUtil.parseDate(due));
  }

  @Test
  public void testQueryByTaskDueDateBefore() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    given()
      .queryParam("taskDueDateBefore", due)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueBefore(DateTimeUtil.parseDate(due));
  }

  @Test
  public void testQueryByTaskDueDateBeforeAsPost() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    Map<String, Object> params = new HashMap<>();
    params.put("taskDueDateBefore", DateTimeUtil.parseDate(due));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueBefore(DateTimeUtil.parseDate(due));
  }

  @Test
  public void testQueryByTaskDueDateAfter() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    given()
      .queryParam("taskDueDateAfter", due)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueAfter(DateTimeUtil.parseDate(due));
  }

  @Test
  public void testQueryByTaskDueDateAfterAsPost() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    Map<String, Object> params = new HashMap<>();
    params.put("taskDueDateAfter", DateTimeUtil.parseDate(due));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueAfter(DateTimeUtil.parseDate(due));
  }

  @Test
  public void testQueryWithoutTaskDueDateQueryParameter() {
    // given
    mockedQuery = setUpMockHistoricTaskInstanceQuery(Collections.singletonList(
        MockProvider.createMockHistoricTaskInstance(MockProvider.EXAMPLE_TENANT_ID, null)));

    // when
    Response response = given()
          .queryParam("withoutTaskDueDate", true)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    // then
    verify(mockedQuery).withoutTaskDueDate();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTaskDueDate = from(content).getString("[0].due");
    assertThat(returnedTaskDueDate).isEqualTo(null);
  }

  @Test
  public void testQueryWithoutTaskDueDatePostParameter() {
    // given
    mockedQuery = setUpMockHistoricTaskInstanceQuery(Collections.singletonList(
        MockProvider.createMockHistoricTaskInstance(MockProvider.EXAMPLE_TENANT_ID, null)));
    Map<String, Object> queryParameters = Collections.singletonMap("withoutTaskDueDate", (Object) true);

    // when
    Response response = given()
          .contentType(POST_JSON_CONTENT_TYPE)
          .body(queryParameters)
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    // then
    verify(mockedQuery).withoutTaskDueDate();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTaskDueDate = from(content).getString("[0].due");
    assertThat(returnedTaskDueDate).isEqualTo(null);
  }

  @Test
  public void testQueryByTaskFollowUpDate() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    given()
    .queryParam("taskFollowUpDate", followUp)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpDate(DateTimeUtil.parseDate(followUp));
  }

  @Test
  public void testQueryByTaskFollowUpDateAsPost() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    Map<String, Object> params = new HashMap<>();
    params.put("taskFollowUpDate", DateTimeUtil.parseDate(followUp));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpDate(DateTimeUtil.parseDate(followUp));
  }

  @Test
  public void testQueryByTaskFollowUpDateBefore() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    given()
      .queryParam("taskFollowUpDateBefore", followUp)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpBefore(DateTimeUtil.parseDate(followUp));
  }

  @Test
  public void testQueryByTaskFollowUpDateBeforeAsPost() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    Map<String, Object> params = new HashMap<>();
    params.put("taskFollowUpDateBefore", DateTimeUtil.parseDate(followUp));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpBefore(DateTimeUtil.parseDate(followUp));
  }

  @Test
  public void testQueryByTaskFollowUpDateAfter() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    given()
      .queryParam("taskFollowUpDateAfter", followUp)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpAfter(DateTimeUtil.parseDate(followUp));
  }

  @Test
  public void testQueryByTaskFollowUpDateAfterAsPost() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    Map<String, Object> params = new HashMap<>();
    params.put("taskFollowUpDateAfter", DateTimeUtil.parseDate(followUp));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpAfter(DateTimeUtil.parseDate(followUp));
  }

    @Test
    public void testQueryByStartedBefore() {
      String startedBefore = MockProvider.EXAMPLE_HISTORIC_TASK_INST_START_TIME;

      given()
        .queryParam("startedBefore", startedBefore)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

      verify(mockedQuery).startedBefore(DateTimeUtil.parseDate(startedBefore));
    }

  @Test
  public void testQueryByStartedBeforeWithInvalidParameter() {
    String startedBefore = "\"pizza\"";

    given()
      .queryParam("startedBefore", startedBefore)
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON).body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body(
        "message",
        containsString("Cannot convert value " + startedBefore))
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

  }

    @Test
    public void testQueryByStartedBeforeAsPost() {
      String startedBefore = MockProvider.EXAMPLE_HISTORIC_TASK_INST_START_TIME;

      Map<String, Object> params = new HashMap<>();
      params.put("startedBefore", DateTimeUtil.parseDate(startedBefore));

      given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(params)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

      verify(mockedQuery).startedBefore(DateTimeUtil.parseDate(startedBefore));
    }


    @Test
    public void testQueryByStartedAfter() {
      String startedAfter = MockProvider.EXAMPLE_HISTORIC_TASK_INST_START_TIME;

      given()
        .queryParam("startedAfter", startedAfter)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

      verify(mockedQuery).startedAfter(DateTimeUtil.parseDate(startedAfter));
    }

    @Test
    public void testQueryByStartedAfterAsPost() {
      String startedAfter = MockProvider.EXAMPLE_HISTORIC_TASK_INST_START_TIME;

      Map<String, Object> params = new HashMap<>();
      params.put("startedAfter", DateTimeUtil.parseDate(startedAfter));

      given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(params)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

      verify(mockedQuery).startedAfter(DateTimeUtil.parseDate(startedAfter));
    }


    @Test
    public void testQueryByFinishedBefore() {
      String finishedBefore = MockProvider.EXAMPLE_HISTORIC_TASK_INST_END_TIME;

      given()
        .queryParam("finishedBefore", finishedBefore)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

      verify(mockedQuery).finishedBefore(DateTimeUtil.parseDate(finishedBefore));
    }

    @Test
    public void testQueryByFinishedBeforeAsPost() {
      String finishedBefore = MockProvider.EXAMPLE_HISTORIC_TASK_INST_END_TIME;

      Map<String, Object> params = new HashMap<>();
      params.put("finishedBefore", DateTimeUtil.parseDate(finishedBefore));

      given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(params)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

      verify(mockedQuery).finishedBefore(DateTimeUtil.parseDate(finishedBefore));
    }


    @Test
    public void testQueryByFinishedAfter() {
      String finishedAfter = MockProvider.EXAMPLE_HISTORIC_TASK_INST_END_TIME;

      given()
        .queryParam("finishedAfter", finishedAfter)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

      verify(mockedQuery).finishedAfter(DateTimeUtil.parseDate(finishedAfter));
    }

    @Test
    public void testQueryByFinishedAfterAsPost() {
      String finishedAfter = MockProvider.EXAMPLE_HISTORIC_TASK_INST_END_TIME;

      Map<String, Object> params = new HashMap<>();
      params.put("finishedAfter", DateTimeUtil.parseDate(finishedAfter));

      given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(params)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

      verify(mockedQuery).finishedAfter(DateTimeUtil.parseDate(finishedAfter));
    }

  @Test
  public void testQueryByTaskVariable() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_eq_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("taskVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByTaskVariableValueIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_eq_" + variableValue;

    String queryValue = variableParameter;

    given()
    .queryParam("taskVariables", queryValue)
    .queryParam("variableValuesIgnoreCase", true)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).taskVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByTaskVariableNameIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_eq_" + variableValue;

    String queryValue = variableParameter;

    given()
    .queryParam("taskVariables", queryValue)
    .queryParam("variableNamesIgnoreCase", true)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).taskVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByTaskVariableNameValueIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_eq_" + variableValue;

    String queryValue = variableParameter;

    given()
    .queryParam("taskVariables", queryValue)
    .queryParam("variableNamesIgnoreCase", true)
    .queryParam("variableValuesIgnoreCase", true)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).taskVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByTaskVariableAsPost() {
    String variableName = "varName";
    String variableValue = "varValue";

    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> params = new HashMap<>();
    params.put("taskVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByInvalidTaskVariable() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_" + invalidComparator + "_" + variableValue;

    given()
      .queryParam("taskVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    // invalid format
    queryValue = "invalidFormattedVariableQuery";

    given()
      .queryParam("taskVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("variable query parameter has to have format KEY_OPERATOR_VALUE"))
      .when()
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testQueryByInvalidTaskVariableAsPost() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";

    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", variableName);
    variableJson.put("operator", invalidComparator);
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> params = new HashMap<>();
    params.put("taskVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when()
        .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testQueryByProcessVariableEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_eq_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByProcessVariableGreaterThan() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_gt_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueGreaterThan(variableName, variableValue);
  }

  @Test
  public void testQueryByProcessVariableGreaterThanEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_gteq_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueGreaterThanOrEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByProcessVariableLessThan() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_lt_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueLessThan(variableName, variableValue);
  }

  @Test
  public void testQueryByProcessVariableLessThanEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_lteq_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueLessThanOrEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByProcessVariableLike() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_like_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueLike(variableName, variableValue);
  }

  @Test
  public void testQueryByProcessVariableNotLike() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_notLike_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueNotLike(variableName, variableValue);
  }

  @Test
  public void testQueryByProcessVariableNotEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_neq_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueNotEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByProcessVariableAsPost() {
    String variableName = "varName";
    String variableValue = "varValue";

    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> params = new HashMap<>();
    params.put("processVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByInvalidProcessVariable() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_" + invalidComparator + "_" + variableValue;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Invalid process variable comparator specified: " + invalidComparator))
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    // invalid format
    queryValue = "invalidFormattedVariableQuery";

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("variable query parameter has to have format KEY_OPERATOR_VALUE"))
      .when()
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testQueryByInvalidProcessVariableAsPost() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";

    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", variableName);
    variableJson.put("operator", invalidComparator);
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> params = new HashMap<>();
    params.put("processVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Invalid process variable comparator specified: " + invalidComparator))
      .when()
        .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testQueryByCaseDefinitionId() {
    String caseDefinitionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_ID;

    given()
      .queryParam("caseDefinitionId", caseDefinitionId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseDefinitionId(caseDefinitionId);
  }

  @Test
  public void testQueryByCaseDefinitionIdAsPost() {
    String caseDefinitionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_ID;

    Map<String, Object> params = new HashMap<>();
    params.put("caseDefinitionId", caseDefinitionId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseDefinitionId(caseDefinitionId);
  }

  @Test
  public void testQueryByCaseDefinitionKey() {
    String caseDefinitionKey = "aCaseDefKey";

    given()
      .queryParam("caseDefinitionKey", caseDefinitionKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseDefinitionKey(caseDefinitionKey);
  }

  @Test
  public void testQueryByCaseDefinitionKeyAsPost() {
    String caseDefinitionKey = "aCaseDefKey";

    Map<String, Object> params = new HashMap<>();
    params.put("caseDefinitionKey", caseDefinitionKey);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseDefinitionKey(caseDefinitionKey);
  }

  @Test
  public void testQueryByCaseDefinitionName() {
    String caseDefinitionName = "aCaseDefName";

    given()
      .queryParam("caseDefinitionName", caseDefinitionName)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseDefinitionName(caseDefinitionName);
  }

  @Test
  public void testQueryByCaseDefinitionNameAsPost() {
    String caseDefinitionName = "aCaseDefName";

    Map<String, Object> params = new HashMap<>();
    params.put("caseDefinitionName", caseDefinitionName);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseDefinitionName(caseDefinitionName);
  }

  @Test
  public void testQueryByCaseInstanceId() {
    String caseInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_INST_ID;

    given()
      .queryParam("caseInstanceId", caseInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseInstanceId(caseInstanceId);
  }

  @Test
  public void testQueryByCaseInstanceIdAsPost() {
    String caseInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_INST_ID;

    Map<String, Object> params = new HashMap<>();
    params.put("caseInstanceId", caseInstanceId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseInstanceId(caseInstanceId);
  }

  @Test
  public void testQueryByCaseExecutionId() {
    String caseExecutionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_EXEC_ID;

    given()
      .queryParam("caseExecutionId", caseExecutionId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseExecutionId(caseExecutionId);
  }

  @Test
  public void testQueryByCaseExecutionIdAsPost() {
    String caseExecutionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_EXEC_ID;

    Map<String, Object> params = new HashMap<>();
    params.put("caseExecutionId", caseExecutionId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseExecutionId(caseExecutionId);
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockHistoricTaskInstanceQuery(createMockHistoricTaskInstancesTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> executions = from(content).getList("");
    assertThat(executions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testTenantIdListPostParameter() {
    mockedQuery = setUpMockHistoricTaskInstanceQuery(createMockHistoricTaskInstancesTwoTenants());

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> executions = from(content).getList("");
    assertThat(executions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testQueryWithoutTenantIdQueryParameter() {
    // given
    mockedQuery = setUpMockHistoricTaskInstanceQuery(Collections.singletonList(MockProvider.createMockHistoricTaskInstance(null)));

    // when
    Response response = given()
          .queryParam("withoutTenantId", true)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    // then
    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId).isEqualTo(null);
  }

  @Test
  public void testQueryWithoutTenantIdPostParameter() {
    // given
    mockedQuery = setUpMockHistoricTaskInstanceQuery(Collections.singletonList(MockProvider.createMockHistoricTaskInstance(null)));
    Map<String, Object> queryParameters = Collections.singletonMap("withoutTenantId", (Object) true);

    // when
    Response response = given()
          .contentType(POST_JSON_CONTENT_TYPE)
          .body(queryParameters)
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    // then
    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId).isEqualTo(null);
  }

  @Test
  public void testQueryTaskInvolvedUser() {
    String taskInvolvedUser = MockProvider.EXAMPLE_HISTORIC_TASK_INST_TASK_INVOLVED_USER;
    given()
      .queryParam("taskInvolvedUser", taskInvolvedUser)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskInvolvedUser(taskInvolvedUser);
  }

  @Test
  public void testQueryTaskInvolvedGroup() {
    String taskInvolvedGroup = MockProvider.EXAMPLE_HISTORIC_TASK_INST_TASK_INVOLVED_GROUP;
    given()
      .queryParam("taskInvolvedGroup", taskInvolvedGroup)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskInvolvedGroup(taskInvolvedGroup);
  }

  @Test
  public void testQueryTaskHadCandidateUser() {
    String taskHadCandidateUser = MockProvider.EXAMPLE_HISTORIC_TASK_INST_TASK_HAD_CANDIDATE_USER;
    given()
      .queryParam("taskHadCandidateUser", taskHadCandidateUser)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskHadCandidateUser(taskHadCandidateUser);
  }

  @Test
  public void testQueryTaskHadCandidateGroup() {
    String taskHadCandidateGroup = MockProvider.EXAMPLE_HISTORIC_TASK_INST_TASK_HAD_CANDIDATE_GROUP;
    given()
      .queryParam("taskHadCandidateGroup", taskHadCandidateGroup)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskHadCandidateGroup(taskHadCandidateGroup);
  }

  @Test
  public void testQueryByTaskDefinitionKeyIn() {

    String taskDefinitionKey1 = "aTaskDefinitionKey";
    String taskDefinitionKey2 = "anotherTaskDefinitionKey";

    given()
      .queryParam("taskDefinitionKeyIn", taskDefinitionKey1 + "," + taskDefinitionKey2)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDefinitionKeyIn(taskDefinitionKey1, taskDefinitionKey2);
    verify(mockedQuery).list();
  }

  @Test
  public void testQueryByTaskDefinitionKeyInAsPost() {

    String taskDefinitionKey1 = "aTaskDefinitionKey";
    String taskDefinitionKey2 = "anotherTaskDefinitionKey";

    List<String> taskDefinitionKeys = new ArrayList<>();
    taskDefinitionKeys.add(taskDefinitionKey1);
    taskDefinitionKeys.add(taskDefinitionKey2);

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("taskDefinitionKeyIn", taskDefinitionKeys);

    given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDefinitionKeyIn(taskDefinitionKey1, taskDefinitionKey2);
    verify(mockedQuery).list();
  }

  @Test
  public void testOrQuery() {
    // given
    HistoricTaskInstanceQueryImpl mockedQuery = mock(HistoricTaskInstanceQueryImpl.class);
    when(processEngine.getHistoryService().createHistoricTaskInstanceQuery()).thenReturn(mockedQuery);

    String payload = "{ \"orQueries\": [{" +
        "\"processDefinitionKey\": \"aKey\", " +
        "\"processInstanceBusinessKey\": \"aBusinessKey\"}] }";

    // when
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header(ACCEPT_JSON_HEADER)
      .body(payload)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    ArgumentCaptor<HistoricTaskInstanceQueryImpl> argument =
        ArgumentCaptor.forClass(HistoricTaskInstanceQueryImpl.class);

    verify(mockedQuery).addOrQuery(argument.capture());

    // then
    assertThat(argument.getValue().getProcessDefinitionKey()).isEqualTo("aKey");
    assertThat(argument.getValue().getProcessInstanceBusinessKey()).isEqualTo("aBusinessKey");
  }

  private List<HistoricTaskInstance> createMockHistoricTaskInstancesTwoTenants() {
    return Arrays.asList(
        MockProvider.createMockHistoricTaskInstance(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockHistoricTaskInstance(MockProvider.ANOTHER_EXAMPLE_TENANT_ID));
  }

  protected void verifyHistoricTaskInstanceEntries(String content) {
    String returnedId = from(content).getString("[0].id");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedActivityInstanceId = from(content).getString("[0].activityInstanceId");
    String returnedName = from(content).getString("[0].name");
    String returnedDescription = from(content).getString("[0].description");
    String returnedDeleteReason = from(content).getString("[0].deleteReason");
    String returnedOwner = from(content).getString("[0].owner");
    String returnedAssignee = from(content).getString("[0].assignee");
    Date returnedStartTime = DateTimeUtil.parseDate(from(content).getString("[0].startTime"));
    Date returnedEndTime = DateTimeUtil.parseDate(from(content).getString("[0].endTime"));
    Long returnedDurationInMillis = from(content).getLong("[0].duration");
    String returnedTaskDefinitionKey = from(content).getString("[0].taskDefinitionKey");
    int returnedPriority = from(content).getInt("[0].priority");
    String returnedParentTaskId = from(content).getString("[0].parentTaskId");
    Date returnedDue = DateTimeUtil.parseDate(from(content).getString("[0].due"));
    Date returnedFollow = DateTimeUtil.parseDate(from(content).getString("[0].followUp"));
    String returnedCaseDefinitionKey = from(content).getString("[0].caseDefinitionKey");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedCaseInstanceId = from(content).getString("[0].caseInstanceId");
    String returnedCaseExecutionId = from(content).getString("[0].caseExecutionId");
    String returnedTenantId = from(content).getString("[0].tenantId");
    Date returnedRemovalTime = DateTimeUtil.parseDate(from(content).getString("[0].removalTime"));
    String returnedRootProcessInstanceId = from(content).getString("[0].rootProcessInstanceId");

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ID, returnedId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID, returnedActivityInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_EXEC_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_KEY, returnedProcessDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_NAME, returnedName);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION, returnedDescription);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON, returnedDeleteReason);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_OWNER, returnedOwner);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE, returnedAssignee);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_TASK_INST_START_TIME), returnedStartTime);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_TASK_INST_END_TIME), returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DEF_KEY, returnedTaskDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PRIORITY, returnedPriority);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE), returnedDue);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE), returnedFollow);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID, returnedParentTaskId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_KEY, returnedCaseDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_DEF_ID, returnedCaseDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_INST_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_CASE_EXEC_ID, returnedCaseExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_TASK_INST_REMOVAL_TIME), returnedRemovalTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ROOT_PROC_INST_ID, returnedRootProcessInstanceId);
  }

}
