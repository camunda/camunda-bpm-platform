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

package org.camunda.bpm.engine.test.api.multitenancy;

import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderCaseInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderHistoricDecisionInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderProcessInstanceContext;

/**
 * Provide the given tenant id for all instances.
 */
public class StaticTenantIdTestProvider implements TenantIdProvider {

  protected final String tenantId;

  public StaticTenantIdTestProvider(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
    return tenantId;
  }

  @Override
  public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
    return tenantId;
  }

  @Override
  public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
    return tenantId;
  }
}