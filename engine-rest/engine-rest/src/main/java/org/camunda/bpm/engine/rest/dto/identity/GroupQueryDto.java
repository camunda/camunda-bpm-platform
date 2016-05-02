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
package org.camunda.bpm.engine.rest.dto.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Daniel Meyer
 */
public class GroupQueryDto extends AbstractQueryDto<GroupQuery> {

  private static final String SORT_BY_GROUP_ID_VALUE = "id";
  private static final String SORT_BY_GROUP_NAME_VALUE = "name";
  private static final String SORT_BY_GROUP_TYPE_VALUE = "type";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_GROUP_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_GROUP_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_GROUP_TYPE_VALUE);
  }

  protected String id;
  protected String name;
  protected String nameLike;
  protected String type;
  protected String member;
  protected String tenantId;

  public GroupQueryDto() {

  }

  public GroupQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("id")
  public void setId(String groupId) {
    this.id = groupId;
  }

  @CamundaQueryParam("name")
  public void setName(String groupName) {
    this.name = groupName;
  }

  @CamundaQueryParam("nameLike")
  public void setNameLike(String groupNameLike) {
    this.nameLike = groupNameLike;
  }

  @CamundaQueryParam("type")
  public void setType(String groupType) {
    this.type = groupType;
  }

  @CamundaQueryParam("member")
  public void setGroupMember(String member) {
    this.member = member;
  }

  @CamundaQueryParam("memberOfTenant")
  public void setMemberOfTenant(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected GroupQuery createNewQuery(ProcessEngine engine) {
    return engine.getIdentityService().createGroupQuery();
  }

  @Override
  protected void applyFilters(GroupQuery query) {
    if (id != null) {
      query.groupId(id);
    }
    if (name != null) {
      query.groupName(name);
    }
    if (nameLike != null) {
      query.groupNameLike(nameLike);
    }
    if (type != null) {
      query.groupType(type);
    }
    if (member != null) {
      query.groupMember(member);
    }
    if (tenantId != null) {
      query.memberOfTenant(tenantId);
    }
  }

  @Override
  protected void applySortBy(GroupQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_GROUP_ID_VALUE)) {
      query.orderByGroupId();
    } else if (sortBy.equals(SORT_BY_GROUP_NAME_VALUE)) {
      query.orderByGroupName();
    } else if (sortBy.equals(SORT_BY_GROUP_TYPE_VALUE)) {
      query.orderByGroupType();
    }
  }

}
