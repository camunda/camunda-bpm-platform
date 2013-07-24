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
package org.camunda.bpm.engine.impl.cfg.auth;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;

/**
 * <p>Provides the default authorizations for camunda BPM.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class DefaultAuthorizationProvider implements ResourceAuthorizationProvider {
  
  public AuthorizationEntity[] newUser(User user) {
    
    // create an authorization which gives the user all permissions on himself:
    AuthorizationEntity resourceOwnerAuthorization = new AuthorizationEntity(AUTH_TYPE_GRANT);
    resourceOwnerAuthorization.setUserId(user.getId());
    resourceOwnerAuthorization.setResource(USER);
    resourceOwnerAuthorization.setResourceId(user.getId());
    resourceOwnerAuthorization.addPermission(ALL);
    
    return new AuthorizationEntity[]{ resourceOwnerAuthorization };
  }

  public AuthorizationEntity[] newGroup(Group group) {

    List<AuthorizationEntity> authorizations = new ArrayList<AuthorizationEntity>();

    // whenever a new group is created, all users part of the
    // group are granted READ permissions on the group
    AuthorizationEntity groupMemberAuthorization = new AuthorizationEntity(AUTH_TYPE_GRANT);
    groupMemberAuthorization.setGroupId(group.getId());
    groupMemberAuthorization.setResource(GROUP);
    groupMemberAuthorization.setResourceId(group.getId());
    groupMemberAuthorization.addPermission(READ);
    authorizations.add(groupMemberAuthorization);

    return authorizations.toArray(new AuthorizationEntity[0]);
  }

  public AuthorizationEntity[] groupMembershipCreated(String groupId, String userId) {
    
    // no default authorizations on memberships. 
    
    return null;
  }

}
