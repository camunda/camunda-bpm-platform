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
package org.camunda.bpm.engine.test.api.dmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Philipp Ossler
 */
public class DecisionServiceTest {

  protected static final String DMN_DECISION_TABLE = "org/camunda/bpm/engine/test/api/dmn/Example.dmn";
  protected static final String DMN_DECISION_TABLE_V2 = "org/camunda/bpm/engine/test/api/dmn/Example_v2.dmn";

  protected static final String DMN_DECISION_LITERAL_EXPRESSION = "org/camunda/bpm/engine/test/api/dmn/DecisionWithLiteralExpression.dmn";
  protected static final String DMN_DECISION_LITERAL_EXPRESSION_V2 = "org/camunda/bpm/engine/test/api/dmn/DecisionWithLiteralExpression_v2.dmn";

  protected static final String DRD_DISH_DECISION_TABLE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected static final String DECISION_DEFINITION_KEY = "decision";

  protected static final String RESULT_OF_FIRST_VERSION = "ok";
  protected static final String RESULT_OF_SECOND_VERSION = "notok";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected DecisionService decisionService;
  protected RepositoryService repositoryService;

  @Before
  public void init() {
    decisionService = engineRule.getDecisionService();
    repositoryService = engineRule.getRepositoryService();
  }

  @Before
  public void enableDmnFeelLegacyBehavior() {
    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        engineRule.getProcessEngineConfiguration()
            .getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();
  }

  @After
  public void disableDmnFeelLegacyBehavior() {

    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        engineRule.getProcessEngineConfiguration()
            .getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();
  }

  @Deployment(resources = DMN_DECISION_TABLE)
  @Test
	public void evaluateDecisionTableById() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableById(decisionDefinition.getId(), createVariables());

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Deployment(resources = DMN_DECISION_TABLE)
  @Test
	public void evaluateDecisionTableByKey() {
    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY, createVariables());

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Deployment(resources = DMN_DECISION_TABLE)
  @Test
	public void evaluateDecisionTableByKeyAndLatestVersion() {
    testRule.deploy(DMN_DECISION_TABLE_V2);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey(DECISION_DEFINITION_KEY, createVariables());

    assertThatDecisionHasResult(decisionResult, RESULT_OF_SECOND_VERSION);
  }

  @Deployment(resources = DMN_DECISION_TABLE)
  @Test
	public void evaluateDecisionTableByKeyAndVersion() {
    testRule.deploy(DMN_DECISION_TABLE_V2);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKeyAndVersion(DECISION_DEFINITION_KEY, 1, createVariables());

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Deployment(resources = DMN_DECISION_TABLE)
  @Test
	public void evaluateDecisionTableByKeyAndNullVersion() {
    testRule.deploy(DMN_DECISION_TABLE_V2);

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKeyAndVersion(DECISION_DEFINITION_KEY, null, createVariables());

    assertThatDecisionHasResult(decisionResult, RESULT_OF_SECOND_VERSION);
  }

  @Test
	public void evaluateDecisionTableByNullId() {
    assertThatThrownBy(() -> decisionService.evaluateDecisionTableById(null, null))
      .isInstanceOf(NotValidException.class)
      .hasMessageContaining("either decision definition id or key must be set");
  }

  @Test
	public void evaluateDecisionTableByNonExistingId() {
    assertThatThrownBy(() -> decisionService.evaluateDecisionTableById("unknown", null))
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("no deployed decision definition found with id 'unknown'");
  }

  @Test
	public void evaluateDecisionTableByNullKey() {
    assertThatThrownBy(() -> decisionService.evaluateDecisionTableByKey(null, null))
      .isInstanceOf(NotValidException.class)
      .hasMessageContaining("either decision definition id or key must be set");
  }

  @Test
	public void evaluateDecisionTableByNonExistingKey() {
    assertThatThrownBy(() -> decisionService.evaluateDecisionTableByKey("unknown", null))
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("no decision definition deployed with key 'unknown'");
  }

  @Deployment(resources = DMN_DECISION_TABLE)
  @Test
	public void evaluateDecisionTableByKeyWithNonExistingVersion() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    assertThatThrownBy(() -> decisionService.evaluateDecisionTableByKeyAndVersion(decisionDefinition.getKey(), 42, null))
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("no decision definition deployed with key = 'decision' and version = '42'");
  }

  @Deployment(resources = DMN_DECISION_LITERAL_EXPRESSION)
  @Test
  public void evaluateDecisionById() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    DmnDecisionResult decisionResult = decisionService
        .evaluateDecisionById(decisionDefinition.getId())
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Deployment(resources = DMN_DECISION_LITERAL_EXPRESSION)
  @Test
  public void evaluateDecisionByKey() {
    DmnDecisionResult decisionResult = decisionService
        .evaluateDecisionByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Deployment(resources = DMN_DECISION_LITERAL_EXPRESSION)
  @Test
  public void evaluateDecisionByKeyAndLatestVersion() {
    testRule.deploy(DMN_DECISION_LITERAL_EXPRESSION_V2);

    DmnDecisionResult decisionResult = decisionService
        .evaluateDecisionByKey(DECISION_DEFINITION_KEY)
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_SECOND_VERSION);
  }

  @Deployment(resources = DMN_DECISION_LITERAL_EXPRESSION)
  @Test
  public void evaluateDecisionByKeyAndVersion() {
    testRule.deploy(DMN_DECISION_LITERAL_EXPRESSION_V2);

    DmnDecisionResult decisionResult = decisionService
        .evaluateDecisionByKey(DECISION_DEFINITION_KEY)
        .version(1)
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_FIRST_VERSION);
  }

  @Deployment(resources = DMN_DECISION_LITERAL_EXPRESSION)
  @Test
  public void evaluateDecisionByKeyAndNullVersion() {
    testRule.deploy(DMN_DECISION_LITERAL_EXPRESSION_V2);

    DmnDecisionResult decisionResult = decisionService
        .evaluateDecisionByKey(DECISION_DEFINITION_KEY)
        .version(null)
        .variables(createVariables())
        .evaluate();

    assertThatDecisionHasResult(decisionResult, RESULT_OF_SECOND_VERSION);
  }

  @Test
  public void evaluateDecisionByNullId() {
    assertThatThrownBy(() -> decisionService.evaluateDecisionById(null).evaluate())
      .isInstanceOf(NotValidException.class)
      .hasMessageContaining("either decision definition id or key must be set");
  }

  @Test
  public void evaluateDecisionByNonExistingId() {
    assertThatThrownBy(() -> decisionService.evaluateDecisionById("unknown").evaluate())
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("no deployed decision definition found with id 'unknown'");
  }

  @Test
  public void evaluateDecisionByNullKey() {
    assertThatThrownBy(() -> decisionService.evaluateDecisionByKey(null).evaluate())
      .isInstanceOf(NotValidException.class)
      .hasMessageContaining("either decision definition id or key must be set");
  }

  @Test
  public void evaluateDecisionByNonExistingKey() {
    assertThatThrownBy(() -> decisionService.evaluateDecisionByKey("unknown").evaluate())
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("no decision definition deployed with key 'unknown'");
  }

  @Deployment(resources = DMN_DECISION_LITERAL_EXPRESSION)
  @Test
  public void evaluateDecisionByKeyWithNonExistingVersion() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    assertThatThrownBy(() -> decisionService
        .evaluateDecisionByKey(decisionDefinition.getKey())
        .version(42)
        .evaluate())
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("no decision definition deployed with key = 'decision' and version = '42'");
  }

  @Deployment( resources = DRD_DISH_DECISION_TABLE )
  @Test
  public void evaluateDecisionWithRequiredDecisions() {

    DmnDecisionTableResult decisionResult = decisionService.evaluateDecisionTableByKey("dish-decision", Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend"));

    assertThatDecisionHasResult(decisionResult, "Light salad");
  }

  protected VariableMap createVariables() {
    return Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
  }

  protected void assertThatDecisionHasResult(DmnDecisionTableResult decisionResult, Object expectedValue) {
    assertThat(decisionResult).isNotNull();
    assertThat(decisionResult).hasSize(1);
    String value = decisionResult.getSingleResult().getFirstEntry();
    assertThat(value).isEqualTo(expectedValue);
  }

  protected void assertThatDecisionHasResult(DmnDecisionResult decisionResult, Object expectedValue) {
    assertThat(decisionResult).isNotNull();
    assertThat(decisionResult).hasSize(1);
    String value = decisionResult.getSingleResult().getFirstEntry();
    assertThat(value).isEqualTo(expectedValue);
  }

}
