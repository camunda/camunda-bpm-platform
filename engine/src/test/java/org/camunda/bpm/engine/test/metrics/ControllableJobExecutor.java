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
package org.camunda.bpm.engine.test.metrics;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.SequentialJobAcquisitionRunnable;
import org.camunda.bpm.engine.test.concurrency.ControllableThread;
import org.camunda.bpm.engine.test.concurrency.ControlledCommand;

/**
 * Job executor that uses a {@link ControllableThread} for job acquisition. That means,
 * the job acquisition thread returns control with each iteration of acquiring jobs (specifically
 * between selecting jobs and returning them to the acquisition runnable).
 *
 * @author Thorben Lindhauer
 */
public class ControllableJobExecutor extends JobExecutor {

  public ControllableJobExecutor(ProcessEngineImpl processEngine) {
    acquireJobsRunnable = new SequentialJobAcquisitionRunnable(this);
    jobAcquisitionThread = new ControllableThread(acquireJobsRunnable);
    acquireJobsCmd = new ControlledCommand<AcquiredJobs>((ControllableThread) jobAcquisitionThread,
        new AcquireJobsCmd(this));
    processEngines.add(processEngine);
  }

  protected void ensureInitialization() {
    // already initialized in constructor
  }

  public ControllableThread getJobAcquisitionThread() {
    return (ControllableThread) jobAcquisitionThread;
  }

  protected void startExecutingJobs() {
    ((ControllableThread) jobAcquisitionThread).startAndWaitUntilControlIsReturned();
  }

  protected void stopExecutingJobs() {
    ((ControllableThread) jobAcquisitionThread).proceedAndWaitTillDone();
  }

  public void executeJobs(List<String> jobIds, ProcessEngineImpl processEngine) {
    // TODO: could use controllable threads for job execution
    new ExecuteJobsRunnable(jobIds, processEngine).run();
  }

}
