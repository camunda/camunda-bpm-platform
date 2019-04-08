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
package org.camunda.bpm.client.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_BAR;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.ProcessModels.TWO_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.engine.variable.Variables.SerializationDataFormats.JSON;
import static org.camunda.bpm.engine.variable.type.ValueType.OBJECT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.client.util.RecordingInvocationHandler;
import org.camunda.bpm.client.util.RecordingInvocationHandler.RecordedInvocation;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.skyscreamer.jsonassert.JSONAssert;

public class JsonSerializationIT {

  protected static final String VARIABLE_NAME_JSON = "jsonVariable";
  protected static final String JSON_DATAFORMAT_NAME = JSON.getName();

  protected static final JsonSerializable VARIABLE_VALUE_JSON_DESERIALIZED = new JsonSerializable("a String", 42, true);

  protected static final String VARIABLE_VALUE_JSON_SERIALIZED = VARIABLE_VALUE_JSON_DESERIALIZED.toExpectedJsonString();
  protected static final String VARIABLE_VALUE_JSON_LIST_SERIALIZED = String.format("[%s, %s]", VARIABLE_VALUE_JSON_SERIALIZED, VARIABLE_VALUE_JSON_SERIALIZED);

  protected static final ObjectValue VARIABLE_VALUE_JSON_OBJECT_VALUE = Variables
      .serializedObjectValue(VARIABLE_VALUE_JSON_DESERIALIZED.toExpectedJsonString())
      .objectTypeName(JsonSerializable.class.getName())
      .serializationDataFormat(JSON_DATAFORMAT_NAME)
      .create();

  protected static final ObjectValue VARIABLE_VALUE_JSON_LIST_OBJECT_VALUE = Variables
      .serializedObjectValue(VARIABLE_VALUE_JSON_LIST_SERIALIZED)
      .objectTypeName(String.format("java.util.ArrayList<%s>", JsonSerializable.class.getName()))
      .serializationDataFormat(JSON_DATAFORMAT_NAME)
      .create();

  protected ClientRule clientRule = new ClientRule();
  protected EngineRule engineRule = new EngineRule();
  protected ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule).around(thrown);

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;
  protected ProcessInstanceDto processInstance;

  protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();
  protected RecordingInvocationHandler invocationHandler = new RecordingInvocationHandler();

  @Before
  public void setup() throws Exception {
    client = clientRule.client();
    processDefinition = engineRule.deploy(TWO_EXTERNAL_TASK_PROCESS).get(0);

    handler.clear();
    invocationHandler.clear();
  }

  @Test
  public void shouldGetDeserializedJson() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    JsonSerializable variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_JSON_DESERIALIZED);
  }

  @Test
  public void shouldGetTypedDeserializedJson() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(typedValue.getValue()).isEqualTo(VARIABLE_VALUE_JSON_DESERIALIZED);
    assertThat(typedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shouldGetTypedSerializedJson() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_DESERIALIZED.toExpectedJsonString(), new String(typedValue.getValueSerialized()), true);
    assertThat(typedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isFalse();
  }

  @Test
  public void shouldGetJsonAsList() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_LIST_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    List<JsonSerializable> variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).hasSize(2);
    assertThat(variableValue).containsExactly(VARIABLE_VALUE_JSON_DESERIALIZED, VARIABLE_VALUE_JSON_DESERIALIZED);
  }

  @Test
  public void shouldGetTypedDeserializedJsonAsList() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(typedValue.getValue()).isEqualTo(VARIABLE_VALUE_JSON_DESERIALIZED);
    assertThat(typedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shouldGetTypedSerializedJsonAsList() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_DESERIALIZED.toExpectedJsonString(), new String(typedValue.getValueSerialized()), true);
    assertThat(typedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isFalse();
  }

  @Test
  public void shouldFailWhileDeserialization() {
    // given
    ObjectValue objectValue = Variables.serializedObjectValue(VARIABLE_VALUE_JSON_SERIALIZED)
      .objectTypeName(FailingDeserializationBean.class.getName())
      .serializationDataFormat(JSON_DATAFORMAT_NAME)
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, objectValue);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariable(VARIABLE_NAME_JSON);
  }

  @Test
  public void shouldFailWhileDeserializationTypedValue() {
    // given
    ObjectValue objectValue = Variables.serializedObjectValue(VARIABLE_VALUE_JSON_SERIALIZED)
      .objectTypeName(FailingDeserializationBean.class.getName())
      .serializationDataFormat(JSON_DATAFORMAT_NAME)
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, objectValue);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariableTyped(VARIABLE_NAME_JSON);
  }

  @Test
  public void shouldStillReturnSerializedJsonWhenDeserializationFails() throws JSONException {
    // given
    ObjectValue objectValue = Variables.serializedObjectValue(VARIABLE_VALUE_JSON_SERIALIZED)
      .objectTypeName(FailingDeserializationBean.class.getName())
      .serializationDataFormat(JSON_DATAFORMAT_NAME)
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, objectValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    try {
      task.getVariableTyped(VARIABLE_NAME_JSON);
      fail("exception expected");
    }
    catch (Exception e) {
    }

    // However, the serialized value can be accessed
    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_DESERIALIZED.toExpectedJsonString(), new String(typedValue.getValueSerialized()), true);
    assertThat(typedValue.getObjectTypeName()).isNotNull();
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isFalse();

    // but not the deserialized properties
    try {
      typedValue.getValue();
      fail("exception expected");
    }
    catch(IllegalStateException e) {
    }

    try {
      typedValue.getValue(JsonSerializable.class);
      fail("exception expected");
    }
    catch(IllegalStateException e) {
    }

    try {
      typedValue.getObjectType();
      fail("exception expected");
    }
    catch(IllegalStateException e) {
    }
  }

  @Test
  public void shouldFailWhileDeserializationDueToMismatchingTypeName() throws JSONException {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue(VARIABLE_VALUE_JSON_SERIALIZED)
      .serializationDataFormat(JSON_DATAFORMAT_NAME)
      .objectTypeName("Insensible type name")  // < not a valid type name
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, serializedValue);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariable(VARIABLE_NAME_JSON);
  }

  @Test
  public void shouldFailWhileDeserializationDueToWrongTypeName() throws JSONException {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue(VARIABLE_VALUE_JSON_SERIALIZED)
      .serializationDataFormat(JSON_DATAFORMAT_NAME)
      .objectTypeName(JsonSerializationIT.class.getName())  // < not the right type name
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, serializedValue);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariable(VARIABLE_NAME_JSON);
  }

  @Test
  public void shouldDeserializeNull() throws JSONException {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue()
        .serializationDataFormat(JSON_DATAFORMAT_NAME)
        .objectTypeName(JsonSerializable.class.getName())
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, serializedValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    JsonSerializable returnedBean = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(returnedBean).isNull();
  }

  @Test
  public void shouldDeserializeNullTyped() throws JSONException {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue()
        .serializationDataFormat(JSON_DATAFORMAT_NAME)
        .objectTypeName(JsonSerializable.class.getName())
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, serializedValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(typedValue.getValue()).isNull();
    assertThat(typedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shouldDeserializeNullWithoutTypeName()  {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue()
        .serializationDataFormat(JSON_DATAFORMAT_NAME)
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, serializedValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    JsonSerializable returnedBean = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(returnedBean).isNull();
  }

  @Test
  public void shouldDeserializeNullTypedWithoutTypeName()  {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue()
        .serializationDataFormat(JSON_DATAFORMAT_NAME)
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, serializedValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(typedValue.getValue()).isNull();
    assertThat(typedValue.getObjectTypeName()).isNull();
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shoudSetVariable() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_DESERIALIZED);
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_SERIALIZED, new String(serializedValue.getValueSerialized()), true);
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isEqualTo(VARIABLE_VALUE_JSON_DESERIALIZED);
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());

    JsonSerializable variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_JSON_DESERIALIZED);
  }

  @Test
  public void shoudSetVariableTyped() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_OBJECT_VALUE);
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_DESERIALIZED.toExpectedJsonString(), new String(serializedValue.getValueSerialized()), true);
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isEqualTo(VARIABLE_VALUE_JSON_DESERIALIZED);
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());

    JsonSerializable variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_JSON_DESERIALIZED);
  }

  @Test
  public void shoudSetVariableUntyped() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_JSON, Variables.untypedValue(VARIABLE_VALUE_JSON_DESERIALIZED));
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_DESERIALIZED.toExpectedJsonString(), new String(serializedValue.getValueSerialized()), true);
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isEqualTo(VARIABLE_VALUE_JSON_DESERIALIZED);
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isEqualTo(JsonSerializable.class.getName());

    JsonSerializable variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_JSON_DESERIALIZED);
  }

  @Test
  public void shoudSetVariableTyped_Null() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_JSON, Variables.objectValue(null)
        .serializationDataFormat(JSON)
        .create());
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    assertThat(serializedValue.getValueSerialized()).isNull();
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isNull();

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isNull();
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isNull();

    JsonSerializable variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isNull();
  }

  @Test
  public void shoudSetJsonListVariable() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    List<JsonSerializable> variable = new ArrayList<>();
    variable.add(VARIABLE_VALUE_JSON_DESERIALIZED);
    variable.add(VARIABLE_VALUE_JSON_DESERIALIZED);

    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_JSON, variable);
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_LIST_SERIALIZED, new String(serializedValue.getValueSerialized()), true);
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isEqualTo("java.util.ArrayList<org.camunda.bpm.client.variable.JsonSerializable>");

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isEqualTo(variable);
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isEqualTo("java.util.ArrayList<org.camunda.bpm.client.variable.JsonSerializable>");

    List<JsonSerializable> variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isEqualTo(variable);
  }

  @Test
  public void shoudSetJsonListVariableTyped() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    List<JsonSerializable> variable = new ArrayList<>();
    variable.add(VARIABLE_VALUE_JSON_DESERIALIZED);
    variable.add(VARIABLE_VALUE_JSON_DESERIALIZED);

    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_JSON, Variables.objectValue(variable).create());
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_LIST_SERIALIZED, new String(serializedValue.getValueSerialized()), true);
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isEqualTo("java.util.ArrayList<org.camunda.bpm.client.variable.JsonSerializable>");

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isEqualTo(variable);
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isEqualTo("java.util.ArrayList<org.camunda.bpm.client.variable.JsonSerializable>");

    List<JsonSerializable> variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isEqualTo(variable);
  }

  @Test
  public void shouldFailWithMapperNotFound() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // then
    thrown.expect(ValueMapperException.class);

    // when
    Map<String, Object> variables = Variables.createVariables();
    ObjectValue objectValue = Variables.objectValue(VARIABLE_VALUE_JSON_DESERIALIZED)
        .serializationDataFormat("not existing data format")
        .create();
    variables.put(VARIABLE_NAME_JSON, objectValue);
    fooService.complete(fooTask, variables);
  }

}
