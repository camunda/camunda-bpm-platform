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
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.task.TaskCountByCandidateGroupResult;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Stefan Hentschel.
 */
public class TaskCountByCandidateGroupAuthorizationTest {

  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();
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

  @Before
  public void setUp() {
    taskService = processEngineRule.getTaskService();
    identityService = processEngineRule.getIdentityService();
    authorizationService = processEngineRule.getAuthorizationService();
    processEngineConfiguration = processEngineRule.getProcessEngineConfiguration();
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
    authenticate();

    // when
    List<TaskCountByCandidateGroupResult> results = taskService.createTaskReport().taskCountByCandidateGroup();
    processEngineConfiguration.setAuthorizationEnabled(false);
    authorizationService.deleteAuthorization(authorization.getId());
    identityService.deleteUser(userId);

    assertEquals(0, results.size());
  }

  @Test
  public void shouldFailToFetchTaskCountWithMissingAuthorization() {
    // given
    boolean testFailed = false;
    processEngineConfiguration.setAuthorizationEnabled(true);
    authenticate();

    // when
    try {
      taskService.createTaskReport().taskCountByCandidateGroup();
      testFailed = true;

    } catch (AuthorizationException aex) {
      if (!aex.getMessage().contains(userId + "' does not have 'READ' permission on resource '*' of type 'Task'")) {
        testFailed = true;
      }
    }

    // then
    processEngineConfiguration.setAuthorizationEnabled(false);

    if (testFailed) {
      fail("There should be an authorization exception for '" + userId + "' because of a missing 'READ' permission on 'Task'.");
    }
  }

  protected void authenticate() {
    identityService.setAuthentication(userId, null, null);
  }
}
