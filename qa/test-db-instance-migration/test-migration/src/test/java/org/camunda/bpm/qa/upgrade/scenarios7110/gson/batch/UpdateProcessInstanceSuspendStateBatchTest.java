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
@ScenarioUnderTest("UpdateProcessInstanceSuspendStateBatchScenario")
@Origin("7.11.0")
public class UpdateProcessInstanceSuspendStateBatchTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initUpdateProcessInstanceSuspendStateBatch.1")
  @Test
  public void testUpdateProcessInstanceSuspendStateBatch() {
    List<ProcessInstance> processInstances = engineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .processDefinitionKey("oneTaskProcess_710")
      .processInstanceBusinessKey("UpdateProcessInstanceSuspendStateBatchScenario")
      .list();

    // assume
    assertThat(processInstances.size(), is(10));

    Batch batch = engineRule.getManagementService().createBatchQuery()
      .type(Batch.TYPE_PROCESS_INSTANCE_UPDATE_SUSPENSION_STATE)
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

    processInstances = engineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .processDefinitionKey("oneTaskProcess_710")
      .processInstanceBusinessKey("UpdateProcessInstanceSuspendStateBatchScenario")
      .list();

    // then
    assertThat(processInstances.size(), is(10));

    for (ProcessInstance processInstance : processInstances) {
      assertThat(processInstance.isSuspended(), is(true));
    }
  }

}