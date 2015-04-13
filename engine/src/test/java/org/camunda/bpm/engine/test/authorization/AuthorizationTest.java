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
package org.camunda.bpm.engine.test.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GLOBAL;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_REVOKE;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.util.Arrays;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Roman Smirnov
 *
 */
public abstract class AuthorizationTest extends PluggableProcessEngineTestCase {

  protected String userId = "test";
  protected String groupId = "accounting";
  protected User user;
  protected Group group;

  protected void initializeProcessEngine() {
    super.initializeProcessEngine();

    processEngineConfiguration = (ProcessEngineConfigurationImpl) getProcessEngine()
        .getProcessEngineConfiguration();

    identityService = processEngineConfiguration.getIdentityService();
    authorizationService = processEngineConfiguration.getAuthorizationService();

    user = createUser(userId);
    group = createGroup(groupId);

    identityService.createMembership(userId, groupId);

    identityService.setAuthentication(userId, Arrays.asList(groupId));
    processEngineConfiguration.setAuthorizationEnabled(true);
  }

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

  protected Group createGroup(String groupId) {
    Group group = identityService.newGroup(groupId);
    identityService.saveGroup(group);;
    return group;
  }

  // authorization ///////////////////////////////////////////////////////

  protected void createGrantAuthorization(Resource resource, String resourceId, String userId, Permission... permissions) {
    Authorization authorization = createGrantAuthorization(resource, resourceId);
    authorization.setUserId(userId);
    for (Permission permission : permissions) {
      authorization.addPermission(permission);
    }
    saveAuthorization(authorization);
  }

  protected void createRevokeAuthorization(Resource resource, String resourceId, String userId, Permission... permissions) {
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

  protected ProcessInstance startProcessInstanceByKey(String key, Map<String, Object> variables) {
    disableAuthorization();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, variables);
    enableAuthorization();
    return processInstance;
  }

  public void executeAvailableJobs() {
    disableAuthorization();
    super.executeAvailableJobs();
    enableAuthorization();
  }

  protected CaseInstance createCaseInstanceByKey(String key) {
    return createCaseInstanceByKey(key, null);
  }

  protected CaseInstance createCaseInstanceByKey(String key, Map<String, Object> variables) {
    disableAuthorization();
    CaseInstance caseInstance = caseService.createCaseInstanceByKey(key, variables);
    enableAuthorization();
    return caseInstance;
  }

  protected void createTask(String taskId) {
    disableAuthorization();
    Task task = taskService.newTask(taskId);
    taskService.saveTask(task);
    enableAuthorization();
  }

  protected void deleteTask(String taskId, boolean cascade) {
    disableAuthorization();
    taskService.deleteTask(taskId, cascade);
    enableAuthorization();
  }

  protected void addCandidateUser(String taskId, String user) {
    disableAuthorization();
    taskService.addCandidateUser(taskId, user);
    enableAuthorization();
  }

  protected void addCandidateGroup(String taskId, String group) {
    disableAuthorization();
    taskService.addCandidateGroup(taskId, group);
    enableAuthorization();
  }

  protected void setAssignee(String taskId, String userId) {
    disableAuthorization();
    taskService.setAssignee(taskId, userId);
    enableAuthorization();
  }

  protected void delegateTask(String taskId, String userId) {
    disableAuthorization();
    taskService.delegateTask(taskId, userId);
    enableAuthorization();
  }

  protected Task selectSingleTask() {
    disableAuthorization();
    Task task = taskService.createTaskQuery().singleResult();
    enableAuthorization();
    return task;
  }

  protected void setTaskVariable(String taskId, String name, Object value) {
    disableAuthorization();
    taskService.setVariable(taskId, name, value);
    enableAuthorization();
  }

  protected void setTaskVariableLocal(String taskId, String name, Object value) {
    disableAuthorization();
    taskService.setVariableLocal(taskId, name, value);
    enableAuthorization();
  }

  protected void setExecutionVariable(String executionId, String name, Object value) {
    disableAuthorization();
    runtimeService.setVariable(executionId, name, value);
    enableAuthorization();
  }

  protected void setExecutionVariableLocal(String executionId, String name, Object value) {
    disableAuthorization();
    runtimeService.setVariableLocal(executionId, name, value);
    enableAuthorization();
  }

  protected void setCaseVariable(String caseExecution, String name, Object value) {
    disableAuthorization();
    caseService.setVariable(caseExecution, name, value);
    enableAuthorization();
  }

  protected void setCaseVariableLocal(String caseExecution, String name, Object value) {
    disableAuthorization();
    caseService.setVariableLocal(caseExecution, name, value);
    enableAuthorization();
  }

  protected ProcessDefinition selectProcessDefinitionByKey(String processDefinitionKey) {
    disableAuthorization();
    ProcessDefinition definition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .singleResult();
    enableAuthorization();
    return definition;
  }

  protected ProcessInstance selectSingleProcessInstance() {
    disableAuthorization();
    ProcessInstance instance = runtimeService
        .createProcessInstanceQuery()
        .singleResult();
    enableAuthorization();
    return instance;
  }

  protected void suspendProcessInstanceById(String processInstanceId) {
    disableAuthorization();
    runtimeService.suspendProcessInstanceById(processInstanceId);
    enableAuthorization();
  }

  // verify query results ////////////////////////////////////////////////////////

  protected void verifyQueryResults(AbstractQuery<?, ?> query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  protected void verifySingleResultFails(AbstractQuery<?, ?> query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

}
