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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.management.DeploymentStatisticsQuery;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestBaseRule;
import org.camunda.bpm.engine.test.api.identity.TestPermissions;
import org.camunda.bpm.engine.test.api.identity.TestResource;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DisabledPermissionsAuthorizationTest {

  protected static final String USER_ID = "user";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestBaseRule authRule = new AuthorizationTestBaseRule(engineRule);
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  ProcessEngineConfigurationImpl processEngineConfiguration;
  RepositoryService repositoryService;
  AuthorizationService authorizationService;
  RuntimeService runtimeService;
  ManagementService managementService;
  TaskService taskService;

  @Before
  public void setUp() {
    authRule.createUserAndGroup(USER_ID, "group");
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    authorizationService = engineRule.getAuthorizationService();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    taskService = engineRule.getTaskService();
  }

  @After
  public void tearDown() {
    authRule.disableAuthorization();
    authRule.deleteUsersAndGroups();
    processEngineConfiguration.setDisabledPermissions(null);
  }

  @Test
  public void testIsUserAuthorizedForIgnoredPermission() {
    // given
    processEngineConfiguration.setDisabledPermissions(Arrays.asList(READ.name()));

    authRule.createGrantAuthorization(PROCESS_INSTANCE, ANY, USER_ID, ProcessInstancePermissions.RETRY_JOB);

    authRule.enableAuthorization(USER_ID);

    // when/then
    assertThatThrownBy(() -> authorizationService.isUserAuthorized(USER_ID, null, READ, PROCESS_DEFINITION))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("The 'READ' permission is disabled, please check your process engine configuration.");
  }

  @Test
  public void testCustomPermissionDuplicateValue() {
    // given
    processEngineConfiguration.setDisabledPermissions(Arrays.asList(ProcessInstancePermissions.SUSPEND.name()));
    Resource resource1 = TestResource.RESOURCE1;
    Resource resource2 = TestResource.RESOURCE2;

    // assume
    assertEquals(ProcessInstancePermissions.SUSPEND.getValue(), TestPermissions.RANDOM.getValue());

    // when
    authRule.createGrantAuthorization(resource1, ANY, USER_ID, TestPermissions.RANDOM);
    authRule.createGrantAuthorization(resource2, "resource2-1", USER_ID, TestPermissions.RANDOM);
    authRule.enableAuthorization(USER_ID);

    // then
    // verify that the custom permission with the same value is not affected by disabling the build-in permission
    assertEquals(true, authorizationService.isUserAuthorized(USER_ID, null, TestPermissions.RANDOM, resource1));
    assertEquals(true, authorizationService.isUserAuthorized(USER_ID, null, TestPermissions.RANDOM, resource2, "resource2-1"));
  }

  // specific scenarios //////////////////////////////////////
  // the next tests cover different combination in the authorization checks
  // i.e. the query doesn't fail if all permissions are disabled in the specific authorization check

  @Test
  public void testGetVariableIgnoreTaskRead() {
    // given
    processEngineConfiguration.setDisabledPermissions(Arrays.asList(TaskPermissions.READ.name()));
    String taskId = "taskId";
    Task task = taskService.newTask(taskId);
    taskService.saveTask(task);

    taskService.setVariables(taskId, Variables.createVariables().putValue("foo", "bar"));
    authRule.enableAuthorization(USER_ID);

    // when
    Object variable = taskService.getVariable(taskId, "foo");

    // then
    assertEquals("bar", variable);
    authRule.disableAuthorization();
    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testQueryTaskIgnoreTaskRead() {
    // given
    List<String> permissions = new ArrayList<>();
    permissions.add(TaskPermissions.READ.name());
    permissions.add(ProcessDefinitionPermissions.READ_TASK.name());
    processEngineConfiguration.setDisabledPermissions(permissions);
    String taskId = "taskId";
    Task task = taskService.newTask(taskId);
    taskService.saveTask(task);

    authRule.enableAuthorization(USER_ID);

    // when
    Task returnedTask = taskService.createTaskQuery().singleResult();

    // then
    assertNotNull(returnedTask);
    authRule.disableAuthorization();
    taskService.deleteTask(taskId, true);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testDeleteHistoricProcessInstanceIgnoreDeleteHistory() {
    // given
    processEngineConfiguration.setDisabledPermissions(Arrays.asList(Permissions.DELETE_HISTORY.name()));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.deleteProcessInstance(processInstance.getId(), "any");
    authRule.enableAuthorization(USER_ID);

    engineRule.getHistoryService().deleteHistoricProcessInstance(processInstance.getId());
    authRule.disableAuthorization();
    assertNull(engineRule.getHistoryService().createHistoricProcessInstanceQuery().singleResult());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testQueryDeploymentIgnoreRead() {
    // given
    engineRule.getProcessEngineConfiguration().setDisabledPermissions(Arrays.asList(READ.name()));

    // when
    authRule.enableAuthorization(USER_ID);
    List<org.camunda.bpm.engine.repository.Deployment> deployments = engineRule.getRepositoryService().createDeploymentQuery().list();

    // then
    assertEquals(1, deployments.size());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testStartableInTasklistIgnoreRead() {
    // given
    processEngineConfiguration.setDisabledPermissions(Arrays.asList(READ.name()));
    authRule.createGrantAuthorization(PROCESS_DEFINITION, "oneTaskProcess", USER_ID, CREATE_INSTANCE);
    authRule.createGrantAuthorization(PROCESS_INSTANCE, "*", USER_ID, CREATE);

    authRule.disableAuthorization();
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult();
    authRule.enableAuthorization(USER_ID);

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startablePermissionCheck().startableInTasklist().list();
    // then
    assertNotNull(processDefinitions);
    assertEquals(1, repositoryService.createProcessDefinitionQuery().startablePermissionCheck().startableInTasklist().count());
    assertEquals(definition.getId(), processDefinitions.get(0).getId());
    assertTrue(processDefinitions.get(0).isStartableInTasklist());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/authorization/timerBoundaryEventProcess.bpmn20.xml")
  public void testDeploymentStatisticsIgnoreReadInstance() {
    // given
    processEngineConfiguration.setDisabledPermissions(Arrays.asList(READ_INSTANCE.name()));

    runtimeService.startProcessInstanceByKey("timerBoundaryProcess");

    authRule.enableAuthorization(USER_ID);

    // when
    DeploymentStatisticsQuery query = engineRule.getManagementService().createDeploymentStatisticsQuery();

    // then
    List<DeploymentStatistics> statistics = query.list();

    for (DeploymentStatistics deploymentStatistics : statistics) {
      assertEquals("Instances", 1, deploymentStatistics.getInstances());
      assertEquals("Failed Jobs", 0, deploymentStatistics.getFailedJobs());

      List<IncidentStatistics> incidentStatistics = deploymentStatistics.getIncidentStatistics();
      assertTrue("Incidents supposed to be empty", incidentStatistics.isEmpty());
    }

  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/authorization/timerBoundaryEventProcess.bpmn20.xml")
  public void testActivityStatisticsIgnoreRead() {
    // given
    List<String> permissions = new ArrayList<>();
    permissions.add(READ.name());
    permissions.add(READ_INSTANCE.name());
    processEngineConfiguration.setDisabledPermissions(permissions);
    String processDefinitionId = runtimeService.startProcessInstanceByKey("timerBoundaryProcess").getProcessDefinitionId();

    authRule.enableAuthorization(USER_ID);

    // when
    ActivityStatistics statistics = managementService.createActivityStatisticsQuery(processDefinitionId).singleResult();

    // then
    assertNotNull(statistics);
    assertEquals("task", statistics.getId());
    assertEquals(1, statistics.getInstances());
    assertEquals(0, statistics.getFailedJobs());
    assertTrue(statistics.getIncidentStatistics().isEmpty());
  }

  @Test
  @Ignore("CAM-9888")
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  public void testFetchAndLockIgnoreRead() {
    // given
    List<String> permissions = new ArrayList<>();
    permissions.add(READ.name());
    permissions.add(READ_INSTANCE.name());
    processEngineConfiguration.setDisabledPermissions(permissions);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    authRule.createGrantAuthorization(PROCESS_INSTANCE, "*", USER_ID, UPDATE);

    authRule.enableAuthorization(USER_ID);

    // when
    List<LockedExternalTask> externalTasks = engineRule.getExternalTaskService()
        .fetchAndLock(1, "aWorkerId")
        .topic("externalTaskTopic", 10000L)
        .execute();

    // then
    assertEquals(1, externalTasks.size());

    LockedExternalTask task = externalTasks.get(0);
    assertNotNull(task.getId());
    assertEquals(processInstance.getId(), task.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), task.getProcessDefinitionId());
    assertEquals("externalTask", task.getActivityId());
    assertEquals("oneExternalTaskProcess", task.getProcessDefinitionKey());
  }

  protected void startProcessAndExecuteJob(String processDefinitionKey) {
    runtimeService.startProcessInstanceByKey(processDefinitionKey);
    executeAvailableJobs(processDefinitionKey);
  }

  protected void executeAvailableJobs(final String key) {
    List<Job> jobs = managementService.createJobQuery().processDefinitionKey(key).withRetriesLeft().list();

    if (jobs.isEmpty()) {
      return;
    }

    for (Job job : jobs) {
      try {
        managementService.executeJob(job.getId());
      } catch (Exception e) {
      }
    }

    executeAvailableJobs(key);
    return;
  }

}
