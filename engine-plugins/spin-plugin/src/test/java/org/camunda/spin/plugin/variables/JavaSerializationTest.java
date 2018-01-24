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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.json.JSONException;
import static org.camunda.bpm.engine.variable.Variables.objectValue;
import static org.camunda.bpm.engine.variable.Variables.serializedObjectValue;

/**
 * Here we test how the engine behaves, when more than one object serializers are available.
 *
 * @author Svetlana Dorokhova
 */
public class JavaSerializationTest extends PluggableProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "org/camunda/spin/plugin/oneTaskProcess.bpmn20.xml";

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSerializationAsJava() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    JavaSerializable bean = new JavaSerializable("a String", 42, true);
    // request object to be serialized as Java
    runtimeService.setVariable(instance.getId(), "simpleBean", objectValue(bean).serializationDataFormat(Variables.SerializationDataFormats.JAVA).create());

    // validate untyped value
    Object value = runtimeService.getVariable(instance.getId(), "simpleBean");
    assertEquals(bean, value);

    // validate typed value
    ObjectValue typedValue = runtimeService.getVariableTyped(instance.getId(), "simpleBean");
    assertEquals(ValueType.OBJECT, typedValue.getType());

    assertTrue(typedValue.isDeserialized());

    assertEquals(bean, typedValue.getValue());
    assertEquals(bean, typedValue.getValue(JavaSerializable.class));
    assertEquals(JavaSerializable.class, typedValue.getObjectType());

    assertEquals(Variables.SerializationDataFormats.JAVA.getName(), typedValue.getSerializationDataFormat());
    assertEquals(JavaSerializable.class.getName(), typedValue.getObjectTypeName());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testJavaSerializedValuesAreProhibited() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    try {
      // request object to be serialized as Java
      runtimeService
        .setVariable(instance.getId(), "simpleBean", serializedObjectValue("").serializationDataFormat(Variables.SerializationDataFormats.JAVA).create());
      fail("Exception is expected.");
    } catch (ProcessEngineException ex) {
      assertEquals("ENGINE-17007 Cannot set variable with name simpleBean. Java serialization format is prohibited", ex.getMessage());
    }

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testJavaSerializedValuesAreProhibitedForTransient() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    try {
      // request object to be serialized as Java
      runtimeService
        .setVariable(instance.getId(), "simpleBean", serializedObjectValue("").serializationDataFormat(Variables.SerializationDataFormats.JAVA).create());
      fail("Exception is expected.");
    } catch (ProcessEngineException ex) {
      assertEquals("ENGINE-17007 Cannot set variable with name simpleBean. Java serialization format is prohibited", ex.getMessage());
    }

  }

  public void testStandaloneTaskVariable() {
    Task task = taskService.newTask();
    task.setName("gonzoTask");
    taskService.saveTask(task);

    String taskId = task.getId();

    try {
      taskService
        .setVariable(taskId, "instrument", Variables.serializedObjectValue("trumpet").serializationDataFormat(Variables.SerializationDataFormats.JAVA).create());
      fail("Exception is expected.");
    } catch (ProcessEngineException ex) {
      assertEquals("ENGINE-17007 Cannot set variable with name instrument. Java serialization format is prohibited", ex.getMessage());
    } finally {
      taskService.deleteTask(taskId, true);
    }

  }

  public void testStandaloneTaskTransientVariable() {
    Task task = taskService.newTask();
    task.setName("gonzoTask");
    taskService.saveTask(task);

    String taskId = task.getId();

    try {
      taskService
        .setVariable(taskId, "instrument", Variables.serializedObjectValue("trumpet")
          .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
          .setTransient(true)
          .create());
      fail("Exception is expected.");
    } catch (ProcessEngineException ex) {
      assertEquals("ENGINE-17007 Cannot set variable with name instrument. Java serialization format is prohibited", ex.getMessage());
    } finally {
      taskService.deleteTask(taskId, true);
    }

  }

}
