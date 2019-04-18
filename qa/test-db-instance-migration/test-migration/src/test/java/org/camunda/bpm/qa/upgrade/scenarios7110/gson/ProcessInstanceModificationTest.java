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
package org.camunda.bpm.qa.upgrade.scenarios7110.gson;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
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
@ScenarioUnderTest("ProcessInstanceModificationScenario")
@Origin("7.11.0")
public class ProcessInstanceModificationTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initProcessInstanceModification.1")
  @Test
  public void testModificationBatch() {
    List<Execution> executions = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("oneTaskProcessInstanceModification_710")
      .processInstanceBusinessKey("ProcessInstanceModificationScenario")
      .active()
      .list();

    // assume
    assertThat(executions.size(), is(3));

    List<Batch> batches = engineRule.getManagementService().createBatchQuery()
      .type(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION)
      .list();

    Batch processInstanceModification = findBatchByTotalJobs(1, batches);

    String jobId = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(processInstanceModification.getSeedJobDefinitionId())
      .singleResult()
      .getId();

    engineRule.getManagementService().executeJob(jobId);

    Job job = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(processInstanceModification.getBatchJobDefinitionId())
      .singleResult();

    // when
    engineRule.getManagementService().executeJob(job.getId());

    executions = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("oneTaskProcessInstanceModification_710")
      .processInstanceBusinessKey("ProcessInstanceModificationScenario")
      .active()
      .list();

    // then
    assertThat(executions.size(), is(0));
  }

  protected Batch findBatchByTotalJobs(int totalJobsCount, List<Batch> batches) {
    for (Batch batch : batches) {
      if (batch.getTotalJobs() == totalJobsCount) {
        return batch;
      }
    }

    return null;
  }

}