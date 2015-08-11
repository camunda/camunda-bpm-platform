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
package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class NotifyAcquisitionRejectedJobsHandler implements RejectedJobsHandler {

  @Override
  public void jobsRejected(List<String> jobIds, ProcessEngineImpl processEngine, JobExecutor jobExecutor) {
    AcquireJobsRunnable acquireJobsRunnable = jobExecutor.getAcquireJobsRunnable();
    if (acquireJobsRunnable instanceof SequentialJobAcquisitionRunnable) {
      JobAcquisitionContext context = ((SequentialJobAcquisitionRunnable) acquireJobsRunnable).getAcquisitionContext();
      context.submitRejectedBatch(processEngine.getName(), jobIds);
    }
    else {
      jobExecutor.getExecuteJobsRunnable(jobIds, processEngine).run();
    }

  }

}
