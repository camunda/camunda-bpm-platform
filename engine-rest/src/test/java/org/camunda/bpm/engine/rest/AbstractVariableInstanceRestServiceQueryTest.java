package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
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
import javax.xml.registry.InvalidRequestException;

import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractVariableInstanceRestServiceQueryTest extends AbstractRestServiceTest {
  
  protected static final String VARIABLE_INSTANCE_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/variable-instance";
  protected static final String VARIABLE_INSTANCE_COUNT_QUERY_URL = VARIABLE_INSTANCE_QUERY_URL + "/count";
  
  private VariableInstanceQuery mockedQuery;
  
  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockVariableInstanceQuery(createMockVariableInstanceList());
  }

  private VariableInstanceQuery setUpMockVariableInstanceQuery(List<VariableInstance> mockedInstances) {
    VariableInstanceQuery sampleInstanceQuery = mock(VariableInstanceQuery.class);
    
    when(sampleInstanceQuery.list()).thenReturn(mockedInstances);
    when(sampleInstanceQuery.count()).thenReturn((long) mockedInstances.size());
    when(processEngine.getRuntimeService().createVariableInstanceQuery()).thenReturn(sampleInstanceQuery);
    
    return sampleInstanceQuery;
  }

  private List<VariableInstance> createMockVariableInstanceList() {
    List<VariableInstance> mocks = new ArrayList<VariableInstance>();
    
    mocks.add(MockProvider.createMockVariableInstance());
    return mocks;
  }
  
  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode()).when().get(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }
  
  @Test
  public void testNoParametersQueryAsPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().post(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }
  
  @Test
  public void testInvalidVariableRequests() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_" + invalidComparator + "_" + variableValue;    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    
    // invalid format
    queryValue = "invalidFormattedVariableQuery";    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("variable query parameter has to have format KEY_OPERATOR_VALUE"))
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("variableName", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "variableName")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableName", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableName();
    inOrder.verify(mockedQuery).asc();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableType", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableType();
    inOrder.verify(mockedQuery).desc();
    
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityInstanceId();
    inOrder.verify(mockedQuery).asc();
  }
  
  @Test
  public void testSuccessfulPagination() {
    
    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    
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
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    
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
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }
  
  @Test
  public void testVariableInstanceRetrieval() {
    String queryVariableName = "aVariableInstanceName";
    Response response = given().queryParam("variableName", queryVariableName)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(VARIABLE_INSTANCE_QUERY_URL);
    
    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).variableName(queryVariableName);
    inOrder.verify(mockedQuery).list();
    
    String content = response.asString();
    List<String> variables = from(content).getList("");
    Assert.assertEquals("There should be one process definition returned.", 1, variables.size());
    Assert.assertNotNull("There should be one process definition returned", variables.get(0));
    
    String returnedName = from(content).getString("[0].name");
    String returnedType = from(content).getString("[0].type");
    String returnedValue = from(content).getString("[0].value");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedTaskId = from(content).getString("[0].taskId");
    String returnedActivityId = from(content).getString("[0].activityInstanceId");
  
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME, returnedName);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_TYPE, returnedType);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_VALUE, returnedValue);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_TASK_ID, returnedTaskId);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID, returnedActivityId);    
  }
  
  @Test
  public void testVariableInstanceRetrievalAsPost() {
    String queryVariableName = "aVariableInstanceName";
    Map<String, String> queryParameter = new HashMap<String, String>();
    queryParameter.put("variableName", queryVariableName);
    
    Response response = given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameter)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().post(VARIABLE_INSTANCE_QUERY_URL);
    
    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).variableName(queryVariableName);
    inOrder.verify(mockedQuery).list();
    
    String content = response.asString();
    List<String> variables = from(content).getList("");
    Assert.assertEquals("There should be one process definition returned.", 1, variables.size());
    Assert.assertNotNull("There should be one process definition returned", variables.get(0));
    
    String returnedName = from(content).getString("[0].name");
    String returnedType = from(content).getString("[0].type");
    String returnedValue = from(content).getString("[0].value");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedTaskId = from(content).getString("[0].taskId");
    String returnedActivityId = from(content).getString("[0].activityInstanceId");
  
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME, returnedName);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_TYPE, returnedType);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_VALUE, returnedValue);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_TASK_ID, returnedTaskId);
    Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID, returnedActivityId);    
  }
   
  @Test
  public void testAdditionalParametersExcludingVariableValues() {
    Map<String, String> queryParameters = new HashMap<String, String>();
    
    queryParameters.put("variableName", "aVariableName");
    queryParameters.put("variableNameLike", "aVariableNameLike");
    queryParameters.put("executionIdIn", "anExecutionId");
    queryParameters.put("processInstanceIdIn", "aProcessInstanceId");
    queryParameters.put("taskIdIn", "aTaskId");
    queryParameters.put("activityInstanceIdIn", "anActivityInstanceId");
    
    given().queryParams(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).variableName(queryParameters.get("variableName"));
    verify(mockedQuery).variableNameLike(queryParameters.get("variableNameLike"));
    verify(mockedQuery).processInstanceIdIn(queryParameters.get("processInstanceIdIn"));
    verify(mockedQuery).executionIdIn(queryParameters.get("executionIdIn"));
    verify(mockedQuery).taskIdIn(queryParameters.get("taskIdIn"));
    verify(mockedQuery).activityInstanceIdIn(queryParameters.get("activityInstanceIdIn"));
    verify(mockedQuery).list();
  }
  
  @Test
  public void testAdditionalParametersExcludingVariableValuesAsPost() {
    String aVariableName = "aVariableName";
    String aVariableNameLike = "aVariableNameLike";
    String aProcessInstanceId = "aProcessInstanceId";
    String anExecutionId = "anExecutionId";
    String aTaskId = "aTaskId";
    String anActivityInstanceId = "anActivityInstanceId";
    
    Map<String, Object> queryParameters = new HashMap<String, Object>();
    
    queryParameters.put("variableName", aVariableName);
    queryParameters.put("variableNameLike", aVariableNameLike);
    
    List<String> executionIdIn = new ArrayList<String>();
    executionIdIn.add(anExecutionId);
    queryParameters.put("executionIdIn", executionIdIn);

    List<String> processInstanceIdIn = new ArrayList<String>();
    processInstanceIdIn.add(aProcessInstanceId);
    queryParameters.put("processInstanceIdIn", processInstanceIdIn);
    
    List<String> taskIdIn = new ArrayList<String>();
    taskIdIn.add(aTaskId);
    queryParameters.put("taskIdIn", taskIdIn);
    
    List<String> activityInstanceIdIn = new ArrayList<String>();
    activityInstanceIdIn.add(anActivityInstanceId);
    queryParameters.put("activityInstanceIdIn", activityInstanceIdIn);
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameters)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).variableName(aVariableName);
    verify(mockedQuery).variableNameLike(aVariableNameLike);
    verify(mockedQuery).processInstanceIdIn(aProcessInstanceId);
    verify(mockedQuery).executionIdIn(anExecutionId);
    verify(mockedQuery).taskIdIn(aTaskId);
    verify(mockedQuery).activityInstanceIdIn(anActivityInstanceId);
    verify(mockedQuery).list();
  }

  @Test
  public void testVariableParameters() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    
    queryValue = variableName + "_gt_" + variableValue;    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);
    
    queryValue = variableName + "_gteq_" + variableValue;    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);
    
    queryValue = variableName + "_lt_" + variableValue;    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueLessThan(variableName, variableValue);
    
    queryValue = variableName + "_lteq_" + variableValue;    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);
  
    queryValue = variableName + "_like_" + variableValue;    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueLike(variableName, variableValue);
  
    queryValue = variableName + "_neq_" + variableValue;    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);    
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
  }
  
  @Test
  public void testMultipleVariableParameters() {
    String variableName1 = "varName";
    String variableValue1 = "varValue";
    String variableParameter1 = variableName1 + "_eq_" + variableValue1; 
    
    String variableName2 = "anotherVarName";
    String variableValue2 = "anotherVarValue";
    String variableParameter2 = variableName2 + "_neq_" + variableValue2;
    
    String queryValue = variableParameter1 + "," + variableParameter2;
    
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);    
    
    verify(mockedQuery).variableValueEquals(variableName1, variableValue1);
    verify(mockedQuery).variableValueNotEquals(variableName2, variableValue2);
  }
  
  @Test
  public void testMultipleVariableParametersAsPost() {
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
    json.put("variableValues", variables);
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).variableValueNotEquals(anotherVariableName, anotherVariableValue);
  }
  
  @Test
  public void testMultipleParameters() {
    String aProcessInstanceId = "aProcessInstanceId";
    String anotherProcessInstanceId = "anotherProcessInstanceId";
    
    String anExecutionId = "anExecutionId";
    String anotherExecutionId = "anotherExecutionId";
    
    String aTaskId = "aTaskId";
    String anotherTaskId = "anotherTaskId";

    String anActivityInstanceId = "anActivityInstanceId";
    String anotherActivityInstanceId = "anotherActivityInstanceId";
    
    given()
      .queryParam("processInstanceIdIn", aProcessInstanceId + "," + anotherProcessInstanceId)
      .queryParam("executionIdIn", anExecutionId + "," + anotherExecutionId)
      .queryParam("taskIdIn", aTaskId + "," + anotherTaskId)
      .queryParam("activityInstanceIdIn", anActivityInstanceId + "," + anotherActivityInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);    
    
    verify(mockedQuery).processInstanceIdIn(aProcessInstanceId, anotherProcessInstanceId);
    verify(mockedQuery).executionIdIn(anExecutionId, anotherExecutionId);
    verify(mockedQuery).taskIdIn(aTaskId, anotherTaskId);
    verify(mockedQuery).activityInstanceIdIn(anActivityInstanceId, anotherActivityInstanceId);
  }
  
  @Test
  public void testMultipleParametersAsPost() {
    String aProcessInstanceId = "aProcessInstanceId";
    String anotherProcessInstanceId = "anotherProcessInstanceId";
    
    List<String> processDefinitionIdIn= new ArrayList<String>();
    processDefinitionIdIn.add(aProcessInstanceId);
    processDefinitionIdIn.add(anotherProcessInstanceId);

    String anExecutionId = "anExecutionId";
    String anotherExecutionId = "anotherExecutionId";
    
    List<String> executionIdIn= new ArrayList<String>();
    executionIdIn.add(anExecutionId);
    executionIdIn.add(anotherExecutionId);
    
    String aTaskId = "aTaskId";
    String anotherTaskId = "anotherTaskId";
    
    List<String> taskIdIn= new ArrayList<String>();
    taskIdIn.add(aTaskId);
    taskIdIn.add(anotherTaskId);
    
    String anActivityInstanceId = "anActivityInstanceId";
    String anotherActivityInstanceId = "anotherActivityInstanceId";
    
    List<String> activityInstanceIdIn= new ArrayList<String>();
    activityInstanceIdIn.add(anActivityInstanceId);
    activityInstanceIdIn.add(anotherActivityInstanceId);
       
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("processInstanceIdIn", processDefinitionIdIn);
    json.put("executionIdIn", executionIdIn);
    json.put("taskIdIn", taskIdIn);
    json.put("activityInstanceIdIn", activityInstanceIdIn);
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).processInstanceIdIn(aProcessInstanceId, anotherProcessInstanceId);
    verify(mockedQuery).executionIdIn(anExecutionId, anotherExecutionId);
    verify(mockedQuery).taskIdIn(aTaskId, anotherTaskId);
    verify(mockedQuery).activityInstanceIdIn(anActivityInstanceId, anotherActivityInstanceId);
  }
  
  @Test
  public void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(VARIABLE_INSTANCE_COUNT_QUERY_URL);
    
    verify(mockedQuery).count();
  }

  @Test
  public void testQueryCountForPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(VARIABLE_INSTANCE_COUNT_QUERY_URL);
    
    verify(mockedQuery).count();
  }
  
}
