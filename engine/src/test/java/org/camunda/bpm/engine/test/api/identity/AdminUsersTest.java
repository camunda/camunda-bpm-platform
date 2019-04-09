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
package org.camunda.bpm.engine.test.api.identity;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.USER;

public class AdminUsersTest {

  protected ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule();

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(bootstrapRule).around(engineRule).around(testRule);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;
  protected ManagementService managementService;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    identityService = engineRule.getIdentityService();
    authorizationService = engineRule.getAuthorizationService();
    managementService = engineRule.getManagementService();
  }

  @After
  public void tearDown() throws Exception {
    processEngineConfiguration.setAuthorizationEnabled(false);
    cleanupAfterTest();
  }

  protected void cleanupAfterTest() {
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

  @Test
  public void testWithoutAdminUser() {
    processEngineConfiguration.setAuthorizationEnabled(false);

    identityService.setAuthentication("adminUser", null, null);
    Authorization userAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    userAuth.setUserId("adminUser");
    userAuth.setResource(USER);
    userAuth.setResourceId(ANY);
    userAuth.addPermission(READ);
    authorizationService.saveAuthorization(userAuth);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // then
    thrown.expect(AuthorizationException.class);
    thrown.expectMessage("Required admin authenticated group or user.");

    // when
    managementService.getProperties();
  }

  @Test
  public void testWithAdminUser() {
    processEngineConfiguration.getAdminUsers().add("adminUser");

    processEngineConfiguration.setAuthorizationEnabled(false);

    identityService.setAuthentication("adminUser", null, null);
    Authorization userAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    userAuth.setUserId("adminUser");
    userAuth.setResource(USER);
    userAuth.setResourceId(ANY);
    userAuth.addPermission(READ);
    authorizationService.saveAuthorization(userAuth);
    processEngineConfiguration.setAuthorizationEnabled(true);

    // when
    managementService.getProperties();

    // then no exception
  }
}
