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
package org.camunda.bpm.engine.test.api.authorization.batch;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenarioWithCount;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class DeleteHistoricProcessInstancesBatchAuthorizationTest extends AbstractBatchAuthorizationTest {

  protected static final long BATCH_OPERATIONS = 4;
  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Parameterized.Parameter
  public AuthorizationScenarioWithCount scenario;

  protected HistoryService historyService;

  @Before
  public void setupHistoricService() {
    historyService = engineRule.getHistoryService();
  }

  public void cleanBatch() {
    super.cleanBatch();
    List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().list();

    if (list.size() > 0) {
      List<String> instances = new ArrayList<String>();
      for (HistoricProcessInstance hpi : list) {
        instances.add(hpi.getId());
      }
      historyService.deleteHistoricProcessInstances(instances);
    }
  }

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        AuthorizationScenarioWithCount.scenario()
            .withCount(2L)
            .withAuthorizations(
                grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_DEFINITION, "Process_1", "userId", Permissions.READ_HISTORY, Permissions.DELETE_HISTORY),
                grant(Resources.PROCESS_DEFINITION, "Process_2", "userId", Permissions.READ_HISTORY)
            )
            .failsDueToRequired(
                grant(Resources.PROCESS_DEFINITION, "Process_2", "userId", Permissions.DELETE_HISTORY)
            ),
        AuthorizationScenarioWithCount.scenario()
            .withCount(0L)
            .withAuthorizations(
                grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_DEFINITION, "Process_1", "userId", Permissions.READ_HISTORY, Permissions.DELETE_HISTORY),
                grant(Resources.PROCESS_DEFINITION, "Process_2", "userId", Permissions.READ_HISTORY, Permissions.DELETE_HISTORY)
            ).succeeds()
    );
  }

  @Test
  public void testWithTwoInvocationsProcessInstancesList() {
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);
    setupAndExecuteHistoricProcessInstancesListTest();

    // then
    assertScenario();

    assertThat(historyService.createHistoricProcessInstanceQuery().count(), is(getScenario().getCount()));
  }

  @Test
  public void testProcessInstancesList() {
    setupAndExecuteHistoricProcessInstancesListTest();
    // then
    assertScenario();
  }

  protected void setupAndExecuteHistoricProcessInstancesListTest() {
    //given
    List<String> processInstanceIds = Arrays.asList(processInstance.getId(), processInstance2.getId());
    runtimeService.deleteProcessInstances(processInstanceIds, null, true, false);

    List<String> historicProcessInstances = new ArrayList<String>();
    for (HistoricProcessInstance hpi : historyService.createHistoricProcessInstanceQuery().list()) {
      historicProcessInstances.add(hpi.getId());
    }

    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("Process_1", sourceDefinition.getKey())
        .bindResource("Process_2", sourceDefinition2.getKey())
        .start();

    // when
    batch = historyService.deleteHistoricProcessInstancesAsync(
        historicProcessInstances, TEST_REASON);

    executeSeedAndBatchJobs();
  }

  @Override
  public AuthorizationScenarioWithCount getScenario() {
    return scenario;
  }

  protected void assertScenario() {
    if (authRule.assertScenario(getScenario())) {
      if (testHelper.isHistoryLevelFull()) {
        assertThat(engineRule.getHistoryService().createUserOperationLogQuery().count(), is(BATCH_OPERATIONS));
      }
      assertThat(historyService.createHistoricProcessInstanceQuery().count(), is(0L));
    }
  }
}
