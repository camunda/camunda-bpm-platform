/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.api.authorization.util;

import static junit.framework.TestCase.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationTestUtil {

  protected static final int BATCH = Resources.BATCH.resourceType();
  protected static final int PROCESS_DEFINITION = Resources.PROCESS_DEFINITION.resourceType();
  protected static final int PROCESS_INSTANCE = Resources.PROCESS_INSTANCE.resourceType();
  protected static final int TASK = Resources.TASK.resourceType();

  protected static Map<Integer, Resource> resourcesByType = new HashMap<Integer, Resource>();
  protected static Map<Integer, Permission[]> permissionMap = new HashMap<Integer, Permission[]>();

  static {
    for (Resource resource : Resources.values()) {
      resourcesByType.put(resource.resourceType(), resource);
    }
  }

  static {
    permissionMap.put(BATCH, BatchPermissions.values());
    permissionMap.put(PROCESS_DEFINITION, ProcessDefinitionPermissions.values());
    permissionMap.put(PROCESS_INSTANCE, ProcessInstancePermissions.values());
  }

  public static Resource getResourceByType(int type) {
    return resourcesByType.get(type);
  }

  /**
   * Checks if the info has the expected parameters.
   *
   * @param expectedPermissionName to use
   * @param expectedResourceName to use
   * @param expectedResourceId to use
   * @param info to check
   */
  public static void assertExceptionInfo(String expectedPermissionName, String expectedResourceName, String expectedResourceId,
      MissingAuthorization info) {
    assertEquals(expectedPermissionName, info.getViolatedPermissionName());
    assertEquals(expectedResourceName, info.getResourceType());
    assertEquals(expectedResourceId, info.getResourceId());
  }

  /**
   * @return the set of permission for the given authorization
   */
  public static Permission[] getPermissions(Authorization authorization)
  {
    int resourceType = authorization.getResourceType();
    if (resourceType == BATCH || resourceType == PROCESS_DEFINITION || resourceType == PROCESS_INSTANCE || resourceType == TASK) {
      Permission[] permissionsForType = permissionMap.get(resourceType);
      return authorization.getPermissions(permissionsForType);
    } else {
      return authorization.getPermissions(Permissions.values());
    }
  }
}
