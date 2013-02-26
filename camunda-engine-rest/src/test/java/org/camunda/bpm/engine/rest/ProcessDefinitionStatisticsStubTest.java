package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;

/**
 * This test only tests the REST interface, but no interaction with the engine as it assumes a stubbed 
 * implementation.
 * @author Thorben Lindhauer
 *
 */
public class ProcessDefinitionStatisticsStubTest extends AbstractRestServiceTest {

  private static final String PROCESS_DEFINITION_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/statistics";
  private static final String ACTIVITY_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/{id}/statistics";
  
  @Test
  public void testStatisticsRetrievalPerProcessDefinition() throws IOException {
    setupTestScenario();
    
    given().queryParam("groupBy", "definition")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(8))
      .body("[0].failedJobs", nullValue())
      .body("definition.key", hasItems(getStubProcessDefinitionKeys()))
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
  
  @Test
  public void testStatisticsRetrievalPerProcessDefinitionVersion() throws IOException {
    setupTestScenario();
    
    given().queryParam("groupBy", "version")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(18))
      .body("definition.size()", is(18))
      .body("definition.key", hasItems(getStubProcessDefinitionKeys()))
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
  
  @Test
  public void testInvalidGroupingOption() throws IOException {
    setupTestScenario();
    
    given().queryParam("groupBy", "aBadGroupingOption")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
  
  @Test
  public void testAdditionalFailedJobsOption() throws IOException {
    setupTestScenario();
    
    given().queryParam("groupBy", "definition").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(8))
      .body("[0].failedJobs", notNullValue())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
  
  /**
   * We expect grouping by process definition versions, if
   * no groupBy query parameter is specified.
   */
  @Test
  public void testNoGroupingOptionSupplied() throws IOException {
    setupTestScenario();
    
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(18))
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
  
  private String[] getStubProcessDefinitionKeys() {
    String[] definitionKeys = new String[] {
        "order_process_key", "fox_invoice", "loan_applicant_process",
        "loan_applicant_process_long_name", "order_process_key_1",
        "fox_invoice_1", "loan_applicant_process_1", "loan_applicant_process_long_name_1"
    };
    return definitionKeys;
  }
  
  @Test
  public void testActivityStatisticsRetrieval() throws IOException {
    setupTestScenario();
    
    given().pathParam("id", "aDefinitionId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(5))
      .body("[0].failedJobs", nullValue())
      .body("name", hasItems(getStubActivityNames()))
    .when().get(ACTIVITY_STATISTICS_URL);
  }
  
  private String[] getStubActivityNames() {
    String[] activityNames = new String[] {
        "Assign Approver", "Approve Invoice", "Review Invoice",
        "Prepare Bank Transfer", "Save Invoice To SVN"
    };
    return activityNames;
  }
}
