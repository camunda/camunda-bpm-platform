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

import java.io.InputStream;
import java.io.Reader;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.camunda.commons.utils.IoUtil;
import org.camunda.dmn.engine.DmnDecisionModel;
import org.camunda.dmn.engine.DmnDecisionResult;
import org.camunda.dmn.engine.DmnEngine;
import org.camunda.dmn.engine.DmnEngineConfiguration;
import org.camunda.dmn.engine.context.DmnDecisionContext;
import org.camunda.dmn.engine.context.DmnVariableContext;
import org.camunda.dmn.engine.impl.DmnEngineConfigurationImpl;

public class DmnScriptEngine extends AbstractScriptEngine implements Compilable {

  public static final String DECISION_ID_ATTRIBUTE = "decisionId";
  public static final String SCRIPT_ENGINE_MANAGER_ATTRIBUTE = "scriptEngineManager";

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

  public DmnDecisionResult eval(String script, String decisionId) throws ScriptException {
    return eval(script, decisionId, context);
  }

  @Override
  public DmnDecisionResult eval(String script, Bindings bindings) throws ScriptException {
    return eval(script, null, bindings);
  }

  public DmnDecisionResult eval(String script, String decisionId, Bindings bindings) throws ScriptException {
    ScriptContext scriptContext = getScriptContext(bindings);
    return eval(script, decisionId, scriptContext);
  }

  @Override
  public DmnDecisionResult eval(String script, ScriptContext context) throws ScriptException {
    String decisionId = getDecisionId(context);
    return eval(script, decisionId, context);
  }

  public DmnDecisionResult eval(String script, String decisionId, ScriptContext context) throws ScriptException {
    DmnDecisionContext decisionContext = getDmnDecisionContext(context);
    return eval(script, decisionId, decisionContext);
  }

  public DmnDecisionResult eval(String script, DmnDecisionContext context) throws ScriptException {
    return eval(script, null, context);
  }

  public DmnDecisionResult eval(String script, String decisionId, DmnDecisionContext context) throws ScriptException {
    DmnDecisionModel dmnDecisionModel = parseDmnDecisionModel(script);
    if (decisionId != null) {
      return dmnDecisionModel.evaluate(decisionId, context);
    }
    else {
      return dmnDecisionModel.evaluate(context);
    }
  }

  @Override
  public DmnDecisionResult eval(Reader reader) throws ScriptException {
    return eval(reader, (String) null);
  }

  public DmnDecisionResult eval(Reader reader, String decisionId) throws ScriptException {
    return eval(reader, decisionId, context);
  }

  @Override
  public DmnDecisionResult eval(Reader reader, Bindings bindings) throws ScriptException {
    return eval(reader, null, bindings);
  }

  public DmnDecisionResult eval(Reader reader, String decisionId, Bindings bindings) throws ScriptException {
    ScriptContext scriptContext = getScriptContext(bindings);
    return eval(reader, decisionId, scriptContext);
  }

  public DmnDecisionResult eval(Reader reader, ScriptContext context) throws ScriptException {
    String decisionId = getDecisionId(context);
    return eval(reader, decisionId, context);
  }

  public DmnDecisionResult eval(Reader reader, String decisionId, ScriptContext context) throws ScriptException {
    DmnDecisionContext decisionContext = getDmnDecisionContext(context);
    return eval(reader, decisionId, decisionContext);
  }

  public DmnDecisionResult eval(Reader reader, DmnDecisionContext context) throws ScriptException {
    return eval(reader, null, context);
  }

  public DmnDecisionResult eval(Reader reader, String decisionId, DmnDecisionContext context) throws ScriptException {
    String script = getScriptFromReader(reader);
    return eval(script, decisionId, context);
  }

  public Bindings createBindings() {
    return new SimpleBindings();
  }

  public ScriptEngineFactory getFactory() {
    synchronized (this) {
      if (scriptEngineFactory == null) {
        scriptEngineFactory = new DmnScriptEngineFactory();
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

  protected DmnDecisionContext getDmnDecisionContext(ScriptContext context) {
    DmnDecisionContext decisionContext = createDmnDecisionContext();
    DmnVariableContext variableContext = decisionContext.getVariableContextChecked();
    Integer[] scopes = new Integer[] {ScriptContext.GLOBAL_SCOPE, ScriptContext.ENGINE_SCOPE};

    for (Integer scope : scopes) {
      Bindings bindings = context.getBindings(scope);
      variableContext.setVariables(bindings);
    }

    if (context != null) {
      // set script engine manager
      ScriptEngineManager scriptEngineManager = (ScriptEngineManager) getScriptContextAttribute(context, SCRIPT_ENGINE_MANAGER_ATTRIBUTE);
      if (scriptEngineManager != null) {
        decisionContext.getScriptContext().setScriptEngineManager(scriptEngineManager);
      }
    }

    return decisionContext;
  }

  protected String getDecisionId(ScriptContext context) {
    String decisionId = null;
    if (context != null) {
      decisionId = (String) getScriptContextAttribute(context, DECISION_ID_ATTRIBUTE);
    }
    return decisionId;
  }

  protected DmnDecisionContext createDmnDecisionContext() {
    return dmnEngine.getConfiguration().getDmnContextFactory().createDecisionContext();
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
