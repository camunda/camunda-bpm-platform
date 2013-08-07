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
package org.camunda.bpm.cockpit.plugin.base;

import static org.fest.assertions.Assertions.assertThat;
import static org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto.*;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentStatisticsDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessInstanceQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.base.resources.ProcessInstanceRestService;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author roman.smirnov
 * @author nico.rehwaldt
 */
public class ProcessInstanceRestServiceTest extends AbstractCockpitPluginTest {

  private ProcessEngine processEngine;
  private RuntimeService runtimeService;
  private RepositoryService repositoryService;
  private JobExecutorHelper helper;

  private ProcessInstanceRestService resource;

  @Before
  public void setUp() throws Exception {
    super.before();

    processEngine = getProcessEngine();

    helper = new JobExecutorHelper(processEngine);

    runtimeService = processEngine.getRuntimeService();
    repositoryService = processEngine.getRepositoryService();

    resource = new ProcessInstanceRestService(processEngine.getName());
  }

  private void startProcessInstances(String processDefinitionKey, int numOfInstances) {
    for (int i = 0; i < numOfInstances; i++) {
      runtimeService.startProcessInstanceByKey(processDefinitionKey, "businessKey_" + i);
    }
  }

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQuery() {
    startProcessInstances("userTaskProcess", 3);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDefinitionId);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);
  }

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQueryCount() {
    startProcessInstances("userTaskProcess", 3);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDefinitionId);

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(3);
  }

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQueryPagination() {
    startProcessInstances("userTaskProcess", 5);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDefinitionId);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, 0, 3);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);

    result = resource.queryProcessInstances(queryParameter, 2, 3);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);

    result = resource.queryProcessInstances(queryParameter, 3, 1);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
  })
  public void testQueryWithoutAnyIncident() {
    startProcessInstances("userTaskProcess", 1);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDefinitionId);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);

    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    List<IncidentStatisticsDto> incidents = result.get(0).getIncidents();

    assertThat(incidents).isEmpty();
  }

  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn"
  })
  public void testQueryWithContainingIncidents() {
    startProcessInstances("FailingProcess", 1);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDefinitionId);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);

    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    List<IncidentStatisticsDto> incidents = result.get(0).getIncidents();

    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);

    IncidentStatisticsDto incident = incidents.get(0);

    assertThat(incident.getIncidentType()).isEqualTo("failedJob");
    assertThat(incident.getIncidentCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = {
    "processes/process-with-two-parallel-failing-services.bpmn"
  })
  public void testQueryWithMoreThanOneIncident() {
    startProcessInstances("processWithTwoParallelFailingServices", 1);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDefinitionId);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);

    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    List<IncidentStatisticsDto> incidents = result.get(0).getIncidents();

    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(3);

    for (IncidentStatisticsDto incident : incidents) {
      String incidentType = incident.getIncidentType();
      assertThat(incidentType).isNotNull();

      if (incidentType.equals("failedJob")) {
        assertThat(incident.getIncidentCount()).isEqualTo(2);
      } else if (incidentType.equals("anIncident")) {
        assertThat(incident.getIncidentCount()).isEqualTo(3);
      } else if (incidentType.equals("anotherIncident")) {
        assertThat(incident.getIncidentCount()).isEqualTo(5);
      } else {
        Assert.fail(incidentType + " not expected.");
      }

    }
  }

  @Test
  @Deployment(resources = {
    "processes/variables-process.bpmn"
  })
  public void testQueryWithBooleanVariable() {
    // given
    startProcessInstances("variableProcess", 2);

    // when
    VariableQueryParameterDto variable = createVariableParameter("varboolean", EQUALS_OPERATOR_NAME, false);

    ProcessInstanceQueryDto parameter = new ProcessInstanceQueryDto();
    parameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> results = resource.queryProcessInstances(parameter, 0, Integer.MAX_VALUE);

    // then
    assertThat(results).hasSize(0);
  }

  @Test
  @Deployment(resources = {
    "processes/variables-process.bpmn"
  })
  public void testQueryWithStringVariable() {
    // given
    startProcessInstances("variableProcess", 2);

    // when
    VariableQueryParameterDto variable = createVariableParameter("varstring", LIKE_OPERATOR_NAME, "B%");

    ProcessInstanceQueryDto parameter = new ProcessInstanceQueryDto();
    parameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> results = resource.queryProcessInstances(parameter, 0, Integer.MAX_VALUE);

    // then
    assertThat(results).hasSize(0);
  }

  @Test
  @Deployment(resources = {
    "processes/variables-process.bpmn"
  })
  public void testQueryWithFloatVariable() {
    // given
    startProcessInstances("variableProcess", 2);

    // when
    VariableQueryParameterDto variable = createVariableParameter("varfloat", EQUALS_OPERATOR_NAME, 0.0);

    ProcessInstanceQueryDto parameter = new ProcessInstanceQueryDto();
    parameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> results = resource.queryProcessInstances(parameter, 0, Integer.MAX_VALUE);

    // then
    assertThat(results).hasSize(0);
  }

  @Test
  @Deployment(resources = {
    "processes/variables-process.bpmn"
  })
  public void testQueryWithIntegerVariable() {
    // given
    startProcessInstances("variableProcess", 2);

    // when
    VariableQueryParameterDto variable = createVariableParameter("varinteger", NOT_EQUALS_OPERATOR_NAME, 12);

    ProcessInstanceQueryDto parameter = new ProcessInstanceQueryDto();
    parameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(parameter, 0, Integer.MAX_VALUE);

    // then
    assertThat(result).hasSize(0);
  }

  @Test
  @Deployment(resources = {
    "processes/variables-process.bpmn"
  })
  public void testQueryWithComplexVariableFilter() {
    // given
    startProcessInstances("variableProcess", 2);

    // when
    ProcessInstanceQueryDto parameter = new ProcessInstanceQueryDto();

    parameter.setVariables(Arrays.asList(
        createVariableParameter("varinteger", VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME, 11),
        createVariableParameter("varinteger", VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME, 12),
        createVariableParameter("varinteger", VariableQueryParameterDto.EQUALS_OPERATOR_NAME, 12),
        createVariableParameter("varboolean", VariableQueryParameterDto.EQUALS_OPERATOR_NAME, true),
        createVariableParameter("varboolean", VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME, false),
        createVariableParameter("varstring", VariableQueryParameterDto.LIKE_OPERATOR_NAME, "F%"),
        createVariableParameter("varstring", VariableQueryParameterDto.EQUALS_OPERATOR_NAME, "FOO"),
        createVariableParameter("varstring", VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME, "BAR"),
        createVariableParameter("varfloat", VariableQueryParameterDto.EQUALS_OPERATOR_NAME, 12.12),
        createVariableParameter("varfloat", VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME, 13.0),
        createVariableParameter("varfloat", VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME, 12.13)));

    List<ProcessInstanceDto> booleanProcessInstances = resource.queryProcessInstances(parameter, 0, Integer.MAX_VALUE);
    assertThat(booleanProcessInstances).hasSize(2);
  }

  @Test
  @Deployment(resources = {
    "processes/call-activity.bpmn",
    "processes/nested-call-activity.bpmn",
    "processes/failing-process.bpmn"
  })
  public void testNestedIncidents() {
    startProcessInstances("NestedCallActivity", 1);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    String nestedCallActivityId = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("NestedCallActivity")
        .singleResult()
        .getId();

    String callActivityId = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("CallActivity")
        .singleResult()
        .getId();

    String failingProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult()
        .getId();

    ProcessInstanceQueryDto queryParameter1 = new ProcessInstanceQueryDto();
    queryParameter1.setProcessDefinitionId(nestedCallActivityId);

    List<ProcessInstanceDto> nestedCallActivityInstances = resource.queryProcessInstances(queryParameter1, null, null);
    assertThat(nestedCallActivityInstances).isNotEmpty();
    assertThat(nestedCallActivityInstances).hasSize(1);

    List<IncidentStatisticsDto> nestedCallActivityIncidents = nestedCallActivityInstances.get(0).getIncidents();
    assertThat(nestedCallActivityIncidents).isNotEmpty();
    assertThat(nestedCallActivityIncidents).hasSize(1);

    IncidentStatisticsDto nestedCallActivityIncident = nestedCallActivityIncidents.get(0);
    assertThat(nestedCallActivityIncident.getIncidentType()).isEqualTo("failedJob");
    assertThat(nestedCallActivityIncident.getIncidentCount()).isEqualTo(1);

    ProcessInstanceQueryDto queryParameter2 = new ProcessInstanceQueryDto();
    queryParameter2.setProcessDefinitionId(callActivityId);

    List<ProcessInstanceDto> callActivityInstances = resource.queryProcessInstances(queryParameter2, null, null);
    assertThat(callActivityInstances).isNotEmpty();
    assertThat(callActivityInstances).hasSize(1);

    List<IncidentStatisticsDto> callActivityIncidents = callActivityInstances.get(0).getIncidents();
    assertThat(callActivityIncidents).isNotEmpty();
    assertThat(callActivityIncidents).hasSize(1);

    IncidentStatisticsDto callActivityIncident = callActivityIncidents.get(0);
    assertThat(callActivityIncident.getIncidentType()).isEqualTo("failedJob");
    assertThat(callActivityIncident.getIncidentCount()).isEqualTo(1);

    ProcessInstanceQueryDto queryParameter3 = new ProcessInstanceQueryDto();
    queryParameter3.setProcessDefinitionId(failingProcess);

    List<ProcessInstanceDto> failingProcessInstances = resource.queryProcessInstances(queryParameter3, null, null);
    assertThat(failingProcessInstances).isNotEmpty();
    assertThat(failingProcessInstances).hasSize(1);

    List<IncidentStatisticsDto> failingProcessIncidents = failingProcessInstances.get(0).getIncidents();
    assertThat(failingProcessIncidents).isNotEmpty();
    assertThat(failingProcessIncidents).hasSize(1);

    IncidentStatisticsDto failingProcessIncident = failingProcessIncidents.get(0);
    assertThat(failingProcessIncident.getIncidentType()).isEqualTo("failedJob");
    assertThat(failingProcessIncident.getIncidentCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryByBusinessKey() {
    startProcessInstances("userTaskProcess", 3);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setBusinessKey("businessKey_2");

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryByBusinessKeyCount() {
    startProcessInstances("userTaskProcess", 3);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setBusinessKey("businessKey_2");

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn",
      "processes/failing-process.bpmn"
    })
  public void testQueryByBusinessKeyWithMoreThanOneProcess() {
    startProcessInstances("userTaskProcess", 3);
    startProcessInstances("FailingProcess", 3);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setBusinessKey("businessKey_2");

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn",
      "processes/failing-process.bpmn"
    })
  public void testQueryByBusinessKeyWithMoreThanOneProcessCount() {
    startProcessInstances("userTaskProcess", 3);
    startProcessInstances("FailingProcess", 3);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setBusinessKey("businessKey_2");

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryByBusinessKeyAndProcessDefinition() {
    startProcessInstances("userTaskProcess", 3);

    ProcessDefinition userTaskProcess = repositoryService.createProcessDefinitionQuery().singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setBusinessKey("businessKey_2");
    queryParameter.setProcessDefinitionId(userTaskProcess.getId());

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryByBusinessKeyAndProcessDefinitionCount() {
    startProcessInstances("userTaskProcess", 3);

    ProcessDefinition userTaskProcess = repositoryService.createProcessDefinitionQuery().singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setBusinessKey("businessKey_2");
    queryParameter.setProcessDefinitionId(userTaskProcess.getId());

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByActivityId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    String[] activityIds = {"firstCallActivity"};
    queryParameter.setActivityIdIn(activityIds);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByActivityIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    String[] activityIds = {"firstCallActivity"};
    queryParameter.setActivityIdIn(activityIds);

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByActivityIds() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    String[] activityIds = {"firstCallActivity", "secondCallActivity"};
    queryParameter.setActivityIdIn(activityIds);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByActivityIdsCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    String[] activityIds = {"firstCallActivity", "secondCallActivity"};
    queryParameter.setActivityIdIn(activityIds);

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByActivityIdAndProcessDefinitionId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessDefinition processDef = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDef.getId());
    String[] activityIds = {"firstCallActivity"};
    queryParameter.setActivityIdIn(activityIds);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByActivityIdAndProcessDefinitionIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessDefinition processDef = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDef.getId());
    String[] activityIds = {"firstCallActivity"};
    queryParameter.setActivityIdIn(activityIds);

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(4);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(4);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdAndProcessDefinitionId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessDefinition userTaskProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("userTaskProcess")
        .singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());

    queryParameter.setProcessDefinitionId(userTaskProcess.getId());

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdAndProcessDefinitionIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessDefinition userTaskProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("userTaskProcess")
        .singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());
    queryParameter.setProcessDefinitionId(userTaskProcess.getId());

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdAndProcessDefinitionIdAndActivityId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessDefinition userTaskProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("userTaskProcess")
        .singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());
    queryParameter.setProcessDefinitionId(userTaskProcess.getId());
    String[] activityIds = {"theUserTask"};
    queryParameter.setActivityIdIn(activityIds);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/user-task-process.bpmn",
      "processes/another-user-task-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdAndProcessDefinitionIdAndActivityIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 2);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessDefinition userTaskProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("userTaskProcess")
        .singleResult();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());
    queryParameter.setProcessDefinitionId(userTaskProcess.getId());
    String[] activityIds = {"theUserTask"};
    queryParameter.setActivityIdIn(activityIds);

    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(2);
  }

  private VariableQueryParameterDto createVariableParameter(String name, String operator, Object value) {
    VariableQueryParameterDto variable = new VariableQueryParameterDto();
    variable.setName(name);
    variable.setOperator(operator);
    variable.setValue(value);

    return variable;
  }
}
