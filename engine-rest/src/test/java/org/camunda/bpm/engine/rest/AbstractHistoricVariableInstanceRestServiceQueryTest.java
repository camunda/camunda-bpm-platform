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

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.response.Response;

public abstract class AbstractHistoricVariableInstanceRestServiceQueryTest
		extends AbstractRestServiceTest {

	protected static final String HISTORIC_VARIABLE_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH
			+ "/historic-variable-instance";
	
	HistoricVariableInstanceQuery mockQuery;
	
	
	@Before
	public void setUpRuntimeData() {
		mockQuery = setUpMockHistoricVariableInstanceQuery(MockProvider.createMockHistoricVariableInstances());
	}

	private HistoricVariableInstanceQuery setUpMockHistoricVariableInstanceQuery(List<HistoricVariableInstance> mockedHistoricVariableInstances) {
		HistoricVariableInstanceQuery mockedhistoricVariableInstanceQuery = mock(HistoricVariableInstanceQuery.class);
		when(mockedhistoricVariableInstanceQuery.list()).thenReturn(mockedHistoricVariableInstances);		

		when(processEngine.getHistoryService().createHistoricVariableInstanceQuery()).thenReturn(
				mockedhistoricVariableInstanceQuery);

		return mockedhistoricVariableInstanceQuery;
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
}
