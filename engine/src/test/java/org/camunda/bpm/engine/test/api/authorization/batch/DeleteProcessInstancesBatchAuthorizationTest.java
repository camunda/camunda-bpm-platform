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

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
@RunWith(Parameterized.class)
public class DeleteProcessInstancesBatchAuthorizationTest extends AbstractBatchAuthorizationTest {

  protected static final long BATCH_OPERATIONS = 3L;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_INSTANCE, "processInstance1", "userId", Permissions.READ, Permissions.DELETE),
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", Permissions.READ)
            )
            .failsDueToRequired(
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", Permissions.DELETE),
                grant(Resources.PROCESS_DEFINITION, "Process_2", "userId", Permissions.DELETE_INSTANCE)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_INSTANCE, "processInstance1", "userId", Permissions.ALL),
                grant(Resources.PROCESS_INSTANCE, "processInstance2", "userId", Permissions.ALL)
            ).succeeds(),
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
                grant(Resources.PROCESS_DEFINITION, "Process_2", "userId", Permissions.READ_INSTANCE, Permissions.DELETE_INSTANCE),
                grant(Resources.PROCESS_DEFINITION, "Process_1", "userId", Permissions.READ_INSTANCE, Permissions.DELETE_INSTANCE)
            ).succeeds()
    );
  }

  @Test
  public void testWithTwoInvocationsProcessInstancesList() {
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);
    setupAndExecuteProcessInstancesListTest();

    // then
    assertScenario();
  }

  @Test
  public void testProcessInstancesList() {
    setupAndExecuteProcessInstancesListTest();
    // then
    assertScenario();
  }

  @Test
  public void testWithQuery() {
    //given
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery()
        .processInstanceIds(new HashSet<String>(Arrays.asList(processInstance.getId(), processInstance2.getId())));

    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("processInstance1", processInstance.getId())
        .bindResource("processInstance2", processInstance2.getId())
        .bindResource("Process_2", sourceDefinition2.getKey())
        .start();

    // when

    batch = runtimeService.deleteProcessInstancesAsync(null,
        processInstanceQuery, TEST_REASON);
    executeSeedAndBatchJobs();

    // then
    if (authRule.assertScenario(scenario)) {
      if (testHelper.isHistoryLevelFull()) {
        assertThat(engineRule.getHistoryService().createUserOperationLogQuery().count(), is(BATCH_OPERATIONS));
      }
    }
  }

  protected void setupAndExecuteProcessInstancesListTest() {
    //given
    List<String> processInstanceIds = Arrays.asList(processInstance.getId(), processInstance2.getId());
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("processInstance1", processInstance.getId())
        .bindResource("processInstance2", processInstance2.getId())
        .bindResource("Process_2", sourceDefinition2.getKey())
        .bindResource("Process_1", sourceDefinition.getKey())
        .start();

    // when
    batch = runtimeService.deleteProcessInstancesAsync(
        processInstanceIds, null, TEST_REASON);

    executeSeedAndBatchJobs();
  }

  protected void assertScenario() {
    if (authRule.assertScenario(getScenario())) {
      if (testHelper.isHistoryLevelFull()) {
        assertThat(engineRule.getHistoryService().createUserOperationLogQuery().count(), is(BATCH_OPERATIONS));
      }

      if (authRule.scenarioSucceeded()) {
        assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));
      }
    }
  }

  @Override
  public AuthorizationScenario getScenario() {
    return scenario;
  }
}
