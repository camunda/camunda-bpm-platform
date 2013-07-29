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
package org.camunda.bpm.cockpit.impl.web.filter.plugin;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.webapp.impl.filter.AbstractTemplateFilter;

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
public class ClientPluginsFilter extends AbstractTemplateFilter {

  // accepts two times the plugin name
  private static final String PLUGIN_PACKAGE_FORMAT = "{ name: 'cockpit-plugin-%s', location: 'api/cockpit/plugin/%s/static/app', main: 'plugin.js' }";

  // accepts two times the plugin name
  private static final String PLUGIN_DEPENDENCY_FORMAT = "'module:cockpit.plugin.%s:cockpit-plugin-%s'";

  private final String PLUGIN_DEPENDENCIES = "window.PLUGIN_DEPENDENCIES";
  private final String PLUGIN_PACKAGES = "window.PLUGIN_PACKAGES";

  @Override
  protected void applyFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String data = getWebResourceContents(request.getRequestURI().replaceFirst(request.getContextPath(), ""));

    data = data.replace(PLUGIN_PACKAGES, createPluginPackagesStr());

    data = data.replace(PLUGIN_DEPENDENCIES, createPluginDependenciesStr());

    response.setContentLength(data.getBytes("UTF-8").length);
    response.setContentType("text/javascript;charset=UTF-8");

    response.getWriter().append(data);
  }

  private CharSequence createPluginPackagesStr() {
    final List<CockpitPlugin> plugins = getCockpitPlugins();

    StringBuilder builder = new StringBuilder();

    for (CockpitPlugin plugin : plugins) {
      if (builder.length() > 0) {
        builder.append(", ").append("\n");
      }

      String definition = String.format(PLUGIN_PACKAGE_FORMAT, plugin.getId(), plugin.getId());

      builder.append(definition);
    }

    return "[" + builder.toString() + "]";
  }

  private CharSequence createPluginDependenciesStr() {
    final List<CockpitPlugin> plugins = getCockpitPlugins();

    StringBuilder builder = new StringBuilder();

    for (CockpitPlugin plugin : plugins) {
      if (builder.length() > 0) {
        builder.append(", ").append("\n");
      }

      String definition = String.format(PLUGIN_DEPENDENCY_FORMAT, plugin.getId(), plugin.getId());

      builder.append(definition);
    }

    return "[" + builder.toString() + "]";
  }

  private List<CockpitPlugin> getCockpitPlugins() {
    return Cockpit.getRuntimeDelegate().getPluginRegistry().getPlugins();
  }
}