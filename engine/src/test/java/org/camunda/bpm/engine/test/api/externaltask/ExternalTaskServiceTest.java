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
package org.camunda.bpm.engine.test.api.externaltask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.AssertUtil;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskServiceTest extends PluggableProcessEngineTest {

  protected static final String WORKER_ID = "aWorkerId";
  protected static final long LOCK_TIME = 10000L;
  protected static final String TOPIC_NAME = "externalTaskTopic";
  protected static final String ERROR_MESSAGE = "error message";
  protected static final String ERROR_DETAILS = "error details";

  protected SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");

  @Before
  public void setUp() throws Exception {
    // get rid of the milliseconds because of MySQL datetime precision
    Date now = formatter.parse(formatter.format(new Date()));
    ClockUtil.setCurrentTime(now);
  }

  @After
  public void tearDown() throws Exception {
    ClockUtil.reset();
  }

  @Test
  public void testFailOnMalformedpriorityInput() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/externaltask/externalTaskInvalidPriority.bpmn20.xml")
        .deploy();
      fail("deploying a process with malformed priority should not succeed");
    } catch (ParseException e) {
      testRule.assertTextPresentIgnoreCase("value 'NOTaNumber' for attribute 'taskPriority' "
          + "is not a valid number", e.getMessage());
      assertThat(e.getResorceReports().get(0).getErrors().get(0).getMainElementId()).isEqualTo("externalTaskWithPrio");
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetch() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    assertEquals(1, externalTasks.size());

    LockedExternalTask task = externalTasks.get(0);
    assertNotNull(task.getId());
    assertEquals(processInstance.getId(), task.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), task.getProcessDefinitionId());
    assertEquals("externalTask", task.getActivityId());
    assertEquals("oneExternalTaskProcess", task.getProcessDefinitionKey());
    assertEquals(TOPIC_NAME, task.getTopicName());

    ActivityInstance activityInstance = runtimeService
      .getActivityInstance(processInstance.getId())
      .getActivityInstances("externalTask")[0];

    assertEquals(activityInstance.getId(), task.getActivityInstanceId());
    assertEquals(activityInstance.getExecutionIds()[0], task.getExecutionId());

    AssertUtil.assertEqualsSecondPrecision(nowPlus(LOCK_TIME), task.getLockExpirationTime());

    assertEquals(WORKER_ID, task.getWorkerId());
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskWithPriorityProcess.bpmn20.xml")
  @Test
  public void testFetchWithPriority() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID, true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    assertEquals(1, externalTasks.size());

    LockedExternalTask task = externalTasks.get(0);
    assertNotNull(task.getId());
    assertEquals(processInstance.getId(), task.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), task.getProcessDefinitionId());
    assertEquals("externalTaskWithPrio", task.getActivityId());
    assertEquals("twoExternalTaskWithPriorityProcess", task.getProcessDefinitionKey());
    assertEquals(TOPIC_NAME, task.getTopicName());
    assertEquals(7, task.getPriority());

    ActivityInstance activityInstance = runtimeService
      .getActivityInstance(processInstance.getId())
      .getActivityInstances("externalTaskWithPrio")[0];

    assertEquals(activityInstance.getId(), task.getActivityInstanceId());
    assertEquals(activityInstance.getExecutionIds()[0], task.getExecutionId());

    AssertUtil.assertEqualsSecondPrecision(nowPlus(LOCK_TIME), task.getLockExpirationTime());

    assertEquals(WORKER_ID, task.getWorkerId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityProcess.bpmn20.xml")
  @Test
  public void testFetchProcessWithPriority() {
    // given
    runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(2, WORKER_ID, true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(2, externalTasks.size());

    // then
    //task with no prio gets prio defined by process
    assertEquals(9, externalTasks.get(0).getPriority());
    //task with own prio overrides prio defined by process
    assertEquals(7, externalTasks.get(1).getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityExpressionProcess.bpmn20.xml")
  @Test
  public void testFetchProcessWithPriorityExpression() {
    // given
    runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess",
                                             Variables.createVariables().putValue("priority", 18));

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(2, WORKER_ID, true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(2, externalTasks.size());

    // then
    //task with no prio gets prio defined by process
    assertEquals(18, externalTasks.get(0).getPriority());
    //task with own prio overrides prio defined by process
    assertEquals(7, externalTasks.get(1).getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityExpression.bpmn20.xml")
  @Test
  public void testFetchWithPriorityExpression() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess",
                                                        Variables.createVariables().putValue("priority", 18));
    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID, true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    assertEquals(1, externalTasks.size());

    LockedExternalTask task = externalTasks.get(0);
    assertNotNull(task.getId());
    assertEquals(processInstance.getId(), task.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), task.getProcessDefinitionId());
    assertEquals("externalTaskWithPrio", task.getActivityId());
    assertEquals("twoExternalTaskWithPriorityProcess", task.getProcessDefinitionKey());
    assertEquals(TOPIC_NAME, task.getTopicName());
    assertEquals(18, task.getPriority());

    ActivityInstance activityInstance = runtimeService
      .getActivityInstance(processInstance.getId())
      .getActivityInstances("externalTaskWithPrio")[0];

    assertEquals(activityInstance.getId(), task.getActivityInstanceId());
    assertEquals(activityInstance.getExecutionIds()[0], task.getExecutionId());

    AssertUtil.assertEqualsSecondPrecision(nowPlus(LOCK_TIME), task.getLockExpirationTime());

    assertEquals(WORKER_ID, task.getWorkerId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskWithPriorityProcess.bpmn20.xml")
  @Test
  public void testFetchWithPriorityOrdering() {
    // given
    runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(2, WORKER_ID, true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    assertEquals(2, externalTasks.size());
    assertTrue(externalTasks.get(0).getPriority() > externalTasks.get(1).getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskWithPriorityProcess.bpmn20.xml")
  @Test
  public void testFetchNextWithPriority() {
    // given
    runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID, true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then the task is locked
    assertEquals(1, externalTasks.size());

    LockedExternalTask task = externalTasks.get(0);
    long firstPrio = task.getPriority();
    AssertUtil.assertEqualsSecondPrecision(nowPlus(LOCK_TIME), task.getLockExpirationTime());

    // another task with next higher priority can be claimed
    externalTasks = externalTaskService.fetchAndLock(1, "anotherWorkerId", true)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
    assertEquals(1, externalTasks.size());
    assertTrue(firstPrio >= externalTasks.get(0).getPriority());

    // the expiration time expires
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    //first can be claimed
    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID, true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(1, externalTasks.size());
    assertEquals(firstPrio, externalTasks.get(0).getPriority());
  }

  @Deployment
  @Test
  public void testFetchTopicSelection() {
    // given
    runtimeService.startProcessInstanceByKey("twoTopicsProcess");

    // when
    List<LockedExternalTask> topic1Tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic("topic1", LOCK_TIME)
        .execute();

    List<LockedExternalTask> topic2Tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic("topic2", LOCK_TIME)
        .execute();

    // then
    assertEquals(1, topic1Tasks.size());
    assertEquals("topic1", topic1Tasks.get(0).getTopicName());

    assertEquals(1, topic2Tasks.size());
    assertEquals("topic2", topic2Tasks.get(0).getTopicName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchWithoutTopicName() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    try {
      externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(null, LOCK_TIME)
        .execute();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("topicName is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchNullWorkerId() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    try {
      externalTaskService.fetchAndLock(1, null)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("workerId is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchNegativeNumberOfTasks() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    try {
      externalTaskService.fetchAndLock(-1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("maxResults is not greater than or equal to 0", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchLessTasksThanExist() {
    // given
    for (int i = 0; i < 10; i++) {
      runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    }

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    assertEquals(5, externalTasks.size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchNegativeLockTime() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    try {
      externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, -1L)
        .execute();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("lockTime is not greater than 0", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchZeroLockTime() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    try {
      externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, 0L)
        .execute();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("lockTime is not greater than 0", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchNoTopics() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .execute();

    // then
    assertEquals(0, tasks.size());
  }

  @Deployment
  @Test
  public void testFetchVariables() {
    // given
    runtimeService.startProcessInstanceByKey("subProcessExternalTask",
          Variables.createVariables().putValue("processVar1", 42).putValue("processVar2", 43));

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .variables("processVar1", "subProcessVar", "taskVar")
      .execute();

    // then
    LockedExternalTask task = externalTasks.get(0);
    VariableMap variables = task.getVariables();
    assertEquals(3, variables.size());

    assertEquals(42, variables.get("processVar1"));
    assertEquals(44L, variables.get("subProcessVar"));
    assertEquals(45L, variables.get("taskVar"));

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchVariables.bpmn20.xml")
  @Test
  public void testShouldNotFetchSerializedVariables() {
    // given
    ExternalTaskCustomValue customValue = new ExternalTaskCustomValue();
    customValue.setTestValue("value1");
    runtimeService.startProcessInstanceByKey("subProcessExternalTask",
        Variables.createVariables().putValue("processVar1", customValue));

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .variables("processVar1")
        .execute();

    // then
    LockedExternalTask task = externalTasks.get(0);
    VariableMap variables = task.getVariables();
    assertEquals(1, variables.size());

    try {
      variables.get("processVar1");
      fail("did not receive an exception although variable was serialized");
    } catch (IllegalStateException e) {
      assertEquals("Object is not deserialized.", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchVariables.bpmn20.xml")
  @Test
  public void testFetchSerializedVariables() {
    // given
    ExternalTaskCustomValue customValue = new ExternalTaskCustomValue();
    customValue.setTestValue("value1");
    runtimeService.startProcessInstanceByKey("subProcessExternalTask",
        Variables.createVariables().putValue("processVar1", customValue));

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .variables("processVar1")
        .enableCustomObjectDeserialization()
        .execute();

    // then
    LockedExternalTask task = externalTasks.get(0);
    VariableMap variables = task.getVariables();
    assertEquals(1, variables.size());

    final ExternalTaskCustomValue receivedCustomValue = (ExternalTaskCustomValue) variables.get("processVar1");
    assertNotNull(receivedCustomValue);
    assertNotNull(receivedCustomValue.getTestValue());
    assertEquals("value1", receivedCustomValue.getTestValue());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskVariablesTest.testExternalTaskVariablesLocal.bpmn20.xml" })
  @Test
  public void testFetchOnlyLocalVariables() {

    VariableMap globalVars = Variables.putValue("globalVar", "globalVal");

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", globalVars);

    final String workerId = "workerId";
    final String topicName = "testTopic";

    List<LockedExternalTask> lockedExternalTasks = externalTaskService.fetchAndLock(10, workerId)
      .topic(topicName, 60000)
      .execute();

    assertEquals(1, lockedExternalTasks.size());

    LockedExternalTask lockedExternalTask = lockedExternalTasks.get(0);
    VariableMap variables = lockedExternalTask.getVariables();
    assertEquals(2, variables.size());
    assertEquals("globalVal", variables.getValue("globalVar", String.class));
    assertEquals("localVal", variables.getValue("localVar", String.class));

    externalTaskService.unlock(lockedExternalTask.getId());

    lockedExternalTasks = externalTaskService.fetchAndLock(10, workerId)
      .topic(topicName, 60000)
      .variables("globalVar", "localVar")
      .localVariables()
      .execute();

    assertEquals(1, lockedExternalTasks.size());

    lockedExternalTask = lockedExternalTasks.get(0);
    variables = lockedExternalTask.getVariables();
    assertEquals(1, variables.size());
    assertEquals("localVal", variables.getValue("localVar", String.class));
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskVariablesTest.testExternalTaskVariablesLocal.bpmn20.xml" })
  @Test
  public void testFetchNonExistingLocalVariables() {

    VariableMap globalVars = Variables.putValue("globalVar", "globalVal");

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", globalVars);

    final String workerId = "workerId";
    final String topicName = "testTopic";

    List<LockedExternalTask> lockedExternalTasks = externalTaskService.fetchAndLock(10, workerId)
      .topic(topicName, 60000)
      .variables("globalVar", "nonExistingLocalVar")
      .localVariables()
      .execute();

    assertEquals(1, lockedExternalTasks.size());

    LockedExternalTask lockedExternalTask = lockedExternalTasks.get(0);
    VariableMap variables = lockedExternalTask.getVariables();
    assertEquals(0, variables.size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchVariables.bpmn20.xml")
  @Test
  public void testFetchAllVariables() {
    // given
    runtimeService.startProcessInstanceByKey("subProcessExternalTask",
        Variables.createVariables()
            .putValue("processVar1", 42)
            .putValue("processVar2", 43));

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    // then
    LockedExternalTask task = externalTasks.get(0);
    verifyVariables(task);

    runtimeService.startProcessInstanceByKey("subProcessExternalTask",
        Variables.createVariables()
            .putValue("processVar1", 42)
            .putValue("processVar2", 43));

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .variables((String[]) null)
        .execute();

    task = externalTasks.get(0);
    verifyVariables(task);

    runtimeService.startProcessInstanceByKey("subProcessExternalTask",
        Variables.createVariables()
            .putValue("processVar1", 42)
            .putValue("processVar2", 43));

    List<String> list = null;
    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .variables(list)
        .execute();

    task = externalTasks.get(0);
    verifyVariables(task);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchNonExistingVariable() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .variables("nonExistingVariable")
      .execute();

    LockedExternalTask task = tasks.get(0);

    // then
    assertTrue(task.getVariables().isEmpty());
  }

  @Deployment
  @Test
  public void testFetchMultipleTopics() {
    // given a process instance with external tasks for topics "topic1", "topic2", and "topic3"
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess");

    // when fetching tasks for two topics
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("topic1", LOCK_TIME)
      .topic("topic2", LOCK_TIME * 2)
      .execute();

    // then those two tasks are locked
    assertEquals(2, tasks.size());
    LockedExternalTask topic1Task = "topic1".equals(tasks.get(0).getTopicName()) ? tasks.get(0) : tasks.get(1);
    LockedExternalTask topic2Task = "topic2".equals(tasks.get(0).getTopicName()) ? tasks.get(0) : tasks.get(1);

    assertEquals("topic1", topic1Task.getTopicName());
    AssertUtil.assertEqualsSecondPrecision(nowPlus(LOCK_TIME), topic1Task.getLockExpirationTime());

    assertEquals("topic2", topic2Task.getTopicName());
    AssertUtil.assertEqualsSecondPrecision(nowPlus(LOCK_TIME * 2), topic2Task.getLockExpirationTime());

    // and the third task can still be fetched
    tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("topic1", LOCK_TIME)
      .topic("topic2", LOCK_TIME * 2)
      .topic("topic3", LOCK_TIME * 3)
      .execute();

    assertEquals(1, tasks.size());

    LockedExternalTask topic3Task = tasks.get(0);
    assertEquals("topic3", topic3Task.getTopicName());
    AssertUtil.assertEqualsSecondPrecision(nowPlus(LOCK_TIME * 3), topic3Task.getLockExpirationTime());
  }

  @Deployment
  @Test
  public void testFetchMultipleTopicsWithVariables() {
    // given a process instance with external tasks for topics "topic1" and "topic2"
    // both have local variables "var1" and "var2"
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess",
        Variables.createVariables().putValue("var1", 0).putValue("var2", 0));

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("topic1", LOCK_TIME).variables("var1", "var2")
      .topic("topic2", LOCK_TIME).variables("var1")
      .execute();

    LockedExternalTask topic1Task = "topic1".equals(tasks.get(0).getTopicName()) ? tasks.get(0) : tasks.get(1);
    LockedExternalTask topic2Task = "topic2".equals(tasks.get(0).getTopicName()) ? tasks.get(0) : tasks.get(1);

    assertEquals("topic1", topic1Task.getTopicName());
    assertEquals("topic2", topic2Task.getTopicName());

    // then the correct variables have been fetched
    VariableMap topic1Variables = topic1Task.getVariables();
    assertEquals(2, topic1Variables.size());
    assertEquals(1L, topic1Variables.get("var1"));
    assertEquals(1L, topic1Variables.get("var2"));

    VariableMap topic2Variables = topic2Task.getVariables();
    assertEquals(1, topic2Variables.size());
    assertEquals(2L, topic2Variables.get("var1"));

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchMultipleTopics.bpmn20.xml")
  @Test
  public void testFetchMultipleTopicsMaxTasks() {
    // given
    for (int i = 0; i < 10; i++) {
      runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess");
    }

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic("topic1", LOCK_TIME)
        .topic("topic2", LOCK_TIME)
        .topic("topic3", LOCK_TIME)
        .execute();

    // then 5 tasks were returned in total, not per topic
    assertEquals(5, tasks.size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchSuspendedTask() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when suspending the process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // then the external task cannot be fetched
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    assertEquals(0, externalTasks.size());

    // when activating the process instance
    runtimeService.activateProcessInstanceById(processInstance.getId());

    // then the task can be fetched
    externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertEquals(1, externalTasks.size());
  }

  /**
   * Note: this does not test a hard API guarantee, i.e. the test is stricter than the API (Javadoc).
   * Its purpose is to ensure that the API implementation is less error-prone to use.
   *
   * Bottom line: if there is good reason to change behavior such that this test breaks, it may
   * be ok to change the test.
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchAndLockWithInitialBuilder() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    ExternalTaskQueryBuilder initialBuilder = externalTaskService.fetchAndLock(1, WORKER_ID);
    initialBuilder.topic(TOPIC_NAME, LOCK_TIME);

    // should execute regardless whether the initial builder is used or the builder returned by the
    // #topic invocation
    List<LockedExternalTask> tasks = initialBuilder.execute();

    // then
    assertEquals(1, tasks.size());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityProcess.bpmn20.xml" })
  @Test
  public void testFetchByProcessDefinitionId() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess");
    String processDefinitionId2 = processInstance2.getProcessDefinitionId();

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .processDefinitionId(processDefinitionId2)
      .execute();

    // then
    assertEquals(1, externalTasks.size());
    assertEquals(processDefinitionId2, externalTasks.get(0).getProcessDefinitionId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchByProcessDefinitionIdCombination() {
    // given
    String topicName1 = "topic1";
    String topicName2 = "topic2";

    String businessKey1 = "testBusinessKey1";
    String businessKey2 = "testBusinessKey2";

    Long lockDuration = 60L * 1000L;

    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey1);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey2);
    String processDefinitionId2 = processInstance2.getProcessDefinitionId();


  //when
    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
        .topic(topicName2, lockDuration)
          .processDefinitionId(processDefinitionId2)
        .execute();

    //then
    assertEquals(3, topicTasks.size());

    for (LockedExternalTask externalTask : topicTasks) {
      ProcessInstance pi = runtimeService.createProcessInstanceQuery()
          .processInstanceId(externalTask.getProcessInstanceId())
          .singleResult();
      if (externalTask.getTopicName().equals(topicName1)) {
        assertEquals(pi.getProcessDefinitionId(), externalTask.getProcessDefinitionId());
      } else if (externalTask.getTopicName().equals(topicName2)){
        assertEquals(processDefinitionId2, pi.getProcessDefinitionId());
        assertEquals(processDefinitionId2, externalTask.getProcessDefinitionId());
      } else {
        fail("No other topic name values should be available!");
      }
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchByProcessDefinitionIdIn() {
    // given
    String topicName1 = "topic1";
    String topicName2 = "topic2";
    String topicName3 = "topic3";

    String businessKey1 = "testBusinessKey1";
    String businessKey2 = "testBusinessKey2";
    String businessKey3 = "testBusinessKey3";

    Long lockDuration = 60L * 1000L;

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey1);
    String processDefinitionId1 = processInstance1.getProcessDefinitionId();
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey2);
    String processDefinitionId2 = processInstance2.getProcessDefinitionId();
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey3);
    String processDefinitionId3 = processInstance3.getProcessDefinitionId();

    // when
    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(2, "externalWorkerId")
        .topic(topicName1, lockDuration)
          .processDefinitionIdIn(processDefinitionId1)
          .processDefinitionKey("parallelExternalTaskProcess")
        .topic(topicName2, lockDuration)
          .processDefinitionId(processDefinitionId2)
          .businessKey(businessKey2)
        .topic(topicName3, lockDuration)
          .processDefinitionId(processDefinitionId3)
          .processDefinitionKeyIn("unexisting")
        .execute();

    // then
    assertEquals(2, topicTasks.size());

    for (LockedExternalTask externalTask : topicTasks) {
      ProcessInstance pi = runtimeService.createProcessInstanceQuery()
          .processInstanceId(externalTask.getProcessInstanceId())
          .singleResult();
      if (externalTask.getTopicName().equals(topicName1)) {
        assertEquals(processDefinitionId1, pi.getProcessDefinitionId());
        assertEquals(processDefinitionId1, externalTask.getProcessDefinitionId());
      } else if (externalTask.getTopicName().equals(topicName2)){
        assertEquals(processDefinitionId2, pi.getProcessDefinitionId());
        assertEquals(processDefinitionId2, externalTask.getProcessDefinitionId());
      } else {
        fail("No other topic name values should be available!");
      }
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityProcess.bpmn20.xml" })
  @Test
  public void testFetchByProcessDefinitionIds() {
    // given
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    String processDefinitionId1 = processInstance1.getProcessDefinitionId();
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess");
    String processDefinitionId2 = processInstance2.getProcessDefinitionId();

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .processDefinitionId(processDefinitionId2)
      .processDefinitionIdIn(processDefinitionId1)
      .execute();

    // then
    assertEquals(0, externalTasks.size());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityProcess.bpmn20.xml" })
  @Test
  public void testFetchByProcessDefinitionKey() {
    // given
    String processDefinitionKey1 = "oneExternalTaskProcess";
    runtimeService.startProcessInstanceByKey(processDefinitionKey1);
    String processDefinitionKey2 = "twoExternalTaskWithPriorityProcess";
    runtimeService.startProcessInstanceByKey(processDefinitionKey2);

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .processDefinitionKey(processDefinitionKey2)
      .execute();

    // then
    assertEquals(1, externalTasks.size());
    assertEquals(processDefinitionKey2, externalTasks.get(0).getProcessDefinitionKey());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityProcess.bpmn20.xml" })
  @Test
  public void testFetchByProcessDefinitionKeyIn() {
    // given
    String processDefinitionKey1 = "oneExternalTaskProcess";
    runtimeService.startProcessInstanceByKey(processDefinitionKey1);
    String processDefinitionKey2 = "twoExternalTaskWithPriorityProcess";
    runtimeService.startProcessInstanceByKey(processDefinitionKey2);

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .processDefinitionKeyIn(processDefinitionKey2)
      .execute();

    // then
    assertEquals(1, externalTasks.size());
    assertEquals(processDefinitionKey2, externalTasks.get(0).getProcessDefinitionKey());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityProcess.bpmn20.xml" })
  @Test
  public void testFetchByProcessDefinitionKeys() {
    // given
    String processDefinitionKey1 = "oneExternalTaskProcess";
    runtimeService.startProcessInstanceByKey(processDefinitionKey1);
    String processDefinitionKey2 = "twoExternalTaskWithPriorityProcess";
    runtimeService.startProcessInstanceByKey(processDefinitionKey2);

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .processDefinitionKey(processDefinitionKey1)
      .processDefinitionKeyIn(processDefinitionKey2)
      .execute();

    // then
    assertEquals(0, externalTasks.size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchByProcessDefinitionIdAndKey() {
    // given
    String topicName1 = "topic1";
    String topicName2 = "topic2";

    String businessKey1 = "testBusinessKey1";
    String businessKey2 = "testBusinessKey2";
    String businessKey3 = "testBusinessKey3";

    Long lockDuration = 60L * 1000L;

    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey1);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey2);
    String processDefinitionId2 = processInstance2.getProcessDefinitionId();
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey3);
    String processDefinitionId3 = processInstance3.getProcessDefinitionId();

    //when
    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
        .topic(topicName2, lockDuration)
          .processDefinitionIdIn(processDefinitionId2, processDefinitionId3)
        .topic("topic3", lockDuration)
          .processDefinitionIdIn("unexisting")
        .execute();

    //then
    assertEquals(3, topicTasks.size());

    for (LockedExternalTask externalTask : topicTasks) {
      ProcessInstance pi = runtimeService.createProcessInstanceQuery()
          .processInstanceId(externalTask.getProcessInstanceId())
          .singleResult();
      if (externalTask.getTopicName().equals(topicName1)) {
        assertEquals(pi.getProcessDefinitionId(), externalTask.getProcessDefinitionId());
      } else if (externalTask.getTopicName().equals(topicName2)){
        assertEquals(processDefinitionId2, pi.getProcessDefinitionId());
        assertEquals(processDefinitionId2, externalTask.getProcessDefinitionId());
      } else {
        fail("No other topic name values should be available!");
      }
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void testFetchWithoutTenant() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .withoutTenantId()
      .execute();

    // then
    assertEquals(1, externalTasks.size());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldLockExternalTask() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().notLocked().singleResult();

    // when
    externalTaskService.lock(externalTask.getId(), WORKER_ID, LOCK_TIME);

    // then
    List<ExternalTask> lockedExternalTasks = externalTaskService.createExternalTaskQuery().locked().list();
    assertThat(lockedExternalTasks).hasSize(1);

    Date lockExpirationTime = new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME).toDate();
    ExternalTask lockedExternalTask = lockedExternalTasks.get(0);
    assertThat(lockedExternalTask.getWorkerId()).isEqualToIgnoringCase(WORKER_ID);
    assertThat(lockedExternalTask.getLockExpirationTime())
        .isEqualTo(lockExpirationTime);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldLockExternalTaskWithExpiredLock() throws java.text.ParseException {
    // given
    // a second worker
    String aSecondWorkerId = "aSecondWorkerId";
    // and a process with an external task
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().notLocked().singleResult();
    // which is locked
    externalTaskService.lock(externalTask.getId(), WORKER_ID, LOCK_TIME);
    // and the lock expires
    // we eliminate milliseconds due to MySQL/MariaDB datetime precision
    Date lockExpiredTime = formatter.parse(
        formatter.format(DateUtils.addMilliseconds(ClockUtil.getCurrentTime(), (int) (LOCK_TIME + 1000L))));
    ClockUtil.setCurrentTime(lockExpiredTime);

    // when
    // the external task is locked again
    externalTaskService.lock(externalTask.getId(), aSecondWorkerId, LOCK_TIME);

    // then
    List<ExternalTask> lockedExternalTasks = externalTaskService.createExternalTaskQuery().locked().list();
    assertThat(lockedExternalTasks).hasSize(1);

    Date lockExpirationTime = new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME).toDate();
    ExternalTask lockedExternalTask = lockedExternalTasks.get(0);
    assertThat(lockedExternalTask.getWorkerId()).isEqualToIgnoringCase(aSecondWorkerId);
    assertThat(lockedExternalTask.getLockExpirationTime())
        .isEqualTo(lockExpirationTime);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldLockAlreadyLockedExternalTaskWithSameWorker() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().notLocked().singleResult();
    externalTaskService.lock(externalTask.getId(), WORKER_ID, LOCK_TIME);

    ExternalTask externalTaskFirstLock = externalTaskService.createExternalTaskQuery().locked().singleResult();
    Date firstLockExpirationTime = externalTaskFirstLock.getLockExpirationTime();

    // when
    externalTaskService.lock(externalTaskFirstLock.getId(), WORKER_ID, LOCK_TIME * 10);

    // then
    ExternalTask externalTaskSecondLock = externalTaskService.createExternalTaskQuery().locked().singleResult();
    Date secondLockExpirationTime = externalTaskSecondLock.getLockExpirationTime();
    assertThat(firstLockExpirationTime).isBefore(secondLockExpirationTime);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldFailToLockAlreadyLockedExternalTask() {
    // given
    String aSecondWorkerId = "aSecondWorkerId";
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().notLocked().singleResult();
    externalTaskService.lock(externalTask.getId(), WORKER_ID, LOCK_TIME);

    // when/then
    assertThatThrownBy(() -> externalTaskService.lock(externalTask.getId(), aSecondWorkerId, LOCK_TIME))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("External Task " + externalTask.getId()
      + " cannot be locked by worker '" + aSecondWorkerId
      + "'. It is locked by worker '" + WORKER_ID + "'.");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldReportMissingWorkerIdOnLockExternalTask() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().notLocked().singleResult();

    // when/then
    assertThatThrownBy(() -> externalTaskService.lock(externalTask.getId(), null, LOCK_TIME))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("workerId is null");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldReportMissingExternalTaskIdOnLockExternalTask() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when/then
    assertThatThrownBy(() -> externalTaskService.lock(null, WORKER_ID, LOCK_TIME))
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Cannot find external task with id null: externalTask is null");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldReportNonexistentExternalTaskIdOnLockExternalTask() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when/then
    assertThatThrownBy(() -> externalTaskService.lock("fakeExternalTaskId", WORKER_ID, LOCK_TIME))
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Cannot find external task with id fakeExternalTaskId: " +
          "externalTask is null");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldFailToLockExternalTaskWithNullLockDuration() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when/then
    assertThatThrownBy(() -> externalTaskService.lock("fakeExternalTaskId", WORKER_ID, 0))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("lockDuration is not greater than 0");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml"})
  @Test
  public void shouldFailToLockExternalTaskWithNegativeLockDuration() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when/then
    assertThatThrownBy(() -> externalTaskService.lock("fakeExternalTaskId", WORKER_ID, -1))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("lockDuration is not greater than 0");
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
  @Test
  public void testComplete() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    externalTaskService.complete(externalTasks.get(0).getId(), WORKER_ID);

    // then
    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());

    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("afterExternalTask")
        .done());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
  @Test
  public void testCompleteWithVariables() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    externalTaskService.complete(
        externalTasks.get(0).getId(),
        WORKER_ID,
        Variables.createVariables().putValue("var", 42));

    // then
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("afterExternalTask")
        .done());

    assertEquals(42, runtimeService.getVariable(processInstance.getId(), "var"));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
  @Test
  public void testCompleteWithWrongWorkerId() {
    // given
    runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then it is not possible to complete the task with a different worker id
    try {
      externalTaskService.complete(externalTasks.get(0).getId(), "someCrazyWorkerId");
      fail("exception expected");
    } catch (BadUserRequestException e) {
      testRule.assertTextPresent("cannot be completed by worker 'someCrazyWorkerId'. It is locked by worker '" + WORKER_ID + "'.", e.getMessage());
    }
  }

  @Test
  public void testCompleteNonExistingTask() {
    try {
      externalTaskService.complete("nonExistingTaskId", WORKER_ID);
      fail("exception expected");
    } catch (NotFoundException e) {
      // not found exception lets client distinguish this from other failures
      testRule.assertTextPresent("Cannot find external task with id nonExistingTaskId", e.getMessage());
    }
  }

  @Test
  public void testCompleteNullTaskId() {
    try {
      externalTaskService.complete(null, WORKER_ID);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Cannot find external task with id " + null, e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testCompleteNullWorkerId() {
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = tasks.get(0);

    try {
      externalTaskService.complete(task.getId(), null);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("workerId is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testCompleteSuspendedTask() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when suspending the process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // then the external task cannot be completed
    try {
      externalTaskService.complete(task.getId(), WORKER_ID);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ExternalTask with id '" + task.getId() + "' is suspended", e.getMessage());
    }

    testRule.assertProcessNotEnded(processInstance.getId());

    // when activating the process instance again
    runtimeService.activateProcessInstanceById(processInstance.getId());

    // then the task can be completed
    externalTaskService.complete(task.getId(), WORKER_ID);

    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinition.bpmn20.xml"})
  public void shouldEvaluateNestedErrorEventDefinitionsOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    externalTaskService.complete(task.getId(), WORKER_ID);

    // then
    // expression was evaluated to true
    // error was thrown and caught
    // flow continued to user task
    List<Task> list = taskService.createTaskQuery().list();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml"})
  public void shouldEvaluateNestedErrorEventDefinitionsOnCompleteWithVariables() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.complete(task.getId(), WORKER_ID, vars);

    // then
    // expression was evaluated to true using a variable
    // error was thrown and caught
    // flow continued to user task
    List<Task> list = taskService.createTaskQuery().list();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml"})
  public void shouldEvaluateNestedErrorEventDefinitionsOnCompleteWithLocalVariables() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.complete(task.getId(), WORKER_ID, null, vars);

    // then
    // expression was evaluated to true using a local variable
    // error was thrown and caught
    // flow continued to user task
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml")
  public void shouldFailNestedErrorEventDefinitionsWhenVariableWasNotProvidedByClientOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // then
    // expression evaluation failed due to missing variable
    assertThatThrownBy(() -> externalTaskService.complete(task.getId(), WORKER_ID))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Unknown property used in expression");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml"})
  public void shouldKeepVariablesAfterEvaluateNestedErrorEventDefinitionsOnCompleteWithVariables() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.complete(task.getId(), WORKER_ID, vars);

    // then
    // expression was evaluated to true using a variable
    // error was caught
    // flow continued to user task
    // variable is still available without output mapping
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery().variableName("foo").list();
    assertThat(variables).hasSize(1);
    assertThat(variables.get(0).getValue()).isEqualTo("bar");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml"})
  public void shouldNotKeepVariablesAfterEvaluateNestedErrorEventDefinitionsOnCompleteWithLocalVariables() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.complete(task.getId(), WORKER_ID, null, vars);

    // then
    // expression was evaluated to true using a local variable
    // error was caught
    // flow continued to user task
    // variable is not available due to missing output mapping
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
    List<VariableInstance> list = runtimeService.createVariableInstanceQuery().variableName("foo").list();
    assertThat(list).hasSize(0);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionTrueAndOutputMapping.bpmn20.xml"})
  public void shouldNotFailOutputMappingAfterNestedErrorEventDefinitionsOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.complete(task.getId(), WORKER_ID, vars);

    // then
    // expression was evaluated to true
    // error was caught
    // flow continued to user task
    // variables were mapped successfully
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery().variableName("foo").list();
    assertThat(variables).hasSize(1);
    assertThat(variables.get(0).getValue()).isEqualTo("bar");
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionTrueAndOutputMapping.bpmn20.xml")
  public void shouldFailOutputMappingAfterNestedErrorEventDefinitionsWhenVariableWasNotProvidedByClientOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when mapping variable does not exist
    // then output mapping fails due to missing variables
    assertThatThrownBy(() -> externalTaskService.complete(task.getId(), WORKER_ID))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("Propagation of bpmn error errorCode failed.");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinition.bpmn20.xml"})
  public void shouldEvaluateNestedErrorEventDefinitionsOnFailure() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 0, 3000L);

    // then
    // expression was evaluated to true
    // error was thrown and caught
    // flow continued to user task
    List<Task> list = taskService.createTaskQuery().list();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml"})
  public void shouldEvaluateNestedErrorEventDefinitionsOnFailWithVariables() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, ERROR_DETAILS, 0, 3000L, vars, null);

    // then
    // expression was evaluated to true using a variable
    // error was thrown and caught
    // flow continued to user task
    List<Task> list = taskService.createTaskQuery().list();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml"})
  public void shouldEvaluateNestedErrorEventDefinitionsOnFailWithLocalVariables() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, ERROR_DETAILS, 0, 3000L, null, vars);

    // then
    // expression was evaluated to true using a local variable
    // error was thrown and caught
    // flow continued to user task
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml")
  public void shouldNotFailNestedErrorEventDefinitionsWhenVariableWasNotProvidedByClientOnFail() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, ERROR_DETAILS, 0, 3000L, null, null);

    // then
    // expression evaluation failed due to missing variable
    // initial handleFailure was executed
    Incident incident = runtimeService.createIncidentQuery().singleResult();

    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_FULL.getId()) {
      HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
      assertNotNull(historicIncident);
      assertEquals(incident.getId(), historicIncident.getId());
      assertTrue(historicIncident.isOpen());
    }

    assertNotNull(incident);
    assertNotNull(incident.getId());
    assertEquals(ERROR_MESSAGE, incident.getIncidentMessage());
    assertEquals(task.getExecutionId(), incident.getExecutionId());
    assertEquals("externalTask", incident.getActivityId());
    assertEquals(incident.getId(), incident.getCauseIncidentId());
    assertEquals("failedExternalTask", incident.getIncidentType());
    assertEquals(task.getProcessDefinitionId(), incident.getProcessDefinitionId());
    assertEquals(task.getProcessInstanceId(), incident.getProcessInstanceId());
    assertEquals(incident.getId(), incident.getRootCauseIncidentId());
    assertEquals(task.getId(), incident.getConfiguration());
    assertNull(incident.getJobDefinitionId());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml"})
  public void shouldKeepVariablesAfterEvaluateNestedErrorEventDefinitionsOnFailWithVariables() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, ERROR_DETAILS, 0, 3000L, vars, null);

    // then
    // expression was evaluated to true using a variable
    // error was caught
    // flow continued to user task
    // variable is still available without output mapping
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery().variableName("foo").list();
    assertThat(variables).hasSize(1);
    assertThat(variables.get(0).getValue()).isEqualTo("bar");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionVariableExpression.bpmn20.xml"})
  public void shouldNotKeepVariablesAfterEvaluateNestedErrorEventDefinitionsOnFailWithLocalVariables() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, ERROR_DETAILS, 0, 3000L, null, vars);

    // then
    // expression was evaluated to true using a local variable
    // error was caught
    // flow continued to user task
    // variable is not available due to missing output mapping
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
    List<VariableInstance> list = runtimeService.createVariableInstanceQuery().variableName("foo").list();
    assertThat(list).hasSize(0);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionTrueAndOutputMapping.bpmn20.xml"})
  public void shouldNotFailOutputMappingAfterNestedErrorEventDefinitionsOnFail() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when
    Map<String, Object> vars = new HashMap<>();
    vars.put("foo", "bar");
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, ERROR_DETAILS, 0, 3000L, vars, null);

    // then
    // expression was evaluated to true
    // error was caught
    // flow continued to user task
    // variables were mapped successfully
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery().variableName("foo").list();
    assertThat(variables).hasSize(1);
    assertThat(variables.get(0).getValue()).isEqualTo("bar");
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionTrueAndOutputMapping.bpmn20.xml")
  public void shouldFailOutputMappingAfterNestedErrorEventDefinitionsWhenVariableWasNotProvidedByClientOnFail() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when mapping variable does not exist
    // then output mapping fails due to missing variables
    assertThatThrownBy(() -> externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 0, 3000L))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Propagation of bpmn error errorCode failed.");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionExpressionIncludesErrorMessage.bpmn20.xml"})
  public void shouldResolveExpressionWithErrorMessageInNestedErrorEventDefinitionOnFailure() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertThat(externalTasks).hasSize(1);
    LockedExternalTask task = externalTasks.get(0);

    // when
    externalTaskService.handleFailure(task.getId(), WORKER_ID, "myErrorMessage", "myErrorDetails", 0, 3000L);

    // then
    // expression was evaluated to true
    // error was caught
    // flow continued to user task
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionExpressionIncludesNullErrorMessage.bpmn20.xml"})
  public void shouldResolveExpressionWithErrorMessageInNestedErrorEventDefinitionOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> externalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertThat(externalTasks).hasSize(1);
    LockedExternalTask task = externalTasks.get(0);

    // when
    externalTaskService.complete(task.getId(), WORKER_ID);

    // then
    // expression was evaluated to true
    // error was caught
    // flow continued to user task
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionExpressionIncludesNullErrorMessage.bpmn20.xml"})
  public void shouldResolveExpressionWithErrorMessageInNestedErrorEventDefinitionOnCompleteWithMultipleActivities() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> lockedExternalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertThat(lockedExternalTasks).hasSize(1);
    LockedExternalTask task = lockedExternalTasks.get(0);
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<ExternalTask> externalTasks = externalTaskService.createExternalTaskQuery().activityId(task.getActivityId()).list();
    assertThat(externalTasks).hasSize(2);

    // when
    externalTaskService.complete(task.getId(), WORKER_ID);

    // then
    // correct external task was completed
    // expression was evaluated to true
    // error was caught
    // flow continued to user task
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask");
    assertThat(tasks.get(0).getProcessInstanceId()).isEqualTo(task.getProcessInstanceId());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithTwoNestedErrorEventDefinitionExpressions.bpmn20.xml"})
  public void shouldResolveFirstOfTwoExpressionsInNestedErrorEventDefinitionOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> lockedExternalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertThat(lockedExternalTasks).hasSize(1);
    LockedExternalTask task = lockedExternalTasks.get(0);

    // when
    // expression for error A is true
    Map<String, Object> vars = new HashMap<>();
    vars.put("a", true);
    vars.put("b", false);
    externalTaskService.complete(task.getId(), WORKER_ID, vars);

    // then
    // error A is thrown
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask A");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithTwoNestedErrorEventDefinitionExpressions.bpmn20.xml"})
  public void shouldResolveSecondOfTwoExpressionsInNestedErrorEventDefinitionOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> lockedExternalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertThat(lockedExternalTasks).hasSize(1);
    LockedExternalTask task = lockedExternalTasks.get(0);

    // when
    // expression for error B is true
    Map<String, Object> vars = new HashMap<>();
    vars.put("a", false);
    vars.put("b", true);
    externalTaskService.complete(task.getId(), WORKER_ID, vars);

    // then
    // error B is thrown
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask B");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithTwoNestedErrorEventDefinitionExpressions.bpmn20.xml"})
  public void shouldResolveBothOfTwoExpressionsInNestedErrorEventDefinitionOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> lockedExternalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertThat(lockedExternalTasks).hasSize(1);
    LockedExternalTask task = lockedExternalTasks.get(0);

    // when
    // expressions for both errors are true
    Map<String, Object> vars = new HashMap<>();
    vars.put("a", true);
    vars.put("b", true);
    externalTaskService.complete(task.getId(), WORKER_ID, vars);

    // then
    // error A is thrown as it is defined first
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("userTask A");
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionEmptyExpression.bpmn20.xml"})
  public void shouldIgnoreEmptyExpressionInNestedErrorEventDefinitionOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> lockedExternalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertThat(lockedExternalTasks).hasSize(1);
    LockedExternalTask task = lockedExternalTasks.get(0);

    // when
    externalTaskService.complete(task.getId(), WORKER_ID);

    // then
    // no error is thrown
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(0);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.externalTaskWithNestedErrorEventDefinitionEmptyExpression.bpmn20.xml"})
  public void shouldIgnoreNullExpressionInNestedErrorEventDefinitionOnComplete() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithNestedErrorEventDefinition");
    List<LockedExternalTask> lockedExternalTasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertThat(lockedExternalTasks).hasSize(1);
    LockedExternalTask task = lockedExternalTasks.get(0);

    // when
    externalTaskService.complete(task.getId(), WORKER_ID);

    // then
    // no error is thrown
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(0);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testLocking() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then the task is locked
    assertEquals(1, externalTasks.size());

    LockedExternalTask task = externalTasks.get(0);
    AssertUtil.assertEqualsSecondPrecision(nowPlus(LOCK_TIME), task.getLockExpirationTime());

    // and cannot be retrieved by another query
    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
    assertEquals(0, externalTasks.size());

    // unless the expiration time expires
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(1, externalTasks.size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testCompleteLockExpiredTask() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // and the lock expires without the task being reclaimed
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    // then the task can successfully be completed
    externalTaskService.complete(externalTasks.get(0).getId(), WORKER_ID);

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testCompleteReclaimedLockExpiredTask() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // and the lock expires
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    // and it is reclaimed by another worker
    List<LockedExternalTask> reclaimedTasks = externalTaskService.fetchAndLock(1, "anotherWorkerId")
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then the first worker cannot complete the task
    try {
      externalTaskService.complete(externalTasks.get(0).getId(), WORKER_ID);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("cannot be completed by worker '" + WORKER_ID + "'. It is locked by worker 'anotherWorkerId'.", e.getMessage());
    }

    // and the second worker can
    externalTaskService.complete(reclaimedTasks.get(0).getId(), "anotherWorkerId");

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testDeleteProcessInstance() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    assertEquals(0, externalTaskService.fetchAndLock(5, WORKER_ID).topic(TOPIC_NAME, LOCK_TIME).execute().size());
    testRule.assertProcessEnded(processInstance.getId());
  }


  @Deployment
  @Test
  public void testExternalTaskExecutionTreeExpansion() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("boundaryExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService
      .fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    LockedExternalTask externalTask = tasks.get(0);

    // when a non-interrupting boundary event is triggered meanwhile
    // such that the execution tree is expanded
    runtimeService.correlateMessage("Message");

    // then the external task can still be completed
    externalTaskService.complete(externalTask.getId(), WORKER_ID);

    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(processInstance.getProcessDefinitionId())
          .activity("afterBoundaryTask")
        .done());

    Task afterBoundaryTask = taskService.createTaskQuery().singleResult();
    taskService.complete(afterBoundaryTask.getId());

    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment
  @Test
  public void testExternalTaskExecutionTreeCompaction() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("concurrentExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService
      .fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    LockedExternalTask externalTask = tasks.get(0);

    Task userTask = taskService.createTaskQuery().singleResult();

    // when the user task completes meanwhile, thereby trigger execution tree compaction
    taskService.complete(userTask.getId());

    // then the external task can still be completed
    externalTaskService.complete(externalTask.getId(), WORKER_ID);

    tasks = externalTaskService
        .fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
    assertEquals(0, tasks.size());

    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testUnlock() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when unlocking the task
    externalTaskService.unlock(task.getId());

    // then it can be acquired again
    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    assertEquals(1, externalTasks.size());
    LockedExternalTask reAcquiredTask = externalTasks.get(0);
    assertEquals(task.getId(), reAcquiredTask.getId());
  }

  @Test
  public void testUnlockNullTaskId() {
    try {
      externalTaskService.unlock(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      Assert.assertThat(e.getMessage(), containsString("externalTaskId is null"));
    }
  }

  @Test
  public void testUnlockNonExistingTask() {
    try {
      externalTaskService.unlock("nonExistingId");
      fail("expected exception");
    } catch (NotFoundException e) {
      // not found exception lets client distinguish this from other failures
      testRule.assertTextPresent("Cannot find external task with id nonExistingId", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailure() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    LockedExternalTask task = tasks.get(0);

    // when submitting a failure (after a simulated processing time of three seconds)
    ClockUtil.setCurrentTime(nowPlus(3000L));

    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 5, 3000L);

    // then the task cannot be immediately acquired again
    tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
    assertEquals(0, tasks.size());

    // and no incident exists because there are still retries left
    assertEquals(0, runtimeService.createIncidentQuery().count());

    // but when the retry time expires, the task is available again
    ClockUtil.setCurrentTime(nowPlus(4000L));

    tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
    assertEquals(1, tasks.size());

    // and the retries and error message are accessible
    task = tasks.get(0);
    assertEquals(ERROR_MESSAGE, task.getErrorMessage());
    assertEquals(5, (int) task.getRetries());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureWithErrorDetails() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = tasks.get(0);

    // when submitting a failure (after a simulated processing time of three seconds)
    ClockUtil.setCurrentTime(nowPlus(3000L));

    String errorMessage;
    String exceptionStackTrace;
    try {
      RuntimeSqlException cause = new RuntimeSqlException("test cause");
      for (int i = 0; i < 10; i++) {
        cause = new RuntimeSqlException(cause);
      }
      throw cause;
    } catch (RuntimeException e) {
      exceptionStackTrace = ExceptionUtils.getStackTrace(e);
      errorMessage = e.getMessage();
      while (errorMessage.length() < 1000) {
        errorMessage = errorMessage + ":" + e.getMessage();
      }
    }
    Assert.assertThat(exceptionStackTrace,is(notNullValue()));
//  make sure that stack trace is longer then errorMessage DB field length
    Assert.assertThat(exceptionStackTrace.length(),is(greaterThan(4000)));
    externalTaskService.handleFailure(task.getId(), WORKER_ID, errorMessage, exceptionStackTrace, 5, 3000L);
    ClockUtil.setCurrentTime(nowPlus(4000L));
    tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
    Assert.assertThat(tasks.size(), is(1));

    // verify that exception is accessible properly
    task = tasks.get(0);
    Assert.assertThat(task.getErrorMessage(),is(errorMessage.substring(0,666)));
    Assert.assertThat(task.getRetries(),is(5));
    Assert.assertThat(externalTaskService.getExternalTaskErrorDetails(task.getId()),is(exceptionStackTrace));
    Assert.assertThat(task.getErrorDetails(),is(exceptionStackTrace));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureZeroRetries() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    LockedExternalTask task = tasks.get(0);

    // when reporting a failure and setting retries to 0
    ClockUtil.setCurrentTime(nowPlus(3000L));

    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 0, 3000L);

    // then the task cannot be fetched anymore even when the lock expires
    ClockUtil.setCurrentTime(nowPlus(4000L));

    tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
    assertEquals(0, tasks.size());

    // and an incident has been created
    Incident incident = runtimeService.createIncidentQuery().singleResult();

    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_FULL.getId()) {
      HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
      assertNotNull(historicIncident);
      assertEquals(incident.getId(), historicIncident.getId());
      assertTrue(historicIncident.isOpen());
      assertEquals(getHistoricTaskLogOrdered(incident.getConfiguration()).get(0).getId(), historicIncident.getHistoryConfiguration());
    }

    assertNotNull(incident);
    assertNotNull(incident.getId());
    assertEquals(ERROR_MESSAGE, incident.getIncidentMessage());
    assertEquals(task.getExecutionId(), incident.getExecutionId());
    assertEquals("externalTask", incident.getActivityId());
    assertEquals(incident.getId(), incident.getCauseIncidentId());
    assertEquals("failedExternalTask", incident.getIncidentType());
    assertEquals(task.getProcessDefinitionId(), incident.getProcessDefinitionId());
    assertEquals(task.getProcessInstanceId(), incident.getProcessInstanceId());
    assertEquals(incident.getId(), incident.getRootCauseIncidentId());
    AssertUtil.assertEqualsSecondPrecision(nowMinus(4000L), incident.getIncidentTimestamp());
    assertEquals(task.getId(), incident.getConfiguration());
    assertNull(incident.getJobDefinitionId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureZeroRetriesAfterIncidentsAreResolved() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    LockedExternalTask task = tasks.get(0);
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 0, 3000L);
    externalTaskService.setRetries(task.getId(), 5);
    ClockUtil.setCurrentTime(nowPlus(3000L));
    tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
    ClockUtil.setCurrentTime(nowPlus(4000L));
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 1, 3000L);

    // when reporting a failure and setting retries to 0
    ClockUtil.setCurrentTime(nowPlus(5000L));
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 0, 3000L);

    // another incident has been created
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(incident);
    assertNotNull(incident.getId());

    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_FULL.getId()) {
      // there are two incidents in the history
      List<HistoricIncident> historicIncidents = historyService.createHistoricIncidentQuery()
          .configuration(task.getId())
          .orderByCreateTime().asc()
          .list();
      assertEquals(2, historicIncidents.size());
      // there are 3 failure logs for external tasks
      List<HistoricExternalTaskLog> logs = getHistoricTaskLogOrdered(task.getId());
      assertEquals(3, logs.size());
      // the oldest incident is resolved and correlates to the oldest external task log entry
      assertTrue(historicIncidents.get(0).isResolved());
      assertEquals(logs.get(2).getId(), historicIncidents.get(0).getHistoryConfiguration());
      // the latest incident is open and correlates to the latest external task log entry
      assertTrue(historicIncidents.get(1).isOpen());
      assertEquals(logs.get(0).getId(), historicIncidents.get(1).getHistoryConfiguration());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureAndDeleteProcessInstance() {
    // given a failed external task with incident
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    LockedExternalTask task = tasks.get(0);

    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 0, LOCK_TIME);

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    testRule.assertProcessEnded(processInstance.getId());
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureThenComplete() {
    // given a failed external task with incident
    runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    LockedExternalTask task = tasks.get(0);

    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 0, LOCK_TIME);

    // when
    externalTaskService.complete(task.getId(), WORKER_ID);

    // then the task has been completed nonetheless
    Task followingTask = taskService.createTaskQuery().singleResult();
    assertNotNull(followingTask);
    assertEquals("afterExternalTask", followingTask.getTaskDefinitionKey());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureWithWrongWorkerId() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then it is not possible to complete the task with a different worker id
    try {
      externalTaskService.handleFailure(externalTasks.get(0).getId(), "someCrazyWorkerId", ERROR_MESSAGE, 5, LOCK_TIME);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      testRule.assertTextPresent("Failure of External Task " + externalTasks.get(0).getId()
          + " cannot be reported by worker 'someCrazyWorkerId'. It is locked by worker '" + WORKER_ID + "'.",
        e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureNonExistingTask() {
    try {
      externalTaskService.handleFailure("nonExistingTaskId", WORKER_ID, ERROR_MESSAGE, 5, LOCK_TIME);
      fail("exception expected");
    } catch (NotFoundException e) {
      // not found exception lets client distinguish this from other failures
      testRule.assertTextPresent("Cannot find external task with id nonExistingTaskId", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureNullTaskId() {
    try {
      externalTaskService.handleFailure(null, WORKER_ID, ERROR_MESSAGE, 5, LOCK_TIME);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Cannot find external task with id " + null, e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureNullWorkerId() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    try {
      externalTaskService.handleFailure(externalTasks.get(0).getId(), null, ERROR_MESSAGE, 5, LOCK_TIME);
      fail("exception expected");
    } catch (NullValueException e) {
      testRule.assertTextPresent("workerId is null", e.getMessage());
    }

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureNegativeLockDuration() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    try {
      externalTaskService.handleFailure(externalTasks.get(0).getId(), WORKER_ID, ERROR_MESSAGE, 5, - LOCK_TIME);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("retryDuration is not greater than or equal to 0", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureNegativeRetries() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    try {
      externalTaskService.handleFailure(externalTasks.get(0).getId(), WORKER_ID, ERROR_MESSAGE, -5, LOCK_TIME);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("retries is not greater than or equal to 0", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureNullErrorMessage() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // when
    externalTaskService.handleFailure(externalTasks.get(0).getId(), WORKER_ID, null, 5, LOCK_TIME);

    // then the failure was reported successfully and the error message is null
    ExternalTask task = externalTaskService.createExternalTaskQuery().singleResult();

    assertEquals(5, (int) task.getRetries());
    assertNull(task.getErrorMessage());
    assertNull(externalTaskService.getExternalTaskErrorDetails(task.getId()));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleFailureSuspendedTask() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when suspending the process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // then a failure cannot be reported
    try {
      externalTaskService.handleFailure(externalTasks.get(0).getId(), WORKER_ID, ERROR_MESSAGE, 5, LOCK_TIME);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ExternalTask with id '" + task.getId() + "' is suspended", e.getMessage());
    }

    testRule.assertProcessNotEnded(processInstance.getId());

    // when activating the process instance again
    runtimeService.activateProcessInstanceById(processInstance.getId());

    // then the failure can be reported successfully
    externalTaskService.handleFailure(externalTasks.get(0).getId(), WORKER_ID, ERROR_MESSAGE, 5, LOCK_TIME);

    ExternalTask updatedTask = externalTaskService.createExternalTaskQuery().singleResult();
    assertEquals(5, (int) updatedTask.getRetries());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testSetRetries() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    // when
    externalTaskService.setRetries(externalTasks.get(0).getId(), 5);

    // then
    ExternalTask task = externalTaskService.createExternalTaskQuery().singleResult();

    assertEquals(5, (int) task.getRetries());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testSetRetriesResolvesFailureIncident() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask lockedTask = externalTasks.get(0);
    externalTaskService.handleFailure(lockedTask.getId(), WORKER_ID, ERROR_MESSAGE, 0, LOCK_TIME);

    Incident incident = runtimeService.createIncidentQuery().singleResult();

    // when
    externalTaskService.setRetries(lockedTask.getId(), 5);

    // then the incident is resolved
    assertEquals(0, runtimeService.createIncidentQuery().count());

    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_FULL.getId()) {

      HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
      assertNotNull(historicIncident);
      assertEquals(incident.getId(), historicIncident.getId());
      assertTrue(historicIncident.isResolved());
    }

    // and the task can be fetched again
    ClockUtil.setCurrentTime(nowPlus(LOCK_TIME + 3000L));

    externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    assertEquals(1, externalTasks.size());
    assertEquals(lockedTask.getId(), externalTasks.get(0).getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testSetRetriesToZero() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask lockedTask = externalTasks.get(0);

    // when
    externalTaskService.setRetries(lockedTask.getId(), 0);

    // then
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(incident);
    assertEquals(lockedTask.getId(), incident.getConfiguration());

    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_FULL.getId()) {

      HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().singleResult();
      assertNotNull(historicIncident);
      assertEquals(incident.getId(), historicIncident.getId());
      assertTrue(historicIncident.isOpen());
      assertNull(historicIncident.getHistoryConfiguration());
    }

    // and resetting the retries removes the incident again
    externalTaskService.setRetries(lockedTask.getId(), 5);

    assertEquals(0, runtimeService.createIncidentQuery().count());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testSetRetriesToZeroAfterFailureWithRetriesLeft() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    LockedExternalTask task = tasks.get(0);
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 2, 3000L);
    ClockUtil.setCurrentTime(nowPlus(3000L));
    tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();
    ClockUtil.setCurrentTime(nowPlus(5000L));
    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 1, 3000L);
    tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();


    // when setting retries to 0
    ClockUtil.setCurrentTime(nowPlus(7000L));
    externalTaskService.setRetries(task.getId(), 0);

    // an incident has been created
    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertNotNull(incident);
    assertNotNull(incident.getId());

    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_FULL.getId()) {
      // there are one incident in the history
      HistoricIncident historicIncident = historyService.createHistoricIncidentQuery().configuration(task.getId()).singleResult();
      assertNotNull(historicIncident);
      // there are two failure logs for external tasks
      List<HistoricExternalTaskLog> logs = getHistoricTaskLogOrdered(task.getId());
      assertEquals(2, logs.size());
      HistoricExternalTaskLog log = logs.get(0);
      // the incident is open and correlates to the oldest external task log entry
      assertTrue(historicIncident.isOpen());
      assertEquals(log.getId(), historicIncident.getHistoryConfiguration());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testSetRetriesNegative() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    try {
      // when
      externalTaskService.setRetries(externalTasks.get(0).getId(), -5);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("retries is not greater than or equal to 0", e.getMessage());
    }
  }

  @Test
  public void testSetRetriesNonExistingTask() {
    try {
      externalTaskService.setRetries("someExternalTaskId", 5);
      fail("expected exception");
    } catch (NotFoundException e) {
      // not found exception lets client distinguish this from other failures
      testRule.assertTextPresent("externalTask is null", e.getMessage());
    }
  }

  @Test
  public void testSetRetriesNullTaskId() {
    try {
      externalTaskService.setRetries((String)null, 5);
      fail("expected exception");
    } catch (NullValueException e) {
      Assert.assertThat(e.getMessage(), containsString("externalTaskId is null"));
    }
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testSetPriority() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    // when
    externalTaskService.setPriority(externalTasks.get(0).getId(), 5);

    // then
    ExternalTask task = externalTaskService.createExternalTaskQuery().singleResult();

    assertEquals(5, (int) task.getPriority());
  }


  @Test
  public void testSetPriorityNonExistingTask() {
    try {
      externalTaskService.setPriority("someExternalTaskId", 5);
      fail("expected exception");
    } catch (NotFoundException e) {
      // not found exception lets client distinguish this from other failures
      testRule.assertTextPresent("externalTask is null", e.getMessage());
    }
  }

  @Test
  public void testSetPriorityNullTaskId() {
    try {
      externalTaskService.setPriority(null, 5);
      fail("expected exception");
    } catch (NullValueException e) {
      Assert.assertThat(e.getMessage(), containsString("externalTaskId is null"));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskWithPriorityProcess.bpmn20.xml")
  @Test
  public void testAfterSetPriorityFetchHigherTask() {
    // given
    runtimeService.startProcessInstanceByKey("twoExternalTaskWithPriorityProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(2, WORKER_ID, true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(2, externalTasks.size());
    LockedExternalTask task = externalTasks.get(1);
    assertEquals(0, task.getPriority());
    externalTaskService.setPriority(task.getId(), 9);
    // and the lock expires without the task being reclaimed
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    // then
    externalTasks = externalTaskService.fetchAndLock(1, "anotherWorkerId", true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(externalTasks.get(0).getPriority(), 9);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testSetPriorityLockExpiredTask() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // and the lock expires without the task being reclaimed
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    // then the priority can be set
    externalTaskService.setPriority(externalTasks.get(0).getId(), 9);

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID, true)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(1, externalTasks.size());
    assertEquals(externalTasks.get(0).getPriority(), 9);
  }

  @Deployment
  @Test
  public void testCancelExternalTaskWithBoundaryEvent() {
    // given
    runtimeService.startProcessInstanceByKey("boundaryExternalTaskProcess");
    assertEquals(1, externalTaskService.createExternalTaskQuery().count());

    // when the external task is cancelled by a boundary event
    runtimeService.correlateMessage("Message");

    // then the external task instance has been removed
    assertEquals(0, externalTaskService.createExternalTaskQuery().count());

    Task afterBoundaryTask = taskService.createTaskQuery().singleResult();
    assertNotNull(afterBoundaryTask);
    assertEquals("afterBoundaryTask", afterBoundaryTask.getTaskDefinitionKey());

  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnError() {
    //given
    runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");
    // when
    List<LockedExternalTask> externalTasks = helperHandleBpmnError(1, WORKER_ID, TOPIC_NAME, LOCK_TIME,  "ERROR-OCCURED");
    //then
    assertEquals(0, externalTasks.size());
    assertEquals(0, externalTaskService.createExternalTaskQuery().count());
    Task afterBpmnError = taskService.createTaskQuery().singleResult();
    assertNotNull(afterBpmnError);
    assertEquals(afterBpmnError.getTaskDefinitionKey(), "afterBpmnError");
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnErrorWithoutDefinedBoundary() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    //when
    List<LockedExternalTask> externalTasks = helperHandleBpmnError(1, WORKER_ID, TOPIC_NAME, LOCK_TIME,  "ERROR-OCCURED");

    //then
    assertEquals(0, externalTasks.size());
    assertEquals(0, externalTaskService.createExternalTaskQuery().count());
    Task afterBpmnError = taskService.createTaskQuery().singleResult();
    assertNull(afterBpmnError);
    testRule.assertProcessEnded(processInstance.getId());
  }

  /**
   * Helper method to handle a bmpn error on an external task, which is fetched with the given parameters.
   *
   * @param taskCount the count of task to fetch
   * @param workerID the worker id
   * @param topicName the topic name of the external task
   * @param lockTime the lock time for the fetch
   * @param errorCode the error code of the bpmn error
   * @return returns the locked external tasks after the bpmn error was handled
   */
  public  List<LockedExternalTask> helperHandleBpmnError(int taskCount, String workerID, String topicName, long lockTime, String errorCode) {
    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(taskCount, workerID)
      .topic(topicName, lockTime)
      .execute();

    externalTaskService.handleBpmnError(externalTasks.get(0).getId(), workerID, errorCode);

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    return externalTasks;
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnErrorLockExpiredTask() {
    //given
    runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");
    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // and the lock expires without the task being reclaimed
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    externalTaskService.handleBpmnError(externalTasks.get(0).getId(), WORKER_ID, "ERROR-OCCURED");

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    assertEquals(0, externalTasks.size());
    assertEquals(0, externalTaskService.createExternalTaskQuery().count());
    Task afterBpmnError = taskService.createTaskQuery().singleResult();
    assertNotNull(afterBpmnError);
    assertEquals(afterBpmnError.getTaskDefinitionKey(), "afterBpmnError");
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnErrorReclaimedLockExpiredTaskWithoutDefinedBoundary() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    handleBpmnErrorReclaimedLockExpiredTask(false);
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnErrorReclaimedLockExpiredTaskWithBoundary() {
    // given
    runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");
    //then
    handleBpmnErrorReclaimedLockExpiredTask(false);
  }

  /**
   * Helpher method which reclaims an external task after the lock is expired.
   * @param includeVariables flag showing if pass or not variables
   */
  public void handleBpmnErrorReclaimedLockExpiredTask(boolean includeVariables) {
    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // and the lock expires
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    // and it is reclaimed by another worker
    List<LockedExternalTask> reclaimedTasks = externalTaskService.fetchAndLock(1, "anotherWorkerId")
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then the first worker cannot complete the task
    try {
      externalTaskService.handleBpmnError(externalTasks.get(0).getId(), WORKER_ID, "ERROR-OCCURED");
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Bpmn error of External Task " + externalTasks.get(0).getId() + " cannot be reported by worker '" + WORKER_ID + "'. It is locked by worker 'anotherWorkerId'.", e.getMessage());
      if (includeVariables) {
        List<VariableInstance> list = runtimeService.createVariableInstanceQuery().list();
        assertEquals(0, list.size());
      }
    }

    // and the second worker can
    externalTaskService.complete(reclaimedTasks.get(0).getId(), "anotherWorkerId");

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());
  }

  @Test
  public void testHandleBpmnErrorNonExistingTask() {
    try {
      externalTaskService.handleBpmnError("nonExistingTaskId", WORKER_ID, "ERROR-OCCURED");
      fail("exception expected");
    } catch (NotFoundException e) {
      // not found exception lets client distinguish this from other failures
      testRule.assertTextPresent("Cannot find external task with id nonExistingTaskId", e.getMessage());
    }
  }

  @Test
  public void testHandleBpmnNullTaskId() {
    try {
      externalTaskService.handleBpmnError(null, WORKER_ID, "ERROR-OCCURED");
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Cannot find external task with id " + null, e.getMessage());
    }
  }


  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnNullErrorCode() {
    //given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    //when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    //then
    LockedExternalTask task = tasks.get(0);
    try {
      externalTaskService.handleBpmnError(task.getId(), WORKER_ID, null);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("errorCode is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnErrorNullWorkerId() {
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = tasks.get(0);

    try {
      externalTaskService.handleBpmnError(task.getId(), null,"ERROR-OCCURED");
      fail("exception expected");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("workerId is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnErrorSuspendedTask() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute();

    LockedExternalTask task = externalTasks.get(0);

    // when suspending the process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // then the external task cannot be completed
    try {
      externalTaskService.handleBpmnError(task.getId(), WORKER_ID, "ERROR-OCCURED");
      fail("expected exception");
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("ExternalTask with id '" + task.getId() + "' is suspended", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnErrorPassVariablesBoundryEvent() {
    //given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // and the lock expires without the task being reclaimed
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    Map<String, Object> variables = new HashMap<>();
    variables.put("foo", "bar");
    variables.put("transientVar", Variables.integerValue(1, true));

    // when
    externalTaskService.handleBpmnError(externalTasks.get(0).getId(), WORKER_ID, "ERROR-OCCURED", null, variables);

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    assertEquals(0, externalTasks.size());
    assertEquals(0, externalTaskService.createExternalTaskQuery().count());
    Task afterBpmnError = taskService.createTaskQuery().singleResult();
    assertNotNull(afterBpmnError);
    assertEquals(afterBpmnError.getTaskDefinitionKey(), "afterBpmnError");
    List<VariableInstance> list = runtimeService.createVariableInstanceQuery().processInstanceIdIn(pi.getId()).list();
    assertEquals(1, list.size());
    assertEquals("foo", list.get(0).getName());
  }

  @Test
  public void testHandleBpmnErrorPassVariablesEventSubProcess() {
    // when
    BpmnModelInstance process =
        Bpmn.createExecutableProcess("process")
        .startEvent("startEvent")
        .serviceTask("externalTask")
          .camundaType("external")
          .camundaTopic(TOPIC_NAME)
        .endEvent("endEvent")
        .done();

    BpmnModelInstance subProcess = modify(process)
        .addSubProcessTo("process")
          .id("eventSubProcess")
          .triggerByEvent()
          .embeddedSubProcess()
            .startEvent("eventSubProcessStart")
                .error("ERROR-SPEC-10")
            .userTask("afterBpmnError")
            .endEvent()
          .subProcessDone()
          .done();

    BpmnModelInstance targetProcess = modify(subProcess);

    String deploymentId = testRule.deploy(targetProcess).getId();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // and the lock expires without the task being reclaimed
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    Map<String, Object> variables = new HashMap<>();
    variables.put("foo", "bar");
    variables.put("transientVar", Variables.integerValue(1, true));

    // when
    externalTaskService.handleBpmnError(externalTasks.get(0).getId(), WORKER_ID, "ERROR-SPEC-10", null, variables);

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    assertEquals(0, externalTasks.size());
    assertEquals(0, externalTaskService.createExternalTaskQuery().count());
    Task afterBpmnError = taskService.createTaskQuery().singleResult();
    assertNotNull(afterBpmnError);
    assertEquals(afterBpmnError.getTaskDefinitionKey(), "afterBpmnError");
    List<VariableInstance> list = runtimeService.createVariableInstanceQuery().processInstanceIdIn(pi.getId()).list();
    assertEquals(1, list.size());
    assertEquals("foo", list.get(0).getName());
  }

  @Deployment
  @Test
  public void testHandleBpmnErrorPassMessageEventSubProcess() {
    //given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // and the lock expires without the task being reclaimed
    ClockUtil.setCurrentTime(new DateTime(ClockUtil.getCurrentTime()).plus(LOCK_TIME * 2).toDate());

    // when
    String anErrorMessage = "Some meaningful message";
    externalTaskService.handleBpmnError(externalTasks.get(0).getId(), WORKER_ID, "ERROR-SPEC-10", anErrorMessage);

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();

    // then
    assertEquals(0, externalTasks.size());
    assertEquals(0, externalTaskService.createExternalTaskQuery().count());
    Task afterBpmnError = taskService.createTaskQuery().singleResult();
    assertNotNull(afterBpmnError);
    assertEquals(afterBpmnError.getTaskDefinitionKey(), "afterBpmnError");
    List<VariableInstance> list = runtimeService.createVariableInstanceQuery().processInstanceIdIn(pi.getId()).list();
    assertEquals(1, list.size());
    assertEquals("errorMessageVariable", list.get(0).getName());
    assertEquals(anErrorMessage, list.get(0).getValue());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
  @Test
  public void testHandleBpmnErrorReclaimedLockExpiredTaskWithBoundaryAndPassVariables() {
    // given
    runtimeService.startProcessInstanceByKey("twoExternalTaskProcess");
    // then
    handleBpmnErrorReclaimedLockExpiredTask(true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testUpdateRetriesByExternalTaskIds() {
    // given
    startProcessInstance("oneExternalTaskProcess", 5);
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    List<String> externalTaskIds = Arrays.asList(
        tasks.get(0).getId(),
        tasks.get(1).getId(),
        tasks.get(2).getId(),
        tasks.get(3).getId(),
        tasks.get(4).getId());

    // when
    externalTaskService.updateRetries().externalTaskIds(externalTaskIds).set(5);

    // then
    tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(5, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testUpdateRetriesByExternalTaskIdArray() {
    // given
    startProcessInstance("oneExternalTaskProcess", 5);
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    List<String> externalTaskIds = Arrays.asList(
        tasks.get(0).getId(),
        tasks.get(1).getId(),
        tasks.get(2).getId(),
        tasks.get(3).getId(),
        tasks.get(4).getId());

    // when
    externalTaskService.updateRetries().externalTaskIds(externalTaskIds.toArray(new String[externalTaskIds.size()])).set(5);

    // then
    tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(5, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testUpdateRetriesByProcessInstanceIds() {
    // given
    List<String> processInstances = startProcessInstance("oneExternalTaskProcess", 5);

    // when
    externalTaskService.updateRetries().processInstanceIds(processInstances).set(5);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(5, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testUpdateRetriesByProcessInstanceIdArray() {
    // given
    List<String> processInstances = startProcessInstance("oneExternalTaskProcess", 5);

    // when
    externalTaskService.updateRetries().processInstanceIds(processInstances.toArray(new String[processInstances.size()])).set(5);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(5, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testUpdateRetriesByExternalTaskQuery() {
    // given
    startProcessInstance("oneExternalTaskProcess", 5);

    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // when
    externalTaskService.updateRetries().externalTaskQuery(query).set(5);

    // then
    List<ExternalTask> tasks = query.list();
    assertEquals(5, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testUpdateRetriesByProcessInstanceQuery() {
    // given
    startProcessInstance("oneExternalTaskProcess", 5);

    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery()
        .processDefinitionKey("oneExternalTaskProcess");

    // when
    externalTaskService.updateRetries().processInstanceQuery(processInstanceQuery).set(5);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(5, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testUpdateRetriesByHistoricProcessInstanceQuery() {
    // given
    startProcessInstance("oneExternalTaskProcess", 5);

    HistoricProcessInstanceQuery query = historyService
        .createHistoricProcessInstanceQuery()
        .processDefinitionKey("oneExternalTaskProcess");

    // when
    externalTaskService.updateRetries().historicProcessInstanceQuery(query).set(5);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(5, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(5, (int) task.getRetries());
    }
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testUpdateRetriesByAllParameters() {
    // given
    List<String> ids = startProcessInstance("oneExternalTaskProcess", 5);

    ExternalTask externalTask = externalTaskService
        .createExternalTaskQuery()
        .processInstanceId(ids.get(0))
        .singleResult();

    ExternalTaskQuery externalTaskQuery = externalTaskService
        .createExternalTaskQuery()
        .processInstanceId(ids.get(1));

    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery()
        .processInstanceId(ids.get(2));

    HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(ids.get(3));

    // when
    externalTaskService.updateRetries()
      .externalTaskIds(externalTask.getId())
      .externalTaskQuery(externalTaskQuery)
      .processInstanceQuery(processInstanceQuery)
      .historicProcessInstanceQuery(historicProcessInstanceQuery)
      .processInstanceIds(ids.get(4))
      .set(5);

    // then
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    assertEquals(5, tasks.size());

    for (ExternalTask task : tasks) {
      assertEquals(Integer.valueOf(5), task.getRetries());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testExtendLockTime() {
    final Date oldCurrentTime = ClockUtil.getCurrentTime();
    try {
      // given
      runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
      ClockUtil.setCurrentTime(nowMinus(1000));
      List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(1, WORKER_ID).topic(TOPIC_NAME, LOCK_TIME).execute();

      // when
      Date extendLockTime = new Date();
      ClockUtil.setCurrentTime(extendLockTime);

      externalTaskService.extendLock(lockedTasks.get(0).getId(), WORKER_ID, LOCK_TIME);

      // then
      ExternalTask taskWithExtendedLock = externalTaskService.createExternalTaskQuery().locked().singleResult();
      assertNotNull(taskWithExtendedLock);
      AssertUtil.assertEqualsSecondPrecision(new Date(extendLockTime.getTime() + LOCK_TIME), taskWithExtendedLock.getLockExpirationTime());

    } finally {
      ClockUtil.setCurrentTime(oldCurrentTime);
    }

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testExtendLockTimeThatExpired() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, 1L)
      .execute();

    assertNotNull(lockedTasks);
    assertEquals(1, lockedTasks.size());

    ClockUtil.setCurrentTime(nowPlus(2));
    // when
    try {
      externalTaskService.extendLock(lockedTasks.get(0).getId(), WORKER_ID, 100);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      // then
      assertTrue(e.getMessage().contains("Cannot extend a lock that expired"));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testExtendLockTimeWithoutLock() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().singleResult();
    // when
    try {
      externalTaskService.extendLock(externalTask.getId(), WORKER_ID, 100);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertTrue(e.getMessage().contains("The lock of the External Task " + externalTask.getId() + " cannot be extended by worker '" + WORKER_ID));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testExtendLockTimeWithNullLockTime() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, 1L)
        .execute();

    assertNotNull(lockedTasks);
    assertEquals(1, lockedTasks.size());

    // when
    try {
      externalTaskService.extendLock(lockedTasks.get(0).getId(), WORKER_ID, 0);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      // then
      assertTrue(e.getMessage().contains("lockTime is not greater than 0"));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testExtendLockTimeWithNegativeLockTime() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, 1L)
        .execute();

    assertNotNull(lockedTasks);
    assertEquals(1, lockedTasks.size());

    // when
    try {
      externalTaskService.extendLock(lockedTasks.get(0).getId(), WORKER_ID, -1);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      // then
      assertTrue(e.getMessage().contains("lockTime is not greater than 0"));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testExtendLockTimeWithNullWorkerId() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, 1L)
        .execute();

    assertNotNull(lockedTasks);
    assertEquals(1, lockedTasks.size());

    // when
    try {
      externalTaskService.extendLock(lockedTasks.get(0).getId(), null, 100);
      fail("Exception expected");
    } catch (NullValueException e) {
      // then
      assertTrue(e.getMessage().contains("workerId is null"));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testExtendLockTimeWithDifferentWorkerId() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, 1L)
        .execute();

    assertNotNull(lockedTasks);
    assertEquals(1, lockedTasks.size());

    LockedExternalTask task = lockedTasks.get(0);
    // when
    try {
      externalTaskService.extendLock(task.getId(),"anAnotherWorkerId", 100);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertTrue(e.getMessage().contains("The lock of the External Task " + task.getId() + " cannot be extended by worker 'anAnotherWorkerId'"));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testExtendLockTimeWithNullExternalTask() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(TOPIC_NAME, 1L)
        .execute();

    assertNotNull(lockedTasks);
    assertEquals(1, lockedTasks.size());

    // when
    try {
      externalTaskService.extendLock(null, WORKER_ID, 100);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertTrue(e.getMessage().contains("Cannot find external task with id null"));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testExtendLockTimeForUnexistingExternalTask() {
    // when
    try {
      externalTaskService.extendLock("unexisting", WORKER_ID, 100);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertTrue(e.getMessage().contains("Cannot find external task with id unexisting"));
    }
  }

  @Test
  public void testCompleteWithLocalVariables() {
    // given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("Process").startEvent().serviceTask("externalTask")
        .camundaType("external").camundaTopic("foo").camundaTaskPriority("100")
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, ReadLocalVariableListenerImpl.class)
        .userTask("user").endEvent().done();

   testRule.deploy(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(1, WORKER_ID).topic("foo", 1L).execute();

    // when
    externalTaskService.complete(lockedTasks.get(0).getId(), WORKER_ID, null,
        Variables.createVariables().putValue("abc", "bar"));

    // then
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId()).singleResult();
    assertNull(variableInstance);
    if (processEngineConfiguration.getHistoryLevel() == HistoryLevel.HISTORY_LEVEL_AUDIT
        || processEngineConfiguration.getHistoryLevel() == HistoryLevel.HISTORY_LEVEL_FULL) {
      HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
          .activityInstanceIdIn(lockedTasks.get(0).getActivityInstanceId()).singleResult();
      assertNotNull(historicVariableInstance);
      assertEquals("abc", historicVariableInstance.getName());
      assertEquals("bar", historicVariableInstance.getValue());
    }
  }

  @Test
  public void testCompleteWithNonLocalVariables() {
    // given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("Process").startEvent().serviceTask("externalTask")
        .camundaType("external").camundaTopic("foo").camundaTaskPriority("100")
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, ReadLocalVariableListenerImpl.class)
        .userTask("user").endEvent().done();

   testRule.deploy(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(1, WORKER_ID).topic("foo", 1L).execute();

    // when
    externalTaskService.complete(lockedTasks.get(0).getId(), WORKER_ID,
        Variables.createVariables().putValue("abc", "bar"), null);

    // then
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId()).singleResult();
    assertNotNull(variableInstance);
    assertEquals("bar", variableInstance.getValue());
    assertEquals("abc", variableInstance.getName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testFetchWithEmptyListOfVariables() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID).topic("externalTaskTopic", LOCK_TIME).variables()
      .execute();

    // then
    assertEquals(1, tasks.size());

    LockedExternalTask task = tasks.get(0);
    assertNotNull(task.getId());
    assertEquals(0, task.getVariables().size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByBusinessKey() {
    // given
    String topicName1 = "topic1";
    String topicName2 = "topic2";
    String topicName3 = "topic3";

    String businessKey1 = "testBusinessKey1";
    String businessKey2 = "testBusinessKey2";

    Long lockDuration = 60L * 1000L;

    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey1);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey2);

    //when
    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
          .businessKey(businessKey1)
        .topic(topicName2, lockDuration)
          .businessKey(businessKey2)
        .topic(topicName3, lockDuration)
          .businessKey("fakeBusinessKey")
        .execute();

    //then
    assertEquals(2, topicTasks.size());

    for (LockedExternalTask externalTask : topicTasks) {
      ProcessInstance pi = runtimeService.createProcessInstanceQuery()
          .processInstanceId(externalTask.getProcessInstanceId())
          .singleResult();
      if (externalTask.getTopicName().equals(topicName1)) {
        assertEquals(businessKey1, pi.getBusinessKey());
        assertEquals(businessKey1, externalTask.getBusinessKey());
      } else if (externalTask.getTopicName().equals(topicName2)){
        assertEquals(businessKey2, pi.getBusinessKey());
        assertEquals(businessKey2, externalTask.getBusinessKey());
      } else {
        fail("No other topic name values should be available!");
      }
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByBusinessKeyCombination1() {
    // given
    String topicName1 = "topic1";
    String topicName2 = "topic2";

    String businessKey1 = "testBusinessKey1";
    String businessKey2 = "testBusinessKey2";

    Long lockDuration = 60L * 1000L;

    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey1);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey2);

    //when
    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
          .businessKey(businessKey1)
        .topic(topicName2, lockDuration)
        .execute();

    //then
    assertEquals(3, topicTasks.size());

    for (LockedExternalTask externalTask : topicTasks) {
      ProcessInstance pi = runtimeService.createProcessInstanceQuery()
          .processInstanceId(externalTask.getProcessInstanceId())
          .singleResult();
      if (externalTask.getTopicName().equals(topicName1)) {
        assertEquals(businessKey1, pi.getBusinessKey());
        assertEquals(businessKey1, externalTask.getBusinessKey());
      } else if (externalTask.getTopicName().equals(topicName2)){
        assertEquals(pi.getBusinessKey(), externalTask.getBusinessKey());
      } else {
        fail("No other topic name values should be available!");
      }
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByBusinessKeyCombination2() {
    // given
    String topicName1 = "topic1";
    String topicName2 = "topic2";

    String businessKey1 = "testBusinessKey1";
    String businessKey2 = "testBusinessKey2";

    Long lockDuration = 60L * 1000L;

    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey1);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey2);

    //when
    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
        .topic(topicName2, lockDuration)
          .businessKey(businessKey2)
        .execute();

    //then
    assertEquals(3, topicTasks.size());

    for (LockedExternalTask externalTask : topicTasks) {
      ProcessInstance pi = runtimeService.createProcessInstanceQuery()
          .processInstanceId(externalTask.getProcessInstanceId())
          .singleResult();
      if (externalTask.getTopicName().equals(topicName1)) {
        assertEquals(pi.getBusinessKey(), externalTask.getBusinessKey());
      } else if (externalTask.getTopicName().equals(topicName2)){
        assertEquals(businessKey2, pi.getBusinessKey());
        assertEquals(businessKey2, externalTask.getBusinessKey());
      } else {
        fail("No other topic name values should be available!");
      }
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByBusinessKeyLocking() {
    // given
    String topicName1 = "topic1";
    String topicName2 = "topic2";
    String topicName3 = "topic3";

    String businessKey1 = "testBusinessKey1";
    String businessKey2 = "testBusinessKey2";

    Long lockDuration = 60L * 1000L;

    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey1);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess", businessKey2);

    //when
    List<LockedExternalTask> lockedTopicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
          .businessKey(businessKey1)
        .topic(topicName2, lockDuration)
          .businessKey(businessKey2)
        .topic(topicName3, lockDuration)
          .businessKey("fakeBusinessKey")
        .execute();

    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
          .businessKey(businessKey1)
        .topic(topicName2, 2 * lockDuration)
          .businessKey(businessKey2)
        .topic(topicName3, 2 * lockDuration)
          .businessKey(businessKey1)
        .execute();

    //then
    assertEquals(2, lockedTopicTasks.size());
    assertEquals(1, topicTasks.size());

    LockedExternalTask externalTask = topicTasks.get(0);
    ProcessInstance pi = runtimeService.createProcessInstanceQuery()
      .processInstanceId(externalTask.getProcessInstanceId())
      .singleResult();

    assertEquals(businessKey1, pi.getBusinessKey());
    assertEquals(businessKey1, externalTask.getBusinessKey());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testVariableValueTopicQuery.bpmn20.xml")
  @Test
  public void testTopicQueryByVariableValue() {
    // given
    String topicName1 = "testTopic1";
    String topicName2 = "testTopic2";

    String variableName = "testVariable";
    String variableValue1 = "testValue1";
    String variableValue2 = "testValue2";

    Map<String, Object> variables = new HashMap<>();

    Long lockDuration = 60L * 1000L;

    //when
    variables.put(variableName, variableValue1);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcessTopicQueryVariableValues", variables);

    variables.put(variableName, variableValue2);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcessTopicQueryVariableValues", variables);

    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
          .processInstanceVariableEquals(variableName, variableValue1)
        .topic(topicName2, lockDuration)
          .processInstanceVariableEquals(variableName, variableValue2)
        .execute();

    //then
    assertEquals(2, topicTasks.size());

    for (LockedExternalTask externalTask : topicTasks) {
      if (externalTask.getTopicName().equals(topicName1)) {
        assertEquals(variableValue1, externalTask.getVariables().get(variableName));
      } else if (externalTask.getTopicName().equals(topicName2)){
        assertEquals(variableValue2, externalTask.getVariables().get(variableName));
      } else {
        fail("No other topic name values should be available!");
      }
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testVariableValueTopicQuery.bpmn20.xml")
  @Test
  public void testTopicQueryByVariableValueLocking() {
    // given
    String topicName1 = "testTopic1";
    String topicName2 = "testTopic2";
    String topicName3 = "testTopic3";

    String variableName = "testVariable";
    String variableValue1 = "testValue1";
    String variableValue2 = "testValue2";

    Map<String, Object> variables = new HashMap<>();

    Long lockDuration = 60L * 1000L;

    //when
    variables.put(variableName, variableValue1);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcessTopicQueryVariableValues", variables);

    variables.put(variableName, variableValue2);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcessTopicQueryVariableValues", variables);

    List<LockedExternalTask> lockedTopicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
          .processInstanceVariableEquals(variableName, variableValue1)
        .topic(topicName2, lockDuration)
          .processInstanceVariableEquals(variableName, variableValue2)
        .execute();

    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, 2 * lockDuration)
          .processInstanceVariableEquals(variableName, variableValue1)
        .topic(topicName2, 2 * lockDuration)
          .processInstanceVariableEquals(variableName, variableValue2)
        .topic(topicName3, lockDuration)
          .processInstanceVariableEquals(variableName, variableValue2)
        .execute();

    //then
    assertEquals(2, lockedTopicTasks.size());
    assertEquals(1, topicTasks.size());

    LockedExternalTask externalTask = topicTasks.get(0);
    assertEquals(topicName3, externalTask.getTopicName());
    assertEquals(variableValue2, externalTask.getVariables().get(variableName));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testVariableValueTopicQuery.bpmn20.xml")
  @Test
  public void testTopicQueryByVariableValues() {
    // given
    String topicName1 = "testTopic1";
    String topicName2 = "testTopic2";
    String topicName3 = "testTopic3";

    String variableName1 = "testVariable1";
    String variableName2 = "testVariable2";
    String variableName3 = "testVariable3";

    String variableValue1 = "testValue1";
    String variableValue2 = "testValue2";
    String variableValue3 = "testValue3";
    String variableValue4 = "testValue4";
    String variableValue5 = "testValue5";
    String variableValue6 = "testValue6";

    Map<String, Object> variables = new HashMap<>();

    Long lockDuration = 60L * 1000L;

    //when
    variables.put(variableName1, variableValue1);
    variables.put(variableName2, variableValue2);
    variables.put(variableName3, variableValue3);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcessTopicQueryVariableValues", variables);

    variables.put(variableName1, variableValue4);
    variables.put(variableName2, variableValue5);
    variables.put(variableName3, variableValue6);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcessTopicQueryVariableValues", variables);

    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
          .processInstanceVariableEquals(variableName1, variableValue1)
          .processInstanceVariableEquals(variableName2, variableValue2)
        .topic(topicName2, lockDuration)
          .processInstanceVariableEquals(variableName2, variableValue5)
          .processInstanceVariableEquals(variableName3, variableValue6)
        .topic(topicName3, lockDuration)
          .processInstanceVariableEquals(variableName1, "fakeVariableValue")
        .execute();

    //then
    assertEquals(2, topicTasks.size());

    for (LockedExternalTask externalTask : topicTasks) {
      // topic names are not always in the same order
      if (externalTask.getTopicName().equals(topicName1)) {
        assertEquals(variableValue1, externalTask.getVariables().get(variableName1));
        assertEquals(variableValue2, externalTask.getVariables().get(variableName2));
      } else if (externalTask.getTopicName().equals(topicName2)){
        assertEquals(variableValue5, externalTask.getVariables().get(variableName2));
        assertEquals(variableValue6, externalTask.getVariables().get(variableName3));
      } else {
        fail("No other topic name values should be available!");
      }

    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testVariableValueTopicQuery.bpmn20.xml")
  @Test
  public void testTopicQueryByBusinessKeyAndVariableValue() {
    // given
    String topicName1 = "testTopic1";
    String topicName2 = "testTopic2";
    String topicName3 = "testTopic3";

    String businessKey1 = "testBusinessKey1";
    String businessKey2 = "testBusinessKey2";

    String variableName = "testVariable1";
    String variableValue1 = "testValue1";
    String variableValue2 = "testValue2";

    Map<String, Object> variables = new HashMap<>();

    Long lockDuration = 60L * 1000L;

    //when
    variables.put(variableName, variableValue1);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcessTopicQueryVariableValues", businessKey1, variables);
    variables.put(variableName, variableValue2);
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcessTopicQueryVariableValues", businessKey2, variables);

    List<LockedExternalTask> topicTasks = externalTaskService
        .fetchAndLock(3, "externalWorkerId")
        .topic(topicName1, lockDuration)
          .businessKey(businessKey1)
          .processInstanceVariableEquals(variableName, variableValue1)
        .topic(topicName2, lockDuration)
          .businessKey(businessKey2)
          .processInstanceVariableEquals(variableName, variableValue2)
        .topic(topicName3, lockDuration)
          .businessKey("fakeBusinessKey")
        .execute();

    //then
    assertEquals(2, topicTasks.size());

    for (LockedExternalTask externalTask : topicTasks) {
      ProcessInstance pi = runtimeService.createProcessInstanceQuery()
          .processInstanceId(externalTask.getProcessInstanceId())
          .singleResult();
      // topic names are not always in the same order
      if (externalTask.getTopicName().equals(topicName1)) {
        assertEquals(businessKey1, pi.getBusinessKey());
        assertEquals(variableValue1, externalTask.getVariables().get(variableName));
      } else if (externalTask.getTopicName().equals(topicName2)){
        assertEquals(businessKey2, pi.getBusinessKey());
        assertEquals(variableValue2, externalTask.getVariables().get(variableName));
      } else {
        fail("No other topic name values should be available!");
      }
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchAndLockByProcessDefinitionVersionTag.bpmn20.xml"})
  @Test
  public void testFetchAndLockByProcessDefinitionVersionTag() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess"); // no version tag
    runtimeService.startProcessInstanceByKey("testFetchAndLockByProcessDefinitionVersionTag"); // version tag: version X.Y

    // when
    Long totalExternalTasks = externalTaskService.createExternalTaskQuery().count();
    List<LockedExternalTask> fetchedExternalTasks = externalTaskService.fetchAndLock(1, "workerID")
        .topic("externalTaskTopic", 1000L).processDefinitionVersionTag("version X.Y").execute();

    //then
    assertThat(totalExternalTasks).isEqualTo(2);
    assertThat(fetchedExternalTasks.size()).isEqualTo(1);
    assertThat(fetchedExternalTasks.get(0).getProcessDefinitionKey()).isEqualTo("testFetchAndLockByProcessDefinitionVersionTag");
    assertThat(fetchedExternalTasks.get(0).getProcessDefinitionVersionTag()).isEqualTo("version X.Y");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchMultipleTopics.bpmn20.xml"})
  @Test
  public void testGetTopicNamesWithLockedTasks(){
    //given
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess");
    externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic("topic1", LOCK_TIME)
      .execute();

    //when
    List<String> result = externalTaskService.getTopicNames(true, false, false);

    //then
    assertThat(result).containsExactly("topic1");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchMultipleTopics.bpmn20.xml"})
  @Test
  public void testGetTopicNamesWithUnlockedTasks(){
    //given
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess");
    externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic("topic1", LOCK_TIME)
      .execute();

    //when
    List<String> result = externalTaskService.getTopicNames(false,true,false);

    //then
    assertThat(result).containsExactlyInAnyOrder("topic2", "topic3");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchMultipleTopics.bpmn20.xml"})
  @Test
  public void testGetTopicNamesWithRetries(){
    //given
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess");

    ExternalTask topic1Task = externalTaskService.createExternalTaskQuery().topicName("topic1").singleResult();
    ExternalTask topic2Task = externalTaskService.createExternalTaskQuery().topicName("topic2").singleResult();
    ExternalTask topic3Task = externalTaskService.createExternalTaskQuery().topicName("topic3").singleResult();

    externalTaskService.setRetries(topic1Task.getId(), 3);
    externalTaskService.setRetries(topic2Task.getId(), 0);
    externalTaskService.setRetries(topic3Task.getId(), 0);

    //when
    List<String> result = externalTaskService.getTopicNames(false,false,true);

    //then
    assertThat(result).containsExactly("topic1");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchMultipleTopics.bpmn20.xml"})
  @Test
  public void testGetTopicNamesisDistinct(){
    //given
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess");
    runtimeService.startProcessInstanceByKey("parallelExternalTaskProcess");

    // when
    List<String> result = externalTaskService.getTopicNames();

    //then
    assertThat(result).containsExactlyInAnyOrder("topic1", "topic2", "topic3");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchAndLockWithExtensionProperties.bpmn20.xml" })
  @Test
  public void testFetchAndLockWithExtensionProperties_shouldReturnProperties() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithCustomProperties");

    // when
    List<LockedExternalTask> lockedExternalTasks = externalTaskService.fetchAndLock(1, WORKER_ID).topic(TOPIC_NAME, LOCK_TIME).includeExtensionProperties()
        .execute();

    // then
    assertThat(lockedExternalTasks).hasSize(1);
    assertThat(lockedExternalTasks.get(0).getExtensionProperties()).containsOnly(entry("property1", "value1"), entry("property2", "value2"),
        entry("property3", "value3"));
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchAndLockWithoutExtensionProperties.bpmn20.xml" })
  @Test
  public void testFetchAndLockWithExtensionProperties_shouldReturnEmptyMap() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithoutCustomProperties");

    // when
    List<LockedExternalTask> lockedExternalTasks = externalTaskService.fetchAndLock(1, WORKER_ID).topic(TOPIC_NAME, LOCK_TIME).includeExtensionProperties()
        .execute();

    // then
    assertThat(lockedExternalTasks).hasSize(1);
    assertThat(lockedExternalTasks.get(0).getExtensionProperties()).isEmpty();
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/externaltask/ExternalTaskServiceTest.testFetchAndLockWithExtensionProperties.bpmn20.xml" })
  @Test
  public void testFetchAndLockWithoutExtensionProperties_shouldReturnEmptyMap() {
    // given
    runtimeService.startProcessInstanceByKey("oneExternalTaskWithCustomProperties");

    // when
    List<LockedExternalTask> lockedExternalTasks = externalTaskService.fetchAndLock(1, WORKER_ID).topic(TOPIC_NAME, LOCK_TIME).execute();

    // then
    assertThat(lockedExternalTasks).hasSize(1);
    assertThat(lockedExternalTasks.get(0).getExtensionProperties()).isEmpty();
  }



  protected Date nowPlus(long millis) {
    return new Date(ClockUtil.getCurrentTime().getTime() + millis);
  }

  protected Date nowMinus(long millis) {
    return new Date(ClockUtil.getCurrentTime().getTime() - millis);
  }

  protected List<String> startProcessInstance(String key, int instances) {
    List<String> ids = new ArrayList<>();
    for (int i = 0; i < instances; i++) {
      ids.add(runtimeService.startProcessInstanceByKey(key, String.valueOf(i)).getId());
    }
    return ids;
  }

  protected void verifyVariables(LockedExternalTask task) {
    VariableMap variables = task.getVariables();
    assertEquals(4, variables.size());

    assertEquals(42, variables.get("processVar1"));
    assertEquals(43, variables.get("processVar2"));
    assertEquals(44L, variables.get("subProcessVar"));
    assertEquals(45L, variables.get("taskVar"));
  }

  protected List<HistoricExternalTaskLog> getHistoricTaskLogOrdered(String taskId) {
    return historyService.createHistoricExternalTaskLogQuery()
        .failureLog()
        .externalTaskId(taskId)
        .orderByTimestamp().desc()
        .list();
  }

  public static class ReadLocalVariableListenerImpl implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
      String value = (String) execution.getVariable("abc");
      assertEquals("bar", value);
    }
  }

}
