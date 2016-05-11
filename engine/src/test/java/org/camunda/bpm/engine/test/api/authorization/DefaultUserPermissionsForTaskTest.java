package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Permissions.TASK_WORK;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import java.util.Arrays;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;

/**
 *
 * @author Deivarayan Azhagappan
 *
 */
public class DefaultUserPermissionsForTaskTest extends AuthorizationTest {

  protected String userId2 = "demo";
  protected User user2;

  protected String groupId2 = "accounting2";
  protected Group group2;

  protected String defaultTaskPermissionValue;

  @Override
  public void tearDown() {
    // reset default permission
    processEngineConfiguration.setDefaultUserPermissionForTask(UPDATE);
    super.tearDown();
  }

  public void testShouldGrantTaskWorkOnAssign() {

    // given
    processEngineConfiguration.setDefaultUserPermissionForTask(TASK_WORK);

    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    processEngine.getTaskService().setAssignee(taskId, userId2);

    // then
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.TASK_WORK, Resources.TASK, taskId));
    assertEquals(false, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));

    deleteTask(taskId, true);
  }

  public void testShouldGrantUpdateOnAssign() {

    // given
    processEngineConfiguration.setDefaultUserPermissionForTask(UPDATE);

    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    processEngine.getTaskService().setAssignee(taskId, userId2);

    // then
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));

    deleteTask(taskId, true);
  }

  public void testShouldGrantTaskWorkOnSetCandidateUser() {

    // given
    processEngineConfiguration.setDefaultUserPermissionForTask(TASK_WORK);

    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    processEngine.getTaskService().addCandidateUser(taskId, userId2);

    // then
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.TASK_WORK, Resources.TASK, taskId));
    assertEquals(false, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));

    deleteTask(taskId, true);
  }

  public void testShouldGrantUpdateOnSetCandidateUser() {

    // given
    processEngineConfiguration.setDefaultUserPermissionForTask(UPDATE);

    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    processEngine.getTaskService().addCandidateUser(taskId, userId2);

    // then
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));

    deleteTask(taskId, true);
  }

  public void testShouldGrantTaskWorkOnSetOwner() {

    // given
    processEngineConfiguration.setDefaultUserPermissionForTask(TASK_WORK);

    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    processEngine.getTaskService().setOwner(taskId, userId2);

    // then
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.TASK_WORK, Resources.TASK, taskId));
    assertEquals(false, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));

    deleteTask(taskId, true);
  }

  public void testShouldGrantUpdateOnSetOwner() {

    // given
    processEngineConfiguration.setDefaultUserPermissionForTask(UPDATE);

    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    processEngine.getTaskService().setOwner(taskId, userId2);

    // then
    assertEquals(true,authorizationService.isUserAuthorized(userId2, null, Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, null,Permissions.UPDATE, Resources.TASK, taskId));

    deleteTask(taskId, true);
  }


  public void testShouldGrantTaskWorkOnSetCandidateGroup() {

    // given
    processEngineConfiguration.setDefaultUserPermissionForTask(TASK_WORK);

    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    processEngine.getTaskService().addCandidateGroup(taskId, groupId);

    // then
    assertEquals(true,authorizationService.isUserAuthorized(userId2, Arrays.asList(groupId), Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, Arrays.asList(groupId),Permissions.TASK_WORK, Resources.TASK, taskId));
    assertEquals(false, authorizationService.isUserAuthorized(userId2, Arrays.asList(groupId),Permissions.UPDATE, Resources.TASK, taskId));

    deleteTask(taskId, true);
  }

  public void testShouldGrantUpdateOnSetCandidateGroup() {

    // given
    processEngineConfiguration.setDefaultUserPermissionForTask(UPDATE);

    String taskId = "myTask";
    createTask(taskId);
    createGrantAuthorization(TASK, taskId, userId, UPDATE);

    // when
    processEngine.getTaskService().addCandidateGroup(taskId, groupId);

    // then
    assertEquals(true,authorizationService.isUserAuthorized(userId2, Arrays.asList(groupId), Permissions.READ, Resources.TASK, taskId));
    assertEquals(true, authorizationService.isUserAuthorized(userId2, Arrays.asList(groupId),Permissions.UPDATE, Resources.TASK, taskId));

    deleteTask(taskId, true);
  }

}
