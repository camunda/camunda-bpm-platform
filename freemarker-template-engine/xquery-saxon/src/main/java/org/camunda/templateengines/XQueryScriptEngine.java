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

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.transform.OutputKeys;

import net.sf.saxon.Configuration;

/**
 * 
 * JSR 223 compatible wrapper for the Saxon XQuery processor.
 *
 */
public class XQueryScriptEngine extends AbstractScriptEngine implements Compilable {

  protected ScriptEngineFactory scriptEngineFactory;
  protected Configuration configuration;
  protected Properties properties;

  public XQueryScriptEngine() {
    this(null);
  }

  public XQueryScriptEngine(ScriptEngineFactory scriptEngineFactory) {
    this.scriptEngineFactory = scriptEngineFactory;
  }

  public Object eval(String script, ScriptContext context) throws ScriptException {
    return eval(new StringReader(script), context);
  }

  public Object eval(Reader script, ScriptContext context) throws ScriptException {
    initConfiguration();

    Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);

    try {
      XQueryOperator operator = XQueryOperator.builder()
          .withStylesheet(script)
          .withOutputProperties(properties)
          .withConfiguration(configuration)
          .build();

      return operator.evaluateToString(bindings);
    } catch (Exception e) {
      throw new ScriptException(e);
    }
  }

  public Bindings createBindings() {
    return new SimpleBindings();
  }

  public ScriptEngineFactory getFactory() {
    if (scriptEngineFactory == null) {
      synchronized (this) {
        if (scriptEngineFactory == null) {
          scriptEngineFactory = new XQueryScriptEngineFactory();
        }
      }
    }
    return scriptEngineFactory;
  }

  public CompiledScript compile(String script) throws ScriptException {
    return compile(new StringReader(script));
  }

  public void initConfiguration() {
    if (configuration == null) {
      synchronized (this) {
        if (configuration == null) {
          configuration = new Configuration();
        }
      }
    }

    if (properties == null) {
      synchronized (this) {
        if (properties == null) {
          properties = new Properties();
          properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
          properties.setProperty(OutputKeys.INDENT, "no");
        }
      }
    }
  }

  public CompiledScript compile(Reader script) throws ScriptException {
    initConfiguration();

    return new XQueryCompiledScript(this, script, configuration, properties);
  }
}
