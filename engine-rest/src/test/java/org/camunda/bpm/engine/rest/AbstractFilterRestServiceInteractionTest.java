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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Before;
import org.junit.Test;

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

  public static final String extendingQuery = "{\"name\": \"" + MockProvider.EXAMPLE_TASK_NAME + "\"}";
  public static final String invalidExtendingQuery = "abc";

  protected FilterService filterServiceMock;
  protected Filter filterMock;

  @Before
  @SuppressWarnings("unchecked")
  public void setUpRuntimeData() {
    filterServiceMock = mock(FilterService.class);

    when(processEngine.getFilterService()).thenReturn(filterServiceMock);

    FilterQuery filterQuery = MockProvider.createMockFilterQuery();

    when(filterServiceMock.createFilterQuery()).thenReturn(filterQuery);

    filterMock = MockProvider.createMockFilter();

    when(filterServiceMock.newFilter()).thenReturn(filterMock);
    when(filterServiceMock.saveFilter(eq(filterMock))).thenReturn(filterMock);
    when(filterServiceMock.getFilter(eq(MockProvider.EXAMPLE_FILTER_ID))).thenReturn(filterMock);
    when(filterServiceMock.getFilter(eq(MockProvider.NON_EXISTING_ID))).thenReturn(null);

    List mockTasks = Collections.singletonList(new TaskEntity());

    when(filterServiceMock.singleResult(eq(MockProvider.EXAMPLE_FILTER_ID)))
      .thenReturn(mockTasks.get(0));
    when(filterServiceMock.singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), anyString()))
      .thenReturn(mockTasks.get(0));
    when(filterServiceMock.list(eq(MockProvider.EXAMPLE_FILTER_ID)))
      .thenReturn(mockTasks);
    when(filterServiceMock.list(eq(MockProvider.EXAMPLE_FILTER_ID), anyString()))
      .thenReturn(mockTasks);
    when(filterServiceMock.listPage(eq(MockProvider.EXAMPLE_FILTER_ID), anyInt(), anyInt()))
      .thenReturn(mockTasks);
    when(filterServiceMock.listPage(eq(MockProvider.EXAMPLE_FILTER_ID), anyString(), anyInt(), anyInt()))
      .thenReturn(mockTasks);
    when(filterServiceMock.count(eq(MockProvider.EXAMPLE_FILTER_ID))).thenReturn((long) 1);
    when(filterServiceMock.count(eq(MockProvider.EXAMPLE_FILTER_ID), anyString())).thenReturn((long) 1);

    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).deleteFilter(eq(MockProvider.NON_EXISTING_ID));
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).singleResult(eq(MockProvider.NON_EXISTING_ID), anyString());
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).list(eq(MockProvider.NON_EXISTING_ID), anyString());
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).listPage(eq(MockProvider.NON_EXISTING_ID), anyString(), anyInt(), anyInt());
    doThrow(new NullValueException("No filter found with given id"))
      .when(filterServiceMock).count(eq(MockProvider.NON_EXISTING_ID), anyString());

    doThrow(new NotValidException("Filter cannot be extended by an invalid query"))
      .when(filterServiceMock).singleResult(eq(MockProvider.EXAMPLE_FILTER_ID), eq(invalidExtendingQuery));
    doThrow(new NotValidException("Filter cannot be extended by an invalid query"))
      .when(filterServiceMock).list(eq(MockProvider.EXAMPLE_FILTER_ID), eq(invalidExtendingQuery));
    doThrow(new NotValidException("Filter cannot be extended by an invalid query"))
      .when(filterServiceMock).listPage(eq(MockProvider.EXAMPLE_FILTER_ID), eq(invalidExtendingQuery), anyInt(), anyInt());
    doThrow(new NotValidException("Filter cannot be extended by an invalid query"))
      .when(filterServiceMock).count(eq(MockProvider.EXAMPLE_FILTER_ID), eq(invalidExtendingQuery));
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

    verify(filterServiceMock).getFilter(MockProvider.EXAMPLE_FILTER_ID);
  }

  @Test
  public void testGetNonExistingFilter() {
    given()
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
    .when()
      .get(SINGLE_FILTER_URL);

    verify(filterServiceMock).getFilter(MockProvider.NON_EXISTING_ID);
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

    verify(filterServiceMock).newFilter();
    verify(filterServiceMock).saveFilter(filterMock);
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

    verify(filterServiceMock).getFilter(MockProvider.EXAMPLE_FILTER_ID);
    verify(filterServiceMock).saveFilter(filterMock);
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

    verify(filterServiceMock).deleteFilter(MockProvider.EXAMPLE_FILTER_ID);
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

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, (String) null);
  }

  @Test
  public void testEmptySingleResult() {
    when(filterServiceMock.singleResult(anyString(), anyString())).thenReturn(null);

    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, (String) null);
  }

  @Test
  public void testInvalidSingleResult() {
    doThrow(new ProcessEngineException("More than one result found"))
      .when(filterServiceMock).singleResult(anyString(), anyString());

    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, (String) null);
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

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, (String) null);
  }

  @Test
  public void testEmptyHalSingleResult() {
    when(filterServiceMock.singleResult(anyString(), anyString())).thenReturn(null);

    given()
      .header("Accept", Hal.MEDIA_TYPE_HAL)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("_links.size()", equalTo(0))
      .body("_embedded.size()", equalTo(0))
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, (String) null);
  }

  @Test
  public void testInvalidHalSingleResult() {
    doThrow(new ProcessEngineException("More than one result found"))
      .when(filterServiceMock).singleResult(anyString(), anyString());

    given()
      .header("Accept", Hal.MEDIA_TYPE_HAL)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, (String) null);
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

    verify(filterServiceMock).singleResult(MockProvider.NON_EXISTING_ID, (String) null);
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

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, EMPTY_JSON_OBJECT);
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

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, EMPTY_JSON_OBJECT);
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

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, invalidExtendingQuery);
  }

  @Test
  public void testExecuteSingleResultWithExtendingQuery() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQuery)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(EXECUTE_SINGLE_RESULT_FILTER_URL);

    verify(filterServiceMock).singleResult(MockProvider.EXAMPLE_FILTER_ID, extendingQuery);
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

    verify(filterServiceMock).list(MockProvider.EXAMPLE_FILTER_ID, (String) null);
  }

  @Test
  public void testEmptyList() {
    when(filterServiceMock.list(anyString(), anyString())).thenReturn(Collections.emptyList());

    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(0))
    .when()
      .get(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(MockProvider.EXAMPLE_FILTER_ID, (String) null);
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

    verify(filterServiceMock).list(MockProvider.EXAMPLE_FILTER_ID, (String) null);
  }

  @Test
  public void testEmptyHalList() {
    when(filterServiceMock.list(anyString(), anyString())).thenReturn(Collections.emptyList());

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

    verify(filterServiceMock).list(MockProvider.EXAMPLE_FILTER_ID, (String) null);
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

    verify(filterServiceMock).list(MockProvider.NON_EXISTING_ID, (String) null);
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

    verify(filterServiceMock).listPage(MockProvider.EXAMPLE_FILTER_ID, (String) null, 1, 2);
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

    verify(filterServiceMock).list(MockProvider.EXAMPLE_FILTER_ID, EMPTY_JSON_OBJECT);
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

    verify(filterServiceMock).list(MockProvider.EXAMPLE_FILTER_ID, EMPTY_JSON_OBJECT);
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

    verify(filterServiceMock).listPage(MockProvider.EXAMPLE_FILTER_ID, EMPTY_JSON_OBJECT, 1, 2);
  }

  @Test
  public void testExecuteListWithExtendingQuery() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQuery)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).list(MockProvider.EXAMPLE_FILTER_ID, extendingQuery);
  }

  @Test
  public void testExecuteListWithExtendingQueryWithPagination() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .queryParams("firstResult", 1)
      .queryParams("maxResults", 2)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQuery)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", equalTo(1))
    .when()
      .post(EXECUTE_LIST_FILTER_URL);

    verify(filterServiceMock).listPage(MockProvider.EXAMPLE_FILTER_ID, extendingQuery, 1, 2);
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

    verify(filterServiceMock).list(MockProvider.EXAMPLE_FILTER_ID, invalidExtendingQuery);
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

    verify(filterServiceMock).count(MockProvider.EXAMPLE_FILTER_ID, (String) null);
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

    verify(filterServiceMock).count(MockProvider.NON_EXISTING_ID, (String) null);
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

    verify(filterServiceMock).count(MockProvider.EXAMPLE_FILTER_ID, EMPTY_JSON_OBJECT);
  }

  @Test
  public void testExecuteCountWithExtendingQuery() {
    given()
      .header("Accept", MediaType.APPLICATION_JSON)
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(extendingQuery)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .post(EXECUTE_COUNT_FILTER_URL);

    verify(filterServiceMock).count(MockProvider.EXAMPLE_FILTER_ID, extendingQuery);
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

    verify(filterServiceMock).count(MockProvider.EXAMPLE_FILTER_ID, invalidExtendingQuery);
  }

}
