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

import java.util.List;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentStatisticsDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.query.parameter.ProcessInstanceQueryParameter;
import org.camunda.bpm.cockpit.impl.plugin.base.resources.ProcessInstanceRestService;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
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
    startProcessInstances("userTaskProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setProcessDefinitionId(processDefinitionId);
    
    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(10);
  }
  
  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQueryCount() {
    startProcessInstances("userTaskProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setProcessDefinitionId(processDefinitionId);
    
    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(10);
  }

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQueryPagination() {
    startProcessInstances("userTaskProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    
    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setProcessDefinitionId(processDefinitionId);

    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, 0, 5);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(5);

    result = resource.queryProcessInstances(queryParameter, 2, 3);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);

    result = resource.queryProcessInstances(queryParameter, 6, 1);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
  }

  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
  })
  public void testQueryWithoutAnyIncident() {
    startProcessInstances("userTaskProcess", 1);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    
    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
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

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
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

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
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

    ProcessInstanceQueryParameter queryParameter1 = new ProcessInstanceQueryParameter();
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

    ProcessInstanceQueryParameter queryParameter2 = new ProcessInstanceQueryParameter();
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

    ProcessInstanceQueryParameter queryParameter3 = new ProcessInstanceQueryParameter();
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
    startProcessInstances("userTaskProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
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
    startProcessInstances("userTaskProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
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
    startProcessInstances("userTaskProcess", 10);
    startProcessInstances("FailingProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
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
    startProcessInstances("userTaskProcess", 10);
    startProcessInstances("FailingProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
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
    startProcessInstances("userTaskProcess", 10);

    ProcessDefinition userTaskProcess = repositoryService.createProcessDefinitionQuery().singleResult();

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
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
    startProcessInstances("userTaskProcess", 10);

    ProcessDefinition userTaskProcess = repositoryService.createProcessDefinitionQuery().singleResult();

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setBusinessKey("businessKey_2");
    queryParameter.setProcessDefinitionId(userTaskProcess.getId());
    
    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(1);
  }

  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByActivityId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    
    String[] activityIds = {"firstCallActivity"};
    queryParameter.setActivityIdIn(activityIds);
    
    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByActivityIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    
    String[] activityIds = {"firstCallActivity"};
    queryParameter.setActivityIdIn(activityIds);
    
    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByActivityIds() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    
    String[] activityIds = {"firstCallActivity", "secondCallActivity"};
    queryParameter.setActivityIdIn(activityIds);
    
    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByActivityIdsCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    
    String[] activityIds = {"firstCallActivity", "secondCallActivity"};
    queryParameter.setActivityIdIn(activityIds);
    
    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByActivityIdAndProcessDefinitionId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);
    
    ProcessDefinition processDef = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setProcessDefinitionId(processDef.getId());
    String[] activityIds = {"firstCallActivity"};
    queryParameter.setActivityIdIn(activityIds);
    
    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByActivityIdAndProcessDefinitionIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);
    
    ProcessDefinition processDef = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setProcessDefinitionId(processDef.getId());
    String[] activityIds = {"firstCallActivity"};
    queryParameter.setActivityIdIn(activityIds);
    
    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);
    startProcessInstances("FailingProcess", 5);
    startProcessInstances("AnotherFailingProcess", 5);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();
   
    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());
    
    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(20);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);
    startProcessInstances("FailingProcess", 5);
    startProcessInstances("AnotherFailingProcess", 5);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();
   
    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());
    
    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(20);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdAndProcessDefinitionId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);
    startProcessInstances("FailingProcess", 5);
    startProcessInstances("AnotherFailingProcess", 5);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessDefinition failingProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();
    
    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());
    queryParameter.setProcessDefinitionId(failingProcess.getId());
    
    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdAndProcessDefinitionIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);
    startProcessInstances("FailingProcess", 5);
    startProcessInstances("AnotherFailingProcess", 5);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessDefinition failingProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();
    
    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());
    queryParameter.setProcessDefinitionId(failingProcess.getId());
    
    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdAndProcessDefinitionIdAndActivityId() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);
    startProcessInstances("FailingProcess", 5);
    startProcessInstances("AnotherFailingProcess", 5);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessDefinition failingProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();
    
    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());
    queryParameter.setProcessDefinitionId(failingProcess.getId());
    String[] activityIds = {"ServiceTask_1"};
    queryParameter.setActivityIdIn(activityIds);
    
    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testQueryByParentProcessDefinitionIdAndProcessDefinitionIdAndActivityIdCount() {
    startProcessInstances("TwoParallelCallActivitiesCallingDifferentProcess", 10);
    startProcessInstances("FailingProcess", 5);
    startProcessInstances("AnotherFailingProcess", 5);

    ProcessDefinition twoCallActivitiesProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("TwoParallelCallActivitiesCallingDifferentProcess")
        .singleResult();

    ProcessDefinition failingProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();
    
    helper.waitForJobExecutorToProcessAllJobs(15000);

    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    queryParameter.setParentProcessDefinitionId(twoCallActivitiesProcess.getId());
    queryParameter.setProcessDefinitionId(failingProcess.getId());
    String[] activityIds = {"ServiceTask_1"};
    queryParameter.setActivityIdIn(activityIds);
    
    CountResultDto result = resource.queryProcessInstancesCount(queryParameter);
    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(10);
  }
  
}
