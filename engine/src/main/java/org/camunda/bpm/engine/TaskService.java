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

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Event;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.NativeTaskQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.task.TaskReport;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/** Service which provides access to {@link Task} and form related operations.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Thorben Lindhauer
 */
public interface TaskService {

	/**
	 * Creates a new task that is not related to any process instance.
	 *
	 * The returned task is transient and must be saved with {@link #saveTask(Task)} 'manually'.
	 *
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#TASK}.
	 */
  Task newTask();

  /** create a new task with a user defined task id */
  Task newTask(String taskId);

	/**
	 * Saves the given task to the persistent data store. If the task is already
	 * present in the persistent store, it is updated.
	 * After a new task has been saved, the task instance passed into this method
	 * is updated with the id of the newly created task.
	 *
	 * @param task the task, cannot be null.
	 *
   * @throws AuthorizationException
   *          If the task is already present and the user has no {@link Permissions#UPDATE} permission
   *          on {@link Resources#TASK} or no {@link Permissions#UPDATE_TASK} permission on
   *          {@link Resources#PROCESS_DEFINITION}.
   *          Or if the task is not present and the user has no {@link Permissions#CREATE} permission
   *          on {@link Resources#TASK}.
	 */
	void saveTask(Task task);

	/**
	 * Deletes the given task, not deleting historic information that is related to this task.
	 *
	 * @param taskId The id of the task that will be deleted, cannot be null. If no task
	 * exists with the given taskId, the operation is ignored.
	 *
	 * @throws ProcessEngineException
   *          when an error occurs while deleting the task or in case the task is part
   *          of a running process or case instance.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#TASK}.
	 */
	void deleteTask(String taskId);

	/**
	 * Deletes all tasks of the given collection, not deleting historic information that is related
	 * to these tasks.
	 *
	 * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
	 * id's in the list that don't have an existing task will be ignored.
	 *
	 * @throws ProcessEngineException
	 *          when an error occurs while deleting the tasks or in case one of the tasks
   *          is part of a running process or case instance.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#TASK}.
	 */
	void deleteTasks(Collection<String> taskIds);

  /**
   * Deletes the given task.
   *
   * @param taskId The id of the task that will be deleted, cannot be null. If no task
   * exists with the given taskId, the operation is ignored.
   *
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   *
   * @throws ProcessEngineException
   *          when an error occurs while deleting the task or in case the task is part
   *          of a running process or case instance.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#TASK}.
   */
  void deleteTask(String taskId, boolean cascade);

  /**
   * Deletes all tasks of the given collection.
   *
   * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
   * id's in the list that don't have an existing task will be ignored.
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   *
   * @throws ProcessEngineException
   *          when an error occurs while deleting the tasks or in case one of the tasks
   *          is part of a running process or case instance.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#TASK}.
   */
  void deleteTasks(Collection<String> taskIds, boolean cascade);

  /**
   * Deletes the given task, not deleting historic information that is related to this task.
   *
   * @param taskId The id of the task that will be deleted, cannot be null. If no task
   * exists with the given taskId, the operation is ignored.
   * @param deleteReason reason the task is deleted. Is recorded in history, if enabled.
   *
   * @throws ProcessEngineException
   *          when an error occurs while deleting the task or in case the task is part
   *          of a running process or case instance.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#TASK}.
   */
  void deleteTask(String taskId, String deleteReason);

  /**
   * Deletes all tasks of the given collection, not deleting historic information that is related to these tasks.
   *
   * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
   * id's in the list that don't have an existing task will be ignored.
   * @param deleteReason reason the task is deleted. Is recorded in history, if enabled.
   *
   * @throws ProcessEngineException
   *          when an error occurs while deleting the tasks or in case one of the tasks
   *          is part of a running process or case instance.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#DELETE} permission on {@link Resources#TASK}.
   */
  void deleteTasks(Collection<String> taskIds, String deleteReason);

  /**
   * Claim responsibility for a task:
   * the given user is made {@link Task#getAssignee() assignee} for the task.
   * The difference with {@link #setAssignee(String, String)} is that here
   * a check is done if the task already has a user assigned to it.
   * No check is done whether the user is known by the identity component.
   *
   * @param taskId task to claim, cannot be null.
   * @param userId user that claims the task. When userId is null the task is unclaimed,
   * assigned to no one.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist or when the task is already claimed by another user.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void claim(String taskId, String userId);

  /**
   * Marks a task as done and continues process execution.
   *
   * This method is typically called by a task list user interface
   * after a task form has been submitted by the
   * {@link Task#getAssignee() assignee}.
   *
   * @param taskId the id of the task to complete, cannot be null.
   *
   * @throws ProcessEngineException
   *          when no task exists with the given id or when this task is {@link DelegationState#PENDING} delegation.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void complete(String taskId);

  /**
   * Delegates the task to another user.
   *
   * This means that the {@link Task#getAssignee() assignee} is set
   * and the {@link Task#getDelegationState() delegation state} is set to
   * {@link DelegationState#PENDING}.
   * If no owner is set on the task, the owner is set to the current
   * {@link Task#getAssignee() assignee} of the task.
   * The new assignee must use {@link TaskService#resolveTask(String)}
   * to report back to the owner.
   * Only the owner can {@link TaskService#complete(String) complete} the task.
   *
   * @param taskId The id of the task that will be delegated.
   * @param userId The id of the user that will be set as assignee.
   *
   * @throws ProcessEngineException
   *          when no task exists with the given id.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void delegateTask(String taskId, String userId);

  /**
   * Marks that the {@link Task#getAssignee() assignee} is done with the task
   * {@link TaskService#delegateTask(String, String) delegated}
   * to her and that it can be sent back to the {@link Task#getOwner() owner}.
   * Can only be called when this task is {@link DelegationState#PENDING} delegation.
   * After this method returns, the {@link Task#getDelegationState() delegation state}
   * is set to {@link DelegationState#RESOLVED} and the task can be
   * {@link TaskService#complete(String) completed}.
   *
   * @param taskId the id of the task to resolve, cannot be null.
   *
   * @throws ProcessEngineException
   *          when no task exists with the given id.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void resolveTask(String taskId);

  /**
   * Marks that the {@link Task#getAssignee() assignee} is done with the task
   * {@link TaskService#delegateTask(String, String) delegated}
   * to her and that it can be sent back to the {@link Task#getOwner() owner}
   * with the provided variables.
   * Can only be called when this task is {@link DelegationState#PENDING} delegation.
   * After this method returns, the {@link Task#getDelegationState() delegation state}
   * is set to {@link DelegationState#RESOLVED} and the task can be
   * {@link TaskService#complete(String) completed}.
   *
   * @param taskId
   * @param variables
   *
   * @throws ProcessEngineException
   *          when no task exists with the given id.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void resolveTask(String taskId, Map<String, Object> variables);

  /**
   * Marks a task as done and continues process execution.
   *
   * This method is typically called by a task list user interface
   * after a task form has been submitted by the
   * {@link Task#getAssignee() assignee}
   * and the required task parameters have been provided.
   *
   * @param taskId the id of the task to complete, cannot be null.
   * @param variables task parameters. May be null or empty.
   *
   * @throws ProcessEngineException
   *          when no task exists with the given id.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void complete(String taskId, Map<String, Object> variables);

  /**
   * Changes the assignee of the given task to the given userId.
   * No check is done whether the user is known by the identity component.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as assignee.
   *
   * @throws ProcessEngineException
   *          when the task or user doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void setAssignee(String taskId, String userId);

  /**
   * Transfers ownership of this task to another user.
   * No check is done whether the user is known by the identity component.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId of the person that is receiving ownership.
   *
   * @throws ProcessEngineException
   *          when the task or user doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void setOwner(String taskId, String userId);

  /**
   * Retrieves the {@link IdentityLink}s associated with the given task.
   * Such an {@link IdentityLink} informs how a certain identity (eg. group or user)
   * is associated with a certain task (eg. as candidate, assignee, etc.)
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  List<IdentityLink> getIdentityLinksForTask(String taskId);

  /**
   * Convenience shorthand for {@link #addUserIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   *
   * @throws ProcessEngineException
   *          when the task or user doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void addCandidateUser(String taskId, String userId);

  /**
   * Convenience shorthand for {@link #addGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   *
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   *
   * @throws ProcessEngineException
   *          when the task or group doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void addCandidateGroup(String taskId, String groupId);

  /**
   * Involves a user with a task. The type of identity link is defined by the
   * given identityLinkType.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   *
   * @throws ProcessEngineException
   *          when the task or user doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void addUserIdentityLink(String taskId, String userId, String identityLinkType);

  /**
   * Involves a group with a task. The type of identityLink is defined by the
   * given identityLink.
   *
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   *
   * @throws ProcessEngineException
   *          when the task or group doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void addGroupIdentityLink(String taskId, String groupId, String identityLinkType);

  /**
   * Convenience shorthand for {@link #deleteUserIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   *
   * @throws ProcessEngineException
   *          when the task or user doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void deleteCandidateUser(String taskId, String userId);

  /**
   * Convenience shorthand for {@link #deleteGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   *
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   *
   * @throws ProcessEngineException
   *          when the task or group doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void deleteCandidateGroup(String taskId, String groupId);

  /**
   * Removes the association between a user and a task for the given identityLinkType.
   *
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   *
   * @throws ProcessEngineException
   *          when the task or user doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void deleteUserIdentityLink(String taskId, String userId, String identityLinkType);

  /**
   * Removes the association between a group and a task for the given identityLinkType.
   *
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   *
   * @throws ProcessEngineException
   *          when the task or group doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType);

  /**
   * Changes the priority of the task.
   *
   * Authorization: actual owner / business admin
   *
   * @param taskId id of the task, cannot be null.
   * @param priority the new priority for the task.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void setPriority(String taskId, int priority);

  /**
   * Returns a new {@link TaskQuery} that can be used to dynamically query tasks.
   */
  TaskQuery createTaskQuery();

  /**
   * Returns a new
   */
  NativeTaskQuery createNativeTaskQuery();

  /**
   * Set variable on a task. If the variable is not already existing, it will be created in the
   * most outer scope.  This means the process instance in case this task is related to an
   * execution.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void setVariable(String taskId, String variableName, Object value);

  /**
   * Set variables on a task. If the variable is not already existing, it will be created in the
   * most outer scope.  This means the process instance in case this task is related to an
   * execution.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void setVariables(String taskId, Map<String, ? extends Object> variables);

  /**
   * Set variable on a task. If the variable is not already existing, it will be created in the
   * task.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void setVariableLocal(String taskId, String variableName, Object value);

  /**
   * Set variables on a task. If the variable is not already existing, it will be created in the
   * task.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void setVariablesLocal(String taskId, Map<String, ? extends Object> variables);

  /**
   * Get a variables and search in the task scope and if available also the execution scopes.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  Object getVariable(String taskId, String variableName);

  /**
   * Get a variables and search in the task scope and if available also the execution scopes.
   *
   * @param taskId the id of the task
   * @param variableName the name of the variable to fetch
   *
   * @return the TypedValue for the variable or 'null' in case no such variable exists.
   *
   * @throws ClassCastException
   *          in case the value is not of the requested type
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   */
  <T extends TypedValue> T getVariableTyped(String taskId, String variableName);

  /**
   * Get a variables and search in the task scope and if available also the execution scopes.
   *
   * @param taskId the id of the task
   * @param variableName the name of the variable to fetch
   * @param deserializeValue if false a, {@link SerializableValue} will not be deserialized.
   *
   * @return the TypedValue for the variable or 'null' in case no such variable exists.
   *
   * @throws ClassCastException
   *          in case the value is not of the requested type
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   */
  <T extends TypedValue> T getVariableTyped(String taskId, String variableName, boolean deserializeValue);

  /**
   * Get a variables and only search in the task scope.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  Object getVariableLocal(String taskId, String variableName);

  /**
   * Get a variables and only search in the task scope.
   *
   * @param taskId the id of the task
   * @param variableName the name of the variable to fetch
   *
   * @return the TypedValue for the variable or 'null' in case no such variable exists.
   *
   * @throws ClassCastException
   *          in case the value is not of the requested type
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   */
  <T extends TypedValue> T getVariableLocalTyped(String taskId, String variableName);

  /**
   * Get a variables and only search in the task scope.
   *
   * @param taskId the id of the task
   * @param variableName the name of the variable to fetch
   * @param deserializeValue if false a, {@link SerializableValue} will not be deserialized.
   *
   * @return the TypedValue for the variable or 'null' in case no such variable exists.
   *
   * @throws ClassCastException
   *          in case the value is not of the requested type
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   */
  <T extends TypedValue> T getVariableLocalTyped(String taskId, String variableName, boolean deserializeValue);

  /**
   * Get all variables and search in the task scope and if available also the execution scopes.
   * If you have many variables and you only need a few, consider using {@link #getVariables(String, Collection)}
   * for better performance.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  Map<String, Object> getVariables(String taskId);

  /**
   * Get all variables and search in the task scope and if available also the execution scopes.
   * If you have many variables and you only need a few, consider using {@link #getVariables(String, Collection)}
   * for better performance.
   *
   * @param taskId the id of the task
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   */
  VariableMap getVariablesTyped(String taskId);

  /**
   * Get all variables and search in the task scope and if available also the execution scopes.
   * If you have many variables and you only need a few, consider using {@link #getVariables(String, Collection)}
   * for better performance.
   *
   * @param taskId the id of the task
   * @param deserializeValues if false, {@link SerializableValue SerializableValues} will not be deserialized.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   */
  VariableMap getVariablesTyped(String taskId, boolean deserializeValues);

  /**
   * Get all variables and search only in the task scope.
   * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)}
   * for better performance.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  Map<String, Object> getVariablesLocal(String taskId);

  /**
   * Get all variables and search only in the task scope.
   * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)}
   * for better performance.
   *
   * @param taskId the id of the task
   * @param deserializeValues if false, {@link SerializableValue SerializableValues} will not be deserialized.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   */
  VariableMap getVariablesLocalTyped(String taskId);

  /**
   * Get all variables and search only in the task scope.
   * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)}
   * for better performance.
   *
   * @param taskId the id of the task
   * @param deserializeValues if false, {@link SerializableValue SerializableValues} will not be deserialized.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   */
  VariableMap getVariablesLocalTyped(String taskId, boolean deserializeValues);

  /**
   * Get values for all given variableNames
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   */
  Map<String, Object> getVariables(String taskId, Collection<String> variableNames);

  /**
   * Get values for all given variableName
   *
   * @param taskId the id of the task
   * @param variableNames only fetch variables whose names are in the collection.
   * @param deserializeValues if false, {@link SerializableValue SerializableValues} will not be deserialized.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   * */
  VariableMap getVariablesTyped(String taskId, Collection<String> variableNames, boolean deserializeValues);

  /**
   * Get a variable on a task
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   */
  Map<String,Object> getVariablesLocal(String taskId, Collection<String> variableNames);

  /**
   * Get values for all given variableName. Only search in the local task scope.
   *
   * @param taskId the id of the task
   * @param variableNames only fetch variables whose names are in the collection.
   * @param deserializeValues if false, {@link SerializableValue SerializableValues} will not be deserialized.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}
   *          or no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   *
   * @since 7.2
   */
  VariableMap getVariablesLocalTyped(String taskId, Collection<String> variableNames, boolean deserializeValues);

  /**
   * Removes the variable from the task.
   * When the variable does not exist, nothing happens.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void removeVariable(String taskId, String variableName);

  /**
   * Removes the variable from the task (not considering parent scopes).
   * When the variable does not exist, nothing happens.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void removeVariableLocal(String taskId, String variableName);

  /**
   * Removes all variables in the given collection from the task.
   * Non existing variable names are simply ignored.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void removeVariables(String taskId, Collection<String> variableNames);

  /**
   * Removes all variables in the given collection from the task (not considering parent scopes).
   * Non existing variable names are simply ignored.
   *
   * @throws ProcessEngineException
   *          when the task doesn't exist.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}
   *          (if the task is part of a running process instance).
   */
  void removeVariablesLocal(String taskId, Collection<String> variableNames);

  /**
   * Add a comment to a task and/or process instance.
   *
   * @deprecated Use {@link #createComment(String, String, String)} instead
   */
  @Deprecated
  void addComment(String taskId, String processInstanceId, String message);

  /** Creates a comment to a task and/or process instance and returns the comment. */
  Comment createComment(String taskId, String processInstanceId, String message);

  /** The comments related to the given task. */
  List<Comment> getTaskComments(String taskId);

  /** Retrieve a particular task comment */
  Comment getTaskComment(String taskId, String commentId);

  /**
   * <p>The all events related to the given task.</p>
   *
   * <p>As of Camunda BPM 7.4 task events are only written in context of a logged in
   * user. This behavior can be toggled in the process engine configuration using the
   * property <code>legacyUserOperationLog</code> (default false). To restore the engine's
   * previous behavior, set the flag to <code>true</code>.</p>
   *
   * @deprecated This method has been deprecated as of camunda BPM 7.1. It has been replaced with
   * the operation log. See {@link UserOperationLogEntry} and {@link UserOperationLogQuery}.
   *
   * @see HistoryService#createUserOperationLogQuery()
   */
  @Deprecated
  List<Event> getTaskEvents(String taskId);

  /** The comments related to the given process instance. */
  List<Comment> getProcessInstanceComments(String processInstanceId);

  /**
   * Add a new attachment to a task and/or a process instance and use an input stream to provide the content
   * please use method in runtime service to operate on process instance.
   *
   * Either taskId or processInstanceId has to be provided
   *
   * @param taskId - task that should have an attachment
   * @param processInstanceId - id of a process to use if task id is null
   * @param attachmentType - name of the attachment, can be null
   * @param attachmentName - name of the attachment, can be null
   * @param attachmentDescription  - full text description, can be null
   * @param content - byte array with content of attachment
   *
   */
  Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content);

  /**
   * Add a new attachment to a task and/or a process instance and use an url as the content
   * please use method in runtime service to operate on process instance
   *
   * Either taskId or processInstanceId has to be provided
   *
   * @param taskId - task that should have an attachment
   * @param processInstanceId - id of a process to use if task id is null
   * @param attachmentType - name of the attachment, can be null
   * @param attachmentName - name of the attachment, can be null
   * @param attachmentDescription  - full text description, can be null
   * @param url - url of the attachment, can be null
   *
   */
  Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, String url);

  /** Update the name and decription of an attachment */
  void saveAttachment(Attachment attachment);

  /** Retrieve a particular attachment */
  Attachment getAttachment(String attachmentId);

  /** Retrieve a particular attachment to the given task id and attachment id*/
  Attachment getTaskAttachment(String taskId, String attachmentId);

  /** Retrieve stream content of a particular attachment */
  InputStream getAttachmentContent(String attachmentId);

  /** Retrieve stream content of a particular attachment to the given task id and attachment id*/
  InputStream getTaskAttachmentContent(String taskId, String attachmentId);

  /** The list of attachments associated to a task */
  List<Attachment> getTaskAttachments(String taskId);

  /** The list of attachments associated to a process instance */
  List<Attachment> getProcessInstanceAttachments(String processInstanceId);

  /** Delete an attachment */
  void deleteAttachment(String attachmentId);

  /** Delete an attachment to the given task id and attachment id */
  void deleteTaskAttachment(String taskId, String attachmentId);

  /** The list of subtasks for this parent task */
  List<Task> getSubTasks(String parentTaskId);

  /** Instantiate a task report */
  TaskReport createTaskReport();
}
