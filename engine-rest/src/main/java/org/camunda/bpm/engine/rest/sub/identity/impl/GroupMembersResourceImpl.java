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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.rest.sub.identity.GroupMembersResource;

/**
 * @author Daniel Meyer
 *
 */
public class GroupMembersResourceImpl extends AbstractIdentityResource implements GroupMembersResource {

  public GroupMembersResourceImpl(ProcessEngine processEngine, String resourceId) {
    super(processEngine, Resources.GROUP_MEMBERSHIP, resourceId);
  }

  public void createGroupMember(String userId) {
    ensureNotReadOnly();
    
    identityService.createMembership(userId, resourceId);      
  }

  public void deleteGroupMember(String userId) {
    ensureNotReadOnly();
    
    identityService.deleteMembership(userId, resourceId);
  }

}
