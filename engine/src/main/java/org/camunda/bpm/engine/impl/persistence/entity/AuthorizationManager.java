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
package org.camunda.bpm.engine.impl.persistence.entity;

import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ_TASK;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.READ_INSTANCE_VARIABLE;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.BATCH;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_REQUIREMENTS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.authorization.TaskPermissions.READ_VARIABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.ActivityStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.AuthorizationQueryImpl;
import org.camunda.bpm.engine.impl.DeploymentQueryImpl;
import org.camunda.bpm.engine.impl.DeploymentStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.ExternalTaskQueryImpl;
import org.camunda.bpm.engine.impl.HistoricActivityInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricDecisionInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricDetailQueryImpl;
import org.camunda.bpm.engine.impl.HistoricExternalTaskLogQueryImpl;
import org.camunda.bpm.engine.impl.HistoricIdentityLinkLogQueryImpl;
import org.camunda.bpm.engine.impl.HistoricIncidentQueryImpl;
import org.camunda.bpm.engine.impl.HistoricJobLogQueryImpl;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricTaskInstanceQueryImpl;
import org.camunda.bpm.engine.impl.HistoricVariableInstanceQueryImpl;
import org.camunda.bpm.engine.impl.IncidentQueryImpl;
import org.camunda.bpm.engine.impl.JobDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.JobQueryImpl;
import org.camunda.bpm.engine.impl.ProcessDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.ProcessDefinitionStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.UserOperationLogQueryImpl;
import org.camunda.bpm.engine.impl.VariableInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchQueryImpl;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.db.CompositePermissionCheck;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.PermissionCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheckBuilder;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.persistence.entity.util.AuthManagerUtil;
import org.camunda.bpm.engine.impl.persistence.entity.util.AuthManagerUtil.VariablePermissions;
import org.camunda.bpm.engine.impl.util.ResourceTypeUtil;

/**
 * @author Daniel Meyer
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AuthorizationManager extends AbstractManager {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  // Used instead of Collections.emptyList() as mybatis uses reflection to call methods
  // like size() which can lead to problems as Collections.EmptyList is a private implementation
  protected static final List<String> EMPTY_LIST = new ArrayList<>();

  /**
   * Group ids for which authorizations exist in the database.
   * This is initialized once per command by the {@link #filterAuthenticatedGroupIds(List)} method. (Manager
   * instances are command scoped).
   * It is used to only check authorizations for groups for which authorizations exist. In other words,
   * if for a given group no authorization exists in the DB, then auth checks are not performed for this group.
   */
  protected Set<String> availableAuthorizedGroupIds = null;

  protected Boolean isRevokeAuthCheckUsed = null;

  public PermissionCheckBuilder newPermissionCheckBuilder() {
    return new PermissionCheckBuilder();
  }

  public Authorization createNewAuthorization(int type) {
    checkAuthorization(CREATE, AUTHORIZATION, null);
    return new AuthorizationEntity(type);
  }

  @Override
  public void insert(DbEntity authorization) {
    checkAuthorization(CREATE, AUTHORIZATION, null);
    getDbEntityManager().insert(authorization);
  }

  public List<Authorization> selectAuthorizationByQueryCriteria(AuthorizationQueryImpl authorizationQuery) {
    configureQuery(authorizationQuery, AUTHORIZATION);
    return getDbEntityManager().selectList("selectAuthorizationByQueryCriteria", authorizationQuery);
  }

  public Long selectAuthorizationCountByQueryCriteria(AuthorizationQueryImpl authorizationQuery) {
    configureQuery(authorizationQuery, AUTHORIZATION);
    return (Long) getDbEntityManager().selectOne("selectAuthorizationCountByQueryCriteria", authorizationQuery);
  }

  public AuthorizationEntity findAuthorizationByUserIdAndResourceId(int type, String userId, Resource resource, String resourceId) {
    return findAuthorization(type, userId, null, resource, resourceId);
  }

  public AuthorizationEntity findAuthorizationByGroupIdAndResourceId(int type, String groupId, Resource resource, String resourceId) {
    return findAuthorization(type, null, groupId, resource, resourceId);
  }

  public AuthorizationEntity findAuthorization(int type, String userId, String groupId, Resource resource, String resourceId) {
    Map<String, Object> params = new HashMap<>();

    params.put("type", type);
    params.put("userId", userId);
    params.put("groupId", groupId);
    params.put("resourceId", resourceId);

    if (resource != null) {
      params.put("resourceType", resource.resourceType());
    }

    return (AuthorizationEntity) getDbEntityManager().selectOne("selectAuthorizationByParameters", params);
  }

  public void update(AuthorizationEntity authorization) {
    checkAuthorization(UPDATE, AUTHORIZATION, authorization.getId());
    getDbEntityManager().merge(authorization);
  }

  @Override
  public void delete(DbEntity authorization) {
    checkAuthorization(DELETE, AUTHORIZATION, authorization.getId());
    deleteAuthorizationsByResourceId(AUTHORIZATION, authorization.getId());
    super.delete(authorization);
  }

  // authorization checks ///////////////////////////////////////////

  public void checkAuthorization(CompositePermissionCheck compositePermissionCheck) {
    if(isAuthCheckExecuted()) {

      Authentication currentAuthentication = getCurrentAuthentication();
      String userId = currentAuthentication.getUserId();

      boolean isAuthorized = isAuthorized(compositePermissionCheck);
      if (!isAuthorized) {

        List<MissingAuthorization> missingAuthorizations = new ArrayList<>();

        for (PermissionCheck check: compositePermissionCheck.getAllPermissionChecks()) {
          missingAuthorizations.add(new MissingAuthorization(
              check.getPermission().getName(),
              check.getResource().resourceName(),
              check.getResourceId()));
        }

        throw new AuthorizationException(userId, missingAuthorizations);
      }
    }
  }

  public void checkAuthorization(Permission permission, Resource resource) {
    checkAuthorization(permission, resource, null);
  }

  @Override
  public void checkAuthorization(Permission permission, Resource resource, String resourceId) {
    if(isAuthCheckExecuted()) {
      Authentication currentAuthentication = getCurrentAuthentication();
      boolean isAuthorized = isAuthorized(currentAuthentication.getUserId(), currentAuthentication.getGroupIds(), permission, resource, resourceId);
      if (!isAuthorized) {
        throw new AuthorizationException(
            currentAuthentication.getUserId(),
            permission.getName(),
            resource.resourceName(),
            resourceId);
      }
    }

  }

  public boolean isAuthorized(Permission permission, Resource resource, String resourceId) {
    // this will be called by LdapIdentityProviderSession#isAuthorized() for executing LdapQueries.
    // to be backward compatible a check whether authorization has been enabled inside the given
    // command context will not be done.
    final Authentication currentAuthentication = getCurrentAuthentication();

    if(isAuthorizationEnabled() && currentAuthentication != null && currentAuthentication.getUserId() != null) {
      return isAuthorized(currentAuthentication.getUserId(), currentAuthentication.getGroupIds(), permission, resource, resourceId);

    } else {
      return true;

    }
  }

  public boolean isAuthorized(String userId, List<String> groupIds, Permission permission, Resource resource, String resourceId) {
    if (!isPermissionDisabled(permission)) {
      PermissionCheck permCheck = new PermissionCheck();
      permCheck.setPermission(permission);
      permCheck.setResource(resource);
      permCheck.setResourceId(resourceId);

      return isAuthorized(userId, groupIds, permCheck);
    } else {
      return true;
    }
  }

  public boolean isAuthorized(String userId, List<String> groupIds, PermissionCheck permissionCheck) {
    if(!isAuthorizationEnabled()) {
      return true;
    }

    if (!isResourceValidForPermission(permissionCheck)) {
      throw LOG.invalidResourceForPermission(permissionCheck.getResource().resourceName(), permissionCheck.getPermission().getName());
    }

    List<String> filteredGroupIds = filterAuthenticatedGroupIds(groupIds);

    boolean isRevokeAuthorizationCheckEnabled = isRevokeAuthCheckEnabled(userId, groupIds);
    CompositePermissionCheck compositePermissionCheck = createCompositePermissionCheck(permissionCheck);
    AuthorizationCheck authCheck = new AuthorizationCheck(userId, filteredGroupIds, compositePermissionCheck, isRevokeAuthorizationCheckEnabled);
    return getDbEntityManager().selectBoolean("isUserAuthorizedForResource", authCheck);
  }

  protected boolean isRevokeAuthCheckEnabled(String userId, List<String> groupIds) {
    Boolean isRevokeAuthCheckEnabled = this.isRevokeAuthCheckUsed;

    if(isRevokeAuthCheckEnabled == null) {
      String configuredMode = Context.getProcessEngineConfiguration().getAuthorizationCheckRevokes();
      if(configuredMode != null) {
        configuredMode = configuredMode.toLowerCase();
      }
      if(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_ALWAYS.equals(configuredMode)) {
        isRevokeAuthCheckEnabled = true;
      }
      else if(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_NEVER.equals(configuredMode)) {
        isRevokeAuthCheckEnabled = false;
      }
      else {
        final Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("authGroupIds", filterAuthenticatedGroupIds(groupIds));
        isRevokeAuthCheckEnabled = getDbEntityManager().selectBoolean("selectRevokeAuthorization", params);
      }
      this.isRevokeAuthCheckUsed = isRevokeAuthCheckEnabled;
    }

    return isRevokeAuthCheckEnabled;
  }

  protected CompositePermissionCheck createCompositePermissionCheck(PermissionCheck permissionCheck) {
    CompositePermissionCheck compositePermissionCheck = new CompositePermissionCheck();
    compositePermissionCheck.setAtomicChecks(Arrays.asList(permissionCheck));
    return compositePermissionCheck;
  }

  public boolean isAuthorized(String userId, List<String> groupIds, CompositePermissionCheck compositePermissionCheck) {
    for (PermissionCheck permissionCheck : compositePermissionCheck.getAllPermissionChecks()) {
      if (!isResourceValidForPermission(permissionCheck)) {
        throw LOG.invalidResourceForPermission(permissionCheck.getResource().resourceName(), permissionCheck.getPermission().getName());
      }
    }
    List<String> filteredGroupIds = filterAuthenticatedGroupIds(groupIds);

    boolean isRevokeAuthorizationCheckEnabled = isRevokeAuthCheckEnabled(userId, groupIds);
    AuthorizationCheck authCheck = new AuthorizationCheck(userId, filteredGroupIds, compositePermissionCheck, isRevokeAuthorizationCheckEnabled);
    return getDbEntityManager().selectBoolean("isUserAuthorizedForResource", authCheck);
  }

  public boolean isAuthorized(CompositePermissionCheck compositePermissionCheck) {
    Authentication currentAuthentication = getCurrentAuthentication();

    if (currentAuthentication != null) {
      return isAuthorized(currentAuthentication.getUserId(), currentAuthentication.getGroupIds(), compositePermissionCheck);
    }
    else {
      return true;
    }
  }

  protected boolean isResourceValidForPermission(PermissionCheck permissionCheck) {
    Resource[] permissionResources = permissionCheck.getPermission().getTypes();
    Resource givenResource = permissionCheck.getResource();
    return ResourceTypeUtil.resourceIsContainedInArray(givenResource.resourceType(), permissionResources);
  }

  public void validateResourceCompatibility(AuthorizationEntity authorization) {
    int resourceType = authorization.getResourceType();
    Set<Permission> permissionSet = authorization.getCachedPermissions();

    for (Permission permission : permissionSet) {
      if (!ResourceTypeUtil.resourceIsContainedInArray(resourceType, permission.getTypes())) {
        throw LOG.invalidResourceForAuthorization(resourceType, permission.getName());
      }
    }
  }


  // authorization checks on queries ////////////////////////////////

  public void configureQuery(ListQueryParameterObject query) {

    AuthorizationCheck authCheck = query.getAuthCheck();
    authCheck.getPermissionChecks().clear();

    if(isAuthCheckExecuted()) {
      Authentication currentAuthentication = getCurrentAuthentication();
      authCheck.setAuthUserId(currentAuthentication.getUserId());
      authCheck.setAuthGroupIds(currentAuthentication.getGroupIds());
      enableQueryAuthCheck(authCheck);
    }
    else {
      authCheck.setAuthorizationCheckEnabled(false);
      authCheck.setAuthUserId(null);
      authCheck.setAuthGroupIds(null);
    }
  }

  public void configureQueryHistoricFinishedInstanceReport(ListQueryParameterObject query, Resource resource) {
    configureQuery(query);

    CompositePermissionCheck compositePermissionCheck = new PermissionCheckBuilder()
      .conjunctive()
        .atomicCheck(resource, "RES.KEY_", READ)
        .atomicCheck(resource, "RES.KEY_", READ_HISTORY)
      .build();

    query.getAuthCheck().setPermissionChecks(compositePermissionCheck);
  }

  public void enableQueryAuthCheck(AuthorizationCheck authCheck) {
    List<String> authGroupIds = authCheck.getAuthGroupIds();
    String authUserId = authCheck.getAuthUserId();

    authCheck.setAuthorizationCheckEnabled(true);
    authCheck.setAuthGroupIds(filterAuthenticatedGroupIds(authGroupIds));
    authCheck.setRevokeAuthorizationCheckEnabled(isRevokeAuthCheckEnabled(authUserId, authGroupIds));
  }

  @Override
  public void configureQuery(AbstractQuery query, Resource resource) {
    configureQuery(query, resource, "RES.ID_");
  }

  public void configureQuery(AbstractQuery query, Resource resource, String queryParam) {
    configureQuery(query, resource, queryParam, Permissions.READ);
  }

  public void configureQuery(AbstractQuery query, Resource resource, String queryParam, Permission permission) {
    configureQuery(query);
    CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
        .atomicCheck(resource, queryParam, permission)
        .build();
    addPermissionCheck(query.getAuthCheck(), permissionCheck);
  }

  public boolean isPermissionDisabled(Permission permission) {
    List<String> disabledPermissions = getCommandContext().getProcessEngineConfiguration().getDisabledPermissions();
    if (disabledPermissions != null) {
      for (String disabledPermission : disabledPermissions) {
        if (permission.getName().equals(disabledPermission)) {
          return true;
        }
      }
    }
    return false;
  }

  protected void addPermissionCheck(AuthorizationCheck authCheck, CompositePermissionCheck compositeCheck) {
    CommandContext commandContext = getCommandContext();
    if (isAuthorizationEnabled() && getCurrentAuthentication() != null && commandContext.isAuthorizationCheckEnabled()) {
      authCheck.setPermissionChecks(compositeCheck);
    }
  }

  // delete authorizations //////////////////////////////////////////////////

  public void deleteAuthorizationsByResourceIds(Resources resource,
                                                List<String> resourceIds) {

    if(resourceIds == null) {
      throw new IllegalArgumentException("Resource ids cannot be null");
    }

    resourceIds.forEach(resourceId ->
        deleteAuthorizationsByResourceId(resource, resourceId));

  }

  public void deleteAuthorizationsByResourceId(Resource resource, String resourceId) {

    if(resourceId == null) {
      throw new IllegalArgumentException("Resource id cannot be null");
    }

    if(isAuthorizationEnabled()) {
      Map<String, Object> deleteParams = new HashMap<>();
      deleteParams.put("resourceType", resource.resourceType());
      deleteParams.put("resourceId", resourceId);
      getDbEntityManager().delete(AuthorizationEntity.class, "deleteAuthorizationsForResourceId", deleteParams);
    }

  }

  public void deleteAuthorizationsByResourceIdAndUserId(Resource resource, String resourceId, String userId) {

    if(resourceId == null) {
      throw new IllegalArgumentException("Resource id cannot be null");
    }

    if(isAuthorizationEnabled()) {
      Map<String, Object> deleteParams = new HashMap<>();
      deleteParams.put("resourceType", resource.resourceType());
      deleteParams.put("resourceId", resourceId);
      deleteParams.put("userId", userId);
      getDbEntityManager().delete(AuthorizationEntity.class, "deleteAuthorizationsForResourceIdAndUserId", deleteParams);
    }

  }

  public void deleteAuthorizationsByResourceIdAndGroupId(Resource resource, String resourceId, String groupId) {

    if(resourceId == null) {
      throw new IllegalArgumentException("Resource id cannot be null");
    }

    if(isAuthorizationEnabled()) {
      Map<String, Object> deleteParams = new HashMap<>();
      deleteParams.put("resourceType", resource.resourceType());
      deleteParams.put("resourceId", resourceId);
      deleteParams.put("groupId", groupId);
      getDbEntityManager().delete(AuthorizationEntity.class, "deleteAuthorizationsForResourceIdAndGroupId", deleteParams);
    }

  }

  // predefined authorization checks

  /* MEMBER OF CAMUNDA_ADMIN */

  /**
   * Checks if the current authentication contains the group
   * {@link Groups#CAMUNDA_ADMIN}. The check is ignored if the authorization is
   * disabled or no authentication exists.
   *
   * @throws AuthorizationException
   */
  public void checkCamundaAdmin() {
    final Authentication currentAuthentication = getCurrentAuthentication();

    if (isAuthorizationEnabled() && getCommandContext().isAuthorizationCheckEnabled()
        && currentAuthentication != null && !isCamundaAdmin(currentAuthentication)) {

      throw LOG.requiredCamundaAdmin();
    }
  }

  public void checkCamundaAdminOrPermission(Consumer<CommandChecker> permissionCheck) {
    if (isAuthorizationEnabled() && getCommandContext().isAuthorizationCheckEnabled()) {

      AuthorizationException authorizationException = null;
      AuthorizationException adminException = null;

      try {
        for (CommandChecker checker : getCommandContext().getProcessEngineConfiguration().getCommandCheckers()) {
          permissionCheck.accept(checker);
        }
      } catch (AuthorizationException e) {
        authorizationException = e;
      }

      try {
        checkCamundaAdmin();
      } catch (AuthorizationException e) {
        adminException = e;
      }

      if (authorizationException != null && adminException != null) {
        // throw combined exception
        List<MissingAuthorization> info = authorizationException.getMissingAuthorizations();
        throw LOG.requiredCamundaAdminOrPermissionException(info);
      }
    }
  }

  /**
   * @param authentication
   *          authentication to check, cannot be <code>null</code>
   * @return <code>true</code> if the given authentication contains the group
   *         {@link Groups#CAMUNDA_ADMIN} or the user
   */
  public boolean isCamundaAdmin(Authentication authentication) {
    List<String> groupIds = authentication.getGroupIds();
    if (groupIds != null) {
      CommandContext commandContext = Context.getCommandContext();
      List<String> adminGroups = commandContext.getProcessEngineConfiguration().getAdminGroups();
      for (String adminGroup : adminGroups) {
        if (groupIds.contains(adminGroup)) {
          return true;
        }
      }
    }

    String userId = authentication.getUserId();
    if (userId != null) {
      CommandContext commandContext = Context.getCommandContext();
      List<String> adminUsers = commandContext.getProcessEngineConfiguration().getAdminUsers();
      return adminUsers != null && adminUsers.contains(userId);
    }

    return false;
  }

  /* QUERIES */

  // deployment query ////////////////////////////////////////

  public void configureDeploymentQuery(DeploymentQueryImpl query) {
    configureQuery(query, DEPLOYMENT);
  }

  // process definition query ////////////////////////////////

  public void configureProcessDefinitionQuery(ProcessDefinitionQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.KEY_");

    if (query.isStartablePermissionCheck()) {
      AuthorizationCheck authorizationCheck = query.getAuthCheck();

      if (!authorizationCheck.isRevokeAuthorizationCheckEnabled()) {
        CompositePermissionCheck permCheck = new PermissionCheckBuilder()
            .atomicCheck(PROCESS_DEFINITION, "RES.KEY_", Permissions.CREATE_INSTANCE)
            .build();

        query.addProcessDefinitionCreatePermissionCheck(permCheck);

      } else {
        CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
            .conjunctive()
            .atomicCheck(PROCESS_DEFINITION, "RES.KEY_", READ)
            .atomicCheck(PROCESS_DEFINITION, "RES.KEY_", Permissions.CREATE_INSTANCE)
            .build();
        addPermissionCheck(authorizationCheck, permissionCheck);
      }

    }

  }

  // execution/process instance query ////////////////////////

  public void configureExecutionQuery(AbstractQuery query) {
    configureQuery(query);
    CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
        .disjunctive()
        .atomicCheck(PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ)
        .atomicCheck(PROCESS_DEFINITION, "P.KEY_", READ_INSTANCE)
        .build();
    addPermissionCheck(query.getAuthCheck(), permissionCheck);
  }

  // task query //////////////////////////////////////////////

  public void configureTaskQuery(TaskQueryImpl query) {
    configureQuery(query);

    if(query.getAuthCheck().isAuthorizationCheckEnabled()) {

      // necessary authorization check when the task is part of
      // a running process instance

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
              .disjunctive()
              .atomicCheck(TASK, "RES.ID_", READ)
              .atomicCheck(PROCESS_DEFINITION, "D.KEY_", READ_TASK)
              .build();
        addPermissionCheck(query.getAuthCheck(), permissionCheck);
    }
  }

  // event subscription query //////////////////////////////

  public void configureEventSubscriptionQuery(EventSubscriptionQueryImpl query) {
    configureQuery(query);
    CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheck(PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ)
            .atomicCheck(PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE)
            .build();
    addPermissionCheck(query.getAuthCheck(), permissionCheck);
  }

  public void configureConditionalEventSubscriptionQuery(ListQueryParameterObject query) {
    configureQuery(query);
    CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
        .atomicCheck(PROCESS_DEFINITION, "P.KEY_", READ)
        .build();
    addPermissionCheck(query.getAuthCheck(), permissionCheck);
  }

  // incident query ///////////////////////////////////////

  public void configureIncidentQuery(IncidentQueryImpl query) {
    configureQuery(query);
    CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheck(PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ)
            .atomicCheck(PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE)
            .build();
    addPermissionCheck(query.getAuthCheck(), permissionCheck);
  }

  // variable instance query /////////////////////////////

  protected void configureVariableInstanceQuery(VariableInstanceQueryImpl query) {
    configureQuery(query);

    if(query.getAuthCheck().isAuthorizationCheckEnabled()) {

      CompositePermissionCheck permissionCheck;
      if (isEnsureSpecificVariablePermission()) {
        permissionCheck = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheck(PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE_VARIABLE)
            .atomicCheck(TASK, "RES.TASK_ID_", READ_VARIABLE)
            .build();
      } else {
        permissionCheck = new PermissionCheckBuilder()
              .disjunctive()
              .atomicCheck(PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ)
              .atomicCheck(PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE)
              .atomicCheck(TASK, "RES.TASK_ID_", READ)
              .build();
      }
        addPermissionCheck(query.getAuthCheck(), permissionCheck);
    }
  }

  // job definition query ////////////////////////////////////////////////

  public void configureJobDefinitionQuery(JobDefinitionQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_");
  }

  // job query //////////////////////////////////////////////////////////

  public void configureJobQuery(JobQueryImpl query) {
    configureQuery(query);
    CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
        .disjunctive()
        .atomicCheck(PROCESS_INSTANCE, "RES.PROCESS_INSTANCE_ID_", READ)
        .atomicCheck(PROCESS_DEFINITION, "RES.PROCESS_DEF_KEY_", READ_INSTANCE)
        .build();
    addPermissionCheck(query.getAuthCheck(), permissionCheck);
  }

  /* HISTORY */

  // historic process instance query ///////////////////////////////////

  public void configureHistoricProcessInstanceQuery(HistoricProcessInstanceQueryImpl query) {
    AuthorizationCheck authCheck = query.getAuthCheck();

    boolean isHistoricInstancePermissionsEnabled = isHistoricInstancePermissionsEnabled();
    authCheck.setHistoricInstancePermissionsEnabled(isHistoricInstancePermissionsEnabled);

    if (!isHistoricInstancePermissionsEnabled) {
      configureQuery(query, PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY);

    } else {
      configureQuery(query);

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheck(PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY)
          .atomicCheck(HISTORIC_PROCESS_INSTANCE, "SELF.ID_",
              HistoricProcessInstancePermissions.READ)
          .build();

      addPermissionCheck(authCheck, permissionCheck);

    }
  }

  // historic activity instance query /////////////////////////////////

  public void configureHistoricActivityInstanceQuery(HistoricActivityInstanceQueryImpl query) {
    AuthorizationCheck authCheck = query.getAuthCheck();

    boolean isHistoricInstancePermissionsEnabled = isHistoricInstancePermissionsEnabled();
    authCheck.setHistoricInstancePermissionsEnabled(isHistoricInstancePermissionsEnabled);

    if (!isHistoricInstancePermissionsEnabled) {
      configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);

    } else {
      configureQuery(query);

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY)
          .atomicCheck(HISTORIC_PROCESS_INSTANCE, "RES.PROC_INST_ID_",
              HistoricProcessInstancePermissions.READ)
          .build();

      addPermissionCheck(authCheck, permissionCheck);

    }
  }

  // historic task instance query ////////////////////////////////////

  public void configureHistoricTaskInstanceQuery(HistoricTaskInstanceQueryImpl query) {
    AuthorizationCheck authCheck = query.getAuthCheck();

    boolean isHistoricInstancePermissionsEnabled = isHistoricInstancePermissionsEnabled();
    authCheck.setHistoricInstancePermissionsEnabled(isHistoricInstancePermissionsEnabled);

    if (!isHistoricInstancePermissionsEnabled) {
      configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);

    } else {
      configureQuery(query);

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY)
          .atomicCheck(HISTORIC_PROCESS_INSTANCE, "RES.PROC_INST_ID_",
              HistoricProcessInstancePermissions.READ)
          .atomicCheck(HISTORIC_TASK, "RES.ID_", HistoricTaskPermissions.READ)
          .build();

      addPermissionCheck(query.getAuthCheck(), permissionCheck);

    }
  }

  // historic variable instance query ////////////////////////////////

  public void configureHistoricVariableInstanceQuery(HistoricVariableInstanceQueryImpl query) {
    configureHistoricVariableAndDetailQuery(query);
  }

  // historic detail query ////////////////////////////////

  public void configureHistoricDetailQuery(HistoricDetailQueryImpl query) {
    configureHistoricVariableAndDetailQuery(query);
  }

  protected void configureHistoricVariableAndDetailQuery(AbstractQuery query) {
    boolean ensureSpecificVariablePermission = isEnsureSpecificVariablePermission();

    VariablePermissions variablePermissions =
        AuthManagerUtil.getVariablePermissions(ensureSpecificVariablePermission);

    Permission processDefinitionPermission = variablePermissions.getProcessDefinitionPermission();

    AuthorizationCheck authCheck = query.getAuthCheck();

    boolean isHistoricInstancePermissionsEnabled = isHistoricInstancePermissionsEnabled();
    authCheck.setHistoricInstancePermissionsEnabled(isHistoricInstancePermissionsEnabled);

    if (!isHistoricInstancePermissionsEnabled) {
      configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", processDefinitionPermission);

    } else {
      configureQuery(query);

      Permission historicTaskPermission = variablePermissions.getHistoricTaskPermission();

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", processDefinitionPermission)
          .atomicCheck(HISTORIC_PROCESS_INSTANCE, "RES.PROC_INST_ID_",
              HistoricProcessInstancePermissions.READ)
          .atomicCheck(HISTORIC_TASK, "TI.ID_", historicTaskPermission)
          .build();

      addPermissionCheck(authCheck, permissionCheck);

    }
  }

  // historic job log query ////////////////////////////////

  public void configureHistoricJobLogQuery(HistoricJobLogQueryImpl query) {
    AuthorizationCheck authCheck = query.getAuthCheck();

    boolean isHistoricInstancePermissionsEnabled = isHistoricInstancePermissionsEnabled();
    authCheck.setHistoricInstancePermissionsEnabled(isHistoricInstancePermissionsEnabled);

    if (!isHistoricInstancePermissionsEnabled) {
      configureQuery(query, PROCESS_DEFINITION, "RES.PROCESS_DEF_KEY_", READ_HISTORY);

    } else {
      configureQuery(query);

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
          .disjunctive()
            .atomicCheck(PROCESS_DEFINITION, "RES.PROCESS_DEF_KEY_", READ_HISTORY)
            .atomicCheck(HISTORIC_PROCESS_INSTANCE, "RES.PROCESS_INSTANCE_ID_",
                HistoricProcessInstancePermissions.READ)
          .build();

      addPermissionCheck(authCheck, permissionCheck);

    }
  }

  // historic incident query ////////////////////////////////

  public void configureHistoricIncidentQuery(HistoricIncidentQueryImpl query) {
    AuthorizationCheck authCheck = query.getAuthCheck();

    boolean isHistoricInstancePermissionsEnabled = isHistoricInstancePermissionsEnabled();
    authCheck.setHistoricInstancePermissionsEnabled(isHistoricInstancePermissionsEnabled);

    if (!isHistoricInstancePermissionsEnabled) {
      configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);

    } else {
      configureQuery(query);

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
          .disjunctive()
            .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY)
            .atomicCheck(HISTORIC_PROCESS_INSTANCE, "RES.PROC_INST_ID_",
                HistoricProcessInstancePermissions.READ)
          .build();

      addPermissionCheck(authCheck, permissionCheck);

    }
  }

  //historic identity link query ////////////////////////////////

  public void configureHistoricIdentityLinkQuery(HistoricIdentityLinkLogQueryImpl query) {
    AuthorizationCheck authCheck = query.getAuthCheck();

    boolean isHistoricInstancePermissionsEnabled = isHistoricInstancePermissionsEnabled();
    authCheck.setHistoricInstancePermissionsEnabled(isHistoricInstancePermissionsEnabled);

    if (!isHistoricInstancePermissionsEnabled) {
      configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);

    } else {
      configureQuery(query);

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY)
          .atomicCheck(HISTORIC_PROCESS_INSTANCE, "TI.PROC_INST_ID_",
              HistoricProcessInstancePermissions.READ)
          .atomicCheck(HISTORIC_TASK, "RES.TASK_ID_", HistoricTaskPermissions.READ)
          .build();

      addPermissionCheck(authCheck, permissionCheck);

    }
  }

  public void configureHistoricDecisionInstanceQuery(HistoricDecisionInstanceQueryImpl query) {
    configureQuery(query, DECISION_DEFINITION, "RES.DEC_DEF_KEY_", READ_HISTORY);
  }

  // historic external task log query /////////////////////////////////

  public void configureHistoricExternalTaskLogQuery(HistoricExternalTaskLogQueryImpl query) {
    AuthorizationCheck authCheck = query.getAuthCheck();

    boolean isHistoricInstancePermissionsEnabled = isHistoricInstancePermissionsEnabled();
    authCheck.setHistoricInstancePermissionsEnabled(isHistoricInstancePermissionsEnabled);

    if (!isHistoricInstancePermissionsEnabled) {
      configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);

    } else {
      configureQuery(query);

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
          .disjunctive()
            .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY)
            .atomicCheck(HISTORIC_PROCESS_INSTANCE, "RES.PROC_INST_ID_",
                HistoricProcessInstancePermissions.READ)
          .build();

      addPermissionCheck(authCheck, permissionCheck);

    }
  }

  // user operation log query ///////////////////////////////

  public void configureUserOperationLogQuery(UserOperationLogQueryImpl query) {
    configureQuery(query);
    PermissionCheckBuilder permissionCheckBuilder = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY)
          .atomicCheck(Resources.OPERATION_LOG_CATEGORY, "RES.CATEGORY_", READ);

    AuthorizationCheck authCheck = query.getAuthCheck();

    boolean isHistoricInstancePermissionsEnabled = isHistoricInstancePermissionsEnabled();
    authCheck.setHistoricInstancePermissionsEnabled(isHistoricInstancePermissionsEnabled);

    if (isHistoricInstancePermissionsEnabled) {
      permissionCheckBuilder
          .atomicCheck(HISTORIC_PROCESS_INSTANCE, "RES.PROC_INST_ID_",
              HistoricProcessInstancePermissions.READ)
          .atomicCheck(HISTORIC_TASK, "RES.TASK_ID_",
              HistoricTaskPermissions.READ);
    }

    CompositePermissionCheck permissionCheck = permissionCheckBuilder.build();

    addPermissionCheck(authCheck, permissionCheck);
  }

  // batch

  public void configureHistoricBatchQuery(HistoricBatchQueryImpl query) {
    configureQuery(query, BATCH, "RES.ID_", READ_HISTORY);
  }

  /* STATISTICS QUERY */

  public void configureDeploymentStatisticsQuery(DeploymentStatisticsQueryImpl query) {
    configureQuery(query, DEPLOYMENT, "RES.ID_");

    query.getProcessInstancePermissionChecks().clear();
    query.getJobPermissionChecks().clear();
    query.getIncidentPermissionChecks().clear();

    if(query.getAuthCheck().isAuthorizationCheckEnabled()) {

      CompositePermissionCheck processInstancePermissionCheck = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheck(PROCESS_INSTANCE, "EXECUTION.PROC_INST_ID_", READ)
          .atomicCheck(PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE)
          .build();

      query.addProcessInstancePermissionCheck(processInstancePermissionCheck.getAllPermissionChecks());

      if (query.isFailedJobsToInclude()) {
        CompositePermissionCheck jobPermissionCheck = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheck(PROCESS_INSTANCE, "JOB.PROCESS_INSTANCE_ID_", READ)
            .atomicCheck(PROCESS_DEFINITION, "JOB.PROCESS_DEF_KEY_", READ_INSTANCE)
            .build();

        query.addJobPermissionCheck(jobPermissionCheck.getAllPermissionChecks());
      }

      if (query.isIncidentsToInclude()) {
        CompositePermissionCheck incidentPermissionCheck = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheck(PROCESS_INSTANCE, "INC.PROC_INST_ID_", READ)
            .atomicCheck(PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE)
            .build();

        query.addIncidentPermissionCheck(incidentPermissionCheck.getAllPermissionChecks());

      }
    }
  }

  public void configureProcessDefinitionStatisticsQuery(ProcessDefinitionStatisticsQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.KEY_");
  }

  public void configureActivityStatisticsQuery(ActivityStatisticsQueryImpl query) {
    configureQuery(query);

    query.getProcessInstancePermissionChecks().clear();
    query.getJobPermissionChecks().clear();
    query.getIncidentPermissionChecks().clear();

    if(query.getAuthCheck().isAuthorizationCheckEnabled()) {

      CompositePermissionCheck processInstancePermissionCheck = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheck(PROCESS_INSTANCE, "E.PROC_INST_ID_", READ)
          .atomicCheck(PROCESS_DEFINITION, "P.KEY_", READ_INSTANCE)
          .build();

      // the following is need in order to evaluate whether to perform authCheck or not
      query.getAuthCheck().setPermissionChecks(processInstancePermissionCheck);
      // the actual check
      query.addProcessInstancePermissionCheck(processInstancePermissionCheck.getAllPermissionChecks());

      if (query.isFailedJobsToInclude()) {
        CompositePermissionCheck jobPermissionCheck = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheck(PROCESS_INSTANCE, "JOB.PROCESS_INSTANCE_ID_", READ)
            .atomicCheck(PROCESS_DEFINITION, "JOB.PROCESS_DEF_KEY_", READ_INSTANCE)
            .build();

        // the following is need in order to evaluate whether to perform authCheck or not
        query.getAuthCheck().setPermissionChecks(jobPermissionCheck);
        // the actual check
        query.addJobPermissionCheck(jobPermissionCheck.getAllPermissionChecks());
      }

      if (query.isIncidentsToInclude()) {
        CompositePermissionCheck incidentPermissionCheck = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheck(PROCESS_INSTANCE, "I.PROC_INST_ID_", READ)
            .atomicCheck(PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE)
            .build();

        // the following is need in order to evaluate whether to perform authCheck or not
        query.getAuthCheck().setPermissionChecks(incidentPermissionCheck);
        // the actual check
        query.addIncidentPermissionCheck(incidentPermissionCheck.getAllPermissionChecks());

      }
    }
  }

  public void configureExternalTaskQuery(ExternalTaskQueryImpl query) {
    configureQuery(query);
    CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
        .disjunctive()
        .atomicCheck(PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ)
        .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_INSTANCE)
        .build();
    addPermissionCheck(query.getAuthCheck(), permissionCheck);
  }

  public void configureExternalTaskFetch(ListQueryParameterObject parameter) {
    configureQuery(parameter);

    CompositePermissionCheck permissionCheck = newPermissionCheckBuilder()
      .conjunctive()
      .composite()
        .disjunctive()
        .atomicCheck(PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ)
        .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_INSTANCE)
        .done()
      .composite()
        .disjunctive()
        .atomicCheck(PROCESS_INSTANCE, "RES.PROC_INST_ID_", UPDATE)
        .atomicCheck(PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", UPDATE_INSTANCE)
        .done()
      .build();

    addPermissionCheck(parameter.getAuthCheck(), permissionCheck);
  }

  public void configureDecisionDefinitionQuery(DecisionDefinitionQueryImpl query) {
    configureQuery(query, DECISION_DEFINITION, "RES.KEY_");
  }

  public void configureDecisionRequirementsDefinitionQuery(DecisionRequirementsDefinitionQueryImpl query) {
    configureQuery(query, DECISION_REQUIREMENTS_DEFINITION, "RES.KEY_");
  }

  public void configureBatchQuery(BatchQueryImpl query) {
    configureQuery(query, BATCH, "RES.ID_", READ);
  }

  public void configureBatchStatisticsQuery(BatchStatisticsQueryImpl query) {
    configureQuery(query, BATCH, "RES.ID_", READ);
  }

  public List<String> filterAuthenticatedGroupIds(List<String> authenticatedGroupIds) {
    if(authenticatedGroupIds == null || authenticatedGroupIds.isEmpty()) {
      return EMPTY_LIST;
    }
    else {
      Set<String> groupIntersection = new HashSet<>(getAllGroups());
      groupIntersection.retainAll(authenticatedGroupIds);
      return new ArrayList<>(groupIntersection);
    }
  }

  protected Set<String> getAllGroups() {
    if(availableAuthorizedGroupIds == null) {
      availableAuthorizedGroupIds = new HashSet<String>();
      List<String> groupsFromDatabase = getDbEntityManager().selectList("selectAuthorizedGroupIds");

      groupsFromDatabase.stream()
        .filter(Objects::nonNull)
        .forEach(availableAuthorizedGroupIds::add);
    }

    return availableAuthorizedGroupIds;
  }

  protected boolean isAuthCheckExecuted() {

    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = Context.getCommandContext();

    return isAuthorizationEnabled()
        && commandContext.isAuthorizationCheckEnabled()
        && currentAuthentication != null
        && currentAuthentication.getUserId() != null;

  }

  public boolean isEnsureSpecificVariablePermission() {
    return Context.getProcessEngineConfiguration().isEnforceSpecificVariablePermission();
  }

  protected boolean isHistoricInstancePermissionsEnabled() {
    return Context.getProcessEngineConfiguration().isEnableHistoricInstancePermissions();
  }

  public DbOperation addRemovalTimeToAuthorizationsByRootProcessInstanceId(String rootProcessInstanceId,
                                                                    Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
        .updatePreserveOrder(AuthorizationEntity.class,
            "updateAuthorizationsByRootProcessInstanceId", parameters);
  }

  public DbOperation addRemovalTimeToAuthorizationsByProcessInstanceId(String processInstanceId,
                                                                Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
        .updatePreserveOrder(AuthorizationEntity.class,
            "updateAuthorizationsByProcessInstanceId", parameters);
  }

  public DbOperation deleteAuthorizationsByRemovalTime(Date removalTime, int minuteFrom,
                                                      int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    return getDbEntityManager()
        .deletePreserveOrder(AuthorizationEntity.class, "deleteAuthorizationsByRemovalTime",
            new ListQueryParameterObject(parameters, 0, batchSize));
  }

}
