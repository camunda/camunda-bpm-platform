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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GLOBAL;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_REVOKE;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;

/**
 * @author Roman Smirnov
 */
public abstract class AuthorizationTest extends PluggableProcessEngineTest {

  protected String userId = "test";
  protected String groupId = "accounting";
  protected User user;
  protected Group group;

  protected static final String VARIABLE_NAME = "aVariableName";
  protected static final String VARIABLE_VALUE = "aVariableValue";
  protected List<String> deploymentIds = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    user = createUser(userId);
    group = createGroup(groupId);

    identityService.createMembership(userId, groupId);

    identityService.setAuthentication(userId, Arrays.asList(groupId));
    processEngineConfiguration.setAuthorizationEnabled(true);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setAuthorizationEnabled(false);
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
    for (String deploymentId : deploymentIds) {
      deleteDeployment(deploymentId);
    }
  }

  protected <T> T runWithoutAuthorization(Callable<T> runnable) {
    boolean authorizationEnabled = processEngineConfiguration.isAuthorizationEnabled();
    try {
      disableAuthorization();
      return runnable.call();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (authorizationEnabled) {
        enableAuthorization();
      }
    }
  }

  protected String permissionException(Resource resource, Permission permission) {
    return "ENGINE-03110 Required admin authenticated group or user or any of the following permissions: '"
        + permission.getName() + "' permission on resource '" + resource.resourceName() + "'";
  }

  // user ////////////////////////////////////////////////////////////////

  protected User createUser(String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);

    // give user all permission to manipulate authorizations
    Authorization authorization = createGrantAuthorization(AUTHORIZATION, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(ALL);
    saveAuthorization(authorization);

    // give user all permission to manipulate users
    authorization = createGrantAuthorization(USER, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(Permissions.ALL);
    saveAuthorization(authorization);

    return user;
  }

  // group //////////////////////////////////////////////////////////////

  protected Group createGroup(final String groupId) {
    return runWithoutAuthorization(() -> {
      Group group = identityService.newGroup(groupId);
      identityService.saveGroup(group);
      return group;
    });
  }

  // authorization ///////////////////////////////////////////////////////

  protected Authorization createGrantAuthorization(Resource resource, String resourceId,
                                                   String userId, Permission... permissions) {
    Authorization authorization = createGrantAuthorization(resource, resourceId);
    authorization.setUserId(userId);
    for (Permission permission : permissions) {
      authorization.addPermission(permission);
    }
    saveAuthorization(authorization);
    return authorization;
  }

  protected Authorization createGrantAuthorizationWithoutAuthentication(Resource resource,
                                                                        String resourceId,
                                                                        String userId,
                                                                        Permission... permissions) {
    Authentication currentAuthentication = identityService.getCurrentAuthentication();
    identityService.clearAuthentication();
    try {
      return createGrantAuthorization(resource, resourceId, userId, permissions);
    } finally {
      identityService.setAuthentication(currentAuthentication);
    }
  }

  protected void createGrantAuthorizationGroup(Resource resource, String resourceId,
                                               String groupId, Permission... permissions) {
    Authorization authorization = createGrantAuthorization(resource, resourceId);
    authorization.setGroupId(groupId);
    for (Permission permission : permissions) {
      authorization.addPermission(permission);
    }
    saveAuthorization(authorization);
  }

  protected void createRevokeAuthorizationWithoutAuthentication(Resource resource,
                                                                String resourceId, String userId,
                                                                Permission... permissions) {
    Authentication currentAuthentication = identityService.getCurrentAuthentication();
    identityService.clearAuthentication();
    try {
      createRevokeAuthorization(resource, resourceId, userId, permissions);
    } finally {
      identityService.setAuthentication(currentAuthentication);
    }
  }

  protected void createRevokeAuthorization(Resource resource, String resourceId, String userId,
                                           Permission... permissions) {
    Authorization authorization = createRevokeAuthorization(resource, resourceId);
    authorization.setUserId(userId);
    for (Permission permission : permissions) {
      authorization.removePermission(permission);
    }
    saveAuthorization(authorization);
  }

  protected Authorization createGlobalAuthorization(Resource resource, String resourceId) {
    Authorization authorization = createAuthorization(AUTH_TYPE_GLOBAL, resource, resourceId);
    return authorization;
  }

  protected Authorization createGrantAuthorization(Resource resource, String resourceId) {
    Authorization authorization = createAuthorization(AUTH_TYPE_GRANT, resource, resourceId);
    return authorization;
  }

  protected Authorization createRevokeAuthorization(Resource resource, String resourceId) {
    Authorization authorization = createAuthorization(AUTH_TYPE_REVOKE, resource, resourceId);
    return authorization;
  }

  protected Authorization createAuthorization(int type, Resource resource, String resourceId) {
    Authorization authorization = authorizationService.createNewAuthorization(type);

    authorization.setResource(resource);
    if (resourceId != null) {
      authorization.setResourceId(resourceId);
    }

    return authorization;
  }

  protected void saveAuthorization(Authorization authorization) {
    authorizationService.saveAuthorization(authorization);
  }

  // enable/disable authorization //////////////////////////////////////////////

  protected void enableAuthorization() {
    processEngineConfiguration.setAuthorizationEnabled(true);
  }

  protected void disableAuthorization() {
    processEngineConfiguration.setAuthorizationEnabled(false);
  }

  // actions (executed without authorization) ///////////////////////////////////

  protected ProcessInstance startProcessInstanceByKey(String key) {
    return startProcessInstanceByKey(key, null);
  }

  protected ProcessInstance startProcessInstanceByKey(final String key,
                                                      final Map<String, Object> variables) {
    return runWithoutAuthorization(() -> runtimeService.startProcessInstanceByKey(key, variables));
  }

  public void executeAvailableJobs() {
    runWithoutAuthorization((Callable<Void>) () -> {
      testRule.executeAvailableJobs();
      return null;
    });
  }

  protected CaseInstance createCaseInstanceByKey(final String key,
                                                 final Map<String, Object> variables) {
    return runWithoutAuthorization(() -> caseService.createCaseInstanceByKey(key, variables));
  }

  protected void createTask(final String taskId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      Task task = taskService.newTask(taskId);
      taskService.saveTask(task);
      return null;
    });
  }

  protected void deleteTask(final String taskId, final boolean cascade) {
    runWithoutAuthorization((Callable<Void>) () -> {
      taskService.deleteTask(taskId, cascade);
      return null;
    });
  }

  protected void addCandidateUser(final String taskId, final String user) {
    runWithoutAuthorization((Callable<Void>) () -> {
      taskService.addCandidateUser(taskId, user);
      return null;
    });
  }

  protected void addCandidateGroup(final String taskId, final String group) {
    runWithoutAuthorization((Callable<Void>) () -> {
      taskService.addCandidateGroup(taskId, group);
      return null;
    });
  }

  protected void setAssignee(final String taskId, final String userId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      taskService.setAssignee(taskId, userId);
      return null;
    });
  }

  protected void delegateTask(final String taskId, final String userId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      taskService.delegateTask(taskId, userId);
      return null;
    });
  }

  protected Task selectSingleTask() {
    return runWithoutAuthorization(() -> taskService.createTaskQuery().singleResult());
  }

  protected void setTaskVariables(final String taskId,
                                  final Map<String, ? extends Object> variables) {
    runWithoutAuthorization((Callable<Void>) () -> {
      taskService.setVariables(taskId, variables);
      return null;
    });
  }

  protected void setTaskVariablesLocal(final String taskId,
                                       final Map<String, ? extends Object> variables) {
    runWithoutAuthorization((Callable<Void>) () -> {
      taskService.setVariablesLocal(taskId, variables);
      return null;
    });
  }

  protected void setTaskVariable(final String taskId, final String name, final Object value) {
    runWithoutAuthorization((Callable<Void>) () -> {
      taskService.setVariable(taskId, name, value);
      return null;
    });
  }

  protected void setTaskVariableLocal(final String taskId, final String name, final Object value) {
    runWithoutAuthorization((Callable<Void>) () -> {
      taskService.setVariableLocal(taskId, name, value);
      return null;
    });
  }

  protected void setExecutionVariable(final String executionId, final String name,
                                      final Object value) {
    runWithoutAuthorization((Callable<Void>) () -> {
      runtimeService.setVariable(executionId, name, value);
      return null;
    });
  }

  protected void setExecutionVariableLocal(final String executionId, final String name,
                                           final Object value) {
    runWithoutAuthorization((Callable<Void>) () -> {
      runtimeService.setVariableLocal(executionId, name, value);
      return null;
    });
  }

  protected void setCaseVariable(final String caseExecution, final String name,
                                 final Object value) {
    runWithoutAuthorization((Callable<Void>) () -> {
      caseService.setVariable(caseExecution, name, value);
      return null;
    });
  }

  protected void setCaseVariableLocal(final String caseExecution, final String name,
                                      final Object value) {
    runWithoutAuthorization((Callable<Void>) () -> {
      caseService.setVariableLocal(caseExecution, name, value);
      return null;
    });
  }

  protected ProcessDefinition selectProcessDefinitionByKey(final String processDefinitionKey) {
    return runWithoutAuthorization(() -> repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .singleResult());
  }

  protected ProcessInstance selectSingleProcessInstance() {
    return runWithoutAuthorization(() -> runtimeService
        .createProcessInstanceQuery()
        .singleResult());
  }

  protected void suspendProcessDefinitionByKey(final String processDefinitionKey) {
    runWithoutAuthorization((Callable<Void>) () -> {
      repositoryService.suspendProcessDefinitionByKey(processDefinitionKey);
      return null;
    });
  }

  protected void suspendProcessDefinitionById(final String processDefinitionId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      repositoryService.suspendProcessDefinitionById(processDefinitionId);
      return null;
    });
  }

  protected void suspendProcessInstanceById(final String processInstanceId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      runtimeService.suspendProcessInstanceById(processInstanceId);
      return null;
    });
  }

  protected void suspendJobById(final String jobId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobById(jobId);
      return null;
    });
  }

  protected void suspendJobByProcessInstanceId(final String processInstanceId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobByProcessInstanceId(processInstanceId);
      return null;
    });
  }

  protected void suspendJobByJobDefinitionId(final String jobDefinitionId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobByJobDefinitionId(jobDefinitionId);
      return null;
    });
  }

  protected void suspendJobByProcessDefinitionId(final String processDefinitionId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobByProcessDefinitionId(processDefinitionId);
      return null;
    });
  }

  protected void suspendJobByProcessDefinitionKey(final String processDefinitionKey) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobByProcessDefinitionKey(processDefinitionKey);
      return null;
    });
  }

  protected void suspendJobDefinitionById(final String jobDefinitionId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobDefinitionById(jobDefinitionId);
      return null;
    });
  }

  protected void suspendJobDefinitionByProcessDefinitionId(final String processDefinitionId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobDefinitionByProcessDefinitionId(processDefinitionId);
      return null;
    });
  }

  protected void suspendJobDefinitionByProcessDefinitionKey(final String processDefinitionKey) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinitionKey);
      return null;
    });
  }

  protected void suspendJobDefinitionIncludingJobsById(final String jobDefinitionId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobDefinitionById(jobDefinitionId, true);
      return null;
    });
  }

  protected void suspendJobDefinitionIncludingJobsByProcessDefinitionId(final String processDefinitionId) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobDefinitionByProcessDefinitionId(processDefinitionId, true);
      return null;
    });
  }

  protected void suspendJobDefinitionIncludingJobsByProcessDefinitionKey(final String processDefinitionKey) {
    runWithoutAuthorization((Callable<Void>) () -> {
      managementService.suspendJobDefinitionByProcessDefinitionKey(processDefinitionKey, true);
      return null;
    });
  }

  protected Deployment createDeployment(final String name, final String... resources) {
    return runWithoutAuthorization(() -> {
      DeploymentBuilder builder = repositoryService.createDeployment();
      for (String resource : resources) {
        builder.addClasspathResource(resource);
      }
      Deployment deployment = builder.deploy();
      deploymentIds.add(deployment.getId());
      return deployment;
    });
  }

  protected void deleteDeployment(String deploymentId) {
    deleteDeployment(deploymentId, true);
  }

  protected void deleteDeployment(final String deploymentId, final boolean cascade) {
    Authentication authentication = identityService.getCurrentAuthentication();
    try {
      identityService.clearAuthentication();
      runWithoutAuthorization((Callable<Void>) () -> {
        repositoryService.deleteDeployment(deploymentId, cascade);
        return null;
      });
    } finally {
      if (authentication != null) {
        identityService.setAuthentication(authentication);
      }
    }
  }

  protected ProcessInstance startProcessAndExecuteJob(final String key) {
    return runWithoutAuthorization(() -> {
      ProcessInstance processInstance = startProcessInstanceByKey(key);
      executeAvailableJobs(key);
      return processInstance;
    });
  }

  protected void executeAvailableJobs(final String key) {
    runWithoutAuthorization((Callable<Void>) () -> {
      List<Job> jobs =
          managementService.createJobQuery().processDefinitionKey(key).withRetriesLeft().list();

      if (jobs.isEmpty()) {
        enableAuthorization();
        return null;
      }

      for (Job job : jobs) {
        try {
          managementService.executeJob(job.getId());
        } catch (Exception e) {
        }
      }

      executeAvailableJobs(key);
      return null;
    });
  }

  protected DecisionDefinition selectDecisionDefinitionByKey(final String decisionDefinitionKey) {
    return runWithoutAuthorization(() -> repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(decisionDefinitionKey)
        .singleResult());
  }

  // verify query results ////////////////////////////////////////////////////////

  protected void verifyQueryResults(Query<?, ?> query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1) {
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  protected void verifySingleResultFails(Query<?, ?> query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {
    }
  }

  public Permission getDefaultTaskPermissionForUser() {
    // get the default task assignee permission
    ProcessEngineConfigurationImpl processEngineConfiguration =
        (ProcessEngineConfigurationImpl) processEngine
            .getProcessEngineConfiguration();

    return processEngineConfiguration.getDefaultUserPermissionForTask();
  }

  // helper ////////////////////////////////////////////////////////////////////

  protected VariableMap getVariables() {
    return Variables.createVariables().putValue(VARIABLE_NAME, VARIABLE_VALUE);
  }

  protected String getMissingPermissionMessageRegex(Permission permission, Resource resource) {
    return ".*'"+ permission.getName() + "' permission .* type '" + resource.resourceName() + "'.*";
  }
}
