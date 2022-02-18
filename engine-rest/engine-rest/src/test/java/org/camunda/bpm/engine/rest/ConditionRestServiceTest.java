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

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ConditionEvaluationBuilderImpl;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.ConditionEvaluationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import io.restassured.response.Response;

public class ConditionRestServiceTest extends AbstractRestServiceTest {

  protected static final String CONDITION_URL = TEST_RESOURCE_ROOT_PATH + ConditionRestService.PATH;

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  private RuntimeService runtimeServiceMock;
  private ConditionEvaluationBuilder conditionEvaluationBuilderMock;
  private List<ProcessInstance> processInstancesMock;

  @Before
  public void setupMocks() {
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);

    conditionEvaluationBuilderMock = mock(ConditionEvaluationBuilderImpl.class);

    when(runtimeServiceMock.createConditionEvaluation()).thenReturn(conditionEvaluationBuilderMock);
    when(conditionEvaluationBuilderMock.processDefinitionId(anyString())).thenReturn(conditionEvaluationBuilderMock);
    when(conditionEvaluationBuilderMock.processInstanceBusinessKey(anyString())).thenReturn(conditionEvaluationBuilderMock);
    when(conditionEvaluationBuilderMock.setVariables(Mockito.any())).thenReturn(conditionEvaluationBuilderMock);
    when(conditionEvaluationBuilderMock.setVariable(anyString(), any())).thenReturn(conditionEvaluationBuilderMock);

    processInstancesMock = MockProvider.createAnotherMockProcessInstanceList();
    when(conditionEvaluationBuilderMock.evaluateStartConditions()).thenReturn(processInstancesMock);
  }

  @Test
  public void testConditionEvaluationOnlyVariables() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    Map<String, Object> variables = VariablesBuilder
        .create()
        .variable("foo", "bar")
        .getVariables();
    parameters.put("variables", variables);

    Response response = given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CONDITION_URL);

    assertNotNull(response);
    String content = response.asString();
    assertTrue(!content.isEmpty());
    checkResult(content);

    verify(runtimeServiceMock).createConditionEvaluation();
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("foo", "bar");
    verify(conditionEvaluationBuilderMock).setVariables(expectedVariables);
    verify(conditionEvaluationBuilderMock).evaluateStartConditions();
    verifyNoMoreInteractions(conditionEvaluationBuilderMock);
  }

  @Test
  public void testConditionEvaluationWithProcessDefinition() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    Map<String, Object> variables = VariablesBuilder
        .create()
        .variable("foo", "bar")
        .getVariables();
    parameters.put("variables", variables);
    parameters.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CONDITION_URL);

    verify(runtimeServiceMock).createConditionEvaluation();
    verify(conditionEvaluationBuilderMock).processDefinitionId(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verify(conditionEvaluationBuilderMock).evaluateStartConditions();
  }

  @Test
  public void testConditionEvaluationWithBusinessKey() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    Map<String, Object> variables = VariablesBuilder
        .create()
        .variable("foo", "bar")
        .getVariables();
    parameters.put("variables", variables);
    parameters.put("businessKey", MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CONDITION_URL);

    verify(runtimeServiceMock).createConditionEvaluation();
    verify(conditionEvaluationBuilderMock).processInstanceBusinessKey(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY));
  }

  @Test
  public void testConditionEvaluationWithTenantId() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    Map<String, Object> variables = VariablesBuilder
        .create()
        .variable("foo", "bar")
        .getVariables();
    parameters.put("variables", variables);
    parameters.put("tenantId", MockProvider.EXAMPLE_TENANT_ID);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CONDITION_URL);

    verify(runtimeServiceMock).createConditionEvaluation();
    verify(conditionEvaluationBuilderMock).tenantId(MockProvider.EXAMPLE_TENANT_ID);
    verify(conditionEvaluationBuilderMock).evaluateStartConditions();
  }

  @Test
  public void testConditionEvaluationWithoutTenantId() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    Map<String, Object> variables = VariablesBuilder
        .create()
        .variable("foo", "bar")
        .getVariables();
    parameters.put("variables", variables);
    parameters.put("withoutTenantId", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CONDITION_URL);

    verify(runtimeServiceMock).createConditionEvaluation();
    verify(conditionEvaluationBuilderMock).withoutTenantId();
    verify(conditionEvaluationBuilderMock).evaluateStartConditions();
  }

  @Test
  public void testConditionEvaluationFailingInvalidTenantParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    Map<String, Object> variables = VariablesBuilder
        .create()
        .variable("foo", "bar")
        .getVariables();
    parameters.put("variables", variables);
    parameters.put("tenantId", MockProvider.EXAMPLE_TENANT_ID);
    parameters.put("withoutTenantId", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Parameter 'tenantId' cannot be used together with parameter 'withoutTenantId'."))
    .when()
      .post(CONDITION_URL);
  }

  @Test
  public void testConditionEvaluationThrowsAuthorizationException() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    Map<String, Object> variables = VariablesBuilder
        .create()
        .variable("foo", "bar")
        .getVariables();
    parameters.put("variables", variables);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(conditionEvaluationBuilderMock).evaluateStartConditions();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(CONDITION_URL);
  }

  protected void checkResult(String content) {
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, from(content).get("[" + 0 + "].id"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, from(content).get("[" + 0+ "].definitionId"));
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID, from(content).get("[" + 1 + "].id"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, from(content).get("[" + 1+ "].definitionId"));
  }

}
