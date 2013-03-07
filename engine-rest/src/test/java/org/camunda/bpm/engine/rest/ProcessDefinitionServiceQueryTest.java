package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.helper.MockDefinitionBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public class ProcessDefinitionServiceQueryTest extends AbstractRestServiceTest {
  
  private static final String EXAMPLE_DEFINITION_KEY = "aKey";
  private static final String EXAMPLE_CATEGORY = "aCategory";
  private static final String EXAMPLE_DEFINITION_NAME = "aName";
  private static final String EXAMPLE_DEFINITION_ID = "anId";
  private static final String EXAMPLE_DEFINITION_DESCRIPTION = "aDesc";
  private static final Integer EXAMPLE_VERSION = 42;
  private static final String EXAMPLE_RESOURCE_NAME = "aResourceName";
  private static final String EXAMPLE_DEPLOYMENT_ID = "aDeployment";
  private static final String EXAMPLE_DIAGRAM_RESOURCE_NAME = "aDiagram";
  private static final Boolean EXAMPLE_IS_SUSPENDED = false;
  
  private static final String PROCESS_DEFINITION_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition";
  private static final String PROCESS_DEFINITION_COUNT_QUERY_URL = PROCESS_DEFINITION_QUERY_URL + "/count";
  
  private ProcessDefinitionQuery mockedQuery;
  
  private ProcessDefinitionQuery setUpMockDefinitionQuery(List<ProcessDefinition> mockedDefinitions) {
    ProcessDefinitionQuery sampleDefinitionsQuery = mock(ProcessDefinitionQuery.class);
    when(sampleDefinitionsQuery.list()).thenReturn(mockedDefinitions);
    when(sampleDefinitionsQuery.count()).thenReturn((long) mockedDefinitions.size());
    when(processEngine.getRepositoryService().createProcessDefinitionQuery()).thenReturn(sampleDefinitionsQuery);
    return sampleDefinitionsQuery;
  }
  
  private List<ProcessDefinition> createMockDefinitions() {
    List<ProcessDefinition> mocks = new ArrayList<ProcessDefinition>();
    
    MockDefinitionBuilder builder = new MockDefinitionBuilder();
    ProcessDefinition mockDefinition = 
        builder.id(EXAMPLE_DEFINITION_ID).category(EXAMPLE_CATEGORY).name(EXAMPLE_DEFINITION_NAME)
          .key(EXAMPLE_DEFINITION_KEY).description(EXAMPLE_DEFINITION_DESCRIPTION)
          .version(EXAMPLE_VERSION).resource(EXAMPLE_RESOURCE_NAME)
          .deploymentId(EXAMPLE_DEPLOYMENT_ID).diagram(EXAMPLE_DIAGRAM_RESOURCE_NAME)
          .suspended(EXAMPLE_IS_SUSPENDED).build();
    
    mocks.add(mockDefinition);
    return mocks;
  }
  
//  @Before
  public void setUpMockedQuery() throws IOException {
    setupTestScenario();
    mockedQuery = setUpMockDefinitionQuery(createMockDefinitions());
  }
  
  @Test
  public void testProcessDefinitionRetrieval() throws IOException {
    setUpMockedQuery();
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
    Integer returnedVersion = from(content).getInt("[0].version");
    String returnedResourceName = from(content).getString("[0].resource");
    String returnedDeploymentId  = from(content).getString("[0].deploymentId");
    String returnedDiagramResourceName = from(content).getString("[0].diagram");
    Boolean returnedIsSuspended = from(content).getBoolean("[0].suspended");

    Assert.assertEquals(EXAMPLE_DEFINITION_ID, returnedDefinitionId);
    Assert.assertEquals(EXAMPLE_DEFINITION_KEY, returnedDefinitionKey);
    Assert.assertEquals(EXAMPLE_CATEGORY, returnedCategory);
    Assert.assertEquals(EXAMPLE_DEFINITION_NAME, returnedDefinitionName);
    Assert.assertEquals(EXAMPLE_DEFINITION_DESCRIPTION, returnedDescription);
    Assert.assertEquals(EXAMPLE_VERSION, returnedVersion);
    Assert.assertEquals(EXAMPLE_RESOURCE_NAME, returnedResourceName);
    Assert.assertEquals(EXAMPLE_DEPLOYMENT_ID, returnedDeploymentId);
    Assert.assertEquals(EXAMPLE_DIAGRAM_RESOURCE_NAME, returnedDiagramResourceName);
    Assert.assertEquals(EXAMPLE_IS_SUSPENDED, returnedIsSuspended);
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
        builder.id(EXAMPLE_DEFINITION_ID).category(EXAMPLE_CATEGORY)
          .name(EXAMPLE_DEFINITION_NAME).key(EXAMPLE_DEFINITION_KEY)
          .suspended(EXAMPLE_IS_SUSPENDED).version(EXAMPLE_VERSION).build();
    
    mocks.add(mockDefinition);
    return mocks;
  }
  
  @Test
  public void testEmptyQuery() throws IOException {
    setUpMockedQuery();
    String queryKey = "";
    given().queryParam("keyLike", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }
  
  @Test
  public void testNoParametersQuery() throws IOException {
    setUpMockedQuery();
    expect().statusCode(Status.OK.getStatusCode()).when().get(PROCESS_DEFINITION_QUERY_URL);
    
    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }
  
  @Test
  public void testInvalidNumericParameter() throws IOException {
    setUpMockedQuery();
    String anInvalidIntegerQueryParam = "aString";
    given().queryParam("ver", anInvalidIntegerQueryParam)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }
  
  /**
   * We assume that boolean query parameters that are not "true"
   * or "false" are evaluated to "false" and don't cause a 400 error.
   */
  @Test
  public void testInvalidBooleanParameter() throws IOException {
    setUpMockedQuery();
    String anInvalidBooleanQueryParam = "neitherTrueNorFalse";
    given().queryParam("active", anInvalidBooleanQueryParam)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }
  
  @Test
  public void testAdditionalParameters() throws IOException {
    setUpMockedQuery();

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
  public void testSortingParameters() throws IOException {
    setUpMockedQuery();
    
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("category", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionCategory();
    inOrder.verify(mockedQuery).asc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("key", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).desc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("id", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("version", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionVersion();
    inOrder.verify(mockedQuery).desc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("name", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionName();
    inOrder.verify(mockedQuery).asc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("deploymentId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeploymentId();
    inOrder.verify(mockedQuery).desc();
  }
  
  @Test
  public void testInvalidSortingOptions() throws IOException {
    setUpMockedQuery();
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("category", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  private void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }
  
  @Test
  public void testSortByParameterOnly() throws IOException {
    setUpMockedQuery();
    given().queryParam("sortBy", "category")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }
  
  @Test
  public void testSortOrderParameterOnly() throws IOException {
    setUpMockedQuery();
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }

  @Test
  public void testSuccessfulPagination() throws IOException {
    setUpMockedQuery();
    
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
  public void testMissingFirstResultParameter() throws IOException {
    setUpMockedQuery();
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
  public void testMissingMaxResultsParameter() throws IOException {
    setUpMockedQuery();
    int firstResult = 10;
    given().queryParam("firstResult", firstResult)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
    
    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }
  
  @Test
  public void testQueryCount() throws IOException {
    setUpMockedQuery();
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(PROCESS_DEFINITION_COUNT_QUERY_URL);
    
    verify(mockedQuery).count();
  }
  
}
