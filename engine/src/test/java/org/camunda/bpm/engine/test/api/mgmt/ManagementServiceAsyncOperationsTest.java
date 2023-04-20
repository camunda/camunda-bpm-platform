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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.AbstractAsyncOperationsTest;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.collection.IsIn;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Askar Akhmerov
 */
public class ManagementServiceAsyncOperationsTest extends AbstractAsyncOperationsTest {
  protected static final int RETRIES = 5;
  protected static final java.lang.String TEST_PROCESS = "exceptionInJobExecution";

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected final Date TEST_DUE_DATE = new Date(1675752840000L);

  protected List<String> processInstanceIds;
  protected List<String> ids;

  boolean tearDownEnsureJobDueDateNotNull;

  @Before
  public void setup() {
    initDefaults(engineRule);
    prepareData();
  }

  protected void prepareData() {
    testRule.deploy("org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml");
    if (processInstanceIds == null) {
      processInstanceIds = new ArrayList<>();
    }
    processInstanceIds.addAll(startTestProcesses(2));
    ids = getAllJobIds();
  }

  @After
  public void tearDown() {
    processInstanceIds = null;
    if(tearDownEnsureJobDueDateNotNull) {
      engineConfiguration.setEnsureJobDueDateNotNull(false);
    }
  }

  @Test
  public void testSetJobsRetryAsyncWithJobList() throws Exception {
    //when
    Batch batch = managementService.setJobRetriesAsync(ids, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).isEmpty();
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void shouldSetInvocationsPerBatchTypeForJobsByJobIds() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_SET_JOB_RETRIES, 42);

    //when
    Batch batch = managementService.setJobRetriesAsync(ids, RETRIES);

    // then
    Assertions.assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);

    // clear
    engineRule.getProcessEngineConfiguration()
        .setInvocationsPerBatchJobByBatchType(new HashMap<>());
  }

  @Test
  public void testSetJobsRetryAsyncWithProcessList() throws Exception {
    //when
    Batch batch = managementService.setJobRetriesAsync(processInstanceIds, (ProcessInstanceQuery) null, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).isEmpty();
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithProcessListInDifferentDeployments() throws Exception {
    // given a second deployment
    prepareData();
    ProcessDefinitionQuery definitionQuery = engineRule.getRepositoryService().createProcessDefinitionQuery();
    String firstDeploymentId = definitionQuery.processDefinitionVersion(1).singleResult().getDeploymentId();
    String secondDeploymentId = definitionQuery.processDefinitionVersion(2).singleResult().getDeploymentId();

    engineRule.getProcessEngineConfiguration().setInvocationsPerBatchJob(2);

    // when
    Batch batch = managementService.setJobRetriesAsync(processInstanceIds, (ProcessInstanceQuery) null, RETRIES);
    executeSeedJobs(batch, 2);
    // then batch jobs with different deployment ids exist
    List<Job> batchJobs = managementService.createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list();
    assertThat(batchJobs).hasSize(2);
    Assert.assertThat(batchJobs.get(0).getDeploymentId(), IsIn.isOneOf(firstDeploymentId, secondDeploymentId));
    Assert.assertThat(batchJobs.get(1).getDeploymentId(), IsIn.isOneOf(firstDeploymentId, secondDeploymentId));
    assertThat(batchJobs.get(0).getDeploymentId()).isNotEqualTo(batchJobs.get(1).getDeploymentId());

    // when the batch jobs for the first deployment are executed
    assertThat(getJobCountWithUnchangedRetries()).isEqualTo(4L);
    getJobIdsByDeployment(batchJobs, firstDeploymentId).forEach(managementService::executeJob);
    // then the retries for jobs from process instances related to the first deployment should be changed
    assertThat(getJobCountWithUnchangedRetries()).isEqualTo(2L);

    // when the remaining batch jobs are executed
    getJobIdsByDeployment(batchJobs, secondDeploymentId).forEach(managementService::executeJob);
    // then
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithEmptyJobList() throws Exception {
    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync(new ArrayList<String>(), RETRIES))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSetJobsRetryAsyncWithEmptyProcessList() throws Exception {
    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync(new ArrayList<String>(), (ProcessInstanceQuery) null, RETRIES))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSetJobsRetryAsyncWithNonExistingJobID() throws Exception {
    //given
    ids.add("aFake");

    //when
    Batch batch = managementService.setJobRetriesAsync(ids, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    //then
    assertThat(exceptions).hasSize(1);
    assertRetries(getAllJobIds(), RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithNonExistingProcessID() throws Exception {
    //given
    processInstanceIds.add("aFake");

    //when
    Batch batch = managementService.setJobRetriesAsync(processInstanceIds, (ProcessInstanceQuery) null, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    //then
    assertThat(exceptions).hasSize(0);
    assertRetries(getAllJobIds(), RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithJobQueryAndList() throws Exception {
    //given
    List<String> extraPi = startTestProcesses(1);
    JobQuery query = managementService.createJobQuery().processInstanceId(extraPi.get(0));

    //when
    Batch batch = managementService.setJobRetriesAsync(ids, query, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    assertRetries(getAllJobIds(), RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithProcessQueryAndList() throws Exception {
    //given
    List<String> extraPi = startTestProcesses(1);
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceId(extraPi.get(0));

    //when
    Batch batch = managementService.setJobRetriesAsync(processInstanceIds, query, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    assertRetries(getAllJobIds(), RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithJobQuery() throws Exception {
    //given
    JobQuery query = managementService.createJobQuery();

    //when
    Batch batch = managementService.setJobRetriesAsync(query, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithProcessQuery() throws Exception {
    //given
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    //when
    Batch batch = managementService.setJobRetriesAsync(null, query, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void testSetJobsRetryAsyncWithHistoryProcessQuery() {
    //given
    HistoricProcessInstanceQuery historicProcessInstanceQuery =
        historyService.createHistoricProcessInstanceQuery();

    //when
    Batch batch = managementService.setJobRetriesAsync(null, null,
        historicProcessInstanceQuery, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void testSetJobsRetryAsyncWithRuntimeAndHistoryProcessQuery() {
    //given
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceIds.get(0));

    HistoricProcessInstanceQuery historicProcessInstanceQuery =
        historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceIds.get(1));

    //when
    Batch batch = managementService.setJobRetriesAsync(null, query,
        historicProcessInstanceQuery, RETRIES);
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithEmptyJobQuery() throws Exception {
    //given
    JobQuery query = managementService.createJobQuery().suspended();

    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync(query, RETRIES))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSetJobsRetryAsyncWithEmptyProcessQuery() throws Exception {
    //given
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().suspended();

    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync(null, query, RETRIES))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSetJobsRetryAsyncWithNonExistingIDAsJobQuery() throws Exception {
    //given
    JobQuery query = managementService.createJobQuery().jobId("aFake");

    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync(query, RETRIES))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSetJobsRetryAsyncWithNonExistingIDAsProcessQuery() throws Exception {
    //given
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceId("aFake");

    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync(null, query, RETRIES))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSetJobsRetryAsyncWithNullJobList() throws Exception {

    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync((List<String>) null, RETRIES))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSetJobsRetryAsyncWithNullJobQuery() throws Exception {
    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync((JobQuery) null, RETRIES))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSetJobsRetryAsyncWithNullProcessQuery() throws Exception {
    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync(null, (ProcessInstanceQuery) null, RETRIES))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSetJobsRetryAsyncWithNegativeRetries() throws Exception {
    //given
    JobQuery query = managementService.createJobQuery();

    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesAsync(query, -1))
      .isInstanceOf(ProcessEngineException.class);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void shouldSetJobDueDateOnJobRetryAsyncByJobQuery() {
    //given
    JobQuery query = managementService.createJobQuery();

    //when
    Batch batch = managementService.setJobRetriesByJobsAsync(RETRIES)
        .jobQuery(query)
        .dueDate(TEST_DUE_DATE).executeAsync();
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    for (String id : ids) {
      Job job = managementService.createJobQuery().jobId(id).singleResult();
      assertThat(job.getRetries()).isEqualTo(RETRIES);
      assertThat(job.getDuedate()).isEqualToIgnoringMillis(TEST_DUE_DATE);
    }
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void shouldSetJobDueDateOnJobRetryAsyncByProcessInstanceIds() {
    //given

    //when
    Batch batch = managementService.setJobRetriesByProcessAsync(RETRIES)
        .processInstanceIds(processInstanceIds)
        .dueDate(TEST_DUE_DATE).executeAsync();
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    for (String id : processInstanceIds) {
      Job job = managementService.createJobQuery().processInstanceId(id).singleResult();
      assertThat(job.getRetries()).isEqualTo(RETRIES);
      assertThat(job.getDuedate()).isEqualToIgnoringMillis(TEST_DUE_DATE);
    }
  }

  @Test
  public void shouldSetJobDueDateOnJobRetryAsyncByProcessInstanceQuery() {
    //given
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    //when
    Batch batch = managementService.setJobRetriesByProcessAsync(RETRIES).processInstanceQuery(query).dueDate(TEST_DUE_DATE).executeAsync();
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    for (String id : ids) {
      Job jobResult = managementService.createJobQuery().jobId(id).singleResult();
      assertThat(jobResult.getRetries()).isEqualTo(RETRIES);
      assertThat(jobResult.getDuedate()).isEqualToIgnoringMillis(TEST_DUE_DATE);
    }
  }


  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void shouldSetJobDueDateOnJobRetryAsyncByHistoricProcessInstanceQuery() {
    //given
    HistoricProcessInstanceQuery historicProcessInstanceQuery =
        historyService.createHistoricProcessInstanceQuery();

    //when
    Batch batch = managementService.setJobRetriesByProcessAsync(RETRIES)
        .historicProcessInstanceQuery(historicProcessInstanceQuery)
        .dueDate(TEST_DUE_DATE).executeAsync();
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    for (String id : ids) {
      Job jobResult = managementService.createJobQuery().jobId(id).singleResult();
      assertThat(jobResult.getRetries()).isEqualTo(RETRIES);
      assertThat(jobResult.getDuedate()).isEqualToIgnoringMillis(TEST_DUE_DATE);
    }
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void shouldSetDueDateNull() {
    // given
    engineConfiguration.setEnsureJobDueDateNotNull(false);
    HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();

    // assume
    List<Job> job = managementService.createJobQuery().list();
    assertThat(job.get(0).getDuedate()).isNotNull();
    assertThat(job.get(1).getDuedate()).isNotNull();

    // when
    Batch batch = managementService.setJobRetriesByProcessAsync(RETRIES)
        .historicProcessInstanceQuery(historicProcessInstanceQuery)
        .dueDate(null)
        .executeAsync();
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    for (String id : ids) {
      Job jobResult = managementService.createJobQuery().jobId(id).singleResult();
      assertThat(jobResult.getRetries()).isEqualTo(RETRIES);
      assertThat(jobResult.getDuedate()).isNull();
    }
  }

  @Test
  public void shouldSetJobDueDateOnJobRetryAsyncByJobIds() {
    //given

    //when
    Batch batch = managementService.setJobRetriesByJobsAsync(RETRIES).jobIds(ids).dueDate(TEST_DUE_DATE).executeAsync();
    completeSeedJobs(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions).hasSize(0);
    for (String id : ids) {
      Job jobResult = managementService.createJobQuery().jobId(id).singleResult();
      assertThat(jobResult.getRetries()).isEqualTo(RETRIES);
      assertThat(jobResult.getDuedate()).isEqualToIgnoringMillis(TEST_DUE_DATE);
    }
  }

  @Test
  public void shouldThrowErrorOnEmptySetRetryByJobsBuilderConfig() {
    // given

    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesByJobsAsync(RETRIES).executeAsync())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("050")
      .hasMessageContaining("You must specify at least one of jobIds or jobQuery.");
  }

  @Test
  public void shouldThrowErrorOnEmptySetRetryByProcessBuilderConfig() {
    // given

    // when/then
    assertThatThrownBy(() -> managementService.setJobRetriesByProcessAsync(RETRIES).executeAsync())
    .isInstanceOf(ProcessEngineException.class)
    .hasMessageContaining("051")
    .hasMessageContaining("You must specify at least one of or one of processInstanceIds, processInstanceQuery, or historicProcessInstanceQuery.");
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void shouldSetInvocationsPerBatchTypeForJobsByProcessInstanceIds() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_SET_JOB_RETRIES, 42);

    HistoricProcessInstanceQuery historicProcessInstanceQuery =
        historyService.createHistoricProcessInstanceQuery();

    //when
    Batch batch = managementService.setJobRetriesAsync(null, null,
        historicProcessInstanceQuery, RETRIES);

    // then
    Assertions.assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);

    // clear
    engineRule.getProcessEngineConfiguration()
        .setInvocationsPerBatchJobByBatchType(new HashMap<>());
  }

  protected List<String> getAllJobIds() {
    return getAllJobs().stream().map(Job::getId).collect(Collectors.toList());
  }

  protected List<Job> getAllJobs() {
    return managementService.createJobQuery().list().stream()
        .filter(j -> j.getProcessInstanceId() != null)
        .collect(Collectors.toList());
  }

  protected List<String> startTestProcesses(int numberOfProcesses) {
    ArrayList<String> ids = new ArrayList<>();

    for (int i = 0; i < numberOfProcesses; i++) {
      ids.add(runtimeService.startProcessInstanceByKey(TEST_PROCESS).getProcessInstanceId());
    }

    return ids;
  }

  protected void assertRetries(List<String> allJobIds, int i) {
    for (String id : allJobIds) {
      assertThat(managementService.createJobQuery().jobId(id).singleResult().getRetries()).isEqualTo(i);
    }
  }

  protected long getJobCountWithUnchangedRetries() {
    return getAllJobs().stream().filter(j -> j.getRetries() != RETRIES).count();
  }
}
