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
package org.camunda.bpm.engine.impl.util;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.OptimizePermissions;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.authorization.UserOperationLogCategoryPermissions;

import static org.camunda.bpm.engine.authorization.Resources.BATCH;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_TASK;
import static org.camunda.bpm.engine.authorization.Resources.OPERATION_LOG_CATEGORY;
import static org.camunda.bpm.engine.authorization.Resources.OPTIMIZE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;

public class ResourceTypeUtil {

  /**
   * A map containing all {@link Resources} as a key and
   * the respective {@link Permission} Enum class for this resource.<p>
   * NOTE: In case of new {@link Permission} Enum class, please adjust the map accordingly
   */
  protected static final Map<Integer, Class<? extends Enum<? extends Permission>>> PERMISSION_ENUMS;

  static {
    PERMISSION_ENUMS = new HashMap<Integer, Class<? extends Enum<? extends Permission>>>() {{
      put(BATCH.resourceType(), BatchPermissions.class);
      put(PROCESS_DEFINITION.resourceType(), ProcessDefinitionPermissions.class);
      put(PROCESS_INSTANCE.resourceType(), ProcessInstancePermissions.class);
      put(TASK.resourceType(), TaskPermissions.class);
      put(HISTORIC_TASK.resourceType(), HistoricTaskPermissions.class);
      put(HISTORIC_PROCESS_INSTANCE.resourceType(), HistoricProcessInstancePermissions.class);
      put(OPERATION_LOG_CATEGORY.resourceType(), UserOperationLogCategoryPermissions.class);
      put(OPTIMIZE.resourceType(), OptimizePermissions.class);
    }};

    // the rest
    for (Resource resource : Resources.values()) {
      int resourceType = resource.resourceType();
      if (!PERMISSION_ENUMS.containsKey(resourceType)) {
        PERMISSION_ENUMS.put(resourceType, Permissions.class);
      }
    }
  }

  /**
   * @return <code>true</code> in case the resource with the provided resourceTypeId is contained by the specified list
   */
  public static boolean resourceIsContainedInArray(Integer resourceTypeId, Resource[] resources) {
    for (Resource resource : resources) {
      if (resourceTypeId == resource.resourceType()) {
        return true;
      }
    }
    return false;
  }

  
  /**
   * @return See {@link ResourceTypeUtil#PERMISSION_ENUMS}
   */
  public static Map<Integer, Class<? extends Enum<? extends Permission>>> getPermissionEnums() {
    return PERMISSION_ENUMS;
  }

  /**
   * Retrieves the {@link Permission} array based on the predifined {@link ResourceTypeUtil#PERMISSION_ENUMS PERMISSION_ENUMS}
   */
  public static Permission[] getPermissionsByResourceType(int givenResourceType) {
    Class<? extends Enum<? extends Permission>> clazz = PERMISSION_ENUMS.get(givenResourceType);
    if (clazz == null) {
      return Permissions.values();
    }
    return ((Permission[]) clazz.getEnumConstants());
  }

  /**
   * Currently used only in the Rest API
   * Returns a {@link Permission} based on the specified <code>permissionName</code> and <code>resourceType</code>
   * @throws BadUserRequestException in case the permission is not valid for the specified resource type
   */
  public static Permission getPermissionByNameAndResourceType(String permissionName, int resourceType) {
    for (Permission permission : getPermissionsByResourceType(resourceType)) {
      if (permission.getName().equals(permissionName)) {
        return permission;
      }
    }
    throw new BadUserRequestException(
        String.format("The permission '%s' is not valid for '%s' resource type.", permissionName, getResourceByType(resourceType))
        );
  }

  /**
   * Iterates over the {@link Resources} and 
   * returns either the resource with specified <code>resourceType</code> or <code>null</code>.
   */
  public static Resource getResourceByType(int resourceType) {
    for (Resource resource : Resources.values()) {
      if (resource.resourceType() == resourceType) {
        return resource;
      }
    }
    return null;
  }
}
