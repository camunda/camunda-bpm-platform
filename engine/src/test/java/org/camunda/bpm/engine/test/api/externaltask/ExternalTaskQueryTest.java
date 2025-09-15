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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.*;
import static org.junit.Assert.*;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskQueryTest extends PluggableProcessEngineTest {

  protected static final String WORKER_ID = "aWorkerId";
  protected static final String TOPIC_NAME = "externalTaskTopic";
  protected static final String ERROR_MESSAGE = "error";
  // The range of Oracle's NUMBER field is limited to ~10e+125
  // which is below Double.MAX_VALUE, so we only test with the following
  // max value
  protected static final double MAX_DOUBLE_VALUE = 10E+124;

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

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
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

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessDefinitionKey() throws Exception {
    assertEquals(0, externalTaskService.createExternalTaskQuery().processDefinitionKey("oneExternalTaskProcess").count());
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    assertEquals(1, externalTaskService.createExternalTaskQuery().processDefinitionKey("oneExternalTaskProcess").count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessDefinitionKeyIn() throws Exception {
    assertEquals(0, externalTaskService.createExternalTaskQuery().processDefinitionKeyIn("oneExternalTaskProcess").count());
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    assertEquals(1, externalTaskService.createExternalTaskQuery().processDefinitionKeyIn("oneExternalTaskProcess").count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessDefinitionName() throws Exception {
    assertEquals(0, externalTaskService.createExternalTaskQuery().processDefinitionName("One external task process").count());
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    assertEquals(1, externalTaskService.createExternalTaskQuery().processDefinitionName("One external task process").count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void processDefinitionNameLike() throws Exception {
    assertEquals(0, externalTaskService.createExternalTaskQuery().processDefinitionNameLike("One external task proc%").count());
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");
    assertEquals(1, externalTaskService.createExternalTaskQuery().processDefinitionNameLike("One external task proc%").count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueEquals() throws Exception {
    Map<String, Object> variables = new HashMap<>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    // Start process-instance with all types of variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", variables);

    // Test query matches
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("longVar", 928374L).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("shortVar",  (short) 123).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("integerVar", 1234).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("stringVar", "stringValue").count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("booleanVar", true).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("dateVar", date).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("nullVar", null).count());

    // Test query for other values on existing variables
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("longVar", 999L).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("shortVar",  (short) 999).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("integerVar", 999).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("stringVar", "999").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("booleanVar", false).count());
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("dateVar", otherDate.getTime()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("nullVar", "999").count());

    // Test querying for task variables not equals
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotEquals("longVar", 999L).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotEquals("shortVar",  (short) 999).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotEquals("integerVar", 999).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotEquals("stringVar", "999").count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotEquals("booleanVar", false).count());

    // and query for the existing variable with NOT should result in nothing found:
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueNotEquals("longVar", 928374L).count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableNameEqualsIgnoreCase() throws Exception {
    String variableName = "someVariable";
    String variableValue = "someCamelCaseValue";
    Map<String, Object> variables = new HashMap<>();
    variables.put(variableName, variableValue);

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", variables);

    // query for case-insensitive variable name should only return a result if case-insensitive search is used
    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableNamesIgnoreCase().processVariableValueEquals(variableName.toLowerCase(), variableValue).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals(variableName.toLowerCase(), variableValue).count());

    // query should treat all variables case-insensitively, even when flag is set after variable
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals(variableName.toLowerCase(), variableValue).matchVariableNamesIgnoreCase().count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueEqualsIgnoreCase() throws Exception {
    String variableName = "someVariable";
    String variableValue = "someCamelCaseValue";
    Map<String, Object> variables = new HashMap<>();
    variables.put(variableName, variableValue);

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", variables);

    // query for existing variable should return one result
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals(variableName, variableValue).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueEquals(variableName, variableValue.toLowerCase()).count());

    // query for non existing variable should return zero results
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("nonExistentVariable", variableValue.toLowerCase()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueEquals("nonExistentVariable", variableValue.toLowerCase()).count());

    // query for existing variable with different value should return zero results
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals(variableName, "nonExistentValue").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueEquals(variableName, "nonExistentValue".toLowerCase()).count());

    // query for case-insensitive variable value should only return a result when case-insensitive search is used
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals(variableName, variableValue.toLowerCase()).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueEquals(variableName, variableValue.toLowerCase()).count());

    // query for case-insensitive variable with not equals operator should only return a result when case-sensitive search is used
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotEquals(variableName, variableValue.toLowerCase()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotEquals(variableName, variableValue.toLowerCase()).count());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueLike() throws Exception {
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "stringValue");
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", variables);

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLike("stringVar", "stringVal%").count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLike("stringVar", "%ngValue").count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLike("stringVar", "%ngVal%").count());

    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLike("stringVar", "stringVar%").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLike("stringVar", "%ngVar").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLike("stringVar", "%ngVar%").count());

    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLike("stringVar", "stringVal").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLike("nonExistingVar", "string%").count());

    // test with null value
    try {
        externalTaskService.createExternalTaskQuery().processVariableValueLike("stringVar", null).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueLikeIgnoreCase() throws Exception {

    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "stringValue");
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", variables);

    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "%ngValue".toLowerCase()).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "%ngVal%".toLowerCase()).count());

    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "stringVar%".toLowerCase()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "%ngVar".toLowerCase()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "%ngVar%".toLowerCase()).count());

    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", "stringVal".toLowerCase()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("nonExistingVar", "stringVal%".toLowerCase()).count());

    // test with null value
    try {
        externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueLike("stringVar", null).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueNotLike() throws Exception {
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "stringValue");
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", variables);

    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueNotLike("stringVar", "stringVal%").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueNotLike("stringVar", "%ngValue").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueNotLike("stringVar", "%ngVal%").count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotLike("stringVar", "stringVar%").count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotLike("stringVar", "%ngVar").count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotLike("stringVar", "%ngVar%").count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotLike("stringVar", "stringVal").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueNotLike("nonExistingVar", "string%").count());

    // test with null value
    assertThatThrownBy(() -> externalTaskService.createExternalTaskQuery().processVariableValueNotLike("stringVar", null).count())
            .isInstanceOf(ProcessEngineException.class);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueNotLikeIgnoreCase() throws Exception {
    Map<String, Object> variables = new HashMap<>();
    variables.put("stringVar", "stringValue");
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", variables);

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueNotLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "stringVal%".toLowerCase()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "%ngValue".toLowerCase()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "%ngVal%".toLowerCase()).count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "stringVar%".toLowerCase()).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "%ngVar".toLowerCase()).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "%ngVar%".toLowerCase()).count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", "stringVal".toLowerCase()).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("nonExistingVar", "stringVal%".toLowerCase()).count());

    // test with null value
    assertThatThrownBy(() -> externalTaskService.createExternalTaskQuery().matchVariableValuesIgnoreCase().processVariableValueNotLike("stringVar", null).count())
            .isInstanceOf(ProcessEngineException.class);
  }

  @Deployment(resources="org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueCompare() throws Exception {

    Map<String, Object> variables = new HashMap<>();
    variables.put("numericVar", 928374);
    Date date = new GregorianCalendar(2014, 2, 2, 2, 2, 2).getTime();
    variables.put("dateVar", date);
    variables.put("stringVar", "ab");
    variables.put("nullVar", null);

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess", variables);

    // test compare methods with numeric values
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("numericVar", 928373).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("numericVar", 928374).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("numericVar", 928375).count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("numericVar", 928373).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("numericVar", 928374).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("numericVar", 928375).count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("numericVar", 928375).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("numericVar", 928374).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("numericVar", 928373).count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("numericVar", 928375).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("numericVar", 928374).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("numericVar", 928373).count());

    // test compare methods with date values
    Date before = new GregorianCalendar(2014, 2, 2, 2, 2, 1).getTime();
    Date after = new GregorianCalendar(2014, 2, 2, 2, 2, 3).getTime();

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("dateVar", before).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("dateVar", date).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("dateVar", after).count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("dateVar", before).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("dateVar", date).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("dateVar", after).count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("dateVar", after).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("dateVar", date).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("dateVar", before).count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("dateVar", after).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("dateVar", date).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("dateVar", before).count());

    //test with string values
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("stringVar", "aa").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("stringVar", "ab").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("stringVar", "ba").count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("stringVar", "aa").count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("stringVar", "ab").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("stringVar", "ba").count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("stringVar", "ba").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("stringVar", "ab").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("stringVar", "aa").count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("stringVar", "ba").count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("stringVar", "ab").count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("stringVar", "aa").count());

    // test with null value
    try {
        externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("nullVar", null).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
        externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("nullVar", null).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
        externalTaskService.createExternalTaskQuery().processVariableValueLessThan("nullVar", null).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
        externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("nullVar", null).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}

    // test with boolean value
    try {
        externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("nullVar", true).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
        externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("nullVar", false).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
        externalTaskService.createExternalTaskQuery().processVariableValueLessThan("nullVar", true).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
    try {
        externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("nullVar", false).count();
        fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}

    // test non existing variable
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("nonExisting", 123).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueEqualsNumber() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", 123));

    // untyped null (should not match)
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", null));

    // typed null (should not match)
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue(null)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testProcessVariableValueNumberComparison() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", 123));

    // untyped null
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", null));

    // typed null
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, externalTaskService.createExternalTaskQuery().processVariableValueNotEquals("var", Variables.numberValue(123)).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThan("var", Variables.numberValue(123)).count());
    assertEquals(5, externalTaskService.createExternalTaskQuery().processVariableValueGreaterThanOrEquals("var", Variables.numberValue(123)).count());
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueLessThan("var", Variables.numberValue(123)).count());
    assertEquals(4, externalTaskService.createExternalTaskQuery().processVariableValueLessThanOrEquals("var", Variables.numberValue(123)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testVariableEqualsNumberMax() throws Exception {
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", MAX_DOUBLE_VALUE));
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", Long.MAX_VALUE));

    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue(MAX_DOUBLE_VALUE)).count());
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue(Long.MAX_VALUE)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testVariableEqualsNumberLongValueOverflow() throws Exception {
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", MAX_DOUBLE_VALUE));

    // this results in an overflow
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", (long) MAX_DOUBLE_VALUE));

    // the query should not find the long variable
    assertEquals(1, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue(MAX_DOUBLE_VALUE)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  @Test
  public void testVariableEqualsNumberNonIntegerDoubleShouldNotMatchInteger() throws Exception {
    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Variables.createVariables().putValue("var", 42).putValue("var2", 52.4d));

    // querying by 42.4 should not match the integer variable 42
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue(42.4d)).count());

    runtimeService.startProcessInstanceByKey("oneExternalTaskProcess",
            Collections.<String, Object>singletonMap("var", 42.4d));

    // querying by 52 should not find the double variable 52.4
    assertEquals(0, externalTaskService.createExternalTaskQuery().processVariableValueEquals("var", Variables.numberValue(52)).count());
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
