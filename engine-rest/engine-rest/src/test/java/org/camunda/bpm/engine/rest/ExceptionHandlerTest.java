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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.ClassRule;
import org.junit.Test;

import io.restassured.http.ContentType;

public class ExceptionHandlerTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  private static final String EXCEPTION_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/unannotated";

  private static final String GENERAL_EXCEPTION_URL = EXCEPTION_RESOURCE_URL + "/exception";
  private static final String PROCESS_ENGINE_EXCEPTION_URL = EXCEPTION_RESOURCE_URL + "/processEngineException";
  private static final String REST_EXCEPTION_URL = EXCEPTION_RESOURCE_URL + "/restException";
  private static final String AUTH_EXCEPTION_URL = EXCEPTION_RESOURCE_URL + "/authorizationException";
  private static final String AUTH_EXCEPTION_MULTIPLE_URL = EXCEPTION_RESOURCE_URL + "/authorizationExceptionMultiple";
  private static final String STACK_OVERFLOW_ERROR_URL = EXCEPTION_RESOURCE_URL + "/stackOverflowError";


  @Test
  public void testGeneralExceptionHandler() {
    given().header(ACCEPT_WILDCARD_HEADER)
    .expect().contentType(ContentType.JSON)
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", equalTo(Exception.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
      .body("code", nullValue())
    .when().get(GENERAL_EXCEPTION_URL);
  }

  @Test
  public void testRestExceptionHandler() {
    given().header(ACCEPT_WILDCARD_HEADER)
    .expect().contentType(ContentType.JSON)
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
      .body("code", nullValue())
    .when().get(REST_EXCEPTION_URL);
  }

  @Test
  public void testProcessEngineExceptionHandler() {
    given().header(ACCEPT_WILDCARD_HEADER)
    .expect().contentType(ContentType.JSON)
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
      .body("code", equalTo(0))
    .when().get(PROCESS_ENGINE_EXCEPTION_URL);
  }

  @Test
  public void testAuthorizationExceptionHandler() {
  //    TODO remove "resourceName", "resourceId", "permissionName" once the deprecated methods from AuthorizationException are removed
    given().header(ACCEPT_WILDCARD_HEADER)
    .expect().contentType(ContentType.JSON)
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo("The user with id 'someUser' does not have 'somePermission' permission on resource 'someResourceId' of type 'someResourceName'."))
      .body("code", equalTo(0))
      .body("userId", equalTo("someUser"))
      .body("resourceName", equalTo("someResourceName"))
      .body("resourceId", equalTo("someResourceId"))
      .body("permissionName", equalTo("somePermission"))
      .body("missingAuthorizations.resourceName", hasItems("someResourceName"))
      .body("missingAuthorizations.resourceId", hasItems("someResourceId"))
      .body("missingAuthorizations.permissionName", hasItems("somePermission"))
    .when().get(AUTH_EXCEPTION_URL);
  }

  @Test
  public void testAuthorizationExceptionWithMultipleMissingAuthorizationsHandler() {
  //    TODO remove "resourceName", "resourceId", "permissionName" once the deprecated methods from AuthorizationException are removed
    given().header(ACCEPT_WILDCARD_HEADER)
    .expect().contentType(ContentType.JSON)
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("code", equalTo(0))
      .body("message", equalTo("The user with id 'someUser' does not have one of the following permissions: 'somePermission1' permission on resource "
          + "'someResourceId1' of type 'someResourceName1' or 'somePermission2' permission on resource "
          + "'someResourceId2' of type 'someResourceName2'"))
      .body("userId", equalTo("someUser"))
      .body("resourceName", equalTo(null))
      .body("resourceId", equalTo(null))
      .body("permissionName", equalTo(null))
      .body("missingAuthorizations.resourceName", hasItems("someResourceName1", "someResourceName2"))
      .body("missingAuthorizations.resourceId", hasItems("someResourceId1", "someResourceId2"))
      .body("missingAuthorizations.permissionName", hasItems("somePermission1", "somePermission2"))
    .when().get(AUTH_EXCEPTION_MULTIPLE_URL);
  }

  @Test
  public void testThrowableHandler() {
    given().header(ACCEPT_WILDCARD_HEADER)
      .expect().contentType(ContentType.JSON)
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", equalTo(StackOverflowError.class.getSimpleName()))
        .body("message", equalTo("Stack overflow"))
        .body("code", nullValue())
      .when().get(STACK_OVERFLOW_ERROR_URL);
  }

}
