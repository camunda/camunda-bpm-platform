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


/**
 * Log entry about an operation performed by a user. This is used for logging
 * actions such as creating a new task, completing a task,
 * canceling a process instance, ...
 *
 * <h2>Operation Type</h2>
 * <p>The type of the operation which has been performed. A user may create a new task,
 * complete a task, delegate a tasks, etc... Check this class for a list of built-in
 * operation type constants.</p>
 *
 * <h2>Entity Type</h2>
 * <p>The type of the entity on which the operation was performed. Operations may be
 * performed on tasks, attachments, ...</p>
 *
 * <h2>Affected Entity Criteria</h2>
 * <p>
 *   The methods that reference other entities (except users), such as {@link #getProcessInstanceId()}
 *   or {@link #getProcessDefinitionId()}, describe which entities were affected
 *   by the operation and represent restriction criteria.
 *   A <code>null</code> return value of any of those methods means that regarding
 *   this criterion, any entity was affected.
 * </p>
 * <p>
 *   For example, if an operation suspends all process instances that belong to a certain
 *   process definition id, one operation log entry is created.
 *   Its return value for the method {@link #getProcessInstanceId()} is <code>null</code>,
 *   while {@link #getProcessDefinitionId()} returns an id. Thus, the return values
 *   of these methods can be understood as selection criteria of instances of the entity type
 *   that were affected by the operation.
 * </p>
 *
 * <h2>Additional Considerations</h2>
 * <p>The event describes which user has requested out the operation and the time
 * at which the operation was performed. Furthermore, one operation can result in multiple
 * {@link UserOperationLogEntry} entities whicha are linked by the value of the
 * {@link #getOperationId()} method.</p>
 *
 * @author Danny Gräf
 * @author Daniel Meyer
 *
 */
public interface UserOperationLogEntry {

  /** @deprecated Please use {@link EntityTypes#TASK} instead. */
  @Deprecated
  public static String ENTITY_TYPE_TASK = EntityTypes.TASK;
  /** @deprecated Please use {@link EntityTypes#IDENTITY_LINK} instead. */
  @Deprecated
  public static String ENTITY_TYPE_IDENTITY_LINK = EntityTypes.IDENTITY_LINK;
  /** @deprecated Please use {@link EntityTypes#ATTACHMENT} instead. */
  @Deprecated
  public static String ENTITY_TYPE_ATTACHMENT = EntityTypes.ATTACHMENT;

  public static String OPERATION_TYPE_ASSIGN = "Assign";
  public static String OPERATION_TYPE_CLAIM = "Claim";
  public static String OPERATION_TYPE_COMPLETE = "Complete";
  public static String OPERATION_TYPE_CREATE = "Create";
  public static String OPERATION_TYPE_DELEGATE = "Delegate";
  public static String OPERATION_TYPE_DELETE = "Delete";
  public static String OPERATION_TYPE_RESOLVE = "Resolve";
  public static String OPERATION_TYPE_SET_OWNER = "SetOwner";
  public static String OPERATION_TYPE_SET_PRIORITY = "SetPriority";
  public static String OPERATION_TYPE_UPDATE = "Update";
  public static String OPERATION_TYPE_ACTIVATE = "Activate";
  public static String OPERATION_TYPE_SUSPEND = "Suspend";
  public static String OPERATION_TYPE_MIGRATE = "Migrate";
  public static String OPERATION_TYPE_ADD_USER_LINK = "AddUserLink";
  public static String OPERATION_TYPE_DELETE_USER_LINK = "DeleteUserLink";
  public static String OPERATION_TYPE_ADD_GROUP_LINK = "AddGroupLink";
  public static String OPERATION_TYPE_DELETE_GROUP_LINK = "DeleteGroupLink";

  public static String OPERATION_TYPE_ADD_ATTACHMENT = "AddAttachment";
  public static String OPERATION_TYPE_DELETE_ATTACHMENT = "DeleteAttachment";

  public static String OPERATION_TYPE_SUSPEND_JOB_DEFINITION = "SuspendJobDefinition";
  public static String OPERATION_TYPE_ACTIVATE_JOB_DEFINITION = "ActivateJobDefinition";
  public static String OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION = "SuspendProcessDefinition";
  public static String OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION = "ActivateProcessDefinition";

  public static String OPERATION_TYPE_UPDATE_HISTORY_TIME_TO_LIVE = "UpdateHistoryTimeToLive";

  public static String OPERATION_TYPE_MODIFY_PROCESS_INSTANCE = "ModifyProcessInstance";
  public static String OPERATION_TYPE_RESTART_PROCESS_INSTANCE  = "RestartProcessInstance";
  public static String OPERATION_TYPE_SUSPEND_JOB = "SuspendJob";
  public static String OPERATION_TYPE_ACTIVATE_JOB = "ActivateJob";
  public static String OPERATION_TYPE_SET_JOB_RETRIES = "SetJobRetries";
  public static String OPERATION_TYPE_SET_EXTERNAL_TASK_RETRIES = "SetExternalTaskRetries";
  public static String OPERATION_TYPE_SET_VARIABLE = "SetVariable";

  public static String OPERATION_TYPE_REMOVE_VARIABLE = "RemoveVariable";
  public static String OPERATION_TYPE_MODIFY_VARIABLE = "ModifyVariable";

  public static String OPERATION_TYPE_SUSPEND_BATCH = "SuspendBatch";
  public static String OPERATION_TYPE_ACTIVATE_BATCH = "ActivateBatch";


  /** The unique identifier of this log entry. */
  String getId();

  /** Deployment reference */
  String getDeploymentId();

  /** Process definition reference. */
  String getProcessDefinitionId();

  /**
   * Key of the process definition this log entry belongs to; <code>null</code> means any.
   */
  String getProcessDefinitionKey();

  /** Process instance reference. */
  String getProcessInstanceId();

  /** Execution reference. */
  String getExecutionId();

  /** Case definition reference. */
  String getCaseDefinitionId();

  /** Case instance reference. */
  String getCaseInstanceId();

  /** Case execution reference. */
  String getCaseExecutionId();

  /** Task instance reference. */
  String getTaskId();

  /** Job instance reference. */
  String getJobId();

  /** Job definition reference. */
  String getJobDefinitionId();

  /** Batch reference. */
  String getBatchId();

  /** The User who performed the operation */
  String getUserId();

  /** Timestamp of this change. */
  Date getTimestamp();

  /**
   * The unique identifier of this operation.
   *
   * If an operation modifies multiple properties, multiple {@link UserOperationLogEntry} instances will be
   * created with a common operationId. This allows grouping multiple entries which are part of a composite operation.
   */
  String getOperationId();

  /**
   * Type of this operation, like create, assign, claim and so on.
   *
   * @see #OPERATION_TYPE_ASSIGN and other fields beginning with OPERATION_TYPE
   */
  String getOperationType();

  /**
   * The type of the entity on which this operation was executed.
   *
   * @see #ENTITY_TYPE_TASK and other fields beginning with ENTITY_TYPE
   */
  String getEntityType();

  /** The property changed by this operation. */
  String getProperty();

  /** The original value of the property. */
  String getOrgValue();

  /** The new value of the property. */
  String getNewValue();
}
