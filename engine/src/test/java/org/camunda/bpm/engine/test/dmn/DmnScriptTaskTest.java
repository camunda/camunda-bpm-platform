/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.test.dmn;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.dmn.engine.DmnDecisionOutput;
import org.camunda.dmn.engine.DmnDecisionResult;

public class DmnScriptTaskTest extends PluggableProcessEngineTestCase {

  public static final String RESULT_VARIABLE = "decisionResult";

  public static final String RESULT_OK = "ok";
  public static final String RESULT_NOTOK = "notok";

  public static final String REASON_BRONZE = "work on your status first, as bronze you're not going to get anything";
  public static final String REASON_SILVER_OK = "you little fish will get what you want";
  public static final String REASON_SILVER_NOTOK = "you took too much man, you took too much!";
  public static final String REASON_GOLD = "you get anything you want";

  protected String processInstanceId;

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/DmnScriptTaskTest.bpmn20.xml",
    "org/camunda/bpm/engine/test/dmn/Example.dmn10.xml"
  })
  public void testDmnExampleWithScriptTask() {
    VariableMap variables = Variables.createVariables()
      .putValue("status", "bronze")
      .putValue("sum", 100);

    startProcess(variables);
    currentTaskIsRejectTask();
    assertEquals(RESULT_NOTOK, getResultVariable());
    assertEquals(REASON_BRONZE, getReasonVariable());
    completeProcess();

    variables.putValue("sum", 10000);

    startProcess(variables);
    currentTaskIsRejectTask();
    assertEquals(RESULT_NOTOK, getResultVariable());
    assertEquals(REASON_BRONZE, getReasonVariable());
    completeProcess();

    variables
      .putValue("status", "silver")
      .putValue("sum", 999);

    startProcess(variables);
    currentTaskIsApproveTask();
    assertEquals(RESULT_OK, getResultVariable());
    assertEquals(REASON_SILVER_OK, getReasonVariable());
    completeProcess();

    variables.putValue("sum", 1000);

    startProcess(variables);
    currentTaskIsRejectTask();
    assertEquals(RESULT_NOTOK, getResultVariable());
    assertEquals(REASON_SILVER_NOTOK, getReasonVariable());
    completeProcess();

    variables.putValue("sum", 100000);

    startProcess(variables);
    currentTaskIsRejectTask();
    assertEquals(RESULT_NOTOK, getResultVariable());
    assertEquals(REASON_SILVER_NOTOK, getReasonVariable());
    completeProcess();

    variables
      .putValue("status", "gold")
      .putValue("sum", 0);

    startProcess(variables);
    currentTaskIsApproveTask();
    assertEquals(RESULT_OK, getResultVariable());
    assertEquals(REASON_GOLD, getReasonVariable());
    completeProcess();

    variables.putValue("sum", 100000);

    startProcess(variables);
    currentTaskIsApproveTask();
    assertEquals(RESULT_OK, getResultVariable());
    assertEquals(REASON_GOLD, getReasonVariable());
    completeProcess();
  }

  protected void currentTaskIsRejectTask() {
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("rejectTask", task.getTaskDefinitionKey());
  }

  protected void currentTaskIsApproveTask() {
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("approveTask", task.getTaskDefinitionKey());
  }

  protected String getResultVariable() {
    return getDecisionOutput().getValue("result");
  }

  protected String getReasonVariable() {
    return getDecisionOutput().getValue("reason");
  }

  protected DmnDecisionOutput getDecisionOutput() {
    DmnDecisionResult decisionResult = (DmnDecisionResult) runtimeService.getVariable(processInstanceId, RESULT_VARIABLE);
    return decisionResult.getOutputs().get(0);
  }

  protected void startProcess(VariableMap variables) {
    processInstanceId =  runtimeService.startProcessInstanceByKey("testProcess", variables).getId();
  }

  protected void completeTask() {
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
  }

  protected void completeProcess() {
    completeTask();
    assertProcessEnded(processInstanceId);
  }

}
