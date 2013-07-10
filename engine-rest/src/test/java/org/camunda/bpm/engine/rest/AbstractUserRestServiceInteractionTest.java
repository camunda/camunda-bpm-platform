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
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.dto.identity.UpdateUserPasswordDto;
import org.camunda.bpm.engine.rest.dto.identity.UserCreateDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractUserRestServiceInteractionTest extends AbstractRestServiceTest {
  
  protected static final String USER_URL = TEST_RESOURCE_ROOT_PATH + "/user/{id}";
  protected static final String USER_UPDATE_PASSWORD_URL = USER_URL + "/password";
  protected static final String CREATE_USER_URL = TEST_RESOURCE_ROOT_PATH + "/user/create";
  
  protected IdentityService identityServiceMock;
  
  @Before
  public void setupUserData() {
    
    identityServiceMock = mock(IdentityService.class);
    
    // mock identity service
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
    
  }
  
  @Test
  public void testGetSingleUser() {    
    User sampleUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(sampleUser);
    
    given().pathParam("id", MockProvider.EXAMPLE_USER_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_USER_ID))
      .body("firstName", equalTo(MockProvider.EXAMPLE_USER_FIRST_NAME))
      .body("lastName", equalTo(MockProvider.EXAMPLE_USER_LAST_NAME))
      .body("email", equalTo(MockProvider.EXAMPLE_USER_EMAIL))
      .when().get(USER_URL);
  }
  
  @Test
  public void testGetNonExistingUser() {    
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(anyString())).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(null);
    
    given().pathParam("id", "aNonExistingUser")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("User with id aNonExistingUser does not exist"))
      .when().get(USER_URL);
  }
  
  @Test
  public void testDeleteUser() {    
    given().pathParam("id", MockProvider.EXAMPLE_USER_ID)
      .expect().statusCode(Status.NO_CONTENT.getStatusCode())      
      .when().delete(USER_URL);
  }
  
  @Test
  public void testDeleteNonExistingUser() {    
    given().pathParam("id", "non-existing")
    .expect().statusCode(Status.NO_CONTENT.getStatusCode())      
    .when().delete(USER_URL);    
  }
  
  @Test
  public void testUpdateExistingUser() {    
    User initialUser = MockProvider.createMockUser();
    User userUpdate = MockProvider.createMockUserUpdate();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(initialUser);
    
    given().pathParam("id", MockProvider.EXAMPLE_USER_ID)
      .body(UserDto.fromUser(userUpdate)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())      
      .when().post(USER_URL);
    
    // initial user was updated
    verify(initialUser).setLastName(userUpdate.getLastName());
    verify(initialUser).setFirstName(userUpdate.getFirstName());
    verify(initialUser).setEmail(userUpdate.getEmail());
    verify(initialUser, never()).setPassword(any(String.class));   
    
    // and then saved
    verify(identityServiceMock).saveUser(initialUser);
  }
  
  @Test
  public void testUpdateUserPassword() {    
    User initialUser = MockProvider.createMockUser();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId(MockProvider.EXAMPLE_USER_ID)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.singleResult()).thenReturn(initialUser);
    
    UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
    dto.setUserId(initialUser.getId());
    dto.setPassword("new-password");
    
    given().pathParam("id", MockProvider.EXAMPLE_USER_ID)
      .body(dto).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())      
      .when().post(USER_UPDATE_PASSWORD_URL);
    
    // password was updated
    verify(initialUser).setPassword(dto.getPassword());
    
    // and then saved
    verify(identityServiceMock).saveUser(initialUser);
  }
  
  @Test
  public void testUpdateNonExistingUser() {
    User userUpdate = MockProvider.createMockUserUpdate();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId("aNonExistingUser")).thenReturn(sampleUserQuery);    
    // this time the query returns null
    when(sampleUserQuery.singleResult()).thenReturn(null);
    
    given().pathParam("id", "aNonExistingUser")
      .body(UserDto.fromUser(userUpdate)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("User with id aNonExistingUser does not exist"))
      .when().post(USER_URL);
    
    verify(identityServiceMock, never()).saveUser(any(User.class));    
  }
  
  @Test
  public void testUpdateNonExistingUserPassword() {    
    User initialUser = MockProvider.createMockUser();
    
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(identityServiceMock.createUserQuery()).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userId("aNonExistingUser")).thenReturn(sampleUserQuery);    
    // this time the query returns null
    when(sampleUserQuery.singleResult()).thenReturn(null);
    
    UpdateUserPasswordDto dto = new UpdateUserPasswordDto();
    dto.setUserId(initialUser.getId());
    dto.setPassword("new-password");
    
    given().pathParam("id", "aNonExistingUser")
    .body(dto).contentType(ContentType.JSON)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("User with id aNonExistingUser does not exist"))
    .when().post(USER_UPDATE_PASSWORD_URL);
  
    verify(identityServiceMock, never()).saveUser(any(User.class));    
  }

  @Test
  public void testUserCreate() {
    User newUser = MockProvider.createMockUser();    
    when(identityServiceMock.newUser(MockProvider.EXAMPLE_USER_ID)).thenReturn(newUser);
    
    UserCreateDto userCreateDto = new UserCreateDto();
    userCreateDto.setId(MockProvider.EXAMPLE_USER_ID);
    userCreateDto.setFirstName(MockProvider.EXAMPLE_USER_FIRST_NAME);
    userCreateDto.setLastName(MockProvider.EXAMPLE_USER_LAST_NAME);
    userCreateDto.setEmail(MockProvider.EXAMPLE_USER_EMAIL);
    userCreateDto.setPassword(MockProvider.EXAMPLE_USER_PASSWORD);
        
    given().body(userCreateDto).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
      .when().post(CREATE_USER_URL);
    
    verify(identityServiceMock).newUser(MockProvider.EXAMPLE_USER_ID);
    verify(newUser).setFirstName(MockProvider.EXAMPLE_USER_FIRST_NAME);
    verify(newUser).setLastName(MockProvider.EXAMPLE_USER_LAST_NAME);
    verify(newUser).setEmail(MockProvider.EXAMPLE_USER_EMAIL);
    verify(newUser).setPassword(MockProvider.EXAMPLE_USER_PASSWORD);
    verify(identityServiceMock).saveUser(newUser);
  }
  
  @Test
  public void testUserCreateExistingFails() {
    User newUser = MockProvider.createMockUser();    
    when(identityServiceMock.newUser(MockProvider.EXAMPLE_USER_ID)).thenReturn(newUser);
    doThrow(new RuntimeException("")).when(identityServiceMock).saveUser(newUser);
    
    given().body(UserDto.fromUser(newUser)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Exception while saving new user "))
      .when().post(CREATE_USER_URL);
    
    verify(identityServiceMock).newUser(MockProvider.EXAMPLE_USER_ID);
    verify(identityServiceMock).saveUser(newUser);
  }
  
  @Test
  public void testReadOnlyUserCreateFails() {
    User newUser = MockProvider.createMockUser();    
    when(identityServiceMock.isReadOnly()).thenReturn(true);
    
    given().body(UserDto.fromUser(newUser)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
      .when().post(CREATE_USER_URL);
    
    verify(identityServiceMock, never()).newUser(MockProvider.EXAMPLE_USER_ID);    
  }
  
  @Test
  public void testReadOnlyUserUpdateFails() {
    User userUdpdate = MockProvider.createMockUser();    
    when(identityServiceMock.isReadOnly()).thenReturn(true);
    
    given().pathParam("id", MockProvider.EXAMPLE_USER_ID)
       .body(UserDto.fromUser(userUdpdate)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
      .when().post(USER_URL);
    
    verify(identityServiceMock, never()).saveUser(userUdpdate);    
  }
  
  @Test
  public void testReadOnlyUserDeleteFails() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);
    
    given().pathParam("id", MockProvider.EXAMPLE_USER_ID)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
      .when().delete(USER_URL);
    
    verify(identityServiceMock, never()).deleteUser(MockProvider.EXAMPLE_USER_ID);    
  }
  
  @Test
  public void testReadOnlyUserPasswordUpdateFails() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    UpdateUserPasswordDto password = new UpdateUserPasswordDto();
    password.setUserId(MockProvider.EXAMPLE_USER_ID);
    password.setPassword("s3cret");
    
    given().pathParam("id", MockProvider.EXAMPLE_USER_ID)
     .body(password).contentType(ContentType.JSON)
     .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
     .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
     .body("message", equalTo("Identity service implementation is read-only."))
     .when().post(USER_UPDATE_PASSWORD_URL);
 
   verify(identityServiceMock, never()).saveUser(any(User.class));  
  }

}
