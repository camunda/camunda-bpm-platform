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
package org.camunda.bpm.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class EnableHistoricInstancePermissionsTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(thrown);

  protected ProcessEngineConfigurationImpl config;
  protected AuthorizationService authorizationService;

  @Before
  public void assign() {
    config = (ProcessEngineConfigurationImpl) engineRule.getProcessEngine()
        .getProcessEngineConfiguration();
    authorizationService = engineRule.getAuthorizationService();
  }

  @After
  public void resetConfig() {
    config.setEnableHistoricInstancePermissions(false);
  }

  @Test
  public void shouldBeFalseByDefault() {
    // given

    // when

    // then
    assertThat(config.isEnableHistoricInstancePermissions())
      .isFalse();
  }

  @Test
  public void shouldBeConfiguredToTrue() {
    // given

    // when
    config.setEnableHistoricInstancePermissions(true);

    // then
    assertThat(config.isEnableHistoricInstancePermissions())
        .isTrue();
  }

  @Test
  public void shouldBeConfiguredToFalse() {
    // given

    // when
    config.setEnableHistoricInstancePermissions(false);

    // then
    assertThat(config.isEnableHistoricInstancePermissions())
        .isFalse();
  }

  @Test
  public void shouldThrowExceptionWhenHistoricInstancePermissionsAreDisabled_Task() {
    // given
    config.setEnableHistoricInstancePermissions(false);

    // then
    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("ENGINE-03090 Historic instance permissions are disabled, " +
        "please check your process engine configuration.");

    // when
    authorizationService.isUserAuthorized("myUserId", null,
        HistoricTaskPermissions.ALL, Resources.HISTORIC_TASK);
  }

  @Test
  public void shouldThrowExceptionWhenHistoricInstancePermissionsAreDisabled_ProcessInstance() {
    // given
    config.setEnableHistoricInstancePermissions(false);

    // then
    thrown.expect(BadUserRequestException.class);
    thrown.expectMessage("ENGINE-03090 Historic instance permissions are disabled, " +
        "please check your process engine configuration.");

    // when
    authorizationService.isUserAuthorized("myUserId", null,
        HistoricTaskPermissions.ALL, Resources.HISTORIC_PROCESS_INSTANCE);
  }

}
