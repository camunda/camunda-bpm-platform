package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public class ProcessInstanceServiceTest extends AbstractRestServiceTest {
  
  private static final String EXAMPLE_BUSINESS_KEY = "aKey";
  private static final String EXAMPLE_ID = "anId";
  private static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcDefId";
  private static final boolean EXAMPLE_IS_SUSPENDED = false;
  private static final boolean EXAMPLE_IS_ENDED = false;

  private static final String PROCESS_INSTANCE_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance";
  private ProcessInstanceQuery mockedQuery;
  
  private ProcessInstanceQuery setUpMockInstanceQuery(List<ProcessInstance> mockedInstances) {
    ProcessInstanceQuery sampleInstanceQuery = mock(ProcessInstanceQuery.class);
    when(sampleInstanceQuery.list()).thenReturn(mockedInstances);
    when(processEngine.getRuntimeService().createProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    return sampleInstanceQuery;
  }
  
  private ProcessInstance createMockInstance() {
    ProcessInstance mockInstance = mock(ProcessInstance.class);
    when(mockInstance.getBusinessKey()).thenReturn(EXAMPLE_BUSINESS_KEY);
    when(mockInstance.getId()).thenReturn(EXAMPLE_ID);
    when(mockInstance.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mockInstance.getProcessInstanceId()).thenReturn(EXAMPLE_ID);
    when(mockInstance.isSuspended()).thenReturn(EXAMPLE_IS_SUSPENDED);
    when(mockInstance.isEnded()).thenReturn(EXAMPLE_IS_ENDED);
    return mockInstance;
  }
  
  private void injectMockedQuery(ProcessInstance mockedInstance) {
    List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
    instances.add(mockedInstance);
    mockedQuery = setUpMockInstanceQuery(instances);
  }
  
//  @Before
  public void setUpMockedQuery() {
    loadProcessEngineService();
    injectMockedQuery(createMockInstance());
  }
  
  @Test
  public void testInstanceRetrieval() {
    setUpMockedQuery();
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    
    String queryKey = "key";
    Response response = given().queryParam("processDefinitionKey", queryKey)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(PROCESS_INSTANCE_QUERY_URL);
    
    // assert query invocation
    inOrder.verify(mockedQuery).processDefinitionKey(queryKey);
    inOrder.verify(mockedQuery).list();
    
    String content = response.asString();
    System.out.println(content);
    List<String> definitions = from(content).getList("");
    Assert.assertEquals("There should be one process definition returned.", 1, definitions.size());
    Assert.assertNotNull("There should be one process definition returned", definitions.get(0));
    
    String returnedInstanceId = from(content).getString("[0].id");
    Boolean returnedIsEnded = from(content).getBoolean("[0].ended");
    String returnedDefinitionId = from(content).getString("[0].definitionId");
    String returnedBusinessKey = from(content).getString("[0].businessKey");
    Boolean returnedIsSuspended = from(content).getBoolean("[0].suspended");

    Assert.assertEquals(EXAMPLE_ID, returnedInstanceId);
    Assert.assertEquals(EXAMPLE_IS_ENDED, returnedIsEnded);
    Assert.assertEquals(EXAMPLE_PROCESS_DEFINITION_ID, returnedDefinitionId);
    Assert.assertEquals(EXAMPLE_BUSINESS_KEY, returnedBusinessKey);
    Assert.assertEquals(EXAMPLE_IS_SUSPENDED, returnedIsSuspended);
  }
  
}
