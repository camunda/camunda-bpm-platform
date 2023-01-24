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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tassilo Weidner
 */
@ScenarioUnderTest("SetJobRetriesBatchScenario")
@Origin("7.11.0")
public class SetJobRetriesBatchTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initSetJobRetriesBatch.1")
  @Test
  public void testSetExternalTaskRetriesBatch() {
    List<Job> jobs = engineRule.getManagementService()
      .createJobQuery()
      .processDefinitionKey("oneTaskProcessAsync_710")
      .list();

    // assume
    assertThat(jobs.size(), is(10));
    for (Job job : jobs) {
      assertThat(job.getRetries(), is(3));
    }

    String batchId = engineRule.getManagementService().getProperties().get("SetJobRetriesBatchScenario.retries.batchId");

    Batch batch = engineRule.getManagementService().createBatchQuery()
      .type(Batch.TYPE_SET_JOB_RETRIES)
      .batchId(batchId)
      .singleResult();

    String jobId = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(batch.getSeedJobDefinitionId())
      .singleResult()
      .getId();

    engineRule.getManagementService().executeJob(jobId);

    List<Job> batchJobs = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(batch.getBatchJobDefinitionId())
      .list();

    // when
    for (Job job : batchJobs) {
      engineRule.getManagementService().executeJob(job.getId());
    }

    jobs = engineRule.getManagementService()
      .createJobQuery()
      .processDefinitionKey("oneTaskProcessAsync_710")
      .list();

    // then
    for (Job job : jobs) {
      assertThat(job.getRetries(), is(22));
    }
  }

}