/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.cfg;


import org.camunda.bpm.engine.ProcessEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * {@link ProcessEnginePlugin} that provides composite behavior. When registered on an engine configuration,
 * all plugins added to this composite will be triggered on preInit/postInit/postProcessEngineBuild.
 * <p>
 * Use to encapsulate common behavior (like engine configuration).
 */
public class CompositeProcessEnginePlugin extends AbstractProcessEnginePlugin {

  protected final List<ProcessEnginePlugin> plugins;

  /**
   * New instance with empty list.
   */
  public CompositeProcessEnginePlugin() {
    this.plugins = new ArrayList<ProcessEnginePlugin>();
  }


  /**
   * New instance with vararg.
   * @param plugin first plugin
   * @param additionalPlugins additional vararg plugins
   */
  public CompositeProcessEnginePlugin(ProcessEnginePlugin plugin, ProcessEnginePlugin... additionalPlugins) {
    this();
    addProcessEnginePlugin(plugin, additionalPlugins);
  }

  /**
   * New instance with initial plugins.
   *
   * @param plugins the initial plugins. Must not be null.
   */
  public CompositeProcessEnginePlugin(final List<ProcessEnginePlugin> plugins) {
    this();
    addProcessEnginePlugins(plugins);
  }

  /**
   * Add one (or more) plugins.
   *
   * @param plugin first plugin
   * @param additionalPlugins additional vararg plugins
   * @return self for fluent usage
   */
  public CompositeProcessEnginePlugin addProcessEnginePlugin(ProcessEnginePlugin plugin, ProcessEnginePlugin... additionalPlugins) {
    return this.addProcessEnginePlugins(toList(plugin, additionalPlugins));
  }

  /**
   * Add collection of plugins.
   *
   * If collection is not sortable, order of plugin execution can not be guaranteed.
   *
   * @param plugins plugins to add
   * @return self for fluent usage
   */
  public CompositeProcessEnginePlugin addProcessEnginePlugins(final Collection<ProcessEnginePlugin> plugins) {
    this.plugins.addAll(plugins);

    return this;
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    for (ProcessEnginePlugin plugin : plugins) {
      plugin.preInit(processEngineConfiguration);
    }
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    for (ProcessEnginePlugin plugin : plugins) {
      plugin.postInit(processEngineConfiguration);
    }
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    for (ProcessEnginePlugin plugin : plugins) {
      plugin.postProcessEngineBuild(processEngine);
    }
  }

  /**
   * Get all plugins.
   *
   * @return the configured plugins
   */
  public List<ProcessEnginePlugin> getPlugins() {
    return plugins;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + plugins;
  }


  private static List<ProcessEnginePlugin> toList(ProcessEnginePlugin plugin, ProcessEnginePlugin... additionalPlugins) {
    final List<ProcessEnginePlugin> plugins = new ArrayList<ProcessEnginePlugin>();
    plugins.add(plugin);
    if (additionalPlugins != null && additionalPlugins.length > 0) {
      plugins.addAll(Arrays.asList(additionalPlugins));
    }
    return plugins;
  }
}
