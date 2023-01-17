/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.cockpit.plugin.resource;

import java.util.List;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheck;
import org.camunda.bpm.engine.impl.db.TenantCheck;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.webapp.plugin.resource.AbstractAppPluginResource;

/**
 * Base class for implementing a plugin REST resource.
 *
 * @author Daniel Meyer
 *
 */
public class AbstractCockpitPluginResource extends AbstractAppPluginResource<CockpitPlugin> {

  public AbstractCockpitPluginResource(String engineName) {
    super(Cockpit.getRuntimeDelegate(), engineName);
  }

  /**
   * Return a {@link CommandExecutor} for the current
   * engine to execute plugin commands.
   *
   * @return
   */
  protected CommandExecutor getCommandExecutor() {
    return Cockpit.getCommandExecutor(engineName);
  }

  /**
   * Return a {@link QueryService} for the current
   * engine to execute queries against the engine datbase.
   *
   * @return
   */
  protected QueryService getQueryService() {
    return Cockpit.getQueryService(engineName);
  }

  // authorization //////////////////////////////////////////////////////////////

  /**
   * Return <code>true</code> if authorization is enabled.
   */
  protected boolean isAuthorizationEnabled() {
    return getProcessEngine().getProcessEngineConfiguration().isAuthorizationEnabled();
  }

  /**
   * Return <code>true</code> if tenant check is enabled.
   */
  protected boolean isTenantCheckEnabled() {
    return getProcessEngine().getProcessEngineConfiguration().isTenantCheckEnabled()
        && getCurrentAuthentication() != null
        && !isCamundaAdmin(getCurrentAuthentication());
  }

  /**
   * Return <code>true</code> if the given authentication is part of the admin groups or admin users
   */
  protected boolean isCamundaAdmin(Authentication authentication) {
    ProcessEngineConfigurationImpl engineConfiguration =
        (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    List<String> groupIds = authentication.getGroupIds();
    if (groupIds != null) {
      List<String> adminGroups = engineConfiguration.getAdminGroups();
      for (String adminGroup : adminGroups) {
        if (groupIds.contains(adminGroup)) {
          return true;
        }
      }
    }

    String userId = authentication.getUserId();
    if (userId != null) {
      List<String> adminUsers = engineConfiguration.getAdminUsers();
      return adminUsers != null && adminUsers.contains(userId);
    }

    return false;
  }

  /**
   * Return the current authentication.
   */
  protected Authentication getCurrentAuthentication() {
    return getProcessEngine().getIdentityService().getCurrentAuthentication();
  }

  /**
   * Configure the authorization check for the given {@link QueryParameters}.
   */
  protected void configureAuthorizationCheck(QueryParameters query) {
    Authentication currentAuthentication = getCurrentAuthentication();

    AuthorizationCheck authCheck = query.getAuthCheck();

    authCheck.getPermissionChecks().clear();

    if (isAuthorizationEnabled() && currentAuthentication != null) {
      authCheck.setAuthorizationCheckEnabled(true);
      String currentUserId = currentAuthentication.getUserId();
      List<String> currentGroupIds = currentAuthentication.getGroupIds();
      authCheck.setAuthUserId(currentUserId);
      authCheck.setAuthGroupIds(currentGroupIds);
    }
  }

  /**
   * Configure the tenant check for the given {@link QueryParameters}.
   */
  protected void configureTenantCheck(QueryParameters query) {
    TenantCheck tenantCheck = query.getTenantCheck();

    if (isTenantCheckEnabled()) {
      Authentication currentAuthentication = getCurrentAuthentication();

      tenantCheck.setTenantCheckEnabled(true);
      tenantCheck.setAuthTenantIds(currentAuthentication.getTenantIds());
    } else {
      tenantCheck.setTenantCheckEnabled(false);
      tenantCheck.setAuthTenantIds(null);
    }
  }

  /**
   * Add a new {@link PermissionCheck} with the given values.
   */
  protected void addPermissionCheck(QueryParameters query, Resource resource, String queryParam, Permission permission) {
    if(!isPermissionDisabled(permission)){
      PermissionCheck permCheck = new PermissionCheck();
      permCheck.setResource(resource);
      permCheck.setResourceIdQueryParam(queryParam);
      permCheck.setPermission(permission);
      query.getAuthCheck().addAtomicPermissionCheck(permCheck);
    }
  }

  protected boolean isPermissionDisabled(Permission permission) {
    List<String> disabledPermissions = getProcessEngine().getProcessEngineConfiguration().getDisabledPermissions();
    for (String disabledPerm : disabledPermissions) {
      if (!disabledPerm.equals(permission.getName())) {
        return true;
      }
    }
    return false;
  }

}
