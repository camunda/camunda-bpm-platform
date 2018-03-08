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
package org.camunda.bpm.client.task;

import org.camunda.bpm.client.exception.BpmnErrorException;
import org.camunda.bpm.client.exception.CompleteTaskException;
import org.camunda.bpm.client.exception.ExtendLockException;
import org.camunda.bpm.client.exception.TaskFailureException;
import org.camunda.bpm.client.exception.UnlockTaskException;

/**
 * <p>Service that provides possibilities to interact with fetched and locked tasks.</p>
 *
 * @author Tassilo Weidner
 */
public interface ExternalTaskService {

  /**
   * Unlocks a task and clears the tasks lock expiration time and worker id.
   *
   * @throws UnlockTaskException
   * <ul>
   *   <li> if the task has been canceled and therefore does not exist anymore
   *   <li> if the connection could not be established
   * </ul>
   */
  void unlock();

  /**
   * Completes a task.
   *
   * @throws CompleteTaskException
   * <ul>
   *   <li> if the task's most recent lock could not be acquired
   *   <li> if the task has been canceled and therefore does not exist anymore
   *   <li> if the corresponding process instance could not be resumed
   *   <li> if the connection could not be established
   * </ul>
   */
  void complete();

  /**
   * Reports a failure to execute a task. A number of retries and a timeout until
   * the task can be specified. If the retries are set to 0, an incident for this
   * task is created.
   *
   * @param errorMessage indicates the reason of the failure.
   * @param errorDetails provides a detailed error description.
   * @param retries      specifies how often the task should be retried. Must be >= 0.
   *                     If 0, an incident is created and the task cannot be fetched anymore
   *                     unless the retries are increased again. The incident's message is set
   *                     to the {@param errorMessage} parameter.
   * @param retryTimeout specifies a timeout in milliseconds before the external task
   *                     becomes available again for fetching. Must be >= 0.
   *
   * @throws TaskFailureException
   * <ul>
   *   <li> if the task's most recent lock could not be acquired
   *   <li> if the task has been canceled and therefore does not exist anymore
   *   <li> if the corresponding process instance could not be resumed
   *   <li> if the connection could not be established
   * </ul>
   */
  void failure(String errorMessage, String errorDetails, int retries, long retryTimeout);

  /**
   * Reports a business error in the context of a running task.
   * The error code must be specified to identify the BPMN error handler.
   *
   * @param errorCode that indicates the predefined error. The error code
   *                  is used to identify the BPMN error handler.
   *
   * @throws BpmnErrorException
   * <ul>
   *   <li> if the task's most recent lock could not be acquired
   *   <li> if the task has been canceled and therefore does not exist anymore
   *   <li> if the corresponding process instance could not be resumed
   *   <li> if the connection could not be established
   * </ul>
   */
  void bpmnError(String errorCode);

  /**
   * Extends the timeout of the lock by a given amount of time.
   *
   * @param newDuration specifies the the new lock duration in milliseconds
   *
   * @throws ExtendLockException
   * <ul>
   *   <li> if the task's most recent lock could not be acquired
   *   <li> if the task has been canceled and therefore does not exist anymore
   *   <li> if the connection could not be established
   * </ul>
   */
  void extendLock(long newDuration);

}
