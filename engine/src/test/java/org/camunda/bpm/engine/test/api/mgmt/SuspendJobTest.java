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
package org.camunda.bpm.engine.test.api.mgmt;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author roman.smirnov
 */
public class SuspendJobTest extends PluggableProcessEngineTestCase {

  public void testSuspensionById_shouldThrowProcessEngineException() {
    try {
      managementService.suspendJobById(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionById_shouldSuspendJob() {
    // given

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertFalse(job.isSuspended());

    // when
    // the job will be suspended
    managementService.suspendJobById(job.getId());

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(job.getId(), suspendedJob.getId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionByJobDefinitionId_shouldSuspendJob() {
    // given

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // the job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertFalse(job.isSuspended());

    // when
    // the job will be suspended
    managementService.suspendJobByJobDefinitionId(jobDefinition.getId());

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(job.getId(), suspendedJob.getId());
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionByProcessInstanceId_shouldSuspendJob() {
    // given

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // the job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertFalse(job.isSuspended());

    // when
    // the job will be suspended
    managementService.suspendJobByProcessInstanceId(processInstance.getId());

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(job.getId(), suspendedJob.getId());
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionByProcessDefinitionId_shouldSuspendJob() {
    // given
    // a deployed process definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertFalse(job.isSuspended());

    // when
    // the job will be suspended
    managementService.suspendJobByProcessDefinitionId(processDefinition.getId());

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(job.getId(), suspendedJob.getId());
    assertTrue(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionByProcessDefinitionKey_shouldSuspendJob() {
    // given
    // a deployed process definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertFalse(job.isSuspended());

    // when
    // the job will be suspended
    managementService.suspendJobByProcessDefinitionKey(processDefinition.getKey());

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.suspended().singleResult();

    assertEquals(job.getId(), suspendedJob.getId());
    assertTrue(suspendedJob.isSuspended());
  }

  public void testMultipleSuspensionByProcessDefinitionKey_shouldSuspendJob() {
    // given
    String key = "suspensionProcess";

    // Deploy three processes and start for each deployment a process instance
    // with a failed job
    int nrOfProcessDefinitions = 3;
    for (int i=0; i < nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn").deploy();
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("fail", Boolean.TRUE);
      runtimeService.startProcessInstanceByKey(key, params);
    }

    // when
    // the job will be suspended
    managementService.suspendJobByProcessDefinitionKey(key);

    // then
    // the job should be suspended
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.active().count());
    assertEquals(3, jobQuery.suspended().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionByIdUsingBuilder() {
    // given

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertFalse(job.isSuspended());

    // when
    // the job will be suspended
    managementService
      .updateJobSuspensionState()
      .byJobId(job.getId())
      .suspend();

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionByJobDefinitionIdUsingBuilder() {
    // given

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.active().count());

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when
    // the job will be suspended
    managementService
      .updateJobSuspensionState()
      .byJobDefinitionId(jobDefinition.getId())
      .suspend();

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionByProcessInstanceIdUsingBuilder() {
    // given

    // a running process instance with a failed job
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.active().count());

    // when
    // the job will be suspended
    managementService
      .updateJobSuspensionState()
      .byProcessInstanceId(processInstance.getId())
      .suspend();

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionByProcessDefinitionIdUsingBuilder() {
    // given

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.active().count());

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // when
    // the job will be suspended
    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionId(processDefinition.getId())
      .suspend();

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testSuspensionByProcessDefinitionKeyUsingBuilder() {
    // given

    // a running process instance with a failed job
    runtimeService.startProcessInstanceByKey("suspensionProcess",
        Variables.createVariables().putValue("fail", true));

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, jobQuery.active().count());

    // when
    // the job will be suspended
    managementService
      .updateJobSuspensionState()
      .byProcessDefinitionKey("suspensionProcess")
      .suspend();

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.active().count());
    assertEquals(1, jobQuery.suspended().count());
  }

}
