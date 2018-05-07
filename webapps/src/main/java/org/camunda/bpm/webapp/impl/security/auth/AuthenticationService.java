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
package org.camunda.bpm.webapp.impl.security.auth;

import static org.camunda.bpm.engine.authorization.Permissions.ACCESS;
import static org.camunda.bpm.engine.authorization.Resources.APPLICATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.webapp.impl.util.ProcessEngineUtil;

public class AuthenticationService {

  public static final String[] APPS = new String[] { "cockpit", "tasklist", "admin"};
  public static final String APP_WELCOME = "welcome";

  public Authentication createAuthenticate(String engineName, String username) {
    return createAuthenticate(engineName, username, null, null);
  }

  public Authentication createAuthenticate(String engineName, String username, List<String> groupIds, List<String> tenantIds) {
    ProcessEngine processEngine = ProcessEngineUtil.lookupProcessEngine(engineName);
    return createAuthenticate(processEngine, username, groupIds, tenantIds);
  }

  public Authentication createAuthenticate(ProcessEngine processEngine, String username, List<String> groupIds, List<String> tenantIds) {

    // make sure authentication is executed without authentication :)
    processEngine.getIdentityService().clearAuthentication();

    if (groupIds == null) {
      groupIds = getGroupsOfUser(processEngine, username);
    }

    if (tenantIds == null) {
      tenantIds = getTenantsOfUser(processEngine, username);
    }

    // check user's app authorizations
    AuthorizationService authorizationService = processEngine.getAuthorizationService();

    HashSet<String> authorizedApps = new HashSet<String>();
    authorizedApps.add(APP_WELCOME);

    if (processEngine.getProcessEngineConfiguration().isAuthorizationEnabled()) {
      for (String application: APPS) {
        if (isAuthorizedForApp(authorizationService, username, groupIds, application)) {
          authorizedApps.add(application);
        }
      }

    } else {
      Collections.addAll(authorizedApps, APPS);
    }

    // create new authentication
    UserAuthentication newAuthentication = new UserAuthentication(username, processEngine.getName());
    newAuthentication.setGroupIds(groupIds);
    newAuthentication.setTenantIds(tenantIds);
    newAuthentication.setAuthorizedApps(authorizedApps);

    return newAuthentication;
  }

  public List<String> getTenantsOfUser(ProcessEngine engine, String userId) {
    List<Tenant> tenants = engine.getIdentityService().createTenantQuery()
      .userMember(userId)
      .includingGroupsOfUser(true)
      .list();

    List<String> tenantIds = new ArrayList<String>();
    for(Tenant tenant : tenants) {
      tenantIds.add(tenant.getId());
    }
    return tenantIds;
  }

  public List<String> getGroupsOfUser(ProcessEngine engine, String userId) {
    List<Group> groups = engine.getIdentityService().createGroupQuery()
      .groupMember(userId)
      .list();

    List<String> groupIds = new ArrayList<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

  protected boolean isAuthorizedForApp(AuthorizationService authorizationService, String username, List<String> groupIds, String application) {
    return authorizationService.isUserAuthorized(username, groupIds, ACCESS, APPLICATION, application);
  }

}
