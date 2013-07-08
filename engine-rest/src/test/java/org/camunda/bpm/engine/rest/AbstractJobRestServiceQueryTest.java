package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

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

	protected static final String JOBS_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH
			+ "/job";

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

		when(processEngine.getManagementService().createJobQuery()).thenReturn(
				sampleJobQuery);

		return sampleJobQuery;
	}

	@Test
	public void testEmptyQuery() {
		String queryJobId = "";
		given().queryParam("jobId", queryJobId).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);
	}

	@Test
	public void testNoParametersQuery() {
		expect().statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

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
		Assert.assertEquals("There should be one job returned.", 1,
				instances.size());
		Assert.assertNotNull("The returned job should not be null.",
				instances.get(0));

		String returnedJobId = from(content).getString("[0].jobId");
		String returnedProcessInstanceId = from(content).getString(
				"[0].processInstanceId");
		String returendExecutionId = from(content).getString("[0].executionId");
		String returnedExceptionMessage = from(content).getString(
				"[0].exceptionMessage");

		Assert.assertEquals(MockProvider.EXAMPLE_JOB_ID, returnedJobId);
		Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID,
				returnedProcessInstanceId);
		Assert.assertEquals(MockProvider.EXAMPLE_EXECUTION_ID,
				returendExecutionId);
		Assert.assertEquals(MockProvider.EXAMPLE_JOB_NO_EXCEPTION_MESSAGE,
				returnedExceptionMessage);
	}

	@Test
	public void testInvalidDueDateComparator() {

		String variableName = "varName";
		String variableValue = "varValue";
		String invalidComparator = "bt";

		String queryValue = variableName + "_" + invalidComparator + "_"
				+ variableValue;
		given().queryParam("dueDates", queryValue)
				.then()
				.expect()
				.statusCode(Status.BAD_REQUEST.getStatusCode())
				.contentType(ContentType.JSON)
				.body("type",
						equalTo(InvalidRequestException.class.getSimpleName()))
				.body("message",
						equalTo("Invalid due date comparator specified: "
								+ invalidComparator)).when()
				.get(JOBS_RESOURCE_URL);
	}

	@Test
	public void testAdditionalParametersExcludingVariables() {
		Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

		given().queryParams(stringQueryParameters).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		verifyStringParameterQueryInvocations();
		verify(mockQuery).list();
	}

	private Map<String, String> getCompleteStringQueryParameters() {
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("jobId", MockProvider.EXAMPLE_JOB_ID);
		parameters.put("processInstanceId",
				MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
		parameters.put("executionId", MockProvider.EXAMPLE_EXECUTION_ID);
		parameters.put("exceptionMessage",
				MockProvider.EXAMPLE_EXCEPTION_MESSAGE);
		return parameters;
	}

	private void verifyStringParameterQueryInvocations() {
		Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

		verify(mockQuery).jobId(stringQueryParameters.get("jobId"));
		verify(mockQuery).processInstanceId(
				stringQueryParameters.get("processInstanceId"));
		verify(mockQuery).executionId(stringQueryParameters.get("executionId"));
		verify(mockQuery).exceptionMessage(
				stringQueryParameters.get("exceptionMessage"));
	}


	@Test
	public void testDueDateParameters() {
		String variableName = "varName";
		String variableValue = "2013-05-05";

		String queryValue = variableName + "_lt_" + variableValue;
		given().queryParam("dueDates", queryValue).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		InOrder inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).duedateLowerThan((any(Date.class)));
		inOrder.verify(mockQuery).list();

		queryValue = variableName + "_gt_" + variableValue;
		given().queryParam("dueDates", queryValue).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).duedateHigherThan((any(Date.class)));
		inOrder.verify(mockQuery).list();
	}



	@Test
	public void testMultipleDueDateParameters() {
		String variableName1 = "varName";
		String variableValue1 =  "2012-05-05";
		String variableParameter1 = variableName1 + "_gt_" + variableValue1;

		String variableName2 = "anotherVarName";
		String variableValue2 = "2013-02-02";
		String variableParameter2 = variableName2 + "_lt_" + variableValue2;

		String queryValue = variableParameter1 + "," + variableParameter2;

		given().queryParam("dueDates", queryValue).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(JOBS_RESOURCE_URL);

		verify(mockQuery).duedateHigherThan((any(Date.class)));
		verify(mockQuery).duedateLowerThan((any(Date.class)));
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
