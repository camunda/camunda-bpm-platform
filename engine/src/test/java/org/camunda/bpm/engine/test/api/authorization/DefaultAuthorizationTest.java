package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Permissions.TASK_WORK;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import java.util.Arrays;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import junit.framework.AssertionFailedError;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class DefaultAuthorizationTest extends AuthorizationTest {
  
  protected String userId2 = "demo";
  protected User user2;
  
  protected String groupId2 = "accounting2";
  protected Group group2;
  
  @Override
  public void setUp() {
    processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("camunda.cfg.xml");
    user = createUser(userId);
    user2 = createUser(userId2);
    
    group = createGroup(groupId);
    group2 = createGroup(groupId2);
    
    processEngine.getIdentityService().createMembership(userId, groupId);
    processEngine.getIdentityService().createMembership(userId2, groupId2);
    
    processEngine.getIdentityService().setAuthentication(userId, Arrays.asList(groupId));
    processEngineConfiguration.setAuthorizationEnabled(true);
  }
  
  @Override
  public void tearDown() {
    processEngineConfiguration.setAuthorizationEnabled(false);
    if (processEngine != null) {
      for (User user : processEngine.getIdentityService().createUserQuery().list()) {
        processEngine.getIdentityService().deleteUser(user.getId());
      }
      for (Group group : processEngine.getIdentityService().createGroupQuery().list()) {
        processEngine.getIdentityService().deleteGroup(group.getId());
      }
      for (Authorization authorization : processEngine.getAuthorizationService().createAuthorizationQuery().list()) {
        processEngine.getAuthorizationService().deleteAuthorization(authorization.getId());
      }
      processEngine.close();
    }
  }
  
  // defaultTaskPermissionForUser configuration test cases 
  public void testShouldCheckUpdatePermissionForTaskAssignee() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(UPDATE.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    // then
    assertEquals(UPDATE, processEngineConfiguration.getDefaultUserPermissionForTask());
  }
  
  public void testShouldCheckDefaultPermissionForTaskAssigneeAsTaskWork() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    // then
    assertEquals(TASK_WORK, processEngineConfiguration.getDefaultUserPermissionForTask());
  }
  
  public void testShouldCheckInvalidTaskPermissionForUser() {
    processEngineConfiguration.setDefaultTaskPermissionForUser("invalidPermission");
    
    try {
      processEngine = processEngineConfiguration.buildProcessEngine();
      fail("Error expected: DefaultTaskAssigneePermission should be either TASK_WORK or UPDATE");
    } catch (Exception ex) {
      assertTextPresent("Permission 'invalidPermission' is invalid", ex.getMessage());
    }
  }
  
  public void testShouldCheckNullTaskPermissionForUser() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(null);
    
    try {
      processEngine = processEngineConfiguration.buildProcessEngine();
      fail("DefaultTaskAssigneePermission should be either TASK_WORK or UPDATE");
    } catch (Exception ex) {
      assertTextPresent("Default task assignee permission is null", ex.getMessage());
    }
  }
  
  // default authorization for task assignee as Task work (Should fail task actions)
  public void testShouldCheckDefaultTaskAssigneePermissionAsTaskWork() {
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    enableAuthorization();
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().setAssignee(taskId, "demo");
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(groupId));
    
    try {
      processEngine.getTaskService().setAssignee(taskId, "demo2");
      fail("Exception expected: It should not be possible to set assignee.");
    } catch (Exception exception) {
      assertTextPresent("The user with id 'demo' does not have one of the following permissions: 'TASK_ASSIGN'", exception.getMessage());
    }
    
    deleteTask(taskId, true);
  }
  
  public void testShouldCheckDefaultTaskPermissionAsTaskWorkForCandidateUser() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    processEngineConfiguration.setAuthorizationEnabled(true);
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().addCandidateUser(taskId, userId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, Arrays.asList(groupId2));
    
    try {
      processEngine.getTaskService().delegateTask(taskId, "demo2");
      fail("Exception expected: It should not be possible for the candidate user to delegate a task.");
    } catch (Exception exception) {
      assertTextPresent("The user with id 'demo' does not have one of the following permissions: 'TASK_ASSIGN'", exception.getMessage());
    }
    
    deleteTask(taskId, true);
  }
  
  public void testShouldCheckDefaultTaskPermissionAsTaskWorkForCandidateGroup() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    processEngine.getProcessEngineConfiguration().setAuthorizationEnabled(true);
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().addCandidateGroup(taskId, groupId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, Arrays.asList(groupId2));
    
    try {
      processEngine.getTaskService().addCandidateGroup(taskId, "demo2");
      fail("Exception expected: It should not be possible for the user (part of candidate group) to add another candidate group.");
    } catch (Exception exception) {
      assertTextPresent("The user with id 'demo' does not have one of the following permissions: 'TASK_ASSIGN'", exception.getMessage());
    }
    
    deleteTask(taskId, true);
  }
  
  public void testShouldCheckDefaultTaskPermissionAsTaskWorkForOwner() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    processEngine.getProcessEngineConfiguration().setAuthorizationEnabled(true);
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().setOwner(taskId, "demo");
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(groupId));
    
    try {
      processEngine.getTaskService().delegateTask(taskId, "demo2");
      fail("Exception expected: It should not be possible for the owner to delegate the task.");
    } catch (Exception exception) {
      assertTextPresent("The user with id 'demo' does not have one of the following permissions: 'TASK_ASSIGN'", exception.getMessage());
    }
    
    deleteTask(taskId, true);
  }
  
  // default task assignee permission as task work (should pass task actions)
  public void testUserTaskCompletionWithDefaultTaskPermissionAsTaskWork() {
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    enableAuthorization();
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().setAssignee(taskId, "demo");
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(groupId));
    
    // then
    processEngine.getTaskService().complete(taskId);
    
    deleteTask(taskId, true);
  }
  
  public void testUserTaskCompletionWithDefaultTaskPermissionAsTaskWorkForCandidateUser() {
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    enableAuthorization();
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().addCandidateUser(taskId, userId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, Arrays.asList(groupId));
    
    // then
    processEngine.getTaskService().claim(taskId, userId2);
    processEngine.getTaskService().complete(taskId);
    
    deleteTask(taskId, true);
  }
  
  public void testUserTaskCompletionWithDefaultTaskPermissionAsTaskWorkForCandidateGroup() {
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    enableAuthorization();
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().addCandidateGroup(taskId, groupId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, Arrays.asList(groupId2));
    
    // then
    processEngine.getTaskService().claim(taskId, userId2);
    processEngine.getTaskService().complete(taskId);
    
    deleteTask(taskId, true);
  }
  
  public void testUserTaskCompletionWithDefaultTaskPermissionAsTaskWorkForOwner() {
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    enableAuthorization();
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().setOwner(taskId, userId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, Arrays.asList(groupId2));
    
    // then
    processEngine.getTaskService().claim(taskId, userId2);
    processEngine.getTaskService().complete(taskId);
    
    deleteTask(taskId, true);
  }
  
  // default task assignee permission - UPDATE
  public void testShouldCheckDefaultTaskPermissionAsUpdateForAssignee() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(UPDATE.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    processEngine.getProcessEngineConfiguration().setAuthorizationEnabled(true);
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().setAssignee(taskId, userId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, Arrays.asList(groupId));
    
    processEngine.getTaskService().delegateTask(taskId, "demo2");
    processEngine.getTaskService().complete(taskId);
    
    deleteTask(taskId, true);
  }
  
  public void testShouldCheckDefaultTaskPermissionAsUpdateForCandidateUser() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(UPDATE.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    processEngine.getProcessEngineConfiguration().setAuthorizationEnabled(true);
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().addCandidateUser(taskId, "demo");
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(groupId));
    
    processEngine.getTaskService().delegateTask(taskId, "demo2");
    
    deleteTask(taskId, true);
  }
  
  public void testShouldCheckDefaultTaskPermissionAsUpdateForCandidateGroup() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(UPDATE.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    processEngine.getProcessEngineConfiguration().setAuthorizationEnabled(true);
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().addCandidateGroup(taskId, groupId);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(groupId));
    
    processEngine.getTaskService().delegateTask(taskId, "demo2");
    
    deleteTask(taskId, true);
  }
  
  public void testShouldCheckDefaultTaskPermissionAsUpdateForOwner() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(UPDATE.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    processEngineConfiguration.setAuthorizationEnabled(true);
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().setOwner(taskId, "demo");
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication("demo", Arrays.asList(groupId));
    
    processEngine.getTaskService().setOwner(taskId, "demo2");
    
    deleteTask(taskId, true);
  }
  
  public void assertTextPresent(String expected, String actual) {
    if ((actual == null) || (actual.indexOf(expected) == -1)) {
      throw new AssertionFailedError("expected presence of [" + expected + "], but was [" + actual + "]");
    }
  }
  
  public void assertTextPresentIgnoreCase(String expected, String actual) {
    assertTextPresent(expected.toLowerCase(), actual.toLowerCase());
  }
}
