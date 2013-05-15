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
import java.util.List;

import org.camunda.bpm.BpmPlatform;
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

  public DefaultRuntimeDelegate() {
    this.pluginRegistry = new DefaultPluginRegistry();
  }

  @Override
  public QueryService getQueryService(String processEngineName) {
    CommandExecutor commandExecutor = getCommandExecutor(processEngineName);
    return new QueryServiceImpl(commandExecutor);
  }

  @Override
  public CommandExecutor getCommandExecutor(String processEngineName) {
    ProcessEngine processEngine = getProcessEngine(processEngineName);
    if (processEngine == null) {
      throw new ProcessEngineException("No process engine with name " + processEngineName + " found.");
    }

    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration();
    List<String> mappingFiles = getMappingFiles();

    CommandExecutor commandExecutor = new CommandExecutorImpl(processEngineConfiguration, mappingFiles);

    return commandExecutor;
  }

  protected List<String> getMappingFiles() {
    List<CockpitPlugin> cockpitPlugins = pluginRegistry.getPlugins();

    List<String> mappingFiles = new ArrayList<String>();
    for (CockpitPlugin plugin: cockpitPlugins) {
      mappingFiles.addAll(plugin.getMappingFiles());
    }

    return mappingFiles;
  }

  @Override
  public PluginRegistry getPluginRegistry() {
    return pluginRegistry;
  }

  @Override
  public ProcessEngine getProcessEngine(String processEngineName) {
    try {
      return BpmPlatform.getProcessEngineService().getProcessEngine(processEngineName);
    } catch (Exception e) {
      throw new ProcessEngineException("No process engine with name " + processEngineName + " found.", e);
    }
  }
}
