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
import static org.camunda.bpm.client.util.ProcessModels.TWO_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.client.variable.ClientValues.JSON;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.client.variable.value.JsonValue;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.skyscreamer.jsonassert.JSONAssert;

public class JsonValueIT {

  protected static final String VARIABLE_NAME_JSON = "jsonVariable";

  protected static final String VARIABLE_VALUE_JSON_SERIALIZED = "{\"foo\": \"bar\"}";
  protected static final String VARIABLE_VALUE_JSON_SERIALIZED_BROKEN = "{\"foo: \"bar\"}";

  protected static final JsonValue VARIABLE_VALUE_JSON_VALUE = ClientValues.jsonValue(VARIABLE_VALUE_JSON_SERIALIZED);

  protected static final JsonValue VARIABLE_VALUE_JSON_VALUE_BROKEN = ClientValues.jsonValue(VARIABLE_VALUE_JSON_SERIALIZED_BROKEN);

  protected ClientRule clientRule = new ClientRule();
  protected EngineRule engineRule = new EngineRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule);

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
  public void shouldGetJson() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(VARIABLE_NAME_JSON);
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_SERIALIZED, variableValue, true);
  }

  @Test
  public void shouldGetTypedJson() throws JSONException {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    JsonValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(typedValue.getType()).isEqualTo(JSON);
    JSONAssert.assertEquals(VARIABLE_VALUE_JSON_SERIALIZED, typedValue.getValue(), true);
  }

  @Test
  public void shouldGetNull() {
    // given
    JsonValue jsonValue = ClientValues.jsonValue(null);

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, jsonValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isNull();
  }

  @Test
  public void shouldGetNullTyped() {
    // given
    JsonValue jsonValue = ClientValues.jsonValue(null);

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, jsonValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    JsonValue typedValue = task.getVariableTyped(VARIABLE_NAME_JSON);
    assertThat(typedValue.getType()).isEqualTo(JSON);
    assertThat(typedValue.getValue()).isNull();
  }

  @Test
  public void shouldReturnBrokenJson() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_JSON, VARIABLE_VALUE_JSON_VALUE_BROKEN);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(VARIABLE_NAME_JSON);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_JSON_SERIALIZED_BROKEN);
  }

}
