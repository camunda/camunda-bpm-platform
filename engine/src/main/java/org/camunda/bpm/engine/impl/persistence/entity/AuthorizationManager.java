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
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ_TASK;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_TASK;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
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
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.db.CompositePermissionCheck;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.PermissionCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheckBuilder;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
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
  public static final String DEFAULT_AUTHORIZATION_CHECK = "defaultAuthorizationCheck";

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

  public void checkAuthorization(List<PermissionCheck> permissionChecks) {
    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      String userId = currentAuthentication.getUserId();
      boolean isAuthorized = isAuthorized(userId, currentAuthentication.getGroupIds(), permissionChecks);
      if (!isAuthorized) {

        List<MissingAuthorization> info = new ArrayList<MissingAuthorization>();

        for (PermissionCheck check: permissionChecks) {
          info.add(new MissingAuthorization(
              check.getPermission().getName(),
              check.getResource().resourceName(),
              check.getResourceId()));
        }

        throw new AuthorizationException(userId, info);
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
    PermissionCheck permCheck = new PermissionCheck();
    permCheck.setPermission(permission);
    permCheck.setResource(resource);
    permCheck.setResourceId(resourceId);

    ArrayList<PermissionCheck> permissionChecks = new ArrayList<PermissionCheck>();
    permissionChecks.add(permCheck);

    return isAuthorized(userId, groupIds, permissionChecks);
  }

  public boolean isAuthorized(String userId, List<String> groupIds, List<PermissionCheck> permissionChecks) {
    AuthorizationCheck authCheck = new AuthorizationCheck();
    authCheck.setAuthUserId(userId);
    authCheck.setAuthGroupIds(groupIds);
    authCheck.setAtomicPermissionChecks(permissionChecks);
    return getDbEntityManager().selectBoolean("isUserAuthorizedForResource", authCheck);
  }

  // authorization checks on queries ////////////////////////////////

  public void configureQuery(ListQueryParameterObject query) {
    final Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();

    query.getPermissionChecks().clear();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      query.setAuthorizationCheckEnabled(true);

      String currentUserId = currentAuthentication.getUserId();
      List<String> currentGroupIds = currentAuthentication.getGroupIds();

      query.setAuthUserId(currentUserId);
      query.setAuthGroupIds(currentGroupIds);
    }
    else {
      query.setAuthorizationCheckEnabled(false);
      query.setAuthUserId(null);
      query.setAuthGroupIds(null);
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
      PermissionCheck permCheck = new PermissionCheck();
      permCheck.setResource(resource);
      permCheck.setResourceIdQueryParam(queryParam);
      permCheck.setPermission(permission);

      query.addAtomicPermissionCheck(permCheck);
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

  public void isCamundaAdmin() {
    final Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = Context.getCommandContext();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();

      String userId = currentAuthentication.getUserId();
      long count = identityService
          .createUserQuery()
          .userId(userId)
          .memberOfGroup(Groups.CAMUNDA_ADMIN)
          .count();
      if (count == 0) {
        throw LOG.notAMemberException(userId, Groups.CAMUNDA_ADMIN);
      }
    }
  }

  /* DEPLOYMENT */

  // create permission ////////////////////////////////////////////////

  public void checkCreateDeployment() {
    checkAuthorization(CREATE, DEPLOYMENT);
  }

  // read permission //////////////////////////////////////////////////

  public void checkReadDeployment(String deploymentId) {
    checkAuthorization(READ, DEPLOYMENT, deploymentId);
  }

  // delete permission ///////////////////////////////////////////////

  public void checkDeleteDeployment(String deploymentId) {
    checkAuthorization(DELETE, DEPLOYMENT, deploymentId);
  }

  /* PROCESS DEFINITION */

  // read permission //////////////////////////////////////////////////

  public void checkReadProcessDefinition(ProcessDefinitionEntity definition) {
    checkReadProcessDefinition(definition.getKey());
  }

  public void checkReadProcessDefinition(String processDefinitionKey) {
    checkAuthorization(READ, PROCESS_DEFINITION, processDefinitionKey);
  }

  // update permission ///////////////////////////////////////////////

  public void checkUpdateProcessDefinitionById(String processDefinitionId) {
    ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
    String processDefinitionKey = definition.getKey();
    checkUpdateProcessDefinitionByKey(processDefinitionKey);
  }

  public void checkUpdateProcessDefinitionByKey(String processDefinitionKey) {
    checkAuthorization(UPDATE, PROCESS_DEFINITION, processDefinitionKey);
  }

  /* PROCESS INSTANCE */

  // create permission ///////////////////////////////////////////////////

  public void checkCreateProcessInstance(ProcessDefinitionEntity definition) {
    // necessary permissions:
    // - CREATE on PROCESS_INSTANCE
    // AND
    // - CREATE_INSTANCE on PROCESS_DEFINITION
    checkAuthorization(CREATE, PROCESS_INSTANCE);
    checkAuthorization(CREATE_INSTANCE, PROCESS_DEFINITION, definition.getKey());
  }

  // read permission ////////////////////////////////////////////////////

  public void checkReadProcessInstance(String processInstanceId) {
    ExecutionEntity execution = getProcessInstanceManager().findExecutionById(processInstanceId);
    if (execution != null) {
      checkReadProcessInstance(execution);
    }
  }

  public void checkReadProcessInstance(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();

    // necessary permissions:
    // - READ on PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(READ);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(execution.getProcessInstanceId());

    // ... OR ...

    // - READ_INSTANCE on PROCESS_DEFINITION
    PermissionCheck secondCheck = new PermissionCheck();
    secondCheck.setPermission(READ_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(processDefinition.getKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    checkAuthorization(firstCheck, secondCheck);
  }

  public void checkReadProcessInstance(JobEntity job) {
    if (job.getProcessDefinitionKey() == null) {
      // "standalone" job: nothing to do!
      return;
    }

    // necessary permissions:
    // - READ on PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(READ);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(job.getProcessInstanceId());

    // ... OR ...

    // - READ_INSTANCE on PROCESS_DEFINITION
    PermissionCheck secondCheck = new PermissionCheck();
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

  public void checkUpdateProcessInstanceById(String processInstanceId) {
    ExecutionEntity execution = getProcessInstanceManager().findExecutionById(processInstanceId);
    if (execution != null) {
      checkUpdateProcessInstance(execution);
    }
  }

  public void checkUpdateProcessInstance(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();

    // necessary permissions:
    // - UPDATE on PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(UPDATE);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(execution.getProcessInstanceId());

    // ... OR ...

    // - UPDATE_INSTANCE on PROCESS_DEFINITION

    PermissionCheck secondCheck = new PermissionCheck();
    secondCheck.setPermission(UPDATE_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(processDefinition.getKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    checkAuthorization(firstCheck, secondCheck);
  }

  public void checkUpdateProcessInstance(JobEntity job) {
    if (job.getProcessDefinitionKey() == null) {
      // "standalone" job: nothing to do!
      return;
    }

    // necessary permissions:
    // - READ on PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(UPDATE);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(job.getProcessInstanceId());

    // ... OR ...

    // - UPDATE_INSTANCE on PROCESS_DEFINITION
    PermissionCheck secondCheck = new PermissionCheck();
    secondCheck.setPermission(UPDATE_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(job.getProcessDefinitionKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    checkAuthorization(firstCheck, secondCheck);
  }

  public void checkUpdateProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
    if (definition != null) {
      String processDefinitionKey = definition.getKey();
      checkUpdateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
    }
  }

  public void checkUpdateProcessInstanceByProcessDefinitionKey(String processDefinitionKey) {
    // necessary permissions:
    // - UPDATE on ANY PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(UPDATE);
    firstCheck.setResource(PROCESS_INSTANCE);

    // ... OR ...

    // - UPDATE_INSTANCE on PROCESS_DEFINITION

    PermissionCheck secondCheck = new PermissionCheck();
    secondCheck.setPermission(UPDATE_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(processDefinitionKey);
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    checkAuthorization(firstCheck, secondCheck);
  }

  // delete permission /////////////////////////////////////////////////

  public void checkDeleteProcessInstance(ExecutionEntity execution) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();

    // necessary permissions:
    // - DELETE on PROCESS_INSTANCE

    PermissionCheck firstCheck = new PermissionCheck();
    firstCheck.setPermission(DELETE);
    firstCheck.setResource(PROCESS_INSTANCE);
    firstCheck.setResourceId(execution.getProcessInstanceId());

    // ... OR ...

    // - DELETE_INSTANCE on PROCESS_DEFINITION

    PermissionCheck secondCheck = new PermissionCheck();
    secondCheck.setPermission(DELETE_INSTANCE);
    secondCheck.setResource(PROCESS_DEFINITION);
    secondCheck.setResourceId(processDefinition.getKey());
    secondCheck.setAuthorizationNotFoundReturnValue(0l);

    checkAuthorization(firstCheck, secondCheck);
  }

  public void checkDeleteHistoricProcessInstance(HistoricProcessInstance instance) {
    checkAuthorization(DELETE_HISTORY, PROCESS_DEFINITION, instance.getProcessDefinitionKey());
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
      ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();

      PermissionCheck readPermissionCheck = new PermissionCheck();
      readPermissionCheck.setPermission(READ);
      readPermissionCheck.setResource(TASK);
      readPermissionCheck.setResourceId(taskId);

      PermissionCheck readTaskPermissionCheck = new PermissionCheck();
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
      ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();

      PermissionCheck updatePermissionCheck = new PermissionCheck();
      updatePermissionCheck.setPermission(UPDATE);
      updatePermissionCheck.setResource(TASK);
      updatePermissionCheck.setResourceId(taskId);

      PermissionCheck updateTaskPermissionCheck = new PermissionCheck();
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
        checkAuthorization(UPDATE, TASK, taskId);
      }

    }
  }

  // delete permission ////////////////////////////////////////

  public void checkDeleteTask(TaskEntity task) {
    String taskId = task.getId();

    // Note: Calling TaskService#deleteTask() to
    // delete a task which exists in context of
    // a process instance or case instance cannot
    // be deleted. In such a case TaskService#deleteTask()
    // throws an exception before invoking the
    // authorization check.

    String executionId = task.getExecutionId();
    String caseExecutionId = task.getCaseExecutionId();

    if (executionId == null && caseExecutionId == null) {
      checkAuthorization(DELETE, TASK, taskId);
    }
  }

  public void checkDeleteHistoricTaskInstance(HistoricTaskInstanceEntity task) {
    if (task != null) {
      if (task.getExecutionId() != null) {
        checkAuthorization(DELETE_HISTORY, PROCESS_DEFINITION, task.getProcessDefinitionKey());
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

  public void checkDeleteHistoricDecisionInstance(String decisionDefinitionKey) {
    checkAuthorization(DELETE_HISTORY, DECISION_DEFINITION, decisionDefinitionKey);
  }

  public void checkEvaluateDecision(String decisionDefinitionKey) {
    checkAuthorization(CREATE_INSTANCE, DECISION_DEFINITION, decisionDefinitionKey);
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
    query.getPermissionChecks().clear();
    query.getTaskPermissionChecks().clear();

    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();
    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      // necessary authorization check when the task is part of
      // a running process instance
      configureQuery(query);
      addPermissionCheck(query, TASK, "RES.ID_", READ);
      addPermissionCheck(query, PROCESS_DEFINITION, "PROCDEF.KEY_", READ_TASK);

      // necessary authorization check when the task is not part
      // of running process or case instance
      PermissionCheck standaloneTaskPermissionCheck = new PermissionCheck();
      standaloneTaskPermissionCheck.setPermission(READ);
      standaloneTaskPermissionCheck.setResource(TASK);
      standaloneTaskPermissionCheck.setResourceIdQueryParam("RES.ID_");
      standaloneTaskPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      query.addTaskPermissionCheck(standaloneTaskPermissionCheck);
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
    query.getPermissionChecks().clear();
    query.getTaskPermissionChecks().clear();

    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();
    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      // necessary authorization check when the variable instance is part of
      // a running process instance
      configureQuery(query);
      addPermissionCheck(query, PROCESS_INSTANCE, "RES.PROC_INST_ID_", READ);
      addPermissionCheck(query, PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE);

      // necessary authorization check when the variable instance is part
      // of a standalone task
      PermissionCheck taskPermissionCheck = new PermissionCheck();
      taskPermissionCheck.setResource(TASK);
      taskPermissionCheck.setPermission(READ);
      taskPermissionCheck.setResourceIdQueryParam("RES.TASK_ID_");
      taskPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      query.addTaskPermissionCheck(taskPermissionCheck);
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
    configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic activity instance query /////////////////////////////////

  public void configureHistoricActivityInstanceQuery(HistoricActivityInstanceQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic task instance query ////////////////////////////////////

  public void configureHistoricTaskInstanceQuery(HistoricTaskInstanceQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic variable instance query ////////////////////////////////

  public void configureHistoricVariableInstanceQuery(HistoricVariableInstanceQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic detail query ////////////////////////////////

  public void configureHistoricDetailQuery(HistoricDetailQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);
  }

  // historic job log query ////////////////////////////////

  public void configureHistoricJobLogQuery(HistoricJobLogQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.PROCESS_DEF_KEY_", READ_HISTORY);
  }

  // historic incident query ////////////////////////////////

  public void configureHistoricIncidentQuery(HistoricIncidentQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);
  }

  public void configureHistoricDecisionInstanceQuery(HistoricDecisionInstanceQueryImpl query) {
    configureQuery(query, DECISION_DEFINITION, "DEC_DEF_KEY_", READ_HISTORY);
  }

  // user operation log query ///////////////////////////////

  public void configureUserOperationLogQuery(UserOperationLogQueryImpl query) {
    configureQuery(query, PROCESS_DEFINITION, "RES.PROC_DEF_KEY_", READ_HISTORY);
  }

  /* STATISTICS QUERY */

  public void configureDeploymentStatisticsQuery(DeploymentStatisticsQueryImpl query) {
    configureQuery(query, DEPLOYMENT, "DEPLOYMENT.ID_");

    query.getProcessInstancePermissionChecks().clear();
    query.getJobPermissionChecks().clear();
    query.getIncidentPermissionChecks().clear();

    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      PermissionCheck firstProcessInstancePermissionCheck = new PermissionCheck();
      firstProcessInstancePermissionCheck.setResource(PROCESS_INSTANCE);
      firstProcessInstancePermissionCheck.setPermission(READ);
      firstProcessInstancePermissionCheck.setResourceIdQueryParam("EXECUTION.PROC_INST_ID_");

      PermissionCheck secondProcessInstancePermissionCheck = new PermissionCheck();
      secondProcessInstancePermissionCheck.setResource(PROCESS_DEFINITION);
      secondProcessInstancePermissionCheck.setPermission(READ_INSTANCE);
      secondProcessInstancePermissionCheck.setResourceIdQueryParam("PROCDEF.KEY_");
      secondProcessInstancePermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      query.addProcessInstancePermissionCheck(firstProcessInstancePermissionCheck);
      query.addProcessInstancePermissionCheck(secondProcessInstancePermissionCheck);

      if (query.isFailedJobsToInclude()) {
        PermissionCheck firstJobPermissionCheck = new PermissionCheck();
        firstJobPermissionCheck.setResource(PROCESS_INSTANCE);
        firstJobPermissionCheck.setPermission(READ);
        firstJobPermissionCheck.setResourceIdQueryParam("JOB.PROCESS_INSTANCE_ID_");

        PermissionCheck secondJobPermissionCheck = new PermissionCheck();
        secondJobPermissionCheck.setResource(PROCESS_DEFINITION);
        secondJobPermissionCheck.setPermission(READ_INSTANCE);
        secondJobPermissionCheck.setResourceIdQueryParam("JOB.PROCESS_DEF_KEY_");
        secondJobPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

        query.addJobPermissionCheck(firstJobPermissionCheck);
        query.addJobPermissionCheck(secondJobPermissionCheck);
      }

      if (query.isIncidentsToInclude()) {
        PermissionCheck firstIncidentPermissionCheck = new PermissionCheck();
        firstIncidentPermissionCheck.setResource(PROCESS_INSTANCE);
        firstIncidentPermissionCheck.setPermission(READ);
        firstIncidentPermissionCheck.setResourceIdQueryParam("INC.PROC_INST_ID_");

        PermissionCheck secondIncidentPermissionCheck = new PermissionCheck();
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
    configureQuery(query, PROCESS_DEFINITION, "PROCDEF.KEY_");
  }

  public void configureActivityStatisticsQuery(ActivityStatisticsQueryImpl query) {
    configureQuery(query);

    query.getProcessInstancePermissionChecks().clear();
    query.getJobPermissionChecks().clear();
    query.getIncidentPermissionChecks().clear();

    Authentication currentAuthentication = getCurrentAuthentication();
    CommandContext commandContext = getCommandContext();

    if(isAuthorizationEnabled() && currentAuthentication != null && commandContext.isAuthorizationCheckEnabled()) {

      PermissionCheck firstProcessInstancePermissionCheck = new PermissionCheck();
      firstProcessInstancePermissionCheck.setResource(PROCESS_INSTANCE);
      firstProcessInstancePermissionCheck.setPermission(READ);
      firstProcessInstancePermissionCheck.setResourceIdQueryParam("E.PROC_INST_ID_");

      PermissionCheck secondProcessInstancePermissionCheck = new PermissionCheck();
      secondProcessInstancePermissionCheck.setResource(PROCESS_DEFINITION);
      secondProcessInstancePermissionCheck.setPermission(READ_INSTANCE);
      secondProcessInstancePermissionCheck.setResourceIdQueryParam("P.KEY_");
      secondProcessInstancePermissionCheck.setAuthorizationNotFoundReturnValue(0l);

      query.addProcessInstancePermissionCheck(firstProcessInstancePermissionCheck);
      query.addProcessInstancePermissionCheck(secondProcessInstancePermissionCheck);

      if (query.isFailedJobsToInclude()) {
        PermissionCheck firstJobPermissionCheck = new PermissionCheck();
        firstJobPermissionCheck.setResource(PROCESS_INSTANCE);
        firstJobPermissionCheck.setPermission(READ);
        firstJobPermissionCheck.setResourceIdQueryParam("JOB.PROCESS_INSTANCE_ID_");

        PermissionCheck secondJobPermissionCheck = new PermissionCheck();
        secondJobPermissionCheck.setResource(PROCESS_DEFINITION);
        secondJobPermissionCheck.setPermission(READ_INSTANCE);
        secondJobPermissionCheck.setResourceIdQueryParam("JOB.PROCESS_DEF_KEY_");
        secondJobPermissionCheck.setAuthorizationNotFoundReturnValue(0l);

        query.addJobPermissionCheck(firstJobPermissionCheck);
        query.addJobPermissionCheck(secondJobPermissionCheck);
      }

      if (query.isIncidentsToInclude()) {
        PermissionCheck firstIncidentPermissionCheck = new PermissionCheck();
        firstIncidentPermissionCheck.setResource(PROCESS_INSTANCE);
        firstIncidentPermissionCheck.setPermission(READ);
        firstIncidentPermissionCheck.setResourceIdQueryParam("I.PROC_INST_ID_");

        PermissionCheck secondIncidentPermissionCheck = new PermissionCheck();
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

    CompositePermissionCheck permissionCheck = new PermissionCheckBuilder()
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

    addPermissionCheck(parameter, permissionCheck);
  }

  public void configureDecisionDefinitionQuery(DecisionDefinitionQueryImpl query) {
    configureQuery(query, DECISION_DEFINITION, "RES.KEY_");
  }

  public void checkReadDecisionDefinition(DecisionDefinitionEntity decisionDefinition) {
    checkReadDecisionDefinition(decisionDefinition.getKey());
  }

  public void checkReadDecisionDefinition(String decisionDefinitionKey) {
    checkAuthorization(READ, DECISION_DEFINITION, decisionDefinitionKey);
  }

}
