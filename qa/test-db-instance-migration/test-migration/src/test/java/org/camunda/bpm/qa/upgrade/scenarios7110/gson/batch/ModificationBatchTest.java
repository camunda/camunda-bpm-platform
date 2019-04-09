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
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
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
@ScenarioUnderTest("ModificationBatchScenario")
@Origin("7.11.0")
public class ModificationBatchTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initModificationBatch.1")
  @Test
  public void testProcessInstanceModification() {
    List<Execution> executions = engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("oneTaskProcessModification_710")
      .processInstanceBusinessKey("ModificationBatchScenario")
      .activityId("userTask1")
      .list();

    // assume
    assertThat(executions.size(), is(10));

    List<Batch> batches = engineRule.getManagementService().createBatchQuery()
      .type(Batch.TYPE_PROCESS_INSTANCE_MODIFICATION)
      .list();

    Batch modificationBatch = findBatchByTotalJobs(10, batches);

    String jobId = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(modificationBatch.getSeedJobDefinitionId())
      .singleResult()
      .getId();

    engineRule.getManagementService().executeJob(jobId);

    List<Job> jobs = engineRule.getManagementService().createJobQuery()
      .jobDefinitionId(modificationBatch.getBatchJobDefinitionId())
      .list();

    // when
    for (Job job : jobs) {
      engineRule.getManagementService().executeJob(job.getId());
    }

    List<Execution> executionsInUserTaskOne = getExecutionsByActivityId("userTask1");

    List<Execution> executionsInUserTaskTwo = getExecutionsByActivityId("userTask2");

    List<Execution> executionsInUserTaskThree = getExecutionsByActivityId("userTask3");

    List<Execution> executionsInUserTaskFour = getExecutionsByActivityId("userTask4");

    List<ProcessInstance> processInstances = engineRule.getRuntimeService().createProcessInstanceQuery()
      .processDefinitionKey("oneTaskProcessModification_710")
      .processInstanceBusinessKey("ModificationBatchScenario")
      .list();

    List<HistoricActivityInstance> canceledActivityInstances = engineRule.getHistoryService()
      .createHistoricActivityInstanceQuery()
      .processInstanceId(processInstances.get(0).getId())
      .canceled()
      .list();

    // then
    assertThat(executionsInUserTaskOne.size(), is(20));
    assertThat(executionsInUserTaskTwo.size(), is(10));
    assertThat(executionsInUserTaskThree.size(), is(10));

    assertThat(executionsInUserTaskFour.size(), is(0));
    assertThat(canceledActivityInstances.size(), is(1));
  }

  protected Batch findBatchByTotalJobs(int totalJobsCount, List<Batch> batches) {
    for (Batch batch : batches) {
      if (batch.getTotalJobs() == totalJobsCount) {
        return batch;
      }
    }

    return null;
  }

  protected List<Execution> getExecutionsByActivityId(String activityId) {
    return engineRule.getRuntimeService().createExecutionQuery()
      .processDefinitionKey("oneTaskProcessModification_710")
      .processInstanceBusinessKey("ModificationBatchScenario")
      .activityId(activityId)
      .list();
  }

}