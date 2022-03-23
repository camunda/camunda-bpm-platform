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
package org.camunda.bpm.engine.test.assertions.bpmn;

import java.util.Date;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;

/**
 * Assertions for a {@link Task}.
 */
public class TaskAssert extends AbstractProcessAssert<TaskAssert, Task> {

  protected TaskAssert(final ProcessEngine engine, final Task actual) {
    super(engine, actual, TaskAssert.class);
  }

  protected static TaskAssert assertThat(final ProcessEngine engine, final Task actual) {
    return new TaskAssert(engine, actual);
  }

  @Override
  protected Task getCurrent() {
    return taskQuery().taskId(actual.getId()).singleResult();
  }

  /**
   * Verifies the expectation that the {@link Task} is currently not assigned to
   * any particular user.
   *
   * @return  this {@link TaskAssert}
   */
  public TaskAssert isNotAssigned() {
    Task current = getExistingCurrent();
    Assertions.assertThat(current.getAssignee())
      .overridingErrorMessage("Expecting %s not to be assigned, but found it to be assigned to user '%s'!",
        toString(current),
        current.getAssignee())
      .isNull();
    return this;
  }

  /**
   * Verifies the expectation that the {@link Task} is currently assigned to
   * the specified user.
   *
   * @param   userId id of the user the task should be currently assigned to.
   * @return  this {@link TaskAssert}
   */
  public TaskAssert isAssignedTo(final String userId) {
    Task current = getExistingCurrent();
    Assertions
      .assertThat(current.getAssignee())
      .overridingErrorMessage("Expecting %s to be assigned to user '%s', but found it to be assigned to '%s'!",
        toString(current),
        userId,
        current.getAssignee())
      .isEqualTo(userId);
    return this;
  }

  /**
   * Verifies the expectation that the {@link Task} is currently waiting to
   * be assigned to a user of the specified candidate group.
   *
   * @param   candidateGroupId id of a candidate group the task is waiting to be assigned to
   * @return  this {@link TaskAssert}
   * @since   Camunda Platform 7.0
   */
  public TaskAssert hasCandidateGroup(final String candidateGroupId) {
    return hasCandidateGroup(candidateGroupId, true);
  }

  /**
   * Verifies the expectation that the {@link Task} is currently associated to the
   * specified candidate group - no matter whether it is already assigned to a
   * specific user or not.
   *
   * @param   candidateGroupId id of a candidate group the task is associated to
   * @return  this {@link TaskAssert}
   * @since   Camunda Platform 7.3
   */
  public TaskAssert hasCandidateGroupAssociated(final String candidateGroupId) {
    return hasCandidateGroup(candidateGroupId, false);
  }

  private TaskAssert hasCandidateGroup(final String candidateGroupId, boolean unassignedOnly) {
    Assertions.assertThat(candidateGroupId).isNotNull();
    final Task current = getExistingCurrent();
    final TaskQuery taskQuery = taskQuery().taskId(actual.getId()).taskCandidateGroup(candidateGroupId);
    if (unassignedOnly) {
      isNotAssigned(); // Useful for better assertion error message in case of assigned task
    } else {
      taskQuery.includeAssignedTasks(); // Available from Camunda Platform 7.3 onwards
    }
    final Task inGroup = taskQuery.singleResult();
    Assertions.assertThat(inGroup)
      .overridingErrorMessage("Expecting %s to have candidate group '%s', but found it not to have that candidate group!",
        toString(current),
        candidateGroupId)
      .isNotNull();
    return this;
  }

  /**
   * Verifies the expectation that the {@link Task} is currently waiting to
   * be assigned to a specified candidate user.
   *
   * @param   candidateUserId id of a candidate user the task is waiting to be assigned to
   * @return  this {@link TaskAssert}
   * @since   Camunda Platform 7.0
   */
  public TaskAssert hasCandidateUser(final String candidateUserId) {
    return hasCandidateUser(candidateUserId, true);
  }

  /**
   * Verifies the expectation that the {@link Task} is currently associated to the
   * specified candidate user - no matter whether it is already assigned to a
   * specific user or not.
   *
   * @param   candidateUserId id of a candidate user the task is associated to
   * @return  this {@link TaskAssert}
   * @since   Camunda Platform 7.3
   */
  public TaskAssert hasCandidateUserAssociated(final String candidateUserId) {
    return hasCandidateUser(candidateUserId, false);
  }

  public TaskAssert hasCandidateUser(final String candidateUserId, boolean unassignedOnly) {
    Assertions.assertThat(candidateUserId).isNotNull();
    final Task current = getExistingCurrent();
    final TaskQuery taskQuery = taskQuery().taskId(actual.getId()).taskCandidateUser(candidateUserId);
    if (unassignedOnly) {
      isNotAssigned(); // Useful for better assertion error message in case of assigned task
    } else {
      taskQuery.includeAssignedTasks(); // Available from Camunda Platform 7.3 onwards
    }
    final Task withUser = taskQuery.singleResult();
    Assertions.assertThat(withUser)
        .overridingErrorMessage("Expecting %s to have candidate user '%s', but found it not to have that candidate user!",
          toString(current),
          candidateUserId)
      .isNotNull();
    return this;
  }

  /**
   * Verifies the due date of a {@link Task}.
   *
   * @param   dueDate the date the task should be due at
   * @return  this {@link TaskAssert}
   */
  public TaskAssert hasDueDate(final Date dueDate) {
    Task current = getExistingCurrent();
    Assertions.assertThat(dueDate).isNotNull();
    Assertions.assertThat(current.getDueDate())
        .overridingErrorMessage("Expecting %s to be due at '%s', but found it to be due at '%s'!",
          toString(current),
          dueDate,
          current.getDueDate()
        )
      .isEqualTo(dueDate);
    return this;
  }

  /**
   * Verifies the definition key of a {@link Task}. This key can be found
   * in the &lt;userTask id="myTaskDefinitionKey" .../&gt; attribute of the
   * process definition BPMN 2.0 XML file.
   *
   * @param   taskDefinitionKey the expected value of the task/@id attribute
   * @return  this {@link TaskAssert}
   */
  public TaskAssert hasDefinitionKey(final String taskDefinitionKey) {
    Task current = getExistingCurrent();
    Assertions.assertThat(taskDefinitionKey).isNotNull();
    Assertions.assertThat(current.getTaskDefinitionKey())
      .overridingErrorMessage("Expecting %s to have definition key '%s', but found it to have '%s'!",
        toString(current),
        taskDefinitionKey,
        current.getTaskDefinitionKey()
      ).isEqualTo(taskDefinitionKey);
    return this;
  }

  /**
   * Verifies the expectation that the {@link Task} has a specified form key.
   *
   * @param   formKey the expected form key.
   * @return  this {@link TaskAssert}
   */
  public TaskAssert hasFormKey(String formKey) {
    Task current = getExistingCurrent();
    String actualformKey = formService().getTaskFormKey(current.getProcessDefinitionId(), current.getTaskDefinitionKey());
    Assertions.assertThat(actualformKey)
      .overridingErrorMessage("Expecting %s to have a form key '%s', but found it to to have form key '%s'!", toString(current), formKey, actualformKey)
      .isEqualTo(formKey);
    return this;
  }

  /**
   * Verifies the internal id of a {@link Task}.
   *
   * @param   id the expected value of the internal task id
   * @return  this {@link TaskAssert}
   */
  public TaskAssert hasId(final String id) {
    Task current = getExistingCurrent();
    Assertions.assertThat(id).isNotNull();
    Assertions.assertThat(current.getId())
      .overridingErrorMessage("Expecting %s to have internal id '%s', but found it to be '%s'!",
        toString(current),
        id,
        current.getId()
      ).isEqualTo(id);
    return this;
  }

  /**
   * Verifies the name (label) of a {@link Task}. This name can be found
   * in the &lt;userTask name="myName" .../&gt; attribute of the
   * process definition BPMN 2.0 XML file.
   *
   * @param   name the expected value of the name
   * @return  this {@link TaskAssert}
   */
  public TaskAssert hasName(final String name) {
    Task current = getExistingCurrent();
    Assertions.assertThat(name).isNotNull();
    Assertions.assertThat(current.getName())
      .overridingErrorMessage("Expecting %s to have name '%s', but found it to be '%s'!",
        toString(current),
        name,
        current.getName()
      ).isEqualTo(name);
    return this;
  }

  /**
   * Verifies the description of a {@link Task}. This description can be found
   * in the &lt;userTask&gt;&lt;documentation&gt;description&lt;/documentation&gt;&lt;/userTask&gt;
   * element of the process definition BPMN 2.0 XML file.
   *
   * @param   description the expected value of the description
   * @return  this {@link TaskAssert}
   */
  public TaskAssert hasDescription(final String description) {
    Task current = getExistingCurrent();
    Assertions.assertThat(description).isNotNull();
    Assertions.assertThat(current.getDescription())
      .overridingErrorMessage("Expecting %s to have description '%s', but found it to be '%s'!",
        toString(current),
        description,
        current.getDescription())
      .isEqualTo(description);
    return this;
  }

  @Override
  protected String toString(Task task) {
    return task != null ?
      String.format("%s {" +
        "id='%s', " +
        "processInstanceId='%s', " +
        "taskDefinitionKey='%s', " +
        "name='%s'}",
        Task.class.getSimpleName(),
        task.getId(),
        task.getProcessInstanceId(),
        task.getTaskDefinitionKey(),
        task.getName()
      ) : null;
  }

  /* TaskQuery, automatically narrowed to {@link ProcessInstance} of actual
   * {@link Task}
   */
  @Override
  protected TaskQuery taskQuery() {
    return super.taskQuery().processInstanceId(actual.getProcessInstanceId());
  }

}
