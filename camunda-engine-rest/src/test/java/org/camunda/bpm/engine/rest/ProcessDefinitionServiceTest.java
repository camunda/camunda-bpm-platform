package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.helper.MockDefinitionBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public class ProcessDefinitionServiceTest extends AbstractRestServiceTest {
  
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
  
  private static final String PROCESS_DEFINITION_QUERY_URL = "/process-definition/query";
  
  private ProcessDefinitionQuery mockedQuery;
  
  private ProcessDefinitionQuery setUpMockDefinitionQuery(List<ProcessDefinition> mockedDefinitions) {
    ProcessDefinitionQuery sampleDefinitionsQuery = mock(ProcessDefinitionQuery.class);
    when(sampleDefinitionsQuery.list()).thenReturn(mockedDefinitions);
    when(processEngine.getRepositoryService().createProcessDefinitionQuery()).thenReturn(sampleDefinitionsQuery);
    when(sampleDefinitionsQuery.processDefinitionKeyLike(any(String.class))).thenReturn(sampleDefinitionsQuery);
    return sampleDefinitionsQuery;
  }
  
  private ProcessDefinition createMockDefinition() {
    MockDefinitionBuilder builder = new MockDefinitionBuilder();
    ProcessDefinition mockDefinition = 
        builder.id(EXAMPLE_DEFINITION_ID).category(EXAMPLE_CATEGORY).name(EXAMPLE_DEFINITION_NAME)
          .key(EXAMPLE_DEFINITION_KEY).description(EXAMPLE_DEFINITION_DESCRIPTION)
          .version(EXAMPLE_VERSION).resource(EXAMPLE_RESOURCE_NAME)
          .deploymentId(EXAMPLE_DEPLOYMENT_ID).diagram(EXAMPLE_DIAGRAM_RESOURCE_NAME)
          .suspended(EXAMPLE_IS_SUSPENDED).build();
    return mockDefinition;
  }
  
  private void injectMockedQuery(ProcessDefinition mockedDefinition) {
    List<ProcessDefinition> definitions = new ArrayList<ProcessDefinition>();
    definitions.add(mockedDefinition);
    mockedQuery = setUpMockDefinitionQuery(definitions);
  }
  
  @Before
  public void setUpMockedQuery() {
    injectMockedQuery(createMockDefinition());
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
    List<String> definitions = from(content).getList("data");
    Assert.assertEquals("There should be one process definition returned.", 1, definitions.size());
    
    String returnedDefinitionKey = from(content).getString("data[0].key");
    String returnedDefinitionId = from(content).getString("data[0].id");
    String returnedCategory = from(content).getString("data[0].category");
    String returnedDefinitionName = from(content).getString("data[0].name");
    String returnedDescription = from(content).getString("data[0].description");
    Integer returnedVersion = from(content).getInt("data[0].version");
    String returnedResourceName = from(content).getString("data[0].resource");
    String returnedDeploymentId  = from(content).getString("data[0].deploymentId");
    String returnedDiagramResourceName = from(content).getString("data[0].diagram");
    Boolean returnedIsSuspended = from(content).getBoolean("data[0].suspended");

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
    injectMockedQuery(createIncompleteMockDefinition());
    Response response = expect().statusCode(Status.OK.getStatusCode())
        .when().get(PROCESS_DEFINITION_QUERY_URL);
    
    String content = response.asString();
    String returnedResourceName = from(content).getString("data[0].resource");
    Assert.assertNull("Should be null, as it is also null in the original process definition on the server.", 
        returnedResourceName);
  }
  
  private ProcessDefinition createIncompleteMockDefinition() {
    MockDefinitionBuilder builder = new MockDefinitionBuilder();
    ProcessDefinition mockDefinition = 
        builder.id(EXAMPLE_DEFINITION_ID).category(EXAMPLE_CATEGORY)
          .name(EXAMPLE_DEFINITION_NAME).key(EXAMPLE_DEFINITION_KEY)
          .suspended(EXAMPLE_IS_SUSPENDED).version(EXAMPLE_VERSION).build();
    return mockDefinition;
  }
  
  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given().queryParam("keyLike", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }
  
  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode()).when().get(PROCESS_DEFINITION_QUERY_URL);
    
    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }
  
  @Test
  public void testInvalidNumericParameter() {
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
  public void testInvalidBooleanParameter() {
    String anInvalidBooleanQueryParam = "neitherTrueNorFalse";
    given().queryParam("active", anInvalidBooleanQueryParam)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }
  
  @Test
  public void testAdditionalParameters() {

    Map<String, String> queryParameters = getCompleteQueryParameters();
    
    RequestSpecification spec = given();
    for (Entry<String, String> queryParam : queryParameters.entrySet()) {
      spec.queryParam(queryParam.getKey(), queryParam.getValue());
    }
    
    spec.expect().statusCode(Status.OK.getStatusCode()).when().get(PROCESS_DEFINITION_QUERY_URL);
    
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
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("category", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  private void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }
  
  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "category")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
  }
  
  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
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
  
}
