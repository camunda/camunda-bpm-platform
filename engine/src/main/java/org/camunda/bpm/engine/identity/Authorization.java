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
 * <p>An {@link Authorization} represents the permission for a given user or group 
 * to access, modify, delete (...) a given resource.</p>
 *  
 * <h2>Permissions</h2>
 * <p></p>
 * 
 * <h2>Resources</h2>
 * <p></p>
 * 
 * @author Daniel Meyer
 * 
 */
public interface Authorization {
  
  /** means that a resource can be accessed. */
  public static final int PERMISSION_TYPE_ACCESS = 1;    // 000...00000001  
  /** means that a user has READ access to a resource  */
  public static final int PERMISSION_TYPE_READ = 2;      // 000...00000010  
  /** means that a user has WRITE access to a resource */
  public static final int PERMISSION_TYPE_WRITE = 4;     // 000...00000100
  /** means that a user is allowed to DELETE a resource */
  public static final int PERMISSION_TYPE_DELETE = 8;    // 000...00001000
  
  /** The identifier to be used for referring to all users. Adding an 
   * {@link Authorization} for this userId makes sure it is applied 
   * to all users */
  public static final String ANY_USER_ID = "*";
  
  /** The identifier to be used for referring to all groups. Adding an 
   * Authorization for this group Id allows applying it to all groups. */
  public static final String ANY_GROUP_ID = "*";
  
  /** allows granting a permission. Out-of-the-box constants:
   * <ul>
   * <li> {@link #PERMISSION_TYPE_ACCESS} </li>
   * <li> {@link #PERMISSION_TYPE_READ} </li>
   * <li> {@link #PERMISSION_TYPE_WRITE} </li>
   * <li> {@link #PERMISSION_TYPE_DELETE} </li>  
   * </ul>
   * */
  public void addPermission(int perm);
  
  /** allows removing a permission. Out-of-the-box constants:
   * <ul>
   * <li> {@link #PERMISSION_TYPE_ACCESS} </li>
   * <li> {@link #PERMISSION_TYPE_READ} </li>
   * <li> {@link #PERMISSION_TYPE_WRITE} </li>
   * <li> {@link #PERMISSION_TYPE_DELETE} </li> 
   * </ul>
   * */
  public void removePermission(int perm);
  
  /**
   * Allows checking whether a permission is set by this {@link Authorization}. 
   * 
   * @param perm the permission to check for
   */
  public boolean hasPermission(int perm);
  
  /** returns the permissions granted by this authorization */
  public int getPermissions();
  
  
  /** @return the ID of the {@link Authorization} object */
  public String getId();

  public void setResourceId(String resourceId);

  public String getResourceId();

  public void setResourceType(String resourceType);

  public String getResourceType();

  public void setUserId(String userId);

  public String getUserId();

  public void setGroupId(String groupId);

  public String getGroupId();
    
}
