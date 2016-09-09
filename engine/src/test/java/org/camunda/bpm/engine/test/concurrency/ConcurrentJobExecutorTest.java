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
package org.camunda.bpm.engine.test.concurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.cmd.SetJobDefinitionPriorityCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendJobCmd;
import org.camunda.bpm.engine.impl.cmd.SuspendJobDefinitionCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobHelper;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobFailureCollector;
import org.camunda.bpm.engine.impl.management.UpdateJobDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.management.UpdateJobSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.slf4j.Logger;

/**
 * @author Thorben Lindhauer
 *
 */
public class ConcurrentJobExecutorTest {

  private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);


  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected Thread testThread = Thread.currentThread();
  protected static ControllableThread activeThread;

  protected static final BpmnModelInstance SIMPLE_ASYNC_PROCESS = Bpmn.createExecutableProcess("simpleAsyncProcess")
      .startEvent()
      .serviceTask()
        .camundaExpression("${true}")
        .camundaAsyncBefore()
      .endEvent()
      .done();

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
    managementService = engineRule.getManagementService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void deleteJobs() {
    for(final Job job : managementService.createJobQuery().list()) {

      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

        public Void execute(CommandContext commandContext) {
          ((JobEntity) job).delete();
          return null;
        }
      });
    }
  }

  @Test
  public void testCompetingJobExecutionDeleteJobDuringExecution() {
    //given a simple process with a async service task
    testRule.deploy(Bpmn
            .createExecutableProcess("process")
              .startEvent()
              .serviceTask("task")
                .camundaAsyncBefore()
                .camundaExpression("${true}")
              .endEvent()
            .done());
    runtimeService.startProcessInstanceByKey("process");
    Job currentJob = managementService.createJobQuery().singleResult();

    // when a job is executed
    JobExecutionThread threadOne = new JobExecutionThread(currentJob.getId());
    threadOne.startAndWaitUntilControlIsReturned();
    //and deleted in parallel
    managementService.deleteJob(currentJob.getId());

    // then the job fails with a OLE and the failed job listener throws no NPE
    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertTrue(threadOne.exception instanceof OptimisticLockingException);
  }

  @Test
  @Deployment
  public void testCompetingJobExecutionDefaultRetryStrategy() {
    // given an MI subprocess with two instances
    runtimeService.startProcessInstanceByKey("miParallelSubprocess");

    List<Job> currentJobs = managementService.createJobQuery().list();
    assertEquals(2, currentJobs.size());

    // when the jobs are executed in parallel
    JobExecutionThread threadOne = new JobExecutionThread(currentJobs.get(0).getId());
    threadOne.startAndWaitUntilControlIsReturned();

    JobExecutionThread threadTwo = new JobExecutionThread(currentJobs.get(1).getId());
    threadTwo.startAndWaitUntilControlIsReturned();

    // then the first committing thread succeeds
    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    // then the second committing thread fails with an OptimisticLockingException
    // and the job retries have not been decremented
    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);

    Job remainingJob = managementService.createJobQuery().singleResult();
    assertEquals(currentJobs.get(1).getRetries(), remainingJob.getRetries());

    assertNotNull(remainingJob.getExceptionMessage());

    JobEntity jobEntity = (JobEntity) remainingJob;
    assertNull(jobEntity.getLockOwner());

    // and there is no lock expiration time due to the default retry strategy
    assertNull(jobEntity.getLockExpirationTime());
  }

  @Test
  @Deployment
  public void testCompetingJobExecutionFoxRetryStrategy() {
    // given an MI subprocess with two instances
    runtimeService.startProcessInstanceByKey("miParallelSubprocess");

    List<Job> currentJobs = managementService.createJobQuery().list();
    assertEquals(2, currentJobs.size());

    // when the jobs are executed in parallel
    JobExecutionThread threadOne = new JobExecutionThread(currentJobs.get(0).getId());
    threadOne.startAndWaitUntilControlIsReturned();

    JobExecutionThread threadTwo = new JobExecutionThread(currentJobs.get(1).getId());
    threadTwo.startAndWaitUntilControlIsReturned();

    // then the first committing thread succeeds
    LOG.debug("test thread notifies thread 1");
    threadOne.proceedAndWaitTillDone();
    assertNull(threadOne.exception);

    // then the second committing thread fails with an OptimisticLockingException
    // and the job retries have not been decremented
    LOG.debug("test thread notifies thread 2");
    threadTwo.proceedAndWaitTillDone();
    assertNotNull(threadTwo.exception);

    Job remainingJob = managementService.createJobQuery().singleResult();
    // retries are configured as R5/PT5M, so no decrement means 5 retries left
    assertEquals(5, remainingJob.getRetries());

    assertNotNull(remainingJob.getExceptionMessage());

    JobEntity jobEntity = (JobEntity) remainingJob;
    assertNull(jobEntity.getLockOwner());

    // and there is a custom lock expiration time
    assertNotNull(jobEntity.getLockExpirationTime());
  }

  @Test
  public void testCompletingJobExecutionSuspendDuringExecution() {
    testRule.deploy(SIMPLE_ASYNC_PROCESS);

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

  @Test
  public void testCompletingSuspendJobDuringAcquisition() {
    testRule.deploy(SIMPLE_ASYNC_PROCESS);

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

  @Test
  public void testCompletingSuspendedJobDuringRunningInstance() {
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
        .receiveTask()
        .intermediateCatchEvent()
          .timerWithDuration("PT0M")
        .endEvent()
        .done());

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

  @Test
  public void testCompletingUpdateJobDefinitionPriorityDuringExecution() {
    testRule.deploy(SIMPLE_ASYNC_PROCESS);

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

  @Test
  public void testCompletingSuspensionJobDuringPriorityUpdate() {
    testRule.deploy(SIMPLE_ASYNC_PROCESS);

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


  public class JobExecutionThread extends ControllableThread {

    OptimisticLockingException exception;
    String jobId;

    JobExecutionThread(String jobId) {
      this.jobId = jobId;
    }

    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }

    @Override
    public void run() {
      try {
        JobFailureCollector jobFailureCollector = new JobFailureCollector(jobId);
        ExecuteJobHelper.executeJob(jobId, processEngineConfiguration.getCommandExecutorTxRequired(),jobFailureCollector, new ControlledCommand<Void>(activeThread, new ExecuteJobsCmd(jobId, jobFailureCollector)));

      }
      catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName() + " ends");
    }
  }

  public class JobAcquisitionThread extends ControllableThread {
    OptimisticLockingException exception;
    AcquiredJobs acquiredJobs;
    @Override
    public synchronized void startAndWaitUntilControlIsReturned() {
      activeThread = this;
      super.startAndWaitUntilControlIsReturned();
    }
    @Override
    public void run() {
      try {
        JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
        acquiredJobs = processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand<AcquiredJobs>(activeThread, new AcquireJobsCmd(jobExecutor)));

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
    @Override
    public void run() {
      try {
        processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand<Void>(activeThread, createSuspendJobCommand()));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }

    protected Command<Void> createSuspendJobCommand() {
      UpdateJobDefinitionSuspensionStateBuilderImpl builder = new UpdateJobDefinitionSuspensionStateBuilderImpl()
        .byProcessDefinitionKey(processDefinitionKey)
        .includeJobs(true);

      return new SuspendJobDefinitionCmd(builder);
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
    @Override
    public void run() {
      try {
        processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand<Void>(activeThread, createSuspendJobCommand()));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
      LOG.debug(getName()+" ends");
    }

    protected SuspendJobCmd createSuspendJobCommand() {
      UpdateJobSuspensionStateBuilderImpl builder = new UpdateJobSuspensionStateBuilderImpl().byJobDefinitionId(jobDefinitionId);
      return new SuspendJobCmd(builder);
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
    @Override
    public void run() {
      try {
        processEngineConfiguration.getCommandExecutorTxRequired()
          .execute(new ControlledCommand<Void>(activeThread, new SetJobDefinitionPriorityCmd(jobDefinitionId, priority, cascade)));

      } catch (OptimisticLockingException e) {
        this.exception = e;
      }
    }
  }

}
