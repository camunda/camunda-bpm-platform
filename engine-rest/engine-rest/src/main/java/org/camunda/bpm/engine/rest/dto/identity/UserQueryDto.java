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
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Daniel Meyer
 */
public class UserQueryDto extends AbstractQueryDto<UserQuery> {

  private static final String SORT_BY_USER_ID_VALUE = "userId";
  private static final String SORT_BY_USER_FIRSTNAME_VALUE = "firstName";
  private static final String SORT_BY_USER_LASTNAME_VALUE = "lastName";
  private static final String SORT_BY_USER_EMAIL_VALUE = "email";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_USER_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_USER_FIRSTNAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_USER_LASTNAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_USER_EMAIL_VALUE);
  }

  protected String id;
  protected String[] idIn;
  protected String firstName;
  protected String firstNameLike;
  protected String lastName;
  protected String lastNameLike;
  protected String email;
  protected String emailLike;
  protected String memberOfGroup;
  protected String potentialStarter;
  protected String tenantId;

  public UserQueryDto() {

  }

  public UserQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("id")
  public void setId(String userId) {
    this.id = userId;
  }

  @CamundaQueryParam(value = "idIn", converter = StringArrayConverter.class)
  public void setIdIn(String[] ids) {
    this.idIn = ids;
  }

  @CamundaQueryParam("firstName")
  public void setFirstName(String userFirstName) {
    this.firstName = userFirstName;
  }

  @CamundaQueryParam("firstNameLike")
  public void setFirstNameLike(String userFirstNameLike) {
    this.firstNameLike = userFirstNameLike;
  }

  @CamundaQueryParam("lastName")
  public void setLastName(String userLastName) {
    this.lastName = userLastName;
  }

  @CamundaQueryParam("lastNameLike")
  public void setLastNameLike(String userLastNameLike) {
    this.lastNameLike = userLastNameLike;
  }

  @CamundaQueryParam("email")
  public void setEmail(String userEmail) {
    this.email = userEmail;
  }

  @CamundaQueryParam("emailLike")
  public void setEmailLike(String userEmailLike) {
    this.emailLike = userEmailLike;
  }

  @CamundaQueryParam("memberOfGroup")
  public void setMemberOfGroup(String memberOfGroup) {
    this.memberOfGroup = memberOfGroup;
  }

  @CamundaQueryParam("potentialStarter")
  public void setPotentialStarter(String potentialStarter) {
    this.potentialStarter = potentialStarter;
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
  protected UserQuery createNewQuery(ProcessEngine engine) {
    return engine.getIdentityService().createUserQuery();
  }

  @Override
  protected void applyFilters(UserQuery query) {
    if (id != null) {
      query.userId(id);
    }
    if(idIn != null) {
      query.userIdIn(idIn);
    }
    if (firstName != null) {
      query.userFirstName(firstName);
    }
    if (firstNameLike != null) {
      query.userFirstNameLike(firstNameLike);
    }
    if (lastName != null) {
      query.userLastName(lastName);
    }
    if (lastNameLike != null) {
      query.userLastNameLike(lastNameLike);
    }
    if (email != null) {
      query.userEmail(email);
    }
    if (emailLike != null) {
      query.userEmailLike(emailLike);
    }
    if (memberOfGroup != null) {
      query.memberOfGroup(memberOfGroup);
    }
    if (potentialStarter != null) {
      query.potentialStarter(potentialStarter);
    }
    if (tenantId != null) {
      query.memberOfTenant(tenantId);
    }
  }

  @Override
  protected void applySortBy(UserQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
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

}
