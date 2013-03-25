package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractProcessDefinitionRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String PROCESS_DEFINITION_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition";
  protected static final String PROCESS_DEFINITION_COUNT_QUERY_URL = PROCESS_DEFINITION_QUERY_URL + "/count";
  

  /**
   * Override this in specific test cases to create runtime data that is asserted.
   */
  @Before
  public abstract void setUpRuntimeData();
  
  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given().queryParam("keyLike", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_DEFINITION_QUERY_URL);
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
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("category", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
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
}
