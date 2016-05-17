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

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.TenantCheck;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

/**
 * @author Kristin Polenz
 *
 */
public class TenantManager extends AbstractManager {

  public ListQueryParameterObject configureQuery(ListQueryParameterObject query) {
    TenantCheck tenantCheck = query.getTenantCheck();

    configureTenantCheck(tenantCheck);

    return query;
  }

  public void configureTenantCheck(TenantCheck tenantCheck) {
    if (isTenantCheckEnabled()) {
      Authentication currentAuthentication = getCurrentAuthentication();

      tenantCheck.setTenantCheckEnabled(true);
      tenantCheck.setAuthTenantIds(currentAuthentication.getTenantIds());

    } else {
      tenantCheck.setTenantCheckEnabled(false);
      tenantCheck.setAuthTenantIds(null);
    }
  }

  public ListQueryParameterObject configureQuery(Object parameters) {
    ListQueryParameterObject queryObject = new ListQueryParameterObject();
    queryObject.setParameter(parameters);

    return configureQuery(queryObject);
  }

  public boolean isAuthenticatedTenant(String tenantId) {
    if (tenantId != null && isTenantCheckEnabled()) {

      Authentication currentAuthentication = getCurrentAuthentication();
      List<String> authenticatedTenantIds = currentAuthentication.getTenantIds();
      if (authenticatedTenantIds != null) {
        return authenticatedTenantIds.contains(tenantId);

      } else {
        return false;
      }
    } else {
      return true;
    }
  }

  public boolean isTenantCheckEnabled() {
    return Context.getProcessEngineConfiguration().isTenantCheckEnabled()
        && Context.getCommandContext().isTenantCheckEnabled()
        && getCurrentAuthentication() != null
        && !getAuthorizationManager().isCamundaAdmin(getCurrentAuthentication());
  }

}
