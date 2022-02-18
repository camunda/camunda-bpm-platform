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
package org.camunda.bpm.engine.rest;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.assertj.core.util.Arrays;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GroupRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String GROUP_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/group";
  protected static final String GROUP_COUNT_QUERY_URL = GROUP_QUERY_URL + "/count";

  private GroupQuery mockQuery;

  @Before
  public void setUpRuntimeData() {
    mockQuery = setUpMockGroupQuery(MockProvider.createMockGroups());
  }

  private GroupQuery setUpMockGroupQuery(List<Group> list) {
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(sampleGroupQuery.list()).thenReturn(list);
    when(sampleGroupQuery.count()).thenReturn((long) list.size());

    when(processEngine.getIdentityService().createGroupQuery()).thenReturn(sampleGroupQuery);

    return sampleGroupQuery;
  }

  @Test
  public void testEmptyQuery() {

    String queryKey = "";
    given().queryParam("name", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(GROUP_QUERY_URL);

  }

  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "name")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(GROUP_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(GROUP_QUERY_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode()).when().get(GROUP_QUERY_URL);

    verify(mockQuery).list();
    verifyNoMoreInteractions(mockQuery);
  }

  @Test
  public void testSimpleGroupQuery() {
    String queryName = MockProvider.EXAMPLE_GROUP_NAME;

    Response response = given().queryParam("name", queryName)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(GROUP_QUERY_URL);

    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).groupName(queryName);
    inOrder.verify(mockQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one group returned.", 1, instances.size());
    Assert.assertNotNull("The returned group should not be null.", instances.get(0));

    String returendName = from(content).getString("[0].name");
    String returendType = from(content).getString("[0].type");

    Assert.assertEquals(MockProvider.EXAMPLE_GROUP_NAME, returendName);
    Assert.assertEquals(MockProvider.EXAMPLE_GROUP_TYPE, returendType);

  }

  @Test
  public void testCompleteGetParameters() {

    Map<String, Object> queryParameters = getCompleteStringQueryParameters();

    RequestSpecification requestSpecification = given().contentType(POST_JSON_CONTENT_TYPE);
    for (Entry<String, Object> paramEntry : queryParameters.entrySet()) {
      requestSpecification.param(paramEntry.getKey(), paramEntry.getValue());
    }

    requestSpecification.expect().statusCode(Status.OK.getStatusCode())
      .when().get(GROUP_QUERY_URL);

    verify(mockQuery).groupName(MockProvider.EXAMPLE_GROUP_NAME);
    verify(mockQuery).groupNameLike("%" + MockProvider.EXAMPLE_GROUP_NAME + "%");
    verify(mockQuery).groupType(MockProvider.EXAMPLE_GROUP_TYPE);
    verify(mockQuery).groupMember(MockProvider.EXAMPLE_USER_ID);
    verify(mockQuery).memberOfTenant(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockQuery).groupId(MockProvider.EXAMPLE_GROUP_ID);
    verify(mockQuery).groupIdIn(MockProvider.EXAMPLE_GROUP_ID, MockProvider.EXAMPLE_GROUP_ID2);

    verify(mockQuery).list();

  }

  private Map<String, Object> getCompleteStringQueryParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("name", MockProvider.EXAMPLE_GROUP_NAME);
    parameters.put("nameLike", "%" + MockProvider.EXAMPLE_GROUP_NAME + "%");
    parameters.put("type", MockProvider.EXAMPLE_GROUP_TYPE);
    parameters.put("member", MockProvider.EXAMPLE_USER_ID);
    parameters.put("memberOfTenant", MockProvider.EXAMPLE_TENANT_ID);
    parameters.put("id", MockProvider.EXAMPLE_GROUP_ID);
    parameters.put("idIn", MockProvider.EXAMPLE_GROUP_ID + "," + MockProvider.EXAMPLE_GROUP_ID2);

    return parameters;
  }

  private Map<String, Object> getCompletePostParameters() {
    Map<String, Object> parameters = getCompleteStringQueryParameters();

    parameters.put("idIn", Arrays.array(MockProvider.EXAMPLE_GROUP_ID, MockProvider.EXAMPLE_GROUP_ID2));

    return parameters;
  }

  @Test
  public void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(GROUP_COUNT_QUERY_URL);

    verify(mockQuery).count();
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(GROUP_QUERY_URL);

    verify(mockQuery).listPage(firstResult, maxResults);
  }


  @Test
  public void testQueryCountForPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .header("accept", MediaType.APPLICATION_JSON)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(GROUP_COUNT_QUERY_URL);

    verify(mockQuery).count();
  }

  @Test
  public void testCompletePostParameters() {

    Map<String, Object> requestBody = getCompletePostParameters();

    given().contentType(POST_JSON_CONTENT_TYPE).body(requestBody)
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().post(GROUP_QUERY_URL);

    verify(mockQuery).groupName(MockProvider.EXAMPLE_GROUP_NAME);
    verify(mockQuery).groupNameLike("%" + MockProvider.EXAMPLE_GROUP_NAME + "%");
    verify(mockQuery).groupType(MockProvider.EXAMPLE_GROUP_TYPE);
    verify(mockQuery).groupMember(MockProvider.EXAMPLE_USER_ID);
    verify(mockQuery).memberOfTenant(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockQuery).groupId(MockProvider.EXAMPLE_GROUP_ID);
    verify(mockQuery).groupIdIn(MockProvider.EXAMPLE_GROUP_ID, MockProvider.EXAMPLE_GROUP_ID2);

    verify(mockQuery).list();
  }


  @Test
  public void testPaginationGet() {

    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(GROUP_QUERY_URL);

    verify(mockQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testPaginationPost() {

    int firstResult = 0;
    int maxResults = 10;
    given().contentType(POST_JSON_CONTENT_TYPE)
      .queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .body(EMPTY_JSON_OBJECT)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(GROUP_QUERY_URL);

    verify(mockQuery).listPage(firstResult, maxResults);
  }
}
