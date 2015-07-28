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
package org.camunda.bpm.engine.impl.scripting;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.scriptengine.DmnCompiledScript;
import org.camunda.bpm.dmn.scriptengine.DmnScriptEngine;
import org.camunda.bpm.dmn.scriptengine.DmnScriptEngineFactory;
import org.camunda.bpm.engine.ScriptCompilationException;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

public class DmnExecutableScript extends CompiledExecutableScript {

  protected DmnExecutableScript(DmnDecision decision) {
    super(DmnScriptEngineFactory.NAME);
    compiledScript = compileDecision(decision);
  }

  protected DmnCompiledScript compileDecision(DmnDecision decision) {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();

    try{
      return dmnScriptEngine.compile(decision);
    } catch (ScriptException e) {
      throw new ScriptCompilationException("Unable to compile decision '" + decision.getKey() + "': " + e.getMessage(), e);
    }
  }

  protected DmnScriptEngine getDmnScriptEngine() {
    ScriptingEngines scriptingEngines = Context.getProcessEngineConfiguration().getScriptingEngines();
    ScriptEngine scriptEngine = scriptingEngines.getScriptEngineForLanguage(language);
    if (scriptEngine != null) {
      return (DmnScriptEngine) scriptEngine;
    }
    else {
      throw new ScriptCompilationException("Unable to find DMN script engine");
    }
  }


}
