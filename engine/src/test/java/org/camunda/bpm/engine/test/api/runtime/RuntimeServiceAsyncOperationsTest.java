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
import org.camunda.bpm.engine.test.bpmn.multiinstance.RecordInvocationListener;

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
    Batch batch = managementService.createBatchQuery().singleResult();
    if (batch != null) {
      managementService.deleteBatch(batch.getId(), true);
    }

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }

    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(1);
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
    ProcessDefinition sourceDefinition = testRule
        .deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "ONE_TASK_PROCESS"));
    ProcessDefinition sourceDefinition2 = testRule
        .deployAndGetDefinition(modify(ProcessModels.TWO_TASKS_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "TWO_TASKS_PROCESS"));
    ProcessInstance processInstance1 = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    ProcessInstance processInstance2 = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition2.getId());
    ProcessInstance processInstance3 = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    ProcessInstance processInstance4 = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition2.getId());
    ProcessInstance processInstance5 = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    // given
    List<String> processInstanceIds = Arrays.asList(processInstance1.getId(), processInstance2.getId(), processInstance3.getId(), processInstance4.getId(),
        processInstance5.getId());
    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processInstanceIds, null, "test_reason");

    String seedJobDefinitionId = batch.getSeedJobDefinitionId();
    Job seedJob = managementService.createJobQuery().jobDefinitionId(seedJobDefinitionId).singleResult();
    assertNotNull(seedJob);
    // seed job
    managementService.executeJob(seedJob.getId());

    // then
    List<Job> list = managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list();
    assertEquals(4, list.size());
    assertEquals(sourceDefinition.getDeploymentId(), list.get(0).getDeploymentId());
    assertEquals(sourceDefinition2.getDeploymentId(), list.get(3).getDeploymentId());

    managementService.executeJob(list.get(0).getId());
    int jobNumToBeExecuted;
    if (list.get(1).getDeploymentId().equals(sourceDefinition.getDeploymentId())) {
      managementService.executeJob(list.get(1).getId());
      jobNumToBeExecuted = 2;
    } else {
      managementService.executeJob(list.get(2).getId());
      jobNumToBeExecuted = 1;
    }
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    assertHistoricTaskDeletionPresent(Arrays.asList(processInstance1.getId(), processInstance3.getId(), processInstance5.getId()), "test_reason", testRule);
    managementService.executeJob(list.get(jobNumToBeExecuted).getId());
    managementService.executeJob(list.get(3).getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
}
