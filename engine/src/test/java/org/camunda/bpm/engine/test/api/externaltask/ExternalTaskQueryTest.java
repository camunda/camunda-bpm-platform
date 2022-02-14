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
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.externalTaskById;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.externalTaskByLockExpirationTime;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.externalTaskByProcessDefinitionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.externalTaskByProcessDefinitionKey;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.externalTaskByProcessInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskQueryTest extends PluggableProcessEngineTest {

  protected static final String WORKER_ID = "aWorkerId";
  protected static final String TOPIC_NAME = "externalTaskTopic";
  protected static final String ERROR_MESSAGE = "error";

  @Before
  public void setUp() throws Exception {
    ClockUtil.setCurrentTime(new Date());
  }

  @After
  public void tearDown() throws Exception {
    ClockUtil.reset();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testSingleResult() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().singleResult();

    // then
    assertNotNull(externalTask.getId());

    assertEquals(processInstance.getId(), externalTask.getProcessInstanceId());
    assertEquals("externalTask", externalTask.getActivityId());
    assertNotNull(externalTask.getActivityInstanceId());
    assertNotNull(externalTask.getExecutionId());
    assertEquals(processInstance.getProcessDefinitionId(), externalTask.getProcessDefinitionId());
    assertEquals("oneExternalTaskProcess", externalTask.getProcessDefinitionKey());
    assertEquals(TOPIC_NAME, externalTask.getTopicName());
    assertNull(externalTask.getWorkerId());
    assertNull(externalTask.getLockExpirationTime());
    assertFalse(externalTask.isSuspended());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testList() {
    startInstancesByKey("oneExternalTaskProcess", 5);
    assertEquals(5, externalTaskService.createExternalTaskQuery().list().size());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testCount() {
    startInstancesByKey("oneExternalTaskProcess", 5);
    assertEquals(5, externalTaskService.createExternalTaskQuery().count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByLockState() {
    // given
    startInstancesByKey("oneExternalTaskProcess", 5);
    lockInstances(TOPIC_NAME, 10000L, 3, WORKER_ID);

    // when
    List<ExternalTask> lockedTasks = externalTaskService.createExternalTaskQuery().locked().list();
    List<ExternalTask> nonLockedTasks = externalTaskService.createExternalTaskQuery().notLocked().list();

    // then
    assertEquals(3, lockedTasks.size());
    for (ExternalTask task : lockedTasks) {
      assertNotNull(task.getLockExpirationTime());
    }

    assertEquals(2, nonLockedTasks.size());
    for (ExternalTask task : nonLockedTasks) {
      assertNull(task.getLockExpirationTime());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByProcessDefinitionId() {
    // given
    org.camunda.bpm.engine.repository.Deployment secondDeployment = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    startInstancesById(processDefinitions.get(0).getId(), 3);
    startInstancesById(processDefinitions.get(1).getId(), 2);

    // when
    List<ExternalTask> definition1Tasks = externalTaskService
        .createExternalTaskQuery()
        .processDefinitionId(processDefinitions.get(0).getId())
        .list();
    List<ExternalTask> definition2Tasks = externalTaskService
        .createExternalTaskQuery()
        .processDefinitionId(processDefinitions.get(1).getId())
        .list();

    // then
    assertEquals(3, definition1Tasks.size());
    for (ExternalTask task : definition1Tasks) {
      assertEquals(processDefinitions.get(0).getId(), task.getProcessDefinitionId());
    }

    assertEquals(2, definition2Tasks.size());
    for (ExternalTask task : definition2Tasks) {
      assertEquals(processDefinitions.get(1).getId(), task.getProcessDefinitionId());
    }

    // cleanup
    repositoryService.deleteDeployment(secondDeployment.getId(), true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByActivityId() {
    // given
    startInstancesByKey("parallelExternalTaskProcess", 3);

    // when
    List<ExternalTask> tasks = externalTaskService
        .createExternalTaskQuery()
        .activityId("externalTask2")
        .list();

    // then
    assertEquals(3, tasks.size());
    for (ExternalTask task : tasks) {
      assertEquals("externalTask2", task.getActivityId());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByActivityIdIn() {
    // given
    startInstancesByKey("parallelExternalTaskProcess", 3);

    List<String> activityIds = Arrays.asList("externalTask1", "externalTask2");

    // when
    List<ExternalTask> tasks = externalTaskService
        .createExternalTaskQuery()
        .activityIdIn(activityIds.toArray(new String[0]))
        .list();

    // then
    assertEquals(6, tasks.size());
    for (ExternalTask task : tasks) {
      assertTrue(activityIds.contains(task.getActivityId()));
    }
  }

  @Test
  public void testFailQueryByActivityIdInNull() {
    try {
      externalTaskService.createExternalTaskQuery()
          .activityIdIn((String) null);
      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/parallelExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByTopicName() {
    // given
    startInstancesByKey("parallelExternalTaskProcess", 3);

    // when
    List<ExternalTask> topic1Tasks = externalTaskService
        .createExternalTaskQuery()
        .topicName("topic1")
        .list();

    // then
    assertEquals(3, topic1Tasks.size());
    for (ExternalTask task : topic1Tasks) {
      assertEquals("topic1", task.getTopicName());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByProcessInstanceId() {
    // given
    List<ProcessInstance> processInstances = startInstancesByKey("oneExternalTaskProcess", 3);

    // when
    ExternalTask task = externalTaskService
      .createExternalTaskQuery()
      .processInstanceId(processInstances.get(0).getId())
      .singleResult();

    // then
    assertNotNull(task);
    assertEquals(processInstances.get(0).getId(), task.getProcessInstanceId());
  }

  /**
   * Excluded on CRDB since the problem does not occur on it. The test execution also times out
   * during test cleanup, on DELETE-ing the deployments, due the the slowness of the SQL statements on CRDB.
   * See CAM-12239 for the performance issue.
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  @RequiredDatabase(excludes = DbSqlSessionFactory.CRDB)
  public void testQueryByLargeListOfProcessInstanceIdIn() {
    // given
    List<String> processInstances = new ArrayList<>();
    for (int i = 0; i < 1001; i++) {
      processInstances.add(runtimeService.startProcessInstanceByKey("oneExternalTaskProcess").getProcessInstanceId());
    }

    // when
    List<ExternalTask> tasks = externalTaskService
      .createExternalTaskQuery()
      .processInstanceIdIn(processInstances.toArray(new String[processInstances.size()]))
      .list();

    // then
    assertNotNull(tasks);
    assertEquals(1001, tasks.size());
    for (ExternalTask task : tasks) {
      assertTrue(processInstances.contains(task.getProcessInstanceId()));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByProcessInstanceIdIn() {
    // given
    List<ProcessInstance> processInstances = startInstancesByKey("oneExternalTaskProcess", 3);

    List<String> processInstanceIds = Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId());

    // when
    List<ExternalTask> tasks = externalTaskService
      .createExternalTaskQuery()
      .processInstanceIdIn(processInstances.get(0).getId(), processInstances.get(1).getId())
      .list();

    // then
    assertNotNull(tasks);
    assertEquals(2, tasks.size());
    for (ExternalTask task : tasks) {
      assertTrue(processInstanceIds.contains(task.getProcessInstanceId()));
    }
  }

  @Test
  public void testQueryByNonExistingProcessInstanceId() {
    ExternalTaskQuery query = externalTaskService
        .createExternalTaskQuery()
        .processInstanceIdIn("nonExisting");

    assertEquals(0, query.count());
  }

  @Test
  public void testQueryByProcessInstanceIdNull() {
    try {
      externalTaskService.createExternalTaskQuery()
        .processInstanceIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByExecutionId() {
    // given
    List<ProcessInstance> processInstances = startInstancesByKey("oneExternalTaskProcess", 3);

    ProcessInstance firstInstance = processInstances.get(0);

    ActivityInstance externalTaskActivityInstance = runtimeService
      .getActivityInstance(firstInstance.getId())
      .getActivityInstances("externalTask")[0];
    String executionId = externalTaskActivityInstance.getExecutionIds()[0];

    // when
    ExternalTask externalTask = externalTaskService
      .createExternalTaskQuery()
      .executionId(executionId)
      .singleResult();

    // then
    assertNotNull(externalTask);
    assertEquals(executionId, externalTask.getExecutionId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByWorkerId() {
    // given
    startInstancesByKey("oneExternalTaskProcess", 10);
    lockInstances(TOPIC_NAME, 10000L, 3, "worker1");
    lockInstances(TOPIC_NAME, 10000L, 4, "worker2");

    // when
    List<ExternalTask> tasks = externalTaskService
      .createExternalTaskQuery()
      .workerId("worker1")
      .list();

    // then
    assertEquals(3, tasks.size());
    for (ExternalTask task : tasks) {
      assertEquals("worker1", task.getWorkerId());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByLockExpirationTime() {
    // given
    startInstancesByKey("oneExternalTaskProcess", 10);
    lockInstances(TOPIC_NAME, 5000L, 3, WORKER_ID);
    lockInstances(TOPIC_NAME, 10000L, 4, WORKER_ID);

    // when
    Date lockDate = new Date(ClockUtil.getCurrentTime().getTime() + 7000L);
    List<ExternalTask> lockedExpirationBeforeTasks = externalTaskService
        .createExternalTaskQuery()
        .lockExpirationBefore(lockDate)
        .list();

    List<ExternalTask> lockedExpirationAfterTasks = externalTaskService
        .createExternalTaskQuery()
        .lockExpirationAfter(lockDate)
        .list();

    // then
    assertEquals(3, lockedExpirationBeforeTasks.size());
    for (ExternalTask task : lockedExpirationBeforeTasks) {
      assertNotNull(task.getLockExpirationTime());
      assertTrue(task.getLockExpirationTime().getTime() < lockDate.getTime());
    }

    assertEquals(4, lockedExpirationAfterTasks.size());
    for (ExternalTask task : lockedExpirationAfterTasks) {
      assertNotNull(task.getLockExpirationTime());
      assertTrue(task.getLockExpirationTime().getTime() > lockDate.getTime());
    }
  }

  @Test
  public void testQueryWithNullValues() {
    try {
      externalTaskService.createExternalTaskQuery().externalTaskId(null).list();
      fail("expected exception");
    } catch (NullValueException e) {
      testRule.assertTextPresent("externalTaskId is null", e.getMessage());
    }

    try {
      externalTaskService.createExternalTaskQuery().activityId(null).list();
      fail("expected exception");
    } catch (NullValueException e) {
      testRule.assertTextPresent("activityId is null", e.getMessage());
    }

    try {
      externalTaskService.createExternalTaskQuery().executionId(null).list();
      fail("expected exception");
    } catch (NullValueException e) {
      testRule.assertTextPresent("executionId is null", e.getMessage());
    }

    try {
      externalTaskService.createExternalTaskQuery().lockExpirationAfter(null).list();
      fail("expected exception");
    } catch (NullValueException e) {
      testRule.assertTextPresent("lockExpirationAfter is null", e.getMessage());
    }

    try {
      externalTaskService.createExternalTaskQuery().lockExpirationBefore(null).list();
      fail("expected exception");
    } catch (NullValueException e) {
      testRule.assertTextPresent("lockExpirationBefore is null", e.getMessage());
    }

    try {
      externalTaskService.createExternalTaskQuery().processDefinitionId(null).list();
      fail("expected exception");
    } catch (NullValueException e) {
      testRule.assertTextPresent("processDefinitionId is null", e.getMessage());
    }

    try {
      externalTaskService.createExternalTaskQuery().processInstanceId(null).list();
      fail("expected exception");
    } catch (NullValueException e) {
      testRule.assertTextPresent("processInstanceId is null", e.getMessage());
    }

    try {
      externalTaskService.createExternalTaskQuery().topicName(null).list();
      fail("expected exception");
    } catch (NullValueException e) {
      testRule.assertTextPresent("topicName is null", e.getMessage());
    }

    try {
      externalTaskService.createExternalTaskQuery().workerId(null).list();
      fail("expected exception");
    } catch (NullValueException e) {
      testRule.assertTextPresent("workerId is null", e.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml"})
  @Test
  public void testQuerySorting() {

    startInstancesByKey("oneExternalTaskProcess", 5);
    startInstancesByKey("twoExternalTaskProcess", 5);

    lockInstances(TOPIC_NAME, 5000L, 5, WORKER_ID);
    lockInstances(TOPIC_NAME, 10000L, 5, WORKER_ID);

    // asc
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().orderById().asc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, externalTaskById());

    tasks = externalTaskService.createExternalTaskQuery().orderByProcessInstanceId().asc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, externalTaskByProcessInstanceId());

    tasks = externalTaskService.createExternalTaskQuery().orderByProcessDefinitionId().asc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, externalTaskByProcessDefinitionId());

    tasks = externalTaskService.createExternalTaskQuery().orderByProcessDefinitionKey().asc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, externalTaskByProcessDefinitionKey());

    tasks = externalTaskService.createExternalTaskQuery().orderByLockExpirationTime().asc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, externalTaskByLockExpirationTime());

    // desc
    tasks = externalTaskService.createExternalTaskQuery().orderById().desc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, inverted(externalTaskById()));

    tasks = externalTaskService.createExternalTaskQuery().orderByProcessInstanceId().desc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, inverted(externalTaskByProcessInstanceId()));

    tasks = externalTaskService.createExternalTaskQuery().orderByProcessDefinitionId().desc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, inverted(externalTaskByProcessDefinitionId()));

    tasks = externalTaskService.createExternalTaskQuery().orderByProcessDefinitionKey().desc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, inverted(externalTaskByProcessDefinitionKey()));

    tasks = externalTaskService.createExternalTaskQuery().orderByLockExpirationTime().desc().list();
    assertEquals(10, tasks.size());
    verifySorting(tasks, inverted(externalTaskByLockExpirationTime()));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryBySuspensionState() {
    // given
    startInstancesByKey("oneExternalTaskProcess", 5);
    suspendInstances(3);

    // when
    List<ExternalTask> suspendedTasks = externalTaskService.createExternalTaskQuery().suspended().list();
    List<ExternalTask> activeTasks = externalTaskService.createExternalTaskQuery().active().list();

    // then
    assertEquals(3, suspendedTasks.size());
    for (ExternalTask task : suspendedTasks) {
      assertTrue(task.isSuspended());
    }

    assertEquals(2, activeTasks.size());
    for (ExternalTask task : activeTasks) {
      assertFalse(task.isSuspended());
      assertFalse(suspendedTasks.contains(task));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByRetries() {
    // given
    startInstancesByKey("oneExternalTaskProcess", 5);

    List<LockedExternalTask> tasks = lockInstances(TOPIC_NAME, 10000L, 3, WORKER_ID);
    failInstances(tasks.subList(0, 2), ERROR_MESSAGE, 0, 5000L);  // two tasks have no retries left
    failInstances(tasks.subList(2, 3), ERROR_MESSAGE, 4, 5000L);  // one task has retries left

    // when
    List<ExternalTask> tasksWithRetries = externalTaskService
        .createExternalTaskQuery().withRetriesLeft().list();
    List<ExternalTask> tasksWithoutRetries = externalTaskService
        .createExternalTaskQuery().noRetriesLeft().list();

    // then
    assertEquals(3, tasksWithRetries.size());
    for (ExternalTask task : tasksWithRetries) {
      assertTrue(task.getRetries() == null || task.getRetries() > 0);
    }

    assertEquals(2, tasksWithoutRetries.size());
    for (ExternalTask task : tasksWithoutRetries) {
      assertTrue(task.getRetries() == 0);
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryById() {
    // given
    startInstancesByKey("oneExternalTaskProcess", 2);
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    ExternalTask firstTask = tasks.get(0);

    // when
    ExternalTask resultTask =
        externalTaskService.createExternalTaskQuery()
          .externalTaskId(firstTask.getId())
          .singleResult();

    // then
    assertEquals(firstTask.getId(), resultTask.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByIds() {
    // given
    startInstancesByKey("oneExternalTaskProcess", 3);
    List<ExternalTask> tasks = externalTaskService.createExternalTaskQuery().list();
    Set<String> ids = new HashSet<>();
    Collections.addAll(ids, tasks.get(0).getId(), tasks.get(1).getId());
    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery().externalTaskIdIn(ids);
    // then
    assertThat(query.count()).isEqualTo(2L);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByIdsWithNull() {
    // given
    Set<String> ids = null;
    try {
      // when
      externalTaskService.createExternalTaskQuery().externalTaskIdIn(ids).list();
    } catch (ProcessEngineException e) {
      // then
      assertThat(e).hasMessage("Set of external task ids is null");
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByIdsWithEmptyList() {
    // given
    Set<String> ids = new HashSet<>();
    try {
      // when
      externalTaskService.createExternalTaskQuery().externalTaskIdIn(ids).list();
    } catch (ProcessEngineException e) {
      // then
      assertThat(e).hasMessage("Set of external task ids is empty");
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryByBusinessKey() {
    // given
    String businessKey = "theUltimateKey";
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", businessKey);

    // when
    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().singleResult();

    // then
    assertNotNull(externalTask);
    assertEquals(businessKey, externalTask.getBusinessKey());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testQueryListByBusinessKey() {
    for (int i = 0; i < 5; i++) {
      runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", "businessKey" + i);
    }

    assertEquals(5, externalTaskService.createExternalTaskQuery().count());
    List<ExternalTask> list = externalTaskService.createExternalTaskQuery().list();
    for (ExternalTask externalTask : list) {
      assertNotNull(externalTask.getBusinessKey());
    }
  }

  @Test
  public void shouldCheckPresenceOfVersionTag() {
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .camundaVersionTag("1.2.3.4")
        .startEvent()
        .serviceTask()
          .camundaExternalTask("my-topic")
        .endEvent()
        .done();

    testRule.deploy(process);

    startInstancesByKey("process", 1);

    ExternalTask task = externalTaskService.createExternalTaskQuery().singleResult();

    assertThat(task.getProcessDefinitionVersionTag()).isEqualTo("1.2.3.4");
  }

  protected List<ProcessInstance> startInstancesByKey(String processDefinitionKey, int number) {
    List<ProcessInstance> processInstances = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      processInstances.add(runtimeService.startProcessInstanceByKey(processDefinitionKey));
    }

    return processInstances;
  }

  protected List<ProcessInstance> startInstancesById(String processDefinitionId, int number) {
    List<ProcessInstance> processInstances = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      processInstances.add(runtimeService.startProcessInstanceById(processDefinitionId));
    }

    return processInstances;
  }

  protected void suspendInstances(int number) {
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().listPage(0, number);

    for (ProcessInstance processInstance : processInstances) {
      runtimeService.suspendProcessInstanceById(processInstance.getId());
    }
  }

  protected List<LockedExternalTask> lockInstances(String topic, long duration, int number, String workerId) {
    return externalTaskService.fetchAndLock(number, workerId).topic(topic, duration).execute();
  }

  protected void failInstances(List<LockedExternalTask> tasks, String errorMessage, int retries, long retryTimeout) {
    this.failInstances(tasks,errorMessage,null,retries,retryTimeout);
  }

  protected void failInstances(List<LockedExternalTask> tasks, String errorMessage, String errorDetails, int retries, long retryTimeout) {
    for (LockedExternalTask task : tasks) {
      externalTaskService.handleFailure(task.getId(), task.getWorkerId(), errorMessage, errorDetails, retries, retryTimeout);
    }
  }

}
