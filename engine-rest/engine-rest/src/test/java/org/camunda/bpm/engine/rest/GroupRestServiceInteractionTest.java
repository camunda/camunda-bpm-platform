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
package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.engine.authorization.Resources.GROUP_MEMBERSHIP;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.dto.identity.GroupDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

/**
 * @author Daniel Meyer
 *
 */
public class GroupRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();
  
  protected static final String SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/group";
  protected static final String GROUP_URL = SERVICE_URL + "/{id}";
  protected static final String GROUP_MEMBERS_URL = GROUP_URL + "/members";
  protected static final String GROUP_MEMBER_URL = GROUP_MEMBERS_URL + "/{userId}";
  protected static final String GROUP_CREATE_URL = TEST_RESOURCE_ROOT_PATH + "/group/create";

  protected IdentityService identityServiceMock;
  protected AuthorizationService authorizationServiceMock;
  protected ProcessEngineConfiguration processEngineConfigurationMock;

  @Before
  public void setupGroupData() {

    identityServiceMock = mock(IdentityService.class);
    authorizationServiceMock = mock(AuthorizationService.class);
    processEngineConfigurationMock = mock(ProcessEngineConfiguration.class);

    // mock identity service
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
    // authorization service
    when(processEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);
    // process engine configuration
    when(processEngine.getProcessEngineConfiguration()).thenReturn(processEngineConfigurationMock);
  }

  @Test
  public void testGetSingleGroup() {
    Group sampleGroup = MockProvider.createMockGroup();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(sampleGroup);

    given().pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_GROUP_ID))
      .body("name", equalTo(MockProvider.EXAMPLE_GROUP_NAME))
      .when().get(GROUP_URL);
  }

  @Test
  public void testUserRestServiceOptions() {
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + GroupRestService.PATH;

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullAuthorizationUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("list"))

        .body("links[1].href", equalTo(fullAuthorizationUrl+"/count"))
        .body("links[1].method", equalTo(HttpMethod.GET))
        .body("links[1].rel", equalTo("count"))

        .body("links[2].href", equalTo(fullAuthorizationUrl+"/create"))
        .body("links[2].method", equalTo(HttpMethod.POST))
        .body("links[2].rel", equalTo("create"))

    .when()
        .options(SERVICE_URL);

    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testUserRestServiceOptionsWithAuthorizationDisabled() {
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + GroupRestService.PATH;

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(false);

    given()
    .then()
      .statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullAuthorizationUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("list"))

      .body("links[1].href", equalTo(fullAuthorizationUrl+"/count"))
      .body("links[1].method", equalTo(HttpMethod.GET))
      .body("links[1].rel", equalTo("count"))

      .body("links[2].href", equalTo(fullAuthorizationUrl+"/create"))
      .body("links[2].method", equalTo(HttpMethod.POST))
      .body("links[2].rel", equalTo("create"))

    .when()
      .options(SERVICE_URL);

    verifyNoAuthorizationCheckPerformed();
  }

  @Test
  public void testGroupResourceOptionsUnauthenticated() {
    String fullGroupUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/group/" + MockProvider.EXAMPLE_GROUP_ID;

    Group sampleGroup = MockProvider.createMockGroup();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(sampleGroup);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
    .then()
        .expect().statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullGroupUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullGroupUrl))
        .body("links[1].method", equalTo(HttpMethod.DELETE))
        .body("links[1].rel", equalTo("delete"))

        .body("links[2].href", equalTo(fullGroupUrl))
        .body("links[2].method", equalTo(HttpMethod.PUT))
        .body("links[2].rel", equalTo("update"))

    .when().options(GROUP_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
  }

  @Test
  public void testGroupResourceOptionsUnauthorized() {
    String fullGroupUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/group/" + MockProvider.EXAMPLE_GROUP_ID;

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, GROUP, MockProvider.EXAMPLE_GROUP_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, GROUP, MockProvider.EXAMPLE_GROUP_ID)).thenReturn(false);

    Group sampleGroup = MockProvider.createMockGroup();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(sampleGroup);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
    .then()
        .expect().statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullGroupUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1]", nullValue())
        .body("links[2]", nullValue())

    .when().options(GROUP_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, GROUP, MockProvider.EXAMPLE_GROUP_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, GROUP, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void testGroupResourceOptionsAuthorized() {
    String fullGroupUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/group/" + MockProvider.EXAMPLE_GROUP_ID;

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, GROUP, MockProvider.EXAMPLE_GROUP_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, GROUP, MockProvider.EXAMPLE_GROUP_ID)).thenReturn(false);

    Group sampleGroup = MockProvider.createMockGroup();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(sampleGroup);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
    .then()
        .expect().statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullGroupUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullGroupUrl))
        .body("links[1].method", equalTo(HttpMethod.DELETE))
        .body("links[1].rel", equalTo("delete"))

        .body("links[2]", nullValue())

    .when().options(GROUP_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, GROUP, MockProvider.EXAMPLE_GROUP_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, GROUP, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void testGroupResourceOptionsWithAuthorizationDisabled() {
    String fullGroupUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/group/" + MockProvider.EXAMPLE_GROUP_ID;

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullGroupUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("self"))

      .body("links[1].href", equalTo(fullGroupUrl))
      .body("links[1].method", equalTo(HttpMethod.DELETE))
      .body("links[1].rel", equalTo("delete"))

      .body("links[2].href", equalTo(fullGroupUrl))
      .body("links[2].method", equalTo(HttpMethod.PUT))
      .body("links[2].rel", equalTo("update"))

    .when()
      .options(GROUP_URL);

    verifyNoAuthorizationCheckPerformed();
  }

  @Test
  public void testGroupMembersResourceOptions() {
    String fullMembersUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/group/" + MockProvider.EXAMPLE_GROUP_ID + "/members";

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
      .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullMembersUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("self"))

      .body("links[1].href", equalTo(fullMembersUrl))
      .body("links[1].method", equalTo(HttpMethod.DELETE))
      .body("links[1].rel", equalTo("delete"))

      .body("links[2].href", equalTo(fullMembersUrl))
      .body("links[2].method", equalTo(HttpMethod.PUT))
      .body("links[2].rel", equalTo("create"))

      .when()
      .options(GROUP_MEMBERS_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
  }

  @Test
  public void testGroupMembersResourceOptionsAuthorized() {
    String fullMembersUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/group/" + MockProvider.EXAMPLE_GROUP_ID + "/members";

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, GROUP_MEMBERSHIP, MockProvider.EXAMPLE_GROUP_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, GROUP_MEMBERSHIP, MockProvider.EXAMPLE_GROUP_ID)).thenReturn(true);

    Group sampleGroup = MockProvider.createMockGroup();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(sampleGroup);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullMembersUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("self"))

      .body("links[1].href", equalTo(fullMembersUrl))
      .body("links[1].method", equalTo(HttpMethod.DELETE))
      .body("links[1].rel", equalTo("delete"))

      .body("links[2].href", equalTo(fullMembersUrl))
      .body("links[2].method", equalTo(HttpMethod.PUT))
      .body("links[2].rel", equalTo("create"))

    .when()
      .options(GROUP_MEMBERS_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, GROUP_MEMBERSHIP, MockProvider.EXAMPLE_GROUP_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, GROUP_MEMBERSHIP, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void testGroupMembersResourceOptionsUnauthorized() {
    String fullMembersUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/group/" + MockProvider.EXAMPLE_GROUP_ID + "/members";

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, GROUP_MEMBERSHIP, MockProvider.EXAMPLE_GROUP_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, GROUP_MEMBERSHIP, MockProvider.EXAMPLE_GROUP_ID)).thenReturn(false);

    Group sampleGroup = MockProvider.createMockGroup();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(sampleGroup);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullMembersUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("self"))

      .body("links[1]", nullValue())

      .body("links[2]", nullValue())

    .when()
      .options(GROUP_MEMBERS_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, GROUP_MEMBERSHIP, MockProvider.EXAMPLE_GROUP_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, GROUP_MEMBERSHIP, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void testGroupMembersResourceOptionsWithAuthorizationDisabled() {
    String fullMembersUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/group/" + MockProvider.EXAMPLE_GROUP_ID + "/members";

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullMembersUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("self"))

      .body("links[1].href", equalTo(fullMembersUrl))
      .body("links[1].method", equalTo(HttpMethod.DELETE))
      .body("links[1].rel", equalTo("delete"))

      .body("links[2].href", equalTo(fullMembersUrl))
      .body("links[2].method", equalTo(HttpMethod.PUT))
      .body("links[2].rel", equalTo("create"))

    .when()
      .options(GROUP_MEMBERS_URL);

    verifyNoAuthorizationCheckPerformed();
  }

  @Test
  public void testGetNonExistingGroup() {
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(anyString())).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(null);

    given().pathParam("id", "aNonExistingGroup")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Group with id aNonExistingGroup does not exist"))
      .when().get(GROUP_URL);
  }

  @Test
  public void testDeleteGroup() {
    given().pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
      .expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(GROUP_URL);
  }

  @Test
  public void testDeleteNonExistingGroup() {
    given().pathParam("id", "non-existing")
    .expect().statusCode(Status.NO_CONTENT.getStatusCode())
    .when().delete(GROUP_URL);
  }

  @Test
  public void testDeleteGroupThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).deleteGroup(MockProvider.EXAMPLE_GROUP_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .delete(GROUP_URL);
  }

  @Test
  public void testUpdateExistingGroup() {
    Group initialGroup = MockProvider.createMockGroup();
    Group groupUpdate = MockProvider.createMockGroupUpdate();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(initialGroup);

    given().pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
      .body(GroupDto.fromGroup(groupUpdate)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(GROUP_URL);

    // initial group was updated
    verify(initialGroup).setName(groupUpdate.getName());

    // and then saved
    verify(identityServiceMock).saveGroup(initialGroup);
  }

  @Test
  public void testUpdateNonExistingGroup() {
    Group groupUpdate = MockProvider.createMockGroupUpdate();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId("aNonExistingGroup")).thenReturn(sampleGroupQuery);
    // this time the query returns null
    when(sampleGroupQuery.singleResult()).thenReturn(null);

    given().pathParam("id", "aNonExistingGroup")
      .body(GroupDto.fromGroup(groupUpdate)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Group with id aNonExistingGroup does not exist"))
      .when().put(GROUP_URL);

    verify(identityServiceMock, never()).saveGroup(any(Group.class));
  }

  @Test
  public void testUpdateGroupThrowsAuthorizationException() {
    Group initialGroup = MockProvider.createMockGroup();
    Group groupUpdate = MockProvider.createMockGroupUpdate();

    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(initialGroup);

    String message = "exception expected";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).saveGroup(any(Group.class));

    given()
      .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
      .body(GroupDto.fromGroup(groupUpdate))
      .contentType(ContentType.JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(GROUP_URL);

    // initial group was updated
    verify(initialGroup).setName(groupUpdate.getName());
  }

  @Test
  public void testGroupCreate() {
    Group newGroup = MockProvider.createMockGroup();
    when(identityServiceMock.newGroup(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(newGroup);

    given()
        .body(GroupDto.fromGroup(newGroup)).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .post(GROUP_CREATE_URL);

    verify(identityServiceMock).newGroup(MockProvider.EXAMPLE_GROUP_ID);
    verify(newGroup).setName(MockProvider.EXAMPLE_GROUP_NAME);
    verify(identityServiceMock).saveGroup(newGroup);
  }

  @Test
  public void testGroupCreateExistingFails() {
    Group newGroup = MockProvider.createMockGroup();
    when(identityServiceMock.newGroup(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(newGroup);
    doThrow(new ProcessEngineException("")).when(identityServiceMock).saveGroup(newGroup);

    given().body(GroupDto.fromGroup(newGroup)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .when().post(GROUP_CREATE_URL);

    verify(identityServiceMock).newGroup(MockProvider.EXAMPLE_GROUP_ID);
    verify(identityServiceMock).saveGroup(newGroup);
  }

  @Test
  public void testGroupCreateThrowsAuthorizationException() {
    Group newGroup = MockProvider.createMockGroup();
    String message = "exception expected";
    when(identityServiceMock.newGroup(newGroup.getId())).thenThrow(new AuthorizationException(message));

    given()
      .body(GroupDto.fromGroup(newGroup))
      .contentType(ContentType.JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(GROUP_CREATE_URL);
  }

  @Test
  public void testSaveGroupThrowsAuthorizationException() {
    Group newGroup = MockProvider.createMockGroup();

    String message = "exception expected";
    when(identityServiceMock.newGroup(newGroup.getId())).thenReturn(newGroup);
    doThrow(new AuthorizationException(message)).when(identityServiceMock).saveGroup(newGroup);

    given()
      .body(GroupDto.fromGroup(newGroup))
      .contentType(ContentType.JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(GROUP_CREATE_URL);
  }

  @Test
  public void testCreateGroupMember() {

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
        .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(GROUP_MEMBER_URL);

    verify(identityServiceMock).createMembership(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void testCreateGroupMemberThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).createMembership(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_GROUP_ID);

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
        .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
        .put(GROUP_MEMBER_URL);
  }

  @Test
  public void testDeleteGroupMember() {

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
        .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .delete(GROUP_MEMBER_URL);

    verify(identityServiceMock).deleteMembership(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void testDeleteGroupMemberThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).deleteMembership(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_GROUP_ID);

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
        .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
        .delete(GROUP_MEMBER_URL);
  }

  @Test
  public void testReadOnlyGroupCreateFails() {
    Group newGroup = MockProvider.createMockGroup();
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given().body(GroupDto.fromGroup(newGroup)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
      .when().post(GROUP_CREATE_URL);

    verify(identityServiceMock, never()).newGroup(MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void testReadOnlyGroupUpdateFails() {
    Group groupUdpdate = MockProvider.createMockGroup();
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
        .body(GroupDto.fromGroup(groupUdpdate)).contentType(ContentType.JSON)
    .then().expect().statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Identity service implementation is read-only."))
    .when().put(GROUP_URL);

    verify(identityServiceMock, never()).saveGroup(groupUdpdate);
  }

  @Test
  public void testReadOnlyGroupDeleteFails() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
    .then().expect().statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Identity service implementation is read-only."))
    .when().delete(GROUP_URL);

    verify(identityServiceMock, never()).deleteGroup(MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void testReadOnlyCreateGroupMemberFails() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
        .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Identity service implementation is read-only."))
    .when()
        .put(GROUP_MEMBER_URL);

    verify(identityServiceMock, never()).createMembership(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void testReadOnlyGroupMemberDeleteFails() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
        .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Identity service implementation is read-only."))
    .when()
        .delete(GROUP_MEMBER_URL);

    verify(identityServiceMock, never()).deleteMembership(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_GROUP_ID);
  }

  protected void verifyNoAuthorizationCheckPerformed() {
    verify(identityServiceMock, times(0)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(0)).isUserAuthorized(anyString(), anyListOf(String.class), any(Permission.class), any(Resource.class));
  }

}
