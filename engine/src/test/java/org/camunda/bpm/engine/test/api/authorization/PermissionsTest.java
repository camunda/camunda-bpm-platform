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
package org.camunda.bpm.engine.test.api.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.junit.Test;

public class PermissionsTest {

  private static final Map<Integer, Class<? extends Enum<? extends Permission>>> PERMISSION_ENUMS = new HashMap<>();
  
  static {
    PERMISSION_ENUMS.put(Resources.BATCH.resourceType(), BatchPermissions.class);
    PERMISSION_ENUMS.put(Resources.PROCESS_DEFINITION.resourceType(), ProcessDefinitionPermissions.class);
    PERMISSION_ENUMS.put(Resources.PROCESS_INSTANCE.resourceType(), ProcessInstancePermissions.class);
    PERMISSION_ENUMS.put(Resources.TASK.resourceType(), TaskPermissions.class);
  }
  
  @Test
  public void testNewPermissionsIntegrityToOld() {
    for (Permissions permission : Permissions.values()) {
      String permissionName = permission.getName();
      for (Resource resource : permission.getTypes()) {
        Class<? extends Enum<?>> clazz = PERMISSION_ENUMS.get(resource.resourceType());
        if (clazz != null) {
          Permission resolvedPermission = null;
          for (Enum<?> enumCandidate : clazz.getEnumConstants()) {
            if (enumCandidate.toString().equals(permissionName)) {
              resolvedPermission = (Permission) enumCandidate;
              break;
            }
          }
          assertThat(resolvedPermission)
            .overridingErrorMessage("Permission %s for resource %s not found in new enum %s", permission, resource, clazz.getSimpleName())
            .isNotNull();
            
          assertThat(resolvedPermission.getValue()).isEqualTo(permission.getValue());
        }
      }
    }
  }

  @Test
  public void testPermissionsValues() {
    verifyValuesAreUniqueAndPowerOfTwo(Permissions.values());
  }

  @Test
  public void testBatchPermissionsValues() {
    verifyValuesAreUniqueAndPowerOfTwo(BatchPermissions.values());
  }

  @Test
  public void testProcessInstancePermissionsValues() {
    verifyValuesAreUniqueAndPowerOfTwo(ProcessInstancePermissions.values());
  }

  @Test
  public void testProcessDefinitionPermissionsValues() {
    verifyValuesAreUniqueAndPowerOfTwo(ProcessDefinitionPermissions.values());
  }

  @Test
  public void testTaskPermissionsValues() {
    verifyValuesAreUniqueAndPowerOfTwo(TaskPermissions.values());
  }

  private void verifyValuesAreUniqueAndPowerOfTwo(Permission[] permissions) {
    Set<Integer> values = new HashSet<>();
    for (Permission permission : permissions) {
      int value = permission.getValue();
      // value is unique
      assertThat(values.add(value))
          .overridingErrorMessage("The value '%s' of '%s' permission is not unique. Another permission already has this value.", value, permission)
          .isTrue();
      if (value != Integer.MAX_VALUE && value != 0) {
        // value is power of 2
        assertThat(isPowerOfTwo(value))
          .overridingErrorMessage("The value '%s' of '%s' permission is invalid. The values must be power of 2.", value, permission)
          .isTrue();
      }
    }
  }

  private boolean isPowerOfTwo(int value) {
    return value > 1 && (value & (value - 1)) == 0;
  }
}
