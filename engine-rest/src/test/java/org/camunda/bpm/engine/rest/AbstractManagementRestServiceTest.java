package org.camunda.bpm.engine.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import static com.jayway.restassured.RestAssured.given;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
//import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractManagementRestServiceTest extends
		AbstractRestServiceTest {

	protected static final String SINGLE_MANAGEMENT_JOB_URL = TEST_RESOURCE_ROOT_PATH
			+ "/management/job/{id}";
	protected static final String CHANGE_JOB_RETRIES_URL = SINGLE_MANAGEMENT_JOB_URL
			+ "/retries/{retries}";	
	protected static final String MANAGEMENT_JOBS_URL = TEST_RESOURCE_ROOT_PATH	+ "/management/jobs";
	protected static final String MANAGEMENT_DELETE_JOBS_BY_PROCESSINSTANCE_URL = MANAGEMENT_JOBS_URL + "/process-instance/{id}";

	private ProcessEngine namedProcessEngine;
	private ManagementService mockManagementService;

	@Before
	public void setUpRuntimeData() {
		namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
		mockManagementService = mock(ManagementService.class);

		when(namedProcessEngine.getManagementService()).thenReturn(
				mockManagementService);
		createJobInstanceMock();
	}

	private void createJobInstanceMock() {
        List<Job> mockJobList = MockProvider.createMockJobList();
        JobQuery mockInstanceQuery;mockInstanceQuery = mock(JobQuery.class);

		when(mockInstanceQuery.jobId(MockProvider.EXAMPLE_JOB_ID)).thenReturn(
				mockInstanceQuery);		
		when(mockManagementService.createJobQuery()).thenReturn(mockInstanceQuery);
		when(mockInstanceQuery.processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(mockInstanceQuery);
		when(mockInstanceQuery.list()).thenReturn(mockJobList);
		doThrow(new ProcessEngineException("No process found with id '" + MockProvider.NON_EXISTING_PROCESS_INSTANCE_ID + "'.")).when(mockInstanceQuery).processInstanceId(MockProvider.NON_EXISTING_PROCESS_INSTANCE_ID);		
		doThrow(new ProcessEngineException("No job found with id '" + MockProvider.NON_EXISTING_JOB_ID + "'."))
				.when(mockManagementService).setJobRetries(MockProvider.NON_EXISTING_JOB_ID,MockProvider.EXAMPLE_JOB_RETRIES);
		doThrow(new ProcessEngineException("The number of job retries must be a non-negative Integer, but '" + MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES
								+ "' has been provided.")).when(mockManagementService).setJobRetries(MockProvider.EXAMPLE_JOB_ID,MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
	}

	@Test
	public void testSetJobRetrys() {
		given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
				.pathParam("retries", MockProvider.EXAMPLE_JOB_RETRIES).then()
				.expect().statusCode(Status.NO_CONTENT.getStatusCode()).when()
				.post(CHANGE_JOB_RETRIES_URL);
		
		verify(mockManagementService).setJobRetries( MockProvider.EXAMPLE_JOB_ID,  MockProvider.EXAMPLE_JOB_RETRIES);
	}

	@Test
	public void testSetJobRetrysNonExistentJob() {
		given().pathParam("id", MockProvider.NON_EXISTING_JOB_ID)
				.pathParam("retries", MockProvider.EXAMPLE_JOB_RETRIES).then()
				.expect()
				.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.when().post(CHANGE_JOB_RETRIES_URL);
		
		verify(mockManagementService).setJobRetries( MockProvider.NON_EXISTING_JOB_ID,  MockProvider.EXAMPLE_JOB_RETRIES);
	}

	@Test
	public void testSetJobRetrysNegativeRetries() {
		given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
				.pathParam("retries", MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES)
				.then().expect()
				.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.when().post(CHANGE_JOB_RETRIES_URL);
	
		verify(mockManagementService).setJobRetries( MockProvider.EXAMPLE_JOB_ID,  MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
	}
	
	@Test
	public void testDeleteJobs() {
		given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)				
				.then().expect()
				.statusCode(Status.NO_CONTENT.getStatusCode())
				.when().delete(MANAGEMENT_DELETE_JOBS_BY_PROCESSINSTANCE_URL);
		
		verify(mockManagementService.createJobQuery()).processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
		verify(mockManagementService).deleteJob(MockProvider.EXAMPLE_JOB_ID);
	}

	@Test
	public void testDeleteJobsNonExistentProcess() {
		given().pathParam("id", MockProvider.NON_EXISTING_PROCESS_INSTANCE_ID)				
				.then().expect()
				.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
				.when().delete(MANAGEMENT_DELETE_JOBS_BY_PROCESSINSTANCE_URL);		
			
		verify(mockManagementService.createJobQuery()).processInstanceId(MockProvider.NON_EXISTING_PROCESS_INSTANCE_ID);
	}

}
