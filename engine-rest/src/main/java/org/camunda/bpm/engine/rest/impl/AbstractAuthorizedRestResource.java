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
package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractAuthorizedRestResource extends AbstractRestProcessEngineAware {

  protected final Resource resource;
  protected final String resourceId;

  public AbstractAuthorizedRestResource(String processEngineName, Resource resource, String resourceId, ObjectMapper objectMapper) {
    super(processEngineName, objectMapper);
    this.resource = resource;
    this.resourceId = resourceId;
  }

  protected boolean isAuthorized(Permission permission, Resource resource, String resourceId) {

    final IdentityService identityService = processEngine.getIdentityService();
    final AuthorizationService authorizationService = processEngine.getAuthorizationService();

    Authentication authentication = identityService.getCurrentAuthentication();
    if(authentication == null) {
      return true;

    } else {
      return authorizationService
         .isUserAuthorized(authentication.getUserId(), authentication.getGroupIds(), permission, resource, resourceId);
    }
  }

  protected boolean isAuthorized(Permission permission, Resource resource) {
    return isAuthorized(permission, resource, resourceId);
  }

  protected boolean isAuthorized(Permission permission) {
    return isAuthorized(permission, resource);
  }

}
