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
package org.camunda.bpm.webapp.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.webapp.AppRuntimeDelegate;
import org.camunda.bpm.webapp.plugin.AppPluginRegistry;
import org.camunda.bpm.webapp.plugin.impl.DefaultAppPluginRegistry;
import org.camunda.bpm.webapp.plugin.resource.PluginResourceOverride;
import org.camunda.bpm.webapp.plugin.spi.AppPlugin;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractAppRuntimeDelegate<T extends AppPlugin> implements AppRuntimeDelegate<T> {

  protected final AppPluginRegistry<T> pluginRegistry;
  protected final ProcessEngineProvider processEngineProvider;

  protected List<PluginResourceOverride> resourceOverrides;

  public AbstractAppRuntimeDelegate(Class<T> pluginType) {
    pluginRegistry = new DefaultAppPluginRegistry<T>(pluginType);
    processEngineProvider = loadProcessEngineProvider();
  }

  public ProcessEngine getProcessEngine(String processEngineName) {
    try {
      return processEngineProvider.getProcessEngine(processEngineName);
    } catch (Exception e) {
      throw new ProcessEngineException("No process engine with name " + processEngineName + " found.", e);
    }
  }

  public Set<String> getProcessEngineNames() {
    return processEngineProvider.getProcessEngineNames();
  }

  public ProcessEngine getDefaultProcessEngine() {
    return processEngineProvider.getDefaultProcessEngine();
  }

  public AppPluginRegistry<T> getAppPluginRegistry() {
    return pluginRegistry;
  }

  /**
   * Load the {@link ProcessEngineProvider} spi implementation.
   *
   * @return
   */
  protected ProcessEngineProvider loadProcessEngineProvider() {
    ServiceLoader<ProcessEngineProvider> loader = ServiceLoader.load(ProcessEngineProvider.class);

    try {
      return loader.iterator().next();
    } catch (NoSuchElementException e) {
      String message = String.format("No implementation for the %s spi found on classpath", ProcessEngineProvider.class.getName());
      throw new IllegalStateException(message, e);
    }
  }

  public List<PluginResourceOverride> getResourceOverrides() {
    if(resourceOverrides == null) {
      initResourceOverrides();
    }
    return resourceOverrides;
  }

  protected synchronized void initResourceOverrides() {
    if(resourceOverrides == null) { // double-checked sync, do not remove
      resourceOverrides = new ArrayList<PluginResourceOverride>();
      List<T> plugins = pluginRegistry.getPlugins();
      for (T p : plugins) {
        resourceOverrides.addAll(p.getResourceOverrides());
      }
    }
  }

}
