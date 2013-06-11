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

import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.cockpit.test.sample.plugin.embedded.EmbeddedPlugin;
import org.camunda.bpm.cockpit.test.util.AbstractCockpitCoreTest;
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
public class EmbeddedPluginApiTest extends AbstractCockpitCoreTest {

  @Deployment
  public static Archive<?> createDeployment() {

    WebArchive archive = createBaseDeployment("test-embedded.war")
          .addAsServiceProvider(CockpitPlugin.class, EmbeddedPlugin.class)
          .addPackages(true, EmbeddedPlugin.class.getPackage())
          .addAsWebResource("org/camunda/bpm/cockpit/test/sample/plugin/simple/assets/test.txt", "/plugin/embedded/test.txt");

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
  public void shouldServeWebappEmbeddedPluginAsset() throws Exception {

    WebResource appResource = client.resource(contextPath.toURI());

    // /api/plugin/:pluginName/static/...
    ClientResponse result = appResource.path("/api/plugin/embedded/static/test.txt").accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity(String.class)).isEqualTo("FOO BAR");
  }
}
