package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

import com.jayway.restassured.response.Response;

public abstract class AbstractHistoricProcessInstanceRestServiceQueryTest extends
		AbstractRestServiceTest {

	protected static final String HISTORIC_PROCESS_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH
			+ "/historic-process-instance";
	
	HistoricProcessInstanceQuery mockQuery;	
	
	@Before
	public void setUpRuntimeData() {
		mockQuery = setUpMockHistoricProcessInstanceQuery(MockProvider.createMockHistoricProcessInstances());
	}

	private HistoricProcessInstanceQuery setUpMockHistoricProcessInstanceQuery(List<HistoricProcessInstance> mockedHistoricProcessInstances) {
		HistoricProcessInstanceQuery mockedhistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
		when(mockedhistoricProcessInstanceQuery.list()).thenReturn(mockedHistoricProcessInstances);		

		when(processEngine.getHistoryService().createHistoricProcessInstanceQuery()).thenReturn(
				mockedhistoricProcessInstanceQuery);

		return mockedhistoricProcessInstanceQuery;
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
	public void testHistoricStartAndEndTimeQuery() {
		String processInstanceId = MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;
		
		given()
			.queryParam("processInstanceId", processInstanceId)
			.queryParam("startedAfter", MockProvider.EXAMPLE_HISTORIC_PROCESS_START_AFTER.toString())
			.queryParam("startedBefore", MockProvider.EXAMPLE_HISTORIC_PROCESS_START_BEFORE.toString())
			.then()
			.expect()
			.statusCode(Status.OK.getStatusCode())
			.when()
			.get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

		InOrder inOrder = inOrder(mockQuery);
		inOrder.verify(mockQuery).processInstanceId(processInstanceId);
		inOrder.verify(mockQuery).startedBefore(any(Date.class));
		inOrder.verify(mockQuery).startedAfter(any(Date.class));				
		inOrder.verify(mockQuery).list();
	}
}
