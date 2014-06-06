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

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
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

/**
 * @author Daniel Meyer
 *
 */
public class JobDefinitionFunctionalTest extends PluggableProcessEngineTestCase {

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

    // then the acquisition will fail with optimistic locking
    assertNull(jobSuspensionThread.exception);
    assertNotNull(acquisitionThread.exception);

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

  protected AcquiredJobs acquireJobs() {
    return processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new AcquireJobsCmd(processEngineConfiguration.getJobExecutor()));
  }

  Thread testThread = Thread.currentThread();
  static ControllableThread activeThread;

  public class JobAcquisitionThread extends ControllableThread {
    OptimisticLockingException exception;
    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    public void run() {
      try {
        JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
        processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand(activeThread, new AcquireJobsCmd(jobExecutor)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      log.fine(getName()+" ends");
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
      log.fine(getName()+" ends");
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
      log.fine(getName()+" ends");
    }
  }

}
