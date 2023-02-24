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
package org.camunda.bpm.engine.test.api.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
public class TaskLastUpdatedTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  TaskService taskService;
  RuntimeService runtimeService;

  @Before
  public void setUp() {
    taskService = engineRule.getTaskService();
    runtimeService = engineRule.getRuntimeService();
  }

  @After
  public void tearDown() {
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      // standalone tasks (deployed process are cleaned up by the engine rule)
      if(task.getProcessDefinitionId() == null) {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }

  // make sure that time passes between two fast operations
  public Date getAfterCurrentTime() {
    return new Date(ClockUtil.getCurrentTime().getTime() + 1000L);
  }

  //make sure that time passes between two fast operations
  public Date getBeforeCurrentTime() {
    return new Date(ClockUtil.getCurrentTime().getTime() - 1000L);
  }

  @Test
  public void shouldNotSetLastUpdatedWithoutTaskUpdate() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    // no update to task, lastUpdated = null

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult.getLastUpdated()).isNull();
  }

  @Test
  @RequiredDatabase(excludes = {DbSqlSessionFactory.MYSQL})
  public void shouldSetLastUpdatedToExactlyNow() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date beforeUpdate = getBeforeCurrentTime();
    // fix time to one ms after
    Date now = new Date(beforeUpdate.getTime() + 1000L);
    ClockUtil.setCurrentTime(now);

    // when
    taskService.setAssignee(task.getId(), "myself");

    // then
    assertThat(task.getLastUpdated()).isNull();
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskUpdatedAfter(beforeUpdate).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isEqualTo(now);
  }

  @Test
  @RequiredDatabase(includes = {DbSqlSessionFactory.MYSQL})
  public void shouldSetLastUpdatedToExactlyNowIgnoringMillis() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date beforeUpdate = getBeforeCurrentTime();
    // fix time to one ms after
    Date now = new Date(beforeUpdate.getTime() + 1000L);
    ClockUtil.setCurrentTime(now);

    // when
    taskService.setAssignee(task.getId(), "myself");

    // then
    assertThat(task.getLastUpdated()).isNull();
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskUpdatedAfter(beforeUpdate).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isEqualToIgnoringMillis(now);
  }

  @Test
  public void shouldSetLastUpdatedOnDescriptionChange() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDescription("updated");
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.saveTask(task);

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnAssigneeChange() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setAssignee("myself");
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.saveTask(task);

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnVariableChange() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.setVariableLocal(task.getId(), "myVariable", "variableValue");

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnCreateAttachment() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.createAttachment(null, task.getId(), processInstance.getId(), "myAttachment", "attachmentDescription", "http://camunda.com");

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnChangeAttachment() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Attachment attachment = taskService.createAttachment(null, task.getId(), processInstance.getId(), "myAttachment", "attachmentDescription", "http://camunda.com");
    attachment.setDescription("updatedDescription");
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.saveAttachment(attachment);

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnDeleteAttachment() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Attachment attachment = taskService.createAttachment(null, task.getId(), processInstance.getId(), "myAttachment", "attachmentDescription", "http://camunda.com");
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.deleteAttachment(attachment.getId());

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnDeleteTaskAttachment() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Attachment attachment = taskService.createAttachment(null, task.getId(), processInstance.getId(), "myAttachment", "attachmentDescription", "http://camunda.com");
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.deleteTaskAttachment(task.getId(), attachment.getId());

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnComment() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.createComment(task.getId(), processInstance.getId(), "my comment");

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnClaimTask() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.claim(task.getId(), "myself");

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnEveryPropertyChange() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // when
    task.setAssignee("myself");
    taskService.saveTask(task);

    Date expectedBeforeSecondUpdate = getBeforeCurrentTime();

    task.setName("My Task");
    taskService.saveTask(task);

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(expectedBeforeSecondUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnDelegate() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // when
    taskService.claim(task.getId(), "myself");
    Date beforeDelegate = getBeforeCurrentTime();
    taskService.delegateTask(task.getId(), "someone");

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeDelegate);
  }

  @Test
  public void shouldSetLastUpdatedOnResolve() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.resolveTask(task.getId());

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnAddIdentityLink() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.addCandidateUser(task.getId(), "myself");

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnDeleteIdentityLink() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.addCandidateUser(task.getId(), "myself");
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.deleteUserIdentityLink(task.getId(), "myself", IdentityLinkType.CANDIDATE);

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  public void shouldSetLastUpdatedOnPriorityChange() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    Date beforeUpdate = getBeforeCurrentTime();

    // when
    taskService.setPriority(task.getId(), 1);

    // then
    Task taskResult = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeUpdate);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/form/DeployedCamundaFormSingleTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/form/task.html"})
  public void shouldSetLastUpdatedOnSubmitTaskForm() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("FormsProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    // delegate is necessary so that submitting the form does not complete the task
    taskService.delegateTask(task.getId(), "myself");
    Date beforeSubmit = getBeforeCurrentTime();

    // when
    engineRule.getFormService().submitTaskForm(task.getId(), null);

    // then
    Task taskResult = taskService.createTaskQuery().singleResult();
    assertThat(taskResult).isNotNull();
    assertThat(taskResult.getLastUpdated()).isAfter(beforeSubmit);
  }

  @Test
  public void shouldNotSaveTaskConcurrentlyUpdatedByDependentEntity() {
    // given
    Task task = taskService.newTask();
    taskService.saveTask(task);
    taskService.createComment(task.getId(), null, "");

    // when/then
    Assertions.assertThatThrownBy(() -> taskService.saveTask(task))
      .isInstanceOf(OptimisticLockingException.class);
  }
}
