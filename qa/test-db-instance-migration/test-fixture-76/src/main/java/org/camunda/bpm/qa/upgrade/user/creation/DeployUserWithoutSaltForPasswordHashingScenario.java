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
package org.camunda.bpm.qa.upgrade.user.creation;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;
import org.camunda.bpm.qa.upgrade.Times;

public class DeployUserWithoutSaltForPasswordHashingScenario {

  protected static final String USER_NAME = "kermit";
  protected static final String USER_PWD = "password";

  @DescribesScenario("initUser")
  @Times(1)
  public static ScenarioSetup initUser() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        // given
        IdentityService identityService = engine.getIdentityService();
        User user = identityService.newUser(USER_NAME);
        user.setPassword(USER_PWD);

        // when
        identityService.saveUser(user);

      }
    };
  }
}
