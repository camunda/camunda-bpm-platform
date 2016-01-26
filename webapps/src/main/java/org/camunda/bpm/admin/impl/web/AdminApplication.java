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
package org.camunda.bpm.admin.impl.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.camunda.bpm.admin.Admin;
import org.camunda.bpm.admin.plugin.spi.AdminPlugin;
import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthenticationResource;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * The rest api exposed by the admin application
 *
 * @author Daniel Meyer
 */
public class AdminApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(JacksonConfigurator.class);
    classes.add(JacksonJsonProvider.class);
    classes.add(RestExceptionHandler.class);
    classes.add(ExceptionHandler.class);

    classes.add(UserAuthenticationResource.class);
    classes.add(SetupResource.class);

    addPluginResourceClasses(classes);

    return classes;
  }


  private void addPluginResourceClasses(Set<Class<?>> classes) {

    List<AdminPlugin> plugins = getPlugins();

    for (AdminPlugin plugin : plugins) {
      classes.addAll(plugin.getResourceClasses());
    }
  }

  private List<AdminPlugin> getPlugins() {
    return Admin.getRuntimeDelegate().getAppPluginRegistry().getPlugins();
  }

}
