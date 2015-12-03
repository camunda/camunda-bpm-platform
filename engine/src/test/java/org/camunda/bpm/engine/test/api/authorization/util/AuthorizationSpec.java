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
package org.camunda.bpm.engine.test.api.authorization.util;

import java.util.Map;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationSpec {
  protected int type;
  protected Resource resource;
  protected String resourceId;
  protected String userId;
  protected Permission[] permissions;

  public static AuthorizationSpec auth(int type, Resource resource, String resourceId, String userId, Permission... permissions) {
    AuthorizationSpec spec = new AuthorizationSpec();
    spec.type = type;
    spec.resource = resource;
    spec.resourceId = resourceId;
    spec.userId = userId;
    spec.permissions = permissions;
    return spec;
  }

  public static AuthorizationSpec global(Resource resource, String resourceId, String userId, Permission... permissions) {
    return auth(Authorization.AUTH_TYPE_GLOBAL, resource, resourceId, userId, permissions);
  }

  public static AuthorizationSpec grant(Resource resource, String resourceId, String userId, Permission... permissions) {
    return auth(Authorization.AUTH_TYPE_GRANT, resource, resourceId, userId, permissions);
  }

  public static AuthorizationSpec revoke(Resource resource, String resourceId, String userId, Permission... permissions) {
    return auth(Authorization.AUTH_TYPE_REVOKE, resource, resourceId, userId, permissions);
  }

  public Authorization instantiate(AuthorizationService authorizationService, Map<String, String> replacements) {
    Authorization authorization = authorizationService.createNewAuthorization(type);

    // TODO: group id is missing
    authorization.setResource(resource);

    if (replacements.containsKey(resourceId)) {
      authorization.setResourceId(replacements.get(resourceId));
    }
    else {
      authorization.setResourceId(resourceId);
    }
    authorization.setUserId(userId);
    authorization.setPermissions(permissions);

    return authorization;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[Resource: ");
    sb.append(resource);
    sb.append(", Resource Id: ");
    sb.append(resourceId);
    sb.append(", Type: ");
    sb.append(type);
    sb.append(", User Id: ");
    sb.append(userId);
    sb.append(", Permissions: [");

    for (Permission permission : permissions) {
      sb.append(permission.getName());
      sb.append(", ");
    }

    sb.append("]]");

    return sb.toString();

  }
}
