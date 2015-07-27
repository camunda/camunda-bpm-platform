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

package org.camunda.bpm.dmn.scriptengine;

import java.util.Map;
import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;

public class DmnCompiledScript extends CompiledScript {

  protected DmnScriptEngine dmnScriptEngine;
  protected DmnEngine dmnEngine;

  protected DmnDecisionModel dmnDecisionModel;
  protected DmnDecision dmnDecision;

  public DmnCompiledScript(DmnScriptEngine dmnScriptEngine, DmnDecisionModel dmnDecisionModel) {
    this.dmnScriptEngine = dmnScriptEngine;
    this.dmnEngine = dmnScriptEngine.getDmnEngine();
    this.dmnDecisionModel = dmnDecisionModel;
  }

  public DmnCompiledScript(DmnScriptEngine dmnScriptEngine, DmnDecision dmnDecision) {
    this.dmnScriptEngine = dmnScriptEngine;
    this.dmnEngine = dmnScriptEngine.getDmnEngine();
    this.dmnDecision = dmnDecision;
  }

  @Override
  public DmnDecisionResult eval() throws ScriptException {
    return eval((String) null);
  }

  public DmnDecisionResult eval(String decisionKey) throws ScriptException {
    return eval(decisionKey, getEngine().getContext());
  }

  @Override
  public DmnDecisionResult eval(Bindings bindings) throws ScriptException {
    return eval((String) null, bindings);
  }

  public DmnDecisionResult eval(String decisionKey, Bindings bindings) throws ScriptException {
    ScriptContext scriptContext = dmnScriptEngine.getScriptContext(bindings);
    return eval(decisionKey, scriptContext);
  }

  public DmnDecisionResult eval(ScriptContext context) throws ScriptException {
    Map<String, Object> variables = dmnScriptEngine.getVariables(context);
    String decisionKey = dmnScriptEngine.getDecisionKey(context);
    return eval(decisionKey, variables);
  }

  public DmnDecisionResult eval(String decisionKey, ScriptContext context) throws ScriptException {
    Map<String, Object> variables = dmnScriptEngine.getVariables(context);
    return eval(decisionKey, variables);
  }

  public DmnDecisionResult eval(Map<String, Object> variables) throws ScriptException {
    return eval((String) null, variables);
  }

  public DmnDecisionResult eval(String decisionKey, Map<String, Object> variables) throws ScriptException {
    DmnDecision decision = getDmnDecision(decisionKey);
    return eval(decision, variables);
  }

  public DmnDecisionResult eval(DmnDecision decision, Map<String, Object> variables) throws ScriptException {
    try {
      return dmnEngine.evaluate(decision, variables);
    }
    catch (Exception e) {
      throw new ScriptException(e);
    }
  }

  public ScriptEngine getEngine() {
    return dmnScriptEngine;
  }

  public DmnDecision getDmnDecision(String decisionKey) {
    if (dmnDecisionModel != null) {
      if (decisionKey != null) {
        return dmnDecisionModel.getDecision(decisionKey);
      }
      else {
        return dmnDecisionModel.getDecisions().get(0);
      }
    }
    else {
      return dmnDecision;
    }
  }

}
