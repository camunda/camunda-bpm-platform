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
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TenantQueryDto extends AbstractQueryDto<TenantQuery> {

  private static final String SORT_BY_TENANT_ID_VALUE = "id";
  private static final String SORT_BY_TENANT_NAME_VALUE = "name";

  private static final List<String> VALID_SORT_BY_VALUES;

  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_NAME_VALUE);
  }

  protected String id;
  protected String name;
  protected String nameLike;
  protected String userId;
  protected String groupId;
  protected Boolean includingGroupsOfUser;

  public TenantQueryDto() {
  }

  public TenantQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("id")
  public void setId(String id) {
    this.id = id;
  }

  @CamundaQueryParam("name")
  public void setName(String name) {
    this.name = name;
  }

  @CamundaQueryParam("nameLike")
  public void setNameLike(String nameLike) {
    this.nameLike = nameLike;
  }

  @CamundaQueryParam("userMember")
  public void setUserMember(String userId) {
    this.userId = userId;
  }

  @CamundaQueryParam("groupMember")
  public void setGroupMember(String groupId) {
    this.groupId = groupId;
  }

  @CamundaQueryParam(value = "includingGroupsOfUser", converter = BooleanConverter.class)
  public void setIncludingGroupsOfUser(Boolean includingGroupsOfUser) {
    this.includingGroupsOfUser = includingGroupsOfUser;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected TenantQuery createNewQuery(ProcessEngine engine) {
    return engine.getIdentityService().createTenantQuery();
  }

  @Override
  protected void applyFilters(TenantQuery query) {
    if (id != null) {
      query.tenantId(id);
    }
    if (name != null) {
      query.tenantName(name);
    }
    if (nameLike != null) {
      query.tenantNameLike(nameLike);
    }
    if (userId != null) {
      query.userMember(userId);
    }
    if (groupId != null) {
      query.groupMember(groupId);
    }
    if (Boolean.TRUE.equals(includingGroupsOfUser)) {
      query.includingGroupsOfUser(true);
    }
  }

  @Override
  protected void applySortBy(TenantQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_TENANT_ID_VALUE)) {
      query.orderByTenantId();
    } else if (sortBy.equals(SORT_BY_TENANT_NAME_VALUE)) {
      query.orderByTenantName();
    }
  }

}
