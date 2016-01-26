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
package org.camunda.bpm.cockpit.plugin.resource;

import java.util.List;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.db.PermissionCheck;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.webapp.plugin.resource.AbstractAppPluginResource;

/**
 * Base class for implementing a pluigin REST resource.
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
   * Return <code>true</code> iff authorization is enabled.
   */
  protected boolean isAuthorizationEnabled() {
    return getProcessEngine().getProcessEngineConfiguration().isAuthorizationEnabled();
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
  protected void configureAuthorizationCheck(QueryParameters<?> query) {
    Authentication currentAuthentication = getCurrentAuthentication();

    query.getPermissionChecks().clear();

    if (isAuthorizationEnabled() && currentAuthentication != null) {
      query.setAuthorizationCheckEnabled(true);
      String currentUserId = currentAuthentication.getUserId();
      List<String> currentGroupIds = currentAuthentication.getGroupIds();
      query.setAuthUserId(currentUserId);
      query.setAuthGroupIds(currentGroupIds);
    }
  }

  /**
   * Add a new {@link PermissionCheck} with the given values.
   */
  protected void addPermissionCheck(QueryParameters<?>  query, Resource resource, String queryParam, Permission permission) {
    PermissionCheck permCheck = new PermissionCheck();
    permCheck.setResource(resource);
    permCheck.setResourceIdQueryParam(queryParam);
    permCheck.setPermission(permission);
    query.addAtomicPermissionCheck(permCheck);
  }

}
