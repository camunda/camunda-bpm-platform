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
package org.camunda.bpm.engine.test.bpmn.callactivity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.CallActivityBuilder;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOut;
import org.junit.Test;

/**
 * @author Joram Barrez
 * @author Nils Preusker
 * @author Bernd Ruecker
 * @author Falko Menge
 */
public class CallActivityTest extends PluggableProcessEngineTest {

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"
  })
  @Test
  public void testCallSimpleSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

    // Completing the task continues the process which leads to calling the subprocess
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcessParentVariableAccess.bpmn20.xml"
  })
  @Test
  public void testAccessSuperInstanceVariables() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

    // the variable does not yet exist
    assertNull(runtimeService.getVariable(processInstance.getId(), "greeting"));

    // completing the task executed the sub process
    taskService.complete(taskBeforeSubProcess.getId());

    // now the variable exists
    assertEquals("hello", runtimeService.getVariable(processInstance.getId(), "greeting"));

  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/concurrentSubProcessParentVariableAccess.bpmn20.xml"
  })
  @Test
  public void testAccessSuperInstanceVariablesFromConcurrentExecution() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

    // the variable does not yet exist
    assertNull(runtimeService.getVariable(processInstance.getId(), "greeting"));

    // completing the task executed the sub process
    taskService.complete(taskBeforeSubProcess.getId());

    // now the variable exists
    assertEquals("hello", runtimeService.getVariable(processInstance.getId(), "greeting"));

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithExpressions.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testCallSimpleSubProcessWithExpressions() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    // one task in the subprocess should be active after starting the process
    // instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

    // Completing the task continues the process which leads to calling the
    // subprocess. The sub process we want to call is passed in as a variable
    // into this task
    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess");
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());
    testRule.assertProcessEnded(processInstance.getId());
  }

  /**
   * Test case for a possible tricky case: reaching the end event of the
   * subprocess leads to an end event in the super process instance.
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessEndsSuperProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessEndsSuperProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessEndsSuperProcess");

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskBeforeSubProcess.getName());

    // Completing this task ends the subprocess which leads to the end of the whole process instance
    taskService.complete(taskBeforeSubProcess.getId());
    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallParallelSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleParallelSubProcess.bpmn20.xml"})
  @Test
  public void testCallParallelSubProcess() {
    runtimeService.startProcessInstanceByKey("callParallelSubProcess");

    // The two tasks in the parallel subprocess should be active
    TaskQuery taskQuery = taskService
            .createTaskQuery()
            .orderByTaskName()
            .asc();
    List<Task> tasks = taskQuery.list();
    assertEquals(2, tasks.size());

    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertEquals("Task A", taskA.getName());
    assertEquals("Task B", taskB.getName());

    // Completing the first task should not end the subprocess
    taskService.complete(taskA.getId());
    assertEquals(1, taskQuery.list().size());

    // Completing the second task should end the subprocess and end the whole process instance
    taskService.complete(taskB.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSequentialSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithExpressions.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess2.bpmn20.xml"})
  @Test
  public void testCallSequentialSubProcessWithExpressions() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSequentialSubProcess");

    // FIRST sub process calls simpleSubProcess
    // one task in the subprocess should be active after starting the process
    // instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

    // Completing the task continues the process which leads to calling the
    // subprocess. The sub process we want to call is passed in as a variable
    // into this task
    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess");
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());

    // SECOND sub process calls simpleSubProcess2
    // one task in the subprocess should be active after starting the process
    // instance
    taskQuery = taskService.createTaskQuery();
    taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

    // Completing the task continues the process which leads to calling the
    // subprocess. The sub process we want to call is passed in as a variable
    // into this task
    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess2");
    taskService.complete(taskBeforeSubProcess.getId());
    taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess 2", taskInSubProcess.getName());

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testTimerOnCallActivity.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testTimerOnCallActivity() {
    // After process start, the task in the subprocess should be active
    runtimeService.startProcessInstanceByKey("timerOnCallActivity");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());

    Job timer = managementService.createJobQuery().singleResult();
    assertNotNull(timer);

    managementService.executeJob(timer.getId());

    Task escalatedTask = taskQuery.singleResult();
    assertEquals("Escalated Task", escalatedTask.getName());

    // Completing the task ends the complete process
    taskService.complete(escalatedTask.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * Test case for handing over process variables to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessDataInputOutput.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessWithDataInputOutput() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("superVariable", "Hello from the super process.");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessDataInputOutput", vars);

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskBeforeSubProcess.getName());
    assertEquals("Hello from the super process.", runtimeService.getVariable(taskBeforeSubProcess.getProcessInstanceId(), "subVariable"));
    assertEquals("Hello from the super process.", taskService.getVariable(taskBeforeSubProcess.getId(), "subVariable"));

    runtimeService.setVariable(taskBeforeSubProcess.getProcessInstanceId(), "subVariable", "Hello from sub process.");

    // super variable is unchanged
    assertEquals("Hello from the super process.", runtimeService.getVariable(processInstance.getId(), "superVariable"));

    // Completing this task ends the subprocess which leads to a task in the super process
    taskService.complete(taskBeforeSubProcess.getId());

    // one task in the subprocess should be active after starting the process instance
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task in super process", taskAfterSubProcess.getName());
    assertEquals("Hello from sub process.", runtimeService.getVariable(processInstance.getId(), "superVariable"));
    assertEquals("Hello from sub process.", taskService.getVariable(taskAfterSubProcess.getId(), "superVariable"));

    vars.clear();
    vars.put("x", new Long(5));

    // Completing this task ends the super process which leads to a task in the super process
    taskService.complete(taskAfterSubProcess.getId(), vars);

    // now we are the second time in the sub process but passed variables via expressions
    Task taskInSecondSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSecondSubProcess.getName());
    assertEquals(10l, runtimeService.getVariable(taskInSecondSubProcess.getProcessInstanceId(), "y"));
    assertEquals(10l, taskService.getVariable(taskInSecondSubProcess.getId(), "y"));

    // Completing this task ends the subprocess which leads to a task in the super process
    taskService.complete(taskInSecondSubProcess.getId());

    // one task in the subprocess should be active after starting the process instance
    Task taskAfterSecondSubProcess = taskQuery.singleResult();
    assertEquals("Task in super process", taskAfterSecondSubProcess.getName());
    assertEquals(15l, runtimeService.getVariable(taskAfterSecondSubProcess.getProcessInstanceId(), "z"));
    assertEquals(15l, taskService.getVariable(taskAfterSecondSubProcess.getId(), "z"));

    // and end last task in Super process
    taskService.complete(taskAfterSecondSubProcess.getId());

    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * Test case for handing over process variables to a sub process via the typed
   * api and passing only certain variables
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessLimitedDataInputOutputTypedApi.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessWithLimitedDataInputOutputTypedApi() {

    TypedValue superVariable = Variables.stringValue(null);
    VariableMap vars = Variables.createVariables();
    vars.putValueTyped("superVariable", superVariable);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessDataInputOutput", vars);

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertThat(taskInSubProcess.getName()).isEqualTo("Task in subprocess");
    assertThat(runtimeService.<TypedValue>getVariableTyped(taskInSubProcess.getProcessInstanceId(), "subVariable")).isEqualTo(superVariable);
    assertThat(taskService.<TypedValue>getVariableTyped(taskInSubProcess.getId(), "subVariable")).isEqualTo(superVariable);

    TypedValue subVariable = Variables.stringValue(null);
    runtimeService.setVariable(taskInSubProcess.getProcessInstanceId(), "subVariable", subVariable);

    // super variable is unchanged
    assertThat(runtimeService.<TypedValue>getVariableTyped(processInstance.getId(), "superVariable")).isEqualTo(superVariable);

    // Completing this task ends the subprocess which leads to a task in the super process
    taskService.complete(taskInSubProcess.getId());

    Task taskAfterSubProcess = taskQuery.singleResult();
    assertThat(taskAfterSubProcess.getName()).isEqualTo("Task in super process");
    assertThat(runtimeService.<TypedValue>getVariableTyped(processInstance.getId(), "superVariable")).isEqualTo(subVariable);
    assertThat(taskService.<TypedValue>getVariableTyped(taskAfterSubProcess.getId(), "superVariable")).isEqualTo(subVariable);

    // Completing this task ends the super process which leads to a task in the super process
    taskService.complete(taskAfterSubProcess.getId());

    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * Test case for handing over process variables to a sub process via the typed
   * api and passing all variables
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessAllDataInputOutputTypedApi.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessWithAllDataInputOutputTypedApi() {

    TypedValue superVariable = Variables.stringValue(null);
    VariableMap vars = Variables.createVariables();
    vars.putValueTyped("superVariable", superVariable);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessDataInputOutput", vars);

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertThat(taskInSubProcess.getName()).isEqualTo("Task in subprocess");
    assertThat(runtimeService.<TypedValue>getVariableTyped(taskInSubProcess.getProcessInstanceId(), "superVariable")).isEqualTo(superVariable);
    assertThat(taskService.<TypedValue>getVariableTyped(taskInSubProcess.getId(), "superVariable")).isEqualTo(superVariable);

    TypedValue subVariable = Variables.stringValue(null);
    runtimeService.setVariable(taskInSubProcess.getProcessInstanceId(), "subVariable", subVariable);

    // Completing this task ends the subprocess which leads to a task in the super process
    taskService.complete(taskInSubProcess.getId());

    Task taskAfterSubProcess = taskQuery.singleResult();
    assertThat(taskAfterSubProcess.getName()).isEqualTo("Task in super process");
    assertThat(runtimeService.<TypedValue>getVariableTyped(processInstance.getId(), "subVariable")).isEqualTo(subVariable);
    assertThat(taskService.<TypedValue>getVariableTyped(taskAfterSubProcess.getId(), "superVariable")).isEqualTo(superVariable);

    // Completing this task ends the super process which leads to a task in the super process
    taskService.complete(taskAfterSubProcess.getId());

    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * Test case for handing over process variables without target attribute set
   */
  @Test
  public void testSubProcessWithDataInputOutputWithoutTarget() {
    String processId = "subProcessDataInputOutputWithoutTarget";

    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(processId)
            .startEvent()
            .callActivity("callActivity")
            .calledElement("simpleSubProcess")
            .userTask()
            .endEvent()
            .done();

    CallActivityBuilder callActivityBuilder = ((CallActivity) modelInstance.getModelElementById("callActivity")).builder();

    // create camunda:in with source but without target
    CamundaIn camundaIn = modelInstance.newInstance(CamundaIn.class);
    camundaIn.setCamundaSource("superVariable");
    callActivityBuilder.addExtensionElement(camundaIn);

    deployAndExpectException(modelInstance);
    // set target
    camundaIn.setCamundaTarget("subVariable");

    // create camunda:in with sourceExpression but without target
    camundaIn = modelInstance.newInstance(CamundaIn.class);
    camundaIn.setCamundaSourceExpression("${x+5}");
    callActivityBuilder.addExtensionElement(camundaIn);

    deployAndExpectException(modelInstance);
    // set target
    camundaIn.setCamundaTarget("subVariable2");

    // create camunda:out with source but without target
    CamundaOut camundaOut = modelInstance.newInstance(CamundaOut.class);
    camundaOut.setCamundaSource("subVariable");
    callActivityBuilder.addExtensionElement(camundaOut);

    deployAndExpectException(modelInstance);
    // set target
    camundaOut.setCamundaTarget("superVariable");

    // create camunda:out with sourceExpression but without target
    camundaOut = modelInstance.newInstance(CamundaOut.class);
    camundaOut.setCamundaSourceExpression("${y+1}");
    callActivityBuilder.addExtensionElement(camundaOut);

    deployAndExpectException(modelInstance);
    // set target
    camundaOut.setCamundaTarget("superVariable2");

    try {
      String deploymentId = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy().getId();
      repositoryService.deleteDeployment(deploymentId, true);
    } catch (ProcessEngineException e) {
      fail("No exception expected");
    }
  }

  /**
   * Test case for handing over a null process variables to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessDataInputOutput.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/dataSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessWithNullDataInput() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("subProcessDataInputOutput").getId();

    // the variable named "subVariable" is not set on process instance
    VariableInstance variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("subVariable")
            .singleResult();
    assertNull(variable);

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("superVariable")
            .singleResult();
    assertNull(variable);

    // the sub process instance is in the task
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Task in subprocess", task.getName());

    // the value of "subVariable" is null
    assertNull(taskService.getVariable(task.getId(), "subVariable"));

    String subProcessInstanceId = task.getProcessInstanceId();
    assertFalse(processInstanceId.equals(subProcessInstanceId));

    // the variable "subVariable" is set on the sub process instance
    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(subProcessInstanceId)
            .variableName("subVariable")
            .singleResult();

    assertNotNull(variable);
    assertNull(variable.getValue());
    assertEquals("subVariable", variable.getName());
  }

  /**
   * Test case for handing over a null process variables to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessDataInputOutputAsExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/dataSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessWithNullDataInputAsExpression() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("superVariable", null);
    String processInstanceId = runtimeService.startProcessInstanceByKey("subProcessDataInputOutput", params).getId();

    // the variable named "subVariable" is not set on process instance
    VariableInstance variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("subVariable")
            .singleResult();
    assertNull(variable);

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("superVariable")
            .singleResult();
    assertNotNull(variable);
    assertNull(variable.getValue());

    // the sub process instance is in the task
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Task in subprocess", task.getName());

    // the value of "subVariable" is null
    assertNull(taskService.getVariable(task.getId(), "subVariable"));

    String subProcessInstanceId = task.getProcessInstanceId();
    assertFalse(processInstanceId.equals(subProcessInstanceId));

    // the variable "subVariable" is set on the sub process instance
    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(subProcessInstanceId)
            .variableName("subVariable")
            .singleResult();

    assertNotNull(variable);
    assertNull(variable.getValue());
    assertEquals("subVariable", variable.getName());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessDataInputOutput.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/dataSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessWithNullDataOutput() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("subProcessDataInputOutput").getId();

    // the variable named "subVariable" is not set on process instance
    VariableInstance variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("subVariable")
            .singleResult();
    assertNull(variable);

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("superVariable")
            .singleResult();
    assertNull(variable);

    // the sub process instance is in the task
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Task in subprocess", task.getName());

    taskService.complete(task.getId());

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("subVariable")
            .singleResult();
    assertNull(variable);

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("superVariable")
            .singleResult();
    assertNotNull(variable);
    assertNull(variable.getValue());

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("hisLocalVariable")
            .singleResult();
    assertNotNull(variable);
    assertNull(variable.getValue());

  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessDataInputOutputAsExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/dataSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessWithNullDataOutputAsExpression() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("superVariable", null);
    String processInstanceId = runtimeService.startProcessInstanceByKey("subProcessDataInputOutput", params).getId();

    // the variable named "subVariable" is not set on process instance
    VariableInstance variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("subVariable")
            .singleResult();
    assertNull(variable);

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("superVariable")
            .singleResult();
    assertNotNull(variable);
    assertNull(variable.getValue());

    // the sub process instance is in the task
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Task in subprocess", task.getName());

    VariableMap variables = Variables.createVariables().putValue("myLocalVariable", null);
    taskService.complete(task.getId(), variables);

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("subVariable")
            .singleResult();
    assertNull(variable);

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("superVariable")
            .singleResult();
    assertNotNull(variable);
    assertNull(variable.getValue());

    variable = runtimeService
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName("hisLocalVariable")
            .singleResult();
    assertNotNull(variable);
    assertNull(variable.getValue());

  }

  private void deployAndExpectException(BpmnModelInstance modelInstance) {
    String deploymentId = null;
    try {
      deploymentId = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy().getId();
      fail("Exception expected");
    } catch (ParseException e) {
       testRule.assertTextPresent("Missing attribute 'target'", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("callActivity");
    } finally {
      if (deploymentId != null) {
        repositoryService.deleteDeployment(deploymentId, true);
      }
    }
  }

  /**
   * Test case for handing over process variables to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testTwoSubProcesses.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testTwoSubProcesses() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callTwoSubProcesses");

    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(3, instanceList.size());

    List<Task> taskList = taskService.createTaskQuery().list();
    assertNotNull(taskList);
    assertEquals(2, taskList.size());

    runtimeService.deleteProcessInstance(processInstance.getId(), "Test cascading");

    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(0, instanceList.size());

    taskList = taskService.createTaskQuery().list();
    assertNotNull(taskList);
    assertEquals(0, taskList.size());
  }

  /**
   * Test case for handing all over process variables to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessAllDataInputOutput.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessAllDataInputOutput() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("superVariable", "Hello from the super process.");
    vars.put("testVariable", "Only a test.");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessAllDataInputOutput", vars);

    // one task in the super process should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());
    assertEquals("Hello from the super process.", runtimeService.getVariable(taskBeforeSubProcess.getProcessInstanceId(), "superVariable"));
    assertEquals("Hello from the super process.", taskService.getVariable(taskBeforeSubProcess.getId(), "superVariable"));
    assertEquals("Only a test.", runtimeService.getVariable(taskBeforeSubProcess.getProcessInstanceId(), "testVariable"));
    assertEquals("Only a test.", taskService.getVariable(taskBeforeSubProcess.getId(), "testVariable"));

    taskService.complete(taskBeforeSubProcess.getId());

    // one task in sub process should be active after starting sub process instance
    taskQuery = taskService.createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());
    assertEquals("Hello from the super process.", runtimeService.getVariable(taskInSubProcess.getProcessInstanceId(), "superVariable"));
    assertEquals("Hello from the super process.", taskService.getVariable(taskInSubProcess.getId(), "superVariable"));
    assertEquals("Only a test.", runtimeService.getVariable(taskInSubProcess.getProcessInstanceId(), "testVariable"));
    assertEquals("Only a test.", taskService.getVariable(taskInSubProcess.getId(), "testVariable"));

    // changed variables in sub process
    runtimeService.setVariable(taskInSubProcess.getProcessInstanceId(), "superVariable", "Hello from sub process.");
    runtimeService.setVariable(taskInSubProcess.getProcessInstanceId(), "testVariable", "Variable changed in sub process.");

    taskService.complete(taskInSubProcess.getId());

    // task after sub process in super process
    taskQuery = taskService.createTaskQuery();
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // variables are changed after finished sub process
    assertEquals("Hello from sub process.", runtimeService.getVariable(processInstance.getId(), "superVariable"));
    assertEquals("Variable changed in sub process.", runtimeService.getVariable(processInstance.getId(), "testVariable"));

    taskService.complete(taskAfterSubProcess.getId());

    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * Test case for handing all over process variables to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessAllDataInputOutputWithAdditionalInputMapping.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessAllDataInputOutputWithAdditionalInputMapping() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("superVariable", "Hello from the super process.");
    vars.put("testVariable", "Only a test.");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessAllDataInputOutput", vars);

    // one task in the super process should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());
    assertEquals("Hello from the super process.", runtimeService.getVariable(taskBeforeSubProcess.getProcessInstanceId(), "superVariable"));
    assertEquals("Hello from the super process.", taskService.getVariable(taskBeforeSubProcess.getId(), "superVariable"));
    assertEquals("Only a test.", runtimeService.getVariable(taskBeforeSubProcess.getProcessInstanceId(), "testVariable"));
    assertEquals("Only a test.", taskService.getVariable(taskBeforeSubProcess.getId(), "testVariable"));

    taskService.complete(taskBeforeSubProcess.getId());

    // one task in sub process should be active after starting sub process instance
    taskQuery = taskService.createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());
    assertEquals("Hello from the super process.", runtimeService.getVariable(taskInSubProcess.getProcessInstanceId(), "superVariable"));
    assertEquals("Hello from the super process.", runtimeService.getVariable(taskInSubProcess.getProcessInstanceId(), "subVariable"));
    assertEquals("Hello from the super process.", taskService.getVariable(taskInSubProcess.getId(), "superVariable"));
    assertEquals("Only a test.", runtimeService.getVariable(taskInSubProcess.getProcessInstanceId(), "testVariable"));
    assertEquals("Only a test.", taskService.getVariable(taskInSubProcess.getId(), "testVariable"));

    // changed variables in sub process
    runtimeService.setVariable(taskInSubProcess.getProcessInstanceId(), "superVariable", "Hello from sub process.");
    runtimeService.setVariable(taskInSubProcess.getProcessInstanceId(), "testVariable", "Variable changed in sub process.");

    taskService.complete(taskInSubProcess.getId());

    // task after sub process in super process
    taskQuery = taskService.createTaskQuery();
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // variables are changed after finished sub process
    assertEquals("Hello from sub process.", runtimeService.getVariable(processInstance.getId(), "superVariable"));
    assertEquals("Variable changed in sub process.", runtimeService.getVariable(processInstance.getId(), "testVariable"));

    taskService.complete(taskAfterSubProcess.getId());

    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * This testcase verifies that <camunda:out variables="all" /> works also in
   * case super process has no variables
   *
   * https://app.camunda.com/jira/browse/CAM-1617
   *
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessAllDataInputOutput.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessAllDataOutput() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessAllDataInputOutput");

    // one task in the super process should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

    taskService.complete(taskBeforeSubProcess.getId());

    // one task in sub process should be active after starting sub process instance
    taskQuery = taskService.createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());

    // add variables to sub process
    runtimeService.setVariable(taskInSubProcess.getProcessInstanceId(), "superVariable", "Hello from sub process.");
    runtimeService.setVariable(taskInSubProcess.getProcessInstanceId(), "testVariable", "Variable changed in sub process.");

    taskService.complete(taskInSubProcess.getId());

    // task after sub process in super process
    taskQuery = taskService.createTaskQuery();
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // variables are copied to super process instance after sub process instance finishes
    assertEquals("Hello from sub process.", runtimeService.getVariable(processInstance.getId(), "superVariable"));
    assertEquals("Variable changed in sub process.", runtimeService.getVariable(processInstance.getId(), "testVariable"));

    taskService.complete(taskAfterSubProcess.getId());

    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessLocalInputAllVariables.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessLocalInputAllVariables() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessLocalInputAllVariables");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when setting a variable in a process instance
    runtimeService.setVariable(processInstance.getId(), "callingProcessVar1", "val1");

    // and executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then only the local variable specified in the io mapping is passed to the called instance
    ProcessInstance calledInstance = runtimeService.createProcessInstanceQuery()
            .superProcessInstanceId(processInstance.getId())
            .singleResult();

    Map<String, Object> calledInstanceVariables = runtimeService.getVariables(calledInstance.getId());
    assertEquals(1, calledInstanceVariables.size());
    assertEquals("val2", calledInstanceVariables.get("inputParameter"));

    // when setting a variable in the called process instance
    runtimeService.setVariable(calledInstance.getId(), "calledProcessVar1", 42L);

    // and completing it
    Task calledProcessInstanceTask = taskService.createTaskQuery().singleResult();
    taskService.complete(calledProcessInstanceTask.getId());

    // then the call activity output variable has been mapped to the process instance execution
    // and the output mapping variable as well
    Map<String, Object> callingInstanceVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(3, callingInstanceVariables.size());
    assertEquals("val1", callingInstanceVariables.get("callingProcessVar1"));
    assertEquals(42L, callingInstanceVariables.get("calledProcessVar1"));
    assertEquals(43L, callingInstanceVariables.get("outputParameter"));
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessLocalInputSingleVariable.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessLocalInputSingleVariable() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessLocalInputSingleVariable");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when setting a variable in a process instance
    runtimeService.setVariable(processInstance.getId(), "callingProcessVar1", "val1");

    // and executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then the local variable specified in the io mapping is passed to the called instance
    ProcessInstance calledInstance = runtimeService.createProcessInstanceQuery()
            .superProcessInstanceId(processInstance.getId())
            .singleResult();

    Map<String, Object> calledInstanceVariables = runtimeService.getVariables(calledInstance.getId());
    assertEquals(1, calledInstanceVariables.size());
    assertEquals("val2", calledInstanceVariables.get("mappedInputParameter"));

    // when setting a variable in the called process instance
    runtimeService.setVariable(calledInstance.getId(), "calledProcessVar1", 42L);

    // and completing it
    Task calledProcessInstanceTask = taskService.createTaskQuery().singleResult();
    taskService.complete(calledProcessInstanceTask.getId());

    // then the call activity output variable has been mapped to the process instance execution
    // and the output mapping variable as well
    Map<String, Object> callingInstanceVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(4, callingInstanceVariables.size());
    assertEquals("val1", callingInstanceVariables.get("callingProcessVar1"));
    assertEquals("val2", callingInstanceVariables.get("mappedInputParameter"));
    assertEquals(42L, callingInstanceVariables.get("calledProcessVar1"));
    assertEquals(43L, callingInstanceVariables.get("outputParameter"));
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessLocalInputSingleVariableExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessLocalInputSingleVariableExpression() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessLocalInputSingleVariableExpression");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then the local input parameter can be resolved because its source expression variable
    // is defined in the call activity's input mapping
    ProcessInstance calledInstance = runtimeService.createProcessInstanceQuery()
            .superProcessInstanceId(processInstance.getId())
            .singleResult();

    Map<String, Object> calledInstanceVariables = runtimeService.getVariables(calledInstance.getId());
    assertEquals(1, calledInstanceVariables.size());
    assertEquals(43L, calledInstanceVariables.get("mappedInputParameter"));

    // and completing it
    Task callActivityTask = taskService.createTaskQuery().singleResult();
    taskService.complete(callActivityTask.getId());

    // and executing a call activity in parameter where the source variable is not mapped by an activity
    // input parameter fails
    Task beforeSecondCallActivityTask = taskService.createTaskQuery().singleResult();
    runtimeService.setVariable(processInstance.getId(), "globalVariable", "42");

    try {
      taskService.complete(beforeSecondCallActivityTask.getId());
      fail("expected exception");
    } catch (ProcessEngineException e) {
       testRule.assertTextPresent("Cannot resolve identifier 'globalVariable'", e.getMessage());
    }
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessLocalOutputAllVariables.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessLocalOutputAllVariables() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessLocalOutputAllVariables");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when setting a variable in a process instance
    runtimeService.setVariable(processInstance.getId(), "callingProcessVar1", "val1");

    // and executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then all variables have been mapped into the called instance
    ProcessInstance calledInstance = runtimeService.createProcessInstanceQuery()
            .superProcessInstanceId(processInstance.getId())
            .singleResult();

    Map<String, Object> calledInstanceVariables = runtimeService.getVariables(calledInstance.getId());
    assertEquals(2, calledInstanceVariables.size());
    assertEquals("val1", calledInstanceVariables.get("callingProcessVar1"));
    assertEquals("val2", calledInstanceVariables.get("inputParameter"));

    // when setting a variable in the called process instance
    runtimeService.setVariable(calledInstance.getId(), "calledProcessVar1", 42L);

    // and completing it
    Task calledProcessInstanceTask = taskService.createTaskQuery().singleResult();
    taskService.complete(calledProcessInstanceTask.getId());

    // then only the output mapping variable has been mapped into the calling process instance
    Map<String, Object> callingInstanceVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(2, callingInstanceVariables.size());
    assertEquals("val1", callingInstanceVariables.get("callingProcessVar1"));
    assertEquals(43L, callingInstanceVariables.get("outputParameter"));
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessLocalOutputSingleVariable.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessLocalOutputSingleVariable() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessLocalOutputSingleVariable");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when setting a variable in a process instance
    runtimeService.setVariable(processInstance.getId(), "callingProcessVar1", "val1");

    // and executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then all variables have been mapped into the called instance
    ProcessInstance calledInstance = runtimeService.createProcessInstanceQuery()
            .superProcessInstanceId(processInstance.getId())
            .singleResult();

    Map<String, Object> calledInstanceVariables = runtimeService.getVariables(calledInstance.getId());
    assertEquals(2, calledInstanceVariables.size());
    assertEquals("val1", calledInstanceVariables.get("callingProcessVar1"));
    assertEquals("val2", calledInstanceVariables.get("inputParameter"));

    // when setting a variable in the called process instance
    runtimeService.setVariable(calledInstance.getId(), "calledProcessVar1", 42L);

    // and completing it
    Task calledProcessInstanceTask = taskService.createTaskQuery().singleResult();
    taskService.complete(calledProcessInstanceTask.getId());

    // then only the output mapping variable has been mapped into the calling process instance
    Map<String, Object> callingInstanceVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(2, callingInstanceVariables.size());
    assertEquals("val1", callingInstanceVariables.get("callingProcessVar1"));
    assertEquals(43L, callingInstanceVariables.get("outputParameter"));
  }

  /**
   * Test case for handing businessKey to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessBusinessKeyInput.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSubProcessBusinessKeyInput() {
    String businessKey = "myBusinessKey";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessBusinessKeyInput", businessKey);

    // one task in the super process should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());
    assertEquals("myBusinessKey", processInstance.getBusinessKey());

    taskService.complete(taskBeforeSubProcess.getId());

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      // called process started so businesskey should be written in history
      HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
      assertEquals(businessKey, hpi.getBusinessKey());

      assertEquals(2, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).list().size());
    }

    // one task in sub process should be active after starting sub process instance
    taskQuery = taskService.createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().processInstanceId(taskInSubProcess.getProcessInstanceId()).singleResult();
    assertEquals("myBusinessKey", subProcessInstance.getBusinessKey());

    taskService.complete(taskInSubProcess.getId());

    // task after sub process in super process
    taskQuery = taskService.createTaskQuery();
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    taskService.complete(taskAfterSubProcess.getId());

    testRule.assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());

    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).finished().singleResult();
      assertEquals(businessKey, hpi.getBusinessKey());

      assertEquals(2, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).finished().list().size());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithHashExpressions.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testCallSimpleSubProcessWithHashExpressions() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    // one task in the subprocess should be active after starting the process
    // instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

    // Completing the task continues the process which leads to calling the
    // subprocess. The sub process we want to call is passed in as a variable
    // into this task
    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess");
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testInterruptingEventSubProcessEventSubscriptions.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/interruptingEventSubProcessEventSubscriptions.bpmn20.xml"})
  @Test
  public void testInterruptingMessageEventSubProcessEventSubscriptionsInsideCallActivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callInterruptingEventSubProcess");

    // one task in the call activity subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskInsideCallActivity = taskQuery.singleResult();
    assertEquals("taskBeforeInterruptingEventSubprocess", taskInsideCallActivity.getTaskDefinitionKey());

    // we should have no event subscriptions for the parent process
    assertEquals(0, runtimeService.createEventSubscriptionQuery().processInstanceId(processInstance.getId()).count());
    // we should have two event subscriptions for the called process instance, one for message and one for signal
    String calledProcessInstanceId = taskInsideCallActivity.getProcessInstanceId();
    EventSubscriptionQuery eventSubscriptionQuery = runtimeService.createEventSubscriptionQuery().processInstanceId(calledProcessInstanceId);
    List<EventSubscription> subscriptions = eventSubscriptionQuery.list();
    assertEquals(2, subscriptions.size());

    // start the message interrupting event sub process
    runtimeService.correlateMessage("newMessage");
    Task taskAfterMessageStartEvent = taskQuery.processInstanceId(calledProcessInstanceId).singleResult();
    assertEquals("taskAfterMessageStartEvent", taskAfterMessageStartEvent.getTaskDefinitionKey());

    // no subscriptions left
    assertEquals(0, eventSubscriptionQuery.count());

    // Complete the task inside the called process instance
    taskService.complete(taskAfterMessageStartEvent.getId());

    testRule.assertProcessEnded(calledProcessInstanceId);
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testInterruptingEventSubProcessEventSubscriptions.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/interruptingEventSubProcessEventSubscriptions.bpmn20.xml"})
  @Test
  public void testInterruptingSignalEventSubProcessEventSubscriptionsInsideCallActivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callInterruptingEventSubProcess");

    // one task in the call activity subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskInsideCallActivity = taskQuery.singleResult();
    assertEquals("taskBeforeInterruptingEventSubprocess", taskInsideCallActivity.getTaskDefinitionKey());

    // we should have no event subscriptions for the parent process
    assertEquals(0, runtimeService.createEventSubscriptionQuery().processInstanceId(processInstance.getId()).count());
    // we should have two event subscriptions for the called process instance, one for message and one for signal
    String calledProcessInstanceId = taskInsideCallActivity.getProcessInstanceId();
    EventSubscriptionQuery eventSubscriptionQuery = runtimeService.createEventSubscriptionQuery().processInstanceId(calledProcessInstanceId);
    List<EventSubscription> subscriptions = eventSubscriptionQuery.list();
    assertEquals(2, subscriptions.size());

    // start the signal interrupting event sub process
    runtimeService.signalEventReceived("newSignal");
    Task taskAfterSignalStartEvent = taskQuery.processInstanceId(calledProcessInstanceId).singleResult();
    assertEquals("taskAfterSignalStartEvent", taskAfterSignalStartEvent.getTaskDefinitionKey());

    // no subscriptions left
    assertEquals(0, eventSubscriptionQuery.count());

    // Complete the task inside the called process instance
    taskService.complete(taskAfterSignalStartEvent.getId());

    testRule.assertProcessEnded(calledProcessInstanceId);
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testLiteralSourceExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"
  })
  @Test
  public void testInputParameterLiteralSourceExpression() {
    runtimeService.startProcessInstanceByKey("process");

    String subInstanceId = runtimeService
            .createProcessInstanceQuery()
            .processDefinitionKey("simpleSubProcess")
            .singleResult()
            .getId();

    Object variable = runtimeService.getVariable(subInstanceId, "inLiteralVariable");
    assertEquals("inLiteralValue", variable);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testLiteralSourceExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"
  })
  @Test
  public void testOutputParameterLiteralSourceExpression() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String taskId = taskService
            .createTaskQuery()
            .singleResult()
            .getId();
    taskService.complete(taskId);

    Object variable = runtimeService.getVariable(processInstanceId, "outLiteralVariable");
    assertEquals("outLiteralValue", variable);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessDataOutputOnError.bpmn",
    "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithError.bpmn"
  })
  @Test
  public void testSubProcessDataOutputOnError() {
    String variableName = "subVariable";
    Object variableValue = "Hello from Subprocess";

    runtimeService.startProcessInstanceByKey("Process_1");
    //first task is the one in the subprocess
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("SubTask");

    runtimeService.setVariable(task.getProcessInstanceId(), variableName, variableValue);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task after error");

    Object variable = runtimeService.getVariable(task.getProcessInstanceId(), variableName);
    assertThat(variable).isNotNull();
    assertThat(variable).isEqualTo(variableValue);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessDataOutputOnThrownError.bpmn",
    "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithThrownError.bpmn"
  })
  @Test
  public void testSubProcessDataOutputOnThrownError() {
    String variableName = "subVariable";
    Object variableValue = "Hello from Subprocess";

    runtimeService.startProcessInstanceByKey("Process_1");
    //first task is the one in the subprocess
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("SubTask");

    runtimeService.setVariable(task.getProcessInstanceId(), variableName, variableValue);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task after error");

    Object variable = runtimeService.getVariable(task.getProcessInstanceId(), variableName);
    assertThat(variable).isNotNull();
    assertThat(variable).isEqualTo(variableValue);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testTwoSubProcessesDataOutputOnError.bpmn",
    "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessCallErrorSubProcess.bpmn",
    "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithError.bpmn"
  })
  @Test
  public void testTwoSubProcessesDataOutputOnError() {
    String variableName = "subVariable";
    Object variableValue = "Hello from Subprocess";

    runtimeService.startProcessInstanceByKey("Process_1");
    //first task is the one in the subprocess
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("SubTask");

    runtimeService.setVariable(task.getProcessInstanceId(), variableName, variableValue);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task after error");

    Object variable = runtimeService.getVariable(task.getProcessInstanceId(), variableName);
    //both processes have and out mapping for all, so we want the variable to be propagated to the process with the event handler
    assertThat(variable).isNotNull();
    assertThat(variable).isEqualTo(variableValue);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testTwoSubProcessesLimitedDataOutputOnError.bpmn",
    "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessCallErrorSubProcessWithLimitedOutMapping.bpmn",
    "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithError.bpmn"
  })
  @Test
  public void testTwoSubProcessesLimitedDataOutputOnError() {
    String variableName1 = "subSubVariable1";
    String variableName2 = "subSubVariable2";
    String variableName3 = "subVariable";
    Object variableValue = "Hello from Subsubprocess";
    Object variableValue2 = "Hello from Subprocess";

    runtimeService.startProcessInstanceByKey("Process_1");

    //task in first subprocess (second process in general)
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task");
    runtimeService.setVariable(task.getProcessInstanceId(), variableName3, variableValue2);
    taskService.complete(task.getId());
    //task in the second subprocess (third process in general)
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("SubTask");
    runtimeService.setVariable(task.getProcessInstanceId(), variableName1, "foo");
    runtimeService.setVariable(task.getProcessInstanceId(), variableName2, variableValue);
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task after error");

    //the two subprocess don't pass all their variables, so we check that not all were passed
    Object variable = runtimeService.getVariable(task.getProcessInstanceId(), variableName2);
    assertThat(variable).isNotNull();
    assertThat(variable).isEqualTo(variableValue);
    variable = runtimeService.getVariable(task.getProcessInstanceId(), variableName3);
    assertThat(variable).isNotNull();
    assertThat(variable).isEqualTo(variableValue2);
    variable = runtimeService.getVariable(task.getProcessInstanceId(), variableName1);
    assertThat(variable).isNull();
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivityAdvancedTest.testCallProcessByVersionAsExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
  })
  @Test
  public void testCallCaseByVersionAsExpression() {
    // given

    String bpmnResourceName = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";

    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String processDefinitionIdInSecondDeployment = repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionKey("oneTaskProcess")
            .deploymentId(secondDeploymentId)
            .singleResult()
            .getId();

    VariableMap variables = Variables.createVariables().putValue("myVersion", 2);

    // when
    runtimeService.startProcessInstanceByKey("process", variables).getId();

    // then
    ProcessInstance subInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").singleResult();
    assertNotNull(subInstance);

    assertEquals(processDefinitionIdInSecondDeployment, subInstance.getProcessDefinitionId());

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivityAdvancedTest.testCallProcessByVersionAsDelegateExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
  })
  @Test
  public void testCallCaseByVersionAsDelegateExpression() {
    processEngineConfiguration.getBeans().put("myDelegate", new MyVersionDelegate());

    // given
    String bpmnResourceName = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";

    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String processDefinitionIdInSecondDeployment = repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionKey("oneTaskProcess")
            .deploymentId(secondDeploymentId)
            .singleResult()
            .getId();

    VariableMap variables = Variables.createVariables().putValue("myVersion", 2);

    // when
    runtimeService.startProcessInstanceByKey("process", variables).getId();

    // then
    ProcessInstance subInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").singleResult();
    assertNotNull(subInstance);

    assertEquals(processDefinitionIdInSecondDeployment, subInstance.getProcessDefinitionId());

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithVersionTag.bpmn20.xml" })
  @Test
  public void testCallProcessByVersionTag() {
    // given
    BpmnModelInstance modelInstance = getModelWithCallActivityVersionTagBinding("ver_tag_1");

   testRule.deploy(modelInstance);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // then
    ProcessInstance subInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess").superProcessInstanceId(processInstance.getId()).singleResult();
    assertNotNull(subInstance);

    // clean up
    cleanupDeployments();
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithVersionTag.bpmn20.xml" })
  @Test
  public void testCallProcessByVersionTagAsExpression() {
    // given
    BpmnModelInstance modelInstance = getModelWithCallActivityVersionTagBinding("${versionTagExpr}");

   testRule.deploy(modelInstance);

    // when
    VariableMap variables = Variables.createVariables().putValue("versionTagExpr", "ver_tag_1");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    // then
    ProcessInstance subInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess").superProcessInstanceId(processInstance.getId()).singleResult();
    assertNotNull(subInstance);

    // clean up
    cleanupDeployments();
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithVersionTag.bpmn20.xml" })
  @Test
  public void testCallProcessByVersionTagAsDelegateExpression() {
    // given
    processEngineConfiguration.getBeans().put("myDelegate", new MyVersionDelegate());
    BpmnModelInstance modelInstance = getModelWithCallActivityVersionTagBinding("${myDelegate.getVersionTag()}");

   testRule.deploy(modelInstance);

    // when
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // then
    ProcessInstance subInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess").superProcessInstanceId(processInstance.getId()).singleResult();
    assertNotNull(subInstance);

    // clean up
    cleanupDeployments();
  }

  @Test
  public void testCallProcessWithoutVersionTag() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .callActivity("callActivity")
        .calledElement("subProcess")
        .camundaCalledElementBinding("versionTag")
        .endEvent()
        .done();

    try {
      // when
     testRule.deploy(modelInstance);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // then
      assertTrue(e.getMessage().contains("Could not parse BPMN process."));
      assertTrue(e.getMessage().contains("Missing attribute 'calledElementVersionTag' when 'calledElementBinding' has value 'versionTag'"));
    }
  }

  @Test
  public void testCallProcessByVersionTagNoneSubprocess() {
    // given
    BpmnModelInstance modelInstance = getModelWithCallActivityVersionTagBinding("ver_tag_1");

   testRule.deploy(modelInstance);

    try {
      // when
      runtimeService.startProcessInstanceByKey("process");
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // then
      assertTrue(e.getMessage().contains("no processes deployed with key = 'subProcess', versionTag = 'ver_tag_1' and tenant-id = 'null': processDefinition is null"));
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithVersionTag.bpmn20.xml" })
  @Test
  public void testCallProcessByVersionTagTwoSubprocesses() {
    // given
    BpmnModelInstance modelInstance = getModelWithCallActivityVersionTagBinding("ver_tag_1");

   testRule.deploy(modelInstance);
   testRule.deploy("org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithVersionTag.bpmn20.xml");

    try {
      // when
      runtimeService.startProcessInstanceByKey("process");
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // then
      assertTrue(e.getMessage().contains("There are '2' results for a process definition with key 'subProcess', versionTag 'ver_tag_1' and tenant-id '{}'."));
    }

    // clean up
    cleanupDeployments();
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/orderProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/checkCreditProcess.bpmn20.xml"
  })
  @Test
  public void testOrderProcessWithCallActivity() {
    // After the process has started, the 'verify credit history' task should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();
    assertEquals("Verify credit history", verifyCreditTask.getName());

    // Verify with Query API
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(pi.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId());

    // Completing the task with approval, will end the subprocess and continue the original process
    taskService.complete(verifyCreditTask.getId(), CollectionUtil.singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertEquals("Prepare and Ship", prepareAndShipTask.getName());
  }

  /**
   * Test case for checking deletion of process instancess in call activity subprocesses
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"
  })
  @Test
  public void testDeleteProcessInstanceInCallActivity() {
    // given
    runtimeService.startProcessInstanceByKey("callSimpleSubProcess");


    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();

    // Completing the task continues the process which leads to calling the subprocess
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();


    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(2, instanceList.size());


    // when
    // Delete the ProcessInstance in the sub process
    runtimeService.deleteProcessInstance(taskInSubProcess.getProcessInstanceId(), "Test upstream deletion");

    // then

    // How many process Instances
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(0, instanceList.size());
  }

  /**
   * Test case for checking deletion of process instances in call activity subprocesses
   *
   * Checks that deletion of process Instance will resepct other process instances in the scope
   * and stop its upward deletion propagation will stop at this point
   *
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testTwoSubProcesses.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  @Test
  public void testSingleDeletionWithTwoSubProcesses() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callTwoSubProcesses");

    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(3, instanceList.size());

    List<Task> taskList = taskService.createTaskQuery().list();
    assertNotNull(taskList);
    assertEquals(2, taskList.size());

    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getProcessInstanceId());
    assertNotNull(activeActivityIds);
    assertEquals(2, activeActivityIds.size());

    // when
    runtimeService.deleteProcessInstance(taskList.get(0).getProcessInstanceId(), "Test upstream deletion");

    // then
    // How many process Instances
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(2, instanceList.size());

    // How man call activities
    activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getProcessInstanceId());
    assertNotNull(activeActivityIds);
    assertEquals(1, activeActivityIds.size());
  }

  /**
   * Test case for checking deletion of process instances in nested call activity subprocesses
   *
   * Checking that nested call activities will propagate upward over multiple nested levels
   *
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testNestedCallActivity.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"
  })
  @Test
  public void testDeleteMultilevelProcessInstanceInCallActivity() {
    // given
    runtimeService.startProcessInstanceByKey("nestedCallActivity");

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();

    // Completing the task continues the process which leads to calling the subprocess
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();

    // Completing the task continues the sub process which leads to calling the deeper subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskInNestedSubProcess = taskQuery.singleResult();

    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(3, instanceList.size());

    // when
    // Delete the ProcessInstance in the sub process
    runtimeService.deleteProcessInstance(taskInNestedSubProcess.getProcessInstanceId(), "Test cascading upstream deletion");


    // then
    // How many process Instances
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(0, instanceList.size());

  }

  /**
   * Test case for checking deletion of process instances in nested call activity subprocesses
   *
   * The test defines a process waiting on three nested call activities to complete
   *
   * At each nested level there is only one process instance, which is waiting on the next level to complete
   *
   * When we delete the process instance of the most inner call activity sub process the expected behaviour is that
   * the delete will propagate upward and delete all process instances.
   *
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testNestedCallActivity.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testDoubleNestedCallActivity.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"
  })
  @Test
  public void testDeleteDoubleNestedProcessInstanceInCallActivity() {
    // given
    runtimeService.startProcessInstanceByKey("doubleNestedCallActivity");

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();

    // Completing the task continues the process which leads to calling the subprocess
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();

    // Completing the task continues the sub process which leads to calling the deeper subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskInNestedSubProcess = taskQuery.singleResult();


    // Completing the task continues the sub process which leads to calling the deeper subprocess
    taskService.complete(taskInNestedSubProcess.getId());
    Task taskInDoubleNestedSubProcess = taskQuery.singleResult();


    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(4, instanceList.size());

    // when
    // Delete the ProcessInstance in the sub process
    runtimeService.deleteProcessInstance(taskInDoubleNestedSubProcess.getProcessInstanceId(), "Test cascading upstream deletion");


    // then
    // How many process Instances
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(0, instanceList.size());

  }

  @Test
  public void testTransientVariableInputMapping() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("simpleSubProcess")
      .startEvent()
      .serviceTask()
      .camundaClass(AssertTransientVariableDelegate.class)
      .userTask()
      .endEvent()
      .done();

   testRule.deploy("org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessAllDataInputOutputTypedApi.bpmn20.xml");
   testRule.deploy(modelInstance);

    VariableMap variables = Variables.createVariables().putValue("var", Variables.stringValue("value", true));

    // when
    runtimeService.startProcessInstanceByKey("subProcessDataInputOutput", variables);

    // then
    // presence of transient variable is asserted in delegate

    // and
    long numVariables = runtimeService.createVariableInstanceQuery().count();
    assertThat(numVariables).isEqualTo(0);
  }


  @Test
  public void testTransientVariableOutputMapping() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("superProcess")
      .startEvent()
      .callActivity()
      .calledElement("oneTaskProcess")
      .camundaOut("var", "var")
      .serviceTask()
      .camundaClass(AssertTransientVariableDelegate.class)
      .userTask()
      .endEvent()
      .done();

   testRule.deploy(modelInstance);
   testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml");

    runtimeService.startProcessInstanceByKey("superProcess");
    Task task = taskService.createTaskQuery().singleResult();


    // when
    VariableMap variables = Variables.createVariables().putValue("var", Variables.stringValue("value", true));
    taskService.complete(task.getId(), variables);

    // then
    // presence of transient variable was asserted in delegate

    long numVariables = runtimeService.createVariableInstanceQuery().count();
    assertThat(numVariables).isEqualTo(0);
  }


  public static class AssertTransientVariableDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      TypedValue typedValue = execution.getVariableTyped("var");
      assertThat(typedValue).isNotNull();
      assertThat(typedValue.getType()).isEqualTo(ValueType.STRING);
      assertThat(typedValue.isTransient()).isTrue();
      assertThat(typedValue.getValue()).isEqualTo("value");
    }

  }

  protected BpmnModelInstance getModelWithCallActivityVersionTagBinding(String versionTag) {
    return Bpmn.createExecutableProcess("process")
        .startEvent()
        .callActivity("callActivity")
        .calledElement("subProcess")
        .camundaCalledElementBinding("versionTag")
        .camundaCalledElementVersionTag(versionTag)
        .endEvent()
        .done();
  }

  protected void cleanupDeployments() {
    List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
    for (org.camunda.bpm.engine.repository.Deployment currentDeployment : deployments) {
      repositoryService.deleteDeployment(currentDeployment.getId(), true);
    }
  }
}
