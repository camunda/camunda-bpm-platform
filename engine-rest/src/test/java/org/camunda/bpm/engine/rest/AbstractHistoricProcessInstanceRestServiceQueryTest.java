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
import static org.mockito.Matchers.any;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public abstract class AbstractHistoricProcessInstanceRestServiceQueryTest extends
		AbstractRestServiceTest {

	protected static final String HISTORIC_PROCESS_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH
			+ "/history/process-instance";
	protected static final String HISTORIC_PROCESS_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_PROCESS_INSTANCE_RESOURCE_URL
			+ "/count";
	
	
	protected HistoricProcessInstanceQuery mockQuery;	
	
	@Before
	public void setUpRuntimeData() {
	  mockQuery = setUpMockHistoricProcessInstanceQuery(MockProvider.createMockHistoricProcessInstances());
	}

	private HistoricProcessInstanceQuery setUpMockHistoricProcessInstanceQuery(List<HistoricProcessInstance> mockedHistoricProcessInstances) {
	  HistoricProcessInstanceQuery mockedhistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
	  when(mockedhistoricProcessInstanceQuery.list()).thenReturn(mockedHistoricProcessInstances);
	  when(mockedhistoricProcessInstanceQuery.count()).thenReturn((long) mockedHistoricProcessInstances.size());

	  when(processEngine.getHistoryService().createHistoricProcessInstanceQuery()).thenReturn(
				mockedhistoricProcessInstanceQuery);

	  return mockedhistoricProcessInstanceQuery;
	}		
   
	@Test
	public void testEmptyQuery() {
	  String queryKey = "";
	    given().queryParam("processDefinitionKey", queryKey)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
	}
	
	@Test
	public void testInvalidSortingOptions() {
	  executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
	  executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
	}

	protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
	  given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
	      .then().expect().statusCode(expectedStatus.getStatusCode())
	      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
	}

	@Test
	public void testSortByParameterOnly() {
	  given().queryParam("sortBy", "definitionId")
	      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
	      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
	}
	  
	@Test
	public void testSortOrderParameterOnly() {
	  given().queryParam("sortOrder", "asc")
	      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
	      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
	}

	@Test
	public void testNoParametersQuery() {
	  expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
	    
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
	  executeAndVerifySorting("startTime", "desc", Status.OK);
	  inOrder.verify(mockQuery).orderByProcessInstanceStartTime();
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
	      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
	    
	  verify(mockQuery).listPage(firstResult, maxResults);
	}

	@Test
	public void testMissingFirstResultParameter() {
	  int maxResults = 10;
	  given().queryParam("maxResults", maxResults)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
	    
	  verify(mockQuery).listPage(0, maxResults);
	}

	@Test
	public void testMissingMaxResultsParameter() {
	  int firstResult = 10;
	  given().queryParam("firstResult", firstResult)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
	    
	  verify(mockQuery).listPage(firstResult, Integer.MAX_VALUE);
	}

	@Test
	public void testQueryCount() {
	  expect().statusCode(Status.OK.getStatusCode())
	      .body("count", equalTo(1))
	      .when().get(HISTORIC_PROCESS_INSTANCE_COUNT_RESOURCE_URL);
	    
	  verify(mockQuery).count();
	}

	@Test
	public void testQueryCountForPost() {
	 given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
	     .expect()
	     .body("count", equalTo(1))
	      .when().post(HISTORIC_PROCESS_INSTANCE_COUNT_RESOURCE_URL);
	    
	  verify(mockQuery).count();
	}

    @Test
	public void testSimpleHistoricProcessQuery() {
	  String processInstanceId = MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;

	  Response response = given().queryParam("processInstanceId", processInstanceId).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

	  InOrder inOrder = inOrder(mockQuery);
	  inOrder.verify(mockQuery).processInstanceId(processInstanceId);
	  inOrder.verify(mockQuery).list();

	  String content = response.asString();
	  List<String> instances = from(content).getList("");
	  Assert.assertEquals("There should be one process instance returned.", 1,
				instances.size());
	  Assert.assertNotNull("The returned process instance should not be null.",
				instances.get(0));
		     
	  String returnedProcessInstanceId = from(content).getString("[0].id");
	  String returnedProcessInstanceBusinessKey = from(content).getString(
				"[0].businessKey");
	  String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
	  String returnedStartTime = from(content).getString("[0].startTime");
	  String returnedEndTime = from(content).getString("[0].endTime");
	  String returnedDurationInMillis = from(content).getString("[0].durationInMillis");

	  Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
	  Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY,
				returnedProcessInstanceBusinessKey);
	  Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID,
				returnedProcessDefinitionId);
	  Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_START_TIME.toString(),
				returnedStartTime);
	  Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_END_TIME.toString(),
				returnedEndTime);
	  Assert.assertEquals(MockProvider.EXAMPLE_HIST_PROCESS_DURATION_MILLIS_AS_STR,
				returnedDurationInMillis);
	}

    @Test
	public void testSimpleHistoricProcessQueryDeleted() {
	  String processDefinitionId = MockProvider.EXAMPLE_PROCESS_DEFINITION_ID;
	  Boolean deleted = true;

	  Response response = given().queryParam("processDefinitionId", processDefinitionId)
				.queryParam("deleted", deleted)
				.then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

	  InOrder inOrder = inOrder(mockQuery);
	  inOrder.verify(mockQuery).processDefinitionId(processDefinitionId);
	  inOrder.verify(mockQuery).deleted();
	  inOrder.verify(mockQuery).list();

	  String content = response.asString();
	  List<String> instances = from(content).getList("");
	  Assert.assertEquals("There should be one process instance returned.", 1,
				instances.size());
	  Assert.assertNotNull("The returned process instance should not be null.",
				instances.get(0));

	  String returnedProcessInstanceId = from(content).getString("[0].id");
	  String returnedProcessInstanceBusinessKey = from(content).getString(
				"[0].businessKey");
	  String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
	  String returnedDeleteReason = from(content).getString("[0].deleteReason");

	  Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
	  Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY,
				returnedProcessInstanceBusinessKey);
	  Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID,
				returnedProcessDefinitionId);
	  Assert.assertEquals(MockProvider.EXAMPLE_HIST_PROCESS_DELETE_REASON,
				returnedDeleteReason);
	}
    
	@Test
	public void testAdditionalParametersExcludingProcesses() {
	  Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

	  given().queryParams(stringQueryParameters).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

	  verify(mockQuery).processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
	  verify(mockQuery).processInstanceBusinessKey( MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
	  verify(mockQuery).processDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
	  verify(mockQuery).list();
	}

	private Map<String, String> getCompleteStringQueryParameters() {
	  Map<String, String> parameters = new HashMap<String, String>();

	  parameters.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
	  parameters.put("processInstanceBusinessKey", MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY.toString());
	  parameters.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);		
	  return parameters;
	}
	
    @Test
	public void testHistoricBeforeAndAfterStartTimeQuery() {
	  String processDefinitionId = MockProvider.EXAMPLE_PROCESS_DEFINITION_ID;
		
	  given()
			.queryParam("processDefinitionId", processDefinitionId)
			.queryParam("startedAfter", MockProvider.EXAMPLE_HISTORIC_PROCESS_START_AFTER)
			.queryParam("startedBefore", MockProvider.EXAMPLE_HISTORIC_PROCESS_START_BEFORE)
			.then()
			.expect()
			.statusCode(Status.OK.getStatusCode())
			.when()
			.get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

	  InOrder inOrder = inOrder(mockQuery);
	  inOrder.verify(mockQuery).processDefinitionId(processDefinitionId);
	  inOrder.verify(mockQuery).startedBefore(any(Date.class));
	  inOrder.verify(mockQuery).startedAfter(any(Date.class));				
	  inOrder.verify(mockQuery).list();
	}

    @Test
	public void testHistoricAfterAndBeforeFinishTimeQuery() {
	  String processDefinitionId = MockProvider.EXAMPLE_PROCESS_DEFINITION_ID;
		
	  given()
			.queryParam("processDefinitionId", processDefinitionId)
			.queryParam("finishedAfter", MockProvider.EXAMPLE_HISTORIC_PROCESS_FINISH_AFTER)
			.queryParam("finishedBefore", MockProvider.EXAMPLE_HISTORIC_PROCESS_FINISH_BEFORE)
			.then()
			.expect()
			.statusCode(Status.OK.getStatusCode())
			.when()
			.get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

	  InOrder inOrder = inOrder(mockQuery);
	  inOrder.verify(mockQuery).processDefinitionId(processDefinitionId);
	  inOrder.verify(mockQuery).finishedBefore(any(Date.class));
	  inOrder.verify(mockQuery).finishedAfter(any(Date.class));				
	  inOrder.verify(mockQuery).list();
	}    
    
    @Test
	public void testProcessQueryFinished() {
	  String processDefinitionId = MockProvider.EXAMPLE_PROCESS_DEFINITION_ID;
	  Boolean finished = true;

	  Response response = given().queryParam("processDefinitionId", processDefinitionId)
				.queryParam("finished", finished)
				.then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

	  InOrder inOrder = inOrder(mockQuery);
	  inOrder.verify(mockQuery).processDefinitionId(processDefinitionId);
	  inOrder.verify(mockQuery).finished();
	  inOrder.verify(mockQuery).list();

	  String content = response.asString();
	  List<String> instances = from(content).getList("");
	  Assert.assertEquals("There should be one process instance returned.", 1,
				instances.size());
	  Assert.assertNotNull("The returned process instance should not be null.",
				instances.get(0));

	  String returnedProcessInstanceId = from(content).getString("[0].id");
	  String returnedEndTime = from(content).getString("[0].endTime");

	  Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
	  Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_END_TIME.toString(),
				returnedEndTime);
	}
    
    @Test
	public void testProcessQueryUnFinished() {
      List<HistoricProcessInstance> mockedHistoricProcessInstances = MockProvider.createMockRunningHistoricProcessInstances();
  	  HistoricProcessInstanceQuery mockedhistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
  	  when(mockedhistoricProcessInstanceQuery.list()).thenReturn(mockedHistoricProcessInstances);
	  when(processEngine.getHistoryService().createHistoricProcessInstanceQuery()).thenReturn(
				mockedhistoricProcessInstanceQuery);
	  
	  String processDefinitionId = MockProvider.EXAMPLE_PROCESS_DEFINITION_ID;
	  Boolean finished = false;

	  Response response = given()
			    .queryParam("processDefinitionId", processDefinitionId)
				.queryParam("finished", finished)
				.then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

	  InOrder inOrder = inOrder(mockedhistoricProcessInstanceQuery);
	  inOrder.verify(mockedhistoricProcessInstanceQuery).processDefinitionId(processDefinitionId);
	  inOrder.verify(mockedhistoricProcessInstanceQuery).unfinished();
	  inOrder.verify(mockedhistoricProcessInstanceQuery).list();

	  String content = response.asString();
	  List<String> instances = from(content).getList("");
	  Assert.assertEquals("There should be one process instance returned.", 1,
				instances.size());
	  Assert.assertNotNull("The returned process instance should not be null.",
				instances.get(0));

	  String returnedProcessInstanceId = from(content).getString("[0].id");
	  String returnedEndTime = from(content).getString("[0].endTime");

	  Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
	  Assert.assertEquals(null,returnedEndTime);
	}
}
