package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public abstract class AbstractDeploymentRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String DEPLOYMENT_URL = TEST_RESOURCE_ROOT_PATH + "/deployments";
  protected static final String SINGLE_DEPLOYMENT_URL = DEPLOYMENT_URL + "/{id}";
  protected static final String DEPLOYMENT_RESOURCE_URL = SINGLE_DEPLOYMENT_URL + "/{resourceId}";
  protected static final String DEPLOYMENT_RESOURCE_DATA_URL = DEPLOYMENT_RESOURCE_URL + "/data";

  protected List<Resource> mockDeploymentResources;
  private RepositoryService repositoryServiceMock;

  @Before
  public void setUpRuntimeData() {
     List<Resource> mockDeploymentResources = MockProvider.createMockDeploymentResources();

     repositoryServiceMock = mock(RepositoryService.class);
     when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);
     when(repositoryServiceMock.getDeploymentResources(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID))).thenReturn(mockDeploymentResources);
     when(repositoryServiceMock.getResourceAsStreamById(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID), eq(MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_ID))).thenReturn(createMockDeploymentResource());
  }

  private InputStream createMockDeploymentResource() {
    // do not close the input stream, will be done in implementation
    InputStream bpmn20XmlIn = null;
    bpmn20XmlIn = ReflectUtil.getResourceAsStream("processes/fox-invoice_en_long_id.bpmn");
    Assert.assertNotNull(bpmn20XmlIn);
    return bpmn20XmlIn;
  }

  @Test
  public void testGetSingleDeployment() {

    Resource mockDeploymentResource = MockProvider.createMockDeploymentResource();

    Response response = given().pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(SINGLE_DEPLOYMENT_URL);

    verifyResponseList(mockDeploymentResource, response);

  }

  @Test
  public void testGetNonExistingSingleDeployment() {

    String nonExistingDeploymentId = "nonExistingId";

    given().pathParam("id", nonExistingDeploymentId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Deployment resources for deployment Id 'nonExistingId' do not exist."))
    .when().get(SINGLE_DEPLOYMENT_URL);

  }

  @Test
  public void testGetSingleDeploymentWithoutParameter() {
    given()
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Deployment resources for deployment Id '{id}' do not exist."))
    .when().get(SINGLE_DEPLOYMENT_URL);
  }

  @Test
  public void testGetDeploymentResource() {
    Resource mockDeploymentResource = MockProvider.createMockDeploymentResource();

    Response response = given()
        .pathParam("resourceId", MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_ID)
        .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(DEPLOYMENT_RESOURCE_URL);

    verifyResponse(mockDeploymentResource, response);
  }

  @Test
  public void testGetNonExistingDeploymentResource() {

    String nonExistingResourceId = "nonExistingId";

    given()
        .pathParam("resourceId", nonExistingResourceId)
        .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Deployment resource with resource id 'nonExistingId' does not exist in deployment with deployment id 'aDeploymentId'."))
    .when().get(DEPLOYMENT_RESOURCE_URL);
  }

  @Test
  public void testGetDeploymentResourceWithNonExistingDeploymentId() {

    String nonExistingDeploymentId = "nonExistingId";

    given()
        .pathParam("resourceId", MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_ID)
        .pathParam("id", nonExistingDeploymentId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Deployment resources for deployment Id 'nonExistingId' do not exist."))
    .when().get(DEPLOYMENT_RESOURCE_URL);
  }

  @Test
  public void testGetDeploymentResourceWithoutParameters() {

    given()
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("Deployment resources for deployment Id '{id}' do not exist."))
    .when().get(DEPLOYMENT_RESOURCE_URL);
  }

  @Test
  public void testGetDeploymentResourceData() {
    Response response = given()
        .pathParam("resourceId", MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_ID)
        .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(DEPLOYMENT_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    Assert.assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testGetDeploymentResourceDataForNonExistingResourceId() {

    String nonExistingResourceId = "nonExistingId";

    given()
        .pathParam("resourceId", nonExistingResourceId)
        .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("No deployment resource stream exists for resource id 'nonExistingId' in deployment with deployment id 'aDeploymentId'."))
    .when().get(DEPLOYMENT_RESOURCE_DATA_URL);
  }

  @Test
  public void testGetDeploymentResourceDataForNonExistingDeploymentId() {

    String nonExistingDeploymentId = "nonExistingId";

    given()
        .pathParam("resourceId", MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_ID)
        .pathParam("id", nonExistingDeploymentId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("No deployment resource stream exists for resource id 'aDeploymentResourceId' in deployment with deployment id 'nonExistingId'."))
    .when().get(DEPLOYMENT_RESOURCE_DATA_URL);
  }

  @SuppressWarnings("unchecked")
  private void verifyResponseList(Resource mockDeploymentResource, Response response) {
    List list = response.as(List.class);
    Assert.assertEquals(1, list.size());

    LinkedHashMap<String, String> resourceHashMap = (LinkedHashMap<String, String>) list.get(0);

    String returnedId = (String )resourceHashMap.get("id");
    String returnedName = (String) resourceHashMap.get("name");
    String returnedDeploymentId = (String) resourceHashMap.get("deploymentId");

    Assert.assertEquals(mockDeploymentResource.getId(), returnedId);
    Assert.assertEquals(mockDeploymentResource.getName(), returnedName);
    Assert.assertEquals(mockDeploymentResource.getDeploymentId(), returnedDeploymentId);
  }

  private void verifyResponse(Resource mockDeploymentResource, Response response) {
    String content = response.asString();

    JsonPath path = from(content);
    String returnedId = path.get("id");
    String returnedName = path.get("name");
    String returnedDeploymentId = path.get("deploymentId");

    Assert.assertEquals(mockDeploymentResource.getId(), returnedId);
    Assert.assertEquals(mockDeploymentResource.getName(), returnedName);
    Assert.assertEquals(mockDeploymentResource.getDeploymentId(), returnedDeploymentId);
  }

}
