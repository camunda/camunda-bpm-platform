/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.cockpit.test.plugin.resources;

import static org.fest.assertions.Assertions.assertThat;

import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.cockpit.test.util.AbstractCockpitCoreTest;
import org.camunda.bpm.cockpit.test.util.DeploymentHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


/**
 *
 * @author nico.rehwaldt
 */
@RunWith(Arquillian.class)
public class PluginApiTest extends AbstractCockpitCoreTest {

  @Deployment
  public static Archive<?> createDeployment() {

    WebArchive archive = createBaseDeployment()
          .addAsLibraries(DeploymentHelper.getTestProcessArchiveJar())
          .addAsLibraries(DeploymentHelper.getCockpitTestPluginJar());

    return archive;
  }

  @ArquillianResource
  private URL contextPath;

  private Client client;

  @Before
  public void before() {
    client = Client.create();
  }

  @After
  public void after() {
    client.destroy();
  }

  @Test
  @RunAsClient
  public void shouldServePluginAsset() throws Exception {

    WebResource appResource = client.resource(contextPath.toURI());

    // /api/plugin/:pluginName/static/...
    ClientResponse result = appResource.path("/api/plugin/simple/static/test.txt").accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity(String.class)).isEqualTo("FOO BAR");
  }

  @Test
  @RunAsClient
  public void shouldServePluginNestedAsset() throws Exception {

    WebResource appResource = client.resource(contextPath.toURI());

    // /api/plugin/:pluginName/static/...
    ClientResponse result = appResource.path("/api/plugin/simple/static/app/plugin.js").accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity(String.class)).isEqualTo("// plugin definition");
  }

  @Test
  @RunAsClient
  public void shouldPublishPluginApi() throws Exception {

    WebResource appResource = client.resource(contextPath.toURI());
    // /api/plugin/:pluginName/:engineName/...
    ClientResponse result = appResource.path("/api/plugin/simple/default/test").accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity(String.class)).contains("[");
  }
}
