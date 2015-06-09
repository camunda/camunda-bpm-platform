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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.camunda.dmn.engine.DmnEngineConfiguration;
import org.camunda.dmn.engine.ScriptEngineContext;

public class ScriptEngineContextImpl implements ScriptEngineContext {

  protected static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  public static final String DEFAULT_SCRIPTING_LANGUAGE = "juel";

  protected ScriptEngineManager scriptEngineManager;
  protected String defaultScriptingLanguage = null;
  protected Map<String, ScriptEngine> cachedEngines = new HashMap<String, ScriptEngine>();

  public ScriptEngineContextImpl(ScriptEngineManager scriptEngineManager) {
    this.scriptEngineManager = scriptEngineManager;
    defaultScriptingLanguage = DEFAULT_SCRIPTING_LANGUAGE;
  }

  public ScriptEngineContextImpl(DmnEngineConfiguration engineConfiguration) {
    this(new ScriptEngineManager());
    if (engineConfiguration != null) {
      defaultScriptingLanguage = engineConfiguration.getDefaultExpressionLanguage();
    }
  }

  public String getDefaultScriptingLanguage() {
    return defaultScriptingLanguage;
  }

  public ScriptEngine getDefaultScriptEngine() {
    return getScriptEngineForName(null);
  }

  public ScriptEngine getScriptEngineForName(String name) {

    if (name == null || name.isEmpty()) {
      name = defaultScriptingLanguage;
    }

    name = name.toLowerCase();

    if (cachedEngines.containsKey(name)) {
      return cachedEngines.get(name);
    }
    else {
      ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(name);
      if (scriptEngine != null) {
        configureScriptEngine(scriptEngine);
        return scriptEngine;
      }
      else {
        throw LOG.unableToFindScriptEngineForName(name);
      }
    }

  }

  protected void configureScriptEngine(ScriptEngine scriptEngine) {
    // do nothing per default
  }

}
