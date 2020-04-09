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
package org.camunda.bpm.engine.test.bpmn.servicetask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ClassLoadingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.servicetask.util.GenderBean;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class JavaServiceTaskTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testJavaServiceDelegation() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("javaServiceDelegation", CollectionUtil.singletonMap("input", "Activiti BPM Engine"));
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState")
      .singleResult();
    assertEquals("ACTIVITI BPM ENGINE", runtimeService.getVariable(execution.getId(), "input"));
  }

  @Deployment
  @Test
  public void testFieldInjection() {
    // Process contains 2 service-tasks using field-injection. One should use the exposed setter,
    // the other is using the private field.
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("fieldInjection");
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState")
      .singleResult();

    assertEquals("HELLO WORLD", runtimeService.getVariable(execution.getId(), "var"));
    assertEquals("HELLO SETTER", runtimeService.getVariable(execution.getId(), "setterVar"));
  }

  @Deployment
  @Test
  public void testExpressionFieldInjection() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("name", "kermit");
    vars.put("gender", "male");
    vars.put("genderBean", new GenderBean());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("expressionFieldInjection", vars);
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState")
      .singleResult();

    assertEquals("timrek .rM olleH", runtimeService.getVariable(execution.getId(), "var2"));
    assertEquals("elam :si redneg ruoY", runtimeService.getVariable(execution.getId(), "var1"));
  }

  @Deployment
  @Test
  public void testUnexistingClassDelegation() {
    try {
      runtimeService.startProcessInstanceByKey("unexistingClassDelegation");
      fail();
    } catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains("Exception while instantiating class 'org.camunda.bpm.engine.test.BogusClass'"));
      assertNotNull(e.getCause());
      assertTrue(e.getCause() instanceof ClassLoadingException);
    }
  }

  @Test
  public void testIllegalUseOfResultVariableName() {
    try {
      repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/bpmn/servicetask/JavaServiceTaskTest.testIllegalUseOfResultVariableName.bpmn20.xml").deploy();
      fail();
    } catch (ProcessEngineException e) {
      assertTrue(e.getMessage().contains("resultVariable"));
    }
  }

  @Deployment
  @Test
  public void testExceptionHandling() {

    // If variable value is != 'throw-exception', process goes
    // through service task and ends immidiately
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", "no-exception");
    runtimeService.startProcessInstanceByKey("exceptionHandling", vars);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // If variable value == 'throw-exception', process executes
    // service task, which generates and catches exception,
    // and takes sequence flow to user task
    vars.put("var", "throw-exception");
    runtimeService.startProcessInstanceByKey("exceptionHandling", vars);
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Fix Exception", task.getName());
  }

  @Deployment
  @Test
  public void testGetBusinessKeyFromDelegateExecution() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("businessKeyProcess", "1234567890");
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("businessKeyProcess").count());

    // Check if business-key was available from the process
    String key = (String) runtimeService.getVariable(processInstance.getId(), "businessKeySetOnExecution");
    assertNotNull(key);
    assertEquals("1234567890", key);

    // check if BaseDelegateExecution#getBusinessKey() behaves like DelegateExecution#getProcessBusinessKey()
    String key2 = (String) runtimeService.getVariable(processInstance.getId(), "businessKeyAsProcessBusinessKey");
    assertEquals(key2, key);
  }

}
