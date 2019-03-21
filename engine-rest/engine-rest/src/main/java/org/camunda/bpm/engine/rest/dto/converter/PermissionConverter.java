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
package org.camunda.bpm.engine.rest.dto.converter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.spi.PermissionProvider;

/**
 * <p>Converts between the String-Array based representation of permissions in the REST API
 * and the Integer-based representation in the JAVA API.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class PermissionConverter {

  public static Permission[] getPermissionsForNames(String[] names, int resourceType) {
    
    final Permission[] permissions = new Permission[names.length];
    
    for (int i = 0; i < names.length; i++) {
      permissions[i] = getPermissionProvider().getPermissionForName(names[i], resourceType);
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

  // permission provider SPI /////////////////////////////////////

  public static PermissionProvider getPermissionProvider() {
    ServiceLoader<PermissionProvider> serviceLoader = ServiceLoader.load(PermissionProvider.class);
    Iterator<PermissionProvider> iterator = serviceLoader.iterator();

    if (iterator.hasNext()) {
      PermissionProvider provider = iterator.next();
      return provider;
    } else {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, "Could not find an implementation of the " + PermissionProvider.class + " - SPI");
    }
  }

}
