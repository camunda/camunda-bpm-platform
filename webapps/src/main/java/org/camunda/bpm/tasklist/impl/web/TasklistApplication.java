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
package org.camunda.bpm.tasklist.impl.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.tasklist.Tasklist;
import org.camunda.bpm.tasklist.plugin.spi.TasklistPlugin;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * @author Roman Smirnov
 *
 */
public class TasklistApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(JacksonConfigurator.class);
    classes.add(JacksonJsonProvider.class);
    classes.add(ExceptionHandler.class);
    classes.add(RestExceptionHandler.class);

    addPluginResourceClasses(classes);

    return classes;
  }

  private void addPluginResourceClasses(Set<Class<?>> classes) {

    List<TasklistPlugin> plugins = getTasklistPlugins();

    for (TasklistPlugin plugin : plugins) {
      classes.addAll(plugin.getResourceClasses());
    }
  }

  private List<TasklistPlugin> getTasklistPlugins() {
    return Tasklist.getRuntimeDelegate().getAppPluginRegistry().getPlugins();
  }

}
