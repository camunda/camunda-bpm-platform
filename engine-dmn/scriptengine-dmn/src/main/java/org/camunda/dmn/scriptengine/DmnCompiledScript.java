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

package org.camunda.dmn.scriptengine;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnDecisionModel;
import org.camunda.dmn.engine.DmnDecisionResult;
import org.camunda.dmn.engine.context.DmnDecisionContext;

public class DmnCompiledScript extends CompiledScript {

  protected DmnScriptEngine dmnScriptEngine;
  protected DmnDecisionModel dmnDecisionModel;

  public DmnCompiledScript(DmnScriptEngine dmnScriptEngine, DmnDecisionModel dmnDecisionModel) {
    this.dmnScriptEngine = dmnScriptEngine;
    this.dmnDecisionModel = dmnDecisionModel;
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
    return eval(null, bindings);
  }

  public DmnDecisionResult eval(String decisionKey, Bindings bindings) throws ScriptException {
    ScriptContext scriptContext = dmnScriptEngine.getScriptContext(bindings);
    return eval(decisionKey, scriptContext);
  }

  public DmnDecisionResult eval(ScriptContext context) throws ScriptException {
    DmnDecisionContext decisionContext = dmnScriptEngine.getDmnDecisionContext(context);
    String decisionKey = dmnScriptEngine.getDecisionKey(context);
    return eval(decisionKey, decisionContext);
  }

  public DmnDecisionResult eval(String decisionKey, ScriptContext context) throws ScriptException {
    DmnDecisionContext decisionContext = dmnScriptEngine.getDmnDecisionContext(context);
    return eval(decisionKey, decisionContext);
  }

  public DmnDecisionResult eval(DmnDecisionContext context) throws ScriptException {
    try {
      return dmnDecisionModel.getDecisions().get(0).evaluate(context);
    }
    catch (Exception e) {
      throw new ScriptException(e);
    }
  }

  public DmnDecisionResult eval(String decisionKey, DmnDecisionContext context) throws ScriptException {
    DmnDecision decision;
    if (decisionKey != null) {
      decision =  dmnDecisionModel.getDecision(decisionKey);
    }
    else {
      decision = dmnDecisionModel.getDecisions().get(0);
    }
    try {
      return decision.evaluate(context);
    }
    catch (Exception e) {
      throw new ScriptException(e);
    }
  }

  public ScriptEngine getEngine() {
    return dmnScriptEngine;
  }

}
