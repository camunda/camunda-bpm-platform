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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class DeleteProcessDefinitionAuthorizationTest {

  public static final String PROCESS_DEFINITION_KEY = "one";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.PROCESS_DEFINITION, PROCESS_DEFINITION_KEY, "userId", Permissions.DELETE)),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_DEFINITION, PROCESS_DEFINITION_KEY, "userId", Permissions.DELETE))
        .succeeds(),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_DEFINITION, "*", "userId", Permissions.DELETE))
        .succeeds()
      );
  }

  @Before
  public void setUp() {
    authRule.createUserAndGroup("userId", "groupId");
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
    repositoryService = null;
    runtimeService = null;
    processEngineConfiguration = null;
  }

  @Test
  public void testDeleteProcessDefinition() {
    testHelper.deploy("org/camunda/bpm/engine/test/repository/twoProcesses.bpmn20.xml");
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    authRule.init(scenario)
      .withUser("userId")
      .start();

    //when a process definition is been deleted
    repositoryService.deleteProcessDefinition(processDefinitions.get(0).getId());

    //then only one process definition should remain
    if (authRule.assertScenario(scenario)) {
      assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    }
  }


  @Test
  public void testDeleteProcessDefinitionCascade() {
    // given process definition and a process instance
    BpmnModelInstance bpmnModel = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().userTask().endEvent().done();
    testHelper.deploy(bpmnModel);

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).executeWithVariablesInReturn();

    authRule.init(scenario)
      .withUser("userId")
      .start();

    //when the corresponding process definition is cascading deleted from the deployment
    repositoryService.deleteProcessDefinition(processDefinition.getId(), true);

    //then exist no process instance and no definition
    if (authRule.assertScenario(scenario)) {
      assertEquals(0, runtimeService.createProcessInstanceQuery().count());
      assertEquals(0, repositoryService.createProcessDefinitionQuery().count());
      if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId()) {
        assertEquals(0, engineRule.getHistoryService().createHistoricActivityInstanceQuery().count());
      }
    }
  }

  @Test
  public void testDeleteProcessDefinitionsByKey() {
    // given
    for (int i = 0; i < 3; i++) {
      deployProcessDefinition();
    }

    authRule.init(scenario)
      .withUser("userId")
      .start();

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey(PROCESS_DEFINITION_KEY)
      .withoutTenantId()
      .delete();

    // then
    if (authRule.assertScenario(scenario)) {
      assertEquals(0, repositoryService.createProcessDefinitionQuery().count());
    }
  }

  @Test
  public void testDeleteProcessDefinitionsByKeyCascade() {
    // given
    for (int i = 0; i < 3; i++) {
      deployProcessDefinition();
    }

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    authRule.init(scenario)
      .withUser("userId")
      .start();

    // when
    repositoryService.deleteProcessDefinitions()
      .byKey(PROCESS_DEFINITION_KEY)
      .withoutTenantId()
      .cascade()
      .delete();

    // then
    if (authRule.assertScenario(scenario)) {
      assertEquals(0, runtimeService.createProcessInstanceQuery().count());
      assertEquals(0, repositoryService.createProcessDefinitionQuery().count());

      if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId()) {
        assertEquals(0, historyService.createHistoricActivityInstanceQuery().count());
      }
    }
  }

  @Test
  public void testDeleteProcessDefinitionsByIds() {
    // given
    for (int i = 0; i < 3; i++) {
      deployProcessDefinition();
    }

    String[] processDefinitionIds = findProcessDefinitionIdsByKey(PROCESS_DEFINITION_KEY);

    authRule.init(scenario)
      .withUser("userId")
      .start();

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(processDefinitionIds)
      .delete();

    // then
    if (authRule.assertScenario(scenario)) {
      assertEquals(0, repositoryService.createProcessDefinitionQuery().count());
    }
  }

  @Test
  public void testDeleteProcessDefinitionsByIdsCascade() {
    // given
    for (int i = 0; i < 3; i++) {
      deployProcessDefinition();
    }

    String[] processDefinitionIds = findProcessDefinitionIdsByKey(PROCESS_DEFINITION_KEY);

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    authRule.init(scenario)
      .withUser("userId")
      .start();

    // when
    repositoryService.deleteProcessDefinitions()
      .byIds(processDefinitionIds)
      .cascade()
      .delete();

    // then
    if (authRule.assertScenario(scenario)) {
      assertEquals(0, runtimeService.createProcessInstanceQuery().count());
      assertEquals(0, repositoryService.createProcessDefinitionQuery().count());

      if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_ACTIVITY.getId()) {
        assertEquals(0, historyService.createHistoricActivityInstanceQuery().count());
      }
    }
  }

  private void deployProcessDefinition() {
    testHelper.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .userTask()
      .endEvent()
      .done());
  }

  private String[] findProcessDefinitionIdsByKey(String processDefinitionKey) {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey(processDefinitionKey).list();
    List<String> processDefinitionIds = new ArrayList<String>();
    for (ProcessDefinition processDefinition: processDefinitions) {
      processDefinitionIds.add(processDefinition.getId());
    }

    return processDefinitionIds.toArray(new String[0]);
  }

}
