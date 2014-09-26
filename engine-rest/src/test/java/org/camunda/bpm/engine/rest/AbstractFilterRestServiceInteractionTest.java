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
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.FILTER;
import static org.camunda.bpm.engine.rest.helper.TaskQueryMatcher.hasName;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
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
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.task.TaskQuery;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
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

  protected FilterService filterServiceMock;
  protected Filter filterMock;

  protected AuthorizationService authorizationServiceMock;
  protected IdentityService identityServiceMock;

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
    when(filterServiceMock.getFilter(eq(MockProvider.EXAMPLE_FILTER_ID))).thenReturn(filterMock);
    when(filterServiceMock.getFilter(eq(MockProvider.NON_EXISTING_ID))).thenReturn(null);

    List mockTasks = Collections.singletonList(new TaskEntity());

    when(filterServiceMock.singleResult(eq(MockProvider.EXAMPLE_FILTER_ID)))
      .thenReturn(mockTasks.get(0));
    when(filterServiceMock.singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), any(Query.class)))
      .thenReturn(mockTasks.get(0));
    when(filterServiceMock.list(eq(MockProvider.EXAMPLE_FILTER_ID)))
      .thenReturn(mockTasks);
    when(filterServiceMock.list(eq(MockProvider.EXAMPLE_FILTER_ID), any(Query.class)))
      .thenReturn(mockTasks);
    when(filterServiceMock.listPage(eq(MockProvider.EXAMPLE_FILTER_ID), anyInt(), anyInt()))
      .thenReturn(mockTasks);
    when(filterServiceMock.listPage(eq(MockProvider.EXAMPLE_FILTER_ID), any(Query.class), anyInt(), anyInt()))
      .thenReturn(mockTasks);
    when(filterServiceMock.count(eq(MockProvider.EXAMPLE_FILTER_ID)))
      .thenReturn((long) 1);
    when(filterServiceMock.count(eq(MockProvider.EXAMPLE_FILTER_ID), any(Query.class)))
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
  }

  @Test
  public void testGetFilter() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_FILTER_ID))
    .when()
      .get(SINGLE_FILTER_URL);

    verify(filterServiceMock).getFilter(eq(MockProvider.EXAMPLE_FILTER_ID));
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
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(dto)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SINGLE_FILTER_URL);

    verify(filterServiceMock).getFilter(eq(MockProvider.EXAMPLE_FILTER_ID));
    verify(filterServiceMock).saveFilter(eq(filterMock));
  }

  @Test
  public void testInvalidResourceType() {
    FilterDto dto = FilterDto.fromFilter(MockProvider.createMockFilter());
    dto.setResourceType("invalid");

    given()
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(dto)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(JsonMappingException.class.getSimpleName()))
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
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
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
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_FILTER_URL);

    verify(filterServiceMock).deleteFilter(eq(MockProvider.EXAMPLE_FILTER_ID));
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
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testEmptySingleResult() {
    when(filterServiceMock.singleResult(anyString(), any(Query.class))).thenReturn(null);

    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testInvalidSingleResult() {
    doThrow(new ProcessEngineException("More than one result found"))
      .when(filterServiceMock).singleResult(anyString(), any(Query.class));

    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteHalSingleResult() {
    given()
      .header("Accept", Hal.MEDIA_TYPE_HAL)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID),isNull(Query.class));
  }

  @Test
  public void testEmptyHalSingleResult() {
    when(filterServiceMock.singleResult(anyString(), any(Query.class))).thenReturn(null);

    given()
      .header("Accept", Hal.MEDIA_TYPE_HAL)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_links.size()", equalTo(0))
      .body("_embedded.size()", equalTo(0))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testInvalidHalSingleResult() {
    doThrow(new ProcessEngineException("More than one result found"))
      .when(filterServiceMock).singleResult(anyString(), any(Query.class));

    given()
      .header("Accept", Hal.MEDIA_TYPE_HAL)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteSingleResultOfNonExistingFilter() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
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
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteHalSingleResultAsPost() {
    given()
      .header("Accept", Hal.MEDIA_TYPE_HAL)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteSingleResultInvalidWithExtendingQuery() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
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
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID),
        argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)));
  }

  @Test
  public void testExecuteList() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testEmptyList() {
    when(filterServiceMock.list(anyString(), any(Query.class))).thenReturn(Collections.emptyList());

    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(0))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteHalList() {
    given()
      .header("Accept", Hal.MEDIA_TYPE_HAL)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testEmptyHalList() {
    when(filterServiceMock.list(anyString(), any(Query.class))).thenReturn(Collections.emptyList());

    given()
      .header("Accept", Hal.MEDIA_TYPE_HAL)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_links.size()", equalTo(0))
      .body("_embedded.size()", equalTo(0))
      .body("count", equalTo(0))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteListOfNonExistingFilter() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
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
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class), eq(1), eq(2));
  }

  @Test
  public void testExecuteListAsPost() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(MockProvider.EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteHalListAsPost() {
    given()
      .header("Accept", Hal.MEDIA_TYPE_HAL)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(MockProvider.EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteListAsPostWithPagination() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class), eq(1), eq(2));
  }

  @Test
  public void testExecuteListWithExtendingQuery() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(eq(MockProvider.EXAMPLE_FILTER_ID), argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)));
  }

  @Test
  public void testExecuteListWithExtendingQueryWithPagination() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(eq(MockProvider.EXAMPLE_FILTER_ID), argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)), eq(1), eq(2));
  }

  @Test
  public void testExecuteListWithInvalidExtendingQuery() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
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
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(eq(MockProvider.EXAMPLE_FILTER_ID), isNull(Query.class));
  }

  @Test
  public void testExecuteCountOfNonExistingFilter() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
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
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(eq(MockProvider.EXAMPLE_FILTER_ID), any(Query.class));
  }

  @Test
  public void testExecuteCountWithExtendingQuery() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQueryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(eq(MockProvider.EXAMPLE_FILTER_ID), argThat(hasName(MockProvider.EXAMPLE_TASK_NAME)));
  }

  @Test
  public void testExecuteCountWithInvalidExtendingQuery() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
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
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL + "/" + MockProvider.EXAMPLE_FILTER_ID;

    // anonymity means the identityService returns a null authentication, so no need to mock here

    given()
        .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
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

    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, MockProvider.EXAMPLE_FILTER_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, MockProvider.EXAMPLE_FILTER_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, MockProvider.EXAMPLE_FILTER_ID)).thenReturn(false);

    given()
        .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links.size()", is(0))

    .when()
        .options(SINGLE_FILTER_URL);

    verify(identityServiceMock, times(3)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, MockProvider.EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, MockProvider.EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, MockProvider.EXAMPLE_FILTER_ID);

  }

  @Test
  public void testFilterResourceOptionsUpdateUnauthorized() {
    String fullFilterUrl = "http://localhost:" + PORT + FILTER_URL + "/" + MockProvider.EXAMPLE_FILTER_ID;

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, MockProvider.EXAMPLE_FILTER_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, MockProvider.EXAMPLE_FILTER_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, MockProvider.EXAMPLE_FILTER_ID)).thenReturn(false);

    given()
        .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
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
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, READ, FILTER, MockProvider.EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, FILTER, MockProvider.EXAMPLE_FILTER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, FILTER, MockProvider.EXAMPLE_FILTER_ID);

  }

}
