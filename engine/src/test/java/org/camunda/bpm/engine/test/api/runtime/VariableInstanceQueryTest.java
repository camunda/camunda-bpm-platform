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
package org.camunda.bpm.engine.test.api.runtime;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author roman.smirnov
 */
public class VariableInstanceQueryTest extends PluggableProcessEngineTestCase {
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQuery() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("intVar", (int)123);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);
    
    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables2);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    
    // then
    assertNotNull(query);
    
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    assertEquals(2, query.count());
    
    for (VariableInstance var : result) {
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
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableName() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableName("stringVar");
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("stringVar", var.getName());
    assertEquals("test", var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableNameLike() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableNameLike("%ingV%");
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("stringVar", var.getName());
    assertEquals("test", var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableNameLikeWithoutAnyResult() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("stringVar", "test");
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("stringVar", var.getName());
    assertEquals("test", var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableValueEquals_String() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("test");
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("stringVar", var.getName());
    assertEquals("test", var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Integer() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("intValue", (int) 1234);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("intValue", (int) 1234);
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("intValue", var.getName());
    assertEquals(1234, var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableValueEquals_Integer() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("intValue", (int) 1234);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals((int) 1234);
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("intValue", var.getName());
    assertEquals(1234, var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Long() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longValue", 123456L);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
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
  }
 
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableValueEquals_Long() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longValue", 123456L);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals(123456L);
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("longValue", var.getName());
    assertEquals(123456L, var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Double() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("doubleValue", 123.456);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
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
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableValueEquals_Double() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("doubleValue", 123.456);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals(123.456);
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("doubleValue", var.getName());
    assertEquals(123.456, var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Short() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("shortValue", (short) 123);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
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
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableValueEquals_Short() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("shortValue", (short) 123);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals((short) 123);
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("shortValue", var.getName());
    assertEquals((short) 123, var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEquals_Bytes() {
    // given
    byte[] bytes = "somebytes".getBytes();
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("bytesVar", bytes);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
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
  public void testQueryByVariableValueEquals_Bytes() {
    // given
    byte[] bytes = "somebytes".getBytes();
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("bytesVar", bytes);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals(bytes);
    
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
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("date", now);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
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
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByVariableValueEquals_Date() {
    // given
     Date now = new Date();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("date", now);
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals(now);
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    
    assertEquals(1, query.count());
    
    VariableInstance var = result.get(0);
    assertEquals("date", var.getName());
    assertEquals(now, var.getValue());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByNameAndVariableValueEqualsWihtoutAnyResult() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableValueEquals("stringVar", "notFoundValue");
    
    // then
    List<VariableInstance> result = query.list();
    assertTrue(result.isEmpty());
    
    assertEquals(0, query.count());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByProcessInstanceId() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().processInstanceIdIn(processInstance.getId());
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    assertEquals(2, query.count());
    
    for (VariableInstance var : result) {
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().processInstanceIdIn(processInstance1.getId(), processInstance2.getId());
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    assertEquals(2, query.count());
    
    for (VariableInstance var : result) {
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().executionIdIn(processInstance.getId());
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    assertEquals(2, query.count());
    
    for (VariableInstance var : result) {
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
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("myVar", "test123");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);
    
    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("myVar", "test123");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables2);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().executionIdIn(processInstance1.getId(), processInstance2.getId());
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(3, result.size());
    
    assertEquals(3, query.count());
    
    for (VariableInstance var : result) {
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
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
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByTaskIds() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
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
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByActivityInstanceId() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    ExecutionEntity execution = (ExecutionEntity) processInstance;
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "taskVariable", "aCustomValue");
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(execution.getActivityInstanceId());
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    assertEquals(2, query.count());
    
    for (VariableInstance var : result) {
      if (var.getName().equals("stringVar")) {
        assertEquals("stringVar", var.getName());
        assertEquals("test", var.getValue());
      } else if (var.getName().equals("taskVariable")) {
        assertEquals("taskVariable", var.getName());
        assertEquals("aCustomValue", var.getValue());
      } else {
        fail("An unexpected variable '" + var.getName() + "' was found with value " + var.getValue());
      }
    }
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryByActivityInstanceIds() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("stringVar", "test");
    variables1.put("myVar", "test123");
    ExecutionEntity execution1 = (ExecutionEntity) runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);
    
    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("myVar", "test123");
    ExecutionEntity execution2 = (ExecutionEntity) runtimeService.startProcessInstanceByKey("oneTaskProcess", variables2);
    
    Map<String, Object> variables3 = new HashMap<String, Object>();
    variables3.put("myVar", "test123");
    ExecutionEntity execution3 = (ExecutionEntity) runtimeService.startProcessInstanceByKey("oneTaskProcess", variables3);
    
    Task task1 = taskService.createTaskQuery().processInstanceId(execution1.getProcessInstanceId()).singleResult();
    Task task2 = taskService.createTaskQuery().processInstanceId(execution2.getProcessInstanceId()).singleResult();
    
    taskService.setVariableLocal(task1.getId(), "taskVariable", "aCustomValue");
    taskService.setVariableLocal(task2.getId(), "anotherTaskVariable", "aCustomValue");
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().activityInstanceIdIn(execution1.getActivityInstanceId(), execution2.getActivityInstanceId(), execution3.getActivityInstanceId());
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(6, result.size());
    
    assertEquals(6, query.count());
    
    for (VariableInstance var : result) {
      if (var.getName().equals("myVar")) {
        assertEquals("myVar", var.getName());
        assertEquals("test123", var.getValue());
      } else if (var.getName().equals("stringVar")) {
        assertEquals("stringVar", var.getName());
        assertEquals("test", var.getValue());
      } else if (var.getName().equals("taskVariable")) {
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
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
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "test");
    variables.put("myVar", "test123");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().orderByVariableName().desc();
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    VariableInstance first = result.get(0);
    VariableInstance second = result.get(1);
    
    assertEquals("stringVar", first.getName());
    assertEquals("myVar", second.getName());
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByType_Asc() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("intVar", (int)123);
    variables.put("myVar", "test123");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().orderByVariableType().asc();
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    VariableInstance first = result.get(0);
    VariableInstance second = result.get(1);
    
    assertEquals("intVar", first.getName()); // integer
    assertEquals("myVar", second.getName()); // string
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByType_Desc() {
    // given
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("intVar", (int)123);
    variables.put("myVar", "test123");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // when
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().orderByVariableType().desc();
    
    // then
    List<VariableInstance> result = query.list();
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    
    VariableInstance first = result.get(0);
    VariableInstance second = result.get(1);
    
    assertEquals("myVar", first.getName()); // string
    assertEquals("intVar", second.getName()); // integer
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByActivityInstanceId_Asc() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("intVar", (int)123);
    ExecutionEntity execution1 = (ExecutionEntity) runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);
    
    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("stringVar", "test");
    ExecutionEntity execution2 = (ExecutionEntity) runtimeService.startProcessInstanceByKey("oneTaskProcess", variables2);
    
    int comparisonResult = execution1.getActivityInstanceId().compareTo(execution2.getActivityInstanceId());
    
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
      assertEquals("stringVar", second.getName());
    } else if (comparisonResult > 0) {
      assertEquals("stringVar", first.getName());
      assertEquals("intVar", second.getName());      
    } else {
      fail("Something went wrong: both activity instances have the same id " + execution1.getActivityInstanceId() + " and " + execution2.getActivityInstanceId());
    }
  }
  
  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testQueryOrderByActivityInstanceId_Desc() {
    // given
    Map<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("intVar", (int)123);
    ExecutionEntity execution1 = (ExecutionEntity) runtimeService.startProcessInstanceByKey("oneTaskProcess", variables1);
    
    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("stringVar", "test");
    ExecutionEntity execution2 = (ExecutionEntity) runtimeService.startProcessInstanceByKey("oneTaskProcess", variables2);
    
    int comparisonResult = execution1.getActivityInstanceId().compareTo(execution2.getActivityInstanceId());
    
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
      assertEquals("intVar", second.getName());   
    } else if (comparisonResult > 0) {
      assertEquals("intVar", first.getName());
      assertEquals("stringVar", second.getName());   
    } else {
      fail("Something went wrong: both activity instances have the same id " + execution1.getActivityInstanceId() + " and " + execution2.getActivityInstanceId());
    }
  }
  
}
