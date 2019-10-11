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
package org.camunda.bpm.engine.test.bpmn.tasklistener;

import static org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.RecorderTaskListener;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.RecorderTaskListener.RecordedTaskEvent;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.TaskDeleteListener;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;


/**
 * @author Joram Barrez
 */
public class TaskListenerTest {

  public static final String ERROR_CODE = "208";
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Before
  public void resetListeners() {
    ThrowBPMNErrorListener.reset();
    DeleteListener.reset();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskCreateListener() {
    runtimeService.startProcessInstanceByKey("taskListenerProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Schedule meeting", task.getName());
    assertEquals("TaskCreateListener is listening!", task.getDescription());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskCompleteListener() {
    TaskDeleteListener.clear();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "greeting"));
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "expressionValue"));

    // Completing first task will change the description
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // Check that the completion did not execute the delete listener
    assertEquals(0, TaskDeleteListener.eventCounter);
    assertNull(TaskDeleteListener.lastTaskDefinitionKey);
    assertNull(TaskDeleteListener.lastDeleteReason);

    assertEquals("Hello from The Process", runtimeService.getVariable(processInstance.getId(), "greeting"));
    assertEquals("Act", runtimeService.getVariable(processInstance.getId(), "shortName"));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskDeleteListenerByProcessDeletion() {
    TaskDeleteListener.clear();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");

    assertEquals(0, TaskDeleteListener.eventCounter);
    assertNull(TaskDeleteListener.lastTaskDefinitionKey);
    assertNull(TaskDeleteListener.lastDeleteReason);

    // delete process instance to delete task
    Task task = taskService.createTaskQuery().singleResult();
    runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "test delete task listener");

    assertEquals(1, TaskDeleteListener.eventCounter);
    assertEquals(task.getTaskDefinitionKey(), TaskDeleteListener.lastTaskDefinitionKey);
    assertEquals("test delete task listener", TaskDeleteListener.lastDeleteReason);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskDeleteListenerByBoundaryEvent() {
    TaskDeleteListener.clear();
    runtimeService.startProcessInstanceByKey("taskListenerProcess");

    assertEquals(0, TaskDeleteListener.eventCounter);
    assertNull(TaskDeleteListener.lastTaskDefinitionKey);
    assertNull(TaskDeleteListener.lastDeleteReason);

    // correlate message to delete task
    Task task = taskService.createTaskQuery().singleResult();
    runtimeService.correlateMessage("message");

    assertEquals(1, TaskDeleteListener.eventCounter);
    assertEquals(task.getTaskDefinitionKey(), TaskDeleteListener.lastTaskDefinitionKey);
    assertEquals("deleted", TaskDeleteListener.lastDeleteReason);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.bpmn20.xml"})
  public void testTaskListenerWithExpression() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
    assertEquals(null, runtimeService.getVariable(processInstance.getId(), "greeting2"));

    // Completing first task will change the description
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertEquals("Write meeting notes", runtimeService.getVariable(processInstance.getId(), "greeting2"));
  }

  @Test
  @Deployment
  public void testScriptListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "create"));

    taskService.setAssignee(task.getId(), "test");
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "assignment"));

    taskService.complete(task.getId());
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "complete"));

    task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    if (processEngineConfiguration.getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().variableName("delete").singleResult();
      assertNotNull(variable);
      assertTrue((Boolean) variable.getValue());
    }
  }

  @Test
  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.testScriptResourceListener.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/tasklistener/taskListener.groovy"
  })
  public void testScriptResourceListener() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "create"));

    taskService.setAssignee(task.getId(), "test");
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "assignment"));

    taskService.complete(task.getId());
    assertTrue((Boolean) runtimeService.getVariable(processInstance.getId(), "complete"));

    task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    if (processEngineConfiguration.getHistoryLevel().getId() >= HISTORYLEVEL_AUDIT) {
      HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().variableName("delete").singleResult();
      assertNotNull(variable);
      assertTrue((Boolean) variable.getValue());
    }
  }


  public static class TaskCreateListener implements TaskListener {
    public void notify(DelegateTask delegateTask) {
      delegateTask.complete();
    }
  }

  @Test
  public void testCompleteTaskInCreateTaskListener() {
    // given process with user task and task create listener
    BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess("startToEnd")
        .startEvent()
        .userTask()
        .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, TaskCreateListener.class.getName())
        .name("userTask")
        .endEvent().done();

    testRule.deploy(modelInstance);

    // when process is started and user task completed in task create listener
    runtimeService.startProcessInstanceByKey("startToEnd");

    // then task is successfully completed without an exception
    assertNull(taskService.createTaskQuery().singleResult());
  }

  @Test
  public void testCompleteTaskInCreateTaskListenerWithIdentityLinks() {
    // given process with user task, identity links and task create listener
    BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess("startToEnd")
        .startEvent()
        .userTask()
        .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, TaskCreateListener.class.getName())
        .name("userTask")
        .camundaCandidateUsers(Arrays.asList(new String[]{"users1", "user2"}))
        .camundaCandidateGroups(Arrays.asList(new String[]{"group1", "group2"}))
        .endEvent().done();

    testRule.deploy(modelInstance);

    // when process is started and user task completed in task create listener
    runtimeService.startProcessInstanceByKey("startToEnd");

    // then task is successfully completed without an exception
    assertNull(taskService.createTaskQuery().singleResult());
  }

  @Test
  public void testActivityInstanceIdOnDeleteInCalledProcess() {
    // given
    RecorderTaskListener.clear();

    BpmnModelInstance callActivityProcess = Bpmn.createExecutableProcess("calling")
        .startEvent()
        .callActivity()
          .calledElement("called")
        .endEvent()
        .done();

    BpmnModelInstance calledProcess = Bpmn.createExecutableProcess("called")
        .startEvent()
        .userTask()
          .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, RecorderTaskListener.class.getName())
          .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, RecorderTaskListener.class.getName())
        .endEvent()
        .done();

    testRule.deploy(callActivityProcess, calledProcess);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("calling");

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    List<RecordedTaskEvent> recordedEvents = RecorderTaskListener.getRecordedEvents();
    assertEquals(2, recordedEvents.size());
    String createActivityInstanceId = recordedEvents.get(0).getActivityInstanceId();
    String deleteActivityInstanceId = recordedEvents.get(1).getActivityInstanceId();

    assertEquals(createActivityInstanceId, deleteActivityInstanceId);
  }

  @Test
  public void testVariableAccessOnDeleteInCalledProcess() {
    // given
    VariablesCollectingListener.reset();

    BpmnModelInstance callActivityProcess = Bpmn.createExecutableProcess("calling")
        .startEvent()
        .callActivity()
          .camundaIn("foo", "foo")
          .calledElement("called")
        .endEvent()
        .done();

    BpmnModelInstance calledProcess = Bpmn.createExecutableProcess("called")
        .startEvent()
        .userTask()
          .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, VariablesCollectingListener.class.getName())
        .endEvent()
        .done();

    testRule.deploy(callActivityProcess, calledProcess);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("calling",
        Variables.createVariables().putValue("foo", "bar"));

    // when
    runtimeService.deleteProcessInstance(processInstance.getId(), null);

    // then
    VariableMap collectedVariables = VariablesCollectingListener.getCollectedVariables();
    assertNotNull(collectedVariables);
    assertEquals(1, collectedVariables.size());
    assertEquals("bar", collectedVariables.get("foo"));
  }

  @Test
  public void testCompleteTaskOnCreateListenerWithFollowingCallActivity() {
    final BpmnModelInstance subProcess = Bpmn.createExecutableProcess("subProc")
        .startEvent()
        .userTask("calledTask")
        .endEvent()
        .done();

    final BpmnModelInstance instance = Bpmn.createExecutableProcess("mainProc")
        .startEvent()
        .userTask("mainTask")
        .camundaTaskListenerClass(TaskListener.EVENTNAME_CREATE, CreateTaskListener.class.getName())
        .callActivity().calledElement("subProc")
        .endEvent()
        .done();

    testRule.deploy(subProcess);
    testRule.deploy(instance);

    engineRule.getRuntimeService().startProcessInstanceByKey("mainProc");
    Task task = engineRule.getTaskService().createTaskQuery().singleResult();

    Assert.assertEquals(task.getTaskDefinitionKey(), "calledTask");
  }

  @Test
  public void testAssignmentTaskListenerWhenSavingTask() {
    AssignmentTaskListener.reset();

    final BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("task")
          .camundaTaskListenerClass("assignment", AssignmentTaskListener.class)
        .endEvent()
        .done();

    testRule.deploy(process);
    engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // given
    Task task = engineRule.getTaskService().createTaskQuery().singleResult();

    // when
    task.setAssignee("gonzo");
    engineRule.getTaskService().saveTask(task);

    // then
    assertEquals(1, AssignmentTaskListener.eventCounter);
  }

  @Test
  @Deployment
  public void testTimeoutTaskListenerDuration() {
    // given
    ProcessInstance instance = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    assertThat((String) runtimeService.getVariable(instance.getId(), "timeout-status"), is("fired"));
  }

  @Test
  @Deployment
  public void testTimeoutTaskListenerDate() throws ParseException {
    // given
    ProcessInstance instance = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    ClockUtil.setCurrentTime(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse("2019-09-09T13:00:00"));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    assertThat((String) runtimeService.getVariable(instance.getId(), "timeout-status"), is("fired"));
  }

  @Test
  @Deployment
  public void testTimeoutTaskListenerCycle() {
    // given
    ProcessInstance instance = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // when
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(130L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    assertThat((String) runtimeService.getVariable(instance.getId(), "timeout-status"), is("fired2"));
  }

  @Test
  @Deployment
  public void testMultipleTimeoutTaskListeners() {
    // given
    ProcessInstance instance = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    // assume
    assertThat(engineRule.getManagementService().createJobQuery().count(), is(2L));

    // when
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    assertThat(engineRule.getManagementService().createJobQuery().count(), is(1L));
    assertThat((String) runtimeService.getVariable(instance.getId(), "timeout-status"), is("fired"));
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.testTimeoutTaskListenerDuration.bpmn20.xml")
  public void testTimeoutTaskListenerNotCalledWhenTaskCompleted() {
    // given
    JobQuery jobQuery = engineRule.getManagementService().createJobQuery();
    TaskQuery taskQuery = taskService.createTaskQuery();
    runtimeService.startProcessInstanceByKey("process");

    // assume
    assertThat(jobQuery.count(), is(1L));

    // when
    taskService.complete(taskQuery.singleResult().getId());

    // then
    HistoricVariableInstanceQuery variableQuery = historyService.createHistoricVariableInstanceQuery().variableName("timeout-status");
    assertThat(variableQuery.count(), is(0L));
    assertThat(jobQuery.count(), is(0L));
  }

  @Test
  @Deployment
  public void testTimeoutTaskListenerNotCalledWhenTaskCompletedByBoundaryEvent() {
    // given
    JobQuery jobQuery = engineRule.getManagementService().createJobQuery();
    runtimeService.startProcessInstanceByKey("process");

    // assume
    assertThat(jobQuery.count(), is(2L));

    // when the boundary event is triggered
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    HistoricVariableInstanceQuery variableQuery = historyService.createHistoricVariableInstanceQuery().variableName("timeout-status");
    assertThat(variableQuery.count(), is(0L));
    assertThat(jobQuery.count(), is(0L));
  }

  @Test
  @Deployment
  public void testRecalculateTimeoutTaskListenerDuedateCreationDateBased() {
    // given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", Variables.putValue("duration", "PT1H"));

    JobQuery jobQuery = engineRule.getManagementService().createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    Date oldDate = job.getDuedate();

    // when
    runtimeService.setVariable(pi.getId(), "duration", "PT15M");
    engineRule.getManagementService().recalculateJobDuedate(job.getId(), true);

    // then
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    assertNotEquals(oldDate, jobUpdated.getDuedate());
    assertTrue(oldDate.after(jobUpdated.getDuedate()));
    assertEquals(LocalDateTime.fromDateFields(jobUpdated.getCreateTime()).plusMinutes(15).toDate(), jobUpdated.getDuedate());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/tasklistener/TaskListenerTest.testRecalculateTimeoutTaskListenerDuedateCreationDateBased.bpmn20.xml")
  public void testRecalculateTimeoutTaskListenerDuedateCurrentDateBased() {
    // given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", Variables.putValue("duration", "PT1H"));

    JobQuery jobQuery = engineRule.getManagementService().createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    Date oldDate = job.getDuedate();
    ClockUtil.offset(2000L);

    // when
    engineRule.getManagementService().recalculateJobDuedate(job.getId(), false);

    // then
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    assertNotEquals(oldDate, jobUpdated.getDuedate());
    assertTrue(oldDate.before(jobUpdated.getDuedate()));
  }

  @Test
  @Deployment
  public void testRecalculateTimeoutTaskListenerDuedateCreationDateBasedWithDefinedBoundaryEvent() {
    // given
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", Variables.putValue("duration", "PT1H"));

    JobQuery jobQuery = engineRule.getManagementService().createJobQuery()
        .processInstanceId(pi.getId())
        .activityId("userTask");
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    Job job = jobs.get(0);
    Date oldDate = job.getDuedate();

    // when
    runtimeService.setVariable(pi.getId(), "duration", "PT15M");
    engineRule.getManagementService().recalculateJobDuedate(job.getId(), true);

    // then
    Job jobUpdated = jobQuery.singleResult();
    assertEquals(job.getId(), jobUpdated.getId());
    assertNotEquals(oldDate, jobUpdated.getDuedate());
    assertTrue(oldDate.after(jobUpdated.getDuedate()));
    assertEquals(LocalDateTime.fromDateFields(jobUpdated.getCreateTime()).plusMinutes(15).toDate(), jobUpdated.getDuedate());
  }

  @Test
  public void testThrowErrorOnCreateAndCatchOnUserTask() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnUserTask(TaskListener.EVENTNAME_CREATE);

    testRule.deploy(model);

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnAssignmentAndCatchOnUserTask() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnUserTask(TaskListener.EVENTNAME_ASSIGNMENT);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    firstTask.setAssignee("elmo");
    engineRule.getTaskService().saveTask(firstTask);

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCompleteAndCatchOnUserTask() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnUserTask(TaskListener.EVENTNAME_COMPLETE);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    taskService.complete(firstTask.getId());

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCreateAndCatchOnSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnSubprocess(TaskListener.EVENTNAME_CREATE);

    testRule.deploy(model);

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnAssignmentAndCatchOnSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnSubprocess(TaskListener.EVENTNAME_ASSIGNMENT);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    firstTask.setAssignee("elmo");
    engineRule.getTaskService().saveTask(firstTask);

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCompleteAndCatchOnSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnSubprocess(TaskListener.EVENTNAME_COMPLETE);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    taskService.complete(firstTask.getId());

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCreateAndCatchOnEventSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnEventSubprocess(TaskListener.EVENTNAME_CREATE);

    testRule.deploy(model);

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnAssignmentAndCatchOnEventSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnEventSubprocess(TaskListener.EVENTNAME_ASSIGNMENT);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    firstTask.setAssignee("elmo");
    engineRule.getTaskService().saveTask(firstTask);

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnCompleteAndCatchOnEventSubprocess() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnEventSubprocess(TaskListener.EVENTNAME_COMPLETE);

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    taskService.complete(firstTask.getId());

    // then
    verifyErrorGotCaught();
  }

  @Test
  @Deployment
  public void testThrowErrorOnCreateScriptListenerAndCatchOnUserTask() {
    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    Task resultTask = taskService.createTaskQuery().singleResult();
    assertNotNull(resultTask);
    assertEquals("afterCatch", resultTask.getName());
  }

  @Test
  public void testThrowErrorOnAssignmentExpressionListenerAndCatchOnUserTask() {
    // given
    processEngineConfiguration.getBeans().put("myListener", new ThrowBPMNErrorListener());
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("mainTask")
          .camundaTaskListenerExpression(TaskListener.EVENTNAME_ASSIGNMENT, "${myListener.notify(task)}")
          .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, DeleteListener.class.getName())
        .boundaryEvent("throw")
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .moveToActivity("mainTask")
        .userTask("afterThrow")
        .endEvent()
        .done();
    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    // when
    firstTask.setAssignee("elmo");
    engineRule.getTaskService().saveTask(firstTask);

    // then
    verifyErrorGotCaught();
  }

  @Test
  public void testThrowErrorOnDeleteAndCatchOnUserTaskShouldNotTriggerPropagation() {
    // given
    BpmnModelInstance model = createModelThrowErrorInListenerAndCatchOnUserTask(TaskListener.EVENTNAME_DELETE);

    DeploymentWithDefinitions deployment = testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // when
    try {
      runtimeService.deleteProcessInstance(processInstance.getId(), "invoke delete listener");
    } catch (Exception e) {
      // then
      assertTrue(e.getMessage().contains("business error"));
      assertEquals(1, ThrowBPMNErrorListener.INVOCATIONS);
      assertEquals(0, DeleteListener.INVOCATIONS);
    }

    // cleanup
    engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true, true);
  }

  @Test
  public void testThrowUncaughtErrorOnCompleteAndCatchOnUserTask() {
    // given
    processEngineConfiguration.setEnableExceptionsAfterUnhandledBpmnError(true);
    BpmnModelInstance model = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("mainTask")
          .camundaTaskListenerClass(TaskListener.EVENTNAME_COMPLETE, ThrowBPMNErrorListener.class.getName())
          .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, DeleteListener.class.getName())
        .userTask("afterThrow")
        .endEvent()
        .done();

    testRule.deploy(model);
    runtimeService.startProcessInstanceByKey("process");

    Task firstTask = taskService.createTaskQuery().singleResult();
    assertNotNull(firstTask);

    try {
      // when
      taskService.complete(firstTask.getId());
    } catch (ProcessEngineException e) {
      // then
      assertTrue(e.getMessage().contains("There was an exception while invoking the TaskListener"));
      assertTrue(e.getMessage().contains("Execution with id 'mainTask' throws an error event with errorCode '208', but no error handler was defined."));
    }

    // then
    Task resultTask = taskService.createTaskQuery().singleResult();
    assertNotNull(resultTask);
    assertEquals("mainTask", resultTask.getName());
    assertEquals(1, ThrowBPMNErrorListener.INVOCATIONS);
    assertEquals(0, DeleteListener.INVOCATIONS);

    // cleanup
    processEngineConfiguration.setEnableExceptionsAfterUnhandledBpmnError(false);
  }

  protected void verifyErrorGotCaught() {
    Task resultTask = taskService.createTaskQuery().singleResult();
    assertNotNull(resultTask);
    assertEquals("afterCatch", resultTask.getName());
    assertEquals(1, ThrowBPMNErrorListener.INVOCATIONS);
    assertEquals(1, DeleteListener.INVOCATIONS);
  }

  protected BpmnModelInstance createModelThrowErrorInListenerAndCatchOnUserTask(String eventName) {
    return Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("mainTask")
          .camundaTaskListenerClass(eventName, ThrowBPMNErrorListener.class.getName())
          .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, DeleteListener.class.getName())
        .boundaryEvent("throw")
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .moveToActivity("mainTask")
        .userTask("afterThrow")
        .endEvent()
        .done();
  }

  protected BpmnModelInstance createModelThrowErrorInListenerAndCatchOnSubprocess(String eventName) {
    return Bpmn.createExecutableProcess("process")
        .startEvent()
        .subProcess("sub")
          .embeddedSubProcess()
          .startEvent("inSub")
          .userTask("mainTask")
            .camundaTaskListenerClass(eventName, ThrowBPMNErrorListener.class.getName())
            .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, DeleteListener.class.getName())
          .userTask("afterThrow")
          .endEvent()
        .moveToActivity("sub")
        .boundaryEvent("throw")
        .error(ERROR_CODE)
        .userTask("afterCatch")
        .endEvent()
        .done();
  }

  protected BpmnModelInstance createModelThrowErrorInListenerAndCatchOnEventSubprocess(String eventName) {
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process");
    BpmnModelInstance model = processBuilder
        .startEvent()
        .userTask("mainTask")
          .camundaTaskListenerClass(eventName, ThrowBPMNErrorListener.class.getName())
          .camundaTaskListenerClass(TaskListener.EVENTNAME_DELETE, DeleteListener.class.getName())
        .userTask("afterThrow")
        .endEvent()
        .done();
    processBuilder.eventSubProcess()
       .startEvent("errorEvent").error(ERROR_CODE)
         .userTask("afterCatch")
       .endEvent();
    return model;
  }

  public static class VariablesCollectingListener implements TaskListener {

    protected static VariableMap collectedVariables;

    public static VariableMap getCollectedVariables() {
      return collectedVariables;
    }

    public static void reset() {
      collectedVariables = null;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
      collectedVariables = delegateTask.getVariablesTyped();
    }

  }

  public static class CreateTaskListener implements TaskListener {

      public void notify(DelegateTask delegateTask) {
          delegateTask.getProcessEngineServices().getTaskService().complete(delegateTask.getId());
      }
  }

  public static class AssignmentTaskListener implements TaskListener {

    public static int eventCounter = 0;

    public void notify(DelegateTask delegateTask) {
      eventCounter++;
    }

    public static void reset() {
      eventCounter = 0;
    }

  }

  public static class ThrowBPMNErrorListener implements TaskListener {
    public static int INVOCATIONS = 0;

    public void notify(DelegateTask delegateTask) {
      INVOCATIONS++;
      throw new BpmnError(ERROR_CODE, "business error 208");
    }

    public static void reset() {
      INVOCATIONS = 0;
    }
  }

  public static class DeleteListener implements TaskListener {
    public static int INVOCATIONS = 0;

    public void notify(DelegateTask delegateTask) {
      INVOCATIONS++;
    }

    public static void reset() {
      INVOCATIONS = 0;
    }
  }
}
