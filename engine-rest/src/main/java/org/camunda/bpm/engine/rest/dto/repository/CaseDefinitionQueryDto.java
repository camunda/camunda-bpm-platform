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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.IntegerConverter;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionQueryDto extends AbstractQueryDto<CaseDefinitionQuery> {

  private static final String SORT_BY_ID_VALUE = "id";
  private static final String SORT_BY_KEY_VALUE = "key";
  private static final String SORT_BY_NAME_VALUE = "name";
  private static final String SORT_BY_VERSION_VALUE = "version";
  private static final String SORT_BY_DEPLOYMENT_ID_VALUE = "deploymentId";
  private static final String SORT_BY_CATEGORY_VALUE = "category";

  private static final List<String> VALID_SORT_BY_VALUES;

  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();

    VALID_SORT_BY_VALUES.add(SORT_BY_CATEGORY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_KEY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_VERSION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DEPLOYMENT_ID_VALUE);

  }

  protected String caseDefinitionId;
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

  public CaseDefinitionQueryDto() {}

  public CaseDefinitionQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("caseDefinitionId")
  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
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

  @CamundaQueryParam(value = "latest", converter = BooleanConverter.class)
  public void setLatestVersion(Boolean latestVersion) {
    this.latestVersion = latestVersion;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected CaseDefinitionQuery createNewQuery(ProcessEngine engine) {
    return engine.getRepositoryService().createCaseDefinitionQuery();
  }

  @Override
  protected void applyFilters(CaseDefinitionQuery query) {
    if (caseDefinitionId != null) {
      query.caseDefinitionId(caseDefinitionId);
    }
    if (category != null) {
      query.caseDefinitionCategory(category);
    }
    if (categoryLike != null) {
      query.caseDefinitionCategoryLike(categoryLike);
    }
    if (name != null) {
      query.caseDefinitionName(name);
    }
    if (nameLike != null) {
      query.caseDefinitionNameLike(nameLike);
    }
    if (deploymentId != null) {
      query.deploymentId(deploymentId);
    }
    if (key != null) {
      query.caseDefinitionKey(key);
    }
    if (keyLike != null) {
      query.caseDefinitionKeyLike(keyLike);
    }
    if (resourceName != null) {
      query.caseDefinitionResourceName(resourceName);
    }
    if (resourceNameLike != null) {
      query.caseDefinitionResourceNameLike(resourceNameLike);
    }
    if (version != null) {
      query.caseDefinitionVersion(version);
    }
    if (latestVersion != null && latestVersion) {
      query.latestVersion();
    }
  }

  @Override
  protected void applySortingOptions(CaseDefinitionQuery query) {
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_CATEGORY_VALUE)) {
        query.orderByCaseDefinitionCategory();
      } else if (sortBy.equals(SORT_BY_KEY_VALUE)) {
        query.orderByCaseDefinitionKey();
      } else if (sortBy.equals(SORT_BY_ID_VALUE)) {
        query.orderByCaseDefinitionId();
      } else if (sortBy.equals(SORT_BY_VERSION_VALUE)) {
        query.orderByCaseDefinitionVersion();
      } else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
        query.orderByCaseDefinitionName();
      } else if (sortBy.equals(SORT_BY_DEPLOYMENT_ID_VALUE)) {
        query.orderByDeploymentId();
      }
    }

    if (sortOrder != null) {
      if (sortOrder.equals(SORT_ORDER_ASC_VALUE)) {
        query.asc();
      } else if (sortOrder.equals(SORT_ORDER_DESC_VALUE)) {
        query.desc();
      }
    }
  }

}
