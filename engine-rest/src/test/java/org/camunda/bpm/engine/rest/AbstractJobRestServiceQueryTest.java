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
package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractJobRestServiceQueryTest extends AbstractRestServiceTest {

	protected static final String JOBS_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/job";

	private JobQuery mockQuery;
	private static final int MAX_RESULTS_TEN = 10;
	private static final int FIRST_RESULTS_ZERO = 0;

	@Before
	public void setUpRuntimeData() {
		mockQuery = setUpMockJobQuery(MockProvider.createMockJobs());
	}

	private JobQuery setUpMockJobQuery(List<Job> mockedJobs) {
		JobQuery sampleJobQuery = mock(JobQuery.class);
		
		when(sampleJobQuery.list()).thenReturn(mockedJobs);
		when(sampleJobQuery.count()).thenReturn((long) mockedJobs.size());

		when(processEngine.getManagementService().createJobQuery()).thenReturn(sampleJobQuery);

		return sampleJobQuery;
	}

	@Test
	public void testEmptyQuery() {
		String queryJobId = "";
		given().queryParam("id", queryJobId).then().expect()
				.statusCode(Status.OK.getStatusCode())
				.when().get(JOBS_RESOURCE_URL);
	}

	@Test
	public void testNoParametersQuery() {
		expect().statusCode(Status.OK.getStatusCode())
		.when().get(JOBS_RESOURCE_URL);

		verify(mockQuery).list();
		verifyNoMoreInteractions(mockQuery);
	}

	@Test
	public void testSortByParameterOnly() {
		given().queryParam("sortBy", "jobDueDate")
				.then()
				.expect()
				.statusCode(Status.BAD_REQUEST.getStatusCode())
				.contentType(ContentType.JSON)
				.body("type",
						equalTo(InvalidRequestException.class.getSimpleName()))
				.body("message",
						equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
				.when().get(JOBS_RESOURCE_URL);
	}

	@Test
	public void testSortOrderParameterOnly() {
		given().queryParam("sortOrder", "asc")
				.then()
				.expect()
				.statusCode(Status.BAD_REQUEST.getStatusCode())
				.contentType(ContentType.JSON)
				.body("type",
						equalTo(InvalidRequestException.class.getSimpleName()))
				.body("message",
						equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
				.when().get(JOBS_RESOURCE_URL);
	}

	@Test
	public void testSimpleJobQuery() {
		String jobId = MockProvider.EXAMPLE_JOB_ID;

		Response response = given().queryParam("jobId", jobId).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		InOrder inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).jobId(jobId);
		inOrder.verify(mockQuery).list();

		String content = response.asString();
		List<String> instances = from(content).getList("");
		Assert.assertEquals("There should be one job returned.", 1, instances.size());
		Assert.assertNotNull("The returned job should not be null.", instances.get(0));

		String returnedJobId = from(content).getString("[0].id");
		String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
		String returendExecutionId = from(content).getString("[0].executionId");
		String returnedExceptionMessage = from(content).getString("[0].exceptionMessage");

		Assert.assertEquals(MockProvider.EXAMPLE_JOB_ID, returnedJobId);
		Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
		Assert.assertEquals(MockProvider.EXAMPLE_EXECUTION_ID, returendExecutionId);
		Assert.assertEquals(MockProvider.EXAMPLE_JOB_NO_EXCEPTION_MESSAGE, returnedExceptionMessage);
	}

	@Test
	public void testInvalidDueDateComparator() {

		String variableValue = "2013-05-05";
		String invalidComparator = "bt";

		String queryValue = invalidComparator + "_" + variableValue;
		given().queryParam("dueDates", queryValue)
				.then()
				.expect()
				.statusCode(Status.BAD_REQUEST.getStatusCode())
				.contentType(ContentType.JSON)
				.body("type",equalTo(InvalidRequestException.class.getSimpleName()))
				.body("message", equalTo("Invalid due date comparator specified: " + invalidComparator))
				.when().get(JOBS_RESOURCE_URL);
	}
	
  @Test
  public void testInvalidDueDatComperatoreAsPost() {
    String invalidComparator = "bt";
    
    Map<String, Object> conditionJson = new HashMap<String, Object>();
    conditionJson.put("operator", invalidComparator);
    conditionJson.put("value", "2013-05-05");

    List<Map<String, Object>> conditions = new ArrayList<Map<String, Object>>();
    conditions.add(conditionJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("dueDates", conditions);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Invalid due date comparator specified: " + invalidComparator))
    .when().post(JOBS_RESOURCE_URL);
  }
	
  @Test
  public void testInvalidDueDate() {

    String variableValue = "invalidValue";
    String invalidComparator = "lt";

    String queryValue = invalidComparator + "_" + variableValue;
    given().queryParam("dueDates", queryValue)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Invalid due date format: Invalid format: \"invalidValue\""))
    .when().get(JOBS_RESOURCE_URL);
  }
  
  @Test
  public void testInvalidDueDateAsPost() {
    Map<String, Object> conditionJson = new HashMap<String, Object>();
    conditionJson.put("operator", "lt");
    conditionJson.put("value", "invalidValue");

    List<Map<String, Object>> conditions = new ArrayList<Map<String, Object>>();
    conditions.add(conditionJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("dueDates", conditions);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Invalid due date format: Invalid format: \"invalidValue\""))
    .when().post(JOBS_RESOURCE_URL);
  }
	

	@Test
	public void testAdditionalParametersExcludingDueDates() {
		Map<String, Object> parameters = getCompleteParameters();

		given().queryParams(parameters).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		verifyStringParameterQueryInvocations();
		verify(mockQuery).list();
	}
	
  @Test
  public void testMessagesParameter() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("messages", MockProvider.EXAMPLE_MESSAGES);

    given().queryParams(parameters)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(JOBS_RESOURCE_URL);

    verify(mockQuery).messages();
    verify(mockQuery).list();
  }

  @Test
  public void testMessagesTimersParameter() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("messages", MockProvider.EXAMPLE_MESSAGES);
    parameters.put("timers", MockProvider.EXAMPLE_TIMERS);

    given().queryParams(parameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .contentType(ContentType.JSON)
    .body("type",equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Parameter timers cannot be used together with parameter messages."))    
    .when().get(JOBS_RESOURCE_URL);
  }
  
  @Test
  public void testMessagesTimersParameterAsPost() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("messages", MockProvider.EXAMPLE_MESSAGES);
    parameters.put("timers", MockProvider.EXAMPLE_TIMERS);

    given().contentType(POST_JSON_CONTENT_TYPE).body(parameters)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .contentType(ContentType.JSON)
    .body("type",equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Parameter timers cannot be used together with parameter messages."))    
    .when().post(JOBS_RESOURCE_URL);
  }
  
  @Test
  public void testMessagesParameterAsPost() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("messages", MockProvider.EXAMPLE_MESSAGES);

    given().contentType(POST_JSON_CONTENT_TYPE).body(parameters)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().post(JOBS_RESOURCE_URL);

    verify(mockQuery).messages();
    verify(mockQuery).list();
  }
	 
  private Map<String, Object> getCompleteParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("jobId", MockProvider.EXAMPLE_JOB_ID);
    parameters.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    parameters.put("executionId", MockProvider.EXAMPLE_EXECUTION_ID);
    parameters.put("withRetriesLeft", MockProvider.EXAMPLE_WITH_RETRIES_LEFT);
    parameters.put("executable", MockProvider.EXAMPLE_EXECUTABLE);
    parameters.put("timers", MockProvider.EXAMPLE_TIMERS);
    parameters.put("withException", MockProvider.EXAMPLE_WITH_EXCEPTION);
    parameters.put("exceptionMessage", MockProvider.EXAMPLE_EXCEPTION_MESSAGE);
    parameters.put("noRetriesLeft", MockProvider.EXAMPLE_NO_RETRIES_LEFT);
    return parameters;
  }
	
  @Test
  public void testAdditionalParametersExcludingDueDatesAsPost() {
    Map<String, Object> parameters = getCompleteParameters();

    given().contentType(POST_JSON_CONTENT_TYPE).body(parameters)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().post(JOBS_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
    verify(mockQuery).list();
  }


	private void verifyStringParameterQueryInvocations() {
		Map<String, Object> parameters = getCompleteParameters();

		verify(mockQuery).jobId((String) parameters.get("jobId"));
		verify(mockQuery).processInstanceId((String) parameters.get("processInstanceId"));
		verify(mockQuery).executionId((String) parameters.get("executionId"));
		verify(mockQuery).withRetriesLeft();
		verify(mockQuery).executable();
		verify(mockQuery).timers();
		verify(mockQuery).withException();
		verify(mockQuery).exceptionMessage((String) parameters.get("exceptionMessage"));
		verify(mockQuery).noRetriesLeft();
	}

	@Test
	public void testDueDateParameters() {
		String variableValue = "2013-05-05";
		
		DateConverter converter = new DateConverter();
		Date date = converter.convertQueryParameterToType(variableValue);

		String queryValue = "lt_" + variableValue;
		given().queryParam("dueDates", queryValue).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		InOrder inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).duedateLowerThan(date);
		inOrder.verify(mockQuery).list();
		
		queryValue = "gt_" + variableValue;
		given().queryParam("dueDates", queryValue).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).duedateHigherThan(date);
		inOrder.verify(mockQuery).list();
	}

  @Test
  public void testDueDateParametersAsPost() {
    String value = "2013-05-18";
    String anotherValue = "2013-05-05";
    
    DateConverter converter = new DateConverter();
    Date date = converter.convertQueryParameterToType(value);
    Date anotherDate = converter.convertQueryParameterToType(anotherValue);

    Map<String, Object> conditionJson = new HashMap<String, Object>();
    conditionJson.put("operator", "lt");
    conditionJson.put("value", value);

    Map<String, Object> anotherConditionJson = new HashMap<String, Object>();
    anotherConditionJson.put("operator", "gt");
    anotherConditionJson.put("value", anotherValue);

    List<Map<String, Object>> conditions = new ArrayList<Map<String, Object>>();
    conditions.add(conditionJson);
    conditions.add(anotherConditionJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("dueDates", conditions);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().post(JOBS_RESOURCE_URL);

    verify(mockQuery).duedateHigherThan(anotherDate);
    verify(mockQuery).duedateLowerThan(date);
  }
	
	@Test
	public void testMultipleDueDateParameters() {
		String variableValue1 =  "2012-05-05";
		String variableParameter1 = "gt_" + variableValue1;

		String variableValue2 = "2013-02-02";
		String variableParameter2 = "lt_" + variableValue2;
		
    DateConverter converter = new DateConverter();
    Date date = converter.convertQueryParameterToType(variableValue1);
    Date anotherDate = converter.convertQueryParameterToType(variableValue2);

		String queryValue = variableParameter1 + "," + variableParameter2;

		given().queryParam("dueDates", queryValue).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		verify(mockQuery).duedateHigherThan(date);
		verify(mockQuery).duedateLowerThan(anotherDate);
	}

	@Test
	public void testSortingParameters() {
		InOrder inOrder = Mockito.inOrder(mockQuery);
		executeAndVerifySorting("jobId", "desc", Status.OK);
		inOrder.verify(mockQuery).orderByJobId();
		inOrder.verify(mockQuery).desc();

		inOrder = Mockito.inOrder(mockQuery);
		executeAndVerifySorting("processInstanceId", "asc", Status.OK);
		inOrder.verify(mockQuery).orderByProcessInstanceId();
		inOrder.verify(mockQuery).asc();

		inOrder = Mockito.inOrder(mockQuery);
		executeAndVerifySorting("executionId", "desc", Status.OK);
		inOrder.verify(mockQuery).orderByExecutionId();
		inOrder.verify(mockQuery).desc();

		inOrder = Mockito.inOrder(mockQuery);
		executeAndVerifySorting("jobRetries", "asc", Status.OK);
		inOrder.verify(mockQuery).orderByJobRetries();
		inOrder.verify(mockQuery).asc();

		inOrder = Mockito.inOrder(mockQuery);
		executeAndVerifySorting("jobDueDate", "desc", Status.OK);
		inOrder.verify(mockQuery).orderByJobDuedate();
		inOrder.verify(mockQuery).desc();
	}

	private void executeAndVerifySorting(String sortBy, String sortOrder,
			Status expectedStatus) {
		given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
				.then().expect().statusCode(expectedStatus.getStatusCode())
				.when().get(JOBS_RESOURCE_URL);
	}

	@Test
	public void testSuccessfulPagination() {

		int firstResult = FIRST_RESULTS_ZERO;
		int maxResults = MAX_RESULTS_TEN;
		given().queryParam("firstResult", firstResult)
				.queryParam("maxResults", maxResults).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		verify(mockQuery).listPage(firstResult, maxResults);
	}
}
