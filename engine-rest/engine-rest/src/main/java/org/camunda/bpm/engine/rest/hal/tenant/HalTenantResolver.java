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
package org.camunda.bpm.engine.rest.hal.tenant;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.cache.HalIdResourceCacheLinkResolver;

public class HalTenantResolver extends HalIdResourceCacheLinkResolver {

  @Override
  protected Class<?> getHalResourceClass() {
    return HalTenant.class;
  }

  @Override
  protected List<HalResource<?>> resolveNotCachedLinks(String[] linkedIds, ProcessEngine processEngine) {
    IdentityService identityService = processEngine.getIdentityService();

    List<Tenant> tenants = identityService
        .createTenantQuery()
        .tenantIdIn(linkedIds)
        .list();

    List<HalResource<?>> resolvedTenants = new ArrayList<HalResource<?>>();
    for (Tenant tenant : tenants) {
      resolvedTenants.add(HalTenant.fromTenant(tenant));
    }

    return resolvedTenants;
  }

}
