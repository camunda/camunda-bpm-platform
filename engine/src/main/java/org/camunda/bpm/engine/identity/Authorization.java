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
 * integers (1,2,4,8,16,...) See: ({@link #PERMISSION_TYPE_ACCESS}, {@link #PERMISSION_TYPE_DELETE},
 * {@link #PERMISSION_TYPE_READ}, {@link #PERMISSION_TYPE_WRITE} ...</p>
 * 
 * <p>A single authorization object may assign multiple permissions to a single user 
 * and resource:</p>
 * <pre>
 * authorization.addPermission(PERMISSION_TYPE_READ);
 * authorization.addPermission(PERMISSION_TYPE_WRITE);
 * authorization.addPermission(PERMISSION_TYPE_DELETE);
 * </pre>
 *  
 * 
 * <h2>Resources</h2>
 * <p>Resources are entities for which a user or a group is authorized. Examples of 
 * resources are applications, process-definitions, process-instances, tasks ...</p> 
 * 
 * <p>A resource has a type and an id. The type ({@link #setResourceType(String)}) 
 * allows to group all resources of the same kind. A resource id is the identifier of 
 * an indivuidual resource instance ({@link #setResourceId(String)}). For example:
 * the resource type could be "processDefinition" and the resource-id could be the 
 * id of an individual process definition.</p>  
 * 
 * @author Daniel Meyer
 * 
 */
public interface Authorization {
  
  /** means that a resource can be accessed.            Value: 1 = (000...00000001) */
  public static final int PERMISSION_TYPE_ACCESS = 1;     
  /** means that a user has READ access to a resource   Value: 2 = (000...00000010)  */
  public static final int PERMISSION_TYPE_READ = 2;       
  /** means that a user has WRITE access to a resource  Value: 4 = (000...00000100) */
  public static final int PERMISSION_TYPE_WRITE = 4;    
  /** means that a user is allowed to DELETE a resource Value: 8 = (000...00001000) */
  public static final int PERMISSION_TYPE_DELETE = 8;
  
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
  public void setResourceType(String resourceType);

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
