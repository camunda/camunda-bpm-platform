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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.impl.migration.validation.instruction.ConditionalEventUpdateEventTriggerValidator.MIGRATION_CONDITIONAL_VALIDATION_ERROR_MSG;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.ConditionalModels.CONDITION_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author roman.smirnov
 */
public class IncidentQueryTest {

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

  private List<String> processInstanceIds;

  protected RuntimeService runtimeService;
  protected ManagementService managementService;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
  }

  /**
   * Setup starts 4 process instances of oneFailingServiceTaskProcess.
   */
  @Before
  public void startProcessInstances() throws Exception {
    testHelper.deploy(FAILING_SERVICE_TASK_MODEL);

    processInstanceIds = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Map<String, Object> variables = Collections.<String, Object>singletonMap("message", "exception" + i);

      processInstanceIds.add(engineRule.getRuntimeService()
        .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "", variables).getId()
      );
    }

    testHelper.executeAvailableJobs();
  }

  @Test
  public void testIncidentQueryIncidentTimestampAfterBefore() {

    IncidentQuery query = runtimeService.createIncidentQuery();

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    assertEquals(0, query.incidentTimestampBefore(hourAgo.getTime()).count());
    assertEquals(4, query.incidentTimestampBefore(hourFromNow.getTime()).count());
    assertEquals(4, query.incidentTimestampAfter(hourAgo.getTime()).count());
    assertEquals(0, query.incidentTimestampAfter(hourFromNow.getTime()).count());
    assertEquals(4, query.incidentTimestampBefore(hourFromNow.getTime())
                                  .incidentTimestampAfter(hourAgo.getTime()).count());
  }

  @Test
  public void testQuery() {
    IncidentQuery query = runtimeService.createIncidentQuery();
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  @Test
  public void testQueryByIncidentType() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentType(Incident.FAILED_JOB_HANDLER_TYPE);
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  @Test
  public void testQueryByInvalidIncidentType() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentType("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  @Test
  public void testQueryByIncidentMessage() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentMessage("exception0");
    assertEquals(1, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());
  }

  @Test
  public void testQueryByInvalidIncidentMessage() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentMessage("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  @Test
  public void testQueryByIncidentMessageLike() {
    IncidentQuery query = runtimeService.createIncidentQuery();
    assertEquals(0, query.incidentMessageLike("exception").list().size());
    assertEquals(4, query.incidentMessageLike("exception%").list().size());
    assertEquals(1, query.incidentMessageLike("%xception1").list().size());
  }

  @Test
  public void testQueryByProcessDefinitionId() {
    String processDefinitionId = engineRule.getRepositoryService().createProcessDefinitionQuery().singleResult().getId();

    IncidentQuery query = runtimeService.createIncidentQuery().processDefinitionId(processDefinitionId);
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  @Test
  public void testQueryByInvalidProcessDefinitionId() {
    IncidentQuery query = runtimeService.createIncidentQuery().processDefinitionId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  @Test
  public void testQueryByProcessDefinitionKeys() {
    // given
    // 4 failed instances of "process"

    // one incident of each of the following processes
    testHelper.deploy(Bpmn.createExecutableProcess("proc1").startEvent().userTask().endEvent().done());
    ProcessInstance instance5 = runtimeService.startProcessInstanceByKey("proc1");
    Incident incident5 = runtimeService.createIncident("foo", instance5.getId(), "a");

    testHelper.deploy(Bpmn.createExecutableProcess("proc2").startEvent().userTask().endEvent().done());
    ProcessInstance instance6 = runtimeService.startProcessInstanceByKey("proc2");
    Incident incident6 = runtimeService.createIncident("foo", instance6.getId(), "b");

    // when
    List<Incident> incidents = runtimeService.createIncidentQuery()
        .processDefinitionKeyIn("proc1", "proc2")
        .orderByConfiguration()
        .asc()
        .list();

    // then
    assertThat(incidents).hasSize(2);
    assertThat(incidents.get(0).getId()).isEqualTo(incident5.getId());
    assertThat(incidents.get(1).getId()).isEqualTo(incident6.getId());
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
  public void testQueryByProcessInstanceId() {
    IncidentQuery query = runtimeService.createIncidentQuery().processInstanceId(processInstanceIds.get(0));

    assertEquals(1, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());

    Incident incident = query.singleResult();
    assertNotNull(incident);
  }

  @Test
  public void testQueryByInvalidProcessInstanceId() {
    IncidentQuery query = runtimeService.createIncidentQuery().processInstanceId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  @Test
  public void testQueryByIncidentId() {
    Incident incident= runtimeService.createIncidentQuery().processInstanceId(processInstanceIds.get(0)).singleResult();
    assertNotNull(incident);

    IncidentQuery query = runtimeService.createIncidentQuery().incidentId(incident.getId());

    assertEquals(1, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());
  }

  @Test
  public void testQueryByInvalidIncidentId() {
    IncidentQuery query = runtimeService.createIncidentQuery().incidentId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  @Test
  public void testQueryByExecutionId() {
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceIds.get(0)).singleResult();
    assertNotNull(execution);

    IncidentQuery query = runtimeService.createIncidentQuery().executionId(execution.getId());

    assertEquals(1, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());
  }

  @Test
  public void testQueryByInvalidExecutionId() {
    IncidentQuery query = runtimeService.createIncidentQuery().executionId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  @Test
  public void testQueryByActivityId() {
    IncidentQuery query = runtimeService.createIncidentQuery().activityId("task");
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  @Test
  public void testQueryByInvalidActivityId() {
    IncidentQuery query = runtimeService.createIncidentQuery().activityId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  @Test
  public void testQueryByFailedActivityId() {
    IncidentQuery query = runtimeService.createIncidentQuery().failedActivityId("task");
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  @Test
  public void testQueryByInvalidFailedActivityId() {
    IncidentQuery query = runtimeService.createIncidentQuery().failedActivityId("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  @Test
  public void testQueryByConfiguration() {
    String jobId = managementService.createJobQuery().processInstanceId(processInstanceIds.get(0)).singleResult().getId();

    IncidentQuery query = runtimeService.createIncidentQuery().configuration(jobId);
    assertEquals(1, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(1, incidents.size());
  }

  @Test
  public void testQueryByInvalidConfiguration() {
    IncidentQuery query = runtimeService.createIncidentQuery().configuration("invalid");

    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());

    Incident incident = query.singleResult();
    assertNull(incident);
  }

  @Test
  public void testQueryByCauseIncidentIdEqualsNull() {
    IncidentQuery query = runtimeService.createIncidentQuery().causeIncidentId(null);
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  @Test
  public void testQueryByInvalidCauseIncidentId() {
    IncidentQuery query = runtimeService.createIncidentQuery().causeIncidentId("invalid");
    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());
    assertEquals(0, incidents.size());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/IncidentQueryTest.testQueryByCauseIncidentId.bpmn"})
  public void testQueryByCauseIncidentId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callFailingProcess");

    testHelper.executeAvailableJobs();

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);

    Incident causeIncident = runtimeService.createIncidentQuery().processInstanceId(subProcessInstance.getId()).singleResult();
    assertNotNull(causeIncident);

    IncidentQuery query = runtimeService.createIncidentQuery().causeIncidentId(causeIncident.getId());
    assertEquals(2, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(2, incidents.size());
  }

  @Test
  public void testQueryByRootCauseIncidentIdEqualsNull() {
    IncidentQuery query = runtimeService.createIncidentQuery().rootCauseIncidentId(null);
    assertEquals(4, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(4, incidents.size());
  }

  @Test
  public void testQueryByRootInvalidCauseIncidentId() {
    IncidentQuery query = runtimeService.createIncidentQuery().rootCauseIncidentId("invalid");
    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertTrue(incidents.isEmpty());
    assertEquals(0, incidents.size());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/IncidentQueryTest.testQueryByRootCauseIncidentId.bpmn",
      "org/camunda/bpm/engine/test/api/runtime/IncidentQueryTest.testQueryByCauseIncidentId.bpmn"})
  public void testQueryByRootCauseIncidentId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callFailingCallActivity");

    testHelper.executeAvailableJobs();

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);

    ProcessInstance failingSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(failingSubProcessInstance.getId()).singleResult();
    assertNotNull(incident);

    IncidentQuery query = runtimeService.createIncidentQuery().rootCauseIncidentId(incident.getId());
    assertEquals(3, query.count());

    List<Incident> incidents = query.list();
    assertFalse(incidents.isEmpty());
    assertEquals(3, incidents.size());

    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {
      // Exception is expected
    }

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

    IncidentQuery query = runtimeService.createIncidentQuery()
      .jobDefinitionIdIn(jobDefinitionId1, jobDefinitionId2);

    assertEquals(2, query.list().size());
    assertEquals(2, query.count());

    query = runtimeService.createIncidentQuery()
      .jobDefinitionIdIn(jobDefinitionId1);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    query = runtimeService.createIncidentQuery()
      .jobDefinitionIdIn(jobDefinitionId2);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  @Test
  public void testQueryByUnknownJobDefinitionId() {
    IncidentQuery query = runtimeService.createIncidentQuery().jobDefinitionIdIn("unknown");
    assertEquals(0, query.count());

    List<Incident> incidents = query.list();
    assertEquals(0, incidents.size());
  }

  @Test
  public void testQueryByNullJobDefinitionId() {
    try {
      runtimeService.createIncidentQuery()
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
      runtimeService.createIncidentQuery()
        .jobDefinitionIdIn((String[]) null);
      fail("Should fail");
    }
    catch (NullValueException e) {
      assertThat(e.getMessage()).contains("jobDefinitionIds is null");
    }
  }

  @Test
  public void testQueryPaging() {
    assertEquals(4, runtimeService.createIncidentQuery().listPage(0, 4).size());
    assertEquals(1, runtimeService.createIncidentQuery().listPage(2, 1).size());
    assertEquals(2, runtimeService.createIncidentQuery().listPage(1, 2).size());
    assertEquals(3, runtimeService.createIncidentQuery().listPage(1, 4).size());
  }

  @Test
  public void testQuerySorting() {
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentTimestamp().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentType().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByExecutionId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByActivityId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByCauseIncidentId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByRootCauseIncidentId().asc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByConfiguration().asc().list().size());

    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentTimestamp().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByIncidentType().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByExecutionId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByActivityId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByCauseIncidentId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByRootCauseIncidentId().desc().list().size());
    assertEquals(4, runtimeService.createIncidentQuery().orderByConfiguration().desc().list().size());

  }

  @Test
  public void testQuerySortingByIncidentMessage()
  {
    // given

    // when
    List<Incident> ascending = runtimeService.createIncidentQuery().orderByIncidentMessage().asc().list();
    List<Incident> descending = runtimeService.createIncidentQuery().orderByIncidentMessage().desc().list();

    // then
    assertThat(ascending).extracting("incidentMessage")
      .containsExactly("exception0", "exception1", "exception2", "exception3");
    assertThat(descending).extracting("incidentMessage")
      .containsExactly("exception3", "exception2", "exception1", "exception0");
  }
}
