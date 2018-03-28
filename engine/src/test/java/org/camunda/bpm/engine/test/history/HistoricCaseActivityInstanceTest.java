/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ACTIVE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.DISABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ENABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseActivityInstanceEntity;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.variable.Variables;
import org.hamcrest.Matcher;

/**
 * @author Sebastian Menski
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricCaseActivityInstanceTest extends CmmnProcessEngineTestCase {

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/emptyStageWithManualActivationCase.cmmn"})
  public void testHistoricCaseActivityInstanceProperties() {
    String activityId = "PI_Stage_1";

    createCaseInstance();
    CaseExecution stage = queryCaseExecutionByActivityId(activityId);
    HistoricCaseActivityInstance historicStage = queryHistoricActivityCaseInstance(activityId);

    assertEquals(stage.getId(), historicStage.getId());
    assertEquals(stage.getParentId(), historicStage.getParentCaseActivityInstanceId());
    assertEquals(stage.getCaseDefinitionId(), historicStage.getCaseDefinitionId());
    assertEquals(stage.getCaseInstanceId(), historicStage.getCaseInstanceId());
    assertEquals(stage.getActivityId(), historicStage.getCaseActivityId());
    assertEquals(stage.getActivityName(), historicStage.getCaseActivityName());
    assertEquals(stage.getActivityType(), historicStage.getCaseActivityType());

    manualStart(stage.getId());

    historicStage = queryHistoricActivityCaseInstance(activityId);
    assertNotNull(historicStage.getEndTime());
  }

  @Deployment
  public void testHistoricCaseActivityTaskStates() {
    String humanTaskId1 = "PI_HumanTask_1";
    String humanTaskId2 = "PI_HumanTask_2";
    String humanTaskId3 = "PI_HumanTask_3";

    // given
    String caseInstanceId = createCaseInstance().getId();
    String taskInstanceId1 = queryCaseExecutionByActivityId(humanTaskId1).getId();
    String taskInstanceId2 = queryCaseExecutionByActivityId(humanTaskId2).getId();
    String taskInstanceId3 = queryCaseExecutionByActivityId(humanTaskId3).getId();

    // human task 1 should enabled and human task 2 and 3 will be available cause the sentry is not fulfilled
    assertHistoricState(humanTaskId1, ENABLED);
    assertHistoricState(humanTaskId2, AVAILABLE);
    assertHistoricState(humanTaskId3, AVAILABLE);
    assertStateQuery(ENABLED, AVAILABLE, AVAILABLE);

    // when human task 1 is started
    manualStart(taskInstanceId1);

    // then human task 1 is active and human task 2 and 3 are still available
    assertHistoricState(humanTaskId1, ACTIVE);
    assertHistoricState(humanTaskId2, AVAILABLE);
    assertHistoricState(humanTaskId3, AVAILABLE);
    assertStateQuery(ACTIVE, AVAILABLE, AVAILABLE);

    // when human task 1 is completed
    complete(taskInstanceId1);

    // then human task 1 is completed and human task 2 is enabled and human task 3 is active
    assertHistoricState(humanTaskId1, COMPLETED);
    assertHistoricState(humanTaskId2, ENABLED);
    assertHistoricState(humanTaskId3, ACTIVE);
    assertStateQuery(COMPLETED, ENABLED, ACTIVE);

    // disable human task 2
    disable(taskInstanceId2);
    assertHistoricState(humanTaskId1, COMPLETED);
    assertHistoricState(humanTaskId2, DISABLED);
    assertHistoricState(humanTaskId3, ACTIVE);
    assertStateQuery(COMPLETED, DISABLED, ACTIVE);

    // re-enable human task 2
    reenable(taskInstanceId2);
    assertHistoricState(humanTaskId1, COMPLETED);
    assertHistoricState(humanTaskId2, ENABLED);
    assertHistoricState(humanTaskId3, ACTIVE);
    assertStateQuery(COMPLETED, ENABLED, ACTIVE);

    // suspend human task 3
    suspend(taskInstanceId3);
    assertHistoricState(humanTaskId1, COMPLETED);
    assertHistoricState(humanTaskId2, ENABLED);
    assertHistoricState(humanTaskId3, SUSPENDED);
    assertStateQuery(COMPLETED, ENABLED, SUSPENDED);

    // resume human task 3
    resume(taskInstanceId3);
    assertHistoricState(humanTaskId1, COMPLETED);
    assertHistoricState(humanTaskId2, ENABLED);
    assertHistoricState(humanTaskId3, ACTIVE);
    assertStateQuery(COMPLETED, ENABLED, ACTIVE);

    // when the case instance is suspended
    suspend(caseInstanceId);

    // then human task 2 and 3 are suspended
    assertHistoricState(humanTaskId1, COMPLETED);
    assertHistoricState(humanTaskId2, SUSPENDED);
    assertHistoricState(humanTaskId3, SUSPENDED);
    assertStateQuery(COMPLETED, SUSPENDED, SUSPENDED);

    // when case instance is re-activated
    reactivate(caseInstanceId);

    // then human task 2 is enabled and human task is active
    assertHistoricState(humanTaskId1, COMPLETED);
    assertHistoricState(humanTaskId2, ENABLED);
    assertHistoricState(humanTaskId3, ACTIVE);
    assertStateQuery(COMPLETED, ENABLED, ACTIVE);

    manualStart(taskInstanceId2);
    // when human task 3 is terminated
    terminate(taskInstanceId3);

    // then human task 2 and 3 are terminated caused by the exitCriteria of human task 2
    assertHistoricState(humanTaskId1, COMPLETED);
    assertHistoricState(humanTaskId2, TERMINATED);
    assertHistoricState(humanTaskId3, TERMINATED);
    assertStateQuery(COMPLETED, TERMINATED, TERMINATED);
  }

  @Deployment
  public void testHistoricCaseActivityMilestoneStates() {
    String milestoneId1 = "PI_Milestone_1";
    String milestoneId2 = "PI_Milestone_2";
    String humanTaskId1 = "PI_HumanTask_1";
    String humanTaskId2 = "PI_HumanTask_2";

    // given
    String caseInstanceId = createCaseInstance().getId();
    String milestoneInstance1 = queryCaseExecutionByActivityId(milestoneId1).getId();
    String milestoneInstance2 = queryCaseExecutionByActivityId(milestoneId2).getId();
    String humanTaskInstance1 = queryCaseExecutionByActivityId(humanTaskId1).getId();

    // then milestone 1 and 2 are available and
    // humanTask 1 and 2 are enabled
    assertHistoricState(milestoneId1, AVAILABLE);
    assertHistoricState(milestoneId2, AVAILABLE);
    assertHistoricState(humanTaskId1, ENABLED);
    assertHistoricState(humanTaskId2, ENABLED);
    assertStateQuery(AVAILABLE, AVAILABLE, ENABLED, ENABLED);

    // suspend event milestone 1 and 2
    suspend(milestoneInstance1);
    suspend(milestoneInstance2);
    assertHistoricState(milestoneId1, SUSPENDED);
    assertHistoricState(milestoneId2, SUSPENDED);
    assertHistoricState(humanTaskId1, ENABLED);
    assertHistoricState(humanTaskId2, ENABLED);
    assertStateQuery(SUSPENDED, SUSPENDED, ENABLED, ENABLED);

    // resume user milestone 1
    resume(milestoneInstance1);
    assertHistoricState(milestoneId1, AVAILABLE);
    assertHistoricState(milestoneId2, SUSPENDED);
    assertHistoricState(humanTaskId1, ENABLED);
    assertHistoricState(humanTaskId2, ENABLED);
    assertStateQuery(AVAILABLE, SUSPENDED, ENABLED, ENABLED);

    // when humanTask 1 is terminated
    manualStart(humanTaskInstance1);
    terminate(humanTaskInstance1);

    // then humanTask 1 is terminated and milestone 1 is completed caused by its entryCriteria
    assertHistoricState(milestoneId1, COMPLETED);
    assertHistoricState(milestoneId2, SUSPENDED);
    assertHistoricState(humanTaskId1, TERMINATED);
    assertHistoricState(humanTaskId2, ENABLED);
    assertStateQuery(COMPLETED, SUSPENDED, TERMINATED, ENABLED);

    // when the case instance is terminated
    terminate(caseInstanceId);

    // then milestone 2 is terminated
    assertHistoricState(milestoneId1, COMPLETED);
    assertHistoricState(milestoneId2, TERMINATED);
    assertHistoricState(humanTaskId1, TERMINATED);
    assertHistoricState(humanTaskId2, TERMINATED);
    assertStateQuery(COMPLETED, TERMINATED, TERMINATED, TERMINATED);
  }

  @Deployment
  public void testHistoricCaseActivityInstanceDates() {
    String taskId1 = "PI_HumanTask_1";
    String taskId2 = "PI_HumanTask_2";
    String taskId3 = "PI_HumanTask_3";
    String milestoneId1 = "PI_Milestone_1";
    String milestoneId2 = "PI_Milestone_2";
    String milestoneId3 = "PI_Milestone_3";

    // create test dates
    long duration = 72 * 3600 * 1000;
    Date created = ClockUtil.getCurrentTime();
    Date ended = new Date(created.getTime() + duration);

    ClockUtil.setCurrentTime(created);
    String caseInstanceId = createCaseInstance().getId();
    String taskInstance1 = queryCaseExecutionByActivityId(taskId1).getId();
    String taskInstance2 = queryCaseExecutionByActivityId(taskId2).getId();
    String taskInstance3 = queryCaseExecutionByActivityId(taskId3).getId();
    String milestoneInstance1 = queryCaseExecutionByActivityId(milestoneId1).getId();
    String milestoneInstance2 = queryCaseExecutionByActivityId(milestoneId2).getId();
    String milestoneInstance3 = queryCaseExecutionByActivityId(milestoneId3).getId();

    // assert create time of all historic instances
    assertHistoricCreateTime(taskId1, created);
    assertHistoricCreateTime(taskId2, created);
    assertHistoricCreateTime(milestoneId1, created);
    assertHistoricCreateTime(milestoneId2, created);

    // complete human task 1
    ClockUtil.setCurrentTime(ended);
    complete(taskInstance1);

    // assert end time of human task 1
    assertHistoricEndTime(taskId1, ended);
    assertHistoricDuration(taskId1, duration);

    // complete milestone 1
    ClockUtil.setCurrentTime(ended);
    occur(milestoneInstance1);

    // assert end time of milestone 1
    assertHistoricEndTime(milestoneId1, ended);
    assertHistoricDuration(milestoneId1, duration);

    // terminate human task 2
    ClockUtil.setCurrentTime(ended);
    terminate(taskInstance2);

    // assert end time of human task 2
    assertHistoricEndTime(taskId2, ended);
    assertHistoricDuration(taskId2, duration);

    // terminate milestone 2
    ClockUtil.setCurrentTime(ended);
    terminate(milestoneInstance2);

    // assert end time of user event 2
    assertHistoricEndTime(milestoneId2, ended);
    assertHistoricDuration(milestoneId2, duration);

    // disable human task 3 and suspend milestone 3
    disable(taskInstance3);
    suspend(milestoneInstance3);

    // when terminate case instance
    ClockUtil.setCurrentTime(ended);
    terminate(caseInstanceId);

    // then human task 3 and milestone 3 should be terminated and a end time is set
    assertHistoricEndTime(taskId3, ended);
    assertHistoricEndTime(milestoneId3, ended);
    assertHistoricDuration(taskId3, duration);
    assertHistoricDuration(milestoneId3, duration);

    // test queries
    Date beforeCreate = new Date(created.getTime() - 3600 * 1000);
    Date afterEnd = new Date(ended.getTime() + 3600 * 1000);

    assertCount(6, historicQuery().createdAfter(beforeCreate));
    assertCount(0, historicQuery().createdAfter(ended));

    assertCount(0, historicQuery().createdBefore(beforeCreate));
    assertCount(6, historicQuery().createdBefore(ended));

    assertCount(0, historicQuery().createdBefore(beforeCreate).createdAfter(ended));

    assertCount(6, historicQuery().endedAfter(created));
    assertCount(0, historicQuery().endedAfter(afterEnd));

    assertCount(0, historicQuery().endedBefore(created));
    assertCount(6, historicQuery().endedBefore(afterEnd));

    assertCount(0, historicQuery().endedBefore(created).endedAfter(afterEnd));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn"})
  public void testHistoricCaseActivityTaskId() {
    String taskId = "PI_HumanTask_1";

    createCaseInstance();

    // as long as the human task was not started there should be no task id set
    assertCount(0, taskService.createTaskQuery());
    HistoricCaseActivityInstance historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertNull(historicInstance.getTaskId());

    // start human task manually to create task instance
    CaseExecution humanTask = queryCaseExecutionByActivityId(taskId);
    manualStart(humanTask.getId());

    // there should exist a single task
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // check that the task id was correctly set
    historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertEquals(task.getId(), historicInstance.getTaskId());

    // complete task
    taskService.complete(task.getId());

    // check that the task id is still set
    assertCount(0, taskService.createTaskQuery());
    historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertEquals(task.getId(), historicInstance.getTaskId());
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCaseWithManualActivation.cmmn",
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
  })
  public void testHistoricCaseActivityCalledProcessInstanceId() {
    String taskId = "PI_ProcessTask_1";

    createCaseInstanceByKey("oneProcessTaskCase").getId();

    // as long as the process task is not activated there should be no process instance
    assertCount(0, runtimeService.createProcessInstanceQuery());

    HistoricCaseActivityInstance historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertNull(historicInstance.getCalledProcessInstanceId());

    // start process task manually to create case instance
    CaseExecution processTask = queryCaseExecutionByActivityId(taskId);
    manualStart(processTask.getId());

    // there should exist a new process instance
    ProcessInstance calledProcessInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(calledProcessInstance);

    // check that the called process instance id was correctly set
    historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertEquals(calledProcessInstance.getId(), historicInstance.getCalledProcessInstanceId());

    // complete task and thereby the process instance
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // check that the task id is still set
    assertCount(0, runtimeService.createProcessInstanceQuery());
    historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertEquals(calledProcessInstance.getId(), historicInstance.getCalledProcessInstanceId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCaseWithManualActivation.cmmn",
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn"
  })
  public void testHistoricCaseActivityCalledCaseInstanceId() {
    String taskId = "PI_CaseTask_1";

    String calledCaseId = "oneTaskCase";
    String calledTaskId = "PI_HumanTask_1";

    createCaseInstanceByKey("oneCaseTaskCase").getId();

    // as long as the case task is not activated there should be no other case instance
    assertCount(0, caseService.createCaseInstanceQuery().caseDefinitionKey(calledCaseId));

    HistoricCaseActivityInstance historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertNull(historicInstance.getCalledCaseInstanceId());

    // start case task manually to create case instance
    CaseExecution caseTask = queryCaseExecutionByActivityId(taskId);
    manualStart(caseTask.getId());

    // there should exist a new case instance
    CaseInstance calledCaseInstance = caseService.createCaseInstanceQuery().caseDefinitionKey(calledCaseId).singleResult();
    assertNotNull(calledCaseInstance);

    // check that the called case instance id was correctly set
    historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertEquals(calledCaseInstance.getId(), historicInstance.getCalledCaseInstanceId());

    // disable task to complete called case instance and close it
    CaseExecution calledTask = queryCaseExecutionByActivityId(calledTaskId);
    disable(calledTask.getId());
    close(calledCaseInstance.getId());

    // check that the called case instance id is still set
    assertCount(0, caseService.createCaseInstanceQuery().caseDefinitionKey(calledCaseId));
    historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertEquals(calledCaseInstance.getId(), historicInstance.getCalledCaseInstanceId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskAndOneStageWithManualActivationCase.cmmn"})
  public void testHistoricCaseActivityQuery() {
    String stageId = "PI_Stage_1";
    String stageName = "A HumanTask";
    String taskId = "PI_HumanTask_1";
    String taskName = "A HumanTask";

    String caseInstanceId = createCaseInstance().getId();

    CaseExecution stageExecution = queryCaseExecutionByActivityId(stageId);
    CaseExecution taskExecution = queryCaseExecutionByActivityId(taskId);

    assertCount(1, historicQuery().caseActivityInstanceId(stageExecution.getId()));
    assertCount(1, historicQuery().caseActivityInstanceId(taskExecution.getId()));

    assertCount(2, historicQuery().caseInstanceId(caseInstanceId));
    assertCount(2, historicQuery().caseDefinitionId(stageExecution.getCaseDefinitionId()));

    assertCount(1, historicQuery().caseExecutionId(stageExecution.getId()));
    assertCount(1, historicQuery().caseExecutionId(taskExecution.getId()));

    assertCount(1, historicQuery().caseActivityId(stageId));
    assertCount(1, historicQuery().caseActivityId(taskId));

    assertCount(1, historicQuery().caseActivityName(stageName));
    assertCount(1, historicQuery().caseActivityName(taskName));

    assertCount(1, historicQuery().caseActivityType("stage"));
    assertCount(1, historicQuery().caseActivityType("humanTask"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryPaging() {
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();

    assertEquals(3, historicQuery().listPage(0, 3).size());
    assertEquals(2, historicQuery().listPage(2, 2).size());
    assertEquals(1, historicQuery().listPage(3, 2).size());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn",
    "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"
  })
  public void testQuerySorting() {
    String taskId1 = "PI_HumanTask_1";
    String taskId2 = "PI_HumanTask_2";

    String oneTaskCaseId = createCaseInstanceByKey("oneTaskCase").getId();
    String twoTaskCaseId = createCaseInstanceByKey("twoTaskCase").getId();

    CaseExecution task1 = caseService.createCaseExecutionQuery().caseInstanceId(oneTaskCaseId).activityId(taskId1).singleResult();
    CaseExecution task2 = caseService.createCaseExecutionQuery().caseInstanceId(twoTaskCaseId).activityId(taskId1).singleResult();
    CaseExecution task3 = caseService.createCaseExecutionQuery().caseInstanceId(twoTaskCaseId).activityId(taskId2).singleResult();

    // sort by historic case activity instance ids
    assertQuerySorting("id", historicQuery().orderByHistoricCaseActivityInstanceId(),
      task1.getId(), task2.getId(), task3.getId());

    // sort by case instance ids
    assertQuerySorting("caseInstanceId", historicQuery().orderByCaseInstanceId(),
      oneTaskCaseId, twoTaskCaseId, twoTaskCaseId);

    // sort by case execution ids
    assertQuerySorting("caseExecutionId", historicQuery().orderByCaseExecutionId(),
      task1.getId(), task2.getId(), task3.getId());

    // sort by case activity ids
    assertQuerySorting("caseActivityId", historicQuery().orderByCaseActivityId(),
      taskId1, taskId1, taskId2);

    // sort by case activity names
    assertQuerySorting("caseActivityName", historicQuery().orderByCaseActivityName(),
      "A HumanTask", "A HumanTask", "Another HumanTask");

    // sort by case definition ids
    assertQuerySorting("caseDefinitionId", historicQuery().orderByCaseDefinitionId(),
      task1.getCaseDefinitionId(), task2.getCaseDefinitionId(), task3.getCaseDefinitionId());

    // manually start tasks to be able to complete them
    manualStart(task2.getId());
    manualStart(task3.getId());

    // complete tasks to set end time and duration
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }

    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();
    HistoricCaseActivityInstance historicTask1 = query.caseInstanceId(oneTaskCaseId).caseActivityId(taskId1).singleResult();
    HistoricCaseActivityInstance historicTask2 = query.caseInstanceId(twoTaskCaseId).caseActivityId(taskId1).singleResult();
    HistoricCaseActivityInstance historicTask3 = query.caseInstanceId(twoTaskCaseId).caseActivityId(taskId2).singleResult();

    // sort by create times
    assertQuerySorting("createTime", historicQuery().orderByHistoricCaseActivityInstanceCreateTime(),
      historicTask1.getCreateTime(), historicTask2.getCreateTime(), historicTask3.getCreateTime());

    // sort by end times
    assertQuerySorting("endTime", historicQuery().orderByHistoricCaseActivityInstanceEndTime(),
      historicTask1.getEndTime(), historicTask2.getEndTime(), historicTask3.getEndTime());

    // sort by durations times
    assertQuerySorting("durationInMillis", historicQuery().orderByHistoricCaseActivityInstanceDuration(),
      historicTask1.getDurationInMillis(), historicTask2.getDurationInMillis(), historicTask3.getDurationInMillis());
  }

  @Deployment
  public void testQuerySortingCaseActivityType() {
    createCaseInstance().getId();

    // sort by case activity type
    assertQuerySorting("caseActivityType", historicQuery().orderByCaseActivityType(),
      "milestone", "processTask", "humanTask");
  }

  public void testInvalidSorting() {
    try {
      historicQuery().asc();
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }

    try {
      historicQuery().desc();
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }

    try {
      historicQuery().orderByHistoricCaseActivityInstanceId().count();
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testNativeQuery() {
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();

    String instanceId = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").list().get(0).getId();

    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    String tableName = managementService.getTableName(HistoricCaseActivityInstance.class);

    assertEquals(tablePrefix + "ACT_HI_CASEACTINST", tableName);
    assertEquals(tableName, managementService.getTableName(HistoricCaseActivityInstanceEntity.class));

    assertEquals(4, historyService.createNativeHistoricCaseActivityInstanceQuery().sql("SELECT * FROM " + tableName).list().size());
    assertEquals(4, historyService.createNativeHistoricCaseActivityInstanceQuery().sql("SELECT count(*) FROM " + tableName).count());

    assertEquals(16, historyService.createNativeHistoricCaseActivityInstanceQuery().sql("SELECT count(*) FROM " + tableName + " H1, " + tableName + " H2").count());

    // select with distinct
    assertEquals(4, historyService.createNativeHistoricCaseActivityInstanceQuery().sql("SELECT DISTINCT * FROM " + tableName).list().size());

    assertEquals(1, historyService.createNativeHistoricCaseActivityInstanceQuery().sql("SELECT count(*) FROM " + tableName + " H WHERE H.ID_ = '" + instanceId + "'").count());
    assertEquals(1, historyService.createNativeHistoricCaseActivityInstanceQuery().sql("SELECT * FROM " + tableName + " H WHERE H.ID_ = '" + instanceId + "'").list().size());

    // use parameters
    assertEquals(1, historyService.createNativeHistoricCaseActivityInstanceQuery().sql("SELECT count(*) FROM " + tableName + " H WHERE H.ID_ = #{caseActivityInstanceId}").parameter("caseActivityInstanceId", instanceId).count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testNativeQueryPaging() {
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();
    createCaseInstance();

    String tableName = managementService.getTableName(HistoricCaseActivityInstance.class);
    assertEquals(3, historyService.createNativeHistoricCaseActivityInstanceQuery().sql("SELECT * FROM " + tableName).listPage(0, 3).size());
    assertEquals(2, historyService.createNativeHistoricCaseActivityInstanceQuery().sql("SELECT * FROM " + tableName).listPage(2, 2).size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn"})
  public void testDeleteHistoricCaseActivityInstance() {
    CaseInstance caseInstance = createCaseInstance();

    HistoricCaseActivityInstance historicInstance = historicQuery().singleResult();
    assertNotNull(historicInstance);

    // disable human task to complete case
    disable(historicInstance.getId());
    // close case to be able to delete historic case instance
    close(caseInstance.getId());
    // delete historic case instance
    historyService.deleteHistoricCaseInstance(caseInstance.getId());

    assertCount(0, historicQuery());
  }

  @Deployment
  public void testNonBlockingHumanTask() {
    CaseInstance caseInstance = createCaseInstance();
    assertNotNull(caseInstance);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/required/RequiredRuleTest.testVariableBasedRule.cmmn")
  public void testRequiredRuleEvaluatesToTrue() {
    caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("required", true));

    HistoricCaseActivityInstance task = historyService
        .createHistoricCaseActivityInstanceQuery()
        .caseActivityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(task);
    assertTrue(task.isRequired());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/required/RequiredRuleTest.testVariableBasedRule.cmmn")
  public void testRequiredRuleEvaluatesToFalse() {
    caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("required", false));

    HistoricCaseActivityInstance task = historyService
        .createHistoricCaseActivityInstanceQuery()
        .caseActivityId("PI_HumanTask_1")
        .singleResult();

    assertNotNull(task);
    assertFalse(task.isRequired());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/required/RequiredRuleTest.testVariableBasedRule.cmmn")
  public void testQueryByRequired() {
    caseService.createCaseInstanceByKey("case", Collections.<String, Object>singletonMap("required", true));

    HistoricCaseActivityInstanceQuery query = historyService
        .createHistoricCaseActivityInstanceQuery()
        .required();

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());

    HistoricCaseActivityInstance activityInstance = query.singleResult();
    assertNotNull(activityInstance);
    assertTrue(activityInstance.isRequired());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/stage/AutoCompleteTest.testCasePlanModel.cmmn"})
  public void testAutoCompleteEnabled() {
    String caseInstanceId = createCaseInstanceByKey("case").getId();

    HistoricCaseInstance caseInstance = historyService
        .createHistoricCaseInstanceQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());

    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();

    HistoricCaseActivityInstance humanTask1 = query.caseActivityId("PI_HumanTask_1").singleResult();
    assertNotNull(humanTask1);
    assertTrue(humanTask1.isTerminated());
    assertNotNull(humanTask1.getEndTime());
    assertNotNull(humanTask1.getDurationInMillis());


    HistoricCaseActivityInstance humanTask2 = query.caseActivityId("PI_HumanTask_2").singleResult();
    assertNotNull(humanTask2);
    assertTrue(humanTask2.isTerminated());
    assertNotNull(humanTask2.getEndTime());
    assertNotNull(humanTask2.getDurationInMillis());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testRepeatTask.cmmn"})
  public void testRepeatTask() {
    // given
    createCaseInstance();
    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(firstHumanTaskId);

    // then
    HistoricCaseActivityInstanceQuery query = historicQuery().caseActivityId("PI_HumanTask_2");
    assertEquals(2, query.count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testRepeatStage.cmmn"})
  public void testRepeatStage() {
    // given
    createCaseInstance();

    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(firstHumanTaskId);

    // then
    HistoricCaseActivityInstanceQuery query = historicQuery().caseActivityId("PI_Stage_1");
    assertEquals(2, query.count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testRepeatMilestone.cmmn"})
  public void testRepeatMilestone() {
    // given
    createCaseInstance();
    String firstHumanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(firstHumanTaskId);

    // then
    HistoricCaseActivityInstanceQuery query = historicQuery().caseActivityId("PI_Milestone_1");
    assertEquals(2, query.count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testAutoCompleteStage.cmmn"})
  public void testAutoCompleteStage() {
    // given
    createCaseInstance();
    String humanTask1 = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    complete(humanTask1);

    // then
    HistoricCaseActivityInstanceQuery query = historicQuery().caseActivityId("PI_Stage_1");
    assertEquals(1, query.count());

    query = historicQuery().caseActivityId("PI_HumanTask_1");
    assertEquals(1, query.count());

    query = historicQuery().caseActivityId("PI_HumanTask_2");
    assertEquals(2, query.count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/repetition/RepetitionRuleTest.testAutoCompleteStageWithoutEntryCriteria.cmmn"})
  public void testAutoCompleteStageWithRepeatableTaskWithoutEntryCriteria() {
    // given
    createCaseInstanceByKey("case", Variables.createVariables().putValue("manualActivation", false));
    queryCaseExecutionByActivityId("PI_Stage_1");

    // when
    String humanTask = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();
    complete(humanTask);

    // then
    HistoricCaseActivityInstanceQuery query = historicQuery().caseActivityId("PI_HumanTask_1");
    assertEquals(2, query.count());

    query = historicQuery().caseActivityId("PI_Stage_1");
    assertEquals(1, query.count());

  }

  @Deployment
  public void testDecisionTask() {
    createCaseInstance();

    HistoricCaseActivityInstance decisionTask = historyService
        .createHistoricCaseActivityInstanceQuery()
        .caseActivityId("PI_DecisionTask_1")
        .singleResult();

    assertNotNull(decisionTask);
    assertEquals("decisionTask", decisionTask.getCaseActivityType());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseInstanceId() {
    // given
    createCaseInstance();

    String taskInstanceId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    // when
    HistoricCaseActivityInstanceQuery query = historicQuery().caseActivityInstanceIdIn(taskInstanceId);

    // then
    assertCount(1, query);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testQueryByCaseInstanceIds() {
    // given
    CaseInstance instance1 = createCaseInstance();
    CaseInstance instance2 = createCaseInstance();

    String taskInstanceId1 = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(instance1.getId())
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String taskInstanceId2 = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(instance2.getId())
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    HistoricCaseActivityInstanceQuery query = historicQuery()
        .caseActivityInstanceIdIn(taskInstanceId1, taskInstanceId2);

    // then
    assertCount(2, query);
  }

  public void testQueryByInvalidCaseInstanceId() {

    // when
    HistoricCaseActivityInstanceQuery query = historicQuery().caseActivityInstanceIdIn("invalid");

    // then
    assertCount(0, query);

    try {
      historicQuery().caseActivityInstanceIdIn((String[])null);
      fail("A NotValidException was expected.");
    } catch (NotValidException e) {}

    try {
      historicQuery().caseActivityInstanceIdIn((String)null);
      fail("A NotValidException was expected.");
    } catch (NotValidException e) {}
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"
  })
  public void testQueryByCaseActivityIds() {
    // given
    createCaseInstanceByKey("oneTaskCase");
    createCaseInstanceByKey("twoTaskCase");

    // when
    HistoricCaseActivityInstanceQuery query = historicQuery()
        .caseActivityIdIn("PI_HumanTask_1", "PI_HumanTask_2");

    // then
    assertCount(3, query);
  }

  public void testQueryByInvalidCaseActivityId() {

    // when
    HistoricCaseActivityInstanceQuery query = historicQuery().caseActivityIdIn("invalid");

    // then
    assertCount(0, query);

    try {
      historicQuery().caseActivityIdIn((String[])null);
      fail("A NotValidException was expected.");
    } catch (NotValidException e) {}

    try {
      historicQuery().caseActivityIdIn((String)null);
      fail("A NotValidException was expected.");
    } catch (NotValidException e) {}
  }

  protected HistoricCaseActivityInstanceQuery historicQuery() {
    return historyService.createHistoricCaseActivityInstanceQuery();
  }

  protected HistoricCaseActivityInstance queryHistoricActivityCaseInstance(String activityId) {
    HistoricCaseActivityInstance historicActivityInstance = historicQuery()
      .caseActivityId(activityId)
      .singleResult();
    assertNotNull("No historic activity instance found for activity id: " + activityId, historicActivityInstance);
    return historicActivityInstance;
  }

  protected void assertHistoricState(String activityId, CaseExecutionState expectedState) {
    HistoricCaseActivityInstanceEventEntity historicActivityInstance = (HistoricCaseActivityInstanceEventEntity) queryHistoricActivityCaseInstance(activityId);
    int actualStateCode = historicActivityInstance.getCaseActivityInstanceState();
    CaseExecutionState actualState = CaseExecutionState.CaseExecutionStateImpl.getStateForCode(actualStateCode);
    assertEquals("The state of historic case activity '" + activityId + "' wasn't as expected", expectedState, actualState);
  }

  protected void assertHistoricCreateTime(String activityId, Date expectedCreateTime) {
    HistoricCaseActivityInstance historicActivityInstance = queryHistoricActivityCaseInstance(activityId);
    Date actualCreateTime = historicActivityInstance.getCreateTime();
    assertSimilarDate(expectedCreateTime, actualCreateTime);
  }

  protected void assertHistoricEndTime(String activityId, Date expectedEndTime) {
    HistoricCaseActivityInstance historicActivityInstance = queryHistoricActivityCaseInstance(activityId);
    Date actualEndTime = historicActivityInstance.getEndTime();
    assertSimilarDate(expectedEndTime, actualEndTime);
  }

  protected void assertSimilarDate(Date expectedDate, Date actualDate) {
    long difference = Math.abs(expectedDate.getTime() - actualDate.getTime());
    // assert that the dates don't differ more than a second
    assertTrue(difference < 1000);
  }

  protected void assertHistoricDuration(String activityId, long expectedDuration) {
    Long actualDuration = queryHistoricActivityCaseInstance(activityId).getDurationInMillis();
    assertNotNull(actualDuration);
    // test that duration is as expected with a maximal difference of one second
    assertTrue(actualDuration >= expectedDuration);
    assertTrue(actualDuration < expectedDuration + 1000);
  }

  protected void assertCount(long count, Query<?, ?> historicQuery) {
    assertEquals(count, historicQuery.count());
  }

  protected void assertStateQuery(CaseExecutionState... states) {
    CaseExecutionStateCountMap stateCounts = new CaseExecutionStateCountMap();

    if (states != null) {
      for (CaseExecutionState state : states) {
        stateCounts.put(state, stateCounts.get(state) + 1);
      }
    }

    assertCount(stateCounts.count(), historicQuery());
    assertCount(stateCounts.unfinished(), historicQuery().notEnded());
    assertCount(stateCounts.finished(), historicQuery().ended());

    assertCount(stateCounts.get(ACTIVE), historicQuery().active());
    assertCount(stateCounts.get(AVAILABLE), historicQuery().available());
    assertCount(stateCounts.get(COMPLETED), historicQuery().completed());
    assertCount(stateCounts.get(DISABLED), historicQuery().disabled());
    assertCount(stateCounts.get(ENABLED), historicQuery().enabled());
    assertCount(stateCounts.get(TERMINATED), historicQuery().terminated());
  }

  protected class CaseExecutionStateCountMap extends HashMap<CaseExecutionState, Long> {

    private static final long serialVersionUID = 1L;

    public final Collection<CaseExecutionState> ALL_STATES = CaseExecutionState.CASE_EXECUTION_STATES.values();
    public final Collection<CaseExecutionState> ENDED_STATES = Arrays.asList(COMPLETED, TERMINATED);
    public final Collection<CaseExecutionState> NOT_ENDED_STATES;

    public CaseExecutionStateCountMap() {
      NOT_ENDED_STATES = new ArrayList<CaseExecutionState>(ALL_STATES);
      NOT_ENDED_STATES.removeAll(ENDED_STATES);
    }

    public Long get(CaseExecutionState state) {
      return state != null && containsKey(state) ? super.get(state) : 0;
    }

    public Long count() {
      return count(ALL_STATES);
    }

    public Long finished() {
      return count(ENDED_STATES);
    }

    public Long unfinished() {
      return count(NOT_ENDED_STATES);
    }

    public Long count(Collection<CaseExecutionState> states) {
      long count = 0;
      for (CaseExecutionState state : states) {
        count += get(state);
      }
      return count;
    }

  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void assertQuerySorting(String property, Query<?, ?> query, Comparable... items) {
    AbstractQuery<?, ?> queryImpl = (AbstractQuery<?, ?>) query;

    // save order properties to later reverse ordering
    List<QueryOrderingProperty> orderProperties = queryImpl.getOrderingProperties();

    List<? extends Comparable> sortedList = Arrays.asList(items);
    Collections.sort(sortedList);

    List<Matcher<Object>> matchers = new ArrayList<Matcher<Object>>();
    for (Comparable comparable : sortedList) {
      matchers.add(hasProperty(property, equalTo(comparable)));
    }

    List<?> instances = query.asc().list();
    assertEquals(sortedList.size(), instances.size());
    assertThat(instances, contains(matchers.toArray(new Matcher[matchers.size()])));

    // reverse ordering
    for (QueryOrderingProperty orderingProperty : orderProperties) {
      orderingProperty.setDirection(Direction.DESCENDING);
    }

    // reverse matchers
    Collections.reverse(matchers);

    instances = query.list();
    assertEquals(sortedList.size(), instances.size());
    assertThat(instances, contains(matchers.toArray(new Matcher[matchers.size()])));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricCaseActivityInstanceTest.oneStageAndOneTaskCaseWithManualActivation.cmmn"})
  public void testHistoricActivityInstanceWithinStageIsMarkedTerminatedOnComplete() {

    // given
    createCaseInstance();

    String stageExecutionId = queryCaseExecutionByActivityId("PI_Stage_1").getId();
    manualStart(stageExecutionId);
    String activeStageTaskExecutionId = queryCaseExecutionByActivityId("PI_HumanTask_Stage_2").getId();
    complete(activeStageTaskExecutionId);
    CaseExecution enabledStageTaskExecutionId = queryCaseExecutionByActivityId("PI_HumanTask_Stage_1");
    assertTrue(enabledStageTaskExecutionId.isEnabled());

    // when
    complete(stageExecutionId);

    // then the remaining stage task that was enabled is set to terminated in history
    HistoricCaseActivityInstance manualActivationTask =
        historyService.createHistoricCaseActivityInstanceQuery().caseActivityId("PI_HumanTask_Stage_1").singleResult();
    HistoricCaseActivityInstance completedTask =
        historyService.createHistoricCaseActivityInstanceQuery().caseActivityId("PI_HumanTask_Stage_2").singleResult();

    assertTrue(manualActivationTask.isTerminated());
    assertTrue(completedTask.isCompleted());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricCaseActivityInstanceTest.oneStageAndOneTaskCaseWithManualActivation.cmmn"})
  public void testHistoricActivityInstancesAreMarkedTerminatedOnComplete() {

    // given
    createCaseInstance();

    CaseExecution humanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    assertTrue(humanTask.isEnabled());
    CaseExecution stage = queryCaseExecutionByActivityId("PI_Stage_1");
    assertTrue(stage.isEnabled());

    // when
    CaseExecution casePlanExecution = queryCaseExecutionByActivityId("CasePlanModel_1");
    complete(casePlanExecution.getId());

    // then make sure all cases in the lower scope are marked as terminated in history
    HistoricCaseActivityInstance stageInstance =
        historyService.createHistoricCaseActivityInstanceQuery().caseActivityId("PI_Stage_1").singleResult();
    HistoricCaseActivityInstance taskInstance =
        historyService.createHistoricCaseActivityInstanceQuery().caseActivityId("PI_HumanTask_3").singleResult();

    assertTrue(stageInstance.isTerminated());
    assertTrue(taskInstance.isTerminated());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricCaseActivityInstanceTest.oneStageAndOneTaskCaseWithManualActivation.cmmn"})
  public void testDisabledHistoricActivityInstancesStayDisabledOnComplete() {

    // given
    createCaseInstance();

    CaseExecution humanTask = queryCaseExecutionByActivityId("PI_HumanTask_3");
    assertTrue(humanTask.isEnabled());
    CaseExecution stageExecution = queryCaseExecutionByActivityId("PI_Stage_1");
    disable(stageExecution.getId());
    stageExecution = queryCaseExecutionByActivityId("PI_Stage_1");
    assertTrue(stageExecution.isDisabled());

    // when
    CaseExecution casePlanExecution = queryCaseExecutionByActivityId("CasePlanModel_1");
    complete(casePlanExecution.getId());

    // then make sure disabled executions stay disabled
    HistoricCaseActivityInstance stageInstance =
        historyService.createHistoricCaseActivityInstanceQuery().caseActivityId("PI_Stage_1").singleResult();
    HistoricCaseActivityInstance taskInstance =
        historyService.createHistoricCaseActivityInstanceQuery().caseActivityId("PI_HumanTask_3").singleResult();

    assertTrue(stageInstance.isDisabled());
    assertTrue(taskInstance.isTerminated());
  }

  @Deployment
  public void testMilestoneHistoricActivityInstanceIsTerminatedOnComplete() {

    // given
    createCaseInstance();
    final String milestoneId = "PI_Milestone_1";
    CaseExecution caseMilestone = queryCaseExecutionByActivityId(milestoneId);
    assertTrue(caseMilestone.isAvailable());

    // when
    CaseExecution casePlanExecution = queryCaseExecutionByActivityId("CasePlanModel_1");
    complete(casePlanExecution.getId());

    // then make sure that the milestone is terminated
    HistoricCaseActivityInstance milestoneInstance =
        historyService.createHistoricCaseActivityInstanceQuery().caseActivityId(milestoneId).singleResult();

    assertTrue(milestoneInstance.isTerminated());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricCaseActivityInstanceTest.oneStageWithSentryAsEntryPointCase.cmmn"})
  public void testHistoricTaskWithSentryIsMarkedTerminatedOnComplete() {

    // given
    createCaseInstance();

    // when
    CaseExecution casePlanExecution = queryCaseExecutionByActivityId("PI_Stage_1");
    complete(casePlanExecution.getId());

    // then both tasks are terminated
    HistoricCaseActivityInstance taskInstance =
        historyService.createHistoricCaseActivityInstanceQuery().caseActivityId("PI_HumanTask_1").singleResult();

    HistoricCaseActivityInstance taskInstance2 =
        historyService.createHistoricCaseActivityInstanceQuery().caseActivityId("PI_HumanTask_2").singleResult();

    assertTrue(taskInstance.isTerminated());
    assertTrue(taskInstance2.isTerminated());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricCaseActivityInstanceTest.oneStageWithSentryAsEntryPointCase.cmmn"})
  public void testHistoricTaskWithSentryDoesNotReachStateActiveOnComplete() {

    // given
    createCaseInstance();

    // when
    CaseExecution casePlanExecution = queryCaseExecutionByActivityId("PI_Stage_1");
    complete(casePlanExecution.getId());

    // then task 2 was never in state 'active'
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseExecutionIdIn(casePlanExecution.getId());

    assertEquals(0, query.count());
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCaseWithManualActivation.cmmn",
    "org/camunda/bpm/engine/test/history/HistoricCaseActivityInstanceTest.oneTaskProcess.bpmn20.xml"
  })
  public void testHistoricCalledProcessInstanceId() {
    String taskId = "PI_ProcessTask_1";

    createCaseInstanceByKey("oneProcessTaskCase").getId();

    // as long as the process task is not activated there should be no process instance
    assertCount(0, historyService.createHistoricProcessInstanceQuery());

    HistoricCaseActivityInstance historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertNull(historicInstance.getCalledProcessInstanceId());

    // start process task manually to create case instance
    CaseExecution processTask = queryCaseExecutionByActivityId(taskId);
    manualStart(processTask.getId());

    // there should exist a new process instance
    HistoricProcessInstance calledProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertNotNull(calledProcessInstance);
    assertNotNull(calledProcessInstance.getEndTime());

    // check that the called process instance id was correctly set
    historicInstance = queryHistoricActivityCaseInstance(taskId);
    assertEquals(calledProcessInstance.getId(), historicInstance.getCalledProcessInstanceId());
  }

}
