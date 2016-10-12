/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.authorization.history;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
@RunWith(Parameterized.class)
public class DeleteHistoricProcessInstancesAuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  protected ProcessInstance processInstance;
  protected ProcessInstance processInstance2;

  protected HistoricProcessInstance historicProcessInstance;
  protected HistoricProcessInstance historicProcessInstance2;

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ManagementService managementService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);
  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .withAuthorizations(
                grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.READ_HISTORY)
            )
            .failsDueToRequired(
                grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.DELETE_HISTORY)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.READ_HISTORY, Permissions.DELETE_HISTORY)
            ).succeeds()
    );
  }

  @Before
  public void setUp() {
    authRule.createUserAndGroup("userId", "groupId");
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();

    deployAndCompleteProcesses();
  }

  public void deployAndCompleteProcesses() {
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    processInstance2 = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    List<String> processInstanceIds = Arrays.asList(
        new String[]{processInstance.getId(), processInstance2.getId()});
    runtimeService.deleteProcessInstances(processInstanceIds, null, false, false);

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).singleResult();

    historicProcessInstance2 = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance2.getId()).singleResult();
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @Test
  public void testProcessInstancesList() {
    //given
    List<String> processInstanceIds = Arrays.asList(historicProcessInstance.getId(), historicProcessInstance2.getId());
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("processInstance1", processInstance.getId())
        .bindResource("processInstance2", processInstance2.getId())
        .start();

    // when
    historyService.deleteHistoricProcessInstances(processInstanceIds);

    // then
    if (authRule.assertScenario(scenario)) {
      assertThat(historyService.createHistoricProcessInstanceQuery().count(), is(0L));
    }
  }
}
