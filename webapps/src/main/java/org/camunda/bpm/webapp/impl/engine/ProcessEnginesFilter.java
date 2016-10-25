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
package org.camunda.bpm.webapp.impl.engine;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.admin.Admin;
import org.camunda.bpm.admin.AdminRuntimeDelegate;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.CockpitRuntimeDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.tasklist.Tasklist;
import org.camunda.bpm.tasklist.TasklistRuntimeDelegate;
import org.camunda.bpm.webapp.impl.IllegalWebAppConfigurationException;
import org.camunda.bpm.webapp.impl.filter.AbstractTemplateFilter;
import org.camunda.bpm.webapp.impl.security.SecurityActions;
import org.camunda.bpm.webapp.impl.security.SecurityActions.SecurityAction;
import org.camunda.bpm.webapp.plugin.spi.AppPlugin;
import org.camunda.bpm.welcome.Welcome;
import org.camunda.bpm.welcome.WelcomeRuntimeDelegate;

/**
 *
 * @author nico.rehwaldt
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Stamm
 *
 */
public class ProcessEnginesFilter extends AbstractTemplateFilter {

  protected static final String COCKPIT_APP_NAME = "cockpit";
  protected static final String ADMIN_APP_NAME = "admin";
  protected static final String TASKLIST_APP_NAME = "tasklist";
  protected static final String WELCOME_APP_NAME = "welcome";

  protected static final String DEFAULT_APP = WELCOME_APP_NAME;
  protected static final String INDEX_PAGE = "index.html";

  protected static final String SETUP_PAGE = "setup/";

  public static final String APP_ROOT_PLACEHOLDER = "$APP_ROOT";
  public static final String BASE_PLACEHOLDER = "$BASE";

  public static final String PLUGIN_DEPENDENCIES_PLACEHOLDER = "$PLUGIN_DEPENDENCIES";
  public static final String PLUGIN_PACKAGES_PLACEHOLDER = "$PLUGIN_PACKAGES";

  public static Pattern APP_PREFIX_PATTERN = Pattern.compile("/app/(?:([\\w-]+?)/(?:(index\\.html|[\\w-]+)?/?([^?]*)?)?)?");

  protected final CockpitRuntimeDelegate cockpitRuntimeDelegate;
  protected final AdminRuntimeDelegate adminRuntimeDelegate;
  protected final TasklistRuntimeDelegate tasklistRuntimeDelegate;
  protected final WelcomeRuntimeDelegate welcomeRuntimeDelegate;

  // accepts two times the plugin name
  protected final String pluginPackageFormat;

  // accepts two times the plugin name
  protected final String pluginDependencyFormat;

  public ProcessEnginesFilter() {
    this.cockpitRuntimeDelegate = Cockpit.getRuntimeDelegate();
    this.adminRuntimeDelegate = Admin.getRuntimeDelegate();
    this.tasklistRuntimeDelegate = Tasklist.getRuntimeDelegate();
    this.welcomeRuntimeDelegate = Welcome.getRuntimeDelegate();
    this.pluginPackageFormat = "{ name: '%s-plugin-%s', location: '%s/api/%s/plugin/%s/static/app', main: 'plugin.js' }";
    this.pluginDependencyFormat = "{ ngModuleName: '%s.plugin.%s', requirePackageName: '%s-plugin-%s' }";
  }

  @Override
  protected void applyFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    String contextPath = request.getContextPath();
    String requestUri = request.getRequestURI().substring(contextPath.length());

    Matcher uriMatcher = APP_PREFIX_PATTERN.matcher(requestUri);

    if (uriMatcher.matches()) {
      String appName = uriMatcher.group(1);
      String engineName = uriMatcher.group(2);
      String pageUri = uriMatcher.group(3);

      // this happens on weblogic - /app/cockpit/index.html
      if (INDEX_PAGE.equals(engineName)) {
        engineName = null;
      }

      if (pageUri == null || pageUri.isEmpty() || SETUP_PAGE.equals(pageUri)) {
        serveIndexPage(appName, engineName, pageUri, contextPath, request, response, chain);
        return;
      }

      if (INDEX_PAGE.equals(pageUri)) {
        response.sendRedirect(String.format("%s/app/%s/%s/", contextPath, appName, engineName));
        return;
      }

      if (pageUri.endsWith(".html")) {
        serveTemplate(requestUri, appName, pageUri, request, response, chain);
        return;
      }

    }

    chain.doFilter(request, response);
  }

  protected void serveIndexPage(String appName, String engineName, String pageUri, String contextPath, HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {

    // access to /
    if (appName == null) {

      // redirect to {defaultApp}/{defaultEngineName}
      response.sendRedirect(String.format("%s/app/%s/%s/", contextPath, DEFAULT_APP, getDefaultEngineName()));
    } else

    // access to /app/
    // redirect to /app/{defaultEngineName}/
    if (engineName == null) {
      // redirect to {defaultApp}/{defaultEngineName}
      response.sendRedirect(String.format("%s/app/%s/%s/", contextPath, appName, getDefaultEngineName()));
    } else {

      // access to /app/{engineName}/
      boolean setupPage = SETUP_PAGE.equals(pageUri);

      if (needsInitialUser(engineName)) {
        if (!setupPage) {
          // redirect to setup
          response.sendRedirect(String.format("%s/app/admin/%s/setup/#/setup", contextPath, engineName));
        } else {
          // serve the index page as a setup page
          // setup will be handled by app
          serveIndexPage(appName, engineName, contextPath, request, response);
        }
      } else {
        if (!setupPage) {
          // correctly serving index page
          serveIndexPage(appName, engineName, contextPath, request, response);
        } else {
          response.sendRedirect(String.format("%s/app/%s/%s/", contextPath, appName, engineName));
        }
      }
    }
  }

  protected String getDefaultEngineName() {
    CockpitRuntimeDelegate runtimeDelegate = Cockpit.getRuntimeDelegate();

    Set<String> processEngineNames = runtimeDelegate.getProcessEngineNames();
    if(processEngineNames.isEmpty()) {
      throw new IllegalWebAppConfigurationException("No process engine found. camunda Webapp cannot work without a process engine. ");

    } else {
      ProcessEngine defaultProcessEngine = runtimeDelegate.getDefaultProcessEngine();
      if(defaultProcessEngine != null) {
        return defaultProcessEngine.getName();

      } else {
        return processEngineNames.iterator().next();

      }
    }
  }

  protected void serveTemplate(String requestUri, String appName, String pageUri, HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

    // check if resource exists
    if (hasWebResource(requestUri)) {

      // if so, include it
      chain.doFilter(request, response);
    } else {
      // strip engine namespace and check if resource would exist
      String cleanAppUri = String.format("/app/%s/%s", appName, pageUri);

      if (hasWebResource(cleanAppUri)) {
        request.getRequestDispatcher(cleanAppUri).forward(request, response);
      } else {
        chain.doFilter(request, response);
      }
    }
  }

  protected boolean needsInitialUser(String engineName) {
    final ProcessEngine processEngine = Cockpit.getProcessEngine(engineName);
    if (processEngine == null) {
      return false;
    }

    if (processEngine.getIdentityService().isReadOnly()) {
      return false;

    } else {

      return SecurityActions.runWithoutAuthentication(new SecurityAction<Boolean>() {
        public Boolean execute() {
          return processEngine.getIdentityService()
              .createUserQuery()
              .memberOfGroup(Groups.CAMUNDA_ADMIN).count() == 0;
        }
      }, processEngine);

    }

  }

  protected void serveIndexPage(String appName, String engineName, String contextPath, HttpServletRequest request, HttpServletResponse response) throws IOException {
    String data = getWebResourceContents("/app/" + appName + "/index.html");

    data = replacePlaceholder(data, appName, engineName, contextPath, request, response);

    response.setContentLength(data.getBytes("UTF-8").length);
    response.setContentType("text/html");

    response.getWriter().append(data);
  }

  protected String replacePlaceholder(String data, String appName, String engineName, String contextPath, HttpServletRequest request, HttpServletResponse response) {
    return data.replace(APP_ROOT_PLACEHOLDER, contextPath)
               .replace(BASE_PLACEHOLDER, String.format("%s/app/%s/%s/", contextPath, appName, engineName))
               .replace(PLUGIN_PACKAGES_PLACEHOLDER, createPluginPackagesStr(appName, contextPath))
               .replace(PLUGIN_DEPENDENCIES_PLACEHOLDER, createPluginDependenciesStr(appName));
  }

  protected <T extends AppPlugin> CharSequence createPluginPackagesStr(String appName, String contextPath) {
    final List<T> plugins = getPlugins(appName);

    StringBuilder builder = new StringBuilder();

    for (T plugin : plugins) {
      if (builder.length() > 0) {
        builder.append(", ").append("\n");
      }

      String pluginId = plugin.getId();
      String definition = String.format(pluginPackageFormat, appName, pluginId, contextPath, appName, pluginId);

      builder.append(definition);
    }

    return "[" + builder.toString() + "]";
  }

  protected <T extends AppPlugin> CharSequence createPluginDependenciesStr(String appName) {
    final List<T> plugins = getPlugins(appName);

    StringBuilder builder = new StringBuilder();

    for (T plugin : plugins) {
      if (builder.length() > 0) {
        builder.append(", ").append("\n");
      }

      String pluginId = plugin.getId();
      String definition = String.format(pluginDependencyFormat, appName, pluginId, appName, pluginId);

      builder.append(definition);
    }

    return "[" + builder.toString() + "]";
  }

  @SuppressWarnings("unchecked")
  protected <T extends AppPlugin> List<T> getPlugins(String appName) {
    if (COCKPIT_APP_NAME.equals(appName)) {
      return (List<T>) cockpitRuntimeDelegate.getAppPluginRegistry().getPlugins();

    } else if (ADMIN_APP_NAME.equals(appName)) {
      return (List<T>) adminRuntimeDelegate.getAppPluginRegistry().getPlugins();

    } else if (TASKLIST_APP_NAME.equals(appName)) {
        return (List<T>) tasklistRuntimeDelegate.getAppPluginRegistry().getPlugins();

    } else if (WELCOME_APP_NAME.equals(appName)) {
      return (List<T>) welcomeRuntimeDelegate.getAppPluginRegistry().getPlugins();

    } else {
      return Collections.emptyList();

    }
  }

}
