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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * <p>
 * Converts between the String-Array based and the Integer-based representation
 * of permissions.
 * </p>
 * 
 * @author Daniel Meyer
 * @author Tobias Metzke
 *
 */
public class PermissionConverter {

  public static Permission[] getPermissionsForNames(String[] names, int resourceType, ProcessEngineConfiguration engineConfiguration) {
    
    final Permission[] permissions = new Permission[names.length];
    
    for (int i = 0; i < names.length; i++) {
      permissions[i] = ((ProcessEngineConfigurationImpl) engineConfiguration).getPermissionProvider().getPermissionForName(names[i], resourceType);
    }
        
    return permissions;    
  }

  public static String[] getNamesForPermissions(Authorization authorization, Permission[] permissions) {

    int type = authorization.getAuthorizationType();

    // special case all permissions are granted
    if ((type == Authorization.AUTH_TYPE_GLOBAL || type == Authorization.AUTH_TYPE_GRANT)
        && authorization.isEveryPermissionGranted()) {
      return new String[] { Permissions.ALL.getName() };
    }

    // special case all permissions are revoked
    if (type == Authorization.AUTH_TYPE_REVOKE && authorization.isEveryPermissionRevoked()) {
      return new String[] { Permissions.ALL.getName() };
    }

    List<String> names = new ArrayList<String>();

    for (Permission permission : permissions) {
      String name = permission.getName();
      // filter NONE and ALL from permissions array
      if (!name.equals(Permissions.NONE.getName()) && !name.equals(Permissions.ALL.getName())) {
        names.add(name);
      }
    }

    return names.toArray(new String[names.size()]);
  }

}
