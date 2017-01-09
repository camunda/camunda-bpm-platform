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
package org.camunda.bpm.engine.test.history;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricExternalTaskLogQueryTest {

  protected final String WORKER_ID = "aWorkerId";
  protected final long LOCK_DURATION = 5 * 60L * 1000L;

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

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
  public void testQuery() {

    // given
    ExternalTask task = startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(task.getId()));
  }

  @Test
  public void testQueryById() {
    // given
    startExternalTaskProcesses(2);
    String logId = retrieveFirstHistoricExternalTaskLog().getId();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .logId(logId)
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getId(), is(logId));
  }

  @Test
  public void testQueryFailsByInvalidId() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .logId(null)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingId() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .logId("foo")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByExternalTaskId() {
    // given
    startExternalTaskProcesses(2);
    String logExternalTaskId = retrieveFirstHistoricExternalTaskLog().getExternalTaskId();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .externalTaskId(logExternalTaskId)
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(logExternalTaskId));
  }

  @Test
  public void testQueryFailsByInvalidExternalTaskId() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .externalTaskId(null)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingExternalTaskId() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .externalTaskId("foo")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByTopicName() {

    // given
    String dummyTopic = "dummy";
    startExternalTaskProcessGivenTopicName(dummyTopic);
    ExternalTask task = startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .topicName(DEFAULT_TOPIC)
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(task.getId()));
  }

  @Test
  public void testQueryFailsByInvalidTopicName() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .topicName(null)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingTopicName() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .topicName("foo bar")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByWorkerId() {
    // given
    List<ExternalTask> taskList = startExternalTaskProcesses(2);
    ExternalTask taskToCheck = taskList.get(1);
    completeExternalTaskWithWorker(taskList.get(0).getId(), "dummyWorker");
    completeExternalTaskWithWorker(taskToCheck.getId(), WORKER_ID);

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .workerId(WORKER_ID)
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(taskToCheck.getId()));
  }

  @Test
  public void testQueryFailsByInvalidWorkerId() {

    // given
    ExternalTask task = startExternalTaskProcess();
    completeExternalTask(task.getId());

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .workerId(null)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingWorkerId() {

    // given
    ExternalTask task = startExternalTaskProcess();
    completeExternalTask(task.getId());

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .workerId("foo")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByErrorMessage() {
    // given
    List<ExternalTask> taskList = startExternalTaskProcesses(2);
    String errorMessage = "This is an important error!";
    reportExternalTaskFailure(taskList.get(0).getId(), "Dummy error message");
    reportExternalTaskFailure(taskList.get(1).getId(), errorMessage);

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .errorMessage(errorMessage)
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(taskList.get(1).getId()));
  }

  @Test
  public void testQueryFailsByInvalidErrorMessage() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .errorMessage(null)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingErrorMessage() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .errorMessage("asdfasdf")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByActivityId() {
    // given
    startExternalTaskProcessGivenActivityId("dummyName");
    ExternalTask task = startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .activityIdIn(DEFAULT_EXTERNAL_TASK_NAME)
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(task.getId()));
  }

  @Test
  public void testQueryFailsByActivityIdsIsNull() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .activityIdIn((String[]) null)
      .singleResult();
  }

  @Test
  public void testQueryFailsByActivityIdsContainNull() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    String[] activityIdsContainNull = {"a", null, "b"};
    historyService
      .createHistoricExternalTaskLogQuery()
      .activityIdIn(activityIdsContainNull)
      .singleResult();
  }

  @Test
  public void testQueryFailsByActivityIdsContainEmptyString() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    String[] activityIdsContainEmptyString = {"a", "", "b"};
    historyService
      .createHistoricExternalTaskLogQuery()
      .activityIdIn(activityIdsContainEmptyString)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingActivityIds() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .activityIdIn("foo")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByActivityInstanceIds() {
    // given
    startExternalTaskProcessGivenActivityId("dummyName");
    ExternalTask task = startExternalTaskProcess();
    String activityInstanceId = historyService.createHistoricActivityInstanceQuery()
      .activityId(DEFAULT_EXTERNAL_TASK_NAME)
      .singleResult()
      .getId();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .activityInstanceIdIn(activityInstanceId)
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(task.getId()));
  }

  @Test
  public void testQueryFailsByActivityInstanceIdsIsNull() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .activityInstanceIdIn((String[]) null)
      .singleResult();

  }

  @Test
  public void testQueryFailsByActivityInstanceIdsContainNull() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    String[] activityIdsContainNull = {"a", null, "b"};
    historyService
      .createHistoricExternalTaskLogQuery()
      .activityInstanceIdIn(activityIdsContainNull)
      .singleResult();

  }

  @Test
  public void testQueryFailsByActivityInstanceIdsContainEmptyString() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    String[] activityIdsContainEmptyString = {"a", "", "b"};
    historyService
      .createHistoricExternalTaskLogQuery()
      .activityInstanceIdIn(activityIdsContainEmptyString)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingActivityInstanceIds() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .activityInstanceIdIn("foo")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByExecutionIds() {
    // given
    startExternalTaskProcesses(2);
    HistoricExternalTaskLog taskLog = retrieveFirstHistoricExternalTaskLog();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .executionIdIn(taskLog.getExecutionId())
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getId(), is(taskLog.getId()));
  }

  @Test
  public void testQueryFailsByExecutionIdsIsNull() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .executionIdIn((String[]) null)
      .singleResult();

  }

  @Test
  public void testQueryFailsByExecutionIdsContainNull() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    String[] activityIdsContainNull = {"a", null, "b"};
    historyService
      .createHistoricExternalTaskLogQuery()
      .executionIdIn(activityIdsContainNull)
      .singleResult();

  }

  @Test
  public void testQueryFailsByExecutionIdsContainEmptyString() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    String[] activityIdsContainEmptyString = {"a", "", "b"};
    historyService
      .createHistoricExternalTaskLogQuery()
      .executionIdIn(activityIdsContainEmptyString)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingExecutionIds() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .executionIdIn("foo")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByProcessInstanceId() {
    // given
    startExternalTaskProcesses(2);
    String processInstanceId = retrieveFirstHistoricExternalTaskLog().getProcessInstanceId();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getProcessInstanceId(), is(processInstanceId));
  }

  @Test
  public void testQueryFailsByInvalidProcessInstanceId() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .processInstanceId(null)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingProcessInstanceId() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .processInstanceId("foo")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByProcessDefinitionId() {
    // given
    startExternalTaskProcesses(2);
    String definitionId = retrieveFirstHistoricExternalTaskLog().getProcessDefinitionId();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .processDefinitionId(definitionId)
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getProcessDefinitionId(), is(definitionId));
  }

  @Test
  public void testQueryFailsByInvalidProcessDefinitionId() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .processDefinitionId(null)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingProcessDefinitionId() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .processDefinitionId("foo")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByProcessDefinitionKey() {
    // given
    startExternalTaskProcessGivenProcessDefinitionKey("dummyProcess");
    ExternalTask task = startExternalTaskProcessGivenProcessDefinitionKey("Process");

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .processDefinitionKey(task.getProcessDefinitionKey())
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(task.getId()));
  }

  @Test
  public void testQueryFailsByInvalidProcessDefinitionKey() {

    // given
    startExternalTaskProcess();

    // then
    thrown.expect(NotValidException.class);

    // when
    historyService
      .createHistoricExternalTaskLogQuery()
      .processDefinitionKey(null)
      .singleResult();

  }

  @Test
  public void testQueryByNonExistingProcessDefinitionKey() {

    // given
    startExternalTaskProcess();

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .processDefinitionKey("foo")
      .singleResult();

    // then
    assertNull(log);
  }

  @Test
  public void testQueryByCreationLog() {
    // given
    ExternalTask task = startExternalTaskProcess();
    completeExternalTask(task.getId());

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .creationLog()
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(task.getId()));
  }

  @Test
  public void testQueryByFailureLog() {
    // given
    ExternalTask task = startExternalTaskProcess();
    reportExternalTaskFailure(task.getId(), "Dummy error message!");

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(task.getId()));
  }

  @Test
  public void testQueryBySuccessLog() {
    // given
    ExternalTask task = startExternalTaskProcess();
    completeExternalTask(task.getId());

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .successLog()
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(task.getId()));
  }

  @Test
  public void testQueryByDeletionLog() {
    // given
    ExternalTask task = startExternalTaskProcess();
    runtimeService.deleteProcessInstance(task.getProcessInstanceId(), null);

    // when
    HistoricExternalTaskLog log = historyService
      .createHistoricExternalTaskLogQuery()
      .deletionLog()
      .singleResult();

    // then
    assertNotNull(log);
    assertThat(log.getExternalTaskId(), is(task.getId()));
  }

  @Test
  public void testQueryByLowerThanOrEqualAPriority() {

    // given
    startExternalTaskProcesses(5);

    // when
    List<HistoricExternalTaskLog> externalTaskLogs = historyService
      .createHistoricExternalTaskLogQuery()
      .priorityLowerThanOrEquals(2L)
      .list();

    // then
    assertThat(externalTaskLogs.size(), is(3));
    for (HistoricExternalTaskLog log : externalTaskLogs) {
      assertTrue(log.getPriority() <= 2);
    }

  }

  @Test
  public void testQueryByHigherThanOrEqualAPriority() {

    // given
    startExternalTaskProcesses(5);

    // when
    List<HistoricExternalTaskLog> externalTaskLogs = historyService
      .createHistoricExternalTaskLogQuery()
      .priorityHigherThanOrEquals(2L)
      .list();

    // then
    assertThat(externalTaskLogs.size(), is(3));
    for (HistoricExternalTaskLog log : externalTaskLogs) {
      assertTrue(log.getPriority() >= 2);
    }

  }

  @Test
  public void testQueryByPriorityRange() {

    // given
    startExternalTaskProcesses(5);

    // when
    List<HistoricExternalTaskLog> externalTaskLogs = historyService
      .createHistoricExternalTaskLogQuery()
      .priorityLowerThanOrEquals(3L)
      .priorityHigherThanOrEquals(1L)
      .list();

    // then
    assertThat(externalTaskLogs.size(), is(3));
    for (HistoricExternalTaskLog log : externalTaskLogs) {
      assertTrue(log.getPriority() <= 3 && log.getPriority() >= 1);
    }

  }

  @Test
  public void testQueryByDisjunctivePriorityStatements() {

    // given
    startExternalTaskProcesses(5);

    // when
    List<HistoricExternalTaskLog> externalTaskLogs = historyService
      .createHistoricExternalTaskLogQuery()
      .priorityLowerThanOrEquals(1L)
      .priorityHigherThanOrEquals(4L)
      .list();

    // then
    assertThat(externalTaskLogs.size(), is(0));
  }


  // helper methods

  protected HistoricExternalTaskLog retrieveFirstHistoricExternalTaskLog() {
    return historyService
      .createHistoricExternalTaskLogQuery()
      .list()
      .get(0);
  }

  protected void completeExternalTaskWithWorker(String externalTaskId, String workerId) {
    completeExternalTask(externalTaskId, DEFAULT_TOPIC, workerId, false);

  }

  protected void completeExternalTask(String externalTaskId) {
    completeExternalTask(externalTaskId, DEFAULT_TOPIC, WORKER_ID, false);
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

  protected void reportExternalTaskFailure(String externalTaskId, String errorMessage) {
    reportExternalTaskFailure(externalTaskId, DEFAULT_TOPIC, WORKER_ID, 1, false, errorMessage);
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

  protected List<ExternalTask> startExternalTaskProcesses(int count) {
    List<ExternalTask> list = new LinkedList<ExternalTask>();
    for (int ithPrio = 0; ithPrio < count; ithPrio++) {
      list.add(startExternalTaskProcessGivenPriority(ithPrio));
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

  protected ExternalTask startExternalTaskProcess() {
    BpmnModelInstance oneExternalTaskProcess = createDefaultExternalTaskModel().build();
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(oneExternalTaskProcess);
    ProcessInstance pi = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
    return externalTaskService.createExternalTaskQuery().processInstanceId(pi.getId()).singleResult();
  }

}
