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
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.BATCH;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.auth.DefaultPermissionProvider;
import org.camunda.bpm.engine.impl.cfg.auth.PermissionProvider;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.ResourceUtil;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.restassured.http.ContentType;

/**
 * @author Daniel Meyer
 *
 */
public class AuthorizationRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String SERVICE_PATH = TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH;
  protected static final String AUTH_CREATE_PATH = SERVICE_PATH + "/create";
  protected static final String AUTH_CHECK_PATH = SERVICE_PATH + "/check";
  protected static final String AUTH_RESOURCE_PATH = SERVICE_PATH + "/{id}";

  protected AuthorizationService authorizationServiceMock;
  protected IdentityService identityServiceMock;
  protected ProcessEngineConfigurationImpl processEngineConfigurationMock;

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  @Before
  public void setUpRuntimeData() {
    authorizationServiceMock = mock(AuthorizationServiceImpl.class);
    identityServiceMock = mock(IdentityServiceImpl.class);
    processEngineConfigurationMock = mock(ProcessEngineConfigurationImpl.class);

    when(processEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
    when(processEngine.getProcessEngineConfiguration()).thenReturn(processEngineConfigurationMock);
    when(processEngineConfigurationMock.getPermissionProvider()).thenReturn(new DefaultPermissionProvider());
  }

  @Test
  public void testIsUserAuthorizedTrue() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    ResourceUtil resource = new ResourceUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource)).thenReturn(true);

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

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testIsUserAuthorizedFalse() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    ResourceUtil resource = new ResourceUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource)).thenReturn(false);

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

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
  }

  @Test
  public void testIsUserAuthorizedBatchResource() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    String resourceName = BATCH.resourceName();
    int resourceType = BATCH.resourceType();
    ResourceUtil resource = new ResourceUtil(resourceName, resourceType);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, resourceType);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource)).thenReturn(true);

    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", resourceName)
        .queryParam("resourceType", resourceType)
    .then().expect().statusCode(Status.OK.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(resourceName))
        .body("resourceId", equalTo(null))
        .body("authorized", equalTo(true))
    .when().get(AUTH_CHECK_PATH);

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
  }

  @Test
  public void testIsUserAuthorizedProcessDefinitionResource() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    String resourceName = PROCESS_DEFINITION.resourceName();
    int resourceType = PROCESS_DEFINITION.resourceType();
    ResourceUtil resource = new ResourceUtil(resourceName, resourceType);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, resourceType);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource)).thenReturn(false);

    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", resourceName)
        .queryParam("resourceType", resourceType)
    .then().expect().statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(resourceName))
        .body("resourceId", equalTo(null))
        .body("authorized", equalTo(false))
    .when().get(AUTH_CHECK_PATH);

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testIsUserAuthorizedProcessInstanceResource() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    int resourceType = PROCESS_INSTANCE.resourceType();
    String resourceName = PROCESS_INSTANCE.resourceName();
    ResourceUtil resource = new ResourceUtil(resourceName, resourceType);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, resourceType);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource)).thenReturn(true);

    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", resourceName)
        .queryParam("resourceType", resourceType)
    .then().expect().statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(resourceName))
        .body("resourceId", equalTo(null))
        .body("authorized", equalTo(true))
    .when().get(AUTH_CHECK_PATH);

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testIsUserAuthorizedTaskResource() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    int resourceType = TASK.resourceType();
    String resourceName = TASK.resourceName();
    ResourceUtil resource = new ResourceUtil(resourceName, resourceType);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, resourceType);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource)).thenReturn(true);

    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", resourceName)
        .queryParam("resourceType", resourceType)
    .then().expect().statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(resourceName))
        .body("resourceId", equalTo(null))
        .body("authorized", equalTo(true))
    .when().get(AUTH_CHECK_PATH);

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testIsUserAuthorizedWithUserIdTrue() {

    List<String> currentUserGroups = new ArrayList<String>();
    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, currentUserGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);

    List<String> userToCheckGroups = setupGroupQueryMock();

    ResourceUtil resource = new ResourceUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, currentUserGroups, Permissions.READ, Resources.AUTHORIZATION)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID2, userToCheckGroups, permission, resource)).thenReturn(true);

    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
        .queryParam("userId", MockProvider.EXAMPLE_USER_ID2)
    .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME))
        .body("resourceId", equalTo(null))
        .body("authorized", equalTo(true))
    .when().get(AUTH_CHECK_PATH);

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, currentUserGroups, Permissions.READ, Resources.AUTHORIZATION);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID2, userToCheckGroups, permission, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
  }

  @Test
  public void testIsUserAuthorizedWithUserIdFalse() {

    List<String> currentUserGroups = new ArrayList<String>();
    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, currentUserGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);

    List<String> userToCheckGroups = setupGroupQueryMock();

    ResourceUtil resource = new ResourceUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, currentUserGroups, Permissions.READ, Resources.AUTHORIZATION)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID2, userToCheckGroups, permission, resource)).thenReturn(false);

    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
        .queryParam("userId", MockProvider.EXAMPLE_USER_ID2)
    .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
        .body("permissionName", equalTo(MockProvider.EXAMPLE_PERMISSION_NAME))
        .body("resourceName", equalTo(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME))
        .body("resourceId", equalTo(null))
        .body("authorized", equalTo(false))
    .when().get(AUTH_CHECK_PATH);

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, currentUserGroups, Permissions.READ, Resources.AUTHORIZATION);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID2, userToCheckGroups, permission, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
  }

  @Test
  public void testIsUserAuthorizedWithUserIdNoReadPermission() {

    List<String> currentUserGroups = new ArrayList<String>();
    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, currentUserGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);

    List<String> userToCheckGroups = setupGroupQueryMock();

    ResourceUtil resource = new ResourceUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, currentUserGroups, Permissions.READ, Resources.AUTHORIZATION)).thenReturn(false);

    given()
        .queryParam("permissionName", MockProvider.EXAMPLE_PERMISSION_NAME)
        .queryParam("resourceName", MockProvider.EXAMPLE_RESOURCE_TYPE_NAME)
        .queryParam("resourceType", MockProvider.EXAMPLE_RESOURCE_TYPE_ID)
        .queryParam("userId", MockProvider.EXAMPLE_USER_ID2)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("You must have READ permission for Authorization resource."))
    .when().get(AUTH_CHECK_PATH);

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, currentUserGroups, Permissions.READ, Resources.AUTHORIZATION);
    verify(authorizationServiceMock, never()).isUserAuthorized(MockProvider.EXAMPLE_USER_ID2, userToCheckGroups, permission, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
  }

  @Test
  public void testIsUserAuthorizedNotValidPermission() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    String resourceName = BATCH.resourceName();
    int resourceType = BATCH.resourceType();
    ResourceUtil resource = new ResourceUtil(resourceName, resourceType);

    // ACCESS permission is not valid for BATCH

    given()
        .queryParam("permissionName", Permissions.ACCESS.name())
        .queryParam("resourceName", resourceName)
        .queryParam("resourceType", resourceType)
    .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(BadUserRequestException.class.getSimpleName()))
        .body("message", equalTo("The permission 'ACCESS' is not valid for 'BATCH' resource type."))
    .when()
        .get(AUTH_CHECK_PATH);

    verify(authorizationServiceMock, times(0)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, Permissions.ACCESS, resource);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();
  }

  @Test
  public void testIsUserAuthorizedResourceIdTrue() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    ResourceUtil resource = new ResourceUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource, MockProvider.EXAMPLE_RESOURCE_ID)).thenReturn(true);

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

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource, MockProvider.EXAMPLE_RESOURCE_ID);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testIsUserAuthorizedResourceIdFalse() {

    List<String> exampleGroups = new ArrayList<String>();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, exampleGroups);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    ResourceUtil resource = new ResourceUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource, MockProvider.EXAMPLE_RESOURCE_ID)).thenReturn(false);

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

    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource, MockProvider.EXAMPLE_RESOURCE_ID);
    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  @SuppressWarnings("unchecked")
  public void testIsUserAuthorizedNoAuthentication() {

    List<String> exampleGroups = new ArrayList<String>();

    when(identityServiceMock.getCurrentAuthentication()).thenReturn(null);

    ResourceUtil resource = new ResourceUtil(MockProvider.EXAMPLE_RESOURCE_TYPE_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    Permission permission = getPermissionProvider().getPermissionForName(MockProvider.EXAMPLE_PERMISSION_NAME, MockProvider.EXAMPLE_RESOURCE_TYPE_ID);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, exampleGroups, permission, resource)).thenReturn(false);

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

    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization, processEngineConfigurationMock);

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

    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization, processEngineConfigurationMock);

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

    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization, processEngineConfigurationMock);

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
  public void testCreateAuthorizationThrowsAuthorizationException() {
    String message = "expected authorization exception";
    when(authorizationServiceMock.createNewAuthorization(Authorization.AUTH_TYPE_GRANT)).thenThrow(new AuthorizationException(message));

    Authorization authorization = MockProvider.createMockGrantAuthorization();
    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization, processEngineConfigurationMock);

    given()
        .body(dto)
        .contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
        .post(AUTH_CREATE_PATH);
  }

  @Test
  public void testCreateAuthorizationNotValidPermission() {
    Authorization authorization = MockProvider.createMockGrantAuthorization();
    when(authorizationServiceMock.createNewAuthorization(Authorization.AUTH_TYPE_GRANT)).thenReturn(authorization);

    Map<String, Object> jsonBody = new HashMap<String, Object>();

    jsonBody.put("type", Authorization.AUTH_TYPE_GRANT);
    jsonBody.put("permissions", Arrays.asList(Permissions.READ_INSTANCE.name()));
    jsonBody.put("userId", MockProvider.EXAMPLE_USER_ID + ","+MockProvider.EXAMPLE_USER_ID2);
    jsonBody.put("groupId", MockProvider.EXAMPLE_GROUP_ID+","+MockProvider.EXAMPLE_GROUP_ID2);
    jsonBody.put("resourceType", Resources.TASK.resourceType());
    jsonBody.put("resourceId", MockProvider.EXAMPLE_RESOURCE_ID);

    // READ_INSTANCE permission is not valid for TASK

    given()
        .body(jsonBody)
        .contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(BadUserRequestException.class.getSimpleName()))
        .body("message", equalTo("The permission 'READ_INSTANCE' is not valid for 'TASK' resource type."))
    .when()
        .post(AUTH_CREATE_PATH);

    verify(authorizationServiceMock, times(1)).createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    verify(authorizationServiceMock, never()).saveAuthorization(authorization);
  }

  @Test
  public void testSaveAuthorizationThrowsAuthorizationException() {
    String message = "expected authorization exception";
    when(authorizationServiceMock.saveAuthorization(any(Authorization.class))).thenThrow(new AuthorizationException(message));

    Authorization authorization = MockProvider.createMockGrantAuthorization();
    when(authorizationServiceMock.createNewAuthorization(Authorization.AUTH_TYPE_GRANT)).thenReturn(authorization);
    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization, processEngineConfigurationMock);

    given()
        .body(dto)
        .contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
        .post(AUTH_CREATE_PATH);
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
  public void testDeleteAuthorizationThrowsAuthorizationException() {
    Authorization authorization = MockProvider.createMockGlobalAuthorization();

    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);

    String message = "expected authorization exception";
    doThrow(new AuthorizationException(message)).when(authorizationServiceMock).deleteAuthorization(MockProvider.EXAMPLE_AUTHORIZATION_ID);

    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
        .delete(AUTH_RESOURCE_PATH);
  }

  @Test
  public void testUpdateAuthorization() {

    Authorization authorization = MockProvider.createMockGlobalAuthorization();

    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);

    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization, processEngineConfigurationMock);

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

    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization, processEngineConfigurationMock);

    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
        .body(dto).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("message", equalTo("Authorization with id "+MockProvider.EXAMPLE_AUTHORIZATION_ID+" does not exist."))
    .when()
        .put(AUTH_RESOURCE_PATH);

    verify(authorizationServiceMock, never()).saveAuthorization(authorization);

  }

  @Test
  public void testUpdateAuthorizationThrowsAuthorizationException() {
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    AuthorizationDto dto = AuthorizationDto.fromAuthorization(authorization, processEngineConfigurationMock);

    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);

    String message = "expected authorization exception";
    when(authorizationServiceMock.saveAuthorization(any(Authorization.class))).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
      .body(dto).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
        .put(AUTH_RESOURCE_PATH);
  }

  @Test
  public void testUpdateAuthorizationNotValidPermission() {
    Authorization authorization = MockProvider.createMockGlobalAuthorization();
    AuthorizationQuery authorizationQuery = mock(AuthorizationQuery.class);
    when(authorizationServiceMock.createAuthorizationQuery()).thenReturn(authorizationQuery);
    when(authorizationQuery.authorizationId(MockProvider.EXAMPLE_AUTHORIZATION_ID)).thenReturn(authorizationQuery);
    when(authorizationQuery.singleResult()).thenReturn(authorization);

    Map<String, Object> jsonBody = new HashMap<String, Object>();

    jsonBody.put("permissions", Arrays.asList(Permissions.TASK_WORK.name()));
    jsonBody.put("userId", MockProvider.EXAMPLE_USER_ID + ","+MockProvider.EXAMPLE_USER_ID2);
    jsonBody.put("groupId", MockProvider.EXAMPLE_GROUP_ID+","+MockProvider.EXAMPLE_GROUP_ID2);
    jsonBody.put("resourceType", Resources.PROCESS_INSTANCE.resourceType());
    jsonBody.put("resourceId", MockProvider.EXAMPLE_RESOURCE_ID);

    // READ_INSTANCE permission is not valid for TASK

    given()
        .pathParam("id", MockProvider.EXAMPLE_AUTHORIZATION_ID)
        .body(jsonBody).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(BadUserRequestException.class.getSimpleName()))
        .body("message", equalTo("The permission 'TASK_WORK' is not valid for 'PROCESS_INSTANCE' resource type."))
    .when()
        .put(AUTH_RESOURCE_PATH);

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
        .body("removalTime", equalTo(MockProvider.EXAMPLE_AUTH_REMOVAL_TIME))
        .body("rootProcessInstanceId", equalTo(authorization.getRootProcessInstanceId()))
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
        .options(SERVICE_PATH);

    verify(identityServiceMock, times(1)).getCurrentAuthentication();

  }

  @Test
  public void testAuthenticationRestServiceOptionsWithAuthorizationDisabled() {
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH;

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
      .options(SERVICE_PATH);

    verifyNoAuthorizationCheckPerformed();
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

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

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

    when(processEngine.getProcessEngineConfiguration().isAuthorizationEnabled()).thenReturn(true);

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

    when(processEngine.getProcessEngineConfiguration().isAuthorizationEnabled()).thenReturn(true);

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

  @Test
  public void testAuthorizationResourceOptionsWithAuthorizationDisabled() {
    String fullAuthorizationUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + AuthorizationRestService.PATH + "/" + MockProvider.EXAMPLE_AUTHORIZATION_ID;

    when(processEngine.getProcessEngineConfiguration().isAuthorizationEnabled()).thenReturn(false);

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

    verifyNoAuthorizationCheckPerformed();
  }

  protected void verifyNoAuthorizationCheckPerformed() {
    verify(identityServiceMock, times(0)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(0)).isUserAuthorized(anyString(), anyList(), any(Permission.class), any(Resource.class));
  }


  protected PermissionProvider getPermissionProvider() {
    return processEngineConfigurationMock.getPermissionProvider();
  }

  protected List<String> setupGroupQueryMock() {
    GroupQuery mockGroupQuery = mock(GroupQuery.class);
    List<Group> groupMocks = MockProvider.createMockGroups();
    when(identityServiceMock.createGroupQuery()).thenReturn(mockGroupQuery);
    when(mockGroupQuery.groupMember(anyString())).thenReturn(mockGroupQuery);
    when(mockGroupQuery.unlimitedList()).thenReturn(groupMocks);
    List<String> groupIds = new ArrayList<String>();
    for (Group group : groupMocks) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }
}
