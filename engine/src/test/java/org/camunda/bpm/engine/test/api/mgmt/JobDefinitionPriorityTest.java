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

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobDefinitionPriorityTest extends PluggableProcessEngineTestCase {

  protected static final long EXPECTED_DEFAULT_PRIORITY = 0;

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/asyncTaskProcess.bpmn20.xml")
  public void testSetJobDefinitionPriority() {
    // given a process instance with a job with default priority and a corresponding job definition
    ProcessInstance instance = runtimeService.createProcessInstanceByKey("asyncTaskProcess")
      .startBeforeActivity("task")
      .execute();

    Job job = managementService.createJobQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery()
      .jobDefinitionId(job.getJobDefinitionId()).singleResult();

    // when I set the job definition's priority
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 42);

    // then the job definition's priority value has changed
    JobDefinition updatedDefinition = managementService.createJobDefinitionQuery()
        .jobDefinitionId(jobDefinition.getId()).singleResult();
    assertEquals(42, (long) updatedDefinition.getOverridingJobPriority());

    // the existing job's priority has not changed
    Job updatedExistingJob = managementService.createJobQuery().singleResult();
    assertEquals(job.getPriority(), updatedExistingJob.getPriority());

    // and a new job of that definition receives the updated priority
    runtimeService.createProcessInstanceModification(instance.getId())
      .startBeforeActivity("task")
      .execute();

    Job newJob = getJobThatIsNot(updatedExistingJob);
    assertEquals(42, newJob.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/asyncTaskProcess.bpmn20.xml")
  public void testSetJobDefinitionPriorityWithCascade() {
    // given a process instance with a job with default priority and a corresponding job definition
    ProcessInstance instance = runtimeService.createProcessInstanceByKey("asyncTaskProcess")
      .startBeforeActivity("task")
      .execute();

    Job job = managementService.createJobQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery()
      .jobDefinitionId(job.getJobDefinitionId()).singleResult();

    // when I set the job definition's priority
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 52, true);

    // then the job definition's priority value has changed
    JobDefinition updatedDefinition = managementService.createJobDefinitionQuery()
        .jobDefinitionId(jobDefinition.getId()).singleResult();
    assertEquals(52, (long) updatedDefinition.getOverridingJobPriority());

    // the existing job's priority has changed as well
    Job updatedExistingJob = managementService.createJobQuery().singleResult();
    assertEquals(52, updatedExistingJob.getPriority());

    // and a new job of that definition receives the updated priority
    runtimeService.createProcessInstanceModification(instance.getId())
      .startBeforeActivity("task")
      .execute();

    Job newJob = getJobThatIsNot(updatedExistingJob);
    assertEquals(52, newJob.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/jobPrioProcess.bpmn20.xml")
  public void testSetJobDefinitionPriorityOverridesBpmnPriority() {
    // given a process instance with a job with default priority and a corresponding job definition
    ProcessInstance instance = runtimeService.createProcessInstanceByKey("jobPrioProcess")
      .startBeforeActivity("task2")
      .execute();

    Job job = managementService.createJobQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery()
      .jobDefinitionId(job.getJobDefinitionId()).singleResult();

    // when I set the job definition's priority
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 62);

    // then the job definition's priority value has changed
    JobDefinition updatedDefinition = managementService.createJobDefinitionQuery()
        .jobDefinitionId(jobDefinition.getId()).singleResult();
    assertEquals(62, (long) updatedDefinition.getOverridingJobPriority());

    // the existing job's priority is still the value as given in the BPMN XML
    Job updatedExistingJob = managementService.createJobQuery().singleResult();
    assertEquals(5, updatedExistingJob.getPriority());

    // and a new job of that definition receives the updated priority
    // meaning that the updated priority overrides the priority specified in the BPMN XML
    runtimeService.createProcessInstanceModification(instance.getId())
      .startBeforeActivity("task2")
      .execute();

    Job newJob = getJobThatIsNot(updatedExistingJob);
    assertEquals(62, newJob.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/jobPrioProcess.bpmn20.xml")
  public void testSetJobDefinitionPriorityWithCascadeOverridesBpmnPriority() {
    // given a process instance with a job with default priority and a corresponding job definition
    ProcessInstance instance = runtimeService.createProcessInstanceByKey("jobPrioProcess")
      .startBeforeActivity("task2")
      .execute();

    Job job = managementService.createJobQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery()
      .jobDefinitionId(job.getJobDefinitionId()).singleResult();

    // when I set the job definition's priority
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 72, true);

    // then the job definition's priority value has changed
    JobDefinition updatedDefinition = managementService.createJobDefinitionQuery()
        .jobDefinitionId(jobDefinition.getId()).singleResult();
    assertEquals(72, (long) updatedDefinition.getOverridingJobPriority());

    // the existing job's priority has changed as well
    Job updatedExistingJob = managementService.createJobQuery().singleResult();
    assertEquals(72, updatedExistingJob.getPriority());

    // and a new job of that definition receives the updated priority
    // meaning that the updated priority overrides the priority specified in the BPMN XML
    runtimeService.createProcessInstanceModification(instance.getId())
      .startBeforeActivity("task2")
      .execute();

    Job newJob = getJobThatIsNot(updatedExistingJob);
    assertEquals(72, newJob.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/jobPrioProcess.bpmn20.xml")
  public void testRedeployOverridesSetJobDefinitionPriority() {
    // given a process instance with a job with default priority and a corresponding job definition
    runtimeService.createProcessInstanceByKey("jobPrioProcess")
      .startBeforeActivity("task2")
      .execute();

    Job job = managementService.createJobQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery()
      .jobDefinitionId(job.getJobDefinitionId()).singleResult();

    // when I set the job definition's priority
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 72, true);

    // then the job definition's priority value has changed
    JobDefinition updatedDefinition = managementService.createJobDefinitionQuery()
      .jobDefinitionId(jobDefinition.getId()).singleResult();
    assertEquals(72, (long) updatedDefinition.getOverridingJobPriority());

    // the existing job's priority has changed as well
    Job updatedExistingJob = managementService.createJobQuery().singleResult();
    assertEquals(72, updatedExistingJob.getPriority());

    // if the process definition is redeployed
    String secondDeploymentId = repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/jobPrioProcess.bpmn20.xml").deploy().getId();

    // then a new job will have the priority from the BPMN xml
    ProcessInstance secondInstance = runtimeService.createProcessInstanceByKey("jobPrioProcess")
      .startBeforeActivity("task2")
      .execute();

    Job newJob = managementService.createJobQuery().processInstanceId(secondInstance.getId()).singleResult();
    assertEquals(5, newJob.getPriority());

    repositoryService.deleteDeployment(secondDeploymentId, true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/asyncTaskProcess.bpmn20.xml")
  public void testResetJobDefinitionPriority() {

    // given a job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when I set a priority
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), 1701);

    // and I reset the priority
    managementService.clearOverridingJobPriorityForJobDefinition(jobDefinition.getId());

    // then the job definition priority is still null
    JobDefinition updatedDefinition = managementService.createJobDefinitionQuery()
        .jobDefinitionId(jobDefinition.getId()).singleResult();
    assertNull(updatedDefinition.getOverridingJobPriority());

    // and a new job instance does not receive the intermittently set priority
    runtimeService.createProcessInstanceByKey("asyncTaskProcess")
      .startBeforeActivity("task")
      .execute();

    Job job = managementService.createJobQuery().singleResult();
    assertEquals(EXPECTED_DEFAULT_PRIORITY, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/asyncTaskProcess.bpmn20.xml")
  public void testResetJobDefinitionPriorityWhenPriorityIsNull() {

    // given a job definition with null priority
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    assertNull(jobDefinition.getOverridingJobPriority());

    // when I set a priority
    managementService.clearOverridingJobPriorityForJobDefinition(jobDefinition.getId());

    // then the priority remains unchanged
    JobDefinition updatedDefinition = managementService.createJobDefinitionQuery()
        .jobDefinitionId(jobDefinition.getId()).singleResult();
    assertNull(updatedDefinition.getOverridingJobPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/jobPrioProcess.bpmn20.xml")
  public void testGetJobDefinitionDefaultPriority() {
    // with a process with job definitions deployed
    // then the definitions have a default null priority, meaning that they don't override the
    // value in the BPMN XML
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery().list();
    assertEquals(4, jobDefinitions.size());

    assertNull(jobDefinitions.get(0).getOverridingJobPriority());
    assertNull(jobDefinitions.get(1).getOverridingJobPriority());
    assertNull(jobDefinitions.get(2).getOverridingJobPriority());
    assertNull(jobDefinitions.get(3).getOverridingJobPriority());
  }

  public void testSetNonExistingJobDefinitionPriority() {
    try {
      managementService.setOverridingJobPriorityForJobDefinition("someNonExistingJobDefinitionId", 42);
      fail("should not succeed");
    } catch (NotFoundException e) {
      // happy path
      assertTextPresentIgnoreCase("job definition with id 'someNonExistingJobDefinitionId' does not exist",
          e.getMessage());
    }

    try {
      managementService.setOverridingJobPriorityForJobDefinition("someNonExistingJobDefinitionId", 42, true);
      fail("should not succeed");
    } catch (NotFoundException e) {
      // happy path
      assertTextPresentIgnoreCase("job definition with id 'someNonExistingJobDefinitionId' does not exist",
          e.getMessage());
    }
  }

  public void testResetNonExistingJobDefinitionPriority() {
    try {
      managementService.clearOverridingJobPriorityForJobDefinition("someNonExistingJobDefinitionId");
      fail("should not succeed");
    } catch (NotFoundException e) {
      // happy path
      assertTextPresentIgnoreCase("job definition with id 'someNonExistingJobDefinitionId' does not exist",
          e.getMessage());
    }
  }

  public void testSetNullJobDefinitionPriority() {
    try {
      managementService.setOverridingJobPriorityForJobDefinition(null, 42);
      fail("should not succeed");
    } catch (NotValidException e) {
      // happy path
      assertTextPresentIgnoreCase("jobDefinitionId is null", e.getMessage());
    }

    try {
      managementService.setOverridingJobPriorityForJobDefinition(null, 42, true);
      fail("should not succeed");
    } catch (NotValidException e) {
      // happy path
      assertTextPresentIgnoreCase("jobDefinitionId is null", e.getMessage());
    }
  }

  public void testResetNullJobDefinitionPriority() {
    try {
      managementService.clearOverridingJobPriorityForJobDefinition(null);
      fail("should not succeed");
    } catch (NotValidException e) {
      // happy path
      assertTextPresentIgnoreCase("jobDefinitionId is null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/asyncTaskProcess.bpmn20.xml")
  public void testSetJobDefinitionPriorityToExtremeValues() {
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // it is possible to set the max long value
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), Long.MAX_VALUE);
    jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    assertEquals(Long.MAX_VALUE, (long) jobDefinition.getOverridingJobPriority());

    // it is possible to set the min long value
    managementService.setOverridingJobPriorityForJobDefinition(jobDefinition.getId(), Long.MIN_VALUE + 1); // +1 for informix
    jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    assertEquals(Long.MIN_VALUE + 1, (long) jobDefinition.getOverridingJobPriority());
  }

  protected Job getJobThatIsNot(Job other) {
    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(2, jobs.size());

    if (jobs.get(0).getId().equals(other.getId())) {
      return jobs.get(1);
    }
    else if (jobs.get(1).getId().equals(other.getId())){
      return jobs.get(0);
    }
    else {
      throw new ProcessEngineException("Job with id " + other.getId() + " does not exist anymore");
    }
  }

}
