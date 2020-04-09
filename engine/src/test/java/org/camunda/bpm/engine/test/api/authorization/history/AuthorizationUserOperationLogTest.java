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
package org.camunda.bpm.engine.test.api.authorization.history;

import static org.camunda.bpm.engine.authorization.Resources.OPERATION_LOG_CATEGORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.UserOperationLogCategoryPermissions.READ;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_ADMIN;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_OPERATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.cfg.auth.DefaultPermissionProvider;
import org.camunda.bpm.engine.impl.cfg.auth.PermissionProvider;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.engine.test.api.identity.TestPermissions;
import org.camunda.bpm.engine.test.api.identity.TestResource;
import org.junit.Test;

/**
 * @author Tobias Metzke
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class AuthorizationUserOperationLogTest extends AuthorizationTest {

  @Test
  public void testLogCreatedOnAuthorizationCreation() {
    // given
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_ADMIN, userId, READ);
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());

    // when
    createGrantAuthorizationGroup(PROCESS_DEFINITION, Authorization.ANY, "testGroupId", ProcessDefinitionPermissions.DELETE);

    // then
    assertEquals(6, query.count());

    UserOperationLogEntry entry = query.property("permissionBits").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(String.valueOf(ProcessDefinitionPermissions.DELETE.getValue()), entry.getNewValue());

    entry = query.property("permissions").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(ProcessDefinitionPermissions.DELETE.getName(), entry.getNewValue());

    entry = query.property("type").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(String.valueOf(Authorization.AUTH_TYPE_GRANT), entry.getNewValue());

    entry = query.property("resource").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(Resources.PROCESS_DEFINITION.resourceName(), entry.getNewValue());

    entry = query.property("resourceId").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(Authorization.ANY, entry.getNewValue());

    entry = query.property("groupId").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals("testGroupId", entry.getNewValue());
  }

  @Test
  public void testLogCreatedOnAuthorizationUpdate() {
    // given
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    Authorization authorization = createGrantAuthorizationWithoutAuthentication(Resources.PROCESS_DEFINITION, Authorization.ANY, "testUserId",
        Permissions.DELETE);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_ADMIN, userId, READ);
    assertEquals(0, query.count());

    // when
    authorization.addPermission(Permissions.READ);
    authorization.setResource(Resources.PROCESS_INSTANCE);
    authorization.setResourceId("abc123");
    authorization.setGroupId("testGroupId");
    authorization.setUserId(null);
    saveAuthorization(authorization);

    // then
    assertEquals(7, query.count());

    UserOperationLogEntry entry = query.property("permissionBits").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(String.valueOf(Permissions.DELETE.getValue() | Permissions.READ.getValue()), entry.getNewValue());
    assertEquals(String.valueOf(Permissions.DELETE.getValue()), entry.getOrgValue());

    entry = query.property("permissions").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(Permissions.READ.getName() + ", " + Permissions.DELETE.getName(), entry.getNewValue());
    assertEquals(Permissions.DELETE.getName(), entry.getOrgValue());

    entry = query.property("type").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(String.valueOf(Authorization.AUTH_TYPE_GRANT), entry.getNewValue());
    assertEquals(String.valueOf(Authorization.AUTH_TYPE_GRANT), entry.getOrgValue());

    entry = query.property("resource").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(Resources.PROCESS_INSTANCE.resourceName(), entry.getNewValue());
    assertEquals(Resources.PROCESS_DEFINITION.resourceName(), entry.getOrgValue());

    entry = query.property("resourceId").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals("abc123", entry.getNewValue());
    assertEquals(Authorization.ANY, entry.getOrgValue());

    entry = query.property("userId").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertNull(entry.getNewValue());
    assertEquals("testUserId", entry.getOrgValue());

    entry = query.property("groupId").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals("testGroupId", entry.getNewValue());
    assertNull(entry.getOrgValue());
  }

  @Test
  public void testLogCreatedOnAuthorizationDeletion() {
    // given
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    Authorization authorization = createGrantAuthorizationWithoutAuthentication(Resources.PROCESS_DEFINITION, Authorization.ANY, "testUserId",
        ProcessDefinitionPermissions.DELETE);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_ADMIN, userId, READ);
    assertEquals(0, query.count());

    // when
    authorizationService.deleteAuthorization(authorization.getId());

    // then
    assertEquals(6, query.count());

    UserOperationLogEntry entry = query.property("permissionBits").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(String.valueOf(ProcessDefinitionPermissions.DELETE.getValue()), entry.getNewValue());

    entry = query.property("permissions").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(ProcessDefinitionPermissions.DELETE.getName(), entry.getNewValue());

    entry = query.property("type").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(String.valueOf(Authorization.AUTH_TYPE_GRANT), entry.getNewValue());

    entry = query.property("resource").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(Resources.PROCESS_DEFINITION.resourceName(), entry.getNewValue());

    entry = query.property("resourceId").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(Authorization.ANY, entry.getNewValue());

    entry = query.property("userId").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals("testUserId", entry.getNewValue());
  }

  @Test
  public void testLogCreatedOnAuthorizationCreationWithExceedingPermissionStringList() {
    // given
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_ADMIN, userId, READ);
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());

    // when
    PermissionProvider permissionProvider = processEngineConfiguration.getPermissionProvider();
    processEngineConfiguration.setPermissionProvider(new TestPermissionProvider());
    createGrantAuthorizationGroup(TestResource.RESOURCE1, Authorization.ANY, "testGroupId", TestPermissions.LONG_NAME);
    processEngineConfiguration.setPermissionProvider(permissionProvider);

    // then
    assertEquals(6, query.count());

    UserOperationLogEntry entry = query.property("permissions").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(TestPermissions.LONG_NAME.getName().substring(0, StringUtil.DB_MAX_STRING_LENGTH), entry.getNewValue());
  }

  @Test
  public void testLogCreatedOnAuthorizationCreationWithAllPermission() {
    // given
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_ADMIN, userId, READ);
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());

    // when
    PermissionProvider permissionProvider = processEngineConfiguration.getPermissionProvider();
    processEngineConfiguration.setPermissionProvider(new TestPermissionProvider());
    createGrantAuthorizationGroup(TestResource.RESOURCE1, Authorization.ANY, "testGroupId", TestPermissions.ALL);
    processEngineConfiguration.setPermissionProvider(permissionProvider);

    // then
    assertEquals(6, query.count());

    UserOperationLogEntry entry = query.property("permissions").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(TestPermissions.ALL.getName(), entry.getNewValue());
  }

  @Test
  public void testLogCreatedOnAuthorizationCreationWithNonePermission() {
    // given
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_ADMIN, userId, READ);
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());

    // when
    PermissionProvider permissionProvider = processEngineConfiguration.getPermissionProvider();
    processEngineConfiguration.setPermissionProvider(new TestPermissionProvider());
    createGrantAuthorizationGroup(TestResource.RESOURCE1, Authorization.ANY, "testGroupId", TestPermissions.NONE);
    processEngineConfiguration.setPermissionProvider(permissionProvider);

    // then
    assertEquals(6, query.count());

    UserOperationLogEntry entry = query.property("permissions").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(TestPermissions.NONE.getName(), entry.getNewValue());
  }

  @Test
  public void testLogCreatedOnAuthorizationCreationWithoutAuthorization() {
    // given
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());

    // when
    createGrantAuthorizationGroup(PROCESS_DEFINITION, Authorization.ANY, "testGroupId", ProcessDefinitionPermissions.DELETE);

    // then the user is not authorised
    assertEquals(0, query.count());
  }

  @Test
  public void testLogCreatedOnAuthorizationCreationWithReadPermissionOnAnyCategoryPermission() {
    // given
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, Authorization.ANY, userId, READ);
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());

    // when
    createGrantAuthorizationGroup(PROCESS_DEFINITION, Authorization.ANY, "testGroupId", ProcessDefinitionPermissions.DELETE);

    // then the user is authorised
    assertEquals(6, query.count());
  }

  @Test
  public void testLogCreatedOnAuthorizationCreationWithReadPermissionOnWrongCategory() {
    // given
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_OPERATOR, userId, READ);
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());

    // when
    createGrantAuthorizationGroup(PROCESS_DEFINITION, Authorization.ANY, "testGroupId", ProcessDefinitionPermissions.DELETE);

    // then the user is not authorised
    assertEquals(0, query.count());
  }

  public static class TestPermissionProvider extends DefaultPermissionProvider {
    @Override
    public String getNameForResource(int resourceType) {
      for (Resource resource : TestResource.values()) {
        if (resourceType == resource.resourceType()) {
          return resource.resourceName();
        }
      }
      return null;
    }

    @Override
    public Permission[] getPermissionsForResource(int resourceType) {
      return TestPermissions.values();
    }
  }
}
