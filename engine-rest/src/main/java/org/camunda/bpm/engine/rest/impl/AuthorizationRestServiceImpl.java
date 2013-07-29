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


import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.AuthorizationRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationCheckResultDto;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationDto;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.authorization.AuthorizationResource;
import org.camunda.bpm.engine.rest.sub.authorization.impl.AuthorizationResourceImpl;
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
  
  public AuthorizationResource getAuthorization(String id) {
    return new AuthorizationResourceImpl(getProcessEngine(), id, relativeRootResourcePath);
  }

  public List<AuthorizationDto> queryAuthorizations(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    AuthorizationQueryDto queryDto = new AuthorizationQueryDto(uriInfo.getQueryParameters());
    return queryAuthorizations(queryDto, firstResult, maxResults);
  }

  public List<AuthorizationDto> queryAuthorizations(AuthorizationQueryDto queryDto, Integer firstResult, Integer maxResults) {
    
    AuthorizationQuery query = queryDto.toQuery(getProcessEngine());
    
    List<Authorization> resultList;
    if(firstResult != null || maxResults != null) {
      resultList = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      resultList = query.list();
    }
    
    return AuthorizationDto.fromAuthorizationList(resultList);
  }

  public CountResultDto getAuthorizationCount(UriInfo uriInfo) {
    AuthorizationQueryDto queryDto = new AuthorizationQueryDto(uriInfo.getQueryParameters());
    return getAuthorizationCount(queryDto);
  }

  protected CountResultDto getAuthorizationCount(AuthorizationQueryDto queryDto) {
    AuthorizationQuery query = queryDto.toQuery(getProcessEngine());    
    long count = query.count();
    return new CountResultDto(count);
  }

  public void createAuthorization(AuthorizationDto dto) {
    final AuthorizationService authorizationService = processEngine.getAuthorizationService();

    Authorization newAuthorization = authorizationService.createNewAuthorization(dto.getType());    
    AuthorizationDto.update(dto, newAuthorization);
    
    authorizationService.saveAuthorization(newAuthorization);      
  }
  
  // utility methods //////////////////////////////////////
  
  protected List<Authorization> executePaginatedQuery(AuthorizationQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults); 
  }

  protected IdentityService getIdentityService() {
    return getProcessEngine().getIdentityService();
  }

}
