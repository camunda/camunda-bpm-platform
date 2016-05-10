package org.camunda.bpm.engine.rest.standalone;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.AuthorizationServiceImpl;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.ProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.impl.NamedProcessEngineRestServiceImpl;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

public abstract class AbstractAuthenticationFilterTest extends AbstractRestServiceTest {

  protected static final String SERVLET_PATH = "/rest";
  protected static final String SERVICE_PATH = TEST_RESOURCE_ROOT_PATH + SERVLET_PATH + NamedProcessEngineRestServiceImpl.PATH + "/{name}"+ ProcessDefinitionRestService.PATH;

  protected AuthorizationService authorizationServiceMock;
  protected IdentityService identityServiceMock;
  protected RepositoryService repositoryServiceMock;

  protected User userMock;
  protected List<String> groupIds;
  protected List<String> tenantIds;

  @Before
  public void setup() {
    authorizationServiceMock = mock(AuthorizationServiceImpl.class);
    identityServiceMock = mock(IdentityServiceImpl.class);
    repositoryServiceMock = mock(RepositoryService.class);

    when(processEngine.getAuthorizationService()).thenReturn(authorizationServiceMock);
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);

    // for authentication
    userMock = MockProvider.createMockUser();

    List<Group> groupMocks = MockProvider.createMockGroups();
    groupIds = setupGroupQueryMock(groupMocks);

    List<Tenant> tenantMocks = Collections.singletonList(MockProvider.createMockTenant());
    tenantIds = setupTenantQueryMock(tenantMocks);

    // example method
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    List<ProcessDefinition> mockDefinitions = Arrays.asList(mockDefinition);
    ProcessDefinitionQuery mockQuery = mock(ProcessDefinitionQuery.class);
    when(repositoryServiceMock.createProcessDefinitionQuery()).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(mockDefinitions);
  }

  protected List<String> setupGroupQueryMock(List<Group> groups) {
    GroupQuery mockGroupQuery = mock(GroupQuery.class);

    when(identityServiceMock.createGroupQuery()).thenReturn(mockGroupQuery);
    when(mockGroupQuery.groupMember(anyString())).thenReturn(mockGroupQuery);
    when(mockGroupQuery.list()).thenReturn(groups);

    List<String> groupIds = new ArrayList<String>();
    for (Group groupMock : groups) {
      groupIds.add(groupMock.getId());
    }
    return groupIds;
  }

  protected List<String> setupTenantQueryMock(List<Tenant> tenants) {
    TenantQuery mockTenantQuery = mock(TenantQuery.class);

    when(identityServiceMock.createTenantQuery()).thenReturn(mockTenantQuery);
    when(mockTenantQuery.userMember(anyString())).thenReturn(mockTenantQuery);
    when(mockTenantQuery.includingGroupsOfUser(anyBoolean())).thenReturn(mockTenantQuery);
    when(mockTenantQuery.list()).thenReturn(tenants);

    List<String> tenantIds = new ArrayList<String>();
    for(Tenant tenant: tenants) {
      tenantIds.add(tenant.getId());
    }
    return tenantIds;
  }

  @Test
  public void testHttpBasicAuthenticationCheck() {
    when(identityServiceMock.checkPassword(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD)).thenReturn(true);

    given()
      .auth().basic(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD)
      .pathParam("name", "default")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(MediaType.APPLICATION_JSON)
    .when().get(SERVICE_PATH);

    verify(identityServiceMock).setAuthentication(MockProvider.EXAMPLE_USER_ID, groupIds, tenantIds);
  }

  @Test
  public void testFailingAuthenticationCheck() {
    when(identityServiceMock.checkPassword(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD)).thenReturn(false);

    given()
      .auth().basic(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD)
      .pathParam("name", "default")
    .then().expect()
      .statusCode(Status.UNAUTHORIZED.getStatusCode())
      .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"default\"")
    .when().get(SERVICE_PATH);
  }

  @Test
  public void testMissingAuthHeader() {
    given()
      .pathParam("name", "someengine")
    .then().expect()
      .statusCode(Status.UNAUTHORIZED.getStatusCode())
      .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"someengine\"")
    .when().get(SERVICE_PATH);
  }

  @Test
  public void testUnexpectedAuthHeaderFormat() {
    given()
      .header(HttpHeaders.AUTHORIZATION, "Digest somevalues, and, some, more")
      .pathParam("name", "someengine")
    .then().expect()
      .statusCode(Status.UNAUTHORIZED.getStatusCode())
      .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"someengine\"")
    .when().get(SERVICE_PATH);
  }

  @Test
  public void testMalformedCredentials() {
    given()
      .header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encodeBase64("this is not a valid format".getBytes())))
      .pathParam("name", "default")
    .then().expect()
      .statusCode(Status.UNAUTHORIZED.getStatusCode())
      .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"default\"")
    .when().get(SERVICE_PATH);
  }

  @Test
  public void testNonExistingEngineAuthentication() {
    given()
      .auth().basic(MockProvider.EXAMPLE_USER_ID, MockProvider.EXAMPLE_USER_PASSWORD)
      .pathParam("name", MockProvider.NON_EXISTING_PROCESS_ENGINE_NAME)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Process engine " + MockProvider.NON_EXISTING_PROCESS_ENGINE_NAME + " not available"))
    .when().get(SERVICE_PATH);
  }

  @Test
  public void testMalformedBase64Value() {
    given()
      .header(HttpHeaders.AUTHORIZATION, "Basic someNonBase64Characters!(#")
      .pathParam("name", "default")
    .then().expect()
      .statusCode(Status.UNAUTHORIZED.getStatusCode())
      .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"default\"")
    .when().get(SERVICE_PATH);
  }

}
