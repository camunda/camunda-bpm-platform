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
package org.camunda.bpm.engine.test.bpmn.job;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobPriorityProvider;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobPrioritizationBpmnConstantValueTest extends PluggableProcessEngineTestCase {

  protected static final int EXPECTED_DEFAULT_PRIORITY = 0;

  protected void setUp() throws Exception {
    processEngineConfiguration.setProducePrioritizedJobs(true);
    processEngineConfiguration.setJobPriorityProvider(new DefaultJobPriorityProvider());
  }

  protected void tearDown() throws Exception {
    processEngineConfiguration.setProducePrioritizedJobs(false);
    processEngineConfiguration.setJobPriorityProvider(null);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/oneTaskProcess.bpmn20.xml")
  public void testDefaultPrioritizationAsyncBefore() {
    // when
    runtimeService
      .createProcessInstanceByKey("oneTaskProcess")
      .startBeforeActivity("task1")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(EXPECTED_DEFAULT_PRIORITY, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/oneTaskProcess.bpmn20.xml")
  public void testDefaultPrioritizationAsyncAfter() {
    // given
    runtimeService
      .createProcessInstanceByKey("oneTaskProcess")
      .startBeforeActivity("task1")
      .execute();

    // when
    managementService.executeJob(managementService.createJobQuery().singleResult().getId());

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(EXPECTED_DEFAULT_PRIORITY, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/oneTimerProcess.bpmn20.xml")
  public void testDefaultPrioritizationTimer() {
    // when
    runtimeService
      .createProcessInstanceByKey("oneTimerProcess")
      .startBeforeActivity("timer1")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(EXPECTED_DEFAULT_PRIORITY, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioProcess.bpmn20.xml")
  public void testProcessDefinitionPrioritizationAsyncBefore() {
    // when
    runtimeService
      .createProcessInstanceByKey("jobPrioProcess")
      .startBeforeActivity("task1")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(10, (int) job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioProcess.bpmn20.xml")
  public void testProcessDefinitionPrioritizationAsyncAfter() {
    // given
    runtimeService
      .createProcessInstanceByKey("jobPrioProcess")
      .startBeforeActivity("task1")
      .execute();

    // when
    managementService.executeJob(managementService.createJobQuery().singleResult().getId());

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(10, (int) job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/intermediateTimerJobPrioProcess.bpmn20.xml")
  public void testProcessDefinitionPrioritizationTimer() {
    // when
    runtimeService
      .createProcessInstanceByKey("intermediateTimerJobPrioProcess")
      .startBeforeActivity("timer1")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(8, (int) job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioProcess.bpmn20.xml")
  public void testActivityPrioritizationAsyncBefore() {
    // when
    runtimeService
      .createProcessInstanceByKey("jobPrioProcess")
      .startBeforeActivity("task2")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(5, (int) job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/jobPrioProcess.bpmn20.xml")
  public void testActivityPrioritizationAsyncAfter() {
    // given
    runtimeService
      .createProcessInstanceByKey("jobPrioProcess")
      .startBeforeActivity("task2")
      .execute();

    // when
    managementService.executeJob(managementService.createJobQuery().singleResult().getId());

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(5, (int) job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/intermediateTimerJobPrioProcess.bpmn20.xml")
  public void testActivityPrioritizationTimer() {
    // when
    runtimeService
      .createProcessInstanceByKey("intermediateTimerJobPrioProcess")
      .startBeforeActivity("timer2")
      .execute();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(4, (int) job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/subProcessJobPrioProcess.bpmn20.xml")
  public void testSubProcessPriorityIsNotDefaultForContainedActivities() {
    // when starting an activity contained in the sub process where the
    // sub process has job priority 20
    runtimeService
      .createProcessInstanceByKey("subProcessJobPrioProcess")
      .startBeforeActivity("task1")
      .execute();

    // then the job for that activity has priority 10 which is the process definition's
    // priority; the sub process priority is not inherited
    Job job = managementService.createJobQuery().singleResult();
    assertEquals(10, (int) job.getPriority());
  }

  public void testFailOnMalformedInput() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/job/invalidPrioProcess.bpmn20.xml")
        .deploy();
      fail("deploying a process with malformed priority should not succeed");
    } catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("value 'thisIsNotANumber' for attribute 'jobPriority' "
          + "is not a valid number", e.getMessage());
    }
  }

  public void testParsePriorityOnNonAsyncActivity() {

    // deploying a process definition where the activity
    // has a priority but defines no jobs succeeds
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/bpmn/job/JobPrioritizationBpmnTest.testParsePriorityOnNonAsyncActivity.bpmn20.xml")
      .deploy();

    // cleanup
    repositoryService.deleteDeployment(deployment.getId());
  }

  public void testTimerStartEventPriorityOnProcessDefinition() {
    // given a timer start job
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/job/JobPrioritizationBpmnConstantValueTest.testTimerStartEventPriorityOnProcessDefinition.bpmn20.xml")
        .deploy();

    Job job = managementService.createJobQuery().singleResult();

    // then the timer start job has the priority defined in the process definition
    assertEquals(8, job.getPriority());

    // cleanup
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  public void testTimerStartEventPriorityOnActivity() {
    // given a timer start job
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/job/JobPrioritizationBpmnConstantValueTest.testTimerStartEventPriorityOnActivity.bpmn20.xml")
        .deploy();

    Job job = managementService.createJobQuery().singleResult();

    // then the timer start job has the priority defined in the process definition
    assertEquals(1515, job.getPriority());

    // cleanup
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/boundaryTimerJobPrioProcess.bpmn20.xml")
  public void testBoundaryTimerEventPriority() {
    // given an active boundary event timer
    runtimeService.startProcessInstanceByKey("boundaryTimerJobPrioProcess");

    Job job = managementService.createJobQuery().singleResult();

    // then the job has the priority specified in the BPMN XML
    assertEquals(20, job.getPriority());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/eventSubprocessTimerJobPrioProcess.bpmn20.xml")
  public void testEventSubprocessTimerPriority() {
    // given an active event subprocess timer
    runtimeService.startProcessInstanceByKey("eventSubprocessTimerJobPrioProcess");

    Job job = managementService.createJobQuery().singleResult();

    // then the job has the priority specified in the BPMN XML
    assertEquals(25, job.getPriority());
  }
}
