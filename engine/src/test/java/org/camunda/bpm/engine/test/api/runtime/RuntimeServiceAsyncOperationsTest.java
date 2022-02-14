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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.AbstractAsyncOperationsTest;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.api.runtime.util.IncrementCounterListener;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;


/**
 * @author Askar Akhmerov
 */
public class RuntimeServiceAsyncOperationsTest extends AbstractAsyncOperationsTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public RuleChain migrationChain = RuleChain.outerRule(testRule).around(migrationRule);

  @Before
  public void setup() {
    initDefaults(engineRule);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithList() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE);

    completeSeedJobs(batch);
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

    executeSeedJobs(batch, 2);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();

    // cleanup
    if (!testRule.isHistoryLevelNone()) {
      batch = historyService.deleteHistoricProcessInstancesAsync(processIds, null);
      executeSeedJobs(batch, 2);
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

    completeSeedJobs(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithFake() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    processIds.add("aFake");

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE);

    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertEquals(0, exceptions.size());

    assertThat(managementService.createJobQuery().withException().list().size()).isEqualTo(0);

    processIds.remove("aFake");
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNullList() throws Exception {

    // when/then
    assertThatThrownBy(() -> runtimeService.deleteProcessInstancesAsync(null, null, TESTING_INSTANCE_DELETE))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("processInstanceIds is empty");

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithEmptyList() throws Exception {

    // when/then
    assertThatThrownBy(() -> runtimeService.deleteProcessInstancesAsync(new ArrayList<String>(), null, TESTING_INSTANCE_DELETE))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("processInstanceIds is empty");

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithQuery() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery().processInstanceIds(new HashSet<>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(null, processInstanceQuery, TESTING_INSTANCE_DELETE);

    completeSeedJobs(batch);
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
        .createProcessInstanceQuery().processInstanceIds(new HashSet<>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processInstanceQuery, TESTING_INSTANCE_DELETE);

    completeSeedJobs(batch);
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
        .createProcessInstanceQuery().processInstanceIds(new HashSet<>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(null, processInstanceQuery, null);

    completeSeedJobs(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, "deleted", testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithHistoryQuery() {
    // given
    List<String> processIds = startTestProcesses(2);
    HistoricProcessInstanceQuery historicProcessInstanceQuery =
        historyService.createHistoricProcessInstanceQuery()
            .processInstanceIds(new HashSet<>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(null, null,
        historicProcessInstanceQuery, "", false, false);

    completeSeedJobs(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, "deleted", testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithRuntimeAndHistoryQuery() {
    // given
    List<String> processIds = startTestProcesses(2);
    HistoricProcessInstanceQuery historicProcessInstanceQuery =
        historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processIds.get(0));

    ProcessInstanceQuery processInstanceQuery =
        runtimeService.createProcessInstanceQuery().processInstanceId(processIds.get(1));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(null, processInstanceQuery,
        historicProcessInstanceQuery, "", false, false);

    completeSeedJobs(batch);
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

    // when/then
    assertThatThrownBy(() -> runtimeService.deleteProcessInstancesAsync(null, null, TESTING_INSTANCE_DELETE))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("processInstanceIds is empty");
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithInvalidQueryParameter() throws Exception {
    // given
    startTestProcesses(2);
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey("invalid");

    // when/then
    assertThatThrownBy(() -> runtimeService.deleteProcessInstancesAsync(null, query, TESTING_INSTANCE_DELETE))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("processInstanceIds is empty");

  }

  protected void assertProcessInstancesAreDeleted() {
    assertThat(runtimeService.createProcessInstanceQuery().list().size()).isEqualTo(0);
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
    completeSeedJobs(batch);
    executeBatchJobs(batch);

    // then
    assertThat(IncrementCounterListener.counter).isEqualTo(0);
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
    completeSeedJobs(batch);
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
    completeSeedJobs(batch);
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
    completeSeedJobs(batch);
    executeBatchJobs(batch);

    // then
    assertThat(IncrementCounterListener.counter).isEqualTo(1);
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

    // seed jobs
    int expectedSeedJobsCount = 5;
    executeSeedJobs(batch, expectedSeedJobsCount);

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

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void shouldSetInvocationsPerBatchType() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_PROCESS_INSTANCE_DELETION, 42);

    List<String> processIds = startTestProcesses(2);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE);

    // then
    Assertions.assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);

    // clear
    engineRule.getProcessEngineConfiguration()
        .setInvocationsPerBatchJobByBatchType(new HashMap<>());
  }

  private List<String> createProcessInstances(ProcessDefinition sourceDefinition1, ProcessDefinition sourceDefinition2, int instanceCountDef1, int instanceCountDef2) {
    List<String> processInstanceIds = new ArrayList<>();
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
    List<String> processInstanceIds = new ArrayList<>();
    for (ProcessInstance processInstance : processInstances) {
      processInstanceIds.add(processInstance.getId());
    }
    return processInstanceIds;
  }
}
