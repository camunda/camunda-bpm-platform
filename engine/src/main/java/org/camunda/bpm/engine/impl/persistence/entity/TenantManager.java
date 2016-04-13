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

  public void configureQuery(ListQueryParameterObject query) {
    final Authentication currentAuthentication = getCurrentAuthentication();

    TenantCheck tenantCheck = query.getTenantCheck();

    if (Context.getProcessEngineConfiguration().isTenantCheckEnabled() && currentAuthentication != null) {
      tenantCheck.setTenantCheckEnabled(true);
      tenantCheck.setAuthTenantIds(currentAuthentication.getTenantIds());

    } else {
      tenantCheck.setTenantCheckEnabled(false);
      tenantCheck.setAuthTenantIds(null);
    }
  }

}
