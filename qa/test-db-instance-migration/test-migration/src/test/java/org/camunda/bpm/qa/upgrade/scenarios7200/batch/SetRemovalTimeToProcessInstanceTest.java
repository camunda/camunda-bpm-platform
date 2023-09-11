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
package org.camunda.bpm.qa.upgrade.scenarios7200.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScenarioUnderTest("SetRemovalTimeToProcessInstanceScenario")
@Origin("7.19.0")
public class SetRemovalTimeToProcessInstanceTest {

  Logger LOG = LoggerFactory.getLogger(SetRemovalTimeToProcessInstanceTest.class);

  @Rule
  public UpgradeTestRule engineRule = new UpgradeTestRule();

  HistoryService historyService;
  ManagementService managementService;
  RuntimeService runtimeService;

  @Before
  public void assignServices() {
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  @ScenarioUnderTest("runBatchJob.1")
  public void shouldRunBatchJobOnce() {
    // given
    Map<String, String> properties = managementService.getProperties();
    String batchId = properties.get("SetRemovalTimeToProcessInstanceTest.batchId");
    String processInstanceId = properties.get("SetRemovalTimeToProcessInstanceTest.processInstanceId");
    Date removalTime = new Date(1363609000000L);

    Batch batch = managementService.createBatchQuery().batchId(batchId).singleResult();
    String seedJobDefinitionId = batch.getSeedJobDefinitionId();
    Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();

    managementService.executeJob(seedJob.getId());
    List<Job> batchJobs = managementService.createJobQuery()
        .jobDefinitionId(batch.getBatchJobDefinitionId())
        .list();

    // when
    batchJobs.forEach(job -> managementService.executeJob(job.getId()));

    // then
    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
        .activityId("userTask")
        .processInstanceId(processInstanceId)
        .singleResult();
    assertThat(historicActivityInstance.getRemovalTime()).isEqualTo(removalTime);

    assertThat(managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).count()).isEqualTo(0);
  }

}
