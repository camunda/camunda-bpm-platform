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
package org.camunda.bpm.engine.test.cmmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnDisabledTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      "org/camunda/bpm/application/impl/deployment/cmmn.disabled.camunda.cfg.xml");

  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule engineTestRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(engineTestRule);

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;

  protected EmbeddedProcessApplication processApplication;

  @Before
  public void setUp() throws Exception {
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();
    authorizationService = engineRule.getAuthorizationService();

    processApplication = new EmbeddedProcessApplication();
  }

  @After
  public void tearDown() {
    identityService.clearAuthentication();
    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(false);
    engineTestRule.deleteAllAuthorizations();
    engineTestRule.deleteAllStandaloneTasks();
  }

  @Test
  public void testCmmnDisabled() {
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
        .deploy();

    // process is deployed:
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);
    assertEquals(1, processDefinition.getVersion());

    try {
      repositoryService.createCaseDefinitionQuery().singleResult();
      fail("Cmmn Disabled: It should not be possible to query for a case definition.");
    } catch (Exception e) {
      // expected
    }

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  public void testVariableInstanceQuery() {
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
        .deploy();

    VariableMap variables = Variables.createVariables().putValue("my-variable", "a-value");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // variable instance query
    List<VariableInstance> result = runtimeService.createVariableInstanceQuery().list();
    assertEquals(1, result.size());

    VariableInstance variableInstance = result.get(0);
    assertEquals("my-variable", variableInstance.getName());

    // get variable
    assertNotNull(runtimeService.getVariable(processInstance.getId(), "my-variable"));

    // get variable local
    assertNotNull(runtimeService.getVariableLocal(processInstance.getId(), "my-variable"));

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  public void testTaskQueryAuthorization() {
    // given
    engineTestRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml");
    engineTestRule.deploy("org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml");

    // a process instance task with read authorization
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task processInstanceTask = taskService.createTaskQuery().processInstanceId(instance1.getId()).singleResult();

    engineTestRule.createGrantAuthorization("user",
        Resources.PROCESS_DEFINITION,
        "oneTaskProcess",
        ProcessDefinitionPermissions.READ_TASK);

    // a standalone task with read authorization
    Task standaloneTask = taskService.newTask();
    taskService.saveTask(standaloneTask);

    engineTestRule.createGrantAuthorization("user",
        Resources.TASK,
        standaloneTask.getId(),
        TaskPermissions.READ);

    // a third task for which we have no authorization
    runtimeService.startProcessInstanceByKey("twoTasksProcess");

    identityService.setAuthenticatedUserId("user");
    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(true);

    // when
    List<Task> tasks = taskService.createTaskQuery().list();

    // then
    assertThat(tasks).extracting("id").containsExactlyInAnyOrder(standaloneTask.getId(), processInstanceTask.getId());
  }

}
