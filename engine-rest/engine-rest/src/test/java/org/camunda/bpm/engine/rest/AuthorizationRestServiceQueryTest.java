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
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String SERVICE_PATH = TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH;
  protected static final String SERVICE_COUNT_PATH = TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH+"/count";

  protected AuthorizationService authorizationServiceMock;
  protected IdentityService identityServiceMock;

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  @Before
  public void setUpRuntimeData() {
    authorizationServiceMock = mock(AuthorizationServiceImpl.class);
    identityServiceMock = mock(IdentityServiceImpl.class);

    when(processEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
  }

  private AuthorizationQuery setUpMockQuery(List<Authorization> list) {
    AuthorizationQuery query = mock(AuthorizationQuery.class);
    when(query.list()).thenReturn(list);
    when(query.count()).thenReturn((long) list.size());

    when(processEngine.getAuthorizationService().createAuthorizationQuery()).thenReturn(query);

    return query;
  }

  @Test
  public void testEmptyQuery() {

    setUpMockQuery(MockProvider.createMockAuthorizations());

    String queryKey = "";
    given().queryParam("name", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(SERVICE_PATH);

  }

  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "resourceType")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(SERVICE_PATH);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(SERVICE_PATH);
  }

  @Test
  public void testNoParametersQuery() {

    AuthorizationQuery mockQuery = setUpMockQuery(MockProvider.createMockAuthorizations());

    expect().statusCode(Status.OK.getStatusCode()).when().get(SERVICE_PATH);

    verify(mockQuery).list();
    verifyNoMoreInteractions(mockQuery);
  }

  @Test
  public void testSimpleAuthorizationQuery() {

    List<Authorization> mockAuthorizations = MockProvider.createMockGlobalAuthorizations();
    AuthorizationQuery mockQuery = setUpMockQuery(mockAuthorizations);

    Response response = given().queryParam("type", Authorization.AUTH_TYPE_GLOBAL)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(SERVICE_PATH);

    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).authorizationType(Authorization.AUTH_TYPE_GLOBAL);
    inOrder.verify(mockQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one authorization returned.", 1, instances.size());
    Assert.assertNotNull("The returned authorization should not be null.", instances.get(0));

    Authorization mockAuthorization = mockAuthorizations.get(0);

    Assert.assertEquals(mockAuthorization.getId(), from(content).getString("[0].id"));
    Assert.assertEquals(mockAuthorization.getAuthorizationType(), from(content).getInt("[0].type"));
    Assert.assertEquals(Permissions.READ.getName(), from(content).getString("[0].permissions[0]"));
    Assert.assertEquals(Permissions.UPDATE.getName(), from(content).getString("[0].permissions[1]"));
    Assert.assertEquals(mockAuthorization.getUserId(), from(content).getString("[0].userId"));
    Assert.assertEquals(mockAuthorization.getGroupId(), from(content).getString("[0].groupId"));
    Assert.assertEquals(mockAuthorization.getResourceType(), from(content).getInt("[0].resourceType"));
    Assert.assertEquals(mockAuthorization.getResourceId(), from(content).getString("[0].resourceId"));

  }

  @Test
  public void testCompleteGetParameters() {

    List<Authorization> mockAuthorizations = MockProvider.createMockGlobalAuthorizations();
    AuthorizationQuery mockQuery = setUpMockQuery(mockAuthorizations);

    Map<String, String> queryParameters = getCompleteStringQueryParameters();

    RequestSpecification requestSpecification = given().contentType(POST_JSON_CONTENT_TYPE);
    for (Entry<String, String> paramEntry : queryParameters.entrySet()) {
      requestSpecification.parameter(paramEntry.getKey(), paramEntry.getValue());
    }

    requestSpecification.expect().statusCode(Status.OK.getStatusCode())
      .when().get(SERVICE_PATH);

    verify(mockQuery).authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID);
    verify(mockQuery).authorizationType(MockProvider.EXAMPLE_AUTHORIZATION_TYPE);
    verify(mockQuery).userIdIn(new String[]{MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_ID2});
    verify(mockQuery).groupIdIn(new String[]{MockProvider.EXAMPLE_GROUP_ID, MockProvider.EXAMPLE_GROUP_ID2});
    verify(mockQuery).resourceType(MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    verify(mockQuery).resourceId(MockProvider.EXAMPLE_RESOURCE_ID);

    verify(mockQuery).list();

  }


  private Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("id", MockProvider.EXAMPLE_AUTHORIZATION_ID);
    parameters.put("type", MockProvider.EXAMPLE_AUTHORIZATION_TYPE_STRING);
    parameters.put("userIdIn", MockProvider.EXAMPLE_USER_ID + ","+MockProvider.EXAMPLE_USER_ID2);
    parameters.put("groupIdIn", MockProvider.EXAMPLE_GROUP_ID+","+MockProvider.EXAMPLE_GROUP_ID2);
    parameters.put("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID_STRING);
    parameters.put("resourceId", MockProvider.EXAMPLE_RESOURCE_ID);

    return parameters;
  }

  @Test
  public void testQueryCount() {

    AuthorizationQuery mockQuery = setUpMockQuery(MockProvider.createMockAuthorizations());

    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(3))
      .when().get(SERVICE_COUNT_PATH);

    verify(mockQuery).count();
  }

  @Test
  public void testSuccessfulPagination() {

    AuthorizationQuery mockQuery = setUpMockQuery(MockProvider.createMockAuthorizations());

    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(SERVICE_PATH);

    verify(mockQuery).listPage(firstResult, maxResults);
  }


}
