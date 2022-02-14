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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.runtime.util.CustomSerializable;
import org.camunda.bpm.engine.test.api.runtime.util.FailingSerializable;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.Test;

/**
 * @author roman.smirnov
 */
public class VariableInstanceQueryTest extends PluggableProcessEngineTest {

  protected static String PROC_DEF_KEY = "oneTaskProcess";

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQuery() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("intVar", 123);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    assertNotNull(query);

    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertNotNull(var.getId());
      if (var.getName().equals("intVar")) {
        assertEquals("intVar", var.getName());
        assertEquals(123, var.getValue());
      } else if (var.getName().equals("stringVar")) {
        assertEquals("stringVar", var.getName());
        assertEquals("test", var.getValue());
      } else {
        fail("An unexpected variable '" + var.getName() + "' was found with value " + var.getValue());
      }

    }
  }

  @Test
  public void testQueryByVariableId() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("var1", "test");
    variables.put("var2", "test");
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.setVariablesLocal(task.getId(), variables);
    VariableInstance result = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(result);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableId(result.getId());

    // then
    assertNotNull(query);
    VariableInstance resultById = query.singleResult();
    assertEquals(result.getId(), resultById.getId());

    // delete taskoneTaskProcess
    taskService.deleteTask(task.getId(), true);
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableName() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableName("stringVar");

    // then
    verifyQueryResult(query, "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableNames() {
    // given
    String variableValue = "a";
    Map<String, Object> variables = new HashMap<>();
    variables.put("process", variableValue);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "task", variableValue);
    runtimeService.setVariableLocal(task.getExecutionId(), "execution", variableValue);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableNameIn("task", "process", "execution");

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance variableInstance : result) {
      assertEquals(variableValue, variableInstance.getValue());
      assertEquals("string", variableInstance.getTypeName());
    }

    assertEquals(1, runtimeService.createVariableInstanceQuery().variableName("task").variableNameIn("task", "execution").count());
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("task").variableNameIn("process", "execution").count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableNameLike() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("string%Var", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableNameLike("%ing\\%V%");

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("string%Var", var.getName());
    assertEquals("test", var.getValue());
    assertEquals("string", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableName_EmptyString() {
    // given
    String varName = "testVar";
    VariableMap variables = Variables.putValue(varName, "");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstance var = runtimeService.createVariableInstanceQuery().variableName(varName).singleResult();

    // then
    assertThat(var.getValue()).isNotNull();
    assertThat(var.getValue()).isEqualTo("");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableNameLikeWithoutAnyResult() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableNameLike("%ingV_");

    // then
    List<VariableInstance> result = query.list();
    assertTrue(result.isEmpty());

    assertEquals(0, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_String() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("stringVar", "test");

    // then
    verifyQueryResult(query, "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_EmptyString() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("stringVar", "");

    // then
    verifyQueryResult(query, "");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueNotEquals_String() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "test123");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueNotEquals("stringVar", "test123");

    // then
    verifyQueryResult(query, "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueNotEquals_EmptyString() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueNotEquals("stringVar", "");

    // then
    verifyQueryResult(query, "test");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueGreaterThan_String() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "a");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "b");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("stringVar", "c");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThan("stringVar", "a");

    // then
    verifyQueryResult(query, "b", "c");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueGreaterThanOrEqual_String() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "a");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "b");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("stringVar", "c");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "a");

    // then
    verifyQueryResult(query, "a", "b", "c");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueLessThan_String() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "a");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "b");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("stringVar", "c");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThan("stringVar", "c");

    // then
    verifyQueryResult(query, "b", "a");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueLessThanOrEqual_String() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "a");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "b");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("stringVar", "c");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThanOrEqual("stringVar", "c");

    // then
    verifyQueryResult(query, "a", "b", "c");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueLike_String() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "test123");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "test456");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("stringVar", "test789");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLike("stringVar", "test%");

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("stringVar", var.getName());
      assertEquals("string", var.getTypeName());
      if (var.getValue().equals("test123")) {
        assertEquals("test123", var.getValue());
      } else if (var.getValue().equals("test456")) {
        assertEquals("test456", var.getValue());
      } else if (var.getValue().equals("test789")) {
        assertEquals("test789", var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testQueryByNameAndVariableValueLikeWithEscape_String() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "test_123");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "test%456");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLike("stringVar", "test\\_%");
    verifyQueryResult(query, "test_123");

    query = runtimeService.createVariableInstanceQuery().variableValueLike("stringVar", "test\\%%");
    verifyQueryResult(query, "test%456");

  }

  private void verifyQueryResult(VariableInstanceQuery query, String...varValues) {
    // then
    assertEquals(varValues.length, query.count());

    List<VariableInstance> result = query.list();
    assertEquals(varValues.length, result.size());

    List<String> expected = Arrays.asList(varValues);

    for (VariableInstance var : result) {
      assertEquals("stringVar", var.getName());
      assertEquals("string", var.getTypeName());
      assertTrue("Unexpected value found: " + var.getValue(), expected.contains(var.getValue()));
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Integer() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("intValue", 1234);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("intValue", 1234);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("intValue", var.getName());
    assertEquals(1234, var.getValue());
    assertEquals("integer", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueNotEquals_Integer() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("intValue", 1234);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("intValue", 5555);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueNotEquals("intValue", 5555);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("intValue", var.getName());
    assertEquals(1234, var.getValue());
    assertEquals("integer", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableGreaterThan_Integer() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("intValue", 1234);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("intValue", 5555);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("intValue", 9876);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThan("intValue", 1234);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("intValue", var.getName());
      assertEquals("integer", var.getTypeName());
      if (var.getValue().equals(5555)) {
        assertEquals(5555, var.getValue());
      } else if (var.getValue().equals(9876)) {
        assertEquals(9876, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableGreaterThanAndEqual_Integer() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("intValue", 1234);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("intValue", 5555);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("intValue", 9876);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThanOrEqual("intValue", 1234);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("intValue", var.getName());
      assertEquals("integer", var.getTypeName());
      if (var.getValue().equals(1234)) {
        assertEquals(1234, var.getValue());
      } else if (var.getValue().equals(5555)) {
        assertEquals(5555, var.getValue());
      } else if (var.getValue().equals(9876)) {
        assertEquals(9876, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableLessThan_Integer() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("intValue", 1234);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("intValue", 5555);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("intValue", 9876);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThan("intValue", 9876);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("intValue", var.getName());
      assertEquals("integer", var.getTypeName());
      if (var.getValue().equals(5555)) {
        assertEquals(5555, var.getValue());
      } else if (var.getValue().equals(1234)) {
        assertEquals(1234, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableLessThanAndEqual_Integer() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("intValue", 1234);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("intValue", 5555);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("intValue", 9876);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThanOrEqual("intValue", 9876);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("intValue", var.getName());
      assertEquals("integer", var.getTypeName());
      if (var.getValue().equals(1234)) {
        assertEquals(1234, var.getValue());
      } else if (var.getValue().equals(5555)) {
        assertEquals(5555, var.getValue());
      } else if (var.getValue().equals(9876)) {
        assertEquals(9876, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Long() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("longValue", 123456L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("longValue", 123456L);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("longValue", var.getName());
    assertEquals(123456L, var.getValue());
    assertEquals("long", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueNotEquals_Long() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("longValue", 123456L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("longValue", 987654L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueNotEquals("longValue", 987654L);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("longValue", var.getName());
    assertEquals(123456L, var.getValue());
    assertEquals("long", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableGreaterThan_Long() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("longValue", 123456L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("longValue", 987654L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("longValue", 555555L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThan("longValue", 123456L);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("longValue", var.getName());
      assertEquals("long", var.getTypeName());
      if (var.getValue().equals(555555L)) {
        assertEquals(555555L, var.getValue());
      } else if (var.getValue().equals(987654L)) {
        assertEquals(987654L, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableGreaterThanAndEqual_Long() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("longValue", 123456L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("longValue", 987654L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("longValue", 555555L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThanOrEqual("longValue", 123456L);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("longValue", var.getName());
      assertEquals("long", var.getTypeName());
      if (var.getValue().equals(123456L)) {
        assertEquals(123456L, var.getValue());
      } else if (var.getValue().equals(555555L)) {
        assertEquals(555555L, var.getValue());
      } else if (var.getValue().equals(987654L)) {
        assertEquals(987654L, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableLessThan_Long() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("longValue", 123456L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("longValue", 987654L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("longValue", 555555L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThan("longValue", 987654L);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("longValue", var.getName());
      assertEquals("long", var.getTypeName());
      if (var.getValue().equals(123456L)) {
        assertEquals(123456L, var.getValue());
      } else if (var.getValue().equals(555555L)) {
        assertEquals(555555L, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableLessThanAndEqual_Long() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("longValue", 123456L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("longValue", 987654L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("longValue", 555555L);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThanOrEqual("longValue", 987654L);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("longValue", var.getName());
      assertEquals("long", var.getTypeName());
      if (var.getValue().equals(123456L)) {
        assertEquals(123456L, var.getValue());
      } else if (var.getValue().equals(555555L)) {
        assertEquals(555555L, var.getValue());
      } else if (var.getValue().equals(987654L)) {
        assertEquals(987654L, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Double() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("doubleValue", 123.456);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("doubleValue", 123.456);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("doubleValue", var.getName());
    assertEquals(123.456, var.getValue());
    assertEquals("double", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueNotEquals_Double() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("doubleValue", 123.456);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("doubleValue", 654.321);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueNotEquals("doubleValue", 654.321);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("doubleValue", var.getName());
    assertEquals(123.456, var.getValue());
    assertEquals("double", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableGreaterThan_Double() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("doubleValue", 123.456);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("doubleValue", 654.321);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("doubleValue", 999.999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThan("doubleValue", 123.456);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("doubleValue", var.getName());
      assertEquals("double", var.getTypeName());
      if (var.getValue().equals(654.321)) {
        assertEquals(654.321, var.getValue());
      } else if (var.getValue().equals(999.999)) {
        assertEquals(999.999, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableGreaterThanAndEqual_Double() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("doubleValue", 123.456);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("doubleValue", 654.321);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("doubleValue", 999.999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThanOrEqual("doubleValue", 123.456);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("doubleValue", var.getName());
      assertEquals("double", var.getTypeName());
      if (var.getValue().equals(123.456)) {
        assertEquals(123.456, var.getValue());
      } else if (var.getValue().equals(654.321)) {
        assertEquals(654.321, var.getValue());
      } else if (var.getValue().equals(999.999)) {
        assertEquals(999.999, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableLessThan_Double() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("doubleValue", 123.456);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("doubleValue", 654.321);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("doubleValue", 999.999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThan("doubleValue", 999.999);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("doubleValue", var.getName());
      assertEquals("double", var.getTypeName());
      if (var.getValue().equals(123.456)) {
        assertEquals(123.456, var.getValue());
      } else if (var.getValue().equals(654.321)) {
        assertEquals(654.321, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableLessThanAndEqual_Double() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("doubleValue", 123.456);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("doubleValue", 654.321);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("doubleValue", 999.999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThanOrEqual("doubleValue", 999.999);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("doubleValue", var.getName());
      assertEquals("double", var.getTypeName());
      if (var.getValue().equals(123.456)) {
        assertEquals(123.456, var.getValue());
      } else if (var.getValue().equals(654.321)) {
        assertEquals(654.321, var.getValue());
      } else if (var.getValue().equals(999.999)) {
        assertEquals(999.999, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Short() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("shortValue", (short) 123);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("shortValue", (short) 123);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("shortValue", var.getName());
    assertEquals((short) 123, var.getValue());
    assertEquals("short", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableValueNotEquals_Short() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("shortValue", (short) 123);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("shortValue", (short) 999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueNotEquals("shortValue", (short) 999);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("shortValue", var.getName());
    assertEquals((short) 123, var.getValue());
    assertEquals("short", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableGreaterThan_Short() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("shortValue", (short) 123);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("shortValue", (short) 999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("shortValue", (short) 555);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThan("shortValue", (short) 123);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("shortValue", var.getName());
      assertEquals("short", var.getTypeName());
      if (var.getValue().equals((short) 555)) {
        assertEquals((short) 555, var.getValue());
      } else if (var.getValue().equals((short) 999)) {
        assertEquals((short) 999, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableGreaterThanAndEqual_Short() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("shortValue", (short) 123);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("shortValue", (short) 999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("shortValue", (short) 555);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueGreaterThanOrEqual("shortValue", (short) 123);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("shortValue", var.getName());
      assertEquals("short", var.getTypeName());
      if (var.getValue().equals((short) 123)) {
        assertEquals((short) 123, var.getValue());
      } else if (var.getValue().equals((short) 555)) {
        assertEquals((short) 555, var.getValue());
      } else if (var.getValue().equals((short) 999)) {
        assertEquals((short) 999, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableLessThan_Short() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("shortValue", (short) 123);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("shortValue", (short) 999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("shortValue", (short) 555);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThan("shortValue", (short) 999);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("shortValue", var.getName());
      assertEquals("short", var.getTypeName());
      if (var.getValue().equals((short) 123)) {
        assertEquals((short) 123, var.getValue());
      } else if (var.getValue().equals((short) 555)) {
        assertEquals((short) 555, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableLessThanAndEqual_Short() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("shortValue", (short) 123);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("shortValue", (short) 999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("shortValue", (short) 555);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueLessThanOrEqual("shortValue", (short) 999);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("shortValue", var.getName());
      assertEquals("short", var.getTypeName());
      if (var.getValue().equals((short) 123)) {
        assertEquals((short) 123, var.getValue());
      } else if (var.getValue().equals((short) 555)) {
        assertEquals((short) 555, var.getValue());
      } else if (var.getValue().equals((short) 999)) {
        assertEquals((short) 999, var.getValue());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Bytes() {
    // given
    byte[] bytes = "somebytes".getBytes();
    Map<String, Object> variables = new HashMap<>();
    variables.put("bytesVar", bytes);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("bytesVar", bytes);

    // then
    try {
      query.list();
      fail("A ProcessEngineException was expected: Variables of type ByteArray cannot be used to query.");
    } catch (ProcessEngineException e) {
      // expected exception
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Date() {
    // given
     Date now = new Date();

    Map<String, Object> variables = new HashMap<>();
    variables.put("date", now);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("date", now);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("date", var.getName());
    assertEquals(now, var.getValue());
    assertEquals("date", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEqualsWihtoutAnyResult() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("stringVar", "notFoundValue");

    // then
    List<VariableInstance> result = query.list();
    assertTrue(result.isEmpty());

    assertEquals(0, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_NullValue() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("nullValue", null);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("nullValue", null);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("nullValue", var.getName());
    assertEquals(null, var.getValue());
    assertEquals("null", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableValueNotEquals_NullValue() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("value", null);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("value", (short) 999);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("value", "abc");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueNotEquals("value", null);

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("value", var.getName());
      if (var.getValue().equals((short) 999)) {
        assertEquals((short) 999, var.getValue());
        assertEquals("short", var.getTypeName());
      } else if (var.getValue().equals("abc")) {
        assertEquals("abc", var.getValue());
        assertEquals("string", var.getTypeName());
      } else {
        fail("A non expected value occured: " + var.getValue());
      }

    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByProcessInstanceId() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().processInstanceIdIn(processInstance.getId());

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("string", var.getTypeName());
      if (var.getName().equals("myVar")) {
        assertEquals("myVar", var.getName());
        assertEquals("test123", var.getValue());
      } else if (var.getName().equals("stringVar")) {
        assertEquals("stringVar", var.getName());
        assertEquals("test", var.getValue());
      } else {
        fail("An unexpected variable '" + var.getName() + "' was found with value " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByProcessInstanceIds() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().processInstanceIdIn(processInstance1.getId(), processInstance2.getId());

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("string", var.getTypeName());
      if (var.getName().equals("myVar")) {
        assertEquals("myVar", var.getName());
        assertEquals("test123", var.getValue());
      } else if (var.getName().equals("stringVar")) {
        assertEquals("stringVar", var.getName());
        assertEquals("test", var.getValue());
      } else {
        fail("An unexpected variable '" + var.getName() + "' was found with value " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByProcessInstanceIdWithoutAnyResult() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().processInstanceIdIn("aProcessInstanceId");

    // then
    List<VariableInstance> result = query.list();
    assertTrue(result.isEmpty());

    assertEquals(0, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByExecutionId() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().executionIdIn(processInstance.getId());

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("string", var.getTypeName());
      if (var.getName().equals("myVar")) {
        assertEquals("myVar", var.getName());
        assertEquals("test123", var.getValue());
      } else if (var.getName().equals("stringVar")) {
        assertEquals("stringVar", var.getName());
        assertEquals("test", var.getValue());
      } else {
        fail("An unexpected variable '" + var.getName() + "' was found with value " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByExecutionIds() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "test");
    variables1.put("myVar", "test123");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("myVar", "test123");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().executionIdIn(processInstance1.getId(), processInstance2.getId());

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());

    assertEquals(3, query.count());

    for (VariableInstance var : result) {
      assertEquals("string", var.getTypeName());
      if (var.getName().equals("myVar")) {
        assertEquals("myVar", var.getName());
        assertEquals("test123", var.getValue());
      } else if (var.getName().equals("stringVar")) {
        assertEquals("stringVar", var.getName());
        assertEquals("test", var.getValue());
      } else {
        fail("An unexpected variable '" + var.getName() + "' was found with value " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByExecutionIdWithoutAnyResult() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().executionIdIn("anExecutionId");

    // then
    List<VariableInstance> result = query.list();
    assertTrue(result.isEmpty());

    assertEquals(0, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByTaskId() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    taskService.setVariableLocal(task.getId(), "taskVariable", "aCustomValue");

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().taskIdIn(task.getId());

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    assertEquals(1, query.count());

    VariableInstance var = result.get(0);
    assertEquals("taskVariable", var.getName());
    assertEquals("aCustomValue", var.getValue());
    assertEquals("string", var.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByTaskIds() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY);

    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    Task task3 = taskService.createTaskQuery().processInstanceId(processInstance3.getId()).singleResult();

    taskService.setVariableLocal(task1.getId(), "taskVariable", "aCustomValue");
    taskService.setVariableLocal(task2.getId(), "anotherTaskVariable", "aCustomValue");

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().taskIdIn(task1.getId(), task2.getId(), task3.getId());

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, query.count());

    for (VariableInstance var : result) {
      assertEquals("string", var.getTypeName());
      if (var.getName().equals("taskVariable")) {
        assertEquals("taskVariable", var.getName());
        assertEquals("aCustomValue", var.getValue());
      } else if (var.getName().equals("anotherTaskVariable")) {
        assertEquals("anotherTaskVariable", var.getName());
        assertEquals("aCustomValue", var.getValue());
      } else {
        fail("An unexpected variable '" + var.getName() + "' was found with value " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByTaskIdWithoutAnyResult() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    taskService.setVariableLocal(task.getId(), "taskVariable", "aCustomValue");

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().taskIdIn("aTaskId");

    // then
    List<VariableInstance> result = query.list();
    assertTrue(result.isEmpty());

    assertEquals(0, query.count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/VariableInstanceQueryTest.taskInEmbeddedSubProcess.bpmn20.xml"})
  public void testQueryByVariableScopeId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    // get variable scope ids
    String taskId = task.getId();
    String executionId = task.getExecutionId();
    String processInstanceId = task.getProcessInstanceId();

    // set variables
    String variableName = "foo";
    Map<String, String> variables = new HashMap<>();
    variables.put(taskId, "task");
    variables.put(executionId, "execution");
    variables.put(processInstanceId, "processInstance");

    taskService.setVariableLocal(taskId, variableName, variables.get(taskId));
    runtimeService.setVariableLocal(executionId, variableName, variables.get(executionId));
    runtimeService.setVariableLocal(processInstanceId, variableName, variables.get(processInstanceId));

    List<VariableInstance> variableInstances;

    // query by variable scope id
    for (String variableScopeId : variables.keySet()) {
      variableInstances = runtimeService.createVariableInstanceQuery().variableScopeIdIn(variableScopeId).list();
      assertEquals(1, variableInstances.size());
      assertEquals(variableName, variableInstances.get(0).getName());
      assertEquals(variables.get(variableScopeId), variableInstances.get(0).getValue());
    }

    // query by multiple variable scope ids
    variableInstances = runtimeService.createVariableInstanceQuery().variableScopeIdIn(taskId, executionId, processInstanceId).list();
    assertEquals(3, variableInstances.size());

    // remove task variable
    taskService.removeVariableLocal(taskId, variableName);

    variableInstances = runtimeService.createVariableInstanceQuery().variableScopeIdIn(taskId).list();
    assertEquals(0, variableInstances.size());

    variableInstances = runtimeService.createVariableInstanceQuery().variableScopeIdIn(taskId, executionId, processInstanceId).list();
    assertEquals(2, variableInstances.size());

    // remove process instance variable variable
    runtimeService.removeVariable(processInstanceId, variableName);

    variableInstances = runtimeService.createVariableInstanceQuery().variableScopeIdIn(processInstanceId, taskId).list();
    assertEquals(0, variableInstances.size());

    variableInstances = runtimeService.createVariableInstanceQuery().variableScopeIdIn(taskId, executionId, processInstanceId).list();
    assertEquals(1, variableInstances.size());

    // remove execution variable
    runtimeService.removeVariable(executionId, variableName);

    variableInstances = runtimeService.createVariableInstanceQuery().variableScopeIdIn(taskId, executionId, processInstanceId).list();
    assertEquals(0, variableInstances.size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByActivityInstanceId() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);
    String activityId = runtimeService.getActivityInstance(processInstance.getId()).getChildActivityInstances()[0].getId();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "taskVariable", "aCustomValue");

    // when
    VariableInstanceQuery taskVariablesQuery = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(activityId);
    VariableInstanceQuery processVariablesQuery = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(processInstance.getId());

    // then
    VariableInstance taskVar = taskVariablesQuery.singleResult();
    assertNotNull(taskVar);

    assertEquals(1, taskVariablesQuery.count());
    assertEquals("string", taskVar.getTypeName());
    assertEquals("taskVariable", taskVar.getName());
    assertEquals("aCustomValue", taskVar.getValue());

    VariableInstance processVar = processVariablesQuery.singleResult();

    assertEquals(1, processVariablesQuery.count());
    assertEquals("string", processVar.getTypeName());
    assertEquals("stringVar", processVar.getName());
    assertEquals("test", processVar.getValue());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByActivityInstanceIds() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("stringVar", "test");
    variables1.put("myVar", "test123");
    ProcessInstance procInst1 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("myVar", "test123");
    ProcessInstance procInst2 =  runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    Map<String, Object> variables3 = new HashMap<>();
    variables3.put("myVar", "test123");
    ProcessInstance procInst3 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables3);

    Task task1 = taskService.createTaskQuery().processInstanceId(procInst1.getId()).singleResult();
    Task task2 = taskService.createTaskQuery().processInstanceId(procInst2.getId()).singleResult();

    taskService.setVariableLocal(task1.getId(), "taskVariable", "aCustomValue");
    taskService.setVariableLocal(task2.getId(), "anotherTaskVariable", "aCustomValue");

    // when
    VariableInstanceQuery processVariablesQuery = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(procInst1.getId(), procInst2.getId(), procInst3.getId());

    VariableInstanceQuery taskVariablesQuery =
            runtimeService.createVariableInstanceQuery()
                          .activityInstanceIdIn(
                                  runtimeService.getActivityInstance(procInst1.getId()).getChildActivityInstances()[0].getId(),
                                  runtimeService.getActivityInstance(procInst2.getId()).getChildActivityInstances()[0].getId());

    // then (process variables)
    List<VariableInstance> result = processVariablesQuery.list();
    assertFalse(result.isEmpty());
    assertEquals(4, result.size());

    assertEquals(4, processVariablesQuery.count());

    for (VariableInstance var : result) {
      assertEquals("string", var.getTypeName());
      if (var.getName().equals("myVar")) {
        assertEquals("myVar", var.getName());
        assertEquals("test123", var.getValue());
      } else if (var.getName().equals("stringVar")) {
        assertEquals("stringVar", var.getName());
        assertEquals("test", var.getValue());
      } else {
        fail("An unexpected variable '" + var.getName() + "' was found with value " + var.getValue());
      }
    }

    // then (task variables)
    result = taskVariablesQuery.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    assertEquals(2, taskVariablesQuery.count());

    for (VariableInstance var : result) {
      assertEquals("string", var.getTypeName());
      if (var.getName().equals("taskVariable")) {
        assertEquals("taskVariable", var.getName());
        assertEquals("aCustomValue", var.getValue());
      } else if (var.getName().equals("anotherTaskVariable")) {
        assertEquals("anotherTaskVariable", var.getName());
        assertEquals("aCustomValue", var.getValue());
      } else {
        fail("An unexpected variable '" + var.getName() + "' was found with value " + var.getValue());
      }
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByName_Asc() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().orderByVariableName().asc();

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    VariableInstance first = result.get(0);
    VariableInstance second = result.get(1);

    assertEquals("myVar", first.getName());
    assertEquals("stringVar", second.getName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByName_Desc() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().orderByVariableName().desc();

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    VariableInstance first = result.get(0);
    VariableInstance second = result.get(1);

    assertEquals("stringVar", first.getName());
    assertEquals("string", first.getTypeName());
    assertEquals("myVar", second.getName());
    assertEquals("string", second.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByType_Asc() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("intVar", 123);
    variables.put("myVar", "test123");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().orderByVariableType().asc();

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    VariableInstance first = result.get(0);
    VariableInstance second = result.get(1);

    assertEquals("intVar", first.getName()); // integer
    assertEquals("integer", first.getTypeName());
    assertEquals("myVar", second.getName()); // string
    assertEquals("string", second.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByType_Desc() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("intVar", 123);
    variables.put("myVar", "test123");
    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().orderByVariableType().desc();

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    VariableInstance first = result.get(0);
    VariableInstance second = result.get(1);

    assertEquals("myVar", first.getName()); // string
    assertEquals("string", first.getTypeName());
    assertEquals("intVar", second.getName()); // integer
    assertEquals("integer", second.getTypeName());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByActivityInstanceId_Asc() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("intVar", 123);
    ProcessInstance procInst1 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);
    String activityId1 = runtimeService.getActivityInstance(procInst1.getId()).getChildActivityInstances()[0].getId();

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "test");
    ProcessInstance procInst2 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);
    String activityId2 = runtimeService.getActivityInstance(procInst2.getId()).getChildActivityInstances()[0].getId();

    int comparisonResult = activityId1.compareTo(activityId2);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().orderByActivityInstanceId().asc();

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    VariableInstance first = result.get(0);
    VariableInstance second = result.get(1);

    if (comparisonResult < 0) {
      assertEquals("intVar", first.getName());
      assertEquals("integer", first.getTypeName());
      assertEquals("stringVar", second.getName());
      assertEquals("string", second.getTypeName());
    } else if (comparisonResult > 0) {
      assertEquals("stringVar", first.getName());
      assertEquals("string", first.getTypeName());
      assertEquals("intVar", second.getName());
      assertEquals("integer", second.getTypeName());
    } else {
      fail("Something went wrong: both activity instances have the same id " + activityId1 + " and " + activityId2);
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByActivityInstanceId_Desc() {
    // given
    Map<String, Object> variables1 = new HashMap<>();
    variables1.put("intVar", 123);
    ProcessInstance procInst1 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables1);

    Map<String, Object> variables2 = new HashMap<>();
    variables2.put("stringVar", "test");
    ProcessInstance procInst2 = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables2);

    String activityId1 = runtimeService.getActivityInstance(procInst1.getId()).getChildActivityInstances()[0].getId();
    String activityId2 = runtimeService.getActivityInstance(procInst2.getId()).getChildActivityInstances()[0].getId();

    int comparisonResult = activityId1.compareTo(activityId2);
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().orderByActivityInstanceId().desc();

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    VariableInstance first = result.get(0);
    VariableInstance second = result.get(1);

    if (comparisonResult < 0) {
      assertEquals("stringVar", first.getName());
      assertEquals("string", first.getTypeName());
      assertEquals("intVar", second.getName());
      assertEquals("integer", second.getTypeName());
    } else if (comparisonResult > 0) {
      assertEquals("intVar", first.getName());
      assertEquals("integer", first.getTypeName());
      assertEquals("stringVar", second.getName());
      assertEquals("string", second.getTypeName());
    } else {
      fail("Something went wrong: both activity instances have the same id " + activityId1 + " and " + activityId2);
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testGetValueOfSerializableVar() {
    // given
    List<String> serializable = new ArrayList<>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");

    Map<String, Object> variables = new HashMap<>();
    variables.put("serializableVar", serializable);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().processInstanceIdIn(processInstance.getId());

    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());

    VariableInstance instance = result.get(0);

    assertEquals("serializableVar", instance.getName());
    assertNotNull(instance.getValue());
    assertEquals(serializable, instance.getValue());
    assertEquals(ValueType.OBJECT.getName(), instance.getTypeName());

  }


  @Test
  @Deployment
  public void testSubProcessVariablesWithParallelGateway() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processWithSubProcess");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertNotNull(tree);
    ActivityInstance[] subprocessInstances = tree.getActivityInstances("SubProcess_1");
    assertEquals(5, subprocessInstances.length);

    //when
    String activityInstanceId1 = subprocessInstances[0].getId();
    VariableInstanceQuery query1 = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(activityInstanceId1);

    String activityInstanceId2 = subprocessInstances[1].getId();
    VariableInstanceQuery query2 = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(activityInstanceId2);

    String activityInstanceId3 = subprocessInstances[2].getId();
    VariableInstanceQuery query3 = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(activityInstanceId3);

    String activityInstanceId4 = subprocessInstances[3].getId();
    VariableInstanceQuery query4 = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(activityInstanceId4);

    String activityInstanceId5 = subprocessInstances[4].getId();
    VariableInstanceQuery query5 = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(activityInstanceId5);

    // then
    checkVariables(query1.list());
    checkVariables(query2.list());
    checkVariables(query3.list());
    checkVariables(query4.list());
    checkVariables(query5.list());
  }

  private void checkVariables(List<VariableInstance> variableInstances) {
    assertFalse(variableInstances.isEmpty());
    for (VariableInstance instance : variableInstances) {
      if (instance.getName().equals("nrOfInstances")) {
        assertEquals("nrOfInstances", instance.getName());
        assertEquals("integer", instance.getTypeName());
      } else if (instance.getName().equals("nrOfCompletedInstances")) {
        assertEquals("nrOfCompletedInstances", instance.getName());
        assertEquals("integer", instance.getTypeName());
      } else if (instance.getName().equals("nrOfActiveInstances")) {
        assertEquals("nrOfActiveInstances", instance.getName());
        assertEquals("integer", instance.getTypeName());
      } else if (instance.getName().equals("loopCounter")) {
        assertEquals("loopCounter", instance.getName());
        assertEquals("integer", instance.getTypeName());
      } else if (instance.getName().equals("nullVar")) {
        assertEquals("nullVar", instance.getName());
        assertEquals("null", instance.getTypeName());
      } else if (instance.getName().equals("integerVar")) {
        assertEquals("integerVar", instance.getName());
        assertEquals("integer", instance.getTypeName());
      } else if (instance.getName().equals("dateVar")) {
        assertEquals("dateVar", instance.getName());
        assertEquals("date", instance.getTypeName());
      } else if (instance.getName().equals("stringVar")) {
        assertEquals("stringVar", instance.getName());
        assertEquals("string", instance.getTypeName());
      } else if (instance.getName().equals("shortVar")) {
        assertEquals("shortVar", instance.getName());
        assertEquals("short", instance.getTypeName());
      } else if (instance.getName().equals("longVar")) {
        assertEquals("longVar", instance.getName());
        assertEquals("long", instance.getTypeName());
      } else if (instance.getName().equals("byteVar")) {
        assertEquals("bytes", instance.getTypeName());
      } else if (instance.getName().equals("serializableVar")) {
        assertEquals("serializableVar", instance.getName());
        try {
          instance.getValue();
        } catch(NullPointerException e) {
          // the serialized value has not been initially loaded
        }
      } else {
        fail("An unexpected variable '" + instance.getName() + "' was found with value " + instance.getValue());
      }
    }
  }

  @Test
  @Deployment
  public void testSubProcessVariables() {
    // given
    Map<String, Object> processVariables = new HashMap<>();
    processVariables.put("processVariable", "aProcessVariable");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processWithSubProcess", processVariables);

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertNotNull(tree);
    assertEquals(1, tree.getChildActivityInstances().length);

    // when
    VariableInstanceQuery query1 = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(tree.getId());

    // then
    VariableInstance processVariable = query1.singleResult();
    assertNotNull(processVariable);
    assertEquals("processVariable", processVariable.getName());
    assertEquals("aProcessVariable", processVariable.getValue());

    // when
    ActivityInstance subProcessActivityInstance = tree.getActivityInstances("SubProcess_1")[0];
    VariableInstanceQuery query2 = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(subProcessActivityInstance.getId());

    // then
    checkVariables(query2.list());

    // when setting a task local variable
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariableLocal(task.getId(), "taskVariable", "taskVariableValue");

    // skip mi body instance
    ActivityInstance taskActivityInstance = subProcessActivityInstance.getChildActivityInstances()[0];
    VariableInstanceQuery query3 = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(taskActivityInstance.getId());

    // then
    VariableInstance taskVariable = query3.singleResult();
    assertNotNull(taskVariable);
    assertEquals("taskVariable", taskVariable.getName());
    assertEquals("taskVariableValue", taskVariable.getValue());
  }

  @Test
  @Deployment
  public void testParallelGatewayVariables() {
    // given
    Map<String, Object> processVariables = new HashMap<>();
    processVariables.put("processVariable", "aProcessVariable");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGatewayProcess", processVariables);

    Execution execution = runtimeService.createExecutionQuery().activityId("task1").singleResult();
    runtimeService.setVariableLocal(execution.getId(), "aLocalVariable", "aLocalValue");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertEquals(2, tree.getChildActivityInstances().length);
    ActivityInstance task1Instance = tree.getActivityInstances("task1")[0];

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .variableName("aLocalVariable")
        .activityInstanceIdIn(task1Instance.getId());
    VariableInstance localVariable = query.singleResult();
    assertNotNull(localVariable);
    assertEquals("aLocalVariable", localVariable.getName());
    assertEquals("aLocalValue", localVariable.getValue());

    Task task = taskService.createTaskQuery().executionId(execution.getId()).singleResult();
    taskService.complete(task.getId());

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertEquals(2, tree.getChildActivityInstances().length);
    ActivityInstance task3Instance = tree.getActivityInstances("task3")[0];

    query = runtimeService
        .createVariableInstanceQuery()
        .variableName("aLocalVariable")
        .activityInstanceIdIn(task3Instance.getId());
    localVariable = query.singleResult();
    assertNotNull(localVariable);
    assertEquals("aLocalVariable", localVariable.getName());
    assertEquals("aLocalValue", localVariable.getValue());
  }

  @Deployment
  @Test
  public void testSimpleSubProcessVariables() {
    // given
    Map<String, Object> processVariables = new HashMap<>();
    processVariables.put("processVariable", "aProcessVariable");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processWithSubProcess", processVariables);

    Task task = taskService.createTaskQuery().taskDefinitionKey("UserTask_1").singleResult();
    runtimeService.setVariableLocal(task.getExecutionId(), "aLocalVariable", "aLocalValue");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertEquals(1, tree.getChildActivityInstances().length);
    ActivityInstance subProcessInstance = tree.getActivityInstances("SubProcess_1")[0];

    // then the local variable has activity instance Id of the subprocess
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(subProcessInstance.getId());
    VariableInstance localVariable = query.singleResult();
    assertNotNull(localVariable);
    assertEquals("aLocalVariable", localVariable.getName());
    assertEquals("aLocalValue", localVariable.getValue());

    // and the global variable has the activity instance Id of the process instance:
    query = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(processInstance.getId());
    VariableInstance globalVariable = query.singleResult();
    assertNotNull(localVariable);
    assertEquals("processVariable", globalVariable.getName());
    assertEquals("aProcessVariable", globalVariable.getValue());

    taskService.complete(task.getId());

  }

  @Test
  public void testDisableBinaryFetching() {
    byte[] binaryContent = "some binary content".getBytes();

    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("binaryVariable", binaryContent);
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.setVariablesLocal(task.getId(), variables);

    // when binary fetching is enabled (default)
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then value is fetched
    VariableInstance result = query.singleResult();
    assertNotNull(result.getValue());

    // when binary fetching is disabled
    query = runtimeService.createVariableInstanceQuery().disableBinaryFetching();

    // then value is not fetched
    result = query.singleResult();
    assertNull(result.getValue());

    // delete task
    taskService.deleteTask(task.getId(), true);
  }

  @Test
  @Deployment(resources= "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
  public void testDisableBinaryFetchingForFileValues() {
    // given
    String fileName = "text.txt";
    String encoding = "crazy-encoding";
    String mimeType = "martini/dry";

    FileValue fileValue = Variables
        .fileValue(fileName)
        .file("ABC".getBytes())
        .encoding(encoding)
        .mimeType(mimeType)
        .create();

    runtimeService.startProcessInstanceByKey(PROC_DEF_KEY,
        Variables.createVariables().putValueTyped("fileVar", fileValue));

    // when enabling binary fetching
    VariableInstance fileVariableInstance =
        runtimeService.createVariableInstanceQuery().singleResult();

    // then the binary value is accessible
    assertNotNull(fileVariableInstance.getValue());

    // when disabling binary fetching
    fileVariableInstance =
        runtimeService.createVariableInstanceQuery().disableBinaryFetching().singleResult();

    // then the byte value is not fetched
    assertNotNull(fileVariableInstance);
    assertEquals("fileVar", fileVariableInstance.getName());

    assertNull(fileVariableInstance.getValue());

    FileValue typedValue = (FileValue) fileVariableInstance.getTypedValue();
    assertNull(typedValue.getValue());

    // but typed value metadata is accessible
    assertEquals(ValueType.FILE, typedValue.getType());
    assertEquals(fileName, typedValue.getFilename());
    assertEquals(encoding, typedValue.getEncoding());
    assertEquals(mimeType, typedValue.getMimeType());

  }

  @Test
  public void testDisableCustomObjectDeserialization() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("customSerializable", new CustomSerializable());
    variables.put("failingSerializable", new FailingSerializable());
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.setVariablesLocal(task.getId(), variables);

    // when
    VariableInstanceQuery query =
        runtimeService.createVariableInstanceQuery().disableCustomObjectDeserialization();

    // then
    List<VariableInstance> results = query.list();

    // both variables are not deserialized, but their serialized values are available
    assertEquals(2, results.size());

    for (VariableInstance variableInstance : results) {
      assertNull(variableInstance.getErrorMessage());

      ObjectValue typedValue = (ObjectValue) variableInstance.getTypedValue();
      assertNotNull(typedValue);
      assertFalse(typedValue.isDeserialized());
      // cannot access the deserialized value
      try {
        typedValue.getValue();
      }
      catch(IllegalStateException e) {
        testRule.assertTextPresent("Object is not deserialized", e.getMessage());
      }
      assertNotNull(typedValue.getValueSerialized());
    }

    // delete task
    taskService.deleteTask(task.getId(), true);
  }

  @Test
  public void testSerializableErrorMessage() {

    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("customSerializable", new CustomSerializable());
    variables.put("failingSerializable", new FailingSerializable());
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.setVariablesLocal(task.getId(), variables);

    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // then
    List<VariableInstance> results = query.list();

    // both variables are fetched
    assertEquals(2, results.size());

    for (VariableInstance variableInstance : results) {
      if(variableInstance.getName().equals("customSerializable")) {
        assertNotNull(variableInstance.getValue());
        assertTrue(variableInstance.getValue() instanceof CustomSerializable);
      }
      if(variableInstance.getName().equals("failingSerializable")) {
        // no value was fetched
        assertNull(variableInstance.getValue());
        // error message is present
        assertNotNull(variableInstance.getErrorMessage());
      }
    }

    // delete task
    taskService.deleteTask(task.getId(), true);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseExecutionId() {
    CaseInstance instance = caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("aVariableName", "abc")
      .create();

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    query
      .caseExecutionIdIn(instance.getId());

    VariableInstance result = query.singleResult();

    assertNotNull(result);

    assertEquals("aVariableName", result.getName());
    assertEquals("abc", result.getValue());
    assertEquals(instance.getId(), result.getCaseExecutionId());
    assertEquals(instance.getId(), result.getCaseInstanceId());

    assertNull(result.getExecutionId());
    assertNull(result.getProcessInstanceId());
    assertNull(result.getProcessDefinitionId());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseExecutionIds() {
    CaseInstance instance1 = caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("aVariableName", "abc")
      .create();

    CaseInstance instance2 = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .setVariable("anotherVariableName", "xyz")
        .create();

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    query
      .caseExecutionIdIn(instance1.getId(), instance2.getId())
      .orderByVariableName()
      .asc();

    List<VariableInstance> result = query.list();

    assertEquals(2, result.size());

    for (VariableInstance variableInstance : result) {
      if (variableInstance.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variableInstance.getName());
        assertEquals("abc", variableInstance.getValue());
      } else if (variableInstance.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variableInstance.getName());
        assertEquals("xyz", variableInstance.getValue());
      } else {
        fail("Unexpected variable: " + variableInstance.getName());
      }

    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseInstanceId() {
    CaseInstance instance = caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("aVariableName", "abc")
      .create();

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    query
      .caseInstanceIdIn(instance.getId());

    VariableInstance result = query.singleResult();

    assertNotNull(result);

    assertEquals("aVariableName", result.getName());
    assertEquals("abc", result.getValue());
    assertEquals(instance.getId(), result.getCaseExecutionId());
    assertEquals(instance.getId(), result.getCaseInstanceId());

    assertNull(result.getExecutionId());
    assertNull(result.getProcessInstanceId());
    assertNull(result.getProcessDefinitionId());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseInstanceIds() {
    CaseInstance instance1 = caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("aVariableName", "abc")
      .create();

    CaseInstance instance2 = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .setVariable("anotherVariableName", "xyz")
        .create();

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    query
      .caseInstanceIdIn(instance1.getId(), instance2.getId())
      .orderByVariableName()
      .asc();

    List<VariableInstance> result = query.list();

    assertEquals(2, result.size());

    for (VariableInstance variableInstance : result) {
      if (variableInstance.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variableInstance.getName());
        assertEquals("abc", variableInstance.getValue());
      } else if (variableInstance.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variableInstance.getName());
        assertEquals("xyz", variableInstance.getValue());
      } else {
        fail("Unexpected variable: " + variableInstance.getName());
      }

    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseActivityInstanceId() {
    CaseInstance instance = caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("aVariableName", "abc")
      .create();

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    query
      .activityInstanceIdIn(instance.getId());

    VariableInstance result = query.singleResult();

    assertNotNull(result);

    assertEquals("aVariableName", result.getName());
    assertEquals("abc", result.getValue());
    assertEquals(instance.getId(), result.getCaseExecutionId());
    assertEquals(instance.getId(), result.getCaseInstanceId());

    assertNull(result.getExecutionId());
    assertNull(result.getProcessInstanceId());
    assertNull(result.getProcessDefinitionId());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseActivityInstanceIds() {
    CaseInstance instance1 = caseService
      .withCaseDefinitionByKey("oneTaskCase")
      .setVariable("aVariableName", "abc")
      .create();

    CaseInstance instance2 = caseService
        .withCaseDefinitionByKey("oneTaskCase")
        .setVariable("anotherVariableName", "xyz")
        .create();

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    query
      // activityInstanceId == caseExecutionId
      .activityInstanceIdIn(instance1.getId(), instance2.getId())
      .orderByVariableName()
      .asc();

    List<VariableInstance> result = query.list();

    assertEquals(2, result.size());

    for (VariableInstance variableInstance : result) {
      if (variableInstance.getName().equals("aVariableName")) {
        assertEquals("aVariableName", variableInstance.getName());
        assertEquals("abc", variableInstance.getValue());
      } else if (variableInstance.getName().equals("anotherVariableName")) {
        assertEquals("anotherVariableName", variableInstance.getName());
        assertEquals("xyz", variableInstance.getValue());
      } else {
        fail("Unexpected variable: " + variableInstance.getName());
      }

    }
  }

  @Deployment
  @Test
  public void testSequentialMultiInstanceSubProcess() {
    // given a process instance in sequential MI
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSequentialSubprocess");

    // when
    VariableInstance nrOfInstances = runtimeService.createVariableInstanceQuery()
        .variableName("nrOfInstances").singleResult();
    VariableInstance nrOfActiveInstances = runtimeService.createVariableInstanceQuery()
        .variableName("nrOfActiveInstances").singleResult();
    VariableInstance nrOfCompletedInstances = runtimeService.createVariableInstanceQuery()
        .variableName("nrOfCompletedInstances").singleResult();
    VariableInstance loopCounter = runtimeService.createVariableInstanceQuery()
        .variableName("loopCounter").singleResult();

    // then the activity instance ids of the variable instances should be correct
    ActivityInstance tree = runtimeService.getActivityInstance(instance.getId());
    assertEquals(tree.getActivityInstances("miSubProcess#multiInstanceBody")[0].getId(), nrOfInstances.getActivityInstanceId());
    assertEquals(tree.getActivityInstances("miSubProcess#multiInstanceBody")[0].getId(), nrOfActiveInstances.getActivityInstanceId());
    assertEquals(tree.getActivityInstances("miSubProcess#multiInstanceBody")[0].getId(), nrOfCompletedInstances.getActivityInstanceId());
    assertEquals(tree.getActivityInstances("miSubProcess#multiInstanceBody")[0].getId(), loopCounter.getActivityInstanceId());

  }

  @Deployment
  @Test
  public void testParallelMultiInstanceSubProcess() {
    // given a process instance in sequential MI
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSequentialSubprocess");

    // when
    VariableInstance nrOfInstances = runtimeService.createVariableInstanceQuery()
        .variableName("nrOfInstances").singleResult();
    VariableInstance nrOfActiveInstances = runtimeService.createVariableInstanceQuery()
        .variableName("nrOfActiveInstances").singleResult();
    VariableInstance nrOfCompletedInstances = runtimeService.createVariableInstanceQuery()
        .variableName("nrOfCompletedInstances").singleResult();
    List<VariableInstance> loopCounters = runtimeService.createVariableInstanceQuery()
        .variableName("loopCounter").list();

    // then the activity instance ids of the variable instances should be correct
    ActivityInstance tree = runtimeService.getActivityInstance(instance.getId());
    assertEquals(tree.getActivityInstances("miSubProcess#multiInstanceBody")[0].getId(), nrOfInstances.getActivityInstanceId());
    assertEquals(tree.getActivityInstances("miSubProcess#multiInstanceBody")[0].getId(), nrOfActiveInstances.getActivityInstanceId());
    assertEquals(tree.getActivityInstances("miSubProcess#multiInstanceBody")[0].getId(), nrOfCompletedInstances.getActivityInstanceId());

    Set<String> loopCounterActivityInstanceIds = new HashSet<>();
    for (VariableInstance loopCounter : loopCounters) {
      loopCounterActivityInstanceIds.add(loopCounter.getActivityInstanceId());
    }

    assertEquals(4, loopCounterActivityInstanceIds.size());

    for (ActivityInstance subProcessInstance : tree.getActivityInstances("miSubProcess")) {
      assertTrue(loopCounterActivityInstanceIds.contains(subProcessInstance.getId()));
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testVariablesProcessDefinitionId() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROC_DEF_KEY,
        Variables.createVariables().putValue("foo", "bar"));

    // when
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();

    // then
    assertNotNull(variable);
    assertEquals(processInstance.getProcessDefinitionId(), variable.getProcessDefinitionId());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void shouldGetBatchId() {
    // given
    String processInstanceId =
        runtimeService.startProcessInstanceByKey(PROC_DEF_KEY).getId();

    List<String> processInstances = Collections.singletonList(processInstanceId);

    VariableMap variables = Variables.putValue("foo", "bar");

    Batch batch = runtimeService.setVariablesAsync(processInstances, variables);

    // when
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();

    // then
    assertThat(variableInstance.getBatchId()).isEqualTo(batch.getId());

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryForBatchId() {
    // given
    VariableMap variables = Variables.putValue("foo", "bar");

    String processInstanceId =
        runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables).getId();

    List<String> processInstances = Collections.singletonList(processInstanceId);

    Batch batch = runtimeService.setVariablesAsync(processInstances, variables);

    VariableInstanceQuery variableInstanceQuery = runtimeService.createVariableInstanceQuery();

    // assume
    assertThat(variableInstanceQuery.list())
        .extracting("name", "value", "batchId")
        .containsExactlyInAnyOrder(
            tuple("foo", "bar", batch.getId()),
            tuple("foo", "bar", null)
        );

    // when
    variableInstanceQuery = variableInstanceQuery.batchIdIn(batch.getId());

    // then
    assertThat(variableInstanceQuery.list())
        .extracting("name", "value", "batchId")
        .containsExactly(
            tuple("foo", "bar", batch.getId())
        );

    // clear
    managementService.deleteBatch(batch.getId(), true);
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryForBatchIds() {
    // given
    VariableMap variables = Variables.putValue("foo", "bar");

    String processInstanceId =
        runtimeService.startProcessInstanceByKey(PROC_DEF_KEY, variables).getId();

    List<String> processInstances = Collections.singletonList(processInstanceId);

    Batch batchOne = runtimeService.setVariablesAsync(processInstances, variables);
    Batch batchTwo = runtimeService.setVariablesAsync(processInstances, variables);
    Batch batchThree = runtimeService.setVariablesAsync(processInstances, variables);

    VariableInstanceQuery variableInstanceQuery = runtimeService.createVariableInstanceQuery();

    // assume
    assertThat(variableInstanceQuery.list())
        .extracting("name", "value", "batchId")
        .containsExactlyInAnyOrder(
            tuple("foo", "bar", batchOne.getId()),
            tuple("foo", "bar", batchTwo.getId()),
            tuple("foo", "bar", batchThree.getId()),
            tuple("foo", "bar", null)
        );

    // when
    variableInstanceQuery = variableInstanceQuery.batchIdIn(
        batchOne.getId(),
        batchTwo.getId()
    );

    // then
    assertThat(variableInstanceQuery.list())
        .extracting("name", "value", "batchId")
        .containsExactlyInAnyOrder(
            tuple("foo", "bar", batchOne.getId()),
            tuple("foo", "bar", batchTwo.getId())
        );

    // clear
    managementService.deleteBatch(batchOne.getId(), true);
    managementService.deleteBatch(batchTwo.getId(), true);
    managementService.deleteBatch(batchThree.getId(), true);
  }

}
