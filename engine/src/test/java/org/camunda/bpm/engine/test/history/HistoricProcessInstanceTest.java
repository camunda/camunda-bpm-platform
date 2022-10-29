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
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicProcessInstanceByProcessDefinitionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicProcessInstanceByProcessDefinitionKey;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicProcessInstanceByProcessDefinitionName;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicProcessInstanceByProcessDefinitionVersion;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicProcessInstanceByProcessInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.models.CallActivityModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricProcessInstanceTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testHelper);

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;
  protected TaskService taskService;
  protected CaseService caseService;

  @Before
  public void initServices() {
    repositoryService = engineRule.getRepositoryService();
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
    caseService = engineRule.getCaseService();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricDataCreatedForProcessExecution() {

    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2010);
    calendar.set(Calendar.MONTH, 8);
    calendar.set(Calendar.DAY_OF_MONTH, 30);
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date noon = calendar.getTime();

    ClockUtil.setCurrentTime(noon);
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "myBusinessKey");

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().unfinished().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finished().count());
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(historicProcessInstance);
    assertEquals(processInstance.getId(), historicProcessInstance.getId());
    assertEquals(processInstance.getBusinessKey(), historicProcessInstance.getBusinessKey());
    assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
    assertEquals(noon, historicProcessInstance.getStartTime());
    assertNull(historicProcessInstance.getEndTime());
    assertNull(historicProcessInstance.getDurationInMillis());
    assertNull(historicProcessInstance.getCaseInstanceId());

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

    assertEquals(1, tasks.size());

    // in this test scenario we assume that 25 seconds after the process start, the
    // user completes the task (yes! he must be almost as fast as me)
    Date twentyFiveSecsAfterNoon = new Date(noon.getTime() + 25*1000);
    ClockUtil.setCurrentTime(twentyFiveSecsAfterNoon);
    taskService.complete(tasks.get(0).getId());

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(historicProcessInstance);
    assertEquals(processInstance.getId(), historicProcessInstance.getId());
    assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
    assertEquals(noon, historicProcessInstance.getStartTime());
    assertEquals(twentyFiveSecsAfterNoon, historicProcessInstance.getEndTime());
    assertEquals(new Long(25 * 1000), historicProcessInstance.getDurationInMillis());
    assertTrue(((HistoricProcessInstanceEventEntity) historicProcessInstance).getDurationRaw() >= 25000);
    assertNull(historicProcessInstance.getCaseInstanceId());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().unfinished().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());

    runtimeService.startProcessInstanceByKey("oneTaskProcess", "myBusinessKey");
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().unfinished().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finished().unfinished().count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testLongRunningHistoricDataCreatedForProcessExecution() {
    final long ONE_YEAR = 1000 * 60 * 60 * 24 * 365;

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    Date now = cal.getTime();
    ClockUtil.setCurrentTime(now);

    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "myBusinessKey");

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().unfinished().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finished().count());
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertEquals(now, historicProcessInstance.getStartTime());

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());

    // in this test scenario we assume that one year after the process start, the
    // user completes the task (incredible speedy!)
    cal.add(Calendar.YEAR, 1);
    Date oneYearLater = cal.getTime();
    ClockUtil.setCurrentTime(oneYearLater);

    taskService.complete(tasks.get(0).getId());

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertEquals(now, historicProcessInstance.getStartTime());
    assertEquals(oneYearLater, historicProcessInstance.getEndTime());
    assertTrue(historicProcessInstance.getDurationInMillis() >= ONE_YEAR);
    assertTrue(((HistoricProcessInstanceEventEntity)historicProcessInstance).getDurationRaw() >= ONE_YEAR);

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().unfinished().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstanceHistoryCreated() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertNotNull(processInstance);

    // delete process instance should not delete the history
    runtimeService.deleteProcessInstance(processInstance.getId(), "cancel");
    HistoricProcessInstance historicProcessInstance =
      historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(historicProcessInstance.getEndTime());
  }

  @Test
  public void testDeleteProcessInstanceWithoutSubprocessInstances() {
    // given a process instance with subprocesses
    BpmnModelInstance calling =
        Bpmn.createExecutableProcess("calling")
          .startEvent()
          .callActivity()
            .calledElement("called")
          .endEvent("endA")
          .done();

    BpmnModelInstance called = Bpmn.createExecutableProcess("called")
        .startEvent()
        .userTask("Task1")
        .endEvent()
        .done();

    deployment(calling, called);

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("calling");

    // when the process instance is deleted and we do skip sub processes
    String id = instance.getId();
    runtimeService.deleteProcessInstance(id, "test_purposes", false, true, false, true);

    // then
    List<HistoricProcessInstance> historicSubprocessList = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("called").list();
    for (HistoricProcessInstance historicProcessInstance : historicSubprocessList) {
      assertNull(historicProcessInstance.getSuperProcessInstanceId());
    }
  }

  @Test
  public void testDeleteProcessInstanceWithSubprocessInstances() {
    // given a process instance with subprocesses
    BpmnModelInstance calling =
        Bpmn.createExecutableProcess("calling")
          .startEvent()
          .callActivity()
            .calledElement("called")
          .endEvent("endA")
          .done();

    BpmnModelInstance called = Bpmn.createExecutableProcess("called")
        .startEvent()
        .userTask("Task1")
        .endEvent()
        .done();

    deployment(calling, called);

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("calling");

    // when the process instance is deleted and we do not skip sub processes
    String id = instance.getId();
    runtimeService.deleteProcessInstance(id, "test_purposes", false, true, false, false);

    // then
    List<HistoricProcessInstance> historicSubprocessList = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("called").list();
    for (HistoricProcessInstance historicProcessInstance : historicSubprocessList) {
      assertNotNull(historicProcessInstance.getSuperProcessInstanceId());
    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceStartDate() {
    ClockUtil.setCurrentTime(new Date());
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Date date = ClockUtil.getCurrentTime();

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startDateOn(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startDateBy(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startDateBy(DateUtils.addDays(date, -1)).count());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startDateBy(DateUtils.addDays(date, 1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startDateOn(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startDateOn(DateUtils.addDays(date, 1)).count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceFinishDateUnfinished() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Date date = new Date();

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(date).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateBy(date).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateBy(DateUtils.addDays(date, 1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateBy(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(DateUtils.addDays(date, 1)).count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceFinishDateFinished() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Date date = new Date();

    runtimeService.deleteProcessInstance(pi.getId(), "cancel");

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishDateOn(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishDateBy(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishDateBy(DateUtils.addDays(date, 1)).count());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateBy(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(DateUtils.addDays(date, 1)).count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceDelete() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    runtimeService.deleteProcessInstance(pi.getId(), "cancel");

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertNotNull(historicProcessInstance.getDeleteReason());
    assertEquals("cancel", historicProcessInstance.getDeleteReason());

    assertNotNull(historicProcessInstance.getEndTime());
  }

  /** See: https://app.camunda.com/jira/browse/CAM-1324 */
  @Test
  @Deployment
  public void testHistoricProcessInstanceDeleteAsync() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failing");

    runtimeService.deleteProcessInstance(pi.getId(), "cancel");

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertNotNull(historicProcessInstance.getDeleteReason());
    assertEquals("cancel", historicProcessInstance.getDeleteReason());

    assertNotNull(historicProcessInstance.getEndTime());
  }

  @Test
  @Deployment
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcessInstanceQueryWithIncidents() {
    // start instance with incidents
    runtimeService.startProcessInstanceByKey("Process_1");
    testHelper.executeAvailableJobs();

    // start instance without incidents
    runtimeService.startProcessInstanceByKey("Process_1");

    assertEquals(2, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().withIncidents().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().withIncidents().list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessageLike("Unknown property used%\\_Tr%").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessageLike("Unknown property used%\\_Tr%").list().size());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().incidentMessageLike("Unknown message%").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().incidentMessageLike("Unknown message%").list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown property used in expression: ${incidentTrigger1}. Cause: Cannot resolve identifier 'incidentTrigger1'").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown property used in expression: ${incidentTrigger1}. Cause: Cannot resolve identifier 'incidentTrigger1'").list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown property used in expression: ${incident_Trigger2}. Cause: Cannot resolve identifier 'incident_Trigger2'").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown property used in expression: ${incident_Trigger2}. Cause: Cannot resolve identifier 'incident_Trigger2'").list().size());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown message").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown message").list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentType("failedJob").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentType("failedJob").list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().withRootIncidents().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().withRootIncidents().list().size());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldDeleteIncidentAfterJobWasSuccessfully.bpmn"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcessInstanceQueryIncidentStatusOpen() {
    //given a processes instance, which will fail
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);

    //when jobs are executed till retry count is zero
    testHelper.executeAvailableJobs();

    //then query for historic process instance with open incidents will return one
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentStatus("open").count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldDeleteIncidentAfterJobWasSuccessfully.bpmn"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcessInstanceQueryIncidentStatusResolved() {
    //given a incident processes instance
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);
    testHelper.executeAvailableJobs();

    //when `fail` variable is set to true and job retry count is set to one and executed again
    runtimeService.setVariable(pi1.getId(), "fail", false);
    Job jobToResolve = managementService.createJobQuery().processInstanceId(pi1.getId()).singleResult();
    managementService.setJobRetries(jobToResolve.getId(), 1);
    testHelper.executeAvailableJobs();

    //then query for historic process instance with resolved incidents will return one
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentStatus("resolved").count());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldDeleteIncidentAfterJobWasSuccessfully.bpmn"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcessInstanceQueryIncidentStatusOpenWithTwoProcesses() {
    //given two processes, which will fail, are started
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);
    runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);
    testHelper.executeAvailableJobs();
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().incidentStatus("open").count());

    //when 'fail' variable is set to false, job retry count is set to one
    //and available jobs are executed
    runtimeService.setVariable(pi1.getId(), "fail", false);
    Job jobToResolve = managementService.createJobQuery().processInstanceId(pi1.getId()).singleResult();
    managementService.setJobRetries(jobToResolve.getId(), 1);
    testHelper.executeAvailableJobs();

    //then query with open and with resolved incidents returns one
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentStatus("open").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentStatus("resolved").count());
  }

  @Test
  public void testHistoricProcessInstanceQueryWithIncidentMessageNull() {
    try {
      historyService.createHistoricProcessInstanceQuery().incidentMessage(null).count();
      fail("incidentMessage with null value is not allowed");
    } catch( NullValueException nex ) {
      // expected
    }
  }

  @Test
  public void testHistoricProcessInstanceQueryWithIncidentMessageLikeNull() {
    try {
      historyService.createHistoricProcessInstanceQuery().incidentMessageLike(null).count();
      fail("incidentMessageLike with null value is not allowed");
    } catch( NullValueException nex ) {
      // expected
    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneAsyncTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceQuery() {
    Calendar startTime = Calendar.getInstance();

    ClockUtil.setCurrentTime(startTime.getTime());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "businessKey_123");
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    // Start/end dates
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedBefore(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedBefore(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedAfter(hourFromNow.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startedBefore(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startedBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startedAfter(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startedAfter(hourFromNow.getTime()).startedBefore(hourAgo.getTime()).count());

    // General fields
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finished().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey("businessKey_123").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKeyLike("business%").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKeyLike("%sinessKey\\_123").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKeyLike("%siness%").count());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionName("The One Task_Process").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionNameLike("The One Task%").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionNameLike("%One Task\\_Process").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionNameLike("%One Task%").count());

    List<String> exludeIds = new ArrayList<String>();
    exludeIds.add("unexistingProcessDefinition");

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(exludeIds).count());

    exludeIds.add("oneTaskProcess");
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("oneTaskProcess").processDefinitionKeyNotIn(exludeIds).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(exludeIds).count());

    try {
      // oracle handles empty string like null which seems to lead to undefined behavior of the LIKE comparison
      historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(Arrays.asList(""));
      fail("Exception expected");
    }
    catch (NotValidException e) {
      // expected
    }

    // After finishing process
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishedBefore(hourFromNow.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedAfter(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedAfter(hourFromNow.getTime()).finishedBefore(hourAgo.getTime()).count());

    // No incidents should are created
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().withIncidents().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().incidentMessageLike("Unknown property used%").count());
    assertEquals(0, historyService
                      .createHistoricProcessInstanceQuery()
                      .incidentMessage("Unknown property used in expression: #{failing}. Cause: Cannot resolve identifier 'failing'")
                      .count()
    );

    // execute activities
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().executedActivityAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().executedActivityBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().executedActivityBefore(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().executedActivityAfter(hourFromNow.getTime()).count());

    // execute jobs
    if (engineRule.getProcessEngineConfiguration().getHistoryLevel().equals(HistoryLevel.HISTORY_LEVEL_FULL)) {
      assertEquals(1, historyService.createHistoricProcessInstanceQuery().executedJobAfter(hourAgo.getTime()).count());
      assertEquals(0, historyService.createHistoricProcessInstanceQuery().executedActivityBefore(hourAgo.getTime()).count());
      assertEquals(1, historyService.createHistoricProcessInstanceQuery().executedActivityBefore(hourFromNow.getTime()).count());
      assertEquals(0, historyService.createHistoricProcessInstanceQuery().executedActivityAfter(hourFromNow.getTime()).count());
    }
  }

  @Test
  public void testHistoricProcessInstanceSorting() {

    deployment("org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml");
    deployment("org/camunda/bpm/engine/test/history/HistoricActivityInstanceTest.testSorting.bpmn20.xml");

    //deploy second version of the same process definition
    deployment("org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml");

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").list();
    for (ProcessDefinition processDefinition: processDefinitions) {
      runtimeService.startProcessInstanceById(processDefinition.getId());
    }
    runtimeService.startProcessInstanceByKey("process");

    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().list();
    assertEquals(3, processInstances.size());
    verifySorting(processInstances, historicProcessInstanceByProcessInstanceId());

    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().list().size());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().list().size());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().list().size());

    processInstances = historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().list();
    assertEquals(3, processInstances.size());
    verifySorting(processInstances, historicProcessInstanceByProcessDefinitionId());

    processInstances = historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionKey().asc().list();
    assertEquals(3, processInstances.size());
    verifySorting(processInstances, historicProcessInstanceByProcessDefinitionKey());

    processInstances = historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionName().asc().list();
    assertEquals(3, processInstances.size());
    verifySorting(processInstances, historicProcessInstanceByProcessDefinitionName());

    processInstances = historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionVersion().asc().list();
    assertEquals(3, processInstances.size());
    verifySorting(processInstances, historicProcessInstanceByProcessDefinitionVersion());

    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().list().size());

    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().list().size());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().list().size());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().list().size());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().list().size());

    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().count());

    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().count());

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/superProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceSubProcess() {
    ProcessInstance superPi = runtimeService.startProcessInstanceByKey("subProcessQueryTest");
    ProcessInstance subPi = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superPi.getProcessInstanceId()).singleResult();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().subProcessInstanceId(subPi.getProcessInstanceId()).singleResult();
    assertNotNull(historicProcessInstance);
    assertEquals(historicProcessInstance.getId(), superPi.getId());
  }

  @Test
  public void testInvalidSorting() {
    try {
      historyService.createHistoricProcessInstanceQuery().asc();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricProcessInstanceQuery().desc();
      fail();
    } catch (ProcessEngineException e) {

    }

    try {
      historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().list();
      fail();
    } catch (ProcessEngineException e) {

    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  // ACT-1098
  public void testDeleteReason() {
    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(engineRule.getProcessEngineConfiguration().getHistory())) {
      final String deleteReason = "some delete reason";
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.deleteProcessInstance(pi.getId(), deleteReason);
      HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(pi.getId()).singleResult();
      assertEquals(deleteReason, hpi.getDeleteReason());
    }
  }

  @Test
  @Deployment
  public void testLongProcessDefinitionKey() {
    // must be equals to attribute id of element process in process model
    final String PROCESS_DEFINITION_KEY = "myrealrealrealrealrealrealrealrealrealrealreallongprocessdefinitionkeyawesome";

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    // get HPI by process instance id
    HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(hpi);
    testHelper.assertProcessEnded(hpi.getId());

    // get HPI by process definition key
    HistoricProcessInstance hpi2 = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    assertNotNull(hpi2);
    testHelper.assertProcessEnded(hpi2.getId());

    // check we got the same HPIs
    assertEquals(hpi.getId(), hpi2.getId());

  }

  @Test
  @Deployment(resources =
    {
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testQueryByCaseInstanceId.cmmn",
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testQueryByCaseInstanceId.bpmn20.xml"
      })
  public void testQueryByCaseInstanceId() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    // then
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    query.caseInstanceId(caseInstanceId);

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());

    HistoricProcessInstance historicProcessInstance = query.singleResult();
    assertNotNull(historicProcessInstance);
    assertNull(historicProcessInstance.getEndTime());

    assertEquals(caseInstanceId, historicProcessInstance.getCaseInstanceId());

    // complete existing user task -> completes the process instance
    String taskId = taskService
        .createTaskQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult()
        .getId();
    taskService.complete(taskId);

    // the completed historic process instance is still associated with the
    // case instance id
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());

    historicProcessInstance = query.singleResult();
    assertNotNull(historicProcessInstance);
    assertNotNull(historicProcessInstance.getEndTime());

    assertEquals(caseInstanceId, historicProcessInstance.getCaseInstanceId());

  }

  @Test
  @Deployment(resources =
    {
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testQueryByCaseInstanceId.cmmn",
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testQueryByCaseInstanceIdHierarchy-super.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testQueryByCaseInstanceIdHierarchy-sub.bpmn20.xml"
      })
  public void testQueryByCaseInstanceIdHierarchy() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    // then
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    query.caseInstanceId(caseInstanceId);

    assertEquals(2, query.count());
    assertEquals(2, query.list().size());

    for (HistoricProcessInstance hpi : query.list()) {
      assertEquals(caseInstanceId, hpi.getCaseInstanceId());
    }

    // complete existing user task -> completes the process instance(s)
    String taskId = taskService
        .createTaskQuery()
        .caseInstanceId(caseInstanceId)
        .singleResult()
        .getId();
    taskService.complete(taskId);

    // the completed historic process instance is still associated with the
    // case instance id
    assertEquals(2, query.count());
    assertEquals(2, query.list().size());

    for (HistoricProcessInstance hpi : query.list()) {
      assertEquals(caseInstanceId, hpi.getCaseInstanceId());
    }

  }

  @Test
  public void testQueryByInvalidCaseInstanceId() {
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    query.caseInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  @Deployment(resources =
    {
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testBusinessKey.cmmn",
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testBusinessKey.bpmn20.xml"
      })
  public void testBusinessKey() {
    // given
    String businessKey = "aBusinessKey";

    caseService
      .withCaseDefinitionByKey("case")
      .businessKey(businessKey)
      .create()
      .getId();

    // then
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    query.processInstanceBusinessKey(businessKey);

    assertEquals(1, query.count());
    assertEquals(1, query.list().size());

    HistoricProcessInstance historicProcessInstance = query.singleResult();
    assertNotNull(historicProcessInstance);

    assertEquals(businessKey, historicProcessInstance.getBusinessKey());

  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testStartActivityId-super.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testStartActivityId-sub.bpmn20.xml"
  })
  public void testStartActivityId() {
    // given

    // when
    runtimeService.startProcessInstanceByKey("super");

    // then
    HistoricProcessInstance hpi = historyService
        .createHistoricProcessInstanceQuery()
        .processDefinitionKey("sub")
        .singleResult();

    assertEquals("theSubStart", hpi.getStartActivityId());

  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testStartActivityId-super.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testAsyncStartActivityId-sub.bpmn20.xml"
  })
  public void testAsyncStartActivityId() {
    // given
    runtimeService.startProcessInstanceByKey("super");

    // when
    testHelper.executeAvailableJobs();

    // then
    HistoricProcessInstance hpi = historyService
        .createHistoricProcessInstanceQuery()
        .processDefinitionKey("sub")
        .singleResult();

    assertEquals("theSubStart", hpi.getStartActivityId());

  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testStartByKeyWithCaseInstanceId() {
    String caseInstanceId = "aCaseInstanceId";

    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess", null, caseInstanceId).getId();

    HistoricProcessInstance firstInstance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    assertNotNull(firstInstance);

    assertEquals(caseInstanceId, firstInstance.getCaseInstanceId());

    // the second possibility to start a process instance /////////////////////////////////////////////

    processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess", null, caseInstanceId, null).getId();

    HistoricProcessInstance secondInstance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    assertNotNull(secondInstance);

    assertEquals(caseInstanceId, secondInstance.getCaseInstanceId());

  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testStartByIdWithCaseInstanceId() {
    String processDefinitionId = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("oneTaskProcess")
        .singleResult()
        .getId();

    String caseInstanceId = "aCaseInstanceId";
    String processInstanceId = runtimeService.startProcessInstanceById(processDefinitionId, null, caseInstanceId).getId();

    HistoricProcessInstance firstInstance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    assertNotNull(firstInstance);

    assertEquals(caseInstanceId, firstInstance.getCaseInstanceId());

    // the second possibility to start a process instance /////////////////////////////////////////////

    processInstanceId = runtimeService.startProcessInstanceById(processDefinitionId, null, caseInstanceId, null).getId();

    HistoricProcessInstance secondInstance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    assertNotNull(secondInstance);

    assertEquals(caseInstanceId, secondInstance.getCaseInstanceId());

  }

  @Test
  @Deployment
  public void testEndTimeAndEndActivity() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String taskId = taskService
        .createTaskQuery()
        .taskDefinitionKey("userTask2")
        .singleResult()
        .getId();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // when (1)
    taskService.complete(taskId);

    // then (1)
    HistoricProcessInstance historicProcessInstance = query.singleResult();

    assertNull(historicProcessInstance.getEndActivityId());
    assertNull(historicProcessInstance.getEndTime());

    // when (2)
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then (2)
    historicProcessInstance = query.singleResult();

    assertNull(historicProcessInstance.getEndActivityId());
    assertNotNull(historicProcessInstance.getEndTime());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"
  })
  public void testQueryBySuperCaseInstanceId() {
    String superCaseInstanceId = caseService.createCaseInstanceByKey("oneProcessTaskCase").getId();

    HistoricProcessInstanceQuery query = historyService
        .createHistoricProcessInstanceQuery()
        .superCaseInstanceId(superCaseInstanceId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    HistoricProcessInstance subProcessInstance = query.singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(superCaseInstanceId, subProcessInstance.getSuperCaseInstanceId());
    assertNull(subProcessInstance.getSuperProcessInstanceId());
  }

  @Test
  public void testQueryByInvalidSuperCaseInstanceId() {
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    query.superCaseInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/runtime/superProcessWithCaseCallActivity.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testQueryBySubCaseInstanceId() {
    String superProcessInstanceId = runtimeService.startProcessInstanceByKey("subProcessQueryTest").getId();

    String subCaseInstanceId = caseService
        .createCaseInstanceQuery()
        .superProcessInstanceId(superProcessInstanceId)
        .singleResult()
        .getId();

    HistoricProcessInstanceQuery query = historyService
        .createHistoricProcessInstanceQuery()
        .subCaseInstanceId(subCaseInstanceId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    HistoricProcessInstance superProcessInstance = query.singleResult();
    assertNotNull(superProcessInstance);
    assertEquals(superProcessInstanceId, superProcessInstance.getId());
    assertNull(superProcessInstance.getSuperCaseInstanceId());
    assertNull(superProcessInstance.getSuperProcessInstanceId());
  }

  @Test
  public void testQueryByInvalidSubCaseInstanceId() {
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    query.subCaseInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"
  })
  public void testSuperCaseInstanceIdProperty() {
    String superCaseInstanceId = caseService.createCaseInstanceByKey("oneProcessTaskCase").getId();

    caseService
        .createCaseExecutionQuery()
        .activityId("PI_ProcessTask_1")
        .singleResult()
        .getId();

    HistoricProcessInstance instance = historyService
        .createHistoricProcessInstanceQuery()
        .singleResult();

    assertNotNull(instance);
    assertEquals(superCaseInstanceId, instance.getSuperCaseInstanceId());

    String taskId = taskService
        .createTaskQuery()
        .singleResult()
        .getId();
    taskService.complete(taskId);

    instance = historyService
        .createHistoricProcessInstanceQuery()
        .singleResult();

    assertNotNull(instance);
    assertEquals(superCaseInstanceId, instance.getSuperCaseInstanceId());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessDefinitionKeyProperty() {
    // given
    String key = "oneTaskProcess";
    String processInstanceId = runtimeService.startProcessInstanceByKey(key).getId();

    // when
    HistoricProcessInstance instance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    // then
    assertNotNull(instance.getProcessDefinitionKey());
    assertEquals(key, instance.getProcessDefinitionKey());
  }

  @Test
  @Deployment
  public void testProcessInstanceShouldBeActive() {
    // given

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then
    HistoricProcessInstance historicProcessInstance = historyService
      .createHistoricProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    assertNull(historicProcessInstance.getEndTime());
    assertNull(historicProcessInstance.getDurationInMillis());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testRetrieveProcessDefinitionName() {

    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();

    // when
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    // then
    assertEquals("The One Task Process", historicProcessInstance.getProcessDefinitionName());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testRetrieveProcessDefinitionVersion() {

    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();

    // when
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    // then
    assertEquals(1, historicProcessInstance.getProcessDefinitionVersion().intValue());
  }

  @Test
  public void testHistoricProcInstExecutedActivityInInterval() {
    // given proc instance with wait state
    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc")
                                  .startEvent()
                                    .userTask()
                                  .endEvent()
                                  .done();
    deployment(model);

    Calendar hourFromNow = (Calendar) now.clone();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    runtimeService.startProcessInstanceByKey("proc");

    //when query historic process instance which has executed an activity after the start time
    // and before a hour after start time
    HistoricProcessInstance historicProcessInstance =
      historyService.createHistoricProcessInstanceQuery()
        .executedActivityAfter(now.getTime())
        .executedActivityBefore(hourFromNow.getTime())
        .singleResult();


    //then query returns result
    assertNotNull(historicProcessInstance);


    // when proc inst is not in interval
    Calendar sixHoursFromNow = (Calendar) now.clone();
    sixHoursFromNow.add(Calendar.HOUR_OF_DAY, 6);


    historicProcessInstance =
      historyService.createHistoricProcessInstanceQuery()
        .executedActivityAfter(hourFromNow.getTime())
        .executedActivityBefore(sixHoursFromNow.getTime())
        .singleResult();

    //then query should return NO result
    assertNull(historicProcessInstance);
  }

  @Test
  public void testHistoricProcInstExecutedActivityAfter() {
    // given
    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar hourFromNow = (Calendar) now.clone();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    runtimeService.startProcessInstanceByKey("proc");

    //when query historic process instance which has executed an activity after the start time
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executedActivityAfter(now.getTime()).singleResult();

    //then query returns result
    assertNotNull(historicProcessInstance);

    //when query historic proc inst with execute activity after a hour of the starting time
    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executedActivityAfter(hourFromNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }

  @Test
  public void testHistoricProcInstExecutedActivityBefore() {
    // given
    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar hourBeforeNow = (Calendar) now.clone();
    hourBeforeNow.add(Calendar.HOUR, -1);

    runtimeService.startProcessInstanceByKey("proc");

    //when query historic process instance which has executed an activity before the start time
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executedActivityBefore(now.getTime()).singleResult();

    //then query returns result, since the query is less-then-equal
    assertNotNull(historicProcessInstance);

    //when query historic proc inst which executes an activity an hour before the starting time
    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executedActivityBefore(hourBeforeNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }

  @Test
  public void testHistoricProcInstExecutedActivityWithTwoProcInsts() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar now = Calendar.getInstance();
    Calendar hourBeforeNow = (Calendar) now.clone();
    hourBeforeNow.add(Calendar.HOUR, -1);

    ClockUtil.setCurrentTime(hourBeforeNow.getTime());
    runtimeService.startProcessInstanceByKey("proc");

    ClockUtil.setCurrentTime(now.getTime());
    runtimeService.startProcessInstanceByKey("proc");

    //when query execute activity between now and an hour ago
    List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                                                       .executedActivityAfter(hourBeforeNow.getTime())
                                                       .executedActivityBefore(now.getTime()).list();

    //then two historic process instance have to be returned
    assertEquals(2, list.size());

    //when query execute activity after an half hour before now
    Calendar halfHour = (Calendar) now.clone();
    halfHour.add(Calendar.MINUTE, -30);
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .executedActivityAfter(halfHour.getTime()).singleResult();

    //then only the latest historic process instance is returned
    assertNotNull(historicProcessInstance);
  }


  @Test
  public void testHistoricProcInstExecutedActivityWithEmptyInterval() {
    // given
    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar hourBeforeNow = (Calendar) now.clone();
    hourBeforeNow.add(Calendar.HOUR, -1);

    runtimeService.startProcessInstanceByKey("proc");

    //when query historic proc inst which executes an activity an hour before and after the starting time
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .executedActivityBefore(hourBeforeNow.getTime())
      .executedActivityAfter(hourBeforeNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcInstExecutedJobAfter() {
    // given
    BpmnModelInstance asyncModel = Bpmn.createExecutableProcess("async").startEvent().camundaAsyncBefore().endEvent().done();
    deployment(asyncModel);
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    Calendar hourFromNow = (Calendar) now.clone();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    runtimeService.startProcessInstanceByKey("async");
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());
    runtimeService.startProcessInstanceByKey("proc");

    //when query historic process instance which has executed an job after the start time
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .executedJobAfter(now.getTime()).singleResult();

    //then query returns only a single process instance
    assertNotNull(historicProcessInstance);

    //when query historic proc inst with execute job after a hour of the starting time
    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executedJobAfter(hourFromNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }


  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcInstExecutedJobBefore() {
    // given
    BpmnModelInstance asyncModel = Bpmn.createExecutableProcess("async").startEvent().camundaAsyncBefore().endEvent().done();
    deployment(asyncModel);
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    Calendar hourBeforeNow = (Calendar) now.clone();
    hourBeforeNow.add(Calendar.HOUR_OF_DAY, -1);

    runtimeService.startProcessInstanceByKey("async");
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());
    runtimeService.startProcessInstanceByKey("proc");

    //when query historic process instance which has executed an job before the start time
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .executedJobBefore(now.getTime()).singleResult();

    //then query returns only a single process instance since before is less-then-equal
    assertNotNull(historicProcessInstance);

    //when query historic proc inst with executed job before an hour of the starting time
    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executedJobBefore(hourBeforeNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcInstExecutedJobWithTwoProcInsts() {
    // given
    BpmnModelInstance asyncModel = Bpmn.createExecutableProcess("async").startEvent().camundaAsyncBefore().endEvent().done();
    deployment(asyncModel);

    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    Calendar hourBeforeNow = (Calendar) now.clone();
    hourBeforeNow.add(Calendar.HOUR_OF_DAY, -1);

    ClockUtil.setCurrentTime(hourBeforeNow.getTime());
    runtimeService.startProcessInstanceByKey("async");
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    ClockUtil.setCurrentTime(now.getTime());
    runtimeService.startProcessInstanceByKey("async");
    runtimeService.startProcessInstanceByKey("proc");

    //when query executed job between now and an hour ago
    List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
      .executedJobAfter(hourBeforeNow.getTime())
      .executedJobBefore(now.getTime()).list();

    //then the two async historic process instance have to be returned
    assertEquals(2, list.size());

    //when query execute activity after an half hour before now
    Calendar halfHour = (Calendar) now.clone();
    halfHour.add(Calendar.MINUTE, -30);
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .executedJobAfter(halfHour.getTime()).singleResult();

    //then only the latest async historic process instance is returned
    assertNotNull(historicProcessInstance);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcInstExecutedJobWithEmptyInterval() {
    // given
    BpmnModelInstance asyncModel = Bpmn.createExecutableProcess("async").startEvent().camundaAsyncBefore().endEvent().done();
    deployment(asyncModel);
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    Calendar hourBeforeNow = (Calendar) now.clone();
    hourBeforeNow.add(Calendar.HOUR_OF_DAY, -1);

    runtimeService.startProcessInstanceByKey("async");
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());
    runtimeService.startProcessInstanceByKey("proc");

    //when query historic proc inst with executed job before and after an hour before the starting time
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .executedJobBefore(hourBeforeNow.getTime())
      .executedJobAfter(hourBeforeNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcInstQueryWithExecutedActivityIds() {
    // given
    deployment(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    Task task = taskService.createTaskQuery().active().singleResult();
    taskService.complete(task.getId());

    // assume
    HistoricActivityInstance historicActivityInstance = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstance.getId())
        .activityId("userTask1")
        .singleResult();
    assertNotNull(historicActivityInstance);

    // when
    List<HistoricProcessInstance> result = historyService
        .createHistoricProcessInstanceQuery()
        .executedActivityIdIn(historicActivityInstance.getActivityId())
        .list();

    // then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(result.get(0).getId(), processInstance.getId());
  }

  @Test
  public void testHistoricProcInstQueryWithExecutedActivityIdsNull() {
    try {
      historyService.createHistoricProcessInstanceQuery()
      .executedActivityIdIn((String[]) null).list();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("activity ids is null");
    }
  }

  @Test
  public void testHistoricProcInstQueryWithExecutedActivityIdsContainNull() {
    try {
      historyService.createHistoricProcessInstanceQuery()
      .executedActivityIdIn(null, "1").list();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("activity ids contains null");
    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcInstQueryWithActiveActivityIds() {
    // given
    deployment(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    // assume
    HistoricActivityInstance historicActivityInstance = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("userTask1")
        .singleResult();
    assertNotNull(historicActivityInstance);

    // when
    List<HistoricProcessInstance> result = historyService
        .createHistoricProcessInstanceQuery()
        .activeActivityIdIn(historicActivityInstance.getActivityId())
        .list();

    // then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(result.get(0).getId(), processInstance.getId());
  }

  @Test
  public void testHistoricProcInstQueryWithActiveActivityIdsNull() {
    try {
      historyService.createHistoricProcessInstanceQuery()
      .activeActivityIdIn((String[]) null).list();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("activity ids is null");
    }
  }

  @Test
  public void testHistoricProcInstQueryWithActiveActivityIdsContainNull() {
    try {
      historyService.createHistoricProcessInstanceQuery()
      .activeActivityIdIn(null, "1").list();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("activity ids contains null");
    }
  }

  @Test
  public void testQueryByActiveActivityIdInAndProcessDefinitionKey() {
    // given
    deployment(ProcessModels.ONE_TASK_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    // when
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey("Process")
      .activeActivityIdIn("userTask")
      .singleResult();

    // then
    assertNotNull(historicProcessInstance);
    assertEquals(processInstance.getId(), historicProcessInstance.getId());
  }

  @Test
  public void testQueryByExecutedActivityIdInAndProcessDefinitionKey() {
    // given
    deployment(ProcessModels.ONE_TASK_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKey("Process")
      .executedActivityIdIn("userTask")
      .singleResult();

    // then
    assertNotNull(historicProcessInstance);
    assertEquals(processInstance.getId(), historicProcessInstance.getId());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testQueryWithRootIncidents() {
    // given
    deployment("org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testQueryWithRootIncidents.bpmn20.xml");
    deployment(CallActivityModels.oneBpmnCallActivityProcess("Process_1"));

    runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance calledProcessInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("Process_1").singleResult();
    testHelper.executeAvailableJobs();

    // when
    List<HistoricProcessInstance> historicProcInstances = historyService.createHistoricProcessInstanceQuery().withRootIncidents().list();

    // then
    assertNotNull(calledProcessInstance);
    assertEquals(1, historicProcInstances.size());
    assertEquals(calledProcessInstance.getId(), historicProcInstances.get(0).getId());
  }

  @Test
  public void testQueryWithProcessDefinitionKeyIn() {
    // given
    deployment(ProcessModels.ONE_TASK_PROCESS);
    runtimeService.startProcessInstanceByKey(ProcessModels.PROCESS_KEY);
    runtimeService.startProcessInstanceByKey(ProcessModels.PROCESS_KEY);
    runtimeService.startProcessInstanceByKey(ProcessModels.PROCESS_KEY);

    deployment(modify(ProcessModels.TWO_TASKS_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "ONE_TASKS_PROCESS"));
    runtimeService.startProcessInstanceByKey("ONE_TASKS_PROCESS");
    runtimeService.startProcessInstanceByKey("ONE_TASKS_PROCESS");

    deployment(modify(ProcessModels.TWO_TASKS_PROCESS).changeElementId(ProcessModels.PROCESS_KEY, "TWO_TASKS_PROCESS"));
    runtimeService.startProcessInstanceByKey("TWO_TASKS_PROCESS");
    runtimeService.startProcessInstanceByKey("TWO_TASKS_PROCESS");
    runtimeService.startProcessInstanceByKey("TWO_TASKS_PROCESS");
    runtimeService.startProcessInstanceByKey("TWO_TASKS_PROCESS");

    // assume
    assertThat(historyService.createHistoricProcessInstanceQuery().count()).isEqualTo(9l);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKeyIn("ONE_TASKS_PROCESS", "TWO_TASKS_PROCESS");

    // then
    assertThat(query.count()).isEqualTo(6l);
    assertThat(query.list().size()).isEqualTo(6);
  }

  @Test
  public void testQueryByNonExistingProcessDefinitionKeyIn() {
    // given
    deployment(ProcessModels.ONE_TASK_PROCESS);
    runtimeService.startProcessInstanceByKey(ProcessModels.PROCESS_KEY);

    // when
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
      .processDefinitionKeyIn("not-existing-key");

    // then
    assertThat(query.count()).isEqualTo(0l);
    assertThat(query.list().size()).isEqualTo(0);
  }

  @Test
  public void testQueryByOneInvalidProcessDefinitionKeyIn() {
    try {
      // when
      historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKeyIn((String) null);
      fail();
    } catch(ProcessEngineException expected) {
      // then Exception is expected
    }
  }

  @Test
  public void testQueryByMultipleInvalidProcessDefinitionKeyIn() {
    try {
      // when
      historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKeyIn(ProcessModels.PROCESS_KEY, null);
      fail();
    } catch(ProcessEngineException expected) {
      // then Exception is expected
    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneAsyncTaskProcess.bpmn20.xml"})
  public void testShouldStoreHistoricProcessInstanceVariableOnAsyncBefore() {
    // given definition with asyncBefore startEvent

    // when trigger process instance with variables
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.createVariables().putValue("foo", "bar"));

    // then
    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableName("foo").singleResult();
    assertNotNull(historicVariable);
    assertEquals("bar", historicVariable.getValue());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneAsyncTaskProcess.bpmn20.xml"})
  public void testShouldStoreInitialHistoricProcessInstanceVariableOnAsyncBefore() {
    // given definition with asyncBefore startEvent

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.createVariables().putValue("foo", "bar"));

    runtimeService.setVariable(processInstance.getId(), "goo", "car");

    // when
    executeJob(managementService.createJobQuery().singleResult());

    // then
    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableName("foo").singleResult();
    assertNotNull(historicVariable);
    assertEquals("bar", historicVariable.getValue());
    historicVariable = historyService.createHistoricVariableInstanceQuery().variableName("goo").singleResult();
    assertNotNull(historicVariable);
    assertEquals("car", historicVariable.getValue());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneAsyncTaskProcess.bpmn20.xml"})
  public void testShouldSetVariableBeforeAsyncBefore() {
    // given definition with asyncBefore startEvent

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    runtimeService.setVariable(processInstance.getId(), "goo", "car");

    // when
    executeJob(managementService.createJobQuery().singleResult());

    // then
    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().variableName("goo").singleResult();
    assertNotNull(historicVariable);
    assertEquals("car", historicVariable.getValue());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_1() {
    // GIVEN
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("foo", "bar").putValue("bar", "foo"))
        .getProcessInstanceId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("foo", "bar").putValue("bar", "foo"))
        .getProcessInstanceId();

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .variableValueEquals("foo", "bar")
        .variableValueEquals("bar", "foo")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne, processInstanceIdTwo);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_2() {
    // GIVEN
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("foo", "bar").putValue("bar", "foo"))
        .getProcessInstanceId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("foo", "bar").putValue("bar", "foo"))
        .getProcessInstanceId();

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .variableValueEquals("foo", "bar")
        .variableValueEquals("foo", "bar")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne, processInstanceIdTwo);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_3() {
    // GIVEN
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("foo", "bar").putValue("bar", "foo"))
        .getProcessInstanceId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("foo", "bar").putValue("bar", "foo"))
        .getProcessInstanceId();

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .variableValueEquals("foo", "bar")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne, processInstanceIdTwo);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_4() {
    // GIVEN
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("foo", "bar"))
        .getProcessInstanceId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("bar", "foo"))
        .getProcessInstanceId();

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .variableValueEquals("foo", "bar")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_5() {
    // GIVEN
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("foo", "bar"))
        .getProcessInstanceId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.putValue("bar", "foo"))
        .getProcessInstanceId();

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .variableValueEquals("foo", "bar")
        .variableValueEquals("foo", "bar")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_6() {
    // GIVEN
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.putValue("foo", "bar"));
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.putValue("bar", "foo"));

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .variableValueEquals("foo", "bar")
        .variableValueEquals("bar", "foo")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_7() {
    // GIVEN
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.putValue("foo", "foo"));
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.putValue("bar", "foo"));

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .variableValueEquals("foo", "bar")
        .variableValueEquals("foo", "foo")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_8() {
    // GIVEN
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.putValue("foo", "foo"));
    runtimeService.startProcessInstanceByKey("oneTaskProcess", Variables.putValue("foo", "bar"));

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .variableValueEquals("foo", "bar")
        .variableValueEquals("foo", "foo")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_9() {
    // GIVEN
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess", "a-business-key").getProcessInstanceId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess", "another-business-key").getProcessInstanceId();

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .processInstanceBusinessKeyIn("a-business-key", "another-business-key")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne, processInstanceIdTwo);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_10() {
    // GIVEN
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess", "a-business-key").getProcessInstanceId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess").getProcessInstanceId();

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .processInstanceBusinessKeyIn("a-business-key", "another-business-key")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_11() {
    // GIVEN
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess", "a-business-key").getProcessInstanceId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess", "a-business-key").getProcessInstanceId();

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .processInstanceBusinessKeyIn("a-business-key")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne, processInstanceIdTwo);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void shouldQueryByVariableValue_12() {
    // GIVEN
    String processInstanceIdOne = runtimeService.startProcessInstanceByKey("oneTaskProcess", "a-business-key").getProcessInstanceId();
    String processInstanceIdTwo = runtimeService.startProcessInstanceByKey("oneTaskProcess", "a-business-key").getProcessInstanceId();

    // WHEN
    List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
        .processInstanceBusinessKeyIn("a-business-key", "another-business-key")
        .list();

    // THEN
    assertThat(processInstances)
        .extracting("processInstanceId")
        .containsExactlyInAnyOrder(processInstanceIdOne, processInstanceIdTwo);
  }

  protected void deployment(String... resources) {
    testHelper.deploy(resources);
  }

  protected void deployment(BpmnModelInstance... modelInstances) {
    testHelper.deploy(modelInstances);
  }

  protected void executeJob(Job job) {
    while (job != null && job.getRetries() > 0) {
      try {
        managementService.executeJob(job.getId());
      }
      catch (Exception e) {
        // ignore
      }

      job = managementService.createJobQuery().jobId(job.getId()).singleResult();
    }
  }

}
