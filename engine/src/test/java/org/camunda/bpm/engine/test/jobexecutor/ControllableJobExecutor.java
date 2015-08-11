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

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.AcquireJobsCommandFactory;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestCase.ControllableCommand;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestCase.ThreadControl;
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

  public ControllableJobExecutor(ProcessEngineImpl processEngine) {
    acquireJobsRunnable = new RecordingAcquireJobsRunnable(this);
    jobAcquisitionThread = new Thread(acquireJobsRunnable);
    acquisitionThreadControl = new ThreadControl(jobAcquisitionThread);
    acquireJobsCmdFactory = new ControllableJobAcquisitionCommandFactory();
    processEngines.add(processEngine);
  }

  protected void ensureInitialization() {
    // already initialized in constructor
  }

  public ThreadControl getAcquisitionThreadControl() {
    return acquisitionThreadControl;
  }

  protected void startExecutingJobs() {
    jobAcquisitionThread.start();
  }

  protected void stopExecutingJobs() {
    acquisitionThreadControl.waitUntilDone(true);
  }

  @Override
  public RecordingAcquireJobsRunnable getAcquireJobsRunnable() {
    return (RecordingAcquireJobsRunnable) super.getAcquireJobsRunnable();
  }

  public void executeJobs(List<String> jobIds, ProcessEngineImpl processEngine) {
    // TODO: could use controllable threads for job execution
    getExecuteJobsRunnable(jobIds, processEngine).run();
  }

  public class ControllableJobAcquisitionCommandFactory implements AcquireJobsCommandFactory {

    public Command<AcquiredJobs> getCommand(int numJobsToAcquire) {
      return new ControllableAcquisitionCommand(acquisitionThreadControl, numJobsToAcquire);
    }
  }

  public class ControllableAcquisitionCommand extends ControllableCommand<AcquiredJobs> {

    protected int numJobsToAcquire;

    public ControllableAcquisitionCommand(ThreadControl threadControl, int numJobsToAcquire) {
      super(threadControl);
      this.numJobsToAcquire = numJobsToAcquire;
    }

    public AcquiredJobs execute(CommandContext commandContext) {

      monitor.sync(); // wait till makeContinue() is called from test thread

      AcquiredJobs acquiredJobs = new AcquireJobsCmd(ControllableJobExecutor.this, numJobsToAcquire).execute(commandContext);

      monitor.sync(); // wait till makeContinue() is called from test thread

      return acquiredJobs;
    }

  }

}
