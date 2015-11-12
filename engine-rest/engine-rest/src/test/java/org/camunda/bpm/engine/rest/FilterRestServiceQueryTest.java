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

import static javax.ws.rs.core.Response.Status;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.rest.dto.runtime.FilterQueryDto;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.response.Response;

/**
 * @author Sebastian Menski
 */
public class FilterRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();
  
  protected static final String FILTER_QUERY_URL = TEST_RESOURCE_ROOT_PATH + FilterRestService.PATH;
  protected static final String SINGLE_FILTER_URL = FILTER_QUERY_URL + "/{id}";
  protected static final String FILTER_COUNT_QUERY_URL = FILTER_QUERY_URL + "/count";

  protected FilterQuery mockedQuery;
  protected Filter mockedFilter;
  protected int mockedFilterItemCount;
  protected Filter anotherMockedFilter;
  protected int anotherMockedFilterItemCount;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = MockProvider.createMockFilterQuery();
    mockedFilter = MockProvider.createMockFilter(MockProvider.EXAMPLE_FILTER_ID);
    mockedFilterItemCount = 13;
    anotherMockedFilter = MockProvider.createMockFilter(MockProvider.ANOTHER_EXAMPLE_FILTER_ID);
    anotherMockedFilterItemCount = 42;

    FilterService filterService = processEngine.getFilterService();

    when(filterService.createFilterQuery()).thenReturn(mockedQuery);
    when(filterService.getFilter(eq(MockProvider.EXAMPLE_FILTER_ID))).thenReturn(mockedFilter);
    when(filterService.count(eq(MockProvider.EXAMPLE_FILTER_ID))).thenReturn((long) mockedFilterItemCount);
    when(filterService.getFilter(eq(MockProvider.ANOTHER_EXAMPLE_FILTER_ID))).thenReturn(anotherMockedFilter);
    when(filterService.count(eq(MockProvider.ANOTHER_EXAMPLE_FILTER_ID))).thenReturn((long) anotherMockedFilterItemCount);
  }

  @Test
  public void testEmptyQuery() {
    String queryFilterId = "";

    given()
      .queryParam("filterId", queryFilterId)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(FILTER_QUERY_URL);

    verify(mockedQuery).list();
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(FILTER_QUERY_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testCountyQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(FILTER_COUNT_QUERY_URL);

    verify(mockedQuery).count();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testFilterRetrieval() {
    Response response = given()
      .queryParam("filterId", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(FILTER_QUERY_URL);

    verifyFilterMock(response);
  }

  @Test
  public void testMultipleParameters() {
    given()
      .queryParams(getQueryParameters())
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(FILTER_QUERY_URL);

    verifyQueryMockMultipleParameters();
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("filterId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "filterId")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(FILTER_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(FILTER_QUERY_URL);
  }

  @Test
  public void testSuccessfulPagination() {
    executeAndVerifyPagination(0, 10, Status.OK);
  }

  @Test
  public void testMissingFirstResultParameter() {
    executeAndVerifyPagination(null, 10, Status.OK);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    executeAndVerifyPagination(0, null, Status.OK);
  }

  @Test
  public void testSingleFilterWithItemCount() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
      .queryParam("itemCount", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("containsKey('itemCount')", is(true))
      .body("itemCount", equalTo(mockedFilterItemCount))
    .when()
      .get(SINGLE_FILTER_URL);
  }

  @Test
  public void testSingleFilterWithoutItemCount() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_FILTER_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("containsKey('itemCount')", is(false))
    .when()
      .get(SINGLE_FILTER_URL);
  }

  @Test
  public void testFilterQueryWithItemCount() {
    given()
      .queryParam("itemCount", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size", equalTo(2))
      .body("any { it.containsKey('itemCount') }", is(true))
      .body("[0].itemCount", equalTo(mockedFilterItemCount))
      .body("[1].itemCount", equalTo(anotherMockedFilterItemCount))
    .when()
      .get(FILTER_QUERY_URL);
  }

  @Test
  public void testFilterQueryWithoutItemCount() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size", equalTo(2))
      .body("any { it.containsKey('itemCount') }", is(false))
    .when()
      .get(FILTER_QUERY_URL);
  }

  protected Map<String, String> getQueryParameters() {
    Map<String, String> params = new HashMap<String, String>();

    params.put("filterId", MockProvider.EXAMPLE_FILTER_ID);
    params.put("resourceType", MockProvider.EXAMPLE_FILTER_RESOURCE_TYPE);
    params.put("name", MockProvider.EXAMPLE_FILTER_NAME);
    params.put("nameLike", MockProvider.EXAMPLE_FILTER_NAME);
    params.put("owner", MockProvider.EXAMPLE_FILTER_OWNER);

    return params;
  }

  @SuppressWarnings("unchecked")
  protected void verifyFilterMock(Response response) {
    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).filterId(MockProvider.EXAMPLE_FILTER_ID);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, String>> filters = from(content).getList("");

    assertThat(filters).hasSize(2);

    assertThat(filters.get(0)).isNotNull();

    String returnedFilterId = from(content).getString("[0].id");
    String returnedResourceType = from(content).getString("[0].resourceType");
    String returnedName = from(content).getString("[0].name");
    String returnedOwner = from(content).getString("[0].owner");
    Map<String, Object> returnedQuery = from(content).getJsonObject("[0].query");
    Map<String, Object> returnedProperties = from(content).getJsonObject("[0].properties");

    Map<String, String> expectedVariable = new HashMap<String, String>();
    expectedVariable.put("name", "foo");
    expectedVariable.put("value", "bar");
    expectedVariable.put("operator", "eq");

    assertThat(returnedFilterId).isEqualTo(MockProvider.EXAMPLE_FILTER_ID);
    assertThat(returnedResourceType).isEqualTo(MockProvider.EXAMPLE_FILTER_RESOURCE_TYPE);
    assertThat(returnedName).isEqualTo(MockProvider.EXAMPLE_FILTER_NAME);
    assertThat(returnedOwner).isEqualTo(MockProvider.EXAMPLE_FILTER_OWNER);
    assertThat(returnedQuery.get("name")).isEqualTo(MockProvider.EXAMPLE_FILTER_QUERY_DTO.getName());
    assertThat((List<Map<String, String>>) returnedQuery.get("processVariables")).hasSize(1).containsExactly(expectedVariable);
    assertThat((List<Map<String, String>>) returnedQuery.get("taskVariables")).hasSize(1).containsExactly(expectedVariable);
    assertThat((List<Map<String, String>>) returnedQuery.get("caseInstanceVariables")).hasSize(1).containsExactly(expectedVariable);
    assertThat(returnedProperties).isEqualTo(MockProvider.EXAMPLE_FILTER_PROPERTIES);

    assertThat(filters.get(1)).isNotNull();

    returnedFilterId = from(content).getString("[1].id");
    returnedResourceType = from(content).getString("[1].resourceType");
    returnedName = from(content).getString("[1].name");
    returnedOwner = from(content).getString("[1].owner");
    returnedQuery = from(content).getJsonObject("[1].query");
    returnedProperties = from(content).getJsonObject("[1].properties");

    assertThat(returnedFilterId).isEqualTo(MockProvider.ANOTHER_EXAMPLE_FILTER_ID);
    assertThat(returnedResourceType).isEqualTo(MockProvider.EXAMPLE_FILTER_RESOURCE_TYPE);
    assertThat(returnedName).isEqualTo(MockProvider.EXAMPLE_FILTER_NAME);
    assertThat(returnedOwner).isEqualTo(MockProvider.EXAMPLE_FILTER_OWNER);
    assertThat(returnedQuery.get("name")).isEqualTo(MockProvider.EXAMPLE_FILTER_QUERY_DTO.getName());
    assertThat(returnedProperties).isEqualTo(MockProvider.EXAMPLE_FILTER_PROPERTIES);
  }

  protected void verifyQueryMockMultipleParameters() {
    verify(mockedQuery).filterId(MockProvider.EXAMPLE_FILTER_ID);
    verify(mockedQuery).filterResourceType(MockProvider.EXAMPLE_FILTER_RESOURCE_TYPE);
    verify(mockedQuery).filterName(MockProvider.EXAMPLE_FILTER_NAME);
    verify(mockedQuery).filterNameLike(MockProvider.EXAMPLE_FILTER_NAME);
    verify(mockedQuery).filterOwner(MockProvider.EXAMPLE_FILTER_OWNER);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("sortBy", sortBy);
    params.put("sortOrder", sortOrder);

    given()
      .queryParams(params)
    .then().expect()
      .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(FILTER_QUERY_URL);

    if (expectedStatus.equals(Status.OK)) {
      verifyQueryMockSorting(sortBy, sortOrder);
    }
  }

  protected void verifyQueryMockSorting(String sortBy, String sortOrder) {
    InOrder inOrder = inOrder(mockedQuery);
    if (sortBy.equals(FilterQueryDto.SORT_BY_ID_VALUE)) {
      inOrder.verify(mockedQuery).orderByFilterId();
    }
    else if (sortBy.equals(FilterQueryDto.SORT_BY_RESOURCE_TYPE_VALUE)) {
      inOrder.verify(mockedQuery).orderByFilterResourceType();
    }
    else if (sortBy.equals(FilterQueryDto.SORT_BY_NAME_VALUE)) {
      inOrder.verify(mockedQuery).orderByFilterName();
    }
    else if (sortBy.equals(FilterQueryDto.SORT_BY_OWNER_VALUE)) {
      inOrder.verify(mockedQuery).orderByFilterOwner();
    }

    if (sortOrder.equals(AbstractQuery.SORTORDER_ASC)) {
      inOrder.verify(mockedQuery).asc();
    }
    else if (sortOrder.equals(AbstractQuery.SORTORDER_DESC)) {
      inOrder.verify(mockedQuery).desc();
    }
  }

  protected void executeAndVerifyPagination(Integer firstResult, Integer maxResults, Status expectedStatus) {
    Map<String, String> params = new HashMap<String, String>();
    if (firstResult != null) {
      params.put("firstResult", firstResult.toString());
    }
    if (maxResults != null) {
      params.put("maxResults", maxResults.toString());
    }

    given()
      .queryParams(params)
    .then().expect()
      .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(FILTER_QUERY_URL);

    verifyQueryMockPagination(firstResult, maxResults);

  }

  protected void verifyQueryMockPagination(Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

}
