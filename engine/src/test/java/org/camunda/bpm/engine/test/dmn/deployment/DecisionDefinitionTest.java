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

package org.camunda.bpm.engine.test.dmn.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DecisionDefinitionTest {

  @ClassRule
  public static ProcessEngineBootstrapRule BOOTSTRAP_RULE = new ProcessEngineBootstrapRule(configuration -> {
    configuration.setHistoryTimeToLive("P30D");
  });

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(BOOTSTRAP_RULE);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule)
      .around(testRule);

  protected RepositoryService repositoryService;
  protected DecisionService decisionService;
  protected HistoryService historyService;

  @Before
  public void init() throws ParseException {
    this.repositoryService = engineRule.getRepositoryService();
    this.decisionService = engineRule.getDecisionService();
    this.historyService = engineRule.getHistoryService();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    Date fixedDate = sdf.parse("01/01/2001 01:01:01.000");

    ClockUtil.setCurrentTime(fixedDate);
  }

  @After
  public void tearDown() {
    ClockUtil.reset();
  }

  @Test
  public void shouldUseHistoryTTLOnDecisionDefinitions() {
    // given
    DmnModelInstance model = createDmnModelInstance(null);

    DeploymentBuilder builder = repositoryService.createDeployment().addModelInstance("foo.dmn", model);

    // when
    DeploymentWithDefinitions deployment = testRule.deploy(builder);

    // then
    assertThat(deployment.getDeployedDecisionDefinitions().size()).isEqualTo(1);
    assertThat(deployment.getDeployedDecisionDefinitions().get(0).getHistoryTimeToLive()).isEqualTo(30);
  }

  @Test
  public void shouldNotOverrideWithGlobalConfigOnDecisionHistoryTTLPresence() {
    // given
    DmnModelInstance model = createDmnModelInstance("P10D");

    DeploymentBuilder builder = repositoryService.createDeployment().addModelInstance("foo.dmn", model);

    // when
    DeploymentWithDefinitions deployment = testRule.deploy(builder);

    // then
    assertThat(deployment.getDeployedDecisionDefinitions().size()).isEqualTo(1);
    assertThat(deployment.getDeployedDecisionDefinitions().get(0).getHistoryTimeToLive()).isEqualTo(10);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void shouldApplyHistoryTTLOnRemovalTimeOfDecisionInstanceLocal() {
    // given
    DmnModelInstance model = createDmnModelInstance("P10D");
    DeploymentBuilder builder = repositoryService.createDeployment().addModelInstance("foo.dmn", model);

    testRule.deploy(builder);

    Map<String, Object> variables = new HashMap<>();
    variables.put("input", "single entry");

    // when
    decisionService.evaluateDecisionByKey("Decision-1")
        .variables(variables)
        .evaluate();

    HistoricDecisionInstance result = historyService.createHistoricDecisionInstanceQuery().singleResult();

    // then
    Date expectedRemovalDate = Date.from(ClockUtil.now()
        .toInstant()
        .plus(10, ChronoUnit.DAYS)
    );

    assertThat(result.getRemovalTime()).isInSameDayAs(expectedRemovalDate);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Test
  public void shouldApplyHistoryTTLOnRemovalTimeOfDecisionInstanceGlobal() {
    // given
    DmnModelInstance model = createDmnModelInstance(null);
    DeploymentBuilder builder = repositoryService.createDeployment().addModelInstance("foo.dmn", model);

    testRule.deploy(builder);

    Map<String, Object> variables = new HashMap<>();
    variables.put("input", "single entry");

    // when
    decisionService.evaluateDecisionByKey("Decision-1")
        .variables(variables)
        .evaluate();

    HistoricDecisionInstance result = historyService.createHistoricDecisionInstanceQuery().singleResult();

    // then
    Date expectedRemovalDate = Date.from(ClockUtil.now()
        .toInstant()
        .plus(30, ChronoUnit.DAYS)
    );

    assertThat(result.getRemovalTime()).isInSameDayAs(expectedRemovalDate);
  }

  protected DmnModelInstance createDmnModelInstance(String historyTTL) {
    DmnModelInstance modelInstance = Dmn.createEmptyModel();
    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setId(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setName(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setNamespace(DmnModelConstants.CAMUNDA_NS);
    modelInstance.setDefinitions(definitions);

    Decision decision = modelInstance.newInstance(Decision.class);
    decision.setId("Decision-1");
    decision.setName("foo");

    decision.setCamundaHistoryTimeToLiveString(historyTTL);

    modelInstance.getDefinitions().addChildElement(decision);

    DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
    decisionTable.setId(DmnModelConstants.DMN_ELEMENT_DECISION_TABLE);
    decisionTable.setHitPolicy(HitPolicy.FIRST);
    decision.addChildElement(decisionTable);

    Input input = modelInstance.newInstance(Input.class);
    input.setId("Input-1");
    input.setLabel("Input");
    decisionTable.addChildElement(input);

    InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
    inputExpression.setId("InputExpression-1");
    Text inputExpressionText = modelInstance.newInstance(Text.class);
    inputExpressionText.setTextContent("input");
    inputExpression.setText(inputExpressionText);
    inputExpression.setTypeRef("string");
    input.setInputExpression(inputExpression);

    Output output = modelInstance.newInstance(Output.class);
    output.setName("output");
    output.setLabel("Output");
    output.setTypeRef("string");
    decisionTable.addChildElement(output);

    return modelInstance;
  }
}
