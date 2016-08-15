/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.upgrade.scenarios.rolling.authorization;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class AuthorizationScenario {

  protected static final String USER_ID = "test";
  protected static final String GROUP_ID = "accounting";
  public static final String PROCESS_DEF_KEY = "oneTaskProcess";

  @Deployment
  public static String deployOneTaskProcess() {
    return "org/camunda/bpm/qa/upgrade/authorization/oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("startProcessInstance")
  public static ScenarioSetup startProcessInstance() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        IdentityService identityService = engine.getIdentityService();

        // create an user
        User user = identityService.newUser(USER_ID);
        identityService.saveUser(user);

        // create group
        Group group = identityService.newGroup(GROUP_ID);
        identityService.saveGroup(group);

        // create membership
        identityService.createMembership(USER_ID, GROUP_ID);

        // start a process instance
        engine.getRuntimeService().startProcessInstanceByKey(PROCESS_DEF_KEY, scenarioName);
      }
    };
  }

}
