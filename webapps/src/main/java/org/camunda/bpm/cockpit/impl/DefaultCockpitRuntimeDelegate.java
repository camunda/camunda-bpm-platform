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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.cockpit.CockpitRuntimeDelegate;
import org.camunda.bpm.cockpit.db.CommandExecutor;
import org.camunda.bpm.cockpit.db.QueryService;
import org.camunda.bpm.cockpit.impl.db.CommandExecutorImpl;
import org.camunda.bpm.cockpit.impl.db.QueryServiceImpl;
import org.camunda.bpm.cockpit.impl.plugin.DefaultPluginRegistry;
import org.camunda.bpm.cockpit.plugin.PluginRegistry;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.webapp.impl.AbstractAppRuntimeDelegate;

/**
 * <p>This is the default {@link CockpitRuntimeDelegate} implementation that provides
 * the camunda cockpit plugin services (i.e. {@link QueryService} and
 * {@link CommandExecutor}).</p>
 *
 * @author roman.smirnov
 * @author nico.rehwaldt
 */
public class DefaultCockpitRuntimeDelegate extends AbstractAppRuntimeDelegate<CockpitPlugin> implements CockpitRuntimeDelegate {

  private  Map<String, CommandExecutor> commandExecutors;

  public DefaultCockpitRuntimeDelegate() {
    super(CockpitPlugin.class);
    this.commandExecutors = new HashMap<String, CommandExecutor>();
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

  /**
   * Deprecated: use {@link #getAppPluginRegistry()}
   */
  @Deprecated
  public PluginRegistry getPluginRegistry() {
    return new DefaultPluginRegistry(pluginRegistry);
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

}
