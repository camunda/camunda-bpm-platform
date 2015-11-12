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
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.cmd.SetJobDefinitionPriorityCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendJobCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendJobDefinitionCmd;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.concurrency.ControllableThread;
import org.camunda.bpm.engine.test.concurrency.ControlledCommand;
import org.slf4j.Logger;

/**
 * @author Daniel Meyer
 *
 */
public class JobDefinitionFunctionalTest extends PluggableProcessEngineTestCase {

private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  @Deployment(resources={"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void testCreateJobInstanceSuspended() {

    // given suspended job definition:
    managementService.suspendJobDefinitionByProcessDefinitionKey("simpleAsyncProcess");

    // if I start a new instance
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // then the new job instance is created as suspended:
    assertNotNull(managementService.createJobQuery().suspended().singleResult());
    assertNull(managementService.createJobQuery().active().singleResult());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void testCreateJobInstanceActive() {

    // given that the job definition is not suspended:

    // if I start a new instance
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // then the new job instance is created as active:
    assertNull(managementService.createJobQuery().suspended().singleResult());
    assertNotNull(managementService.createJobQuery().active().singleResult());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void testJobExecutorOnlyAcquiresActiveJobs() {

    // given suspended job definition:
    managementService.suspendJobDefinitionByProcessDefinitionKey("simpleAsyncProcess");

    // if I start a new instance
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // then the new job executor will not acquire the job:
    AcquiredJobs acquiredJobs = acquireJobs();
    assertEquals(0, acquiredJobs.size());

    // -------------------------

    // given a active job definition:
    managementService.activateJobDefinitionByProcessDefinitionKey("simpleAsyncProcess", true);

    // then the new job executor will not acquire the job:
    acquiredJobs = acquireJobs();
    assertEquals(1, acquiredJobs.size());
  }

  @Deployment
  public void testExclusiveJobs() {

    JobDefinition jobDefinition = managementService.createJobDefinitionQuery()
      .activityIdIn("task2")
      .singleResult();

    // given that the second task is suspended
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // if I start a process instance
    runtimeService.startProcessInstanceByKey("testProcess");

    waitForJobExecutorToProcessAllJobs(6000);

    // then the second task is not executed
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    // there is a suspended job instance
    Job job = managementService.createJobQuery()
      .singleResult();
    assertEquals(job.getJobDefinitionId(), jobDefinition.getId());
    assertTrue(job.isSuspended());

    // if I unsuspend the job definition, the job is executed:
    managementService.activateJobDefinitionById(jobDefinition.getId(), true);

    waitForJobExecutorToProcessAllJobs(5000);

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }


  ////////////////////////////////////////////////////////////////////////////////////////////////
  // The following are testcases which frame the behavior of concurrent job execution, acquisition & suspension

  @Deployment(resources={"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void testSuspendJobDuringAcquisition() {

    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // given a waiting acquisition and a waiting suspension
    JobAcquisitionThread acquisitionThread = new JobAcquisitionThread();
    acquisitionThread.startAndWaitUntilControlIsReturned();

    JobSuspensionThread jobSuspensionThread = new JobSuspensionThread("simpleAsyncProcess");
    jobSuspensionThread.startAndWaitUntilControlIsReturned();

    // first complete suspension:
    jobSuspensionThread.proceedAndWaitTillDone();
    acquisitionThread.proceedAndWaitTillDone();

    // then the acquisition will not fail with optimistic locking
    assertNull(jobSuspensionThread.exception);
    assertNull(acquisitionThread.exception);
    // but the job will also not be acquired
    assertEquals(0, acquisitionThread.acquiredJobs.size());

    //--------------------------------------------

    // given a waiting acquisition and a waiting suspension
    acquisitionThread = new JobAcquisitionThread();
    acquisitionThread.startAndWaitUntilControlIsReturned();

    jobSuspensionThread = new JobSuspensionThread("simpleAsyncProcess");
    jobSuspensionThread.startAndWaitUntilControlIsReturned();

    // first complete acquisition:
    acquisitionThread.proceedAndWaitTillDone();
    jobSuspensionThread.proceedAndWaitTillDone();

    // then there are no optimistic locking exceptions
    assertNull(jobSuspensionThread.exception);
    assertNull(acquisitionThread.exception);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml"})
  public void testSuspendJobDuringExecution() {

    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    Job job = managementService.createJobQuery().singleResult();

    // given a waiting execution and a waiting suspension
    JobExecutionThread executionthread = new JobExecutionThread(job.getId());
    executionthread.startAndWaitUntilControlIsReturned();

    JobSuspensionThread jobSuspensionThread = new JobSuspensionThread("simpleAsyncProcess");
    jobSuspensionThread.startAndWaitUntilControlIsReturned();

    // first complete suspension:
    jobSuspensionThread.proceedAndWaitTillDone();
    executionthread.proceedAndWaitTillDone();

    // then the execution will fail with optimistic locking
    assertNull(jobSuspensionThread.exception);
    assertNotNull(executionthread.exception);

    //--------------------------------------------

    // given a waiting execution and a waiting suspension
    executionthread = new JobExecutionThread(job.getId());
    executionthread.startAndWaitUntilControlIsReturned();

    jobSuspensionThread = new JobSuspensionThread("simpleAsyncProcess");
    jobSuspensionThread.startAndWaitUntilControlIsReturned();

    // first complete execution:
    executionthread.proceedAndWaitTillDone();
    jobSuspensionThread.proceedAndWaitTillDone();

    // then there are no optimistic locking exceptions
    assertNull(jobSuspensionThread.exception);
    assertNull(executionthread.exception);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/jobexecutor/JobDefinitionFunctionalTest.testRunningInstance.bpmn"})
  public void testNewSuspendedJobDuringRunningInstance() {
    // given
    // a process definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // a running instance
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    // suspend the process definition (and the job definitions)
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    // assert that there still exists a running and active process instance
    assertEquals(1, runtimeService.createProcessInstanceQuery().active().count());

    // when
    runtimeService.signal(processInstance.getId());

    // then
    // there should be one suspended job
    assertEquals(1, managementService.createJobQuery().suspended().count());
    assertEquals(0, managementService.createJobQuery().active().count());

    assertEquals(1, runtimeService.createProcessInstanceQuery().active().count());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testUpdateJobDefinitionPriority() {
    // given
    // two running instances
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // and a job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // and two jobs
    List<Job> jobs = managementService.createJobQuery().list();

    // when the first job is executed but has not yet committed
    JobExecutionThread executionThread = new JobExecutionThread(jobs.get(0).getId());
    executionThread.startAndWaitUntilControlIsReturned();

    // and the job priority is updated
    JobDefinitionPriorityThread priorityThread = new JobDefinitionPriorityThread(jobDefinition.getId(), 42L, true);
    priorityThread.startAndWaitUntilControlIsReturned();

    // and the priority threads commits first
    priorityThread.proceedAndWaitTillDone();

    // then both jobs priority has changed
    List<Job> currentJobs = managementService.createJobQuery().list();
    for (Job job : currentJobs) {
      assertEquals(42, job.getPriority());
    }

    // and the execution thread can nevertheless successfully finish job execution
    executionThread.proceedAndWaitTillDone();

    assertNull(executionThread.exception);

    // and ultimately only one job with an updated priority is left
    Job remainingJob = managementService.createJobQuery().singleResult();
    assertNotNull(remainingJob);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testParallelSuspensionAndPriorityUpdate() {
    // given
    // two running instances (ie two jobs)
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");

    // a job definition
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // when suspending the jobs is attempted
    JobSuspensionByJobDefinitionThread suspensionThread = new JobSuspensionByJobDefinitionThread(jobDefinition.getId());
    suspensionThread.startAndWaitUntilControlIsReturned();

    // and updating the priority is attempted
    JobDefinitionPriorityThread priorityUpdateThread = new JobDefinitionPriorityThread(jobDefinition.getId(), 42L, true);
    priorityUpdateThread.startAndWaitUntilControlIsReturned();

    // and both commands overlap each other
    suspensionThread.proceedAndWaitTillDone();
    priorityUpdateThread.proceedAndWaitTillDone();

    // then both updates have been performed
    List<Job> updatedJobs = managementService.createJobQuery().list();
    assertEquals(2, updatedJobs.size());
    for (Job job : updatedJobs) {
      assertEquals(42, job.getPriority());
      assertTrue(job.isSuspended());
    }
  }

  protected AcquiredJobs acquireJobs() {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();

    return processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new AcquireJobsCmd(jobExecutor));
  }

  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;

  public class JobAcquisitionThread extends ControllableThread {
    OptimisticLockingException exception;
    AcquiredJobs acquiredJobs;
    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
        acquiredJobs = (AcquiredJobs) processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new AcquireJobsCmd(jobExecutor)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  public class JobExecutionThread extends ControllableThread {
    OptimisticLockingException exception;
    String jobId;

    public JobExecutionThread(String jobId) {
      this.jobId = jobId;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new ExecuteJobsCmd(jobId)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  public class JobSuspensionThread extends ControllableThread {
    OptimisticLockingException exception;
    String processDefinitionKey;

    public JobSuspensionThread(String processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new SuspendJobDefinitionCmd(null, null, processDefinitionKey, true, null)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  public class JobSuspensionByJobDefinitionThread extends ControllableThread {
    OptimisticLockingException exception;
    String jobDefinitionId;

    public JobSuspensionByJobDefinitionThread(String jobDefinitionId) {
      this.jobDefinitionId = jobDefinitionId;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new SuspendJobCmd(null, jobDefinitionId, null, null, null)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }
  }

  public class JobDefinitionPriorityThread extends ControllableThread {
    OptimisticLockingException exception;
    String jobDefinitionId;
    Long priority;
    boolean cascade;

    public JobDefinitionPriorityThread(String jobDefinitionId, Long priority, boolean cascade) {
      this.jobDefinitionId = jobDefinitionId;
      this.priority = priority;
      this.cascade = cascade;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new SetJobDefinitionPriorityCmd(jobDefinitionId, priority, cascade)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
    }
  }

}
