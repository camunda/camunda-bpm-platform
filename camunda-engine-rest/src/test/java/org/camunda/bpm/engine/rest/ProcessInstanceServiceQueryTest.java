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

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public class ProcessInstanceServiceQueryTest extends AbstractRestServiceTest {
  
  private static final String EXAMPLE_BUSINESS_KEY = "aKey";
  private static final String EXAMPLE_ID = "anId";
  private static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcDefId";
  private static final boolean EXAMPLE_IS_SUSPENDED = false;
  private static final boolean EXAMPLE_IS_ENDED = false;

  private static final String PROCESS_INSTANCE_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance";
  private static final String PROCESS_INSTANCE_COUNT_QUERY_URL = PROCESS_INSTANCE_QUERY_URL + "/count";
  private ProcessInstanceQuery mockedQuery;
  
  private ProcessInstanceQuery setUpMockInstanceQuery(List<ProcessInstance> mockedInstances) {
    ProcessInstanceQuery sampleInstanceQuery = mock(ProcessInstanceQuery.class);
    when(sampleInstanceQuery.list()).thenReturn(mockedInstances);
    when(sampleInstanceQuery.count()).thenReturn((long) mockedInstances.size());
    when(processEngine.getRuntimeService().createProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    return sampleInstanceQuery;
  }
  
  private List<ProcessInstance> createMockInstances() {
    List<ProcessInstance> mocks = new ArrayList<ProcessInstance>();
    
    ProcessInstance mockInstance = mock(ProcessInstance.class);
    when(mockInstance.getBusinessKey()).thenReturn(EXAMPLE_BUSINESS_KEY);
    when(mockInstance.getId()).thenReturn(EXAMPLE_ID);
    when(mockInstance.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mockInstance.getProcessInstanceId()).thenReturn(EXAMPLE_ID);
    when(mockInstance.isSuspended()).thenReturn(EXAMPLE_IS_SUSPENDED);
    when(mockInstance.isEnded()).thenReturn(EXAMPLE_IS_ENDED);
    
    mocks.add(mockInstance);
    return mocks;
  }
  
//  @Before
  public void setUpMockedQuery() throws IOException {
    setupTestScenario();
    mockedQuery = setUpMockInstanceQuery(createMockInstances());
  }
  
  @Test
  public void testInstanceRetrieval() throws IOException {
    setUpMockedQuery();
    
    String queryKey = "key";
    Response response = given().queryParam("processDefinitionKey", queryKey)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(PROCESS_INSTANCE_QUERY_URL);
    
    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).processDefinitionKey(queryKey);
    inOrder.verify(mockedQuery).list();
    
    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one process definition returned.", 1, instances.size());
    Assert.assertNotNull("There should be one process definition returned", instances.get(0));
    
    String returnedInstanceId = from(content).getString("[0].id");
    Boolean returnedIsEnded = from(content).getBoolean("[0].ended");
    String returnedDefinitionId = from(content).getString("[0].definitionId");
    String returnedBusinessKey = from(content).getString("[0].businessKey");
    Boolean returnedIsSuspended = from(content).getBoolean("[0].suspended");

    Assert.assertEquals(EXAMPLE_ID, returnedInstanceId);
    Assert.assertEquals(EXAMPLE_IS_ENDED, returnedIsEnded);
    Assert.assertEquals(EXAMPLE_PROCESS_DEFINITION_ID, returnedDefinitionId);
    Assert.assertEquals(EXAMPLE_BUSINESS_KEY, returnedBusinessKey);
    Assert.assertEquals(EXAMPLE_IS_SUSPENDED, returnedIsSuspended);
  }
  
  @Test
  public void testIncompleteProcessInstance() {
    setUpMockInstanceQuery(createIncompleteMockInstances());
    Response response = expect().statusCode(Status.OK.getStatusCode())
        .when().get(PROCESS_INSTANCE_QUERY_URL);
    
    String content = response.asString();
    String returnedBusinessKey = from(content).getString("[0].businessKey");
    Assert.assertNull("Should be null, as it is also null in the original process instance on the server.", 
        returnedBusinessKey);
  }
  
  private List<ProcessInstance> createIncompleteMockInstances() {
    List<ProcessInstance> mocks = new ArrayList<ProcessInstance>();
    ProcessInstance mockInstance = mock(ProcessInstance.class);
    when(mockInstance.getId()).thenReturn(EXAMPLE_ID);
    
    mocks.add(mockInstance);
    return mocks;
  }
  
  @Test
  public void testEmptyQuery() throws IOException {
    setUpMockedQuery();
    String queryKey = "";
    given().queryParam("processDefinitionKey", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testNoParametersQuery() throws IOException {
    setUpMockedQuery();
    expect().statusCode(Status.OK.getStatusCode()).when().get(PROCESS_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }
  
  @Test
  public void testAdditionalParametersExcludingVariables() throws IOException {
    setUpMockedQuery();

    Map<String, String> queryParameters = getCompleteQueryParameters();
    
    given().queryParams(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).processInstanceBusinessKey(queryParameters.get("businessKey"));
    verify(mockedQuery).processDefinitionKey(queryParameters.get("processDefinitionKey"));
    verify(mockedQuery).processDefinitionId(queryParameters.get("processDefinitionId"));
    verify(mockedQuery).superProcessInstanceId(queryParameters.get("superProcessInstance"));
    verify(mockedQuery).subProcessInstanceId(queryParameters.get("subProcessInstance"));
    verify(mockedQuery).suspended();
    verify(mockedQuery).active();
    verify(mockedQuery).list();
  }
  
  private Map<String, String> getCompleteQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    
    parameters.put("businessKey", "aBusinessKey");
    parameters.put("processDefinitionKey", "aProcDefKey");
    parameters.put("processDefinitionId", "aProcDefId");
    parameters.put("superProcessInstance", "aSuperProcInstId");
    parameters.put("subProcessInstance", "aSubProcInstId");
    parameters.put("suspended", "true");
    parameters.put("active", "true");
    
    return parameters;
  }
  
  @Test
  public void testVariableParameters() throws IOException {
    setUpMockedQuery();
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;    
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    
    setUpMockedQuery();
    queryValue = variableName + "_gt_" + variableValue;    
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);
    
    setUpMockedQuery();
    queryValue = variableName + "_gteq_" + variableValue;    
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);
    
    setUpMockedQuery();
    queryValue = variableName + "_lt_" + variableValue;    
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueLessThan(variableName, variableValue);
    
    setUpMockedQuery();
    queryValue = variableName + "_lteq_" + variableValue;    
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);

    setUpMockedQuery();
    queryValue = variableName + "_like_" + variableValue;    
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueLike(variableName, variableValue);

    setUpMockedQuery();
    queryValue = variableName + "_neq_" + variableValue;    
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
  }
  
  @Test
  public void testInvalidVariableRequests() throws IOException {
    // invalid comparator
    setUpMockedQuery();
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_anInvalidComparator_" + variableValue;    
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    
    // invalid format
    queryValue = "invalidFormattedVariableQuery";    
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testMultipleVariableParametersAsPost() throws IOException {
    setUpMockedQuery();
    
    String variableName = "varName";
    String variableValue = "varValue";
    String anotherVariableName = "anotherVarName";
    Integer anotherVariableValue = 30;
    
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);
    
    Map<String, Object> anotherVariableJson = new HashMap<String, Object>();
    anotherVariableJson.put("name", anotherVariableName);
    anotherVariableJson.put("operator", "neq");
    anotherVariableJson.put("value", anotherVariableValue);
    
    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);
    variables.add(anotherVariableJson);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(PROCESS_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).variableValueNotEquals(anotherVariableName, anotherVariableValue);
    
  }
  
  @Test
  public void testCompletePostParameters() throws IOException {
    setUpMockedQuery();
    
    Map<String, String> queryParameters = getCompleteQueryParameters();
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().post(PROCESS_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).processInstanceBusinessKey(queryParameters.get("businessKey"));
    verify(mockedQuery).processDefinitionKey(queryParameters.get("processDefinitionKey"));
    verify(mockedQuery).processDefinitionId(queryParameters.get("processDefinitionId"));
    verify(mockedQuery).superProcessInstanceId(queryParameters.get("superProcessInstance"));
    verify(mockedQuery).subProcessInstanceId(queryParameters.get("subProcessInstance"));
    verify(mockedQuery).suspended();
    verify(mockedQuery).active();
    verify(mockedQuery).list();
  }
  
  @Test
  public void testSortingParameters() throws IOException {
    setUpMockedQuery();
    
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).desc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();
    setUpMockedQuery();
  }
  
  @Test
  public void testInvalidSortingOptions() throws IOException {
    setUpMockedQuery();
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  private void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testSortByParameterOnly() throws IOException {
    setUpMockedQuery();
    given().queryParam("sortBy", "definitionId")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testSortOrderParameterOnly() throws IOException {
    setUpMockedQuery();
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testSuccessfulPagination() throws IOException {
    setUpMockedQuery();
    
    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    
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
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    
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
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }
  
  @Test
  public void testQueryCount() throws IOException {
    setUpMockedQuery();
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(PROCESS_INSTANCE_COUNT_QUERY_URL);
    
    verify(mockedQuery).count();
  }
  
  @Test
  public void testQueryCountForPost() throws IOException {
    setUpMockedQuery();
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(PROCESS_INSTANCE_COUNT_QUERY_URL);
    
    verify(mockedQuery).count();
  }
}
