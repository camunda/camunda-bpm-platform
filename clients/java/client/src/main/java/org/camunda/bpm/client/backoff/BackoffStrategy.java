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

package org.camunda.bpm.client.backoff;

import org.camunda.bpm.client.task.ExternalTask;

import java.util.List;

/**
 * <p>The BackoffStrategy provides a way to define a back off between fetch and lock requests.</p>
 *
 * <p>Since an implementation of this interface may be executed by multiple threads,
 * it is recommended that a BackoffStrategy is implemented in a thread-safe manner.</p>
 *
 * @author Nikola Koevski
 */
public interface BackoffStrategy {

  /**
   * Is invoked before the wait time calculation in order to check the conditions for executing the
   * backoff strategy and do a reconfiguration of the backoff strategy parameters if needed.
   * The implementation might realize a strategy reset.
   *
   * @param externalTasks the list of retrieved external tasks
   * @return a boolean indicating if a backoff strategy should be invoked
   */
  void reconfigure(List<ExternalTask> externalTasks);

  /**
   * Is invoked when the retrieved external tasks do not satisfy the defined conditions.
   * The implementation might realize a back off between fetch and lock requests.
   */
  long calculateBackoffTime();
}
