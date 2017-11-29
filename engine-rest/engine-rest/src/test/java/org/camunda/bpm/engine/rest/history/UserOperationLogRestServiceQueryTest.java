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

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_TASK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CLAIM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.history.UserOperationLogEntryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * @author Danny Gräf
 */
public class UserOperationLogRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String USER_OPERATION_LOG_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/user-operation";

  protected static final String USER_OPERATION_LOG_COUNT_RESOURCE_URL = USER_OPERATION_LOG_RESOURCE_URL + "/count";

  protected UserOperationLogQuery queryMock;

  @Before
  public void setUpMock() {
    List<UserOperationLogEntry> entries = MockProvider.createUserOperationLogEntries();
    queryMock = mock(UserOperationLogQuery.class);
    when(queryMock.list()).thenReturn(entries);
    when(queryMock.listPage(anyInt(), anyInt())).thenReturn(entries);
    when(queryMock.count()).thenReturn((long) entries.size());
    when(processEngine.getHistoryService().createUserOperationLogQuery()).thenReturn(queryMock);
  }

  @Test
  public void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode())
        .body("count", equalTo(1))
        .when().get(USER_OPERATION_LOG_COUNT_RESOURCE_URL);

    verify(queryMock).count();
  }

  @Test
  public void testEmptyQuery() {
    expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);

    verify(queryMock, never()).deploymentId(anyString());
    verify(queryMock, never()).processDefinitionId(anyString());
    verify(queryMock, never()).processDefinitionKey(anyString());
    verify(queryMock, never()).processInstanceId(anyString());
    verify(queryMock, never()).executionId(anyString());
    verify(queryMock, never()).caseDefinitionId(anyString());
    verify(queryMock, never()).caseInstanceId(anyString());
    verify(queryMock, never()).caseExecutionId(anyString());
    verify(queryMock, never()).taskId(anyString());
    verify(queryMock, never()).jobId(anyString());
    verify(queryMock, never()).jobDefinitionId(anyString());
    verify(queryMock, never()).batchId(anyString());
    verify(queryMock, never()).userId(anyString());
    verify(queryMock, never()).operationId(anyString());
    verify(queryMock, never()).operationType(anyString());
    verify(queryMock, never()).entityType(anyString());
    verify(queryMock, never()).property(anyString());
    verify(queryMock, never()).afterTimestamp(any(Date.class));
    verify(queryMock, never()).beforeTimestamp(any(Date.class));
    verify(queryMock, never()).orderByTimestamp();
    verify(queryMock, never()).asc();
    verify(queryMock, never()).desc();
    verify(queryMock).list();
  }

  @Test
  public void testQueryParameter() {
    Response response = given()
        .queryParam("deploymentId", "a-deployment-id")
        .queryParam("processDefinitionId", "1")
        .queryParam("processDefinitionKey", "6")
        .queryParam("processInstanceId", "2")
        .queryParam("executionId", "3")
        .queryParam("caseDefinitionId", "x")
        .queryParam("caseInstanceId", "y")
        .queryParam("caseExecutionId", "z")
        .queryParam("taskId", "4")
        .queryParam("jobId", "7")
        .queryParam("jobDefinitionId", "8")
        .queryParam("batchId", MockProvider.EXAMPLE_BATCH_ID)
        .queryParam("userId", "icke")
        .queryParam("operationId", "5")
        .queryParam("operationType", OPERATION_TYPE_CLAIM)
        .queryParam("entityType", EntityTypes.TASK)
        .queryParam("entityTypeIn", EntityTypes.TASK + "," + EntityTypes.VARIABLE)
        .queryParam("property", "owner")
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);

    verify(queryMock).deploymentId("a-deployment-id");
    verify(queryMock).processDefinitionId("1");
    verify(queryMock).processDefinitionKey("6");
    verify(queryMock).processInstanceId("2");
    verify(queryMock).executionId("3");
    verify(queryMock).caseDefinitionId("x");
    verify(queryMock).caseInstanceId("y");
    verify(queryMock).caseExecutionId("z");
    verify(queryMock).taskId("4");
    verify(queryMock).jobId("7");
    verify(queryMock).jobDefinitionId("8");
    verify(queryMock).batchId(MockProvider.EXAMPLE_BATCH_ID);
    verify(queryMock).userId("icke");
    verify(queryMock).operationId("5");
    verify(queryMock).operationType(OPERATION_TYPE_CLAIM);
    verify(queryMock).entityType(EntityTypes.TASK);
    verify(queryMock).entityTypeIn(EntityTypes.TASK, EntityTypes.VARIABLE);
    verify(queryMock).property("owner");
    verify(queryMock).list();

    String json = response.asString();
    UserOperationLogEntryDto actual = from(json).getObject("[0]", UserOperationLogEntryDto.class);
    assertEquals(MockProvider.EXAMPLE_USER_OPERATION_LOG_ID, actual.getId());
    assertEquals(MockProvider.EXAMPLE_DEPLOYMENT_ID, actual.getDeploymentId());
    assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, actual.getProcessDefinitionId());
    assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, actual.getProcessDefinitionKey());
    assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, actual.getProcessInstanceId());
    assertEquals(MockProvider.EXAMPLE_EXECUTION_ID, actual.getExecutionId());
    assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, actual.getCaseDefinitionId());
    assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, actual.getCaseInstanceId());
    assertEquals(MockProvider.EXAMPLE_CASE_EXECUTION_ID, actual.getCaseExecutionId());
    assertEquals(MockProvider.EXAMPLE_TASK_ID, actual.getTaskId());
    assertEquals(MockProvider.EXAMPLE_JOB_ID, actual.getJobId());
    assertEquals(MockProvider.EXAMPLE_JOB_DEFINITION_ID, actual.getJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_BATCH_ID, actual.getBatchId());
    assertEquals(MockProvider.EXAMPLE_USER_ID, actual.getUserId());
    assertEquals(MockProvider.EXAMPLE_USER_OPERATION_TIMESTAMP, from(json).getString("[0].timestamp"));
    assertEquals(MockProvider.EXAMPLE_USER_OPERATION_ID, actual.getOperationId());
    assertEquals(MockProvider.EXAMPLE_USER_OPERATION_TYPE, actual.getOperationType());
    assertEquals(MockProvider.EXAMPLE_USER_OPERATION_ENTITY, actual.getEntityType());
    assertEquals(MockProvider.EXAMPLE_USER_OPERATION_PROPERTY, actual.getProperty());
    assertEquals(MockProvider.EXAMPLE_USER_OPERATION_ORG_VALUE, actual.getOrgValue());
    assertEquals(MockProvider.EXAMPLE_USER_OPERATION_NEW_VALUE, actual.getNewValue());
  }

  @Test
  public void testQueryAfterTimestamp() {
    given().queryParam("afterTimestamp", MockProvider.EXAMPLE_USER_OPERATION_TIMESTAMP)
        .expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);
    verify(queryMock).afterTimestamp(DateTimeUtil.parseDate(MockProvider.EXAMPLE_USER_OPERATION_TIMESTAMP));
  }

  @Test
  public void testQueryBeforeTimestamp() {
    given().queryParam("beforeTimestamp", MockProvider.EXAMPLE_USER_OPERATION_TIMESTAMP)
        .expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);
    verify(queryMock).beforeTimestamp(DateTimeUtil.parseDate(MockProvider.EXAMPLE_USER_OPERATION_TIMESTAMP));
  }

  @Test
  public void testSortByTimestampAscending() {
    given()
        .queryParam("sortBy", "timestamp")
        .queryParam("sortOrder", "asc")
        .expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);
    verify(queryMock).orderByTimestamp();
    verify(queryMock).asc();
    verify(queryMock, never()).desc();
  }

  @Test
  public void testSortByTimestampDescending() {
    given()
        .queryParam("sortBy", "timestamp")
        .queryParam("sortOrder", "desc")
        .expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);
    verify(queryMock).orderByTimestamp();
    verify(queryMock).desc();
    verify(queryMock, never()).asc();
  }

  @Test
  public void testInvalidSortByParameter() {
    given()
        .queryParam("sortBy", "unknownField")
        .queryParam("sortOrder", "desc")
        .expect().statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);
  }

  @Test
  public void testPagination() {
    given()
        .queryParam("firstResult", 7)
        .queryParam("maxResults", 13)
        .expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);
    verify(queryMock).listPage(7, 13);
  }

  @Test
  public void testFirstResultMissing() {
    given().queryParam("maxResults", 13)
        .expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);
    verify(queryMock).listPage(0, 13);
  }

  @Test
  public void testMaxResultsMissing() {
    given().queryParam("firstResult", 7)
        .expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);
    verify(queryMock).listPage(7, Integer.MAX_VALUE);
  }

}
