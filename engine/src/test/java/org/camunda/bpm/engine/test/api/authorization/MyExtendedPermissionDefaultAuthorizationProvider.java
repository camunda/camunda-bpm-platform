package org.camunda.bpm.engine.test.api.authorization;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.impl.cfg.auth.DefaultAuthorizationProvider;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Johannes Heinemann
 */
public class MyExtendedPermissionDefaultAuthorizationProvider extends DefaultAuthorizationProvider{

  public AuthorizationEntity[] newTaskAssignee(Task task, String oldAssignee, String newAssignee) {
    AuthorizationEntity[] authorizations = super.newTaskAssignee(task, oldAssignee, newAssignee);
    authorizations[0].addPermission(Permissions.DELETE_HISTORY);
    return authorizations;
  }
}
