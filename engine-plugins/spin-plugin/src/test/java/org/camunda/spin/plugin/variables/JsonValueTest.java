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
package org.camunda.spin.plugin.variables;

import static org.camunda.spin.DataFormats.json;
import static org.camunda.spin.plugin.variable.SpinValues.jsonValue;
import static org.camunda.spin.plugin.variable.type.SpinValueType.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.spin.DataFormats;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.camunda.spin.plugin.variable.value.builder.JsonValueBuilder;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author Roman Smirnov
 *
 */
public class JsonValueTest extends PluggableProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "org/camunda/spin/plugin/oneTaskProcess.bpmn20.xml";
  protected static final String JSON_FORMAT_NAME = DataFormats.JSON_DATAFORMAT_NAME;

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";

  protected String jsonString = "{\"foo\": \"bar\"}";
  protected String brokenJsonString = "{\"foo: \"bar\"}";

  protected String variableName = "x";

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testGetUntypedJsonValue() throws JSONException {
    // given
    JsonValue jsonValue = jsonValue(jsonString).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, jsonValue);

    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY, variables).getId();

    // when
    SpinJsonNode value = (SpinJsonNode) runtimeService.getVariable(processInstanceId, variableName);

    // then
    JSONAssert.assertEquals(jsonString, value.toString(), true);
    assertEquals(json().getName(), value.getDataFormatName());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testGetTypedJsonValue() throws JSONException {
    // given
    JsonValue jsonValue = jsonValue(jsonString).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, jsonValue);

    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY, variables).getId();

    // when
    JsonValue typedValue = runtimeService.getVariableTyped(processInstanceId, variableName);

    // then
    SpinJsonNode value = typedValue.getValue();
    JSONAssert.assertEquals(jsonString, value.toString(), true);

    assertTrue(typedValue.isDeserialized());
    assertEquals(JSON, typedValue.getType());
    assertEquals(JSON_FORMAT_NAME, typedValue.getSerializationDataFormat());
    JSONAssert.assertEquals(jsonString, typedValue.getValueSerialized(), true);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testBrokenJsonSerialization() {
    // given
    JsonValue value = jsonValue(brokenJsonString).create();

    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when
      runtimeService.setVariable(processInstanceId, variableName, value);
    } catch (Exception e) {
      fail("no exception expected");
    }
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testFailingDeserialization() {
    // given
    JsonValue value = jsonValue(brokenJsonString).create();

    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    runtimeService.setVariable(processInstanceId, variableName, value);

    try {
      // when
      runtimeService.getVariable(processInstanceId, variableName);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // happy path
    }

    try {
      runtimeService.getVariableTyped(processInstanceId, variableName);
      fail("exception expected");
    } catch(ProcessEngineException e) {
      // happy path
    }

    // However, I can access the serialized value
    JsonValue jsonValue = runtimeService.getVariableTyped(processInstanceId, variableName, false);
    assertFalse(jsonValue.isDeserialized());
    assertEquals(brokenJsonString, jsonValue.getValueSerialized());

    // but not the deserialized properties
    try {
      jsonValue.getValue();
      fail("exception expected");
    } catch(SpinRuntimeException e) {
    }
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testFailForNonExistingSerializationFormat() {
    // given
    JsonValueBuilder builder = jsonValue(jsonString).serializationDataFormat("non existing data format");
    String processInstanceId = runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when (1)
      runtimeService.setVariable(processInstanceId, variableName, builder);
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      // then (1)
      assertTextPresent("Cannot find serializer for value", e.getMessage());
      // happy path
    }

    try {
      // when (2)
      runtimeService.setVariable(processInstanceId, variableName, builder.create());
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      // then (2)
      assertTextPresent("Cannot find serializer for value", e.getMessage());
      // happy path
    }
  }

  @Deployment(resources = "org/camunda/spin/plugin/jsonConditionProcess.bpmn20.xml")
  public void testJsonValueInCondition() {
    // given
    String jsonString = "{\"age\": 22 }";
    JsonValue value = jsonValue(jsonString).create();
    VariableMap variables = Variables.createVariables().putValueTyped("customer", value);

    // when
    runtimeService.startProcessInstanceByKey("process", variables);

    // then
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task1", task.getTaskDefinitionKey());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testTransientJsonValueFluent() {
    // given
    JsonValue jsonValue = jsonValue(jsonString).setTransient(true).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, jsonValue);

    // when
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY, variables).getId();

    // then
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    assertEquals(0, variableInstances.size());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testTransientJsonValue() {
    // given
    JsonValue jsonValue = jsonValue(jsonString, true).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, jsonValue);

    // when
    runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS_KEY, variables).getId();

    // then
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    assertEquals(0, variableInstances.size());
  }

  public void testApplyValueInfoFromSerializedValue() {
    // given
    Map<String, Object> valueInfo = new HashMap<String, Object>();
    valueInfo.put(ValueType.VALUE_INFO_TRANSIENT, true);

    // when
    JsonValue jsonValue = (JsonValue) SpinValueType.JSON.createValueFromSerialized(jsonString, valueInfo);

    // then
    assertEquals(true, jsonValue.isTransient());
    Map<String, Object> returnedValueInfo = SpinValueType.JSON.getValueInfo(jsonValue);
    assertEquals(true, returnedValueInfo.get(ValueType.VALUE_INFO_TRANSIENT));
  }

  public void testDeserializeTransientJsonValue() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .exclusiveGateway("gtw")
          .sequenceFlowId("flow1")
          .condition("cond", "${S(" + variableName + ").prop(\"foo\").stringValue() == \"bar\"}")
          .userTask("userTask1")
          .endEvent()
        .moveToLastGateway()
          .sequenceFlowId("flow2")
          .userTask("userTask2")
          .endEvent()
        .done();

    deployment(modelInstance);

    JsonValue jsonValue = jsonValue(jsonString, true).create();
    VariableMap variables = Variables.createVariables().putValueTyped(variableName, jsonValue);

    // when
    runtimeService.startProcessInstanceByKey("foo", variables);

    // then
    List<VariableInstance> variableInstances = runtimeService.createVariableInstanceQuery().list();
    assertEquals(0, variableInstances.size());

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("userTask1", task.getTaskDefinitionKey());
  }

}
