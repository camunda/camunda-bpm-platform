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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.DbEntity;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationEntity implements Authorization, DbEntity, HasDbRevision, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String id;
  protected int revision;
  
  protected int authorizationType;
  protected int permissions;  
  protected String userId;
  protected String groupId;
  protected Integer resourceType;
  protected String resourceId;  

  public AuthorizationEntity() {
  }
  
  public AuthorizationEntity(int type) {
    this.authorizationType = type;
    
    if(authorizationType == AUTH_TYPE_GLOBAL) {
      this.userId = ANY;
    }
    
    resetPermissions();
  }

  protected void resetPermissions() {
    if(authorizationType == AUTH_TYPE_GLOBAL) {
      this.permissions = Permissions.NONE.getValue();
      
    } else if(authorizationType == AUTH_TYPE_GRANT) {
      this.permissions = Permissions.NONE.getValue();
      
    } else if(authorizationType == AUTH_TYPE_REVOKE) {
      this.permissions = Permissions.ALL.getValue();
      
    } else {
      throw new ProcessEngineException("Unrecognized authorization type '"+authorizationType+"' Must be one of "
          +AUTH_TYPE_GLOBAL+","+AUTH_TYPE_GRANT+", "+AUTH_TYPE_REVOKE);      
    }
  }
  
  // grant / revoke methods ////////////////////////////

  public void addPermission(Permission p) {
    permissions |= p.getValue();
  }
  
  public void removePermission(Permission p) {
    permissions &= ~p.getValue();
  }
  
  public boolean isPermissionGranted(Permission p) {
    if(AUTH_TYPE_REVOKE == authorizationType) {
      throw new IllegalStateException("Method isPermissionGranted cannot be used for authorization type REVOKE.");
    }        
    return (permissions & p.getValue()) == p.getValue();    
  }
  
  public boolean isPermissionRevoked(Permission p) {
    if(AUTH_TYPE_GRANT == authorizationType) {
      throw new IllegalStateException("Method isPermissionRevoked cannot be used for authorization type GRANT.");
    }    
    return (permissions & p.getValue()) != p.getValue();    
  }
  
  public Permission[] getPermissions(Permission[] permissions) {

    List<Permission> result = new ArrayList<Permission>();
        
    for (Permission permission : permissions) {
      if((AUTH_TYPE_GLOBAL == authorizationType || AUTH_TYPE_GRANT == authorizationType) 
          && isPermissionGranted(permission)) {
        
        result.add(permission);
        
      } else if(AUTH_TYPE_REVOKE == authorizationType 
          && isPermissionRevoked(permission)) {
        
        result.add(permission);
        
      }
    }
    return result.toArray(new Permission[ result.size() ]);
  }
  
  public void setPermissions(Permission[] permissions) {
    resetPermissions();
    for (Permission permission : permissions) {
      if(AUTH_TYPE_REVOKE == authorizationType) {
        removePermission(permission);
        
      } else {
        addPermission(permission);
        
      }
    }    
  }
  
  // getters setters ///////////////////////////////

  public int getAuthorizationType() {
    return authorizationType;
  }
  
  public void setAuthorizationType(int authorizationType) {
    this.authorizationType = authorizationType;
  }
  
  public String getGroupId() {
    return groupId;
  }
  
  public void setGroupId(String groupId) {
    if(groupId != null && authorizationType == AUTH_TYPE_GLOBAL) {
      throw new ProcessEngineException("Cannot use groupId for GLOBAL authorization.");
    }
    this.groupId = groupId;
  }
  
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    if(userId != null && authorizationType == AUTH_TYPE_GLOBAL && !ANY.equals(userId)) {
      throw new ProcessEngineException("Illegal value "+userId+" for userId for GLOBAL authorization. must be '"+ANY+"'.");
    }
    this.userId = userId;
  }

  public int getResourceType() {
    return resourceType;
  }
  
  public void setResourceType(int type) {
    this.resourceType = type;
  }
  
  public Integer getResource() {
    return resourceType;
  }

  public void setResource(Resource resource) {
    this.resourceType = resource.resourceType();
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }

  public void setPermissions(int permissions) {
    this.permissions = permissions;
  }

  public int getPermissions() {
    return permissions;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public Object getPersistentState() {
        
    HashMap<String, Object> state = new HashMap<String, Object>();
    state.put("userId", userId);
    state.put("groupId", groupId);
    state.put("resourceType", resourceType);
    state.put("resourceId", resourceId);
    state.put("permissions", permissions);
    
    return state;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", revision=" + revision
           + ", authorizationType=" + authorizationType
           + ", permissions=" + permissions
           + ", userId=" + userId
           + ", groupId=" + groupId
           + ", resourceType=" + resourceType
           + ", resourceId=" + resourceId
           + "]";
  }
}
