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
 * <p>The event contains data about the Operation performed</p>
 *
 * @author Danny Gräf
 * @author Daniel Meyer
 *
 */
public interface UserOperationLogEntry {

  public static String ENTITY_TYPE_TASK = "Task";
  public static String ENTITY_TYPE_IDENTITY_LINK = "IdentityLink";
  public static String ENTITY_TYPE_ATTACHMENT = "Attachment";

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
  public static String OPERATION_TYPE_ADD_USER_LINK = "AddUserLink";
  public static String OPERATION_TYPE_DELETE_USER_LINK = "DeleteUserLink";
  public static String OPERATION_TYPE_ADD_GROUP_LINK = "AddGroupLink";
  public static String OPERATION_TYPE_DELETE_GROUP_LINK = "DeleteGroupLink";
  public static String OPERATION_TYPE_ADD_ATTACHMENT = "AddAttachment";
  public static String OPERATION_TYPE_DELETE_ATTACHMENT = "DeleteAttachment";

  /** The unique identifier of this log entry. */
  String getId();

  /** Process definition reference. */
  String getProcessDefinitionId();

  /** Process instance reference. */
  String getProcessInstanceId();

  /** Execution reference. */
  String getExecutionId();

  /** Task instance reference. */
  String getTaskId();

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
