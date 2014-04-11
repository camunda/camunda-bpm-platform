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

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.helper.MockProvider.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public abstract class AbstractDeploymentRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String DEPLOYMENT_URL = TEST_RESOURCE_ROOT_PATH + "/deployment/{id}";
  protected static final String RESOURCES_URL = DEPLOYMENT_URL + "/resources";
  protected static final String SINGLE_RESOURCE_URL = RESOURCES_URL + "/{resourceId}";
  protected static final String SINGLE_RESOURCE_DATA_URL = SINGLE_RESOURCE_URL + "/data";
  protected static final String CREATE_DEPLOYMENT_URL = TEST_RESOURCE_ROOT_PATH + "/deployment";

  protected Deployment mockDeployment;
  protected List<Resource> mockDeploymentResources;
  protected Resource mockDeploymentResource;
  protected DeploymentQuery deploymentQueryMock;
  protected DeploymentBuilder deploymentBuilderMock;
  protected Deployment deploymentMock;
  protected Collection<String> resourceNames = new ArrayList<String>();

  @Before
  public void setUpRuntimeData() {
    RepositoryService repositoryServiceMock = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);

    mockDeployment = MockProvider.createMockDeployment();
    deploymentQueryMock = mock(DeploymentQuery.class);
    when(deploymentQueryMock.deploymentId(EXAMPLE_DEPLOYMENT_ID)).thenReturn(deploymentQueryMock);
    when(deploymentQueryMock.singleResult()).thenReturn(mockDeployment);
    when(repositoryServiceMock.createDeploymentQuery()).thenReturn(deploymentQueryMock);

    mockDeploymentResources = MockProvider.createMockDeploymentResources();
    when(repositoryServiceMock.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(mockDeploymentResources);

    mockDeploymentResource = MockProvider.createMockDeploymentResource();

    when(repositoryServiceMock.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_RESOURCE_ID))).thenReturn(createMockDeploymentResourceData());

    deploymentBuilderMock = mock(DeploymentBuilder.class);
    deploymentMock = MockProvider.createMockDeployment();
    when(repositoryServiceMock.createDeployment()).thenReturn(deploymentBuilderMock);
    when(deploymentBuilderMock.addInputStream(anyString(), any(InputStream.class))).thenReturn(deploymentBuilderMock);
    when(deploymentBuilderMock.getResourceNames()).thenReturn(resourceNames);
    when(deploymentBuilderMock.deploy()).thenReturn(deploymentMock);
  }

  private InputStream createMockDeploymentResourceData() {
    // do not close the input stream, will be done in implementation
    InputStream bpmn20XmlIn = ReflectUtil.getResourceAsStream("processes/fox-invoice_en_long_id.bpmn");
    Assert.assertNotNull(bpmn20XmlIn);
    return bpmn20XmlIn;
  }

  @Test
  public void testGetSingleDeployment() {

    Response response = given().pathParam("id", EXAMPLE_DEPLOYMENT_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(DEPLOYMENT_URL);

    verifyDeployment(mockDeployment, response);

  }

  @Test
  public void testGetNonExistingSingleDeployment() {

    when(deploymentQueryMock.deploymentId(NON_EXISTING_DEPLOYMENT_ID)).thenReturn(deploymentQueryMock);
    when(deploymentQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", NON_EXISTING_DEPLOYMENT_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
        .body(containsString("Deployment with id '" + NON_EXISTING_DEPLOYMENT_ID + "' does not exist"))
      .when().get(DEPLOYMENT_URL);

  }

  @Test
  public void testGetDeploymentResources() {

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(RESOURCES_URL);

    verifyDeploymentResources(mockDeploymentResources, response);

  }

  @Test
  public void testGetNonExistingDeploymentResources() {

    given().pathParam("id", NON_EXISTING_DEPLOYMENT_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
        .body(containsString("Deployment resources for deployment id '" + NON_EXISTING_DEPLOYMENT_ID + "' do not exist."))
      .when().get(RESOURCES_URL);

  }

  @Test
  public void testGetDeploymentResource() {

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RESOURCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(SINGLE_RESOURCE_URL);

    verifyDeploymentResource(mockDeploymentResource, response);

  }

  @Test
  public void testGetNonExistingDeploymentResource() {

    given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", NON_EXISTING_DEPLOYMENT_RESOURCE_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
        .body(containsString("Deployment resource with resource id '" + NON_EXISTING_DEPLOYMENT_RESOURCE_ID + "' for deployment id '" + EXAMPLE_DEPLOYMENT_ID + "' does not exist."))
      .when().get(SINGLE_RESOURCE_URL);

  }

  @Test
  public void testGetDeploymentResourceWithNonExistingDeploymentId() {

    given()
        .pathParam("id", NON_EXISTING_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RESOURCE_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
        .body(containsString("Deployment resources for deployment id '" + NON_EXISTING_DEPLOYMENT_ID + "' do not exist."))
      .when().get(SINGLE_RESOURCE_URL);

  }

  @Test
  public void testGetDeploymentResourceWithNonExistingDeploymentIdAndNonExistingResourceId() {

    given()
        .pathParam("id", NON_EXISTING_DEPLOYMENT_ID)
        .pathParam("resourceId", NON_EXISTING_DEPLOYMENT_RESOURCE_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
        .body(containsString("Deployment resources for deployment id '" + NON_EXISTING_DEPLOYMENT_ID + "' do not exist."))
      .when().get(SINGLE_RESOURCE_URL);

  }

  @Test
  public void testGetDeploymentResourceData() {

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RESOURCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertTrue(responseContent.contains("<?xml"));

  }

  @Test
  public void testGetDeploymentResourceDataForNonExistingDeploymentId() {

    given()
        .pathParam("id", NON_EXISTING_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RESOURCE_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
        .body(containsString("Deployment resource '" + EXAMPLE_DEPLOYMENT_RESOURCE_ID + "' for deployment id '" + NON_EXISTING_DEPLOYMENT_ID + "' does not exist."))
      .when().get(SINGLE_RESOURCE_DATA_URL);

  }

  @Test
  public void testGetDeploymentResourceDataForNonExistingResourceId() {

    given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", NON_EXISTING_DEPLOYMENT_RESOURCE_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
        .body(containsString("Deployment resource '" + NON_EXISTING_DEPLOYMENT_RESOURCE_ID + "' for deployment id '" + EXAMPLE_DEPLOYMENT_ID + "' does not exist."))
      .when().get(SINGLE_RESOURCE_DATA_URL);

  }

  @Test
  public void testGetDeploymentResourceDataForNonExistingDeploymentIdAndNonExistingResourceId() {

    given()
      .pathParam("id", NON_EXISTING_DEPLOYMENT_ID)
      .pathParam("resourceId", NON_EXISTING_DEPLOYMENT_RESOURCE_ID)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("Deployment resource '" + NON_EXISTING_DEPLOYMENT_RESOURCE_ID + "' for deployment id '" + NON_EXISTING_DEPLOYMENT_ID + "' does not exist."))
      .when().get(SINGLE_RESOURCE_DATA_URL);

  }

  @Test
  public void testCreateCompleteDeployment() throws Exception {
    byte[] bytes = "someContent".getBytes();

    resourceNames.addAll( Arrays.asList("data", "more-data") );

    Response response = given()
      .multiPart("data", "unspecified", bytes)
      .multiPart("more-data", "unspecified", createMockDeploymentResourceData())
      .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .multiPart("enable-duplicate-filtering", "true")
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    verifyDeployment(deploymentMock, response);
  }

  @Test
  public void testCreateDeploymentOnlyWithBytes() throws Exception {
    Deployment deploymentMock = mock(Deployment.class);
    when(deploymentMock.getId()).thenReturn(MockProvider.EXAMPLE_DEPLOYMENT_ID);
    when(deploymentMock.getName()).thenReturn(null);
    when(deploymentMock.getDeploymentTime()).thenReturn(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_DEPLOYMENT_TIME).toDate());
    when(deploymentBuilderMock.deploy()).thenReturn(deploymentMock);

    byte[] bytes = "someContent".getBytes();

    resourceNames.addAll(Arrays.asList("data", "more-data"));

    Response response = given()
      .multiPart("data", "unspecified", bytes)
      .multiPart("more-data", "unspecified", createMockDeploymentResourceData())
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    String content = response.asString();

    JsonPath path = from(content);
    String returnedId = path.get("id");
    String returnedName = path.get("name");
    String returnedDeploymentTime = path.get("deploymentTime");

    List<HashMap<String, String>> returnedLinks = path.getList("links");
    Assert.assertEquals(1, returnedLinks.size());

    Assert.assertEquals(deploymentMock.getId(), returnedId);
    Assert.assertEquals(null, returnedName);
    Assert.assertEquals(deploymentMock.getDeploymentTime(), DateTimeUtil.parseDateTime(returnedDeploymentTime).toDate());
  }

  @Test
  public void testCreateDeploymentWithoutBytes() throws Exception {
    Response response = given()
        .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

  }


  private void verifyDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();

    JsonPath path = from(content);
    String returnedId = path.get("id");
    String returnedName = path.get("name");
    Date returnedDeploymentTime = DateTimeUtil.parseDateTime(path.<String>get("deploymentTime")).toDate();

    Assert.assertEquals(mockDeployment.getId(), returnedId);
    Assert.assertEquals(mockDeployment.getName(), returnedName);
    Assert.assertEquals(mockDeployment.getDeploymentTime(), returnedDeploymentTime);
  }

  private void verifyDeploymentResource(Resource mockDeploymentResource, Response response) {
    String content = response.asString();

    JsonPath path = from(content);
    String returnedId = path.get("id");
    String returnedName = path.get("name");
    String returnedDeploymentId = path.get("deploymentId");

    Assert.assertEquals(mockDeploymentResource.getId(), returnedId);
    Assert.assertEquals(mockDeploymentResource.getName(), returnedName);
    Assert.assertEquals(mockDeploymentResource.getDeploymentId(), returnedDeploymentId);
  }

  @SuppressWarnings("unchecked")
  private void verifyDeploymentResources(List<Resource> mockDeploymentResources, Response response) {
    List list = response.as(List.class);
    Assert.assertEquals(1, list.size());

    LinkedHashMap<String, String> resourceHashMap = (LinkedHashMap<String, String>) list.get(0);

    String returnedId = resourceHashMap.get("id");
    String returnedName = resourceHashMap.get("name");
    String returnedDeploymentId = resourceHashMap.get("deploymentId");

    Resource mockDeploymentResource = mockDeploymentResources.get(0);

    Assert.assertEquals(mockDeploymentResource.getId(), returnedId);
    Assert.assertEquals(mockDeploymentResource.getName(), returnedName);
    Assert.assertEquals(mockDeploymentResource.getDeploymentId(), returnedDeploymentId);
  }

}
