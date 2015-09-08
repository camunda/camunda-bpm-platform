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
package org.camunda.bpm.engine.impl.cfg.auth;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.camunda.bpm.engine.authorization.Resources.FILTER;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 * <p>Provides the default authorizations for camunda BPM.</p>
 *
 * @author Daniel Meyer
 *
 */
public class DefaultAuthorizationProvider implements ResourceAuthorizationProvider {

  public AuthorizationEntity[] newUser(User user) {
    // create an authorization which gives the user all permissions on himself:
    String userId = user.getId();
    AuthorizationEntity resourceOwnerAuthorization = createGrantAuthorization(userId, null, USER, userId, ALL);

    return new AuthorizationEntity[]{ resourceOwnerAuthorization };
  }

  public AuthorizationEntity[] newGroup(Group group) {
    List<AuthorizationEntity> authorizations = new ArrayList<AuthorizationEntity>();

    // whenever a new group is created, all users part of the
    // group are granted READ permissions on the group
    String groupId = group.getId();
    AuthorizationEntity groupMemberAuthorization = createGrantAuthorization(null, groupId, GROUP, groupId, READ);
    authorizations.add(groupMemberAuthorization);

    return authorizations.toArray(new AuthorizationEntity[0]);
  }

  public AuthorizationEntity[] groupMembershipCreated(String groupId, String userId) {

    // no default authorizations on memberships.

    return null;
  }

  public AuthorizationEntity[] newFilter(Filter filter) {

    String owner = filter.getOwner();
    if(owner != null) {
      // create an authorization which gives the owner of the filter all permissions on the filter
      String filterId = filter.getId();
      AuthorizationEntity filterOwnerAuthorization = createGrantAuthorization(owner, null, FILTER, filterId, ALL);

      return new AuthorizationEntity[]{ filterOwnerAuthorization };

    } else {
      return null;

    }
  }

  // Deployment ///////////////////////////////////////////////

  public AuthorizationEntity[] newDeployment(Deployment deployment) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    Authentication currentAuthentication = identityService.getCurrentAuthentication();

    if (currentAuthentication != null && currentAuthentication.getUserId() != null) {
      String userId = currentAuthentication.getUserId();
      String deploymentId = deployment.getId();
      AuthorizationEntity authorization = createGrantAuthorization(userId, null, DEPLOYMENT, deploymentId, READ, DELETE);
      return new AuthorizationEntity[]{ authorization };
    }

    return null;
  }

  // Process Definition //////////////////////////////////////

  public AuthorizationEntity[] newProcessDefinition(ProcessDefinition processDefinition) {
    // no default authorizations on process definitions.
    return null;
  }

  // Process Instance ///////////////////////////////////////

  public AuthorizationEntity[] newProcessInstance(ProcessInstance processInstance) {
    // no default authorizations on process instances.
    return null;
  }

  // Task /////////////////////////////////////////////////

  public AuthorizationEntity[] newTask(Task task) {
    // no default authorizations on tasks.
    return null;
  }

  public AuthorizationEntity[] newTaskAssignee(Task task, String oldAssignee, String newAssignee) {
    if (newAssignee != null) {

      // create (or update) an authorization for the new assignee.

      String taskId = task.getId();

      // fetch existing authorization
      AuthorizationEntity authorization = getGrantAuthorizationByUserId(newAssignee, TASK, taskId);

      // update authorization:
      // (1) fetched authorization == null -> create a new authorization (with READ/UPDATE permission)
      // (2) fetched authorization != null -> add READ and UPDATE permission
      authorization = updateAuthorization(authorization, newAssignee, null, TASK, taskId, READ, UPDATE);

      // return always created or updated authorization
      return new AuthorizationEntity[]{ authorization };
    }

    return null;
  }

  public AuthorizationEntity[] newTaskOwner(Task task, String oldOwner, String newOwner) {
    if (newOwner != null) {

      // create (or update) an authorization for the new owner.

      String taskId = task.getId();

      // fetch existing authorization
      AuthorizationEntity authorization = getGrantAuthorizationByUserId(newOwner, TASK, taskId);

      // update authorization:
      // (1) fetched authorization == null -> create a new authorization (with READ/UPDATE permission)
      // (2) fetched authorization != null -> add READ and UPDATE permission
      authorization = updateAuthorization(authorization, newOwner, null, TASK, taskId, READ, UPDATE);

      // return always created or updated authorization
      return new AuthorizationEntity[]{ authorization };
    }

    return null;
  }

  public AuthorizationEntity[] newTaskUserIdentityLink(Task task, String userId, String type) {
    // create (or update) an authorization for the given user
    // whenever a new user identity link will be added

    String taskId = task.getId();

    // fetch existing authorization
    AuthorizationEntity authorization = getGrantAuthorizationByUserId(userId, TASK, taskId);

    // update authorization:
    // (1) fetched authorization == null -> create a new authorization (with READ/UPDATE permission)
    // (2) fetched authorization != null -> add READ and UPDATE permission
    authorization = updateAuthorization(authorization, userId, null, TASK, taskId, READ, UPDATE);

    // return always created or updated authorization
    return new AuthorizationEntity[]{ authorization };
  }

  public AuthorizationEntity[] newTaskGroupIdentityLink(Task task, String groupId, String type) {
    // create (or update) an authorization for the given group
    // whenever a new user identity link will be added
    String taskId = task.getId();

    // fetch existing authorization
    AuthorizationEntity authorization = getGrantAuthorizationByGroupId(groupId, TASK, taskId);

    // update authorization:
    // (1) fetched authorization == null -> create a new authorization (with READ/UPDATE permission)
    // (2) fetched authorization != null -> add READ and UPDATE permission
    authorization = updateAuthorization(authorization, null, groupId, TASK, taskId, READ, UPDATE);

    // return always created or updated authorization
    return new AuthorizationEntity[]{ authorization };
  }

  public AuthorizationEntity[] deleteTaskUserIdentityLink(Task task, String userId, String type) {
    // an existing authorization will not be deleted in such a case
    return null;
  }

  public AuthorizationEntity[] deleteTaskGroupIdentityLink(Task task, String groupId, String type) {
    // an existing authorization will not be deleted in such a case
    return null;
  }

  public AuthorizationEntity[] newDecisionDefinition(DecisionDefinition decisionDefinition) {
    // no default authorizations on decision definitions.
    return null;
  }

  // helper //////////////////////////////////////////////////////////////

  protected AuthorizationManager getAuthorizationManager() {
    CommandContext commandContext = Context.getCommandContext();
    return commandContext.getAuthorizationManager();
  }

  protected AuthorizationEntity getGrantAuthorizationByUserId(String userId, Resource resource, String resourceId) {
    AuthorizationManager authorizationManager = getAuthorizationManager();
    return authorizationManager.findAuthorizationByUserIdAndResourceId(AUTH_TYPE_GRANT, userId, resource, resourceId);
  }

  protected AuthorizationEntity getGrantAuthorizationByGroupId(String groupId, Resource resource, String resourceId) {
    AuthorizationManager authorizationManager = getAuthorizationManager();
    return authorizationManager.findAuthorizationByGroupIdAndResourceId(AUTH_TYPE_GRANT, groupId, resource, resourceId);
  }

  protected AuthorizationEntity updateAuthorization(AuthorizationEntity authorization, String userId, String groupId, Resource resource, String resourceId, Permission... permissions) {
    if (authorization == null) {
      authorization = createGrantAuthorization(userId, groupId, resource, resourceId);
    }

    if (permissions != null) {
      for (Permission permission : permissions) {
        authorization.addPermission(permission);
      }
    }

    return authorization;
  }

  protected AuthorizationEntity createGrantAuthorization(String userId, String groupId, Resource resource, String resourceId, Permission... permissions) {
    AuthorizationEntity authorization = new AuthorizationEntity(AUTH_TYPE_GRANT);
    authorization.setUserId(userId);
    authorization.setGroupId(groupId);
    authorization.setResource(resource);
    authorization.setResourceId(resourceId);

    if (permissions != null) {
      for (Permission permission : permissions) {
        authorization.addPermission(permission);
      }
    }

    return authorization;
  }

}
