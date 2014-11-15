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

package org.camunda.bpm.engine.test.api.task;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskAlreadyClaimedException;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Event;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 * @author Falko Menge
 */
public class TaskServiceTest extends PluggableProcessEngineTestCase {

  protected static final String TWO_TASKS_PROCESS = "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml";

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

      Set<String> expectedComments = new HashSet<String>();
      expectedComments.add("one");
      expectedComments.add("two");

      Set<String> comments = new HashSet<String>();
      for (Comment cmt: taskService.getProcessInstanceComments("pid")) {
        comments.add(cmt.getFullMessage());
      }

      assertEquals(expectedComments, comments);

      // Finally, delete task
      taskService.deleteTask(taskId, true);
    }
  }

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

  public void testSaveTaskNullTask() {
    try {
      taskService.saveTask(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("task is null", ae.getMessage());
    }
  }

  public void testDeleteTaskNullTaskId() {
    try {
      taskService.deleteTask(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Expected exception
    }
  }

  public void testDeleteTaskUnexistingTaskId() {
    // Deleting unexisting task should be silently ignored
    taskService.deleteTask("unexistingtaskid");
  }

  public void testDeleteTasksNullTaskIds() {
    try {
      taskService.deleteTasks(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Expected exception
    }
  }

  public void testDeleteTasksTaskIdsUnexistingTaskId() {

    Task existingTask = taskService.newTask();
    taskService.saveTask(existingTask);

    // The unexisting taskId's should be silently ignored. Existing task should
    // have been deleted.
    taskService.deleteTasks(Arrays.asList("unexistingtaskid1", existingTask.getId()), true);

    existingTask = taskService.createTaskQuery().taskId(existingTask.getId()).singleResult();
    assertNull(existingTask);
  }

  public void testClaimNullArguments() {
    try {
      taskService.claim(null, "userid");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testClaimUnexistingTaskId() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.claim("unexistingtaskid", user.getId());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingtaskid", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

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
      assertTextPresent("Task '" + task.getId() + "' is already claimed by someone else.", ae.getMessage());
    }

    taskService.deleteTask(task.getId(), true);
    identityService.deleteUser(user.getId());
    identityService.deleteUser(secondUser.getId());
  }

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

  public void testCompleteTaskNullTaskId() {
    try {
      taskService.complete(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testCompleteTaskUnexistingTaskId() {
    try {
      taskService.complete("unexistingtask");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }

  public void testCompleteTaskWithParametersNullTaskId() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("myKey", "myValue");

    try {
      taskService.complete(null, variables);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testCompleteTaskWithParametersUnexistingTaskId() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("myKey", "myValue");

    try {
      taskService.complete("unexistingtask", variables);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }

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
  public void testCompleteWithParametersTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

    // Fetch first task
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("First task", task.getName());

    // Complete first task
    Map<String, Object> taskParams = new HashMap<String, Object>();
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

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
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

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

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

  public void testResolveTaskNullTaskId() {
    try {
      taskService.resolveTask(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testResolveTaskUnexistingTaskId() {
    try {
      taskService.resolveTask("unexistingtask");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }

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
  public void testResolveWithParametersTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

    // Fetch first task
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("First task", task.getName());

    task.delegate("johndoe");

    // Resolve first task
    Map<String, Object> taskParams = new HashMap<String, Object>();
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

  public void testSetAssigneeNullTaskId() {
    try {
      taskService.setAssignee(null, "userId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testSetAssigneeUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.setAssignee("unexistingTaskId", user.getId());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

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

  public void testAddCandidateUserNullTaskId() {
    try {
      taskService.addCandidateUser(null, "userId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testAddCandidateUserNullUserId() {
    try {
      taskService.addCandidateUser("taskId", null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }

  public void testAddCandidateUserUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.addCandidateUser("unexistingTaskId", user.getId());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

  public void testAddCandidateGroupNullTaskId() {
    try {
      taskService.addCandidateGroup(null, "groupId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testAddCandidateGroupNullGroupId() {
    try {
      taskService.addCandidateGroup("taskId", null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }

  public void testAddCandidateGroupUnexistingTask() {
    Group group = identityService.newGroup("group");
    identityService.saveGroup(group);
    try {
      taskService.addCandidateGroup("unexistingTaskId", group.getId());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }
    identityService.deleteGroup(group.getId());
  }

  public void testAddGroupIdentityLinkNullTaskId() {
    try {
      taskService.addGroupIdentityLink(null, "groupId", IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testAddGroupIdentityLinkNullUserId() {
    try {
      taskService.addGroupIdentityLink("taskId", null, IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }

  public void testAddGroupIdentityLinkUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.addGroupIdentityLink("unexistingTaskId", user.getId(), IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

  public void testAddUserIdentityLinkNullTaskId() {
    try {
      taskService.addUserIdentityLink(null, "userId", IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testAddUserIdentityLinkNullUserId() {
    try {
      taskService.addUserIdentityLink("taskId", null, IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }

  public void testAddUserIdentityLinkUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.addUserIdentityLink("unexistingTaskId", user.getId(), IdentityLinkType.CANDIDATE);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

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

  public void testSetPriority() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    taskService.setPriority(task.getId(), 12345);

    // Fetch task again to check if the priority is set
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals(12345, task.getPriority());

    taskService.deleteTask(task.getId(), true);
  }

  public void testSetPriorityUnexistingTaskId() {
    try {
      taskService.setPriority("unexistingtask", 12345);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }

  public void testSetPriorityNullTaskId() {
    try {
      taskService.setPriority(null, 12345);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  /**
   * @see http://jira.codehaus.org/browse/ACT-1059
   */
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

  public void testRemoveVariableNullTaskId() {
    try {
      taskService.removeVariable(null, "variable");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Deployment(resources = {
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testRemoveVariables() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task currentTask = taskService.createTaskQuery().singleResult();

    Map<String, Object> varsToDelete = new HashMap<String, Object>();
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
  public void testRemoveVariablesNullTaskId() {
    try {
      taskService.removeVariables(null, Collections.EMPTY_LIST);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Deployment(resources = {
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
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

  public void testRemoveVariableLocalNullTaskId() {
    try {
      taskService.removeVariableLocal(null, "variable");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Deployment(resources = {
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testRemoveVariablesLocal() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task currentTask = taskService.createTaskQuery().singleResult();

    Map<String, Object> varsToDelete = new HashMap<String, Object>();
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
  public void testRemoveVariablesLocalNullTaskId() {
    try {
      taskService.removeVariablesLocal(null, Collections.EMPTY_LIST);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
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

    caseService
      .withCaseExecution(caseExecutionId)
      .manualStart();

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

  public void testTaskAttachmentByTaskIdAndAttachmentId() {
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

      // delete attachment for taskId and attachmentId
      taskService.deleteTaskAttachment(taskId, attachmentId);

      // check if attachment deleted
      assertNull(taskService.getTaskAttachment(taskId, attachmentId));

      taskService.deleteTask(taskId, true);
    }
  }

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

  public void testGetTaskAttachmentWithNullParameters() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      Attachment attachment = taskService.getTaskAttachment(null, null);
      assertNull(attachment);
    }
  }

  public void testGetTaskAttachmentContentWithNullParameters() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      InputStream content = taskService.getTaskAttachmentContent(null, null);
      assertNull(content);
    }
  }

  public void testCreateTaskAttachmentWithNullTaskId() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      try {
        taskService.createAttachment("web page", null, "someprocessinstanceid", "weatherforcast", "temperatures and more", new ByteArrayInputStream("someContent".getBytes()));
        fail("expected process engine exception");
      } catch (ProcessEngineException e) {}
    }
  }

  public void testDeleteTaskAttachmentWithNullParameters() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      try {
        taskService.deleteTaskAttachment(null, null);
        fail("expected process engine exception");
      } catch (ProcessEngineException e) {}
    }
  }

  public void testDeleteTaskAttachmentWithTaskIdNull() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      try {
        taskService.deleteTaskAttachment(null, "myAttachmentId");
        fail("expected process engine exception");
      } catch(ProcessEngineException e) {}
    }
  }

  public void testGetTaskAttachmentsWithTaskIdNull() {
    int historyLevel = processEngineConfiguration.getHistoryLevel().getId();
    if (historyLevel> ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      assertEquals(Collections.<Attachment>emptyList(), taskService.getTaskAttachments(null));
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml"})
  public void testUpdateVariablesLocal() {
    Map<String, Object> globalVars = new HashMap<String, Object>();
    globalVars.put("variable4", "value4");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", globalVars);

    Task currentTask = taskService.createTaskQuery().singleResult();
    Map<String, Object> localVars = new HashMap<String, Object>();
    localVars.put("variable1", "value1");
    localVars.put("variable2", "value2");
    localVars.put("variable3", "value3");
    taskService.setVariablesLocal(currentTask.getId(), localVars);

    Map<String, Object> modifications = new HashMap<String, Object>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<String>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    ((TaskServiceImpl) taskService).updateVariablesLocal(currentTask.getId(), modifications, deletions);

    assertEquals("anotherValue1", taskService.getVariable(currentTask.getId(), "variable1"));
    assertNull(taskService.getVariable(currentTask.getId(), "variable2"));
    assertNull(taskService.getVariable(currentTask.getId(), "variable3"));
    assertEquals("value4", runtimeService.getVariable(processInstance.getId(), "variable4"));
  }

  public void testUpdateVariablesLocalForNonExistingTaskId() {
    Map<String, Object> modifications = new HashMap<String, Object>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<String>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    try {
      ((TaskServiceImpl) taskService).updateVariablesLocal("nonExistingId", modifications, deletions);
      fail("expected process engine exception");
    } catch (ProcessEngineException e) {
    }
  }

  public void testUpdateVariablesLocaForNullTaskId() {
    Map<String, Object> modifications = new HashMap<String, Object>();
    modifications.put("variable1", "anotherValue1");
    modifications.put("variable2", "anotherValue2");

    List<String> deletions = new ArrayList<String>();
    deletions.add("variable2");
    deletions.add("variable3");
    deletions.add("variable4");

    try {
      ((TaskServiceImpl) taskService).updateVariablesLocal(null, modifications, deletions);
      fail("expected process engine exception");
    } catch (ProcessEngineException e) {
    }
  }

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
  public void testGetVariablesTyped() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");

    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    String taskId = taskService.createTaskQuery().singleResult().getId();
    VariableMap variablesTyped = taskService.getVariablesTyped(taskId);
    assertEquals(vars, variablesTyped);
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesTypedDeserialize() {

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Variables.createVariables()
          .putValue("broken", Variables.serializedObjectValue("broken")
              .serializationDataFormat(JavaObjectSerializer.SERIALIZATION_DATA_FORMAT)
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
      assertTextPresent("Cannot deserialize object", e.getMessage());
    }

    // this does not
    try {
      taskService.getVariablesTyped(taskId, Arrays.asList("broken"), true);
    } catch(ProcessEngineException e) {
      assertTextPresent("Cannot deserialize object", e.getMessage());
    }
  }

  @Deployment(resources={
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariablesLocalTyped() {
    Map<String, Object> vars = new HashMap<String, Object>();
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
  public void testGetVariablesLocalTypedDeserialize() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.setVariablesLocal(taskId, Variables.createVariables()
          .putValue("broken", Variables.serializedObjectValue("broken")
              .serializationDataFormat(JavaObjectSerializer.SERIALIZATION_DATA_FORMAT)
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
      assertTextPresent("Cannot deserialize object", e.getMessage());
    }

    // this does not
    try {
      taskService.getVariablesLocalTyped(taskId, Arrays.asList("broken"), true);
    } catch(ProcessEngineException e) {
      assertTextPresent("Cannot deserialize object", e.getMessage());
    }

  }

}
