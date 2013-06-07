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

import org.camunda.bpm.cockpit.plugin.base.persistence.entity.IncidentDto;
import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessInstanceDto;
import org.camunda.bpm.cockpit.plugin.base.resources.ProcessInstanceResource;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author roman.smirnov 
 * @author nico.rehwaldt
 */
public class ProcessInstancesQueryTest extends AbstractCockpitPluginTest {

  private ProcessEngine processEngine;
  private RuntimeService runtimeService;
  private RepositoryService repositoryService;
  private JobExecutorHelper helper;
  
  private ProcessInstanceResource resource;
  
  @Before
  public void setUp() throws Exception {
    super.before();
    
    processEngine = getProcessEngine();
    
    helper = new JobExecutorHelper(processEngine);
    
    runtimeService = processEngine.getRuntimeService();
    repositoryService = processEngine.getRepositoryService();
    
    resource = new ProcessInstanceResource(processEngine.getName());
  }

  private void startProcessInstances(String processDefinitionKey, int numOfInstances) {
    for (int i = 0; i < numOfInstances; i++) {
      runtimeService.startProcessInstanceByKey(processDefinitionKey);
    }    
  }
  

  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQuery() {
    startProcessInstances("userTaskProcess", 10);
    
    helper.waitForJobExecutorToProcessAllJobs(15000, 500);
    
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    
    List<ProcessInstanceDto> result = resource.getProcessInstances(processDefinitionId, null, null);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(10);
  }
  
  @Test
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQueryPagination() {
    startProcessInstances("userTaskProcess", 10);
    
    helper.waitForJobExecutorToProcessAllJobs(15000, 500);
    
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    
    List<ProcessInstanceDto> result = resource.getProcessInstances(processDefinitionId, 0, 5);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(5);
    
    result = resource.getProcessInstances(processDefinitionId, 2, 3);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);
    
    result = resource.getProcessInstances(processDefinitionId, 6, 1);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
  }
  
  @Deployment(resources = {
    "processes/user-task-process.bpmn"
  })
  public void testQueryWithException() {
    startProcessInstances("userTaskProcess", 1);
    
    helper.waitForJobExecutorToProcessAllJobs(15000, 500);
    
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    
    try {
      resource.getProcessInstances(processDefinitionId, 0, null);
      Assert.fail("Runtime exception expected.");
    } catch (Exception e) {
      // exception expected
    }
    
    try {
      resource.getProcessInstances(processDefinitionId, null, 1);
      Assert.fail("Runtime exception expected.");
    } catch (Exception e) {
      // exception expected
    }
    
  }
  
  @Test
  @Deployment(resources = {
      "processes/user-task-process.bpmn"
  })
  public void testQueryWithoutAnyIncident() {
    startProcessInstances("userTaskProcess", 1);

    helper.waitForJobExecutorToProcessAllJobs(15000, 500);
    
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    
    List<ProcessInstanceDto> result = resource.getProcessInstances(processDefinitionId, null, null);
    
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    
    List<IncidentDto> incidents = result.get(0).getIncidents();
    
    assertThat(incidents).isEmpty();
  }
  
  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn"
  })
  public void testQueryWithContainingIncidents() {
    startProcessInstances("FailingProcess", 1);

    helper.waitForJobExecutorToProcessAllJobs(15000, 500);
    
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    
    List<ProcessInstanceDto> result = resource.getProcessInstances(processDefinitionId, null, null);
    
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    
    List<IncidentDto> incidents = result.get(0).getIncidents();
    
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);
    
    IncidentDto incident = incidents.get(0);
    
    assertThat(incident.getIncidentType()).isEqualTo("failedJob");
    assertThat(incident.getIncidentCount()).isEqualTo(1);
  }
  
  @Test
  @Deployment(resources = {
    "processes/process-with-two-parallel-failing-services.bpmn"
  })
  public void testQueryWithMoreThanOneIncident() {
    startProcessInstances("processWithTwoParallelFailingServices", 1);

    helper.waitForJobExecutorToProcessAllJobs(15000, 500);
    
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    
    List<ProcessInstanceDto> result = resource.getProcessInstances(processDefinitionId, null, null);
    
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
    
    List<IncidentDto> incidents = result.get(0).getIncidents();
    
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(3);
    
    for (IncidentDto incident : incidents) {
      String incidentType = incident.getIncidentType();
      assertThat(incidentType).isNotNull();
      
      if (incidentType.equals("failedJob")) {
        assertThat(incident.getIncidentCount()).isEqualTo(2);
      } else if (incidentType.equals("anIncident")) {
        assertThat(incident.getIncidentCount()).isEqualTo(3);
      } else if (incidentType.equals("anotherIncident")) {
        assertThat(incident.getIncidentCount()).isEqualTo(5);
      } else {
        Assert.fail(incidentType + " no expected.");
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
    
    helper.waitForJobExecutorToProcessAllJobs(15000, 500);
    
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
    
    List<ProcessInstanceDto> nestedCallActivityInstances = resource.getProcessInstances(nestedCallActivityId, null, null);
    assertThat(nestedCallActivityInstances).isNotEmpty();
    assertThat(nestedCallActivityInstances).hasSize(1);
    
    List<IncidentDto> nestedCallActivityIncidents = nestedCallActivityInstances.get(0).getIncidents();
    assertThat(nestedCallActivityIncidents).isNotEmpty();
    assertThat(nestedCallActivityIncidents).hasSize(1);
    
    IncidentDto nestedCallActivityIncident = nestedCallActivityIncidents.get(0); 
    assertThat(nestedCallActivityIncident.getIncidentType()).isEqualTo("failedJob");
    assertThat(nestedCallActivityIncident.getIncidentCount()).isEqualTo(1);
    
    List<ProcessInstanceDto> callActivityInstances = resource.getProcessInstances(callActivityId, null, null);
    assertThat(callActivityInstances).isNotEmpty();
    assertThat(callActivityInstances).hasSize(1);
    
    List<IncidentDto> callActivityIncidents = callActivityInstances.get(0).getIncidents();
    assertThat(callActivityIncidents).isNotEmpty();
    assertThat(callActivityIncidents).hasSize(1);
    
    IncidentDto callActivityIncident = callActivityIncidents.get(0); 
    assertThat(callActivityIncident.getIncidentType()).isEqualTo("failedJob");
    assertThat(callActivityIncident.getIncidentCount()).isEqualTo(1);
    
    List<ProcessInstanceDto> failingProcessInstances = resource.getProcessInstances(failingProcess, null, null);
    assertThat(failingProcessInstances).isNotEmpty();
    assertThat(failingProcessInstances).hasSize(1);
    
    List<IncidentDto> failingProcessIncidents = failingProcessInstances.get(0).getIncidents();
    assertThat(failingProcessIncidents).isNotEmpty();
    assertThat(failingProcessIncidents).hasSize(1);
    
    IncidentDto failingProcessIncident = failingProcessIncidents.get(0); 
    assertThat(failingProcessIncident.getIncidentType()).isEqualTo("failedJob");
    assertThat(failingProcessIncident.getIncidentCount()).isEqualTo(1);
  }

}
