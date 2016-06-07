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
package org.camunda.bpm.engine.test.api.task;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskCountByCandidateGroupResult;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Daniel Meyer
 * @author Stefan Hentschel
 *
 */
public class TaskCountByCandidateGroupsTest {

  public ProcessEngineRule processEngineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule processEngineTestRule = new ProcessEngineTestRule(processEngineRule);

  @Rule
  public RuleChain ruleChain = RuleChain
                                  .outerRule(processEngineTestRule)
                                  .around(processEngineRule);


  protected TaskService taskService;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  protected String userId = "user";
  protected List<String> tasks = new ArrayList<String>();
  protected List<String> tenants = Arrays.asList("tenant1", "tenant2");
  protected List<String> groups = Arrays.asList("aGroupId", "anotherGroupId");


  @Before
  public void setUp() {
    taskService = processEngineRule.getTaskService();
    identityService = processEngineRule.getIdentityService();
    authorizationService = processEngineRule.getAuthorizationService();
    processEngineConfiguration = processEngineRule.getProcessEngineConfiguration();

    createTask(groups.get(0), tenants.get(0));
    createTask(groups.get(0), tenants.get(1));
    createTask(groups.get(1), tenants.get(1));
    createTask(null, tenants.get(1));
  }

  @After
  public void cleanUp() {
    processEngineConfiguration.setTenantCheckEnabled(false);

    authenticateWithMultipleTenants();
    for( String taskId : tasks ) {
      taskService.deleteTask(taskId, true);
    }

    processEngineConfiguration.setTenantCheckEnabled(true);
  }

  @Test
  public void shouldReturnTaskCountsByGroup() {
    // given
    authenticateWithMultipleTenants();

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();

    // then
    assertEquals(3, results.size());
  }

  @Test
  public void shouldProvideTaskCountForEachGroup() {
    // given
    authenticateWithMultipleTenants();

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();

    // then
    assertEquals(1, results.get(0).getTaskCount());
    assertEquals(2, results.get(1).getTaskCount());
    assertEquals(1, results.get(2).getTaskCount());
  }

  @Test
  public void shouldProvideGroupNameForEachGroup() {
    // given
    authenticateWithMultipleTenants();

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();

    // then
    assertEquals(null, results.get(0).getGroupName());
    assertEquals(groups.get(0), results.get(1).getGroupName());
    assertEquals(groups.get(1), results.get(2).getGroupName());
  }

  @Test
  public void shouldDecreaseTaskCountOnComplete() {
    // given
    authenticateWithMultipleTenants();
    taskService.complete(tasks.get(1));

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();

    // then
    assertEquals(1, results.get(0).getTaskCount());
    assertEquals(1, results.get(1).getTaskCount());
    assertEquals(1, results.get(2).getTaskCount());
  }

  @Test
  public void shouldRemoveGroupNameOnComplete() {
    // given
    authenticateWithMultipleTenants();
    taskService.complete(tasks.get(2));

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();

    // then
    assertEquals(2, results.size());
  }

  @Test
  public void shouldDecreaseTaskCountOnDelete() {
    // given
    authenticateWithMultipleTenants();
    taskService.deleteTask(tasks.get(1), true);

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();

    // then
    assertEquals(1, results.get(0).getTaskCount());
    assertEquals(1, results.get(1).getTaskCount());
    assertEquals(1, results.get(2).getTaskCount());
  }

  @Test
  public void shouldRemoveGroupNameOnDelete() {
    // given
    authenticateWithMultipleTenants();
    taskService.deleteTask(tasks.get(2), true);

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();

    // then
    assertEquals(2, results.size());
  }

  @Test
  public void shouldOnlyShowTenantSpecificTasks() {
    // given
    authenticateWithSingleTenant();

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();

    // then
    assertEquals(1, results.size());
    assertEquals(groups.get(0), results.get(0).getGroupName());
  }

  @Test
  public void shouldFetchTaskCountWithAuthorization() {
    // given
    User user = identityService.newUser(userId);
    identityService.saveUser(user);

    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.addPermission(READ);
    authorization.setResource(TASK);
    authorization.setResourceId(ANY);
    authorization.setUserId(userId);
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);
    authenticateWithMultipleTenants();

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();
    processEngineConfiguration.setAuthorizationEnabled(false);
    authorizationService.deleteAuthorization(authorization.getId());
    identityService.deleteUser(userId);

    assertEquals(3, results.size());
  }

  @Test
  public void shouldFailToFetchTaskCountWithMissingAuthorization() {
    // given
    boolean testFailed = false;
    processEngineConfiguration.setAuthorizationEnabled(true);
    authenticateWithMultipleTenants();

    // when
    try {
      taskService.createTaskReport().taskCountByCandidateGroup();
      testFailed = true;

    } catch( AuthorizationException aex ) {
      if(!aex.getMessage().contains(userId + "' does not have 'READ' permission on resource '*' of type 'Task'")) {
        testFailed = true;
      }
    }

  // then
  processEngineConfiguration.setAuthorizationEnabled(false);

  if( testFailed ) {
    fail("There should be an authorization exception for '" + userId + "' because of a missing 'READ' permission on 'Task'.");
  }
}

  protected void createTask(String groupId, String tenantId) {
    Task task = taskService.newTask();
    task.setTenantId(tenantId);
    taskService.saveTask(task);

    if( groupId != null ) {
      taskService.addCandidateGroup(task.getId(), groupId);
    }

    tasks.add(task.getId());
  }

  protected void authenticateWithSingleTenant() {
    identityService.setAuthentication(userId, null, tenants.subList(0, 1));
  }

  protected void authenticateWithMultipleTenants() {
    identityService.setAuthentication(userId, null, tenants);
  }
}
