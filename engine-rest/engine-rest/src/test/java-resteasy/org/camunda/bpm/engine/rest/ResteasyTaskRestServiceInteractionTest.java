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
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.TaskRestServiceInteractionTest;
import org.junit.Ignore;
import org.junit.Test;

/*
 *  in 2.3.X resteasy, the thrown exception for unsupported media type is not extending WebApplicationException =>
 *  the ExceptionHandler is returning INTERNAL_SERVER_ERROR instead of UNSUPPORTED_MEDIA_TYPE
 *  after update of resteasy > 3.X/4.0, the problem should be resolved and this test should be no longer needed
 */
public class ResteasyTaskRestServiceInteractionTest extends TaskRestServiceInteractionTest {

  @Test
  @Override
  @Ignore
  public void testAddTaskCommentWithoutBody() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode()) // INTERNAL_SERVER_ERROR
    .when()
      .post(SINGLE_TASK_ADD_COMMENT_URL);
  }

  @Test
  @Override
  @Ignore
  public void testCreateTaskAttachmentWithoutMultiparts() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode()) // INTERNAL_SERVER_ERROR
    .when()
      .post(SINGLE_TASK_ADD_ATTACHMENT_URL);
  }
}
