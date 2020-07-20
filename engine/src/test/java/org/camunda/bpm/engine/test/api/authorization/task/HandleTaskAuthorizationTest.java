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
package org.camunda.bpm.engine.test.api.authorization.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.authorization.Permissions.TASK_WORK;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.List;

import ch.qos.logback.classic.Level;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class HandleTaskAuthorizationTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
                                                      .watch(BPMN_BEHAVIOR_LOGGER)
                                                      .level(Level.INFO);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule);

  @Parameter
  public AuthorizationScenario scenario;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected TaskService taskService;
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;

  protected static final String userId = "userId";
  protected String deploymentId;

  protected static final String BPMN_BEHAVIOR_LOGGER = "org.camunda.bpm.engine.bpmn.behavior";
  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String PROCESS_KEY = "oneTaskProcess";

  @Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(TASK, "taskId", userId, TASK_WORK),
          grant(PROCESS_DEFINITION, PROCESS_KEY, userId, TASK_WORK),
          grant(TASK, "taskId", userId, UPDATE),
          grant(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK)),
      scenario()
        .withAuthorizations(
          grant(TASK, "taskId", userId, TASK_WORK)),
      scenario()
        .withAuthorizations(
          grant(PROCESS_DEFINITION, PROCESS_KEY, userId, TASK_WORK)),
      scenario()
        .withAuthorizations(
          grant(TASK, "taskId", userId, UPDATE)),
      scenario()
        .withAuthorizations(
          grant(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_TASK))
        .succeeds()
      );
  }

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    taskService = engineRule.getTaskService();
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();

    authRule.createUserAndGroup("userId", "groupId");
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Test
  public void testHandleTaskBpmnError() {
    // given
    deploymentId = repositoryService.createDeployment().addClasspathResource(ONE_TASK_PROCESS).deployWithResult().getId();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    taskService.handleBpmnError(taskId, "anErrorCode");

    // then
    if (authRule.assertScenario(scenario)) {
      assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult());
      assertThat(loggingRule.getFilteredLog(BPMN_BEHAVIOR_LOGGER, "Execution is ended (none end event semantics)").size()).isEqualTo(1);
      assertThat(loggingRule.getFilteredLog(BPMN_BEHAVIOR_LOGGER, "no catching boundary event was defined").size()).isEqualTo(1);
    }
  }

  @Test
  public void testHandleTaskEscalation() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask("throw-escalation")
          .boundaryEvent()
            .escalation("anEscalationCode")
          .userTask("after-catch")
        .moveToActivity("throw-escalation")
        .userTask("after-throw")
        .endEvent()
        .done();
    deploymentId = repositoryService.createDeployment().addModelInstance("escalation.bpmn", model).deploy().getId();
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("taskId", taskId)
        .start();

    taskService.handleEscalation(taskId, "anEscalationCode");

    // then
    if (authRule.assertScenario(scenario)) {
      List<Task> tasks = taskService.createTaskQuery().list();
      assertThat(tasks.size()).isEqualTo(1);
      assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("after-catch");
    }
  }

}
