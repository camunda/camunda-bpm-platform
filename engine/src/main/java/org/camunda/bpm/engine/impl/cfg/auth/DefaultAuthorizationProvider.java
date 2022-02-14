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
package org.camunda.bpm.engine.impl.cfg.auth;

import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.camunda.bpm.engine.authorization.Resources.FILTER;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_TASK;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.authorization.Resources.TENANT;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureValidIndividualResourceId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 * <p>Provides the default authorizations for Camunda Platform.</p>
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

      return createOrUpdateAuthorizationsByUserId(task, newAssignee);
    }

    return null;
  }

  public AuthorizationEntity[] newTaskOwner(Task task, String oldOwner, String newOwner) {
    if (newOwner != null) {

      ensureValidIndividualResourceId("Cannot create default authorization for owner " + newOwner,
          newOwner);

      // create (or update) an authorization for the new owner.

      return createOrUpdateAuthorizationsByUserId(task, newOwner);
    }

    return null;
  }

  public AuthorizationEntity[] newTaskUserIdentityLink(Task task, String userId, String type) {
    // create (or update) an authorization for the given user
    // whenever a new user identity link will be added

    ensureValidIndividualResourceId("Cannot grant default authorization for identity link to user " + userId,
        userId);

    return createOrUpdateAuthorizationsByUserId(task, userId);
  }

  public AuthorizationEntity[] newTaskGroupIdentityLink(Task task, String groupId, String type) {

    ensureValidIndividualResourceId("Cannot grant default authorization for identity link to group " + groupId,
        groupId);

    // create (or update) an authorization for the given group
    // whenever a new user identity link will be added

    return createOrUpdateAuthorizationsByGroupId(task, groupId);
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

  protected AuthorizationEntity[] createOrUpdateAuthorizationsByGroupId(Task task, String groupId) {
    return createOrUpdateAuthorizations(task, groupId, null);
  }

  protected AuthorizationEntity[] createOrUpdateAuthorizationsByUserId(Task task, String userId) {
    return createOrUpdateAuthorizations(task, null, userId);
  }

  /**
   * (1) Fetch existing runtime & history authorizations
   * (2) Update authorizations:
   *     (2a) fetched authorization == null
   *         ->  create a new runtime authorization (with READ, (UPDATE/TASK_WORK) permission,
   *             and READ_VARIABLE if enabled)
   *         ->  create a new history authorization (with READ on HISTORIC_TASK)
   *     (2b) fetched authorization != null
   *         ->  Add READ, (UPDATE/TASK_WORK) permission, and READ_VARIABLE if enabled
   *             UPDATE or TASK_WORK permission is configurable in camunda.cfg.xml and by default,
   *             UPDATE permission is provided
   *         ->  Add READ on HISTORIC_TASK
   */
  protected AuthorizationEntity[] createOrUpdateAuthorizations(Task task, String groupId,
                                                               String userId) {

    boolean enforceSpecificVariablePermission = isEnforceSpecificVariablePermission();

    Permission[] runtimeTaskPermissions = getRuntimePermissions(enforceSpecificVariablePermission);

    AuthorizationEntity runtimeAuthorization = createOrUpdateAuthorization(task, userId, groupId,
        TASK, false, runtimeTaskPermissions);

    if (!isHistoricInstancePermissionsEnabled()) {
      return new AuthorizationEntity[]{ runtimeAuthorization };

    } else {
      Permission[] historicTaskPermissions =
          getHistoricPermissions(enforceSpecificVariablePermission);

      AuthorizationEntity historyAuthorization = createOrUpdateAuthorization(task, userId,
          groupId, HISTORIC_TASK, true, historicTaskPermissions);

      return new AuthorizationEntity[]{ runtimeAuthorization, historyAuthorization };
    }
  }

  protected AuthorizationEntity createOrUpdateAuthorization(Task task, String userId,
                                                            String groupId, Resource resource,
                                                            boolean isHistoric,
                                                            Permission... permissions) {

    String taskId = task.getId();

    AuthorizationEntity authorization = getGrantAuthorization(taskId, userId, groupId, resource);

    if (authorization == null) {
      authorization = createAuthorization(userId, groupId, resource, taskId, permissions);

      if (isHistoric) {
        provideRemovalTime(authorization, task);
      }

    } else {
      addPermissions(authorization, permissions);

    }

    return authorization;
  }

  protected void provideRemovalTime(AuthorizationEntity authorization, Task task) {
    String rootProcessInstanceId = getRootProcessInstanceId(task);

    if (rootProcessInstanceId != null) {
      authorization.setRootProcessInstanceId(rootProcessInstanceId);

      if (isHistoryRemovalTimeStrategyStart()) {
        HistoryEvent rootProcessInstance = findHistoricProcessInstance(rootProcessInstanceId);

        Date removalTime = null;
        if (rootProcessInstance != null) {
          removalTime = rootProcessInstance.getRemovalTime();

        }

        authorization.setRemovalTime(removalTime);

      }
    }
  }

  protected String getRootProcessInstanceId(Task task) {
    ExecutionEntity execution = (ExecutionEntity) ((DelegateTask) task).getExecution();

    if (execution != null) {
      return execution.getRootProcessInstanceId();

    } else {
      return null;

    }
  }

  protected boolean isHistoryRemovalTimeStrategyStart() {
    return HISTORY_REMOVAL_TIME_STRATEGY_START.equals(getHistoryRemovalTimeStrategy());
  }

  protected String getHistoryRemovalTimeStrategy() {
    return Context.getProcessEngineConfiguration()
        .getHistoryRemovalTimeStrategy();
  }

  protected HistoryEvent findHistoricProcessInstance(String rootProcessInstanceId) {
    return Context.getCommandContext()
        .getDbEntityManager()
        .selectById(HistoricProcessInstanceEventEntity.class, rootProcessInstanceId);
  }

  protected Permission[] getHistoricPermissions(boolean enforceSpecificVariablePermission) {
    List<Permission> historicPermissions = new ArrayList<>();
    historicPermissions.add(HistoricTaskPermissions.READ);

    if (enforceSpecificVariablePermission) {
      historicPermissions.add(HistoricTaskPermissions.READ_VARIABLE);
    }

    return historicPermissions.toArray(new Permission[0]);
  }

  protected Permission[] getRuntimePermissions(boolean enforceSpecificVariablePermission) {
    List<Permission> runtimePermissions = new ArrayList<>();
    runtimePermissions.add(READ);

    Permission defaultUserPermissionForTask = getDefaultUserPermissionForTask();
    runtimePermissions.add(defaultUserPermissionForTask);

    if (enforceSpecificVariablePermission) {
      runtimePermissions.add(TaskPermissions.READ_VARIABLE);
    }

    return runtimePermissions.toArray(new Permission[0]);
  }

  protected boolean isHistoricInstancePermissionsEnabled() {
    return Context.getProcessEngineConfiguration().isEnableHistoricInstancePermissions();
  }

  protected AuthorizationManager getAuthorizationManager() {
    CommandContext commandContext = Context.getCommandContext();
    return commandContext.getAuthorizationManager();
  }

  protected AuthorizationEntity getGrantAuthorization(String taskId, String userId,
                                                      String groupId, Resource resource) {
    if (groupId != null) {
      return getGrantAuthorizationByGroupId(groupId, resource, taskId);

    } else {
      return getGrantAuthorizationByUserId(userId, resource, taskId);

    }
  }

  protected AuthorizationEntity getGrantAuthorizationByUserId(String userId, Resource resource, String resourceId) {
    AuthorizationManager authorizationManager = getAuthorizationManager();
    return authorizationManager.findAuthorizationByUserIdAndResourceId(AUTH_TYPE_GRANT, userId, resource, resourceId);
  }

  protected AuthorizationEntity getGrantAuthorizationByGroupId(String groupId, Resource resource, String resourceId) {
    AuthorizationManager authorizationManager = getAuthorizationManager();
    return authorizationManager.findAuthorizationByGroupIdAndResourceId(AUTH_TYPE_GRANT, groupId, resource, resourceId);
  }

  protected AuthorizationEntity createAuthorization(String userId, String groupId,
                                                    Resource resource, String resourceId,
                                                    Permission... permissions) {
    AuthorizationEntity authorization =
        createGrantAuthorization(userId, groupId, resource, resourceId, permissions);

    updateAuthorizationBasedOnCacheEntries(authorization, userId, groupId, resource, resourceId);

    return authorization;
  }

  protected void addPermissions(AuthorizationEntity authorization, Permission... permissions) {
    if (permissions != null) {
      for (Permission permission : permissions) {
        if (permission != null) {
          authorization.addPermission(permission);
        }
      }
    }
  }

  protected AuthorizationEntity createGrantAuthorization(String userId, String groupId,
                                                         Resource resource, String resourceId,
                                                         Permission... permissions) {
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

    addPermissions(authorization, permissions);

    return authorization;
  }

  protected Permission getDefaultUserPermissionForTask() {
    return Context
      .getProcessEngineConfiguration()
      .getDefaultUserPermissionForTask();
  }

  protected boolean isEnforceSpecificVariablePermission() {
    return Context.getProcessEngineConfiguration()
        .isEnforceSpecificVariablePermission();
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
