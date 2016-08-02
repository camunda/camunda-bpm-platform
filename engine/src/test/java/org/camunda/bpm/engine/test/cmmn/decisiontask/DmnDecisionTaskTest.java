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
package org.camunda.bpm.engine.test.cmmn.decisiontask;

import org.camunda.bpm.engine.exception.dmn.DecisionDefinitionNotFoundException;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class DmnDecisionTaskTest extends CmmnProcessEngineTestCase {

  public static final String CMMN_CALL_DECISION_CONSTANT = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallDecisionAsConstant.cmmn";
  public static final String CMMN_CALL_DECISION_CONSTANT_WITH_MANUAL_ACTIVATION = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallDecisionAsConstantWithManualActiovation.cmmn";
  public static final String CMMN_CALL_DECISION_EXPRESSION = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallDecisionAsExpressionStartsWithDollar.cmmn";
  public static final String CMMN_CALL_DECISION_EXPRESSION_WITH_MANUAL_ACTIVATION = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallDecisionAsExpressionStartsWithDollarWithManualActiovation.cmmn";

  public static final String DECISION_OKAY_DMN = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testDecisionOkay.dmn11.xml";
  public static final String DECISION_NOT_OKAY_DMN = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testDecisionNotOkay.dmn11.xml";
  public static final String DECISION_POJO_DMN = "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testPojo.dmn11.xml";

  public static final String DECISION_LITERAL_EXPRESSION_DMN = "org/camunda/bpm/engine/test/dmn/deployment/DecisionWithLiteralExpression.dmn";
  public static final String DRD_DISH_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected final String CASE_KEY = "case";
  protected final String DECISION_TASK = "PI_DecisionTask_1";
  protected final String DECISION_KEY = "testDecision";

  @Deployment(resources = {CMMN_CALL_DECISION_CONSTANT, DECISION_OKAY_DMN })
  public void testCallDecisionAsConstant() {
    // given
    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY);

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("okay", getDecisionResult(caseInstance));
  }

  @Deployment(resources = {
      CMMN_CALL_DECISION_EXPRESSION,
      DECISION_OKAY_DMN
    })
  public void testCallDecisionAsExpressionStartsWithDollar() {
    // given
    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY, Variables.createVariables().putValue("testDecision", "testDecision"));

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("okay", getDecisionResult(caseInstance));
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallDecisionAsExpressionStartsWithHash.cmmn",
      DECISION_OKAY_DMN
    })
  public void testCallDecisionAsExpressionStartsWithHash() {
    // given
    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY, Variables.createVariables().putValue("testDecision", "testDecision"));

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("okay", getDecisionResult(caseInstance));
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallLatestDecision.cmmn",
      DECISION_OKAY_DMN
    })
  public void testCallLatestCase() {
    // given
    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource(DECISION_NOT_OKAY_DMN)
        .deploy()
        .getId();

    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY);

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("not okay", getDecisionResult(caseInstance));

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallDecisionByDeployment.cmmn",
      DECISION_OKAY_DMN
    })
  public void testCallDecisionByDeployment() {
    // given
    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource(DECISION_NOT_OKAY_DMN)
        .deploy()
        .getId();

    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY);

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("okay", getDecisionResult(caseInstance));

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallDecisionByVersion.cmmn",
      DECISION_OKAY_DMN
    })
  public void testCallDecisionByVersion() {
    // given
    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource(DECISION_NOT_OKAY_DMN)
        .deploy()
        .getId();

    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY);

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("not okay", getDecisionResult(caseInstance));

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallDecisionByVersionAsExpressionStartsWithDollar.cmmn",
      DECISION_OKAY_DMN
    })
  public void testCallDecisionByVersionAsExpressionStartsWithDollar() {
    // given
    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource(DECISION_NOT_OKAY_DMN)
        .deploy()
        .getId();

    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY, Variables.createVariables().putValue("myVersion", 2));

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("not okay", getDecisionResult(caseInstance));

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/decisiontask/DmnDecisionTaskTest.testCallDecisionByVersionAsExpressionStartsWithHash.cmmn",
      DECISION_OKAY_DMN
    })
  public void testCallDecisionByVersionAsExpressionStartsWithHash() {
    // given
    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource(DECISION_NOT_OKAY_DMN)
        .deploy()
        .getId();

    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY, Variables.createVariables().putValue("myVersion", 2));

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("not okay", getDecisionResult(caseInstance));

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = CMMN_CALL_DECISION_CONSTANT_WITH_MANUAL_ACTIVATION)
  public void testDecisionNotFound() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String decisionTaskId = queryCaseExecutionByActivityId(DECISION_TASK).getId();

    try {
      // when
      caseService
        .withCaseExecution(decisionTaskId)
        .manualStart();
      fail("It should not be possible to evaluate a not existing decision.");
    } catch (DecisionDefinitionNotFoundException e) {}
  }

  @Deployment(resources = {
      CMMN_CALL_DECISION_CONSTANT,
      DECISION_POJO_DMN
    })
  public void testPojo() {
    // given
    VariableMap variables = Variables.createVariables()
      .putValue("pojo", new TestPojo("okay", 13.37));
    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY, variables);

    assertEquals("okay", getDecisionResult(caseInstance));
  }

  @Deployment(resources = { CMMN_CALL_DECISION_CONSTANT, DECISION_OKAY_DMN })
  public void testIgnoreNonBlockingFlag() {
    // given
    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY);

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("okay", getDecisionResult(caseInstance));
  }

  @Deployment( resources = { CMMN_CALL_DECISION_EXPRESSION_WITH_MANUAL_ACTIVATION, DECISION_LITERAL_EXPRESSION_DMN} )
  public void testCallDecisionWithLiteralExpression() {
    // given
    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY, Variables.createVariables()
        .putValue("testDecision", "decisionLiteralExpression")
        .putValue("a", 2)
        .putValue("b", 3));

    String decisionTaskId = queryCaseExecutionByActivityId(DECISION_TASK).getId();

    // when
    caseService
      .withCaseExecution(decisionTaskId)
      .manualStart();

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals(5, getDecisionResult(caseInstance));
  }

  @Deployment(resources = { CMMN_CALL_DECISION_EXPRESSION, DRD_DISH_RESOURCE })
  public void testCallDecisionWithRequiredDecisions() {
    // given
    CaseInstance caseInstance = createCaseInstanceByKey(CASE_KEY, Variables.createVariables()
        .putValue("testDecision", "dish-decision")
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend"));

    // then
    assertNull(queryCaseExecutionByActivityId(DECISION_TASK));
    assertEquals("Light salad", getDecisionResult(caseInstance));
  }

  protected Object getDecisionResult(CaseInstance caseInstance) {
    return caseService.getVariable(caseInstance.getId(), "result");
  }

}
