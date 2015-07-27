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

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.commons.utils.IoUtil;

public class DmnScriptEngine extends AbstractScriptEngine implements Compilable {

  public static final String DECISION_ID_ATTRIBUTE = "decisionKey";

  protected ScriptEngineFactory scriptEngineFactory;
  protected DmnEngine dmnEngine;

  public DmnScriptEngine() {
    this((ScriptEngineFactory) null);
  }

  public DmnScriptEngine(DmnEngine dmnEngine) {
    this(null, dmnEngine);
  }

  public DmnScriptEngine(ScriptEngineFactory scriptEngineFactory) {
    this.scriptEngineFactory = scriptEngineFactory;
    this.dmnEngine = new DmnEngineConfigurationImpl().buildEngine();
  }

  public DmnScriptEngine(ScriptEngineFactory scriptEngineFactory, DmnEngine dmnEngine) {
    this.scriptEngineFactory = scriptEngineFactory;
    this.dmnEngine = dmnEngine;
  }

  public DmnEngine getDmnEngine() {
    return dmnEngine;
  }

  public void setDmnEngine(DmnEngine dmnEngine) {
    this.dmnEngine = dmnEngine;
  }

  public void setDmnEngine(DmnEngineConfiguration dmnEngineConfiguration) {
    this.dmnEngine = dmnEngineConfiguration.buildEngine();
  }

  public DmnCompiledScript compile(String script) throws ScriptException {
    DmnDecisionModel dmnDecisionModel = parseDmnDecisionModel(script);
    return new DmnCompiledScript(this, dmnDecisionModel);
  }

  public DmnCompiledScript compile(Reader reader) throws ScriptException {
    String script = getScriptFromReader(reader);
    return compile(script);
  }

  @Override
  public DmnDecisionResult eval(String script) throws ScriptException {
    return eval(script, (String) null);
  }

  public DmnDecisionResult eval(String script, String decisionKey) throws ScriptException {
    return eval(script, decisionKey, context);
  }

  @Override
  public DmnDecisionResult eval(String script, Bindings bindings) throws ScriptException {
    return eval(script, null, bindings);
  }

  public DmnDecisionResult eval(String script, String decisionKey, Bindings bindings) throws ScriptException {
    ScriptContext scriptContext = getScriptContext(bindings);
    return eval(script, decisionKey, scriptContext);
  }

  @Override
  public DmnDecisionResult eval(String script, ScriptContext context) throws ScriptException {
    String decisionKey = getDecisionKey(context);
    return eval(script, decisionKey, context);
  }

  public DmnDecisionResult eval(String script, String decisionKey, ScriptContext context) throws ScriptException {
    Map<String, Object> variables = getVariables(context);

    DmnDecisionModel dmnDecisionModel = parseDmnDecisionModel(script);
    DmnDecision decision;
    if (decisionKey != null) {
      decision = dmnDecisionModel.getDecision(decisionKey);
    }
    else {
      decision = dmnDecisionModel.getDecisions().get(0);
    }

    try {
      return dmnEngine.evaluate(decision, variables);
    }
    catch (Exception e) {
      throw new ScriptException(e);
    }
  }

  @Override
  public DmnDecisionResult eval(Reader reader) throws ScriptException {
    return eval(reader, (String) null);
  }

  public DmnDecisionResult eval(Reader reader, String decisionKey) throws ScriptException {
    return eval(reader, decisionKey, context);
  }

  @Override
  public DmnDecisionResult eval(Reader reader, Bindings bindings) throws ScriptException {
    return eval(reader, null, bindings);
  }

  public DmnDecisionResult eval(Reader reader, String decisionKey, Bindings bindings) throws ScriptException {
    ScriptContext scriptContext = getScriptContext(bindings);
    return eval(reader, decisionKey, scriptContext);
  }

  public DmnDecisionResult eval(Reader reader, ScriptContext context) throws ScriptException {
    String decisionKey = getDecisionKey(context);
    return eval(reader, decisionKey, context);
  }

  public DmnDecisionResult eval(Reader reader, String decisionKey, ScriptContext context) throws ScriptException {
    String script = getScriptFromReader(reader);
    return eval(script, decisionKey, context);
  }

  public Bindings createBindings() {
    return new SimpleBindings();
  }

  public ScriptEngineFactory getFactory() {
    if (scriptEngineFactory == null) {
      synchronized (this) {
        if (scriptEngineFactory == null) {
          scriptEngineFactory = new DmnScriptEngineFactory();
        }
      }
    }
    return scriptEngineFactory;
  }

  protected String getScriptFromReader(Reader reader) {
    return IoUtil.readerAsString(reader);
  }

  protected DmnDecisionModel parseDmnDecisionModel(String script) {
    InputStream inputStream = IoUtil.stringAsInputStream(script);
    return dmnEngine.parseDecisionModel(inputStream);
  }

  protected Map<String, Object> getVariables(ScriptContext context) {
    Map<String, Object> variables = new HashMap<String, Object>();

    Integer[] scopes = new Integer[] {ScriptContext.GLOBAL_SCOPE, ScriptContext.ENGINE_SCOPE};

    for (Integer scope : scopes) {
      Bindings bindings = context.getBindings(scope);
      variables.putAll(bindings);
    }

    return variables;
  }

  protected String getDecisionKey(ScriptContext context) {
    String decisionKey = null;
    if (context != null) {
      decisionKey = (String) getScriptContextAttribute(context, DECISION_ID_ATTRIBUTE);
    }
    return decisionKey;
  }

  @Override
  /* Hack to access this function from DmnCompiledScript */
  protected ScriptContext getScriptContext(Bindings bindings) {
    return super.getScriptContext(bindings);
  }

  protected Object getScriptContextAttribute(ScriptContext context, String name) {
    Object attribute = context.getAttribute(name);
    if (attribute == null) {
      attribute = getContext().getAttribute(name);
    }
    return attribute;
  }

}
