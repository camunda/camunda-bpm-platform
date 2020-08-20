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
package org.camunda.bpm.engine.test.api.authorization.batch.creation;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

public class SetVariablesBatchAuthorizationTest extends BatchCreationAuthorizationTest {

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .withAuthorizations(
              grant(Resources.PROCESS_DEFINITION, "processDefinitionKey", "userId",
                  ProcessDefinitionPermissions.READ_INSTANCE)
            )
            .failsDueToRequired(
                grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
                grant(Resources.BATCH, "batchId", "userId",
                    BatchPermissions.CREATE_BATCH_SET_VARIABLES)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.PROCESS_DEFINITION, "processDefinitionKey", "userId",
                    ProcessDefinitionPermissions.READ_INSTANCE),
                grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE)
            ).succeeds(),
        scenario()
            .withAuthorizations(
                grant(Resources.PROCESS_DEFINITION, "processDefinitionKey", "userId",
                    ProcessDefinitionPermissions.READ_INSTANCE),
                grant(Resources.BATCH, "batchId", "userId",
                    BatchPermissions.CREATE_BATCH_SET_VARIABLES)
            ).succeeds()
    );
  }

  @Test
  public void shouldAuthorizeSetVariablesBatch() {
    // given
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("batchId", "*")
        .bindResource("processDefinitionKey", ProcessModels.PROCESS_KEY)
        .start();

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();

    // when
    runtimeService.setVariablesAsync(processInstanceQuery,
        Variables.createVariables().putValue("foo", "bar"));

    // then
    authRule.assertScenario(scenario);
  }

}
