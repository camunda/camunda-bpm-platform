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

package org.camunda.bpm.engine.test.api.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

public class MultiTenancyDecisionEvaluationTest extends PluggableProcessEngineTestCase {

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/api/dmn/Example.dmn";
  protected static final String DMN_FILE_SECOND_VERSION = "org/camunda/bpm/engine/test/api/dmn/Example_v2.dmn";

  protected static final String DECISION_DEFINITION_KEY = "decision";

  protected static final String RESULT_OF_FIRST_VERSION = "ok";
  protected static final String RESULT_OF_SECOND_VERSION = "notok";

  @Deployment(resources = DMN_FILE)
  public void testFailToEvaluateDecisionByIdWithoutTenantId() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    try {
      decisionService.evaluateDecisionTableById(decisionDefinition.getId())
          .variables(createVariables())
          .decisionDefinitionWithoutTenantId()
          .evaluate();
      fail("ProcessEngineException exception");
    } catch(ProcessEngineException e) {
      // Expected exception
    }
  }

  public void testFailToEvaluateDecisionByKeyWithNoTenantId() {
    deploymentForTenant("tenantOne", DMN_FILE);
    deploymentForTenant("tenantTwo", DMN_FILE);

    try {
      decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
          .variables(createVariables())
          .evaluate();
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      // Expected exception
    }
  }

  @Deployment(resources = DMN_FILE)
  public void testEvaluateDecisionByKeyWithoutTenantId() {
    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .decisionDefinitionWithoutTenantId()
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  public void testEvaluateDecisionByKeyAndTenantId() {
    deploymentForTenant("myTenant", DMN_FILE);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .decisionDefinitionTenantId("myTenant")
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  public void testEvaluateDecisionByKeyVersionAndTenantId() {
    deploymentForTenant("myTenant", DMN_FILE);
    deploymentForTenant("myTenant", DMN_FILE_SECOND_VERSION);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .version(1)
        .decisionDefinitionTenantId("myTenant")
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  protected VariableMap createVariables() {
    return Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
  }

  protected void assertThatDecisionHasResult(DmnDecisionTableResult decisionResult, Object expectedValue) {
    assertThat(decisionResult, is(notNullValue()));
    assertThat(decisionResult.size(), is(1));
    String value = decisionResult.getSingleResult().getFirstEntry();
    assertThat(value, is(expectedValue));
  }

}
