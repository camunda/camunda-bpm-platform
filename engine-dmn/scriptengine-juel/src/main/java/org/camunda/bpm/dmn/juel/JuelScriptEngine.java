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

package org.camunda.bpm.dmn.juel;

import java.io.Reader;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.camunda.commons.utils.IoUtil;
import org.camunda.commons.utils.StringUtil;

import de.odysseus.el.ExpressionFactoryImpl;

public class JuelScriptEngine extends AbstractScriptEngine implements Compilable {

  public static final String EL_CONTEXT_ATTRIBUTE = "elContext";

  protected ScriptEngineFactory scriptEngineFactory;
  protected ExpressionFactoryImpl expressionFactory;

  public JuelScriptEngine() {
    this(null);
  }

  public JuelScriptEngine(ScriptEngineFactory scriptEngineFactory) {
    this.scriptEngineFactory = scriptEngineFactory;
    this.expressionFactory = new ExpressionFactoryImpl();
  }

  public CompiledScript compile(String script) throws ScriptException {
    return new JuelCompiledScript(this, script);
  }

  public CompiledScript compile(Reader reader) throws ScriptException {
    String script = IoUtil.readerAsString(reader);
    return compile(script);
  }

  public Object eval(String script, ScriptContext context) throws ScriptException {
    ValueExpression expression = createExpression(script, context);
    return evaluateExpression(expression, context);
  }

  public Object eval(Reader reader, ScriptContext context) throws ScriptException {
    String script = IoUtil.readerAsString(reader);
    return eval(script, context);
  }

  public Bindings createBindings() {
    return new SimpleBindings();
  }

  public ScriptEngineFactory getFactory() {
    synchronized (this) {
      if (scriptEngineFactory == null) {
        scriptEngineFactory = new JuelScriptEngineFactory();
      }
    }
    return scriptEngineFactory;
  }

  public ELContext createElContext(ScriptContext context) {
    ELContext elContext;

    Object elContextAttribute = context.getAttribute(EL_CONTEXT_ATTRIBUTE);
    if (elContextAttribute instanceof ELContext) {
      elContext = (ELContext) elContextAttribute;
    }
    else {
      elContext = new JuelScriptElContext(context, expressionFactory);
      context.setAttribute(EL_CONTEXT_ATTRIBUTE, elContext, ScriptContext.ENGINE_SCOPE);
    }

    return elContext;
  }

  public ValueExpression createExpression(String expression, ScriptContext context) throws ScriptException {
    ELContext elContext = createElContext(context);
    try {
      if (!StringUtil.isExpression(expression)) {
        expression = "${" + expression + "}";
      }
      return expressionFactory.createValueExpression(elContext, expression, Object.class);
    }
    catch (ELException e) {
      throw new ScriptException(e);
    }
  }

  public Object evaluateExpression(ValueExpression expression, ScriptContext context) throws ScriptException {
    ELContext elContext = createElContext(context);
    try {
      return expression.getValue(elContext);
    }
    catch (ELException e) {
      throw new ScriptException(e);
    }
  }

}
