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
package org.camunda.bpm.run.qa;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.junit.After;
import org.junit.Test;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class AutoDeploymentIntegrationTest {
  static final String PROCESS_DEFINITION_ENDPOINT = "/rest/process-definition";
  static final String DEPLOYMENT_ENDPOINT = "/rest/deployment";

  static URL distroBase = AutoDeploymentIntegrationTest.class.getClassLoader().getResource("camunda-bpm-run-distro");
  static List<File> dummyFiles = new ArrayList<>();
  static SpringBootManagedContainer container;

  @After
  public void stopApp() {
    try {
      if (container != null) {
        container.stop();
      }
    } catch (Exception e) {
      throw new RuntimeException("Cannot stop managed Spring Boot application!", e);
    } finally {
      container = null;
    }
  }

  public void runStartScript() throws IOException {
    assertNotNull(distroBase);

    File file = new File(distroBase.getFile());
    container = new SpringBootManagedContainer(file.getAbsolutePath());
    try {
      container.start();
    } catch (Exception e) {
      throw new RuntimeException("Cannot start managed Spring Boot application!", e);
    }
  }

  public static void createBPMNFile(String path, String processDefinitionId) throws IOException {
    File baseDir = new File(new File(distroBase.getFile()), "/configuration/");
    File resourcesDir = new File(baseDir, "resources/" + path);
    resourcesDir.mkdirs();
    File bpmnFile = new File(resourcesDir, "process.bpmn");
    bpmnFile.createNewFile();
    BpmnModelInstance model = Bpmn.createExecutableProcess(processDefinitionId).startEvent().endEvent().done();
    Bpmn.writeModelToFile(bpmnFile, model);
    dummyFiles.add(bpmnFile);
  }

  @Test
  public void shouldAutoDeployProcessDefinition() throws IOException {
    // given
    createBPMNFile("", "process1");
    runStartScript();

    // when
    Response deploymentResponse = when().get(container.getBaseUrl() + DEPLOYMENT_ENDPOINT + "/count");

    // then
    // one deployment was made
    deploymentResponse.then()
      .statusCode(Status.OK.getStatusCode())
      .body("count", is(1));

    // when
    Response definitionResponse = when().get(container.getBaseUrl() + PROCESS_DEFINITION_ENDPOINT);

    // then
    // only the dummy file was deployed
    definitionResponse.then()
      .statusCode(Status.OK.getStatusCode())
      .body("size()", is(1))
      .body("[0].key", is("process1"));
  }

  @Test
  public void shouldSetRelativePathAsResourceName() throws IOException {
    // given
    createBPMNFile("", "process1");
    createBPMNFile("nested/", "process2");
    runStartScript();

    Response definitionResponse = when().get(container.getBaseUrl() + DEPLOYMENT_ENDPOINT);
    definitionResponse.then()
      .body("size()", is(1));
    String deploymentId = definitionResponse.then().extract().path("[0].id");

    // when
    Response resourcesResponse = when().get(container.getBaseUrl() + DEPLOYMENT_ENDPOINT + "/" + deploymentId + "/resources");
    List<String> resourceNames = extractResourceNames(resourcesResponse);
    for (String name : resourceNames) {
      assertThatResourceNameIsRelativePath(name);
    }
  }

  private List<String> extractResourceNames(Response response) {
    ExtractableResponse<Response> extract = response.then().extract();
    int size = extract.path("size()");
    ArrayList<String> resourceNames = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      resourceNames.add(extract.path("[" + i + "].name"));
    }
    return resourceNames;
  }

  private void assertThatResourceNameIsRelativePath(String name) {
    for (File file : dummyFiles) {
      String path = file.getAbsolutePath().replace(File.separator, "/");
      String[] split = path.split("resources");
      String string = split[split.length - 1];
      if (string.equals(name)) {
        return;
      }
    }
    fail("Expected file " + name + "to be any of " + dummyFiles.stream().map(f -> f.getName()).collect(Collectors.joining(", ")));
  }
}