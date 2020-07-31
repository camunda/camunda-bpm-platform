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
package org.camunda.bpm.engine.test.api.authorization.batch.creation.removaltime;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.batch.creation.BatchCreationAuthorizationTest;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.junit.Test;
import org.junit.runners.Parameterized;

/**
 * @author Tassilo Weidner
 */
public class SetRemovalTimeForHistoricBatchesBatchAuthorizationTest extends BatchCreationAuthorizationTest {

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .withAuthorizations(
              grant(Resources.BATCH, "batchId", "userId", Permissions.READ_HISTORY)
            )
            .failsDueToRequired(
                grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
                grant(Resources.BATCH, "batchId", "userId", BatchPermissions.CREATE_BATCH_SET_REMOVAL_TIME)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "batchId", "userId", Permissions.READ_HISTORY, Permissions.CREATE)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "batchId", "userId", Permissions.READ_HISTORY, BatchPermissions.CREATE_BATCH_SET_REMOVAL_TIME)
            ).succeeds()
    );
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void shouldAuthorizeSetRemovalTimeForHistoricBatchesBatch() {
    // given
    String batchId = engineRule.getHistoryService()
      .deleteHistoricProcessInstancesAsync(Collections.singletonList(processInstance.getId()), "aDeleteReason").getId();

    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("batchId", "*")
        .start();

    HistoricBatchQuery query = historyService.createHistoricBatchQuery().batchId(batchId);

    // when
    historyService.setRemovalTimeToHistoricBatches()
      .absoluteRemovalTime(new Date())
      .byQuery(query)
      .executeAsync();

    // then
    authRule.assertScenario(scenario);

    // clear database
    managementService.deleteBatch(batchId, true);
  }

}
