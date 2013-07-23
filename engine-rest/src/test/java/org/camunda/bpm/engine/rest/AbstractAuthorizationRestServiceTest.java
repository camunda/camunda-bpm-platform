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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.AuthorizationUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractAuthorizationRestServiceTest extends AbstractRestServiceTest {

  protected static final String RESOURCE_PATH = TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH;
  protected static final String AUTH_CHECK_PATH = RESOURCE_PATH + "/check";
  
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
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_PERMISSION_VALUE);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil)).thenReturn(true);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("permissionValue", MockProvider.EXAMPLE_PERMISSION_VALUE)
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
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_PERMISSION_VALUE);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil)).thenReturn(false);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("permissionValue", MockProvider.EXAMPLE_PERMISSION_VALUE)
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
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_PERMISSION_VALUE);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil, MockProvider.EXAMPLE_RESOURCE_ID)).thenReturn(true);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("permissionValue", MockProvider.EXAMPLE_PERMISSION_VALUE)
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
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_PERMISSION_VALUE);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil, MockProvider.EXAMPLE_RESOURCE_ID)).thenReturn(false);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("permissionValue", MockProvider.EXAMPLE_PERMISSION_VALUE)
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
    
    AuthorizationUtil authorizationUtil = new AuthorizationUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID, MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_PERMISSION_VALUE);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, authorizationUtil, authorizationUtil)).thenReturn(false);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("permissionValue", MockProvider.EXAMPLE_PERMISSION_VALUE)
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
        .queryParam("permissionValue", MockProvider.EXAMPLE_PERMISSION_VALUE)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())        
    .when().get(AUTH_CHECK_PATH);
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("permissionValue", MockProvider.EXAMPLE_PERMISSION_VALUE)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())        
    .when().get(AUTH_CHECK_PATH);
    
    
    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())        
    .when().get(AUTH_CHECK_PATH);
    
    given()
        .queryParam("permissionValue", MockProvider.EXAMPLE_PERMISSION_VALUE)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())        
    .when().get(AUTH_CHECK_PATH);
    
    verify(identityServiceMock, never()).getCurrentAuthentication();
    verify(authorizationServiceMock, never()).isUserAuthorized(any(String.class), any(List.class), any(Permission.class), any(Resource.class));
    verify(authorizationServiceMock, never()).isUserAuthorized(any(String.class), any(List.class), any(Permission.class), any(Resource.class), any(String.class));
    
  }
  
}
