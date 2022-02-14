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
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.OptimizePermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestBaseRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class OptimizeAuthorizationTest {

  protected static final String USER_ID = "user";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestBaseRule authRule = new AuthorizationTestBaseRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule);

  ProcessEngineConfigurationImpl processEngineConfiguration;
  AuthorizationService authorizationService;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    authorizationService = engineRule.getAuthorizationService();
  }

  @Test
  public void testOptimizePermissionExists() {
    // given
    authRule.createGrantAuthorization(Resources.OPTIMIZE, ANY, USER_ID, OptimizePermissions.ALL);

    // when
    authRule.enableAuthorization(USER_ID);

    // then
    assertTrue(authorizationService.isUserAuthorized(USER_ID, null, OptimizePermissions.EDIT, Resources.OPTIMIZE));
    assertTrue(authorizationService.isUserAuthorized(USER_ID, null, OptimizePermissions.SHARE, Resources.OPTIMIZE));
  }

  @After
  public void tearDown() {
    authRule.disableAuthorization();
    authRule.deleteUsersAndGroups();
  }
}
