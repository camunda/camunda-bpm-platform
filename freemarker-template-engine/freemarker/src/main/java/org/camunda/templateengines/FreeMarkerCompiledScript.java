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

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.*;
import javax.script.*;

/**
 * Wraps a {@link Template Freemaker template} as a {@link CompiledScript} to
 * cache the used template.
 *
 * @author Sebastian Menski
 */
public class FreeMarkerCompiledScript extends CompiledScript {

  protected final ScriptEngine scriptEngine;
  protected final Template template;

  public FreeMarkerCompiledScript(ScriptEngine scriptEngine, String script, Configuration configuration) throws ScriptException {
    this(scriptEngine, "unknown", script, configuration);
  }

  public FreeMarkerCompiledScript(ScriptEngine scriptEngine, Reader script, Configuration configuration) throws ScriptException {
    this(scriptEngine, "unknown", script, configuration);
  }

  public FreeMarkerCompiledScript(ScriptEngine scriptEngine, String filename, String script, Configuration configuration) throws ScriptException {
    this(scriptEngine, filename, new StringReader(script), configuration);
  }

  public FreeMarkerCompiledScript(ScriptEngine scriptEngine, String filename, Reader script, Configuration configuration) throws ScriptException {
    this.scriptEngine = scriptEngine;
    try {
      template = new Template(filename, script, configuration);
    } catch (IOException e) {
      throw new ScriptException(e);
    }
  }

  public Object eval(ScriptContext context) throws ScriptException {
    Writer writer = new StringWriter();
    Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
    try {
      template.process(bindings, writer);
      writer.flush();
    } catch (Exception e) {
      throw new ScriptException(e);
    }
    return writer.toString();
  }

  public ScriptEngine getEngine() {
    return scriptEngine;
  }
}
