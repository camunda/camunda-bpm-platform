package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractJobRestServiceTest extends
		AbstractRestServiceTest {

	protected static final String SINGLE_MANAGEMENT_JOB_URL = TEST_RESOURCE_ROOT_PATH
			+ "/management/job/{id}";
	protected static final String CHANGE_JOB_RETRIES_URL = SINGLE_MANAGEMENT_JOB_URL
			+ "/retries/{retries}";

	private ProcessEngine namedProcessEngine;
	private ManagementService mockManagementService;
	private RuntimeService mockRuntimeService;

	@Before
	public void setUpRuntimeData() {
		namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
		mockManagementService = mock(ManagementService.class);
		mockRuntimeService = mock(RuntimeService.class);

		when(namedProcessEngine.getManagementService()).thenReturn(
				mockManagementService);
		when(namedProcessEngine.getRuntimeService()).thenReturn(
				mockRuntimeService);
		createJobInstanceMock();
	}

	private void createJobInstanceMock() {
		Job mockInstance = MockProvider.createMockJob();

		JobQuery mockInstanceQuery = mock(JobQuery.class);
		when(mockInstanceQuery.jobId(MockProvider.EXAMPLE_JOB_ID)).thenReturn(
				mockInstanceQuery);
		when(mockInstanceQuery.singleResult()).thenReturn(mockInstance);
		when(mockManagementService.createJobQuery()).thenReturn(
				mockInstanceQuery);
		doThrow(new ProcessEngineException("No job found with id '" + MockProvider.EXAMPLE_NON_EXISTING_JOB_ID + "'."))
				.when(mockManagementService).setJobRetries(MockProvider.EXAMPLE_NON_EXISTING_JOB_ID,MockProvider.EXAMPLE_JOB_RETRIES);
		doThrow(new ProcessEngineException("The number of job retries must be a non-negative Integer, but '" + MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES
								+ "' has been provided.")).when(mockManagementService).setJobRetries(MockProvider.EXAMPLE_JOB_ID,MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
	}

	@Test
	public void testSetJobRetrys() {
		given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
				.pathParam("retries", MockProvider.EXAMPLE_JOB_RETRIES).then()
				.expect().statusCode(Status.NO_CONTENT.getStatusCode()).when()
				.post(CHANGE_JOB_RETRIES_URL);
	}

	@Test
	public void testSetJobRetrysNonExistentJob() {
		given().pathParam("id", MockProvider.EXAMPLE_NON_EXISTING_JOB_ID)
				.pathParam("retries", MockProvider.EXAMPLE_JOB_RETRIES).then()
				.expect()
				.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.when().post(CHANGE_JOB_RETRIES_URL);
	}

	@Test
	public void testSetJobRetrysNegativeRetries() {
		given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
				.pathParam("retries", MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES)
				.then().expect()
				.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.when().post(CHANGE_JOB_RETRIES_URL);
	}

}
