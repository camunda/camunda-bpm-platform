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
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.FILTER;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_FILTER_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.STRING_VARIABLE_INSTANCE_TYPE;
import static org.camunda.bpm.engine.rest.helper.MockProvider.mockFilter;
import static org.camunda.bpm.engine.rest.helper.MockProvider.mockVariableInstance;
import static org.camunda.bpm.engine.rest.helper.TaskQueryMatcher.hasName;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.MockTaskBuilder;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.response.Response;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractFilterRestServiceInteractionTest extends AbstractRestServiceTest {

  public static final String FILTER_URL = TEST_RESOURCE_ROOT_PATH + FilterRestService.PATH;
  public static final String SINGLE_FILTER_URL = FILTER_URL + "/{id}";
  public static final String CREATE_FILTER_URL = FILTER_URL + "/create";
  public static final String EXECUTE_SINGLE_RESULT_FILTER_URL = SINGLE_FILTER_URL + "/singleResult";
  public static final String EXECUTE_LIST_FILTER_URL = SINGLE_FILTER_URL + "/list";
  public static final String EXECUTE_COUNT_FILTER_URL = SINGLE_FILTER_URL + "/count";

  public static final TaskQuery extendingQuery = new TaskQueryImpl().taskName(MockProvider.EXAMPLE_TASK_NAME);
  public static final TaskQueryDto extendingQueryDto = TaskQueryDto.fromQuery(extendingQuery);
  public static final String invalidExtendingQuery = "abc";

  public static final String PROCESS_INSTANCE_A_ID = "processInstanceA";
  public static final String CASE_INSTANCE_A_ID = "caseInstanceA";
  public static final String EXECUTION_A_ID = "executionA";
  public static final String EXECUTION_B_ID = "executionB";
  public static final String CASE_EXECUTION_A_ID = "caseExecutionA";
  public static final String TASK_A_ID = "taskA";
  public static final String TASK_B_ID = "taskB";
  public static final String TASK_C_ID = "taskC";

  protected FilterService filterServiceMock;
  protected Filter filterMock;

  protected AuthorizationService authorizationServiceMock;
  protected IdentityService identityServiceMock;
  private VariableInstanceQuery variableInstanceQueryMock;

  @Before
  @SuppressWarnings("unchecked")
  public void setUpRuntimeData() {
    filterServiceMock = mock(FilterService.class);

    when(processEngine.getFilterService()).thenReturn(filterServiceMock);

    FilterQuery filterQuery = MockProvider.createMockFilterQuery();

    when(filterServiceMock.createFilterQuery()).thenReturn(filterQuery);

    filterMock = MockProvider.createMockFilter();

    when(filterServiceMock.newTaskFilter()).thenReturn(filterMock);
    when(filterServiceMock.saveFilter(eq(filterMock))).thenReturn(filterMock);
    when(filterServiceMock.getFilter(eq(EXAMPLE_FILTER_ID))).thenReturn(filterMock);
    when(filterServiceMock.getFilter(eq(MockProvider.NON_EXISTING_ID))).thenReturn(null);

    List mockTasks = Collections.singletonList(new TaskEntity());

    when(filterServiceMock.singleResult(eq(EXAMPLE_FILTER_ID)))
      .thenReturn(mockTasks.get(0));
    when(filterServiceMock.singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class)))
      .thenReturn(mockTasks.get(0));
    when(filterServiceMock.list(eq(EXAMPLE_FILTER_ID)))
      .thenReturn(mockTasks);
    when(filterServiceMock.list(eq(EXAMPLE_FILTER_ID), any(Query.class)))
      .thenReturn(mockTasks);
    when(filterServiceMock.listPage(eq(EXAMPLE_FILTER_ID), anyInt(), anyInt()))
      .thenReturn(mockTasks);
    when(filterServiceMock.listPage(eq(EXAMPLE_FILTER_ID), any(Query.class), anyInt(), anyInt()))
      .thenReturn(mockTasks);
    when(filterServiceMock.count(eq(EXAMPLE_FILTER_ID)))
      .thenReturn((long) 1);
    when(filterServiceMock.count(eq(EXAMPLE_FILTER_ID), any(Query.class)))
      .thenReturn((long) 1);

    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).singleResult(eq(MockProvider.NON_EXISTING_ID));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).singleResult(eq(MockProvider.NON_EXISTING_ID), any(Query.class));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).list(eq(MockProvider.NON_EXISTING_ID));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).list(eq(MockProvider.NON_EXISTING_ID), any(Query.class));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).listPage(eq(MockProvider.NON_EXISTING_ID), anyInt(), anyInt());
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).listPage(eq(MockProvider.NON_EXISTING_ID), any(Query.class), anyInt(), anyInt());
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).count(eq(MockProvider.NON_EXISTING_ID));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).count(eq(MockProvider.NON_EXISTING_ID), any(Query.class));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).deleteFilter(eq(MockProvider.NON_EXISTING_ID));

    authorizationServiceMock = mock(AuthorizationServiceImpl.class);
    identityServiceMock = mock(IdentityServiceImpl.class);

    when(processEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);

    TaskService taskService = processEngine.getTaskService();
    when(taskService.createTaskQuery()).thenReturn(new TaskQueryImpl());

    variableInstanceQueryMock = mock(VariableInstanceQuery.class);
    when(processEngine.getRuntimeService().createVariableInstanceQuery())
      .thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.variableScopeIdIn((String) anyVararg()))
      .thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.variableNameIn((String) anyVararg()))
      .thenReturn(variableInstanceQueryMock);
  }

  @Test
  public void testGetFilter() {
    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(EXAMPLE_FILTER_ID))
    .when()
      .get(SINGLE_FILTER_URL);

    verify(filterServiceMock).getFilter(eq(EXAMPLE_FILTER_ID));
  }

  @Test
  public void testGetNonExistingFilter() {
    given()
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(SINGLE_FILTER_URL);

    verify(filterServiceMock).getFilter(eq(MockProvider.NON_EXISTING_ID));
  }

  @Test
  public void testCreateFilter() {
    FilterDto dto = FilterDto.fromFilter(MockProvider.createMockFilter());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(dto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", notNullValue())
    .when()
      .post(CREATE_FILTER_URL);

    verify(filterServiceMock).newTaskFilter();
    verify(filterServiceMock).saveFilter(eq(filterMock));
  }

  @Test
  public void testCreateInvalidFilter() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(CREATE_FILTER_URL);
  }

  @Test
  public void testUpdateFilter() {
    FilterDto dto = FilterDto.fromFilter(MockProvider.createMockFilter());

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(dto)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SINGLE_FILTER_URL);

    verify(filterServiceMock).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(filterServiceMock).saveFilter(eq(filterMock));
  }

  @Test
  public void testInvalidResourceType() {
    FilterDto dto = FilterDto.fromFilter(MockProvider.createMockFilter());
    dto.setResourceType("invalid");

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(dto)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .put(SINGLE_FILTER_URL);
  }

  @Test
  public void testUpdateNonExistingFilter() {
    given()
      .pathParam("id", MockProvider.NON_EXISTING_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .put(SINGLE_FILTER_URL);
  }

  @Test
  public void testUpdateInvalidFilter() {
    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .put(SINGLE_FILTER_URL);
  }

  @Test
  public void testDeleteFilter() {
    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_FILTER_URL);

    verify(filterServiceMock).deleteFilter(eq(EXAMPLE_FILTER_ID));
  }

  @Test
  public void testDeleteNonExistingFilter() {
    given()
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .delete(SINGLE_FILTER_URL);
  }

  @Test
  public void testExecuteSingleResult() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testEmptySingleResult() {
    when(filterServiceMock.singleResult(anyString(), any(Query.class))).thenReturn(null);

    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testInvalidSingleResult() {
    doThrow(new ProcessEngineException("More than one result found"))
      .when(filterServiceMock).singleResult(anyString(), any(Query.class));

    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteHalSingleResult() {
    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID),isNull(Query.class));
  }

  @Test
  public void testEmptyHalSingleResult() {
    when(filterServiceMock.singleResult(anyString(), any(Query.class))).thenReturn(null);

    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_links.size()", equalTo(0))
      .body("_embedded.size()", equalTo(0))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testInvalidHalSingleResult() {
    doThrow(new ProcessEngineException("More than one result found"))
      .when(filterServiceMock).singleResult(anyString(), any(Query.class));

    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteSingleResultOfNonExistingFilter() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.NON_EXISTING_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteSingleResultAsPost() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteHalSingleResultAsPost() {
    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteSingleResultInvalidWithExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(invalidExtendingQuery)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);
  }

  @Test
  public void testExecuteSingleResultWithExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(EXAMPLE_FILTER_ID),
        argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)));
  }

  @Test
  public void testExecuteList() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testEmptyList() {
    when(filterServiceMock.list(anyString(), any(Query.class))).thenReturn(Collections.emptyList());

    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(0))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteHalList() {
    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testEmptyHalList() {
    when(filterServiceMock.list(anyString(), any(Query.class))).thenReturn(Collections.emptyList());

    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_links.size()", equalTo(0))
      .body("_embedded.size()", equalTo(0))
      .body("count", equalTo(0))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteListOfNonExistingFilter() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(MockProvider.NON_EXISTING_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteListWithPagination() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(eq(EXAMPLE_FILTER_ID), isNull(Query.class), eq(1), eq(2));
  }

  @Test
  public void testExecuteListAsPost() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteHalListAsPost() {
    given()
      .header(ACCEPT_HAL_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteListAsPostWithPagination() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(eq(EXAMPLE_FILTER_ID), isNull(Query.class), eq(1), eq(2));
  }

  @Test
  public void testExecuteListWithExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(EXAMPLE_FILTER_ID), argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)));
  }

  @Test
  public void testExecuteListWithExtendingQueryWithPagination() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(eq(EXAMPLE_FILTER_ID), argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)), eq(1), eq(2));
  }

  @Test
  public void testExecuteListWithInvalidExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(invalidExtendingQuery)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(EXECUTE_LIST_FILTER_URL);
  }

  @Test
  public void testExecuteCount() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(eq(EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteCountOfNonExistingFilter() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(MockProvider.NON_EXISTING_ID, null);
  }

  @Test
  public void testExecuteCountAsPost() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(eq(EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteCountWithExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(eq(EXAMPLE_FILTER_ID), argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)));
  }

  @Test
  public void testExecuteCountWithInvalidExtendingQuery() {
    given()
      .header(ACCEPT_JSON_HEADER)
      .pathParam("id", EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(invalidExtendingQuery)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(EXECUTE_COUNT_FILTER_URL);
  }


  @Test
  public void testAnonymousFilterOptions() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL;

    // anonymity means the identityService returns a null authentication, so no need to mock here

    given()
      .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(3))

        .body("links[0].href", equalTo(fullFilterUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("list"))

        .body("links[1].href", equalTo(fullFilterUrl + "/count"))
        .body("links[1].method", equalTo(HttpMethod.GET))
        .body("links[1].rel", equalTo("count"))

        .body("links[2].href", equalTo(fullFilterUrl + "/create"))
        .body("links[2].method", equalTo(HttpMethod.POST))
        .body("links[2].rel", equalTo("create"))

    .when()
        .options(FILTER_URL);

    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testRestrictedFilterOptions() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL;

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);

    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, ANY)).thenReturn(false);

    given()
      .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(2))

        .body("links[0].href", equalTo(fullFilterUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("list"))

        .body("links[1].href", equalTo(fullFilterUrl + "/count"))
        .body("links[1].method", equalTo(HttpMethod.GET))
        .body("links[1].rel", equalTo("count"))

    .when()
        .options(FILTER_URL);

    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testAnonymousFilterResourceOptions() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL + "/" + EXAMPLE_FILTER_ID;

    // anonymity means the identityService returns a null authentication, so no need to mock here

    given()
        .pathParam("id", EXAMPLE_FILTER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(9))

        .body("links[0].href", equalTo(fullFilterUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullFilterUrl + "/singleResult"))
        .body("links[1].method", equalTo(HttpMethod.GET))
        .body("links[1].rel", equalTo("singleResult"))

        .body("links[2].href", equalTo(fullFilterUrl + "/singleResult"))
        .body("links[2].method", equalTo(HttpMethod.POST))
        .body("links[2].rel", equalTo("singleResult"))

        .body("links[3].href", equalTo(fullFilterUrl + "/list"))
        .body("links[3].method", equalTo(HttpMethod.GET))
        .body("links[3].rel", equalTo("list"))

        .body("links[4].href", equalTo(fullFilterUrl + "/list"))
        .body("links[4].method", equalTo(HttpMethod.POST))
        .body("links[4].rel", equalTo("list"))

        .body("links[5].href", equalTo(fullFilterUrl + "/count"))
        .body("links[5].method", equalTo(HttpMethod.GET))
        .body("links[5].rel", equalTo("count"))

        .body("links[6].href", equalTo(fullFilterUrl + "/count"))
        .body("links[6].method", equalTo(HttpMethod.POST))
        .body("links[6].rel", equalTo("count"))

        .body("links[7].href", equalTo(fullFilterUrl))
        .body("links[7].method", equalTo(HttpMethod.DELETE))
        .body("links[7].rel", equalTo("delete"))

        .body("links[8].href", equalTo(fullFilterUrl))
        .body("links[8].method", equalTo(HttpMethod.PUT))
        .body("links[8].rel", equalTo("update"))

    .when()
        .options(SINGLE_FILTER_URL);

    verify(identityServiceMock, times(3)).getCurrentAuthentication();

  }

  @Test
  public void testFilterResourceOptionsUnauthorized() {
    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);

    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, EXAMPLE_FILTER_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, EXAMPLE_FILTER_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, EXAMPLE_FILTER_ID)).thenReturn(false);

    given()
        .pathParam("id", EXAMPLE_FILTER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(0))

    .when()
        .options(SINGLE_FILTER_URL);

    verify(identityServiceMock, times(3)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, EXAMPLE_FILTER_ID);

  }

  @Test
  public void testFilterResourceOptionsUpdateUnauthorized() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL + "/" + EXAMPLE_FILTER_ID;

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, EXAMPLE_FILTER_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, EXAMPLE_FILTER_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, EXAMPLE_FILTER_ID)).thenReturn(false);

    given()
        .pathParam("id", EXAMPLE_FILTER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(8))

        .body("links[0].href", equalTo(fullFilterUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullFilterUrl + "/singleResult"))
        .body("links[1].method", equalTo(HttpMethod.GET))
        .body("links[1].rel", equalTo("singleResult"))

        .body("links[2].href", equalTo(fullFilterUrl + "/singleResult"))
        .body("links[2].method", equalTo(HttpMethod.POST))
        .body("links[2].rel", equalTo("singleResult"))

        .body("links[3].href", equalTo(fullFilterUrl + "/list"))
        .body("links[3].method", equalTo(HttpMethod.GET))
        .body("links[3].rel", equalTo("list"))

        .body("links[4].href", equalTo(fullFilterUrl + "/list"))
        .body("links[4].method", equalTo(HttpMethod.POST))
        .body("links[4].rel", equalTo("list"))

        .body("links[5].href", equalTo(fullFilterUrl + "/count"))
        .body("links[5].method", equalTo(HttpMethod.GET))
        .body("links[5].rel", equalTo("count"))

        .body("links[6].href", equalTo(fullFilterUrl + "/count"))
        .body("links[6].method", equalTo(HttpMethod.POST))
        .body("links[6].rel", equalTo("count"))

        .body("links[7].href", equalTo(fullFilterUrl))
        .body("links[7].method", equalTo(HttpMethod.DELETE))
        .body("links[7].rel", equalTo("delete"))

    .when()
        .options(SINGLE_FILTER_URL);

    verify(identityServiceMock, times(3)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, EXAMPLE_FILTER_ID);

  }

  @Test
  public void testHalTaskQueryWithWrongFormatVariablesProperties() {
    // mock properties with variable name list in wrong format
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("variables", "foo");
    Filter filter =  mockFilter().properties(properties).build();
    when(filterServiceMock.getFilter(eq(EXAMPLE_FILTER_ID))).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, never()).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock, never()).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock, never()).list();
  }

  @Test
  public void testHalTaskQueryWithEmptyVariablesProperties() {
    // mock properties with variable name list in wrong format
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("variables", Collections.emptyList());
    Filter filter =  mockFilter().properties(properties).build();
    when(filterServiceMock.getFilter(eq(EXAMPLE_FILTER_ID))).thenReturn(filter);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_embedded", equalTo(null))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, never()).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock, never()).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock, never()).list();
  }

  @Test
  public void testHalTaskQueryWithVariableNotSetOnTask() {
    // mock filter with variable names set
    mockFilterWithVariableNames();

    // mock resulting task
    Task task = createTaskMock(TASK_A_ID, PROCESS_INSTANCE_A_ID, EXECUTION_A_ID, null, null);
    when(filterServiceMock.singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class))).thenReturn(task);

    // mock variable instances
    List<VariableInstance> variableInstances = Arrays.asList(
      createExecutionVariableInstanceMock("foo", "execution", EXECUTION_B_ID),
      createExecutionVariableInstanceMock("execution", "bar", EXECUTION_B_ID),
      createTaskVariableInstanceMock("foo", "task", TASK_B_ID),
      createTaskVariableInstanceMock("task", "bar", TASK_B_ID)
    );
    when(variableInstanceQueryMock.list()).thenReturn(variableInstances);

    given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_embedded.containsKey('variable')", is(true))
      .body("_embedded.variable.size", equalTo(0))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, times(1)).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableScopeIdIn(TASK_A_ID, EXECUTION_A_ID, PROCESS_INSTANCE_A_ID);
    verify(variableInstanceQueryMock, times(1)).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableNameIn("foo", "bar");
    verify(variableInstanceQueryMock, times(1)).list();
  }

  @Test
  public void testHalTaskQueryWithAdditionalVariables() {
    // mock filter with variable names set
    mockFilterWithVariableNames();

    // mock resulting task
    Task task = createTaskMock(TASK_A_ID, PROCESS_INSTANCE_A_ID, EXECUTION_A_ID, CASE_INSTANCE_A_ID, CASE_EXECUTION_A_ID);
    when(filterServiceMock.singleResult(eq(EXAMPLE_FILTER_ID), any(Query.class))).thenReturn(task);

    // mock variable instances
    List<VariableInstance> variableInstances = Arrays.asList(
      createProcessInstanceVariableInstanceMock("foo", "processInstance", PROCESS_INSTANCE_A_ID),
      createProcessInstanceVariableInstanceMock("processInstance", "bar", PROCESS_INSTANCE_A_ID),
      createExecutionVariableInstanceMock("foo", "execution", EXECUTION_A_ID),
      createExecutionVariableInstanceMock("execution", "bar", EXECUTION_A_ID),
      createTaskVariableInstanceMock("foo", "task", TASK_A_ID),
      createTaskVariableInstanceMock("task", "bar", TASK_A_ID),
      createCaseInstanceVariableInstanceMock("foo", "caseInstance", CASE_INSTANCE_A_ID),
      createCaseInstanceVariableInstanceMock("caseInstance", "bar", CASE_INSTANCE_A_ID),
      createCaseExecutionVariableInstanceMock("foo", "caseExecution", CASE_EXECUTION_A_ID),
      createCaseExecutionVariableInstanceMock("caseExecution", "bar", CASE_EXECUTION_A_ID)
    );
    when(variableInstanceQueryMock.list()).thenReturn(variableInstances);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_embedded.containsKey('variable')", is(true))
      .body("_embedded.variable.size", equalTo(6))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, times(1)).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableScopeIdIn(TASK_A_ID, EXECUTION_A_ID, PROCESS_INSTANCE_A_ID, CASE_EXECUTION_A_ID, CASE_INSTANCE_A_ID);
    verify(variableInstanceQueryMock, times(1)).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableNameIn("foo", "bar");
    verify(variableInstanceQueryMock, times(1)).list();

    String content = response.asString();
    List<Map<String, Object>> variables = from(content).getJsonObject("_embedded.variable");

    // task variable 'foo'
    verifyTaskVariableValue(variables.get(0), "foo", "task", TASK_A_ID);
    // task variable 'task'
    verifyTaskVariableValue(variables.get(1), "task", "bar", TASK_A_ID);
    // execution variable 'execution'
    verifyExecutionVariableValue(variables.get(2), "execution", "bar", EXECUTION_A_ID);
    // process instance variable 'processInstance'
    verifyProcessInstanceVariableValue(variables.get(3), "processInstance", "bar", PROCESS_INSTANCE_A_ID);
    // caseExecution variable 'caseExecution'
    verifyCaseExecutionVariableValue(variables.get(4), "caseExecution", "bar", CASE_EXECUTION_A_ID);
    // case instance variable 'caseInstance'
    verifyCaseInstanceVariableValue(variables.get(5), "caseInstance", "bar", CASE_INSTANCE_A_ID);
  }

  @Test
  public void testHalTaskListQueryWithAdditionalVariables() {
    // mock filter with variable names set
    mockFilterWithVariableNames();

    // mock resulting task
    List<Task> tasks = Arrays.asList(
      createTaskMock(TASK_A_ID, PROCESS_INSTANCE_A_ID, EXECUTION_A_ID, null, null),
      createTaskMock(TASK_B_ID, PROCESS_INSTANCE_A_ID, EXECUTION_B_ID, null, null),
      createTaskMock(TASK_C_ID, null, null, CASE_INSTANCE_A_ID, CASE_EXECUTION_A_ID)
    );
    when(filterServiceMock.list(eq(EXAMPLE_FILTER_ID), any(Query.class))).thenReturn(tasks);

    // mock variable instances
    List<VariableInstance> variableInstances = Arrays.asList(
      createProcessInstanceVariableInstanceMock("foo", PROCESS_INSTANCE_A_ID, PROCESS_INSTANCE_A_ID),
      createProcessInstanceVariableInstanceMock(PROCESS_INSTANCE_A_ID, "bar", PROCESS_INSTANCE_A_ID),
      createExecutionVariableInstanceMock("foo", EXECUTION_A_ID, EXECUTION_A_ID),
      createExecutionVariableInstanceMock(EXECUTION_A_ID, "bar", EXECUTION_A_ID),
      createExecutionVariableInstanceMock("foo", EXECUTION_B_ID, EXECUTION_B_ID),
      createExecutionVariableInstanceMock(EXECUTION_B_ID, "bar", EXECUTION_B_ID),
      createTaskVariableInstanceMock("foo", TASK_A_ID, TASK_A_ID),
      createTaskVariableInstanceMock(TASK_A_ID, "bar", TASK_A_ID),
      createTaskVariableInstanceMock("foo", TASK_B_ID, TASK_B_ID),
      createTaskVariableInstanceMock(TASK_B_ID, "bar", TASK_B_ID),
      createTaskVariableInstanceMock("foo", TASK_C_ID, TASK_C_ID),
      createTaskVariableInstanceMock(TASK_C_ID, "bar", TASK_C_ID),
      createCaseInstanceVariableInstanceMock("foo", CASE_INSTANCE_A_ID, CASE_INSTANCE_A_ID),
      createCaseInstanceVariableInstanceMock(CASE_INSTANCE_A_ID, "bar", CASE_INSTANCE_A_ID),
      createCaseExecutionVariableInstanceMock("foo", CASE_EXECUTION_A_ID, CASE_EXECUTION_A_ID),
      createCaseExecutionVariableInstanceMock(CASE_EXECUTION_A_ID, "bar", CASE_EXECUTION_A_ID)
    );
    when(variableInstanceQueryMock.list()).thenReturn(variableInstances);

    Response response = given()
      .pathParam("id", EXAMPLE_FILTER_ID)
      .header(ACCEPT_HAL_HEADER)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_embedded.task.size", equalTo(3))
      .body("_embedded.task.any { it._embedded.containsKey('variable') }", is(true))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock, times(1)).getFilter(eq(EXAMPLE_FILTER_ID));
    verify(variableInstanceQueryMock, times(1)).variableScopeIdIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableScopeIdIn(TASK_A_ID, EXECUTION_A_ID, PROCESS_INSTANCE_A_ID, TASK_B_ID, EXECUTION_B_ID, TASK_C_ID, CASE_EXECUTION_A_ID, CASE_INSTANCE_A_ID);
    verify(variableInstanceQueryMock, times(1)).variableNameIn((String) anyVararg());
    verify(variableInstanceQueryMock).variableNameIn("foo", "bar");
    verify(variableInstanceQueryMock, times(1)).list();

    String content = response.asString();
    List<Map<String, Object>> taskList = from(content).getList("_embedded.task");

    // task A
    List<Map<String, Object>> variables = getEmbeddedTaskVariables(taskList.get(0));
    assertThat(variables).hasSize(4);

    // task variable 'foo'
    verifyTaskVariableValue(variables.get(0), "foo", TASK_A_ID, TASK_A_ID);
    // task variable 'taskA'
    verifyTaskVariableValue(variables.get(1), TASK_A_ID, "bar", TASK_A_ID);
    // execution variable 'executionA'
    verifyExecutionVariableValue(variables.get(2), EXECUTION_A_ID, "bar", EXECUTION_A_ID);
    // process instance variable 'processInstanceA'
    verifyProcessInstanceVariableValue(variables.get(3), PROCESS_INSTANCE_A_ID, "bar", PROCESS_INSTANCE_A_ID);

    // task B
    variables = getEmbeddedTaskVariables(taskList.get(1));
    assertThat(variables).hasSize(4);

    // task variable 'foo'
    verifyTaskVariableValue(variables.get(0), "foo", TASK_B_ID, TASK_B_ID);
    // task variable 'taskA'
    verifyTaskVariableValue(variables.get(1), TASK_B_ID, "bar", TASK_B_ID);
    // execution variable 'executionA'
    verifyExecutionVariableValue(variables.get(2), EXECUTION_B_ID, "bar", EXECUTION_B_ID);
    // process instance variable 'processInstanceA'
    verifyProcessInstanceVariableValue(variables.get(3), PROCESS_INSTANCE_A_ID, "bar", PROCESS_INSTANCE_A_ID);

    // task C
    variables = getEmbeddedTaskVariables(taskList.get(2));
    assertThat(variables).hasSize(4);

    // task variable 'foo'
    verifyTaskVariableValue(variables.get(0), "foo", TASK_C_ID, TASK_C_ID);
    // task variable 'taskC'
    verifyTaskVariableValue(variables.get(1), TASK_C_ID, "bar", TASK_C_ID);
    // case execution variable 'caseExecutionA'
    verifyCaseExecutionVariableValue(variables.get(2), CASE_EXECUTION_A_ID, "bar", CASE_EXECUTION_A_ID);
    // case instance variable 'caseInstanceA'
    verifyCaseInstanceVariableValue(variables.get(3), CASE_INSTANCE_A_ID, "bar", CASE_INSTANCE_A_ID);

  }

  @SuppressWarnings("unchecked")
  protected List<Map<String, Object>> getEmbeddedTaskVariables(Map<String, Object> task) {
    Map<String, Object> embedded = (Map<String, Object>) task.get("_embedded");
    assertThat(embedded).isNotNull();
    return (List<Map<String, Object>>) embedded.get("variable");
  }

  protected Filter mockFilterWithVariableNames() {
    // mock properties with variable name list (names are ignored but variable names list must not be empty)
    List<Map<String, String>> variables = new ArrayList<Map<String, String>>();
    Map<String, String> foo = new HashMap<String, String>();
    foo.put("name", "foo");
    foo.put("label", "Foo");
    variables.add(foo);
    Map<String, String> bar = new HashMap<String, String>();
    bar.put("name", "bar");
    bar.put("label", "Bar");
    variables.add(bar);

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("variables", variables);

    Filter filter = mockFilter().properties(properties).build();
    when(filterServiceMock.getFilter(eq(EXAMPLE_FILTER_ID))).thenReturn(filter);

    return filter;
  }

  protected Task createTaskMock(String taskId, String processInstanceId, String executionId, String caseInstanceId, String caseExecutionId) {
    return new MockTaskBuilder()
      .id(taskId)
      .processInstanceId(processInstanceId)
      .executionId(executionId)
      .caseInstanceId(caseInstanceId)
      .caseExecutionId(caseExecutionId)
      .build();
  }

  protected VariableInstance createTaskVariableInstanceMock(String name, Object value, String taskId) {
    return createVariableInstanceMock(name, value, taskId, null, null, null, null);
  }

  protected VariableInstance createExecutionVariableInstanceMock(String name, Object value, String executionId) {
    return createVariableInstanceMock(name, value, null, executionId, null, null, null);
  }

  protected VariableInstance createProcessInstanceVariableInstanceMock(String name, Object value, String processInstanceId) {
    return createVariableInstanceMock(name, value, null, processInstanceId, processInstanceId, null, null);
  }

  protected VariableInstance createCaseExecutionVariableInstanceMock(String name, Object value, String caseExecutionId) {
    return createVariableInstanceMock(name, value, null, null, null, caseExecutionId, null);
  }

  protected VariableInstance createCaseInstanceVariableInstanceMock(String name, Object value, String caseInstanceId) {
    return createVariableInstanceMock(name, value, null, null, null, caseInstanceId, caseInstanceId);
  }

  protected VariableInstance createVariableInstanceMock(String name, Object value, String taskId, String executionId, String processInstanceId, String caseExecutionId, String caseInstanceId) {
    return mockVariableInstance().name(name).value(value).taskId(taskId)
      .executionId(executionId).processInstanceId(processInstanceId).caseExecutionId(caseExecutionId).caseInstanceId(caseInstanceId)
      .buildEntity();
  }

  protected void verifyTaskVariableValue(Map<String, Object> variable, String name, String value, String taskId) {
    verifyVariableValue(variable, name, value, TaskRestService.PATH, taskId, "localVariables");
  }

  protected void verifyExecutionVariableValue(Map<String, Object> variable, String name, String value, String executionId) {
    verifyVariableValue(variable, name, value, ExecutionRestService.PATH, executionId, "localVariables");
  }

  protected void verifyCaseExecutionVariableValue(Map<String, Object> variable, String name, String value, String caseExecutionId) {
    verifyVariableValue(variable, name, value, CaseExecutionRestService.PATH, caseExecutionId, "localVariables");
  }

  protected void verifyProcessInstanceVariableValue(Map<String, Object> variable, String name, String value, String processInstanceId) {
    verifyVariableValue(variable, name, value, ProcessInstanceRestService.PATH, processInstanceId, "variables");
  }

  protected void verifyCaseInstanceVariableValue(Map<String, Object> variable, String name, String value, String caseInstanceId) {
    verifyVariableValue(variable, name, value, CaseInstanceRestService.PATH, caseInstanceId, "variables");
  }

  @SuppressWarnings("unchecked")
  protected void verifyVariableValue(Map<String, Object> variable, String name, String value, String scopeResourcePath, String scopeId, String variablesName) {
    assertThat(variable.get("name")).isEqualTo(name);
    assertThat(variable.get("value")).isEqualTo(value);
    assertThat(variable.get("type")).isEqualTo(STRING_VARIABLE_INSTANCE_TYPE);
    assertThat(variable.get("variableType")).isEqualTo(STRING_VARIABLE_INSTANCE_TYPE);
    assertThat(variable.get("serializationConfig")).isNull();
    assertThat(variable.get("_embedded")).isNull();
    Map<String, Map<String, String>> links = (Map<String, Map<String, String>>) variable.get("_links");
    assertThat(links).hasSize(1);
    assertThat(links.get("self")).hasSize(1);
    assertThat(links.get("self").get("href")).isEqualTo(scopeResourcePath + "/" + scopeId + "/" + variablesName + "/" + name);
  }

}
