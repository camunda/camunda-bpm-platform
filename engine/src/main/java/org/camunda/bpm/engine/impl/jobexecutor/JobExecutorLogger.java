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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * @author Daniel Meyer
 *
 */
public class JobExecutorLogger extends ProcessEngineLogger {

  public void debugAcquiredJobNotFound(String jobId) {
    logDebug(
        "001", "Acquired job with id '{}' not found.", jobId);
  }

  public void exceptionWhileExecutingJob(JobEntity job, RuntimeException exception) {
    logWarn(
        "002", "Exception while executing job {}: {}", job, exception.getMessage());
  }

  public void debugFallbackToDefaultRetryStrategy() {
    logDebug(
        "003", "Falling back to default retry stratefy");
  }

  public void debugDecrementingRetriesForJob(String id) {
    logDebug(
        "004", "Decrementing retries of job {}", id);
  }

  public void debugInitiallyAppyingRetryCycleForJob(String id, int times) {
    logDebug(
        "005", "Applying job retry time cycle for the first time for job {}, retires {}", id, times);
  }

}
