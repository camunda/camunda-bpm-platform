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
package org.camunda.bpm.engine.test.api.authorization.optimize;


import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

import java.util.Date;
import java.util.List;

import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class OptimizeDecisionDefinitionServiceAuthorizationTest extends AuthorizationTest {

  protected String deploymentId;
  private OptimizeService optimizeService;

  public static final String DECISION_PROCESS =
    "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml";
  public static final String DECISION_SINGLE_OUTPUT_DMN =
    "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";

  @Override
  public void setUp() throws Exception {

    ProcessEngineConfigurationImpl config =
      (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    optimizeService = config.getOptimizeService();

    super.setUp();
  }

  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void testGetDecisionInstancesWithoutAuthorization() {
    // given
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    startProcessInstanceByKey("testProcess", variables);

    try {
      // when
      optimizeService.getHistoricDecisionInstances(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the decision instances");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(DECISION_DEFINITION.resourceName(), exceptionMessage);
    }

  }

  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void testGetDecisionInstancesWithAuthorization() {
    // given
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    startProcessInstanceByKey("testProcess", variables);
    createGrantAuthorization(DECISION_DEFINITION, "*", userId, READ_HISTORY);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(new Date(0L), null, 10);

    // then
    assertThat(decisionInstances.size(), is(1));
  }

  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void testAuthorizationsOnSingleDecisionDefinitionIsNotEnough() {
    // given
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    startProcessInstanceByKey("testProcess", variables);
    createGrantAuthorization(DECISION_DEFINITION, "testProcess", userId, READ_HISTORY);

    try {
      // when
      optimizeService.getHistoricDecisionInstances(new Date(0L), null, 10);
      fail("Exception expected: It should not be possible to retrieve the decision instances");
    } catch (AuthorizationException e) {
      // then
      String exceptionMessage = e.getMessage();
      assertTextPresent(userId, exceptionMessage);
      assertTextPresent(READ_HISTORY.getName(), exceptionMessage);
      assertTextPresent(DECISION_DEFINITION.resourceName(), exceptionMessage);
    }
  }

  @Deployment(resources = {DECISION_PROCESS, DECISION_SINGLE_OUTPUT_DMN})
  public void testGrantAuthorizationWithAllPermissions() {
    // given
    VariableMap variables = Variables.createVariables();
    variables.put("input1", null);
    startProcessInstanceByKey("testProcess", variables);
    createGrantAuthorization(DECISION_DEFINITION, "*", userId, ALL);

    // when
    List<HistoricDecisionInstance> decisionInstances =
      optimizeService.getHistoricDecisionInstances(new Date(0L), null, 10);

    // then
    assertThat(decisionInstances.size(), is(1));
  }

}
