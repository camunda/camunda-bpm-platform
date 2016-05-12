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
package org.camunda.bpm.engine.impl.persistence.entity;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ_TASK;
import static org.camunda.bpm.engine.authorization.Permissions.TASK_ASSIGN;
import static org.camunda.bpm.engine.authorization.Permissions.TASK_WORK;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.BATCH;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
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
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.db.CompositePermissionCheck;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.PermissionCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheckBuilder;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionQueryImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.CollectionUtil;

/**
 * @author Daniel Meyer
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AuthorizationManager extends AbstractManager {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  // Used instead of Collections.emptyList() as mybatis uses reflection to call methods
  // like size() which can lead to problems as Collections.EmptyList is a private implementation
  protected static final List<String> EMPTY_LIST = new ArrayList<String>();

  /**
   * Group ids for which authorizations exist in the database.
   * This is initialized once per command by the {@link #filterAuthenticatedGroupIds(List)} method. (Manager
   * instances are command scoped).
   * It is used to only check authorizations for groups for which authorizations exist. In other words,
   * if for a given group no authorization exists in the DB, then auth checks are not performed for this group.
   */
  protected Set<String> availableAuthorizedGroupIds = null;

  protected Boolean isRevokeAuthCheckUsed = null;

  public PermissionCheck newPermissionCheck() {
    return new PermissionCheck();
  }

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
    Map<String, Object> params = new HashMap<String, Object>();

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

  public void checkAuthorization(PermissionCheck... permissionChecks) {
    ensureNotNull("permissionChecks", (Object[]) permissionChecks);
    for (PermissionCheck permissionCheck : permissionChecks) {
      ensureNotNull("permissionCheck", permissionCheck);
    }

    checkAuthorization(CollectionUtil.asArrayList(permissionChecks));
  }

  public void checkAuthorization(CompositePermissionCheck compositePermissionCheck) {
    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      String userId = currentAuthentication.getUserId();
      boolean isAuthorized = isAuthorized(compositePermissionCheck);
      if (!isAuthorized) {

        List<MissingAuthorization> missingAuthorizations = new ArrayList<MissingAuthorization>();

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

  public void checkAuthorization(List<PermissionCheck> permissionChecks) {
    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      String userId = currentAuthentication.getUserId();
      boolean isAuthorized = isAuthorized(userId, currentAuthentication.getGroupIds(), permissionChecks);
      if (!isAuthorized) {

        List<MissingAuthorization> missingAuthorizations = new ArrayList<MissingAuthorization>();

        for (PermissionCheck check: permissionChecks) {
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

    final Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = Context.getCommandContext();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

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

    if(isAuthorizationEnabled() && currentAuthentication != null) {
      return isAuthorized(currentAuthentication.getUserId(), currentAuthentication.getGroupIds(), permission, resource, resourceId);

    } else {
      return true;

    }
  }

  public boolean isAuthorized(String userId, List<String> groupIds, Permission permission, Resource resource, String resourceId) {
    PermissionCheck permCheck = newPermissionCheck();
    permCheck.setPermission(permission);
    permCheck.setResource(resource);
    permCheck.setResourceId(resourceId);

    ArrayList<PermissionCheck> permissionChecks = new ArrayList<PermissionCheck>();
    permissionChecks.add(permCheck);

    return isAuthorized(userId, groupIds, permissionChecks);
  }

  public boolean isAuthorized(String userId, List<String> groupIds, List<PermissionCheck> permissionChecks) {
    if(!isAuthorizationEnabled()) {
      return true;
    }

    List<String> filteredGroupIds = filterAuthenticatedGroupIds(groupIds);

    boolean isRevokeAuthorizationCheckEnabled = isRevokeAuthCheckEnabled(userId, groupIds);
    AuthorizationCheck authCheck = new AuthorizationCheck(userId, filteredGroupIds, permissionChecks, isRevokeAuthorizationCheckEnabled);
    return getDbEntityManager().selectBoolean("isUserAuthorizedForResource", authCheck);
  }

  protected boolean isRevokeAuthCheckEnabled(String userId, List<String> groupIds) {
    Boolean isRevokeAuthCheckEnabled = this.isRevokeAuthCheckUsed;

    if(isRevokeAuthCheckEnabled == null) {
      final String configuredMode = Context.getProcessEngineConfiguration().getAuthorizationCheckRevokes();
      if(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_ALWAYS.equals(configuredMode)) {
        isRevokeAuthCheckEnabled = true;
      }
      else if(ProcessEngineConfiguration.AUTHORIZATION_CHECK_REVOKE_NEVER.equals(configuredMode)) {
        isRevokeAuthCheckEnabled = false;
      }
      else {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("authGroupIds", groupIds);
        isRevokeAuthCheckEnabled = getDbEntityManager().selectBoolean("selectRevokeAuthorization", params);
      }
      this.isRevokeAuthCheckUsed = isRevokeAuthCheckEnabled;
    }

    return isRevokeAuthCheckEnabled;
  }

  public boolean isAuthorized(String userId, List<String> groupIds, CompositePermissionCheck compositePermissionCheck) {
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

  // authorization checks on queries ////////////////////////////////

  public void configureQuery(ListQueryParameterObject query) {
    final Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();

    AuthorizationCheck authCheck = query.getAuthCheck();

    authCheck.getPermissionChecks().clear();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      authCheck.setAuthorizationCheckEnabled(true);

      String currentUserId = currentAuthentication.getUserId();
      List<String> currentGroupIds = filterAuthenticatedGroupIds(currentAuthentication.getGroupIds());

      authCheck.setAuthUserId(currentUserId);
      authCheck.setAuthGroupIds(currentGroupIds);
      authCheck.setRevokeAuthorizationCheckEnabled(isRevokeAuthCheckEnabled(currentUserId, currentGroupIds));
    }
    else {
      authCheck.setAuthorizationCheckEnabled(false);
      authCheck.setAuthUserId(null);
      authCheck.setAuthGroupIds(null);
    }
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
    addPermissionCheck(query, resource, queryParam, permission);
  }

  protected void addPermissionCheck(ListQueryParameterObject query, Resource resource, String queryParam, Permission permission) {
    CommandContext commandContext = getCommandContext();
    if (isAuthorizationEnabled() && getCurrentAuthentication() != null && commandContext.isAuthorizationCheckEnabled()) {
      PermissionCheck permCheck = newPermissionCheck();
      permCheck.setResource(resource);
      permCheck.setResourceIdQueryParam(queryParam);
      permCheck.setPermission(permission);

      query.getAuthCheck().addAtomicPermissionCheck(permCheck);
    }
  }

  protected void addPermissionCheck(AuthorizationCheck authCheck, CompositePermissionCheck compositeCheck) {
    CommandContext commandContext = getCommandContext();
    if (isAuthorizationEnabled() && getCurrentAuthentication() != null && commandContext.isAuthorizationCheckEnabled()) {
      authCheck.setPermissionChecks(compositeCheck);
    }
  }

  // delete authorizations //////////////////////////////////////////////////

  public void deleteAuthorizationsByResourceId(Resource resource, String resourceId) {

    if(resourceId == null) {
      throw new IllegalArgumentException("Resource id cannot be null");
    }

    if(isAuthorizationEnabled()) {
      Map<String, Object> deleteParams = new HashMap<String, Object>();
      deleteParams.put("resourceType", resource.resourceType());
      deleteParams.put("resourceId", resourceId);
      getDbEntityManager().delete(AuthorizationEntity.class, "deleteAuthorizationsForResourceId", deleteParams);
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
    CommandContext commandContext = Context.getCommandContext();

    if (isAuthorizationEnabled() && commandContext.isAuthorizationCheckEnabled()
        && currentAuthentication != null  && !isCamundaAdmin(currentAuthentication)) {

      throw LOG.requiredCamundaAdminException();
    }
  }

  /**
   * @param authentication
   *          authentication to check, cannot be <code>null</code>
   * @return <code>true</code> if the given authentication contains the group
   *         {@link Groups#CAMUNDA_ADMIN}
   */
  public boolean isCamundaAdmin(Authentication authentication) {
    List<String> groupIds = authentication.getGroupIds();
    if (groupIds != null) {
      return groupIds.contains(Groups.CAMUNDA_ADMIN);
    } else {
      return false;
    }
  }

  /* DEPLOYMENT */

  /* PROCESS DEFINITION */

  // read permission //////////////////////////////////////////////////

  public void checkReadProcessDefinition(ProcessDefinitionEntity definition) {
    checkAuthorization(READ, PROCESS_DEFINITION, definition.getKey());
  }

  // update permission ///////////////////////////////////////////////

  /* PROCESS INSTANCE */


  // read permission ////////////////////////////////////////////////////

  public void checkReadProcessInstance(JobEntity job) {
    if (job.getProcessDefinitionKey() == null) {
      // "standalone" job: nothing to do!
      return;
    }

    // necessary permissions:
    // - READ on PROCESS_INSTANCE

    PermissionCheck firstCheck = newPermissionCheck();
    firstCheck.setPermission(READ);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(job.getProcessInstanceId());

    // ... OR ...

    // - READ_INSTANCE on PROCESS_DEFINITION
    PermissionCheck secondCheck = newPermissionCheck();
    secondCheck.setPermission(READ_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(job.getProcessDefinitionKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    checkAuthorization(firstCheck, secondCheck);
  }

  public void checkReadHistoricJobLog(HistoricJobLogEventEntity historicJobLog) {
    if (historicJobLog.getProcessDefinitionKey() != null) {
      checkAuthorization(READ_HISTORY, PROCESS_DEFINITION, historicJobLog.getProcessDefinitionKey());
    }
  }

  public void checkReadHistoryAnyProcessDefinition() {
    checkAuthorization(READ_HISTORY, PROCESS_DEFINITION, ANY);
  }

  // update permission //////////////////////////////////////////////////

  public void checkUpdateProcessInstance(JobEntity job) {
    if (job.getProcessDefinitionKey() == null) {
      // "standalone" job: nothing to do!
      return;
    }

    // necessary permissions:
    // - READ on PROCESS_INSTANCE

    PermissionCheck firstCheck = newPermissionCheck();
    firstCheck.setPermission(UPDATE);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(job.getProcessInstanceId());

    // ... OR ...

    // - UPDATE_INSTANCE on PROCESS_DEFINITION
    PermissionCheck secondCheck = newPermissionCheck();
    secondCheck.setPermission(UPDATE_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(job.getProcessDefinitionKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    checkAuthorization(firstCheck, secondCheck);
  }

  /* TASK */

  // create permission /////////////////////////////////////////////

  public void checkCreateTask() {
    checkAuthorization(CREATE, TASK);
  }

  // read permission //////////////////////////////////////////////

  public void checkReadTask(TaskEntity task) {
    String taskId = task.getId();

    String executionId = task.getExecutionId();
    if (executionId != null) {

      // if task exists in context of a process instance
      // then check the following permissions:
      // - READ on TASK
      // - READ_TASK on PROCESS_DEFINITION

      ExecutionEntity execution = task.getExecution();
      ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();


      PermissionCheck readPermissionCheck = newPermissionCheck();
      readPermissionCheck.setPermission(READ);
      readPermissionCheck.setResource(TASK);
      readPermissionCheck.setResourceId(taskId);

      PermissionCheck readTaskPermissionCheck = newPermissionCheck();
      readTaskPermissionCheck.setPermission(READ_TASK);
      readTaskPermissionCheck.setResource(PROCESS_DEFINITION);
      readTaskPermissionCheck.setResourceId(processDefinition.getKey());
      readTaskPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      checkAuthorization(readPermissionCheck, readTaskPermissionCheck);

    } else {

      // if task does not exist in context of process
      // instance, then it is either a (a) standalone task
      // or (b) it exists in context of a case instance.

      // (a) standalone task: check following permission
      // - READ on TASK
      // (b) task in context of a case instance, in this
      // case it is not necessary to check any permission,
      // because such tasks can always be read

      String caseExecutionId = task.getCaseExecutionId();
      if (caseExecutionId == null) {
        checkAuthorization(READ, TASK, taskId);
      }

    }
  }

  // update permission ////////////////////////////////////////////

  public void checkUpdateTask(TaskEntity task) {
    String taskId = task.getId();

    String executionId = task.getExecutionId();
    if (executionId != null) {

      // if task exists in context of a process instance
      // then check the following permissions:
      // - UPDATE on TASK
      // - UPDATE_TASK on PROCESS_DEFINITION

      ExecutionEntity execution = task.getExecution();
      ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();

      PermissionCheck updatePermissionCheck = newPermissionCheck();
      updatePermissionCheck.setPermission(UPDATE);
      updatePermissionCheck.setResource(TASK);
      updatePermissionCheck.setResourceId(taskId);

      PermissionCheck updateTaskPermissionCheck = newPermissionCheck();
      updateTaskPermissionCheck.setPermission(UPDATE_TASK);
      updateTaskPermissionCheck.setResource(PROCESS_DEFINITION);
      updateTaskPermissionCheck.setResourceId(processDefinition.getKey());
      updateTaskPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      checkAuthorization(updatePermissionCheck, updateTaskPermissionCheck);

    } else {

      // if task does not exist in context of process
      // instance, then it is either a (a) standalone task
      // or (b) it exists in context of a case instance.

      // (a) standalone task: check following permission
      // - READ on TASK
      // (b) task in context of a case instance, in this
      // case it is not necessary to check any permission,
      // because such tasks can always be updated

      String caseExecutionId = task.getCaseExecutionId();
      if (caseExecutionId == null) {
        // standalone task
        checkAuthorization(UPDATE, TASK, taskId);
      }

    }
  }

  public void checkTaskWork(TaskEntity task) {

    String taskId = task.getId();

    String executionId = task.getExecutionId();
    if (executionId != null) {

      // Permissions to task actions is based on the order in which PermissioncheckBuilder is built
      CompositePermissionCheck taskWorkPermission = new PermissionCheckBuilder()
          .disjunctive()
          .atomicCheckForResourceId(TASK, taskId, TASK_WORK)
          .atomicCheckForResourceId(PROCESS_DEFINITION, task.getProcessDefinition().getKey(), TASK_WORK)
          .atomicCheckForResourceId(TASK, taskId, UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, task.getProcessDefinition().getKey(), UPDATE_TASK)
        .build();

      checkAuthorization(taskWorkPermission);

    }
    else {

      // if task does not exist in context of process
      // instance, then it is either a (a) standalone task
      // or (b) it exists in context of a case instance.

      // (a) standalone task: check following permission
      // - TASK_WORK or UPDATE
      // (b) task in context of a case instance, in this
      // case it is not necessary to check any permission,
      // because such tasks can always be updated

      String caseExecutionId = task.getCaseExecutionId();
      if (caseExecutionId == null) {
        // standalone task
        CompositePermissionCheck taskWorkPermission = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheckForResourceId(TASK, taskId, TASK_WORK)
            .atomicCheckForResourceId(TASK, taskId, UPDATE)
          .build();

          checkAuthorization(taskWorkPermission);
      }
    }
  }

  public void checkTaskAssign(TaskEntity task) {

    String taskId = task.getId();

    String executionId = task.getExecutionId();
    if (executionId != null) {

      // Permissions to task actions is based on the order in which PermissioncheckBuilder is built
      CompositePermissionCheck taskWorkPermission = new PermissionCheckBuilder()
        .disjunctive()
          .atomicCheckForResourceId(TASK, taskId, TASK_ASSIGN)
          .atomicCheckForResourceId(PROCESS_DEFINITION, task.getProcessDefinition().getKey(), TASK_ASSIGN)
          .atomicCheckForResourceId(TASK, taskId, UPDATE)
          .atomicCheckForResourceId(PROCESS_DEFINITION, task.getProcessDefinition().getKey(), UPDATE_TASK)
        .build();

      checkAuthorization(taskWorkPermission);

    }
    else {

      // if task does not exist in context of process
      // instance, then it is either a (a) standalone task
      // or (b) it exists in context of a case instance.

      // (a) standalone task: check following permission
      // - TASK_ASSIGN or UPDATE
      // (b) task in context of a case instance, in this
      // case it is not necessary to check any permission,
      // because such tasks can always be updated

      String caseExecutionId = task.getCaseExecutionId();
      if (caseExecutionId == null) {
        // standalone task
        CompositePermissionCheck taskWorkPermission = new PermissionCheckBuilder()
            .disjunctive()
            .atomicCheckForResourceId(TASK, taskId, TASK_ASSIGN)
            .atomicCheckForResourceId(TASK, taskId, UPDATE)
          .build();

          checkAuthorization(taskWorkPermission);
      }
    }
  }

  /* USER OPERATION LOG */

  // delete user operation log ///////////////////////////////

  public void checkDeleteUserOperationLog(UserOperationLogEntry entry) {
    if (entry != null) {
      String processDefinitionKey = entry.getProcessDefinitionKey();
      if (processDefinitionKey != null) {
        checkAuthorization(DELETE_HISTORY, PROCESS_DEFINITION, processDefinitionKey);
      }
    }
  }

  /* QUERIES */

  // deployment query ////////////////////////////////////////

  public void configureDeploymentQuery(DeploymentQueryImpl query) {
    configureQuery(query, DEPLOYMENT);
  }

  // process definition query ////////////////////////////////

  public void configureProcessDefinitionQuery(ProcessDefinitionQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.KEY_");
  }

  // execution/process instance query ////////////////////////

  public void configureExecutionQuery(AbstractQuery query) {
    configureQuery(query);
    addPermissionCheck(query, PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ);
    addPermissionCheck(query, PROCESS_DEFINITION, "P.KEY_", READ_INSTANCE);
  }

  // task query //////////////////////////////////////////////

  public void configureTaskQuery(TaskQueryImpl query) {
    query.getAuthCheck().getPermissionChecks().clear();

    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();
    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      // necessary authorization check when the task is part of
      // a running process instance
      configureQuery(query);


      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
              .disjunctive()
              .atomicCheck(TASK, "RES.ID_", READ)
              .atomicCheck(PROCESS_DEFINITION, "PROCDEF.KEY_", READ_TASK)
              .build();
        addPermissionCheck(query.getAuthCheck(), permissionCheck);
    }
  }

  // event subscription query //////////////////////////////

  public void configureEventSubscriptionQuery(EventSubscriptionQueryImpl query) {
    configureQuery(query);
    addPermissionCheck(query, PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ);
    addPermissionCheck(query, PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE);
  }

  // incident query ///////////////////////////////////////

  public void configureIncidentQuery(IncidentQueryImpl query) {
    configureQuery(query);
    addPermissionCheck(query, PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ);
    addPermissionCheck(query, PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE);
  }

  // variable instance query /////////////////////////////

  protected void configureVariableInstanceQuery(VariableInstanceQueryImpl query) {
    query.getAuthCheck().getPermissionChecks().clear();

    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();
    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      configureQuery(query);

      CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
              .disjunctive()
              .atomicCheck(PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ)
              .atomicCheck(PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE)
              .atomicCheck(TASK, "RES.TASK_ID_", READ)
              .build();
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
    addPermissionCheck(query, PROCESS_INSTANCE, "RES.PROCESS_INSTANCE_ID_", READ);
    addPermissionCheck(query, PROCESS_DEFINITION, "RES.PROCESS_DEF_KEY_", READ_INSTANCE);
  }

  /* HISTORY */

  // historic process instance query ///////////////////////////////////

  public void configureHistoricProcessInstanceQuery(HistoricProcessInstanceQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic activity instance query /////////////////////////////////

  public void configureHistoricActivityInstanceQuery(HistoricActivityInstanceQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic task instance query ////////////////////////////////////

  public void configureHistoricTaskInstanceQuery(HistoricTaskInstanceQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic variable instance query ////////////////////////////////

  public void configureHistoricVariableInstanceQuery(HistoricVariableInstanceQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic detail query ////////////////////////////////

  public void configureHistoricDetailQuery(HistoricDetailQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic job log query ////////////////////////////////

  public void configureHistoricJobLogQuery(HistoricJobLogQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "SELF.PROCESS_DEF_KEY_", READ_HISTORY);
  }

  // historic incident query ////////////////////////////////

  public void configureHistoricIncidentQuery(HistoricIncidentQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY);
  }

  //historic identity link query ////////////////////////////////

  public void configureHistoricIdentityLinkQuery(HistoricIdentityLinkLogQueryImpl query) {
   configureQuery(query, PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY);
  }

  public void configureHistoricDecisionInstanceQuery(HistoricDecisionInstanceQueryImpl query) {
    configureQuery(query, DECISION_DEFINITION, "SELF.DEC_DEF_KEY_", READ_HISTORY);
  }

  // user operation log query ///////////////////////////////

  public void configureUserOperationLogQuery(UserOperationLogQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "SELF.PROC_DEF_KEY_", READ_HISTORY);
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

    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      PermissionCheck firstProcessInstancePermissionCheck = newPermissionCheck();
      firstProcessInstancePermissionCheck.setResource(PROCESS_INSTANCE);
      firstProcessInstancePermissionCheck.setPermission(READ);
      firstProcessInstancePermissionCheck.setResourceIdQueryParam("EXECUTION.PROC_INST_ID_");

      PermissionCheck secondProcessInstancePermissionCheck = newPermissionCheck();
      secondProcessInstancePermissionCheck.setResource(PROCESS_DEFINITION);
      secondProcessInstancePermissionCheck.setPermission(READ_INSTANCE);
      secondProcessInstancePermissionCheck.setResourceIdQueryParam("PROCDEF.KEY_");
      secondProcessInstancePermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      query.addProcessInstancePermissionCheck(firstProcessInstancePermissionCheck);
      query.addProcessInstancePermissionCheck(secondProcessInstancePermissionCheck);

      if (query.isFailedJobsToInclude()) {
        PermissionCheck firstJobPermissionCheck = newPermissionCheck();
        firstJobPermissionCheck.setResource(PROCESS_INSTANCE);
        firstJobPermissionCheck.setPermission(READ);
        firstJobPermissionCheck.setResourceIdQueryParam("JOB.PROCESS_INSTANCE_ID_");

        PermissionCheck secondJobPermissionCheck = newPermissionCheck();
        secondJobPermissionCheck.setResource(PROCESS_DEFINITION);
        secondJobPermissionCheck.setPermission(READ_INSTANCE);
        secondJobPermissionCheck.setResourceIdQueryParam("JOB.PROCESS_DEF_KEY_");
        secondJobPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

        query.addJobPermissionCheck(firstJobPermissionCheck);
        query.addJobPermissionCheck(secondJobPermissionCheck);
      }

      if (query.isIncidentsToInclude()) {
        PermissionCheck firstIncidentPermissionCheck = newPermissionCheck();
        firstIncidentPermissionCheck.setResource(PROCESS_INSTANCE);
        firstIncidentPermissionCheck.setPermission(READ);
        firstIncidentPermissionCheck.setResourceIdQueryParam("INC.PROC_INST_ID_");

        PermissionCheck secondIncidentPermissionCheck = newPermissionCheck();
        secondIncidentPermissionCheck.setResource(PROCESS_DEFINITION);
        secondIncidentPermissionCheck.setPermission(READ_INSTANCE);
        secondIncidentPermissionCheck.setResourceIdQueryParam("PROCDEF.KEY_");
        secondIncidentPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

        query.addIncidentPermissionCheck(firstIncidentPermissionCheck);
        query.addIncidentPermissionCheck(secondIncidentPermissionCheck);

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

    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      PermissionCheck firstProcessInstancePermissionCheck = newPermissionCheck();
      firstProcessInstancePermissionCheck.setResource(PROCESS_INSTANCE);
      firstProcessInstancePermissionCheck.setPermission(READ);
      firstProcessInstancePermissionCheck.setResourceIdQueryParam("E.PROC_INST_ID_");

      PermissionCheck secondProcessInstancePermissionCheck = newPermissionCheck();
      secondProcessInstancePermissionCheck.setResource(PROCESS_DEFINITION);
      secondProcessInstancePermissionCheck.setPermission(READ_INSTANCE);
      secondProcessInstancePermissionCheck.setResourceIdQueryParam("P.KEY_");
      secondProcessInstancePermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      query.addProcessInstancePermissionCheck(firstProcessInstancePermissionCheck);
      query.addProcessInstancePermissionCheck(secondProcessInstancePermissionCheck);

      if (query.isFailedJobsToInclude()) {
        PermissionCheck firstJobPermissionCheck = newPermissionCheck();
        firstJobPermissionCheck.setResource(PROCESS_INSTANCE);
        firstJobPermissionCheck.setPermission(READ);
        firstJobPermissionCheck.setResourceIdQueryParam("JOB.PROCESS_INSTANCE_ID_");

        PermissionCheck secondJobPermissionCheck = newPermissionCheck();
        secondJobPermissionCheck.setResource(PROCESS_DEFINITION);
        secondJobPermissionCheck.setPermission(READ_INSTANCE);
        secondJobPermissionCheck.setResourceIdQueryParam("JOB.PROCESS_DEF_KEY_");
        secondJobPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

        query.addJobPermissionCheck(firstJobPermissionCheck);
        query.addJobPermissionCheck(secondJobPermissionCheck);
      }

      if (query.isIncidentsToInclude()) {
        PermissionCheck firstIncidentPermissionCheck = newPermissionCheck();
        firstIncidentPermissionCheck.setResource(PROCESS_INSTANCE);
        firstIncidentPermissionCheck.setPermission(READ);
        firstIncidentPermissionCheck.setResourceIdQueryParam("I.PROC_INST_ID_");

        PermissionCheck secondIncidentPermissionCheck = newPermissionCheck();
        secondIncidentPermissionCheck.setResource(PROCESS_DEFINITION);
        secondIncidentPermissionCheck.setPermission(READ_INSTANCE);
        secondIncidentPermissionCheck.setResourceIdQueryParam("PROCDEF.KEY_");
        secondIncidentPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

        query.addIncidentPermissionCheck(firstIncidentPermissionCheck);
        query.addIncidentPermissionCheck(secondIncidentPermissionCheck);

      }
    }
  }

  public void configureExternalTaskQuery(ExternalTaskQueryImpl query) {
    configureQuery(query);
    addPermissionCheck(query, PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ);
    addPermissionCheck(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_INSTANCE);
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

  public void configureBatchQuery(BatchQueryImpl query) {
    configureQuery(query);
    addPermissionCheck(query, BATCH, "RES.ID_", READ);
  }

  public void configureBatchStatisticsQuery(BatchStatisticsQueryImpl query) {
    configureQuery(query);
    addPermissionCheck(query, BATCH, "RES.ID_", READ);
  }

  public List<String> filterAuthenticatedGroupIds(List<String> authenticatedGroupIds) {
    if(authenticatedGroupIds == null || authenticatedGroupIds.isEmpty()) {
      return EMPTY_LIST;
    }
    else {
      if(availableAuthorizedGroupIds == null) {
        availableAuthorizedGroupIds = new HashSet<String>(getDbEntityManager().selectList("selectAuthorizedGroupIds"));
      }
      Set<String> copy = new HashSet<String>(availableAuthorizedGroupIds);
      copy.retainAll(authenticatedGroupIds);
      return new ArrayList<String>(copy);
    }
  }

}
