/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.batch;

/**
 * <p>
 *  Additional statistics for a single batch.
 * </p>
 *
 * <p>
 *   Contains the number of remaining jobs, completed and failed batch
 *   execution jobs. The following relation between these exists:
 *
 *   <code>
 *     batch total jobs = remaining jobs + completed jobs
 *   </code>
 * </p>
 */
public interface BatchStatistics extends Batch {
  /**
   * <p>
   *   The number of remaining batch execution jobs.
   *   This does include failed batch execution jobs and
   *   batch execution jobs which still have to be created by the seed job.
   * </p>
   *
   * <p>
   *   See
   *   {@link #getTotalJobs()} for the number of all batch execution jobs,
   *   {@link #getCompletedJobs()} for the number of completed batch execution jobs and
   *   {@link #getFailedJobs()} for the number of failed batch execution jobs.
   * </p>
   *
   * @return the number of remaining batch execution jobs
   */
  int getRemainingJobs();

  /**
   * <p>
   *   The number of completed batch execution jobs.
   *   This does include aborted/deleted batch execution jobs.
   * </p>
   *
   * <p>
   *   See
   *   {@link #getTotalJobs()} for the number of all batch execution jobs,
   *   {@link #getRemainingJobs()} ()} for the number of remaining batch execution jobs and
   *   {@link #getFailedJobs()} for the number of failed batch execution jobs.
   * </p>
   *
   * @return the number of completed batch execution jobs
   */
  int getCompletedJobs();

  /**
   * <p>
   *   The number of failed batch execution jobs.
   *   This does not include aborted or deleted batch execution jobs.
   * </p>
   *
   * <p>
   *   See
   *   {@link #getTotalJobs()} for the number of all batch execution jobs,
   *   {@link #getRemainingJobs()} ()} for the number of remaining batch execution jobs and
   *   {@link #getCompletedJobs()} ()} for the number of completed batch execution jobs.
   * </p>
   *
   * @return the number of failed batch execution jobs
   */
  int getFailedJobs();

}
