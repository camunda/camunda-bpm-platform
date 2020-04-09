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
package org.camunda.bpm.engine.test.api.optimize;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
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

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class GetHistoricDecisionInstancesForOptimizeTest {

  public static final String DECISION_PROCESS =
    "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml";
  public static final String DECISION_SINGLE_OUTPUT_DMN =
    "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";

  protected static final String VARIABLE_NAME = "aVariableName";
  protected static final String VARIABLE_VALUE = "aVariableValue";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  protected String userId = "test";
  private OptimizeService optimizeService;
  private IdentityService identityService;
  private RuntimeService runtimeService;
  private AuthorizationService authorizationService;

  @Before
  public void init() {
    ProcessEngineConfigurationImpl config =
      engineRule.getProcessEngineConfiguration();
    optimizeService = config.getOptimizeService();
    identityService = engineRule.getIdentityService();
    runtimeService = engineRule.getRuntimeService();
    authorizationService = engineRule.getAuthorizationService();

    createUser(userId);
  }

  @After
  public void cleanUp() {
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
    ClockUtil.reset();
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

  @Test
  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void getCompletedHistoricDecisionInstances() {
    // given start process and evaluate decision
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(pastDate(), null, 10);

    // then
    assertThat(decisionInstances.size(), is(1));
    assertThatDecisionsHaveAllImportantInformation(decisionInstances);
  }

  @Test
  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void decisionInputInstanceProperties() {
    // given start process and evaluate decision
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(pastDate(), null, 10);

    // then
    assertThat(decisionInstances.size(), is(1));
    HistoricDecisionInstance decisionInstance = decisionInstances.get(0);
    List<HistoricDecisionInputInstance> inputs = decisionInstance.getInputs();
    assertThat(inputs, is(notNullValue()));
    assertThat(inputs.size(), is(1));

    HistoricDecisionInputInstance input = inputs.get(0);
    assertThat(input.getDecisionInstanceId(), is(decisionInstance.getId()));
    assertThat(input.getClauseId(), is("in"));
    assertThat(input.getClauseName(), is("input"));
  }

  @Test
  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void decisionOutputInstanceProperties() {
    // given start process and evaluate decision
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(pastDate(), null, 10);

    // then
    assertThat(decisionInstances.size(), is(1));
    HistoricDecisionInstance decisionInstance = decisionInstances.get(0);
    List<HistoricDecisionOutputInstance> outputs = decisionInstance.getOutputs();
    assertThat(outputs, is(notNullValue()));
    assertThat(outputs.size(), is(1));

    HistoricDecisionOutputInstance output = outputs.get(0);
    assertThat(output.getDecisionInstanceId(), is(decisionInstance.getId()));
    assertThat(output.getClauseId(), is("out"));
    assertThat(output.getClauseName(), is("output"));

    assertThat(output.getRuleId(), is("rule"));
    assertThat(output.getRuleOrder(), is(1));

    assertThat(output.getVariableName(), is("result"));
  }

  @Test
  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void fishedAfterParameterWorks() {
    // given start process and evaluate decision
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    ProcessInstance secondProcessInstance =
      runtimeService.startProcessInstanceByKey("testProcess", variables);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(now, null, 10);

    // then
    assertThat(decisionInstances.size(), is(1));
    HistoricDecisionInstance decisionInstance = decisionInstances.get(0);
    assertThat(decisionInstance.getProcessInstanceId(), is(secondProcessInstance.getId()));
  }

  @Test
  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void fishedAtParameterWorks() {
    // given start process and evaluate decision
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    ProcessInstance firstProcessInstance =
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(null, now, 10);

    // then
    assertThat(decisionInstances.size(), is(1));
    HistoricDecisionInstance decisionInstance = decisionInstances.get(0);
    assertThat(decisionInstance.getProcessInstanceId(), is(firstProcessInstance.getId()));
  }

  @Test
  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void fishedAfterAndFinishedAtParameterWorks() {
    // given start process and evaluate decision
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    Date now = new Date();
    Date nowMinus2Seconds = new Date(now.getTime() - 2000L);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);

    ClockUtil.setCurrentTime(nowMinus2Seconds);
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    ClockUtil.setCurrentTime(now);
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(now, now, 10);

    // then
    assertThat(decisionInstances.size(), is(0));
  }

  @Test
  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void maxResultsParameterWorks() {
    // given start process and evaluate decision
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(null, null, 2);

    // then
    assertThat(decisionInstances.size(), is(2));
  }

  @Test
  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void resultIsSortedByEvaluationTime() {
    // given start process and evaluate decision
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    Date now = new Date();
    Date nowMinus2Seconds = new Date(now.getTime() - 2000L);
    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);

    ClockUtil.setCurrentTime(nowMinus2Seconds);
    ProcessInstance firstProcessInstance =
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    ClockUtil.setCurrentTime(now);
    ProcessInstance secondProcessInstance =
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    ProcessInstance thirdProcessInstance =
      runtimeService.startProcessInstanceByKey("testProcess", variables);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(pastDate(), null, 3);

    // then
    assertThat(decisionInstances.size(), is(3));
    assertThat(decisionInstances.get(0).getProcessInstanceId(), is(firstProcessInstance.getId()));
    assertThat(decisionInstances.get(1).getProcessInstanceId(), is(secondProcessInstance.getId()));
    assertThat(decisionInstances.get(2).getProcessInstanceId(), is(thirdProcessInstance.getId()));
  }

  private Date pastDate() {
    return new Date(2L);
  }

  protected void createUser(String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);
  }

  private void assertThatDecisionsHaveAllImportantInformation(List<HistoricDecisionInstance> decisionInstances) {
    assertThat(decisionInstances.size(), is(1));
    HistoricDecisionInstance decisionInstance =
      decisionInstances.get(0);


    assertThat(decisionInstance, notNullValue());
    assertThat(decisionInstance.getProcessDefinitionKey(), is("testProcess"));
    assertThat(decisionInstance.getProcessDefinitionId(), notNullValue());
    assertThat(decisionInstance.getDecisionDefinitionId(), notNullValue());
    assertThat(decisionInstance.getDecisionDefinitionKey(), is("testDecision"));
    assertThat(decisionInstance.getDecisionDefinitionName(), is("sample decision"));

    assertThat(decisionInstance.getActivityId(), is("task"));
    assertThat(decisionInstance.getActivityInstanceId(), notNullValue());

    assertThat(decisionInstance.getProcessInstanceId(), is(notNullValue()));
    assertThat(decisionInstance.getRootProcessInstanceId(), is(notNullValue()));

    assertThat(decisionInstance.getCaseDefinitionKey(), is(nullValue()));
    assertThat(decisionInstance.getCaseDefinitionId(), is(nullValue()));

    assertThat(decisionInstance.getCaseInstanceId(), is(nullValue()));

    assertThat(decisionInstance.getRootDecisionInstanceId(), is(nullValue()));
    assertThat(decisionInstance.getDecisionRequirementsDefinitionId(), is(nullValue()));
    assertThat(decisionInstance.getDecisionRequirementsDefinitionKey(), is(nullValue()));

    assertThat(decisionInstance.getEvaluationTime(), is(notNullValue()));
  }

}
