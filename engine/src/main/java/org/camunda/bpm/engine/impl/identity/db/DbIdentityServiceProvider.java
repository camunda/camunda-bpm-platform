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
package org.camunda.bpm.engine.impl.identity.db;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.IdentityOperationResult;
import org.camunda.bpm.engine.impl.identity.IndentityLogger;
import org.camunda.bpm.engine.impl.identity.WritableIdentityProvider;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MembershipEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TenantEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TenantMembershipEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * <p>{@link WritableIdentityProvider} implementation backed by a
 * database. This implementation is used for the built-in user management.</p>
 *
 * @author Daniel Meyer
 *
 */
public class DbIdentityServiceProvider extends DbReadOnlyIdentityServiceProvider implements WritableIdentityProvider {

  protected static final IndentityLogger LOG = ProcessEngineLogger.INDENTITY_LOGGER;

  // users ////////////////////////////////////////////////////////

  @Override
  public UserEntity createNewUser(String userId) {
    return new UserEntity(userId);
  }

  @Override
  public IdentityOperationResult saveUser(User user) {
    UserEntity userEntity = (UserEntity) user;

    // encrypt password
    userEntity.encryptPassword();

    String operation = null;
    if(userEntity.getRevision() == 0) {
      operation = IdentityOperationResult.OPERATION_CREATE;
      checkAuthorization(Permissions.CREATE, Resources.USER, null);
      getDbEntityManager().insert(userEntity);
      createDefaultAuthorizations(userEntity);
    } else {
      operation = IdentityOperationResult.OPERATION_UPDATE;
      checkAuthorization(Permissions.UPDATE, Resources.USER, user.getId());
      getDbEntityManager().merge(userEntity);
    }

    return new IdentityOperationResult(userEntity, operation);
  }

  @Override
  public IdentityOperationResult deleteUser(final String userId) {
    checkAuthorization(Permissions.DELETE, Resources.USER, userId);
    UserEntity user = findUserById(userId);
    if(user != null) {
      deleteMembershipsByUserId(userId);
      deleteTenantMembershipsOfUser(userId);

      deleteAuthorizations(Resources.USER, userId);

      Context.getCommandContext().runWithoutAuthorization(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          final List<Tenant> tenants = createTenantQuery().userMember(userId).list();
          if (tenants != null && !tenants.isEmpty()) {
            for (Tenant tenant : tenants) {
              deleteAuthorizationsForUser(Resources.TENANT, tenant.getId(), userId);
            }
          }
          return null;
        }
      });

      getDbEntityManager().delete(user);
      return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_DELETE);
    }
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_NONE);
  }

  @Override
  public boolean checkPassword(String userId, String password) {
    UserEntity user = findUserById(userId);
    if (user == null || password == null) {
      return false;
    }

    if (isUserLocked(user)) {
      return false;
    }

    if (matchPassword(password, user)) {
      unlockUser(user);
      return true;
    }
    else {
      lockUser(user);
      return false;
    }
  }

  protected boolean isUserLocked(UserEntity user) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    int maxAttempts = processEngineConfiguration.getLoginMaxAttempts();
    int attempts = user.getAttempts();

    if (attempts >= maxAttempts) {
      return true;
    }

    Date lockExpirationTime = user.getLockExpirationTime();
    Date currentTime = ClockUtil.getCurrentTime();

    return lockExpirationTime != null && lockExpirationTime.after(currentTime);
  }

  protected void lockUser(UserEntity user) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    int max = processEngineConfiguration.getLoginDelayMaxTime();
    int baseTime = processEngineConfiguration.getLoginDelayBase();
    int factor = processEngineConfiguration.getLoginDelayFactor();
    int attempts = user.getAttempts() + 1;

    long delay = (long) (baseTime * Math.pow(factor, attempts - 1));
    delay = Math.min(delay, max) * 1000;

    long currentTime = ClockUtil.getCurrentTime().getTime();
    Date lockExpirationTime = new Date(currentTime + delay);

    if(attempts >= processEngineConfiguration.getLoginMaxAttempts()) {
      LOG.infoUserPermanentlyLocked(user.getId());
    } else {
      LOG.infoUserTemporarilyLocked(user.getId(), lockExpirationTime);
    }

    getIdentityInfoManager().updateUserLock(user, attempts, lockExpirationTime);
  }

  @Override
  public IdentityOperationResult unlockUser(String userId) {
    UserEntity user = findUserById(userId);
    if(user != null) {
      return unlockUser(user);
    }
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_NONE);
  }

  protected IdentityOperationResult unlockUser(UserEntity user) {
    if (user.getAttempts() > 0 || user.getLockExpirationTime() != null) {
      getIdentityInfoManager().updateUserLock(user, 0, null);
      return new IdentityOperationResult(user, IdentityOperationResult.OPERATION_UNLOCK);
    }
    return new IdentityOperationResult(user, IdentityOperationResult.OPERATION_NONE);
  }

  // groups ////////////////////////////////////////////////////////

  @Override
  public GroupEntity createNewGroup(String groupId) {
    return new GroupEntity(groupId);
  }

  @Override
  public IdentityOperationResult saveGroup(Group group) {
    GroupEntity groupEntity = (GroupEntity) group;
    String operation = null;
    if(groupEntity.getRevision() == 0) {
      operation = IdentityOperationResult.OPERATION_CREATE;
      checkAuthorization(Permissions.CREATE, Resources.GROUP, null);
      getDbEntityManager().insert(groupEntity);
      createDefaultAuthorizations(group);
    } else {
      operation = IdentityOperationResult.OPERATION_UPDATE;
      checkAuthorization(Permissions.UPDATE, Resources.GROUP, group.getId());
      getDbEntityManager().merge(groupEntity);
    }
    return new IdentityOperationResult(groupEntity, operation);
  }

  @Override
  public IdentityOperationResult deleteGroup(final String groupId) {
    checkAuthorization(Permissions.DELETE, Resources.GROUP, groupId);
    GroupEntity group = findGroupById(groupId);
    if(group != null) {
      deleteMembershipsByGroupId(groupId);
      deleteTenantMembershipsOfGroup(groupId);

      deleteAuthorizations(Resources.GROUP, groupId);

      Context.getCommandContext().runWithoutAuthorization(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          final List<Tenant> tenants = createTenantQuery().groupMember(groupId).list();
          if (tenants != null && !tenants.isEmpty()) {
            for (Tenant tenant : tenants) {
              deleteAuthorizationsForGroup(Resources.TENANT, tenant.getId(), groupId);
            }
          }
          return null;
        }
      });
      getDbEntityManager().delete(group);
      return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_DELETE);
    }
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_NONE);
  }

  // tenants //////////////////////////////////////////////////////

  @Override
  public Tenant createNewTenant(String tenantId) {
    return new TenantEntity(tenantId);
  }

  @Override
  public IdentityOperationResult saveTenant(Tenant tenant) {
    TenantEntity tenantEntity = (TenantEntity) tenant;
    String operation = null;
    if (tenantEntity.getRevision() == 0) {
      operation = IdentityOperationResult.OPERATION_CREATE;
      checkAuthorization(Permissions.CREATE, Resources.TENANT, null);
      getDbEntityManager().insert(tenantEntity);
      createDefaultAuthorizations(tenant);
    } else {
      operation = IdentityOperationResult.OPERATION_UPDATE;
      checkAuthorization(Permissions.UPDATE, Resources.TENANT, tenant.getId());
      getDbEntityManager().merge(tenantEntity);
    }
    return new IdentityOperationResult(tenantEntity, operation);
  }

  @Override
  public IdentityOperationResult deleteTenant(String tenantId) {
    checkAuthorization(Permissions.DELETE, Resources.TENANT, tenantId);
    TenantEntity tenant = findTenantById(tenantId);
    if (tenant != null) {
      deleteTenantMembershipsOfTenant(tenantId);

      deleteAuthorizations(Resources.TENANT, tenantId);
      getDbEntityManager().delete(tenant);
      return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_DELETE);
    }
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_NONE);
  }

  // membership //////////////////////////////////////////////////////

  @Override
  public IdentityOperationResult createMembership(String userId, String groupId) {
    checkAuthorization(Permissions.CREATE, Resources.GROUP_MEMBERSHIP, groupId);
    UserEntity user = findUserById(userId);
    ensureNotNull("No user found with id '" + userId + "'.", "user", user);
    GroupEntity group = findGroupById(groupId);
    ensureNotNull("No group found with id '" + groupId + "'.", "group", group);
    MembershipEntity membership = new MembershipEntity();
    membership.setUser(user);
    membership.setGroup(group);
    getDbEntityManager().insert(membership);
    createDefaultMembershipAuthorizations(userId, groupId);
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_CREATE);
  }

  @Override
  public IdentityOperationResult deleteMembership(String userId, String groupId) {
    checkAuthorization(Permissions.DELETE, Resources.GROUP_MEMBERSHIP, groupId);
    if (existsMembership(userId, groupId)) {
      deleteAuthorizations(Resources.GROUP_MEMBERSHIP, groupId);

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("userId", userId);
      parameters.put("groupId", groupId);
      getDbEntityManager().delete(MembershipEntity.class, "deleteMembership", parameters);
      return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_DELETE);
    }
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_NONE);
  }

  protected void deleteMembershipsByUserId(String userId) {
    getDbEntityManager().delete(MembershipEntity.class, "deleteMembershipsByUserId", userId);
  }

  protected void deleteMembershipsByGroupId(String groupId) {
    getDbEntityManager().delete(MembershipEntity.class, "deleteMembershipsByGroupId", groupId);
  }

  @Override
  public IdentityOperationResult createTenantUserMembership(String tenantId, String userId) {
    checkAuthorization(Permissions.CREATE, Resources.TENANT_MEMBERSHIP, tenantId);

    TenantEntity tenant = findTenantById(tenantId);
    UserEntity user = findUserById(userId);

    ensureNotNull("No tenant found with id '" + tenantId + "'.", "tenant", tenant);
    ensureNotNull("No user found with id '" + userId + "'.", "user", user);

    TenantMembershipEntity membership = new TenantMembershipEntity();
    membership.setTenant(tenant);
    membership.setUser(user);

    getDbEntityManager().insert(membership);

    createDefaultTenantMembershipAuthorizations(tenant, user);
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_CREATE);
  }

  @Override
  public IdentityOperationResult createTenantGroupMembership(String tenantId, String groupId) {
    checkAuthorization(Permissions.CREATE, Resources.TENANT_MEMBERSHIP, tenantId);

    TenantEntity tenant = findTenantById(tenantId);
    GroupEntity group = findGroupById(groupId);

    ensureNotNull("No tenant found with id '" + tenantId + "'.", "tenant", tenant);
    ensureNotNull("No group found with id '" + groupId + "'.", "group", group);

    TenantMembershipEntity membership = new TenantMembershipEntity();
    membership.setTenant(tenant);
    membership.setGroup(group);

    getDbEntityManager().insert(membership);

    createDefaultTenantMembershipAuthorizations(tenant, group);
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_CREATE);
  }

  @Override
  public IdentityOperationResult deleteTenantUserMembership(String tenantId, String userId) {
    checkAuthorization(Permissions.DELETE, Resources.TENANT_MEMBERSHIP, tenantId);
    if (existsTenantMembership(tenantId, userId, null)) {
      deleteAuthorizations(Resources.TENANT_MEMBERSHIP, userId);

      deleteAuthorizationsForUser(Resources.TENANT, tenantId, userId);

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("tenantId", tenantId);
      parameters.put("userId", userId);
      getDbEntityManager().delete(TenantMembershipEntity.class, "deleteTenantMembership", parameters);
      return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_DELETE);
    }
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_NONE);
  }

  @Override
  public IdentityOperationResult deleteTenantGroupMembership(String tenantId, String groupId) {
    checkAuthorization(Permissions.DELETE, Resources.TENANT_MEMBERSHIP, tenantId);

    if (existsTenantMembership(tenantId, null, groupId)) {
      deleteAuthorizations(Resources.TENANT_MEMBERSHIP, groupId);

      deleteAuthorizationsForGroup(Resources.TENANT, tenantId, groupId);

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("tenantId", tenantId);
      parameters.put("groupId", groupId);
      getDbEntityManager().delete(TenantMembershipEntity.class, "deleteTenantMembership", parameters);
      return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_DELETE);
    }
    return new IdentityOperationResult(null, IdentityOperationResult.OPERATION_NONE);
  }

  protected void deleteTenantMembershipsOfUser(String userId) {
    getDbEntityManager().delete(TenantMembershipEntity.class, "deleteTenantMembershipsOfUser", userId);
  }

  protected void deleteTenantMembershipsOfGroup(String groupId) {
    getDbEntityManager().delete(TenantMembershipEntity.class, "deleteTenantMembershipsOfGroup", groupId);
  }

  protected void deleteTenantMembershipsOfTenant(String tenant) {
    getDbEntityManager().delete(TenantMembershipEntity.class, "deleteTenantMembershipsOfTenant", tenant);
  }

  // authorizations ////////////////////////////////////////////////////////////

  protected void createDefaultAuthorizations(UserEntity userEntity) {
    if(Context.getProcessEngineConfiguration().isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().newUser(userEntity));
    }
  }

  protected void createDefaultAuthorizations(Group group) {
    if(isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().newGroup(group));
    }
  }

  protected void createDefaultAuthorizations(Tenant tenant) {
    if (isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().newTenant(tenant));
    }
  }

  protected void createDefaultMembershipAuthorizations(String userId, String groupId) {
    if(isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().groupMembershipCreated(groupId, userId));
    }
  }

  protected void createDefaultTenantMembershipAuthorizations(Tenant tenant, User user) {
    if(isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().tenantMembershipCreated(tenant, user));
    }
  }

  protected void createDefaultTenantMembershipAuthorizations(Tenant tenant, Group group) {
    if(isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().tenantMembershipCreated(tenant, group));
    }
  }

}
