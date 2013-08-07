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
package org.camunda.bpm.engine.impl.identity;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.interceptor.Session;

/**
 * <p>SPI Interface for identity service implementations which offer 
 * read / write access to the user database.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public interface WritableIdentityProvider extends Session {

  // users /////////////////////////////////////////////////
  
  /** 
   * <p>Returns a new (transient) {@link User} object. The Object is not 
   * yet persistent and must be saved using the {@link #saveUser(User)} 
   * method.</p>
   * 
   * <p>NOTE: the implementation does not validate the uniqueness of the userId 
   * parameter at this time.</p>
   *  
   * @param userId
   * @return an non-persistent user object.
   */
  public User createNewUser(String userId);
  
  /**
   * Allows saving or updates a {@link User} object
   *  
   * @param user a User object. 
   * @return the User object.
   * @throws IdentityProviderException in case an internal error occurs
   */
  public User saveUser(User user);
    
  /**
   * Allows deleting a persistent {@link User} object.
   *  
   * @param UserId the id of the User object to delete.
   * @throws IdentityProviderException in case an internal error occurs
   */
  public void deleteUser(String userId);
  
  
  // groups /////////////////////////////////////////////////
  
  /** 
   * <p>Returns a new (transient) {@link Group} object. The Object is not 
   * yet persistent and must be saved using the {@link #saveGroup(Group)} 
   * method.</p>
   * 
   * <p>NOTE: the implementation does not validate the uniqueness of the groupId 
   * parameter at this time.</p>
   *  
   * @param groupId
   * @return an non-persistent group object.
   */
  public Group createNewGroup(String groupId);
  
  /**
   * Allows saving a {@link Group} object which is not yet persistent.
   *  
   * @param group a group object. 
   * @return the persistent group object.
   * @throws IdentityProviderException in case an internal error occurs
   */
  public Group saveGroup(Group group);
    
  /**
   * Allows deleting a persistent {@link Group} object.
   *  
   * @param groupId the id of the group object to delete.   * 
   * @throws IdentityProviderException in case an internal error occurs
   */
  public void deleteGroup(String groupId);
  
  // Membership ///////////////////////////////////////////////
  
  /**
   * Creates a membership relation between a user and a group. If the user is already part of that group,
   * IdentityProviderException is thrown.
   * 
   * @param userId the id of the user
   * @param groupId id of the group
   * @throws IdentityProviderException
   */
  public void createMembership(String userId, String groupId);

  /**
   * Creates a membership relation between a user and a group. If the user is not part of that group,
   * IdentityProviderException is thrown.
   * 
   * @param userId the id of the user
   * @param groupId id of the group
   * @throws IdentityProviderException
   */
  public void deleteMembership(String userId, String groupId);
  
}
