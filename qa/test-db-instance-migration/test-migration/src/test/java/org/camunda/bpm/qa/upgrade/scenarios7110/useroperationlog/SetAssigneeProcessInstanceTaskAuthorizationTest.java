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
package org.camunda.bpm.qa.upgrade.scenarios7110.useroperationlog;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Yana.Vasileva
 *
 */
public class SetAssigneeProcessInstanceTaskAuthorizationTest {

  private static final String USER_ID = "jane" + "SetAssigneeProcessInstanceTask";

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  protected HistoryService historyService;
  protected AuthorizationService authorizationService;
  protected ProcessEngineConfigurationImpl engineConfiguration;

  @Before
  public void assignServices() {
    historyService = engineRule.getHistoryService();
    authorizationService = engineRule.getAuthorizationService();
    engineConfiguration = engineRule.getProcessEngineConfiguration();
    engineRule.getIdentityService().setAuthenticatedUserId(USER_ID);
  }

  @After
  public void tearDown() {
    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(false);
    engineRule.getIdentityService().clearAuthentication();
    List<Authorization> auths = authorizationService.createAuthorizationQuery().userIdIn(USER_ID).list();
    for (Authorization authorization : auths) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

  @Test
  public void testWithoutAuthorization() {
    // given
    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(true);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery().processDefinitionKey("oneTaskProcess_userOpLog");

    // then
    assertEquals(0, query.count());
  }

  @Test
  public void testWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    Authorization auth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    auth.setUserId(USER_ID);
    auth.setPermissions(new Permissions[] {Permissions.READ_HISTORY});
    auth.setResource(Resources.PROCESS_DEFINITION);
    auth.setResourceId("*");

    authorizationService.saveAuthorization(auth);
    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(true);
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery().processDefinitionKey("oneTaskProcess_userOpLog");

    // then
    assertEquals(1, query.count());
  }

  @Test
  public void testWithReadHistoryPermissionOnProcessDefinition() {
    // given
    Authorization auth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    auth.setUserId(USER_ID);
    auth.setPermissions(new Permissions[] {Permissions.READ_HISTORY});
    auth.setResource(Resources.PROCESS_DEFINITION);
    auth.setResourceId("oneTaskProcess_userOpLog");

    authorizationService.saveAuthorization(auth);
    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(true);
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery().processDefinitionKey("oneTaskProcess_userOpLog");

    // then
    assertEquals(1, query.count());
  }
}
