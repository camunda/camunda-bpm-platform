package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractTaskRestServiceInteractionTest extends
    AbstractRestServiceTest {

  protected static final String TASK_SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  protected static final String SINGLE_TASK_URL = TASK_SERVICE_URL + "/{id}";
  protected static final String CLAIM_TASK_URL = SINGLE_TASK_URL + "/claim";
  protected static final String UNCLAIM_TASK_URL = SINGLE_TASK_URL + "/unclaim";
  protected static final String COMPLETE_TASK_URL = SINGLE_TASK_URL + "/complete";
  protected static final String RESOLVE_TASK_URL = SINGLE_TASK_URL + "/resolve";
  protected static final String DELEGATE_TASK_URL = SINGLE_TASK_URL + "/delegate";
  protected static final String TASK_FORM_URL = SINGLE_TASK_URL + "/form";
  
  /**
   * Override this in specific test cases to create runtime data that is asserted.
   */
  @Before
  public abstract void setUpRuntimeData();

  @Test
  public void testGetSingleTask() {
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(ExampleDataProvider.EXAMPLE_TASK_ID))
      .body("name", equalTo(ExampleDataProvider.EXAMPLE_TASK_NAME))
      .body("assignee", equalTo(ExampleDataProvider.EXAMPLE_TASK_ASSIGNEE_NAME))
      .body("created", equalTo(ExampleDataProvider.EXAMPLE_TASK_CREATE_TIME))
      .body("due", equalTo(ExampleDataProvider.EXAMPLE_TASK_DUE_DATE))
      .body("delegationState", equalTo(ExampleDataProvider.EXAMPLE_TASK_DELEGATION_STATE.toString()))
      .body("description", equalTo(ExampleDataProvider.EXAMPLE_TASK_DESCRIPTION))
      .body("executionId", equalTo(ExampleDataProvider.EXAMPLE_TASK_EXECUTION_ID))
      .body("owner", equalTo(ExampleDataProvider.EXAMPLE_TASK_OWNER))
      .body("parentTaskId", equalTo(ExampleDataProvider.EXAMPLE_TASK_PARENT_TASK_ID))
      .body("priority", equalTo(ExampleDataProvider.EXAMPLE_TASK_PRIORITY))
      .body("processDefinitionId", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("processInstanceId", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("taskDefinitionKey", equalTo(ExampleDataProvider.EXAMPLE_TASK_DEFINITION_KEY))
      .when().get(SINGLE_TASK_URL);
  }
  
  @Test
  public void testGetForm() {
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(ExampleDataProvider.EXAMPLE_FORM_KEY))
      .when().get(TASK_FORM_URL);
  }

}
