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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.AbstractAsyncOperationsTest;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import org.camunda.bpm.engine.test.api.runtime.util.IncrementCounterListener;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;


/**
 * @author Askar Akhmerov
 */
public class RuntimeServiceAsyncOperationsTest extends AbstractAsyncOperationsTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);

  private int defaultBatchJobsPerSeed;
  private int defaultInvocationsPerBatchJob;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public RuleChain migrationChain = RuleChain.outerRule(testRule).around(migrationRule);

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
  }

  @After
  public void cleanBatch() {
    List<Batch> batches = managementService.createBatchQuery().list();
    if (batches.size() > 0) {
      for (Batch batch : batches)
        managementService.deleteBatch(batch.getId(), true);
    }

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }
  }

  @Before
  public void storeEngineSettings() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    defaultBatchJobsPerSeed = configuration.getBatchJobsPerSeed();
    defaultInvocationsPerBatchJob = configuration.getInvocationsPerBatchJob();
  }

  @After
  public void restoreEngineSettings() {
    ProcessEngineConfigurationImpl configuration = engineRule.getProcessEngineConfiguration();
    configuration.setBatchJobsPerSeed(defaultBatchJobsPerSeed);
    configuration.setInvocationsPerBatchJob(defaultInvocationsPerBatchJob);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithList() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithLargeList() throws Exception {
    // given
    engineRule.getProcessEngineConfiguration().setBatchJobsPerSeed(1010);
    List<String> processIds = startTestProcesses(1100);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE);

    createAndExecuteSeedJobs(batch.getSeedJobDefinitionId(), 2);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();

    // cleanup
    if (!testRule.isHistoryLevelNone()) {
      batch = historyService.deleteHistoricProcessInstancesAsync(processIds, null);
      createAndExecuteSeedJobs(batch.getSeedJobDefinitionId(), 2);
      executeBatchJobs(batch);
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithListOnly() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
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
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertEquals(1, exceptions.size());

    Exception e = exceptions.get(0);
    assertTrue(e.getMessage().startsWith("No process instance found for id 'unknown'"));

    assertThat(managementService.createJobQuery().withException().list().size(), is(1));

    processIds.remove("unknown");
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNullList() throws Exception {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    runtimeService.deleteProcessInstancesAsync(null, null, TESTING_INSTANCE_DELETE);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithEmptyList() throws Exception {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    runtimeService.deleteProcessInstancesAsync(new ArrayList<String>(), null, TESTING_INSTANCE_DELETE);

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
    Batch batch = runtimeService.deleteProcessInstancesAsync(null, processInstanceQuery, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithQueryOnly() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery().processInstanceIds(new HashSet<String>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processInstanceQuery, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
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
    Batch batch = runtimeService.deleteProcessInstancesAsync(null, processInstanceQuery, null);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, "deleted", testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNullQueryParameter() throws Exception {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    runtimeService.deleteProcessInstancesAsync(null, null, TESTING_INSTANCE_DELETE);
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
    runtimeService.deleteProcessInstancesAsync(null, query, TESTING_INSTANCE_DELETE);
  }

  protected void assertProcessInstancesAreDeleted() {
    assertThat(runtimeService.createProcessInstanceQuery().list().size(), is(0));
  }

  @Test
  public void testDeleteProcessInstancesAsyncWithSkipCustomListeners() {

    // given
    IncrementCounterListener.counter = 0;

    BpmnModelInstance instance = ProcessModels.newModel(ONE_TASK_PROCESS)
        .startEvent()
        .userTask()
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, IncrementCounterListener.class.getName())
        .endEvent()
        .done();

    testRule.deploy(instance);
    List<String> processIds = startTestProcesses(1);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE, true);
    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertThat(IncrementCounterListener.counter, is(0));
  }

  @Test
  public void testDeleteProcessInstancesAsyncWithSkipSubprocesses() {

    // given
    BpmnModelInstance callingInstance = ProcessModels.newModel(ONE_TASK_PROCESS)
        .startEvent()
          .callActivity()
            .calledElement("called")
        .endEvent()
        .done();

    BpmnModelInstance calledInstance = ProcessModels.newModel("called")
        .startEvent()
        .userTask()
        .endEvent()
        .done();

    testRule.deploy(callingInstance, calledInstance);
    List<String> processIds = startTestProcesses(1);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE, false, true);
    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    ProcessInstance superInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processIds.get(0)).singleResult();
    assertNull(superInstance);

    ProcessInstance subInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("called").singleResult();
    assertNotNull(subInstance);
  }

  @Test
  public void testDeleteProcessInstancesAsyncWithoutSkipSubprocesses() {

    // given
    BpmnModelInstance callingInstance = ProcessModels.newModel(ONE_TASK_PROCESS)
        .startEvent()
          .callActivity()
            .calledElement("called")
        .endEvent()
        .done();

    BpmnModelInstance calledInstance = ProcessModels.newModel("called")
        .startEvent()
        .userTask()
        .endEvent()
        .done();

    testRule.deploy(callingInstance, calledInstance);
    List<String> processIds = startTestProcesses(1);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE, false, false);
    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    ProcessInstance superInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processIds.get(0)).singleResult();
    assertNull(superInstance);

    ProcessInstance subInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("called").singleResult();
    assertNull(subInstance);
  }


  @Test
  public void testInvokeListenersWhenDeletingProcessInstancesAsync() {

    // given
    IncrementCounterListener.counter = 0;

    BpmnModelInstance instance = ProcessModels.newModel(ONE_TASK_PROCESS)
        .startEvent()
        .userTask()
          .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, IncrementCounterListener.class.getName())
        .endEvent()
        .done();

    migrationRule.deploy(instance);
    List<String> processIds = startTestProcesses(1);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, TESTING_INSTANCE_DELETE);
    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertThat(IncrementCounterListener.counter, is(1));
  }

  @Test
  public void testDeleteProcessInstancesAsyncWithListInDifferentDeployments() {
    // given
    ProcessDefinition sourceDefinition1 = testRule
        .deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "ONE_TASK_PROCESS"));
    ProcessDefinition sourceDefinition2 = testRule
        .deployAndGetDefinition(modify(ProcessModels.TWO_TASKS_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "TWO_TASKS_PROCESS"));
    List<String> processInstanceIds = createProcessInstances(sourceDefinition1, sourceDefinition2, 15, 10);
    final String firstDeploymentId = sourceDefinition1.getDeploymentId();
    final String secondDeploymentId = sourceDefinition2.getDeploymentId();

    List<String> processInstanceIdsFromFirstDeployment = getProcessInstanceIdsByDeploymentId(firstDeploymentId);
    List<String> processInstanceIdsFromSecondDeployment = getProcessInstanceIdsByDeploymentId(secondDeploymentId);

    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);
    engineRule.getProcessEngineConfiguration().setBatchJobsPerSeed(3);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processInstanceIds, null, "test_reason");

    String seedJobDefinitionId = batch.getSeedJobDefinitionId();
    // seed jobs
    int expectedSeedJobsCount = 5;
    createAndExecuteSeedJobs(seedJobDefinitionId, expectedSeedJobsCount);

    // then
    List<Job> jobs = managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list();

    // execute jobs related to the first deployment
    List<String> jobIdsForFirstDeployment = getJobIdsByDeployment(jobs, firstDeploymentId);
    assertNotNull(jobIdsForFirstDeployment);
    for (String jobId : jobIdsForFirstDeployment) {
      managementService.executeJob(jobId);
    }

    // the process instances related to the first deployment should be deleted
    assertEquals(0, runtimeService.createProcessInstanceQuery().deploymentId(firstDeploymentId).count());
    assertHistoricTaskDeletionPresent(processInstanceIdsFromFirstDeployment, "test_reason", testRule);
    // and process instances related to the second deployment should not be deleted
    assertEquals(processInstanceIdsFromSecondDeployment.size(), runtimeService.createProcessInstanceQuery().deploymentId(secondDeploymentId).count());
    assertHistoricTaskDeletionPresent(processInstanceIdsFromSecondDeployment, null, testRule);

    // execute jobs related to the second deployment
    List<String> jobIdsForSecondDeployment = getJobIdsByDeployment(jobs, secondDeploymentId);
    assertNotNull(jobIdsForSecondDeployment);
    for (String jobId : jobIdsForSecondDeployment) {
      managementService.executeJob(jobId);
    }

    // all of the process instances should be deleted
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  private List<String> createProcessInstances(ProcessDefinition sourceDefinition1, ProcessDefinition sourceDefinition2, int instanceCountDef1, int instanceCountDef2) {
    List<String> processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < instanceCountDef1; i++) {
      ProcessInstance processInstance1 = runtimeService.startProcessInstanceById(sourceDefinition1.getId());
      processInstanceIds.add(processInstance1.getId());
      if (i < instanceCountDef2) {
        ProcessInstance processInstance2 = runtimeService.startProcessInstanceById(sourceDefinition2.getId());
        processInstanceIds.add(processInstance2.getId());
      }
    }
    return processInstanceIds;
  }

  private List<String> getProcessInstanceIdsByDeploymentId(final String deploymentId) {
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().deploymentId(deploymentId).list();
    List<String> processInstanceIds = new ArrayList<String>();
    for (ProcessInstance processInstance : processInstances) {
      processInstanceIds.add(processInstance.getId());
    }
    return processInstanceIds;
  }

  private List<String> getJobIdsByDeployment(List<Job> jobs, String deploymentId) {
    List<String> jobIdsForDeployment = new LinkedList<String>();
    for (int i = 0; i < jobs.size(); i++) {
      if (jobs.get(i).getDeploymentId().equals(deploymentId)) {
        jobIdsForDeployment.add(jobs.get(i).getId());
      }
    }
    return jobIdsForDeployment;
  }

  private void createAndExecuteSeedJobs(String seedJobDefinitionId, int expectedSeedJobsCount) {
    for (int i = 0; i <= expectedSeedJobsCount; i++) {
      Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();
      if (i != expectedSeedJobsCount) {
        assertNotNull(seedJob);
        managementService.executeJob(seedJob.getId());
      } else {
        //the last seed job should not trigger another seed job
        assertNull(seedJob);
      }
    }
  }
}
