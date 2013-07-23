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


import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.AuthorizationRestService;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationCheckResultDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.util.AuthorizationUtil;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationRestServiceImpl extends AbstractRestProcessEngineAware implements AuthorizationRestService {

  public AuthorizationRestServiceImpl() {
  }
  
  public AuthorizationRestServiceImpl(String engineName) {
    super(engineName);
  }

  public AuthorizationCheckResultDto isUserAuthorized(String permissionName, Integer permissionValue, String resourceName, Integer resourceType, String resourceId) {    
    
    // validate request:
    if(permissionName == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Query parameter 'permissionName' cannot be null");
      
    } else if(permissionValue == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Query parameter 'permissionValue' cannot be null");
      
    } else if(resourceName == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Query parameter 'resourceName' cannot be null");
      
    } else if(resourceType == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Query parameter 'resourceType' cannot be null");
      
    }  
    
    final Authentication currentAuthentication = processEngine.getIdentityService().getCurrentAuthentication();    
    if(currentAuthentication == null) {
      throw new InvalidRequestException(Status.UNAUTHORIZED, "You must be authenticated in order to use this resource.");
    }
    
    final AuthorizationService authorizationService = processEngine.getAuthorizationService();
    
    // create new authorization dto implementing both Permission and Resource
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(resourceName, resourceType, permissionName, permissionValue);    

    boolean isUserAuthorized = false;    
    if(resourceId == null || Authorization.ANY.equals(resourceId)) {
      isUserAuthorized = authorizationService.isUserAuthorized(currentAuthentication.getUserId(), currentAuthentication.getGroupIds(), authorizationUtil, authorizationUtil);
      
    } else {
      isUserAuthorized = authorizationService.isUserAuthorized(currentAuthentication.getUserId(), currentAuthentication.getGroupIds(), authorizationUtil, authorizationUtil, resourceId);
      
    }
    
    return new AuthorizationCheckResultDto(isUserAuthorized, authorizationUtil, resourceId);
  }

}
