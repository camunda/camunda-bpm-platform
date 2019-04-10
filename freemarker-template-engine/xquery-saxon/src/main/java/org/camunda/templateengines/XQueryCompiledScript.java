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
package org.camunda.templateengines;

import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import net.sf.saxon.Configuration;

/**
 * 
 * Wraps a {@link XQueryOperator} as a {@link CompiledScript} to cache the used template.
 *
 */
public class XQueryCompiledScript extends CompiledScript {

  protected final ScriptEngine scriptEngine;
  protected final XQueryOperator operator;

  public XQueryCompiledScript(ScriptEngine scriptEngine, String script, Configuration configuration, Properties properties) throws ScriptException {
    this(scriptEngine, new StringReader(script), configuration, properties);
  }

  public XQueryCompiledScript(ScriptEngine scriptEngine, Reader script, Configuration configuration, Properties properties) throws ScriptException {
    this.scriptEngine = scriptEngine;
    try {
      operator = XQueryOperator.builder()
          .withStylesheet(script)
          .withConfiguration(configuration)
          .withOutputProperties(properties)
          .build();
    } catch (Exception e) {
      throw new ScriptException(e);
    }
  }

  public Object eval(ScriptContext context) throws ScriptException {
    Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
    try {
      return operator.evaluateToString(bindings);
    } catch (Exception e) {
      throw new ScriptException(e);
    }
  }

  public ScriptEngine getEngine() {
    return scriptEngine;
  }
}
