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

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public abstract class AbstractHistoricVariableInstanceRestServiceQueryTest
		extends AbstractRestServiceTest {

	protected static final String HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH
			+ "/history/variable-instance";

	protected static final String HISTORIC_VARIABLE_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL
			+ "/count";

	protected HistoricVariableInstanceQuery mockQuery;	

	@Before
	public void setUpRuntimeData() {
	  mockQuery = setUpMockHistoricVariableInstanceQuery(MockProvider.createMockHistoricVariableInstances());
	}

	private HistoricVariableInstanceQuery setUpMockHistoricVariableInstanceQuery(List<HistoricVariableInstance> mockedHistoricVariableInstances) {
	  HistoricVariableInstanceQuery mockedhistoricVariableInstanceQuery = mock(HistoricVariableInstanceQuery.class);
	  when(mockedhistoricVariableInstanceQuery.list()).thenReturn(mockedHistoricVariableInstances);
	  when(mockedhistoricVariableInstanceQuery.count()).thenReturn((long) mockedHistoricVariableInstances.size());

	  when(processEngine.getHistoryService().createHistoricVariableInstanceQuery()).thenReturn(
				mockedhistoricVariableInstanceQuery);

	  return mockedhistoricVariableInstanceQuery;
	}

	@Test
	public void testEmptyQuery() {
	  String queryKey = "";
	  given().queryParam("processInstanceId", queryKey)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);
	}

	@Test
	public void testInvalidSortingOptions() {
	  executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
	  executeAndVerifySorting("processInstanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
	}

	protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
	  given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
	      .then().expect().statusCode(expectedStatus.getStatusCode())
	      .when().get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);
	}

	@Test
	public void testSortByParameterOnly() {
	  given().queryParam("sortBy", "processInstanceId")
	      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
	      .when().get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);
	}

	@Test
	public void testSortOrderParameterOnly() {
	  given().queryParam("sortOrder", "asc")
	      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
	      .when().get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);
	}

	@Test
	public void testNoParametersQuery() {
	  expect().statusCode(Status.OK.getStatusCode()).when().get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);

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
	  executeAndVerifySorting("variableName", "desc", Status.OK);
	  inOrder.verify(mockQuery).orderByVariableName();
	  inOrder.verify(mockQuery).desc();
	}

	@Test
	public void testSuccessfulPagination() {

	  int firstResult = 0;
	  int maxResults = 10;
	  given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);

	  verify(mockQuery).listPage(firstResult, maxResults);
	}

	@Test
	public void testMissingFirstResultParameter() {
	  int maxResults = 10;
	  given().queryParam("maxResults", maxResults)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);

	  verify(mockQuery).listPage(0, maxResults);
	}

	@Test
	public void testMissingMaxResultsParameter() {
	  int firstResult = 10;
	  given().queryParam("firstResult", firstResult)
	      .then().expect().statusCode(Status.OK.getStatusCode())
	      .when().get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);
	    
	  verify(mockQuery).listPage(firstResult, Integer.MAX_VALUE);
	}

	@Test
	public void testQueryCount() {
	  expect().statusCode(Status.OK.getStatusCode())
	      .body("count", equalTo(1))
	      .when().get(HISTORIC_VARIABLE_INSTANCE_COUNT_RESOURCE_URL);

	  verify(mockQuery).count();
	}

	@Test
	public void testQueryCountForPost() {
	 given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
	     .expect()
	     .body("count", equalTo(1))
	      .when().post(HISTORIC_VARIABLE_INSTANCE_COUNT_RESOURCE_URL);

	  verify(mockQuery).count();
	}

	@Test
	public void testSimpleHistoricVariableQuery() {
		String processInstanceId = MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;

		Response response = given().queryParam("processInstanceId", processInstanceId).then().expect()
				.statusCode(Status.OK.getStatusCode())				
				.when()
				.get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);

		InOrder inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).processInstanceId(processInstanceId);
		inOrder.verify(mockQuery).list();

		String content = response.asString();
		List<String> instances = from(content).getList("");
		Assert.assertEquals("There should be one variable instance returned.", 1,
				instances.size());
		Assert.assertNotNull("The returned variable instance should not be null.",
				instances.get(0));

		String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
		String returnedVariableName = from(content).getString(
				"[0].variableName");
		String returnedVariableValue = from(content).getString("[0].value");


		Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID, returnedProcessInstanceId);
		Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME,
				returnedVariableName);
		Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_VALUE,
				returnedVariableValue);
	}

	@Test
	public void testAdditionalParametersExcludingVariables() {
		Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

		given().queryParams(stringQueryParameters).then().expect()
				.statusCode(Status.OK.getStatusCode()).when()
				.get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);

		verifyStringParameterQueryInvocations();
		verify(mockQuery).list();
	}

	private Map<String, String> getCompleteStringQueryParameters() {
		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("processInstanceId", MockProvider.EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID);
		parameters.put("variableName",
				MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME);
		parameters.put("variableValue", MockProvider.EXAMPLE_VARIABLE_INSTANCE_VALUE);	
		return parameters;
	}

	private void verifyStringParameterQueryInvocations() {
		Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

		verify(mockQuery).processInstanceId(stringQueryParameters.get("processInstanceId"));
		verify(mockQuery).variableName(
				stringQueryParameters.get("variableName"));
		verify(mockQuery).variableValueEquals(stringQueryParameters.get("variableName"),stringQueryParameters.get("variableValue"));	
	}
	
	@Test
	public void testVariableNameAndValueQuery() {
		String variableName = MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME;
        String variableValue = MockProvider.EXAMPLE_VARIABLE_INSTANCE_VALUE;
		
		Response response = given()
				.queryParam("variableName", variableName)
				.queryParam("variableValue", variableValue)
				.then().expect()
				.statusCode(Status.OK.getStatusCode())				
				.when()
				.get(HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL);

		InOrder inOrder = inOrder(mockQuery);		
		inOrder.verify(mockQuery).variableValueEquals(variableName, variableValue);
		inOrder.verify(mockQuery).list();

		String content = response.asString();
		List<String> instances = from(content).getList("");
		Assert.assertEquals("There should be one variable instance returned.", 1,
				instances.size());
		Assert.assertNotNull("The returned variable instance should not be null.",
				instances.get(0));

		String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
		String returnedVariableName = from(content).getString(
				"[0].variableName");
		String returnedVariableValue = from(content).getString("[0].value");


		Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID, returnedProcessInstanceId);
		Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME,
				returnedVariableName);
		Assert.assertEquals(MockProvider.EXAMPLE_VARIABLE_INSTANCE_VALUE,
				returnedVariableValue);
	}

}
