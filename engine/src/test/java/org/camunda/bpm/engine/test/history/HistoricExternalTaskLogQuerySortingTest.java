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
package org.camunda.bpm.engine.test.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskByTimestamp;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByActivityId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByActivityInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByExecutionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByExternalTaskId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByPriority;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByProcessDefinitionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByProcessDefinitionKey;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByProcessInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByRetries;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByTopicName;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicExternalTaskLogByWorkerId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.DEFAULT_TOPIC;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.createDefaultExternalTaskModel;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil;
import org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.NullTolerantComparator;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricExternalTaskLogQuerySortingTest {

  protected final String WORKER_ID = "aWorkerId";
  protected final long LOCK_DURATION = 5 * 60L * 1000L;

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  protected ProcessInstance processInstance;
  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ExternalTaskService externalTaskService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    externalTaskService = engineRule.getExternalTaskService();
  }

  @Test
  public void testQuerySortingByTimestampAsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByTimestamp()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskByTimestamp());
  }

  @Test
  public void testQuerySortingByTimestampDsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByTimestamp()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskByTimestamp()));
  }

  @Test
  public void testQuerySortingByTaskIdAsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByExternalTaskId()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByExternalTaskId());
  }

  @Test
  public void testQuerySortingByTaskIdDsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByExternalTaskId()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByExternalTaskId()));
  }

  @Test
  public void testQuerySortingByRetriesAsc() {

    // given
    int taskCount = 10;
    List<ExternalTask> list = startProcesses(taskCount);
    reportExternalTaskFailure(list);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .failureLog()
      .orderByRetries()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByRetries());
  }

  @Test
  public void testQuerySortingByRetriesDsc() {

    // given
    int taskCount = 10;
    List<ExternalTask> list = startProcesses(taskCount);
    reportExternalTaskFailure(list);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .failureLog()
      .orderByRetries()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByRetries()));
  }

  @Test
  public void testQuerySortingByPriorityAsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByPriority()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByPriority());
  }

  @Test
  public void testQuerySortingByPriorityDsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByPriority()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByPriority()));
  }

  @Test
  public void testQuerySortingByTopicNameAsc() {

    // given
    int taskCount = 10;
    startProcessesByTopicName(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByTopicName()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByTopicName());
  }

  @Test
  public void testQuerySortingByTopicNameDsc() {

    // given
    int taskCount = 10;
    startProcessesByTopicName(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByTopicName()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByTopicName()));
  }

  @Test
  public void testQuerySortingByWorkerIdAsc() {

    // given
    int taskCount = 10;
    List<ExternalTask> list = startProcesses(taskCount);
    completeExternalTasksWithWorkers(list);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .successLog()
      .orderByWorkerId()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByWorkerId());
  }

  @Test
  public void testQuerySortingByWorkerIdDsc() {

    // given
    int taskCount = 10;
    List<ExternalTask> list = startProcesses(taskCount);
    completeExternalTasksWithWorkers(list);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .successLog()
      .orderByWorkerId()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByWorkerId()));
  }

  @Test
  public void testQuerySortingByActivityIdAsc() {

    // given
    int taskCount = 10;
    startProcessesByActivityId(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByActivityId()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByActivityId());
  }

  @Test
  public void testQuerySortingByActivityIdDsc() {

    // given
    int taskCount = 10;
    startProcessesByActivityId(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByActivityId()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByActivityId()));
  }

  @Test
  public void testQuerySortingByActivityInstanceIdAsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByActivityInstanceId()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByActivityInstanceId());
  }

  @Test
  public void testQuerySortingByActivityInstanceIdDsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByActivityInstanceId()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByActivityInstanceId()));
  }

  @Test
  public void testQuerySortingByExecutionIdAsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByExecutionId()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByExecutionId());
  }


  @Test
  public void testQuerySortingByExecutionIdDsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByExecutionId()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByExecutionId()));
  }

  @Test
  public void testQuerySortingByProcessInstanceIdAsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByProcessInstanceId()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByProcessInstanceId());
  }


  @Test
  public void testQuerySortingByProcessInstanceIdDsc() {

    // given
    int taskCount = 10;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByProcessInstanceId()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByProcessInstanceId()));
  }

  @Test
  public void testQuerySortingByProcessDefinitionIdAsc() {

    // given
    int taskCount = 8;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByProcessDefinitionId()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByProcessDefinitionId());
  }


  @Test
  public void testQuerySortingByProcessDefinitionIdDsc() {

    // given
    int taskCount = 8;
    startProcesses(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByProcessDefinitionId()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByProcessDefinitionId()));
  }

  @Test
  public void testQuerySortingByProcessDefinitionKeyAsc() {

    // given
    int taskCount = 10;
    startProcessesByProcessDefinitionKey(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByProcessDefinitionKey()
      .asc();

    // then
    verifyQueryWithOrdering(query, taskCount, historicExternalTaskLogByProcessDefinitionKey(engineRule.getProcessEngine()));
  }


  @Test
  public void testQuerySortingByProcessDefinitionKeyDsc() {

    // given
    int taskCount = 10;
    startProcessesByProcessDefinitionKey(taskCount);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();
    query
      .orderByProcessDefinitionKey()
      .desc();

    // then
    verifyQueryWithOrdering(query, taskCount, inverted(historicExternalTaskLogByProcessDefinitionKey(engineRule.getProcessEngine())));
  }

  // helper ------------------------------------

  protected void completeExternalTasksWithWorkers(List<ExternalTask> taskLIst) {
    for (Integer i=0; i<taskLIst.size(); i++) {
      completeExternalTaskWithWorker(taskLIst.get(i).getId(), i.toString());
    }
  }

  protected void completeExternalTaskWithWorker(String externalTaskId, String workerId) {
    completeExternalTask(externalTaskId, DEFAULT_TOPIC, workerId, false);

  }

  protected void completeExternalTask(String externalTaskId, String topic, String workerId, boolean usePriority) {
    List<LockedExternalTask> list = externalTaskService.fetchAndLock(100, workerId, usePriority)
      .topic(topic, LOCK_DURATION)
      .execute();
    externalTaskService.complete(externalTaskId, workerId);
    // unlock the remaining tasks
    for (LockedExternalTask lockedExternalTask : list) {
      if (!lockedExternalTask.getId().equals(externalTaskId)) {
        externalTaskService.unlock(lockedExternalTask.getId());
      }
    }
  }

  protected void reportExternalTaskFailure(List<ExternalTask> taskLIst) {
    for (Integer i=0; i<taskLIst.size(); i++) {
      reportExternalTaskFailure(taskLIst.get(i).getId(), DEFAULT_TOPIC, WORKER_ID, i+1, false, "foo");
    }
  }

  protected void reportExternalTaskFailure(String externalTaskId, String topic, String workerId, Integer retries, boolean usePriority, String errorMessage) {
    List<LockedExternalTask> list = externalTaskService.fetchAndLock(100, workerId, usePriority)
      .topic(topic, LOCK_DURATION)
      .execute();
    externalTaskService.handleFailure(externalTaskId, workerId, errorMessage, retries, 0L);

    for (LockedExternalTask lockedExternalTask : list) {
      externalTaskService.unlock(lockedExternalTask.getId());
    }
  }

  protected List<ExternalTask> startProcesses(int count) {
    List<ExternalTask> list = new LinkedList<ExternalTask>();
    for (int ithPrio = 0; ithPrio < count; ithPrio++) {
      list.add(startExternalTaskProcessGivenPriority(ithPrio));
      ensureEnoughTimePassedByForTimestampOrdering();
    }
    return list;
  }

  protected List<ExternalTask> startProcessesByTopicName(int count) {
    List<ExternalTask> list = new LinkedList<ExternalTask>();
    for (Integer ithTopic = 0; ithTopic < count; ithTopic++) {
      list.add(startExternalTaskProcessGivenTopicName(ithTopic.toString()));
    }
    return list;
  }

  protected List<ExternalTask> startProcessesByActivityId(int count) {
    List<ExternalTask> list = new LinkedList<ExternalTask>();
    for (Integer ithTopic = 0; ithTopic < count; ithTopic++) {
      list.add(startExternalTaskProcessGivenActivityId("Activity" + ithTopic.toString()));
    }
    return list;
  }

  protected List<ExternalTask> startProcessesByProcessDefinitionKey(int count) {
    List<ExternalTask> list = new LinkedList<ExternalTask>();
    for (Integer ithTopic = 0; ithTopic < count; ithTopic++) {
      list.add(startExternalTaskProcessGivenProcessDefinitionKey("ProcessKey" + ithTopic.toString()));
    }
    return list;
  }

  protected ExternalTask startExternalTaskProcessGivenTopicName(String topicName) {
    BpmnModelInstance processModelWithCustomTopic = createDefaultExternalTaskModel().topic(topicName).build();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(processModelWithCustomTopic);
    ProcessInstance pi = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    return externalTaskService.createExternalTaskQuery().processInstanceId(pi.getId()).singleResult();
  }

  protected ExternalTask startExternalTaskProcessGivenActivityId(String activityId) {
    BpmnModelInstance processModelWithCustomActivityId = createDefaultExternalTaskModel().externalTaskName(activityId).build();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(processModelWithCustomActivityId);
    ProcessInstance pi = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    return externalTaskService.createExternalTaskQuery().processInstanceId(pi.getId()).singleResult();
  }

  protected ExternalTask startExternalTaskProcessGivenProcessDefinitionKey(String processDefinitionKey) {
    BpmnModelInstance processModelWithCustomKey = createDefaultExternalTaskModel().processKey(processDefinitionKey).build();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(processModelWithCustomKey);
    ProcessInstance pi = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    return externalTaskService.createExternalTaskQuery().processInstanceId(pi.getId()).singleResult();
  }

  protected ExternalTask startExternalTaskProcessGivenPriority(int priority) {
    BpmnModelInstance processModelWithCustomPriority = createDefaultExternalTaskModel().priority(priority).build();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(processModelWithCustomPriority);
    ProcessInstance pi = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    return externalTaskService.createExternalTaskQuery().processInstanceId(pi.getId()).singleResult();
  }

  protected void verifyQueryWithOrdering(HistoricExternalTaskLogQuery query, int countExpected, NullTolerantComparator<HistoricExternalTaskLog> expectedOrdering) {
    assertThat(query.list()).hasSize(countExpected);
    assertThat(query.count()).isEqualTo(countExpected);
    TestOrderingUtil.verifySorting(query.list(), expectedOrdering);
  }

  protected void ensureEnoughTimePassedByForTimestampOrdering() {
    long timeToAddInSeconds = 5 * 1000L;
    Date nowPlus5Seconds = new Date(ClockUtil.getCurrentTime().getTime() + timeToAddInSeconds);
    ClockUtil.setCurrentTime(nowPlus5Seconds);
  }

}
