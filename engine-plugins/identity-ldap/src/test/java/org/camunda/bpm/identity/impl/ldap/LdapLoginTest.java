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
package org.camunda.bpm.identity.impl.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironmentRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class LdapLoginTest {

  @ClassRule
  public static LdapTestEnvironmentRule ldapRule = new LdapTestEnvironmentRule();
  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  IdentityService identityService;

  @Before
  public void setup() {
    identityService = engineRule.getIdentityService();
  }

  @Test
  public void testLdapLoginSuccess() {
    assertThat(identityService.checkPassword("roman", "roman")).isTrue();
  }

  @Test
  public void testLdapLoginCapitalization() {
    assertThat(identityService.checkPassword("Roman", "roman")).isTrue();
  }

  @Test
  public void testLdapLoginFailure() {
    assertThat(identityService.checkPassword("roman", "ro")).isFalse();
    assertThat(identityService.checkPassword("r", "roman")).isFalse();
  }

  @Test
  public void testLdapLoginNullValues() {
    assertThat(identityService.checkPassword(null, "roman")).isFalse();
    assertThat(identityService.checkPassword("roman", null)).isFalse();
    assertThat(identityService.checkPassword(null, null)).isFalse();
  }

  @Test
  public void testLdapLoginEmptyPassword() {
    assertThat(identityService.checkPassword("roman", "")).isTrue();
  }

}
