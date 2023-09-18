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

package org.camunda.bpm.qa.upgrade.scenarios7190.httl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.qa.upgrade.scenarios7190.httl.EnforceHistoryTimeToLiveTest.Assert.assertDoesNotThrow;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("EnforceHistoryTimeToLiveScenario")
@Origin("7.19.0")
public class EnforceHistoryTimeToLiveTest {

  private static final String SCENARIO_PROCESS = "processWithoutHTTL";
  private static final String SCENARIO_DECISION = "decisionWithoutHTTL";
  private static final String SCENARIO_CASE = "caseWithoutHTTL";

  @Rule
  public final UpgradeTestRule engineRule = new UpgradeTestRule();

  ManagementService managementService;
  RuntimeService runtimeService;
  ProcessEngineConfigurationImpl engineConfiguration;
  RepositoryService repositoryService;


  @Before
  public void assignServices() {
    managementService = engineRule.getManagementService();
    runtimeService = engineRule.getRuntimeService();
    engineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();

    // given
    engineConfiguration.setEnforceHistoryTimeToLive(true);
  }

  @Test
  @ScenarioUnderTest("enforceHistoryTimeToLive.onExistingProcess")
  public void shouldNotFailDueToDefaultEnforceHistoryTimeToLiveTrueOnProcess() {
    // given a new deployment (clean cache) of 7.20 & the already deployed process 'processWithoutHTTL' by 7.19

    // then enforceHistoryTimeToLive=true should be ignored and not throw exception
    assertDoesNotThrow(() -> {
      String id = repositoryService.createProcessDefinitionQuery()
          .processDefinitionKey(SCENARIO_PROCESS)
          .singleResult()
          .getId();

      // when re-parsing of process definition is triggered due to getProcessDefinition and fresh cache
      ProcessDefinition result = repositoryService.getProcessDefinition(id);
      assertThat(result).isNotNull();
    });
  }

  @Test
  @ScenarioUnderTest("enforceHistoryTimeToLive.onExistingDecision")
  public void shouldNotFailDueToDefaultEnforceHistoryTimeToLiveTrueOnDecision() {
    // given a new deployment (clean cache) of 7.20 & the already deployed decision 'decisionWithoutHTTL' by 7.19

    // then enforceHistoryTimeToLive=true should be ignored and not throw exception
    assertDoesNotThrow(() -> {
      String id = repositoryService.createDecisionDefinitionQuery()
          .decisionDefinitionKey(SCENARIO_DECISION)
          .singleResult()
          .getId();

      // when re-parsing of process definition is triggered due to getDecisionDefinition and fresh cache
      DecisionDefinition result = repositoryService.getDecisionDefinition(id);
      assertThat(result).isNotNull();
    });
  }

  @Test
  @ScenarioUnderTest("enforceHistoryTimeToLive.onExistingCase")
  public void shouldNotFailDueToDefaultEnforceHistoryTimeToLiveTrueOnCase() {
    // given a new deployment (clean cache) of 7.20 & the already deployed case 'caseWithoutHTTL' by 7.19

    // then enforceHistoryTimeToLive=true should be ignored and not throw exception
    assertDoesNotThrow(() -> {
      String id = repositoryService.createCaseDefinitionQuery()
          .caseDefinitionKey(SCENARIO_CASE)
          .singleResult()
          .getId();

      // when re-parsing of process definition is triggered due to getCaseDefinition and fresh cache
      CaseDefinition result = repositoryService.getCaseDefinition(id);
      assertThat(result).isNotNull();
    });
  }

  static class Assert {

    @FunctionalInterface
    interface FailingRunnable {
      void run() throws Exception;
    }

    static void assertDoesNotThrow(FailingRunnable runnable) {
      try {
        runnable.run();
      } catch (Exception ex) {
        fail("Expected No Exceptions but threw " + ex);
      }
    }
  }

}