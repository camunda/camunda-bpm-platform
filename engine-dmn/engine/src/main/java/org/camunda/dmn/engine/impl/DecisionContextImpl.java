/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.dmn.engine.impl;

import java.util.HashMap;
import java.util.Map;

import org.camunda.dmn.engine.DmnDecisionContext;
import org.camunda.dmn.engine.DmnEngineConfiguration;
import org.camunda.dmn.engine.ScriptEngineContext;

public class DecisionContextImpl implements DmnDecisionContext {

  protected static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected DmnEngineConfiguration configuration;
  protected ScriptEngineContext scriptEngineContext;
  protected Map<String, Object> variables = new HashMap<String, Object>();

  public void setConfiguration(DmnEngineConfiguration configuration) {
    this.configuration = configuration;
  }

  public DmnEngineConfiguration getConfiguration() {
    return configuration;
  }

  public DmnEngineConfiguration getConfigurationChecked() {
    if (configuration != null) {
      return configuration;
    }
    else {
      throw LOG.notConfigurationSetInContext();
    }
  }

  public void setScriptEngineContext(ScriptEngineContext scriptEngineContext) {
    this.scriptEngineContext = scriptEngineContext;
  }

  public ScriptEngineContext getScriptEngineContext() {
    return scriptEngineContext;
  }

  public ScriptEngineContext getScriptEngineContextChecked() {
    if (scriptEngineContext != null) {
      return scriptEngineContext;
    }
    else {
      throw LOG.notScriptEngineContextSetInContext();
    }
  }

  public void addVariable(String name, Object value) {
    variables.put(name, value);
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  @SuppressWarnings("unchecked")
  public <T> T getVariable(String name) {
    return (T) variables.get(name);
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

}
