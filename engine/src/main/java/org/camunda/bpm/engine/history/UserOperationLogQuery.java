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
package org.camunda.bpm.engine.history;

import java.util.Date;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.query.Query;


/**
 * Programmatic querying for {@link UserOperationLogEntry} instances.
 *
 * @author Danny Gräf
 */
public interface UserOperationLogQuery extends Query<UserOperationLogQuery, UserOperationLogEntry> {

  /**
   * Query for operations on entities of a given type only. This allows you to restrict the
   * result set to all operations which were performed on the same Entity (ie. all Task Operations,
   * All IdentityLink Operations ...)
   *
   * @see EntityTypes#TASK
   * @see EntityTypes#IDENTITY_LINK
   * @see EntityTypes#ATTACHMENT
   */
  UserOperationLogQuery entityType(String entityType);

  /**
   * Query for operations on entities of a given type only. This allows you to restrict the
   * result set to all operations which were performed on the same Entity (ie. all Task Operations,
   * All IdentityLink Operations ...)
   *
   * @see EntityTypes#TASK
   * @see EntityTypes#IDENTITY_LINK
   * @see EntityTypes#ATTACHMENT
   */
  UserOperationLogQuery entityTypeIn(String... entityTypes);

  /**
   * Query for operations of a given type only. Types of operations depend on the entity on which the operation
   * was performed. For Instance: Tasks may be delegated, claimed, completed ...
   * Check the {@link UserOperationLogEntry} class for a list of constants of supported operations.
   */
  UserOperationLogQuery operationType(String operationType);

  /** Query entries which are existing for the given deployment id. */
  UserOperationLogQuery deploymentId(String deploymentId);

  /** Query entries which are existing for the given process definition id. */
  UserOperationLogQuery processDefinitionId(String processDefinitionId);

  /** Query entries which are operate on all process definitions of the given key. */
  UserOperationLogQuery processDefinitionKey(String processDefinitionKey);

  /** Query entries which are existing for the given process instance. */
  UserOperationLogQuery processInstanceId(String processInstanceId);

  /** Query entries which are existing for the given execution. */
  UserOperationLogQuery executionId(String executionId);

  /** Query entries which are existing for the given case definition id. */
  UserOperationLogQuery caseDefinitionId(String caseDefinitionId);

  /** Query entries which are existing for the given case instance. */
  UserOperationLogQuery caseInstanceId(String caseInstanceId);

  /** Query entries which are existing for the given case execution. */
  UserOperationLogQuery caseExecutionId(String caseExecutionId);

  /** Query entries which are existing for the task. */
  UserOperationLogQuery taskId(String taskId);

  /** Query entries which are existing for the job. */
  UserOperationLogQuery jobId(String jobId);

  /** Query entries which are existing for the job definition. */
  UserOperationLogQuery jobDefinitionId(String jobDefinitionId);

  /** Query entries which are existing for the batch. */
  UserOperationLogQuery batchId(String batchId);

  /** Query entries which are existing for the user. */
  UserOperationLogQuery userId(String userId);

  /** Query entries of a composite operation.
   * This allows grouping multiple updates which are part of the same operation:
   * for instance, a User may update multiple fields of a UserTask when calling {@link TaskService#saveTask(org.camunda.bpm.engine.task.Task)}
   * which will be logged as separate {@link UserOperationLogEntry OperationLogEntries} with the same 'operationId'
   * */
  UserOperationLogQuery operationId(String operationId);

  /** Query entries that changed a property. */
  UserOperationLogQuery property(String property);

  /** Query entries after the time stamp. */
  UserOperationLogQuery afterTimestamp(Date after);

  /** Query entries before the time stamp. */
  UserOperationLogQuery beforeTimestamp(Date before);

  /** Order by time stamp (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserOperationLogQuery orderByTimestamp();
}
