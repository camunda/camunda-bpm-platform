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
package org.camunda.bpm.qa.rolling.update.authorization;

import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.rolling.update.AbstractRollingUpdateTestCase;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@ScenarioUnderTest("AuthorizationScenario")
public class AuthorizationTest extends AbstractRollingUpdateTestCase {

  public static final String PROCESS_DEF_KEY = "oneTaskProcess";
  protected static final String USER_ID = "user";
  protected static final String GROUP_ID = "group";

  protected IdentityService identityService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected FormService formService;

  @Before
  public void setUp() {
    identityService = rule.getIdentityService();
    repositoryService = rule.getRepositoryService();
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
    historyService = rule.getHistoryService();
    formService = rule.getFormService();

    identityService.clearAuthentication();
    identityService.setAuthentication(USER_ID + rule.getBuisnessKey(), Arrays.asList(GROUP_ID + rule.getBuisnessKey()));
  }

  @After
  public void cleanUp() {
    identityService.clearAuthentication();
  }

  @Test
  @ScenarioUnderTest("startProcessInstance.1")
  public void testAuthorization() {
    //test access process related
    testGetDeployment();
    testGetProcessDefinition();
    testGetProcessInstance();
    testGetExecution();
    testGetTask();

    //test access historic
    testGetHistoricProcessInstance();
    testGetHistoricActivityInstance();
    testGetHistoricTaskInstance();

    //test process modification
    testSetVariable();
    testSubmitStartForm();
    testStartProcessInstance();
    testCompleteTaskInstance();
    testSubmitTaskForm();
  }


  public void testGetDeployment() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    assertFalse(deployments.isEmpty());
  }

  public void testGetProcessDefinition() {
    List<ProcessDefinition> definitions = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEF_KEY)
        .list();
    assertFalse(definitions.isEmpty());
  }

  public void testGetProcessInstance() {
    List<ProcessInstance> instances = runtimeService
        .createProcessInstanceQuery()
        .processInstanceBusinessKey(rule.getBuisnessKey())
        .processDefinitionKey(PROCESS_DEF_KEY)
        .list();
    assertFalse(instances.isEmpty());
  }

  public void testGetExecution() {
    List<Execution> executions = runtimeService
        .createExecutionQuery()
        .processInstanceBusinessKey(rule.getBuisnessKey())
        .processDefinitionKey(PROCESS_DEF_KEY)
        .list();
    assertFalse(executions.isEmpty());
  }

  public void testGetTask() {
    List<Task> tasks = taskService
        .createTaskQuery()
        .processInstanceBusinessKey(rule.getBuisnessKey())
        .processDefinitionKey(PROCESS_DEF_KEY)
        .list();
    assertFalse(tasks.isEmpty());
  }

  public void testGetHistoricProcessInstance() {
    List<HistoricProcessInstance> instances= historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceBusinessKey(rule.getBuisnessKey())
        .processDefinitionKey(PROCESS_DEF_KEY)
        .list();
    assertFalse(instances.isEmpty());
  }

  public void testGetHistoricActivityInstance() {
    List<HistoricActivityInstance> instances= historyService
        .createHistoricActivityInstanceQuery()
        .list();
    assertFalse(instances.isEmpty());
  }

  public void testGetHistoricTaskInstance() {
    List<HistoricTaskInstance> instances= historyService
        .createHistoricTaskInstanceQuery()
        .processDefinitionKey(PROCESS_DEF_KEY)
        .list();
    assertFalse(instances.isEmpty());
  }

  public void testStartProcessInstance() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_DEF_KEY, rule.getBuisnessKey());
    assertNotNull(instance);
  }

  public void testSubmitStartForm() {
    ProcessInstance instance = formService.submitStartForm(rule.processInstance().getProcessDefinitionId(), rule.getBuisnessKey(), null);
    assertNotNull(instance);
  }

  public void testCompleteTaskInstance() {
    String taskId = taskService
        .createTaskQuery()
        .processDefinitionKey(PROCESS_DEF_KEY)
        .processInstanceBusinessKey(rule.getBuisnessKey())
        .listPage(0, 1)
        .get(0)
        .getId();
    taskService.complete(taskId);
  }

  public void testSubmitTaskForm() {
    String taskId = taskService
        .createTaskQuery()
        .processDefinitionKey(PROCESS_DEF_KEY)
        .processInstanceBusinessKey(rule.getBuisnessKey())
        .listPage(0, 1)
        .get(0)
        .getId();
    formService.submitTaskForm(taskId, null);
  }

  public void testSetVariable() {
    String processInstanceId = runtimeService
        .createProcessInstanceQuery()
        .processDefinitionKey(PROCESS_DEF_KEY)
        .listPage(0, 1)
        .get(0)
        .getId();
    runtimeService.setVariable(processInstanceId, "abc", "def");
  }
}
