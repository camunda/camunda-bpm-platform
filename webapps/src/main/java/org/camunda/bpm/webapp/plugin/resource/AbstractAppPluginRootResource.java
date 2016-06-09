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
package org.camunda.bpm.webapp.plugin.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.webapp.AppRuntimeDelegate;
import org.camunda.bpm.webapp.plugin.spi.AppPlugin;
import org.camunda.bpm.engine.impl.util.IoUtil;

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
 * {@link AbstractAppPluginRootResource#subResource(AbstractAppPluginResource, String) }.
 *
 * <pre>
 * @Path("myplugin")
 * public class MyPluginRootResource extends AbstractAppPluginRootResource {
 *
 *   @Path("{engine}/my-resource")
 *   public FooResource getFooResource(@PathParam("engine") String engine) {
 *     return subResource(new FooResource(engine), engine);
 *   }
 * }
 * </pre>
 *
 * @author nico.rehwaldt
 * @author Daniel Meyer
 */
public class AbstractAppPluginRootResource<T extends AppPlugin> {

  public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
  public static final String MIME_TYPE_TEXT_HTML = "text/html";
  public static final String MIME_TYPE_TEXT_CSS = "text/css";
  public static final String MIME_TYPE_TEXT_JAVASCRIPT = "text/javascript";

  @Context
  private ServletContext servletContext;

  @Context
  private HttpHeaders headers;

  @Context
  private UriInfo uriInfo;

  private final String pluginName;

  protected AppRuntimeDelegate<T> runtimeDelegate;

  public AbstractAppPluginRootResource(String pluginName, AppRuntimeDelegate<T> runtimeDelegate) {
    this.pluginName = pluginName;
    this.runtimeDelegate = runtimeDelegate;
  }

  /**
   *
   * @param <T>
   * @param subResource
   * @param engineName
   * @return
   */
  protected <S extends AbstractAppPluginResource<T>> S subResource(S subResource, String engineName) {
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

    AppPlugin plugin = runtimeDelegate.getAppPluginRegistry().getPlugin(pluginName);

    if (plugin != null) {
      InputStream assetStream = getPluginAssetAsStream(plugin, file);
      final InputStream filteredStream = applyResourceOverrides(file, assetStream);

      if (assetStream != null) {
        String contentType = getContentType(file);
        return Response.ok(new StreamingOutput() {

          @Override
          public void write(OutputStream out) throws IOException, WebApplicationException {

            try {
              byte[] buff = new byte[16 * 1000];
              int read = 0;
              while((read = filteredStream.read(buff)) > 0) {
                out.write(buff, 0, read);
              }
            }
            finally {
              IoUtil.closeSilently(filteredStream);
              IoUtil.closeSilently(out);
            }

          }
        }, contentType).build();
      }
    }

    // no asset found
    throw new RestException(Status.NOT_FOUND, "It was not able to load the following file '" + file + "'.");
  }

  /**
   * @param file
   * @param assetStream
   */
  protected InputStream applyResourceOverrides(String file, InputStream assetStream) {
    // use a copy of the list cause it could be modified during iteration
    List<PluginResourceOverride> resourceOverrides = new ArrayList<PluginResourceOverride>(runtimeDelegate.getResourceOverrides());
    for (PluginResourceOverride pluginResourceOverride : resourceOverrides) {
      assetStream = pluginResourceOverride.filterResource(assetStream, new RequestInfo(headers, servletContext, uriInfo));
    }
    return assetStream;
  }

  protected String getContentType(String file) {
    if (file.endsWith(".js")) {
      return MIME_TYPE_TEXT_JAVASCRIPT;
    } else
    if (file.endsWith(".html")) {
      return MIME_TYPE_TEXT_HTML;
    } else
    if (file.endsWith(".css")) {
      return MIME_TYPE_TEXT_CSS;
    } else {
      return MIME_TYPE_TEXT_PLAIN;
    }
  }

  /**
   * Returns an input stream for a given resource
   *
   * @param resourceName
   * @return
   */
  protected InputStream getPluginAssetAsStream(AppPlugin plugin, String fileName) {

    String assetDirectory = plugin.getAssetDirectory();

    if (assetDirectory == null) {
      return null;
    }

    InputStream result = getWebResourceAsStream(assetDirectory, fileName);

    if (result == null) {
      result = getClasspathResourceAsStream(plugin, assetDirectory, fileName);
    }
    return result;
  }

  protected InputStream getWebResourceAsStream(String assetDirectory, String fileName) {
    String resourceName = String.format("/%s/%s", assetDirectory, fileName);

    return servletContext.getResourceAsStream(resourceName);
  }

  protected InputStream getClasspathResourceAsStream(AppPlugin plugin, String assetDirectory, String fileName) {
    String resourceName = String.format("%s/%s", assetDirectory, fileName);
    return plugin.getClass().getClassLoader().getResourceAsStream(resourceName);
  }

}
