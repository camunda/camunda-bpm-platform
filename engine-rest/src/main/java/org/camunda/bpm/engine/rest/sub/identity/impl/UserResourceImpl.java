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
package org.camunda.bpm.engine.rest.sub.identity.impl;

import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.identity.UserResource;

/**
 * @author Daniel Meyer
 *
 */
public class UserResourceImpl extends AbstractIdentityResource implements UserResource {
  
  protected String rootResourcePath;

  public UserResourceImpl(ProcessEngine processEngine, String userId, String rootResourcePath) {
    super(processEngine, USER, userId);
    this.rootResourcePath = rootResourcePath;
  }

  public UserProfileDto getUserProfile(UriInfo context) {
    
    User dbUser = findUserObject();    
    if(dbUser == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "User with id " + resourceId + " does not exist");
    }
    
    UserProfileDto user = UserProfileDto.fromUser(dbUser);
    
    // add links if operations are authorized
    UriBuilder baseUriBuilder = context.getBaseUriBuilder()
        .path(rootResourcePath)
        .path(UserRestService.class)
        .path(resourceId);
    URI baseUri = baseUriBuilder.build();
    URI profileUri = baseUriBuilder.path("/profile").build();
    
    user.addReflexiveLink(profileUri, HttpMethod.GET, "self");    
    
    if(isAuthorized(DELETE)) {
      user.addReflexiveLink(baseUri, HttpMethod.DELETE, "delete");
    }    
    if(isAuthorized(UPDATE)) {
      user.addReflexiveLink(profileUri, HttpMethod.PUT, "update");
    }
    
    return user;
  }

  public void deleteUser() {    
    ensureNotReadOnly();    
    identityService.deleteUser(resourceId);        
  }
  
  public void updateCredentials(UserCredentialsDto account) {
    ensureNotReadOnly();
    
    User dbUser = findUserObject();
    if(dbUser == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "User with id " + resourceId + " does not exist");
    }
    
    dbUser.setPassword(account.getPassword());
    
    identityService.saveUser(dbUser);    
  }
  
  public void updateProfile(UserProfileDto profile) {
    ensureNotReadOnly();
    
    User dbUser = findUserObject();
    if(dbUser == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "User with id " + resourceId + " does not exist");
    }
    
    profile.update(dbUser);
    
    identityService.saveUser(dbUser);      
  }

  protected User findUserObject() {
    try {      
      return identityService.createUserQuery()
          .userId(resourceId)
          .singleResult();      
    } catch(ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Exception while performing user query: "+e.getMessage());
    }
  }

}
