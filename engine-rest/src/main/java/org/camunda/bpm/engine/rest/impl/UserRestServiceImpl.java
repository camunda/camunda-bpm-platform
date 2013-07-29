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

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;
import org.camunda.bpm.engine.rest.dto.identity.UserQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.identity.UserResource;
import org.camunda.bpm.engine.rest.sub.identity.impl.UserResourceImpl;

/**
 * @author Daniel Meyer
 *
 */
public class UserRestServiceImpl extends AbstractRestProcessEngineAware implements UserRestService {
  
  public UserRestServiceImpl() {
    super();
  }

  public UserRestServiceImpl(String engineName) {
    super(engineName);
  }

  public UserResource getUser(String id) {
    return new UserResourceImpl(getProcessEngine(), id, relativeRootResourcePath);
  }

  public List<UserProfileDto> queryUsers(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    UserQueryDto queryDto = new UserQueryDto(uriInfo.getQueryParameters());
    return queryUsers(queryDto, firstResult, maxResults);
  }

  public List<UserProfileDto> queryUsers(UserQueryDto queryDto, Integer firstResult, Integer maxResults) {
    
    UserQuery query = queryDto.toQuery(getProcessEngine());
    
    List<User> resultList;
    if(firstResult != null || maxResults != null) {
      resultList = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      resultList = query.list();
    }
    
    return UserProfileDto.fromUserList(resultList);
  }
  

  public CountResultDto getUserCount(UriInfo uriInfo) {
    UserQueryDto queryDto = new UserQueryDto(uriInfo.getQueryParameters());
    return getUserCount(queryDto);
  }

  protected CountResultDto getUserCount(UserQueryDto queryDto) {
    UserQuery query = queryDto.toQuery(getProcessEngine());    
    long count = query.count();
    return new CountResultDto(count);
  }
  
  public void createUser(UserDto userDto) {
    final IdentityService identityService = getIdentityService();
    
    if(identityService.isReadOnly()) {
      throw new InvalidRequestException(Status.FORBIDDEN, "Identity service implementation is read-only.");
    }
    
    UserProfileDto profile = userDto.getProfile();    
    if(profile == null || profile.getId() == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "request object must provide profile information with valid id.");
    }
    
    User newUser = identityService.newUser(profile.getId());    
    profile.update(newUser);
    
    if(userDto.getCredentials() != null) {
      newUser.setPassword(userDto.getCredentials().getPassword());
    }
    
    identityService.saveUser(newUser);      
      
  }
  
  // utility methods //////////////////////////////////////
  
  protected List<User> executePaginatedQuery(UserQuery query, Integer firstResult, Integer maxResults) {
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
