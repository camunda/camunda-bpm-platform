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
package org.camunda.bpm.cockpit.plugin.core.resource;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.cockpit.plugin.core.Registry;
import org.camunda.bpm.cockpit.plugin.core.spi.CockpitPlugin;

/**
 * A resource class that may be implemented as a root resource
 * that constitutes a plugins restful API.
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

  @GET
  @Path("/static/{file:.*}")
  public InputStream getAsset(@PathParam("file") String file) {

    CockpitPlugin plugin = Registry.getCockpitPlugin(pluginName);

    if (plugin != null) {
      String assetDirectory = plugin.getAssetDirectory();
      if (assetDirectory != null) {
        String assetName = String.format("%s/%s", assetDirectory, file);

        InputStream assetStream = getAssetAsStream(plugin, assetName);
        if (assetStream != null) {
          return assetStream;
        }
      }
    }

    // no asset found
    throw new WebApplicationException(Status.NOT_FOUND);
  }

  /**
   * Returns an input stream for a given resource
   *
   * @param resourceName
   * @return
   */
  private InputStream getAssetAsStream(CockpitPlugin plugin, String resourceName) {

    return plugin.getClass().getResourceAsStream(resourceName);
  }

}
