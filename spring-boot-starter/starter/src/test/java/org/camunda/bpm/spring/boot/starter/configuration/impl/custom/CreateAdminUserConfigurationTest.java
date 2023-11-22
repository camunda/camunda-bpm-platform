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
package org.camunda.bpm.spring.boot.starter.configuration.impl.custom;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.test.helper.StandaloneInMemoryTestConfiguration;
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEngineLogger;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAdminUserConfigurationTest {

  private final CamundaBpmProperties camundaBpmProperties = new CamundaBpmProperties();
  {
    camundaBpmProperties.getAdminUser().setId("admin");
    camundaBpmProperties.getAdminUser().setPassword("password");
  }

  private final CreateAdminUserConfiguration createAdminUserConfiguration = new CreateAdminUserConfiguration();
  {
    ReflectionTestUtils.setField(createAdminUserConfiguration, "camundaBpmProperties", camundaBpmProperties);
    createAdminUserConfiguration.init();
  }

  private final ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemoryTestConfiguration(createAdminUserConfiguration);

  @Rule
  public final ProcessEngineRule processEngineRule = new ProcessEngineRule(processEngineConfiguration.buildProcessEngine());

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(SpringBootProcessEngineLogger.PACKAGE);

  @Test
  public void createAdminUser() {
    User user = processEngineRule.getIdentityService().createUserQuery().userId("admin").singleResult();
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("admin@localhost");
  }

  @Test
  public void shouldLogInitialAdminUserCreationOnDebug() {
    processEngineConfiguration.buildProcessEngine();
    verifyLogs(Level.DEBUG, "STARTER-SB010 Creating initial Admin User: AdminUserProperty[id=admin, firstName=Admin, lastName=Admin, email=admin@localhost, password=******]");
  }

  protected void verifyLogs(Level logLevel, String message) {
    List<ILoggingEvent> logs = loggingRule.getLog();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getLevel()).isEqualTo(logLevel);
    assertThat(logs.get(0).getFormattedMessage()).containsIgnoringCase(message);
  }

}
