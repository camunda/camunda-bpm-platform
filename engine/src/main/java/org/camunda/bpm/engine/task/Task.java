/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.task;

import java.util.Date;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.CamundaFormRef;



/** Represents one task for a human user.
 *
 * @author Joram Barrez
 */
public interface Task {

  int PRIORITY_MINIUM = 0;
  int PRIORITY_NORMAL = 50;
  int PRIORITY_MAXIMUM = 100;

  /** DB id of the task. */
	String getId();

  /** Name or title of the task. */
	String getName();

  /** Name or title of the task. */
	void setName(String name);

  /** Free text description of the task. */
	String getDescription();

  /** Change the description of the task */
	void setDescription(String description);

	/** indication of how important/urgent this task is with a number between
	 * 0 and 100 where higher values mean a higher priority and lower values mean
	 * lower priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high
	 * [80..100] highest */
	int getPriority();

  /** indication of how important/urgent this task is with a number between
   * 0 and 100 where higher values mean a higher priority and lower values mean
   * lower priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high
   * [80..100] highest */
	void setPriority(int priority);

  /** The {@link User#getId() userId} of the person that is responsible for this task.
   * This is used when a task is {@link TaskService#delegateTask(String, String) delegated}. */
  String getOwner();

  /** The {@link User#getId() userId} of the person that is responsible for this task.
   * This is used when a task is {@link TaskService#delegateTask(String, String) delegated}. */
  void setOwner(String owner);

  /** The {@link User#getId() userId} of the person to which this task is
   * {@link TaskService#setAssignee(String, String) assigned} or
   * {@link TaskService#delegateTask(String, String) delegated}. */
  String getAssignee();

  /** The {@link User#getId() userId} of the person to which this task is
   * {@link TaskService#setAssignee(String, String) assigned} or
   * {@link TaskService#delegateTask(String, String) delegated}. */
  void setAssignee(String assignee);

  /** The current {@link DelegationState} for this task. */
  DelegationState getDelegationState();

  /** The current {@link DelegationState} for this task. */
  void setDelegationState(DelegationState delegationState);

  /** Reference to the process instance or null if it is not related to a process instance. */
	String getProcessInstanceId();

  /** Reference to the path of execution or null if it is not related to a process instance. */
	String getExecutionId();

  /** Reference to the process definition or null if it is not related to a process. */
	String getProcessDefinitionId();

  /** Reference to the case instance or null if it is not related to a case instance. */
  String getCaseInstanceId();

  /**
   * The case instance id for which this task is associated for.
   */
  void setCaseInstanceId(String caseInstanceId);

  /** Reference to the path of case execution or null if it is not related to a case instance. */
  String getCaseExecutionId();

  /** Reference to the case definition or null if it is not related to a case. */
  String getCaseDefinitionId();

	/** The date/time when this task was created */
	Date getCreateTime();

	/** The id of the activity in the process defining this task or null if this is not related to a process */
	String getTaskDefinitionKey();

	/** Due date of the task. */
	Date getDueDate();

	/** Change due date of the task. */
	void setDueDate(Date dueDate);

	/** Follow-up date of the task. */
	Date getFollowUpDate();

	/** Change follow-up date of the task. */
	void setFollowUpDate(Date followUpDate);

	/** delegates this task to the given user and sets the {@link #getDelegationState() delegationState} to {@link DelegationState#PENDING}.
	 * If no owner is set on the task, the owner is set to the current assignee of the task. */
  void delegate(String userId);

  /** the parent task for which this task is a subtask */
  void setParentTaskId(String parentTaskId);

  /** the parent task for which this task is a subtask */
  String getParentTaskId();

  /** Indicated whether this task is suspended or not. */
  boolean isSuspended();

  /**
   * Provides the form key for the task.
   *
   * <p><strong>NOTE:</strong> If the task instance is obtained through a query, this property is only populated in case the
   * {@link TaskQuery#initializeFormKeys()} method is called. If this method is called without a prior call to
   * {@link TaskQuery#initializeFormKeys()}, it will throw a {@link BadUserRequestException}.</p>
   *
   * @return the form key for this task
   * @throws BadUserRequestException in case the form key is not initialized.
   */
  String getFormKey();

  /**
   * Provides the form binding reference to the Camunda Form for the task.
   *
   * <p><strong>NOTE:</strong> If the task instance is obtained through a query, this property is only populated in case the
   * {@link TaskQuery#initializeFormKeys()} method is called. If this method is called without a prior call to
   * {@link TaskQuery#initializeFormKeys()}, it will throw a {@link BadUserRequestException}.</p>
   *
   * @return the reference key, binding type and version (if type is {@code version})
   * @throws BadUserRequestException in case the form key is not initialized.
   */
  CamundaFormRef getCamundaFormRef();

  /**
   * Returns the task's tenant id or null in case this task does not belong to a tenant.
   *
   * @return the task's tenant id or null
   *
   * @since 7.5
   */
  String getTenantId();

  /**
   * Sets the tenant id for this task.
   *
   * @param tenantId the tenant id to set
   *
   * @since 7.5
   */
  void setTenantId(String tenantId);

}
