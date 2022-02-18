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

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.Problem;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.ResourceReport;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.bpmn.parser.ResourceReportImpl;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.*;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.helper.MockProvider.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

public class DeploymentRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String PROPERTY_DEPLOYED_PROCESS_DEFINITIONS = "deployedProcessDefinitions";
  protected static final String PROPERTY_DEPLOYED_CASE_DEFINITIONS = "deployedCaseDefinitions";
  protected static final String PROPERTY_DEPLOYED_DECISION_DEFINITIONS = "deployedDecisionDefinitions";
  protected static final String PROPERTY_DEPLOYED_DECISION_REQUIREMENTS_DEFINITIONS = "deployedDecisionRequirementsDefinitions";
  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/deployment";
  protected static final String DEPLOYMENT_URL = RESOURCE_URL + "/{id}";
  protected static final String RESOURCES_URL = DEPLOYMENT_URL + "/resources";
  protected static final String SINGLE_RESOURCE_URL = RESOURCES_URL + "/{resourceId}";
  protected static final String SINGLE_RESOURCE_DATA_URL = SINGLE_RESOURCE_URL + "/data";
  protected static final String CREATE_DEPLOYMENT_URL = RESOURCE_URL + "/create";
  protected static final String REDEPLOY_DEPLOYMENT_URL = DEPLOYMENT_URL + "/redeploy";

  protected RepositoryService mockRepositoryService;
  protected Deployment mockDeployment;
  protected DeploymentWithDefinitions mockDeploymentWithDefinitions;
  protected List<Resource> mockDeploymentResources;
  protected Resource mockDeploymentResource;
  protected DeploymentQuery mockDeploymentQuery;
  protected DeploymentBuilder mockDeploymentBuilder;
  protected Collection<String> resourceNames = new ArrayList<>();

  @Before
  public void setUpRuntimeData() {
    mockRepositoryService = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(mockRepositoryService);

    mockDeployment = MockProvider.createMockDeployment();
    mockDeploymentWithDefinitions = MockProvider.createMockDeploymentWithDefinitions();
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
    when(mockDeploymentBuilder.addDeploymentResourcesById(anyString(), anyList())).thenReturn(mockDeploymentBuilder);
    when(mockDeploymentBuilder.addDeploymentResourcesByName(anyString(), anyList())).thenReturn(mockDeploymentBuilder);
    when(mockDeploymentBuilder.source(anyString())).thenReturn(mockDeploymentBuilder);
    when(mockDeploymentBuilder.tenantId(anyString())).thenReturn(mockDeploymentBuilder);
    when(mockDeploymentBuilder.getResourceNames()).thenReturn(resourceNames);
    when(mockDeploymentBuilder.deployWithResult()).thenReturn(mockDeploymentWithDefinitions);
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

  private InputStream createMockDeploymentResourceBpmnDataNonExecutableProcess() {
    // do not close the input stream, will be done in implementation
    String model = Bpmn.convertToString(Bpmn.createProcess().startEvent().endEvent().done());
    InputStream inputStream = new ByteArrayInputStream(model.getBytes());
    return inputStream;
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertTrue(responseContent.contains("<?xml"));

  }

  @Test
  public void testGetDeploymentSvgResourceData() {
    Resource resource = MockProvider.createMockDeploymentSvgResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_SVG_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_SVG_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertTrue(responseContent.contains("<?xml"));
  }

  @Test
  public void testGetDeploymentPngResourceData() {
    Resource resource = MockProvider.createMockDeploymentPngResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_PNG_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_PNG_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentGifResourceData() {
    Resource resource = MockProvider.createMockDeploymentGifResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_GIF_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_GIF_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJpgResourceData() {
    Resource resource = MockProvider.createMockDeploymentJpgResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_JPG_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_JPG_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJpegResourceData() {
    Resource resource = MockProvider.createMockDeploymentJpegResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_JPEG_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJpeResourceData() {
    Resource resource = MockProvider.createMockDeploymentJpeResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_JPE_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_JPE_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentTifResourceData() {
    Resource resource = MockProvider.createMockDeploymentTifResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_TIF_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_TIF_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentTiffResourceData() {
    Resource resource = MockProvider.createMockDeploymentTiffResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_TIFF_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentBpmnResourceData() {
    Resource resource = MockProvider.createMockDeploymentBpmnResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_BPMN_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentBpmnXmlResourceData() {
    Resource resource = MockProvider.createMockDeploymentBpmnXmlResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_BPMN_XML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentCmmnResourceData() {
    Resource resource = MockProvider.createMockDeploymentCmmnResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_CMMN_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentCmmnXmlResourceData() {
    Resource resource = MockProvider.createMockDeploymentCmmnXmlResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_CMMN_XML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentDmnResourceData() {
    Resource resource = MockProvider.createMockDeploymentDmnResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_DMN_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_DMN_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentDmnXmlResourceData() {
    Resource resource = MockProvider.createMockDeploymentDmnXmlResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_DMN_XML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentXmlResourceData() {
    Resource resource = MockProvider.createMockDeploymentXmlResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_XML_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_XML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJsonResourceData() {
    Resource resource = MockProvider.createMockDeploymentJsonResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_JSON_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_JSON_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentGroovyResourceData() {
    Resource resource = MockProvider.createMockDeploymentGroovyResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_GROOVY_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJavaResourceData() {
    Resource resource = MockProvider.createMockDeploymentJavaResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_JAVA_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentJsResourceData() {
    Resource resource = MockProvider.createMockDeploymentJsResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_JS_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_JS_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentPythonResourceData() {
    Resource resource = MockProvider.createMockDeploymentPythonResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_PYTHON_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentRubyResourceData() {
    Resource resource = MockProvider.createMockDeploymentRubyResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_RUBY_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentPhpResourceData() {
    Resource resource = MockProvider.createMockDeploymentPhpResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_PHP_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_PHP_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentHtmlpResourceData() {
    Resource resource = MockProvider.createMockDeploymentHtmlResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_HTML_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_HTML_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentTxtResourceData() {
    Resource resource = MockProvider.createMockDeploymentTxtResource();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_TXT_RESOURCE_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_TXT_RESOURCE_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentCamundaFormResourceData() {
    Resource resource = MockProvider.createMockDeploymentCamundaFormResource();

    List<Resource> resources = new ArrayList<>();
    resources.add(resource);

    InputStream input = new ByteArrayInputStream(createMockDeploymentResourceByteData());

    when(mockRepositoryService.getDeploymentResources(eq(EXAMPLE_DEPLOYMENT_ID))).thenReturn(resources);
    when(mockRepositoryService.getResourceAsStreamById(eq(EXAMPLE_DEPLOYMENT_ID), eq(EXAMPLE_DEPLOYMENT_CAMFORM_RESOURCE_ID))).thenReturn(input);

    Response response = given()
          .pathParam("id", EXAMPLE_DEPLOYMENT_ID)
          .pathParam("resourceId", EXAMPLE_DEPLOYMENT_CAMFORM_RESOURCE_ID)
        .then()
          .expect()
            .statusCode(Status.OK.getStatusCode())
            .contentType("application/octet-stream")
            .header("Content-Disposition", "attachment; " +
                    "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_CAMFORM_RESOURCE_NAME + "\"; " +
                    "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_CAMFORM_RESOURCE_NAME)
        .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentResourceDataFilename() {
    Resource resource = MockProvider.createMockDeploymentResourceFilename();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_NAME)
      .when().get(SINGLE_RESOURCE_DATA_URL);

    String responseContent = response.asString();
    assertNotNull(responseContent);
  }

  @Test
  public void testGetDeploymentResourceDataFilenameBackslash() {
    Resource resource = MockProvider.createMockDeploymentResourceFilenameBackslash();

    List<Resource> resources = new ArrayList<>();
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
          .header("Content-Disposition", "attachment; " +
                  "filename=\"" + MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_NAME + "\"; " +
                  "filename*=UTF-8''" + MockProvider.EXAMPLE_DEPLOYMENT_RESOURCE_FILENAME_NAME)
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
  public void testCreateCompleteBpmnDeployment() throws Exception {
    // given
    DeploymentWithDefinitions mockDeployment = MockProvider.createMockDeploymentWithDefinitions();
    when(mockDeployment.getDeployedDecisionDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedCaseDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedDecisionRequirementsDefinitions()).thenReturn(null);
    when(mockDeploymentBuilder.deployWithResult()).thenReturn(mockDeployment);

    // when
    resourceNames.addAll(Arrays.asList("data", "more-data"));

    Response response = given()
      .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
      .multiPart("more-data", "unspecified", createMockDeploymentResourceBpmnData())
      .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .multiPart("enable-duplicate-filtering", "true")
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    // then
    verifyCreatedBpmnDeployment(mockDeployment, response);

    verify(mockDeploymentBuilder).name(MockProvider.EXAMPLE_DEPLOYMENT_ID);
    verify(mockDeploymentBuilder).enableDuplicateFiltering(false);

  }

  @Test
  public void testCreateCompleteCmmnDeployment() throws Exception {
    // given
    DeploymentWithDefinitions mockDeployment = MockProvider.createMockDeploymentWithDefinitions();
    when(mockDeployment.getDeployedDecisionDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedProcessDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedDecisionRequirementsDefinitions()).thenReturn(null);
    when(mockDeploymentBuilder.deployWithResult()).thenReturn(mockDeployment);

    // when
    resourceNames.addAll(Arrays.asList("data", "more-data"));

    Response response = given()
        .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
        .multiPart("more-data", "unspecified", createMockDeploymentResourceBpmnData())
        .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
        .multiPart("enable-duplicate-filtering", "true")
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CREATE_DEPLOYMENT_URL);

    // then
    verifyCreatedCmmnDeployment(mockDeployment, response);

    verify(mockDeploymentBuilder).name(MockProvider.EXAMPLE_DEPLOYMENT_ID);
    verify(mockDeploymentBuilder).enableDuplicateFiltering(false);

  }

  @Test
  public void testCreateCompleteDmnDeployment() throws Exception {
    // given
    DeploymentWithDefinitions mockDeployment = MockProvider.createMockDeploymentWithDefinitions();
    when(mockDeployment.getDeployedCaseDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedProcessDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedDecisionRequirementsDefinitions()).thenReturn(null);
    when(mockDeploymentBuilder.deployWithResult()).thenReturn(mockDeployment);

    // when
    resourceNames.addAll(Arrays.asList("data", "more-data"));

    Response response = given()
        .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
        .multiPart("more-data", "unspecified", createMockDeploymentResourceBpmnData())
        .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
        .multiPart("enable-duplicate-filtering", "true")
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CREATE_DEPLOYMENT_URL);

    // then
    verifyCreatedDmnDeployment(mockDeployment, response);

    verify(mockDeploymentBuilder).name(MockProvider.EXAMPLE_DEPLOYMENT_ID);
    verify(mockDeploymentBuilder).enableDuplicateFiltering(false);

  }

  @Test
  public void testCreateCompleteDrdDeployment() throws Exception {
    // given
    DeploymentWithDefinitions mockDeployment = MockProvider.createMockDeploymentWithDefinitions();
    when(mockDeployment.getDeployedCaseDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedProcessDefinitions()).thenReturn(null);
    when(mockDeploymentBuilder.deployWithResult()).thenReturn(mockDeployment);

    // when
    resourceNames.addAll(Arrays.asList("data", "more-data"));

    Response response = given()
        .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
        .multiPart("more-data", "unspecified", createMockDeploymentResourceBpmnData())
        .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
        .multiPart("enable-duplicate-filtering", "true")
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CREATE_DEPLOYMENT_URL);

    // then
    verifyCreatedDrdDeployment(mockDeployment, response);

    verify(mockDeploymentBuilder).name(MockProvider.EXAMPLE_DEPLOYMENT_ID);
    verify(mockDeploymentBuilder).enableDuplicateFiltering(false);

  }

  @Test
  public void testCreateDeploymentWithNonExecutableProcess() throws Exception {

    // given
    DeploymentWithDefinitions mockDeployment = MockProvider.createMockDeploymentWithDefinitions();
    when(mockDeployment.getDeployedDecisionDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedCaseDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedProcessDefinitions()).thenReturn(null);
    when(mockDeployment.getDeployedDecisionRequirementsDefinitions()).thenReturn(null);
    when(mockDeploymentBuilder.deployWithResult()).thenReturn(mockDeployment);

    // when
    resourceNames.addAll(Arrays.asList("data", "more-data"));

    Response response = given()
        .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
        .multiPart("more-data", "unspecified", createMockDeploymentResourceBpmnDataNonExecutableProcess())
        .multiPart("deployment-name", MockProvider.EXAMPLE_DEPLOYMENT_ID)
        .multiPart("enable-duplicate-filtering", "true")
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CREATE_DEPLOYMENT_URL);

    // then
    verifyCreatedEmptyDeployment(mockDeployment, response);

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
  public void testCreateDeploymentWithActivationTime() throws Exception {

    resourceNames.addAll( Arrays.asList("data", "more-data") );

    given()
        .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
        .multiPart("deployment-activation-time", "2030-11-11T11:11:11Z")
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .when()
        .post(CREATE_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).activateProcessDefinitionsOn(DateTimeUtil.parseDate("2030-11-11T11:11:11Z"));

  }

  @Test
  public void testCreateDeploymentWithTenantId() throws Exception {

    resourceNames.addAll( Arrays.asList("data", "more-data") );

    given()
      .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
      .multiPart("tenant-id", EXAMPLE_TENANT_ID)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).tenantId(EXAMPLE_TENANT_ID);
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
  public void testCreateDeploymentWithNonExistentPart() throws Exception {

    given()
    .multiPart("non-existent-body-part", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .expect()
    .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
    .post(CREATE_DEPLOYMENT_URL);

  }

  @Test
  public void testCreateDeploymentThrowsAuthorizationException() {
    String message = "expected exception";
    when(mockDeploymentBuilder.deployWithResult()).thenThrow(new AuthorizationException(message));

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
  @SuppressWarnings("unchecked")
  public void testCreateDeploymentThrowsParseException() {
    resourceNames.addAll( Arrays.asList("data", "more-data") );
    String message = "expected exception";
    List<Problem> mockErrors = mockProblems(EXAMPLE_PROBLEM_COLUMN, EXAMPLE_PROBLEM_LINE, message, EXAMPLE_PROBLEM_ELEMENT_ID);
    List<Problem> mockWarnings = mockProblems(EXAMPLE_PROBLEM_COLUMN_2, EXAMPLE_PROBLEM_LINE_2, EXAMPLE_EXCEPTION_MESSAGE, EXAMPLE_PROBLEM_ELEMENT_ID_2);
    ParseException mockParseException = createMockParseException(mockErrors, mockWarnings, message);
    when(mockDeploymentBuilder.deployWithResult()).thenThrow(mockParseException);

    Response response = given()
      .multiPart("data", "unspecified", createMockDeploymentResourceByteData())
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(ParseException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .post(CREATE_DEPLOYMENT_URL);

    String content = response.asString();

    Map<String, ResourceReport> details = from(content).getMap("details");
    HashMap<String, List<HashMap<String, Object>>> problems = (HashMap<String, List<HashMap<String, Object>>>) details.get("abc");

    List<HashMap<String, Object>> errors = problems.get("errors");
    HashMap<String, Object> error = errors.get(0);
    assertEquals(EXAMPLE_PROBLEM_COLUMN, error.get("column"));
    assertEquals(EXAMPLE_PROBLEM_LINE, error.get("line"));
    assertEquals(message, error.get("message"));
    assertEquals(EXAMPLE_PROBLEM_ELEMENT_ID, error.get("mainElementId"));
    assertEquals(EXAMPLE_ELEMENT_IDS, error.get("еlementIds"));

    List<HashMap<String, Object>> warnings = problems.get("warnings");
    HashMap<String, Object> warning = warnings.get(0);
    assertEquals(EXAMPLE_PROBLEM_COLUMN_2, warning.get("column"));
    assertEquals(EXAMPLE_PROBLEM_LINE_2, warning.get("line"));
    assertEquals(EXAMPLE_EXCEPTION_MESSAGE, warning.get("message"));
    assertEquals(EXAMPLE_PROBLEM_ELEMENT_ID_2, warning.get("mainElementId"));
    assertEquals(EXAMPLE_ELEMENT_IDS, warning.get("еlementIds"));
  }

  @Test
  public void testDeleteDeployment() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, true, false, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, true, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false, false);
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

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, true, true, false);
  }

  @Test
  public void testDeleteDeploymentSkipIoMappings() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .queryParam("cascade", true)
      .queryParam("skipIoMappings", true)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, true, false, true);
  }

  @Test
  public void testDeleteDeploymentSkipIoMappingsFalse() {

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .queryParam("cascade", true)
      .queryParam("skipIoMappings", false)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DEPLOYMENT_URL);

    verify(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, true, false, false);
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
    doThrow(new AuthorizationException(message)).when(mockRepositoryService).deleteDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID, false, false, false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
       .delete(DEPLOYMENT_URL);
  }

  @Test
  public void testRedeployDeployment() {
    Map<String, Object> json = new HashMap<>();

    List<String> resourceIds = new ArrayList<>();
    resourceIds.add("first-resource-id");
    resourceIds.add("second-resource-id");
    json.put("resourceIds", resourceIds);

    List<String> resourceNames = new ArrayList<>();
    resourceNames.add("first-resource-name");
    resourceNames.add("second-resource-name");
    json.put("resourceNames", resourceNames);

    json.put("source", MockProvider.EXAMPLE_DEPLOYMENT_SOURCE);

    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder, never()).addDeploymentResources(anyString());
    verify(mockDeploymentBuilder).nameFromDeployment(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder, never()).addDeploymentResourceById(anyString(), anyString());
    verify(mockDeploymentBuilder).addDeploymentResourcesById(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID), eq(resourceIds));
    verify(mockDeploymentBuilder, never()).addDeploymentResourceByName(anyString(), anyString());
    verify(mockDeploymentBuilder).addDeploymentResourcesByName(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID), eq(resourceNames));
    verify(mockDeploymentBuilder).source(MockProvider.EXAMPLE_DEPLOYMENT_SOURCE);
    verify(mockDeploymentBuilder).deployWithResult();

    verifyDeployment(mockDeployment, response);
  }

  @Test
  public void testRedeployDeploymentWithoutRequestBody() {
    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).addDeploymentResources(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder).nameFromDeployment(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder, never()).addDeploymentResourceById(anyString(), anyString());
    verify(mockDeploymentBuilder, never()).addDeploymentResourcesById(anyString(), anyList());
    verify(mockDeploymentBuilder, never()).addDeploymentResourceByName(anyString(), anyString());
    verify(mockDeploymentBuilder, never()).addDeploymentResourcesByName(anyString(), anyList());
    verify(mockDeploymentBuilder, never()).source(anyString());
    verify(mockDeploymentBuilder).deployWithResult();

    verifyDeployment(mockDeployment, response);
  }

  @Test
  public void testRedeployDeploymentEmptyRequestBody() {
    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body("{}")
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).addDeploymentResources(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder).nameFromDeployment(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder, never()).addDeploymentResourceById(anyString(), anyString());
    verify(mockDeploymentBuilder, never()).addDeploymentResourcesById(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID), anyList());
    verify(mockDeploymentBuilder, never()).addDeploymentResourceByName(anyString(), anyString());
    verify(mockDeploymentBuilder, never()).addDeploymentResourcesByName(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID), anyList());
    verify(mockDeploymentBuilder).source(null);
    verify(mockDeploymentBuilder).deployWithResult();

    verifyDeployment(mockDeployment, response);
  }

  @Test
  public void testRedeployDeploymentResourceIds() {
    Map<String, Object> json = new HashMap<>();

    List<String> resourceIds = new ArrayList<>();
    resourceIds.add("first-resource-id");
    resourceIds.add("second-resource-id");
    json.put("resourceIds", resourceIds);

    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder, never()).addDeploymentResources(anyString());
    verify(mockDeploymentBuilder).nameFromDeployment(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder, never()).addDeploymentResourceById(anyString(), anyString());
    verify(mockDeploymentBuilder).addDeploymentResourcesById(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID), eq(resourceIds));
    verify(mockDeploymentBuilder, never()).addDeploymentResourceByName(anyString(), anyString());
    verify(mockDeploymentBuilder, never()).addDeploymentResourcesByName(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID), anyList());
    verify(mockDeploymentBuilder).source(null);
    verify(mockDeploymentBuilder).deployWithResult();

    verifyDeployment(mockDeployment, response);
  }

  @Test
  public void testRedeployDeploymentResourceNames() {
    Map<String, Object> json = new HashMap<>();

    List<String> resourceNames = new ArrayList<>();
    resourceNames.add("first-resource-name");
    resourceNames.add("second-resource-name");
    json.put("resourceNames", resourceNames);

    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder, never()).addDeploymentResources(anyString());
    verify(mockDeploymentBuilder).nameFromDeployment(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder, never()).addDeploymentResourceById(anyString(), anyString());
    verify(mockDeploymentBuilder, never()).addDeploymentResourcesById(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID), anyList());
    verify(mockDeploymentBuilder, never()).addDeploymentResourceByName(anyString(), anyString());
    verify(mockDeploymentBuilder).addDeploymentResourcesByName(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID), eq(resourceNames));
    verify(mockDeploymentBuilder).source(null);
    verify(mockDeploymentBuilder).deployWithResult();

    verifyDeployment(mockDeployment, response);
  }

  @Test
  public void testRedeployDeploymentSource() {
    Map<String, String> json = new HashMap<>();
    json.put("source", MockProvider.EXAMPLE_DEPLOYMENT_SOURCE);

    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).addDeploymentResources(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder).nameFromDeployment(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder, never()).addDeploymentResourceById(anyString(), anyString());
    verify(mockDeploymentBuilder, never()).addDeploymentResourcesById(anyString(), anyList());
    verify(mockDeploymentBuilder, never()).addDeploymentResourceByName(anyString(), anyString());
    verify(mockDeploymentBuilder, never()).addDeploymentResourcesByName(anyString(), anyList());
    verify(mockDeploymentBuilder).source(eq(MockProvider.EXAMPLE_DEPLOYMENT_SOURCE));
    verify(mockDeploymentBuilder).deployWithResult();

    verifyDeployment(mockDeployment, response);
  }

  @Test
  public void testRedeployDeploymentWithoutTenantId() {
    when(mockDeployment.getTenantId()).thenReturn(null);

    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).addDeploymentResources(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder, never()).tenantId(any(String.class));
    verify(mockDeploymentBuilder).deployWithResult();

    verifyDeployment(mockDeployment, response);
  }

  @Test
  public void testRedeployDeploymentWithTenantId() {
    when(mockDeployment.getTenantId()).thenReturn(MockProvider.EXAMPLE_TENANT_ID);

    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
    .expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);

    verify(mockDeploymentBuilder).addDeploymentResources(eq(MockProvider.EXAMPLE_DEPLOYMENT_ID));
    verify(mockDeploymentBuilder).tenantId(eq(MockProvider.EXAMPLE_TENANT_ID));
    verify(mockDeploymentBuilder).deployWithResult();

    verifyDeployment(mockDeployment, response);
  }

  @Test
  public void testRedeployThrowsNotFoundException() {
    String message = "deployment not found";
    doThrow(new NotFoundException(message)).when(mockDeploymentBuilder).deployWithResult();

    String expected = "Cannot redeploy deployment '" + MockProvider.EXAMPLE_DEPLOYMENT_ID + "': " + message;

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
    .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is(expected))
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);
  }

  @Test
  public void testRedeployThrowsNotValidException() {
    String message = "not valid";
    doThrow(new NotValidException(message)).when(mockDeploymentBuilder).deployWithResult();

    String expected = "Cannot redeploy deployment '" + MockProvider.EXAMPLE_DEPLOYMENT_ID + "': " + message;

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is(expected))
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);
  }

  @Test
  public void testRedeployThrowsProcessEngineException() {
    String message = "something went wrong";
    doThrow(new ProcessEngineException(message)).when(mockDeploymentBuilder).deployWithResult();

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
    .expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(ProcessEngineException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);
  }

  @Test
  public void testRedeployThrowsAuthorizationException() {
    String message = "missing authorization";
    doThrow(new AuthorizationException(message)).when(mockDeploymentBuilder).deployWithResult();

    given()
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
    .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(message))
    .when()
      .post(REDEPLOY_DEPLOYMENT_URL);
  }

  private void verifyDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();
    verifyDeploymentValues(mockDeployment, content);
  }

  private void verifyCreatedDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();
    verifyDeploymentWithDefinitionsValues(mockDeployment, content);
    verifyDeploymentLink(mockDeployment, content);
  }

  private void verifyCreatedBpmnDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();
    verifyBpmnDeploymentValues(mockDeployment, content);
    verifyDeploymentLink(mockDeployment, content);
  }

  private void verifyCreatedCmmnDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();
    verifyCmmnDeploymentValues(mockDeployment, content);
    verifyDeploymentLink(mockDeployment, content);
  }

  private void verifyCreatedDmnDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();
    verifyDmnDeploymentValues(mockDeployment, content);
    verifyDeploymentLink(mockDeployment, content);
  }

  private void verifyCreatedDrdDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();
    verifyDrdDeploymentValues(mockDeployment, content);
    verifyDeploymentLink(mockDeployment, content);
  }

  private void verifyCreatedEmptyDeployment(Deployment mockDeployment, Response response) {
    String content = response.asString();
    verifyDeploymentValuesEmptyDefinitions(mockDeployment, content);
    verifyDeploymentLink(mockDeployment, content);
  }

  private void verifyDeploymentValues(Deployment mockDeployment, String responseContent) {
    JsonPath path = from(responseContent);
    verifyStandardDeploymentValues(mockDeployment, path);
  }

  private void verifyDeploymentWithDefinitionsValues(Deployment mockDeployment, String responseContent) {
    JsonPath path = from(responseContent);
    verifyStandardDeploymentValues(mockDeployment, path);

    Map<String, HashMap<String, Object>> deployedProcessDefinitions = path.getMap(PROPERTY_DEPLOYED_PROCESS_DEFINITIONS);
    Map<String, HashMap<String, Object>> deployedCaseDefinitions = path.getMap(PROPERTY_DEPLOYED_CASE_DEFINITIONS);
    Map<String, HashMap<String, Object>>  deployedDecisionDefinitions = path.getMap(PROPERTY_DEPLOYED_DECISION_DEFINITIONS);
    Map<String, HashMap<String, Object>>  deployedDecisionRequirementsDefinitions = path.getMap(PROPERTY_DEPLOYED_DECISION_REQUIREMENTS_DEFINITIONS);

    assertEquals(1, deployedProcessDefinitions.size());
    assertNotNull(deployedProcessDefinitions.get(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    assertEquals(1, deployedCaseDefinitions.size());
    assertNotNull(deployedCaseDefinitions.get(EXAMPLE_CASE_DEFINITION_ID));
    assertEquals(1, deployedDecisionDefinitions.size());
    assertNotNull(deployedDecisionDefinitions.get(EXAMPLE_DECISION_DEFINITION_ID));
    assertEquals(1, deployedDecisionRequirementsDefinitions.size());
    assertNotNull(deployedDecisionRequirementsDefinitions.get(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID));
  }

  private void verifyBpmnDeploymentValues(Deployment mockDeployment, String responseContent) {
    JsonPath path = from(responseContent);
    verifyStandardDeploymentValues(mockDeployment, path);

    Map<String, HashMap<String, Object>> deployedProcessDefinitionDtos = path.getMap(PROPERTY_DEPLOYED_PROCESS_DEFINITIONS);

    assertEquals(1, deployedProcessDefinitionDtos.size());
    HashMap processDefinitionDto = deployedProcessDefinitionDtos.get(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    assertNotNull(processDefinitionDto);
    verifyBpmnDeployment(processDefinitionDto);

    assertNull(path.get(PROPERTY_DEPLOYED_CASE_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_DECISION_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_DECISION_REQUIREMENTS_DEFINITIONS));
  }

  private void verifyCmmnDeploymentValues(Deployment mockDeployment, String responseContent) {
    JsonPath path = from(responseContent);
    verifyStandardDeploymentValues(mockDeployment, path);

    Map<String, HashMap<String, Object>> deployedCaseDefinitions = path.getMap(PROPERTY_DEPLOYED_CASE_DEFINITIONS);

    assertEquals(1, deployedCaseDefinitions.size());
    HashMap caseDefinitionDto = deployedCaseDefinitions.get(EXAMPLE_CASE_DEFINITION_ID);
    assertNotNull(caseDefinitionDto);
    verifyCmnDeployment(caseDefinitionDto);

    assertNull(path.get(PROPERTY_DEPLOYED_PROCESS_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_DECISION_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_DECISION_REQUIREMENTS_DEFINITIONS));
  }

  private void verifyDmnDeploymentValues(Deployment mockDeployment, String responseContent) {
    JsonPath path = from(responseContent);
    verifyStandardDeploymentValues(mockDeployment, path);

    Map<String, HashMap<String, Object>> deployedDecisionDefinitions = path.getMap(PROPERTY_DEPLOYED_DECISION_DEFINITIONS);

    assertEquals(1, deployedDecisionDefinitions.size());
    HashMap decisionDefinitionDto = deployedDecisionDefinitions.get(EXAMPLE_DECISION_DEFINITION_ID);
    assertNotNull(decisionDefinitionDto);
    verifyDmnDeployment(decisionDefinitionDto);

    assertNull(path.get(PROPERTY_DEPLOYED_DECISION_REQUIREMENTS_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_PROCESS_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_CASE_DEFINITIONS));
  }

  private void verifyDrdDeploymentValues(Deployment mockDeployment, String responseContent) {
    JsonPath path = from(responseContent);
    verifyStandardDeploymentValues(mockDeployment, path);

    Map<String, HashMap<String, Object>>  deployedDecisionDefinitions =
      path.getMap(PROPERTY_DEPLOYED_DECISION_DEFINITIONS);
    Map<String, HashMap<String, Object>> deployedDecisionRequirementsDefinitions =
      path.getMap(PROPERTY_DEPLOYED_DECISION_REQUIREMENTS_DEFINITIONS);

    assertEquals(1, deployedDecisionDefinitions.size());
    HashMap decisionDefinitionDto = deployedDecisionDefinitions.get(EXAMPLE_DECISION_DEFINITION_ID);
    assertNotNull(decisionDefinitionDto);
    verifyDmnDeployment(decisionDefinitionDto);

    assertEquals(1, deployedDecisionRequirementsDefinitions.size());
    HashMap decisionRequirementsDefinitionDto = deployedDecisionRequirementsDefinitions.get(EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    assertNotNull(decisionRequirementsDefinitionDto);
    verifyDrdDeployment(decisionRequirementsDefinitionDto);

    assertNull(path.get(PROPERTY_DEPLOYED_PROCESS_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_CASE_DEFINITIONS));
  }

  private void verifyBpmnDeployment(HashMap<String, Object> dto) {
    assertEquals(dto.get("id"), MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    assertEquals(dto.get("category"), EXAMPLE_PROCESS_DEFINITION_CATEGORY);
    assertEquals(dto.get("name"), EXAMPLE_PROCESS_DEFINITION_NAME);
    assertEquals(dto.get("key"), EXAMPLE_PROCESS_DEFINITION_KEY);
    assertEquals(dto.get("description"), EXAMPLE_PROCESS_DEFINITION_DESCRIPTION);
    assertEquals(dto.get("version"), EXAMPLE_PROCESS_DEFINITION_VERSION);
    assertEquals(dto.get("resource"), EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME);
    assertEquals(dto.get("deploymentId"), EXAMPLE_DEPLOYMENT_ID);
    assertEquals(dto.get("diagram"), EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME);
    assertEquals(dto.get("suspended"), EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED);
  }
  private void verifyCmnDeployment(HashMap<String, Object> dto) {
    assertEquals(dto.get("id"), EXAMPLE_CASE_DEFINITION_ID);
    assertEquals(dto.get("category"), EXAMPLE_CASE_DEFINITION_CATEGORY);
    assertEquals(dto.get("name"), EXAMPLE_CASE_DEFINITION_NAME);
    assertEquals(dto.get("key"), EXAMPLE_CASE_DEFINITION_KEY);
    assertEquals(dto.get("version"), EXAMPLE_CASE_DEFINITION_VERSION);
    assertEquals(dto.get("resource"), EXAMPLE_CASE_DEFINITION_RESOURCE_NAME);
    assertEquals(dto.get("deploymentId"), EXAMPLE_DEPLOYMENT_ID);
  }

  private void verifyDmnDeployment(HashMap<String, Object> dto) {
    assertEquals(dto.get("id"), EXAMPLE_DECISION_DEFINITION_ID);
    assertEquals(dto.get("category"), EXAMPLE_DECISION_DEFINITION_CATEGORY);
    assertEquals(dto.get("name"), EXAMPLE_DECISION_DEFINITION_NAME);
    assertEquals(dto.get("key"), EXAMPLE_DECISION_DEFINITION_KEY);
    assertEquals(dto.get("version"), EXAMPLE_DECISION_DEFINITION_VERSION);
    assertEquals(dto.get("resource"), EXAMPLE_DECISION_DEFINITION_RESOURCE_NAME);
    assertEquals(dto.get("deploymentId"), EXAMPLE_DEPLOYMENT_ID);
    assertEquals(dto.get("decisionRequirementsDefinitionId"), EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    assertEquals(dto.get("decisionRequirementsDefinitionKey"), EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY);
  }

  private void verifyDrdDeployment(HashMap<String, Object> dto) {
    assertEquals(dto.get("id"), EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    assertEquals(dto.get("category"), EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_CATEGORY);
    assertEquals(dto.get("name"), EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_NAME);
    assertEquals(dto.get("key"), EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY);
    assertEquals(dto.get("version"), EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_VERSION);
    assertEquals(dto.get("resource"), EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_RESOURCE_NAME);
    assertEquals(dto.get("deploymentId"), EXAMPLE_DEPLOYMENT_ID);
  }

  private void verifyDeploymentValuesEmptyDefinitions(Deployment mockDeployment, String responseContent) {
    JsonPath path = from(responseContent);
    verifyStandardDeploymentValues(mockDeployment, path);

    assertNull(path.get(PROPERTY_DEPLOYED_PROCESS_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_CASE_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_DECISION_DEFINITIONS));
    assertNull(path.get(PROPERTY_DEPLOYED_DECISION_REQUIREMENTS_DEFINITIONS));
  }

  private void verifyStandardDeploymentValues(Deployment mockDeployment, JsonPath path) {
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
    assertTrue(returnedLink.get("href").endsWith(RESOURCE_URL + "/" + mockDeployment.getId()));
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

  private List<Problem> mockProblems(int column, int line, String message, String elementId) {
    Problem mockProblem = mock(Problem.class);
    when(mockProblem.getColumn()).thenReturn(column);
    when(mockProblem.getLine()).thenReturn(line);
    when(mockProblem.getMessage()).thenReturn(message);
    when(mockProblem.getMainElementId()).thenReturn(elementId);
    when(mockProblem.getElementIds()).thenReturn(EXAMPLE_ELEMENT_IDS);
    List<Problem> mockProblems = new ArrayList<>();
    mockProblems.add(mockProblem);
    return mockProblems;
  }

  private ParseException createMockParseException(List<Problem> mockErrors,
      List<Problem> mockWarnings, String message) {
    ParseException mockParseException = mock(ParseException.class);
    when(mockParseException.getMessage()).thenReturn(message);

    ResourceReportImpl report = mock(ResourceReportImpl.class);
    when(report.getResourceName()).thenReturn(EXAMPLE_RESOURCE_NAME);
    when(report.getErrors()).thenReturn(mockErrors);
    when(report.getWarnings()).thenReturn(mockWarnings);

    List<ResourceReport> reports = new ArrayList<>();
    reports.add(report);

    when(mockParseException.getResorceReports()).thenReturn(reports);
    return mockParseException;
  }

}
