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
package org.camunda.bpm.cockpit.plugin.base;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.IncidentQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.resources.IncidentRestService;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author roman.smirnov
 */
public class IncidentRestServiceTest extends AbstractCockpitPluginTest {

  private ProcessEngine processEngine;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  private RepositoryService repositoryService;
  private RuntimeService runtimeService;
  private IncidentRestService resource;
  protected IdentityService identityService;

  @Before
  public void setUp() throws Exception {
    super.before();

    processEngine = getProcessEngine();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine
      .getProcessEngineConfiguration();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    identityService = processEngine.getIdentityService();

    resource = new IncidentRestService(processEngine.getName());
  }

  @After
  public void clearAuthentication() {
    identityService.clearAuthentication();
  }

  @After
  public void resetQueryMaxResultsLimit() {
    processEngineConfiguration.setQueryMaxResultsLimit(Integer.MAX_VALUE);
  }

  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn"
  })
  public void testQueryByProcessInstanceId() {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");

    executeAvailableJobs();

    String incidentId = runtimeService.createIncidentQuery().processInstanceId(processInstance1.getId()).singleResult().getId();

    runtimeService.setAnnotationForIncidentById(incidentId, "an Annotation");

    String[] processInstanceIds= {processInstance1.getId()};

    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setProcessInstanceIdIn(processInstanceIds);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    IncidentDto incident = result.get(0);

    assertThat(incident.getId()).isNotNull();
    assertThat(incident.getIncidentType()).isEqualTo(Incident.FAILED_JOB_HANDLER_TYPE);
    assertThat(incident.getIncidentMessage()).isEqualTo("I am failing!");
    assertThat(incident.getIncidentTimestamp()).isNotNull();
    assertThat(incident.getActivityId()).isEqualTo("ServiceTask_1");
    assertThat(incident.getFailedActivityId()).isEqualTo("ServiceTask_1");
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getProcessDefinitionId()).isEqualTo(processInstance1.getProcessDefinitionId());
    assertThat(incident.getExecutionId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getConfiguration()).isNotNull();
    assertThat(incident.getAnnotation()).isEqualTo("an Annotation");
    assertThat(incident.getCauseIncidentId()).isEqualTo(incident.getId());
    assertThat(incident.getCauseIncidentProcessInstanceId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getCauseIncidentProcessDefinitionId()).isEqualTo(processInstance1.getProcessDefinitionId());
    assertThat(incident.getCauseIncidentActivityId()).isEqualTo("ServiceTask_1");
    assertThat(incident.getCauseIncidentFailedActivityId()).isEqualTo("ServiceTask_1");
    assertThat(incident.getRootCauseIncidentId()).isEqualTo(incident.getId());
    assertThat(incident.getRootCauseIncidentProcessInstanceId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getRootCauseIncidentProcessDefinitionId()).isEqualTo(processInstance1.getProcessDefinitionId());
    assertThat(incident.getRootCauseIncidentActivityId()).isEqualTo("ServiceTask_1");
    assertThat(incident.getRootCauseIncidentFailedActivityId()).isEqualTo("ServiceTask_1");
    assertThat(incident.getRootCauseIncidentConfiguration()).isNotNull();
    assertThat(incident.getRootCauseIncidentMessage()).isEqualTo("I am failing!");
  }

  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn"
  })
  public void testQueryByProcessInstanceIds() {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("FailingProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("FailingProcess");

    executeAvailableJobs();

    String[] processInstanceIds= {processInstance1.getId(), processInstance2.getId()};

    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setProcessInstanceIdIn(processInstanceIds);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @Deployment(resources = {
    "processes/process-with-two-parallel-failing-services.bpmn"
  })
  public void testQueryByActivityId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processWithTwoParallelFailingServices");

    executeAvailableJobs();

    String[] activityIds= {"theServiceTask1"};

    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setActivityIdIn(activityIds);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    IncidentDto incident = result.get(0);

    assertThat(incident.getId()).isNotNull();
    assertThat(incident.getIncidentType()).isEqualTo(Incident.FAILED_JOB_HANDLER_TYPE);
    assertThat(incident.getIncidentMessage()).isEqualTo("I am failing!");
    assertThat(incident.getIncidentTimestamp()).isNotNull();
    assertThat(incident.getActivityId()).isEqualTo("theServiceTask1");
    assertThat(incident.getFailedActivityId()).isEqualTo("theServiceTask1");
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(incident.getExecutionId()).isNotNull();
    assertThat(incident.getConfiguration()).isNotNull();
    assertThat(incident.getCauseIncidentId()).isEqualTo(incident.getId());
    assertThat(incident.getCauseIncidentProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getCauseIncidentProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(incident.getCauseIncidentActivityId()).isEqualTo("theServiceTask1");
    assertThat(incident.getCauseIncidentFailedActivityId()).isEqualTo("theServiceTask1");
    assertThat(incident.getRootCauseIncidentId()).isEqualTo(incident.getId());
    assertThat(incident.getRootCauseIncidentProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getRootCauseIncidentProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(incident.getRootCauseIncidentActivityId()).isEqualTo("theServiceTask1");
    assertThat(incident.getRootCauseIncidentFailedActivityId()).isEqualTo("theServiceTask1");
    assertThat(incident.getRootCauseIncidentConfiguration()).isNotNull();
    assertThat(incident.getRootCauseIncidentMessage()).isEqualTo("I am failing!");
  }

  @Test
  @Deployment(resources = {
    "processes/process-with-two-parallel-failing-services.bpmn"
  })
  public void testQueryByActivityIds() {
    runtimeService.startProcessInstanceByKey("processWithTwoParallelFailingServices");

    executeAvailableJobs();

    String[] activityIds= {"theServiceTask1", "theServiceTask2"};

    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setActivityIdIn(activityIds);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn"
  })
  public void testQueryByProcessInstanceIdAndActivityId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("FailingProcess");

    executeAvailableJobs();

    String[] processInstanceIds= {processInstance.getId()};
    String[] activityIds= {"ServiceTask_1"};

    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setProcessInstanceIdIn(processInstanceIds);
    queryParameter.setActivityIdIn(activityIds);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
  }

  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn",
    "processes/process-with-two-parallel-failing-services.bpmn"
  })
  public void testQueryByProcessInstanceIdAndActivityId_ShouldReturnEmptyList() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("processWithTwoParallelFailingServices");

    executeAvailableJobs();

    String[] processInstanceIds= {processInstance.getId()};
    String[] activityIds= {"theServiceTask1"}; // is an activity id in "processWithTwoParallelFailingServices"

    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setProcessInstanceIdIn(processInstanceIds);
    queryParameter.setActivityIdIn(activityIds);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);

    assertThat(result).isEmpty();
  }

  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn",
    "processes/call-activity.bpmn",
    "processes/nested-call-activity.bpmn"
  })
  public void testQueryWithNestedIncidents() {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("NestedCallActivity");

    executeAvailableJobs();

    ProcessInstance processInstance2 = runtimeService.createProcessInstanceQuery().processDefinitionKey("CallActivity").singleResult();
    ProcessInstance processInstance3 = runtimeService.createProcessInstanceQuery().processDefinitionKey("FailingProcess").singleResult();

    String[] processInstanceIds= {processInstance1.getId()};

    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setProcessInstanceIdIn(processInstanceIds);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    IncidentDto incident = result.get(0);

    assertThat(incident.getId()).isNotNull();
    assertThat(incident.getIncidentType()).isEqualTo(Incident.FAILED_JOB_HANDLER_TYPE);
    assertThat(incident.getIncidentMessage()).isNull();
    assertThat(incident.getIncidentTimestamp()).isNotNull();
    assertThat(incident.getActivityId()).isEqualTo("CallActivity_1");
    assertThat(incident.getFailedActivityId()).isEqualTo("CallActivity_1");
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getProcessDefinitionId()).isEqualTo(processInstance1.getProcessDefinitionId());
    assertThat(incident.getExecutionId()).isNotNull();
    assertThat(incident.getConfiguration()).isNull();

    assertThat(incident.getCauseIncidentId()).isNotEqualTo(incident.getId());
    assertThat(incident.getCauseIncidentProcessInstanceId()).isEqualTo(processInstance2.getId());
    assertThat(incident.getCauseIncidentProcessDefinitionId()).isEqualTo(processInstance2.getProcessDefinitionId());
    assertThat(incident.getCauseIncidentActivityId()).isEqualTo("CallActivity_1");
    assertThat(incident.getCauseIncidentFailedActivityId()).isEqualTo("CallActivity_1");

    assertThat(incident.getRootCauseIncidentId()).isNotEqualTo(incident.getId());
    assertThat(incident.getRootCauseIncidentProcessInstanceId()).isEqualTo(processInstance3.getId());
    assertThat(incident.getRootCauseIncidentProcessDefinitionId()).isEqualTo(processInstance3.getProcessDefinitionId());
    assertThat(incident.getRootCauseIncidentActivityId()).isEqualTo("ServiceTask_1");
    assertThat(incident.getRootCauseIncidentFailedActivityId()).isEqualTo("ServiceTask_1");
    assertThat(incident.getRootCauseIncidentConfiguration()).isNotNull();
    assertThat(incident.getRootCauseIncidentMessage()).isEqualTo("I am failing!");
  }

  @Test
  @Deployment(resources = {
    "processes/process-with-two-parallel-failing-services.bpmn"
  })
  public void testQueryPaginiation() {
    runtimeService.startProcessInstanceByKey("processWithTwoParallelFailingServices");

    executeAvailableJobs();

    IncidentQueryDto queryParameter = new IncidentQueryDto();

    List<IncidentDto> result = resource.queryIncidents(queryParameter, 0, 2);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);

    result = resource.queryIncidents(queryParameter, 2, 1);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    result = resource.queryIncidents(queryParameter, 4, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(6);

    result = resource.queryIncidents(queryParameter, null, 4);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(4);
  }

  @Test
  @Deployment(resources = {
      "processes/failing-process.bpmn",
      "processes/call-activity.bpmn",
      "processes/nested-call-activity.bpmn"
  })
  public void testQueryByProcessDefinitionId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("NestedCallActivity");

    executeAvailableJobs();

    String[] processDefinitionIds = { processInstance.getProcessDefinitionId() };

    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setProcessDefinitionIdIn(processDefinitionIds);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    IncidentDto incident = result.get(0);

    assertThat(incident.getId()).isNotNull();
    assertThat(incident.getIncidentType()).isEqualTo(Incident.FAILED_JOB_HANDLER_TYPE);
    assertThat(incident.getIncidentMessage()).isNull();
    assertThat(incident.getIncidentTimestamp()).isNotNull();
    assertThat(incident.getActivityId()).isEqualTo("CallActivity_1");
    assertThat(incident.getFailedActivityId()).isEqualTo("CallActivity_1");
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(incident.getExecutionId()).isNotNull();
    assertThat(incident.getConfiguration()).isNull();
  }

  @Test
  @Deployment(resources = {
      "processes/failing-process.bpmn",
      "processes/call-activity.bpmn",
      "processes/nested-call-activity.bpmn"
  })
  public void testQueryByProcessDefinitionIds() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("NestedCallActivity");

    executeAvailableJobs();

    String processDefinition2 = repositoryService.createProcessDefinitionQuery().processDefinitionKey("CallActivity").singleResult().getId();

    String[] processDefinitionIds = { processInstance.getProcessDefinitionId(), processDefinition2 };

    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setProcessDefinitionIdIn(processDefinitionIds);

    List<IncidentDto> result = resource.queryIncidents(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @Deployment(resources = {
      "processes/failing-process.bpmn",
      "processes/call-activity.bpmn",
      "processes/nested-call-activity.bpmn"
  })
  public void testQuerySorting() {
    runtimeService.startProcessInstanceByKey("NestedCallActivity");

    executeAvailableJobs();

    // asc
    verifySorting("incidentMessage", "asc", 3);
    verifySorting("incidentTimestamp", "asc", 3);
    verifySorting("incidentType", "asc", 3);
    verifySorting("activityId", "asc", 3);
    verifySorting("causeIncidentProcessInstanceId", "asc", 3);
    verifySorting("rootCauseIncidentProcessInstanceId", "asc", 3);

    // desc
    verifySorting("incidentMessage", "desc", 3);
    verifySorting("incidentTimestamp", "desc", 3);
    verifySorting("incidentType", "desc", 3);
    verifySorting("activityId", "desc", 3);
    verifySorting("causeIncidentProcessInstanceId", "desc", 3);
    verifySorting("rootCauseIncidentProcessInstanceId", "desc", 3);
  }

  @Test
  @Deployment(resources = "processes/simple-user-task-process.bpmn")
  public void testQuerySortingByIncidentMessage()
  {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("simpleUserTaskProcess");
    Incident incident1 = runtimeService.createIncident("foo", instance.getId(), null, "message1");
    Incident incident2 = runtimeService.createIncident("foo", instance.getId(), null, "message3");
    Incident incident3 = runtimeService.createIncident("foo", instance.getId(), null, "message2");

    // when
    List<IncidentDto> ascending = queryIncidents("incidentMessage", "asc");
    List<IncidentDto> descending = queryIncidents("incidentMessage", "desc");

    // then
    assertThat(ascending).extracting("id").containsExactly(incident1.getId(), incident3.getId(), incident2.getId());
    assertThat(descending).extracting("id").containsExactly(incident2.getId(), incident3.getId(), incident1.getId());
  }

  @Test
  public void shouldReturnPaginatedResult() {
    // given
    processEngineConfiguration.setQueryMaxResultsLimit(10);

    identityService.setAuthenticatedUserId("foo");

    try {
      // when
      resource.queryIncidents(new IncidentQueryDto(), 0, 10);
      // then: no exception expected
    } catch (BadUserRequestException e) {
      // then
      fail("No exception expected");
    }
  }

  @Test
  public void shouldReturnUnboundedResult_NotAuthenticated() {
    // given
    processEngineConfiguration.setQueryMaxResultsLimit(10);

    try {
      // when
      resource.queryIncidents(new IncidentQueryDto(), null, null);
      // then: no exception expected
    } catch (BadUserRequestException e) {
      // then
      fail("No exception expected");
    }
  }

  @Test
  public void shouldReturnUnboundedResult_NoLimitConfigured() {
    // given
    identityService.setAuthenticatedUserId("foo");

    try {
      // when
      resource.queryIncidents(new IncidentQueryDto(), null, null);
      // then: no exception expected
    } catch (BadUserRequestException e) {
      // then
      fail("No exception expected");
    }
  }

  @Test
  public void shouldThrowExceptionWhenMaxResultsLimitExceeded() {
    // given
    processEngineConfiguration.setQueryMaxResultsLimit(10);

    identityService.setAuthenticatedUserId("foo");

    try {
      // when
      resource.queryIncidents(new IncidentQueryDto(), 0, 11);
      fail("Exception expected!");
    } catch (BadUserRequestException e) {
      // then
      assertThat(e).hasMessage("Max results limit of 10 exceeded!");
    }
  }

  @Test
  public void shouldThrowExceptionWhenQueryUnbounded() {
    // given
    processEngineConfiguration.setQueryMaxResultsLimit(10);

    identityService.setAuthenticatedUserId("foo");

    try {
      // when
      resource.queryIncidents(new IncidentQueryDto(), null, null);
      fail("Exception expected!");
    } catch (BadUserRequestException e) {
      // then
      assertThat(e).hasMessage("An unbound number of results is forbidden!");
    }
  }

  protected List<IncidentDto> queryIncidents(String sorting, String order)
  {
    IncidentQueryDto queryParameter = new IncidentQueryDto();
    queryParameter.setSortBy(sorting);
    queryParameter.setSortOrder(order);

    return resource.queryIncidents(queryParameter, null, null);
  }

  protected void verifySorting(String sortBy, String sortOrder, int expectedResult) {
    List<IncidentDto> result = queryIncidents(sortBy, sortOrder);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(expectedResult);
  }

}
