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
package org.camunda.bpm.engine.test.api.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.util.ResourceTypeUtil;
import org.junit.Test;

public class PermissionsTest {

  @Test
  public void testNewPermissionsIntegrityToOld() {
    for (Permissions permission : Permissions.values()) {
      String permissionName = permission.getName();
      for (Resource resource : permission.getTypes()) {
        Class<? extends Enum<?>> clazz = ResourceTypeUtil.getPermissionEnums().get(resource.resourceType());
        if (clazz != null && !clazz.equals(Permissions.class)) {
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
    verifyValuesAreUniqueAndPowerOfTwo(Permissions.values(), Permissions.class.getSimpleName());
  }

  @Test
  public void testRestOfPermissionsEnumValues() {
    for (Class<? extends Enum<? extends Permission>> permissionsClass : ResourceTypeUtil.getPermissionEnums().values()) {
      if(!permissionsClass.equals(Permissions.class)) {
        verifyValuesAreUniqueAndPowerOfTwo((Permission[])permissionsClass.getEnumConstants(), permissionsClass.getSimpleName());
      }
    }
  }

  @Test
  public void testThatPermissionsEnumContainsAllPermissions() {
    // when
    Map<Integer, Class<? extends Enum<? extends Permission>>> permissionEnums = ResourceTypeUtil.getPermissionEnums();

    // then
    Integer[] allResourceTypes = Stream.of(Resources.values()).map(r -> r.resourceType()).toArray(Integer[]::new);
    assertThat(permissionEnums).containsKeys(allResourceTypes);
  }

  private void verifyValuesAreUniqueAndPowerOfTwo(Permission[] permissions, String className) {
    Set<Integer> values = new HashSet<>();
    for (Permission permission : permissions) {
      int value = permission.getValue();
      // value is unique
      assertThat(values.add(value))
          .overridingErrorMessage("The value '%s' of '%s' permission is not unique for '%s' permission enum. Another permission already has this value.", value, permission, className)
          .isTrue();
      if (value != Integer.MAX_VALUE && value != 0) {
        // value is power of 2
        assertThat(isPowerOfTwo(value))
          .overridingErrorMessage("The value '%s' of '%s' permission is invalid for '%s' permission enum. The values must be power of 2.", value, permission, className)
          .isTrue();
      }
    }
  }

  private boolean isPowerOfTwo(int value) {
    return value > 1 && (value & (value - 1)) == 0;
  }
}
