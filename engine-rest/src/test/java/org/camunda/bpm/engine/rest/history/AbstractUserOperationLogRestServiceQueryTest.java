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
import static org.mockito.Mockito.*;

import javax.ws.rs.core.Response.Status;
import javax.xml.registry.InvalidRequestException;
import java.util.Date;
import java.util.List;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.history.UserOperationLogEntryDto;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Danny Gräf
 */
public abstract class AbstractUserOperationLogRestServiceQueryTest extends AbstractRestServiceTest {

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

    verify(queryMock, never()).processDefinitionId(anyString());
    verify(queryMock, never()).processInstanceId(anyString());
    verify(queryMock, never()).executionId(anyString());
    verify(queryMock, never()).taskId(anyString());
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
        .queryParam("processDefinitionId", "1")
        .queryParam("processInstanceId", "2")
        .queryParam("executionId", "3")
        .queryParam("taskId", "4")
        .queryParam("userId", "icke")
        .queryParam("operationId", "5")
        .queryParam("operationType", OPERATION_TYPE_CLAIM)
        .queryParam("entityType", ENTITY_TYPE_TASK)
        .queryParam("property", "owner")
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);

    verify(queryMock).processDefinitionId("1");
    verify(queryMock).processInstanceId("2");
    verify(queryMock).executionId("3");
    verify(queryMock).taskId("4");
    verify(queryMock).userId("icke");
    verify(queryMock).operationId("5");
    verify(queryMock).operationType(OPERATION_TYPE_CLAIM);
    verify(queryMock).entityType(ENTITY_TYPE_TASK);
    verify(queryMock).property("owner");
    verify(queryMock).list();

    String json = response.asString();
    UserOperationLogEntryDto actual = from(json).getObject("[0]", UserOperationLogEntryDto.class);
    assertEquals(MockProvider.EXAMPLE_USER_OPERATION_LOG_ID, actual.getId());
    assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, actual.getProcessDefinitionId());
    assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, actual.getProcessInstanceId());
    assertEquals(MockProvider.EXAMPLE_EXECUTION_ID, actual.getExecutionId());
    assertEquals(MockProvider.EXAMPLE_TASK_ID, actual.getTaskId());
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
    verify(queryMock).afterTimestamp(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_USER_OPERATION_TIMESTAMP).toDate());
  }

  @Test
  public void testQueryBeforeTimestamp() {
    given().queryParam("beforeTimestamp", MockProvider.EXAMPLE_USER_OPERATION_TIMESTAMP)
        .expect().statusCode(Status.OK.getStatusCode())
        .when().get(USER_OPERATION_LOG_RESOURCE_URL);
    verify(queryMock).beforeTimestamp(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_USER_OPERATION_TIMESTAMP).toDate());
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
