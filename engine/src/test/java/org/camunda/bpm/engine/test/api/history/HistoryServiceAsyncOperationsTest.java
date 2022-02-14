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
package org.camunda.bpm.engine.test.api.history;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.AbstractAsyncOperationsTest;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsIn;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Askar Akhmerov
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoryServiceAsyncOperationsTest extends AbstractAsyncOperationsTest {

  protected static final String TEST_REASON = "test reason";

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected TaskService taskService;
  protected List<String> historicProcessInstances;

  @Before
  public void setup() {
    initDefaults(engineRule);
    taskService = engineRule.getTaskService();

    prepareData();
  }

  protected void prepareData() {
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml");
    startTestProcesses(2);

    for (Task activeTask : taskService.createTaskQuery().list()) {
      taskService.complete(activeTask.getId());
    }

    historicProcessInstances = new ArrayList<>();
    for (HistoricProcessInstance pi : historyService.createHistoricProcessInstanceQuery().list()) {
      historicProcessInstances.add(pi.getId());
    }
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithList() throws Exception {
    //when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(historicProcessInstances, TEST_REASON);

    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithListForDeletedDeployment() throws Exception {
    // given a second deployment
    prepareData();
    ProcessDefinitionQuery definitionQuery = engineRule.getRepositoryService().createProcessDefinitionQuery();
    String firstDeploymentId = definitionQuery.processDefinitionVersion(1).singleResult().getDeploymentId();
    String secondDeploymentId = definitionQuery.processDefinitionVersion(2).singleResult().getDeploymentId();
    engineRule.getRepositoryService().deleteDeployment(secondDeploymentId);

    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);

    // when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(historicProcessInstances, TEST_REASON);
    // then a seed job with the lowest deployment id exist
    Job seedJob = getSeedJob(batch);
    assertEquals(firstDeploymentId, seedJob.getDeploymentId());
    // when
    executeSeedJob(batch);
    // then
    seedJob = getSeedJob(batch);
    assertEquals(firstDeploymentId, seedJob.getDeploymentId());
    // when
    executeSeedJob(batch);
    // then batch jobs with different deployment ids exist
    JobQuery batchJobQuery = managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId());
    List<Job> batchJobs = batchJobQuery.list();
    assertThat(batchJobs.size(), is(2));
    assertThat(batchJobs.get(0).getDeploymentId(), anyOf(is(firstDeploymentId), is(nullValue())));
    assertThat(batchJobs.get(1).getDeploymentId(), anyOf(is(firstDeploymentId), is(nullValue())));
    assertThat(batchJobs.get(0).getDeploymentId(), is(not(batchJobs.get(1).getDeploymentId())));
    assertThat(historicProcessInstances.size(), is(4));
    assertThat(getHistoricProcessInstanceCountByDeploymentId(firstDeploymentId), is(2L));

    // when the batch jobs for the first deployment are executed
    getJobIdsByDeployment(batchJobs, firstDeploymentId).forEach(managementService::executeJob);
    // then the historic process instances related to the first deployment should be deleted
    assertThat(getHistoricProcessInstanceCountByDeploymentId(firstDeploymentId), is(0L));
    // and historic process instances related to the second deployment should not be deleted
    assertThat(historyService.createHistoricProcessInstanceQuery().count(), is(2L));

    // when the remaining batch jobs are executed
    batchJobQuery.list().forEach(j -> managementService.executeJob(j.getId()));
    // then
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithListInDifferentDeployments() throws Exception {
    // given a second deployment
    prepareData();
    ProcessDefinitionQuery definitionQuery = engineRule.getRepositoryService().createProcessDefinitionQuery();
    String firstDeploymentId = definitionQuery.processDefinitionVersion(1).singleResult().getDeploymentId();
    String secondDeploymentId = definitionQuery.processDefinitionVersion(2).singleResult().getDeploymentId();

    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);

    // when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(historicProcessInstances, TEST_REASON);
    executeSeedJobs(batch, 2);
    // then batch jobs with different deployment ids exist
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list();
    assertThat(batchJobs.size(), is(2));
    assertThat(batchJobs.get(0).getDeploymentId(), IsIn.isOneOf(firstDeploymentId, secondDeploymentId));
    assertThat(batchJobs.get(1).getDeploymentId(), IsIn.isOneOf(firstDeploymentId, secondDeploymentId));
    assertThat(batchJobs.get(0).getDeploymentId(), is(not(batchJobs.get(1).getDeploymentId())));
    assertThat(historicProcessInstances.size(), is(4));
    assertThat(getHistoricProcessInstanceCountByDeploymentId(firstDeploymentId), is(2L));
    assertThat(getHistoricProcessInstanceCountByDeploymentId(secondDeploymentId), is(2L));

    // when the batch jobs for the first deployment are executed
    getJobIdsByDeployment(batchJobs, firstDeploymentId).forEach(managementService::executeJob);
    // then the historic process instances related to the first deployment should be deleted
    assertThat(getHistoricProcessInstanceCountByDeploymentId(firstDeploymentId), is(0L));
    // and historic process instances related to the second deployment should not be deleted
    assertThat(getHistoricProcessInstanceCountByDeploymentId(secondDeploymentId), is(2L));

    // when the remaining batch jobs are executed
    getJobIdsByDeployment(batchJobs, secondDeploymentId).forEach(managementService::executeJob);
    // then
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithEmptyList() throws Exception {
    // when/then
    assertThatThrownBy(() -> historyService.deleteHistoricProcessInstancesAsync(new ArrayList<String>(), TEST_REASON))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithFake() throws Exception {
    //given
    ArrayList<String> processInstanceIds = new ArrayList<>();
    processInstanceIds.add(historicProcessInstances.get(0));
    processInstanceIds.add("aFakeId");

    //when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(processInstanceIds, TEST_REASON);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    //then
    assertThat(exceptions.size(), is(0));
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithQueryAndList() throws Exception {
    //given
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(historicProcessInstances.get(0));
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(
        historicProcessInstances.subList(1, historicProcessInstances.size()), query, TEST_REASON);
    completeSeedJobs(batch);

    //when
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithQuery() throws Exception {
    //given
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
        .processInstanceIds(new HashSet<>(historicProcessInstances));
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(query, TEST_REASON);
    completeSeedJobs(batch);

    //when
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithEmptyQuery() throws Exception {
    //given
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().unfinished();

    // when/then
    assertThatThrownBy(() -> historyService.deleteHistoricProcessInstancesAsync(query, TEST_REASON))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithNonExistingIDAsQuery() throws Exception {
    //given
    ArrayList<String> processInstanceIds = new ArrayList<>();
    processInstanceIds.add(historicProcessInstances.get(0));
    processInstanceIds.add("aFakeId");
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
        .processInstanceIds(new HashSet<>(processInstanceIds));

    //when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(query, TEST_REASON);
    completeSeedJobs(batch);
    executeBatchJobs(batch);

    //then
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithoutDeleteReason() throws Exception {
    //when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(historicProcessInstances, null);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    //then
    assertThat(exceptions.size(), is(0));
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithNullList() throws Exception {
    // when/then
    assertThatThrownBy(() -> historyService.deleteHistoricProcessInstancesAsync((List<String>) null, TEST_REASON))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithNullQuery() throws Exception {
    // when/then
    assertThatThrownBy(() -> historyService.deleteHistoricProcessInstancesAsync((HistoricProcessInstanceQuery) null, TEST_REASON))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void shouldSetInvocationsPerBatchType() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION, 42);

    //when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(historicProcessInstances, TEST_REASON);

    // then
    Assertions.assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);

    // clear
    engineRule.getProcessEngineConfiguration()
        .setInvocationsPerBatchJobByBatchType(new HashMap<>());
  }

  protected long getHistoricProcessInstanceCountByDeploymentId(String deploymentId) {
    // fetch process definitions of the deployment
    Set<String> processDefinitionIds = engineRule.getRepositoryService().createProcessDefinitionQuery()
        .deploymentId(deploymentId).list().stream()
        .map(ProcessDefinition::getId)
        .collect(Collectors.toSet());
    // return historic instances of the deployed definitions
    return historyService.createHistoricProcessInstanceQuery().list().stream()
        .filter(hpi -> processDefinitionIds.contains(hpi.getProcessDefinitionId()))
        .map(HistoricProcessInstance::getId)
        .count();
  }

  protected void assertNoHistoryForTasks() {
    if (!testRule.isHistoryLevelNone()) {
      Assert.assertThat(historyService.createHistoricTaskInstanceQuery().count(), CoreMatchers.is(0L));
    }
  }

  protected void assertAllHistoricProcessInstancesAreDeleted() {
    assertThat(historyService.createHistoricProcessInstanceQuery().count(), is(0L));
  }

}
