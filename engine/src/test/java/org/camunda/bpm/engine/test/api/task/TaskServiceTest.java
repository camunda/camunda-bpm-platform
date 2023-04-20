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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskAlreadyClaimedException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Event;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 * @author Falko Menge
 */
public class TaskServiceTest {


  protected static final String TWO_TASKS_PROCESS = "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml";

  protected static final String USER_TASK_THROW_ERROR = "throw-error";
  protected static final String ERROR_CODE = "300";
  protected static final String ESCALATION_CODE = "432";
  protected static final String PROCESS_KEY = "process";
  protected static final String USER_TASK_AFTER_CATCH = "after-catch";
  protected static final String USER_TASK_AFTER_THROW = "after-throw";
  protected static final String USER_TASK_THROW_ESCALATION = "throw-escalation";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setJavaSerializationFormatEnabled(true));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private TaskService taskService;
  private RepositoryService repositoryService;
  private HistoryService historyService;
  private CaseService caseService;
  private IdentityService identityService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
    historyService = engineRule.getHistoryService();
    caseService = engineRule.getCaseService();
    identityService = engineRule.getIdentityService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @After
  public void tearDown() {
    ClockUtil.setCurrentTime(new Date());
  }

  @Test
  public void testSaveTaskUpdate() throws Exception{

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    Task task = taskService.newTask();
    task.setDescription("description");
    task.setName("taskname");
    task.setPriority(0);
    task.setAssignee("taskassignee");
    task.setOwner("taskowner");
    Date dueDate = sdf.parse("01/02/2003 04:05:06");
    task.setDueDate(dueDate);
    task.setCaseInstanceId("taskcaseinstanceid");
    taskService.saveTask(task);

    // Fetch the task again and update
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("description", task.getDescription());
    assertEquals("taskname", task.getName());
    assertEquals("taskassignee", task.getAssignee());
    assertEquals("taskowner", task.getOwner());
    assertEquals(dueDate, task.getDueDate());
    assertEquals(0, task.getPriority());
    assertEquals("taskcaseinstanceid", task.getCaseInstanceId());

    if (processEngineConfiguration.getHistoryLevel().getId()>= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstance historicTaskInstance = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(task.getId())
        .singleResult();
      assertEquals("taskname", historicTaskInstance.getName());
      assertEquals("description", historicTaskInstance.getDescription());
      assertEquals("taskassignee", historicTaskInstance.getAssignee());
      assertEquals("taskowner", historicTaskInstance.getOwner());
      assertEquals(dueDate, historicTaskInstance.getDueDate());
      assertEquals(0, historicTaskInstance.getPriority());
      assertEquals("taskcaseinstanceid", historicTaskInstance.getCaseInstanceId());
    }

    task.setName("updatedtaskname");
    task.setDescription("updateddescription");
    task.setPriority(1);
    task.setAssignee("updatedassignee");
    task.setOwner("updatedowner");
    dueDate = sdf.parse("01/02/2003 04:05:06");
    task.setDueDate(dueDate);
    task.setCaseInstanceId("updatetaskcaseinstanceid");
    taskService.saveTask(task);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("updatedtaskname", task.getName());
    assertEquals("updateddescription", task.getDescription());
    assertEquals("updatedassignee", task.getAssignee());
    assertEquals("updatedowner", task.getOwner());
    assertEquals(dueDate, task.getDueDate());
    assertEquals(1, task.getPriority());
    assertEquals("updatetaskcaseinstanceid", task.getCaseInstanceId());

    if (processEngineConfiguration.getHistoryLevel().getId()>= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstance historicTaskInstance = historyService
        .createHistoricTaskInstanceQuery()
        .taskId(task.getId())
        .singleResult();
      assertEquals("updatedtaskname", historicTaskInstance.getName());
      assertEquals("updateddescription", historicTaskInstance.getDescription());
      assertEquals("updatedassignee", historicTaskInstance.getAssignee());
      assertEquals("updatedowner", historicTaskInstance.getOwner());
      assertEquals(dueDate, historicTaskInstance.getDueDate());
      assertEquals(1, historicTaskInstance.getPriority());
      assertEquals("updatetaskcaseinstanceid", historicTaskInstance.getCaseInstanceId());
    }

    // Finally, delete task
    taskService.deleteTask(task.getId(), true);
  }

  @Test
  public void testSaveTaskSetParentTaskId() {
    // given
    Task parent = taskService.newTask("parent");
    taskService.saveTask(parent);

    Task task = taskService.newTask("subTask");

    // when
    task.setParentTaskId("parent");

    // then
    taskService.saveTask(task);

    // update task
    task = taskService.createTaskQuery().taskId("subTask").singleResult();

    assertEquals(parent.getId(), task.getParentTaskId());

    taskService.deleteTask("parent", true);
    taskService.deleteTask("subTask", true);
  }

  @Test
  public void testSaveTaskWithNonExistingParentTask() {
    // given
    Task task = taskService.newTask();

    // when
    task.setParentTaskId("non-existing");

    // then
    try {
      taskService.saveTask(task);
      fail("It should not be possible to save a task with a non existing parent task.");
    } catch (NotValidException e) {}
  }

  @Test
  public void testTaskOwner() {
    Task task = taskService.newTask();
    task.setOwner("johndoe");
    taskService.saveTask(task);

    // Fetch the task again and update
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("johndoe", task.getOwner());

    task.setOwner("joesmoe");
    taskService.saveTask(task);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("joesmoe", task.getOwner());

    // Finally, delete task
    taskService.deleteTask(task.getId(), true);
  }

  @Test
  public void testTaskComments() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      Task task = taskService.newTask();
      task.setOwner("johndoe");
      taskService.saveTask(task);
      String taskId = task.getId();

      identityService.setAuthenticatedUserId("johndoe");
      // Fetch the task again and update
      Comment comment = taskService.createComment(taskId, null, "look at this \n       isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg kajsh dfuieqpgkja rzvkfnjviuqerhogiuvysbegkjz lkhf ais liasduh flaisduh ajiasudh vaisudhv nsfd");
      assertNotNull(comment.getId());
      assertEquals("johndoe", comment.getUserId());
      assertEquals(taskId, comment.getTaskId());
      assertNull(comment.getProcessInstanceId());
      assertEquals("look at this isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg ...", ((Event)comment).getMessage());
      assertEquals("look at this \n       isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg kajsh dfuieqpgkja rzvkfnjviuqerhogiuvysbegkjz lkhf ais liasduh flaisduh ajiasudh vaisudhv nsfd", comment.getFullMessage());
      assertNotNull(comment.getTime());

      taskService.createComment(taskId, "pid", "one");
      taskService.createComment(taskId, "pid", "two");

      Set<String> expectedComments = new HashSet<>();
      expectedComments.add("one");
      expectedComments.add("two");

      Set<String> comments = new HashSet<>();
      for (Comment cmt: taskService.getProcessInstanceComments("pid")) {
        comments.add(cmt.getFullMessage());
      }

      assertEquals(expectedComments, comments);

      // Finally, delete task
      taskService.deleteTask(taskId, true);
    }
  }

  @Test
  public void testAddTaskCommentNull() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      Task task = taskService.newTask("testId");
      taskService.saveTask(task);
      try {
        taskService.createComment(task.getId(), null, null);
        fail("Expected process engine exception");
      }
      catch (ProcessEngineException e) {}
      finally {
        taskService.deleteTask(task.getId(), true);
      }
    }
  }

  @Test
  public void testAddTaskNullComment() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      try {
        taskService.createComment(null, null, "test");
        fail("Expected process engine exception");
      }
      catch (ProcessEngineException e){}
    }
  }

  @Test
  public void testTaskAttachments() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      Task task = taskService.newTask();
      task.setOwner("johndoe");
      taskService.saveTask(task);
      String taskId = task.getId();
      identityService.setAuthenticatedUserId("johndoe");
      // Fetch the task again and update
      taskService.createAttachment("web page", taskId, "someprocessinstanceid", "weatherforcast", "temperatures and more", "http://weather.com");
      Attachment attachment = taskService.getTaskAttachments(taskId).get(0);
      assertEquals("weatherforcast", attachment.getName());
      assertEquals("temperatures and more", attachment.getDescription());
      assertEquals("web page", attachment.getType());
      assertEquals(taskId, attachment.getTaskId());
      assertEquals("someprocessinstanceid", attachment.getProcessInstanceId());
      assertEquals("http://weather.com", attachment.getUrl());
      assertNull(taskService.getAttachmentContent(attachment.getId()));

      // Finally, clean up
      taskService.deleteTask(taskId);

      assertEquals(0, taskService.getTaskComments(taskId).size());
      assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskId(taskId).list().size());

      taskService.deleteTask(taskId, true);
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testProcessAttachmentsOneProcessExecution() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

      // create attachment
      Attachment attachment = taskService.createAttachment("web page", null, processInstance.getId(), "weatherforcast", "temperatures and more",
          "http://weather.com");

      assertEquals("weatherforcast", attachment.getName());
      assertEquals("temperatures and more", attachment.getDescription());
      assertEquals("web page", attachment.getType());
      assertNull(attachment.getTaskId());
      assertEquals(processInstance.getId(), attachment.getProcessInstanceId());
      assertEquals("http://weather.com", attachment.getUrl());
      assertNull(taskService.getAttachmentContent(attachment.getId()));
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/twoParallelTasksProcess.bpmn20.xml" })
  public void testProcessAttachmentsTwoProcessExecutions() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoParallelTasksProcess");

      // create attachment
      Attachment attachment = taskService.createAttachment("web page", null, processInstance.getId(), "weatherforcast", "temperatures and more",
          "http://weather.com");

      assertEquals("weatherforcast", attachment.getName());
      assertEquals("temperatures and more", attachment.getDescription());
      assertEquals("web page", attachment.getType());
      assertNull(attachment.getTaskId());
      assertEquals(processInstance.getId(), attachment.getProcessInstanceId());
      assertEquals("http://weather.com", attachment.getUrl());
      assertNull(taskService.getAttachmentContent(attachment.getId()));
    }
  }

  @Test
  public void testSaveAttachment() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      // given
      Task task = taskService.newTask();
      taskService.saveTask(task);

      String attachmentType = "someAttachment";
      String processInstanceId = "someProcessInstanceId";
      String attachmentName = "attachmentName";
      String attachmentDescription = "attachmentDescription";
      String url = "http://camunda.org";

      Attachment attachment = taskService.createAttachment(
          attachmentType,
          task.getId(),
          processInstanceId,
          attachmentName,
          attachmentDescription,
          url);

      // when
      attachment.setDescription("updatedDescription");
      attachment.setName("updatedName");
      taskService.saveAttachment(attachment);

      // then
      Attachment fetchedAttachment = taskService.getAttachment(attachment.getId());
      assertEquals(attachment.getId(), fetchedAttachment.getId());
      assertEquals(attachmentType, fetchedAttachment.getType());
      assertEquals(task.getId(), fetchedAttachment.getTaskId());
      assertEquals(processInstanceId, fetchedAttachment.getProcessInstanceId());
      assertEquals("updatedName", fetchedAttachment.getName());
      assertEquals("updatedDescription", fetchedAttachment.getDescription());
      assertEquals(url, fetchedAttachment.getUrl());

      taskService.deleteTask(task.getId(), true);
    }
  }

  @Test
  public void testTaskDelegation() {
    Task task = taskService.newTask();
    task.setOwner("johndoe");
    task.delegate("joesmoe");
    taskService.saveTask(task);
    String taskId = task.getId();

    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals("johndoe", task.getOwner());
    assertEquals("joesmoe", task.getAssignee());
    assertEquals(DelegationState.PENDING, task.getDelegationState());

    taskService.resolveTask(taskId);
    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals("johndoe", task.getOwner());
    assertEquals("johndoe", task.getAssignee());
    assertEquals(DelegationState.RESOLVED, task.getDelegationState());

    task.setAssignee(null);
    task.setDelegationState(null);
    taskService.saveTask(task);
    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals("johndoe", task.getOwner());
    assertNull(task.getAssignee());
    assertNull(task.getDelegationState());

    task.setAssignee("jackblack");
    task.setDelegationState(DelegationState.RESOLVED);
    taskService.saveTask(task);
    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals("johndoe", task.getOwner());
    assertEquals("jackblack", task.getAssignee());
    assertEquals(DelegationState.RESOLVED, task.getDelegationState());

    // Finally, delete task
    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testTaskDelegationThroughServiceCall() {
    Task task = taskService.newTask();
    task.setOwner("johndoe");
    taskService.saveTask(task);
    String taskId = task.getId();

    // Fetch the task again and update
    task = taskService.createTaskQuery().taskId(taskId).singleResult();

    taskService.delegateTask(taskId, "joesmoe");

    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals("johndoe", task.getOwner());
    assertEquals("joesmoe", task.getAssignee());
    assertEquals(DelegationState.PENDING, task.getDelegationState());

    taskService.resolveTask(taskId);

    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals("johndoe", task.getOwner());
    assertEquals("johndoe", task.getAssignee());
    assertEquals(DelegationState.RESOLVED, task.getDelegationState());

    // Finally, delete task
    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testTaskAssignee() {
    Task task = taskService.newTask();
    task.setAssignee("johndoe");
    taskService.saveTask(task);

    // Fetch the task again and update
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("johndoe", task.getAssignee());

    task.setAssignee("joesmoe");
    taskService.saveTask(task);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("joesmoe", task.getAssignee());

    // Finally, delete task
    taskService.deleteTask(task.getId(), true);
  }


  @Test
  public void testSaveTaskNullTask() {
    try {
      taskService.saveTask(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("task is null", ae.getMessage());
    }
  }

  @Test
  public void testDeleteTaskNullTaskId() {
    try {
      taskService.deleteTask(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Expected exception
    }
  }

  @Test
  public void testDeleteTaskUnexistingTaskId() {
    // Deleting unexisting task should be silently ignored
    taskService.deleteTask("unexistingtaskid");
  }

  @Test
  public void testDeleteTasksNullTaskIds() {
    try {
      taskService.deleteTasks(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Expected exception
    }
  }

  @Test
  public void testDeleteTasksTaskIdsUnexistingTaskId() {

    Task existingTask = taskService.newTask();
    taskService.saveTask(existingTask);

    // The unexisting taskId's should be silently ignored. Existing task should
    // have been deleted.
    taskService.deleteTasks(Arrays.asList("unexistingtaskid1", existingTask.getId()), true);

    existingTask = taskService.createTaskQuery().taskId(existingTask.getId()).singleResult();
    assertNull(existingTask);
  }

  @Test
  public void testClaimNullArguments() {
    try {
      taskService.claim(null, "userid");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Test
  public void testClaimUnexistingTaskId() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.claim("unexistingtaskid", user.getId());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingtaskid", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

  @Test
  public void testClaimAlreadyClaimedTaskByOtherUser() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    User secondUser = identityService.newUser("seconduser");
    identityService.saveUser(secondUser);

    // Claim task the first time
    taskService.claim(task.getId(), user.getId());

    try {
      taskService.claim(task.getId(), secondUser.getId());
      fail("ProcessEngineException expected");
    } catch (TaskAlreadyClaimedException ae) {
      testRule.assertTextPresent("Task '" + task.getId() + "' is already claimed by someone else.", ae.getMessage());
    }

    taskService.deleteTask(task.getId(), true);
    identityService.deleteUser(user.getId());
    identityService.deleteUser(secondUser.getId());
  }

  @Test
  public void testClaimAlreadyClaimedTaskBySameUser() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    // Claim task the first time
    taskService.claim(task.getId(), user.getId());
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();

    // Claim the task again with the same user. No exception should be thrown
    taskService.claim(task.getId(), user.getId());

    taskService.deleteTask(task.getId(), true);
    identityService.deleteUser(user.getId());
  }

  @Test
  public void testUnClaimTask() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    // Claim task the first time
    taskService.claim(task.getId(), user.getId());
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals(user.getId(), task.getAssignee());

    // Unclaim the task
    taskService.claim(task.getId(), null);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertNull(task.getAssignee());

    taskService.deleteTask(task.getId(), true);
    identityService.deleteUser(user.getId());
  }

  @Test
  public void testCompleteTaskNullTaskId() {
    try {
      taskService.complete(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Test
  public void testCompleteTaskUnexistingTaskId() {
    try {
      taskService.complete("unexistingtask");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }

  @Test
  public void testCompleteTaskWithParametersNullTaskId() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("myKey", "myValue");

    try {
      taskService.complete(null, variables);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Test
  public void testCompleteTaskWithParametersUnexistingTaskId() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("myKey", "myValue");

    try {
      taskService.complete("unexistingtask", variables);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }

  @Test
  public void testCompleteTaskWithParametersNullParameters() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    String taskId = task.getId();
    taskService.complete(taskId, null);

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      historyService.deleteHistoricTaskInstance(taskId);
    }

    // Fetch the task again
    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertNull(task);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCompleteTaskWithParametersEmptyParameters() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    String taskId = task.getId();
    taskService.complete(taskId, Collections.EMPTY_MAP);

    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      historyService.deleteHistoricTaskInstance(taskId);
    }

    // Fetch the task again
    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertNull(task);
  }


  @Deployment(resources = TWO_TASKS_PROCESS)
  @Test
  public void testCompleteWithParametersTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

    // Fetch first task
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("First task", task.getName());

    // Complete first task
    Map<String, Object> taskParams = new HashMap<>();
    taskParams.put("myParam", "myValue");
    taskService.complete(task.getId(), taskParams);

    // Fetch second task
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Second task", task.getName());

    // Verify task parameters set on execution
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(1, variables.size());
    assertEquals("myValue", variables.get("myParam"));
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/task/TaskServiceTest.testCompleteTaskWithVariablesInReturn.bpmn20.xml" })
  @Test
  public void testCompleteTaskWithVariablesInReturn() {
    String processVarName = "processVar";
    String processVarValue = "processVarValue";

    String taskVarName = "taskVar";
    String taskVarValue = "taskVarValue";

    Map<String, Object> variables = new HashMap<>();
    variables.put(processVarName, processVarValue);

    runtimeService.startProcessInstanceByKey("TaskServiceTest.testCompleteTaskWithVariablesInReturn", variables);

    Task firstUserTask = taskService.createTaskQuery().taskName("First User Task").singleResult();
    taskService.setVariable(firstUserTask.getId(), "x", 1);
    // local variables should not be returned
    taskService.setVariableLocal(firstUserTask.getId(), "localVar", "localVarValue");

    Map<String, Object> additionalVariables = new HashMap<>();
    additionalVariables.put(taskVarName, taskVarValue);

    // After completion of firstUserTask a script Task sets 'x' = 5
    VariableMap vars = taskService.completeWithVariablesInReturn(firstUserTask.getId(), additionalVariables, true);

    assertEquals(3, vars.size());
    assertEquals(5, vars.get("x"));
    assertEquals(ValueType.INTEGER, vars.getValueTyped("x").getType());
    assertEquals(processVarValue, vars.get(processVarName));
    assertEquals(taskVarValue, vars.get(taskVarName));
    assertEquals(ValueType.STRING, vars.getValueTyped(taskVarName).getType());

    additionalVariables = new HashMap<>();
    additionalVariables.put("x", 7);
    Task secondUserTask = taskService.createTaskQuery().taskName("Second User Task").singleResult();

    vars = taskService.completeWithVariablesInReturn(secondUserTask.getId(), additionalVariables, true);
    assertEquals(3, vars.size());
    assertEquals(7, vars.get("x"));
    assertEquals(processVarValue, vars.get(processVarName));
    assertEquals(taskVarValue, vars.get(taskVarName));
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testCompleteStandaloneTaskWithVariablesInReturn() {
    String taskVarName = "taskVar";
    String taskVarValue = "taskVarValue";

    String taskId = "myTask";
    Task standaloneTask = taskService.newTask(taskId);
    taskService.saveTask(standaloneTask);

    Map<String, Object> variables = new HashMap<>();
    variables.put(taskVarName, taskVarValue);

    Map<String, Object> returnedVariables = taskService.completeWithVariablesInReturn(taskId, variables, true);
    // expect empty Map for standalone tasks
    assertEquals(0, returnedVariables.size());

    historyService.deleteHistoricTaskInstance(taskId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/twoParallelTasksProcess.bpmn20.xml" })
  @Test
  public void testCompleteTaskWithVariablesInReturnParallel() {
    String processVarName = "processVar";
    String processVarValue = "processVarValue";

    String task1VarName = "taskVar1";
    String task2VarName = "taskVar2";
    String task1VarValue = "taskVarValue1";
    String task2VarValue = "taskVarValue2";

    String additionalVar = "additionalVar";
    String additionalVarValue = "additionalVarValue";

    Map<String, Object> variables = new HashMap<>();
    variables.put(processVarName, processVarValue);
    runtimeService.startProcessInstanceByKey("twoParallelTasksProcess", variables);

    Task firstTask = taskService.createTaskQuery().taskName("First Task").singleResult();
    taskService.setVariable(firstTask.getId(), task1VarName, task1VarValue);
    Task secondTask = taskService.createTaskQuery().taskName("Second Task").singleResult();
    taskService.setVariable(secondTask.getId(), task2VarName, task2VarValue);

    Map<String, Object> vars = taskService.completeWithVariablesInReturn(firstTask.getId(), null, true);

    assertEquals(3, vars.size());
    assertEquals(processVarValue, vars.get(processVarName));
    assertEquals(task1VarValue, vars.get(task1VarName));
    assertEquals(task2VarValue, vars.get(task2VarName));

    Map<String, Object> additionalVariables = new HashMap<>();
    additionalVariables.put(additionalVar, additionalVarValue);

    vars = taskService.completeWithVariablesInReturn(secondTask.getId(), additionalVariables, true);
    assertEquals(4, vars.size());
    assertEquals(processVarValue, vars.get(processVarName));
    assertEquals(task1VarValue, vars.get(task1VarName));
    assertEquals(task2VarValue, vars.get(task2VarName));
    assertEquals(additionalVarValue, vars.get(additionalVar));
  }

  /**
   * Tests that the variablesInReturn logic is not applied
   * when we call the regular complete API. This is a performance optimization.
   * Loading all variables may be expensive.
   */
  @Test
  public void testCompleteTaskAndDoNotDeserializeVariables()
  {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
      .startEvent()
      .subProcess()
      .embeddedSubProcess()
      .startEvent()
      .userTask("task1")
      .userTask("task2")
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    testRule.deploy(process);

    runtimeService.startProcessInstanceByKey("process", Variables.putValue("var", "val"));

    final Task task = taskService.createTaskQuery().singleResult();

    // when
    final boolean hasLoadedAnyVariables =
      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Boolean>() {

        @Override
        public Boolean execute(CommandContext commandContext) {
          taskService.complete(task.getId());
          return !commandContext.getDbEntityManager().getCachedEntitiesByType(VariableInstanceEntity.class).isEmpty();
        }
      });

    // then
    assertThat(hasLoadedAnyVariables).isFalse();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml")
  public void testCompleteTaskWithVariablesInReturnShouldDeserializeObjectValue()
  {
    // given
    ObjectValue value = Variables.objectValue("value").create();
    VariableMap variables = Variables.createVariables().putValue("var", value);

    runtimeService.startProcessInstanceByKey("twoTasksProcess", variables);

    Task task = taskService.createTaskQuery().singleResult();

    // when
    VariableMap result = taskService.completeWithVariablesInReturn(task.getId(), null, true);

    // then
    ObjectValue returnedValue = result.getValueTyped("var");
    assertThat(returnedValue.isDeserialized()).isTrue();
    assertThat(returnedValue.getValue()).isEqualTo("value");
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml")
  public void testCompleteTaskWithVariablesInReturnShouldNotDeserializeObjectValue()
  {
    // given
    ObjectValue value = Variables.objectValue("value").create();
    VariableMap variables = Variables.createVariables().putValue("var", value);

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("twoTasksProcess", variables);
    String serializedValue = ((ObjectValue) runtimeService.getVariableTyped(instance.getId(), "var")).getValueSerialized();

    Task task = taskService.createTaskQuery().singleResult();

    // when
    VariableMap result = taskService.completeWithVariablesInReturn(task.getId(), null, false);

    // then
    ObjectValue returnedValue = result.getValueTyped("var");
    assertThat(returnedValue.isDeserialized()).isFalse();
    assertThat(returnedValue.getValueSerialized()).isEqualTo(serializedValue);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  @Test
  public void testCompleteTaskWithVariablesInReturnCMMN() {
    String taskVariableName = "taskVar";
    String taskVariableValue = "taskVal";

    String caseDefinitionId = repositoryService.createCaseDefinitionQuery().singleResult().getId();
    caseService.withCaseDefinition(caseDefinitionId).create();

    Task task1 = taskService.createTaskQuery().singleResult();
    assertNotNull(task1);

    taskService.setVariable(task1.getId(), taskVariableName, taskVariableValue);
    Map<String, Object> vars = taskService.completeWithVariablesInReturn(task1.getId(), null, true);
    assertNull(vars);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testCompleteTaskShouldCompleteCaseExecution() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // when
    taskService.complete(task.getId());

    // then

    task = taskService.createTaskQuery().singleResult();

    assertNull(task);

    CaseExecution caseExecution = caseService
      .createCaseExecutionQuery()
      .activityId("PI_HumanTask_1")
      .singleResult();

    assertNull(caseExecution);

    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .singleResult();

    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());
  }

  @Test
  public void testResolveTaskNullTaskId() {
    try {
      taskService.resolveTask(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Test
  public void testResolveTaskUnexistingTaskId() {
    try {
      taskService.resolveTask("unexistingtask");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }

  @Test
  public void testResolveTaskWithParametersNullParameters() {
    Task task = taskService.newTask();
    task.setDelegationState(DelegationState.PENDING);
    taskService.saveTask(task);

    String taskId = task.getId();
    taskService.resolveTask(taskId, null);

    if (processEngineConfiguration.getHistoryLevel().getId()>= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      historyService.deleteHistoricTaskInstance(taskId);
    }

    // Fetch the task again
    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals(DelegationState.RESOLVED, task.getDelegationState());

    taskService.deleteTask(taskId, true);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testResolveTaskWithParametersEmptyParameters() {
    Task task = taskService.newTask();
    task.setDelegationState(DelegationState.PENDING);
    taskService.saveTask(task);

    String taskId = task.getId();
    taskService.resolveTask(taskId, Collections.EMPTY_MAP);

    if (processEngineConfiguration.getHistoryLevel().getId()>= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      historyService.deleteHistoricTaskInstance(taskId);
    }

    // Fetch the task again
    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals(DelegationState.RESOLVED, task.getDelegationState());

    taskService.deleteTask(taskId, true);
  }

  @Deployment(resources = TWO_TASKS_PROCESS)
  @Test
  public void testResolveWithParametersTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

    // Fetch first task
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("First task", task.getName());

    task.delegate("johndoe");

    // Resolve first task
    Map<String, Object> taskParams = new HashMap<>();
    taskParams.put("myParam", "myValue");
    taskService.resolveTask(task.getId(), taskParams);

    // Verify that task is resolved
    task = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED).singleResult();
    assertEquals("First task", task.getName());

    // Verify task parameters set on execution
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(1, variables.size());
    assertEquals("myValue", variables.get("myParam"));
  }

  @Test
  public void testSetAssignee() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    Task task = taskService.newTask();
    assertNull(task.getAssignee());
    taskService.saveTask(task);

    // Set assignee
    taskService.setAssignee(task.getId(), user.getId());

    // Fetch task again
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals(user.getId(), task.getAssignee());

    identityService.deleteUser(user.getId());
    taskService.deleteTask(task.getId(), true);
  }

  @Test
  public void testSetAssigneeNullTaskId() {
    try {
      taskService.setAssignee(null, "userId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Test
  public void testSetAssigneeUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.setAssignee("unexistingTaskId", user.getId());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

  @Test
  public void testAddCandidateUserDuplicate() {
    // Check behavior when adding the same user twice as candidate
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    Task task = taskService.newTask();
    taskService.saveTask(task);

    taskService.addCandidateUser(task.getId(), user.getId());

    // Add as candidate the second time
    taskService.addCandidateUser(task.getId(), user.getId());

    identityService.deleteUser(user.getId());
    taskService.deleteTask(task.getId(), true);
  }

  @Test
  public void testAddCandidateUserNullTaskId() {
    try {
      taskService.addCandidateUser(null, "userId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Test
  public void testAddCandidateUserNullUserId() {
    try {
      taskService.addCandidateUser("taskId", null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }

  @Test
  public void testAddCandidateUserUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.addCandidateUser("unexistingTaskId", user.getId());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

  @Test
  public void testAddCandidateGroupNullTaskId() {
    try {
      taskService.addCandidateGroup(null, "groupId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Test
  public void testAddCandidateGroupNullGroupId() {
    try {
      taskService.addCandidateGroup("taskId", null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }

  @Test
  public void testAddCandidateGroupUnexistingTask() {
    Group group = identityService.newGroup("group");
    identityService.saveGroup(group);
    try {
      taskService.addCandidateGroup("unexistingTaskId", group.getId());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }
    identityService.deleteGroup(group.getId());
  }

  @Test
  public void testAddGroupIdentityLinkNullTaskId() {
    try {
      taskService.addGroupIdentityLink(null, "groupId", IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Test
  public void testAddGroupIdentityLinkNullUserId() {
    try {
      taskService.addGroupIdentityLink("taskId", null, IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }

  @Test
  public void testAddGroupIdentityLinkUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.addGroupIdentityLink("unexistingTaskId", user.getId(), IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

  @Test
  public void testAddUserIdentityLinkNullTaskId() {
    try {
      taskService.addUserIdentityLink(null, "userId", IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Test
  public void testAddUserIdentityLinkNullUserId() {
    try {
      taskService.addUserIdentityLink("taskId", null, IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }

  @Test
  public void testAddUserIdentityLinkUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.addUserIdentityLink("unexistingTaskId", user.getId(), IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

  @Test
  public void testGetIdentityLinksWithCandidateUser() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();

    identityService.saveUser(identityService.newUser("kermit"));

    taskService.addCandidateUser(taskId, "kermit");
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(1, identityLinks.size());
    assertEquals("kermit", identityLinks.get(0).getUserId());
    assertNull(identityLinks.get(0).getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLinks.get(0).getType());

    //cleanup
    taskService.deleteTask(taskId, true);
    identityService.deleteUser("kermit");
  }

  @Test
  public void testGetIdentityLinksWithCandidateGroup() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();

    identityService.saveGroup(identityService.newGroup("muppets"));

    taskService.addCandidateGroup(taskId, "muppets");
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(1, identityLinks.size());
    assertEquals("muppets", identityLinks.get(0).getGroupId());
    assertNull(identityLinks.get(0).getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLinks.get(0).getType());

    //cleanup
    taskService.deleteTask(taskId, true);
    identityService.deleteGroup("muppets");
  }

  @Test
  public void testGetIdentityLinksWithAssignee() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();

    identityService.saveUser(identityService.newUser("kermit"));

    taskService.claim(taskId, "kermit");
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(1, identityLinks.size());
    assertEquals("kermit", identityLinks.get(0).getUserId());
    assertNull(identityLinks.get(0).getGroupId());
    assertEquals(IdentityLinkType.ASSIGNEE, identityLinks.get(0).getType());

    //cleanup
    taskService.deleteTask(taskId, true);
    identityService.deleteUser("kermit");
  }

  @Test
  public void testGetIdentityLinksWithNonExistingAssignee() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();

    taskService.claim(taskId, "nonExistingAssignee");
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(1, identityLinks.size());
    assertEquals("nonExistingAssignee", identityLinks.get(0).getUserId());
    assertNull(identityLinks.get(0).getGroupId());
    assertEquals(IdentityLinkType.ASSIGNEE, identityLinks.get(0).getType());

    //cleanup
    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testGetIdentityLinksWithOwner() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();

    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("fozzie"));

    taskService.claim(taskId, "kermit");
    taskService.delegateTask(taskId, "fozzie");

    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(2, identityLinks.size());

    IdentityLink assignee = identityLinks.get(0);
    assertEquals("fozzie", assignee.getUserId());
    assertNull(assignee.getGroupId());
    assertEquals(IdentityLinkType.ASSIGNEE, assignee.getType());

    IdentityLink owner = identityLinks.get(1);
    assertEquals("kermit", owner.getUserId());
    assertNull(owner.getGroupId());
    assertEquals(IdentityLinkType.OWNER, owner.getType());

    //cleanup
    taskService.deleteTask(taskId, true);
    identityService.deleteUser("kermit");
    identityService.deleteUser("fozzie");
  }

  @Test
  public void testGetIdentityLinksWithNonExistingOwner() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();

    taskService.claim(taskId, "nonExistingOwner");
    taskService.delegateTask(taskId, "nonExistingAssignee");
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(2, identityLinks.size());

    IdentityLink assignee = identityLinks.get(0);
    assertEquals("nonExistingAssignee", assignee.getUserId());
    assertNull(assignee.getGroupId());
    assertEquals(IdentityLinkType.ASSIGNEE, assignee.getType());

    IdentityLink owner = identityLinks.get(1);
    assertEquals("nonExistingOwner", owner.getUserId());
    assertNull(owner.getGroupId());
    assertEquals(IdentityLinkType.OWNER, owner.getType());

    //cleanup
    taskService.deleteTask(taskId, true);
  }

  @Test
  public void testSetPriority() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    taskService.setPriority(task.getId(), 12345);

    // Fetch task again to check if the priority is set
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals(12345, task.getPriority());

    taskService.deleteTask(task.getId(), true);
  }

  @Test
  public void testSetPriorityUnexistingTaskId() {
    try {
      taskService.setPriority("unexistingtask", 12345);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }

  @Test
  public void testSetPriorityNullTaskId() {
    try {
      taskService.setPriority(null, 12345);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  /**
   * @see http://jira.codehaus.org/browse/ACT-1059
   */
  @Test
  public void testSetDelegationState() {
    Task task = taskService.newTask();
    task.setOwner("wuzh");
    task.delegate("other");
    taskService.saveTask(task);
    String taskId = task.getId();

    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals("wuzh", task.getOwner());
    assertEquals("other", task.getAssignee());
    assertEquals(DelegationState.PENDING, task.getDelegationState());

    task.setDelegationState(DelegationState.RESOLVED);
    taskService.saveTask(task);

    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertEquals("wuzh", task.getOwner());
    assertEquals("other", task.getAssignee());
    assertEquals(DelegationState.RESOLVED, task.getDelegationState());

    taskService.deleteTask(taskId, true);
  }

  private void checkHistoricVariableUpdateEntity(String variableName, String processInstanceId) {
    if (processEngineConfiguration.getHistoryLevel().getId() == ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      boolean deletedVariableUpdateFound = false;

      List<HistoricDetail> resultSet = historyService.createHistoricDetailQuery().processInstanceId(processInstanceId).list();
      for (HistoricDetail currentHistoricDetail : resultSet) {
        assertTrue(currentHistoricDetail instanceof HistoricDetailVariableInstanceUpdateEntity);
        HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = (HistoricDetailVariableInstanceUpdateEntity) currentHistoricDetail;

        if (historicVariableUpdate.getName().equals(variableName)) {
          if (historicVariableUpdate.getValue() == null) {
            if (deletedVariableUpdateFound) {
              fail("Mismatch: A HistoricVariableUpdateEntity with a null value already found");
            } else {
              deletedVariableUpdateFound = true;
            }
          }
        }
      }

      assertTrue(deletedVariableUpdateFound);
    }
  }

  @Deployment(resources = {
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testRemoveVariable() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task currentTask = taskService.createTaskQuery().singleResult();

    taskService.setVariable(currentTask.getId(), "variable1", "value1");
    assertEquals("value1", taskService.getVariable(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));

    taskService.removeVariable(currentTask.getId(), "variable1");

    assertNull(taskService.getVariable(currentTask.getId(), "variable1"));

    checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
  }

  @Test
  public void testRemoveVariableNullTaskId() {
    try {
      taskService.removeVariable(null, "variable");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Deployment(resources = {
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testRemoveVariables() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task currentTask = taskService.createTaskQuery().singleResult();

    Map<String, Object> varsToDelete = new HashMap<>();
    varsToDelete.put("variable1", "value1");
    varsToDelete.put("variable2", "value2");
    taskService.setVariables(currentTask.getId(), varsToDelete);
    taskService.setVariable(currentTask.getId(), "variable3", "value3");

    assertEquals("value1", taskService.getVariable(currentTask.getId(), "variable1"));
    assertEquals("value2", taskService.getVariable(currentTask.getId(), "variable2"));
    assertEquals("value3", taskService.getVariable(currentTask.getId(), "variable3"));
    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable2"));
    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable3"));

    taskService.removeVariables(currentTask.getId(), varsToDelete.keySet());

    assertNull(taskService.getVariable(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariable(currentTask.getId(), "variable2"));
    assertEquals("value3", taskService.getVariable(currentTask.getId(), "variable3"));

    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable2"));
    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable3"));

    checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveVariablesNullTaskId() {
    try {
      taskService.removeVariables(null, Collections.EMPTY_LIST);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Deployment(resources = {
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testRemoveVariableLocal() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task currentTask = taskService.createTaskQuery().singleResult();

    taskService.setVariableLocal(currentTask.getId(), "variable1", "value1");
    assertEquals("value1", taskService.getVariable(currentTask.getId(), "variable1"));
    assertEquals("value1", taskService.getVariableLocal(currentTask.getId(), "variable1"));

    taskService.removeVariableLocal(currentTask.getId(), "variable1");

    assertNull(taskService.getVariable(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));

    checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
  }

  @Test
  public void testRemoveVariableLocalNullTaskId() {
    try {
      taskService.removeVariableLocal(null, "variable");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Deployment(resources = {
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testRemoveVariablesLocal() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task currentTask = taskService.createTaskQuery().singleResult();

    Map<String, Object> varsToDelete = new HashMap<>();
    varsToDelete.put("variable1", "value1");
    varsToDelete.put("variable2", "value2");
    taskService.setVariablesLocal(currentTask.getId(), varsToDelete);
    taskService.setVariableLocal(currentTask.getId(), "variable3", "value3");

    assertEquals("value1", taskService.getVariable(currentTask.getId(), "variable1"));
    assertEquals("value2", taskService.getVariable(currentTask.getId(), "variable2"));
    assertEquals("value3", taskService.getVariable(currentTask.getId(), "variable3"));
    assertEquals("value1", taskService.getVariableLocal(currentTask.getId(), "variable1"));
    assertEquals("value2", taskService.getVariableLocal(currentTask.getId(), "variable2"));
    assertEquals("value3", taskService.getVariableLocal(currentTask.getId(), "variable3"));

    taskService.removeVariables(currentTask.getId(), varsToDelete.keySet());

    assertNull(taskService.getVariable(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariable(currentTask.getId(), "variable2"));
    assertEquals("value3", taskService.getVariable(currentTask.getId(), "variable3"));

    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariableLocal(currentTask.getId(), "variable2"));
    assertEquals("value3", taskService.getVariableLocal(currentTask.getId(), "variable3"));

    checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveVariablesLocalNullTaskId() {
    try {
      taskService.removeVariablesLocal(null, Collections.EMPTY_LIST);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testUserTaskOptimisticLocking() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task1 = taskService.createTaskQuery().singleResult();
    Task task2 = taskService.createTaskQuery().singleResult();

    task1.setDescription("test description one");
    taskService.saveTask(task1);

    try {
      task2.setDescription("test description two");
      taskService.saveTask(task2);

      fail("Expecting exception");
    } catch(OptimisticLockingException e) {
      // Expected exception
    }
  }

  @Test
  public void testDeleteTaskWithDeleteReason() {
    // ACT-900: deleteReason can be manually specified - can only be validated when historyLevel > ACTIVITY
    if (processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      Task task = taskService.newTask();
      task.setName("test task");
      taskService.saveTask(task);

      assertNotNull(task.getId());

      taskService.deleteTask(task.getId(), "deleted for testing purposes");

      HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
        .taskId(task.getId()).singleResult();

      assertNotNull(historicTaskInstance);
      assertEquals("deleted for testing purposes", historicTaskInstance.getDeleteReason());

      // Delete historic task that is left behind, will not be cleaned up because this is not part of a process
      taskService.deleteTask(task.getId(), true);

    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testDeleteTaskPartOfProcess() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    try {
      taskService.deleteTask(task.getId());
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
    }

    try {
      taskService.deleteTask(task.getId(), true);
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
    }

    try {
      taskService.deleteTask(task.getId(), "test");
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
    }

    try {
      taskService.deleteTasks(Arrays.asList(task.getId()));
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
    }

    try {
      taskService.deleteTasks(Arrays.asList(task.getId()), true);
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
    }

    try {
      taskService.deleteTasks(Arrays.asList(task.getId()), "test");
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running process", ae.getMessage());
    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testDeleteTaskPartOfCaseInstance() {
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    try {
      taskService.deleteTask(task.getId());
      fail("Should not be possible to delete task");
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running case instance", ae.getMessage());
    }

    try {
      taskService.deleteTask(task.getId(), true);
      fail("Should not be possible to delete task");
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running case instance", ae.getMessage());
    }

    try {
      taskService.deleteTask(task.getId(), "test");
      fail("Should not be possible to delete task");
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running case instance", ae.getMessage());
    }

    try {
      taskService.deleteTasks(Arrays.asList(task.getId()));
      fail("Should not be possible to delete task");
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running case instance", ae.getMessage());
    }

    try {
      taskService.deleteTasks(Arrays.asList(task.getId()), true);
      fail("Should not be possible to delete task");
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running case instance", ae.getMessage());
    }

    try {
      taskService.deleteTasks(Arrays.asList(task.getId()), "test");
      fail("Should not be possible to delete task");
    } catch(ProcessEngineException ae) {
      assertEquals("The task cannot be deleted because is part of a running case instance", ae.getMessage());
    }

  }

  @Test
  public void testGetTaskCommentByTaskIdAndCommentId() {
    if (processEngineConfiguration.getHistoryLevel().getId() > ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      // create and save new task
      Task task = taskService.newTask();
      taskService.saveTask(task);

      String taskId = task.getId();

      // add comment to task
      Comment comment = taskService.createComment(taskId, null, "look at this \n       isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg kajsh dfuieqpgkja rzvkfnjviuqerhogiuvysbegkjz lkhf ais liasduh flaisduh ajiasudh vaisudhv nsfd");

      // select task comment for task id and comment id
      comment = taskService.getTaskComment(taskId, comment.getId());
      // check returned comment
      assertNotNull(comment.getId());
      assertEquals(taskId, comment.getTaskId());
      assertNull(comment.getProcessInstanceId());
      assertEquals("look at this isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg ...", ((Event)comment).getMessage());
      assertEquals("look at this \n       isn't this great? slkdjf sldkfjs ldkfjs ldkfjs ldkfj sldkfj sldkfj sldkjg laksfg sdfgsd;flgkj ksajdhf skjdfh ksjdhf skjdhf kalskjgh lskh dfialurhg kajsh dfuieqpgkja rzvkfnjviuqerhogiuvysbegkjz lkhf ais liasduh flaisduh ajiasudh vaisudhv nsfd", comment.getFullMessage());
      assertNotNull(comment.getTime());

      // delete task
      taskService.deleteTask(task.getId(), true);
    }
  }

  @Test
  public void testTaskAttachmentByTaskIdAndAttachmentId() throws ParseException {
    Date fixedDate = SDF.parse("01/01/2001 01:01:01.000");
    ClockUtil.setCurrentTime(fixedDate);

    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      // create and save task
      Task task = taskService.newTask();
      taskService.saveTask(task);
      String taskId = task.getId();

      // Fetch the task again and update
      // add attachment
      Attachment attachment = taskService.createAttachment("web page", taskId, "someprocessinstanceid", "weatherforcast", "temperatures and more", "http://weather.com");
      String attachmentId = attachment.getId();

      // get attachment for taskId and attachmentId
      attachment = taskService.getTaskAttachment(taskId, attachmentId);
      assertEquals("weatherforcast", attachment.getName());
      assertEquals("temperatures and more", attachment.getDescription());
      assertEquals("web page", attachment.getType());
      assertEquals(taskId, attachment.getTaskId());
      assertEquals("someprocessinstanceid", attachment.getProcessInstanceId());
      assertEquals("http://weather.com", attachment.getUrl());
      assertNull(taskService.getAttachmentContent(attachment.getId()));
      assertThat(attachment.getCreateTime()).isEqualTo(fixedDate);

      // delete attachment for taskId and attachmentId
      taskService.deleteTaskAttachment(taskId, attachmentId);

      // check if attachment deleted
      assertNull(taskService.getTaskAttachment(taskId, attachmentId));

      taskService.deleteTask(taskId, true);
    }
  }

  @Test
  public void testGetTaskAttachmentContentByTaskIdAndAttachmentId() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      // create and save task
      Task task = taskService.newTask();
      taskService.saveTask(task);
      String taskId = task.getId();

      // Fetch the task again and update
      // add attachment
      Attachment attachment = taskService.createAttachment("web page", taskId, "someprocessinstanceid", "weatherforcast", "temperatures and more", new ByteArrayInputStream("someContent".getBytes()));
      String attachmentId = attachment.getId();

      // get attachment for taskId and attachmentId
      InputStream taskAttachmentContent = taskService.getTaskAttachmentContent(taskId, attachmentId);
      assertNotNull(taskAttachmentContent);

      byte[] byteContent = IoUtil.readInputStream(taskAttachmentContent, "weatherforcast");
      assertEquals("someContent", new String(byteContent));

      taskService.deleteTask(taskId, true);
    }
  }

  @Test
  public void testGetTaskAttachmentWithNullParameters() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      Attachment attachment = taskService.getTaskAttachment(null, null);
      assertNull(attachment);
    }
  }

  @Test
  public void testGetTaskAttachmentContentWithNullParameters() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      InputStream content = taskService.getTaskAttachmentContent(null, null);
      assertNull(content);
    }
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  @Test
  public void testCreateTaskAttachmentWithNullTaskAndProcessInstance() {
    try {
      taskService.createAttachment("web page", null, null, "weatherforcast", "temperatures and more", new ByteArrayInputStream("someContent".getBytes()));
      fail("expected process engine exception");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  @Test
  public void testCreateTaskAttachmentWithNullTaskId() throws ParseException {
    Date fixedDate = SDF.parse("01/01/2001 01:01:01.000");
    ClockUtil.setCurrentTime(fixedDate);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Attachment attachment = taskService.createAttachment("web page", null, processInstance.getId(), "weatherforcast", "temperatures and more", new ByteArrayInputStream("someContent".getBytes()));
    Attachment fetched = taskService.getAttachment(attachment.getId());
    assertThat(fetched).isNotNull();
    assertThat(fetched.getTaskId()).isNull();
    assertThat(fetched.getProcessInstanceId()).isNotNull();
    assertThat(fetched.getCreateTime()).isEqualTo(fixedDate);
    taskService.deleteAttachment(attachment.getId());
  }

  @Test
  public void testDeleteTaskAttachmentWithNullParameter() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      try {
        taskService.deleteAttachment(null);
        fail("expected process engine exception");
      } catch (ProcessEngineException e) {}
    }
  }

  @Test
  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testDeleteAttachment() throws ParseException {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    Attachment attachment = taskService.createAttachment("web page", taskId, null, "weatherforcast", "temperatures and more",
        new ByteArrayInputStream("someContent".getBytes()));
    Attachment fetched = taskService.getAttachment(attachment.getId());
    assertThat(fetched).isNotNull();
    // when
    taskService.deleteAttachment(attachment.getId());
    // then
    fetched = taskService.getAttachment(attachment.getId());
    assertThat(fetched).isNull();
  }

  @Test
  public void testDeleteTaskAttachmentWithNullParameters() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      try {
        taskService.deleteTaskAttachment(null, null);
        fail("expected process engine exception");
      } catch (ProcessEngineException e) {}
    }
  }

  @Test
  public void testDeleteTaskAttachmentWithTaskIdNull() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      try {
        taskService.deleteTaskAttachment(null, "myAttachmentId");
        fail("expected process engine exception");
      } catch(ProcessEngineException e) {}
    }
  }

  @Test
  public void testGetTaskAttachmentsWithTaskIdNull() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(Collections.<Attachment>emptyList(), taskService.getTaskAttachments(null));
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml"})
  @Test
  public void testUpdateVariablesLocal() {
    Map<String, Object> globalVars = new HashMap<>();
    globalVars.put("variable4", "value4");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", globalVars);

    Task currentTask = taskService.createTaskQuery().singleResult();
    Map<String, Object> localVars = new HashMap<>();
    localVars.put("variable1", "value1");
    localVars.put("variable2", "value2");
    localVars.put("variable3", "value3");
    taskService.setVariablesLocal(currentTask.getId(), localVars);

    Map<String, Object> modifications = new HashMap<>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    ((TaskServiceImpl) taskService).updateVariablesLocal(currentTask.getId(), modifications, deletions);

    assertEquals("anotherValue1", taskService.getVariable(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariable(currentTask.getId(), "variable2"));
    assertNull(taskService.getVariable(currentTask.getId(), "variable3"));
    assertEquals("value4", runtimeService.getVariable(processInstance.getId(), "variable4"));
  }

  @Test
  public void testUpdateVariablesLocalForNonExistingTaskId() {
    Map<String, Object> modifications = new HashMap<>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    try {
      ((TaskServiceImpl) taskService).updateVariablesLocal("nonExistingId", modifications, deletions);
      fail("expected process engine exception");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testUpdateVariablesLocaForNullTaskId() {
    Map<String, Object> modifications = new HashMap<>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    try {
      ((TaskServiceImpl) taskService).updateVariablesLocal(null, modifications, deletions);
      fail("expected process engine exception");
    } catch (ProcessEngineException e) {
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml"})
  @Test
  public void testUpdateVariables() {
    Map<String, Object> globalVars = new HashMap<>();
    globalVars.put("variable4", "value4");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", globalVars);

    Task currentTask = taskService.createTaskQuery().singleResult();
    Map<String, Object> localVars = new HashMap<>();
    localVars.put("variable1", "value1");
    localVars.put("variable2", "value2");
    localVars.put("variable3", "value3");
    taskService.setVariablesLocal(currentTask.getId(), localVars);

    Map<String, Object> modifications = new HashMap<>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    ((TaskServiceImpl) taskService).updateVariables(currentTask.getId(), modifications, deletions);

    assertEquals("anotherValue1", taskService.getVariable(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariable(currentTask.getId(), "variable2"));
    assertNull(taskService.getVariable(currentTask.getId(), "variable3"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "variable4"));
  }

  @Test
  public void testUpdateVariablesForNonExistingTaskId() {
    Map<String, Object> modifications = new HashMap<>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    try {
      ((TaskServiceImpl) taskService).updateVariables("nonExistingId", modifications, deletions);
      fail("expected process engine exception");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testUpdateVariablesForNullTaskId() {
    Map<String, Object> modifications = new HashMap<>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    try {
      ((TaskServiceImpl) taskService).updateVariables(null, modifications, deletions);
      fail("expected process engine exception");
    } catch (ProcessEngineException e) {
    }
  }

  @Test
  public void testTaskCaseInstanceId() {
    Task task = taskService.newTask();
    task.setCaseInstanceId("aCaseInstanceId");
    taskService.saveTask(task);

    // Fetch the task again and update
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("aCaseInstanceId", task.getCaseInstanceId());

    task.setCaseInstanceId("anotherCaseInstanceId");
    taskService.saveTask(task);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("anotherCaseInstanceId", task.getCaseInstanceId());

    // Finally, delete task
    taskService.deleteTask(task.getId(), true);

  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testGetVariablesTyped() {
    Map<String, Object> vars = new HashMap<>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    String taskId = taskService.createTaskQuery().singleResult().getId();
    VariableMap variablesTyped = taskService.getVariablesTyped(taskId);
    assertEquals(vars, variablesTyped);
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testGetVariablesTypedDeserialize() {

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables()
          .putValue("broken", Variables.serializedObjectValue("broken")
              .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
              .objectTypeName("unexisting").create()));
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // this works
    VariableMap variablesTyped = taskService.getVariablesTyped(taskId, false);
    assertNotNull(variablesTyped.getValueTyped("broken"));
    variablesTyped = taskService.getVariablesTyped(taskId, Arrays.asList("broken"), false);
    assertNotNull(variablesTyped.getValueTyped("broken"));

    // this does not
    try {
      taskService.getVariablesTyped(taskId);
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Cannot deserialize object", e.getMessage());
    }

    // this does not
    try {
      taskService.getVariablesTyped(taskId, Arrays.asList("broken"), true);
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Cannot deserialize object", e.getMessage());
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testGetVariablesLocalTyped() {
    Map<String, Object> vars = new HashMap<>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setVariablesLocal(taskId, vars);

    VariableMap variablesTyped = taskService.getVariablesLocalTyped(taskId);
    assertEquals(vars, variablesTyped);
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testGetVariablesLocalTypedDeserialize() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setVariablesLocal(taskId, Variables.createVariables()
          .putValue("broken", Variables.serializedObjectValue("broken")
              .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
              .objectTypeName("unexisting").create()));

    // this works
    VariableMap variablesTyped = taskService.getVariablesLocalTyped(taskId, false);
    assertNotNull(variablesTyped.getValueTyped("broken"));
    variablesTyped = taskService.getVariablesLocalTyped(taskId, Arrays.asList("broken"), false);
    assertNotNull(variablesTyped.getValueTyped("broken"));

    // this does not
    try {
      taskService.getVariablesLocalTyped(taskId);
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Cannot deserialize object", e.getMessage());
    }

    // this does not
    try {
      taskService.getVariablesLocalTyped(taskId, Arrays.asList("broken"), true);
    } catch(ProcessEngineException e) {
      testRule.assertTextPresent("Cannot deserialize object", e.getMessage());
    }

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testHumanTaskCompleteWithVariables() {
    // given
    caseService.createCaseInstanceByKey("oneTaskCase");

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();

    String variableName = "aVariable";
    String variableValue = "aValue";

    // when
    taskService.complete(taskId, Variables.createVariables().putValue(variableName, variableValue));

    // then
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();

    assertEquals(variable.getName(), variableName);
    assertEquals(variable.getValue(), variableValue);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testHumanTaskWithLocalVariablesCompleteWithVariable() {
    // given
    caseService.createCaseInstanceByKey("oneTaskCase");

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String variableName = "aVariable";
    String variableValue = "aValue";
    String variableAnotherValue = "anotherValue";

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariableLocal(taskId, variableName, variableValue);

    // when
    taskService.complete(taskId, Variables.createVariables().putValue(variableName, variableAnotherValue));

    // then
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();

    assertEquals(variable.getName(), variableName);
    assertEquals(variable.getValue(), variableAnotherValue);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml"})
  @Test
  public void testUserTaskWithLocalVariablesCompleteWithVariable() {
    // given
    runtimeService.startProcessInstanceByKey("twoTasksProcess");

    String variableName = "aVariable";
    String variableValue = "aValue";
    String variableAnotherValue = "anotherValue";

    String taskId = taskService.createTaskQuery().singleResult().getId();

    taskService.setVariableLocal(taskId, variableName, variableValue);

    // when
    taskService.complete(taskId, Variables.createVariables().putValue(variableName, variableAnotherValue));

    // then
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();

    assertEquals(variable.getName(), variableName);
    assertEquals(variable.getValue(), variableAnotherValue);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testHumanTaskLocalVariables() {
    // given
    String caseInstanceId = caseService.createCaseInstanceByKey("oneTaskCase").getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    String variableName = "aVariable";
    String variableValue = "aValue";

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariableLocal(taskId, variableName, variableValue);

    // then
    VariableInstance variableInstance = runtimeService
      .createVariableInstanceQuery()
      .taskIdIn(taskId)
      .singleResult();
    assertNotNull(variableInstance);

    assertEquals(caseInstanceId, variableInstance.getCaseInstanceId());
    assertEquals(humanTaskId, variableInstance.getCaseExecutionId());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesByEmptyList() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    String taskId = taskService.createTaskQuery()
      .processInstanceId(processInstanceId)
      .singleResult()
      .getId();

    // when
    Map<String, Object> variables = taskService.getVariables(taskId, new ArrayList<String>());

    // then
    assertNotNull(variables);
    assertTrue(variables.isEmpty());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesTypedByEmptyList() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    String taskId = taskService.createTaskQuery()
      .processInstanceId(processInstanceId)
      .singleResult()
      .getId();

    // when
    Map<String, Object> variables = taskService.getVariablesTyped(taskId, new ArrayList<String>(), false);

    // then
    assertNotNull(variables);
    assertTrue(variables.isEmpty());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesLocalByEmptyList() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    String taskId = taskService.createTaskQuery()
      .processInstanceId(processInstanceId)
      .singleResult()
      .getId();

    // when
    Map<String, Object> variables = taskService.getVariablesLocal(taskId, new ArrayList<String>());

    // then
    assertNotNull(variables);
    assertTrue(variables.isEmpty());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesLocalTypedByEmptyList() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    String taskId = taskService.createTaskQuery()
      .processInstanceId(processInstanceId)
      .singleResult()
      .getId();

    // when
    Map<String, Object> variables = taskService.getVariablesLocalTyped(taskId, new ArrayList<String>(), false);

    // then
    assertNotNull(variables);
    assertTrue(variables.isEmpty());
  }

  @Test
  public void testHandleBpmnErrorWithNonexistingTask() {
    // given
    // non-existing task

    // when/then
    assertThatThrownBy(() -> taskService.handleBpmnError("non-existing", ERROR_CODE))
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Cannot find task with id non-existing: task is null");
  }

  @Test
  public void testThrowBpmnErrorWithoutCatch() {
    // given
    BpmnModelInstance model =Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask(USER_TASK_THROW_ERROR)
        .userTask("skipped-error")
        .endEvent()
        .done();
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ERROR, task.getTaskDefinitionKey());

    // when
    taskService.handleBpmnError(task.getId(), ERROR_CODE);

    // then
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(0, processInstances.size());
  }

  @Test
  public void testHandleBpmnErrorWithErrorCodeVariable() {
    // given
    BpmnModelInstance model = createUserTaskProcessWithCatchBoundaryEvent();
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ERROR, task.getTaskDefinitionKey());

    // when
    taskService.handleBpmnError(task.getId(), ERROR_CODE);

    // then
    Task taskAfterThrow = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_AFTER_CATCH, taskAfterThrow.getTaskDefinitionKey());
    VariableInstance errorCodeVariable = runtimeService.createVariableInstanceQuery().variableName("errorCodeVar").singleResult();
    assertEquals(ERROR_CODE, errorCodeVariable.getValue());
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testHandleBpmnErrorWithEmptyErrorCode() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // when/then
    assertThatThrownBy(() -> taskService.handleBpmnError(task.getId(), ""))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("errorCode is empty");
  }

  @Test
  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testHandleBpmnErrorWithNullErrorCode() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // when/then
    assertThatThrownBy(() -> taskService.handleBpmnError(task.getId(), null))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("errorCode is null");
  }

  @Test
  public void testHandleBpmnErrorIncludingMessage() {
    // given
    BpmnModelInstance model = createUserTaskProcessWithCatchBoundaryEvent();
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ERROR, task.getTaskDefinitionKey());
    String errorMessageValue = "Error message for ERROR-" + ERROR_CODE;

    // when
    taskService.handleBpmnError(task.getId(), ERROR_CODE, errorMessageValue);

    // then
    Task taskAfterThrow = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_AFTER_CATCH, taskAfterThrow.getTaskDefinitionKey());
    VariableInstance errorMessageVariable = runtimeService.createVariableInstanceQuery().variableName("errorMessageVar").singleResult();
    assertEquals(errorMessageValue, errorMessageVariable.getValue());
  }

  @Test
  public void testHandleBpmnErrorWithVariables() {
    // given
    BpmnModelInstance model = createUserTaskProcessWithCatchBoundaryEvent();
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ERROR, task.getTaskDefinitionKey());
    String variableName = "foo";
    String variableValue = "bar";

    // when
    taskService.handleBpmnError(task.getId(), ERROR_CODE, null, Variables.createVariables().putValue(variableName, variableValue));

    // then
    Task taskAfterThrow = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_AFTER_CATCH, taskAfterThrow.getTaskDefinitionKey());
    VariableInstance variablePassedDuringThrowError = runtimeService.createVariableInstanceQuery().variableName(variableName).singleResult();
    assertEquals(variableValue, variablePassedDuringThrowError.getValue());
  }

  @Test
  public void testThrowBpmnErrorCatchInEventSubprocess() {
    // given
    String errorCodeVariableName = "errorCodeVar";
    String errorMessageVariableName = "errorMessageVar";
    BpmnModelInstance model = createUserTaskProcessWithEventSubprocess(errorCodeVariableName, errorMessageVariableName);
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ERROR, task.getTaskDefinitionKey());
    String variableName = "foo";
    String variableValue = "bar";
    String errorMessageValue = "Error message for ERROR-" + ERROR_CODE;

    // when
    taskService.handleBpmnError(task.getId(), ERROR_CODE, errorMessageValue, Variables.createVariables().putValue(variableName, variableValue));

    // then
    Task taskAfterThrow = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_AFTER_CATCH, taskAfterThrow.getTaskDefinitionKey());
    VariableInstance variablePassedDuringThrowError = runtimeService.createVariableInstanceQuery().variableName(variableName).singleResult();
    assertEquals(variableValue, variablePassedDuringThrowError.getValue());
    VariableInstance errorMessageVariable = runtimeService.createVariableInstanceQuery().variableName(errorMessageVariableName).singleResult();
    assertEquals(errorMessageValue, errorMessageVariable.getValue());
    VariableInstance errorCodeVariable = runtimeService.createVariableInstanceQuery().variableName(errorCodeVariableName).singleResult();
    assertEquals(ERROR_CODE, errorCodeVariable.getValue());
  }

  @Test
  public void testHandleEscalationWithNonexistingTask() {
    // given
    // non-existing task

    // when/then
    assertThatThrownBy(() -> taskService.handleEscalation("non-existing", ESCALATION_CODE))
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Cannot find task with id non-existing: task is null");
  }

  @Test
  public void testHandleEscalationWithoutEscalationCode() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY).startEvent()
      .userTask(USER_TASK_THROW_ESCALATION).boundaryEvent("catch-escalation").escalation(ESCALATION_CODE)
      .userTask(USER_TASK_AFTER_CATCH).endEvent().moveToActivity(USER_TASK_THROW_ESCALATION)
      .userTask(USER_TASK_AFTER_THROW).endEvent().done();
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ESCALATION, task.getTaskDefinitionKey());

    // when/then
    assertThatThrownBy(() -> taskService.handleEscalation(task.getId(), ""))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("escalationCode is empty");

    assertThatThrownBy(() -> taskService.handleEscalation(task.getId(), null))
      .isInstanceOf(BadUserRequestException.class)
      .hasMessageContaining("escalationCode is null");
  }

  @Test
  public void testThrowEscalationWithoutCatchEvent() {
    // given
    BpmnModelInstance model =Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask(USER_TASK_THROW_ESCALATION)
        .userTask("skipped-error")
        .endEvent()
        .done();
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ESCALATION, task.getTaskDefinitionKey());

    // when/then
    assertThatThrownBy(() -> taskService.handleEscalation(task.getId(), ESCALATION_CODE))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Execution with id '" + task.getTaskDefinitionKey()
      + "' throws an escalation event with escalationCode '" + ESCALATION_CODE
      + "', but no escalation handler was defined.");
  }

  @Test
  public void testHandleEscalationInterruptEventWithVariables() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask(USER_TASK_THROW_ESCALATION)
          .boundaryEvent("catch-escalation")
            .escalation(ESCALATION_CODE)
          .userTask(USER_TASK_AFTER_CATCH)
          .endEvent()
        .moveToActivity(USER_TASK_THROW_ESCALATION)
        .userTask(USER_TASK_AFTER_THROW)
        .endEvent()
        .done();
    testRule.deploy(model);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ESCALATION, task.getTaskDefinitionKey());

    // when
    taskService.handleEscalation(task.getId(), ESCALATION_CODE, Variables.createVariables().putValue("foo", "bar"));

    // then
    Task taskAfterThrow = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_AFTER_CATCH, taskAfterThrow.getTaskDefinitionKey());
    assertEquals("bar",runtimeService.createVariableInstanceQuery().variableName("foo").singleResult().getValue());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/task/TaskServiceTest.handleUserTaskEscalation.bpmn20.xml" })
  public void testHandleEscalationNonInterruptWithVariables() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ESCALATION, task.getTaskDefinitionKey());

    // when
    taskService.handleEscalation(task.getId(), "301", Variables.createVariables().putValue("foo", "bar"));

    // then
    List<Task> list = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, list.size());
    for (Task taskAfterThrow : list) {
      if (!taskAfterThrow.getTaskDefinitionKey().equals(task.getTaskDefinitionKey()) && !taskAfterThrow.getTaskDefinitionKey().equals("after-301")) {
        fail("Two task should be active:" + task.getTaskDefinitionKey() + " & "
                                          + "after-301");
      }
    }
    assertEquals("bar",runtimeService.createVariableInstanceQuery().variableName("foo").singleResult().getValue());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/task/TaskServiceTest.handleUserTaskEscalation.bpmn20.xml" })
  public void testHandleEscalationInterruptWithVariables() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ESCALATION, task.getTaskDefinitionKey());

    // when
    taskService.handleEscalation(task.getId(), "302", Variables.createVariables().putValue("foo", "bar"));

    // then
    Task taskAfterThrow = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("after-302", taskAfterThrow.getTaskDefinitionKey());
    assertEquals("bar",runtimeService.createVariableInstanceQuery().variableName("foo").singleResult().getValue());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/task/TaskServiceTest.handleUserTaskEscalation.bpmn20.xml" })
  public void testHandleEscalationNonInterruptEventSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ESCALATION, task.getTaskDefinitionKey());

    // when
    taskService.handleEscalation(task.getId(), "303");

    // then
    List<Task> list = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, list.size());
    for (Task taskAfterThrow : list) {
      if (!taskAfterThrow.getTaskDefinitionKey().equals(task.getTaskDefinitionKey()) && !taskAfterThrow.getTaskDefinitionKey().equals("after-303")) {
        fail("Two task should be active:" + task.getTaskDefinitionKey() + " & "
                                          + "after-303");
      }
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/task/TaskServiceTest.handleUserTaskEscalation.bpmn20.xml" })
  public void testHandleEscalationInterruptInEventSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ESCALATION, task.getTaskDefinitionKey());

    // when
    taskService.handleEscalation(task.getId(), "304", Variables.createVariables().putValue("foo", "bar"));

    // then
    Task taskAfterThrow = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("after-304", taskAfterThrow.getTaskDefinitionKey());
    assertEquals("bar",runtimeService.createVariableInstanceQuery().variableName("foo").singleResult().getValue());
  }


  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/task/TaskServiceTest.handleUserTaskEscalation.bpmn20.xml" })
  public void testHandleEscalationNonInterruptEmbeddedSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ESCALATION, task.getTaskDefinitionKey());

    // when
    taskService.handleEscalation(task.getId(), "305");

    // then
    List<Task> list = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, list.size());
    for (Task taskAfterThrow : list) {
      if (!taskAfterThrow.getTaskDefinitionKey().equals(task.getTaskDefinitionKey()) && !taskAfterThrow.getTaskDefinitionKey().equals("after-305")) {
        fail("Two task should be active:" + task.getTaskDefinitionKey() + " & "
                                          + "after-305");
      }
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/task/TaskServiceTest.handleUserTaskEscalation.bpmn20.xml" })
  public void testHandleEscalationInterruptInEmbeddedSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(USER_TASK_THROW_ESCALATION, task.getTaskDefinitionKey());

    // when
    taskService.handleEscalation(task.getId(), "306", Variables.createVariables().putValue("foo", "bar"));

    // then
    Task taskAfterThrow = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("after-306", taskAfterThrow.getTaskDefinitionKey());
    assertEquals("bar",runtimeService.createVariableInstanceQuery().variableName("foo").singleResult().getValue());
  }

  protected BpmnModelInstance createUserTaskProcessWithCatchBoundaryEvent() {
    return Bpmn.createExecutableProcess(PROCESS_KEY)
        .startEvent()
        .userTask(USER_TASK_THROW_ERROR)
          .boundaryEvent("catch-error")
            .errorEventDefinition()
              .error(ERROR_CODE)
              .errorCodeVariable("errorCodeVar")
              .errorMessageVariable("errorMessageVar")
            .errorEventDefinitionDone()
          .userTask(USER_TASK_AFTER_CATCH)
          .endEvent()
        .moveToActivity(USER_TASK_THROW_ERROR)
        .userTask(USER_TASK_AFTER_THROW)
        .endEvent()
        .done();
  }

  protected BpmnModelInstance createUserTaskProcessWithEventSubprocess(
      String errorCodeVariable, String errorMessageVariableName) {
    ProcessBuilder processBuilder = Bpmn.createExecutableProcess(PROCESS_KEY);
    BpmnModelInstance model = processBuilder
        .startEvent()
        .userTask(USER_TASK_THROW_ERROR)
        .userTask(USER_TASK_AFTER_THROW)
        .endEvent()
        .done();
    processBuilder.eventSubProcess()
       .startEvent("catch-error")
         .errorEventDefinition()
           .error(ERROR_CODE)
           .errorCodeVariable(errorCodeVariable)
           .errorMessageVariable(errorMessageVariableName)
         .errorEventDefinitionDone()
       .userTask(USER_TASK_AFTER_CATCH)
       .endEvent();
    return model;
  }

}
