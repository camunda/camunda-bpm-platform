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

import java.util.Arrays;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
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

/**
 * @author Tobias Metzke
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class AuthorizationUserOperationLogTest extends AuthorizationTest {

  public void testLogCreatedOnAuthorizationCreation() {
    // given
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());
    
    // when
    createGrantAuthorizationGroup(Resources.PROCESS_DEFINITION, Authorization.ANY, "testGroupId", ProcessDefinitionPermissions.DELETE);
    
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
  
  public void testLogCreatedOnAuthorizationUpdate() {
    // given
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    identityService.clearAuthentication();
    Authorization authorization = 
        createGrantAuthorization(Resources.PROCESS_DEFINITION, Authorization.ANY, "testUserId", ProcessDefinitionPermissions.DELETE);
    assertEquals(0, query.count());
    identityService.setAuthentication(userId, Arrays.asList(groupId));
    
    // when
    authorization.addPermission(ProcessDefinitionPermissions.READ);
    saveAuthorization(authorization);
    
    // then
    assertEquals(6, query.count());
    
    UserOperationLogEntry entry = query.property("permissionBits").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(String.valueOf(ProcessDefinitionPermissions.DELETE.getValue()|ProcessDefinitionPermissions.READ.getValue()), entry.getNewValue());
    
    entry = query.property("permissions").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(ProcessDefinitionPermissions.READ.getName() + ", " + ProcessDefinitionPermissions.DELETE.getName(), entry.getNewValue());
    
    entry = query.property("type").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(String.valueOf(Authorization.AUTH_TYPE_GRANT), entry.getNewValue());
    
    entry = query.property("resource").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(Resources.PROCESS_DEFINITION.resourceName(), entry.getNewValue());
    
    entry = query.property("resourceId").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(Authorization.ANY, entry.getNewValue());
    
    entry = query.property("userId").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals("testUserId", entry.getNewValue());
  }
  
  public void testLogCreatedOnAuthorizationDeletion() {
    // given
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    identityService.clearAuthentication();
    Authorization authorization = 
        createGrantAuthorization(Resources.PROCESS_DEFINITION, Authorization.ANY, "testUserId", ProcessDefinitionPermissions.DELETE);
    assertEquals(0, query.count());
    identityService.setAuthentication(userId, Arrays.asList(groupId));
    
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
  
  public void testLogCreatedOnAuthorizationCreationWithExceedingPermissionStringList() {
    // given
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
  
  public void testLogCreatedOnAuthorizationCreationWithAllPermission() {
    // given
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());
    
    // when
    PermissionProvider permissionProvider = processEngineConfiguration.getPermissionProvider();
    processEngineConfiguration.setPermissionProvider(new TestPermissionProvider());
    createGrantAuthorizationGroup(TestResource.RESOURCE1, Authorization.ANY, "testGroupId", TestPermissions.ALL, TestPermissions.CREATE,
        TestPermissions.DELETE, TestPermissions.LONG_NAME, TestPermissions.RANDOM);
    processEngineConfiguration.setPermissionProvider(permissionProvider);
    
    // then
    assertEquals(6, query.count());
    
    UserOperationLogEntry entry = query.property("permissions").singleResult();
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, entry.getOperationType());
    assertEquals(UserOperationLogEntry.CATEGORY_ADMIN, entry.getCategory());
    assertEquals(EntityTypes.AUTHORIZATION, entry.getEntityType());
    assertEquals(TestPermissions.ALL.getName(), entry.getNewValue());
  }
  
  public static class TestPermissionProvider extends DefaultPermissionProvider {
    @Override
    public String getNameForResource(int resourceType) {
      for (Resource resource : TestResource.values()) {
        if (resourceType == resource.resourceType()) {
          return  resource.resourceName();
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
