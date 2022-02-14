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
package org.camunda.bpm.qa.upgrade.scenarios7160.migration;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioUnderTest("CreateSetVariablesMigrationBatchScenario")
@Origin("7.15.0")
public class SetVariablesMigrationBatchTest {

  @Rule
  public UpgradeTestRule engineRule = new UpgradeTestRule();

  ManagementService managementService;

  @Before
  public void assignServices() {
    managementService = engineRule.getManagementService();
  }

  @Test
  @ScenarioUnderTest("createSetVariablesMigrationBatchScenario.1")
  public void shouldCompleteBatchSuccessfully() {
    Map<String, String> properties = managementService.getProperties();

    String batchId = properties.get("CreateSetVariablesMigrationBatchScenario.batch.id");
    Batch batch = managementService.createBatchQuery()
        .batchId(batchId)
        .singleResult();

    List<Job> jobs = managementService.createJobQuery()
        .jobDefinitionId(batch.getSeedJobDefinitionId())
        .list();

    jobs.forEach(job -> managementService.executeJob(job.getId()));

    jobs = managementService.createJobQuery()
        .jobDefinitionId(batch.getBatchJobDefinitionId())
        .list();

    jobs.forEach(job -> managementService.executeJob(job.getId()));

    jobs = managementService.createJobQuery()
        .jobDefinitionId(batch.getMonitorJobDefinitionId())
        .list();

    jobs.forEach(job -> managementService.executeJob(job.getId()));

    ProcessInstance processInstance = engineRule.getRuntimeService()
        .createProcessInstanceQuery()
        .processDefinitionKey("oneTaskProcess-target-715")
        .singleResult();

    assertThat(processInstance).isNotNull();
  }

}
