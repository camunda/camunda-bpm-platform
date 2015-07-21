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
package org.camunda.bpm.integrationtest.functional.scriptengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.dmn.engine.DmnDecisionResult;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DmnScriptEngineSupportTest extends AbstractFoxPlatformIntegrationTest {

  public static final String RESULT_VARIABLE = "decisionResult";
  public static final String REASON_BRONZE = "work on your status first, as bronze you're not going to get anything";
  public static final String REASON_GOLD = "you get anything you want";

  @Deployment
  public static WebArchive processArchive() {

    return initWebArchiveDeployment()
      .addAsResource("org/camunda/bpm/integrationtest/functional/scriptengine/DmnScriptTaskTest.bpmn20.xml", "DmnScriptTaskTest.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/scriptengine/Example.dmn10.xml", "Example.dmn10.xml");
  }

  @Test
  public void testDeployProcessArchive() {

    VariableMap variables = Variables.createVariables()
      .putValue("status", "bronze")
      .putValue("sum", 100);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("rejectTask", task.getTaskDefinitionKey());
    assertEquals("notok", getResultVariable(pi));
    assertEquals(REASON_BRONZE, getReasonVariable(pi));
    taskService.complete(task.getId());

    variables.putValue("status", "gold");

    pi = runtimeService.startProcessInstanceByKey("testProcess", variables);
    task = taskService.createTaskQuery().singleResult();
    assertEquals("approveTask", task.getTaskDefinitionKey());
    assertEquals("ok", getResultVariable(pi));
    assertEquals(REASON_GOLD, getReasonVariable(pi));
    taskService.complete(task.getId());
  }

  @Test
  public void testDmnClassesAvailable() {
    try {
      Class.forName("org.camunda.dmn.engine.impl.DmnEngineImpl");
    }
    catch (ClassNotFoundException e) {
      fail("DMN engine not available");
    }

    try {
      Class.forName("org.camunda.bpm.model.dmn.Dmn");
    }
    catch (ClassNotFoundException e) {
      fail("DMN model not available");
    }

    try {
      Class.forName("org.camunda.dmn.scriptengine.DmnScriptEngine");
    }
    catch (ClassNotFoundException e) {
      fail("DMN scriptengine not available");
    }
  }

  protected String getResultVariable(ProcessInstance processInstance) {
    DmnDecisionResult decisionResult = (DmnDecisionResult) runtimeService.getVariable(processInstance.getId(), RESULT_VARIABLE);
    return decisionResult.getOutputs().get(0).getValue("result");
  }

  private String getReasonVariable(ProcessInstance processInstance) {
    DmnDecisionResult decisionResult = (DmnDecisionResult) runtimeService.getVariable(processInstance.getId(), RESULT_VARIABLE);
    return decisionResult.getOutputs().get(0).getValue("reason");
  }

}
