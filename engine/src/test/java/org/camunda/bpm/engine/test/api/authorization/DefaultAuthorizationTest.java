package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.TASK_WORK;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import java.util.Arrays;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;

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
  
  protected String defaultTaskPermissionValue;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    defaultTaskPermissionValue = processEngineConfiguration.getDefaultTaskPermissionForUser();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    processEngineConfiguration.setDefaultTaskPermissionForUser(defaultTaskPermissionValue);
    
  }
  
  // defaultTaskPermissionForUser configuration test cases 
  public void testShouldCheckDefaultTaskPermissionasUpdateForUser() {
    
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

 public void testShouldCheckDefaultTaskPermissionForUserWithRandomPermission() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(READ.getName());
   
    try {
      processEngine = processEngineConfiguration.buildProcessEngine();
      fail("Error expected: DefaultTaskAssigneePermission should be either TASK_WORK or UPDATE");
    } catch (Exception ex) {
      assertTextPresent("defaultTaskPermissionForUser is neither UPDATE nor TASK_WORK", ex.getMessage());
    }
  }

  public void testShouldCheckDefaultTaskPermissionForUserWithInvalidPermission() {
    processEngineConfiguration.setDefaultTaskPermissionForUser("invalidPermission");
    
    try {
      processEngine = processEngineConfiguration.buildProcessEngine();
      fail("Error expected: DefaultTaskAssigneePermission should be either TASK_WORK or UPDATE");
    } catch (Exception ex) {
      assertTextPresent("Permission 'invalidPermission' is invalid", ex.getMessage());
    }
  }
  
  public void testShouldCheckDefaultTaskPermissionForUserWithNullPermission() {
    
    processEngineConfiguration.setDefaultTaskPermissionForUser(null);
    
    try {
      processEngine = processEngineConfiguration.buildProcessEngine();
      fail("DefaultTaskAssigneePermission should be either TASK_WORK or UPDATE");
    } catch (Exception ex) {
      assertTextPresent("Default task assignee permission is null", ex.getMessage());
    }
  }
  
  // default authorization for user as Task work
  public void testShouldCheckDefaultTaskAssigneePermissionAsTaskWork() {
    processEngineConfiguration.setDefaultTaskPermissionForUser(TASK_WORK.getName());
    processEngine = processEngineConfiguration.buildProcessEngine();
    
    String taskId = "myTask";
    createTask(taskId);
    
    enableAuthorization();
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().setAssignee(taskId, userId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, null);
    
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.TASK_WORK, Resources.TASK, taskId));

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
    processEngine.getIdentityService().setAuthentication(userId2, null);
    
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.TASK_WORK, Resources.TASK, taskId));
    
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
    
    assertEquals(true,authorizationService.isUserAuthorized(userId2, Arrays.asList(groupId2), Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, Arrays.asList(groupId2),Permissions.TASK_WORK, Resources.TASK, taskId));
    
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
    processEngine.getTaskService().setOwner(taskId, userId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, null);
    
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.TASK_WORK, Resources.TASK, taskId));
    
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
    processEngine.getIdentityService().setAuthentication(userId2, null);
    
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));
    
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
    processEngine.getTaskService().addCandidateUser(taskId, userId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, null);
    
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));
    
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
    processEngine.getIdentityService().setAuthentication(userId2, Arrays.asList(groupId));
    
    assertEquals(true,authorizationService.isUserAuthorized(userId2, Arrays.asList(groupId), Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, Arrays.asList(groupId),Permissions.UPDATE, Resources.TASK, taskId));
    
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
    processEngine.getTaskService().setOwner(taskId, userId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, null);
    
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));
    
    deleteTask(taskId, true);
  }

  // check default permission is UPDATE without configuration
  public void testShouldCheckDefaultTaskPermissionAsUpdate() {

    processEngine = processEngineConfiguration.buildProcessEngine();

    String taskId = "myTask";
    createTask(taskId);
    
    processEngine.getProcessEngineConfiguration().setAuthorizationEnabled(true);
    
    createGrantAuthorization(TASK, taskId, userId, UPDATE);
    
    // when
    processEngine.getTaskService().setAssignee(taskId, userId2);
    
    // Change context to user demo
    processEngine.getIdentityService().setAuthentication(userId2, null);
    
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));
    
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
