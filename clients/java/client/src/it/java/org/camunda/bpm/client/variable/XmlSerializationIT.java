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
import static org.camunda.bpm.engine.variable.Variables.SerializationDataFormats.XML;
import static org.camunda.bpm.engine.variable.type.ValueType.OBJECT;

import java.util.Arrays;
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
import org.camunda.spin.Spin;
import org.camunda.spin.SpinList;
import org.camunda.spin.xml.SpinXmlElement;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class XmlSerializationIT {

  protected static final String VARIABLE_NAME_XML = "xmlVariable";
  protected static final String XML_DATAFORMAT_NAME = XML.getName();

  protected static final XmlSerializable VARIABLE_VALUE_XML_DESERIALIZED = new XmlSerializable("a String", 42, true);
  protected static final XmlSerializables VARIABLE_VALUE_XML_LIST_DESERIALIZED = new XmlSerializables(Arrays.asList(VARIABLE_VALUE_XML_DESERIALIZED, VARIABLE_VALUE_XML_DESERIALIZED));

  protected static final XmlSerializableNoAnnotation VARIABLE_VALUE_XML_NO_ANNOTATION_DESERIALIZED = new XmlSerializableNoAnnotation("a String", 42, true);

  protected static final String VARIABLE_VALUE_XML_SERIALIZED = VARIABLE_VALUE_XML_DESERIALIZED.toExpectedXmlString();
  protected static final String VARIABLE_VALUE_XML_LIST_SERIALIZED = VARIABLE_VALUE_XML_LIST_DESERIALIZED.toExpectedXmlString();

  protected static final ObjectValue VARIABLE_VALUE_XML_OBJECT_VALUE = Variables
      .serializedObjectValue(VARIABLE_VALUE_XML_DESERIALIZED.toExpectedXmlString())
      .objectTypeName(XmlSerializable.class.getName())
      .serializationDataFormat(XML_DATAFORMAT_NAME)
      .create();

  protected static final ObjectValue VARIABLE_VALUE_XML_NO_ANNOTATION_OBJECT_VALUE = Variables
    .serializedObjectValue(VARIABLE_VALUE_XML_NO_ANNOTATION_DESERIALIZED.toExpectedXmlString())
    .objectTypeName(XmlSerializableNoAnnotation.class.getName())
    .serializationDataFormat(XML_DATAFORMAT_NAME)
    .create();

  protected static final ObjectValue VARIABLE_VALUE_XML_LIST_OBJECT_VALUE = Variables
      .serializedObjectValue(VARIABLE_VALUE_XML_LIST_SERIALIZED)
      .objectTypeName(XmlSerializables.class.getName())
      .serializationDataFormat(XML_DATAFORMAT_NAME)
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
  public void shouldGetDeserializedXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlSerializable variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_XML_DESERIALIZED);
  }

  @Test
  public void shouldGetDeserializedXmlNoAnnotation() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_NO_ANNOTATION_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlSerializableNoAnnotation variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_XML_NO_ANNOTATION_DESERIALIZED);
  }

  @Test
  public void shouldGetTypedDeserializedXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getValue()).isEqualTo(VARIABLE_VALUE_XML_DESERIALIZED);
    assertThat(typedValue.getObjectTypeName()).isEqualTo(XmlSerializable.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shouldGetTypedSerializedXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML, false);
    assertThat(typedValue.getObjectTypeName()).isEqualTo(XmlSerializable.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isFalse();

    SpinXmlElement serializedValue = Spin.XML(typedValue.getValueSerialized());
    assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getStringProperty()).isEqualTo(serializedValue.childElement("stringProperty").textContent());
    assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getBooleanProperty()).isEqualTo(Boolean.parseBoolean(serializedValue.childElement("booleanProperty").textContent()));
    assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getIntProperty()).isEqualTo(Integer.parseInt(serializedValue.childElement("intProperty").textContent()));
  }

  @Test
  public void shouldGetXmlAsList() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_LIST_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlSerializables variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue.size()).isEqualTo(2);
    assertThat(variableValue.get(0)).isEqualTo(VARIABLE_VALUE_XML_DESERIALIZED);
    assertThat(variableValue.get(1)).isEqualTo(VARIABLE_VALUE_XML_DESERIALIZED);
  }

  @Test
  public void shouldGetTypedDeserializedXmlAsList() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_LIST_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getValue()).isEqualTo(VARIABLE_VALUE_XML_LIST_DESERIALIZED);
    assertThat(typedValue.getObjectTypeName()).isEqualTo(XmlSerializables.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shouldGetTypedSerializedXmlAsList() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_LIST_OBJECT_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML, false);
    assertThat(typedValue.getObjectTypeName()).isEqualTo(XmlSerializables.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isFalse();

    SpinXmlElement serializedValue = Spin.XML(typedValue.getValueSerialized());
    SpinList<SpinXmlElement> childElements = serializedValue.childElements();
    childElements.forEach((c) -> {
      assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getStringProperty()).isEqualTo(c.childElement("stringProperty").textContent());
      assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getBooleanProperty()).isEqualTo(Boolean.parseBoolean(c.childElement("booleanProperty").textContent()));
      assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getIntProperty()).isEqualTo(Integer.parseInt(c.childElement("intProperty").textContent()));
    });
  }

  @Test
  public void shouldFailWhileDeserialization() {
    // given
    ObjectValue objectValue = Variables.serializedObjectValue(VARIABLE_VALUE_XML_SERIALIZED)
      .objectTypeName(FailingDeserializationBean.class.getName())
      .serializationDataFormat(XML_DATAFORMAT_NAME)
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, objectValue);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariable(VARIABLE_NAME_XML);
  }

  @Test
  public void shouldFailWhileDeserializationTypedValue() {
    // given
    ObjectValue objectValue = Variables.serializedObjectValue(VARIABLE_VALUE_XML_SERIALIZED)
      .objectTypeName(FailingDeserializationBean.class.getName())
      .serializationDataFormat(XML_DATAFORMAT_NAME)
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, objectValue);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariableTyped(VARIABLE_NAME_XML);
  }

  @Test
  public void shouldStillReturnSerializedXmlWhenDeserializationFails() {
    // given
    ObjectValue objectValue = Variables.serializedObjectValue(VARIABLE_VALUE_XML_SERIALIZED)
      .objectTypeName(FailingDeserializationBean.class.getName())
      .serializationDataFormat(XML_DATAFORMAT_NAME)
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, objectValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    try {
      task.getVariableTyped(VARIABLE_NAME_XML);
      fail("exception expected");
    }
    catch (Exception e) {
    }

    // However, the serialized value can be accessed
    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML, false);
    assertThat(typedValue.getObjectTypeName()).isNotNull();
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isFalse();

    SpinXmlElement serializedValue = Spin.XML(typedValue.getValueSerialized());
    assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getStringProperty()).isEqualTo(serializedValue.childElement("stringProperty").textContent());
    assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getBooleanProperty()).isEqualTo(Boolean.parseBoolean(serializedValue.childElement("booleanProperty").textContent()));
    assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getIntProperty()).isEqualTo(Integer.parseInt(serializedValue.childElement("intProperty").textContent()));

    // but not the deserialized properties
    try {
      typedValue.getValue();
      fail("exception expected");
    }
    catch(IllegalStateException e) {
    }

    try {
      typedValue.getValue(XmlSerializable.class);
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
  public void shouldFailWhileDeserializationDueToMismatchingTypeName() {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue(VARIABLE_VALUE_XML_SERIALIZED)
      .serializationDataFormat(XML_DATAFORMAT_NAME)
      .objectTypeName("Insensible type name")  // < not a valid type name
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, serializedValue);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariable(VARIABLE_NAME_XML);
  }

  @Test
  public void shouldFailWhileDeserializationDueToWrongTypeName() {
    // given

    // not reachable class
    class Foo {}

    ObjectValue serializedValue = Variables.serializedObjectValue(VARIABLE_VALUE_XML_SERIALIZED)
      .serializationDataFormat(XML_DATAFORMAT_NAME)
      .objectTypeName(Foo.class.getName())  // < not the right type name
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, serializedValue);

    // then
    thrown.expect(ValueMapperException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariable(VARIABLE_NAME_XML);
  }

  @Test
  public void shouldDeserializeNull() {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue()
        .serializationDataFormat(XML_DATAFORMAT_NAME)
        .objectTypeName(XmlSerializable.class.getName())
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, serializedValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlSerializable returnedBean = task.getVariable(VARIABLE_NAME_XML);
    assertThat(returnedBean).isNull();
  }

  @Test
  public void shouldDeserializeNullTyped() {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue()
        .serializationDataFormat(XML_DATAFORMAT_NAME)
        .objectTypeName(XmlSerializable.class.getName())
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, serializedValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getValue()).isNull();
    assertThat(typedValue.getObjectTypeName()).isEqualTo(XmlSerializable.class.getName());
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shouldDeserializeNullWithoutTypeName()  {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue()
        .serializationDataFormat(XML_DATAFORMAT_NAME)
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, serializedValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlSerializable returnedBean = task.getVariable(VARIABLE_NAME_XML);
    assertThat(returnedBean).isNull();
  }

  @Test
  public void shouldDeserializeNullTypedWithoutTypeName()  {
    // given
    ObjectValue serializedValue = Variables.serializedObjectValue()
        .serializationDataFormat(XML_DATAFORMAT_NAME)
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, serializedValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getValue()).isNull();
    assertThat(typedValue.getObjectTypeName()).isNull();
    assertThat(typedValue.getType()).isEqualTo(OBJECT);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shoudSetVariableTyped() {
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
    variables.put(VARIABLE_NAME_XML, Variables.objectValue(VARIABLE_VALUE_XML_DESERIALIZED).serializationDataFormat(XML).create());
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_XML, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isEqualTo(XmlSerializable.class.getName());

    SpinXmlElement spinElement = Spin.XML(serializedValue.getValueSerialized());
    assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getStringProperty()).isEqualTo(spinElement.childElement("stringProperty").textContent());
    assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getBooleanProperty()).isEqualTo(Boolean.parseBoolean(spinElement.childElement("booleanProperty").textContent()));
    assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getIntProperty()).isEqualTo(Integer.parseInt(spinElement.childElement("intProperty").textContent()));

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isEqualTo(VARIABLE_VALUE_XML_DESERIALIZED);
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isEqualTo(XmlSerializable.class.getName());

    XmlSerializable variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_XML_DESERIALIZED);
  }

  @Test
  public void shoudSetVariableTypedNoAnnotation() {
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
    variables.put(VARIABLE_NAME_XML, Variables.objectValue(VARIABLE_VALUE_XML_NO_ANNOTATION_DESERIALIZED).serializationDataFormat(XML).create());
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_XML, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isEqualTo(XmlSerializableNoAnnotation.class.getName());

    SpinXmlElement spinElement = Spin.XML(serializedValue.getValueSerialized());
    assertThat(VARIABLE_VALUE_XML_NO_ANNOTATION_DESERIALIZED.getStringProperty()).isEqualTo(spinElement.childElement("stringProperty").textContent());
    assertThat(VARIABLE_VALUE_XML_NO_ANNOTATION_DESERIALIZED.getBooleanProperty()).isEqualTo(Boolean.parseBoolean(spinElement.childElement("booleanProperty").textContent()));
    assertThat(VARIABLE_VALUE_XML_NO_ANNOTATION_DESERIALIZED.getIntProperty()).isEqualTo(Integer.parseInt(spinElement.childElement("intProperty").textContent()));

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isEqualTo(VARIABLE_VALUE_XML_NO_ANNOTATION_DESERIALIZED);
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isEqualTo(XmlSerializableNoAnnotation.class.getName());

    XmlSerializableNoAnnotation variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_XML_NO_ANNOTATION_DESERIALIZED);
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
    variables.put(VARIABLE_NAME_XML, Variables.objectValue(null)
        .serializationDataFormat(XML)
        .create());
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_XML, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    assertThat(serializedValue.getValueSerialized()).isNull();
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isNull();

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isNull();
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isNull();

    XmlSerializable variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isNull();
  }

  @Test
  public void shoudSetXmlListVariable() {
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
    variables.put(VARIABLE_NAME_XML, Variables.objectValue(VARIABLE_VALUE_XML_LIST_DESERIALIZED).serializationDataFormat(XML).create());
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    ObjectValue serializedValue = task.getVariableTyped(VARIABLE_NAME_XML, false);
    assertThat(serializedValue.isDeserialized()).isFalse();
    assertThat(serializedValue.getType()).isEqualTo(OBJECT);
    assertThat(serializedValue.getObjectTypeName()).isEqualTo("org.camunda.bpm.client.variable.XmlSerializables");

    SpinXmlElement spinElement = Spin.XML(serializedValue.getValueSerialized());
    SpinList<SpinXmlElement> childElements = spinElement.childElements();
    childElements.forEach((c) -> {
      assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getStringProperty()).isEqualTo(c.childElement("stringProperty").textContent());
      assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getBooleanProperty()).isEqualTo(Boolean.parseBoolean(c.childElement("booleanProperty").textContent()));
      assertThat(VARIABLE_VALUE_XML_DESERIALIZED.getIntProperty()).isEqualTo(Integer.parseInt(c.childElement("intProperty").textContent()));
    });

    ObjectValue deserializedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(deserializedValue.isDeserialized()).isTrue();
    assertThat(deserializedValue.getValue()).isEqualTo(VARIABLE_VALUE_XML_LIST_DESERIALIZED);
    assertThat(deserializedValue.getType()).isEqualTo(OBJECT);
    assertThat(deserializedValue.getObjectTypeName()).isEqualTo("org.camunda.bpm.client.variable.XmlSerializables");

    XmlSerializables variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_XML_LIST_DESERIALIZED);
  }

}
