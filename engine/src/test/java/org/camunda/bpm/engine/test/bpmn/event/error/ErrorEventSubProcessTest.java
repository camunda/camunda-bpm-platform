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
package org.camunda.bpm.engine.test.bpmn.event.error;

import static org.camunda.bpm.engine.test.bpmn.event.error.ThrowErrorDelegate.throwError;
import static org.camunda.bpm.engine.test.bpmn.event.error.ThrowErrorDelegate.throwException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;


/**
 * @author Falko Menge
 */
public class ErrorEventSubProcessTest extends PluggableProcessEngineTestCase {
  
  @Deployment
  // an event subprocesses takes precedence over a boundary event
  public void testEventSubprocessTakesPrecedence() {
    String procId = runtimeService.startProcessInstanceByKey("CatchErrorInEmbeddedSubProcess").getId();
    assertThatErrorHasBeenCaught(procId);
  }
  
  @Deployment
  // an event subprocess with errorCode takes precedence over a catch-all handler
  public void testErrorCodeTakesPrecedence() {
    String procId = runtimeService.startProcessInstanceByKey("CatchErrorInEmbeddedSubProcess").getId();
        
    // The process will throw an error event,
    // which is caught and escalated by a User Task
    assertEquals("No tasks found in task list.", 1, taskService.createTaskQuery()
            .taskDefinitionKey("taskAfterErrorCatch2") // <!>
            .count());
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Escalated Task", task.getName());
    
    // Completing the Task will end the process instance
    taskService.complete(task.getId());
    assertProcessEnded(procId);
    
  }
  
  @Deployment
  public void testCatchErrorInEmbeddedSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("CatchErrorInEmbeddedSubProcess").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByScriptTaskInEmbeddedSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("CatchErrorThrownByScriptTaskInEmbeddedSubProcess").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByScriptTaskInEmbeddedSubProcessWithErrorCode() {
    String procId = runtimeService.startProcessInstanceByKey("CatchErrorThrownByScriptTaskInEmbeddedSubProcessWithErrorCode").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByScriptTaskInTopLevelProcess() {
    String procId = runtimeService.startProcessInstanceByKey("CatchErrorThrownByScriptTaskInTopLevelProcess").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByScriptTaskInsideSubProcessInTopLevelProcess() {
    String procId = runtimeService.startProcessInstanceByKey("CatchErrorThrownByScriptTaskInsideSubProcessInTopLevelProcess").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testThrowErrorInScriptTaskInsideCallActivitiCatchInTopLevelProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-child.bpmn20.xml" })
  public void testThrowErrorInScriptTaskInsideCallActivitiCatchInTopLevelProcess() {
    String procId = runtimeService.startProcessInstanceByKey("testThrowErrorInScriptTaskInsideCallActivitiCatchInTopLevelProcess").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchErrorThrownByAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  public void testCatchExceptionThrownByExecuteOfAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess", throwException()).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchErrorThrownByAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  public void testCatchErrorThrownByExecuteOfAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess", throwError()).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchErrorThrownByAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  public void testCatchExceptionThrownBySignalOfAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess").getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult();
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwException());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchErrorThrownByAbstractBpmnActivityBehavior.bpmn20.xml"
  })
  public void testCatchErrorThrownBySignalOfAbstractBpmnActivityBehavior() {
    String pi = runtimeService.startProcessInstanceByKey("testProcess").getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult();
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwError());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchErrorThrownByDelegateExpression.bpmn20.xml"
  })
  public void testCatchExceptionThrownByExecuteOfDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    variables.putAll(throwException());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchErrorThrownByDelegateExpression.bpmn20.xml"
  })
  public void testCatchErrorThrownByExecuteOfDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    variables.putAll(throwError());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchErrorThrownByDelegateExpression.bpmn20.xml"
  })
  public void testCatchExceptionThrownBySignalOfDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult();
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwException());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskException", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchErrorThrownByDelegateExpression.bpmn20.xml"
  })
  public void testCatchErrorThrownBySignalOfDelegateExpression() {
    VariableMap variables = Variables.createVariables().putValue("myDelegate", new ThrowErrorDelegate());
    String pi = runtimeService.startProcessInstanceByKey("testProcess", variables).getId();

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertNull(runtimeService.getVariable(pi, "signaled"));

    Execution serviceTask = runtimeService.createExecutionQuery().processInstanceId(pi).activityId("serviceTask").singleResult();
    assertNotNull(serviceTask);

    runtimeService.setVariables(pi, throwError());
    runtimeService.signal(serviceTask.getId());

    assertTrue((Boolean) runtimeService.getVariable(pi, "executed"));
    assertTrue((Boolean) runtimeService.getVariable(pi, "signaled"));

    Task userTask = taskService.createTaskQuery().processInstanceId(pi).singleResult();
    assertNotNull(userTask);
    assertEquals("userTaskError", userTask.getTaskDefinitionKey());

    taskService.complete(userTask.getId());
  }

  private void assertThatErrorHasBeenCaught(String procId) {
    // The process will throw an error event,
    // which is caught and escalated by a User Task
    assertEquals("No tasks found in task list.", 1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Escalated Task", task.getName());
    
    // Completing the Task will end the process instance
    taskService.complete(task.getId());
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testCatchErrorEventSubprocessSetErrorVariables(){
    runtimeService.startProcessInstanceByKey("Process_1");
    //the name used in "camunda:errorCodeVariable" in the BPMN
    String variableName = "errorCode";
    VariableInstance errorVariable = runtimeService.createVariableInstanceQuery().variableName(variableName).singleResult();

    assertThat(errorVariable, is(notNullValue()));
    //the code we gave the thrown error
    Object errorCode = "error";
    assertThat(errorVariable.getValue(), is(errorCode));

  }
  
  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/error/ThrowErrorProcess.bpmn",
      "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchErrorFromCallActivitySetsErrorVariables.bpmn"
  })
  public void testCatchErrorFromCallActivitySetsErrorVariable(){
    runtimeService.startProcessInstanceByKey("Process_1");
    //the name used in "camunda:errorCodeVariable" in the BPMN
    String variableName = "errorCode";
    VariableInstance errorVariable = runtimeService.createVariableInstanceQuery().variableName(variableName).singleResult();

    assertThat(errorVariable, is(notNullValue()));
    //the code we gave the thrown error
    Object errorCode = "error";
    assertThat(errorVariable.getValue(), is(errorCode));
  }
  
  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testCatchBpmnErrorFromJavaDelegateInsideCallActivitySetsErrorVariable.bpmn",
      "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithThrownError.bpmn"
    })
  public void testCatchBpmnErrorFromJavaDelegateInsideCallActivitySetsErrorVariable(){
    runtimeService.startProcessInstanceByKey("Process_1");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    //the name used in "camunda:errorCodeVariable" in the BPMN
    String variableName = "errorCode";
    //the code we gave the thrown error
    Object errorCode = "errorCode";
    VariableInstance errorVariable = runtimeService.createVariableInstanceQuery().variableName(variableName).singleResult();
    assertThat(errorVariable.getValue(), is(errorCode));

    errorVariable = runtimeService.createVariableInstanceQuery().variableName("errorMessageVariable").singleResult();
    assertThat(errorVariable.getValue(), is((Object)"ouch!"));
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testThrowErrorInLoop.bpmn20.xml"
    })
  public void testShouldNotThrowErrorInLoop(){
    runtimeService.startProcessInstanceByKey("looping-error"); 
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("WaitState", task.getName());
    taskService.complete(task.getId());

    assertEquals("ErrorHandlingUserTask", taskService.createTaskQuery().singleResult().getName());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testThrowErrorInLoopWithCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/error/ThrowErrorToCallActivity.bpmn20.xml"
    })
  public void testShouldNotThrowErrorInLoopWithCallActivity(){
    runtimeService.startProcessInstanceByKey("CallActivityErrorInLoop"); 
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("ErrorLog", task.getName());
    taskService.complete(task.getId());

    assertEquals("ErrorHandlingUserTask", taskService.createTaskQuery().singleResult().getName());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testThrowErrorInLoopWithMultipleSubProcess.bpmn20.xml",
    })
  public void testShouldNotThrowErrorInLoopForMultipleSubProcess(){
    runtimeService.startProcessInstanceByKey("looping-error"); 

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("LoggerTask", task.getName());
    taskService.complete(task.getId());
    
    assertEquals("ErrorHandlingTask", taskService.createTaskQuery().singleResult().getName());
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/bpmn/event/error/ErrorEventSubProcessTest.testThrowErrorInLoopFromCallActivityToEventSubProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/bpmn/event/error/ThrowErrorToCallActivity.bpmn20.xml"
    })
  public void FAILING_testShouldNotThrowErrorInLoopFromCallActivityToEventSubProcess(){
    runtimeService.startProcessInstanceByKey("Process_1"); 
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("userTask", task.getName());
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();
    assertEquals("ErrorLog", task.getName());
    taskService.complete(task.getId());

    // TODO: Loop exists when error thrown from call activity to event sub process
    // as they both have different process definition - CAM-6212
    assertEquals("BoundaryEventTask", taskService.createTaskQuery().singleResult().getName());
  }

  
}
