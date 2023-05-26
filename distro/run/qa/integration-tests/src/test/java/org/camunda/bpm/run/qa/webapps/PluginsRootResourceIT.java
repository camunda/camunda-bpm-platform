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
package org.camunda.bpm.run.qa.webapps;

import com.sun.jersey.api.client.ClientResponse;
import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.AfterParam;
import org.junit.runners.Parameterized.BeforeParam;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * NOTE:
 * copied from
 * <a href="https://github.com/camunda/camunda-bpm-platform/blob/master/qa/integration-tests-webapps/integration-tests/src/main/java/org/camunda/bpm/PluginsRootResourceIT.java">platform</a>
 * then added <code>@BeforeParam</code> and <code>@AfterParam</code> methods for container setup
 * and changed  <code>appBasePath</code> to <code>APP_BASE_PATH</code>, might be removed with https://jira.camunda.com/browse/CAM-11379
 */
@RunWith(Parameterized.class)
public class PluginsRootResourceIT extends AbstractWebIT {

  @Parameter(0)
  public String assetName;

  @Parameter(1)
  public boolean assetAllowed;

  @Before
  public void createClient() throws Exception {
    createClient(getWebappCtxPath());
  }

  private static SpringBootManagedContainer container;

  @BeforeParam
  public static void runStartScript(String assetName, boolean assetAllowed) {
    container = new SpringBootManagedContainer("--webapps");
    try {
      container.start();
    } catch (Exception e) {
      throw new RuntimeException("Cannot start managed Spring Boot application!", e);
    }
  }

  @AfterParam
  public static void stopApp() {
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

  @Parameters(name = "Test instance: {index}. Asset: {0}, Allowed: {1}")
  public static Collection<Object[]> getAssets() {
    return Arrays.asList(new Object[][]{
        {"app/plugin.js", true},
        {"app/plugin.css", true},
        {"app/asset.js", false},
        {"../..", false},
        {"../../annotations-api.jar", false},
    });
  }

  @Test
  public void shouldGetAssetIfAllowed() {
    // when
    ClientResponse response = getAsset("api/admin/plugin/adminPlugins/static/" + assetName);

    // then
    assertResponse(assetName, response);

    // cleanup
    response.close();
  }

  protected ClientResponse getAsset(String path) {
    return client.resource(APP_BASE_PATH + path).get(ClientResponse.class);
  }

  protected void assertResponse(String asset, ClientResponse response) {
    if (assetAllowed) {
      assertEquals(Status.OK.getStatusCode(), response.getStatus());
    } else {
      assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
      assertTrue(response.getType().toString().startsWith(MediaType.APPLICATION_JSON));
      String responseEntity = response.getEntity(String.class);
      assertTrue(responseEntity.contains("\"type\":\"RestException\""));
      assertTrue(responseEntity.contains("\"message\":\"Not allowed to load the following file '" + asset + "'.\""));
    }
  }

}
