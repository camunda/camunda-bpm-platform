package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractProcessDefinitionRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String SINGLE_PROCESS_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/{id}";
  protected static final String START_PROCESS_INSTANCE_URL = SINGLE_PROCESS_DEFINITION_URL + "/start";
  protected static final String XML_DEFINITION_URL = SINGLE_PROCESS_DEFINITION_URL + "/xml";
  protected static final String START_FORM_URL = SINGLE_PROCESS_DEFINITION_URL + "/startForm";
  private RuntimeService runtimeServiceMock;
  private RepositoryService repositoryServiceMock;
  private FormService formServiceMock;
  

  @Before
  public void setUpRuntimeData() {
    ProcessInstance mockInstance = MockProvider.createMockInstance();
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    
    // we replace this mock with every test in order to have a clean one (in terms of invocations) for verification
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
    when(runtimeServiceMock.startProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), Matchers.<Map<String, Object>>any())).thenReturn(mockInstance);
    
    repositoryServiceMock = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);
    when(repositoryServiceMock.getProcessDefinition(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(mockDefinition);
    when(repositoryServiceMock.getProcessModel(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(createMockProcessDefinionBpmn20Xml());
    
    StartFormData formDataMock = MockProvider.createMockStartFormData(mockDefinition);
    formServiceMock = mock(FormService.class);
    when(processEngine.getFormService()).thenReturn(formServiceMock);
    when(formServiceMock.getStartFormData(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(formDataMock);
  }

  private InputStream createMockProcessDefinionBpmn20Xml() {
    // do not close the input stream, will be done in implementation
    InputStream bpmn20XmlIn = null;
    bpmn20XmlIn = ReflectUtil.getResourceAsStream("processes/fox-invoice_en_long_id.bpmn");
    Assert.assertNotNull(bpmn20XmlIn);
    return bpmn20XmlIn;
  }

  @Test
  public void testInstanceResourceLinkResult() {
    String fullInstanceUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/process-instance/" + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("links[0].href", equalTo(fullInstanceUrl))
      .when().post(START_PROCESS_INSTANCE_URL);
  }
  

  @Test
  public void testInstanceResourceLinkWithEnginePrefix() {
    String startInstanceOnExplicitEngineUrl = TEST_RESOURCE_ROOT_PATH + "/engine/default/process-definition/{id}/start";
    
    String fullInstanceUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/engine/default/process-instance/" + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("links[0].href", equalTo(fullInstanceUrl))
      .when().post(startInstanceOnExplicitEngineUrl);
  }

  @Test
  public void testProcessDefinitionBpmn20XmlRetrieval() {
    // Rest-assured has problems with extracting json with escaped quotation marks, i.e. the xml content in our case
    Response response = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
//      .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
//      .body("bpmn20Xml", startsWith("<?xml"))
    .when().get(XML_DEFINITION_URL);

    String responseContent = response.asString();
    Assert.assertTrue(responseContent.contains(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    Assert.assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testGetStartFormData() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
    .when().get(START_FORM_URL);
  }
  
  @Test
  public void testSimpleProcessInstantiation() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testProcessInstantiationWithParameters() throws IOException {
    Map<String, Object> parameters = VariablesBuilder.create()
        .variable("aBoolean", Boolean.TRUE)
        .variable("aString", "aStringVariableValue")
        .variable("anInteger", 42).getVariables();
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", parameters);
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);
    
    Map<String, Object> expectedParameters = new HashMap<String, Object>();
    expectedParameters.put("aBoolean", Boolean.TRUE);
    expectedParameters.put("aString", "aStringVariableValue");
    expectedParameters.put("anInteger", 42);
    
    verify(runtimeServiceMock).startProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), argThat(new EqualsMap(expectedParameters)));
    
  }

  /**
   * {@link RuntimeService#startProcessInstanceById(String, Map)} throws an {@link ProcessEngineException}, if a definition with the given id does not exist.
   */
  @Test
  public void testUnsuccessfulInstantiation() {
    when(runtimeServiceMock.startProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), Matchers.<Map<String, Object>>any()))
      .thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(RestException.class.getSimpleName()))
        .body("message", containsString("Cannot instantiate process definition"))
      .when().post(START_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDefinitionRetrieval() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("key", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
      .body("category", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_CATEGORY))
      .body("name", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME))
      .body("description", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_DESCRIPTION))
      .body("deploymentId", equalTo(MockProvider.EXAMPLE_DEPLOYMENT_ID))
      .body("version", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_VERSION))
      .body("resource", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME))
      .body("diagram", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME))
      .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED))
    .when().get(SINGLE_PROCESS_DEFINITION_URL);
    
    verify(repositoryServiceMock).getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testNonExistingProcessDefinitionRetrieval() {
    String nonExistingId = "aNonExistingDefinitionId";
    when(repositoryServiceMock.getProcessDefinition(eq(nonExistingId))).thenThrow(new ProcessEngineException("no matching definition"));
    
    given().pathParam("id", "aNonExistingDefinitionId")
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("No matching definition with id " + nonExistingId))
    .when().get(SINGLE_PROCESS_DEFINITION_URL);
  }

  @Test
  public void testNonExistingProcessDefinitionBpmn20XmlRetrieval() {
    String nonExistingId = "aNonExistingDefinitionId";
    when(repositoryServiceMock.getProcessModel(eq(nonExistingId))).thenThrow(new ProcessEngineException("no matching process definition found."));
    
    given().pathParam("id", nonExistingId)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("No matching definition with id " + nonExistingId))
    .when().get(XML_DEFINITION_URL);
  }

  @Test
  public void testGetStartFormDataForNonExistingProcessDefinition() {
    when(formServiceMock.getStartFormData(anyString())).thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", "aNonExistingProcessDefinitionId")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot get start form data for process definition"))
    .when().get(START_FORM_URL);
  }
  
  @Test
  public void testUnparseableIntegerVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId due to number format exception: For input string: \"1abc\""))
    .when().post(START_PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testUnparseableShortVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId due to number format exception: For input string: \"1abc\""))
    .when().post(START_PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testUnparseableLongVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId due to number format exception: For input string: \"1abc\""))
    .when().post(START_PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testUnparseableDoubleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId due to number format exception: For input string: \"1abc\""))
    .when().post(START_PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testUnparseableDateVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId due to parse exception: Unparseable date: \"1abc\""))
    .when().post(START_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testNotSupportedTypeVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: The variable type 'X' is not supported."))
    .when().post(START_PROCESS_INSTANCE_URL);
  }

}
