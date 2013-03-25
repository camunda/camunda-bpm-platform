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
package org.camunda.bpm.engine.rest.embedded;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.AbstractProcessDefinitionRestServiceQueryTest;
import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.camunda.bpm.engine.rest.helper.MockDefinitionBuilder;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.ResteasyServerBootstrap;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public class ProcessDefinitionRestServiceQueryTest extends AbstractProcessDefinitionRestServiceQueryTest {
  
  private static ResteasyServerBootstrap resteasyBootstrap;
  
  private ProcessDefinitionQuery mockedQuery;

  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    resteasyBootstrap = new ResteasyServerBootstrap();
    resteasyBootstrap.start();
  }
  
  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    resteasyBootstrap.stop();
  }
  
  private ProcessDefinitionQuery setUpMockDefinitionQuery(List<ProcessDefinition> mockedDefinitions) {
    ProcessDefinitionQuery sampleDefinitionsQuery = mock(ProcessDefinitionQuery.class);
    when(sampleDefinitionsQuery.list()).thenReturn(mockedDefinitions);
    when(sampleDefinitionsQuery.count()).thenReturn((long) mockedDefinitions.size());
    when(processEngine.getRepositoryService().createProcessDefinitionQuery()).thenReturn(sampleDefinitionsQuery);
    return sampleDefinitionsQuery;
  }

  @Override
  public void setUpRuntimeData() {
    mockedQuery = setUpMockDefinitionQuery(MockProvider.createMockDefinitions());
  }
  
  @Test
  public void testProcessDefinitionRetrieval() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    
    String queryKey = "Key";
    Response response = given().queryParam("keyLike", queryKey)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(PROCESS_DEFINITION_QUERY_URL);
    
    // assert query invocation
    inOrder.verify(mockedQuery).processDefinitionKeyLike(queryKey);
    inOrder.verify(mockedQuery).list();
    
    String content = response.asString();
    List<String> definitions = from(content).getList("");
    Assert.assertEquals("There should be one process definition returned.", 1, definitions.size());
    Assert.assertNotNull("There should be one process definition returned", definitions.get(0));
    
    String returnedDefinitionKey = from(content).getString("[0].key");
    String returnedDefinitionId = from(content).getString("[0].id");
    String returnedCategory = from(content).getString("[0].category");
    String returnedDefinitionName = from(content).getString("[0].name");
    String returnedDescription = from(content).getString("[0].description");
    int returnedVersion = from(content).getInt("[0].version");
    String returnedResourceName = from(content).getString("[0].resource");
    String returnedDeploymentId  = from(content).getString("[0].deploymentId");
    String returnedDiagramResourceName = from(content).getString("[0].diagram");
    Boolean returnedIsSuspended = from(content).getBoolean("[0].suspended");

    Assert.assertEquals(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedDefinitionId);
    Assert.assertEquals(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_KEY, returnedDefinitionKey);
    Assert.assertEquals(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_CATEGORY, returnedCategory);
    Assert.assertEquals(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_NAME, returnedDefinitionName);
    Assert.assertEquals(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_DESCRIPTION, returnedDescription);
    Assert.assertEquals(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_VERSION, returnedVersion);
    Assert.assertEquals(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME, returnedResourceName);
    Assert.assertEquals(ExampleDataProvider.EXAMPLE_DEPLOYMENT_ID, returnedDeploymentId);
    Assert.assertEquals(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME, returnedDiagramResourceName);
    Assert.assertEquals(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED, returnedIsSuspended);
  }
  
  @Test
  public void testIncompleteProcessDefinition() {
    setUpMockDefinitionQuery(createIncompleteMockDefinitions());
    Response response = expect().statusCode(Status.OK.getStatusCode())
        .when().get(PROCESS_DEFINITION_QUERY_URL);
    
    String content = response.asString();
    String returnedResourceName = from(content).getString("[0].resource");
    Assert.assertNull("Should be null, as it is also null in the original process definition on the server.", 
        returnedResourceName);
  }
  
  private List<ProcessDefinition> createIncompleteMockDefinitions() {
    List<ProcessDefinition> mocks = new ArrayList<ProcessDefinition>();
    
    MockDefinitionBuilder builder = new MockDefinitionBuilder();
    ProcessDefinition mockDefinition = 
        builder.id(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
          .category(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_CATEGORY)
          .name(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_NAME)
          .key(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
          .suspended(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED)
          .version(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_VERSION).build();
    
    mocks.add(mockDefinition);
    return mocks;
  }
  
  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode()).when().get(PROCESS_DEFINITION_QUERY_URL);
    
    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }
  
  @Test
  public void testAdditionalParameters() {
    Map<String, String> queryParameters = getCompleteQueryParameters();
    
    given().queryParams(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
    
    // assert query invocation
    verify(mockedQuery).processDefinitionCategory(queryParameters.get("category"));
    verify(mockedQuery).processDefinitionCategoryLike(queryParameters.get("categoryLike"));
    verify(mockedQuery).processDefinitionName(queryParameters.get("name"));
    verify(mockedQuery).processDefinitionNameLike(queryParameters.get("nameLike"));
    verify(mockedQuery).deploymentId(queryParameters.get("deploymentId"));
    verify(mockedQuery).processDefinitionKey(queryParameters.get("key"));
    verify(mockedQuery).processDefinitionKeyLike(queryParameters.get("keyLike"));
    verify(mockedQuery).processDefinitionVersion(Integer.parseInt(queryParameters.get("ver")));
    verify(mockedQuery).latestVersion();
    verify(mockedQuery).processDefinitionResourceName(queryParameters.get("resourceName"));
    verify(mockedQuery).processDefinitionResourceNameLike(queryParameters.get("resourceNameLike"));
    verify(mockedQuery).startableByUser(queryParameters.get("startableBy"));
    verify(mockedQuery).active();
    verify(mockedQuery).suspended();
    verify(mockedQuery).list();
  }
  
  private Map<String, String> getCompleteQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    
    parameters.put("category", "cat");
    parameters.put("categoryLike", "catlike");
    parameters.put("name", "name");
    parameters.put("nameLike", "namelike");
    parameters.put("deploymentId", "depId");
    parameters.put("key", "key");
    parameters.put("keyLike", "keylike");
    parameters.put("ver", "0");
    parameters.put("latest", "true");
    parameters.put("resourceName", "res");
    parameters.put("resourceNameLike", "resLike");
    parameters.put("startableBy", "kermit");
    parameters.put("suspended", "true");
    parameters.put("active", "true");
    
    return parameters;
  }
  

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("category", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionCategory();
    inOrder.verify(mockedQuery).asc();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("key", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).desc();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("id", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("version", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionVersion();
    inOrder.verify(mockedQuery).desc();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("name", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionName();
    inOrder.verify(mockedQuery).asc();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("deploymentId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeploymentId();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
    
    verify(mockedQuery).listPage(firstResult, maxResults);
  }
  
  /**
   * If parameter "firstResult" is missing, we expect 0 as default.
   */
  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;
    given().queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
    
    verify(mockedQuery).listPage(0, maxResults);
  }
  
  /**
   * If parameter "maxResults" is missing, we expect Integer.MAX_VALUE as default.
   */
  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;
    given().queryParam("firstResult", firstResult)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
    
    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }
  
  @Test
  public void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(PROCESS_DEFINITION_COUNT_QUERY_URL);
    
    verify(mockedQuery).count();
  }
  
}
