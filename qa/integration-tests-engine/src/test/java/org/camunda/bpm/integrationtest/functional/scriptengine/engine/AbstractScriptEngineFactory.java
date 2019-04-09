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
package org.camunda.bpm.integrationtest.functional.scriptengine.engine;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractScriptEngineFactory implements ScriptEngineFactory {

  protected String name;
  protected String version;
  protected ScriptEngineBehavior behavior;

  public AbstractScriptEngineFactory(String name, String version, ScriptEngineBehavior behavior) {
    this.name = name;
    this.version = version;
    this.behavior = behavior;
  }

  public String getEngineName() {
    return name;
  }

  public String getEngineVersion() {
    return version;
  }

  public List<String> getExtensions() {
    return Collections.emptyList();
  }

  public List<String> getMimeTypes() {
    return Collections.emptyList();
  }

  public List<String> getNames() {
    return Arrays.asList(name);
  }

  public String getLanguageName() {
    return name;
  }

  public String getLanguageVersion() {
    return version;
  }

  public Object getParameter(String key) {
    if (key.equals("THREADING")) {
      return "MULTITHREADED";
    }
    return null;
  }

  public String getMethodCallSyntax(String obj, String m, String... args) {
    throw new UnsupportedOperationException("getMethodCallSyntax");
  }

  public String getOutputStatement(String toDisplay) {
    throw new UnsupportedOperationException("getOutputStatement");
  }

  public String getProgram(String... statements) {
    throw new UnsupportedOperationException("getProgram");
  }

  public ScriptEngine getScriptEngine() {
    return new AbstractScriptEngine() {

      @Override
      public Object eval(String script, ScriptContext context) throws ScriptException {
        return behavior.eval(script, context);
      }

      @Override
      public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return null;
      }

      @Override
      public Bindings createBindings() {
        return new SimpleBindings();
      }

      @Override
      public ScriptEngineFactory getFactory() {
        return AbstractScriptEngineFactory.this;
      }

    };
  }


  public static interface ScriptEngineBehavior {
    public Object eval(String script, ScriptContext context);
  }
}
