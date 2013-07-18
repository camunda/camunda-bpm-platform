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

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.identity.Authorization;
import org.camunda.bpm.engine.identity.Permission;
import org.camunda.bpm.engine.identity.Permissions;
import org.camunda.bpm.engine.identity.Resource;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.AuthorizationQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<Authorization> selectAuthorizationByQueryCriteria(AuthorizationQueryImpl authorizationQuery) {    
    return getDbSqlSession().selectList("selectAuthorizationByQueryCriteria", authorizationQuery);    
  }
  
  public Long selectAuthorizationCountByQueryCriteria(AuthorizationQueryImpl authorizationQuery) {
    return (Long) getDbSqlSession().selectOne("selectAuthorizationCountByQueryCriteria", authorizationQuery);
  }

  public void update(AuthorizationEntity authorization) {
    getDbSqlSession().update(authorization);    
  }

  public AuthorizationEntity selectAuthorizationById(String authorizationId) {
    return getDbSqlSession().selectById(AuthorizationEntity.class, authorizationId);
  }
  
  public void configureQuery(@SuppressWarnings("rawtypes") AbstractQuery query, Resource resource) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    String authenticatedUserId = Authentication.getAuthenticatedUserId();
    
    if(processEngineConfiguration.isAuthorizationChecksEnabled() && authenticatedUserId != null) {
      query.setAuthorizationCheckEnabled(true);
      query.setAuthUserId(authenticatedUserId);
      query.setAuthResourceType(resource.resourceType());
      query.setAuthResourceId("RES.ID_");
      query.setAuthPerms(Permissions.READ.getId());
    }
    
  }

  public void checkAuthorization(Permission permission, Resource resource, String resourceId) {
    
    final String authenticatedUserId = Authentication.getAuthenticatedUserId();
    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    if (processEngineConfiguration.isAuthorizationChecksEnabled() && authenticatedUserId != null) {

      boolean isAuthorized = isAuthorized(authenticatedUserId, null, permission, resource, resourceId);
      if (!isAuthorized) {
        throw new AuthorizationException(authenticatedUserId, permission.getName(), resource.resourceName(), resourceId);
      }
    }

  }

  public boolean isAuthorized(String userId, List<String> groupIds, Permission permission, Resource resource, String resourceId) {

    AuthorizationCheck authCheck = new AuthorizationCheck();
    authCheck.setAuthUserId(userId);
    authCheck.setAuthGroupIds(groupIds);
    authCheck.setAuthResourceType(resource.resourceType());
    authCheck.setAuthResourceId(resourceId);
    authCheck.setAuthPerms(permission.getId());
    
    return getDbSqlSession().selectBoolean("isUserAuthorizedForResource", authCheck);
  }

  
}
