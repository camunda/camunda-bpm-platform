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

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;
import org.junit.runners.Parameterized;

public class ModificationBatchAuthorizationTest extends BatchCreationAuthorizationTest {

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .withoutAuthorizations()
            .failsDueToRequired(
                grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
                grant(Resources.BATCH, "batchId", "userId", BatchPermissions.CREATE_BATCH_MODIFY_PROCESS_INSTANCES)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "batchId", "userId", BatchPermissions.CREATE_BATCH_MODIFY_PROCESS_INSTANCES)
            ).succeeds()
    );
  }

  @Test
  public void createBatchModification() {
    //given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process1").startEvent().userTask("user1").userTask("user2").endEvent().done();
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(instance);

    List<String> instances = new ArrayList<String>();
    for (int i = 0; i < 2; i++) {
      ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process1");
      instances.add(processInstance.getId());
    }

    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("batchId", "*")
        .start();

    // when
    engineRule.getRuntimeService().createModification(processDefinition.getId()).startAfterActivity("user1").processInstanceIds(instances).executeAsync();

    // then
    if (authRule.assertScenario(scenario)) {
      Batch batch = engineRule.getManagementService().createBatchQuery().singleResult();
      assertEquals("userId", batch.getCreateUserId());
    }
  }


}
