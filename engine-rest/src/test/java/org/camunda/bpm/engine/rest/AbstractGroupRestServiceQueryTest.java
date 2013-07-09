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

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public abstract class AbstractGroupRestServiceQueryTest extends AbstractRestServiceTest {
  
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
    given().queryParam("sortBy", "groupName")
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
    
    Response response = given().queryParam("groupName", queryName)
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
    
    Assert.assertEquals(MockProvider.EXAMPLE_GROUP_NAME, returendName);
    
  }
  
  @Test
  public void testCompleteGetParameters() {
    
    Map<String, String> queryParameters = getCompleteStringQueryParameters();
    queryParameters.put("groupMember", MockProvider.EXAMPLE_USER_ID);
    
    RequestSpecification requestSpecification = given().contentType(POST_JSON_CONTENT_TYPE);
    for (Entry<String, String> paramEntry : queryParameters.entrySet()) {
      requestSpecification.parameter(paramEntry.getKey(), paramEntry.getValue());
    }
    
    requestSpecification.expect().statusCode(Status.OK.getStatusCode())
      .when().get(GROUP_QUERY_URL);
    
    verify(mockQuery).groupName(MockProvider.EXAMPLE_GROUP_NAME);
    verify(mockQuery).groupMember(MockProvider.EXAMPLE_USER_ID);
    
    verify(mockQuery).list();
    
  }

  private Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    
    parameters.put("groupName", MockProvider.EXAMPLE_GROUP_NAME);
  
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

  
}
