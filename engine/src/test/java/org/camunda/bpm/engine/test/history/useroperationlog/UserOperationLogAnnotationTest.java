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
package org.camunda.bpm.engine.test.history.useroperationlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(HISTORY_FULL)
public class UserOperationLogAnnotationTest {

  protected static final String USER_ID = "demo";
  protected static final String TASK_ID = "aTaskId";
  protected static final String ANNOTATION = "anAnnotation";
  protected static final String TASK_NAME = "aTaskName";
  protected static final String OPERATION_ID = "operationId";
  protected final Date CREATE_TIME = new Date(1363608000000L);

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule engineTestRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(engineTestRule);

  protected HistoryService historyService;
  protected TaskService taskService;

  @Before
  public void assignServices() {
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
  }

  @After
  public void clearDatabase() {
    taskService.deleteTask(TASK_ID, true);
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @Before
  public void setAuthentication() {
    engineRule.getIdentityService()
        .setAuthenticatedUserId(USER_ID);
  }

  @After
  public void clearAuthentication() {
    engineRule.getIdentityService()
        .clearAuthentication();
  }

  @Test
  public void shouldSetAnnotation() {
    // given
    createTask();

    UserOperationLogEntry userOperationLogEntry = historyService
        .createUserOperationLogQuery()
        .singleResult();

    // assume
    assertThat(userOperationLogEntry).isNotNull();

    // when
    historyService.setAnnotationForOperationLogById(userOperationLogEntry.getOperationId(), ANNOTATION);

    userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TASK)
        .singleResult();

    // then
    assertThat(userOperationLogEntry.getAnnotation()).isEqualTo(ANNOTATION);
  }

  /**
   * See https://app.camunda.com/jira/browse/CAM-10664
   */
  @Test
  public void shouldSetAnnotation_WithPreservedTimeStamp() {
    // given
    ClockUtil.setCurrentTime(CREATE_TIME);

    createTask();

    UserOperationLogEntry userOperationLogEntry = historyService
        .createUserOperationLogQuery()
        .singleResult();

    // assume
    assertThat(userOperationLogEntry).isNotNull();

    // when
    historyService.setAnnotationForOperationLogById(userOperationLogEntry.getOperationId(), ANNOTATION);

    userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TASK)
        .singleResult();

    // then
    assertThat(userOperationLogEntry.getAnnotation()).isEqualTo(ANNOTATION);
    assertThat(userOperationLogEntry.getTimestamp()).isEqualTo(CREATE_TIME);
  }

  @Test
  public void shouldSetAnnotationForAllEntries() {
    // given
    Task task = createTask();

    updateMultiplePropertiesOfTask(task);

    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_UPDATE)
        .list();

    // assume
    assertThat(userOperationLogEntries.size()).isEqualTo(2);

    String operationId = userOperationLogEntries.get(0)
        .getOperationId();

    // when
    historyService.setAnnotationForOperationLogById(operationId, ANNOTATION);

    userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_UPDATE)
        .list();

    // then
    assertThat(userOperationLogEntries.get(0).getAnnotation()).isEqualTo(ANNOTATION);
    assertThat(userOperationLogEntries.get(1).getAnnotation()).isEqualTo(ANNOTATION);
  }

  @Test
  public void shouldClearAnnotation() {
    // given
    createTask();

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .singleResult();

    // assume
    assertThat(userOperationLogEntry).isNotNull();

    historyService.setAnnotationForOperationLogById(userOperationLogEntry.getOperationId(), ANNOTATION);

    userOperationLogEntry = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE)
        .singleResult();

    assertThat(userOperationLogEntry.getAnnotation()).isEqualTo(ANNOTATION);

    // when
    historyService.clearAnnotationForOperationLogById(userOperationLogEntry.getOperationId());

    userOperationLogEntry = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE)
        .singleResult();

    // then
    assertThat(userOperationLogEntry.getAnnotation()).isNull();
  }

  @Test
  public void shouldClearAnnotationForAllEntries() {
    // given
    Task task = createTask();

    updateMultiplePropertiesOfTask(task);

    List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_UPDATE)
        .list();

    // assume
    assertThat(userOperationLogEntries.size()).isEqualTo(2);

    String operationId = userOperationLogEntries.get(0)
        .getOperationId();

    historyService.setAnnotationForOperationLogById(operationId, ANNOTATION);

    userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_UPDATE)
        .list();

    assertThat(userOperationLogEntries.get(0).getAnnotation()).isEqualTo(ANNOTATION);
    assertThat(userOperationLogEntries.get(1).getAnnotation()).isEqualTo(ANNOTATION);

    // when
    historyService.clearAnnotationForOperationLogById(operationId);

    userOperationLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_UPDATE)
        .list();

    // then
    assertThat(userOperationLogEntries.get(0).getAnnotation()).isNull();
    assertThat(userOperationLogEntries.get(1).getAnnotation()).isNull();
  }

  @Test
  public void shouldWriteOperationLogOnClearAnnotation() {
    // given
    createTask();

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .singleResult();

    String operationId = userOperationLogEntry.getOperationId();

    // assume
    assertThat(userOperationLogEntry).isNotNull();

    historyService.setAnnotationForOperationLogById(userOperationLogEntry.getOperationId(), ANNOTATION);

    userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.TASK)
        .singleResult();

    assertThat(userOperationLogEntry.getAnnotation()).isEqualTo(ANNOTATION);

    // when
    historyService.clearAnnotationForOperationLogById(userOperationLogEntry.getOperationId());

    userOperationLogEntry = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CLEAR_ANNOTATION)
        .singleResult();

    // then
    assertThat(userOperationLogEntry.getEntityType()).isEqualTo(EntityTypes.OPERATION_LOG);
    assertThat(userOperationLogEntry.getOperationType())
        .isEqualTo(UserOperationLogEntry.OPERATION_TYPE_CLEAR_ANNOTATION);
    assertThat(userOperationLogEntry.getProperty()).isEqualTo(OPERATION_ID);
    assertThat(userOperationLogEntry.getNewValue()).isEqualTo(operationId);
    assertThat(userOperationLogEntry.getUserId()).isEqualTo(USER_ID);
  }

  @Test
  public void shouldWriteOperationLogOnSetAnnotation() {
    // given
    createTask();

    UserOperationLogEntry userOperationLogEntry = historyService
        .createUserOperationLogQuery()
        .singleResult();

    // assume
    assertThat(userOperationLogEntry).isNotNull();

    // when
    historyService.setAnnotationForOperationLogById(userOperationLogEntry.getOperationId(), OPERATION_ID);

    String operationId = userOperationLogEntry.getOperationId();

    userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType(EntityTypes.OPERATION_LOG)
        .singleResult();

    // then
    assertThat(userOperationLogEntry.getEntityType()).isEqualTo(EntityTypes.OPERATION_LOG);
    assertThat(userOperationLogEntry.getOperationType())
        .isEqualTo(UserOperationLogEntry.OPERATION_TYPE_SET_ANNOTATION);
    assertThat(userOperationLogEntry.getProperty()).isEqualTo(OPERATION_ID);
    assertThat(userOperationLogEntry.getNewValue()).isEqualTo(operationId);
    assertThat(userOperationLogEntry.getUserId()).isEqualTo(USER_ID);
  }

  @Test
  public void shouldThrowExceptionWhenOperationIdNull() {
    // given

    // when/then
    assertThatThrownBy(() -> historyService.setAnnotationForOperationLogById(null, ANNOTATION))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("operation id is null");
  }

  @Test
  public void shouldThrowExceptionWhenOperationNull() {
    // given

    // when/then
    assertThatThrownBy(() -> historyService.setAnnotationForOperationLogById("anOperationId", ANNOTATION))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("operation is null");

  }

  // helper ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected void updateMultiplePropertiesOfTask(Task task) {
    task.setDueDate(new Date());
    task.setName(TASK_NAME);

    taskService.saveTask(task);
  }

  protected Task createTask() {
    Task task = taskService.newTask(TASK_ID);
    taskService.saveTask(task);
    return task;
  }

}
