/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.repository.CalledProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.CalledProcessDefinition;
import org.camunda.bpm.engine.repository.DeleteProcessDefinitionsBuilder;
import org.camunda.bpm.engine.repository.DeleteProcessDefinitionsSelectBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.HistoryTimeToLiveDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.camunda.bpm.engine.rest.helper.*;
import org.camunda.bpm.engine.rest.helper.variable.EqualsObjectValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsUntypedValue;
import org.camunda.bpm.engine.rest.sub.repository.impl.ProcessDefinitionResourceImpl;
import org.camunda.bpm.engine.rest.util.EncodingUtil;
import org.camunda.bpm.engine.rest.util.ModificationInstructionBuilder;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockSerializedVariables;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.*;

public class ProcessDefinitionRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String PROCESS_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition";
  protected static final String SINGLE_PROCESS_DEFINITION_URL = PROCESS_DEFINITION_URL + "/{id}";
  protected static final String SINGLE_PROCESS_DEFINITION_BY_KEY_URL = PROCESS_DEFINITION_URL + "/key/{key}";
  protected static final String SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_URL = PROCESS_DEFINITION_URL + "/key/{key}/tenant-id/{tenant-id}";

  protected static final String START_PROCESS_INSTANCE_URL = SINGLE_PROCESS_DEFINITION_URL + "/start";
  protected static final String START_PROCESS_INSTANCE_BY_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/start";
  protected static final String START_PROCESS_INSTANCE_BY_KEY_AND_TENANT_ID_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_URL + "/start";

  protected static final String XML_DEFINITION_URL = SINGLE_PROCESS_DEFINITION_URL + "/xml";
  protected static final String XML_DEFINITION_BY_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/xml";
  protected static final String DIAGRAM_DEFINITION_URL = SINGLE_PROCESS_DEFINITION_URL + "/diagram";
  protected static final String DIAGRAM_DEFINITION_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/diagram";

  protected static final String START_FORM_URL = SINGLE_PROCESS_DEFINITION_URL + "/startForm";
  protected static final String START_FORM_BY_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/startForm";
  protected static final String DEPLOYED_START_FORM_URL = SINGLE_PROCESS_DEFINITION_URL + "/deployed-start-form";
  protected static final String DEPLOYED_START_FORM_BY_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/deployed-start-form";
  protected static final String RENDERED_FORM_URL = SINGLE_PROCESS_DEFINITION_URL + "/rendered-form";
  protected static final String RENDERED_FORM_BY_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/rendered-form";
  protected static final String SUBMIT_FORM_URL = SINGLE_PROCESS_DEFINITION_URL + "/submit-form";
  protected static final String SUBMIT_FORM_BY_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/submit-form";
  protected static final String START_FORM_VARIABLES_URL = SINGLE_PROCESS_DEFINITION_URL + "/form-variables";
  protected static final String START_FORM_VARIABLES_BY_KEY_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/form-variables";

  protected static final String SINGLE_PROCESS_DEFINITION_SUSPENDED_URL = SINGLE_PROCESS_DEFINITION_URL + "/suspended";
  protected static final String SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/suspended";
  protected static final String SINGLE_PROCESS_DEFINITION_HISTORY_TIMETOLIVE_URL = SINGLE_PROCESS_DEFINITION_URL + "/history-time-to-live";
  protected static final String PROCESS_DEFINITION_SUSPENDED_URL = PROCESS_DEFINITION_URL + "/suspended";
  protected static final String SINGLE_PROCESS_DEFINITION_BY_KEY_DELETE_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_URL + "/delete";
  protected static final String SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_DELETE_URL = SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_URL + "/delete";

  protected static final String PROCESS_DEFINITION_CALL_ACTIVITY_MAPPINGS = SINGLE_PROCESS_DEFINITION_URL + "/static-called-process-definitions";

  private RuntimeService runtimeServiceMock;
  private RepositoryService repositoryServiceMock;
  private FormService formServiceMock;
  private ManagementService managementServiceMock;
  private ProcessDefinitionQuery processDefinitionQueryMock;
  private ProcessInstanceWithVariables mockInstance;
  private ProcessInstantiationBuilder mockInstantiationBuilder;

  @Before
  public void setUpRuntimeData() {
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    setUpRuntimeDataForDefinition(mockDefinition);

    managementServiceMock = mock(ManagementService.class);
    when(processEngine.getManagementService()).thenReturn(managementServiceMock);
    when(managementServiceMock.getProcessApplicationForDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID)).thenReturn(MockProvider.EXAMPLE_PROCESS_APPLICATION_NAME);

    // replace the runtime container delegate & process application service with a mock

    ProcessApplicationService processApplicationService = mock(ProcessApplicationService.class);
    ProcessApplicationInfo appMock = MockProvider.createMockProcessApplicationInfo();
    when(processApplicationService.getProcessApplicationInfo(MockProvider.EXAMPLE_PROCESS_APPLICATION_NAME)).thenReturn(appMock);

    RuntimeContainerDelegate delegate = mock(RuntimeContainerDelegate.class);
    when(delegate.getProcessApplicationService()).thenReturn(processApplicationService);
    RuntimeContainerDelegate.INSTANCE.set(delegate);
  }

  private void setUpRuntimeDataForDefinition(ProcessDefinition mockDefinition) {
    mockInstance = MockProvider.createMockInstanceWithVariables();

    // we replace this mock with every test in order to have a clean one (in terms of invocations) for verification
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
    when(runtimeServiceMock.startProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), Mockito.<Map<String, Object>>any())).thenReturn(mockInstance);
    when(runtimeServiceMock.startProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), any(), any(), any())).thenReturn(mockInstance);


    mockInstantiationBuilder = setUpMockInstantiationBuilder();
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean())).thenReturn(mockInstance);
    when(runtimeServiceMock.createProcessInstanceById(any())).thenReturn(mockInstantiationBuilder);


    repositoryServiceMock = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);
    when(repositoryServiceMock.getProcessDefinition(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(mockDefinition);
    when(repositoryServiceMock.getProcessModel(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(createMockProcessDefinionBpmn20Xml());

    DeleteProcessDefinitionsSelectBuilder deleteProcessDefinitionsSelectBuilder = mock(DeleteProcessDefinitionsSelectBuilder.class, RETURNS_DEEP_STUBS);
    when(repositoryServiceMock.deleteProcessDefinitions()).thenReturn(deleteProcessDefinitionsSelectBuilder);

    setUpMockDefinitionQuery(mockDefinition);

    StartFormData formDataMock = MockProvider.createMockStartFormData(mockDefinition);
    formServiceMock = mock(FormService.class);
    when(processEngine.getFormService()).thenReturn(formServiceMock);
    when(formServiceMock.getStartFormData(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(formDataMock);
    when(formServiceMock.getStartFormKey(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(MockProvider.EXAMPLE_FORM_KEY);
    when(formServiceMock.submitStartForm(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), any())).thenReturn(mockInstance);
    when(formServiceMock.submitStartForm(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), any(), any())).thenReturn(mockInstance);

    VariableMap startFormVariablesMock = MockProvider.createMockFormVariables();
    when(formServiceMock.getStartFormVariables(eq(EXAMPLE_PROCESS_DEFINITION_ID), any(), anyBoolean())).thenReturn(startFormVariablesMock);

  }

  private InputStream createMockProcessDefinionBpmn20Xml() {
    // do not close the input stream, will be done in implementation
    InputStream bpmn20XmlIn = null;
    bpmn20XmlIn = ReflectUtil.getResourceAsStream("processes/fox-invoice_en_long_id.bpmn");
    Assert.assertNotNull(bpmn20XmlIn);
    return bpmn20XmlIn;
  }

  private void setUpMockDefinitionQuery(ProcessDefinition mockDefinition) {
    processDefinitionQueryMock = mock(ProcessDefinitionQuery.class);
    when(processDefinitionQueryMock.processDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.processDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.tenantIdIn(anyString())).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.withoutTenantId()).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.latestVersion()).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.singleResult()).thenReturn(mockDefinition);
    when(processDefinitionQueryMock.count()).thenReturn(1L);
    when(processDefinitionQueryMock.list()).thenReturn(Collections.singletonList(mockDefinition));
    when(repositoryServiceMock.createProcessDefinitionQuery()).thenReturn(processDefinitionQueryMock);
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
  public void testProcessDiagramRetrieval() throws FileNotFoundException, URISyntaxException {
    // setup additional mock behavior
    File file = getFile("/processes/todo-process.png");
    when(repositoryServiceMock.getProcessDiagram(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .thenReturn(new FileInputStream(file));

    // call method
    byte[] actual = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/png")
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME)
        .when().get(DIAGRAM_DEFINITION_URL).getBody().asByteArray();

    // verify service interaction
    verify(repositoryServiceMock).getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(repositoryServiceMock).getProcessDiagram(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    // compare input stream with response body bytes
    byte[] expected = IoUtil.readInputStream(new FileInputStream(file), "process diagram");
    Assert.assertArrayEquals(expected, actual);
  }

  @Test
  public void testProcessDiagramNullFilename() throws FileNotFoundException, URISyntaxException {
    // setup additional mock behavior
    File file = getFile("/processes/todo-process.png");
    when(repositoryServiceMock.getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID).getDiagramResourceName())
      .thenReturn(null);
    when(repositoryServiceMock.getProcessDiagram(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .thenReturn(new FileInputStream(file));

    // call method
    byte[] actual = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType("application/octet-stream")
      .header("Content-Disposition", "attachment; " +
              "filename=\"" + null + "\"; " +
              "filename*=UTF-8''" + null)
      .when().get(DIAGRAM_DEFINITION_URL).getBody().asByteArray();

    // verify service interaction
    verify(repositoryServiceMock).getProcessDiagram(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    // compare input stream with response body bytes
    byte[] expected = IoUtil.readInputStream(new FileInputStream(file), "process diagram");
    Assert.assertArrayEquals(expected, actual);
  }

  @Test
  public void testProcessDiagramNotExist() {
    // setup additional mock behavior
    when(repositoryServiceMock.getProcessDiagram(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenReturn(null);

    // call method
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
        .expect().statusCode(Status.NO_CONTENT.getStatusCode())
        .when().get(DIAGRAM_DEFINITION_URL);

    // verify service interaction
    verify(repositoryServiceMock).getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(repositoryServiceMock).getProcessDiagram(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testProcessDiagramMediaType() {
    Assert.assertEquals("image/png", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("process.png"));
    Assert.assertEquals("image/png", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("process.PNG"));
    Assert.assertEquals("image/svg+xml", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("process.svg"));
    Assert.assertEquals("image/jpeg", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("process.jpeg"));
    Assert.assertEquals("image/jpeg", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("process.jpg"));
    Assert.assertEquals("image/gif", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("process.gif"));
    Assert.assertEquals("image/bmp", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("process.bmp"));
    Assert.assertEquals("application/octet-stream", ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix("process.UNKNOWN"));
  }

  @Test
  public void testGetProcessDiagramGetDefinitionThrowsAuthorizationException() {
    String message = "expected exception";
    when(repositoryServiceMock.getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(DIAGRAM_DEFINITION_URL);
  }

  @Test
  public void testGetProcessDiagramThrowsAuthorizationException() {
    String message = "expected exception";
    when(repositoryServiceMock.getProcessDiagram(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(DIAGRAM_DEFINITION_URL);
  }

  @Test
  public void testGetProcessDiagramGetDefinitionThrowsAuthorizationException_ByKey() {
    String message = "expected exception";
    when(repositoryServiceMock.getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(DIAGRAM_DEFINITION_KEY_URL);
  }

  @Test
  public void testGetProcessDiagramThrowsAuthorizationException_ByKey() {
    String message = "expected exception";
    when(repositoryServiceMock.getProcessDiagram(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(DIAGRAM_DEFINITION_KEY_URL);
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
  public void testGetStartForm_shouldReturnKeyContainingTaskId() {
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    StartFormData mockStartFormData = MockProvider.createMockStartFormDataUsingFormFieldsWithoutFormKey(mockDefinition);
    when(formServiceMock.getStartFormData(mockDefinition.getId())).thenReturn(mockStartFormData);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo("embedded:engine://engine/:engine/process-definition/" + mockDefinition.getId() + "/rendered-form"))
      .body("contextPath", equalTo(MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH))
      .when().get(START_FORM_URL);
  }

  @Test
  public void testGetStartForm_shouldReturnCamundaFormRef() {
    StartFormData mockStartFormData = MockProvider.createMockStartFormDataUsingFormRef();
    when(formServiceMock.getStartFormData(anyString())).thenReturn(mockStartFormData);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("key", nullValue())
      .body("camundaFormRef.key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
      .body("camundaFormRef.binding", equalTo(MockProvider.EXAMPLE_FORM_REF_BINDING))
      .body("camundaFormRef.version", equalTo(MockProvider.EXAMPLE_FORM_REF_VERSION))
    .when().get(START_FORM_URL);
  }

  @Test
  public void testGetStartForm_StartFormDataEqualsNull() {
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    when(formServiceMock.getStartFormData(mockDefinition.getId())).thenReturn(null);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("contextPath", equalTo(MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH))
      .when().get(START_FORM_URL);
  }

  @Test
  public void testGetStartFormThrowsAuthorizationException() {
    String message = "expected exception";
    when(formServiceMock.getStartFormData(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(START_FORM_URL);
  }

  @Test
  public void testGetRenderedStartForm() {
    String expectedResult = "<formField>anyContent</formField>";

    when(formServiceMock.getRenderedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenReturn(expectedResult);

    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(XHTML_XML_CONTENT_TYPE)
      .when()
        .get(RENDERED_FORM_URL);

    String responseContent = response.asString();
    Assertions.assertThat(responseContent).isEqualTo(expectedResult);
  }

  @Test
  public void testGetRenderedStartFormForDifferentPlatformEncoding() throws NoSuchFieldException, IllegalAccessException, UnsupportedEncodingException {
    String expectedResult = "<formField>unicode symbol: \u2200</formField>";
    when(formServiceMock.getRenderedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenReturn(expectedResult);

    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
        .then()
          .expect()
            .statusCode(Status.OK.getStatusCode())
            .contentType(XHTML_XML_CONTENT_TYPE)
        .when()
          .get(RENDERED_FORM_URL);

    String responseContent = new String(response.asByteArray(), EncodingUtil.DEFAULT_ENCODING);
    Assertions.assertThat(responseContent).isEqualTo(expectedResult);
  }

  @Test
  public void testGetRenderedStartFormReturnsNotFound() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .then()
        .expect()
          .statusCode(Status.NOT_FOUND.getStatusCode())
          .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
          .body("message", equalTo("No matching rendered start form for process definition with the id " + MockProvider.EXAMPLE_PROCESS_DEFINITION_ID + " found."))
      .when()
        .get(RENDERED_FORM_URL);
  }

  @Test
  public void testGetRenderedStartFormThrowsAuthorizationException() {
    String message = "expected exception";
    when(formServiceMock.getRenderedStartForm(anyString())).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(RENDERED_FORM_URL);
  }

  @Test
  public void testSubmitStartForm() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
      .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
      .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
    .when().post(SUBMIT_FORM_URL);

    verify(formServiceMock).submitStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, null);
  }

  @Test
  public void testSubmitStartFormWithParameters() {
    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("anotherVariable", 42)
        .variable("aThirdValue", Boolean.TRUE).getVariables();

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
        .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
        .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
        .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().post(SUBMIT_FORM_URL);

    Map<String, Object> expectedVariables = new HashMap<>();
    expectedVariables.put("aVariable", "aStringValue");
    expectedVariables.put("anotherVariable", 42);
    expectedVariables.put("aThirdValue", Boolean.TRUE);

    verify(formServiceMock).submitStartForm(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testSubmitStartFormWithSerializedVariableValue() {

    String jsonValue = "{}";

    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("aSerializedVariable", ValueType.OBJECT.getName(), jsonValue, "aFormat", "aRootType")
        .getVariables();

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
        .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
        .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
        .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().post(SUBMIT_FORM_URL);

    verify(formServiceMock).submitStartForm(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID),
        argThat(
            new EqualsVariableMap()
              .matcher("aVariable", EqualsUntypedValue.matcher().value("aStringValue"))
              .matcher("aSerializedVariable", EqualsObjectValue
                                                .objectValueMatcher()
                                                .serializedValue(jsonValue)
                                                .serializationFormat("aFormat")
                                                .objectTypeName("aRootType"))));
  }

  @Test
  public void testSubmitStartFormWithBase64EncodedBytes() {

    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", Base64.encodeBase64String("someBytes".getBytes()), ValueType.BYTES.getName())
        .getVariables();

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
        .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
        .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
        .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().post(SUBMIT_FORM_URL);

    verify(formServiceMock).submitStartForm(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID),
        argThat(
            new EqualsVariableMap()
              .matcher("aVariable", EqualsPrimitiveValue.bytesValue("someBytes".getBytes()))));
  }

  @Test
  public void testSubmitStartFormWithBusinessKey() {
    Map<String, Object> json = new HashMap<>();
    json.put("businessKey", "myBusinessKey");

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
        .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
        .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
        .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().post(SUBMIT_FORM_URL);

    verify(formServiceMock).submitStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, "myBusinessKey", null);
  }

  @Test
  public void testSubmitStartFormWithBusinessKeyAndParameters() {
    Map<String, Object> json = new HashMap<>();
    json.put("businessKey", "myBusinessKey");

    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("anotherVariable", 42)
        .variable("aThirdValue", Boolean.TRUE).getVariables();

    json.put("variables", variables);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
        .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
        .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
        .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().post(SUBMIT_FORM_URL);

    Map<String, Object> expectedVariables = new HashMap<>();
    expectedVariables.put("aVariable", "aStringValue");
    expectedVariables.put("anotherVariable", 42);
    expectedVariables.put("aThirdValue", Boolean.TRUE);

    verify(formServiceMock).submitStartForm(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), eq("myBusinessKey"), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testSubmitStartFormWithUnparseableIntegerVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
    .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitStartFormWithUnparseableShortVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
    .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitStartFormWithUnparseableLongVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
    .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitStartFormWithUnparseableDoubleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
    .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitStartFormWithUnparseableDateVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
    .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitStartFormWithNotSupportedVariableType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: Unsupported value type 'X'"))
    .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testUnsuccessfulSubmitStartForm() {
    doThrow(new ProcessEngineException("expected exception")).when(formServiceMock).submitStartForm(any(String.class), Mockito.any());

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(RestException.class.getSimpleName()))
        .body("message", equalTo("Cannot instantiate process definition " + MockProvider.EXAMPLE_PROCESS_DEFINITION_ID + ": expected exception"))
      .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitFormByIdThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(formServiceMock).submitStartForm(any(String.class), Mockito.any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitFormByIdThrowsFormFieldValidationException() {
    String message = "expected exception";
    doThrow(new FormFieldValidationException("form-exception", message)).when(formServiceMock).submitStartForm(any(String.class), Mockito.any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot instantiate process definition " + MockProvider.EXAMPLE_PROCESS_DEFINITION_ID + ": " + message))
    .when()
      .post(SUBMIT_FORM_URL);
  }

  @Test
  public void testGetStartFormVariables() {

    given().pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
        .body(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME+".value", equalTo(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getValue()))
        .body(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME+".type",
            equalTo(VariableTypeHelper.toExpectedValueTypeName(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getType())))
      .when().get(START_FORM_VARIABLES_URL)
      .body();

    verify(formServiceMock, times(1)).getStartFormVariables(EXAMPLE_PROCESS_DEFINITION_ID, null, true);
  }

  @Test
  public void testGetStartFormVariablesVarNames() {

    given()
      .pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("variableNames", "a,b,c")
    .then().expect()
      .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
    .when().get(START_FORM_VARIABLES_URL);

    verify(formServiceMock, times(1)).getStartFormVariables(EXAMPLE_PROCESS_DEFINITION_ID, Arrays.asList("a", "b", "c"), true);
  }

  @Test
  public void testGetStartFormVariablesAndDoNotDeserializeVariables() {

    given()
      .pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("deserializeValues", false)
     .then()
       .expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
        .body(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME+".value", equalTo(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getValue()))
        .body(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME+".type",
            equalTo(VariableTypeHelper.toExpectedValueTypeName(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getType())))
      .when().get(START_FORM_VARIABLES_URL)
      .body();

    verify(formServiceMock, times(1)).getStartFormVariables(EXAMPLE_PROCESS_DEFINITION_ID, null, false);
  }

  @Test
  public void testGetStartFormVariablesVarNamesAndDoNotDeserializeVariables() {

    given()
      .pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("deserializeValues", false)
      .queryParam("variableNames", "a,b,c")
    .then().expect()
      .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
    .when().get(START_FORM_VARIABLES_URL);

    verify(formServiceMock, times(1)).getStartFormVariables(EXAMPLE_PROCESS_DEFINITION_ID, Arrays.asList("a", "b", "c"), false);
  }

  @Test
  public void testGetStartFormVariablesThrowsAuthorizationException() {
    String message = "expected exception";
    when(formServiceMock.getStartFormVariables(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, null, true)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(START_FORM_VARIABLES_URL);
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
  public void testSimpleProcessInstantiationWithVariables() {
    //mock process instance
    ProcessInstanceWithVariables mockInstance = MockProvider.createMockInstanceWithVariables();
    ProcessInstantiationBuilder mockInstantiationBuilder = setUpMockInstantiationBuilder();
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean())).thenReturn(mockInstance);
    when(runtimeServiceMock.createProcessInstanceById(anyString())).thenReturn(mockInstantiationBuilder);

    //given request with parameter withVariables to get variables in return
    Map<String, Object> json = new HashMap<>();
    json.put("withVariablesInReturn", true);

    //when request then return process instance with serialized variables
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
            .contentType(POST_JSON_CONTENT_TYPE).body(json)
            .then().expect()
            .statusCode(Status.OK.getStatusCode())
            .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
            //serialized variable
            .body("variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".value",
                    equalTo(MockProvider.EXAMPLE_VARIABLE_INSTANCE_SERIALIZED_VALUE))
            .body("variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".type",
                    equalTo("Object"))
            .body("variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".valueInfo.objectTypeName",
                    equalTo(ArrayList.class.getName()))
            .body("variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".valueInfo.serializationDataFormat",
                    equalTo(MockProvider.FORMAT_APPLICATION_JSON))
            //deserialized variable should also returned as serialized variable
            .body("variables." + MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME + ".value",
                    equalTo(MockProvider.EXAMPLE_VARIABLE_INSTANCE_SERIALIZED_VALUE))
            .body("variables." + MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME + ".type",
                    equalTo("Object"))
            .body("variables." + MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME + ".valueInfo.objectTypeName",
                    equalTo(Object.class.getName()))
            .body("variables." + MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME + ".valueInfo.serializationDataFormat",
                    equalTo(MockProvider.FORMAT_APPLICATION_JSON))
            .when().post(START_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());

  }

  @Test
  public void testProcessInstantiationWithParameters() throws IOException {
    Map<String, Object> parameters = VariablesBuilder.create()
        .variable("aBoolean", Boolean.TRUE)
        .variable("aString", "aStringVariableValue")
        .variable("anInteger", 42).getVariables();

    Map<String, Object> json = new HashMap<>();
    json.put("variables", parameters);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);

    Map<String, Object> expectedParameters = new HashMap<>();
    expectedParameters.put("aBoolean", Boolean.TRUE);
    expectedParameters.put("aString", "aStringVariableValue");
    expectedParameters.put("anInteger", 42);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).setVariables(argThat(new EqualsMap(expectedParameters)));
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  @Test
  public void testProcessInstantiationWithBusinessKey() throws IOException {
    Map<String, Object> json = new HashMap<>();
    json.put("businessKey", "myBusinessKey");

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).businessKey("myBusinessKey");
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  @Test
  public void testProcessInstantiationWithBusinessKeyAndParameters() throws IOException {
    Map<String, Object> json = new HashMap<>();
    json.put("businessKey", "myBusinessKey");

    Map<String, Object> parameters = VariablesBuilder.create()
        .variable("aBoolean", Boolean.TRUE)
        .variable("aString", "aStringVariableValue")
        .variable("anInteger", 42).getVariables();

    json.put("variables", parameters);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);

    Map<String, Object> expectedParameters = new HashMap<>();
    expectedParameters.put("aBoolean", Boolean.TRUE);
    expectedParameters.put("aString", "aStringVariableValue");
    expectedParameters.put("anInteger", 42);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).businessKey("myBusinessKey");
    verify(mockInstantiationBuilder).setVariables(argThat(new EqualsMap(expectedParameters)));
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  @Test
  public void testProcessInstantiationWithTransientVariables() throws IOException {
    Map<String, Object> json = new HashMap<>();

    json.put("variables", VariablesBuilder.create().variableTransient("foo", "bar", "string").getVariables());

    final VariableMap varMap = new VariableMapImpl();

    when(mockInstantiationBuilder.setVariables(anyMap())).thenAnswer(new Answer<ProcessInstantiationBuilder>() {
      @Override
      public ProcessInstantiationBuilder answer(InvocationOnMock invocation) throws Throwable {
        varMap.putAll((VariableMap) invocation.getArguments()[0]);
        return mockInstantiationBuilder;
      }
    });

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);

    VariableMap expectedVariables = Variables.createVariables().putValueTyped("foo", Variables.stringValue("bar", true));
    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).setVariables(expectedVariables);
    assertEquals(expectedVariables.getValueTyped("foo").isTransient(), varMap.getValueTyped("foo").isTransient());
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  @Test
  public void testProcessInstantiationAtActivitiesById() {

    Map<String, Object> json = new HashMap<>();
    json.put("variables", VariablesBuilder.create()
        .variable("processVariable", "aString", "String").getVariables());
    json.put("businessKey", "aBusinessKey");
    json.put("caseInstanceId", "aCaseInstanceId");

    List<Map<String, Object>> startInstructions = new ArrayList<>();

    startInstructions.add(
        ModificationInstructionBuilder.startBefore()
          .activityId("activityId")
          .variables(VariablesBuilder.create()
              .variable("var", "value", "String", false)
              .variable("varLocal", "valueLocal", "String", true)
              .getVariables())
          .getJson());
    startInstructions.add(
        ModificationInstructionBuilder.startAfter()
          .activityId("activityId")
          .variables(VariablesBuilder.create()
              .variable("var", 52, "Integer", false)
              .variable("varLocal", 74, "Integer", true)
              .getVariables())
          .getJson());
    startInstructions.add(
        ModificationInstructionBuilder.startTransition()
          .transitionId("transitionId")
          .variables(VariablesBuilder.create()
              .variable("var", 53, "Integer", false)
              .variable("varLocal", 75, "Integer", true)
              .getVariables())
          .getJson());


    json.put("startInstructions", startInstructions);

    given().pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).createProcessInstanceById(eq(EXAMPLE_PROCESS_DEFINITION_ID));

    InOrder inOrder = inOrder(mockInstantiationBuilder);

    inOrder.verify(mockInstantiationBuilder).businessKey("aBusinessKey");
    inOrder.verify(mockInstantiationBuilder).caseInstanceId("aCaseInstanceId");
    inOrder.verify(mockInstantiationBuilder).setVariables(argThat(EqualsVariableMap.matches()
        .matcher("processVariable", EqualsPrimitiveValue.stringValue("aString"))));

    inOrder.verify(mockInstantiationBuilder).startBeforeActivity("activityId");

    verify(mockInstantiationBuilder).setVariableLocal(eq("varLocal"), argThat(EqualsPrimitiveValue.stringValue("valueLocal")));
    verify(mockInstantiationBuilder).setVariable(eq("var"), argThat(EqualsPrimitiveValue.stringValue("value")));

    inOrder.verify(mockInstantiationBuilder).startAfterActivity("activityId");

    verify(mockInstantiationBuilder).setVariable(eq("var"), argThat(EqualsPrimitiveValue.integerValue(52)));
    verify(mockInstantiationBuilder).setVariableLocal(eq("varLocal"), argThat(EqualsPrimitiveValue.integerValue(74)));

    inOrder.verify(mockInstantiationBuilder).startTransition("transitionId");

    verify(mockInstantiationBuilder).setVariable(eq("var"), argThat(EqualsPrimitiveValue.integerValue(53)));
    verify(mockInstantiationBuilder).setVariableLocal(eq("varLocal"), argThat(EqualsPrimitiveValue.integerValue(75)));

    inOrder.verify(mockInstantiationBuilder).executeWithVariablesInReturn(false, false);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testProcessInstantiationAtActivitiesByIdWithVariablesInReturn() {
    //set up variables and parameters
    Map<String, Object> json = new HashMap<>();
    json.put("variables", VariablesBuilder.create()
        .variable("processVariable", "aString", "String").getVariables());
    json.put("businessKey", "aBusinessKey");
    json.put("caseInstanceId", "aCaseInstanceId");

    VariableMap variables = createMockSerializedVariables()
            .putValueTyped("processVariable", Variables.stringValue("aString"))
            .putValueTyped("var", Variables.stringValue("value"))
            .putValueTyped("varLocal", Variables.stringValue("valueLocal"));

    //mock process instance and instantiation builder
    ProcessInstanceWithVariables mockInstance = MockProvider.createMockInstanceWithVariables();
    when(mockInstance.getVariables()).thenReturn(variables);

    ProcessInstantiationBuilder mockInstantiationBuilder = setUpMockInstantiationBuilder();
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean())).thenReturn(mockInstance);
    when(runtimeServiceMock.createProcessInstanceById(anyString())).thenReturn(mockInstantiationBuilder);

    //create instructions
    List<Map<String, Object>> startInstructions = new ArrayList<>();

    startInstructions.add(
        ModificationInstructionBuilder.startBefore()
          .activityId("activityId")
          .variables(VariablesBuilder.create()
              .variable("var", "value", "String", false)
              .variable("varLocal", "valueLocal", "String", true)
              .getVariables())
          .getJson());

    json.put("startInstructions", startInstructions);
    json.put("withVariablesInReturn", true);

    //request which response should contain serialized variables of process instance
    given().pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
            .contentType(POST_JSON_CONTENT_TYPE).body(json)
            .then().expect()
            .statusCode(Status.OK.getStatusCode())
            //serialized variable
            .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
            .body("variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".value",
                    equalTo(MockProvider.EXAMPLE_VARIABLE_INSTANCE_SERIALIZED_VALUE))
            .body("variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".type",
                    equalTo("Object"))
            .body("variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".valueInfo.objectTypeName",
                    equalTo(ArrayList.class.getName()))
            .body("variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".valueInfo.serializationDataFormat",
                    equalTo(MockProvider.FORMAT_APPLICATION_JSON))
            //deserialized variable should also returned as serialized variable
            .body("variables." + MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME + ".value",
                    equalTo(MockProvider.EXAMPLE_VARIABLE_INSTANCE_SERIALIZED_VALUE))
            .body("variables." + MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME + ".type",
                    equalTo("Object"))
            .body("variables." + MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME + ".valueInfo.objectTypeName",
                    equalTo(Object.class.getName()))
            .body("variables." + MockProvider.EXAMPLE_DESERIALIZED_VARIABLE_INSTANCE_NAME + ".valueInfo.serializationDataFormat",
                    equalTo(MockProvider.FORMAT_APPLICATION_JSON))
            .body("variables.processVariable.type", equalTo("String"))
            .body("variables.processVariable.value", equalTo("aString"))
            .body("variables.var.type", equalTo("String"))
            .body("variables.var.value", equalTo("value"))
            .body("variables.varLocal.type", equalTo("String"))
            .body("variables.varLocal.value", equalTo("valueLocal"))
            .when().post(START_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).createProcessInstanceById(eq(EXAMPLE_PROCESS_DEFINITION_ID));
  }

  @Test
  public void testProcessInstantiationAtActivitiesByKey() {
    ProcessInstantiationBuilder mockInstantiationBuilder = setUpMockInstantiationBuilder();
    when(runtimeServiceMock.createProcessInstanceById(anyString())).thenReturn(mockInstantiationBuilder);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", VariablesBuilder.create()
        .variable("processVariable", "aString", "String").getVariables());
    json.put("businessKey", "aBusinessKey");
    json.put("caseInstanceId", "aCaseInstanceId");

    List<Map<String, Object>> startInstructions = new ArrayList<>();

    startInstructions.add(
        ModificationInstructionBuilder.startBefore()
          .activityId("activityId")
          .variables(VariablesBuilder.create()
              .variable("var", "value", "String", false)
              .variable("varLocal", "valueLocal", "String", true)
              .getVariables())
          .getJson());
    startInstructions.add(
        ModificationInstructionBuilder.startAfter()
          .activityId("activityId")
          .variables(VariablesBuilder.create()
              .variable("var", 52, "Integer", false)
              .variable("varLocal", 74, "Integer", true)
              .getVariables())
          .getJson());
    startInstructions.add(
        ModificationInstructionBuilder.startTransition()
          .transitionId("transitionId")
          .variables(VariablesBuilder.create()
              .variable("var", 53, "Integer", false)
              .variable("varLocal", 75, "Integer", true)
              .getVariables())
          .getJson());


    json.put("startInstructions", startInstructions);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));

    InOrder inOrder = inOrder(mockInstantiationBuilder);

    inOrder.verify(mockInstantiationBuilder).businessKey("aBusinessKey");
    inOrder.verify(mockInstantiationBuilder).caseInstanceId("aCaseInstanceId");
    inOrder.verify(mockInstantiationBuilder).setVariables(argThat(EqualsVariableMap.matches()
        .matcher("processVariable", EqualsPrimitiveValue.stringValue("aString"))));

    inOrder.verify(mockInstantiationBuilder).startBeforeActivity("activityId");

    verify(mockInstantiationBuilder).setVariableLocal(eq("varLocal"), argThat(EqualsPrimitiveValue.stringValue("valueLocal")));
    verify(mockInstantiationBuilder).setVariable(eq("var"), argThat(EqualsPrimitiveValue.stringValue("value")));

    inOrder.verify(mockInstantiationBuilder).startAfterActivity("activityId");

    verify(mockInstantiationBuilder).setVariable(eq("var"), argThat(EqualsPrimitiveValue.integerValue(52)));
    verify(mockInstantiationBuilder).setVariableLocal(eq("varLocal"), argThat(EqualsPrimitiveValue.integerValue(74)));

    inOrder.verify(mockInstantiationBuilder).startTransition("transitionId");

    verify(mockInstantiationBuilder).setVariable(eq("var"), argThat(EqualsPrimitiveValue.integerValue(53)));
    verify(mockInstantiationBuilder).setVariableLocal(eq("varLocal"), argThat(EqualsPrimitiveValue.integerValue(75)));

    inOrder.verify(mockInstantiationBuilder).executeWithVariablesInReturn(false, false);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testProcessInstantiationAtActivitiesSkipIoMappingsAndListeners() {
    ProcessInstantiationBuilder mockInstantiationBuilder = setUpMockInstantiationBuilder();
    when(runtimeServiceMock.createProcessInstanceById(anyString())).thenReturn(mockInstantiationBuilder);

    Map<String, Object> json = new HashMap<>();

    List<Map<String, Object>> startInstructions = new ArrayList<>();

    startInstructions.add(
        ModificationInstructionBuilder.startBefore()
          .activityId("activityId")
          .getJson());

    json.put("startInstructions", startInstructions);
    json.put("skipIoMappings", true);
    json.put("skipCustomListeners", true);

    given().pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).createProcessInstanceById(eq(EXAMPLE_PROCESS_DEFINITION_ID));

    InOrder inOrder = inOrder(mockInstantiationBuilder);

    inOrder.verify(mockInstantiationBuilder).startBeforeActivity("activityId");
    inOrder.verify(mockInstantiationBuilder).executeWithVariablesInReturn(true, true);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testInvalidInstantiationAtActivities() {
    ProcessInstantiationBuilder mockInstantiationBuilder = setUpMockInstantiationBuilder();
    when(runtimeServiceMock.createProcessInstanceById(anyString())).thenReturn(mockInstantiationBuilder);

    Map<String, Object> json = new HashMap<>();

    // start before: missing activity id
    List<Map<String, Object>> instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.startBefore().getJson());
    json.put("startInstructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("'activityId' must be set"))
    .when()
      .post(START_PROCESS_INSTANCE_URL);

    // start after: missing ancestor activity instance id
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.startAfter().getJson());
    json.put("startInstructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("'activityId' must be set"))
    .when()
      .post(START_PROCESS_INSTANCE_URL);

    // start transition: missing ancestor activity instance id
    instructions = new ArrayList<>();
    instructions.add(ModificationInstructionBuilder.startTransition().getJson());
    json.put("startInstructions", instructions);

    given()
      .pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("'transitionId' must be set"))
    .when()
      .post(START_PROCESS_INSTANCE_URL);
  }

  @SuppressWarnings("unchecked")
  protected ProcessInstantiationBuilder setUpMockInstantiationBuilder() {
    ProcessInstanceWithVariables resultInstanceWithVariables = MockProvider.createMockInstanceWithVariables();
    ProcessInstantiationBuilder mockInstantiationBuilder = mock(ProcessInstantiationBuilder.class);

    when(mockInstantiationBuilder.startAfterActivity(any())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.startBeforeActivity(any())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.startTransition(any())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.setVariables(any())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.setVariablesLocal(any())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.businessKey(any())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.caseInstanceId(any())).thenReturn(mockInstantiationBuilder);
    when(mockInstantiationBuilder.execute(anyBoolean(), anyBoolean())).thenReturn(resultInstanceWithVariables);
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean())).thenReturn(resultInstanceWithVariables);

    return mockInstantiationBuilder;
  }

  /**
   * {@link RuntimeService#startProcessInstanceById(String, Map)} throws an {@link ProcessEngineException}, if a definition with the given id does not exist.
   */
  @Test
  public void testUnsuccessfulInstantiation() {
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean()))
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
  public void testStartProcessInstanceByIdThrowsAuthorizationException() {
    String message = "expected exception";
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean()))
      .thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(START_PROCESS_INSTANCE_URL);
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
      .body("tenantId", nullValue())
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
    when(repositoryServiceMock.getProcessModel(eq(nonExistingId))).thenThrow(new NotFoundException("no matching process definition found."));

    given().pathParam("id", nonExistingId)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("No matching definition with id " + nonExistingId))
    .when().get(XML_DEFINITION_URL);
  }

  @Test
  public void testGetProcessDefinitionBpmn20XmlThrowsProcessEngineException() {
    String processDefinitionId = "someId";
    when(repositoryServiceMock.getProcessModel(eq(processDefinitionId))).thenThrow(new ProcessEngineException("generic message"));

    given().pathParam("id", processDefinitionId)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("generic message"))
    .when().get(XML_DEFINITION_URL);
  }

  @Test
  public void testGetProcessDefinitionBpmn20XmlThrowsAuthorizationException() {
    String message = "expected exception";
    when(repositoryServiceMock.getProcessModel(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(XML_DEFINITION_URL);
  }

  @Test
  public void testDeleteDeployment() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);

    verify(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, false, false);
  }


  @Test
  public void testDeleteDeploymentCascade() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("cascade", true)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);

    verify(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, false, false);
  }

  @Test
  public void testDeleteDeploymentCascadeNonsense() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("cascade", "bla")
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);

    verify(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, false, false);
  }

  @Test
  public void testDeleteDeploymentCascadeFalse() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("cascade", false)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);

    verify(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, false, false);
  }

  @Test
  public void testDeleteDeploymentSkipCustomListeners() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("skipCustomListeners", true)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);

    verify(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, true, false);
  }

  @Test
  public void testDeleteDeploymentSkipCustomListenersNonsense() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("skipCustomListeners", "bla")
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);

    verify(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, false, false);
  }

  @Test
  public void testDeleteDeploymentSkipCustomListenersFalse() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("skipCustomListeners", false)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);

    verify(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, false, false);
  }

  @Test
  public void testDeleteDeploymentSkipCustomListenersAndCascade() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("cascade", true)
      .queryParam("skipCustomListeners", true)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);

    verify(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, true, false);
  }

  @Test
  public void testDeleteNonExistingDeployment() {

    doThrow(new NotFoundException("No process definition found with id 'NON_EXISTING_ID'"))
            .when(repositoryServiceMock)
            .deleteProcessDefinition("NON_EXISTING_ID", false, false, false);

    given()
      .pathParam("id", "NON_EXISTING_ID")
    .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("No process definition found with id 'NON_EXISTING_ID'"))
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);
  }

  @Test
  public void testDeleteDeploymentThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, false, false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);
  }

  @Test
  public void testDeleteDefinitionSkipIoMappings() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("skipIoMappings", true)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_URL);

    verify(repositoryServiceMock).deleteProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, false, true);
  }

  @Test
  public void testDeleteDefinitionsByKey() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_DELETE_URL);

    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    verify(builder).delete();
  }

  @Test
  public void testDeleteDefinitionsByKeyCascade() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .queryParam("cascade", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_DELETE_URL);

    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .cascade();

    verify(builder).delete();
  }

  @Test
  public void testDeleteDefinitionsByKeySkipCustomListeners() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .queryParam("skipCustomListeners", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_DELETE_URL);

    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .skipCustomListeners();

    verify(builder).delete();
  }

  @Test
  public void testDeleteDefinitionsByKeySkipIoMappings() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .queryParam("skipIoMappings", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_DELETE_URL);

    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .skipIoMappings();

    verify(builder).delete();
  }

  @Test
  public void testDeleteDefinitionsByKeySkipCustomListenersAndCascade() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .queryParam("cascade", true)
      .queryParam("skipCustomListeners", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_DELETE_URL);

    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .skipCustomListeners()
      .cascade();

    verify(builder).delete();
  }

  @Test
  public void testDeleteDefinitionsByKeyNotExistingKey() {
    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey("NOT_EXISTING_KEY");

    doThrow(new NotFoundException("No process definition found with key 'NOT_EXISTING_KEY'")).when(builder).delete();

    given()
      .pathParam("key", "NOT_EXISTING_KEY")
    .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("No process definition found with key 'NOT_EXISTING_KEY'"))
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_DELETE_URL);
  }

  @Test
  public void testDeleteDefinitionsByKeyWithTenantId() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .pathParam("tenant-id", MockProvider.EXAMPLE_TENANT_ID)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_DELETE_URL);

    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .withTenantId(MockProvider.EXAMPLE_TENANT_ID);

    verify(builder).delete();
  }


  @Test
  public void testDeleteDefinitionsByKeyCascadeWithTenantId() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .pathParam("tenant-id", MockProvider.EXAMPLE_TENANT_ID)
      .queryParam("cascade", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_DELETE_URL);

    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .withTenantId(MockProvider.EXAMPLE_TENANT_ID)
      .cascade();

    verify(builder).delete();
  }

  @Test
  public void testDeleteDefinitionsByKeySkipCustomListenersWithTenantId() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .pathParam("tenant-id", MockProvider.EXAMPLE_TENANT_ID)
      .queryParam("skipCustomListeners", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_DELETE_URL);

    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .withTenantId(MockProvider.EXAMPLE_TENANT_ID)
      .skipCustomListeners();

    verify(builder).delete();
  }

  @Test
  public void testDeleteDefinitionsByKeySkipCustomListenersAndCascadeWithTenantId() {
    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .queryParam("skipCustomListeners", true)
      .queryParam("cascade", true)
      .pathParam("tenant-id", MockProvider.EXAMPLE_TENANT_ID)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_DELETE_URL);

    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .withTenantId(MockProvider.EXAMPLE_TENANT_ID)
      .skipCustomListeners()
      .cascade();

    verify(builder).delete();
  }

  @Test
  public void testDeleteDefinitionsByKeyNoPermissions() {
    DeleteProcessDefinitionsBuilder builder = repositoryServiceMock.deleteProcessDefinitions()
      .byKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .withTenantId(MockProvider.EXAMPLE_TENANT_ID);

    doThrow(new AuthorizationException("No permission to delete process definitions")).when(builder).delete();

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .pathParam("tenant-id", MockProvider.EXAMPLE_TENANT_ID)
    .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body(containsString("No permission to delete process definitions"))
    .when()
      .delete(SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_DELETE_URL);
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

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
    .when().post(START_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testUnparseableShortVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
    .when().post(START_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testUnparseableLongVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
    .when().post(START_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testUnparseableDoubleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
    .when().post(START_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testUnparseableDateVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
    .when().post(START_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testNotSupportedTypeVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: Unsupported value type 'X'"))
    .when().post(START_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testActivateProcessDefinitionExcludingInstances() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);
  }

  @Test
  public void testDelayedActivateProcessDefinitionExcludingInstances() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", false);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, executionDate);
  }

  @Test
  public void testActivateProcessDefinitionIncludingInstances() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, null);
  }

  @Test
  public void testDelayedActivateProcessDefinitionIncludingInstances() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", true);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, executionDate);
  }

  @Test
  public void testActivateThrowsProcessEngineException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(repositoryServiceMock)
      .activateProcessDefinitionById(eq(MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID), eq(false), isNull());

    given()
      .pathParam("id", MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateNonParseableDateFormat() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", false);
    params.put("executionDate", "a");

    String expectedMessage = "Invalid format: \"a\"";
    String exceptionMessage = "The suspension state of Process Definition with id " + MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID + " could not be updated due to: " + expectedMessage;

    given()
      .pathParam("id", MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(exceptionMessage))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessDefinitionThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionExcludingInstances() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);
  }

  @Test
  public void testDelayedSuspendProcessDefinitionExcludingInstances() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", false);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, executionDate);
  }

  @Test
  public void testSuspendProcessDefinitionIncludingInstances() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, null);
  }

  @Test
  public void testDelayedSuspendProcessDefinitionIncludingInstances() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", true);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, executionDate);
  }

  @Test
  public void testSuspendThrowsProcessEngineException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(repositoryServiceMock)
      .suspendProcessDefinitionById(eq(MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID), eq(false), isNull());

    given()
      .pathParam("id", MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendNonParseableDateFormat() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", false);
    params.put("executionDate", "a");

    String expectedMessage = "Invalid format: \"a\"";
    String exceptionMessage = "The suspension state of Process Definition with id " + MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID + " could not be updated due to: " + expectedMessage;

    given()
      .pathParam("id", MockProvider.NON_EXISTING_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(exceptionMessage))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendWithMultipleByParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "Only one of processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessDefinitionByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);
  }

  @Test
  public void testActivateProcessDefinitionByKeyIncludingInstaces() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, true, null);
  }

  @Test
  public void testDelayedActivateProcessDefinitionByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, executionDate);
  }

  @Test
  public void testDelayedActivateProcessDefinitionByKeyIncludingInstaces() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, true, executionDate);
  }

  @Test
  public void testActivateProcessDefinitionByKeyWithUnparseableDate() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", "a");

    String message = "Could not update the suspension state of Process Definitions due to: Invalid format: \"a\"";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessDefinitionByKeyWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(repositoryServiceMock)
      .activateProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessDefinitionByKeyThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(repositoryServiceMock).activateProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);
  }

  @Test
  public void testSuspendProcessDefinitionByKeyIncludingInstaces() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, true, null);
  }

  @Test
  public void testDelayedSuspendProcessDefinitionByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, executionDate);
  }

  @Test
  public void testDelayedSuspendProcessDefinitionByKeyIncludingInstaces() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, true, executionDate);
  }

  @Test
  public void testSuspendProcessDefinitionByKeyWithUnparseableDate() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", "a");

    String message = "Could not update the suspension state of Process Definitions due to: Invalid format: \"a\"";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionByKeyWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(repositoryServiceMock)
      .suspendProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionByKeyThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(repositoryServiceMock).suspendProcessDefinitionByKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessDefinitionByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String message = "Only processDefinitionKey can be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessDefinitionByNothing() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);

    String message = "Either processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String message = "Only processDefinitionKey can be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionByNothing() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);

    String message = "Either processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionThrowsAuthorizationExcpetion() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);

    String message = "Either processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(PROCESS_DEFINITION_SUSPENDED_URL);
  }

  /**
   *
   ********************************* test cases for operations of the latest process definition ********************************
   * get the latest process definition by key
   *
   */

  @Test
  public void testInstanceResourceLinkResult_ByKey() {
    String fullInstanceUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/process-instance/" + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("links[0].href", equalTo(fullInstanceUrl))
      .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void testInstanceResourceLinkWithEnginePrefix_ByKey() {
    String startInstanceOnExplicitEngineUrl = TEST_RESOURCE_ROOT_PATH + "/engine/default/process-definition/key/{key}/start";

    String fullInstanceUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/engine/default/process-instance/" + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("links[0].href", equalTo(fullInstanceUrl))
      .when().post(startInstanceOnExplicitEngineUrl);
  }

  @Test
  public void testProcessDefinitionBpmn20XmlRetrieval_ByKey() {
    // Rest-assured has problems with extracting json with escaped quotation marks, i.e. the xml content in our case
    Response response = given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
//      .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
//      .body("bpmn20Xml", startsWith("<?xml"))
    .when().get(XML_DEFINITION_BY_KEY_URL);

    String responseContent = response.asString();
    Assert.assertTrue(responseContent.contains(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    Assert.assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testGetProcessDefinitionBpmn20XmlThrowsAuthorizationException_ByKey() {
    String message = "expected exception";
    when(repositoryServiceMock.getProcessModel(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(XML_DEFINITION_BY_KEY_URL);
  }

  @Test
  public void testGetStartFormData_ByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
    .when().get(START_FORM_BY_KEY_URL);
  }

  @Test
  public void testGetStartFormThrowsAuthorizationException_ByKey() {
    String message = "expected exception";
    when(formServiceMock.getStartFormData(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(START_FORM_BY_KEY_URL);
  }

  @Test
  public void testGetStartForm_shouldReturnKeyContainingTaskId_ByKey() {
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    StartFormData mockStartFormData = MockProvider.createMockStartFormDataUsingFormFieldsWithoutFormKey(mockDefinition);
    when(formServiceMock.getStartFormData(mockDefinition.getId())).thenReturn(mockStartFormData);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo("embedded:engine://engine/:engine/process-definition/" + mockDefinition.getId() + "/rendered-form"))
      .body("contextPath", equalTo(MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH))
      .when().get(START_FORM_BY_KEY_URL);
  }

  @Test
  public void testGetRenderedStartForm_ByKey() {
    String expectedResult = "<formField>anyContent</formField>";

    when(formServiceMock.getRenderedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenReturn(expectedResult);

    Response response = given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(XHTML_XML_CONTENT_TYPE)
      .when()
        .get(RENDERED_FORM_BY_KEY_URL);

    String responseContent = response.asString();
    Assertions.assertThat(responseContent).isEqualTo(expectedResult);
  }

  @Test
  public void testGetRenderedStartFormReturnsNotFound_ByKey() {
    when(formServiceMock.getRenderedStartForm(anyString(), anyString())).thenReturn(null);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .then()
        .expect()
          .statusCode(Status.NOT_FOUND.getStatusCode())
          .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
          .body("message", equalTo("No matching rendered start form for process definition with the id " + MockProvider.EXAMPLE_PROCESS_DEFINITION_ID + " found."))
      .when()
        .get(RENDERED_FORM_BY_KEY_URL);
  }

  @Test
  public void testGetRenderedStartFormThrowsAuthorizationException_ByKey() {
    String message = "expected exception";
    when(formServiceMock.getRenderedStartForm(anyString())).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(RENDERED_FORM_BY_KEY_URL);
  }

  @Test
  public void testSubmitStartForm_ByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
      .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
      .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
    .when().post(SUBMIT_FORM_BY_KEY_URL);

    verify(formServiceMock).submitStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, null);
  }

  @Test
  public void testSubmitStartFormWithParameters_ByKey() {
    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("anotherVariable", 42)
        .variable("aThirdValue", Boolean.TRUE).getVariables();

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
        .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
        .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
        .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().post(SUBMIT_FORM_BY_KEY_URL);

    Map<String, Object> expectedVariables = new HashMap<>();
    expectedVariables.put("aVariable", "aStringValue");
    expectedVariables.put("anotherVariable", 42);
    expectedVariables.put("aThirdValue", Boolean.TRUE);

    verify(formServiceMock).submitStartForm(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testSubmitStartFormWithBusinessKey_ByKey() {
    Map<String, Object> json = new HashMap<>();
    json.put("businessKey", "myBusinessKey");

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
        .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
        .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
        .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().post(SUBMIT_FORM_BY_KEY_URL);

    verify(formServiceMock).submitStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, "myBusinessKey", null);
  }

  @Test
  public void testSubmitStartFormWithBusinessKeyAndParameters_ByKey() {
    Map<String, Object> json = new HashMap<>();
    json.put("businessKey", "myBusinessKey");

    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("anotherVariable", 42)
        .variable("aThirdValue", Boolean.TRUE).getVariables();

    json.put("variables", variables);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
        .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
        .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
        .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().post(SUBMIT_FORM_BY_KEY_URL);

    Map<String, Object> expectedVariables = new HashMap<>();
    expectedVariables.put("aVariable", "aStringValue");
    expectedVariables.put("anotherVariable", 42);
    expectedVariables.put("aThirdValue", Boolean.TRUE);

    verify(formServiceMock).submitStartForm(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), eq("myBusinessKey"), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testSubmitStartFormWithUnparseableIntegerVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
    .when().post(SUBMIT_FORM_BY_KEY_URL);
  }

  @Test
  public void testSubmitStartFormWithUnparseableShortVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
    .when().post(SUBMIT_FORM_BY_KEY_URL);
  }

  @Test
  public void testSubmitStartFormWithUnparseableLongVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
    .when().post(SUBMIT_FORM_BY_KEY_URL);
  }

  @Test
  public void testSubmitStartFormWithUnparseableDoubleVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
    .when().post(SUBMIT_FORM_BY_KEY_URL);
  }

  @Test
  public void testSubmitStartFormWithUnparseableDateVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
    .when().post(SUBMIT_FORM_BY_KEY_URL);
  }

  @Test
  public void testSubmitStartFormWithNotSupportedVariableType_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: Unsupported value type 'X'"))
    .when().post(SUBMIT_FORM_BY_KEY_URL);
  }

  @Test
  public void testUnsuccessfulSubmitStartForm_ByKey() {
    doThrow(new ProcessEngineException("expected exception")).when(formServiceMock).submitStartForm(any(String.class), Mockito.any());

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(RestException.class.getSimpleName()))
        .body("message", equalTo("Cannot instantiate process definition " + MockProvider.EXAMPLE_PROCESS_DEFINITION_ID + ": expected exception"))
      .when().post(SUBMIT_FORM_BY_KEY_URL);
  }

  @Test
  public void testSubmitFormByKeyThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(formServiceMock).submitStartForm(any(String.class), Mockito.any());

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(SUBMIT_FORM_BY_KEY_URL);
  }

  @Test
  public void testSubmitFormByKeyThrowsFormFieldValidationException() {
    String message = "expected exception";
    doThrow(new FormFieldValidationException("form-exception", message)).when(formServiceMock).submitStartForm(any(String.class), Mockito.any());

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot instantiate process definition " + MockProvider.EXAMPLE_PROCESS_DEFINITION_ID + ": " + message))
    .when()
      .post(SUBMIT_FORM_BY_KEY_URL);
  }

  @Test
  public void testSimpleProcessInstantiation_ByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void testProcessInstantiationWithParameters_ByKey() throws IOException {
    Map<String, Object> parameters = VariablesBuilder.create()
        .variable("aBoolean", Boolean.TRUE)
        .variable("aString", "aStringVariableValue")
        .variable("anInteger", 42).getVariables();

    Map<String, Object> json = new HashMap<>();
    json.put("variables", parameters);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);

    Map<String, Object> expectedParameters = new HashMap<>();
    expectedParameters.put("aBoolean", Boolean.TRUE);
    expectedParameters.put("aString", "aStringVariableValue");
    expectedParameters.put("anInteger", 42);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).setVariables(argThat(new EqualsMap(expectedParameters)));
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  @Test
  public void testProcessInstantiationWithBusinessKey_ByKey() throws IOException {
    Map<String, Object> json = new HashMap<>();
    json.put("businessKey", "myBusinessKey");

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).businessKey("myBusinessKey");
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  @Test
  public void testProcessInstantiationWithBusinessKeyAndParameters_ByKey() throws IOException {
    Map<String, Object> json = new HashMap<>();
    json.put("businessKey", "myBusinessKey");

    Map<String, Object> parameters = VariablesBuilder.create()
        .variable("aBoolean", Boolean.TRUE)
        .variable("aString", "aStringVariableValue")
        .variable("anInteger", 42).getVariables();

    json.put("variables", parameters);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);

    Map<String, Object> expectedParameters = new HashMap<>();
    expectedParameters.put("aBoolean", Boolean.TRUE);
    expectedParameters.put("aString", "aStringVariableValue");
    expectedParameters.put("anInteger", 42);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).businessKey("myBusinessKey");
    verify(mockInstantiationBuilder).setVariables(argThat(new EqualsMap(expectedParameters)));
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  /**
   * {@link RuntimeService#startProcessInstanceById(String, Map)} throws an {@link ProcessEngineException}, if a definition with the given id does not exist.
   */
  @Test
  public void testUnsuccessfulInstantiation_ByKey() {
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean()))
      .thenThrow(new ProcessEngineException("expected exception"));

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(RestException.class.getSimpleName()))
        .body("message", containsString("Cannot instantiate process definition"))
      .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void testStartProcessInstanceByKeyThrowsAuthorizationException() {
    String message = "expected exception";
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean()))
      .thenThrow(new AuthorizationException(message));

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void testDefinitionRetrieval_ByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
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
      .body("tenantId", nullValue())
    .when().get(SINGLE_PROCESS_DEFINITION_BY_KEY_URL);

    verify(processDefinitionQueryMock).withoutTenantId();
    verify(repositoryServiceMock).getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testNonExistingProcessDefinitionRetrieval_ByKey() {
    String nonExistingKey = "aNonExistingDefinitionKey";

    when(repositoryServiceMock.createProcessDefinitionQuery().processDefinitionKey(nonExistingKey)).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.latestVersion()).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.singleResult()).thenReturn(null);
    when(processDefinitionQueryMock.list()).thenReturn(Collections.emptyList());
    when(processDefinitionQueryMock.count()).thenReturn(0L);

    given().pathParam("key", nonExistingKey)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", containsString("No matching process definition with key: " + nonExistingKey + " and no tenant-id"))
    .when().get(SINGLE_PROCESS_DEFINITION_BY_KEY_URL);
  }

  @Test
  public void testDefinitionRetrieval_ByKeyAndTenantId() {
    ProcessDefinition mockDefinition = MockProvider.mockDefinition().tenantId(MockProvider.EXAMPLE_TENANT_ID).build();
    setUpRuntimeDataForDefinition(mockDefinition);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .pathParam("tenant-id", MockProvider.EXAMPLE_TENANT_ID)
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
      .body("tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
    .when().get(SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_URL);

    verify(processDefinitionQueryMock).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID);
    verify(repositoryServiceMock).getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testNonExistingProcessDefinitionRetrieval_ByKeyAndTenantId() {
    String nonExistingKey = "aNonExistingDefinitionKey";
    String nonExistingTenantId = "aNonExistingTenantId";

    when(repositoryServiceMock.createProcessDefinitionQuery().processDefinitionKey(nonExistingKey)).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.singleResult()).thenReturn(null);

    given()
      .pathParam("key", nonExistingKey)
      .pathParam("tenant-id", nonExistingTenantId)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", containsString("No matching process definition with key: " + nonExistingKey + " and tenant-id: " + nonExistingTenantId))
    .when().get(SINGLE_PROCESS_DEFINITION_BY_KEY_AND_TENANT_ID_URL);
  }

  @Test
  public void testSimpleProcessInstantiation_ByKeyAndTenantId() {
    ProcessDefinition mockDefinition = MockProvider.mockDefinition().tenantId(MockProvider.EXAMPLE_TENANT_ID).build();
    setUpRuntimeDataForDefinition(mockDefinition);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .pathParam("tenant-id", MockProvider.EXAMPLE_TENANT_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
    .when().post(START_PROCESS_INSTANCE_BY_KEY_AND_TENANT_ID_URL);

    verify(processDefinitionQueryMock).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void testUnparseableIntegerVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
    .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void testUnparseableShortVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
    .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void testUnparseableLongVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
    .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void testUnparseableDoubleVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
    .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void testUnparseableDateVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: "
        + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
    .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void testNotSupportedTypeVariable_ByKey() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<>();
    variables.put("variables", variableJson);

    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot instantiate process definition aProcDefId: Unsupported value type 'X'"))
    .when().post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }


  @Test
  public void testUpdateHistoryTimeToLive() {
    given()
        .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
        .body(new HistoryTimeToLiveDto(5))
        .contentType(ContentType.JSON)
        .then().expect()
          .statusCode(Status.NO_CONTENT.getStatusCode())
        .when()
          .put(SINGLE_PROCESS_DEFINITION_HISTORY_TIMETOLIVE_URL);

    verify(repositoryServiceMock).updateProcessDefinitionHistoryTimeToLive(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, 5);
  }

  @Test
  public void testUpdateHistoryTimeToLiveNullValue() {
    given()
        .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
        .body(new HistoryTimeToLiveDto())
        .contentType(ContentType.JSON)
        .then().expect()
          .statusCode(Status.NO_CONTENT.getStatusCode())
        .when()
          .put(SINGLE_PROCESS_DEFINITION_HISTORY_TIMETOLIVE_URL);

    verify(repositoryServiceMock).updateProcessDefinitionHistoryTimeToLive(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, null);
  }

  @Test
  public void testUpdateHistoryTimeToLiveNegativeValue() {
    String expectedMessage = "expectedMessage";

    doThrow(new BadUserRequestException(expectedMessage))
        .when(repositoryServiceMock)
        .updateProcessDefinitionHistoryTimeToLive(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), eq(-1));

    given()
        .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
        .body(new HistoryTimeToLiveDto(-1))
        .contentType(ContentType.JSON)
        .then().expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
          .body("type", is(BadUserRequestException.class.getSimpleName()))
          .body("message", containsString(expectedMessage))
        .when()
          .put(SINGLE_PROCESS_DEFINITION_HISTORY_TIMETOLIVE_URL);

    verify(repositoryServiceMock).updateProcessDefinitionHistoryTimeToLive(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, -1);
  }

  @Test
  public void testUpdateHistoryTimeToLiveAuthorizationException() {
    String expectedMessage = "expectedMessage";

    doThrow(new AuthorizationException(expectedMessage))
        .when(repositoryServiceMock)
        .updateProcessDefinitionHistoryTimeToLive(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), eq(5));

    given()
        .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
        .body(new HistoryTimeToLiveDto(5))
        .contentType(ContentType.JSON)
        .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", containsString(expectedMessage))
        .when()
        .put(SINGLE_PROCESS_DEFINITION_HISTORY_TIMETOLIVE_URL);

    verify(repositoryServiceMock).updateProcessDefinitionHistoryTimeToLive(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, 5);
  }

  @Test
  public void testActivateProcessDefinitionExcludingInstances_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", false);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);
  }

  @Test
  public void testDelayedActivateProcessDefinitionExcludingInstances_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", false);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, executionDate);
  }

  @Test
  public void testActivateProcessDefinitionIncludingInstances_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", true);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, null);
  }

  @Test
  public void testDelayedActivateProcessDefinitionIncludingInstances_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", true);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);

    verify(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, executionDate);
  }

  @Test
  public void testActivateThrowsProcessEngineException_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(repositoryServiceMock)
      .activateProcessDefinitionById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), eq(false), isNull());

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", containsString(expectedMessage))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);
  }

  @Test
  public void testActivateNonParseableDateFormat_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("includeProcessInstances", false);
    params.put("executionDate", "a");

    String expectedMessage = "Invalid format: \"a\"";
    String exceptionMessage = "The suspension state of Process Definition with id " + MockProvider.EXAMPLE_PROCESS_DEFINITION_ID + " could not be updated due to: " + expectedMessage;

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(exceptionMessage))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);
  }

  @Test
  public void testActivateProcessDefinitionThrowsAuthorizationException_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(repositoryServiceMock).activateProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionExcludingInstances_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", false);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);
  }

  @Test
  public void testDelayedSuspendProcessDefinitionExcludingInstances_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", false);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, executionDate);
  }

  @Test
  public void testSuspendProcessDefinitionIncludingInstances_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", true);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, null);
  }



  @Test
  public void testDelayedSuspendProcessDefinitionIncludingInstances_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", true);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDate(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);

    verify(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, executionDate);
  }

  @Test
  public void testSuspendThrowsProcessEngineException_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(repositoryServiceMock)
      .suspendProcessDefinitionById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID), eq(false), isNull());

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", containsString(expectedMessage))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);
  }

  @Test
  public void testSuspendNonParseableDateFormat_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("includeProcessInstances", false);
    params.put("executionDate", "a");

    String expectedMessage = "Invalid format: \"a\"";
    String exceptionMessage = "The suspension state of Process Definition with id " + MockProvider.EXAMPLE_PROCESS_DEFINITION_ID + " could not be updated due to: " + expectedMessage;

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(exceptionMessage))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);
  }

  @Test
  public void testSuspendProcessDefinitionThrowsAuthorizationException_ByKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(repositoryServiceMock).suspendProcessDefinitionById(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(SINGLE_PROCESS_DEFINITION_BY_KEY_SUSPENDED_URL);
  }

  @Test
  public void testProcessInstantiationWithCaseInstanceId() throws IOException {
    Map<String, Object> json = new HashMap<>();
    json.put("caseInstanceId", "myCaseInstanceId");

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).caseInstanceId("myCaseInstanceId");
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  @Test
  public void testProcessInstantiationWithCaseInstanceIdAndBusinessKey() throws IOException {
    Map<String, Object> json = new HashMap<>();
    json.put("caseInstanceId", "myCaseInstanceId");
    json.put("businessKey", "myBusinessKey");

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).businessKey("myBusinessKey");
    verify(mockInstantiationBuilder).caseInstanceId("myCaseInstanceId");
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  @Test
  public void testProcessInstantiationWithCaseInstanceIdAndBusinessKeyAndParameters() throws IOException {
    Map<String, Object> json = new HashMap<>();
    json.put("caseInstanceId", "myCaseInstanceId");
    json.put("businessKey", "myBusinessKey");

    Map<String, Object> parameters = VariablesBuilder.create()
        .variable("aBoolean", Boolean.TRUE)
        .variable("aString", "aStringVariableValue")
        .variable("anInteger", 42).getVariables();

    json.put("variables", parameters);

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);

    Map<String, Object> expectedParameters = new HashMap<>();
    expectedParameters.put("aBoolean", Boolean.TRUE);
    expectedParameters.put("aString", "aStringVariableValue");
    expectedParameters.put("anInteger", 42);

    verify(runtimeServiceMock).createProcessInstanceById(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(mockInstantiationBuilder).businessKey("myBusinessKey");
    verify(mockInstantiationBuilder).caseInstanceId("myCaseInstanceId");
    verify(mockInstantiationBuilder).setVariables(argThat(new EqualsMap(expectedParameters)));
    verify(mockInstantiationBuilder).executeWithVariablesInReturn(anyBoolean(), anyBoolean());
  }

  @Test
  public void testGetStartFormVariablesThrowsAuthorizationException_ByKey() {
    String message = "expected exception";
    when(formServiceMock.getStartFormVariables(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, null, true)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(START_FORM_VARIABLES_BY_KEY_URL);
  }

  @Test
  public void testGetDeployedStartForm_ByKey() {
    InputStream deployedStartFormMock = new ByteArrayInputStream("Test".getBytes());
    when(formServiceMock.getDeployedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .thenReturn(deployedStartFormMock);

    given()
    .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
    .statusCode(Status.OK.getStatusCode())
    .body(equalTo("Test"))
    .when()
    .get(DEPLOYED_START_FORM_BY_KEY_URL);

    verify(formServiceMock).getDeployedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testGetDeployedStartForm() {
    InputStream deployedStartFormMock = new ByteArrayInputStream("Test".getBytes());
    when(formServiceMock.getDeployedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .thenReturn(deployedStartFormMock);

    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
    .statusCode(Status.OK.getStatusCode())
    .body(equalTo("Test"))
    .contentType(MediaType.APPLICATION_XHTML_XML)
    .when()
    .get(DEPLOYED_START_FORM_URL);

    verify(formServiceMock).getDeployedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testGetDeployedStartFormJson() {
    InputStream deployedStartFormMock = new ByteArrayInputStream("Test".getBytes());
    when(formServiceMock.getDeployedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .thenReturn(deployedStartFormMock);
    when(formServiceMock.getStartFormKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .thenReturn("test.form");

    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
    .statusCode(Status.OK.getStatusCode())
    .body(equalTo("Test"))
    .contentType(MediaType.APPLICATION_JSON)
    .when()
    .get(DEPLOYED_START_FORM_URL);

    verify(formServiceMock).getDeployedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testGetDeployedStartFormWithoutAuthorization() {
    String message = "unauthorized";
    when(formServiceMock.getDeployedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .thenThrow(new AuthorizationException(message));

    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
    .statusCode(Status.FORBIDDEN.getStatusCode())
    .body("message", equalTo(message))
    .when()
    .get(DEPLOYED_START_FORM_URL);
  }

  @Test
  public void testGetDeployedStartFormWithWrongFormKeyFormat() {
    String message = "wrong key format";
    when(formServiceMock.getDeployedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .thenThrow(new BadUserRequestException(message));

    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
    .statusCode(Status.BAD_REQUEST.getStatusCode())
    .body("message", equalTo(message))
    .when()
    .get(DEPLOYED_START_FORM_URL);
  }

  @Test
  public void testGetDeployedStartFormWithUnexistingForm() {
    String message = "not found";
    when(formServiceMock.getDeployedStartForm(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .thenThrow(new NotFoundException(message));

    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
    .statusCode(Status.NOT_FOUND.getStatusCode())
    .body("message", equalTo(message))
    .when()
    .get(DEPLOYED_START_FORM_URL);
  }

  @Test
  public void testGetStaticCalledProcessDefinitions() {
    CalledProcessDefinition mock = mock(CalledProcessDefinitionImpl.class);
    when(mock.getCalledFromActivityIds()).thenReturn(Arrays.asList("anActivity", "anotherActivity"));
    when(mock.getId()).thenReturn("aKey:1:123");
    when(mock.getCallingProcessDefinitionId()).thenReturn("aCallingId");
    when(mock.getName()).thenReturn("a Name");
    when(mock.getKey()).thenReturn("aKey");
    when(mock.getVersion()).thenReturn(1);
    List<CalledProcessDefinition> result = Collections.singletonList(mock);
    when(repositoryServiceMock.getStaticCalledProcessDefinitions(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .thenReturn(result);

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
      .body("[0].callingProcessDefinitionId", equalTo(mock.getCallingProcessDefinitionId()))
      .body("[0].id", equalTo(mock.getId()))
      .body("[0].key", equalTo(mock.getKey()))
      .body("[0].name", equalTo(mock.getName()))
      .body("[0].version", equalTo(mock.getVersion()))
      .body("[0].calledFromActivityIds[0]", equalTo("anActivity"))
      .body("[0].calledFromActivityIds[1]", equalTo("anotherActivity"))
    .when()
      .get(PROCESS_DEFINITION_CALL_ACTIVITY_MAPPINGS);
  }

  @Test
  public void testGetStaticCalledProcessDefinitionNonExistingProcess() {

    when(repositoryServiceMock.getStaticCalledProcessDefinitions("NonExistingId")).thenThrow(
      new NotFoundException());

    given()
      .pathParam("id", "NonExistingId")
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
    .when()
      .get(PROCESS_DEFINITION_CALL_ACTIVITY_MAPPINGS);
  }

  @Test
  public void shouldReturnErrorCodeWhenStartingProcessInstance() {
    when(mockInstantiationBuilder.executeWithVariablesInReturn(anyBoolean(), anyBoolean()))
      .thenThrow(new ProcessEngineException("foo", 123));

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot instantiate process definition aProcDefId: foo"))
      .body("code", equalTo(123))
    .when()
      .post(START_PROCESS_INSTANCE_BY_KEY_URL);
  }

  @Test
  public void shouldReturnErrorCodeWhenSubmittingForm() {
    doThrow(new ProcessEngineException("foo", 123))
        .when(formServiceMock).submitStartForm(any(String.class), Mockito.any());

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot instantiate process definition aProcDefId: foo"))
      .body("code", equalTo(123))
    .when()
      .post(SUBMIT_FORM_URL);
  }

}
