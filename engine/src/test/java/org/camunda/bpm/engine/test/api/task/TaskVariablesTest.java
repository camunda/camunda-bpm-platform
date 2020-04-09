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
package org.camunda.bpm.engine.test.api.task;

import static org.camunda.bpm.engine.variable.Variables.objectValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.junit.Test;


/**
 * @author Tom Baeyens
 */
public class TaskVariablesTest extends PluggableProcessEngineTest {

  @Test
  public void testStandaloneTaskVariables() {
    Task task = taskService.newTask();
    task.setName("gonzoTask");
    taskService.saveTask(task);

    String taskId = task.getId();
    taskService.setVariable(taskId, "instrument", "trumpet");
    assertEquals("trumpet", taskService.getVariable(taskId, "instrument"));

    taskService.deleteTask(taskId, true);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/task/TaskVariablesTest.testTaskExecutionVariables.bpmn20.xml"})
  @Test
  public void testTaskExecutionVariableLongValue() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();

    StringBuffer longString = new StringBuffer();
    for (int i = 0; i < 500; i++) {
      longString.append("tensymbols");
    }
    try {
      runtimeService.setVariable(processInstanceId, "var", longString.toString());
    } catch (Exception ex) {
      if (!(ex instanceof BadUserRequestException)) {
        fail("BadUserRequestException is expected, but another exception was received:  " + ex);
      }
      assertEquals("Variable value is too long", ex.getMessage());
    }

  }

  @Deployment
  @Test
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
