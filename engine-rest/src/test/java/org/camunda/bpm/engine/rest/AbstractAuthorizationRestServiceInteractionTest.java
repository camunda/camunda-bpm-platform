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
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationDto;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.AuthorizationUtil;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractAuthorizationRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String SERVICE_PATH = TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH;
  protected static final String AUTH_CREATE_PATH = SERVICE_PATH + "/create";
  protected static final String AUTH_CHECK_PATH = SERVICE_PATH + "/check";
  protected static final String AUTH_RESOURCE_PATH = SERVICE_PATH + "/{id}";
  
  protected AuthorizationService authorizationServiceMock;
  protected IdentityService identityServiceMock;
  
  @Before
  public void setUpRuntimeData() {
    authorizationServiceMock = mock(AuthorizationServiceImpl.class);
    identityServiceMock = mock(IdentityServiceImpl.class);
    
    when(processEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
  }
  
  @Test
  public void testIsUserAuthorizedTrue() {
    
    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);    
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);    
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil)).thenReturn(true);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME))
        .body("resourceId", equalTo(null))
        .body("authorized", equalTo(true))
    .when().get(AUTH_CHECK_PATH);
    
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
    
  }
  
  @Test
  public void testIsUserAuthorizedFalse() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);    
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);    
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil)).thenReturn(false);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME))
        .body("resourceId", equalTo(null))
        .body("authorized", equalTo(false))
    .when().get(AUTH_CHECK_PATH);
    
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
  }
  
  @Test
  public void testIsUserAuthorizedResourceIdTrue() {
    
    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);    
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);    
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil, MockProvider.EXAMPLE_RESOURCE_ID)).thenReturn(true);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
        .queryParam("resourceId", MockProvider.EXAMPLE_RESOURCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME))
        .body("resourceId", equalTo(MockProvider.EXAMPLE_RESOURCE_ID))
        .body("authorized", equalTo(true))
    .when().get(AUTH_CHECK_PATH);
    
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil, MockProvider.EXAMPLE_RESOURCE_ID);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
        
  }
  
  @Test
  public void testIsUserAuthorizedResourceIdFalse() {
    
    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);    
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);    
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil, MockProvider.EXAMPLE_RESOURCE_ID)).thenReturn(false);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
        .queryParam("resourceId", MockProvider.EXAMPLE_RESOURCE_ID)
    .then().expect().statusCode(Status.OK.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME))
        .body("resourceId", equalTo(MockProvider.EXAMPLE_RESOURCE_ID))
        .body("authorized", equalTo(false))
    .when().get(AUTH_CHECK_PATH);
    
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil, MockProvider.EXAMPLE_RESOURCE_ID);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
        
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testIsUserAuthorizedNoAuthentication() {
    
    List<String> exampleGroups = new ArrayList<String>();

    when(identityServiceMock.getCurrentAuthentication()).thenReturn(null);    
    
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil)).thenReturn(false);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
    .then().expect().statusCode(Status.UNAUTHORIZED.getStatusCode())        
    .when().get(AUTH_CHECK_PATH);
    
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
    verify(authorizationServiceMock, never()).isUserAuthorized(any(String.class), any(List.class), any(Permission.class), any(Resource.class));
    verify(authorizationServiceMock, never()).isUserAuthorized(any(String.class), any(List.class), any(Permission.class), any(Resource.class), any(String.class));
    
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testIsUserAuthorizedBadRequests() {
        
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())        
    .when().get(AUTH_CHECK_PATH);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())        
    .when().get(AUTH_CHECK_PATH);
           
    given()
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())        
    .when().get(AUTH_CHECK_PATH);
    
    verify(identityServiceMock, never()).getCurrentAuthentication();
    verify(authorizationServiceMock, never()).isUserAuthorized(any(String.class), any(List.class), any(Permission.class), any(Resource.class));
    verify(authorizationServiceMock, never()).isUserAuthorized(any(String.class), any(List.class), any(Permission.class), any(Resource.class), any(String.class));
    
  }
  
  @Test
  public void testCreateGlobalAuthorization() {
    
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    when(authorizationServiceMock.createNewAuthorization(Authorization.AUTH_TYPE_GLOBAL)).thenReturn(authorization);
    when(authorizationServiceMock.saveAuthorization(authorization)).thenReturn(authorization);
      
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);
    
    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization);
    
    given()
        .body(dto).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
        .post(AUTH_CREATE_PATH);
    
    verify(authorizationServiceMock).createNewAuthorization(Authorization.AUTH_TYPE_GLOBAL);
    verify(authorization).setUserId(Authorization.ANY);
    verify(authorization, times(4)).setResourceType(authorization.getAuthorizationType());
    verify(authorization, times(2)).setResourceId(authorization.getResourceId());
    verify(authorization, times(2)).setPermissions(authorization.getPermissions(Permissions.values()));
    verify(authorizationServiceMock).saveAuthorization(authorization);
  }
  
  @Test
  public void testCreateGrantAuthorization() {

    Authorization authorization = MockProvider.createMockGrantAuthorization();
    when(authorizationServiceMock.createNewAuthorization(Authorization.AUTH_TYPE_GRANT)).thenReturn(authorization);
    when(authorizationServiceMock.saveAuthorization(authorization)).thenReturn(authorization);
      
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);
    
    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization);
    
    given()
        .body(dto).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
        .post(AUTH_CREATE_PATH);
    
    verify(authorizationServiceMock).createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    verify(authorization, times(2)).setUserId(authorization.getUserId());
    verify(authorization, times(4)).setResourceType(authorization.getAuthorizationType());
    verify(authorization, times(2)).setResourceId(authorization.getResourceId());
    verify(authorization, times(2)).setPermissions(authorization.getPermissions(Permissions.values()));
    verify(authorizationServiceMock).saveAuthorization(authorization);
  }
  
  @Test
  public void testCreateRevokeAuthorization() {

    Authorization authorization = MockProvider.createMockRevokeAuthorization();
    when(authorizationServiceMock.createNewAuthorization(Authorization.AUTH_TYPE_REVOKE)).thenReturn(authorization);
    when(authorizationServiceMock.saveAuthorization(authorization)).thenReturn(authorization);
      
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);
    
    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization);
    
    given()
        .body(dto).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
        .post(AUTH_CREATE_PATH);
    
    verify(authorizationServiceMock).createNewAuthorization(Authorization.AUTH_TYPE_REVOKE);
    verify(authorization, times(2)).setUserId(authorization.getUserId());
    verify(authorization, times(4)).setResourceType(authorization.getAuthorizationType());
    verify(authorization, times(2)).setResourceId(authorization.getResourceId());
    verify(authorization, times(2)).setPermissions(authorization.getPermissions(Permissions.values()));
    verify(authorizationServiceMock).saveAuthorization(authorization);
  }
  
  @Test
  public void testDeleteAuthorization() {
    
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);
     
    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .delete(AUTH_RESOURCE_PATH);
    
    verify(authorizationQuery).authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID);
    verify(authorizationServiceMock).deleteAuthorization(MockProvider.EXAMPLE_AUTHORIZATION_ID);
    
  }
  
  @Test
  public void testDeleteNonExistingAuthorization() {
    
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(null);
     
    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
    .then().expect()
        .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("message", equalTo("Authorization with id "+MockProvider.EXAMPLE_AUTHORIZATION_ID+" does not exist."))
    .when()
        .delete(AUTH_RESOURCE_PATH);
    
    verify(authorizationServiceMock, never()).deleteAuthorization(MockProvider.EXAMPLE_AUTHORIZATION_ID);
    
  }
  
  @Test
  public void testUpdateAuthorization() {
    
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);
     
    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization);
    
    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
        .body(dto).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(AUTH_RESOURCE_PATH);
    
    verify(authorizationQuery).authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID);
    
    verify(authorization).setGroupId(dto.getGroupId());
    verify(authorization).setUserId(dto.getUserId());
    verify(authorization).setResourceId(dto.getResourceId());
    verify(authorization).setResourceType(dto.getResourceType());
    
    verify(authorizationServiceMock).saveAuthorization(authorization);
    
  }
  
  @Test
  public void testUpdateNonExistingAuthorization() {
    
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(null);
    
    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization);
    
    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
        .body(dto).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("message", equalTo("Authorization with id "+MockProvider.EXAMPLE_AUTHORIZATION_ID+" does not exist."))
    .when()
        .delete(AUTH_RESOURCE_PATH);
    
    verify(authorizationServiceMock, never()).saveAuthorization(authorization);
    
  }
  
  @Test
  public void testGetAuthorizationById() {
    
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);
     
    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
    .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
        .body("id", equalTo(authorization.getId()))
        .body("type", equalTo(authorization.getAuthorizationType()))
        .body("permissions[0]", equalTo(Permissions.READ.getName()))
        .body("permissions[1]", equalTo(Permissions.UPDATE.getName()))
        .body("userId", equalTo(authorization.getUserId()))
        .body("groupId", equalTo(authorization.getGroupId()))
        .body("resourceType", equalTo(authorization.getResourceType()))
        .body("resourceId", equalTo(authorization.getResourceId()))        
    .when()
        .get(AUTH_RESOURCE_PATH);
     
  }
  
  @Test
  public void testGetNonExistingAuthorizationById() {
    
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(null);
    
    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
    .then().expect()
        .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("message", equalTo("Authorization with id "+MockProvider.EXAMPLE_AUTHORIZATION_ID+" does not exist."))
    .when()
        .get(AUTH_RESOURCE_PATH);
    
  }
  
  @Test
  public void testAuthenticationRestServiceOptions() {
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH;
        
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
        .options(SERVICE_PATH);
    
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
    
  }
    
  @Test
  public void testAuthorizationResourceOptions() {
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH + "/" + MockProvider.EXAMPLE_AUTHORIZATION_ID;
    
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(null);
    
    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())
        
        .body("links[0].href", equalTo(fullAuthorizationUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))
        
        .body("links[1].href", equalTo(fullAuthorizationUrl))
        .body("links[1].method", equalTo(HttpMethod.DELETE))
        .body("links[1].rel", equalTo("delete"))
        
        .body("links[2].href", equalTo(fullAuthorizationUrl))
        .body("links[2].method", equalTo(HttpMethod.PUT))
        .body("links[2].rel", equalTo("update"))
        
    .when()
        .options(AUTH_RESOURCE_PATH);
    
    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    
  }
  
  @Test
  public void testAuthorizationResourceOptionsUnauthorized() {
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH + "/" + MockProvider.EXAMPLE_AUTHORIZATION_ID;
    
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);    
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, AUTHORIZATION, MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, AUTHORIZATION, MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(false);
    
    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())
        
        .body("links[0].href", equalTo(fullAuthorizationUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))
        
        .body("links[1]", nullValue())
        .body("links[2]", nullValue())
                
    .when()
        .options(AUTH_RESOURCE_PATH);
    
    verify(identityServiceMock, times(2)).getCurrentAuthentication();    
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, AUTHORIZATION, MockProvider.EXAMPLE_AUTHORIZATION_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, AUTHORIZATION, MockProvider.EXAMPLE_AUTHORIZATION_ID);
    
  }
    
  @Test
  public void testAuthorizationResourceOptionsUpdateUnauthorized() {
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH + "/" + MockProvider.EXAMPLE_AUTHORIZATION_ID;
    
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);    
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, AUTHORIZATION, MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, AUTHORIZATION, MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(false);
    
    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
    .then()
        .statusCode(Status.OK.getStatusCode())
        
        .body("links[0].href", equalTo(fullAuthorizationUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))
        
        .body("links[1].href", equalTo(fullAuthorizationUrl))
        .body("links[1].method", equalTo(HttpMethod.DELETE))
        .body("links[1].rel", equalTo("delete"))
        
        .body("links[2]", nullValue())
                
    .when()
        .options(AUTH_RESOURCE_PATH);
    
    verify(identityServiceMock, times(2)).getCurrentAuthentication();    
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, AUTHORIZATION, MockProvider.EXAMPLE_AUTHORIZATION_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, AUTHORIZATION, MockProvider.EXAMPLE_AUTHORIZATION_ID);
    
  }
    
}
