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
package org.camunda.bpm.qa.upgrade.scenarios7190.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScenarioUnderTest("SetJobRetriesWithDueDateScenario")
@Origin("7.18.0")
public class SetJobRetriesWithDueDateTest {

  Logger LOG = LoggerFactory.getLogger(SetJobRetriesWithDueDateTest.class);
  @Rule
  public UpgradeTestRule engineRule = new UpgradeTestRule();

  ManagementService managementService;

  @Before
  public void assignServices() {
    managementService = engineRule.getManagementService();
  }

  @Test
  @ScenarioUnderTest("createSetRetriesBatch.1")
  public void shouldNotSetDueDate() {
    // given
    Map<String, String> properties = managementService.getProperties();
    String batchId = properties.get("SetJobRetriesWithDueDateTest.retries.batchId");
    Batch batch = managementService.createBatchQuery().batchId(batchId).singleResult();
    String seedJobDefinitionId = batch.getSeedJobDefinitionId();
    Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();

    Job timerJob = managementService.createJobQuery().processDefinitionKey("createProcessForSetRetriesWithDueDate_718").singleResult();

    managementService.executeJob(seedJob.getId());
    List<Job> batchJobs = managementService.createJobQuery()
        .jobDefinitionId(batch.getBatchJobDefinitionId())
        .list();

    // when
    batchJobs.forEach(job -> managementService.executeJob(job.getId()));

    // then
    Job timerJobAfterBatch = managementService.createJobQuery().processDefinitionKey("createProcessForSetRetriesWithDueDate_718").singleResult();
    assertThat(timerJob.getDuedate()).isEqualToIgnoringMillis(timerJobAfterBatch.getDuedate());
    assertThat(timerJob.getRetries()).isEqualTo(3);
    assertThat(timerJobAfterBatch.getRetries()).isEqualTo(5);
  }
}
