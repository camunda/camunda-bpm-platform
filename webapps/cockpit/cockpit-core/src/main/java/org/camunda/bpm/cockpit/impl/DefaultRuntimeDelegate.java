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
package org.camunda.bpm.cockpit.impl;

import org.camunda.bpm.cockpit.impl.plugin.DefaultPluginRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.Set;

import org.camunda.bpm.cockpit.CockpitRuntimeDelegate;
import org.camunda.bpm.cockpit.plugin.PluginRegistry;
import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.cockpit.impl.db.CommandExecutorImpl;
import org.camunda.bpm.cockpit.impl.db.QueryServiceImpl;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

/**
 * <p>This is the default {@link CockpitRuntimeDelegate} implementation that provides
 * the camunda cockpit plugin services (i.e. {@link QueryService} and
 * {@link CommandExecutor}).</p>
 *
 * @author roman.smirnov
 * @author nico.rehwaldt
 */
public class DefaultRuntimeDelegate implements CockpitRuntimeDelegate {

  private final PluginRegistry pluginRegistry;

  private final ProcessEngineProvider processEngineProvider;

  private  Map<String, CommandExecutor> commandExecutors;

  public DefaultRuntimeDelegate() {

    this.pluginRegistry = new DefaultPluginRegistry();
    this.commandExecutors = new HashMap<String, CommandExecutor>();
    this.processEngineProvider = loadProcessEngineProvider();
  }

  @Override
  public QueryService getQueryService(String processEngineName) {
    CommandExecutor commandExecutor = getCommandExecutor(processEngineName);
    return new QueryServiceImpl(commandExecutor);
  }

  @Override
  public CommandExecutor getCommandExecutor(String processEngineName) {

    CommandExecutor commandExecutor = commandExecutors.get(processEngineName);
    if (commandExecutor == null) {
      commandExecutor = createCommandExecutor(processEngineName);
      commandExecutors.put(processEngineName, commandExecutor);
    }

    return commandExecutor;
  }

  @Override
  public PluginRegistry getPluginRegistry() {
    return pluginRegistry;
  }

  @Override
  public ProcessEngine getProcessEngine(String processEngineName) {
    try {
      return processEngineProvider.getProcessEngine(processEngineName);
    } catch (Exception e) {
      throw new ProcessEngineException("No process engine with name " + processEngineName + " found.", e);
    }
  }

  @Override
  public Set<String> getProcessEngineNames() {
    return processEngineProvider.getProcessEngineNames();
  }

  @Override
  public ProcessEngine getDefaultProcessEngine() {
    return processEngineProvider.getDefaultProcessEngine();
  }

  /**
   * Returns the list of mapping files that should be used to create the
   * session factory for this runtime.
   *
   * @return
   */
  protected List<String> getMappingFiles() {
    List<CockpitPlugin> cockpitPlugins = pluginRegistry.getPlugins();

    List<String> mappingFiles = new ArrayList<String>();
    for (CockpitPlugin plugin: cockpitPlugins) {
      mappingFiles.addAll(plugin.getMappingFiles());
    }

    return mappingFiles;
  }

  /**
   * Create command executor for the engine with the given name
   *
   * @param processEngineName
   * @return
   */
  protected CommandExecutor createCommandExecutor(String processEngineName) {

    ProcessEngine processEngine = getProcessEngine(processEngineName);
    if (processEngine == null) {
      throw new ProcessEngineException("No process engine with name " + processEngineName + " found.");
    }

    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration();
    List<String> mappingFiles = getMappingFiles();

    return new CommandExecutorImpl(processEngineConfiguration, mappingFiles);
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
}
