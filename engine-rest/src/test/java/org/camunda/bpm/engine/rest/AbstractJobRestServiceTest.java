package org.camunda.bpm.engine.rest;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import static org.hamcrest.Matchers.equalTo;

import static com.jayway.restassured.RestAssured.given;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockJobBuilder;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.http.ContentType;

public abstract class AbstractJobRestServiceTest extends
		AbstractRestServiceTest {

	protected static final String JOB_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH
			+ "/job/{id}";
	protected static final String JOB_RESOURCE_SET_RETRIES_URL = JOB_RESOURCE_URL
			+ "/retries";

	private ProcessEngine namedProcessEngine;
	private ManagementService mockManagementService;

	private JobQuery mockQuery;

	@Before
	public void setUpRuntimeData() {

		mockQuery = mock(JobQuery.class);
		Job mockedJob = new MockJobBuilder()
				.id(MockProvider.EXAMPLE_JOB_ID)
				.processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
				.executionId(MockProvider.EXAMPLE_EXECUTION_ID)
				.retries(MockProvider.EXAMPLE_JOB_RETRIES)
				.exceptionMessage(MockProvider.EXAMPLE_JOB_NO_EXCEPTION_MESSAGE)
				.dueDate(new Date()).build();

		when(mockQuery.singleResult()).thenReturn(mockedJob);
		when(mockQuery.jobId(MockProvider.EXAMPLE_JOB_ID))
				.thenReturn(mockQuery);

		mockManagementService = mock(ManagementService.class);
		when(mockManagementService.createJobQuery()).thenReturn(mockQuery);

		namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
		when(namedProcessEngine.getManagementService()).thenReturn(
				mockManagementService);
	}

	@Test
	public void testSetJobRetrys() {

		Map<String, Object> retriesVariableJson = new HashMap<String, Object>();
		retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);

		given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
				.contentType(ContentType.JSON).body(retriesVariableJson).then()
				.expect().statusCode(Status.NO_CONTENT.getStatusCode()).when()
				.put(JOB_RESOURCE_SET_RETRIES_URL);

		verify(mockManagementService).setJobRetries(
				MockProvider.EXAMPLE_JOB_ID, MockProvider.EXAMPLE_JOB_RETRIES);
	}

	@Test
	public void testSetJobRetrysNonExistentJob() {
		doThrow(new ProcessEngineException("No job found with id '"
						+ MockProvider.NON_EXISTING_JOB_ID + "'."))
				.when(mockManagementService)
				.setJobRetries(MockProvider.NON_EXISTING_JOB_ID, MockProvider.EXAMPLE_JOB_RETRIES);

		Map<String, Object> retriesVariableJson = new HashMap<String, Object>();
		retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);

		given().pathParam("id", MockProvider.NON_EXISTING_JOB_ID)
				.contentType(ContentType.JSON).body(retriesVariableJson).then()
				.expect()
				.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.when().put(JOB_RESOURCE_SET_RETRIES_URL);

		verify(mockManagementService).setJobRetries(
				MockProvider.NON_EXISTING_JOB_ID,
				MockProvider.EXAMPLE_JOB_RETRIES);
	}

	@Test
	public void testSetJobRetrysNegativeRetries() {

		doThrow(new ProcessEngineException(
						"The number of job retries must be a non-negative Integer, but '"
								+ MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES
								+ "' has been provided."))
				.when(mockManagementService)
				.setJobRetries(MockProvider.EXAMPLE_JOB_ID, MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);

		Map<String, Object> retriesVariableJson = new HashMap<String, Object>();
		retriesVariableJson.put("retries",
				MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);

		given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
				.contentType(ContentType.JSON).body(retriesVariableJson).then()
				.then().expect()
				.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.when().put(JOB_RESOURCE_SET_RETRIES_URL);

		verify(mockManagementService).setJobRetries(
				MockProvider.EXAMPLE_JOB_ID,
				MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
	}

	@Test
	public void testSimpleJobGet() {
		given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
				.then()
				.expect()
				.statusCode(Status.OK.getStatusCode())
				.body("jobId", equalTo(MockProvider.EXAMPLE_JOB_ID))
				.body("processInstanceId",
						equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
				.body("executionId", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
				.body("exceptionMessage",
						equalTo(MockProvider.EXAMPLE_JOB_NO_EXCEPTION_MESSAGE))
				.when().get(JOB_RESOURCE_URL);

		InOrder inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).jobId(MockProvider.EXAMPLE_JOB_ID);
		inOrder.verify(mockQuery).singleResult();
	}

	@Test
	public void testJobGetIdDoesntExist() {
		JobQuery invalidQueryNonExistingJob;
		invalidQueryNonExistingJob = mock(JobQuery.class);
		when(mockManagementService.createJobQuery()
				.jobId(MockProvider.NON_EXISTING_JOB_ID))
				.thenReturn(invalidQueryNonExistingJob);
		when(invalidQueryNonExistingJob.singleResult()).thenReturn(null);

		String jobId = MockProvider.NON_EXISTING_JOB_ID;

		given().pathParam("id", jobId)
				.then()
				.expect()
				.statusCode(Status.NOT_FOUND.getStatusCode())
				.contentType(ContentType.JSON)
				.body("type",
						equalTo(InvalidRequestException.class.getSimpleName()))
				.body("message",
						equalTo("Job with id " + jobId + " does not exist"))
				.when().get(JOB_RESOURCE_URL);
	}

}
