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
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.IncidentQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.resources.IncidentRestService;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;

/**
 * @author roman.smirnov
 */
public class IncidentRestServiceTest extends AbstractCockpitPluginTest {

  private ProcessEngine processEngine;
  private RuntimeService runtimeService;
  private IncidentRestService resource;

  @Before
  public void setUp() throws Exception {
    super.before();

    processEngine = getProcessEngine();
    runtimeService = processEngine.getRuntimeService();

    resource = new IncidentRestService(processEngine.getName());
  }

  @Test
  @Deployment(resources = {
    "processes/failing-process.bpmn"
  })
  public void testQueryByProcessInstanceId() {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("FailingProcess");
    runtimeService.startProcessInstanceByKey("FailingProcess");

    executeAvailableJobs();

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
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getProcessDefinitionId()).isEqualTo(processInstance1.getProcessDefinitionId());
    assertThat(incident.getExecutionId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getConfiguration()).isNotNull();
    assertThat(incident.getCauseIncidentId()).isEqualTo(incident.getId());
    assertThat(incident.getCauseIncidentProcessInstanceId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getCauseIncidentProcessDefinitionId()).isEqualTo(processInstance1.getProcessDefinitionId());
    assertThat(incident.getCauseIncidentActivityId()).isEqualTo("ServiceTask_1");
    assertThat(incident.getRootCauseIncidentId()).isEqualTo(incident.getId());
    assertThat(incident.getRootCauseIncidentProcessInstanceId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getRootCauseIncidentProcessDefinitionId()).isEqualTo(processInstance1.getProcessDefinitionId());
    assertThat(incident.getRootCauseIncidentActivityId()).isEqualTo("ServiceTask_1");
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
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(incident.getExecutionId()).isNotNull();
    assertThat(incident.getConfiguration()).isNotNull();
    assertThat(incident.getCauseIncidentId()).isEqualTo(incident.getId());
    assertThat(incident.getCauseIncidentProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getCauseIncidentProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(incident.getCauseIncidentActivityId()).isEqualTo("theServiceTask1");
    assertThat(incident.getRootCauseIncidentId()).isEqualTo(incident.getId());
    assertThat(incident.getRootCauseIncidentProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getRootCauseIncidentProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(incident.getRootCauseIncidentActivityId()).isEqualTo("theServiceTask1");
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
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance1.getId());
    assertThat(incident.getProcessDefinitionId()).isEqualTo(processInstance1.getProcessDefinitionId());
    assertThat(incident.getExecutionId()).isNotNull();
    assertThat(incident.getConfiguration()).isNull();

    assertThat(incident.getCauseIncidentId()).isNotEqualTo(incident.getId());
    assertThat(incident.getCauseIncidentProcessInstanceId()).isEqualTo(processInstance2.getId());
    assertThat(incident.getCauseIncidentProcessDefinitionId()).isEqualTo(processInstance2.getProcessDefinitionId());
    assertThat(incident.getCauseIncidentActivityId()).isEqualTo("CallActivity_1");

    assertThat(incident.getRootCauseIncidentId()).isNotEqualTo(incident.getId());
    assertThat(incident.getRootCauseIncidentProcessInstanceId()).isEqualTo(processInstance3.getId());
    assertThat(incident.getRootCauseIncidentProcessDefinitionId()).isEqualTo(processInstance3.getProcessDefinitionId());
    assertThat(incident.getRootCauseIncidentActivityId()).isEqualTo("ServiceTask_1");
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

}
