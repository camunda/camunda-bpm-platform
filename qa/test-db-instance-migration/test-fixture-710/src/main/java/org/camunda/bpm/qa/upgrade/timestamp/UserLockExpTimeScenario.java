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
package org.camunda.bpm.qa.upgrade.timestamp;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityInfoManager;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

/**
 * @author Nikola Koevski
 */
public class UserLockExpTimeScenario extends AbstractTimestampMigrationScenario {

  protected static final String USER_ID = "lockExpTimeTestUser";
  protected static final String PASSWORD = "testPassword";

  @DescribesScenario("initUserLockExpirationTime")
  @Times(1)
  public static ScenarioSetup initUserLockExpirationTime() {
    return new ScenarioSetup() {
      @Override
      public void execute(ProcessEngine processEngine, String s) {

        final IdentityService identityService = processEngine.getIdentityService();

        User user = identityService.newUser(USER_ID);
        user.setPassword(PASSWORD);
        identityService.saveUser(user);

        ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired().execute(new Command<Void>() {
          @Override
          public Void execute(CommandContext context) {
            IdentityInfoManager identityInfoManager = Context.getCommandContext()
              .getSession(IdentityInfoManager.class);

            UserEntity userEntity = (UserEntity) identityService.createUserQuery()
              .userId(USER_ID)
              .singleResult();

            identityInfoManager.updateUserLock(userEntity, 10, TIMESTAMP);
            return null;
          }
        });
      }
    };
  }
}