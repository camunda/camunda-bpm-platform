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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AuthorizationQueryAuthorizationsTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected AuthorizationService authorizationService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    authorizationService = engineRule.getAuthorizationService();
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setAuthorizationEnabled(false);
    cleanupAfterTest();
  }

  @Test
  public void testQuerySingleCorrectPermission() throws Exception {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("userId");
    authorization.setResource(Resources.PROCESS_DEFINITION);
    authorization.addPermission(Permissions.READ);
    authorization.setResourceId(ANY);
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);

    // assume
    Authorization authResult = authorizationService.createAuthorizationQuery().userIdIn("userId").resourceType(Resources.PROCESS_DEFINITION).singleResult();
    assertNotNull(authResult);

    // then
    assertEquals(1, authorizationService.createAuthorizationQuery().hasPermission(Permissions.READ).count());
  }

  @Test
  public void testQuerySingleIncorrectPermission() throws Exception {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("userId");
    authorization.setResource(Resources.BATCH);
    authorization.addPermission(BatchPermissions.CREATE_BATCH_DELETE_RUNNING_PROCESS_INSTANCES);
    authorization.setResourceId(ANY);
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);

    // assume
    Authorization authResult = authorizationService.createAuthorizationQuery().userIdIn("userId").resourceType(Resources.BATCH).singleResult();
    assertNotNull(authResult);

    // then
    assertEquals(0, authorizationService.createAuthorizationQuery().hasPermission(Permissions.CREATE_INSTANCE).count());
  }

  @Test
  public void testQueryPermissionsWithWrongResource() throws Exception {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("userId");
    authorization.setResource(Resources.APPLICATION);
    authorization.addPermission(Permissions.ACCESS);
    authorization.setResourceId(ANY);
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);

    // assume
    Authorization authResult = authorizationService.createAuthorizationQuery().userIdIn("userId").resourceType(Resources.APPLICATION).singleResult();
    assertNotNull(authResult);

    // when
    Authorization accessResult = authorizationService.createAuthorizationQuery()
        .hasPermission(Permissions.ACCESS)
        .singleResult();
    List<Authorization> retryJobPDResult = authorizationService.createAuthorizationQuery()
        .hasPermission(ProcessDefinitionPermissions.RETRY_JOB)
        .list();
    List<Authorization> retryJobPIResult = authorizationService.createAuthorizationQuery()
        .hasPermission(ProcessInstancePermissions.RETRY_JOB)
        .list();

    // then
    assertNotNull(accessResult);
    assertEquals(1, authorizationService.createAuthorizationQuery().hasPermission(Permissions.ACCESS).count());
    assertTrue(retryJobPDResult.isEmpty());
    assertEquals(0, authorizationService.createAuthorizationQuery().hasPermission(ProcessDefinitionPermissions.RETRY_JOB).count());
    assertTrue(retryJobPIResult.isEmpty());
    assertEquals(0, authorizationService.createAuthorizationQuery().hasPermission(ProcessInstancePermissions.RETRY_JOB).count());
  }

  @Test
  public void testQueryPermissionWithMixedResource() throws Exception {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("userId");
    authorization.setResource(Resources.APPLICATION);
    authorization.addPermission(Permissions.ACCESS);
    authorization.setResourceId(ANY);
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);

    // assume
    Authorization authResult = authorizationService.createAuthorizationQuery().userIdIn("userId").resourceType(Resources.APPLICATION).singleResult();
    assertNotNull(authResult);

    // then
    assertEquals(0, authorizationService.createAuthorizationQuery()
        .resourceType(Resources.BATCH)
        .hasPermission(Permissions.ACCESS)
        .count());
  }

  @Test
  public void testQueryPermissionsWithMixedResource() throws Exception {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("userId");
    authorization.setResource(Resources.PROCESS_DEFINITION);
    authorization.addPermission(Permissions.READ);
    authorization.addPermission(ProcessDefinitionPermissions.RETRY_JOB);
    authorization.setResourceId(ANY);
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);

    // assume
    Authorization authResult = authorizationService.createAuthorizationQuery().userIdIn("userId").resourceType(Resources.PROCESS_DEFINITION).singleResult();
    assertNotNull(authResult);
    assertEquals(1, authorizationService.createAuthorizationQuery()
        .resourceType(Resources.PROCESS_DEFINITION)
        .hasPermission(ProcessDefinitionPermissions.READ)
        .hasPermission(ProcessDefinitionPermissions.RETRY_JOB)
        .count());
    assertEquals(1, authorizationService.createAuthorizationQuery()
        .resourceType(Resources.PROCESS_DEFINITION)
        .hasPermission(ProcessDefinitionPermissions.READ)
        .count());

    // then
    assertEquals(0, authorizationService.createAuthorizationQuery()
        .resourceType(Resources.PROCESS_DEFINITION)
        .hasPermission(Permissions.READ)
        .hasPermission(Permissions.ACCESS)
        .count());
  }

  @Test
  public void testQueryCorrectAndIncorrectPersmission() throws Exception {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("userId");
    authorization.setResource(Resources.PROCESS_DEFINITION);
    authorization.addPermission(Permissions.READ);
    authorization.addPermission(ProcessDefinitionPermissions.RETRY_JOB);
    authorization.setResourceId(ANY);
    authorizationService.saveAuthorization(authorization);

    processEngineConfiguration.setAuthorizationEnabled(true);

    // assume
    Authorization authResult = authorizationService.createAuthorizationQuery().userIdIn("userId").resourceType(Resources.PROCESS_DEFINITION).singleResult();
    assertNotNull(authResult);

    // then
    assertEquals(0, authorizationService.createAuthorizationQuery()
        .hasPermission(Permissions.READ)
        .hasPermission(Permissions.ACCESS)
        .count());
  }

  protected void cleanupAfterTest() {
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }
}
