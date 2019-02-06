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

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.junit.Test;

public class PermissionsTest {

  @Test
  public void testNewPermissionsIntegrityToOld() {
    for (Permissions permission : Permissions.values()) {
      String permissionName = permission.getName();
      for (Resource resource : permission.getTypes()) {
        Permission resolvedPermission = null;
        int resourceType = resource.resourceType();
        if (resourceType == Resources.BATCH.resourceType()) {
          resolvedPermission = BatchPermissions.forName(permissionName);
        } else if (resourceType == Resources.PROCESS_DEFINITION.resourceType()) {
          resolvedPermission = ProcessDefinitionPermissions.forName(permissionName);
        } else if (resourceType == Resources.PROCESS_INSTANCE.resourceType()) {
          resolvedPermission = ProcessInstancePermissions.forName(permissionName);
        } else {
          continue;
        }
        assertThat(resolvedPermission).isNotNull();
        assertThat(resolvedPermission.getValue()).isEqualTo(permission.getValue());
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
    return value > 0 && (value & (value - 1)) == 0;
  }
}
