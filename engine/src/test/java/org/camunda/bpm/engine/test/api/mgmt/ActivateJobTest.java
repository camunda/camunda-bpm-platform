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

/**
 * @author roman.smirnov
 */
public class ActivateJobTest extends PluggableProcessEngineTestCase {

  public void testActivationById_shouldThrowProcessEngineException() {
    try {
      managementService.activateJobById(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testActivationById_shouldActivateJob() {
    // given

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // suspended job definitions and corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertTrue(job.isSuspended());

    // when
    // the job will be activated
    managementService.activateJobById(job.getId());

    // then
    // the job should be active
    assertEquals(1, jobQuery.active().count());
    assertEquals(0, jobQuery.suspended().count());

    Job activeJob = jobQuery.active().singleResult();

    assertEquals(job.getId(), activeJob.getId());
    assertFalse(activeJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testActivationByJobDefinitionId_shouldActivateJob() {
    // given

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // suspended job definitions and corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // the job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertTrue(job.isSuspended());

    // when
    // the job will be activated
    managementService.activateJobByJobDefinitionId(jobDefinition.getId());

    // then
    // the job should be activated
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.active().singleResult();

    assertEquals(job.getId(), activeJob.getId());
    assertEquals(jobDefinition.getId(), activeJob.getJobDefinitionId());
    assertFalse(activeJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testActivateByProcessInstanceId_shouldActivateJob() {
    // given

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // suspended job definitions and corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // the job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertTrue(job.isSuspended());

    // when
    // the job will be activate
    managementService.activateJobByProcessInstanceId(processInstance.getId());

    // then
    // the job should be suspended
    assertEquals(1, jobQuery.active().count());
    assertEquals(0, jobQuery.suspended().count());

    Job suspendedJob = jobQuery.active().singleResult();

    assertEquals(job.getId(), suspendedJob.getId());
    assertEquals(jobDefinition.getId(), suspendedJob.getJobDefinitionId());
    assertFalse(suspendedJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testActivationByProcessDefinitionId_shouldActivateJob() {
    // given
    // a deployed process definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // suspended job definitions and corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertTrue(job.isSuspended());

    // when
    // the job will be activated
    managementService.activateJobByProcessDefinitionId(processDefinition.getId());

    // then
    // the job should be active
    assertEquals(1, jobQuery.active().count());
    assertEquals(0, jobQuery.suspended().count());

    Job activeJob = jobQuery.active().singleResult();

    assertEquals(job.getId(), activeJob.getId());
    assertFalse(activeJob.isSuspended());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/SuspensionTest.testBase.bpmn"})
  public void testActivationByProcessDefinitionKey_shouldActivateJob() {
    // given
    // a deployed process definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running process instance with a failed job
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey("suspensionProcess", params);

    // suspended job definitions and corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey("suspensionProcess", true);

    // the failed job
    JobQuery jobQuery = managementService.createJobQuery();
    Job job = jobQuery.singleResult();
    assertTrue(job.isSuspended());

    // when
    // the job will be activated
    managementService.activateJobByProcessDefinitionKey(processDefinition.getKey());

    // then
    // the job should be suspended
    assertEquals(0, jobQuery.suspended().count());
    assertEquals(1, jobQuery.active().count());

    Job activeJob = jobQuery.active().singleResult();

    assertEquals(job.getId(), activeJob.getId());
    assertFalse(activeJob.isSuspended());
  }

  public void testMultipleActivationByProcessDefinitionKey_shouldActivateJob() {
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

    // suspended job definitions and corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionKey(key, true);

    // when
    // the job will be suspended
    managementService.activateJobByProcessDefinitionKey(key);

    // then
    // the job should be activated
    JobQuery jobQuery = managementService.createJobQuery();

    assertEquals(0, jobQuery.suspended().count());
    assertEquals(3, jobQuery.active().count());

    // Clean DB
    for (org.camunda.bpm.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

}
