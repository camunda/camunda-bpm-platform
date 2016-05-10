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

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public class TenantRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/tenant";
  protected static final String COUNT_QUERY_URL = QUERY_URL + "/count";

  private TenantQuery mockQuery;

  @Before
  public void setUpRuntimeData() {
    List<Tenant> tenants = Collections.singletonList(MockProvider.createMockTenant());
    mockQuery = setUpMockQuery(tenants);
  }

  private TenantQuery setUpMockQuery(List<Tenant> tenants) {
    TenantQuery query = mock(TenantQuery.class);
    when(query.list()).thenReturn(tenants);
    when(query.count()).thenReturn((long) tenants.size());

    when(processEngine.getIdentityService().createTenantQuery()).thenReturn(query);

    return query;
  }

  @Test
  public void emptyQuery() {
    String queryKey = "";

    given().queryParam("name", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(QUERY_URL);
  }

  @Test
  public void noParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode()).when().get(QUERY_URL);

    verify(mockQuery).list();
    verifyNoMoreInteractions(mockQuery);
  }

  @Test
  public void tenantRetrieval() {
    String name = MockProvider.EXAMPLE_TENANT_NAME;

    Response response = given().queryParam("name", name)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(QUERY_URL);

    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).tenantName(name);
    inOrder.verify(mockQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    assertThat(instances.size(), is(1));

    String returnedId = from(content).getString("[0].id");
    String returnedName = from(content).getString("[0].name");

    assertThat(returnedId, is(MockProvider.EXAMPLE_TENANT_ID));
    assertThat(returnedName, is(MockProvider.EXAMPLE_TENANT_NAME));
  }

  @Test
  public void completeGetParameters() {
    Map<String, String> queryParameters = getCompleteStringQueryParameters();

    RequestSpecification requestSpecification = given().contentType(POST_JSON_CONTENT_TYPE);
    for (Entry<String, String> paramEntry : queryParameters.entrySet()) {
      requestSpecification.parameter(paramEntry.getKey(), paramEntry.getValue());
    }

    requestSpecification
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(QUERY_URL);

    verify(mockQuery).tenantId(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockQuery).tenantName(MockProvider.EXAMPLE_TENANT_NAME);
    verify(mockQuery).tenantNameLike("%" + MockProvider.EXAMPLE_TENANT_NAME + "%");
    verify(mockQuery).userMember(MockProvider.EXAMPLE_USER_ID);
    verify(mockQuery).groupMember(MockProvider.EXAMPLE_GROUP_ID);

    verify(mockQuery).list();
  }

  private Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("id", MockProvider.EXAMPLE_TENANT_ID);
    parameters.put("name", MockProvider.EXAMPLE_TENANT_NAME);
    parameters.put("nameLike", "%" + MockProvider.EXAMPLE_TENANT_NAME + "%");
    parameters.put("userMember", MockProvider.EXAMPLE_USER_ID);
    parameters.put("groupMember", MockProvider.EXAMPLE_GROUP_ID);

    return parameters;
  }

  @Test
  public void queryByUserIncludingGroups() {

    given()
      .queryParam("userMember", MockProvider.EXAMPLE_USER_ID)
      .queryParam("includingGroupsOfUser", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(QUERY_URL);

    verify(mockQuery).userMember(MockProvider.EXAMPLE_USER_ID);
    verify(mockQuery).includingGroupsOfUser(true);

    verify(mockQuery).list();
  }

  @Test
  public void queryCount() {
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(COUNT_QUERY_URL);

    verify(mockQuery).count();
  }

  @Test
  public void queryPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .get(QUERY_URL);

    verify(mockQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void sortByParameterOnly() {
    given().queryParam("sortBy", "name")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(QUERY_URL);
  }

  @Test
  public void sortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(QUERY_URL);
  }

  @Test
  public void sortById() {
    given()
      .queryParam("sortBy", "id")
      .queryParam("sortOrder", "asc")
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .get(QUERY_URL);

    InOrder inOrder = Mockito.inOrder(mockQuery);
    inOrder.verify(mockQuery).orderByTenantId();
    inOrder.verify(mockQuery).asc();
  }

  @Test
  public void sortByName() {
    given()
      .queryParam("sortBy", "name")
      .queryParam("sortOrder", "desc")
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .get(QUERY_URL);

    InOrder inOrder = Mockito.inOrder(mockQuery);
    inOrder.verify(mockQuery).orderByTenantName();
    inOrder.verify(mockQuery).desc();
  }

}
