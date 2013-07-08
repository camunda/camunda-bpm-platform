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

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;

/**
 * 
 * @author Daniel Meyer
 */
public class GroupQueryDto extends AbstractQueryDto<GroupQuery> {

  private static final String SORT_BY_GROUP_ID_VALUE = "groupId";
  private static final String SORT_BY_GROUP_NAME_VALUE = "groupName";
  private static final String SORT_BY_GROUP_TYPE_VALUE = "groupType";
  
  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_GROUP_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_GROUP_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_GROUP_TYPE_VALUE);
  }
  
  protected String groupId;
  protected String groupName;
  protected String groupNameLike;
  protected String groupType;
  protected String groupMember;
    
  public GroupQueryDto() {
    
  }
  
  public GroupQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
  }

  @CamundaQueryParam("groupId")
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  
  @CamundaQueryParam("groupName")
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  @CamundaQueryParam("groupNameLike")
  public void setGroupNameLike(String groupNameLike) {
    this.groupNameLike = groupNameLike;
  }
  
  @CamundaQueryParam("groupType")
  public void setGroupType(String groupType) {
    this.groupType = groupType;
  }
  
  @CamundaQueryParam("groupMember")
  public void setGroupMember(String groupMember) {
    this.groupMember = groupMember;
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
    if (groupId != null) {
      query.groupId(groupId);
    }
    if (groupName != null) {
      query.groupName(groupName);
    }
    if (groupNameLike != null) {
      query.groupNameLike(groupNameLike);
    }
    if (groupType != null) {
      query.groupType(groupType);
    }
    if (groupMember != null) {
      query.groupMember(groupMember);
    }
  }
  
  @Override
  protected void applySortingOptions(GroupQuery query) {
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_GROUP_ID_VALUE)) {
        query.orderByGroupId();
      } else if (sortBy.equals(SORT_BY_GROUP_NAME_VALUE)) {
        query.orderByGroupName();
      } else if (sortBy.equals(SORT_BY_GROUP_TYPE_VALUE)) {
        query.orderByGroupType();
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
