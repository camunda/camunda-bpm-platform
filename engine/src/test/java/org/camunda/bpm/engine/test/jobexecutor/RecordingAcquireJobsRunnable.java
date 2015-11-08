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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobAcquisitionStrategy;
import org.camunda.bpm.engine.impl.jobexecutor.JobAcquisitionContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.SequentialJobAcquisitionRunnable;

/**
 * @author Thorben Lindhauer
 *
 */
public class RecordingAcquireJobsRunnable extends SequentialJobAcquisitionRunnable {

  protected List<RecordedWaitEvent> waitEvents = new ArrayList<RecordedWaitEvent>();
  protected List<RecordedAcquisitionEvent> acquisitionEvents = new ArrayList<RecordedAcquisitionEvent>();

  public RecordingAcquireJobsRunnable(JobExecutor jobExecutor) {
    super(jobExecutor);
  }

  @Override
  protected void suspendAcquisition(long millis) {
    // don't actually wait
  }

  @Override
  protected AcquiredJobs acquireJobs(JobAcquisitionContext context, JobAcquisitionStrategy configuration, ProcessEngineImpl currentProcessEngine) {
    acquisitionEvents.add(new RecordedAcquisitionEvent(System.currentTimeMillis(), configuration.getNumJobsToAcquire(currentProcessEngine.getName())));
    return super.acquireJobs(context, configuration, currentProcessEngine);
  }

  public List<RecordedWaitEvent> getWaitEvents() {
    return waitEvents;
  }

  public List<RecordedAcquisitionEvent> getAcquisitionEvents() {
    return acquisitionEvents;
  }

  protected void configureNextAcquisitionCycle(JobAcquisitionContext acquisitionContext, JobAcquisitionStrategy acquisitionStrategy) {
    super.configureNextAcquisitionCycle(acquisitionContext, acquisitionStrategy);

    long timeBetweenCurrentAndNextAcquisition = acquisitionStrategy.getWaitTime();
    waitEvents.add(new RecordedWaitEvent(System.currentTimeMillis(), timeBetweenCurrentAndNextAcquisition));
  }

  public static class RecordedWaitEvent {

    protected long timestamp;
    protected long timeBetweenAcquisitions;

    public RecordedWaitEvent(long timestamp, long timeBetweenAcquisitions) {
      this.timestamp = timestamp;
      this.timeBetweenAcquisitions = timeBetweenAcquisitions;
    }

    public long getTimestamp() {
      return timestamp;
    }
    public long getTimeBetweenAcquisitions() {
      return timeBetweenAcquisitions;
    }
  }

  public static class RecordedAcquisitionEvent {
    protected long timestamp;
    protected int numJobsToAcquire;

    public RecordedAcquisitionEvent(long timestamp, int numJobsToAcquire) {
      this.timestamp = timestamp;
      this.numJobsToAcquire = numJobsToAcquire;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public int getNumJobsToAcquire() {
      return numJobsToAcquire;
    }
  }

}
