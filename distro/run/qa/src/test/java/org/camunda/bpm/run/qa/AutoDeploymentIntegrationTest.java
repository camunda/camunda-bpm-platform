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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AutoDeploymentIntegrationTest {

  static final String PROCESS_DEFINITION_ENDPOINT = "/rest/process-definition";
  static final String DEPLOYMENT_ENDPOINT = "/rest/deployment/count";
  static final String PROCESS_DEFINITION_ID = "dummyProcDef";

  static SpringBootManagedContainer container;
  static URL distroBase = AutoDeploymentIntegrationTest.class.getClassLoader().getResource("camunda-bpm-run-distro");
  static URL subfolderDistroBase = AutoDeploymentIntegrationTest.class.getClassLoader().getResource("subfolder/camunda-bpm-run-distro");
  static List<File> dummyFiles = new ArrayList<>();
  static BpmnModelInstance dummyModel;

  @BeforeClass
  public static void createFiles() throws IOException {
    createBpmnFile(distroBase);
    createBpmnFile(subfolderDistroBase);
  }
  
  @AfterClass
  public static void cleanup() {
    for (File file : dummyFiles) {
      file.delete();
    }
  }

  public void runStartScript(URL base) throws IOException {
    assertNotNull(base);

    File file = new File(base.getFile());
    container = new SpringBootManagedContainer(file.getAbsolutePath(), AutoDeploymentIntegrationTest.class);
    try {
      container.start();
    } catch (Exception e) {
      throw new RuntimeException("Cannot start managed Spring Boot application!", e);
    }
  }

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

  private static void createBpmnFile(URL base) throws IOException {
      File baseDir = new File(new File(base.getFile()), "/configuration/");
      File resourcesDir = new File(baseDir, "resources/");
      resourcesDir.mkdir();
      File bpmnFile = new File(resourcesDir, "process.bpmn");
      bpmnFile.createNewFile();
      if(dummyModel == null) {
        dummyModel = Bpmn.createExecutableProcess(PROCESS_DEFINITION_ID).startEvent().endEvent().done();
      }
      Bpmn.writeModelToFile(bpmnFile, dummyModel);
      dummyFiles.add(bpmnFile);
  }

  @Test
  public void test1_shouldAutoDeployProcessDefinition() throws IOException {
    // given
    runStartScript(distroBase);

    assertOnlyOneDeploymentWasMade();
  }

  @Test
  public void test2_shouldNotRedeployAfterMigration() throws IOException {
    // given
    runStartScript(subfolderDistroBase);

    assertOnlyOneDeploymentWasMade();
  }

  private void assertOnlyOneDeploymentWasMade () {
    // when
    Response deploymentResponse = when().get(container.getBaseUrl() + DEPLOYMENT_ENDPOINT);

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
      .body("[0].key", is(PROCESS_DEFINITION_ID));
  }
}