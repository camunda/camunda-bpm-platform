package org.camunda.bpm.cockpit.plugin.core.resources;

import static org.camunda.bpm.cockpit.test.util.DeploymentUtil.cockpitWar;
import static org.camunda.bpm.cockpit.test.util.DeploymentUtil.festAssertions;
import static org.camunda.bpm.cockpit.test.util.DeploymentUtil.resteasyJaxRs;
import static org.camunda.bpm.cockpit.test.util.DeploymentUtil.testPluginJar;
import static org.camunda.bpm.cockpit.test.util.DeploymentUtil.testProcessArchiveJar;
import static org.fest.assertions.Assertions.assertThat;

import java.net.URL;

import javax.ws.rs.core.MediaType;

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
public class PluginApiTest {

  @Deployment
  public static Archive<?> createDeployment() {

    WebArchive archive = cockpitWar()
          .addAsLibraries(festAssertions())
          .addAsLibraries(resteasyJaxRs())
          .addAsLibraries(testProcessArchiveJar())
          .addAsLibraries(testPluginJar());

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
    ClientResponse result = appResource.path("/api/plugin/test/static/test.txt").accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity(String.class)).isEqualTo("FOO BAR");
  }

  @Test
  @RunAsClient
  public void shouldPublishPluginApi() throws Exception {

    WebResource appResource = client.resource(contextPath.toURI());
    // /api/plugin/:pluginName/:engineName/...
    ClientResponse result = appResource.path("/api/plugin/test/default/test").accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity(String.class)).contains("[ProcessInstance[");
  }
}
