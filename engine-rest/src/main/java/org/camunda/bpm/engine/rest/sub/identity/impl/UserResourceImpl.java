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

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.identity.UserResource;

/**
 * @author Daniel Meyer
 *
 */
public class UserResourceImpl extends AbstractIdentityResource implements UserResource {
  
  public UserResourceImpl(ProcessEngine processEngine, String userId) {
    super(processEngine, userId);
  }

  public UserDto getUser() {
    
    User dbUser = findUserObject();    
    if(dbUser == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "User with id " + resourceId + " does not exist");
    }
    
    return UserDto.fromUser(dbUser);
  }


  public UserDto updateUser(UserDto user) {    
    ensureNotReadOnly();
    
    User dbUser = findUserObject();    
    if(dbUser == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "User with id " + resourceId + " does not exist");
    }
    
    user.update(dbUser);        
    try {
      identityService.saveUser(dbUser);
      
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Exception while updating user "+resourceId+": "+e.getMessage());
    }
    
    return UserDto.fromUser(dbUser);
  }
  

  public void deleteUser() {    
    ensureNotReadOnly();
    
    try {
      identityService.deleteUser(resourceId);
    } catch(ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Exception while deleting user "+resourceId+": "+e.getMessage());
    }
        
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
