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
package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

import java.util.Collection;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
@RunWith(Parameterized.class)
public class RestartAuthorizationTest {

  protected static final String TEST_REASON = "test reason";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testRule);

  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.READ_HISTORY)
        ),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.READ_HISTORY)
        )
        .failsDueToRequired(
          grant(Resources.PROCESS_INSTANCE, "*", "userId", Permissions.CREATE)
        ),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.READ_HISTORY, Permissions.CREATE_INSTANCE),
          grant(Resources.PROCESS_INSTANCE, "*", "userId", Permissions.CREATE)
        )
        .succeeds()
    );
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @Test
  public void execute() {
    //given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);

    ProcessInstance processInstance1 = engineRule.getRuntimeService().startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = engineRule.getRuntimeService().startProcessInstanceByKey("Process");
    engineRule.getRuntimeService().deleteProcessInstance(processInstance1.getId(), TEST_REASON);
    engineRule.getRuntimeService().deleteProcessInstance(processInstance2.getId(), TEST_REASON);

    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("processInstance1", processInstance1.getId())
        .bindResource("restartedProcessInstance", "*")
        .bindResource("processInstance2", processInstance2.getId())
        .bindResource("processDefinition", "Process")
        .start();

    engineRule.getRuntimeService()
        .restartProcessInstances(processDefinition.getId())
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .startAfterActivity("userTask1")
        .execute();

    // then
    authRule.assertScenario(scenario);
  }
}
