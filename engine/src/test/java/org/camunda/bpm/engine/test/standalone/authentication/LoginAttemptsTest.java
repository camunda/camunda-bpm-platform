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
package org.camunda.bpm.engine.test.standalone.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.qos.logback.classic.Level;
import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class LoginAttemptsTest {

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private static final String INDENTITY_LOGGER = "org.camunda.bpm.engine.identity";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
      configuration.setJdbcUrl("jdbc:h2:mem:LoginAttemptsTest;DB_CLOSE_DELAY=1000");
      configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
      configuration.setLoginMaxAttempts(5);
      configuration.setLoginDelayFactor(2);
      configuration.setLoginDelayMaxTime(30);
      configuration.setLoginDelayBase(1);
  });

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
                                                      .watch(INDENTITY_LOGGER)
                                                      .level(Level.INFO);

  protected IdentityService identityService;
  protected ProcessEngine processEngine;

  @Before
  public void setup() {
    identityService = engineRule.getIdentityService();
  }

  @After
  public void tearDown() {
    ClockUtil.setCurrentTime(new Date());
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
  }

  @Test
  public void testUsuccessfulAttemptsResultInLockedUser() throws ParseException {
    // given
    User user = identityService.newUser("johndoe");
    user.setPassword("xxx");
    identityService.saveUser(user);

    Date now = sdf.parse("2000-01-24T13:00:00");
    ClockUtil.setCurrentTime(now);
    // when
    for (int i = 0; i <= 6; i++) {
      assertThat(identityService.checkPassword("johndoe", "invalid pwd")).isFalse();
      now = DateUtils.addSeconds(now, 5);
      ClockUtil.setCurrentTime(now);
    }

    // then
    assertThat(loggingRule.getFilteredLog(INDENTITY_LOGGER, "The user with id 'johndoe' is permanently locked.").size()).isEqualTo(1);
  }
}
