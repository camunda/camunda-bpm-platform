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
package org.camunda.bpm.engine.identity;

/**
 * <p>An {@link Authorization} assigns a set of permissions to a given 
 * user or group to interact with a given resource.</p>
 *  
 * <h2>Permissions</h2>
 * <p>A permission defines the way a user or group is allowed to interact 
 * with a certain resource. Examples of permissions are READ, WRITE, UPDATE, ...</p> 
 * <p>A permission is either granted or revoked. Permissions are represented as 
 * integers (1,2,4,8,16,...) See: {@link Permissions}. </p>
 * 
 * <p>A single authorization object may assign multiple permissions to a single user 
 * and resource:</p>
 * <pre>
 * authorization.addPermission(Permissions.READ);
 * authorization.addPermission(Permissions.WRITE);
 * authorization.addPermission(Permissions.DELETE);
 * </pre>
 *  
 * 
 * <h2>Resources</h2>
 * <p>Resources are entities for which a user or a group is authorized. Examples of 
 * resources are applications, process-definitions, process-instances, tasks ...</p> 
 *   
 * 
 * @author Daniel Meyer
 * 
 */
public interface Authorization {
    
  /** The identifyer used for relating to all users / resourceTypes / resourceIds.
   * NOTE: cannot be used for groups. */
  public static final String ANY = "*";
  
  /** allows granting a permission. Out-of-the-box constants can be found in {@link Permissions}.
   * */
  public void addPermission(Permission permission);
  
  /** allows removing a permission. Out-of-the-box constants can be found in {@link Permissions}.
   * */
  public void removePermission(Permission permission);
  
  /**
   * Allows checking for a permission. Out-of-the-box constants can be found in {@link Permissions}.
   * 
   * @param perm the permission to check for
   */
  public boolean hasPermission(Permission permission);
  
  /** returns the permissions granted by this authorization */
  public int getPermissions();
  
  
  /** @return the ID of the {@link Authorization} object */
  public String getId();

  /**
   * set the id of the resource
   */
  public void setResourceId(String resourceId);

  /** 
   * @return the id of the resource
   */
  public String getResourceId();

  /**
   * sets the type of the resource
   */
  public void setResourceType(String resourceId);

  /**
   * @return the type of the resource
   */
  public String getResourceType();

  /**
   * set the id of the user this authorization is created for  
   */
  public void setUserId(String userId);

  /**
   * @return the id of the user this authorization is created for
   */
  public String getUserId();

  /**
   * set the id of the group this authorization is created for  
   */
  public void setGroupId(String groupId);

  /**
   * @return the id of the group this authorization is created for
   */
  public String getGroupId();
    
}
