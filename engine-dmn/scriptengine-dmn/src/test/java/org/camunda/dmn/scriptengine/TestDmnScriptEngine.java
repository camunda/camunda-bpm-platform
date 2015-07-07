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

import static org.camunda.dmn.engine.test.asserts.DmnAssertions.assertThat;

import java.io.Reader;
import java.io.StringReader;
import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.camunda.commons.utils.IoUtil;
import org.camunda.dmn.engine.DmnDecisionResult;
import org.camunda.dmn.engine.context.DmnDecisionContext;
import org.camunda.dmn.engine.context.DmnVariableContext;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDmnScriptEngine {

  public static final String EXAMPLE_DMN = "org/camunda/dmn/scriptengine/Example.dmn";
  public static final String EXAMPLE_DMN_SCRIPT = IoUtil.fileAsString(EXAMPLE_DMN);
  public static final String FIRST_DECISION = "decision1";
  public static final String SECOND_DECISION = "decision2";

  protected static ScriptEngineManager scriptEngineManager;

  @BeforeClass
  public static void createScriptEngineManager() {
    scriptEngineManager = new ScriptEngineManager();
  }

  @Test
  public void shouldFindScriptEngineByName() {
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("dmn");
    assertScriptEngine(scriptEngine);

    scriptEngine = scriptEngineManager.getEngineByName("Dmn");
    assertScriptEngine(scriptEngine);

    scriptEngine = scriptEngineManager.getEngineByName("DMN");
    assertScriptEngine(scriptEngine);
  }

  @Test
  public void shouldFindScriptEngineByExtension() {
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByExtension("dmn");
    assertScriptEngine(scriptEngine);

    scriptEngine = scriptEngineManager.getEngineByExtension("dmn10.xml");
    assertScriptEngine(scriptEngine);
  }

  @Test
  public void shouldCompileScript() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    CompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);
    assertThat(compiledScript).isNotNull();

    compiledScript = dmnScriptEngine.compile(getExampleDmnReader());
    assertThat(compiledScript).isNotNull();
  }

  @Test
  public void shouldEvalFirstDecision() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();

    Bindings bindings = dmnScriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
    bindings.put("status", "bronze");

    DmnDecisionResult result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader());
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    bindings.put("status", "gold");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader());
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");
  }

  @Test
  public void shouldEvalFirstDecisionWithBindings() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    DmnDecisionResult result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    bindings.put("status", "gold");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");
  }

  @Test
  public void shouldEvalFirstDecisionWithScriptContext() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    ScriptContext scriptContext = dmnScriptEngine.getScriptContext(bindings);

    DmnDecisionResult result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    bindings.put("status", "gold");
    scriptContext = dmnScriptEngine.getScriptContext(bindings);

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");
  }

  @Test
  public void shouldEvalDecisionById() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();

    Bindings bindings = dmnScriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
    bindings.put("status", "bronze");

    DmnDecisionResult result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, FIRST_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), FIRST_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, SECOND_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), SECOND_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    bindings.put("status", "gold");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, FIRST_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), FIRST_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, SECOND_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), SECOND_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");
  }

  @Test
  public void shouldEvalDecisionByIdWithBindings() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    DmnDecisionResult result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, FIRST_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), FIRST_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, SECOND_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), SECOND_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    bindings.put("status", "gold");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, FIRST_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), FIRST_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, SECOND_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), SECOND_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");
  }

  @Test
  public void shouldEvalDecisionByIdInScriptContext() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    ScriptContext scriptContext = dmnScriptEngine.getScriptContext(bindings);
    scriptContext.setAttribute(DmnScriptEngine.DECISION_ID_ATTRIBUTE, FIRST_DECISION, ScriptContext.ENGINE_SCOPE);

    DmnDecisionResult result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    scriptContext.setAttribute(DmnScriptEngine.DECISION_ID_ATTRIBUTE, SECOND_DECISION, ScriptContext.ENGINE_SCOPE);

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    bindings.put("status", "gold");
    scriptContext = dmnScriptEngine.getScriptContext(bindings);

    scriptContext.setAttribute(DmnScriptEngine.DECISION_ID_ATTRIBUTE, FIRST_DECISION, ScriptContext.ENGINE_SCOPE);

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    scriptContext.setAttribute(DmnScriptEngine.DECISION_ID_ATTRIBUTE, SECOND_DECISION, ScriptContext.ENGINE_SCOPE);

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");
  }

  @Test
  public void shouldEvalDecisionByIdWithScriptContext() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    ScriptContext scriptContext = dmnScriptEngine.getScriptContext(bindings);

    DmnDecisionResult result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, FIRST_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), FIRST_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, SECOND_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), SECOND_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    bindings.put("status", "gold");
    scriptContext = dmnScriptEngine.getScriptContext(bindings);

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, FIRST_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), FIRST_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = dmnScriptEngine.eval(EXAMPLE_DMN_SCRIPT, SECOND_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = dmnScriptEngine.eval(getExampleDmnReader(), SECOND_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");
  }

  @Test
  public void shouldEvalFirstDecisionOfCompiledScript() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    DmnCompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);

    Bindings bindings = dmnScriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
    bindings.put("status", "bronze");

    DmnDecisionResult result = compiledScript.eval();
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    bindings.put("status", "gold");

    result = compiledScript.eval();
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");
  }

  @Test
  public void shouldEvalFirstDecisionOfCompiledScriptWithBindings() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    DmnCompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    DmnDecisionResult result = compiledScript.eval(bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    bindings.put("status", "gold");

    result = compiledScript.eval(bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");
  }

  @Test
  public void shouldEvalFirstDecisionOfCompiledScriptWithScriptContext() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    DmnCompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    ScriptContext scriptContext = dmnScriptEngine.getScriptContext(bindings);

    DmnDecisionResult result = compiledScript.eval(scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    bindings.put("status", "gold");
    scriptContext = dmnScriptEngine.getScriptContext(bindings);

    result = compiledScript.eval(scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");
  }

  @Test
  public void shouldEvalFirstDecisionOfCompiledScriptWithDecisionContext() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    DmnCompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);

    DmnDecisionContext decisionContext = dmnScriptEngine.getDmnDecisionContext(dmnScriptEngine.getContext());
    DmnVariableContext variableContext = decisionContext.getVariableContextChecked();

    variableContext.setVariable("status", "bronze");

    DmnDecisionResult result = compiledScript.eval(decisionContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    variableContext.setVariable("status", "gold");

    result = compiledScript.eval(decisionContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");
  }

  @Test
  public void shouldEvalDecisionOfCompiledScriptById() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    DmnCompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);

    Bindings bindings = dmnScriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
    bindings.put("status", "bronze");

    DmnDecisionResult result = compiledScript.eval(FIRST_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = compiledScript.eval(SECOND_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    bindings.put("status", "gold");

    result = compiledScript.eval(FIRST_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = compiledScript.eval(SECOND_DECISION);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");
  }

  @Test
  public void shouldEvalDecisionOfCompiledScriptByIdWithBindings() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    DmnCompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    DmnDecisionResult result = compiledScript.eval(FIRST_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = compiledScript.eval(SECOND_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    bindings.put("status", "gold");

    result = compiledScript.eval(FIRST_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = compiledScript.eval(SECOND_DECISION, bindings);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");
  }

  @Test
  public void shouldEvalDecisionOfCompiledScriptByIdInScriptContext() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    DmnCompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    ScriptContext scriptContext = dmnScriptEngine.getScriptContext(bindings);
    scriptContext.setAttribute(DmnScriptEngine.DECISION_ID_ATTRIBUTE, FIRST_DECISION, ScriptContext.ENGINE_SCOPE);

    DmnDecisionResult result = compiledScript.eval(scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    scriptContext.setAttribute(DmnScriptEngine.DECISION_ID_ATTRIBUTE, SECOND_DECISION, ScriptContext.ENGINE_SCOPE);

    result = compiledScript.eval(scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    bindings.put("status", "gold");
    scriptContext = dmnScriptEngine.getScriptContext(bindings);

    scriptContext.setAttribute(DmnScriptEngine.DECISION_ID_ATTRIBUTE, FIRST_DECISION, ScriptContext.ENGINE_SCOPE);

    result = compiledScript.eval(scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    scriptContext.setAttribute(DmnScriptEngine.DECISION_ID_ATTRIBUTE, SECOND_DECISION, ScriptContext.ENGINE_SCOPE);

    result = compiledScript.eval(scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");
  }

  @Test
  public void shouldEvalDecisionOfCompiledScriptByIdWithScriptContext() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    DmnCompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);

    Bindings bindings = dmnScriptEngine.createBindings();
    bindings.put("status", "bronze");

    ScriptContext scriptContext = dmnScriptEngine.getScriptContext(bindings);

    DmnDecisionResult result = compiledScript.eval(FIRST_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = compiledScript.eval(SECOND_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    bindings.put("status", "gold");
    scriptContext = dmnScriptEngine.getScriptContext(bindings);

    result = compiledScript.eval(FIRST_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = compiledScript.eval(SECOND_DECISION, scriptContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");
  }

  @Test
  public void shouldEvalDecisionOfCompiledScriptByIdWithDecisionContext() throws ScriptException {
    DmnScriptEngine dmnScriptEngine = getDmnScriptEngine();
    DmnCompiledScript compiledScript = dmnScriptEngine.compile(EXAMPLE_DMN_SCRIPT);

    DmnDecisionContext decisionContext = dmnScriptEngine.getDmnDecisionContext(dmnScriptEngine.getContext());
    DmnVariableContext variableContext = decisionContext.getVariableContextChecked();

    variableContext.setVariable("status", "bronze");

    DmnDecisionResult result = compiledScript.eval(FIRST_DECISION, decisionContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");

    result = compiledScript.eval(SECOND_DECISION, decisionContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    variableContext.setVariable("status", "gold");

    result = compiledScript.eval(FIRST_DECISION, decisionContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "ok");

    result = compiledScript.eval(SECOND_DECISION, decisionContext);
    assertThat(result).hasSingleOutput().hasEntry("result", "notok");
  }


  protected DmnScriptEngine getDmnScriptEngine() {
    return (DmnScriptEngine) scriptEngineManager.getEngineByName("DMN");
  }

  protected void assertScriptEngine(ScriptEngine scriptEngine) {
    assertThat(scriptEngine)
      .isNotNull()
      .isInstanceOf(DmnScriptEngine.class);
  }

  protected Reader getExampleDmnReader() {
    return new StringReader(EXAMPLE_DMN_SCRIPT);
  }

}
