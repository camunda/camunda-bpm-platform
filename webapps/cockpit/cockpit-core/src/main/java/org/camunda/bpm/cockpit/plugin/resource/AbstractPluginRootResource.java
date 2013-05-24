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
package org.camunda.bpm.cockpit.plugin.resource;

import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.plugin.PluginRegistry;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;

/**
 * A resource class that provides a plugins restful API.
 *
 * <p>
 *
 * Subclasses of this class may provide subresources using annotated getters
 * in order to be multi-engine aware.
 *
 * <p>
 *
 * Subresources must properly initialize the subresources via
 * {@link AbstractPluginRootResource#subResource(AbstractPluginResource, String) }.
 *
 * <pre>
 * @Path("myplugin")
 * public class MyPluginRootResource extends AbstractPluginRootResource {
 *
 *   @Path("{engine}/my-resource")
 *   public FooResource getFooResource(@PathParam("engine") String engine) {
 *     return subResource(new FooResource(engine), engine);
 *   }
 * }
 * </pre>
 *
 * @author nico.rehwaldt
 */
public class AbstractPluginRootResource {

  protected static final String WEBAPP_PREFIX = "webapp://";

  @Context
  private ServletContext servletContext;

  private final String pluginName;

  public AbstractPluginRootResource(String pluginName) {
    this.pluginName = pluginName;
  }

  /**
   *
   * @param <T>
   * @param subResource
   * @param engineName
   * @return
   */
  protected <T extends AbstractPluginResource> T subResource(T subResource, String engineName) {
    return subResource;
  }

  /**
   * Provides a plugins asset files via <code>$PLUGIN_ROOT_PATH/static</code>.
   *
   * @param file
   * @return
   */
  @GET
  @Path("/static/{file:.*}")
  public Response getAsset(@PathParam("file") String file) {

    CockpitPlugin plugin = getPluginRegistry().getPlugin(pluginName);

    if (plugin != null) {
      InputStream assetStream = getPluginAssetAsStream(plugin, file);
      if (assetStream != null) {
        String contentType = getContentType(file);
        return Response.ok(assetStream, contentType).build();
      }
    }

    // no asset found
    throw new WebApplicationException(Status.NOT_FOUND);
  }

  protected String getContentType(String file) {
    if (file.endsWith(".js")) {
      return "text/javascript";
    } else
    if (file.endsWith(".html")) {
      return "text/html";
    } else {
      return "text/plain";
    }
  }

  /**
   * Returns an input stream for a given resource
   *
   * @param resourceName
   * @return
   */
  private InputStream getPluginAssetAsStream(CockpitPlugin plugin, String fileName) {

    String assetDirectory = plugin.getAssetDirectory();

    if (assetDirectory == null) {
      return null;
    }

    if (assetDirectory.startsWith(WEBAPP_PREFIX)) {
      assetDirectory = assetDirectory.substring(WEBAPP_PREFIX.length());
      return getWebResourceAsStream(assetDirectory, fileName);
    } else {
      return getClasspathResourceAsStream(plugin, assetDirectory, fileName);
    }
  }

  private InputStream getWebResourceAsStream(String assetDirectory, String fileName) {
    String resourceName = String.format("/%s/%s", assetDirectory, fileName);

    return servletContext.getResourceAsStream(resourceName);
  }

  private InputStream getClasspathResourceAsStream(CockpitPlugin plugin, String assetDirectory, String fileName) {
    String resourceName = String.format("%s/%s", assetDirectory, fileName);
    return plugin.getClass().getResourceAsStream(resourceName);
  }

  private PluginRegistry getPluginRegistry() {
    return Cockpit.getRuntimeDelegate().getPluginRegistry();
  }
}
