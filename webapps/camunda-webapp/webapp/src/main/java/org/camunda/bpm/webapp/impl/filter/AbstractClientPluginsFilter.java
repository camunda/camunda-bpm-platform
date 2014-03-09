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
package org.camunda.bpm.webapp.impl.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.webapp.AppRuntimeDelegate;
import org.camunda.bpm.webapp.plugin.spi.AppPlugin;

/**
 * A filter that injects the environment variables <code>PLUGIN_DEPENDENCIES</code>
 * and <code>PLUGIN_PACKAGES</code> in the correct places of the cockpit client
 * application.
 *
 * <p>
 *
 * Must be configured appropriately in an applications <code>web.xml</code>.
 *
 * @author nico.rehwaldt
 */
public abstract class AbstractClientPluginsFilter<T extends AppPlugin> extends AbstractTemplateFilter {

  private final String PLUGIN_DEPENDENCIES = "window.PLUGIN_DEPENDENCIES";
  private final String PLUGIN_PACKAGES = "window.PLUGIN_PACKAGES";

  // accepts two times the plugin name
  protected final String pluginPackageFormat;

  // accepts two times the plugin name
  protected final String pluginDependencyFormat;

  protected final AppRuntimeDelegate<T> runtimeDelegate;
  protected final String appName;

  public AbstractClientPluginsFilter(String appName, AppRuntimeDelegate<T> runtimeDelegate) {
    this.runtimeDelegate = runtimeDelegate;
    this.appName = appName;

    this.pluginPackageFormat = "{ name: '"+appName+"-plugin-%s', location: 'api/"+appName+"/plugin/%s/static/app', main: 'plugin.js' }";
    this.pluginDependencyFormat = "'module:"+appName+".plugin.%s:"+appName+"-plugin-%s'";
  }

  @Override
  protected void applyFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String data = getWebResourceContents(request.getRequestURI().replaceFirst(request.getContextPath(), ""));

    data = data.replace(PLUGIN_PACKAGES, createPluginPackagesStr());

    data = data.replace(PLUGIN_DEPENDENCIES, createPluginDependenciesStr());

    response.setContentLength(data.getBytes("UTF-8").length);
    response.setContentType("text/javascript;charset=UTF-8");

    response.getWriter().append(data);
  }

  protected CharSequence createPluginPackagesStr() {
    final List<T> plugins = getPlugins();

    StringBuilder builder = new StringBuilder();

    for (T plugin : plugins) {
      if (builder.length() > 0) {
        builder.append(", ").append("\n");
      }

      String definition = String.format(pluginPackageFormat, plugin.getId(), plugin.getId());

      builder.append(definition);
    }

    return "[" + builder.toString() + "]";
  }

  protected List<T> getPlugins() {
    return runtimeDelegate.getAppPluginRegistry().getPlugins();
  }

  protected CharSequence createPluginDependenciesStr() {
    final List<T> plugins = getPlugins();

    StringBuilder builder = new StringBuilder();

    for (T plugin : plugins) {
      if (builder.length() > 0) {
        builder.append(", ").append("\n");
      }

      String definition = String.format(pluginDependencyFormat, plugin.getId(), plugin.getId());

      builder.append(definition);
    }

    return "[" + builder.toString() + "]";
  }

}