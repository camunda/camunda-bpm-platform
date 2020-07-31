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
package org.camunda.bpm.engine.test.api.authorization.task.getvariable;

import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.authorization.TaskPermissions.READ_VARIABLE;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

import java.util.Collection;

import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Yana.Vasileva
 *
 */
@RunWith(Parameterized.class)
public class StandaloneTaskReadVariablePermissionAuthorizationTest extends StandaloneTaskAuthorizationTest{

  @Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(TASK, "taskId", userId, READ_VARIABLE)),
      scenario()
        .withAuthorizations(
          grant(TASK, "taskId", userId, READ_VARIABLE)),
      scenario()
        .withAuthorizations(
          grant(TASK, "*", userId, READ_VARIABLE))
        .succeeds()
      );
  }

  @Before
  public void setUp() {
    super.setUp();
    ensureSpecificVariablePermission = processEngineConfiguration.isEnforceSpecificVariablePermission();
    // prerequisite of the whole test suite
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);
  }

  @After
  public void tearDown() {
    super.tearDown();
    processEngineConfiguration.setEnforceSpecificVariablePermission(ensureSpecificVariablePermission);
  }

}
