/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingListener;
import org.camunda.bpm.engine.impl.db.entitymanager.OptimisticLockingResult;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AcquireJobsCommandFactory;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestHelper.ControllableCommand;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestHelper.ThreadControl;
import org.camunda.bpm.engine.test.concurrency.ControllableThread;

/**
 * Job executor that uses a {@link ControllableThread} for job acquisition. That means,
 * the job acquisition thread returns control with each iteration of acquiring jobs (specifically
 * between selecting jobs and returning them to the acquisition runnable).
 *
 * @author Thorben Lindhauer
 */
public class ControllableJobExecutor extends JobExecutor {

  protected ThreadControl acquisitionThreadControl;
  protected ThreadControl executionThreadControl;

  protected boolean syncOnShutdown = true;
  protected boolean syncAsSuspendEnabled;

  protected boolean shouldThrowOle;
  protected boolean oleThrown;

  public ControllableJobExecutor() {
    acquireJobsRunnable = new RecordingAcquireJobsRunnable(this);
    jobAcquisitionThread = new Thread(acquireJobsRunnable);
    acquisitionThreadControl = new ThreadControl(jobAcquisitionThread);
    executionThreadControl = new ThreadControl(jobAcquisitionThread); // execution thread is same as acquisition thread
    acquireJobsCmdFactory = new ControllableJobAcquisitionCommandFactory();
  }

  /**
   * <p>Creates the job executor and registers the given process engine
   * with it.
   *
   * <p>Use this constructor if the process engine is not registered
   * with the job executor when the process engine is bootstrapped.
   *
   * <p>Note: this is a hack since it enables to use multiple job executors with
   * the same engine which is not a supported feature (and for example clashes with
   * processEngineConfiguration#getJobExecutor)
   */
  public ControllableJobExecutor(ProcessEngineImpl processEngine) {
    this();
    processEngines.add(processEngine);
  }

  public ControllableJobExecutor(boolean syncAsSuspendEnabled) {
    this();
    this.syncAsSuspendEnabled = syncAsSuspendEnabled;
  }

  public boolean isSyncAsSuspendEnabled() {
    return syncAsSuspendEnabled;
  }

  /**
   * <p>true: behave like embedded job executor where shutdown waits for all jobs to end
   * <p>false: behave like runtime container job executor where shutdown does not influence job execution
   */
  public ControllableJobExecutor proceedAndWaitOnShutdown(boolean syncOnShutdown) {
    this.syncOnShutdown = syncOnShutdown;
    return this;
  }

  protected void ensureInitialization() {
    // already initialized in constructor
  }

  public ThreadControl getAcquisitionThreadControl() {
    return acquisitionThreadControl;
  }

  public ThreadControl getExecutionThreadControl() {
    return executionThreadControl;
  }

  protected void startExecutingJobs() {
    jobAcquisitionThread.start();
  }

  protected void stopExecutingJobs() {
    if (syncOnShutdown) {
      acquisitionThreadControl.waitUntilDone(true);
    }
  }

  @Override
  public RecordingAcquireJobsRunnable getAcquireJobsRunnable() {
    return (RecordingAcquireJobsRunnable) super.getAcquireJobsRunnable();
  }

  public void executeJobs(List<String> jobIds, ProcessEngineImpl processEngine) {
    getExecuteJobsRunnable(jobIds, processEngine).run();
  }

  public class ControllableJobAcquisitionCommandFactory implements AcquireJobsCommandFactory {

    public Command<AcquiredJobs> getCommand(int numJobsToAcquire) {
      return new ControllableAcquisitionCommand(acquisitionThreadControl, numJobsToAcquire);
    }
  }

  public class ControllableAcquisitionCommand extends ControllableCommand<AcquiredJobs> {

    protected AcquireJobsCmd wrappedCommand;
    
    public ControllableAcquisitionCommand(ThreadControl threadControl, int numJobsToAcquire) {
      super(threadControl);
      this.wrappedCommand = new AcquireJobsCmd(ControllableJobExecutor.this, numJobsToAcquire);
    }

    public AcquiredJobs execute(CommandContext commandContext) {

      if (shouldThrowOle) {
        rethrowOptimisticLockingException(commandContext);
      }

      monitor.sync(); // wait till makeContinue() is called from test thread

      AcquiredJobs acquiredJobs = wrappedCommand.execute(commandContext);

      monitor.sync(); // wait till makeContinue() is called from test thread

      return acquiredJobs;
    }
    
    @Override
    public boolean isRetryable() {
      return wrappedCommand.isRetryable();
    }

    protected void rethrowOptimisticLockingException(CommandContext commandContext) {
      commandContext.getDbEntityManager().registerOptimisticLockingListener(new OptimisticLockingListener() {

        @Override
        public Class<? extends DbEntity> getEntityType() {
          return JobEntity.class;
        }

        @Override
        public OptimisticLockingResult failedOperation(DbOperation operation) {
          oleThrown = true;

          // Mark OLE as handled
          return OptimisticLockingResult.IGNORE;
        }

      });
    }

  }

  public void indicateOptimisticLockingException() {
    shouldThrowOle = true;
  }

  public boolean isOleThrown() {
    return oleThrown;
  }

  public void resetOleThrown() {
    oleThrown = false;
  }

}
