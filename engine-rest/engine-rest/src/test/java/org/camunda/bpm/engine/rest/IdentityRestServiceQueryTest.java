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
package org.camunda.bpm.engine.rest;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.dto.identity.BasicUserCredentialsDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.restassured.http.ContentType;

public class IdentityRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String TEST_USERNAME = "testUsername";
  protected static final String TEST_PASSWORD = "testPassword";
  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String IDENTITY_URL = TEST_RESOURCE_ROOT_PATH + "/identity";
  protected static final String TASK_GROUPS_URL = IDENTITY_URL + "/groups";
  protected static final String VERIFY_USER_URL = IDENTITY_URL + "/verify";

  private User mockUser;

  @Before
  public void setUpRuntimeData() {
    createMockIdentityQueries();
  }

  private void createMockIdentityQueries() {
    UserQuery sampleUserQuery = mock(UserQuery.class);

    List<User> mockUsers = new ArrayList<User>();

    mockUser = MockProvider.createMockUser();
    mockUsers.add(mockUser);

    when(sampleUserQuery.unlimitedList()).thenReturn(mockUsers);
    when(sampleUserQuery.memberOfGroup(anyString())).thenReturn(sampleUserQuery);
    when(sampleUserQuery.count()).thenReturn((long) mockUsers.size());

    GroupQuery sampleGroupQuery = mock(GroupQuery.class);

    List<Group> mockGroups = MockProvider.createMockGroups();
    when(sampleGroupQuery.unlimitedList()).thenReturn(mockGroups);
    when(sampleGroupQuery.groupMember(anyString())).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupName()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupId()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupType()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.asc()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.desc()).thenReturn(sampleGroupQuery);

    when(processEngine.getIdentityService().createGroupQuery()).thenReturn(sampleGroupQuery);
    when(processEngine.getIdentityService().createUserQuery()).thenReturn(sampleUserQuery);
  }


  @Test
  public void testGroupInfoQuery() {
    given().queryParam("userId", "name")
        .then().expect().statusCode(Status.OK.getStatusCode())
        .body("groups.size()", is(1))
        .body("groups[0].id", equalTo(MockProvider.EXAMPLE_GROUP_ID))
        .body("groups[0].name", equalTo(MockProvider.EXAMPLE_GROUP_NAME))
        .body("groupUsers.size()", is(1))
        .body("groupUsers[0].id", equalTo(mockUser.getId()))
        .when().get(TASK_GROUPS_URL);
  }

  @Test
  public void testGroupInfoQueryWithMissingUserParameter() {
    expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("No user id was supplied"))
    .when().get(TASK_GROUPS_URL);
  }

  @Test
  public void verifyUserWithMissingParameter () {
    given()
        .body(new BasicUserCredentialsDto()).contentType(ContentType.JSON).
    expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Username and password are required"))
        .when().post(VERIFY_USER_URL);
  }

  @Test
  public void verifyUser () {
    when(processEngine.getIdentityService()
        .checkPassword(TEST_USERNAME, TEST_PASSWORD))
    .thenReturn(true);

    BasicUserCredentialsDto userCredentialsDto = new BasicUserCredentialsDto();
    userCredentialsDto.setUsername(TEST_USERNAME);
    userCredentialsDto.setUsername(TEST_PASSWORD);
    given()
        .body(userCredentialsDto).contentType(ContentType.JSON).
    expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Username and password are required"))
        .when().post(VERIFY_USER_URL);
  }
}
