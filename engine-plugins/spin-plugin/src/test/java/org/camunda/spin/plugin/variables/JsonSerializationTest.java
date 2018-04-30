/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.spin.plugin.variables;

import static org.camunda.bpm.engine.variable.Variables.objectValue;
import static org.camunda.bpm.engine.variable.Variables.serializedObjectValue;
import static org.camunda.spin.plugin.variables.TypedValueAssert.assertObjectValueDeserializedNull;
import static org.camunda.spin.plugin.variables.TypedValueAssert.assertObjectValueSerializedNull;
import static org.camunda.spin.plugin.variables.TypedValueAssert.assertUntypedNullValue;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.engine.variable.value.builder.SerializedObjectValueBuilder;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.spin.DataFormats;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

public class JsonSerializationTest extends PluggableProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "org/camunda/spin/plugin/oneTaskProcess.bpmn20.xml";
  protected static final String SERVICE_TASK_PROCESS = "org/camunda/spin/plugin/serviceTaskProcess.bpmn20.xml";

  protected static final String JSON_FORMAT_NAME = DataFormats.JSON_DATAFORMAT_NAME;

  protected String originalSerializationFormat;

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSerializationAsJson() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    JsonSerializable bean = new JsonSerializable("a String", 42, true);
    // request object to be serialized as JSON
    runtimeService.setVariable(instance.getId(), "simpleBean", objectValue(bean).serializationDataFormat(JSON_FORMAT_NAME).create());

    // validate untyped value
    Object value = runtimeService.getVariable(instance.getId(), "simpleBean");
    assertEquals(bean, value);

    // validate typed value
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "simpleBean");
    assertEquals(ValueType.OBJECT, typedValue.getType());

    assertTrue(typedValue.isDeserialized());

    assertEquals(bean, typedValue.getValue());
    assertEquals(bean, typedValue.getValue(JsonSerializable.class));
    assertEquals(JsonSerializable.class, typedValue.getObjectType());

    assertEquals(JSON_FORMAT_NAME, typedValue.getSerializationDataFormat());
    assertEquals(JsonSerializable.class.getName(), typedValue.getObjectTypeName());
    JSONAssert.assertEquals(bean.toExpectedJsonString(), new String(typedValue.getValueSerialized()), true);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testListSerializationAsJson() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<JsonSerializable> beans = new ArrayList<JsonSerializable>();
    for (int i = 0; i < 20; i++) {
      beans.add(new JsonSerializable("a String" + i, 42 + i, true));
    }

    runtimeService.setVariable(instance.getId(), "simpleBeans", objectValue(beans).serializationDataFormat(JSON_FORMAT_NAME).create());

    // validate untyped value
    Object value = runtimeService.getVariable(instance.getId(), "simpleBeans");
    assertEquals(beans, value);

    // validate typed value
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "simpleBeans");
    assertEquals(ValueType.OBJECT, typedValue.getType());
    assertEquals(beans, typedValue.getValue());
    assertTrue(typedValue.isDeserialized());
    assertEquals(JSON_FORMAT_NAME, typedValue.getSerializationDataFormat());
    assertNotNull(typedValue.getObjectTypeName());
    JSONAssert.assertEquals(toExpectedJsonArray(beans), new String(typedValue.getValueSerialized()), true);

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testFailingSerialization() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    FailingSerializationBean failingBean = new FailingSerializationBean("a String", 42, true);

    try {
      runtimeService.setVariable(instance.getId(), "simpleBean", objectValue(failingBean).serializationDataFormat(JSON_FORMAT_NAME));
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // happy path
    }
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testFailingDeserialization() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    FailingDeserializationBean failingBean = new FailingDeserializationBean("a String", 42, true);

    runtimeService.setVariable(instance.getId(), "simpleBean", objectValue(failingBean).serializationDataFormat(JSON_FORMAT_NAME));

    try {
      runtimeService.getVariable(instance.getId(), "simpleBean");
      fail("exception expected");
    }
    catch(ProcessEngineException e) {
      // happy path
    }

    try {
      runtimeService.getVariableTyped(instance.getId(), "simpleBean");
      fail("exception expected");
    }
    catch(ProcessEngineException e) {
      // happy path
    }

    // However, I can access the serialized value
    ObjectValue objectValue = runtimeService.getVariableTyped(instance.getId(), "simpleBean", false);
    assertFalse(objectValue.isDeserialized());
    assertNotNull(objectValue.getObjectTypeName());
    assertNotNull(objectValue.getValueSerialized());
    // but not the deserialized properties
    try {
      objectValue.getValue();
      fail("exception expected");
    }
    catch(IllegalStateException e) {
      assertTextPresent("Object is not deserialized", e.getMessage());
    }

    try {
      objectValue.getValue(JsonSerializable.class);
      fail("exception expected");
    }
    catch(IllegalStateException e) {
      assertTextPresent("Object is not deserialized", e.getMessage());
    }

    try {
      objectValue.getObjectType();
      fail("exception expected");
    }
    catch(IllegalStateException e) {
      assertTextPresent("Object is not deserialized", e.getMessage());
    }

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testFailForNonExistingSerializationFormat() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    JsonSerializable jsonSerializable = new JsonSerializable();

    try {
      runtimeService.setVariable(instance.getId(), "simpleBean", objectValue(jsonSerializable).serializationDataFormat("non existing data format"));
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      assertTextPresent("Cannot find serializer for value", e.getMessage());
      // happy path
    }
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testVariableValueCaching() {
    final ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        JsonSerializable bean = new JsonSerializable("a String", 42, true);
        runtimeService.setVariable(instance.getId(), "simpleBean", bean);

        Object returnedBean = runtimeService.getVariable(instance.getId(), "simpleBean");
        assertSame(bean, returnedBean);

        return null;
      }
    });

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();

    Object returnedBean = variableInstance.getValue();
    Object theSameReturnedBean = variableInstance.getValue();
    assertSame(returnedBean, theSameReturnedBean);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testGetSerializedVariableValue() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    JsonSerializable bean = new JsonSerializable("a String", 42, true);
    runtimeService.setVariable(instance.getId(), "simpleBean", objectValue(bean).serializationDataFormat(JSON_FORMAT_NAME).create());

    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "simpleBean", false);

    String serializedValue = typedValue.getValueSerialized();
    JSONAssert.assertEquals(bean.toExpectedJsonString(), serializedValue, true);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetSerializedVariableValue() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    JsonSerializable bean = new JsonSerializable("a String", 42, true);
    String beanAsJson = bean.toExpectedJsonString();

    SerializedObjectValueBuilder serializedValue = serializedObjectValue(beanAsJson)
      .serializationDataFormat(JSON_FORMAT_NAME)
      .objectTypeName(bean.getClass().getCanonicalName());

    runtimeService.setVariable(instance.getId(), "simpleBean", serializedValue);

    // java object can be retrieved
    JsonSerializable returnedBean = (JsonSerializable) runtimeService.getVariable(instance.getId(), "simpleBean");
    assertEquals(bean, returnedBean);

    // validate typed value metadata
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "simpleBean");
    assertEquals(bean, typedValue.getValue());
    assertEquals(JSON_FORMAT_NAME, typedValue.getSerializationDataFormat());
    assertEquals(bean.getClass().getCanonicalName(), typedValue.getObjectTypeName());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetSerializedVariableValueNoTypeName() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    JsonSerializable bean = new JsonSerializable("a String", 42, true);
    String beanAsJson = bean.toExpectedJsonString();

    SerializedObjectValueBuilder serializedValue = serializedObjectValue(beanAsJson)
      .serializationDataFormat(JSON_FORMAT_NAME);
      // no type name

    try {
      runtimeService.setVariable(instance.getId(), "simpleBean", serializedValue);
      fail("Exception expected.");
    }
    catch(Exception e) {
      assertTextPresent("no 'objectTypeName' provided for non-null value", e.getMessage());
    }
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetSerializedVariableValueMismatchingTypeName() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    JsonSerializable bean = new JsonSerializable("a String", 42, true);
    String beanAsJson = bean.toExpectedJsonString();

    SerializedObjectValueBuilder serializedValue = serializedObjectValue(beanAsJson)
      .serializationDataFormat(JSON_FORMAT_NAME)
      .objectTypeName("Insensible type name."); // < not a valid type name

    runtimeService.setVariable(instance.getId(), "simpleBean", serializedValue);

    try {
      runtimeService.getVariable(instance.getId(), "simpleBean");
      fail("Exception expected.");
    }
    catch(Exception e) {
      // happy path
    }

    serializedValue = serializedObjectValue(beanAsJson)
      .serializationDataFormat(JSON_FORMAT_NAME)
      .objectTypeName(JsonSerializationTest.class.getName()); // < not the right type name

    runtimeService.setVariable(instance.getId(), "simpleBean", serializedValue);

    try {
      runtimeService.getVariable(instance.getId(), "simpleBean");
      fail("Exception expected.");
    }
    catch(Exception e) {
      // happy path
    }
  }


  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetSerializedVariableValueNull() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    SerializedObjectValueBuilder serializedValue = serializedObjectValue()
      .serializationDataFormat(JSON_FORMAT_NAME)
      .objectTypeName(JsonSerializable.class.getCanonicalName());

    runtimeService.setVariable(instance.getId(), "simpleBean", serializedValue);

    // null can be retrieved
    JsonSerializable returnedBean = (JsonSerializable) runtimeService.getVariable(instance.getId(), "simpleBean");
    assertNull(returnedBean);

    // validate typed value metadata
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "simpleBean");
    assertNull(typedValue.getValue());
    assertNull(typedValue.getValueSerialized());
    assertEquals(JSON_FORMAT_NAME, typedValue.getSerializationDataFormat());
    assertEquals(JsonSerializable.class.getCanonicalName(), typedValue.getObjectTypeName());

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetSerializedVariableValueNullNoTypeName() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    SerializedObjectValueBuilder serializedValue = serializedObjectValue()
      .serializationDataFormat(JSON_FORMAT_NAME);
    // no objectTypeName specified

    runtimeService.setVariable(instance.getId(), "simpleBean", serializedValue);

    // null can be retrieved
    JsonSerializable returnedBean = (JsonSerializable) runtimeService.getVariable(instance.getId(), "simpleBean");
    assertNull(returnedBean);

    // validate typed value metadata
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "simpleBean");
    assertNull(typedValue.getValue());
    assertNull(typedValue.getValueSerialized());
    assertEquals(JSON_FORMAT_NAME, typedValue.getSerializationDataFormat());
    assertNull(typedValue.getObjectTypeName());
  }

  protected String toExpectedJsonArray(List<JsonSerializable> beans) {
    StringBuilder jsonBuilder = new StringBuilder();

    jsonBuilder.append("[");
    for (int i = 0; i < beans.size(); i++) {
      jsonBuilder.append(beans.get(i).toExpectedJsonString());

      if (i != beans.size() - 1)  {
        jsonBuilder.append(", ");
      }
    }
    jsonBuilder.append("]");

    return jsonBuilder.toString();
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetJavaOjectNullDeserialized() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // set null value as "deserialized" object
    runtimeService.setVariable(instance.getId(), "nullObject",
        objectValue(null)
        .serializationDataFormat(JSON_FORMAT_NAME)
        .create());

    // get null value via untyped api
    assertNull(runtimeService.getVariable(instance.getId(), "nullObject"));

    // get null via typed api
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject");
    assertObjectValueDeserializedNull(typedValue);

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetJavaOjectNullSerialized() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // set null value as "serialized" object
    runtimeService.setVariable(instance.getId(), "nullObject",
        serializedObjectValue()
        .serializationDataFormat(JSON_FORMAT_NAME)
        .create()); // Note: no object type name provided

    // get null value via untyped api
    assertNull(runtimeService.getVariable(instance.getId(), "nullObject"));

    // get null via typed api
    ObjectValue deserializedTypedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject");
    assertObjectValueDeserializedNull(deserializedTypedValue);

    ObjectValue serializedTypedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject", false);
    assertObjectValueSerializedNull(serializedTypedValue);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetJavaOjectNullSerializedObjectTypeName() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    String typeName = "some.type.Name";

    // set null value as "serialized" object
    runtimeService.setVariable(instance.getId(), "nullObject",
        serializedObjectValue()
        .serializationDataFormat(JSON_FORMAT_NAME)
        .objectTypeName(typeName) // This time an objectTypeName is provided
        .create());

    // get null value via untyped api
    assertNull(runtimeService.getVariable(instance.getId(), "nullObject"));

    // get null via typed api
    ObjectValue deserializedTypedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject");
    assertNotNull(deserializedTypedValue);
    assertTrue(deserializedTypedValue.isDeserialized());
    assertEquals(JSON_FORMAT_NAME, deserializedTypedValue.getSerializationDataFormat());
    assertNull(deserializedTypedValue.getValue());
    assertNull(deserializedTypedValue.getValueSerialized());
    assertNull(deserializedTypedValue.getObjectType());
    assertEquals(typeName, deserializedTypedValue.getObjectTypeName());

    ObjectValue serializedTypedValue = runtimeService.getVariableTyped(instance.getId(), "nullObject", false);
    assertNotNull(serializedTypedValue);
    assertFalse(serializedTypedValue.isDeserialized());
    assertEquals(JSON_FORMAT_NAME, serializedTypedValue.getSerializationDataFormat());
    assertNull(serializedTypedValue.getValueSerialized());
    assertEquals(typeName, serializedTypedValue.getObjectTypeName());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetUntypedNullForExistingVariable() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // initially the variable has a value
    JsonSerializable object = new JsonSerializable();

    runtimeService.setVariable(instance.getId(), "varName",
        objectValue(object)
        .serializationDataFormat(JSON_FORMAT_NAME)
        .create());

    // get value via untyped api
    assertEquals(object, runtimeService.getVariable(instance.getId(), "varName"));

    // set the variable to null via untyped Api
    runtimeService.setVariable(instance.getId(), "varName", null);

    // variable is now untyped null
    TypedValue nullValue = runtimeService.getVariableTyped(instance.getId(), "varName");
    assertUntypedNullValue(nullValue);

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetTypedNullForExistingVariable() throws Exception {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // initially the variable has a value
    JsonSerializable javaSerializable = new JsonSerializable();

    runtimeService.setVariable(instance.getId(), "varName",
        objectValue(javaSerializable)
        .serializationDataFormat(JSON_FORMAT_NAME)
        .create());

    // get value via untyped api
    assertEquals(javaSerializable, runtimeService.getVariable(instance.getId(), "varName"));

    // set the variable to null via typed Api
    runtimeService.setVariable(instance.getId(), "varName", objectValue(null));

    // variable is still of type object
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "varName");
    assertObjectValueDeserializedNull(typedValue);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testRemoveVariable() throws JSONException {
    // given a serialized json variable
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    JsonSerializable bean = new JsonSerializable("a String", 42, true);
    String beanAsJson = bean.toExpectedJsonString();

    SerializedObjectValueBuilder serializedValue = serializedObjectValue(beanAsJson)
      .serializationDataFormat(JSON_FORMAT_NAME)
      .objectTypeName(bean.getClass().getCanonicalName());

    runtimeService.setVariable(instance.getId(), "simpleBean", serializedValue);

    // when
    runtimeService.removeVariable(instance.getId(), "simpleBean");

    // then
    assertNull(runtimeService.getVariable(instance.getId(), "simpleBean"));
    assertNull(runtimeService.getVariableTyped(instance.getId(), "simpleBean"));
    assertNull(runtimeService.getVariableTyped(instance.getId(), "simpleBean", false));
  }


  /**
   * CAM-3222
   */
  @Deployment(resources = SERVICE_TASK_PROCESS)
  public void testImplicitlyUpdateEmptyList() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValueTyped("listVar",
            Variables.objectValue(new ArrayList<JsonSerializable>())
              .serializationDataFormat("application/json").create())
          .putValue("delegate", new UpdateValueDelegate()));

    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "listVar");
    // this should match Jackson's format
    String expectedTypeName = ArrayList.class.getName() + "<" + JsonSerializable.class.getName() + ">";
    assertEquals(expectedTypeName, typedValue.getObjectTypeName());

    List<JsonSerializable> list = (List<JsonSerializable>) typedValue.getValue();
    assertEquals(1, list.size());
    assertTrue(list.get(0) instanceof JsonSerializable);
    assertEquals(UpdateValueDelegate.STRING_PROPERTY, list.get(0).getStringProperty());
  }

  public void testTransientJsonValue() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo")
        .startEvent()
        .exclusiveGateway("gtw")
          .sequenceFlowId("flow1")
          .condition("cond", "${x.stringProperty == \"bar\"}")
          .userTask("userTask1")
          .endEvent()
        .moveToLastGateway()
          .sequenceFlowId("flow2")
          .userTask("userTask2")
          .endEvent()
        .done();

    deployment(modelInstance);

    JsonSerializable bean = new JsonSerializable("bar", 42, true);
    ObjectValue jsonValue = serializedObjectValue(bean.toExpectedJsonString(), true)
        .serializationDataFormat(JSON_FORMAT_NAME)
        .objectTypeName(JsonSerializable.class.getName())
        .create();
    VariableMap variables = Variables.createVariables().putValueTyped("x", jsonValue);

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
