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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.IntegerConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ProcessDefinitionQueryDto extends AbstractQueryDto<ProcessDefinitionQuery> {

  private static final String SORT_BY_CATEGORY_VALUE = "category";
  private static final String SORT_BY_KEY_VALUE = "key";
  private static final String SORT_BY_ID_VALUE = "id";
  private static final String SORT_BY_NAME_VALUE = "name";
  private static final String SORT_BY_VERSION_VALUE = "version";
  private static final String SORT_BY_DEPLOYMENT_ID_VALUE = "deploymentId";
  private static final String SORT_BY_DEPLOY_TIME_VALUE = "deployTime";
  private static final String SORT_BY_TENANT_ID = "tenantId";
  private static final String SORT_BY_VERSION_TAG = "versionTag";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_CATEGORY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_KEY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_VERSION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEPLOYMENT_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID);
    VALID_SORT_BY_VALUES.add(SORT_BY_VERSION_TAG);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEPLOY_TIME_VALUE);
  }

  protected String processDefinitionId;
  protected List<String> processDefinitionIdIn;
  protected String category;
  protected String categoryLike;
  protected String name;
  protected String nameLike;
  protected String deploymentId;
  protected Date deployedAfter;
  protected Date deployedAt;
  protected String key;
  protected String keyLike;
  protected Integer version;
  protected Boolean latestVersion;
  protected String resourceName;
  protected String resourceNameLike;
  protected String startableBy;
  protected Boolean active;
  protected Boolean suspended;
  protected String incidentId;
  protected String incidentType;
  protected String incidentMessage;
  protected String incidentMessageLike;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;
  protected Boolean includeDefinitionsWithoutTenantId;
  protected String versionTag;
  protected String versionTagLike;
  protected Boolean withoutVersionTag;
  protected List<String> keys;
  protected Boolean startableInTasklist;
  protected Boolean notStartableInTasklist;
  protected Boolean startablePermissionCheck;

  public ProcessDefinitionQueryDto() {

  }

  public ProcessDefinitionQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam(value = "processDefinitionIdIn", converter = StringListConverter.class)
  public void setProcessDefinitionIdIn(List<String> processDefinitionIdIn) {
    this.processDefinitionIdIn = processDefinitionIdIn;
  }

  @CamundaQueryParam("category")
  public void setCategory(String category) {
    this.category = category;
  }

  @CamundaQueryParam("categoryLike")
  public void setCategoryLike(String categoryLike) {
    this.categoryLike = categoryLike;
  }

  @CamundaQueryParam("name")
  public void setName(String name) {
    this.name = name;
  }

  @CamundaQueryParam("nameLike")
  public void setNameLike(String nameLike) {
    this.nameLike = nameLike;
  }

  @CamundaQueryParam("deploymentId")
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  @CamundaQueryParam(value = "deployedAfter", converter = DateConverter.class)
  public void setDeployedAfter(Date deployedAfter) {
    this.deployedAfter = deployedAfter;
  }

  @CamundaQueryParam(value = "deployedAt", converter = DateConverter.class)
  public void setDeployedAt(Date deployedAt) {
    this.deployedAt = deployedAt;
  }

  @CamundaQueryParam("key")
  public void setKey(String key) {
    this.key = key;
  }


  @CamundaQueryParam(value = "keysIn", converter = StringListConverter.class)
  public void setKeysIn(List<String> keys) {
    this.keys = keys;
  }

  @CamundaQueryParam("keyLike")
  public void setKeyLike(String keyLike) {
    this.keyLike = keyLike;
  }

  /**
   * @deprecated use {@link #setVersion(Integer)}
   */
  @Deprecated
  @CamundaQueryParam(value = "ver", converter = IntegerConverter.class)
  public void setVer(Integer ver) {
    setVersion(ver);
  }

  @CamundaQueryParam(value = "version", converter = IntegerConverter.class)
  public void setVersion(Integer version) {
    this.version = version;
  }

  /**
   * @deprecated use {@link #setLatestVersion(Boolean)}
   */
  @Deprecated
  @CamundaQueryParam(value = "latest", converter = BooleanConverter.class)
  public void setLatest(Boolean latest) {
    setLatestVersion(latest);
  }

  @CamundaQueryParam(value = "latestVersion", converter = BooleanConverter.class)
  public void setLatestVersion(Boolean latestVersion) {
    this.latestVersion = latestVersion;
  }

  @CamundaQueryParam("resourceName")
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  @CamundaQueryParam("resourceNameLike")
  public void setResourceNameLike(String resourceNameLike) {
    this.resourceNameLike = resourceNameLike;
  }

  @CamundaQueryParam("startableBy")
  public void setStartableBy(String startableBy) {
    this.startableBy = startableBy;
  }

  @CamundaQueryParam(value = "active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value = "suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value = "incidentId")
  public void setIncidentId(String incidentId) {
    this.incidentId = incidentId;
  }

  @CamundaQueryParam(value = "incidentType")
  public void setIncidentType(String incidentType) {
    this.incidentType = incidentType;
  }

  @CamundaQueryParam(value = "incidentMessage")
  public void setIncidentMessage(String incidentMessage) {
    this.incidentMessage = incidentMessage;
  }

  @CamundaQueryParam(value = "incidentMessageLike")
  public void setIncidentMessageLike(String incidentMessageLike) {
    this.incidentMessageLike = incidentMessageLike;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam(value = "includeProcessDefinitionsWithoutTenantId", converter = BooleanConverter.class)
  public void setIncludeProcessDefinitionsWithoutTenantId(Boolean includeDefinitionsWithoutTenantId) {
    this.includeDefinitionsWithoutTenantId = includeDefinitionsWithoutTenantId;
  }

  @CamundaQueryParam(value = "versionTag")
  public void setVersionTag(String versionTag) {
    this.versionTag = versionTag;
  }

  @CamundaQueryParam(value = "versionTagLike")
  public void setVersionTagLike(String versionTagLike) {
    this.versionTagLike = versionTagLike;
  }

  @CamundaQueryParam(value = "withoutVersionTag", converter = BooleanConverter.class)
  public void setWithoutVersionTag(Boolean withoutVersionTag) {
    this.withoutVersionTag = withoutVersionTag;
  }

  @CamundaQueryParam(value = "startableInTasklist", converter = BooleanConverter.class)
  public void setStartableInTasklist(Boolean startableInTasklist) {
    this.startableInTasklist = startableInTasklist;
  }

  @CamundaQueryParam(value = "notStartableInTasklist", converter = BooleanConverter.class)
  public void setNotStartableInTasklist(Boolean notStartableInTasklist) {
    this.notStartableInTasklist = notStartableInTasklist;
  }

  @CamundaQueryParam(value = "startablePermissionCheck", converter = BooleanConverter.class)
  public void setStartablePermissionCheck(Boolean startablePermissionCheck) {
    this.startablePermissionCheck = startablePermissionCheck;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected ProcessDefinitionQuery createNewQuery(ProcessEngine engine) {
    return engine.getRepositoryService().createProcessDefinitionQuery();
  }

  @Override
  protected void applyFilters(ProcessDefinitionQuery query) {
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (processDefinitionIdIn != null && !processDefinitionIdIn.isEmpty()) {
      query.processDefinitionIdIn(processDefinitionIdIn.toArray(new String[processDefinitionIdIn.size()]));
    }
    if (category != null) {
      query.processDefinitionCategory(category);
    }
    if (categoryLike != null) {
      query.processDefinitionCategoryLike(categoryLike);
    }
    if (name != null) {
      query.processDefinitionName(name);
    }
    if (nameLike != null) {
      query.processDefinitionNameLike(nameLike);
    }
    if (deploymentId != null) {
      query.deploymentId(deploymentId);
    }
    if(deployedAfter != null) {
      query.deployedAfter(deployedAfter);
    }
    if(deployedAt != null) {
      query.deployedAt(deployedAt);
    }
    if (key != null) {
      query.processDefinitionKey(key);
    }
    if (keyLike != null) {
      query.processDefinitionKeyLike(keyLike);
    }

    if (keys != null && !keys.isEmpty()) {
      query.processDefinitionKeysIn(keys.toArray(new String[keys.size()]));
    }
    if (version != null) {
      query.processDefinitionVersion(version);
    }
    if (TRUE.equals(latestVersion)) {
      query.latestVersion();
    }
    if (resourceName != null) {
      query.processDefinitionResourceName(resourceName);
    }
    if (resourceNameLike != null) {
      query.processDefinitionResourceNameLike(resourceNameLike);
    }
    if (startableBy != null) {
      query.startableByUser(startableBy);
    }
    if (TRUE.equals(active)) {
      query.active();
    }
    if (TRUE.equals(suspended)) {
      query.suspended();
    }
    if (incidentId != null) {
      query.incidentId(incidentId);
    }
    if (incidentType != null) {
      query.incidentType(incidentType);
    }
    if (incidentMessage != null) {
      query.incidentMessage(incidentMessage);
    }
    if (incidentMessageLike != null) {
      query.incidentMessageLike(incidentMessageLike);
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (TRUE.equals(includeDefinitionsWithoutTenantId)) {
      query.includeProcessDefinitionsWithoutTenantId();
    }
    if( versionTag != null) {
      query.versionTag(versionTag);
    }
    if( versionTagLike != null) {
      query.versionTagLike(versionTagLike);
    }
    if (TRUE.equals(withoutVersionTag)) {
      query.withoutVersionTag();
    }
    if (TRUE.equals(startableInTasklist)) {
      query.startableInTasklist();
    }
    if (TRUE.equals(notStartableInTasklist)) {
      query.notStartableInTasklist();
    }
    if (TRUE.equals(startablePermissionCheck)) {
      query.startablePermissionCheck();
    }

  }

  @Override
  protected void applySortBy(ProcessDefinitionQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_CATEGORY_VALUE)) {
      query.orderByProcessDefinitionCategory();
    } else if (sortBy.equals(SORT_BY_KEY_VALUE)) {
      query.orderByProcessDefinitionKey();
    } else if (sortBy.equals(SORT_BY_ID_VALUE)) {
      query.orderByProcessDefinitionId();
    } else if (sortBy.equals(SORT_BY_VERSION_VALUE)) {
      query.orderByProcessDefinitionVersion();
    } else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
      query.orderByProcessDefinitionName();
    } else if (sortBy.equals(SORT_BY_DEPLOYMENT_ID_VALUE)) {
      query.orderByDeploymentId();
    } else if (sortBy.equals(SORT_BY_DEPLOY_TIME_VALUE)) {
      query.orderByDeploymentTime();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    } else if (sortBy.equals(SORT_BY_VERSION_TAG)) {
      query.orderByVersionTag();
    }
  }
}
