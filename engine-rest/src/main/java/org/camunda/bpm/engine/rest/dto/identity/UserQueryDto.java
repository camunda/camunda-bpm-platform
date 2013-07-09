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
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;

/**
 * 
 * @author Daniel Meyer
 */
public class UserQueryDto extends AbstractQueryDto<UserQuery> {

  private static final String SORT_BY_USER_ID_VALUE = "userId";
  private static final String SORT_BY_USER_FIRSTNAME_VALUE = "userFirstName";
  private static final String SORT_BY_USER_LASTNAME_VALUE = "userLastName";
  private static final String SORT_BY_USER_EMAIL_VALUE = "userEmail";
  
  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_USER_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_USER_FIRSTNAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_USER_LASTNAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_USER_EMAIL_VALUE);
  }
  
  protected String userId;
  protected String userFirstName;
  protected String userFirstNameLike;
  protected String userLastName;
  protected String userLastNameLike;
  protected String userEmail;
  protected String userEmailLike;
  protected String memberOfGroup;
  protected String potentialStarter;
    
  public UserQueryDto() {
    
  }
  
  public UserQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
  }

  @CamundaQueryParam("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  @CamundaQueryParam("userFirstName")
  public void setUserFirstName(String userFirstName) {
    this.userFirstName = userFirstName;
  }

  @CamundaQueryParam("userFirstNameLike")
  public void setUserFirstNameLike(String userFirstNameLike) {
    this.userFirstNameLike = userFirstNameLike;
  }

  @CamundaQueryParam("userLastName")
  public void setUserLastName(String userLastName) {
    this.userLastName = userLastName;
  }

  @CamundaQueryParam("userEmail")
  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  @CamundaQueryParam("userEmailLike")
  public void setUserEmailLike(String userEmailLike) {
    this.userEmailLike = userEmailLike;
  }
  
  @CamundaQueryParam("memberOfGroup")
  public void setMemberOfGroup(String memberOfGroup) {
    this.memberOfGroup = memberOfGroup;
  }
  
  @CamundaQueryParam("potentialStarter")
  public void setPotentialStarter(String potentialStarter) {
    this.potentialStarter = potentialStarter;
  }
  
  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected UserQuery createNewQuery(ProcessEngine engine) {
    return engine.getIdentityService().createUserQuery();
  }

  @Override
  protected void applyFilters(UserQuery query) {
    if (userId != null) {
      query.userId(userId);
    }
    if (userFirstName != null) {
      query.userFirstName(userFirstName);
    }
    if (userFirstNameLike != null) {
      query.userFirstNameLike(userFirstNameLike);
    }
    if (userLastName != null) {
      query.userLastName(userLastName);
    }
    if (userLastNameLike != null) {
      query.userLastNameLike(userLastNameLike);
    }
    if (userEmail != null) {
      query.userEmail(userEmail);
    }
    if (userEmailLike != null) {
      query.userEmailLike(userEmailLike);
    }
    if (memberOfGroup != null) {
      query.memberOfGroup(memberOfGroup);
    }
    if (potentialStarter != null) {
      query.potentialStarter(potentialStarter);
    }
  }
  
  @Override
  protected void applySortingOptions(UserQuery query) {
    if (sortBy != null) {
      if (sortBy.equals(SORT_BY_USER_ID_VALUE)) {
        query.orderByUserId();
      } else if (sortBy.equals(SORT_BY_USER_FIRSTNAME_VALUE)) {
        query.orderByUserFirstName();
      } else if (sortBy.equals(SORT_BY_USER_LASTNAME_VALUE)) {
        query.orderByUserLastName();
      } else if (sortBy.equals(SORT_BY_USER_EMAIL_VALUE)) {
        query.orderByUserEmail();
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
