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
package org.camunda.bpm.engine.test.api.authorization.task.updatevariable;

import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.UPDATE_TASK_VARIABLE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.authorization.TaskPermissions.UPDATE_VARIABLE;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Yana.Vasileva
 *
 */
@RunWith(Parameterized.class)
public class ProcessTaskAuthorizationTest {

  private static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String userId = "userId";
  protected static final String VARIABLE_NAME = "aVariableName";
  protected static final String VARIABLE_VALUE = "aVariableValue";
  protected static final String PROCESS_KEY = "oneTaskProcess";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule);

  @Parameter
  public AuthorizationScenario scenario;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected TaskService taskService;
  protected RuntimeService runtimeService;

  protected boolean ensureSpecificVariablePermission;
  protected String deploumentId;

  @Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(TASK, "taskId", userId, UPDATE),
          grant(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK),
          grant(TASK, "taskId", userId, UPDATE_VARIABLE),
          grant(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK_VARIABLE)),
      scenario()
        .withAuthorizations(
          grant(TASK, "taskId", userId, UPDATE)),
      scenario()
        .withAuthorizations(
          grant(TASK, "*", userId, UPDATE)),
      scenario()
        .withAuthorizations(
          grant(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK)),
      scenario()
        .withAuthorizations(
          grant(PROCESS_DEFINITION, "*", userId, UPDATE_TASK)),
      scenario()
        .withAuthorizations(
          grant(TASK, "taskId", userId, UPDATE_VARIABLE)),
      scenario()
        .withAuthorizations(
          grant(TASK, "*", userId, UPDATE_VARIABLE)),
      scenario()
        .withAuthorizations(
          grant(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK_VARIABLE)),
      scenario()
        .withAuthorizations(
          grant(PROCESS_DEFINITION, "*", userId, UPDATE_TASK_VARIABLE))
        .succeeds()
      );
  }

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    taskService = engineRule.getTaskService();
    runtimeService = engineRule.getRuntimeService();

    authRule.createUserAndGroup("userId", "groupId");
    deploumentId = engineRule.getRepositoryService().createDeployment().addClasspathResource(ONE_TASK_PROCESS).deployWithResult().getId();
    ensureSpecificVariablePermission = processEngineConfiguration.isEnforceSpecificVariablePermission();

    // prerequisite of the whole test suite
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
    processEngineConfiguration.setEnforceSpecificVariablePermission(ensureSpecificVariablePermission);
    engineRule.getRepositoryService().deleteDeployment(deploumentId, true);
  }

  @Test
  public void testSetVariable() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    taskService.setVariable(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    if (authRule.assertScenario(scenario)) {
      verifySetVariables();
    }
  }

  @Test
  public void testSetVariableLocal() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    taskService.setVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    if (authRule.assertScenario(scenario)) {
      verifySetVariables();
    }
  }

  @Test
  public void testSetVariables() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    taskService.setVariables(taskId, getVariables());

    // then
    if (authRule.assertScenario(scenario)) {
      verifySetVariables();
    }
  }

  @Test
  public void testSetVariablesLocal() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    taskService.setVariablesLocal(taskId, getVariables());

    // then
    if (authRule.assertScenario(scenario)) {
      verifySetVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testRemoveVariable() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    taskService.removeVariable(taskId, VARIABLE_NAME);

    // then
    if (authRule.assertScenario(scenario)) {
      verifyRemoveVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testRemoveVariableLocal() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    taskService.removeVariableLocal(taskId, VARIABLE_NAME);

    // then
    if (authRule.assertScenario(scenario)) {
      verifyRemoveVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testRemoveVariables() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY, getVariables());
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    taskService.removeVariables(taskId, Arrays.asList(VARIABLE_NAME));

    // then
    if (authRule.assertScenario(scenario)) {
      verifyRemoveVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testRemoveVariablesLocal() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("taskId", taskId)
      .start();

    taskService.removeVariablesLocal(taskId, Arrays.asList(VARIABLE_NAME));

    // then
    if (authRule.assertScenario(scenario)) {
      verifyRemoveVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testUpdateVariablesAdd() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    ((TaskServiceImpl) taskService).updateVariables(taskId, getVariables(), null);

    // then
    if (authRule.assertScenario(scenario)) {
      verifySetVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testUpdateVariablesRemove() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setVariable(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    ((TaskServiceImpl) taskService).updateVariables(taskId, null, Arrays.asList(VARIABLE_NAME));

    // then
    if (authRule.assertScenario(scenario)) {
      verifyRemoveVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testUpdateVariablesAddRemove() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    ((TaskServiceImpl) taskService).updateVariables(taskId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then
    if (authRule.assertScenario(scenario)) {
      verifyRemoveVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testUpdateVariablesLocalAdd() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    ((TaskServiceImpl) taskService).updateVariablesLocal(taskId, getVariables(), null);

    // then
    if (authRule.assertScenario(scenario)) {
      verifySetVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testUpdateVariablesLocalRemove() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setVariableLocal(taskId, VARIABLE_NAME, VARIABLE_VALUE);

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    ((TaskServiceImpl) taskService).updateVariablesLocal(taskId, null, Arrays.asList(VARIABLE_NAME));

    // then
    if (authRule.assertScenario(scenario)) {
      verifyRemoveVariables();
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testUpdateVariablesLocalAddRemove() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    ((TaskServiceImpl) taskService).updateVariablesLocal(taskId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then
    if (authRule.assertScenario(scenario)) {
      verifyRemoveVariables();
    }
  }

  protected void verifySetVariables() {
    verifyVariableInstanceCount(1);
    assertNotNull(runtimeService.createVariableInstanceQuery().singleResult());
  }

  protected void verifyRemoveVariables() {
    verifyVariableInstanceCount(0);
    assertNull(runtimeService.createVariableInstanceQuery().singleResult());
    HistoricVariableInstance deletedVariable = engineRule.getHistoryService().createHistoricVariableInstanceQuery().includeDeleted().singleResult();
    Assert.assertEquals("DELETED", deletedVariable.getState());
  }

  protected void verifyVariableInstanceCount(int count) {
    assertEquals(count, runtimeService.createVariableInstanceQuery().list().size());
    assertEquals(count, runtimeService.createVariableInstanceQuery().count());
  }

  protected VariableMap getVariables() {
    return Variables.createVariables().putValue(VARIABLE_NAME, VARIABLE_VALUE);
  }

}
