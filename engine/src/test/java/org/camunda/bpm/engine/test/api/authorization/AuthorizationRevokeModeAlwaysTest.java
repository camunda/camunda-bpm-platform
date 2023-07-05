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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.List;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AuthorizationRevokeModeAlwaysTest extends AuthorizationTest {

  protected static final String LOGGING_CONTEXT = "org.camunda.bpm.engine.impl.persistence.entity.TaskEntity";

  protected String defaultRevokeMode;

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(LOGGING_CONTEXT, Level.DEBUG);

  @Before
  public void storeRevokeMode() {
    defaultRevokeMode = processEngineConfiguration.getAuthorizationCheckRevokes();
  }

  @After
  public void resetRevokeMode() {
    processEngineConfiguration.setAuthorizationCheckRevokes(defaultRevokeMode);
  }

  @Test
  public void shouldCreateEqualQueriesForModesAlwaysAndAutoWhenRevokeExists() {
    // given
    disableAuthorization();
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    createGrantAuthorization(TASK, ANY, userId, READ);
    createRevokeAuthorization(TASK, ANY, userId, UPDATE);
    enableAuthorization();

    processEngineConfiguration.setAuthorizationCheckRevokes(ProcessEngineConfigurationImpl.AUTHORIZATION_CHECK_REVOKE_ALWAYS);
    int taskLogOffset = loggingRule.getLog(LOGGING_CONTEXT).size();

    taskService.createTaskQuery().list();
    List<ILoggingEvent> taskLog = loggingRule.getLog(LOGGING_CONTEXT);
    String modeAlwaysQuery = taskLog.get(taskLogOffset).getFormattedMessage();
    taskLogOffset = taskLog.size();

    processEngineConfiguration.setAuthorizationCheckRevokes(ProcessEngineConfigurationImpl.AUTHORIZATION_CHECK_REVOKE_AUTO);

    // when
    taskService.createTaskQuery().list();

    // then
    String modeAutoQuery = loggingRule.getLog(LOGGING_CONTEXT).get(taskLogOffset).getFormattedMessage();
    assertThat(modeAutoQuery).containsIgnoringCase("Preparing: select").isEqualTo(modeAlwaysQuery);
  }

  @Test
  public void shouldCreateUnequalQueriesForModesAlwaysAndAutoWhenNoRevokeExists() {
    // given
    disableAuthorization();
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    createGrantAuthorization(TASK, ANY, userId, READ);
    enableAuthorization();

    processEngineConfiguration.setAuthorizationCheckRevokes(ProcessEngineConfigurationImpl.AUTHORIZATION_CHECK_REVOKE_ALWAYS);
    int taskLogOffset = loggingRule.getLog(LOGGING_CONTEXT).size();

    taskService.createTaskQuery().list();
    List<ILoggingEvent> taskLog = loggingRule.getLog(LOGGING_CONTEXT);
    String modeAlwaysQuery = taskLog.get(taskLogOffset).getFormattedMessage();
    taskLogOffset = taskLog.size();

    processEngineConfiguration.setAuthorizationCheckRevokes(ProcessEngineConfigurationImpl.AUTHORIZATION_CHECK_REVOKE_AUTO);

    // when
    taskService.createTaskQuery().list();

    // then
    String modeAutoQuery = loggingRule.getLog(LOGGING_CONTEXT).get(taskLogOffset).getFormattedMessage();
    assertThat(modeAutoQuery).containsIgnoringCase("Preparing: select").isNotEqualTo(modeAlwaysQuery);
  }

}
