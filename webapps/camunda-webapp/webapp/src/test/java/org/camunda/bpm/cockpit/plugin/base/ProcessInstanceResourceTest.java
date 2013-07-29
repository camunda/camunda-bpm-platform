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

import org.camunda.bpm.cockpit.impl.plugin.base.dto.CalledProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.query.parameter.ProcessInstanceQueryParameter;
import org.camunda.bpm.cockpit.impl.plugin.base.resources.ProcessInstanceRestService;
import org.camunda.bpm.cockpit.impl.plugin.base.sub.resources.ProcessInstanceResource;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
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
    runtimeService = processEngine.getRuntimeService();
    repositoryService = processEngine.getRepositoryService();
    managementService = processEngine.getManagementService();
  }
  
  @Test
  @Deployment(resources = "processes/failing-process.bpmn")
  public void testGetIncidents() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("FailingProcess");
    resource = new ProcessInstanceResource(getProcessEngine().getName(), processInstance.getId());
    
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .singleResult();
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    Job job = managementService.createJobQuery().singleResult();
      
    List<IncidentDto> incidents = resource.getIncidents();
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
    resource = new ProcessInstanceResource(getProcessEngine().getName(), processInstance.getId());
    
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("CallActivity")
        .singleResult();
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    List<IncidentDto> incidents = resource.getIncidents();
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
    resource = new ProcessInstanceResource(getProcessEngine().getName(), processInstance.getId());
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    List<IncidentDto> incidents = resource.getIncidents();
    assertThat(incidents).isNotEmpty();
    // 2x failedJob, 3x anIncident, 5x anotherIncident
    assertThat(incidents).hasSize(10);
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testGetCalledProcessInstancesByParentProcessInstanceId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");
    resource = new ProcessInstanceResource(getProcessEngine().getName(), processInstance.getId());
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    ProcessDefinition failingProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("FailingProcess")
        .singleResult();
    
    ProcessDefinition anotherFailingProcess = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("AnotherFailingProcess")
        .singleResult();
    
    ProcessInstanceQueryParameter queryParameter = new ProcessInstanceQueryParameter();
    
    List<ProcessInstanceDto> result = resource.queryCalledProcessInstances(queryParameter);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);
    
    ProcessDefinition compareWith = null;
    for (ProcessInstanceDto instance : result) {
      CalledProcessInstanceDto dto = (CalledProcessInstanceDto) instance;
      if (dto.getProcessDefinitionId().equals(failingProcess.getId())) {
        compareWith = failingProcess;
        assertThat(dto.getCallActivityId()).isEqualTo("firstCallActivity");
      } else if (dto.getProcessDefinitionId().equals(anotherFailingProcess.getId())) {
        compareWith = anotherFailingProcess;
        assertThat(dto.getCallActivityId()).isEqualTo("secondCallActivity");
      } else {
        Assert.fail("Unexpected called process instance: " + dto.getId());
      }
      
      assertThat(dto.getCallActivityInstanceId()).isNotNull();
      
      assertThat(dto.getProcessDefinitionId()).isEqualTo(compareWith.getId());
      assertThat(dto.getProcessDefinitionName()).isEqualTo(compareWith.getName());
      assertThat(dto.getProcessDefinitionKey()).isEqualTo(compareWith.getKey());
    }
  }
  
  @Test
  @Deployment(resources = {
      "processes/two-parallel-call-activities-calling-different-process.bpmn",
      "processes/failing-process.bpmn",
      "processes/another-failing-process.bpmn"
    })
  public void testGetCalledProcessInstancesByParentProcessInstanceIdAndActivityInstanceId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TwoParallelCallActivitiesCallingDifferentProcess");
    resource = new ProcessInstanceResource(getProcessEngine().getName(), processInstance.getId());
    
    ActivityInstance processInstanceActivityInstance = runtimeService.getActivityInstance(processInstance.getId());
    
    String firstActivityInstanceId = null;
    String secondActivityInstanceId = null;
    
    for (ActivityInstance child : processInstanceActivityInstance.getChildActivityInstances()) {
      if (child.getActivityId().equals("firstCallActivity")) {
        firstActivityInstanceId = child.getId();
      } else if (child.getActivityId().equals("secondCallActivity")) {
        secondActivityInstanceId = child.getId();
      } else {
        Assert.fail("Unexpected activity instance with activity id: " + child.getActivityId() + " and instance id: " + child.getId());
      }
    }
    
    helper.waitForJobExecutorToProcessAllJobs(15000);
    
    ProcessInstanceQueryParameter queryParameter1 = new ProcessInstanceQueryParameter();
    String[] activityInstanceIds1 = {firstActivityInstanceId};
    queryParameter1.setActivityInstanceIdIn(activityInstanceIds1);
    
    List<ProcessInstanceDto> result1 = resource.queryCalledProcessInstances(queryParameter1);
    assertThat(result1).isNotEmpty();
    assertThat(result1).hasSize(1);
    
    ProcessInstanceQueryParameter queryParameter2 = new ProcessInstanceQueryParameter();
    String[] activityInstanceIds2 = {secondActivityInstanceId};
    queryParameter2.setActivityInstanceIdIn(activityInstanceIds2);
    
    List<ProcessInstanceDto> result2 = resource.queryCalledProcessInstances(queryParameter2);
    assertThat(result2).isNotEmpty();
    assertThat(result2).hasSize(1);
   
    ProcessInstanceQueryParameter queryParameter3 = new ProcessInstanceQueryParameter();
    String[] activityInstanceIds3 = {firstActivityInstanceId, secondActivityInstanceId};
    queryParameter3.setActivityInstanceIdIn(activityInstanceIds3);
    
    List<ProcessInstanceDto> result3 = resource.queryCalledProcessInstances(queryParameter3);
    assertThat(result3).isNotEmpty();
    assertThat(result3).hasSize(2);
  }
}
