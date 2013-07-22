package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

import com.jayway.restassured.response.Response;

public abstract class AbstractHistoricActivityInstanceRestServiceQueryTest
		extends AbstractRestServiceTest {

	protected static final String HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH
			+ "/historic-activity-instance";
	
	HistoricActivityInstanceQuery mockQuery;
	
	
	@Before
	public void setUpRuntimeData() {
		mockQuery = setUpMockHistoricActivityInstanceQuery(MockProvider.createMockHistoricActivityInstances());
	}

	private HistoricActivityInstanceQuery setUpMockHistoricActivityInstanceQuery(List<HistoricActivityInstance> mockedHistoricActivityInstances) {
		HistoricActivityInstanceQuery mockedhistoricActivityInstanceQuery = mock(HistoricActivityInstanceQuery.class);
		when(mockedhistoricActivityInstanceQuery.list()).thenReturn(mockedHistoricActivityInstances);		

		when(processEngine.getHistoryService().createHistoricActivityInstanceQuery()).thenReturn(
				mockedhistoricActivityInstanceQuery);

		return mockedhistoricActivityInstanceQuery;
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
	
}
