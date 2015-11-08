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

package org.camunda.bpm.engine.test.api.dmn;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Philipp Ossler
 */
public class DecisionServiceTest extends PluggableProcessEngineTestCase {

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/api/dmn/Example.dmn";
  protected static final String DMN_FILE_SECOND_VERSION = "org/camunda/bpm/engine/test/api/dmn/Example_v2.dmn";

  protected static final String DECISION_DEFINITION_KEY = "decision";

  protected static final String RESULT_OF_FIRST_VERSION = "ok";
  protected static final String RESULT_OF_SECOND_VERSION = "notok";

  @Deployment(resources = DMN_FILE)
  public void testEvaluateDecisionById() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    DmnDecisionResult decisionResult = decisionService.evaluateDecisionById(decisionDefinition.getId(), createVariables());

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Deployment(resources = DMN_FILE)
  public void testEvaluateDecisionByKey() {
    DmnDecisionResult decisionResult = decisionService.evaluateDecisionByKey(DECISION_DEFINITION_KEY, createVariables());

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Deployment(resources = DMN_FILE)
  public void testEvaluateDecisionByKeyAndLatestVersion() {
    String secondDeploymentId = repositoryService.createDeployment().addClasspathResource(DMN_FILE_SECOND_VERSION).deploy().getId();
    try {

      DmnDecisionResult decisionResult = decisionService.evaluateDecisionByKey(DECISION_DEFINITION_KEY, createVariables());

      assertThatDecisionHasResult(decisionResult, RESULT_OF_SECOND_VERSION);

    } finally {
      repositoryService.deleteDeployment(secondDeploymentId, true);
    }
  }

  @Deployment(resources = DMN_FILE)
  public void testEvaluateDecisionByKeyAndVersion() {
    String secondDeploymentId = repositoryService.createDeployment().addClasspathResource(DMN_FILE_SECOND_VERSION).deploy().getId();
    try {

      DmnDecisionResult decisionResult = decisionService.evaluateDecisionByKeyAndVersion(DECISION_DEFINITION_KEY, 1, createVariables());

      assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);

    } finally {
      repositoryService.deleteDeployment(secondDeploymentId, true);
    }
  }

  @Deployment(resources = DMN_FILE)
  public void testEvaluateDecisionByKeyAndNullVersion() {
    String secondDeploymentId = repositoryService.createDeployment().addClasspathResource(DMN_FILE_SECOND_VERSION).deploy().getId();
    try {

      DmnDecisionResult decisionResult = decisionService.evaluateDecisionByKeyAndVersion(DECISION_DEFINITION_KEY, null, createVariables());

      assertThatDecisionHasResult(decisionResult, RESULT_OF_SECOND_VERSION);

    } finally {
      repositoryService.deleteDeployment(secondDeploymentId, true);
    }
  }

  public void testEvaluateDecisionByNullId() {
    try {
      decisionService.evaluateDecisionById(null, null);
      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("decision definition id is null", e.getMessage());
    }
  }

  public void testEvaluateDecisionByNonExistingId() {
    try {
      decisionService.evaluateDecisionById("unknown", null);
      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("no deployed decision definition found with id 'unknown'", e.getMessage());
    }
  }

  public void testEvaluateDecisionByNullKey() {
    try {
      decisionService.evaluateDecisionByKey(null, null);
      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("decision definition key is null", e.getMessage());
    }
  }

  public void testEvaluateDecisionByNonExistingKey() {
    try {
      decisionService.evaluateDecisionByKey("unknown", null);
      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("no decision definition deployed with key 'unknown'", e.getMessage());
    }
  }

  @Deployment(resources = DMN_FILE)
  public void testEvaluateDecisionByKeyWithNonExistingVersion() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    try {
      decisionService.evaluateDecisionByKeyAndVersion(decisionDefinition.getKey(), 42, null);
      fail("expect exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("no decision definition deployed with key = 'decision' and version = '42'", e.getMessage());
    }
  }

  protected VariableMap createVariables() {
    return Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
  }

  protected void assertThatDecisionHasResult(DmnDecisionResult decisionResult, Object expectedValue) {
    assertThat(decisionResult, is(notNullValue()));
    assertThat(decisionResult.size(), is(1));
    String value = decisionResult.getSingleOutput().getFirstValue();
    assertThat(value, is(expectedValue));
  }

}
