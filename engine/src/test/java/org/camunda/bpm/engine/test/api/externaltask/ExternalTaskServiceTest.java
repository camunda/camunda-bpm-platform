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
package org.camunda.bpm.engine.test.api.externaltask;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.joda.time.DateTime;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskServiceTest extends PluggableProcessEngineTestCase {

  protected static final String WORKER_ID = "aWorkerId";
  protected static final long LOCK_TIME = 10000L;
  protected static final String TOPIC_NAME = "externalTaskTopic";

  protected void setUp() throws Exception {
    ClockUtil.setCurrentTime(new Date());
  }

  protected void tearDown() throws Exception {
    ClockUtil.reset();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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

    assertEquals(nowPlus(LOCK_TIME), task.getLockExpirationTime());

    assertEquals(WORKER_ID, task.getWorkerId());
  }

  @Deployment
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
      assertTextPresent("topicName is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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
      assertTextPresent("workerId is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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
      assertTextPresent("maxResults is not greater than or equal to 0", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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
      assertTextPresent("lockTime is not positive", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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
      assertTextPresent("lockTime is not positive", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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
    assertEquals(nowPlus(LOCK_TIME), topic1Task.getLockExpirationTime());

    assertEquals("topic2", topic2Task.getTopicName());
    assertEquals(nowPlus(LOCK_TIME * 2), topic2Task.getLockExpirationTime());

    // and the third task can still be fetched
    tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("topic1", LOCK_TIME)
      .topic("topic2", LOCK_TIME * 2)
      .topic("topic3", LOCK_TIME * 3)
      .execute();

    assertEquals(1, tasks.size());

    LockedExternalTask topic3Task = tasks.get(0);
    assertEquals("topic3", topic3Task.getTopicName());
    assertEquals(nowPlus(LOCK_TIME * 3), topic3Task.getLockExpirationTime());
  }

  @Deployment
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

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml")
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
    } catch (ProcessEngineException e) {
      assertTextPresent("cannot be completed by worker 'someCrazyWorkerId'. It is locked by worker '" + WORKER_ID + "'.", e.getMessage());
    }
  }

  public void testCompleteNonExistingTask() {
    try {
      externalTaskService.complete("nonExistingTaskId", WORKER_ID);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      assertTextPresent("Cannot find external task with id nonExistingTaskId", e.getMessage());
    }
  }

  public void testCompleteNullTaskId() {
    try {
      externalTaskService.complete(null, WORKER_ID);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      assertTextPresent("externalTaskId is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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
      assertTextPresent("workerId is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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
    assertEquals(nowPlus(LOCK_TIME), task.getLockExpirationTime());

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
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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
      assertTextPresent("cannot be completed by worker '" + WORKER_ID + "'. It is locked by worker 'anotherWorkerId'.", e.getMessage());
    }

    // and the second worker can
    externalTaskService.complete(reclaimedTasks.get(0).getId(), "anotherWorkerId");

    externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  public void testDeleteProcessInstance() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    assertEquals(0, externalTaskService.fetchAndLock(5, WORKER_ID).topic(TOPIC_NAME, LOCK_TIME).execute().size());
    assertProcessEnded(processInstance.getId());
  }


  @Deployment
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

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
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

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
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

  public void testUnlockNullTaskId() {
    try {
      externalTaskService.unlock(null);
      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("externalTaskId is null", e.getMessage());
    }
  }

  public void testUnlockNonExistingTask() {
    try {
      externalTaskService.unlock("nonExistingId");
      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("Cannot find external task with id nonExistingId", e.getMessage());
    }
  }

  protected Date nowPlus(long millis) {
    return new Date(ClockUtil.getCurrentTime().getTime() + millis);
  }
}
