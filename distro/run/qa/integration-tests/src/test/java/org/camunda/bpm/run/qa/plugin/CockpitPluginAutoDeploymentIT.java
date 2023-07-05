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
package org.camunda.bpm.run.qa.plugin;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import io.restassured.response.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.junit.After;
import org.junit.Test;

public class CockpitPluginAutoDeploymentIT {

  static final String EXAMPLE_PLUGIN_HOME = "example.plugin.home";
  static final String PLUGIN_ENDPOINT = "/camunda/api/cockpit/plugin/test-cockpit-plugin/test-string";

  static SpringBootManagedContainer container;
  static String baseDirectory = SpringBootManagedContainer.getRunHome();

  protected List<String> deployedPlugins = new ArrayList<>();

  @After
  public void teardown() {
    stopApp();
    undeployPlugins();
  }

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

  public void runStartScript() {
    container = new SpringBootManagedContainer();
    container.replaceConfigurationYml(SpringBootManagedContainer.APPLICATION_YML_PATH,
        SpringBootManagedContainer.class.getClassLoader().getResourceAsStream("base-test-application.yml"));
    try {
      container.start();
    } catch (Exception e) {
      throw new RuntimeException("Cannot start managed Spring Boot application!", e);
    }
  }

  @Test
  public void shouldAutoDeployCockpitPlugin() throws IOException {
    // given
    deployPlugin("camunda-bpm-run-example-plugin.jar");
    runStartScript();

    // when
    Response response = when().get(container.getBaseUrl() + PLUGIN_ENDPOINT);

    // then
    String responseBody = response.then()
      .statusCode(Status.OK.getStatusCode())
      .extract().body().asString();

    assertThat(responseBody).isEqualTo("test string");
  }

  protected void deployPlugin(String jarName) throws IOException {
    Path runUserlibDir = Paths.get(baseDirectory, SpringBootManagedContainer.USERLIB_PATH);
    String pluginHome = System.getProperty(EXAMPLE_PLUGIN_HOME);

    if (pluginHome == null || pluginHome.isEmpty()) {
      throw new RuntimeException("System property " + EXAMPLE_PLUGIN_HOME + " not set. This property must point "
          + "to the root directory of the plugin to deploy.");
    }

    Path pluginPath = Paths.get(pluginHome, jarName).toAbsolutePath();
    Path copy = Files.copy(pluginPath, runUserlibDir.resolve(pluginPath.getFileName()));

    deployedPlugins.add(copy.toString());
  }

  protected void undeployPlugins() {
    for (String pluginPath : deployedPlugins) {
      try {
        Files.delete(Paths.get(pluginPath));
      } catch (IOException e) {
        fail("unable to undeploy plugin " + pluginPath);
      }
    }
  }
}
