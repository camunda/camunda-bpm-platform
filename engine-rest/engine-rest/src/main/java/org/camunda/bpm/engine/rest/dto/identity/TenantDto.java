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
package org.camunda.bpm.engine.rest.dto.identity;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.identity.Tenant;

public class TenantDto {

  protected String id;
  protected String name;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static TenantDto fromTenant(Tenant tenant) {
    TenantDto dto = new TenantDto();
    dto.id = tenant.getId();
    dto.name = tenant.getName();
    return dto;
  }

  public static List<TenantDto> fromTenantList(List<Tenant> tenants) {
    List<TenantDto> dtos = new ArrayList<TenantDto>();
    for (Tenant tenant : tenants) {
      dtos.add(fromTenant(tenant));
    }
    return dtos;
  }

  public void update(Tenant tenant) {
    tenant.setId(id);
    tenant.setName(name);
  }

}
