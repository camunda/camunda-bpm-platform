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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.PasswordPolicy;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.identity.DefaultPasswordPolicyImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Miklas Boskamp
 */
public class CustomPasswordPolicyTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private ProcessEngineConfigurationImpl processEngineConfiguration;
  private IdentityService identityService;

  @Before
  public void init() {
    identityService = engineRule.getIdentityService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    processEngineConfiguration.setPasswordPolicy(new DefaultPasswordPolicyImpl());
    processEngineConfiguration.setEnablePasswordPolicy(true);
  }

  @After
  public void tearDown() {
    // reset configuration
    processEngineConfiguration.setPasswordPolicy(null);
    processEngineConfiguration.setEnablePasswordPolicy(false);
    // reset database
    identityService.deleteUser("user");
  }

  @Test
  public void testPasswordPolicyConfiguration() {
    PasswordPolicy policy = processEngineConfiguration.getPasswordPolicy();
    assertThat(policy.getClass().isAssignableFrom(DefaultPasswordPolicyImpl.class), is(true));
    assertThat(policy.getRules().size(), is(6));
  }

  @Test
  public void testCustomPasswordPolicyWithCompliantPassword() {
    User user = identityService.newUser("user");
    user.setPassword("this-is-1-STRONG-password");
    identityService.saveUser(user);
    assertThat(identityService.createUserQuery().userId(user.getId()).count(), is(1L));
  }

  @Test
  public void testCustomPasswordPolicyWithNonCompliantPassword() {
    thrown.expect(ProcessEngineException.class);
    User user = identityService.newUser("user");
    user.setPassword("weakpassword");
    identityService.saveUser(user);
    thrown.expectMessage("Password does not match policy");
    assertThat(identityService.createUserQuery().userId(user.getId()).count(), is(0L));
  }
}