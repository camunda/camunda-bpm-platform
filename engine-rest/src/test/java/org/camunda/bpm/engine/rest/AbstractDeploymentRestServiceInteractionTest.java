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
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import java.io.InputStream;
import java.util.*;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.helper.MockProvider.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public abstract class AbstractDeploymentRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String RESEOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/deployment";
  protected static final String DEPLOYMENT_URL = RESEOURCE_URL + "/{id}";
  protected static final String RESOURCES_URL = DEPLOYMENT_URL + "/resources";
  protected static final String SINGLE_RESOURCE_URL = RESOURCES_URL + "/{resourceId}";
  protected static final String SINGLE_RESOURCE_DATA_URL = SINGLE_RESOURCE_URL + "/data";
  protected static final String CREATE_DEPLOYMENT_URL = TEST_RESOURCE_ROOT_PATH + "/deployment/create";

  protected RepositoryService mockRepositoryService;
  protected Deployment mockDeployment;
  protected List<Resource> mockDeploymentResources;
  protected Resource mockDeploymentResource;
  protected DeploymentQuery mockDeploymentQuery;
  protected DeploymentBuilder mockDeploymentBuilder;
  protected Collection<String> resourceNames = new ArrayList<String>();

  @Before
  public void setUpRuntimeData() {
    mockRepositoryService = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(mockRepositoryService);

    mockDeployment = MockProvider.createMockDeployment();
    mockDeploymentQuery = mock(DeploymentQuery.class);
    when(mockDeploymentQuery.deploymentId(EXAMPLE_DEPLOYMENT_ID)).thenReturn(mockDeploymentQuery);
    when(mockDeploymentQuery.singleResult()).thenReturn(mockDeployment);
    when(mockRepositoryService.createDeploymentQuery()).thenReturn(mockDeploymentQuery);

    mockDeploymentResources = MockProvider.createMockDeploymentResources();
    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(mockDeploymentResources);

    mockDeploymentResource = MockProvider.createMockDeploymentResource();

    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_RESOURCE_ID))).thenReturn(createMockDeploymentResourceBpmnData());

    mockDeploymentBuilder = mock(DeploymentBuilder.class);
    when(mockRepositoryService.createDeployment()).thenReturn(mockDeploymentBuilder);
    when(mockDeploymentBuilder.addInputStream(anyString(), any(InputStream.class))).thenReturn(mockDeploymentBuilder);
    when(mockDeploymentBuilder.getResourceNames()).thenReturn(resourceNames);
    when(mockDeploymentBuilder.deploy()).thenReturn(mockDeployment);
  }

  private byte[] createMockDeploymentResourceByteData() {
    return "someContent".getBytes();
  }

  private InputStream createMockDeploymentResourceBpmnData() {
    // do not close the input stream, will be done in implementation
    InputStream bpmn20XmlIn = ReflectUtil.getResourceAsStream("processes/fox-invoice_en_long_id.bpmn");
    assertNotNull(bpmn20XmlIn);
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

    when(mockDeploymentQuery.deploymentId(NON_EXISTING_DEPLOYMENT_ID)).thenReturn(mockDeploymentQuery);
    when(mockDeploymentQuery.singleResult()).thenReturn(null);

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

    resourceNames.addAll( Arrays.asList("data", "more-data") );

    Response response = given()
      .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
      .multiPart("more-data", "unspecified", createMockDeploymentResourceBpmnData())
      .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .multiPart("enable-duplicate-filtering", "true")
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    verifyCreatedDeployment(mockDeployment, response);

    verify(mockDeploymentBuilder).name(MockProvider.EXAMPLE_DEPLOYMENT_ID);
    verify(mockDeploymentBuilder).enableDuplicateFiltering(false);

  }

  @Test
  public void testCreateCompleteDeploymentDeployChangedOnly() throws Exception {

    resourceNames.addAll( Arrays.asList("data", "more-data") );

    given()
      .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
      .multiPart("deploy-changed-only", "true")
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).enableDuplicateFiltering(true);

  }

  @Test
  public void testCreateCompleteDeploymentConflictingDuplicateSetting() throws Exception {

    resourceNames.addAll( Arrays.asList("data", "more-data") );

    // deploy-changed-only should override enable-duplicate-filtering
    given()
      .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
      .multiPart("enable-duplicate-filtering", "false")
      .multiPart("deploy-changed-only", "true")
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).enableDuplicateFiltering(true);

  }

  @Test
  public void testCreateDeploymentOnlyWithBytes() throws Exception {

    resourceNames.addAll(Arrays.asList("data", "more-data"));

    Response response = given()
      .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
      .multiPart("more-data", "unspecified", createMockDeploymentResourceBpmnData())
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    verifyCreatedDeployment(mockDeployment, response);

  }

  @Test
  public void testCreateDeploymentWithoutBytes() throws Exception {

    given()
      .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

  }
  
  @Test
  public void testDeleteDeployment() {
    
    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);
    
    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false);
  }
  
  @Test
  public void testDeleteDeploymentCascade() {
    
    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .queryParam("cascade", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);
    
    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, true);
  }
  
  @Test
  public void testDeleteDeploymentCascadeNonsense() {
    
    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .queryParam("cascade", "bla")
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);
    
    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false);
  }

  @Test
  public void testDeleteDeploymentCascadeFalse() {
    
    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .queryParam("cascade", false)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);
    
    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false);
  }
  
  @Test
  public void testDeleteNonExistingDeployment() {

    when(mockDeploymentQuery.deploymentId(NON_EXISTING_DEPLOYMENT_ID)).thenReturn(mockDeploymentQuery);
    when(mockDeploymentQuery.singleResult()).thenReturn(null);
    
    given()
      .pathParam("id", NON_EXISTING_DEPLOYMENT_ID)
    .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("Deployment with id '" + NON_EXISTING_DEPLOYMENT_ID + "' do not exist"))
    .when()
       .delete(DEPLOYMENT_URL);
  }

  private void verifyDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();
    verifyDeploymentValues(mockDeployment, content);
  }

  private void verifyCreatedDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();
    verifyDeploymentValues(mockDeployment, content);
    verifyDeploymentLink(mockDeployment, content);
  }

  private void verifyDeploymentValues(Deployment mockDeployment, String responseContent) {
    JsonPath path = from(responseContent);
    String returnedId = path.get("id");
    String returnedName = path.get("name");
    Date returnedDeploymentTime = DateTimeUtil.parseDate(path.<String>get("deploymentTime"));

    assertEquals(mockDeployment.getId(), returnedId);
    assertEquals(mockDeployment.getName(), returnedName);
    assertEquals(mockDeployment.getDeploymentTime(), returnedDeploymentTime);
  }

  private void verifyDeploymentLink(Deployment mockDeployment, String responseContent) {
    List<Map<String, String>> returnedLinks = from(responseContent).getList("links");
    assertEquals(1, returnedLinks.size());

    Map<String, String> returnedLink = returnedLinks.get(0);
    assertEquals(HttpMethod.GET, returnedLink.get("method"));
    assertTrue(returnedLink.get("href").endsWith(RESEOURCE_URL + "/" + mockDeployment.getId()));
    assertEquals("self", returnedLink.get("rel"));
  }

  private void verifyDeploymentResource(Resource mockDeploymentResource, Response response) {
    String content = response.asString();

    JsonPath path = from(content);
    String returnedId = path.get("id");
    String returnedName = path.get("name");
    String returnedDeploymentId = path.get("deploymentId");

    assertEquals(mockDeploymentResource.getId(), returnedId);
    assertEquals(mockDeploymentResource.getName(), returnedName);
    assertEquals(mockDeploymentResource.getDeploymentId(), returnedDeploymentId);
  }

  @SuppressWarnings("unchecked")
  private void verifyDeploymentResources(List<Resource> mockDeploymentResources, Response response) {
    List list = response.as(List.class);
    assertEquals(1, list.size());

    LinkedHashMap<String, String> resourceHashMap = (LinkedHashMap<String, String>) list.get(0);

    String returnedId = resourceHashMap.get("id");
    String returnedName = resourceHashMap.get("name");
    String returnedDeploymentId = resourceHashMap.get("deploymentId");

    Resource mockDeploymentResource = mockDeploymentResources.get(0);

    assertEquals(mockDeploymentResource.getId(), returnedId);
    assertEquals(mockDeploymentResource.getName(), returnedName);
    assertEquals(mockDeploymentResource.getDeploymentId(), returnedDeploymentId);
  }

}
