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
package org.camunda.bpm.engine.rest.dto.repository;

import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeploymentQueryDto extends AbstractQueryDto<DeploymentQuery> {

  private static final String SORT_BY_ID_VALUE = "id";
  private static final String SORT_BY_NAME_VALUE = "name";
  private static final String SORT_BY_DEPLOYMENT_TIME_VALUE = "deploymentTime";
  private static final String SORT_BY_TENANT_ID = "tenantId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEPLOYMENT_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
  }

  private String id;
  private String name;
  private String nameLike;
  private String source;
  private Boolean withoutSource;
  private Date before;
  private Date after;
  private List<String> tenantIds;
  private Boolean withoutTenantId;
  private Boolean includeDeploymentsWithoutTenantId;

  public DeploymentQueryDto() {
  }

  public DeploymentQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
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

  @CamundaQueryParam("source")
  public void setSource(String source) {
    this.source = source;
  }

  @CamundaQueryParam(value = "withoutSource", converter = BooleanConverter.class)
  public void setWithoutSource(Boolean withoutSource) {
    this.withoutSource = withoutSource;
  }

  @CamundaQueryParam(value = "before", converter = DateConverter.class)
  public void setDeploymentBefore(Date deploymentBefore) {
    this.before = deploymentBefore;
  }

  @CamundaQueryParam(value = "after", converter = DateConverter.class)
  public void setDeploymentAfter(Date deploymentAfter) {
    this.after = deploymentAfter;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam(value = "includeDeploymentsWithoutTenantId", converter = BooleanConverter.class)
  public void setIncludeDeploymentsWithoutTenantId(Boolean includeDeploymentsWithoutTenantId) {
    this.includeDeploymentsWithoutTenantId = includeDeploymentsWithoutTenantId;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected DeploymentQuery createNewQuery(ProcessEngine engine) {
    return engine.getRepositoryService().createDeploymentQuery();
  }

  @Override
  protected void applyFilters(DeploymentQuery query) {
    if (withoutSource != null && withoutSource && source != null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "The query parameters \"withoutSource\" and \"source\" cannot be used in combination.");
    }

    if (id != null) {
      query.deploymentId(id);
    }
    if (name != null) {
      query.deploymentName(name);
    }
    if (nameLike != null) {
      query.deploymentNameLike(nameLike);
    }
    if (TRUE.equals(withoutSource)) {
      query.deploymentSource(null);
    }
    if (source != null) {
      query.deploymentSource(source);
    }
    if (before != null) {
      query.deploymentBefore(before);
    }
    if (after != null) {
      query.deploymentAfter(after);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (TRUE.equals(includeDeploymentsWithoutTenantId)) {
      query.includeDeploymentsWithoutTenantId();
    }
  }

  @Override
  protected void applySortBy(DeploymentQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_ID_VALUE)) {
      query.orderByDeploymentId();
    } else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
      query.orderByDeploymentName();
    } else if (sortBy.equals(SORT_BY_DEPLOYMENT_TIME_VALUE)) {
      query.orderByDeploymentTime();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
