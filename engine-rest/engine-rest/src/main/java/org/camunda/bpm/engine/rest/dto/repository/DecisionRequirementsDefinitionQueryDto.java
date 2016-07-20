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
package org.camunda.bpm.engine.rest.dto.repository;

import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.IntegerConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DecisionRequirementsDefinitionQueryDto extends AbstractQueryDto<DecisionRequirementsDefinitionQuery> {

  private static final String SORT_BY_ID_VALUE = "id";
  private static final String SORT_BY_KEY_VALUE = "key";
  private static final String SORT_BY_NAME_VALUE = "name";
  private static final String SORT_BY_VERSION_VALUE = "version";
  private static final String SORT_BY_DEPLOYMENT_ID_VALUE = "deploymentId";
  private static final String SORT_BY_CATEGORY_VALUE = "category";
  private static final String SORT_BY_TENANT_ID = "tenantId";

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

  }

  protected String decisionRequirementsDefinitionId;
  protected List<String> decisionRequirementsDefinitionIdIn;
  protected String category;
  protected String categoryLike;
  protected String name;
  protected String nameLike;
  protected String deploymentId;
  protected String key;
  protected String keyLike;
  protected String resourceName;
  protected String resourceNameLike;
  protected Integer version;
  protected Boolean latestVersion;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;
  protected Boolean includeDefinitionsWithoutTenantId;

  public DecisionRequirementsDefinitionQueryDto() {}

  public DecisionRequirementsDefinitionQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("decisionRequirementsDefinitionId")
  public void setDecisionRequirementsDefinitionId(String decisionRequirementsDefinitionId) {
    this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
  }

  @CamundaQueryParam(value = "decisionRequirementsDefinitionIdIn", converter = StringListConverter.class)
  public void setDecisionRequirementsDefinitionIdIn(List<String> decisionRequirementsDefinitionIdIn) {
    this.decisionRequirementsDefinitionIdIn = decisionRequirementsDefinitionIdIn;
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

  @CamundaQueryParam("key")
  public void setKey(String key) {
    this.key = key;
  }

  @CamundaQueryParam("keyLike")
  public void setKeyLike(String keyLike) {
    this.keyLike = keyLike;
  }

  @CamundaQueryParam("resourceName")
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  @CamundaQueryParam("resourceNameLike")
  public void setResourceNameLike(String resourceNameLike) {
    this.resourceNameLike = resourceNameLike;
  }

  @CamundaQueryParam(value = "version", converter = IntegerConverter.class)
  public void setVersion(Integer version) {
    this.version = version;
  }

  @CamundaQueryParam(value = "latestVersion", converter = BooleanConverter.class)
  public void setLatestVersion(Boolean latestVersion) {
    this.latestVersion = latestVersion;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam(value = "includeDecisionRequirementsDefinitionsWithoutTenantId", converter = BooleanConverter.class)
  public void setIncludeDecisionRequirementsDefinitionsWithoutTenantId(Boolean includeDefinitionsWithoutTenantId) {
    this.includeDefinitionsWithoutTenantId = includeDefinitionsWithoutTenantId;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected DecisionRequirementsDefinitionQuery createNewQuery(ProcessEngine engine) {
    return engine.getRepositoryService().createDecisionRequirementsDefinitionQuery();
  }

  @Override
  protected void applyFilters(DecisionRequirementsDefinitionQuery query) {
    if (decisionRequirementsDefinitionId != null) {
      query.decisionRequirementsDefinitionId(decisionRequirementsDefinitionId);
    }
    if (decisionRequirementsDefinitionIdIn != null && !decisionRequirementsDefinitionIdIn.isEmpty()) {
      query.decisionRequirementsDefinitionIdIn(decisionRequirementsDefinitionIdIn.toArray(new String[decisionRequirementsDefinitionIdIn.size()]));
    }
    if (category != null) {
      query.decisionRequirementsDefinitionCategory(category);
    }
    if (categoryLike != null) {
      query.decisionRequirementsDefinitionCategoryLike(categoryLike);
    }
    if (name != null) {
      query.decisionRequirementsDefinitionName(name);
    }
    if (nameLike != null) {
      query.decisionRequirementsDefinitionNameLike(nameLike);
    }
    if (deploymentId != null) {
      query.deploymentId(deploymentId);
    }
    if (key != null) {
      query.decisionRequirementsDefinitionKey(key);
    }
    if (keyLike != null) {
      query.decisionRequirementsDefinitionKeyLike(keyLike);
    }
    if (resourceName != null) {
      query.decisionRequirementsDefinitionResourceName(resourceName);
    }
    if (resourceNameLike != null) {
      query.decisionRequirementsDefinitionResourceNameLike(resourceNameLike);
    }
    if (version != null) {
      query.decisionRequirementsDefinitionVersion(version);
    }
    if (TRUE.equals(latestVersion)) {
      query.latestVersion();
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (TRUE.equals(includeDefinitionsWithoutTenantId)) {
      query.includeDecisionRequirementsDefinitionsWithoutTenantId();
    }
  }

  @Override
  protected void applySortBy(DecisionRequirementsDefinitionQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_CATEGORY_VALUE)) {
      query.orderByDecisionRequirementsDefinitionCategory();
    } else if (sortBy.equals(SORT_BY_KEY_VALUE)) {
      query.orderByDecisionRequirementsDefinitionKey();
    } else if (sortBy.equals(SORT_BY_ID_VALUE)) {
      query.orderByDecisionRequirementsDefinitionId();
    } else if (sortBy.equals(SORT_BY_VERSION_VALUE)) {
      query.orderByDecisionRequirementsDefinitionVersion();
    } else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
      query.orderByDecisionRequirementsDefinitionName();
    } else if (sortBy.equals(SORT_BY_DEPLOYMENT_ID_VALUE)) {
      query.orderByDeploymentId();
    } else if (sortBy.equals(SORT_BY_TENANT_ID)) {
      query.orderByTenantId();
    }
  }

}
