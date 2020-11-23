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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 *
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricIncidentQueryTest {

  public static String PROCESS_DEFINITION_KEY = "oneFailingServiceTaskProcess";
  public static BpmnModelInstance FAILING_SERVICE_TASK_MODEL  = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
    .startEvent("start")
    .serviceTask("task")
      .camundaAsyncBefore()
      .camundaClass(FailingDelegate.class.getName())
    .endEvent("end")
    .done();

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testHelper);

  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testHistoricIncidentQueryCreateEndAfterBefore() {

    // given
    // 1 failed instance of "oneFailingServiceTaskProcess"
    startProcessInstance(PROCESS_DEFINITION_KEY);
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    // one incident of each of the following processes
    testHelper.deploy(Bpmn.createExecutableProcess("proc1").startEvent().userTask().endEvent().done());
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("proc1");
    Incident incident2 = runtimeService.createIncident("foo", instance2.getId(), "a");
    // resolve incident2
    runtimeService.resolveIncident(incident2.getId());

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    // createTime
    assertEquals(0, query.createTimeBefore(hourAgo.getTime()).count());
    assertEquals(2, query.createTimeBefore(hourFromNow.getTime()).count());
    assertEquals(2, query.createTimeAfter(hourAgo.getTime()).count());
    assertEquals(0, query.createTimeAfter(hourFromNow.getTime()).count());
    assertEquals(2, query.createTimeBefore(hourFromNow.getTime()).createTimeAfter(hourAgo.getTime()).count());

    //endTime
    assertEquals(0, query.endTimeBefore(hourAgo.getTime()).count());
    assertEquals(1, query.endTimeBefore(hourFromNow.getTime()).count());
    assertEquals(1, query.endTimeAfter(hourAgo.getTime()).count());
    assertEquals(0, query.endTimeAfter(hourFromNow.getTime()).count());
    assertEquals(1, query.endTimeBefore(hourFromNow.getTime()).endTimeAfter(hourAgo.getTime()).count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByIncidentId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String incidentId = historyService.createHistoricIncidentQuery().singleResult().getId();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .incidentId(incidentId);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByInvalidIncidentId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.incidentId("invalid").list().size());
    assertEquals(0, query.incidentId("invalid").count());

    try {
      query.incidentId(null);
      fail("It was possible to set a null value as incidentId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByIncidentType() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .incidentType(Incident.FAILED_JOB_HANDLER_TYPE);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidIncidentType() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.incidentType("invalid").list().size());
    assertEquals(0, query.incidentType("invalid").count());

    try {
      query.incidentType(null);
      fail("It was possible to set a null value as incidentType.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByIncidentMessage() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .incidentMessage("exception0");

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidIncidentMessage() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.incidentMessage("invalid").list().size());
    assertEquals(0, query.incidentMessage("invalid").count());

    try {
      query.incidentMessage(null);
      fail("It was possible to set a null value as incidentMessage.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByIncidentMessageLike() {

    startProcessInstance(PROCESS_DEFINITION_KEY);
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.incidentMessageLike("exception").list().size());
    assertEquals(1, query.incidentMessageLike("exception%").list().size());
    assertEquals(1, query.incidentMessageLike("%xception%").list().size());
  }


  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByProcessDefinitionId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    ProcessInstance pi = runtimeService.createProcessInstanceQuery().singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .processDefinitionId(pi.getProcessDefinitionId());

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidProcessDefinitionId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.processDefinitionId("invalid").list().size());
    assertEquals(0, query.processDefinitionId("invalid").count());

    try {
      query.processDefinitionId(null);
      fail("It was possible to set a null value as processDefinitionId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByProcessDefinitionKey() {
    // given
    // 1 failed instance of "oneFailingServiceTaskProcess"
    startProcessInstance(PROCESS_DEFINITION_KEY);

    // one incident of each of the following processes
    testHelper.deploy(Bpmn.createExecutableProcess("proc1").startEvent().userTask().endEvent().done());
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("proc1");
    Incident incident2 = runtimeService.createIncident("foo", instance2.getId(), "a");

    testHelper.deploy(Bpmn.createExecutableProcess("proc2").startEvent().userTask().endEvent().done());
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("proc2");
    Incident incident3 = runtimeService.createIncident("foo", instance3.getId(), "b");

    // when
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();
    List<HistoricIncident> incidents = query.processDefinitionKeyIn("proc1", "proc2")
        .orderByConfiguration()
        .asc()
        .list();

    // then
    assertThat(incidents).hasSize(2);
    assertThat(incidents.get(0).getId()).isEqualTo(incident2.getId());
    assertThat(incidents.get(1).getId()).isEqualTo(incident3.getId());

    assertEquals(0, query.processDefinitionKey("proc").list().size());
    assertEquals(1, query.processDefinitionKey("proc1").list().size());
    assertEquals(1, query.processDefinitionKey("proc2").list().size());
  }

  @Test
  public void testQueryByInvalidProcessDefinitionKeys() {
    // given
    IncidentQuery incidentQuery = runtimeService.createIncidentQuery();

    // when/then
    assertThatThrownBy(() -> incidentQuery.processDefinitionKeyIn((String[]) null))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testQueryByOneInvalidProcessDefinitionKey() {
    // given
    IncidentQuery incidentQuery = runtimeService.createIncidentQuery();

    // when/then
    assertThatThrownBy(() -> incidentQuery.processDefinitionKeyIn((String) null))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByProcessInstanceId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    ProcessInstance pi = runtimeService.createProcessInstanceQuery().singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .processInstanceId(pi.getId());

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidProcessInstanceId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.processInstanceId("invalid").list().size());
    assertEquals(0, query.processInstanceId("invalid").count());

    try {
      query.processInstanceId(null);
      fail("It was possible to set a null value as processInstanceId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByExecutionId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    ProcessInstance pi = runtimeService.createProcessInstanceQuery().singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .executionId(pi.getId());

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidExecutionId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.executionId("invalid").list().size());
    assertEquals(0, query.executionId("invalid").count());

    try {
      query.executionId(null);
      fail("It was possible to set a null value as executionId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByActivityId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .activityId("theServiceTask");

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByInvalidActivityId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.activityId("invalid").list().size());
    assertEquals(0, query.activityId("invalid").count());

    try {
      query.activityId(null);
      fail("It was possible to set a null value as activityId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByFailedActivityId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .failedActivityId("theServiceTask");

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByFailedInvalidActivityId() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.failedActivityId("invalid").list().size());
    assertEquals(0, query.failedActivityId("invalid").count());

    try {
      query.failedActivityId(null);
      fail("It was possible to set a null value as failedActivityId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricIncidentQueryTest.testQueryByCauseIncidentId.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByCauseIncidentId() {
    startProcessInstance("process");

    String processInstanceId = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .singleResult()
        .getId();

    Incident incident = runtimeService.createIncidentQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .causeIncidentId(incident.getId());

    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
  }

  @Test
  public void testQueryByInvalidCauseIncidentId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.causeIncidentId("invalid").list().size());
    assertEquals(0, query.causeIncidentId("invalid").count());

    try {
      query.causeIncidentId(null);
      fail("It was possible to set a null value as causeIncidentId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricIncidentQueryTest.testQueryByCauseIncidentId.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByRootCauseIncidentId() {
    startProcessInstance("process");

    String processInstanceId = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .singleResult()
        .getId();

    Incident incident = runtimeService.createIncidentQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .rootCauseIncidentId(incident.getId());

    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
  }

  @Test
  public void testQueryByInvalidRootCauseIncidentId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.rootCauseIncidentId("invalid").list().size());
    assertEquals(0, query.rootCauseIncidentId("invalid").count());

    try {
      query.rootCauseIncidentId(null);
      fail("It was possible to set a null value as rootCauseIncidentId.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByConfiguration() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String configuration = managementService.createJobQuery().singleResult().getId();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .configuration(configuration);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidConfigurationId() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(0, query.configuration("invalid").list().size());
    assertEquals(0, query.configuration("invalid").count());

    try {
      query.configuration(null);
      fail("It was possible to set a null value as configuration.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByOpen() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .open();

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidOpen() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    try {
      query.open().open();
      fail("It was possible to set a the open flag twice.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByResolved() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 1);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .resolved();

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidResolved() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    try {
      query.resolved().resolved();
      fail("It was possible to set a the resolved flag twice.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryByDeleted() {
    startProcessInstance(PROCESS_DEFINITION_KEY);

    String processInstanceId = runtimeService.createProcessInstanceQuery().singleResult().getId();
    runtimeService.deleteProcessInstance(processInstanceId, null);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
        .deleted();

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByInvalidDeleted() {
    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    try {
      query.deleted().deleted();
      fail("It was possible to set a the deleted flag twice.");
    } catch (ProcessEngineException e) { }
  }

  @Test
  public void testQueryByJobDefinitionId() {
    String processDefinitionId1 = testHelper.deployAndGetDefinition(FAILING_SERVICE_TASK_MODEL).getId();
    String processDefinitionId2 = testHelper.deployAndGetDefinition(FAILING_SERVICE_TASK_MODEL).getId();

    runtimeService.startProcessInstanceById(processDefinitionId1);
    runtimeService.startProcessInstanceById(processDefinitionId2);
    testHelper.executeAvailableJobs();

    String jobDefinitionId1 = managementService.createJobQuery()
      .processDefinitionId(processDefinitionId1)
      .singleResult().getJobDefinitionId();
    String jobDefinitionId2 = managementService.createJobQuery()
      .processDefinitionId(processDefinitionId2)
      .singleResult().getJobDefinitionId();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
      .jobDefinitionIdIn(jobDefinitionId1, jobDefinitionId2);

    assertEquals(2, query.list().size());
    assertEquals(2, query.count());

    query = historyService.createHistoricIncidentQuery()
      .jobDefinitionIdIn(jobDefinitionId1);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    query = historyService.createHistoricIncidentQuery()
      .jobDefinitionIdIn(jobDefinitionId2);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByUnknownJobDefinitionId() {
    String processDefinitionId = testHelper.deployAndGetDefinition(FAILING_SERVICE_TASK_MODEL).getId();

    runtimeService.startProcessInstanceById(processDefinitionId);
    testHelper.executeAvailableJobs();

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery()
      .jobDefinitionIdIn("unknown");

    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  @Test
  public void testQueryByNullJobDefinitionId() {
    try {
      historyService.createHistoricIncidentQuery()
        .jobDefinitionIdIn((String) null);
      fail("Should fail");
    }
    catch (NullValueException e) {
      assertThat(e.getMessage()).contains("jobDefinitionIds contains null value");
    }
  }

  @Test
  public void testQueryByNullJobDefinitionIds() {
    try {
      historyService.createHistoricIncidentQuery()
        .jobDefinitionIdIn((String[]) null);
      fail("Should fail");
    }
    catch (NullValueException e) {
      assertThat(e.getMessage()).contains("jobDefinitionIds is null");
    }
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQueryPaging() {
    startProcessInstances(PROCESS_DEFINITION_KEY, 4);

    HistoricIncidentQuery query = historyService.createHistoricIncidentQuery();

    assertEquals(4, query.listPage(0, 4).size());
    assertEquals(1, query.listPage(2, 1).size());
    assertEquals(2, query.listPage(1, 2).size());
    assertEquals(3, query.listPage(1, 4).size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQuerySorting() {
    startProcessInstances(PROCESS_DEFINITION_KEY, 4);

    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByCreateTime().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByEndTime().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentType().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByExecutionId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByActivityId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessDefinitionKey().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByCauseIncidentId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByRootCauseIncidentId().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByConfiguration().asc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentState().asc().list().size());

    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByCreateTime().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByEndTime().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentType().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByExecutionId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByActivityId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessDefinitionKey().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByCauseIncidentId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByRootCauseIncidentId().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByConfiguration().desc().list().size());
    assertEquals(4, historyService.createHistoricIncidentQuery().orderByIncidentState().desc().list().size());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml"})
  public void testQuerySortingByIncidentMessage()
  {
    // given
    startProcessInstances(PROCESS_DEFINITION_KEY, 4);

    // when
    List<HistoricIncident> ascending = historyService.createHistoricIncidentQuery().orderByIncidentMessage().asc().list();
    List<HistoricIncident> descending = historyService.createHistoricIncidentQuery().orderByIncidentMessage().desc().list();

    // then
    assertThat(ascending).extracting("incidentMessage")
      .containsExactly("exception0", "exception1", "exception2", "exception3");
    assertThat(descending).extracting("incidentMessage")
      .containsExactly("exception3", "exception2", "exception1", "exception0");
  }

  protected void startProcessInstance(String key) {
    startProcessInstances(key, 1);
  }

  protected void startProcessInstances(String key, int numberOfInstances) {

    for (int i = 0; i < numberOfInstances; i++) {
      Map<String, Object> variables = Collections.<String, Object>singletonMap("message", "exception" + i);

      runtimeService.startProcessInstanceByKey(key, i + "", variables);
    }

    testHelper.executeAvailableJobs();
  }
}
