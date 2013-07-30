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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public abstract class AbstractHistoricActivityInstanceRestServiceQueryTest
		extends AbstractRestServiceTest {

	protected static final String HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH
			+ "/history/activity-instance";

	protected static final String HISTORIC_ACTIVITY_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL
			+ "/count";
	
	protected HistoricActivityInstanceQuery mockQuery;	
	
	@Before
	public void setUpRuntimeData() {
	  mockQuery = setUpMockHistoricActivityInstanceQuery(MockProvider.createMockHistoricActivityInstances());
	}

	private HistoricActivityInstanceQuery setUpMockHistoricActivityInstanceQuery(List<HistoricActivityInstance> mockedHistoricActivityInstances) {
	  HistoricActivityInstanceQuery mockedhistoricActivityInstanceQuery = mock(HistoricActivityInstanceQuery.class);
	  when(mockedhistoricActivityInstanceQuery.list()).thenReturn(mockedHistoricActivityInstances);
	  when(mockedhistoricActivityInstanceQuery.count()).thenReturn((long) mockedHistoricActivityInstances.size());
	  
	  when(processEngine.getHistoryService().createHistoricActivityInstanceQuery()).thenReturn(
				mockedhistoricActivityInstanceQuery);

	  return mockedhistoricActivityInstanceQuery;
	}

	@Test
	public void testEmptyQuery() {
	  String queryKey = "";
	   given().queryParam("processInstanceId", queryKey)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
	}
	
	@Test
	public void testInvalidSortingOptions() {
	  executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
	  executeAndVerifySorting("instanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
	}

	protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
	  given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
	      .then().expect().statusCode(expectedStatus.getStatusCode())
	      .when().get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
	}

	@Test
	public void testSortByParameterOnly() {
	  given().queryParam("sortBy", "instanceId")
	      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
	      .when().get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
	}
	  
	@Test
	public void testSortOrderParameterOnly() {
	  given().queryParam("sortOrder", "asc")
	      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
	      .when().get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
	}

	@Test
	public void testNoParametersQuery() {
	  expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
	    
	  verify(mockQuery).list();
	  verifyNoMoreInteractions(mockQuery);
	}	  
	  
	@Test
	public void testSortingParameters() {
	  InOrder inOrder = Mockito.inOrder(mockQuery);
	  executeAndVerifySorting("instanceId", "asc", Status.OK);
	  inOrder.verify(mockQuery).orderByProcessInstanceId();
      inOrder.verify(mockQuery).asc();
  
	  inOrder = Mockito.inOrder(mockQuery);
	  executeAndVerifySorting("activityId", "desc", Status.OK);
	  inOrder.verify(mockQuery).orderByActivityId();
	  inOrder.verify(mockQuery).desc();
	  
	  inOrder = Mockito.inOrder(mockQuery);
	  executeAndVerifySorting("definitionId", "asc", Status.OK);
	  inOrder.verify(mockQuery).orderByProcessDefinitionId();
	  inOrder.verify(mockQuery).asc();
	}

	@Test
	public void testSuccessfulPagination() {
	    
	  int firstResult = 0;
	  int maxResults = 10;
	  given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
	    
	  verify(mockQuery).listPage(firstResult, maxResults);
	}

	@Test
	public void testMissingFirstResultParameter() {
	  int maxResults = 10;
	  given().queryParam("maxResults", maxResults)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
	    
	  verify(mockQuery).listPage(0, maxResults);
	}

	@Test
	public void testMissingMaxResultsParameter() {
	  int firstResult = 10;
	  given().queryParam("firstResult", firstResult)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
	    
	  verify(mockQuery).listPage(firstResult, Integer.MAX_VALUE);
	}

	@Test
	public void testQueryCount() {
	  expect().statusCode(Status.OK.getStatusCode())
	      .body("count", equalTo(1))
	      .when().get(HISTORIC_ACTIVITY_INSTANCE_COUNT_RESOURCE_URL);

	  verify(mockQuery).count();
	}

	@Test
	public void testQueryCountForPost() {
	 given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
	     .expect()
	     .body("count", equalTo(1))
	      .when().post(HISTORIC_ACTIVITY_INSTANCE_COUNT_RESOURCE_URL);

	  verify(mockQuery).count();
	}	

	@Test
	public void testSimpleHistoricActivityQuery() {
		String processInstanceId = MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;

		Response response = given().queryParam("processInstanceId", processInstanceId).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

		InOrder inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).processInstanceId(processInstanceId);
		inOrder.verify(mockQuery).list();

		String content = response.asString();
		List<String> instances = from(content).getList("");
		Assert.assertEquals("There should be one activity instance returned.", 1,
				instances.size());
		Assert.assertNotNull("The returned activity instance should not be null.",
				instances.get(0));

		String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
		String returnedProcessDefinitionId = from(content).getString(
				"[0].processDefinitionId");
		String returnedActivityName = from(content).getString("[0].activityName");
		String returnedActivityId = from(content).getString(
				"[0].activityId");

		Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
		Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID,
				returnedProcessDefinitionId);
		Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_NAME,
				returnedActivityName);
		Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_ID,
				returnedActivityId);
	}	

	@Test
	public void testAdditionalParametersExcludingVariables() {
		Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

		given().queryParams(stringQueryParameters).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

		verifyStringParameterQueryInvocations();
		verify(mockQuery).list();
	}

	private Map<String, String> getCompleteStringQueryParameters() {
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
		parameters.put("processDefinitionId",
				MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
		parameters.put("activityName", MockProvider.EXAMPLE_ACTIVITY_NAME);
		parameters.put("activityId",
				MockProvider.EXAMPLE_ACTIVITY_ID);
		return parameters;
	}

	private void verifyStringParameterQueryInvocations() {
		Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

		verify(mockQuery).processInstanceId(stringQueryParameters.get("processInstanceId"));
		verify(mockQuery).processDefinitionId(
				stringQueryParameters.get("processDefinitionId"));
		verify(mockQuery).activityId(stringQueryParameters.get("activityId"));
		verify(mockQuery).activityName(
				stringQueryParameters.get("activityName"));
	}
	
	@Test
	public void testFinishedHistoricActivityQuery() {
		String processDefinitionId = MockProvider.EXAMPLE_PROCESS_DEFINITION_ID;
		
		Response response = given()
		        .queryParam("finished", true)
				.queryParam("processDefinitionId", processDefinitionId)
				.then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

		InOrder inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).processDefinitionId(processDefinitionId);
		inOrder.verify(mockQuery).finished();
		inOrder.verify(mockQuery).list();

		String content = response.asString();
		List<String> instances = from(content).getList("");
		Assert.assertEquals("There should be one activity instance returned.", 1,
				instances.size());
		Assert.assertNotNull("The returned activity instance should not be null.",
				instances.get(0));

		String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
		String returnedProcessDefinitionId = from(content).getString(
				"[0].processDefinitionId");
		String returnedActivityEndTime = from(content).getString("[0].endTime");

		Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
		Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID,
				returnedProcessDefinitionId);
		Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_END_TIME.toString(),
				returnedActivityEndTime);
	}
	
	@Test
	public void testUnFinishedHistoricActivityQuery() {
		List<HistoricActivityInstance> mockedHistoricActivityInstances = MockProvider.createMockRunningHistoricActivityInstances();
	    HistoricActivityInstanceQuery mockedhistoricActivityInstanceQuery = mock(HistoricActivityInstanceQuery.class);
		when(mockedhistoricActivityInstanceQuery.list()).thenReturn(mockedHistoricActivityInstances);	  
		when(processEngine.getHistoryService().createHistoricActivityInstanceQuery()).thenReturn(
						mockedhistoricActivityInstanceQuery);		
		
		String processDefinitionId = MockProvider.EXAMPLE_PROCESS_DEFINITION_ID;
		
		Response response = given()
				.queryParam("processDefinitionId", processDefinitionId)
		        .queryParam("finished", false)				
				.then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

		InOrder inOrder = inOrder(mockedhistoricActivityInstanceQuery);
		inOrder.verify(mockedhistoricActivityInstanceQuery).processDefinitionId(processDefinitionId);
		inOrder.verify(mockedhistoricActivityInstanceQuery).unfinished();
		inOrder.verify(mockedhistoricActivityInstanceQuery).list();

		String content = response.asString();
		List<String> instances = from(content).getList("");
		Assert.assertEquals("There should be one activity instance returned.", 1,
				instances.size());
		Assert.assertNotNull("The returned activity instance should not be null.",
				instances.get(0));

		String returnedProcessDefinitionId = from(content).getString(
				"[0].processDefinitionId");
		String returnedActivityEndTime = from(content).getString("[0].endTime");

		Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID,
				returnedProcessDefinitionId);
		Assert.assertEquals(null,returnedActivityEndTime);
	}	
}
