package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.mockito.Matchers;

public class ProcessDefinitionServiceInteractionTest extends
    AbstractRestServiceTest {

  private static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcessDefinitionId";
  private static final String EXAMPLE_INSTANCE_ID = "anId";
  
  private static final String PROCESS_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition";
  private static final String START_PROCESS_INSTANCE_URL = PROCESS_DEFINITION_URL + "/{id}/start";
  
  private ProcessInstance mockInstance;
  
  public void setupMockInstance() {
    mockInstance = createMockInstance();
    RuntimeService runtimeServiceMock = processEngine.getRuntimeService();
    when(runtimeServiceMock.startProcessInstanceById(EXAMPLE_PROCESS_DEFINITION_ID, Matchers.<Map<String, Object>>any())).thenReturn(mockInstance);
  }
  
  private ProcessInstance createMockInstance() {
    ProcessInstance mock = mock(ProcessInstance.class);
    
    when(mock.getId()).thenReturn(EXAMPLE_INSTANCE_ID);
    
    return mock;
  }
  
  @Test
  public void testSimpleProcessInstantiation() {
    given().pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(EXAMPLE_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);
  }
  
  public void testProcessInstantiationWithParameters() {
    // TODO implement
  }
}
