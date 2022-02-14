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

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.DEFAULT_TOPIC;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.createDefaultExternalTaskModel;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricExternalTaskLogTest {

  protected final String WORKER_ID = "aWorkerId";
  protected final String ERROR_MESSAGE = "This is an error!";
  protected final String ERROR_DETAILS = "These are the error details!";
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

  @After
  public void tearDown() {
    List<ExternalTask> list = externalTaskService.createExternalTaskQuery().workerId(WORKER_ID).list();
    for (ExternalTask externalTask : list) {
      externalTaskService.unlock(externalTask.getId());
    }
  }

  @Test
  public void testHistoricExternalTaskLogCreateProperties() {

    // given
    ExternalTask task = startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .creationLog()
      .singleResult();

    // then
    assertHistoricLogPropertiesAreProperlySet(task, log);
    assertEquals(null, log.getWorkerId());
    assertLogIsInCreatedState(log);

  }

  @Test
  public void testHistoricExternalTaskLogFailedProperties() {

    // given
    ExternalTask task = startExternalTaskProcess();
    reportExternalTaskFailure(task.getId());
    task = externalTaskService.createExternalTaskQuery().singleResult();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .singleResult();

    // then
    assertHistoricLogPropertiesAreProperlySet(task, null, log);
    assertEquals(WORKER_ID, log.getWorkerId());
    assertLogIsInFailedState(log);

  }

  @Test
  public void testHistoricExternalTaskLogSuccessfulProperties() {

    // given
    ExternalTask task = startExternalTaskProcess();
    completeExternalTask(task.getId());

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .successLog()
      .singleResult();

    // then
    assertHistoricLogPropertiesAreProperlySet(task, log);
    assertEquals(WORKER_ID, log.getWorkerId());
    assertLogIsInSuccessfulState(log);

  }

  @Test
  public void testHistoricExternalTaskLogDeletedProperties() {

    // given
    ExternalTask task = startExternalTaskProcess();
    runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "Dummy reason for deletion!");

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .deletionLog()
      .singleResult();

    // then
    assertHistoricLogPropertiesAreProperlySet(task, log);
    assertEquals(null, log.getWorkerId());
    assertLogIsInDeletedState(log);

  }

  @Test
  public void testRetriesAndWorkerIdWhenFirstFailureAndThenComplete() {

    // given
    ExternalTask task = startExternalTaskProcess();
    reportExternalTaskFailure(task.getId());
    completeExternalTask(task.getId());

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .successLog()
      .singleResult();

    // then
    assertEquals(WORKER_ID, log.getWorkerId());
    assertEquals(Integer.valueOf(1), log.getRetries());
    assertLogIsInSuccessfulState(log);
  }

  @Test
  public void testErrorDetails() {
    // given
    ExternalTask task = startExternalTaskProcess();
    reportExternalTaskFailure(task.getId());

    // when
    String failedHistoricExternalTaskLogId = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .singleResult()
      .getId();

    // then
    String stacktrace = historyService.getHistoricExternalTaskLogErrorDetails(failedHistoricExternalTaskLogId);
    assertNotNull(stacktrace);
    assertEquals(ERROR_DETAILS, stacktrace);
  }

  @Test
  public void testErrorDetailsWithTwoDifferentErrorsThrown() {
    // given
    ExternalTask task = startExternalTaskProcess();
    String firstErrorDetails = "Dummy error details!";
    String secondErrorDetails = ERROR_DETAILS;
    reportExternalTaskFailure(task.getId(), ERROR_MESSAGE, firstErrorDetails);
    ensureEnoughTimePassedByForTimestampOrdering();
    reportExternalTaskFailure(task.getId(), ERROR_MESSAGE, secondErrorDetails);

    // when
    List<HistoricExternalTaskLog> list = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .orderByTimestamp()
      .asc()
      .list();

    String firstFailedLogId = list.get(0).getId();
    String secondFailedLogId = list.get(1).getId();

    // then
    String stacktrace1 = historyService.getHistoricExternalTaskLogErrorDetails(firstFailedLogId);
    String stacktrace2 = historyService.getHistoricExternalTaskLogErrorDetails(secondFailedLogId);
    assertNotNull(stacktrace1);
    assertNotNull(stacktrace2);
    assertEquals(firstErrorDetails, stacktrace1);
    assertEquals(secondErrorDetails, stacktrace2);
  }


  @Test
  public void testGetExceptionStacktraceForNonexistentExternalTaskId() {
    try {
      historyService.getHistoricExternalTaskLogErrorDetails("foo");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      String expectedMessage = "No historic external task log found with id foo";
      assertTrue(re.getMessage().contains(expectedMessage));
    }
  }

  @Test
  public void testGetExceptionStacktraceForNullExternalTaskId() {
    try {
      historyService.getHistoricExternalTaskLogErrorDetails(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      String expectedMessage = "historicExternalTaskLogId is null";
      assertTrue(re.getMessage().contains(expectedMessage));
    }
  }

  @Test
  public void testErrorMessageTruncation() {
    // given
    String exceptionMessage = createStringOfLength(1000);
    ExternalTask task = startExternalTaskProcess();
    reportExternalTaskFailure(task.getId(), exceptionMessage, ERROR_DETAILS);

    // when
    HistoricExternalTaskLog failedLog = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .singleResult();

    String errorMessage = failedLog.getErrorMessage();
    String expectedErrorMessage = exceptionMessage.substring(0, ExternalTaskEntity.MAX_EXCEPTION_MESSAGE_LENGTH);

    // then
    assertNotNull(failedLog);
    assertEquals(ExternalTaskEntity.MAX_EXCEPTION_MESSAGE_LENGTH, errorMessage.length());
    assertEquals(expectedErrorMessage, errorMessage);

  }

  // helper

  protected void assertLogIsInCreatedState(HistoricExternalTaskLog log) {
    assertTrue(log.isCreationLog());
    assertFalse(log.isFailureLog());
    assertFalse(log.isSuccessLog());
    assertFalse(log.isDeletionLog());
  }

  protected void assertLogIsInFailedState(HistoricExternalTaskLog log) {
    assertFalse(log.isCreationLog());
    assertTrue(log.isFailureLog());
    assertFalse(log.isSuccessLog());
    assertFalse(log.isDeletionLog());
  }

  protected void assertLogIsInSuccessfulState(HistoricExternalTaskLog log) {
    assertFalse(log.isCreationLog());
    assertFalse(log.isFailureLog());
    assertTrue(log.isSuccessLog());
    assertFalse(log.isDeletionLog());
  }

  protected void assertLogIsInDeletedState(HistoricExternalTaskLog log) {
    assertFalse(log.isCreationLog());
    assertFalse(log.isFailureLog());
    assertFalse(log.isSuccessLog());
    assertTrue(log.isDeletionLog());
  }

  protected void assertHistoricLogPropertiesAreProperlySet(ExternalTask task, HistoricExternalTaskLog log) {
    assertHistoricLogPropertiesAreProperlySet(task, task.getRetries(), log);
  }

  protected void assertHistoricLogPropertiesAreProperlySet(ExternalTask task, Integer retries, HistoricExternalTaskLog log) {
    assertNotNull(log);
    assertNotNull(log.getId());
    assertNotNull(log.getTimestamp());

    assertEquals(task.getId(), log.getExternalTaskId());
    assertEquals(task.getActivityId(), log.getActivityId());
    assertEquals(task.getActivityInstanceId(), log.getActivityInstanceId());
    assertEquals(task.getTopicName(), log.getTopicName());
    assertEquals(retries, log.getRetries());
    assertEquals(task.getExecutionId(), log.getExecutionId());
    assertEquals(task.getProcessInstanceId(), log.getProcessInstanceId());
    assertEquals(task.getProcessDefinitionId(), log.getProcessDefinitionId());
    assertEquals(task.getProcessDefinitionKey(), log.getProcessDefinitionKey());
    assertEquals(task.getPriority(), log.getPriority());
  }

  protected void completeExternalTask(String externalTaskId) {
    externalTaskService.fetchAndLock(100, WORKER_ID, false)
      .topic(DEFAULT_TOPIC, LOCK_DURATION)
      .execute();
    externalTaskService.complete(externalTaskId, WORKER_ID);
  }

  protected void reportExternalTaskFailure(String externalTaskId) {
    reportExternalTaskFailure(externalTaskId, ERROR_MESSAGE, ERROR_DETAILS);
  }

  protected void reportExternalTaskFailure(String externalTaskId, String errorMessage, String errorDetails) {
    externalTaskService.fetchAndLock(100, WORKER_ID, false)
      .topic(DEFAULT_TOPIC, LOCK_DURATION)
      .execute();
    externalTaskService.handleFailure(externalTaskId, WORKER_ID, errorMessage, errorDetails, 1, 0L);
  }

  protected ExternalTask startExternalTaskProcess() {
    BpmnModelInstance oneExternalTaskProcess = createDefaultExternalTaskModel().build();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(oneExternalTaskProcess);
    ProcessInstance pi = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    return externalTaskService.createExternalTaskQuery().processInstanceId(pi.getId()).singleResult();
  }

  protected String createStringOfLength(int count) {
    return repeatString(count, "a");
  }

  protected String repeatString(int count, String with) {
    return new String(new char[count]).replace("\0", with);
  }

  protected void ensureEnoughTimePassedByForTimestampOrdering() {
    long timeToAddInSeconds = 5 * 1000L;
    Date nowPlus5Seconds = new Date(ClockUtil.getCurrentTime().getTime() + timeToAddInSeconds);
    ClockUtil.setCurrentTime(nowPlus5Seconds);
  }

}
