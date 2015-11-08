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
package org.camunda.bpm.engine.test.dmn.businessruletask;

import org.camunda.bpm.engine.exception.dmn.DecisionDefinitionNotFoundException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

public class DmnBusinessRuleTaskTest extends PluggableProcessEngineTestCase {

  public static final String DECISION_PROCESS = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRef.bpmn20.xml";
  public static final String DECISION_PROCESS_EXPRESSION = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRefExpression.bpmn20.xml";
  public static final String DECISION_PROCESS_LATEST = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRefLatestBinding.bpmn20.xml";
  public static final String DECISION_PROCESS_DEPLOYMENT = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRefDeploymentBinding.bpmn20.xml";
  public static final String DECISION_PROCESS_VERSION = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionRefVersionBinding.bpmn20.xml";
  public static final String DECISION_OKAY_DMN = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionOkay.dmn11.xml";
  public static final String DECISION_NOT_OKAY_DMN = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testDecisionNotOkay.dmn11.xml";
  public static final String DECISION_POJO_DMN = "org/camunda/bpm/engine/test/dmn/businessruletask/DmnBusinessRuleTaskTest.testPojo.dmn11.xml";

  @Deployment(resources = { DECISION_PROCESS, DECISION_PROCESS_EXPRESSION, DECISION_OKAY_DMN })
  public void testDecisionRef() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    assertEquals("okay", getDecisionResult(processInstance));

    processInstance = startExpressionProcess("testDecision", 1);
    assertEquals("okay", getDecisionResult(processInstance));
  }

  @Deployment(resources = { DECISION_PROCESS, DECISION_PROCESS_EXPRESSION })
  public void testNoDecisionFound() {
    try {
      runtimeService.startProcessInstanceByKey("testProcess");
      fail("Exception expected");
    }
    catch (DecisionDefinitionNotFoundException e) {
      assertTextPresent("no decision definition deployed with key 'testDecision'", e.getMessage());
    }

    try {
      startExpressionProcess("testDecision", 1);
      fail("Exception expected");
    }
    catch (DecisionDefinitionNotFoundException e) {
      assertTextPresent("no decision definition deployed with key = 'testDecision' and version = '1'", e.getMessage());
    }
  }

  @Deployment(resources = { DECISION_PROCESS_LATEST, DECISION_OKAY_DMN })
  public void testDecisionRefLatestBinding() {
    String secondDeploymentId = repositoryService.createDeployment().addClasspathResource(DECISION_NOT_OKAY_DMN).deploy().getId();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    assertEquals("not okay", getDecisionResult(processInstance));

    repositoryService.deleteDeployment(secondDeploymentId, true);
  }

  @Deployment(resources = { DECISION_PROCESS_DEPLOYMENT, DECISION_OKAY_DMN })
  public void testDecisionRefDeploymentBinding() {
    String secondDeploymentId = repositoryService.createDeployment().addClasspathResource(DECISION_NOT_OKAY_DMN).deploy().getId();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    assertEquals("okay", getDecisionResult(processInstance));

    repositoryService.deleteDeployment(secondDeploymentId, true);
  }

  @Deployment(resources = { DECISION_PROCESS_VERSION, DECISION_PROCESS_EXPRESSION, DECISION_OKAY_DMN })
  public void testDecisionRefVersionBinding() {
    String secondDeploymentId = repositoryService.createDeployment().addClasspathResource(DECISION_NOT_OKAY_DMN).deploy().getId();
    String thirdDeploymentId = repositoryService.createDeployment().addClasspathResource(DECISION_OKAY_DMN).deploy().getId();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    assertEquals("not okay", getDecisionResult(processInstance));

    processInstance = startExpressionProcess("testDecision", 2);
    assertEquals("not okay", getDecisionResult(processInstance));

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

   @Deployment(resources = {DECISION_PROCESS, DECISION_POJO_DMN})
   public void testPojo() {
     VariableMap variables = Variables.createVariables()
       .putValue("pojo", new TestPojo("okay", 13.37));
     ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", variables);

     assertEquals("okay", getDecisionResult(processInstance));
   }

  protected ProcessInstance startExpressionProcess(Object decisionKey, Object version) {
    VariableMap variables = Variables.createVariables()
        .putValue("decision", decisionKey)
        .putValue("version", version);
    return runtimeService.startProcessInstanceByKey("testProcessExpression", variables);
  }

  protected Object getDecisionResult(ProcessInstance processInstance) {
    // the single value of the single output of the decision result is stored as process variable
    return runtimeService.getVariable(processInstance.getId(), "result");
  }

}
