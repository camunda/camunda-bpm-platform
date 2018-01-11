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

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.UpdateExternalTaskRetriesBuilder;
import org.camunda.bpm.engine.externaltask.UpdateExternalTaskRetriesSelectBuilder;

/**
 * Service that provides access to {@link ExternalTask} instances. External tasks
 * represent work items that are processed externally and independently of the process
 * engine.
 *
 * @author Thorben Lindhauer
 * @author Christopher Zell
 */
public interface ExternalTaskService {

  /**
   * Calls method fetchAndLock(maxTasks, workerId, usePriority), where usePriority is false.
   *
   * @param maxTasks the maximum number of tasks to return
   * @param workerId the id of the worker to lock the tasks for
   * @return a builder to define and execute an external task fetching operation
   * @see {@link ExternalTaskService#fetchAndLock(int, java.lang.String, boolean)}.
   */
  public ExternalTaskQueryBuilder fetchAndLock(int maxTasks, String workerId);

  /**
   * <p>Defines fetching of external tasks by using a fluent builder.
   * The following parameters must be specified:
   * A worker id, a maximum number of tasks to fetch and a flag that indicates
   * whether priority should be regarded or not.
   * The builder allows to specify multiple topics to fetch tasks for and
   * individual lock durations. For every topic, variables can be fetched
   * in addition.Is the priority enabled the tasks with the highest priority are fetched.</p>
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
   * @param usePriority the flag to enable the priority fetching mechanism
   * @return a builder to define and execute an external task fetching operation
   */
  public ExternalTaskQueryBuilder fetchAndLock(int maxTasks, String workerId, boolean usePriority);

  /**
   * <p>Completes an external task on behalf of a worker. The given task must be
   * assigned to the worker.</p>
   *
   * @param externalTaskId the id of the external to complete
   * @param workerId the id of the worker that completes the task
   * @throws NotFoundException if no external task with the given id exists
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
   *
   * @throws NotFoundException if no external task with the given id exists
   * @throws BadUserRequestException if the task is assigned to a different worker
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void complete(String externalTaskId, String workerId, Map<String, Object> variables);

  /**
   * <p>Completes an external task on behalf of a worker and submits variables
   * to the process instance before continuing execution. The given task must be
   * assigned to the worker.</p>
   *
   * @param externalTaskId the id of the external to complete
   * @param workerId the id of the worker that completes the task
   * @param variables a map of variables to set on the execution
   *   the external task is assigned to
   * @param localVariables a map of variables to set on the execution locally
   *
   * @throws NotFoundException if no external task with the given id exists
   * @throws BadUserRequestException if the task is assigned to a different worker
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void complete(String externalTaskId, String workerId, Map<String, Object> variables, Map<String, Object> localVariables);

  /**
   * <p>Extends a lock of an external task on behalf of a worker.
   * The given task must be assigned to the worker.</p>
   *
   * @param externalTaskId the id of the external task
   * @param workerId the id of the worker that extends the lock of the task
   *
   * @throws NotFoundException if no external task with the given id exists
   * @throws BadUserRequestException if the task is assigned to a different worker
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void extendLock(String externalTaskId, String workerId, long newLockDuration);

  /**
   * <p>Signals that an external task could not be successfully executed.
   * The task must be assigned to the given worker. The number of retries left can be specified. In addition, a timeout can be
   * provided, such that the task cannot be fetched before <code>now + retryTimeout</code> again.</p>
   *
   * <p>If <code>retries</code> is 0, an incident with the given error message is created. The incident gets resolved,
   * once the number of retries is increased again.</p>
   *
   * @param externalTaskId the id of the external task to report a failure for
   * @param workerId the id of the worker that reports the failure
   * @param errorMessage short error message related to this failure. This message can be retrieved via
   *   {@link ExternalTask#getErrorMessage()} and is used as the incident message in case <code>retries</code> is <code>null</code>.
   *   May be <code>null</code>.
   * @param retries the number of retries left. External tasks with 0 retries cannot be fetched anymore unless
   *   the number of retries is increased via API. Must be >= 0.
   * @param retryTimeout the timeout before the task can be fetched again. Must be >= 0.
   *
   * @throws NotFoundException if no external task with the given id exists
   * @throws BadUserRequestException if the task is assigned to a different worker
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void handleFailure(String externalTaskId, String workerId, String errorMessage, int retries, long retryTimeout);

  /**
   * <p>Signals that an external task could not be successfully executed.
   * The task must be assigned to the given worker. The number of retries left can be specified. In addition, a timeout can be
   * provided, such that the task cannot be fetched before <code>now + retryTimeout</code> again.</p>
   *
   * <p>If <code>retries</code> is 0, an incident with the given error message is created. The incident gets resolved,
   * once the number of retries is increased again.</p>
   *
   * @param externalTaskId the id of the external task to report a failure for
   * @param workerId the id of the worker that reports the failure
   * @param errorMessage short error message related to this failure. This message can be retrieved via
   *   {@link ExternalTask#getErrorMessage()} and is used as the incident message in case <code>retries</code> is <code>null</code>.
   *   May be <code>null</code>.
   * @param errorDetails full error message related to this failure. This message can be retrieved via
   *   {@link ExternalTaskService#getExternalTaskErrorDetails(String)} ()}
   * @param retries the number of retries left. External tasks with 0 retries cannot be fetched anymore unless
   *   the number of retries is increased via API. Must be >= 0.
   * @param retryTimeout the timeout before the task can be fetched again. Must be >= 0.
   *
   * @throws NotFoundException if no external task with the given id exists
   * @throws BadUserRequestException if the task is assigned to a different worker
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void handleFailure(String externalTaskId, String workerId, String errorMessage, String errorDetails, int retries, long retryTimeout);

  /**
   * <p>Signals that an business error appears, which should be handled by the process engine.
   * The task must be assigned to the given worker. The error will be propagated to the next error handler.
   * Is no existing error handler for the given bpmn error the activity instance of the external task
   * ends.</p>
   *
   * @param externalTaskId the id of the external task to report a bpmn error
   * @param workerId the id of the worker that reports the bpmn error
   * @param errorCode the error code of the corresponding bmpn error
   * @since 7.5
   *
   * @throws NotFoundException if no external task with the given id exists
   * @throws BadUserRequestException if the task is assigned to a different worker
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void handleBpmnError(String externalTaskId, String workerId, String errorCode);

  /**
   * Unlocks an external task instance.
   *
   * @param externalTaskId the id of the task to unlock
   * @throws NotFoundException if no external task with the given id exists
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void unlock(String externalTaskId);

  /**
   * Sets the retries for an external task. If the new value is 0, a new incident with a <code>null</code>
   * message is created. If the old value is 0 and the new value is greater than 0, an existing incident
   * is resolved.
   *
   * @param externalTaskId the id of the task to set the
   * @param retries
   * @throws NotFoundException if no external task with the given id exists
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void setRetries(String externalTaskId, int retries);

  /**
   * Sets the retries for external tasks. If the new value is 0, a new incident with a <code>null</code>
   * message is created. If the old value is 0 and the new value is greater than 0, an existing incident
   * is resolved.
   *
   * @param externalTaskIds the ids of the tasks to set the
   * @param retries
   * @throws NotFoundException if no external task with one of the given id exists
   * @throws BadUserRequestException if the ids are null or the number of retries is negative
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void setRetries(List<String> externalTaskIds, int retries);

  /**
   * Sets the retries for external tasks asynchronously as batch. The returned batch
   * can be used to track the progress. If the new value is 0, a new incident with a <code>null</code>
   * message is created. If the old value is 0 and the new value is greater than 0, an existing incident
   * is resolved.
   *
   *
   * @return the batch
   *
   * @param externalTaskIds the ids of the tasks to set the
   * @param retries
   * @param externalTaskQuery a query which selects the external tasks to set the retries for.
   * @throws NotFoundException if no external task with one of the given id exists
   * @throws BadUserRequestException if the ids are null or the number of retries is negative
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  public Batch setRetriesAsync(List<String> externalTaskIds, ExternalTaskQuery externalTaskQuery, int retries);

  /**
   * Sets the retries for external tasks using a fluent builder.
   *
   * Specify the instances by calling one of the following methods, like
   * <i>externalTaskIds</i>. To set the retries call
   * {@link UpdateExternalTaskRetriesBuilder#set(int)} or
   * {@link UpdateExternalTaskRetriesBuilder#setAsync(int)}.
   *
   * @since 7.8
   */
  public UpdateExternalTaskRetriesSelectBuilder updateRetries();

  /**
   * Sets the priority for an external task.
   *
   * @param externalTaskId the id of the task to set the
   * @param priority the new priority of the task
   * @throws NotFoundException if no external task with the given id exists
   * @throws AuthorizationException thrown if the current user does not possess any of the following permissions:
   *   <ul>
   *     <li>{@link Permissions#UPDATE} on {@link Resources#PROCESS_INSTANCE}</li>
   *     <li>{@link Permissions#UPDATE_INSTANCE} on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  public void setPriority(String externalTaskId, long priority);

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

  /**
   * Returns the full error details that occurred while running external task
   * with the given id. Returns null when the external task has no error details.
   *
   * @param externalTaskId id of the external task, cannot be null.
   *
   * @throws ProcessEngineException
   *          When no external task exists with the given id.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   * @since 7.6
   */
  String getExternalTaskErrorDetails(String externalTaskId);
}
