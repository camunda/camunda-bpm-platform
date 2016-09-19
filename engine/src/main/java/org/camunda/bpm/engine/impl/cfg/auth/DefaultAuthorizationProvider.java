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
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.camunda.bpm.engine.authorization.Resources.FILTER;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.authorization.Resources.TENANT;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureValidIndividualResourceId;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
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

    ensureValidIndividualResourceId("Cannot create default authorization for user " + userId,
        userId);
    AuthorizationEntity resourceOwnerAuthorization = createGrantAuthorization(userId, null, USER, userId, ALL);

    return new AuthorizationEntity[]{ resourceOwnerAuthorization };
  }

  public AuthorizationEntity[] newGroup(Group group) {
    List<AuthorizationEntity> authorizations = new ArrayList<AuthorizationEntity>();

    // whenever a new group is created, all users part of the
    // group are granted READ permissions on the group
    String groupId = group.getId();

    ensureValidIndividualResourceId("Cannot create default authorization for group " + groupId,
        groupId);

    AuthorizationEntity groupMemberAuthorization = createGrantAuthorization(null, groupId, GROUP, groupId, READ);
    authorizations.add(groupMemberAuthorization);

    return authorizations.toArray(new AuthorizationEntity[0]);
  }

  public AuthorizationEntity[] newTenant(Tenant tenant) {
    // no default authorizations on tenants.
    return null;
  }

  public AuthorizationEntity[] groupMembershipCreated(String groupId, String userId) {

    // no default authorizations on memberships.

    return null;
  }

  public AuthorizationEntity[] tenantMembershipCreated(Tenant tenant, User user) {

    AuthorizationEntity userAuthorization = createGrantAuthorization(user.getId(), null, TENANT, tenant.getId(), READ);

    return new AuthorizationEntity[]{ userAuthorization };
  }

  public AuthorizationEntity[] tenantMembershipCreated(Tenant tenant, Group group) {
    AuthorizationEntity userAuthorization = createGrantAuthorization(null, group.getId(), TENANT, tenant.getId(), READ);

    return new AuthorizationEntity[]{ userAuthorization };
  }

  public AuthorizationEntity[] newFilter(Filter filter) {

    String owner = filter.getOwner();
    if(owner != null) {
      // create an authorization which gives the owner of the filter all permissions on the filter
      String filterId = filter.getId();

      ensureValidIndividualResourceId("Cannot create default authorization for filter owner " + owner,
          owner);

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

      ensureValidIndividualResourceId("Cannot create default authorization for assignee " + newAssignee,
          newAssignee);

      // create (or update) an authorization for the new assignee.

      String taskId = task.getId();

      // fetch existing authorization
      AuthorizationEntity authorization = getGrantAuthorizationByUserId(newAssignee, TASK, taskId);

      // update authorization:
      // (1) fetched authorization == null -> create a new authorization (with READ and (UPDATE/TASK_WORK) permission)
      // (2) fetched authorization != null -> add READ and (UPDATE/TASK_WORK) permission
      // Update or TASK_WORK permission is configurable in camunda.cfg.xml and by default, UPDATE permission is provided
      authorization = updateAuthorization(authorization, newAssignee, null, TASK, taskId, READ, getDefaultUserPermissionForTask());

      // return always created or updated authorization
      return new AuthorizationEntity[]{ authorization };
    }

    return null;
  }

  public AuthorizationEntity[] newTaskOwner(Task task, String oldOwner, String newOwner) {
    if (newOwner != null) {

      ensureValidIndividualResourceId("Cannot create default authorization for owner " + newOwner,
          newOwner);

      // create (or update) an authorization for the new owner.
      String taskId = task.getId();

      // fetch existing authorization
      AuthorizationEntity authorization = getGrantAuthorizationByUserId(newOwner, TASK, taskId);

      // update authorization:
      // (1) fetched authorization == null -> create a new authorization (with READ and (UPDATE/TASK_WORK) permission)
      // (2) fetched authorization != null -> add READ and (UPDATE/TASK_WORK) permission
      // Update or TASK_WORK permission is configurable in camunda.cfg.xml and by default, UPDATE permission is provided
      authorization = updateAuthorization(authorization, newOwner, null, TASK, taskId, READ, getDefaultUserPermissionForTask());

      // return always created or updated authorization
      return new AuthorizationEntity[]{ authorization };
    }

    return null;
  }

  public AuthorizationEntity[] newTaskUserIdentityLink(Task task, String userId, String type) {
    // create (or update) an authorization for the given user
    // whenever a new user identity link will be added

    ensureValidIndividualResourceId("Cannot grant default authorization for identity link to user " + userId,
        userId);

    String taskId = task.getId();

    // fetch existing authorization
    AuthorizationEntity authorization = getGrantAuthorizationByUserId(userId, TASK, taskId);

    // update authorization:
    // (1) fetched authorization == null -> create a new authorization (with READ and (UPDATE/TASK_WORK) permission)
    // (2) fetched authorization != null -> add READ and (UPDATE or TASK_WORK) permission
    // Update or TASK_WORK permission is configurable in camunda.cfg.xml and by default, UPDATE permission is provided
    authorization = updateAuthorization(authorization, userId, null, TASK, taskId, READ, getDefaultUserPermissionForTask());

    // return always created or updated authorization
    return new AuthorizationEntity[]{ authorization };
  }

  public AuthorizationEntity[] newTaskGroupIdentityLink(Task task, String groupId, String type) {

    ensureValidIndividualResourceId("Cannot grant default authorization for identity link to group " + groupId,
        groupId);

    // create (or update) an authorization for the given group
    // whenever a new user identity link will be added
    String taskId = task.getId();

    // fetch existing authorization
    AuthorizationEntity authorization = getGrantAuthorizationByGroupId(groupId, TASK, taskId);

    // update authorization:
    // (1) fetched authorization == null -> create a new authorization (with READ and (UPDATE/TASK_WORK) permission)
    // (2) fetched authorization != null -> add READ and UPDATE permission
    // Update or TASK_WORK permission is configurable in camunda.cfg.xml and by default, UPDATE permission is provided
    authorization = updateAuthorization(authorization, null, groupId, TASK, taskId, READ, getDefaultUserPermissionForTask());

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

  public AuthorizationEntity[] newDecisionRequirementsDefinition(DecisionRequirementsDefinition decisionRequirementsDefinition) {
    // no default authorizations on decision requirements definitions.
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
      updateAuthorizationBasedOnCacheEntries(authorization, userId, groupId, resource, resourceId);
    }

    if (permissions != null) {
      for (Permission permission : permissions) {
        authorization.addPermission(permission);
      }
    }

    return authorization;
  }

  protected AuthorizationEntity createGrantAuthorization(String userId, String groupId, Resource resource, String resourceId, Permission... permissions) {
    // assuming that there are no default authorizations for *
    if (userId != null) {
      ensureValidIndividualResourceId("Cannot create authorization for user " + userId, userId);
    }
    if (groupId != null) {
      ensureValidIndividualResourceId("Cannot create authorization for group " + groupId, groupId);
    }

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

  protected Permission getDefaultUserPermissionForTask() {
    return Context
      .getProcessEngineConfiguration()
      .getDefaultUserPermissionForTask();
  }

  /**
   * Searches through the cache, if there is already an authorization with same rights. If that's the case
   * update the given authorization with the permissions and remove the old one from the cache.
   */
  protected void updateAuthorizationBasedOnCacheEntries(AuthorizationEntity authorization, String userId, String groupId,
                                                        Resource resource, String resourceId) {
    DbEntityManager dbManager = Context.getCommandContext().getDbEntityManager();
    List<AuthorizationEntity> list = dbManager.getCachedEntitiesByType(AuthorizationEntity.class);
    for (AuthorizationEntity authEntity : list) {
      boolean hasSameAuthRights = hasEntitySameAuthorizationRights(authEntity, userId, groupId, resource, resourceId);
      if (hasSameAuthRights) {
        int previousPermissions = authEntity.getPermissions();
        authorization.setPermissions(previousPermissions);
        dbManager.getDbEntityCache().remove(authEntity);
        return;
      }
    }
  }

  protected boolean hasEntitySameAuthorizationRights(AuthorizationEntity authEntity, String userId, String groupId,
                                                     Resource resource, String resourceId) {
    boolean sameUserId = areIdsEqual(authEntity.getUserId(), userId);
    boolean sameGroupId = areIdsEqual(authEntity.getGroupId(), groupId);
    boolean sameResourceId = areIdsEqual(authEntity.getResourceId(), (resourceId));
    boolean sameResourceType = authEntity.getResourceType() == resource.resourceType();
    boolean sameAuthorizationType = authEntity.getAuthorizationType() == AUTH_TYPE_GRANT;
    return sameUserId && sameGroupId &&
        sameResourceType && sameResourceId &&
        sameAuthorizationType;
  }

  protected boolean areIdsEqual(String firstId, String secondId) {
    if (firstId == null || secondId == null) {
      return firstId == secondId;
    }else {
      return firstId.equals(secondId);
    }
  }
}
