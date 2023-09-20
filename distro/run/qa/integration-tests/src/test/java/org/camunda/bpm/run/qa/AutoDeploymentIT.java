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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class AutoDeploymentIT {
  static final String PROCESS_DEFINITION_ENDPOINT = "/engine-rest/process-definition";
  static final String DEPLOYMENT_ENDPOINT = "/engine-rest/deployment";

  static List<File> dummyFiles = new ArrayList<>();
  static SpringBootManagedContainer container;
  static String baseDirectory = SpringBootManagedContainer.getRunHome();

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
    container = new SpringBootManagedContainer();
    container.replaceConfigurationYml(SpringBootManagedContainer.APPLICATION_YML_PATH,
        AutoDeploymentIT.class.getClassLoader().getResourceAsStream("example-disabled.yml"));
    try {
      container.start();
    } catch (Exception e) {
      throw new RuntimeException("Cannot start managed Spring Boot application!", e);
    }
  }

  public void createBPMNFile(String path, String processDefinitionId) throws IOException {
    Path resourcesDir = Paths.get(baseDirectory, SpringBootManagedContainer.RESOURCES_PATH, path);
    resourcesDir.toFile().mkdirs();
    File bpmnFile = Paths.get(resourcesDir.toString(), "process.bpmn").toFile();
    bpmnFile.createNewFile();

    BpmnModelInstance model = Bpmn.createExecutableProcess(processDefinitionId)
        .camundaHistoryTimeToLive(180)
        .startEvent()
        .endEvent()
        .done();

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
  public void shouldAutoDeployScriptAndForms() throws IOException {
    // given
    InputStream formFile = AutoDeploymentIT.class.getClassLoader().getResourceAsStream("deployment/form.html");
    InputStream  scriptFile = AutoDeploymentIT.class.getClassLoader().getResourceAsStream("deployment/script.js");
    File resourcesDirectory = Paths.get(baseDirectory, "configuration", "resources").toFile();
    resourcesDirectory.mkdirs();
    Files.copy(formFile, Paths.get(resourcesDirectory.getAbsolutePath(), "form.html"));
    Files.copy(scriptFile, Paths.get(resourcesDirectory.getAbsolutePath(), "script.js"));

    // when
    runStartScript();

    Response definitionResponse = when().get(container.getBaseUrl() + DEPLOYMENT_ENDPOINT);
    definitionResponse.then()
      .body("size()", is(1));
    String deploymentId = definitionResponse.then().extract().path("[0].id");

    // when
    Response resourcesResponse = when().get(container.getBaseUrl() + DEPLOYMENT_ENDPOINT + "/" + deploymentId + "/resources");
    List<String> resourceNames = extractResourceNames(resourcesResponse);

    // then
    assertThat(resourceNames).contains("/form.html", "/script.js");
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