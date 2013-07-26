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

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentDto;
import org.camunda.bpm.cockpit.impl.plugin.base.resources.ProcessInstanceResource;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;

/**
 * @author roman.smirnov
 */
public class ProcessInstanceResourceTest extends AbstractCockpitPluginTest {
  
  private ProcessInstanceResource resource;
  private ProcessEngine processEngine;
  private JobExecutorHelper helper;
  private RuntimeService runtimeService;
  private RepositoryService repositoryService;
  private ManagementService managementService;
  
  @Before
  public void setUp() throws Exception {
    super.before();
    
    processEngine = getProcessEngine();
    helper = new JobExecutorHelper(processEngine);
    resource = new ProcessInstanceResource(processEngine.getName());
    runtimeService = processEngine.getRuntimeService();
    repositoryService = processEngine.getRepositoryService();
    managementService = processEngine.getManagementService();
  }
  
  @Test
  @Deployment(resources = "processes/failing-process.bpmn")
  public void testGetIncidents() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("FailingProcess");
    
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .singleResult();
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    Job job = managementService.createJobQuery().singleResult();
    
    List<IncidentDto> incidents = resource.getIncidents(processInstance.getId());
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);
    
    IncidentDto dto = incidents.get(0);
    
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getIncidentTimestamp()).isNotNull();
    assertThat(dto.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(dto.getActivityId()).isEqualTo("ServiceTask_1");
    assertThat(dto.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(dto.getProcessDefinitionId()).isEqualTo(processDefinition.getId());
    assertThat(dto.getCauseIncidentId()).isEqualTo(dto.getId());
    assertThat(dto.getRootCauseIncidentId()).isEqualTo(dto.getId());
    assertThat(dto.getConfiguration()).isEqualTo(job.getId());
  }

  @Test
  @Deployment(resources = {"processes/failing-process.bpmn", "processes/call-activity.bpmn"})
  public void testGetRecursivePropagatedIncident() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CallActivity");
    
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("CallActivity")
        .singleResult();
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    List<IncidentDto> incidents = resource.getIncidents(processInstance.getId());
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);
    
    IncidentDto dto = incidents.get(0);
    
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getIncidentTimestamp()).isNotNull();
    assertThat(dto.getExecutionId()).isNotNull().isNotEmpty();
    assertThat(dto.getActivityId()).isEqualTo("CallActivity_1");
    assertThat(dto.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(dto.getProcessDefinitionId()).isEqualTo(processDefinition.getId());
    assertThat(dto.getCauseIncidentId()).isNotNull().isNotEmpty();
    assertThat(dto.getRootCauseIncidentId()).isNotNull().isNotEmpty();
    assertThat(dto.getConfiguration()).isNull();
  }
  
  @Test
  @Deployment(resources = "processes/process-with-two-parallel-failing-services.bpmn")
  public void testGetTenIncidents() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processWithTwoParallelFailingServices");
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    List<IncidentDto> incidents = resource.getIncidents(processInstance.getId());
    assertThat(incidents).isNotEmpty();
    // 2x failedJob, 3x anIncident, 5x anotherIncident
    assertThat(incidents).hasSize(10);
  }
  
}
