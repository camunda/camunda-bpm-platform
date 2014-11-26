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

package org.camunda.bpm.engine.test.bpmn.callactivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.CallActivityBuilder;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOut;

/**
 * @author Joram Barrez
 * @author Nils Preusker
 * @author Bernd Ruecker
 * @author Falko Menge
 */
public class CallActivityAdvancedTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"
  })
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
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithExpressions.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
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
    assertProcessEnded(processInstance.getId());
  }

  /**
   * Test case for a possible tricky case: reaching the end event
   * of the subprocess leads to an end event in the super process instance.
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessEndsSuperProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testSubProcessEndsSuperProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessEndsSuperProcess");

    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskBeforeSubProcess.getName());

    // Completing this task ends the subprocess which leads to the end of the whole process instance
    taskService.complete(taskBeforeSubProcess.getId());
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallParallelSubProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleParallelSubProcess.bpmn20.xml"})
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

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSequentialSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithExpressions.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess2.bpmn20.xml"})
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
      assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testTimerOnCallActivity.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
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
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
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

    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * Test case for handing over process variables without target attribute set
   */
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
    }
    catch (ProcessEngineException e) {
      fail("No exception expected");
    }
  }

  /**
   * Test case for handing over a null process variables to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessDataInputOutput.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/dataSubProcess.bpmn20.xml" })
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
    "org/camunda/bpm/engine/test/bpmn/callactivity/dataSubProcess.bpmn20.xml" })
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
      "org/camunda/bpm/engine/test/bpmn/callactivity/dataSubProcess.bpmn20.xml" })
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
      "org/camunda/bpm/engine/test/bpmn/callactivity/dataSubProcess.bpmn20.xml" })
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

  private void deployAndExpectException(BpmnModelInstance modelInstance) {
    String deploymentId = null;
    try {
      deploymentId = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy().getId();
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      assertTextPresent("Missing attribute 'target'", e.getMessage());
    }
    finally {
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
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
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
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
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

    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * Test case for handing all over process variables to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessAllDataInputOutputWithAdditionalInputMapping.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
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

    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * This testcase verifies that <camunda:out variables="all" /> works also in case super process has no variables
   *
   * https://app.camunda.com/jira/browse/CAM-1617
   *
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessAllDataInputOutput.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
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

    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }

  /**
   * Test case for handing businessKey to a sub process
   */
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testSubProcessBusinessKeyInput.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testSubProcessBusinessKeyInput() {
    String businessKey = "myBusinessKey";
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessBusinessKeyInput", businessKey);

    // one task in the super process should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());
    assertEquals("myBusinessKey", processInstance.getBusinessKey());

    taskService.complete(taskBeforeSubProcess.getId());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
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

    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());

    if(processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).finished().singleResult();
      assertEquals(businessKey, hpi.getBusinessKey());

      assertEquals(2, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).finished().list().size());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithHashExpressions.bpmn20.xml",
  "org/camunda/bpm/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
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
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testInterruptingEventSubProcessEventSubscriptions.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/callactivity/interruptingEventSubProcessEventSubscriptions.bpmn20.xml" })
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

    assertProcessEnded(calledProcessInstanceId);
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/callactivity/CallActivity.testInterruptingEventSubProcessEventSubscriptions.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/callactivity/interruptingEventSubProcessEventSubscriptions.bpmn20.xml" })
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

    assertProcessEnded(calledProcessInstanceId);
    assertProcessEnded(processInstance.getId());
  }

}
