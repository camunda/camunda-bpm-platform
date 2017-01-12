/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.helper.MockObjectValue;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.MockVariableInstanceBuilder;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * @author Daniel Meyer
 *
 */
public class VariableInstanceRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/variable-instance";
  protected static final String VARIABLE_INSTANCE_URL = SERVICE_URL + "/{id}";
  protected static final String VARIABLE_INSTANCE_BINARY_DATA_URL = VARIABLE_INSTANCE_URL + "/data";

  protected RuntimeService runtimeServiceMock;

  protected VariableInstanceQuery variableInstanceQueryMock;

  @Before
  public void setupTestData() {
    runtimeServiceMock = mock(RuntimeService.class);
    variableInstanceQueryMock = mock(VariableInstanceQuery.class);

    // mock runtime service.
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
    when(runtimeServiceMock.createVariableInstanceQuery()).thenReturn(variableInstanceQueryMock);
  }

  @Test
  public void testGetSingleVariableInstance() {

    MockVariableInstanceBuilder builder = MockProvider.mockVariableInstance();
    VariableInstance variableInstanceMock = builder.build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("name", equalTo(builder.getName()))
      .body("type", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo(builder.getTypedValue().getValue()))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("executionId", equalTo(builder.getExecutionId()))
      .body("caseInstanceId", equalTo(builder.getCaseInstanceId()))
      .body("caseExecutionId", equalTo(builder.getCaseExecutionId()))
      .body("taskId", equalTo(builder.getTaskId()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
      .body("tenantId", equalTo(builder.getTenantId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();
  }

  @Test
  public void testGetSingleVariableInstanceDeserialized() {
    ObjectValue serializedValue = MockObjectValue.fromObjectValue(
        Variables.objectValue("a value").serializationDataFormat("aDataFormat").create())
        .objectTypeName("aTypeName");

    MockVariableInstanceBuilder builder = MockProvider.mockVariableInstance().typedValue(serializedValue);
    VariableInstance variableInstanceMock = builder.build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given()
      .pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("name", equalTo(builder.getName()))
      .body("type", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo("a value"))
      .body("valueInfo.serializationDataFormat", equalTo("aDataFormat"))
      .body("valueInfo.objectTypeName", equalTo("aTypeName"))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("executionId", equalTo(builder.getExecutionId()))
      .body("caseInstanceId", equalTo(builder.getCaseInstanceId()))
      .body("caseExecutionId", equalTo(builder.getCaseExecutionId()))
      .body("taskId", equalTo(builder.getTaskId()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
      .body("tenantId", equalTo(builder.getTenantId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();
    verify(variableInstanceQueryMock, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testGetSingleVariableInstanceSerialized() {
    ObjectValue serializedValue = Variables.serializedObjectValue("a serialized value")
        .serializationDataFormat("aDataFormat").objectTypeName("aTypeName").create();

    MockVariableInstanceBuilder builder = MockProvider.mockVariableInstance().typedValue(serializedValue);
    VariableInstance variableInstanceMock = builder.build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given()
      .pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
      .queryParam("deserializeValue", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("id", equalTo(builder.getId()))
      .body("name", equalTo(builder.getName()))
      .body("type", equalTo(VariableTypeHelper.toExpectedValueTypeName(builder.getTypedValue().getType())))
      .body("value", equalTo("a serialized value"))
      .body("valueInfo.serializationDataFormat", equalTo("aDataFormat"))
      .body("valueInfo.objectTypeName", equalTo("aTypeName"))
      .body("processInstanceId", equalTo(builder.getProcessInstanceId()))
      .body("executionId", equalTo(builder.getExecutionId()))
      .body("caseInstanceId", equalTo(builder.getCaseInstanceId()))
      .body("caseExecutionId", equalTo(builder.getCaseExecutionId()))
      .body("taskId", equalTo(builder.getTaskId()))
      .body("activityInstanceId", equalTo(builder.getActivityInstanceId()))
      .body("tenantId", equalTo(builder.getTenantId()))
      .body("errorMessage", equalTo(builder.getErrorMessage()))
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();
    verify(variableInstanceQueryMock, times(1)).disableCustomObjectDeserialization();
  }

  @Test
  public void testGetSingleVariableInstanceForBinaryVariable() {
    MockVariableInstanceBuilder builder = MockProvider.mockVariableInstance();
    VariableInstance variableInstanceMock =
        builder
          .typedValue(Variables.byteArrayValue(null))
          .build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .body("type", equalTo(VariableTypeHelper.toExpectedValueTypeName(ValueType.BYTES)))
      .body("value", nullValue())
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();

  }

  @Test
  public void testGetNonExistingVariableInstance() {

    String nonExistingId = "nonExistingId";

    when(variableInstanceQueryMock.variableId(nonExistingId)).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", nonExistingId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Variable instance with Id 'nonExistingId' does not exist."))
    .when().get(VARIABLE_INSTANCE_URL);

    verify(variableInstanceQueryMock, times(1)).disableBinaryFetching();
  }

  @Test
  public void testBinaryDataForBinaryVariable() {
    final byte[] byteContent = "some bytes".getBytes();

    VariableInstance variableInstanceMock =
        MockProvider.mockVariableInstance()
          .typedValue(Variables.byteArrayValue(byteContent))
          .build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.BINARY.toString())
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    byte[] responseBytes = response.getBody().asByteArray();
    Assert.assertEquals(new String(byteContent), new String(responseBytes));
    verify(variableInstanceQueryMock, never()).disableBinaryFetching();
    verify(variableInstanceQueryMock).disableCustomObjectDeserialization();

  }

  @Test
  public void testBinaryDataForNonBinaryVariable() {
    VariableInstance variableInstanceMock = MockProvider.createMockVariableInstance();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("Value of variable with id aVariableInstanceId is not a binary value"))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    verify(variableInstanceQueryMock, never()).disableBinaryFetching();
    verify(variableInstanceQueryMock).disableCustomObjectDeserialization();

  }

  @Test
  public void testGetBinaryDataForNonExistingVariableInstance() {

    String nonExistingId = "nonExistingId";

    when(variableInstanceQueryMock.variableId(nonExistingId)).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", nonExistingId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Variable instance with Id 'nonExistingId' does not exist."))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

    verify(variableInstanceQueryMock, never()).disableBinaryFetching();
    verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
  }

  @Test
  public void testGetBinaryDataForFileVariable() {
    String filename = "test.txt";
    byte[] byteContent = "test".getBytes();
    String encoding = "UTF-8";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).encoding(encoding).create();

    MockVariableInstanceBuilder builder = MockProvider.mockVariableInstance();
    VariableInstance variableInstanceMock =
        builder
          .typedValue(variableValue)
          .build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .header("Content-Disposition", "attachment; filename="+filename)
    .and()
      .body(is(equalTo(new String(byteContent))))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);
    //due to some problems with wildfly we gotta check this separately
    String contentType = response.getContentType();
    assertThat(contentType, is(either(CoreMatchers.<Object>equalTo(ContentType.TEXT.toString() + "; charset=UTF-8")).or(CoreMatchers.<Object>equalTo(ContentType.TEXT.toString() + ";charset=UTF-8"))));
  }

  @Test
  public void testGetBinaryDataForNullFileVariable() {
    String filename = "test.txt";
    byte[] byteContent = null;
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).create();

    MockVariableInstanceBuilder builder = MockProvider.mockVariableInstance();
    VariableInstance variableInstanceMock =
        builder
          .typedValue(variableValue)
          .build();

    when(variableInstanceQueryMock.variableId(variableInstanceMock.getId())).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableBinaryFetching()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.disableCustomObjectDeserialization()).thenReturn(variableInstanceQueryMock);
    when(variableInstanceQueryMock.singleResult()).thenReturn(variableInstanceMock);

    given().pathParam("id", MockProvider.EXAMPLE_VARIABLE_INSTANCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .and()
      .contentType(ContentType.TEXT)
      .and()
        .body(is(equalTo(new String())))
    .when().get(VARIABLE_INSTANCE_BINARY_DATA_URL);

  }

}
