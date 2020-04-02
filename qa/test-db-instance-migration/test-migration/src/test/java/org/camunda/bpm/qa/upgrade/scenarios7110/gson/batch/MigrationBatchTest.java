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
package org.camunda.bpm.qa.upgrade.scenarios7110.gson.batch;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Tassilo Weidner
 */
@ScenarioUnderTest("MigrationBatchScenario")
@Origin("7.11.0")
public class MigrationBatchTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initMigrationBatch.1")
  @Test
  public void testMigrationBatch() {
    List<ProcessInstance> processInstances = engineRule.getRuntimeService().createProcessInstanceQuery()
      .processDefinitionKey("oneTaskProcessMigration_710")
      .list();

    // assume
    assertThat(processInstances.size(), is(10));

    String batchId = engineRule.getManagementService().getProperties()
        .get("MigrationBatchScenario.batchId");
    Batch batch = engineRule.getManagementService().createBatchQuery()
      .type(Batch.TYPE_PROCESS_INSTANCE_MIGRATION)
      .batchId(batchId)
      .singleResult();

    String jobId = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(batch.getSeedJobDefinitionId())
      .singleResult()
      .getId();

    engineRule.getManagementService().executeJob(jobId);

    List<Job> jobs = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(batch.getBatchJobDefinitionId())
      .list();

    // when
    for (Job job : jobs) {
      engineRule.getManagementService().executeJob(job.getId());
    }

    for (ProcessInstance processInstance : processInstances) {
      String taskId = engineRule.getTaskService().createTaskQuery()
        .processInstanceId(processInstance.getId())
        .singleResult()
        .getId();

      engineRule.getTaskService().complete(taskId);
    }

    processInstances = engineRule.getRuntimeService().createProcessInstanceQuery()
      .processDefinitionKey("oneTaskProcessMigration_710")
      .list();

    // then
    assertThat(processInstances.size(), is(0));
  }

}