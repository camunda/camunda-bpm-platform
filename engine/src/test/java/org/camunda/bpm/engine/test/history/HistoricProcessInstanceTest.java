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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricProcessInstanceTest extends PluggableProcessEngineTestCase {

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

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceStartDate() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Date date = new Date();

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startDateOn(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startDateBy(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startDateBy(DateUtils.addDays(date, -1)).count());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startDateBy(DateUtils.addDays(date, 1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startDateOn(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startDateOn(DateUtils.addDays(date, 1)).count());
  }

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
  @Deployment
  public void testHistoricProcessInstanceDeleteAsync() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failing");

    runtimeService.deleteProcessInstance(pi.getId(), "cancel");

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertNotNull(historicProcessInstance.getDeleteReason());
    assertEquals("cancel", historicProcessInstance.getDeleteReason());

    assertNotNull(historicProcessInstance.getEndTime());
  }

  @Deployment
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcessInstanceQueryWithIncidents() {
    // start instance with incidents
    runtimeService.startProcessInstanceByKey("Process_1");
    executeAvailableJobs();

    // start instance without incidents
    runtimeService.startProcessInstanceByKey("Process_1");

    assertEquals(2, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().withIncidents().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().withIncidents().list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessageLike("Unknown property used%").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessageLike("Unknown property used%").list().size());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().incidentMessageLike("Unknown message%").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().incidentMessageLike("Unknown message%").list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown property used in expression: ${incidentTrigger1}. Cause: Cannot resolve identifier 'incidentTrigger1'").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown property used in expression: ${incidentTrigger1}. Cause: Cannot resolve identifier 'incidentTrigger1'").list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown property used in expression: ${incidentTrigger2}. Cause: Cannot resolve identifier 'incidentTrigger2'").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown property used in expression: ${incidentTrigger2}. Cause: Cannot resolve identifier 'incidentTrigger2'").list().size());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown message").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().incidentMessage("Unknown message").list().size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldDeleteIncidentAfterJobWasSuccessfully.bpmn"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcessInstanceQueryIncidentStatusOpen() {
    //given a processes instance, which will fail
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);

    //when jobs are executed till retry count is zero
    executeAvailableJobs();

    //then query for historic process instance with open incidents will return one
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentStatus("open").count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldDeleteIncidentAfterJobWasSuccessfully.bpmn"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcessInstanceQueryIncidentStatusResolved() {
    //given a incident processes instance
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);
    executeAvailableJobs();

    //when `fail` variable is set to true and job retry count is set to one and executed again
    runtimeService.setVariable(pi1.getId(), "fail", false);
    Job jobToResolve = managementService.createJobQuery().processInstanceId(pi1.getId()).singleResult();
    managementService.setJobRetries(jobToResolve.getId(), 1);
    executeAvailableJobs();

    //then query for historic process instance with resolved incidents will return one
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentStatus("resolved").count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/IncidentTest.testShouldDeleteIncidentAfterJobWasSuccessfully.bpmn"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcessInstanceQueryIncidentStatusOpenWithTwoProcesses() {
    //given two processes, which will fail, are started
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", true);
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);
    runtimeService.startProcessInstanceByKey("failingProcessWithUserTask", parameters);
    executeAvailableJobs();
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().incidentStatus("open").count());

    //when 'fail' variable is set to false, job retry count is set to one
    //and available jobs are executed
    runtimeService.setVariable(pi1.getId(), "fail", false);
    Job jobToResolve = managementService.createJobQuery().processInstanceId(pi1.getId()).singleResult();
    managementService.setJobRetries(jobToResolve.getId(), 1);
    executeAvailableJobs();

    //then query with open and with resolved incidents returns one
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentStatus("open").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().incidentStatus("resolved").count());
  }

  public void testHistoricProcessInstanceQueryWithIncidentMessageNull() {
    try {
      historyService.createHistoricProcessInstanceQuery().incidentMessage(null).count();
      fail("incidentMessage with null value is not allowed");
    } catch( NullValueException nex ) {
      // expected
    }
  }

  public void testHistoricProcessInstanceQueryWithIncidentMessageLikeNull() {
    try {
      historyService.createHistoricProcessInstanceQuery().incidentMessageLike(null).count();
      fail("incidentMessageLike with null value is not allowed");
    } catch( NullValueException nex ) {
      // expected
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneAsyncTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceQuery() {
    Calendar startTime = Calendar.getInstance();

    ClockUtil.setCurrentTime(startTime.getTime());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "businessKey123");
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

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey("businessKey123").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKeyLike("business%").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKeyLike("%sinessKey123").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKeyLike("%siness%").count());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionName("The One Task Process").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionNameLike("The One Task%").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionNameLike("%One Task Process").count());
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
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().executeActivityAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().executeActivityBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().executeActivityBefore(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().executeActivityAfter(hourFromNow.getTime()).count());

    // execute jobs

    if (processEngineConfiguration.getHistoryLevel().equals(HistoryLevel.HISTORY_LEVEL_FULL)) {
      assertEquals(1, historyService.createHistoricProcessInstanceQuery().executeJobAfter(hourAgo.getTime()).count());
      assertEquals(0, historyService.createHistoricProcessInstanceQuery().executeActivityBefore(hourAgo.getTime()).count());
      assertEquals(1, historyService.createHistoricProcessInstanceQuery().executeActivityBefore(hourFromNow.getTime()).count());
      assertEquals(0, historyService.createHistoricProcessInstanceQuery().executeActivityAfter(hourFromNow.getTime()).count());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceSorting() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().count());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/superProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceSubProcess() {
    ProcessInstance superPi = runtimeService.startProcessInstanceByKey("subProcessQueryTest");
    ProcessInstance subPi = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superPi.getProcessInstanceId()).singleResult();

    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().subProcessInstanceId(subPi.getProcessInstanceId()).singleResult();
    assertNotNull(historicProcessInstance);
    assertEquals(historicProcessInstance.getId(), superPi.getId());
  }

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

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  // ACT-1098
  public void testDeleteReason() {
    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(processEngineConfiguration.getHistory())) {
      final String deleteReason = "some delete reason";
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.deleteProcessInstance(pi.getId(), deleteReason);
      HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(pi.getId()).singleResult();
      assertEquals(deleteReason, hpi.getDeleteReason());
    }
  }

  @Deployment
  public void testLongProcessDefinitionKey() {
    // must be equals to attribute id of element process in process model
    final String PROCESS_DEFINITION_KEY = "myrealrealrealrealrealrealrealrealrealrealreallongprocessdefinitionkeyawesome";

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    // get HPI by process instance id
    HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(hpi);
    assertProcessEnded(hpi.getId());

    // get HPI by process definition key
    HistoricProcessInstance hpi2 = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    assertNotNull(hpi2);
    assertProcessEnded(hpi2.getId());

    // check we got the same HPIs
    assertEquals(hpi.getId(), hpi2.getId());

  }

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

  public void testQueryByInvalidCaseInstanceId() {
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    query.caseInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

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

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testStartActivityId-super.bpmn20.xml",
      "org/camunda/bpm/engine/test/history/HistoricProcessInstanceTest.testAsyncStartActivityId-sub.bpmn20.xml"
  })
  public void testAsyncStartActivityId() {
    // given
    runtimeService.startProcessInstanceByKey("super");

    // when
    executeAvailableJobs();

    // then
    HistoricProcessInstance hpi = historyService
        .createHistoricProcessInstanceQuery()
        .processDefinitionKey("sub")
        .singleResult();

    assertEquals("theSubStart", hpi.getStartActivityId());

  }

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

  public void testQueryByInvalidSuperCaseInstanceId() {
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    query.superCaseInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

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

  public void testQueryByInvalidSubCaseInstanceId() {
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    query.subCaseInstanceId("invalid");

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    query.caseInstanceId(null);

    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

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
  public void testHistoricProcInstExecuteActivityAfter() {
    // given
    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar hourFromNow = (Calendar) now.clone();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    runtimeService.startProcessInstanceByKey("proc");

    //when query historic process instance which has executed an activity after the start time
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executeActivityAfter(now.getTime()).singleResult();

    //then query returns result
    assertNotNull(historicProcessInstance);

    //when query historic proc inst with execute activity after a hour of the starting time
    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executeActivityAfter(hourFromNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }

  @Test
  public void testHistoricProcInstExecuteActivityBefore() {
    // given
    Calendar now = Calendar.getInstance();
    ClockUtil.setCurrentTime(now.getTime());
    BpmnModelInstance model = Bpmn.createExecutableProcess("proc").startEvent().endEvent().done();
    deployment(model);

    Calendar hourBeforeNow = (Calendar) now.clone();
    hourBeforeNow.add(Calendar.HOUR, -1);

    runtimeService.startProcessInstanceByKey("proc");

    //when query historic process instance which has executed an activity before the start time
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executeActivityBefore(now.getTime()).singleResult();

    //then query returns result, since the query is less-then-equal
    assertNotNull(historicProcessInstance);

    //when query historic proc inst which executes an activity an hour before the starting time
    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executeActivityBefore(hourBeforeNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }


  @Test
  public void testHistoricProcInstExecuteActivityWithTwoProcInsts() {
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
                                                       .executeActivityAfter(hourBeforeNow.getTime())
                                                       .executeActivityBefore(now.getTime()).list();

    //then two historic process instance have to be returned
    assertEquals(2, list.size());

    //when query execute activity after an half hour before now
    Calendar halfHour = (Calendar) now.clone();
    halfHour.add(Calendar.MINUTE, -30);
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .executeActivityAfter(halfHour.getTime()).singleResult();

    //then only the latest historic process instance is returned
    assertNotNull(historicProcessInstance);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcInstExecuteJobAfter() {
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
      .executeJobAfter(now.getTime()).singleResult();

    //then query returns only a single process instance
    assertNotNull(historicProcessInstance);

    //when query historic proc inst with execute job after a hour of the starting time
    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executeJobAfter(hourFromNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }


  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcInstExecuteJobBefore() {
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
      .executeJobBefore(now.getTime()).singleResult();

    //then query returns only a single process instance since before is less-then-equal
    assertNotNull(historicProcessInstance);

    //when query historic proc inst with execute job before an hour of the starting time
    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().executeJobBefore(hourBeforeNow.getTime()).singleResult();

    //then query returns no result
    assertNull(historicProcessInstance);
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testHistoricProcInstExecuteJobWithTwoProcInsts() {
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

    //when query execute job between now and an hour ago
    List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
      .executeJobAfter(hourBeforeNow.getTime())
      .executeJobBefore(now.getTime()).list();

    //then the two async historic process instance have to be returned
    assertEquals(2, list.size());

    //when query execute activity after an half hour before now
    Calendar halfHour = (Calendar) now.clone();
    halfHour.add(Calendar.MINUTE, -30);
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
      .executeJobAfter(halfHour.getTime()).singleResult();

    //then only the latest async historic process instance is returned
    assertNotNull(historicProcessInstance);
  }
}
