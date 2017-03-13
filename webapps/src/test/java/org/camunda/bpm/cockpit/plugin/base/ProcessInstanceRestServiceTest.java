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

import static org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto.EQUALS_OPERATOR_NAME;
import static org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto.GREATER_THAN_OPERATOR_NAME;
import static org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME;
import static org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto.LESS_THAN_OPERATOR_NAME;
import static org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME;
import static org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto.LIKE_OPERATOR_NAME;
import static org.camunda.bpm.engine.rest.dto.ConditionQueryParameterDto.NOT_EQUALS_OPERATOR_NAME;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentStatisticsDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessInstanceQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.resources.ProcessInstanceRestService;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author roman.smirnov
 * @author nico.rehwaldt
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class ProcessInstanceRestServiceTest extends AbstractCockpitPluginTest {

  private ProcessEngine processEngine;
  private RuntimeService runtimeService;
  private RepositoryService repositoryService;

  private ProcessInstanceRestService resource;

  @Before
  public void setUp() throws Exception {
    super.before();

    processEngine = getProcessEngine();
    runtimeService = processEngine.getRuntimeService();
    repositoryService = processEngine.getRepositoryService();

    resource = new ProcessInstanceRestService(processEngine.getName());
  }

  private void startProcessInstances(String processDefinitionKey, int numOfInstances) {
    for (int i = 0; i < numOfInstances; i++) {
      ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 1000));
      runtimeService.startProcessInstanceByKey(processDefinitionKey, "businessKey_" + i);
    }

    executeAvailableJobs();
  }

  private void startProcessInstancesDelayed(String processDefinitionKey, int numOfInstances) {
    for (int i = 0; i < numOfInstances; i++) {
      ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 3000));
      runtimeService.startProcessInstanceByKey(processDefinitionKey, "businessKey_" + i);
    }

    executeAvailableJobs();
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
  public void testQueryOrderByStartTime() {
    startProcessInstancesDelayed("userTaskProcess", 3);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDefinitionId);
    queryParameter.setSortBy("startTime");

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);

    for (int i=1; i < result.size(); i++) {
      Date previousStartTime = result.get(i - 1).getStartTime();
      Date startTime = result.get(i).getStartTime();
      assertThat(startTime.after(previousStartTime)).isTrue();
    }
  }

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQueryOrderByStartTimeAsc() {
    startProcessInstancesDelayed("userTaskProcess", 3);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDefinitionId);
    queryParameter.setSortBy("startTime");
    queryParameter.setSortOrder("asc");

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);

    for (int i=1; i < result.size(); i++) {
      Date previousStartTime = result.get(i - 1).getStartTime();
      Date startTime = result.get(i).getStartTime();
      assertThat(startTime.after(previousStartTime)).isTrue();
    }
  }

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQueryOrderByStartTimeDesc() {
    startProcessInstancesDelayed("userTaskProcess", 3);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setProcessDefinitionId(processDefinitionId);
    queryParameter.setSortBy("startTime");
    queryParameter.setSortOrder("desc");

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);

    for (int i=1; i < result.size(); i++) {
      Date previousStartTime = result.get(i - 1).getStartTime();
      Date startTime = result.get(i).getStartTime();
      assertThat(startTime.before(previousStartTime)).isTrue();
    }
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
  @Category(SlowMariaDbTest.class)
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
        createVariableParameter("varstring2", VariableQueryParameterDto.LIKE_OPERATOR_NAME, "F\\_%"),
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

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithIntegerVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithIntegerVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithIntegerVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithIntegerVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithIntegerVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithIntegerVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithLongVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithLongVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, (long) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithLongVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithLongVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, (long) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithLongVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithLongVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, (long) 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithShortVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithShortVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, (short) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithShortVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithShortVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, (short) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithShortVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithShortVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, (short) 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithDoubleVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, 5.0);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithDoubleVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithDoubleVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithDoubleVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithDoubleVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, 5.1);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterShortVariableWithDoubleVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (short) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, 5.1);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithIntegerVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithIntegerVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithIntegerVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithIntegerVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithIntegerVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithIntegerVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithLongVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithLongVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, (long) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithLongVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithLongVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, (long) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithLongVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithLongVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, (long) 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithShortVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithShortVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, (short) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithShortVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithShortVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, (short) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithShortVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithShortVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, (short) 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithDoubleVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, 5.0);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithDoubleVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithDoubleVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithDoubleVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithDoubleVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, 5.1);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterIntegerVariableWithDoubleVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, 5.1);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithIntegerVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithIntegerVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithIntegerVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithIntegerVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithIntegerVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithIntegerVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithLongVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithLongVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, (long) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithLongVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithLongVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, (long) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithLongVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithLongVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, (long) 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithShortVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithShortVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, (short) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithShortVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithShortVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, (short) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithShortVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithShortVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, (short) 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithDoubleVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, 5.0);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithDoubleVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithDoubleVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithDoubleVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithDoubleVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, 5.1);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Ignore
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterLongVariableWithDoubleVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, 5.1);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithIntegerVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithIntegerVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithIntegerVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithIntegerVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithIntegerVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithIntegerVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithLongVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithLongVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, (long) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithLongVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithLongVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, (long) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithLongVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, (long) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithLongVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, (long) 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithShortVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithShortVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, (short) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithShortVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", (long) 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithShortVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, (short) 4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithShortVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, (short) 5);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithShortVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, (short) 6);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithDoubleVariable_Eq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.0);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", EQUALS_OPERATOR_NAME, 5.0);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithDoubleVariable_Neq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", NOT_EQUALS_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithDoubleVariable_Gteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OR_EQUALS_OPERATOR_NAME, 5.3);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithDoubleVariable_Gt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", GREATER_THAN_OPERATOR_NAME, 4.9);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithDoubleVariable_Lteq() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.1);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OR_EQUALS_OPERATOR_NAME, 5.1);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryAfterDoubleVariableWithDoubleVariable_Lt() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 5.3);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userTaskProcess", vars);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    VariableQueryParameterDto variable = createVariableParameter("var", LESS_THAN_OPERATOR_NAME, 5.4);
    queryParameter.setVariables(Arrays.asList(variable));

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);

    ProcessInstanceDto dto = result.get(0);

    assertThat(dto.getId()).isEqualTo(processInstance.getId());
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryByStartedAfter() {
    String date = "2014-01-01T13:13:00";
    Date currentDate = DateTimeUtil.parseDateTime(date).toDate();

    ClockUtil.setCurrentTime(currentDate);

    startProcessInstances("userTaskProcess", 5);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setStartedAfter(currentDate);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(5);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryByStartedBefore() {
    String date = "2014-01-01T13:13:00";
    Date currentDate = DateTimeUtil.parseDateTime(date).toDate();

    ClockUtil.setCurrentTime(currentDate);

    startProcessInstances("userTaskProcess", 5);

    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setStartedBefore(hourFromNow.getTime());

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(5);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
    })
  public void testQueryByStartedBetween() {
    String date = "2014-01-01T13:13:00";
    Date currentDate = DateTimeUtil.parseDateTime(date).toDate();

    ClockUtil.setCurrentTime(currentDate);

    startProcessInstances("userTaskProcess", 5);

    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();
    queryParameter.setStartedAfter(currentDate);
    queryParameter.setStartedBefore(hourFromNow.getTime());

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(5);
  }

  private VariableQueryParameterDto createVariableParameter(String name, String operator, Object value) {
    VariableQueryParameterDto variable = new VariableQueryParameterDto();
    variable.setName(name);
    variable.setOperator(operator);
    variable.setValue(value);

    return variable;
  }

}
