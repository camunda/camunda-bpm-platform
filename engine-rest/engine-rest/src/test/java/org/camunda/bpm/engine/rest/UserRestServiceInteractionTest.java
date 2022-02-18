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

import static io.restassured.RestAssured.given;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;
import org.camunda.bpm.engine.rest.exception.ExceptionLogger;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import io.restassured.http.ContentType;

/**
 * @author Daniel Meyer
 *
 */
public class UserRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule()
      .watch(ExceptionLogger.REST_API);

  protected static final String SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/user";
  protected static final String USER_URL = SERVICE_URL + "/{id}";
  protected static final String USER_CREATE_URL = SERVICE_URL + "/create";
  protected static final String USER_PROFILE_URL = USER_URL + "/profile";
  protected static final String USER_CREDENTIALS_URL = USER_URL + "/credentials";
  protected static final String USER_UNLOCK = USER_URL + "/unlock";

  protected IdentityService identityServiceMock;
  protected AuthorizationService authorizationServiceMock;
  protected ProcessEngineConfiguration processEngineConfigurationMock;

  @Before
  public void setupUserData() {

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
  public void testGetSingleUserProfile() {
    User sampleUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(sampleUser);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_USER_ID))
        .body("firstName", equalTo(MockProvider.EXAMPLE_USER_FIRST_NAME))
        .body("lastName", equalTo(MockProvider.EXAMPLE_USER_LAST_NAME))
        .body("email", equalTo(MockProvider.EXAMPLE_USER_EMAIL))
    .when()
        .get(USER_PROFILE_URL);
  }

  @Test
  public void testUserRestServiceOptions() {
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + UserRestService.PATH;

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
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + UserRestService.PATH;

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(false);

    given()
    .then()
      .statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullAuthorizationUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("list"))

      .body("links[1].href", equalTo(fullAuthorizationUrl + "/count"))
      .body("links[1].method", equalTo(HttpMethod.GET))
      .body("links[1].rel", equalTo("count"))

      .body("links[2].href", equalTo(fullAuthorizationUrl + "/create"))
      .body("links[2].method", equalTo(HttpMethod.POST))
      .body("links[2].rel", equalTo("create"))

    .when()
      .options(SERVICE_URL);

    verifyNoAuthorizationCheckPerformed();
  }

  @Test
  public void testUserResourceOptionsUnauthenticated() {
    String fullUserUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/user/" + MockProvider.EXAMPLE_USER_ID;

    User sampleUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(sampleUser);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(null);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullUserUrl+"/profile"))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullUserUrl))
        .body("links[1].method", equalTo(HttpMethod.DELETE))
        .body("links[1].rel", equalTo("delete"))

        .body("links[2].href", equalTo(fullUserUrl+"/profile"))
        .body("links[2].method", equalTo(HttpMethod.PUT))
        .body("links[2].rel", equalTo("update"))

    .when()
        .options(USER_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();

  }

  @Test
  public void testUserResourceOptionsUnauthorized() {
    String fullUserUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/user/" + MockProvider.EXAMPLE_USER_ID;

    User sampleUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(sampleUser);

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, USER, MockProvider.EXAMPLE_USER_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, USER, MockProvider.EXAMPLE_USER_ID)).thenReturn(false);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullUserUrl+"/profile"))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1]", nullValue())
        .body("links[2]", nullValue())

    .when()
        .options(USER_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, USER, MockProvider.EXAMPLE_USER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, USER, MockProvider.EXAMPLE_USER_ID);
  }

  @Test
  public void testUserResourceOptionsDeleteAuthorized() {
    String fullUserUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/user/" + MockProvider.EXAMPLE_USER_ID;

    User sampleUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(sampleUser);

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, USER, MockProvider.EXAMPLE_USER_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, USER, MockProvider.EXAMPLE_USER_ID)).thenReturn(false);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullUserUrl+"/profile"))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullUserUrl))
        .body("links[1].method", equalTo(HttpMethod.DELETE))
        .body("links[1].rel", equalTo("delete"))

        .body("links[2]", nullValue())

    .when()
        .options(USER_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, USER, MockProvider.EXAMPLE_USER_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, USER, MockProvider.EXAMPLE_USER_ID);
  }

  @Test
  public void testUserResourceOptionsWithAuthorizationDisabled() {
    String fullUserUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/user/" + MockProvider.EXAMPLE_USER_ID;

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_USER_ID)
    .then()
      .statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullUserUrl + "/profile"))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("self"))

      .body("links[1].href", equalTo(fullUserUrl))
      .body("links[1].method", equalTo(HttpMethod.DELETE))
      .body("links[1].rel", equalTo("delete"))

      .body("links[2].href", equalTo(fullUserUrl + "/profile"))
      .body("links[2].method", equalTo(HttpMethod.PUT))
      .body("links[2].rel", equalTo("update"))

    .when()
      .options(USER_URL);

    verifyNoAuthorizationCheckPerformed();
  }

  @Test
  public void testGetNonExistingUserProfile() {
    String exceptionMessage = "User with id aNonExistingUser does not exist";
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(anyString())).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(null);

    given()
        .pathParam("id", "aNonExistingUser")
    .then()
        .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo(exceptionMessage))
    .when()
        .get(USER_PROFILE_URL);

    verifyLogs(Level.DEBUG, exceptionMessage);
  }

  @Test
  public void testDeleteUser() {
    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
    .then()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .delete(USER_URL);
  }

  @Test
  public void testDeleteNonExistingUser() {
    given()
        .pathParam("id", "non-existing")
    .then()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .delete(USER_URL);
  }

  @Test
  public void testDeleteUserThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).deleteUser(MockProvider.EXAMPLE_USER_ID);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
    .then()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
        .delete(USER_URL);

    verifyLogs(Level.DEBUG, message);
  }

  @Test
  public void testCreateNewUserWithCredentials() {
    User newUser = MockProvider.createMockUser();
    when(identityServiceMock.newUser(MockProvider.EXAMPLE_USER_ID)).thenReturn(newUser);

    UserDto userDto = UserDto.fromUser(newUser, true);

    given()
        .body(userDto).contentType(ContentType.JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .post(USER_CREATE_URL);

    verify(identityServiceMock).newUser(MockProvider.EXAMPLE_USER_ID);
    verify(newUser).setFirstName(MockProvider.EXAMPLE_USER_FIRST_NAME);
    verify(newUser).setLastName(MockProvider.EXAMPLE_USER_LAST_NAME);
    verify(newUser).setEmail(MockProvider.EXAMPLE_USER_EMAIL);
    verify(newUser).setPassword(MockProvider.EXAMPLE_USER_PASSWORD);
    verify(identityServiceMock).saveUser(newUser);
  }

  @Test
  public void testCreateNewUserWithoutCredentials() {
    User newUser = MockProvider.createMockUser();
    when(identityServiceMock.newUser(MockProvider.EXAMPLE_USER_ID)).thenReturn(newUser);

    UserDto userDto = UserDto.fromUser(newUser, false);

    given()
        .body(userDto).contentType(ContentType.JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .post(USER_CREATE_URL);

    verify(identityServiceMock).newUser(MockProvider.EXAMPLE_USER_ID);
    verify(newUser).setFirstName(MockProvider.EXAMPLE_USER_FIRST_NAME);
    verify(newUser).setLastName(MockProvider.EXAMPLE_USER_LAST_NAME);
    verify(newUser).setEmail(MockProvider.EXAMPLE_USER_EMAIL);
    // no password was set
    verify(newUser, never()).setPassword(any(String.class));

    verify(identityServiceMock).saveUser(newUser);
  }

  @Test
  public void testUserCreateExistingFails() {
    User newUser = MockProvider.createMockUser();
    when(identityServiceMock.newUser(MockProvider.EXAMPLE_USER_ID)).thenReturn(newUser);
    doThrow(new ProcessEngineException("")).when(identityServiceMock).saveUser(newUser);

    UserDto userDto = UserDto.fromUser(newUser, true);

    given()
      .body(userDto).contentType(ContentType.JSON)
    .then()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
    .when()
      .post(USER_CREATE_URL);

    verify(identityServiceMock).newUser(MockProvider.EXAMPLE_USER_ID);
    verify(identityServiceMock).saveUser(newUser);

    verifyLogs(Level.WARN, "org.camunda.bpm.engine.ProcessEngineException");
  }

  @Test
  public void testUserCreateThrowsAuthorizationException() {
    User newUser = MockProvider.createMockUser();
    String message = "exception expected";
    when(identityServiceMock.newUser(MockProvider.EXAMPLE_USER_ID)).thenThrow(new AuthorizationException(message));

    UserDto userDto = UserDto.fromUser(newUser, true);

    given()
      .body(userDto)
      .contentType(ContentType.JSON)
    .then()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(USER_CREATE_URL);

    verifyLogs(Level.DEBUG, message);
  }

  @Test
  public void testSaveNewUserThrowsAuthorizationException() {
    User newUser = MockProvider.createMockUser();
    when(identityServiceMock.newUser(MockProvider.EXAMPLE_USER_ID)).thenReturn(newUser);
    String message = "exception expected";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).saveUser(newUser);

    UserDto userDto = UserDto.fromUser(newUser, true);

    given()
      .body(userDto)
      .contentType(ContentType.JSON)
    .then()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(USER_CREATE_URL);

    verifyLogs(Level.DEBUG, message);
  }

  @Test
  public void testPutCredentials() {
    User initialUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(initialUser);

    UserCredentialsDto dto = new UserCredentialsDto();
    dto.setPassword("new-password");

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
        .body(dto).contentType(ContentType.JSON)
    .then()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(USER_CREDENTIALS_URL);

    // password was updated
    verify(initialUser).setPassword(dto.getPassword());

    // and then saved
    verify(identityServiceMock).saveUser(initialUser);
  }

  @Test
  public void testPutCredentialsThrowsAuthorizationException() {
    User initialUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(initialUser);

    String message = "exception expected";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).saveUser(any(User.class));

    UserCredentialsDto dto = new UserCredentialsDto();
    dto.setPassword("new-password");

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
        .body(dto).contentType(ContentType.JSON)
    .then()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
        .put(USER_CREDENTIALS_URL);

    verifyLogs(Level.DEBUG, message);
  }

  @Test
  public void testChangeCredentials() {
    User initialUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(initialUser);

    Authentication authentication = MockProvider.createMockAuthentication();
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);

    when(identityServiceMock.checkPassword(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD)).thenReturn(true);

    UserCredentialsDto dto = new UserCredentialsDto();
    dto.setPassword("new-password");
    dto.setAuthenticatedUserPassword(MockProvider.EXAMPLE_USER_PASSWORD);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
        .contentType(ContentType.JSON)
        .body(dto)
    .then()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(USER_CREDENTIALS_URL);

    verify(identityServiceMock).getCurrentAuthentication();
    verify(identityServiceMock).checkPassword(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD);

    // password was updated
    verify(initialUser).setPassword(dto.getPassword());

    // and then saved
    verify(identityServiceMock).saveUser(initialUser);
  }

  @Test
  public void testChangeCredentialsWithWrongAuthenticatedUserPassword() {
    String exceptionMessage = "The given authenticated user password is not valid.";
    User initialUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(initialUser);

    Authentication authentication = MockProvider.createMockAuthentication();
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);

    when(identityServiceMock.checkPassword(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD)).thenReturn(false);

    UserCredentialsDto dto = new UserCredentialsDto();
    dto.setPassword("new-password");
    dto.setAuthenticatedUserPassword(MockProvider.EXAMPLE_USER_PASSWORD);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
        .contentType(ContentType.JSON)
        .body(dto)
    .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo("InvalidRequestException"))
        .body("message", equalTo(exceptionMessage))
    .when()
        .put(USER_CREDENTIALS_URL);

    verifyLogs(Level.DEBUG, exceptionMessage);
  }

  @Test
  public void testPutCredentialsNonExistingUserFails() {
    String exceptionMessage = "User with id aNonExistingUser does not exist";
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId("aNonExistingUser")).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(null);

    UserCredentialsDto dto = new UserCredentialsDto();
    dto.setPassword("new-password");

    given()
        .pathParam("id", "aNonExistingUser")
        .body(dto).contentType(ContentType.JSON)
    .then()
        .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo(exceptionMessage))
    .when()
        .put(USER_CREDENTIALS_URL);

    // user was not updated
    verify(identityServiceMock, never()).saveUser(any(User.class));
    verifyLogs(Level.DEBUG, exceptionMessage);
  }

  @Test
  public void testPutProfile() {
    User initialUser = MockProvider.createMockUser();
    User userUpdate = MockProvider.createMockUserUpdate();

    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(initialUser);

    UserProfileDto updateDto = UserProfileDto.fromUser(userUpdate);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
        .body(updateDto).contentType(ContentType.JSON)
    .then()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(USER_PROFILE_URL);

    // password was updated
    verify(initialUser).setEmail(updateDto.getEmail());
    verify(initialUser).setFirstName(updateDto.getFirstName());
    verify(initialUser).setLastName(updateDto.getLastName());

    // and then saved
    verify(identityServiceMock).saveUser(initialUser);
  }

  @Test
  public void testPutProfileNonexistingFails() {
    String exceptionMessage = "User with id aNonExistingUser does not exist";
    User userUpdate = MockProvider.createMockUserUpdate();

    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId("aNonExistingUser")).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(null);

    UserProfileDto updateDto = UserProfileDto.fromUser(userUpdate);

    given()
        .pathParam("id", "aNonExistingUser")
        .body(updateDto).contentType(ContentType.JSON)
    .then()
        .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo(exceptionMessage))
    .when()
        .put(USER_PROFILE_URL);

    // nothing was saved
    verify(identityServiceMock, never()).saveUser(any(User.class));
    verifyLogs(Level.DEBUG, exceptionMessage);
  }

  @Test
  public void testPutProfileThrowsAuthorizationException() {
    User initialUser = MockProvider.createMockUser();
    User userUpdate = MockProvider.createMockUserUpdate();

    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(initialUser);

    String message = "exception expected";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).saveUser(any(User.class));

    UserProfileDto updateDto = UserProfileDto.fromUser(userUpdate);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
        .body(updateDto).contentType(ContentType.JSON)
    .then()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
        .put(USER_PROFILE_URL);

    verifyLogs(Level.DEBUG, message);
  }

  @Test
  public void testReadOnlyUserCreateFails() {
    String exceptionMessage = "Identity service implementation is read-only.";
    User newUser = MockProvider.createMockUser();
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given().body(UserDto.fromUser(newUser, true)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo(exceptionMessage))
      .when().post(USER_CREATE_URL);

    verify(identityServiceMock, never()).newUser(MockProvider.EXAMPLE_USER_ID);
    verifyLogs(Level.DEBUG, exceptionMessage);
  }

  @Test
  public void testReadOnlyPutUserProfileFails() {
    String exceptionMessage = "Identity service implementation is read-only.";
    User userUdpdate = MockProvider.createMockUser();
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
        .body(UserProfileDto.fromUser(userUdpdate)).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo(exceptionMessage))
    .when().put(USER_PROFILE_URL);

    verify(identityServiceMock, never()).saveUser(userUdpdate);
    verifyLogs(Level.DEBUG, exceptionMessage);
  }

  @Test
  public void testReadOnlyPutUserCredentialsFails() {
    String exceptionMessage = "Identity service implementation is read-only.";
    User userUdpdate = MockProvider.createMockUser();
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_USER_ID)
        .body(UserCredentialsDto.fromUser(userUdpdate)).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo(exceptionMessage))
    .when().put(USER_CREDENTIALS_URL);

    verify(identityServiceMock, never()).saveUser(userUdpdate);
    verifyLogs(Level.DEBUG, exceptionMessage);
  }

  @Test
  public void testReadOnlyUserDeleteFails() {
    String exceptionMessage = "Identity service implementation is read-only.";
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given().pathParam("id", MockProvider.EXAMPLE_USER_ID)
      .then().expect().statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo(exceptionMessage))
      .when().delete(USER_URL);

    verify(identityServiceMock, never()).deleteUser(MockProvider.EXAMPLE_USER_ID);
    verifyLogs(Level.DEBUG, exceptionMessage);
  }

  @Test
  public void testUnlockUser() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(USER_UNLOCK);

    verify(identityServiceMock).unlockUser(MockProvider.EXAMPLE_USER_ID);
  }

  @Test
  public void testUnlockUserNonExistingUser() {
    given()
      .pathParam("id", "non-existing")
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(USER_UNLOCK);
  }

  @Test
  public void testUnlockUserThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).unlockUser(MockProvider.EXAMPLE_USER_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_USER_ID)
    .then()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(USER_UNLOCK);

    verifyLogs(Level.DEBUG, message);
  }

  protected void verifyNoAuthorizationCheckPerformed() {
    verify(identityServiceMock, times(0)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(0)).isUserAuthorized(anyString(), anyList(), any(Permission.class), any(Resource.class));
  }

  protected void verifyLogs(Level logLevel, String message) {
    List<ILoggingEvent> logs = loggingRule.getLog();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getLevel()).isEqualTo(logLevel);
    assertThat(logs.get(0).getMessage()).containsIgnoringCase(message);
  }
}
