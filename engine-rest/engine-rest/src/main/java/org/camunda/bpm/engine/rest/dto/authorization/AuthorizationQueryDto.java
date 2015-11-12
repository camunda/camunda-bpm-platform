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
package org.camunda.bpm.engine.rest.dto.authorization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.IntegerConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Daniel Meyer
 */
public class AuthorizationQueryDto extends AbstractQueryDto<AuthorizationQuery> {

  private static final String SORT_BY_RESOURCE_TYPE = "resourceType";
  private static final String SORT_BY_RESOURCE_ID = "resourceId";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_RESOURCE_TYPE);
    VALID_SORT_BY_VALUES.add(SORT_BY_RESOURCE_ID);
  }

  protected String id;
  protected Integer type;
  protected String[] userIdIn;
  protected String[] groupIdIn;
  protected Integer resourceType;
  protected String resourceId;

  public AuthorizationQueryDto() {

  }

  public AuthorizationQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("id")
  public void setId(String id) {
    this.id = id;
  }

  @CamundaQueryParam(value="type", converter = IntegerConverter.class)
  public void setType(Integer type) {
    this.type = type;
  }

  @CamundaQueryParam(value="userIdIn", converter = StringArrayConverter.class)
  public void setUserIdIn(String[] userIdIn) {
    this.userIdIn = userIdIn;
  }

  @CamundaQueryParam(value="groupIdIn", converter = StringArrayConverter.class)
  public void setGroupIdIn(String[] groupIdIn) {
    this.groupIdIn = groupIdIn;
  }

  @CamundaQueryParam(value="resourceType", converter = IntegerConverter.class)
  public void setResourceType(int resourceType) {
    this.resourceType = resourceType;
  }

  @CamundaQueryParam("resourceId")
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  protected AuthorizationQuery createNewQuery(ProcessEngine engine) {
    return engine.getAuthorizationService().createAuthorizationQuery();
  }

  protected void applyFilters(AuthorizationQuery query) {

    if (id != null) {
      query.authorizationId(id);
    }
    if (type != null) {
      query.authorizationType(type);
    }
    if (userIdIn != null) {
      query.userIdIn(userIdIn);
    }
    if (groupIdIn != null) {
      query.groupIdIn(groupIdIn);
    }
    if (resourceType != null) {
      query.resourceType(resourceType);
    }
    if (resourceId != null) {
      query.resourceId(resourceId);
    }
  }

  @Override
  protected void applySortBy(AuthorizationQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_RESOURCE_ID)) {
      query.orderByResourceId();
    } else if (sortBy.equals(SORT_BY_RESOURCE_TYPE)) {
      query.orderByResourceType();
    }
  }

}
