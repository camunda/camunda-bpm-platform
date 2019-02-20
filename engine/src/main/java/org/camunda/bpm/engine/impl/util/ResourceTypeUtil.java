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
package org.camunda.bpm.engine.impl.util;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.TaskPermissions;

public class ResourceTypeUtil {

  private static final Map<Integer, Class<? extends Enum<? extends Permission>>> permissionEnums = new HashMap<>();

  static {
    permissionEnums.put(Resources.BATCH.resourceType(), BatchPermissions.class);
    permissionEnums.put(Resources.PROCESS_DEFINITION.resourceType(), ProcessDefinitionPermissions.class);
    permissionEnums.put(Resources.PROCESS_INSTANCE.resourceType(), ProcessInstancePermissions.class);
    permissionEnums.put(Resources.TASK.resourceType(), TaskPermissions.class);

    // the rest
    for (Resource resource : Resources.values()) {
      int resourceType = resource.resourceType();
      if (!permissionEnums.containsKey(resourceType)) {
        permissionEnums.put(resourceType, Permissions.class);
      }
    }
  }

  public static boolean resourceIsContainedInArray(Integer resourceTypeId, Resource[] list) {
    for (Resource resource : list) {
      if (resourceTypeId == resource.resourceType()) {
        return true;
      }
    }
    return false;
  }

  public static Map<Integer, Class<? extends Enum<? extends Permission>>> getPermissionEnums() {
    return permissionEnums;
  }

  public static Permission[] getPermissionsByResourceType(int givenResourceType) {
    Class<? extends Enum<? extends Permission>> clazz = permissionEnums.get(givenResourceType);
    if (clazz == null) {
      return Permissions.values();
    }
    return ((Permission[]) clazz.getEnumConstants());
  }

  public static Permission getPermissionForNameByResourceType(String name, int resourceType) {
    if (resourceType == Resources.BATCH.resourceType()) {
      return BatchPermissions.forName(name);
    } else if (resourceType == Resources.PROCESS_DEFINITION.resourceType()) {
      return ProcessDefinitionPermissions.forName(name);
    } else if (resourceType == Resources.PROCESS_INSTANCE.resourceType()) {
      return ProcessInstancePermissions.forName(name);
    } else if (resourceType == Resources.TASK.resourceType()) {
      return TaskPermissions.forName(name);
    } else {
      return Permissions.forName(name);
    }
  }
}
