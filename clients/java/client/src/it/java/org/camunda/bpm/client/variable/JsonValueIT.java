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
package org.camunda.bpm.client.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.ProcessModels.LOCK_DURATION;
import static org.camunda.bpm.client.util.ProcessModels.TWO_EXTERNAL_TASK_PROCESS;
import static org.camunda.spin.DataFormats.JSON_DATAFORMAT_NAME;
import static org.camunda.spin.plugin.variable.type.SpinValueType.JSON;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.skyscreamer.jsonassert.JSONAssert;

public class JsonValueIT {

  protected static final String VARIABLE_NAME_JSON = "jsonVariable";

  protected static final String VARIABLE_VALUE_JSON_SERIALIZED = "{\"foo\": \"bar\"}";
  protected static final String VARIABLE_VALUE_JSON_SERIALIZED_BROKEN = "{\"foo: \"bar\"}";

  protected static final JsonValue VARIABLE_VALUE_JSON_VALUE = SpinValues.jsonValue(VARIABLE_VALUE_JSON_SERIALIZED)
      .serializationDataFormat(JSON_DATAFORMAT_NAME)
      .create();

  protected static final JsonValue VARIABLE_VALUE_JSON_VALUE_BROKEN = SpinValues.jsonValue(VARIABLE_VALUE_JSON_SERIALIZED_BROKEN)
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

  @Before
  public void setup() throws Exception {
    client = clientRule.client();
    handler.clear();
    processDefinition = engineRule.deploy(TWO_EXTERNAL_TASK_PROCESS).get(0);
  }

  @Test
  public void shouldGetDeserializedJson() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(LOCK_DURATION)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    SpinJsonNode variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isNotNull();
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_SERIALIZED, variableValue.toString(), true);
  }

  @Test
  public void shouldGetTypedDeserializedJson() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(LOCK_DURATION)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    JsonValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(typedValue.getType()).isEqualTo(JSON);
    assertThat(typedValue.isDeserialized()).isTrue();

    SpinJsonNode jsonNode = typedValue.getValue();
    assertThat(jsonNode).isNotNull();
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_SERIALIZED, jsonNode.toString(), true);
  }

  @Test
  public void shouldGetTypedSerializedJson() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(LOCK_DURATION)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    JsonValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    assertThat(typedValue.getType()).isEqualTo(JSON);
    assertThat(typedValue.isDeserialized()).isFalse();

    String jsonValueSerialized = typedValue.getValueSerialized();
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_SERIALIZED, jsonValueSerialized, true);
  }

  @Test
  public void shouldDeserializeNull() throws JSONException {
    // given
    JsonValue jsonValue = SpinValues.jsonValue((String) null)
        .serializationDataFormat(JSON_DATAFORMAT_NAME)
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, jsonValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(LOCK_DURATION)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    SpinJsonNode returnedBean = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(returnedBean).isNull();
  }

  @Test
  public void shouldDeserializeNullTyped() throws JSONException {
    // given
    JsonValue jsonValue = SpinValues.jsonValue((String) null)
        .serializationDataFormat(JSON_DATAFORMAT_NAME)
        .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, jsonValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(LOCK_DURATION)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    JsonValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(typedValue.getValue()).isNull();
    assertThat(typedValue.getType()).isEqualTo(JSON);
    assertThat(typedValue.isDeserialized()).isTrue();
  }

  @Test
  public void shouldFailWithBrokenJsonWhileSerialization() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_VALUE_BROKEN);

    // then
    thrown.expect(RuntimeException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(LOCK_DURATION)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariable(VARIABLE_NAME_JSON);
  }

  @Test
  public void shouldFailWithBrokenJsonWhileSerializationTyped() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_VALUE_BROKEN);

    // then
    thrown.expect(RuntimeException.class);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(LOCK_DURATION)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    task.getVariableTyped(VARIABLE_NAME_JSON);
  }

  @Test
  public void shouldReturnBrokenSerializedJson() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_VALUE_BROKEN);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .lockDuration(LOCK_DURATION)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    JsonValue jsonValue = task.getVariableTyped(VARIABLE_NAME_JSON, false);
    String jsonSerialized = jsonValue.getValueSerialized();
    assertThat(jsonSerialized).isEqualTo(VARIABLE_VALUE_JSON_SERIALIZED_BROKEN);
  }
}
