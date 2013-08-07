/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.dto.converter;

import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;

/**
 * <p>Converts between the String-Array based representation of permissions in the REST API
 * and the Integer-based representation in the JAVA API.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class PermissionConverter {
  
  public static Permission[] getPermissionsForNames(String[] names) {
    
    final Permission[] permissions = new Permission[names.length];
    
    for (int i = 0; i < names.length; i++) {
      permissions[i] = getPermissionForName(names[i]);      
    }
        
    return permissions;    
  }
  
  public static String[] getNamesForPermissions(Permission[] permissions) {
    
    final String[] names = new String[permissions.length];
    
    for (int i = 0; i < permissions.length; i++) {
      names[i] = permissions[i].getName();
    }
    
    return names;
    
  }
  
  // permission provider SPI /////////////////////////////////////
  
  public static Permission[] getAllPermissions() {
    // TODO: make this configurable via SPI
    return Permissions.values();
  }
  
  public static Permission getPermissionForName(String name) {
    // TODO: make this configuratble via SPI       
    return Permissions.forName(name);
  }
  
}
