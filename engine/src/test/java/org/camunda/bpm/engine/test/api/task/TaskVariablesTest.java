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

package org.camunda.bpm.engine.test.api.task;

import static org.camunda.bpm.engine.variable.Variables.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.StringValue;


/**
 * @author Tom Baeyens
 */
public class TaskVariablesTest extends PluggableProcessEngineTestCase {

  public void testStandaloneTaskVariables() {
    Task task = taskService.newTask();
    task.setName("gonzoTask");
    taskService.saveTask(task);

    String taskId = task.getId();
    taskService.setVariable(taskId, "instrument", "trumpet");
    assertEquals("trumpet", taskService.getVariable(taskId, "instrument"));

    taskService.deleteTask(taskId, true);
  }

  @Deployment
  public void testTaskExecutionVariables() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    String taskId = taskService.createTaskQuery().singleResult().getId();

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    assertEquals(expectedVariables, runtimeService.getVariables(processInstanceId));
    assertEquals(expectedVariables, taskService.getVariables(taskId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId));
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));

    runtimeService.setVariable(processInstanceId, "instrument", "trumpet");

    expectedVariables = new HashMap<String, Object>();
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));
    expectedVariables.put("instrument", "trumpet");
    assertEquals(expectedVariables, runtimeService.getVariables(processInstanceId));
    assertEquals(expectedVariables, taskService.getVariables(taskId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId));

    taskService.setVariable(taskId, "player", "gonzo");

    expectedVariables = new HashMap<String, Object>();
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertEquals(expectedVariables, runtimeService.getVariables(processInstanceId));
    assertEquals(expectedVariables, taskService.getVariables(taskId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId, null));
    assertEquals(expectedVariables, runtimeService.getVariablesLocalTyped(processInstanceId, null, true));

    taskService.setVariableLocal(taskId, "budget", "unlimited");

    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("budget", "unlimited");
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));
    assertEquals(expectedVariables, taskService.getVariablesLocalTyped(taskId, true));
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertEquals(expectedVariables, taskService.getVariables(taskId));
    assertEquals(expectedVariables, taskService.getVariablesTyped(taskId, true));

    assertEquals(expectedVariables, taskService.getVariables(taskId, null));
    assertEquals(expectedVariables, taskService.getVariablesTyped(taskId, null, true));

    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertEquals(expectedVariables, runtimeService.getVariables(processInstanceId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId));


    // typed variable API

    ArrayList<String> serializableValue = new ArrayList<String>();
    serializableValue.add("1");
    serializableValue.add("2");
    taskService.setVariable(taskId, "objectVariable", objectValue(serializableValue).create());

    ArrayList<String> serializableValueLocal = new ArrayList<String>();
    serializableValueLocal.add("3");
    serializableValueLocal.add("4");
    taskService.setVariableLocal(taskId, "objectVariableLocal", objectValue(serializableValueLocal).create());

    Object value = taskService.getVariable(taskId, "objectVariable");
    assertEquals(serializableValue, value);

    Object valueLocal = taskService.getVariableLocal(taskId, "objectVariableLocal");
    assertEquals(serializableValueLocal, valueLocal);

    ObjectValue typedValue = taskService.getVariableTyped(taskId, "objectVariable");
    assertEquals(serializableValue, typedValue.getValue());

    ObjectValue serializedValue = taskService.getVariableTyped(taskId, "objectVariable", false);
    assertFalse(serializedValue.isDeserialized());

    ObjectValue typedValueLocal = taskService.getVariableLocalTyped(taskId, "objectVariableLocal");
    assertEquals(serializableValueLocal, typedValueLocal.getValue());

    ObjectValue serializedValueLocal = taskService.getVariableLocalTyped(taskId, "objectVariableLocal", false);
    assertFalse(serializedValueLocal.isDeserialized());

    try {
      StringValue val = taskService.getVariableTyped(taskId, "objectVariable");
      fail("expected exception");
    }
    catch(ClassCastException e) {
      //happy path
    }

  }
}
