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
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.TENANT;
import static org.camunda.bpm.engine.authorization.Resources.TENANT_MEMBERSHIP;
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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.dto.identity.TenantDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.restassured.http.ContentType;

public class TenantRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/tenant";
  protected static final String TENANT_URL = SERVICE_URL + "/{id}";
  protected static final String TENANT_CREATE_URL = SERVICE_URL + "/create";
  protected static final String TENANT_USER_MEMBERS_URL = TENANT_URL + "/user-members";
  protected static final String TENANT_USER_MEMBER_URL = TENANT_USER_MEMBERS_URL + "/{userId}";
  protected static final String TENANT_GROUP_MEMBERS_URL = TENANT_URL + "/group-members";
  protected static final String TENANT_GROUP_MEMBER_URL = TENANT_GROUP_MEMBERS_URL + "/{groupId}";

  protected IdentityService identityServiceMock;
  protected AuthorizationService authorizationServiceMock;
  protected ProcessEngineConfiguration processEngineConfigurationMock;

  protected Tenant mockTenant;
  protected TenantQuery mockQuery;

  @Before
  public void setupData() {

    identityServiceMock = mock(IdentityService.class);
    authorizationServiceMock = mock(AuthorizationService.class);
    processEngineConfigurationMock = mock(ProcessEngineConfiguration.class);

    // mock identity service
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
    // authorization service
    when(processEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);
    // process engine configuration
    when(processEngine.getProcessEngineConfiguration()).thenReturn(processEngineConfigurationMock);

    mockTenant = MockProvider.createMockTenant();
    mockQuery = setUpMockQuery(mockTenant);
  }

  protected TenantQuery setUpMockQuery(Tenant tenant) {
    TenantQuery query = mock(TenantQuery.class);
    when(query.tenantId(anyString())).thenReturn(query);
    when(query.singleResult()).thenReturn(tenant);

    when(identityServiceMock.createTenantQuery()).thenReturn(query);

    return query;
  }

  @Test
  public void getTenant() {
   given()
     .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
   .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .body("name", equalTo(MockProvider.EXAMPLE_TENANT_NAME))
   .when()
     .get(TENANT_URL);
  }

  @Test
  public void getNonExistingTenant() {
    when(mockQuery.singleResult()).thenReturn(null);

    given()
      .pathParam("id", "aNonExistingTenant")
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Tenant with id aNonExistingTenant does not exist"))
    .when()
      .get(TENANT_URL);
  }

  @Test
  public void deleteTenant() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(TENANT_URL);
  }

  @Test
  public void deleteNonExistingTenant() {
    given().pathParam("id", "aNonExistingTenant")
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(TENANT_URL);
  }

  @Test
  public void deleteTenantThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).deleteTenant(MockProvider.EXAMPLE_TENANT_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .delete(TENANT_URL);
  }

  @Test
  public void updateTenant() {
    Tenant updatedTenant = MockProvider.createMockTenant();
    when(updatedTenant.getName()).thenReturn("updatedName");

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .body(TenantDto.fromTenant(updatedTenant))
      .contentType(ContentType.JSON)
    .then()
      .expect().statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(TENANT_URL);

    // tenant was updated
    verify(mockTenant).setName(updatedTenant.getName());

    // and then saved
    verify(identityServiceMock).saveTenant(mockTenant);
  }

  @Test
  public void updateNonExistingTenant() {
    Tenant updatedTenant = MockProvider.createMockTenant();
    when(updatedTenant.getName()).thenReturn("updatedName");

    when(mockQuery.singleResult()).thenReturn(null);

    given()
      .pathParam("id", "aNonExistingTenant")
      .body(TenantDto.fromTenant(updatedTenant))
      .contentType(ContentType.JSON)
    .then()
      .expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Tenant with id aNonExistingTenant does not exist"))
    .when()
      .put(TENANT_URL);

    verify(identityServiceMock, never()).saveTenant(any(Tenant.class));
  }

  @Test
  public void updateTenantThrowsAuthorizationException() {
    Tenant updatedTenant = MockProvider.createMockTenant();
    when(updatedTenant.getName()).thenReturn("updatedName");

    String message = "exception expected";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).saveTenant(any(Tenant.class));

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .body(TenantDto.fromTenant(updatedTenant))
      .contentType(ContentType.JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(TENANT_URL);
  }

  @Test
  public void createTenant() {
    Tenant newTenant = MockProvider.createMockTenant();
    when(identityServiceMock.newTenant(MockProvider.EXAMPLE_TENANT_ID)).thenReturn(newTenant);

    given()
        .body(TenantDto.fromTenant(mockTenant)).contentType(ContentType.JSON)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .post(TENANT_CREATE_URL);

    verify(identityServiceMock).newTenant(MockProvider.EXAMPLE_TENANT_ID);
    verify(newTenant).setName(MockProvider.EXAMPLE_TENANT_NAME);
    verify(identityServiceMock).saveTenant(newTenant);
  }

  @Test
  public void createExistingTenant() {
    Tenant newTenant = MockProvider.createMockTenant();
    when(identityServiceMock.newTenant(MockProvider.EXAMPLE_TENANT_ID)).thenReturn(newTenant);

    String message = "exception expected";
    doThrow(new ProcessEngineException(message)).when(identityServiceMock).saveTenant(newTenant);

    given()
      .body(TenantDto.fromTenant(newTenant)).contentType(ContentType.JSON)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(TENANT_CREATE_URL);
  }

  @Test
  public void createTenantThrowsAuthorizationException() {
    Tenant newTenant = MockProvider.createMockTenant();

    String message = "exception expected";
    when(identityServiceMock.newTenant(MockProvider.EXAMPLE_TENANT_ID)).thenThrow(new AuthorizationException(message));

    given()
      .body(TenantDto.fromTenant(newTenant))
      .contentType(ContentType.JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(TENANT_CREATE_URL);
  }

  @Test
  public void saveTenantThrowsAuthorizationException() {
    Tenant newTenant = MockProvider.createMockTenant();
    when(identityServiceMock.newTenant(MockProvider.EXAMPLE_TENANT_ID)).thenReturn(newTenant);

    String message = "exception expected";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).saveTenant(newTenant);

    given()
      .body(TenantDto.fromTenant(newTenant))
      .contentType(ContentType.JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(TENANT_CREATE_URL);
  }

  @Test
  public void userRestServiceOptionUnauthenticated() {
    String fullAuthorizationUrl = getFullAuthorizationUrl();

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
  public void userRestServiceOptionUnauthorized() {
    String fullAuthorizationUrl = getFullAuthorizationUrl();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT, ANY)).thenReturn(false);

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

        .body("links[2]", nullValue())

    .when()
        .options(SERVICE_URL);

    verify(identityServiceMock, times(1)).getCurrentAuthentication();
  }

  @Test
  public void userRestServiceOptionAuthorized() {
    String fullAuthorizationUrl = getFullAuthorizationUrl();

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT, ANY)).thenReturn(true);

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
  public void userRestServiceOptionsWithAuthorizationDisabled() {
    String fullAuthorizationUrl = getFullAuthorizationUrl();

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
  public void tenantResourceOptionsUnauthenticated() {
    String fullTenantUrl = getFullAuthorizationTenantUrl();

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then()
        .expect().statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullTenantUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullTenantUrl))
        .body("links[1].method", equalTo(HttpMethod.DELETE))
        .body("links[1].rel", equalTo("delete"))

        .body("links[2].href", equalTo(fullTenantUrl))
        .body("links[2].method", equalTo(HttpMethod.PUT))
        .body("links[2].rel", equalTo("update"))

    .when().options(TENANT_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
  }

  @Test
  public void tenantResourceOptionsUnauthorized() {
    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, TENANT, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(false);

    String fullTenantUrl = getFullAuthorizationTenantUrl();

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then()
        .expect().statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullTenantUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1]", nullValue())
        .body("links[2]", nullValue())

    .when().options(TENANT_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT, MockProvider.EXAMPLE_TENANT_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, TENANT, MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void tenantResourceOptionsAuthorized() {
    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, TENANT, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(false);

    String fullTenantUrl = getFullAuthorizationTenantUrl();

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
        .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then()
        .expect().statusCode(Status.OK.getStatusCode())

        .body("links[0].href", equalTo(fullTenantUrl))
        .body("links[0].method", equalTo(HttpMethod.GET))
        .body("links[0].rel", equalTo("self"))

        .body("links[1].href", equalTo(fullTenantUrl))
        .body("links[1].method", equalTo(HttpMethod.DELETE))
        .body("links[1].rel", equalTo("delete"))

        .body("links[2]", nullValue())

    .when().options(TENANT_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT, MockProvider.EXAMPLE_TENANT_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, UPDATE, TENANT, MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void tenantResourceOptionsWithAuthorizationDisabled() {
    String fullTenantUrl = getFullAuthorizationTenantUrl();

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullTenantUrl))
      .body("links[0].method", equalTo(HttpMethod.GET))
      .body("links[0].rel", equalTo("self"))

      .body("links[1].href", equalTo(fullTenantUrl))
      .body("links[1].method", equalTo(HttpMethod.DELETE))
      .body("links[1].rel", equalTo("delete"))

      .body("links[2].href", equalTo(fullTenantUrl))
      .body("links[2].method", equalTo(HttpMethod.PUT))
      .body("links[2].rel", equalTo("update"))

    .when()
      .options(TENANT_URL);

    verifyNoAuthorizationCheckPerformed();
  }

  @Test
  public void createTenantUserMembership() {

    given()
        .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
        .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(TENANT_USER_MEMBER_URL);

    verify(identityServiceMock).createTenantUserMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_USER_ID);
  }

  @Test
  public void createTenantGroupMembership() {

    given()
        .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
        .pathParam("groupId", MockProvider.EXAMPLE_GROUP_ID)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(TENANT_GROUP_MEMBER_URL);

    verify(identityServiceMock).createTenantGroupMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void createTenantUserMembershipThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).createTenantUserMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_USER_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
        .put(TENANT_USER_MEMBER_URL);
  }

  @Test
  public void deleteTenantUserMembership() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .delete(TENANT_USER_MEMBER_URL);

    verify(identityServiceMock).deleteTenantUserMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_USER_ID);
  }

  @Test
  public void deleteTenantGroupMembership() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .pathParam("groupId", MockProvider.EXAMPLE_GROUP_ID)
    .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .delete(TENANT_GROUP_MEMBER_URL);

    verify(identityServiceMock).deleteTenantGroupMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void deleteTenantGroupMembershipThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(identityServiceMock).deleteTenantGroupMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_GROUP_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .pathParam("groupId", MockProvider.EXAMPLE_GROUP_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
        .delete(TENANT_GROUP_MEMBER_URL);
  }

  @Test
  public void tenantUserMembershipResourceOptionsUnauthenticated() {
    String fullMembersUrl = getFullAuthorizationTenantUrl() + "/user-members";

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullMembersUrl))
      .body("links[0].method", equalTo(HttpMethod.DELETE))
      .body("links[0].rel", equalTo("delete"))

      .body("links[1].href", equalTo(fullMembersUrl))
      .body("links[1].method", equalTo(HttpMethod.PUT))
      .body("links[1].rel", equalTo("create"))

    .when()
      .options(TENANT_USER_MEMBERS_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
  }

  @Test
  public void tenantUserMembershipResourceOptionsAuthorized() {
    String fullMembersUrl = getFullAuthorizationTenantUrl() + "/user-members";

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(true);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullMembersUrl))
      .body("links[0].method", equalTo(HttpMethod.DELETE))
      .body("links[0].rel", equalTo("delete"))

      .body("links[1].href", equalTo(fullMembersUrl))
      .body("links[1].method", equalTo(HttpMethod.PUT))
      .body("links[1].rel", equalTo("create"))

    .when()
      .options(TENANT_USER_MEMBERS_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void tenantGroupMembershipResourceOptionsAuthorized() {
    String fullMembersUrl = getFullAuthorizationTenantUrl() + "/group-members";

    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(true);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(true);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullMembersUrl))
      .body("links[0].method", equalTo(HttpMethod.DELETE))
      .body("links[0].rel", equalTo("delete"))

      .body("links[1].href", equalTo(fullMembersUrl))
      .body("links[1].method", equalTo(HttpMethod.PUT))
      .body("links[1].rel", equalTo("create"))

    .when()
      .options(TENANT_GROUP_MEMBERS_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void tenantUserMembershipResourceOptionsUnauthorized() {
    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(false);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0]", nullValue())

      .body("links[1]", nullValue())

    .when()
      .options(TENANT_USER_MEMBERS_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void tenantGroupMembershipResourceOptionsUnauthorized() {
    Authentication authentication = new Authentication(MockProvider.EXAMPLE_USER_ID, null);
    when(identityServiceMock.getCurrentAuthentication()).thenReturn(authentication);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(false);
    when(authorizationServiceMock.isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID)).thenReturn(false);

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0]", nullValue())

      .body("links[1]", nullValue())

    .when()
      .options(TENANT_GROUP_MEMBERS_URL);

    verify(identityServiceMock, times(2)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, DELETE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID);
    verify(authorizationServiceMock, times(1)).isUserAuthorized(MockProvider.EXAMPLE_USER_ID, null, CREATE, TENANT_MEMBERSHIP, MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void tenantUserMembershipResourceOptionsWithAuthorizationDisabled() {
    String fullMembersUrl = getFullAuthorizationTenantUrl() + "/user-members";

    when(processEngineConfigurationMock.isAuthorizationEnabled()).thenReturn(false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())

      .body("links[0].href", equalTo(fullMembersUrl))
      .body("links[0].method", equalTo(HttpMethod.DELETE))
      .body("links[0].rel", equalTo("delete"))

      .body("links[1].href", equalTo(fullMembersUrl))
      .body("links[1].method", equalTo(HttpMethod.PUT))
      .body("links[1].rel", equalTo("create"))

    .when()
      .options(TENANT_USER_MEMBERS_URL);

    verifyNoAuthorizationCheckPerformed();
  }

  @Test
  public void failToCreateTenantForReadOnlyService() {
    Tenant newTenant = MockProvider.createMockTenant();
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
      .body(TenantDto.fromTenant(newTenant)).contentType(ContentType.JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
    .when()
      .post(TENANT_CREATE_URL);

    verify(identityServiceMock, never()).newTenant(MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void failToUpdateTenantForReadOnlyService() {
    Tenant updatedTenant = MockProvider.createMockTenant();
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .body(TenantDto.fromTenant(updatedTenant)).contentType(ContentType.JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
    .when()
      .put(TENANT_URL);

    verify(identityServiceMock, never()).saveTenant(mockTenant);
  }

  @Test
  public void failToDeleteTenantForReadOnlyService() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
    .when()
      .delete(TENANT_URL);

    verify(identityServiceMock, never()).deleteTenant(MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  public void failToCreateTenantUserMembershipForReadOnlyService() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Identity service implementation is read-only."))
    .when()
        .put(TENANT_USER_MEMBER_URL);

    verify(identityServiceMock, never()).createTenantUserMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_USER_ID);
  }

  @Test
  public void failToCreateTenantGroupMembershipForReadOnlyService() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .pathParam("groupId", MockProvider.EXAMPLE_GROUP_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Identity service implementation is read-only."))
    .when()
        .put(TENANT_GROUP_MEMBER_URL);

    verify(identityServiceMock, never()).createTenantGroupMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_GROUP_ID);
  }

  @Test
  public void failToDeleteTenantUserMembershipForReadOnlyService() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .pathParam("userId", MockProvider.EXAMPLE_USER_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Identity service implementation is read-only."))
    .when()
        .delete(TENANT_USER_MEMBER_URL);

    verify(identityServiceMock, never()).deleteTenantUserMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_USER_ID);
  }

  @Test
  public void failToDeleteTenantGroupMembershipForReadOnlyService() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TENANT_ID)
      .pathParam("groupId", MockProvider.EXAMPLE_GROUP_ID)
    .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Identity service implementation is read-only."))
    .when()
        .delete(TENANT_GROUP_MEMBER_URL);

    verify(identityServiceMock, never()).deleteTenantGroupMembership(MockProvider.EXAMPLE_TENANT_ID, MockProvider.EXAMPLE_GROUP_ID);
  }

  protected void verifyNoAuthorizationCheckPerformed() {
    verify(identityServiceMock, times(0)).getCurrentAuthentication();
    verify(authorizationServiceMock, times(0)).isUserAuthorized(anyString(), anyList(), any(Permission.class), any(Resource.class));
  }

  protected String getFullAuthorizationUrl() {
    return "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + TenantRestService.PATH;
  }

  protected String getFullAuthorizationTenantUrl() {
    return getFullAuthorizationUrl() + "/" + MockProvider.EXAMPLE_TENANT_ID;
  }

}
