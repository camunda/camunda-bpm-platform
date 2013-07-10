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
import org.camunda.bpm.engine.rest.dto.identity.CreateGroupMemberDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.identity.GroupMemberResource;

/**
 * @author Daniel Meyer
 *
 */
public class GroupMemberResourceImpl extends AbstractIdentityResource implements GroupMemberResource {

  public GroupMemberResourceImpl(ProcessEngine processEngine, String resourceId) {
    super(processEngine, resourceId);
  }

  public void createGroupMember(CreateGroupMemberDto groupMemberDto) {
    ensureNotReadOnly();
    
    try {
      identityService.createMembership(groupMemberDto.getUserId(), resourceId);
      
    } catch(Exception e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Exception adding user to group "+resourceId+": "+e.getMessage());
    }
  }

  public void deleteGroupMember(String userId) {
    ensureNotReadOnly();
    
    try {
      identityService.deleteMembership(userId, resourceId);
      
    } catch(Exception e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Exception removing user form group "+resourceId+": "+e.getMessage());
      
    }

  }

}
