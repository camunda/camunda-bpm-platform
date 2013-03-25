package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractProcessInstanceRestServiceQueryTest extends
    AbstractRestServiceTest {

  protected static final String PROCESS_INSTANCE_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance";
  protected static final String PROCESS_INSTANCE_COUNT_QUERY_URL = PROCESS_INSTANCE_QUERY_URL + "/count";
  
  /**
   * Override this in specific test cases to create runtime data that is asserted.
   */
  @Before
  public abstract void setUpRuntimeData();

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given().queryParam("processDefinitionKey", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testInvalidVariableRequests() {
    // invalid comparator
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
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "definitionId")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }
  
  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }
}
