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
package org.camunda.bpm.engine;

import java.util.Map;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;

/**
 * @author Thorben Lindhauer
 *
 */
public interface ExternalTaskService {

  /**
   * <p>Defines fetching of external tasks by using a fluent builder.
   * A worker id and a maximum number of tasks to fetch must be specified.
   * The builder allows to specify multiple topics to fetch tasks for and
   * individual lock durations. For every topic, variables can be fetched
   * in addition.</p>
   *
   * <p>Returned tasks are locked for the given worker until
   * <code>now + lockDuration</code> expires.
   * Locked tasks cannot be fetched or completed by other workers. When the lock time has expired,
   * a task may be fetched and locked by other workers.</p>
   *
   * <p>Returns at most <code>maxTasks</code> tasks. The tasks are arbitrarily
   * distributed among the specified topics. Example: Fetching 10 tasks of topics
   * "a"/"b"/"c" may return 3/3/4 tasks, or 10/0/0 tasks, etc.</p>
   *
   * <p>May return less than <code>maxTasks</code> tasks, if there exist not enough
   * unlocked tasks matching the provided topics or if parallel fetching by other workers
   * results in locking failures.</p>
   *
   * <p>
   *   Returns only tasks that the currently authenticated user has at least one
   *   permission out of all of the following groups for:
   *
   *   <ul>
   *     <li>{@link Permissions#READ} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#READ_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   * </p>
   *
   * @param maxTasks the maximum number of tasks to return
   * @param workerId the id of the worker to lock the tasks for
   * @return a builder to define and execute an external task fetching operation
   */
  public ExternalTaskQueryBuilder fetchAndLock(int maxTasks, String workerId);

  /**
   * <p>Completes an external task on behalf of a worker. The given task must be
   * assigned to the worker.</p>
   *
   * @param externalTaskId the id of the external to complete
   * @param workerId the id of the worker that completes the task
   * @throws ProcessEngineException if the task does not exist anymore or an error
   *   occurs in the following process execution
   * @throws BadUserRequestException if the task is assigned to a different worker
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void complete(String externalTaskId, String workerId);

  /**
   * <p>Completes an external task on behalf of a worker and submits variables
   * to the process instance before continuing execution. The given task must be
   * assigned to the worker.</p>
   *
   * @param externalTaskId the id of the external to complete
   * @param workerId the id of the worker that completes the task
   * @param variables a map of variables to set on the execution (non-local)
   *   the external task is assigned to
   * @throws ProcessEngineException if the task does not exist anymore or an error
   *   occurs in the following process execution
   * @throws BadUserRequestException if the task is assigned to a different worker
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void complete(String externalTaskId, String workerId, Map<String, Object> variables);

  /**
   * Unlocks an external task instance.
   *
   * @param externalTaskId the id of the task to unlock
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void unlock(String externalTaskId);

  /**
   * <p>
   *   Queries for tasks that the currently authenticated user has at least one
   *   of the following permissions for:
   *
   *   <ul>
   *     <li>{@link Permissions#READ} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#READ_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   * </p>
   *
   * @return a new {@link ExternalTaskQuery} that can be used to dynamically
   * query for external tasks.
   */
  public ExternalTaskQuery createExternalTaskQuery();
}
