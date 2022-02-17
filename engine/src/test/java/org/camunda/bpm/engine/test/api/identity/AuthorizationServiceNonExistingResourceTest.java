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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AuthorizationServiceNonExistingResourceTest {

  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  protected AuthorizationService authorizationService;

  @Before
  public void setup() {
    authorizationService = rule.getAuthorizationService();
  }

  @Test
  public void shouldFailSaveAuthorizationWithIncompatibleResourceAndPermission() {
    // given
    Authorization authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setUserId("testUser");
    authorization.addPermission(TestPermissions.RANDOM);
    authorization.setResource(Resources.TASK);
    authorization.setResourceId(ANY);

    // when attempt to save, expect BadUserRequest
    assertThatThrownBy(() -> authorizationService.saveAuthorization(authorization))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessage("ENGINE-03087 The resource type with id:'" + Resources.TASK.resourceType() + "' is not valid for '" + TestPermissions.RANDOM.getName() + "' permission." );
  }
}
