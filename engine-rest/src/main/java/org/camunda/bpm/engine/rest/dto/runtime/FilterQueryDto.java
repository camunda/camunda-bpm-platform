/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Sebastian Menski
 */
public class FilterQueryDto extends AbstractQueryDto<FilterQuery> {

  public static final String SORT_BY_ID_VALUE = "filterId";
  public static final String SORT_BY_RESOURCE_TYPE_VALUE = "resourceType";
  public static final String SORT_BY_NAME_VALUE = "name";
  public static final String SORT_BY_OWNER_VALUE = "owner";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_RESOURCE_TYPE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_OWNER_VALUE);
  }

  protected String filterId;
  protected String resourceType;
  protected String name;
  protected String nameLike;
  protected String owner;

  public FilterQueryDto() {

  }

  public FilterQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("filterId")
  public void setFilterId(String filterId) {
    this.filterId = filterId;
  }

  @CamundaQueryParam("resourceType")
  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  @CamundaQueryParam("name")
  public void setName(String name) {
    this.name = name;
  }

  @CamundaQueryParam("nameLike")
  public void setNameLike(String nameLike) {
    this.nameLike = nameLike;
  }

  @CamundaQueryParam("owner")
  public void setOwner(String owner) {
    this.owner = owner;
  }

  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  protected FilterQuery createNewQuery(ProcessEngine engine) {
    return engine.getFilterService().createFilterQuery();
  }

  protected void applyFilters(FilterQuery query) {
    if (filterId != null) {
      query.filterId(filterId);
    }
    if (resourceType != null) {
      query.filterResourceType(resourceType);
    }
    if (name != null) {
      query.filterName(name);
    }
    if (nameLike != null) {
      query.filterNameLike(nameLike);
    }
    if (owner != null) {
      query.filterOwner(owner);
    }
  }

  protected void applySortBy(FilterQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_ID_VALUE)) {
      query.orderByFilterId();
    }
    else if (sortBy.equals(SORT_BY_RESOURCE_TYPE_VALUE)) {
      query.orderByFilterResourceType();
    }
    else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
      query.orderByFilterName();
    }
    else if (sortBy.equals(SORT_BY_OWNER_VALUE)) {
      query.orderByFilterOwner();
    }
  }

}
