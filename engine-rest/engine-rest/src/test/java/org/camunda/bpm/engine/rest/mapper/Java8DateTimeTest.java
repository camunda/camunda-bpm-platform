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
package org.camunda.bpm.engine.rest.mapper;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class Java8DateTimeTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance";
  protected static final String SINGLE_PROCESS_INSTANCE_URL = PROCESS_INSTANCE_URL + "/{id}";
  protected static final String PROCESS_INSTANCE_VARIABLES_URL = SINGLE_PROCESS_INSTANCE_URL + "/variables";
  protected static final String SINGLE_PROCESS_INSTANCE_VARIABLE_URL = PROCESS_INSTANCE_VARIABLES_URL + "/{varId}";

  protected RuntimeServiceImpl runtimeServiceMock;

  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeServiceImpl.class);

    when(runtimeServiceMock.getVariableTyped(eq(EXAMPLE_PROCESS_INSTANCE_ID), eq(EXAMPLE_VARIABLE_KEY), eq(true)))
      .thenReturn(Variables.objectValue(new Java8DateTimePojo()).create());

    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
  }

  @Test
  public void testGetVariable() {
    given()
        .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
        .pathParam("varId", EXAMPLE_VARIABLE_KEY)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("value.localDate", is(LocalDate.now().toString()))
        .body("type", is("Object"))
      .when()
        .get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

}
