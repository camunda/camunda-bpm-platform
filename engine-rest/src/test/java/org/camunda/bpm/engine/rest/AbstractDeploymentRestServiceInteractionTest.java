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
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_DMN_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_GIF_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_HTML_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_JPE_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_JPG_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_JSON_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_JS_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_PHP_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_PNG_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_SVG_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_TIF_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_TXT_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_DEPLOYMENT_XML_RESOURCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.NON_EXISTING_DEPLOYMENT_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.NON_EXISTING_DEPLOYMENT_RESOURCE_ID;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
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

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

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

  private InputStream createMockDeploymentResourceSvgData() {
    // do not close the input stream, will be done in implementation
    InputStream image = ReflectUtil.getResourceAsStream("processes/diagram.svg");
    assertNotNull(image);
    return image;
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
  public void testGetDeploymentResourcesThrowsAuthorizationException() {
    String message = "expected exception";
    when(mockRepositoryService.getDeploymentResources(EXAMPLE_DEPLOYMENT_ID)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
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
  public void testGetDeploymentResourceThrowsAuthorizationException() {
    String message = "expected exception";
    when(mockRepositoryService.getDeploymentResources(EXAMPLE_DEPLOYMENT_ID)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
      .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RESOURCE_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .get(SINGLE_RESOURCE_URL);
  }

  @Test
  public void testGetDeploymentResourceData() {

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("application/octet-stream")
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertTrue(responseContent.contains("<?xml"));

  }

  @Test
  public void testGetDeploymentSvgResourceData() {
    Resource resource = MockProvider.createMockDeploymentSvgResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_SVG_RESOURCE_ID))).thenReturn(createMockDeploymentResourceSvgData());

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_SVG_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/svg+xml")
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_SVG_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testGetDeploymentPngResourceData() {
    Resource resource = MockProvider.createMockDeploymentPngResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_PNG_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_PNG_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/png")
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_PNG_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentGifResourceData() {
    Resource resource = MockProvider.createMockDeploymentGifResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_GIF_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_GIF_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/gif")
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_GIF_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJpgResourceData() {
    Resource resource = MockProvider.createMockDeploymentJpgResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_JPG_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_JPG_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/jpeg")
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_JPG_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJpegResourceData() {
    Resource resource = MockProvider.createMockDeploymentJpegResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/jpeg")
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJpeResourceData() {
    Resource resource = MockProvider.createMockDeploymentJpeResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_JPE_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_JPE_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/jpeg")
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_JPE_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentTifResourceData() {
    Resource resource = MockProvider.createMockDeploymentTifResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_TIF_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_TIF_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/tiff")
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_TIF_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentTiffResourceData() {
    Resource resource = MockProvider.createMockDeploymentTiffResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("image/tiff")
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentBpmnResourceData() {
    Resource resource = MockProvider.createMockDeploymentBpmnResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_ID))).thenReturn(createMockDeploymentResourceBpmnData());

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.XML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentBpmnXmlResourceData() {
    Resource resource = MockProvider.createMockDeploymentBpmnXmlResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_ID))).thenReturn(createMockDeploymentResourceBpmnData());

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.XML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentCmmnResourceData() {
    Resource resource = MockProvider.createMockDeploymentCmmnResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.XML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentCmmnXmlResourceData() {
    Resource resource = MockProvider.createMockDeploymentCmmnXmlResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.XML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentDmnResourceData() {
    Resource resource = MockProvider.createMockDeploymentDmnResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_DMN_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_DMN_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.XML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_DMN_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentDmnXmlResourceData() {
    Resource resource = MockProvider.createMockDeploymentDmnXmlResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.XML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentXmlResourceData() {
    Resource resource = MockProvider.createMockDeploymentXmlResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_XML_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_XML_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.XML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_XML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJsonResourceData() {
    Resource resource = MockProvider.createMockDeploymentJsonResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_JSON_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_JSON_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.JSON)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_JSON_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentGroovyResourceData() {
    Resource resource = MockProvider.createMockDeploymentGroovyResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.TEXT)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJavaResourceData() {
    Resource resource = MockProvider.createMockDeploymentJavaResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.TEXT)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJsResourceData() {
    Resource resource = MockProvider.createMockDeploymentJsResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_JS_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_JS_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.TEXT)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_JS_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentPythonResourceData() {
    Resource resource = MockProvider.createMockDeploymentPythonResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.TEXT)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentRubyResourceData() {
    Resource resource = MockProvider.createMockDeploymentRubyResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.TEXT)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentPhpResourceData() {
    Resource resource = MockProvider.createMockDeploymentPhpResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_PHP_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_PHP_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.TEXT)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_PHP_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentHtmlpResourceData() {
    Resource resource = MockProvider.createMockDeploymentHtmlResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_HTML_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_HTML_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.HTML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_HTML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentTxtResourceData() {
    Resource resource = MockProvider.createMockDeploymentTxtResource();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_TXT_RESOURCE_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_TXT_RESOURCE_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.TEXT)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_TXT_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentResourceDataFilename() {
    Resource resource = MockProvider.createMockDeploymentResourceFilename();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.XML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentResourceDataFilenameBackslash() {
    Resource resource = MockProvider.createMockDeploymentResourceFilenameBackslash();

    List<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_ID))).thenReturn(input);

    Response response = given()
        .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
        .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.XML)
          .header("Content-Disposition", "attachment; filename=" + MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
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
  public void testGetDeploymentResourceDataThrowsAuthorizationException() {
    String message = "expected exception";
    when(mockRepositoryService.getResourceAsStreamById(EXAMPLE_DEPLOYMENT_ID, EXAMPLE_DEPLOYMENT_RESOURCE_ID)).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
      .pathParam("resourceId", EXAMPLE_DEPLOYMENT_RESOURCE_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .get(SINGLE_RESOURCE_DATA_URL);
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
  public void testCreateDeploymentWithDeploymentSource() throws Exception {

    resourceNames.addAll( Arrays.asList("data", "more-data") );

    // deploy-changed-only should override enable-duplicate-filtering
    given()
      .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
      .multiPart("enable-duplicate-filtering", "false")
      .multiPart("deployment-source", "my-deployment-source")
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).source("my-deployment-source");

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
  public void testCreateDeploymentThrowsAuthorizationException() {
    String message = "expected exception";
    when(mockDeploymentBuilder.deploy()).thenThrow(new AuthorizationException(message));

    resourceNames.addAll( Arrays.asList("data", "more-data") );

    given()
      .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
      .multiPart("more-data", "unspecified", createMockDeploymentResourceBpmnData())
      .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .multiPart("enable-duplicate-filtering", "true")
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, true, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false);
  }

  @Test
  public void testDeleteDeploymentSkipCustomListeners() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .queryParam("skipCustomListeners", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, true);
  }

  @Test
  public void testDeleteDeploymentSkipCustomListenersNonsense() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .queryParam("skipCustomListeners", "bla")
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false);
  }

  @Test
  public void testDeleteDeploymentSkipCustomListenersFalse() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .queryParam("skipCustomListeners", false)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false);
  }

  @Test
  public void testDeleteDeploymentSkipCustomListenersAndCascade() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .queryParam("cascade", true)
      .queryParam("skipCustomListeners", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, true, true);
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

  @Test
  public void testDeleteDeploymentThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
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
