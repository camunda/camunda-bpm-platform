/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
public abstract class AbstractAsyncOperationsTest {

  public static final String ONE_TASK_PROCESS = "oneTaskProcess";
  public static final String TESTING_INSTANCE_DELETE = "testing instance delete";

  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
  }

  protected void executeSeedJob(Batch batch) {
    String seedJobDefinitionId = batch.getSeedJobDefinitionId();
    Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();
    assertNotNull(seedJob);
    managementService.executeJob(seedJob.getId());
  }

  /**
   * Execute all batch jobs of batch once and collect exceptions during job execution.
   *
   * @param batch the batch for which the batch jobs should be executed
   * @return the catched exceptions of the batch job executions, is empty if non where thrown
   */
  protected List<Exception> executeBatchJobs(Batch batch) {
    String batchJobDefinitionId = batch.getBatchJobDefinitionId();
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batchJobDefinitionId).list();
    assertFalse(batchJobs.isEmpty());

    List<Exception> catchedExceptions = new ArrayList<Exception>();

    for (Job batchJob : batchJobs) {
      try {
        managementService.executeJob(batchJob.getId());
      } catch (Exception e) {
        catchedExceptions.add(e);
      }
    }

    return catchedExceptions;
  }

  protected List<String> startTestProcesses(int numberOfProcesses) {
    ArrayList<String> ids = new ArrayList<String>();

    for (int i = 0; i < numberOfProcesses; i++) {
      ids.add(runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS).getProcessInstanceId());
    }

    return ids;
  }

  protected void assertHistoricTaskDeletionPresent(List<String> processIds, String deleteReason, ProcessEngineTestRule testRule) {
    if (!testRule.isHistoryLevelNone()) {

      for (String processId : processIds) {
        HistoricTaskInstance historicTaskInstance = historyService
            .createHistoricTaskInstanceQuery()
            .processInstanceId(processId)
            .singleResult();

        assertThat(historicTaskInstance.getDeleteReason(), is(deleteReason));
      }
    }
  }

  protected void assertHistoricBatchExists(ProcessEngineTestRule testRule) {
    if (testRule.isHistoryLevelFull()) {
      assertThat(historyService.createHistoricBatchQuery().count(), is(1L));
    }
  }

}
