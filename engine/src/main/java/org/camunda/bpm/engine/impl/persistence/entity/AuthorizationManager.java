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

import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.AuthorizationQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

/**
 * @author Daniel Meyer
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AuthorizationManager extends AbstractManager {

  public Authorization createNewAuthorization(int type) {
    checkAuthorization(CREATE, AUTHORIZATION, null);
    return new AuthorizationEntity(type);
  }

  public void insert(DbEntity authorization) {
    checkAuthorization(CREATE, AUTHORIZATION, null);
    getDbSqlSession().insert(authorization);
  }

  public List<Authorization> selectAuthorizationByQueryCriteria(AuthorizationQueryImpl authorizationQuery) {
    configureQuery(authorizationQuery, AUTHORIZATION);
    return getDbSqlSession().selectList("selectAuthorizationByQueryCriteria", authorizationQuery);
  }

  public Long selectAuthorizationCountByQueryCriteria(AuthorizationQueryImpl authorizationQuery) {
    configureQuery(authorizationQuery, AUTHORIZATION);
    return (Long) getDbSqlSession().selectOne("selectAuthorizationCountByQueryCriteria", authorizationQuery);
  }

  public void update(AuthorizationEntity authorization) {
    checkAuthorization(UPDATE, AUTHORIZATION, authorization.getId());
    getDbSqlSession().merge(authorization);
  }

  public void delete(DbEntity authorization) {
    checkAuthorization(DELETE, AUTHORIZATION, authorization.getId());
    deleteAuthorizationsByResourceId(AUTHORIZATION, authorization.getId());
    super.delete(authorization);
  }

  // authorization checks ///////////////////////////////////////////

  public void configureQuery(AbstractQuery query, Resource resource) {
    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    final Authentication currentAuthentication = Context.getCommandContext().getAuthentication();

    if(processEngineConfiguration.isAuthorizationEnabled() && currentAuthentication != null) {
      query.setAuthorizationCheckEnabled(true);
      query.setAuthUserId(currentAuthentication.getUserId());
      query.setAuthGroupIds(currentAuthentication.getGroupIds());
      query.setAuthResourceType(resource.resourceType());
      query.setAuthResourceIdQueryParam("RES.ID_");
      query.setAuthPerms(Permissions.READ.getValue());
    }

  }

  public void checkAuthorization(Permission permission, Resource resource, String resourceId) {

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    final Authentication currentAuthentication = Context.getCommandContext().getAuthentication();

    if(processEngineConfiguration.isAuthorizationEnabled() && currentAuthentication != null) {

      boolean isAuthorized = isAuthorized(currentAuthentication.getUserId(), currentAuthentication.getGroupIds(), permission, resource, resourceId);
      if (!isAuthorized) {
        throw new AuthorizationException(currentAuthentication.getUserId(), permission.getName(), resource.resourceName(), resourceId);
      }
    }

  }

  public boolean isAuthorized(String userId, List<String> groupIds, Permission permission, Resource resource, String resourceId) {

    AuthorizationCheck authCheck = new AuthorizationCheck();
    authCheck.setAuthUserId(userId);
    authCheck.setAuthGroupIds(groupIds);
    authCheck.setAuthResourceType(resource.resourceType());
    authCheck.setAuthResourceId(resourceId);
    authCheck.setAuthPerms(permission.getValue());

    return getDbSqlSession().selectBoolean("isUserAuthorizedForResource", authCheck);
  }

  public boolean isAuthorized(Permission permission, Resource resource, String resourceId) {

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    final Authentication currentAuthentication = Context.getCommandContext().getAuthentication();

    if(processEngineConfiguration.isAuthorizationEnabled() && currentAuthentication != null) {
      return isAuthorized(currentAuthentication.getUserId(), currentAuthentication.getGroupIds(), permission, resource, resourceId);

    } else {
      return true;

    }
  }

  public void deleteAuthorizationsByResourceId(Resource resource, String resourceId) {

    if(resourceId == null) {
      throw new IllegalArgumentException("Resource id cannot be null");
    }

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if(processEngineConfiguration.isAuthorizationEnabled()) {
      Map<String, Object> deleteParams = new HashMap<String, Object>();
      deleteParams.put("resourceType", resource.resourceType());
      deleteParams.put("resourceId", resourceId);
      getDbSqlSession().delete(AuthorizationEntity.class, "deleteAuthorizationsForResourceId", deleteParams);
    }

  }

}
