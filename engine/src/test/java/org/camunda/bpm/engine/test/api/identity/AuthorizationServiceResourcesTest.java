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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GLOBAL;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_REVOKE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ResourceTypeUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AuthorizationServiceResourcesTest {

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl configuration;
  protected AuthorizationService authorizationService;
  protected String userId = "testUser";

  @Parameter(0)
  public Resource resource;

  @Parameter(1)
  public Permission permission;


  @Before
  public void setup() {
    configuration = rule.getProcessEngineConfiguration();
    authorizationService = rule.getAuthorizationService();
    configuration.setAuthorizationEnabled(true);
    configuration.setEnableHistoricInstancePermissions(true);
  }

  @After
  public void tearDown() {
    configuration.setAuthorizationEnabled(false);
    configuration.setEnableHistoricInstancePermissions(false);
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

  @Parameters(name = "{0} {1}")
  public static Collection<Object[]> parameters() {
    List<Object[]> parameters = new ArrayList<>();
    for (Resources resource : Resources.values()) {
      Class<? extends Enum<? extends Permission>> permissionClass = ResourceTypeUtil.getPermissionEnums().get(resource.resourceType());
      // if the resource type is not contained in the permissions enum it's a first sign that the enum is not complete
      // in that case PermissionsTest#testThatPermissionsEnumContainsAllPermissions should fail
      if (permissionClass != null) {
        Object[] permissionsForResource = Stream.of(permissionClass.getEnumConstants()).filter(p -> Arrays.asList(((Permission) p).getTypes()).contains(resource)).toArray();
        for (Object permissionForResource : permissionsForResource) {
          Object[] param = { resource, permissionForResource };
          parameters.add(param);
        }
      }
    }
    return parameters;

  }

  @Test
  public void testGrantAuthPermissions() {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId(userId);
    authorization.addPermission(permission);
    authorization.setResource(resource);
    authorization.setResourceId(ANY);

    // when
    authorizationService.saveAuthorization(authorization);

    // then
    assertThat(authorizationService.isUserAuthorized(userId, null, permission, resource)).isTrue();
  }

  @Test
  public void testRevokeAuthPermissions() {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_REVOKE);
    authorization.setUserId(userId);
    authorization.addPermission(permission);
    authorization.setResource(resource);
    authorization.setResourceId(ANY);

    // when
    authorizationService.saveAuthorization(authorization);

    // then
    assertThat(authorizationService.createAuthorizationQuery().userIdIn(userId).resourceType(resource).authorizationType(AUTH_TYPE_REVOKE).singleResult()).isNotNull();
  }

  @Test
  public void testGlobalAuthPermission() {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    authorization.addPermission(permission);
    authorization.setResource(resource);
    authorization.setResourceId(ANY);

    // when
    authorizationService.saveAuthorization(authorization);

    // then
    assertThat(authorizationService.isUserAuthorized(userId, null, permission, resource)).isTrue();
  }

}
