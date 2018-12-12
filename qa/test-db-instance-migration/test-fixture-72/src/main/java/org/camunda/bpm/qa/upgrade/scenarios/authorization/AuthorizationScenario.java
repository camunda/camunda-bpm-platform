/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.qa.upgrade.scenarios.authorization;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

/**
 * @author Roman Smirnov
 *
 */
public class AuthorizationScenario {

  protected String user = "test";
  protected String group = "accounting";

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
        String userId = "test";
        User user = identityService.newUser(userId);
        identityService.saveUser(user);

        // create group
        String groupId = "accounting";
        Group group = identityService.newGroup(groupId);
        identityService.saveGroup(group);

        // create membership
        identityService.createMembership("test", "accounting");

        // start a process instance
        engine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess", scenarioName);
      }
    };
  }

  // user ////////////////////////////////////////////////////////////////

  protected User createUser(IdentityService identityService, String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);
    return user;
  }

  // group //////////////////////////////////////////////////////////////

  protected Group createGroup(IdentityService identityService, String groupId) {
    Group group = identityService.newGroup(groupId);
    identityService.saveGroup(group);
    return group;
  }

}
