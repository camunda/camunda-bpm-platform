/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.api.runtime;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Askar Akhmerov
 */
public class RuntimeServiceAsyncOperationsTest {

  public static final String TESTING_INSTANCE_DELETE = "testing instance delete";
  public static final String ONE_TASK_PROCESS = "oneTaskProcess";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private ManagementService managementService;
  private HistoryService historyService;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
  }

  @After
  public void cleanBatch() {
    Batch batch = managementService.createBatchQuery().singleResult();
    if (batch != null) {
      managementService.deleteBatch(batch.getId(), true);
    }

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithList() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertTasksAreDeleted(processIds, TESTING_INSTANCE_DELETE);
    assertHistoricBatchExists();
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNonExistingId() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    processIds.add("unknown");

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);

    try {
      executeBatchJobs(batch);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertTrue(e.getMessage().startsWith("No process instance found for id 'unknown'"));
    }

    // then
    assertThat(managementService.createJobQuery().withException().list().size(), is(1));

    processIds.remove("unknown");
    assertTasksAreDeleted(processIds, TESTING_INSTANCE_DELETE);
    assertHistoricBatchExists();
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNullList() throws Exception {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    runtimeService.deleteProcessInstancesAsync((List<String>) null, TESTING_INSTANCE_DELETE);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithEmptyList() throws Exception {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    runtimeService.deleteProcessInstancesAsync(new ArrayList<String>(), TESTING_INSTANCE_DELETE);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithQuery() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery().processInstanceIds(new HashSet<String>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processInstanceQuery, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertTasksAreDeleted(processIds, TESTING_INSTANCE_DELETE);
    assertHistoricBatchExists();
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithQueryWithoutDeleteReason() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery().processInstanceIds(new HashSet<String>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processInstanceQuery, null);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertTasksAreDeleted(processIds, "deleted");
    assertHistoricBatchExists();
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNullQueryParameter() throws Exception {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceQuery is null");

    runtimeService.deleteProcessInstancesAsync((ProcessInstanceQuery) null, TESTING_INSTANCE_DELETE);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithInvalidQueryParameter() throws Exception {
    // given
    startTestProcesses(2);
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey("invalid");

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    // when
    runtimeService.deleteProcessInstancesAsync(query, TESTING_INSTANCE_DELETE);
  }

  /// helper //////

  private void executeSeedJob(Batch batch) {
    String seedJobDefinitionId = batch.getSeedJobDefinitionId();
    Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();
    assertNotNull(seedJob);
    managementService.executeJob(seedJob.getId());
  }

  private void executeBatchJobs(Batch batch) {
    String batchJobDefinitionId = batch.getBatchJobDefinitionId();
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batchJobDefinitionId).list();
    assertFalse(batchJobs.isEmpty());
    for (Job batchJob : batchJobs) {
      managementService.executeJob(batchJob.getId());
    }
  }

  protected List<String> startTestProcesses(int numberOfProcesses) {
    ArrayList<String> ids = new ArrayList<String>();

    for (int i = 0; i < numberOfProcesses; i++) {
      ids.add(runtimeService.startProcessInstanceByKey(ONE_TASK_PROCESS).getProcessInstanceId());
    }

    return ids;
  }

  protected void assertTasksAreDeleted(List<String> processIds, String deleteReason) {
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

  protected void assertHistoricBatchExists() {
    if (testRule.isHistoryLevelFull()) {
      assertThat(historyService.createHistoricBatchQuery().count(), is(1L));
    }
  }

  protected void assertProcessInstancesAreDeleted() {
    assertThat(runtimeService.createProcessInstanceQuery().list().size(), is(0));
  }

}
