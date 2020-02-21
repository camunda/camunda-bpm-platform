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

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.restassured.response.Response;

public class AutoDeploymentIntegrationTest {

  private File dummyFile;
  
  protected static SpringBootManagedContainer container;
  protected URL distroBase = AutoDeploymentIntegrationTest.class.getClassLoader().getResource("camunda-bpm-run-distro");

  @Before
  public void runStartScript() throws IOException {
    assertNotNull(distroBase);

    createBpmnFile(distroBase);
    
    File file = new File(distroBase.getFile());
    container = new SpringBootManagedContainer(file.getAbsolutePath());
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
      cleanup();
    }
  }

  private void createBpmnFile(URL distroBase) throws IOException {
      File baseDir = new File(distroBase.getFile());
      File bpmnFile = new File(baseDir, "/configuration/resources/process.bpmn");
      bpmnFile.createNewFile();
      BpmnModelInstance simpleBPMN = Bpmn.createExecutableProcess().startEvent().endEvent().done();
      Bpmn.writeModelToFile(bpmnFile, simpleBPMN);
      
      dummyFile = bpmnFile;
  }
  
  private void cleanup() {
    if(dummyFile != null) {
      dummyFile.delete();
    }
  }

  @Test
  public void testA() {
    Response response = when().get(container.getBaseUrl() + "/rest/deployment/count");
      response.then()
        .body("size()", is(1))
        .body("count", is(1));
  }
}
