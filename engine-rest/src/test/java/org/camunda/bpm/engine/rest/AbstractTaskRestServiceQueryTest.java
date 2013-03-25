package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractTaskRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String TASK_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  protected static final String TASK_GROUPS_URL = TASK_QUERY_URL + "/groups";
  protected static final String TASK_COUNT_QUERY_URL = TASK_QUERY_URL + "/count";
  
  /**
   * Override this in specific test cases to create runtime data that is asserted.
   */
  @Before
  public abstract void setUpRuntimeData();
  
  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given().queryParam("name", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testInvalidDateParameter() {
    given().queryParams("due", "anInvalidDate")
      .expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "dueDate")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testGroupInfoQuery() {
    given().queryParam("userId", "name")
        .then().expect().statusCode(Status.OK.getStatusCode())
        .body("groups.size()", is(1))
        .body("groups[0].id", equalTo(ExampleDataProvider.EXAMPLE_GROUP_ID))
        .body("groups[0].name", equalTo(ExampleDataProvider.EXAMPLE_GROUP_NAME))
        .when().get(TASK_GROUPS_URL);
  }
  
  @Test
  public void testGroupInfoQueryWithMissingUserParameter() {
    expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(TASK_GROUPS_URL);
  }
  
}
