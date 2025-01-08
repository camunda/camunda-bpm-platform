/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.scripting.engine;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines.GRAAL_JS_SCRIPT_ENGINE_NAME;

/**
 * Custom Script Engine Manager that can execute custom logic:
 * <p>
 * a) after the discovery of the engines on the classpath; the respective engine factories are created
 * b) before the engines are created.
 *
 * If custom logic is needed for a specific engine after the classpath detection, before the engine creation,
 * it can be added to the classes map.
 */
public class CamundaScriptEngineManager extends ScriptEngineManager {

  protected final Map<String, Runnable> engineNameToInitLogicMappings = Map.of(
      GRAAL_JS_SCRIPT_ENGINE_NAME, this::disableGraalVMInterpreterOnlyModeWarnings
  );

  public CamundaScriptEngineManager() {
    super(); // creates engine factories after classpath discovery
    applyConfigOnEnginesAfterClasspathDiscovery();
  }

  protected void applyConfigOnEnginesAfterClasspathDiscovery() {
    var engineNames = getEngineNamesFoundInClasspath();

    for (var engineName : engineNames) {
      executeConfigurationBeforeEngineCreation(engineName);
    }
  }

  protected List<String> getEngineNamesFoundInClasspath() {
    var engineFactories = getEngineFactories();

    return engineFactories.stream()
        .map(ScriptEngineFactory::getEngineName)
        .collect(Collectors.toList());

  }

  /**
   * Fetches the config logic of a given engine from the mappings and executes it in case it exists.
   *
   * @param engineName the given engine name
   */
  protected void executeConfigurationBeforeEngineCreation(String engineName) {
    var config = engineNameToInitLogicMappings.get(engineName);
    if (config != null) {
      config.run();
    }
  }

  protected void disableGraalVMInterpreterOnlyModeWarnings() {
    System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
  }

}